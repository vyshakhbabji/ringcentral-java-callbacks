package http;

import com.squareup.okhttp.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vyshakh.babji on 11/9/15.
 */
public class Client {

    private final OkHttpClient client = new OkHttpClient();

    /**
     * Makes a OKHttp call
     *
     * @param request
     */
    public void sendRequest(final Request request, final ApiCallback callback) {

        Callback responseLoaderCallback = new Callback() {

            @Override
            public void onResponse(Response response) throws IOException {
                ApiResponse apiresponse = new ApiResponse(response,
                        response.request());
                if (apiresponse.ok())
                    callback.onResponse(apiresponse);
                else {

                    throw new ApiException(apiresponse.error());
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(new ApiException(new ApiResponse(request), e));
            }
        };

        loadResponse(request, responseLoaderCallback);
    }

    /**
     * Creates OKHttp Request
     *
     * @param method
     * @param URL
     * @param body
     * @param headers
     * @return OKHttp Request
     */
    public Request createRequest(String method, String URL, RequestBody body,
                                 HashMap<String, String> headers) {

        Request.Builder builder = new Request.Builder();

        if (headers == null)
            headers = new HashMap<String, String>();

        for (Map.Entry<String, String> entry : headers.entrySet())
            builder.addHeader(entry.getKey(), entry.getValue());

        // TODO Add default headers here, e.g. JSON-related stuff

        if (method.equalsIgnoreCase("get")) {
            builder = builder.url(URL);
        } else if (method.equalsIgnoreCase("delete")) {
            builder = builder.url(URL).delete();
        } else {
            if (method.equalsIgnoreCase("post")) {
                builder = builder.url(URL).post(body);

            } else if (method.equalsIgnoreCase("put")) {
                builder = builder.url(URL).put(body);
            } else
                throw new ApiException(
                        method
                                + " Method not Allowed. Please Refer API Documentation. See\n"
                                + "     * <a href =\"https://developer.ringcentral.com/api-docs/latest/index.html#!#Resources.html\">Server Endpoint</a> for more information. ");
        }

        return builder.build();
    }

    /**
     * Loads OKHttp Response synchronizing async api calls
     *
     * @param request
     * @param callback
     */
    protected void loadResponse(final Request request, final Callback callback) {
        client.newCall(request).enqueue(callback);
    }

}