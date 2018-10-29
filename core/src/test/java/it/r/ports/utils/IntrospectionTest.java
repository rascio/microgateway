package it.r.ports.utils;


import it.r.ports.api.Request;
import lombok.Value;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public class IntrospectionTest {


    @Value
    public static class TestReq implements Request<Void, Void, Void, Map<Integer, String>> {
        Void id;
        Void body;
        Void parameters;
    }
    @Test
    void responseType() {
        assertEquals(Introspection.responseType(TestReq.class), Map.class);
    }
}