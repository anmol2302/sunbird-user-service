package controllers.usermanagement;

import controllers.BaseController;

import java.util.concurrent.CompletionStage;

import org.sunbird.util.LoggerEnum;
import org.sunbird.util.ProjectLogger;
import play.mvc.Result;

/**
 * This controller is responsible to only user related update task. CompletionStage: A stage of a
 * possibly asynchronous computation, that performs an action or computes a value when another
 * CompletionStage completes CompletableFuture: A Future that may be explicitly completed (setting
 * its value and status), and may be used as a CompletionStage, supporting dependent functions and
 * actions that trigger upon its completion.
 */
public class UserProfileUpdateController extends BaseController {

    /**
     * Updates current login time for given user in Keycloak.
     *
     * @return Return a promise for update login time API result.
     */
    public CompletionStage<Result> updateLoginTime() {
        ProjectLogger.log(
                "UserProfileUpdateController :updateLoginTime: request reached to UserProfileUpdateController controller with request " + request().body(), LoggerEnum.INFO.name());
        return handelRequest();
    }

    /**
     * This action method is uses to assign Roles to the user
     *
     * @return success response
     */
    public CompletionStage<Result> assignRoles() {
        ProjectLogger.log(
                "UserProfileUpdateController :assignRoles: request reached to UserProfileUpdateController controller with request " + request().body(), LoggerEnum.INFO.name());
        return handelRequest();
    }

    /**
     * This method is used to update user details
     *
     * @return Return a promise of updated userResponse
     */
    public CompletionStage<Result> updateUser() {
        ProjectLogger.log(
                "UserProfileUpdateController :updateUser: request reached to UserProfileUpdateController controller with request " + request().body(), LoggerEnum.INFO.name());

        return handelRequest();
    }
}
