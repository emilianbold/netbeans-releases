/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * Message.java
 *
 * Created on November 18, 2004, 4:25 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author Nitya Doraisamy
 */
public interface Message extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String JAVA_METHOD = "JavaMethod";	// NOI18N
    public static final String OPERATION_NAME = "OperationName";	// NOI18N
        
    /** Setter for java-method property
     * @param value property value
     */
    public void setJavaMethod(JavaMethod value);
    /** Getter for java-method property.
     * @return property value
     */
    public JavaMethod getJavaMethod();
    
    public JavaMethod newJavaMethod();
    
    /** Setter for operation-name property
     * @param value property value
     */
    public void setOperationName(java.lang.String value);
    /** Getter for operation-name property.
     * @return property value
     */
    public java.lang.String getOperationName();
}
