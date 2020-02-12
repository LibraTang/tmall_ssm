package com.how2java.tmall.service;

import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.pojo.Review;

import java.util.List;

public interface ReviewService {
    void add(Review review, Order order);
    void delete(int id);
    void update(Review review);
    Review get(int id);
    List<Review> list(int pid);
    int getCount(int pid);
}
