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
 * Means of passing XML attribute/value pairs to {@link XMLEncoderStream}.
 * <p>
 * There is no need to escape attribute values.
 * <p>
 * <pre>
 AttrValuePair attrs[] = {
    new AttrValuePair("firstName", person.getFirstName()),
    new AttrValuePair("lastName", person.getLastName()),
 }
 * </pre>
 */

public class AttrValuePair {
    private String attr;
    private String value;

    public AttrValuePair(String attr, String value) {
	this.attr = attr;
	this.value = value;
    }

    public String getAttr() {
	return attr;
    } 

    public String getValue() {
	return value;
    }
}
