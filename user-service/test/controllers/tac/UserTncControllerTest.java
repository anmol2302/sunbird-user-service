package controllers.tac;

import controllers.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.request.HeaderParam;
import play.Application;
import play.mvc.Result;
import play.test.Helpers;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class UserTncControllerTest {
    TestHelper testHelper;
    public static Application app;
    public static Map<String, String[]> headerMap;

    @Before
    public void setUp() throws Exception {

        testHelper = new TestHelper();
        app = Helpers.fakeApplication();
        Helpers.start(app);
        headerMap = new HashMap<>();
        headerMap.put(HeaderParam.X_Consumer_ID.getName(), new String[]{"Some consumer ID"});
        headerMap.put(HeaderParam.X_Device_ID.getName(), new String[]{"Some device ID"});
        headerMap.put(
                HeaderParam.X_Authenticated_Userid.getName(), new String[]{"Some authenticated user ID"});
        headerMap.put(JsonKey.MESSAGE_ID, new String[]{"Some message ID"});
        headerMap.put(HeaderParam.X_APP_ID.getName(), new String[]{"Some app Id"});
    }

    @After
    public void tearDown() throws Exception {
        headerMap = null;
        app = null;
        testHelper = null;
    }

    @Test
    public void testAcceptTncSuccess() {
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("accept", "yes");
        Result result = testHelper.performTest("/v1.3/user/tnc/accept", "POST", reqMap, headerMap);
        assertTrue(testHelper.getResponseStatus(result) == 200);
    }
    @Test
    public void testAcceptTncFailure() {
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("accept", "yes");
        Result result = testHelper.performTest("/v1.3/user/tnc/accept", "POST", reqMap, headerMap);
        assertFalse(testHelper.getResponseStatus(result) == 400);
    }
}