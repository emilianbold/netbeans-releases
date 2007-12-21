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

package org.netbeans.modules.wsdlextensions.jms;

/**
 * JMSOption
 */
public interface JMSOption extends JMSComponent {

    public static final String ATTR_NAME = "name";
    public static final String ATTR_VALUE = "value";
        
    /**
     * Get value of name attribute
     * @return The String value of name attribute
     */
    public String getName();

    /**
     * Sets value of name attribute
     * @param val The String value of name attribute
     */
    public void setName(String val);

    /**
     * Get value of value attribute
     * @return The String value of value attribute
     */
    public String getValue();

    /**
     * Sets value of value attribute
     * @param val The String value of value attribute
     */
    public void setValue(String val);

}
