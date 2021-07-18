package com.pratap.estore.order.saga;

import com.pratap.estore.order.command.commands.ApproveOrderCommand;
import com.pratap.estore.order.core.events.OrderApprovedEvent;
import com.pratap.estore.order.core.events.OrderCreatedEvent;
import com.pratap.estore.shared.commands.ProcessPaymentCommand;
import com.pratap.estore.shared.commands.ProductReservedCommand;
import com.pratap.estore.shared.events.PaymentProcessedEvent;
import com.pratap.estore.shared.events.ProductReservedEvent;
import com.pratap.estore.shared.model.User;
import com.pratap.estore.shared.query.FetchUserPaymentDetailsQuery;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Pratap Narayan
 */
@Saga
@Slf4j
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    private void handle(OrderCreatedEvent orderCreatedEvent){

        ProductReservedCommand productReservedCommand = ProductReservedCommand.builder()
                .orderId(orderCreatedEvent.getOrderId())
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();

        log.info("OrderCreatedEvent handled for orderId={} and productId={}", productReservedCommand.getOrderId(), productReservedCommand.getProductId());
        commandGateway.send(productReservedCommand, ((commandMessage, commandResultMessage) -> {
            if (commandResultMessage.isExceptional()){
                //
            }
        }));

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent){
        log.info("ProductReservedEvent handled for orderId={} and productId={}", productReservedEvent.getOrderId(), productReservedEvent.getProductId());
        // process user payment here
        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery = new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());

        User userPaymentDetails = null;

        try {
            userPaymentDetails = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class)).join();
        } catch (Exception ex){
            log.error(String.valueOf(ex));

            // start compensating transaction
        }

        if (userPaymentDetails == null){
            log.info("userPaymentDetails not Found by userId={}", productReservedEvent.getUserId());
        } else {
            log.info("successfully fetched userPaymentDetails by userId={} and firstName={}", productReservedEvent.getUserId(), userPaymentDetails.getFirstName());
        }

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(Objects.requireNonNull(userPaymentDetails).getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();

        String result = null;

        try {
            result = commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.MILLISECONDS);
        } catch (Exception exception){
            log.error(exception.getMessage());
            // start compensating transaction
        }
        if (result == null){
            log.info("The ProcessPaymentCommand result is NULL, Initiating a compensating transaction");
            // start compensating transaction
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent){
        // send an ApproveOrderCommand
        log.info("send an ApproveOrderCommand, paymentProcessedEvent={}", paymentProcessedEvent);
        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());
        commandGateway.send(approveOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent){
        log.info("Order is Approved. OrderSaga is completed for orderId={}", orderApprovedEvent.getOrderId());
        //SagaLifecycle.end();
    }
}
