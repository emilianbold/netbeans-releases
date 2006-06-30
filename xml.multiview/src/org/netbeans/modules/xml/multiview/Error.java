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

package org.netbeans.modules.xml.multiview;

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
    private String errorMessage;
    private javax.swing.JComponent focusableComponent;
    private ErrorLocation errorLocation;
    
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
    
    public Error(int errorType, String errorMessage, ErrorLocation errorLocation) {
        this(TYPE_WARNING,errorType, errorMessage, errorLocation);
    }   
    
    public Error(int severityLevel, int errorType, String errorMessage, ErrorLocation errorLocation) {
        this.severityLevel=severityLevel;
        this.errorType=errorType;
        this.errorMessage=errorMessage;
        this.errorLocation=errorLocation;
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

    public ErrorLocation getErrorLocation() {
        return errorLocation;
    }
    
    public boolean isEditError() {
        return (focusableComponent!=null);
    }
    
    /** Object that will enable to identify the place in section view where the error 
     * should be fixed. This is intended to use in SectionView:validateView() method.
     */
    public static class ErrorLocation {
        private Object key;
        private String componentId;
        
        public ErrorLocation (Object key, String componentId) {
            this.key=key;
            this.componentId=componentId;
        }
        
        public Object getKey() {
            return key;
        }
        public String getComponentId() {
            return componentId;
        }
    }
    
}
