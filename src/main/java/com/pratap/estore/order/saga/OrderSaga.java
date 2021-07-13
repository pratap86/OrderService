package com.pratap.estore.order.saga;

import com.pratap.estore.order.core.events.OrderCreatedEvent;
import com.pratap.estore.shared.commands.ProductReservedCommand;
import com.pratap.estore.shared.events.ProductReservedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
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

        // process user payment here
        log.info("ProductReservedEvent handled for orderId={} and productId={}", productReservedEvent.getOrderId(), productReservedEvent.getProductId());
    }
}
