package controllers;

import org.sunbird.util.LoggerEnum;
import org.sunbird.util.ProjectLogger;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This controller we can use for writing some common method to handel api request.
 * CompletableFuture: A Future that may be explicitly completed (setting its value and status), and
 * may be used as a CompletionStage, supporting dependent functions and actions that trigger upon
 * its completion. CompletionStage: A stage of a possibly asynchronous computation, that performs an
 * action or computes a value when another CompletionStage completes
 *
 * @author Anmol
 */
public class BaseController extends Controller {

  @Inject private HttpExecutionContext httpObject;

  public String getDummyResponse() {
      String dummyResponse =
        "{\"id\":\"api.user.200ok\",\"ver\":\"v1\",\"ts\":\"2019-01-17 16:53:26:286+0530\",\"params\":{\"resmsgid\":null,\"msgid\":\"8e27cbf5-e299-43b0-bca7-8347f7ejk5abcf\",\"err\":null,\"status\":\"success\",\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"response\":{\"response\":\"SUCCESS\",\"errors\":[]}}}";
    ProjectLogger.log(
            "BaseController : getDummyResponse : "+ dummyResponse, LoggerEnum.INFO.name());
    return dummyResponse;
  }

  public CompletionStage<Result> handelRequest() {
    Http.RequestBody requestBody = request().body();
    ProjectLogger.log(
            "BaseController :handelRequest : request reached to BaseController controller with request " + requestBody, LoggerEnum.INFO.name());
    CompletableFuture<String> cf = new CompletableFuture<>();
    cf.complete(getDummyResponse());
    return cf.thenApplyAsync(Results::ok, httpObject.current());
  }
}
