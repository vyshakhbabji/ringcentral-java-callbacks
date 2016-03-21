package platform;




import com.squareup.okhttp.*;
import com.squareup.okhttp.Request.Builder;
import core.RingCentralException;
import http.ApiCallback;
import http.ApiException;
import http.ApiResponse;
import http.Client;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.*;


public class Platform {
    /*
    Revoke Session Endpoint
     */
    private static final String REVOKE_ENDPOINT_URL = "/restapi/oauth/revoke";

    /*
    Authentication and Refresh Token Endpoint
     */
    private static final String TOKEN_ENDPOINT_URL = "/restapi/oauth/token";
    private static final String USER_AGENT = "JAVA " + System.getProperty("java.version") + "/RCJAVASDK";

    protected String appKey;
    protected String appSecret;
    protected Server server;

    protected Auth auth;
    //protected Request request;
    protected Client client;
    protected Queue<ApiCallback> queue = new LinkedBlockingQueue<>();
    protected Builder requestBuilder;
    Object lock = new Object();
    boolean refreshInProgress;
    boolean state;

    /**
     * Creates Platform object
     *
     * @param client
     * @param appKey
     * @param appSecret
     * @param server
     */
    public Platform(Client client, String appKey, String appSecret, Server server) {
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.server = server;
        this.auth = new Auth();
        this.client = client;
    }

    static double getVersion () {
        String version = System.getProperty("java.version");
        int pos = version.indexOf('.');
        pos = version.indexOf('.', pos+1);
        return Double.parseDouble (version.substring (0, pos));
    }

    /**
     * @return Base 64 encoded app credentials
     */
    protected String apiKey() {
        return Credentials.basic(appKey, appSecret);
    }

    /**
     * @return Authorization Header "bearer @accesstoken"
     */
    protected String authHeader() {
        return this.auth.tokenType() + " " + this.auth.access_token;
    }

    /**
     * Checks if the current access token is valid. If the access token is expired, it does token refresh.
     * If refresh is not needed null will be returned as response
     */
    protected void ensureAuthentication(final ApiCallback callback) throws ApiException {
        if (auth.accessTokenValid()) {
            callback.onResponse(null);
        } else {
            refreshInProgress = true;
            refresh(callback);
        }
    }

    /**
     * Sets Request body for content type FORM_TYPE("application/x-www-form-urlencoded")
     *
     * @param body Input body as key:value pairs
     * @return
     */
    protected RequestBody formBody(HashMap<String, String> body) {
        FormEncodingBuilder formBody = new FormEncodingBuilder();
        for (HashMap.Entry<String, String> entry : body.entrySet())
            formBody.add(entry.getKey(), entry.getValue());
        return formBody.build();
    }

    /**
     * Get Auth object
     *
     * @return Auth Object
     */
    public Auth auth() {
        return auth;
    }

    /**
     * Checks if the login is valid
     */
    public void loggedIn(final LoggedInCallback callback) {
        ensureAuthentication(new ApiCallback() {
            @Override
            public void onResponse(ApiResponse response) {
                if (response == null)
                    callback.onResponse(true);
            }

            @Override
            public void onFailure(ApiException e) {
                callback.onResponse(false);
            }
        });
    }

    /**
     * Sets Login Credentials for authentication
     *
     * @param userName
     * @param extension
     * @param password
     * @param callback
     */
    public void login(String userName, String extension, String password, ApiCallback callback) throws ApiException {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("username", userName);
        body.put("password", password);
        body.put("extension", extension);
        body.put("grant_type", "password");
        requestToken(TOKEN_ENDPOINT_URL, body, callback);
    }

    /**
     * Actual function that inflates request
     *
     * @param request
     * @return
     */
    protected Request inflateRequestHeaders(final Request request, boolean skipAuthCheck) {
        if (!auth().accessTokenValid() && !skipAuthCheck) {
            throw new RingCentralException("Internal inflate request has been called without authentication");
        }
        Builder requestBuilder = request.newBuilder();
        if (!skipAuthCheck) {
            requestBuilder.addHeader("Authorization", authHeader());
        }
        //      requestBuilder.addHeader("User-Agent", USER_AGENT);

        return requestBuilder.build();

    }


    /**
     * Sets Request Header
     *
     * @param request
     * @param callback
     */
    public void inflateRequest(final Request request, final boolean skipAuthCheck, final InflateCallback callback) throws ApiException {
        if (!skipAuthCheck) {
            ensureAuthentication(new ApiCallback() {
                @Override
                public void onResponse(ApiResponse response) {
                    if (response == null) {
                        Request inflatedRequest = inflateRequestHeaders(request, skipAuthCheck);
                        callback.onResponse(inflatedRequest);
                    }
                }

                @Override
                public void onFailure(ApiException e) {
                    callback.onFailure(e);
                }
            });
        } else {
            Request inflatedRequest = inflateRequestHeaders(request, skipAuthCheck);
            callback.onResponse(inflatedRequest);
        }
    }

    /**
     * Sets authentication values after successful authentication
     *
     * @param response
     */
    protected void setAuth(ApiResponse response) throws ApiException {
        this.auth.setData(auth.jsonToHashMap(response));
    }

