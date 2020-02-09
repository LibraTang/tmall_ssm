package com.how2java.tmall.service.impl;

import com.how2java.tmall.mapper.PropertyValueMapper;
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.Property;
import com.how2java.tmall.pojo.PropertyValue;
import com.how2java.tmall.pojo.PropertyValueExample;
import com.how2java.tmall.service.PropertyService;
import com.how2java.tmall.service.PropertyValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyValueServiceImpl implements PropertyValueService {
    @Autowired
    PropertyService propertyService;
    @Autowired
    PropertyValueMapper propertyValueMapper;

    //初始化属性值
    @Override
    public void init(Product product) {
        //先取出所有该分类所有属性字段
        List<Property> pts = propertyService.list(product.getCid());
        //检查该产品和属性是否存在属性值
        for(Property pt : pts) {
            PropertyValue pv = get(product.getId(), pt.getId());
            //若不存在，则创建一个属性值，设置产品和属性id
            if(pv == null) {
                pv = new PropertyValue();
                pv.setPid(product.getId());
                pv.setPtid(pt.getId());
                propertyValueMapper.insert(pv);
            }
        }
    }

    //修改属性值
    @Override
    public void update(PropertyValue pv) {
        propertyValueMapper.updateByPrimaryKeySelective(pv);
    }

    @Override
    public PropertyValue get(int pid, int ptid) {
        PropertyValueExample example = new PropertyValueExample();
        example.createCriteria().andPidEqualTo(pid).andPtidEqualTo(ptid);
        List<PropertyValue> pvs = propertyValueMapper.selectByExample(example);
        if(pvs.isEmpty())
            return null;
        return pvs.get(0);
    }

    @Override
    public List<PropertyValue> list(int pid) {
        PropertyValueExample example = new PropertyValueExample();
        example.createCriteria().andPidEqualTo(pid);
        List<PropertyValue> pvs = propertyValueMapper.selectByExample(example);
        //别忘了非数据库字段的添加
        for(PropertyValue pv : pvs) {
            Property property = propertyService.get(pv.getPid());
            pv.setProperty(property);
        }
        return pvs;
    }
}
