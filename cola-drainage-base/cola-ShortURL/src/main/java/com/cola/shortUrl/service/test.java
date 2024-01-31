package com.cola.shortUrl.service;


import com.cola.shortUrl.domain.LinkGroup;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class test {


    public void test1(){
        fin("LinkGroup");
    }


    public List<String> fin(String entityName){





        try {
            // 获取实体类的Class对象
            Class<?> entityClass =  Class.forName("com.cola.shortUrl.domain."+entityName);

            // 获取实体类的所有字段
            Field[] fields = entityClass.getDeclaredFields();

            // 创建一个列表来存储字段名
            List<String> fieldNames = new ArrayList<>();

            // 遍历字段并将字段名添加到列表中
            for (Field field : fields) {
                fieldNames.add(field.getName());
            }

            // 输出列表中的字段名
            for (String fieldName : fieldNames) {
                System.out.println(fieldName);
            }

            LinkGroup entity=new LinkGroup();
            entity.setUserId(1);
            entity.setGroupName("ces");
            entity.setRemark("reeee");
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = objectMapper.convertValue(entity, Map.class);

            for (String key:map.keySet()) {
                System.out.println("------------------");
                System.out.println("key:"+key);
                System.out.println("value:"+map.get(key));
            }




            return fieldNames;
        } catch (Exception e) {
            e.printStackTrace();

        }

        return null;


    }
}
