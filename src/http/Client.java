package http;

/*
 * Copyright (c) 2015 RingCentral, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

/**
 * Created by vyshakh.babji on 11/9/15.
 */
public class Client {

	private final OkHttpClient client = new OkHttpClient();

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

	/**
	 * Makes a OKHttp call
	 *
	 * @param request
	 */
	public void sendRequest(final Request request, final ApiCallback callback) {

		Callback responseLoaderCallback = new Callback() {

			@Override
			public void onFailure(Request request, IOException e) {
				callback.onFailure(new ApiException(new ApiResponse(request), e));
			}

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
		};

		loadResponse(request, responseLoaderCallback);
	}

}