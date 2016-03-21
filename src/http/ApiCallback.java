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

public interface ApiCallback {

	/**
	 * Called when the request could not be executed due to cancellation, a
	 * connectivity problem or timeout. Because networks can fail during an
	 * exchange, it is possible that the remote server accepted the request
	 * before the failure.
	 */
	public void onFailure(ApiException e);

	/**
	 * Called when the HTTP response was successfully returned by the remote
	 * server. The callback may proceed to read the response body with
	 * {@link ApiResponse#body}. The recipient of the callback may even consume
	 * the response body on another thread.
	 */
	public void onResponse(ApiResponse response);

}