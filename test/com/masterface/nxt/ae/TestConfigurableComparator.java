package com.masterface.nxt.ae;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TestConfigurableComparator {

    @Test
    public void sortOrder() {
        Map<String, Object> m1 = new HashMap<>();
        m1.put("StringObj", "AAA");
        m1.put("IntegerObj", 9);
        m1.put("LongObj", 99);

        Map<String, Object> m2 = new HashMap<>();
        m2.put("StringObj", "AB");
        m2.put("IntegerObj", 100);
        m2.put("LongObj", 10);

        Map<String, Object> m3 = new HashMap<>();
        m3.put("StringObj", "AAA");
        m3.put("IntegerObj", -8);
        m3.put("LongObj", 99);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(m1);
        list.add(m2);
        list.add(m3);

        String msg = null;
        try {
            list.stream().sorted(new ConfigurableComparator("dummy")).count();
        } catch (IllegalArgumentException e) {
            msg = e.getMessage();
        }
        Assert.assertEquals("dummy is not a legal field name", msg);

        msg = null;
        try {
            list.stream().sorted(new ConfigurableComparator("StringObj,dummy")).count();
        } catch (IllegalArgumentException e) {
            msg = e.getMessage();
        }
        Assert.assertEquals("dummy is not a legal field name", msg);

        Stream<Map<String, Object>> sorted = list.stream().sorted(new ConfigurableComparator("StringObj"));
        Object[] objects = sorted.toArray();
        Assert.assertEquals(m1, objects[0]);
        Assert.assertEquals(m3, objects[1]);
        Assert.assertEquals(m2, objects[2]);

        sorted = list.stream().sorted(new ConfigurableComparator("StringObj,IntegerObj"));
        objects = sorted.toArray();
        Assert.assertEquals(m3, objects[0]);
        Assert.assertEquals(m1, objects[1]);
        Assert.assertEquals(m2, objects[2]);

        sorted = list.stream().sorted(new ConfigurableComparator("-StringObj"));
        objects = sorted.toArray();
        Assert.assertEquals(m2, objects[0]);
        Assert.assertEquals(m1, objects[1]);
        Assert.assertEquals(m3, objects[2]);

        sorted = list.stream().sorted(new ConfigurableComparator("StringObj,-IntegerObj"));
        objects = sorted.toArray();
        Assert.assertEquals(m1, objects[0]);
        Assert.assertEquals(m3, objects[1]);
        Assert.assertEquals(m2, objects[2]);
    }

    @Test
    public void numericStrings() {
        Map<String, Object> m1 = new HashMap<>();
        m1.put("String", "100.05");
        m1.put("NumericString", "100.05");

        Map<String, Object> m2 = new HashMap<>();
        m2.put("String", "nine");
        m2.put("NumericString", "9");

        Map<String, Object> m3 = new HashMap<>();
        m3.put("String", "nine");
        m3.put("NumericString", "-9");

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(m1);
        list.add(m2);
        list.add(m3);

        Stream<Map<String, Object>> sorted = list.stream().sorted(new ConfigurableComparator("NumericString"));
        Object[] objects = sorted.toArray();
        Assert.assertEquals(m3, objects[0]);
        Assert.assertEquals(m2, objects[1]);
        Assert.assertEquals(m1, objects[2]);

        sorted = list.stream().sorted(new ConfigurableComparator("String,NumericString"));
        objects = sorted.toArray();
        Assert.assertEquals(m1, objects[0]);
        Assert.assertEquals(m3, objects[1]);
        Assert.assertEquals(m2, objects[2]);

        sorted = list.stream().sorted(new ConfigurableComparator("String,-NumericString"));
        objects = sorted.toArray();
        Assert.assertEquals(m1, objects[0]);
        Assert.assertEquals(m2, objects[1]);
        Assert.assertEquals(m3, objects[2]);

        sorted = list.stream().sorted(new ConfigurableComparator("-NumericString,String"));
        objects = sorted.toArray();
        Assert.assertEquals(m1, objects[0]);
        Assert.assertEquals(m2, objects[1]);
        Assert.assertEquals(m3, objects[2]);
    }
}
