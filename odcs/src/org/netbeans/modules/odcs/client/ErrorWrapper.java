/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.client;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.HttpStatusCodeException;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.common.service.ServerRuntimeException;
import com.tasktop.c2c.server.common.service.ValidationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author tomas
 */
public class ErrorWrapper {
    Error error;

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public static class Error {

	/** General Server Error code. */
	private static final String GENERAL_SERVER_EXCEPTION = "ServerException";

	private static Map<String, Class<? extends Throwable>> nameToExceptionType = new HashMap<String, Class<? extends Throwable>>();
	private static Set<Class<? extends Throwable>> exceptionTypes = new HashSet<Class<? extends Throwable>>();
	static {
//		register(ValidationException.class);
		register(EntityNotFoundException.class);
		register(ConcurrentUpdateException.class);
//		register(AccessDeniedException.class);
		register(AuthenticationException.class);
//		register(BadCredentialsException.class);
//		register(AuthenticationCredentialsNotFoundException.class);
//		register(InsufficientAuthenticationException.class);
		register(InsufficientPermissionsException.class);
//		register(UsernameNotFoundException.class);
		register(HttpStatusCodeException.class);
	}

	private String errorCode;
	private String message;
	private String detail;
//	private ValidationErrors validationErrors;

	public Error() {

	}

	private static void register(Class<? extends Throwable> exceptionType) {
		if (exceptionType != ValidationException.class) {
			try {
				exceptionType.getConstructor(String.class);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException(e);
			}
		}

		Class<? extends Throwable> previous = nameToExceptionType.put(exceptionType.getSimpleName(), exceptionType);
		if (previous != null) {
			throw new IllegalStateException();
		}
		exceptionTypes.add(exceptionType);
	}

	public Error(String message) {
		this.errorCode = GENERAL_SERVER_EXCEPTION;
		this.message = message;
	}

	/**
	 * Construct an error from an exception.
	 * 
	 * @param e
	 */
	public Error(Throwable e) {
		this(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
		if (exceptionTypes.contains(e.getClass())) {
			errorCode = e.getClass().getSimpleName();
		}
		StringWriter detailWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(detailWriter);
		e.printStackTrace(printWriter);
		printWriter.close();
		detail = detailWriter.toString();

	}

//	/**
//	 * Construct an error from a validation exception.
//	 */
//	public Error(ValidationException validationException, MessageSource messageSource) {
//		this(validationException);
//		validationErrors = new ValidationErrors();
//		message = "";
//		for (ObjectError error : validationException.getErrors().getAllErrors()) {
//			String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : null;
//			String defaultMessage;
//			try {
//				defaultMessage = messageSource.getMessage(error, getLocale());
//			} catch (NoSuchMessageException e) {
//				defaultMessage = error.getDefaultMessage();
//				LoggerFactory.getLogger(Error.class).warn(e.getMessage(), e);
//			}
//			validationErrors.addError(error.getObjectName(), fieldName, error.getCode(), error.getArguments(),
//					defaultMessage);
//			if (message.length() > 0) {
//				message += ", ";
//			}
//			message += defaultMessage;
//		}
//	}

	private Locale getLocale() {
		// FIXME
		return Locale.ENGLISH;
	}

//	/**
//	 * Construct an error from an authentication exception.
//	 * 
//	 * @param e
//	 */
//	public Error(org.springframework.security.core.AuthenticationException e) {
//		this(e.getMessage());
//		if (!exceptionTypes.contains(e.getClass())) {
//			e = new BadCredentialsException(e.getMessage(), e);
//		}
//		if (exceptionTypes.contains(e.getClass())) {
//			errorCode = e.getClass().getSimpleName();
//		}
//	}

	/**
	 * a message that describes the cause of the error
	 */
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * a code that identifies the kind of exception that occurred.
	 */
	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * A detailed message for diagnostic information. Normally this would not be presented to the user.
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * A detailed message for diagnostic information. Normally this would not be presented to the user.
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

//	public ValidationErrors getValidationErrors() {
//		return validationErrors;
//	}
//
//	public void setValidationErrors(ValidationErrors validationErrors) {
//		this.validationErrors = validationErrors;
//	}

	/**
	 * Build the exception that this error represents.
	 * 
	 * @return
	 */
	@JsonIgnore
	public Throwable getException() {
		Throwable e;
		Class<? extends Throwable> exceptionClass = nameToExceptionType.get(errorCode);
		if (exceptionClass != null) {
//			if (exceptionClass == ValidationException.class) {
//				e = new ValidationException(message, new ValidationErrorsBridge(validationErrors));
//			} else {
				try {
					e = exceptionClass.getConstructor(String.class).newInstance(message);
				} catch (Throwable t) {
					throw new IllegalStateException(message, t);
				}
//			}
		} else {
			e = new ServerRuntimeException(message);
		}
		return e;
	}
}
}
