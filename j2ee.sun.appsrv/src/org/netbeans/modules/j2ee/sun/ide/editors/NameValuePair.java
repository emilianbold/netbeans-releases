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

package org.netbeans.modules.j2ee.sun.ide.editors;

public class NameValuePair implements java.io.Serializable
{
    private String paramName;
    private String paramValue;
    private String paramDescription;

    public void setParamName(String value) {
        paramName = value;
    }
    public String getParamName() {
        return paramName;
    }
    public void setParamValue(String value) {
        paramValue = value;
    }
    public String getParamValue() {
        return paramValue;
    }
    public String getParamDescription() {
        return paramDescription;
    }
    public void setParamDescription(String value) {
        paramDescription = value;
    }
}
