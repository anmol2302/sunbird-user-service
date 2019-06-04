package controllers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BaseControllerTest {
    BaseController controllerObject;


    @Before
    public void setUp() throws Exception {
        controllerObject=new BaseController();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getDummyResponse() {
        String dummyResponse = "{\"id\":\"api.user.200ok\",\"ver\":\"v1\",\"ts\":\"2019-01-17 16:53:26:286+0530\",\"params\":{\"resmsgid\":null,\"msgid\":\"8e27cbf5-e299-43b0-bca7-8347f7ejk5abcf\",\"err\":null,\"status\":\"success\",\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"response\":{\"response\":\"SUCCESS\",\"errors\":[]}}}";
        assertEquals(dummyResponse,controllerObject.getDummyResponse());
    }

    @Test
    public void handelRequest() {
    }
}