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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.common.api;

/**
 * ConfigurationException occurs if there is a problem with the server-specific
 * configuration.
 * 
 * @author sherold
 * 
 * @since 1.23
 */
public class ConfigurationException extends Exception {

    /**
     * Constructs a new ConfigurationException with a message describing the error.
     * 
     * @param message describing the error. The message should be localized so that
     *        it can be displayed to the user.
     */
    public ConfigurationException(String message) {
	super(message);
    }

    /**
     * Constructs a new ConfigurationException with a message and cause of the error.
     * 
     * @param message describing the error. The message should be localized so that
     *        it can be displayed to the user.
     * @param cause the cause of the error.
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
