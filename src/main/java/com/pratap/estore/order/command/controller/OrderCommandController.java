package com.pratap.estore.order.command.controller;

import com.pratap.estore.order.command.commands.CreateOrderCommand;
import com.pratap.estore.order.core.model.constant.OrderStatus;
import com.pratap.estore.order.core.model.request.OrderRequestModel;
import com.pratap.estore.order.core.model.response.OrderResponseModel;
import com.pratap.estore.order.query.FindOrderQuery;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderCommandController {

    private final CommandGateway commandGateway;

    @Autowired
    public OrderCommandController(CommandGateway commandGateway){
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequestModel orderRequestModel){

        log.info("Executing createOrder() with orderRequestModel={}",orderRequestModel);

        String userId = "27b95829-4f3f-4ddf-8983-151ba010e35b";
        String orderId = UUID.randomUUID().toString();

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .orderId(orderId)
                .userId(userId)
                .productId(orderRequestModel.getProductId())
                .addressId(orderRequestModel.getAddressId())
                .quantity(orderRequestModel.getQuantity())
                .orderStatus(OrderStatus.CREATED)
                .build();


        String returnValue = commandGateway.sendAndWait(createOrderCommand);

        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }
}
