package com.pratap.estore.order.saga;

import com.pratap.estore.order.core.events.OrderCreatedEvent;
import com.pratap.estore.shared.commands.ProductReservedCommand;
import com.pratap.estore.shared.events.ProductReservedEvent;
import com.pratap.estore.shared.model.User;
import com.pratap.estore.shared.query.FetchUserPaymentDetailsQuery;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

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
    }
}
