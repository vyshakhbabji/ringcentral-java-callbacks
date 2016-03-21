package core;

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

import http.Client;
import platform.Platform;

public class SDK {

	Client client;
	Platform platform;

	/**
	 * Initialize the SDK and Platfrom object using appKey appSecret and server
	 * endpoint
	 *
	 * @param appKey
	 *            This is the application key
	 * @param appSecret
	 *            This is application secret
	 * @param server
	 *            This is the server endpoint. Server endpoint can be set to
	 *            SANDBOX or PRODUCTION i.e Platform.Server.SANDBOX or
	 *            Platform.Server.PRODUCTION
	 */
	public SDK(String appKey, String appSecret, Platform.Server server) {

		this.client = new Client();
		this.platform = new Platform(client, appKey, appSecret, server);
	}

	/**
	 * Returns platform object
	 *
	 * @return platfrom object
	 */
	public Platform platform() {
		return this.platform;
	}
}
