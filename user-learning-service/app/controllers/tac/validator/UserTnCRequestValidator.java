package controllers.tac.validator;

import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.request.BaseRequestValidator;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;

/**
 * This is a validation class specific to Terms and Condition validation
 */
public class UserTnCRequestValidator extends BaseRequestValidator {


  /**
   * This method is used  to validate the Terms and condition request.
   * @param request
   */
  public void validateTnCRequest(Request request) {
    validateParam(
        (String) request.get(JsonKey.VERSION),
        ResponseCode.mandatoryParamsMissing,
        JsonKey.VERSION);
  }
}
