/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2005-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.xml;

/**
 * Thrown by {@link XMLDecoder#checkVersion} if a version mismatch is
 * detected.
 */

public class VersionException extends Exception {

    private String element;
    private int expectedVersion;
    private int actualVersion;

    VersionException(String element,
			    int expectedVersion, int actualVersion) {
	super();
	this.element = element;
	this.expectedVersion = expectedVersion;
	this.actualVersion = actualVersion;
    }

    String element() {
	return element;
    }

    int expectedVersion() {
	return expectedVersion;
    } 

    int actualVersion() {
	return actualVersion;
    } 
}
