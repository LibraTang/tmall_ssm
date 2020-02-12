package com.how2java.tmall.service.impl;

import com.how2java.tmall.mapper.OrderMapper;
import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.pojo.OrderExample;
import com.how2java.tmall.pojo.OrderItem;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.OrderItemService;
import com.how2java.tmall.service.OrderService;
import com.how2java.tmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserService userService;
    @Autowired
    OrderItemService orderItemService;

    @Override
    public void add(Order order) {
        orderMapper.insert(order);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public double add(Order order, List<OrderItem> ois) {
        double total = 0;
        add(order);

        //模拟订单增加发生异常时，事务管理是否起作用
        if(false)
            throw new RuntimeException();

        for(OrderItem oi : ois) {
            oi.setOid(order.getId());
            orderItemService.update(oi);
            total += oi.getProduct().getPromotePrice() * oi.getNumber();
        }

        return total;
    }

    @Override
    public void delete(int id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public Order get(int id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Order> list() {
        OrderExample example = new OrderExample();
        example.setOrderByClause("id DESC");
        List<Order> os = orderMapper.selectByExample(example);
        setUser(os);
        return os;
    }

    public void setUser(Order order) {
        int uid = order.getUid();
        User user = userService.get(uid);
        order.setUser(user);
    }

    public void setUser(List<Order> os) {
        for(Order o : os) {
            setUser(o);
        }
    }
}
