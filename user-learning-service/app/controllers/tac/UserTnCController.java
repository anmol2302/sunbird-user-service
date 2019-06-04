package controllers.tac;

import play.mvc.Http;
import play.mvc.Result;
import controllers.BaseController;
import play.mvc.Results;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This controller is used to handel the agreement related action
 */
public class UserTnCController extends BaseController {

    /**
     * This action method is used to accept the agreement when user login for first time.
     * @return success response
     */
    public CompletionStage<Result> acceptTnC() {
        Http.RequestBody requestBody = request().body();
        CompletableFuture<String> cf = new CompletableFuture<>();
        cf.complete(getDummyResponse());
        return cf.thenApplyAsync(Results::ok);
    }


}
