package com.pratap.estore.order.core.data;


import com.pratap.estore.order.core.model.constant.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "orders")// note order is reserved key word for h2 data base, use orders as table name
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = -8389962641255968861L;

    @Id
    @Column(unique = true)
    private String orderId;
    private String productId;
    private String userId;
    private Integer quantity;
    private String addressId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
}
