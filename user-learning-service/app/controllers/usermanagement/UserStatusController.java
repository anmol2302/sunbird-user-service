package controllers.usermanagement;

import controllers.BaseController;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

/**
 * This controller is used to change status of user
 * CompletionStage: A stage of a possibly asynchronous computation, that performs an action or computes a value when another CompletionStage completes
 * CompletableFuture: A Future that may be explicitly completed (setting its value and status), and may be used as a CompletionStage, supporting dependent functions and actions that trigger upon its completion.
 */


public class UserStatusController extends BaseController {


    /**
     * This action method is used to block the user
     *
     * @return CompletionStage of block user api result
     */

    public CompletionStage<Result> blockUser() {
        return handelRequest();
    }

    /**
     * This action method is used to unblock the user
     *
     * @return CompletionStage of unblock user api result
     */

    public CompletionStage<Result> unblockUser() {
        return handelRequest();
    }

}
