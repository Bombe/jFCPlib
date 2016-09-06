/*
 * jFCPlib - GetResult.java - Copyright © 2010–2016 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.fcp.highlevel;

import java.io.InputStream;
import java.util.EventListener;

/**
 * A get result encapsulates the result of {@link FcpClient#getURI(String)}. It
 * is used to allow synchronous retrieval of a file without resorting to
 * {@link EventListener} interfaces to notify the application of intermediary
 * results, such as redirects.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetResult {

	/** Whether the request was successful. */
	private boolean success;

	/** The error code, if an error occured. */
	private int errorCode;

	/** The exception, if an exception occured. */
	private Throwable exception;

	/** The real URI, if a redirect was found. */
	private String realUri;

	/** The content type of the file. */
	private String contentType;

	/** The length of the file. */
	private long contentLength;

	/** An input stream containing the data of the file. */
	private InputStream inputStream;

	/**
	 * Returns whether the request was successful.
	 *
	 * @return {@code true} if the request was successful, {@code false}
	 *         otherwise
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Sets whether the request was successful.
	 *
	 * @param success
	 *            {@code true} if the request was successful, {@code false}
	 *            otherwise
	 * @return This result, to allow method chaning
	 */
	GetResult success(boolean success) {
		this.success = success;
		return this;
	}

	/**
	 * Returns the error code of the request. The error code is the error code
	 * that is transferred in FCP’s “GetFailed” message. The error code is not
	 * valid if {@link #isSuccess()} is {@code true}. If an exception occured
	 * (i.e. if {@link #getException()} returns a non-{@code null} value) the
	 * error code might also be invalid.
	 *
	 * @return The error code of the request
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Sets the error code of the request.
	 *
	 * @param errorCode
	 *            The error code of the request
	 * @return This result, to allow method chaining
	 */
	GetResult errorCode(int errorCode) {
		this.errorCode = errorCode;
		return this;
	}

	/**
	 * Returns the exception, if any occured.
	 *
	 * @return The occured exception, or {@code null} if there was no exception
	 */
	public Throwable getException() {
		return exception;
	}

	/**
	 * Sets the exception that occured.
	 *
	 * @param exception
	 *            The occured exception
	 * @return This result, to allow method chaining
	 */
	GetResult exception(Throwable exception) {
		this.exception = exception;
		return this;
	}

	/**
	 * Returns the real URI in case of a redirect.
	 *
	 * @return The real URI, or {@code null} if there was no redirect
	 */
	public String getRealUri() {
		return realUri;
	}

	/**
	 * Sets the real URI in case of a redirect.
	 *
	 * @param realUri
	 *            The real URI
	 * @return This result, to allow method chaining
	 */
	GetResult realUri(String realUri) {
		this.realUri = realUri;
		return this;
	}

	/**
	 * Returns the content type of the result.
	 *
	 * @return The content type of the result
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type of the result.
	 *
	 * @param contentType
	 *            The content type of the result
	 * @return This result, to allow method chaining
	 */
	GetResult contentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	/**
	 * Returns the content length of the result.
	 *
	 * @return The content length of the result
	 */
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * Sets the content length of the result.
	 *
	 * @param contentLength
	 *            The content length of the result
	 * @return This result, to allow method chaining
	 */
	GetResult contentLength(long contentLength) {
		this.contentLength = contentLength;
		return this;
	}

	/**
	 * Returns the data.
	 *
	 * @return The data
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Sets the input stream that will deliver the data.
	 *
	 * @param inputStream
	 *            The input stream containing the data
	 * @return This result, to allow method chaining
	 */
	GetResult inputStream(InputStream inputStream) {
		this.inputStream = inputStream;
		return this;
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[success=" + success + ",errorCode=" + errorCode + ",exception=" + exception + ",realUri=" + realUri + ",contentType=" + contentType + ",contentLength=" + contentLength + ",inputStream=" + inputStream + "]";
	}

}
