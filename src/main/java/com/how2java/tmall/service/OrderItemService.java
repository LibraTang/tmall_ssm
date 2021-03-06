package com.how2java.tmall.service;

import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.pojo.OrderItem;

import java.util.List;

public interface OrderItemService {
    void add(OrderItem orderItem);
    void delete(int id);
    void update(OrderItem orderItem);
    OrderItem get(int id);
    List list();
    void fill(List<Order> os);
    void fill(Order order);
    int getSaleCount(int pid);
    List<OrderItem> listByUser(int uid);
}
