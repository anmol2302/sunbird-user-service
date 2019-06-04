package controllers.usermanagement;

import controllers.BaseController;
import play.mvc.Result;
import java.util.concurrent.CompletionStage;


/**
 * This controller is used to handle master roles. it will have get all roles, create new roles,remove roles api's .
 * CompletionStage: A stage of a possibly asynchronous computation, that performs an action or computes a value when another CompletionStage completes
 * Currently it's having only get all roles api.
 */
public class RolesController extends BaseController {


    /**
     * This method will provide all available roles inside sunbird system.
     * * CompletionStage: A stage of a possibly asynchronous computation, that performs an action or computes a value when another CompletionStage completes
     * @return CompletionStage of get User api  result
     */

    public CompletionStage<Result> getRoles() {
        return handelRequest();
    }


}
