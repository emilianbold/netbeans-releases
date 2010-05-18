/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.edm.project.anttasks;

/**
 * This is the base exception which is thrown during the edm wsdl generation ,
 * the underlying cause for the exception could be different
 * 
 *
 */
public class WsdlGenerateException extends Exception {

	private static final long serialVersionUID = 8323174064724661949L;

	public WsdlGenerateException() {
		super();
	}

	public WsdlGenerateException(String message, Throwable cause) {
		super(message, cause);
	}

	public WsdlGenerateException(String message) {
		super(message);
	}

	public WsdlGenerateException(Throwable cause) {
		super(cause);
	}

}
