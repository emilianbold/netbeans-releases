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

package org.netbeans.modules.visualweb.websvcmgr.codegen;

import org.netbeans.modules.visualweb.websvcmgr.util.Util;

/**
 *
 * @author  cao
 */
public class DataProviderInfo {
    
    private String clientWrapperClassName;
    private String packageName;
    
    // Everyting about this method
    private DataProviderMethod method;
    
    private String methodName4DPClass;
    private int outputHolderIndex = -1;
    private DataProviderParameter wrappedProperty = null;
    
    public DataProviderInfo( String packageName, String clientWrapperClassName, DataProviderMethod method, String methodName4DPClass ) {
        this.packageName = packageName;
        this.clientWrapperClassName = clientWrapperClassName;
        this.method = method;
        this.methodName4DPClass = methodName4DPClass;
    }
    
    public int getOutputHolderIndex() {
        return outputHolderIndex;
    }
    
    public void setOutputHolderIndex(int outputHolderIndex) {
        this.outputHolderIndex = outputHolderIndex;
    }
    
    public void setWrappedProperty(DataProviderParameter wrappedProperty) {
        this.wrappedProperty = wrappedProperty;
    }
    
    public DataProviderParameter getWrappedProperty() {
        return wrappedProperty;
    }
    
    public String getPackageName()
    {
        return this.packageName;
    }
    
    public String getClassName() 
    { 
        int lastIndex = clientWrapperClassName.lastIndexOf( "Client" ); // NOI18N
        return clientWrapperClassName.substring( 0, lastIndex ) + Util.upperCaseFirstChar( methodName4DPClass );
    }
    
    public String getClientWrapperClassName()
    {
        return this.clientWrapperClassName;
    }
    
    public DataProviderMethod getMethod() {
        return method;
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        
        buf.append( "DPClassName: " + getClassName() + "\n" );
        buf.append( "ClientWrapperClassName: " + clientWrapperClassName + "\n" );
        buf.append( "Method: " + method.getMethodName() );
        
        return buf.toString();
    }
}