    /**
     * Creates request object
     *
     * @param endpoint
     * @param body
     * @param callback
     */
    protected void requestToken(String endpoint, final HashMap<String, String> body, final ApiCallback callback) throws ApiException {
        final String URL = server.value + endpoint;
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", apiKey());
        headers.put("Content-Type", ContentTypeSelection.FORM_TYPE.value.toString());
        Request request = client.createRequest("POST", URL, formBody(body), headers);
        sendRequest(request, true, new ApiCallback() {
            @Override
            public void onFailure(ApiException e) {
                callback.onFailure(new ApiException("Unable to request token. Request Failed.", e));
            }

            @Override
            public void onResponse(ApiResponse response) {
                try {
                    setAuth(response);
                    callback.onResponse(response);
                } catch (ApiException e) {
                    callback.onFailure(e);
                }
            }
        });
    }


    public void refresh(final ApiCallback callback) throws ApiException {

        ExecutorService executorService = Executors.newCachedThreadPool();

        synchronized (lock) {
            if (refreshInProgress == false) {
                refreshInProgress = true;
            }
            queue.add(callback);
        }

        synchronized (lock) {
            try {
                Future future = executorService.submit(new Runnable() {
                    public void run() {
                        System.out.println("Queue "+ String.valueOf(queue.size()));
                        try {
                            makeRefresh(new ApiCallback() {
                                public void onResponse(ApiResponse response) {
                                    while (!queue.isEmpty()) {
                                        ApiCallback c = queue.poll();
                                        c.onResponse(response);
                                        System.out.println("dequeue "+String.valueOf(queue.size()));
                                    }
                                }

                                @Override
                                public void onFailure(ApiException e) {
                                    while (!queue.isEmpty()) {
                                        ApiCallback c = queue.poll();
                                        c.onFailure(e);
                                        System.out.println("dequeue "+ String.valueOf(queue.size()));
                                    }
                                }
                            });
                        } catch (ApiException e) {
                            callback.onFailure(e);
                        }
                    }
                });
                while (!future.isDone())
                    future.get();
                System.out.println("future.get() = "+ String.valueOf(future.isDone()));
            } catch (InterruptedException e) {
                callback.onFailure(new ApiException("Interupted exception Occured while refreshing. Refresh Failed.Try logging in Again"));
            } catch (ExecutionException e) {
                callback.onFailure(new ApiException("Thread execution exception Occured while refreshing. Refresh Failed.Try logging in Again"));
            }
        }


        synchronized (lock) {
            refreshInProgress = false;
        }
    }

    protected void makeRefresh(final ApiCallback callback) throws ApiException {
        if (!this.auth.refreshTokenValid()) {
            callback.onFailure(new ApiException("Refresh Token Invalid. Please Login again."));
        } else {
            HashMap<String, String> body = new HashMap<String, String>();
            body.put("grant_type", "refresh_token");
            body.put("refresh_token", this.auth.refreshToken());

            requestToken(TOKEN_ENDPOINT_URL, body, callback);
        }
    }

    /**
     * Revoke current session
     *
     * @param callback
     */
    public void logout(final ApiCallback callback) throws ApiException {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("access_token", this.auth.access_token);
        requestToken(REVOKE_ENDPOINT_URL, body, new ApiCallback() {
            public void onFailure(ApiException e) {
                callback.onFailure(e);
            }

            public void onResponse(ApiResponse response) {
                auth.reset();
                callback.onResponse(response);
            }
        });
    }

    protected void sendRequest(Request request, boolean skipAuthentication, final ApiCallback callback) throws ApiException {
        inflateRequest(request, skipAuthentication, new InflateCallback() {
            public void onResponse(Request inflatedRequest) {
                client.sendRequest(inflatedRequest, callback);
            }

            public void onFailure(ApiException e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Send API Request
     *
     * @param method
     * @param apiURL
     * @param body
     * @param headerMap
     * @param callback
     */
    public void send(String method, String apiURL, RequestBody body, HashMap<String, String> headerMap, final ApiCallback callback) throws ApiException {
        final String endpointURL = server.value + apiURL;
        Request request = client.createRequest(method, endpointURL, body == null ? null : body, headerMap);
        sendRequest(request, false, callback);
    }

    public void get(String apiURL, RequestBody body, HashMap<String, String> headerMap, final ApiCallback callback) throws ApiException {
        send("get", apiURL, body == null ? null : body, headerMap, callback);
    }

    public void post(String apiURL, RequestBody body, HashMap<String, String> headerMap, final ApiCallback callback) throws ApiException {
        send("post", apiURL, body, headerMap, callback);
    }

    public void put(String apiURL, RequestBody body, HashMap<String, String> headerMap, final ApiCallback callback) throws ApiException {
        send("put", apiURL, body, headerMap, callback);
    }

    public void delete(String apiURL, RequestBody body, HashMap<String, String> headerMap, final ApiCallback callback) throws ApiException {
        send("delete", apiURL, body == null ? null : body, headerMap, callback);
    }

    public void expire_access() {
        auth().expire_access();
    } //FIXME Remove

    /**
     * Sets content-type
     */
    public enum ContentTypeSelection {
        FORM_TYPE("application/x-www-form-urlencoded"), JSON_TYPE(
                "application/json"), MULTIPART_TYPE("multipart/mixed;");
        protected MediaType value;

        private ContentTypeSelection(String contentType) {
            this.value = MediaType.parse(contentType);
        }
    }

    /**
     * RingCentral API Endpoint Server. See
     * "https://developer.ringcentral.com/api-docs/latest/index.html#!#Resources.html" Server Endpoint for more information.
     */
    public enum Server {
        PRODUCTION("https://platform.ringcentral.com"), SANDBOX(
                "https://platform.devtest.ringcentral.com");
        private String value;

        Server(String url) {
            this.value = url;
        }
    }

}