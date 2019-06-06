package controllers.usermanagement;

import controllers.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.request.HeaderParam;
import play.Application;
import play.test.Helpers;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class UserDataEncryptionControllerTest {

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
    }

    @Test
    public void encrypt() {
    }

    @Test
    public void decrypt() {
    }
}