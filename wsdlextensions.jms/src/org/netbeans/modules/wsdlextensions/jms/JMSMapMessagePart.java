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
 *
 * Represents the jms message part mapping to a jms map message object value
 */
public interface JMSMapMessagePart extends JMSNamedPart{

    // JMS MapMessage value types
    public static final String MAPMESSAGE_TYPE_BOOLEAN = "boolean";
    public static final String MAPMESSAGE_TYPE_BYTE = "byte";
    public static final String MAPMESSAGE_TYPE_BYTES = "bytes";
    public static final String MAPMESSAGE_TYPE_CHAR = "char";
    public static final String MAPMESSAGE_TYPE_SHORT = "short";
    public static final String MAPMESSAGE_TYPE_INT = "int";
    public static final String MAPMESSAGE_TYPE_LONG = "long";
    public static final String MAPMESSAGE_TYPE_FLOAT = "float";
    public static final String MAPMESSAGE_TYPE_DOUBLE = "double";
    public static final String MAPMESSAGE_TYPE_STRING = "string";    
}
