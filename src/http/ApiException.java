package http;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Response;
import core.RingCentralException;

/**
 * This is a RingCentral APIException Class
 */
public class ApiException extends RingCentralException {
    public enum ErrorType {
        Client, Service, Unknown
    }

    private static final long serialVersionUID = 1L;
    private String errorCode;
    private String errorMessage;
    private ErrorType errorType = ErrorType.Unknown;
    private String extraInfo;
    private String message;
    private String rawResponseContent;
    private Request request;

    private ApiResponse response;
    private String serviceName;

    private int statusCode;

    public ApiException(ApiResponse response, Exception cause) {
        super(null, cause);
        this.response = response;
        this.extraInfo = response.error();
    }

    public ApiException(Response response) {
        super((String) null);
    }

    public ApiException(String errorMessage) {
        super((String) null);
        this.errorMessage = errorMessage;
    }

    public ApiException(String errorMessage, Exception cause) {
        super(null, cause);
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    @Override
    public String getMessage() {
        String errormessage = getErrorMessage() + " (Service: "
                + getServiceName() + "; " + "; Error Code: " + getErrorCode()
                + ")";
        return extraInfo == "" ? errormessage : errormessage + extraInfo;
    }

    public String getRawResponseContent() {
        return rawResponseContent;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String value) {
        errorMessage = value;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public void setRawResponseContent(String rawResponseContent) {
        this.rawResponseContent = rawResponseContent;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}
