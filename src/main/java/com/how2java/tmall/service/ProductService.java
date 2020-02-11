package com.how2java.tmall.service;

import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.pojo.Product;

import java.util.List;

public interface ProductService {
    void add(Product product);
    void delete(int id);
    void update(Product product);
    Product get(int id);
    List<Product> list(int cid);
    void setFirstProductImage(Product p);
    void setFirstProductImage(List<Product> ps);
    void fill(Category c);
    void fill(List<Category> cs);
    void fillByRow(List<Category> cs);
    void setSaleAndReviewNumber(Product p);
    void setSaleAndReviewNumber(List<Product> ps);
    List<Product> search(String keyword);
}
