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

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class ApiResponse {

	protected Request request;
	protected Response response;

	public ApiResponse(Request request) {
		this.request = request;
	}

	public ApiResponse(Response response, Request request) {
		this.request = request;
		this.response = response;
	}

	/**
	 * Peeks up to bytes from the response body and returns them as a new
	 * response body.
	 * <p>
	 * It is an error to call this method after the body has been consumed.
	 */
	public ResponseBody body() {
		if (response == null) {
			throw new ApiException("No Response Recieved.");
		}
		return this.response.body();
	}

	public int code() {
		return this.response.code();
	}

	// FIXME Naming
	public String error() {
		String message = "";
		if (!response.isSuccessful()) {
			message = "HTTP error code: " + response.code() + "\n";

			try {

				String msg = response.body().string();
				JSONObject data = new JSONObject(msg);

				if (data == null) {
					message = "Unknown response reason phrase";
				}

				if (data.getString("message") != null)
					message = message + data.getString("message");

				if (data.getString("error_description") != null)
					message = message + data.getString("error_description");

				if (data.getString("description") != null)
					message = message + data.getString("description");

			} catch (JSONException | IOException e) {
				message = message
						+ " and additional error happened during JSON parse "
						+ e.getMessage();
			}
		} else {
			message = "";
		}
		return message;

	}

	/**
	 * Returns content type of the response
	 */
	protected String getContentType() {
		return this.response.headers().get("Content-Type");
	}

	// FIXME Remove
	public Headers getRequestHeader(Request request) {
		return request.headers();
	}

	protected boolean isContentType(String contentType) {
		return getContentType().equalsIgnoreCase(contentType);
	}

	public JsonElement json() throws ApiException {
		JsonElement jObject;
		try {
			JsonParser parser = new JsonParser();
			jObject = parser.parse(body().string());
			return jObject;
		} catch (Exception e) {
			throw new ApiException(
					"Exception occured while converting the HTTP response to JSON in Class:  ",
					e);
		}
	}

	public boolean ok() {
		return (code() >= 200 && code() < 300);
	}

	/**
	 * The wire-level request that initiated this HTTP response. This is not
	 * necessarily the same request issued by the application
	 *
	 * <ul>
	 * <li>It may be transformed by the HTTP client. For example, the client may
	 * copy headers like {@code Content-Length} from the request body.
	 * <li>It may be the request generated in response to an HTTP redirect or
	 * authentication challenge. In this case the request URL may be different
	 * than the initial request URL.
	 * </ul>
	 */
	public Request request() {
		return this.request;
	}

	/**
	 * The wire-level HTTP response. This is not necessarily the same response
	 * issued by the application
	 */
	public Response response() {
		return this.response;
	}

	public String text() throws IOException {
		return body().string();

	}

	// multipart
}