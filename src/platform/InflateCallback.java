package platform;


import com.squareup.okhttp.Request;
import http.ApiException;

public interface  InflateCallback {
     public void onResponse(Request request);

     public void onFailure(ApiException e);
}