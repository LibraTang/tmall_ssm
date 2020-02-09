package com.how2java.tmall.service;

import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.PropertyValue;

import java.util.List;

public interface PropertyValueService {
    void init(Product product);
    void update(PropertyValue pv);
    PropertyValue get(int pid, int ptid);
    List<PropertyValue> list(int pid);
}
