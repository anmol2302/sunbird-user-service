package utils.validator.logvalidator;

import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.sunbird.util.LoggerEnum;
import org.sunbird.util.ProjectLogger;
import utils.validator.IModelValidator;
import utils.validator.schema.SchemaFactory;

public class LogModelValidator implements IModelValidator {

  @Override
  public void validate(JSONObject request, String uri) {
    Schema schema = SchemaFactory.getInstance().getSchemaOrNull(uri).getSchema();
    schema.validate(request);
    ProjectLogger.log(
        String.format("%s:%s:request validated", this.getClass().getSimpleName(), "validate"),
        LoggerEnum.DEBUG.name());
  }
}
