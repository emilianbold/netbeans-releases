/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview;

import org.netbeans.modules.xml.multiview.cookies.ErrorComponentContainer;

 /** Error.java
 *
 * Created on November 20, 2004, 12:27 PM
 * @author mkuchtiak
 */
public class Error {

    public static final int TYPE_FATAL=0;
    public static final int TYPE_WARNING=1;
    
    public static final int ERROR_MESSAGE=0;
    public static final int WARNING_MESSAGE=1;
    public static final int MISSING_VALUE_MESSAGE=2;
    public static final int DUPLICATE_VALUE_MESSAGE=3;

    private int errorType;
    private int severityLevel;
    private String errorMessage, errorId;
    private javax.swing.JComponent focusableComponent;
    private ErrorComponentContainer errorContainer;
    
    /*
    public Error(int errorType, String errorMessage, javax.swing.JComponent focusableComponent) {
        this(TYPE_WARNING, errorType, errorMessage, focusableComponent);
    }

    public Error(int severityLevel, int errorType, String errorMessage, javax.swing.JComponent focusableComponent) {
        this.severityLevel=severityLevel;
        this.errorType=errorType;
        this.errorMessage=errorMessage;
        this.focusableComponent=focusableComponent;
    }
    */
    public Error(int errorType, String errorMessage, javax.swing.JComponent focusableComponent) {
        this(TYPE_WARNING ,errorType, errorMessage, focusableComponent);
    }
        
    public Error(int severityLevel, int errorType, String errorMessage, javax.swing.JComponent focusableComponent) {
        this.severityLevel=severityLevel;
        this.errorType=errorType;
        this.errorMessage=errorMessage;
        this.focusableComponent=focusableComponent;
    }  
    
    public Error(int errorType, String errorMessage, ErrorComponentContainer errorContainer, String errorId) {
        this(TYPE_WARNING,errorType, errorMessage, errorContainer, errorId);
    }   
    
    public Error(int severityLevel, int errorType, String errorMessage, ErrorComponentContainer errorContainer, String errorId) {
        this.severityLevel=severityLevel;
        this.errorType=errorType;
        this.errorMessage=errorMessage;
        this.errorContainer=errorContainer;
        this.errorId=errorId;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }
    
    public int getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public javax.swing.JComponent getFocusableComponent() {
        return focusableComponent;
    }

    public ErrorComponentContainer getErrorComponentContainer() {
        return errorContainer;
    }
    
    public String getErrorId() {
        return errorId;
    }
    
    public boolean isEditError() {
        return (focusableComponent!=null);
    }
    
}
