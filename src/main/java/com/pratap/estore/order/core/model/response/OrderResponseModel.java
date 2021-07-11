package com.pratap.estore.order.core.model.response;

import com.pratap.estore.order.core.model.constant.OrderStatus;
import lombok.Data;

@Data
public class OrderResponseModel {

    private  String orderId;
    private  OrderStatus orderStatus;
    private  String message;
}
