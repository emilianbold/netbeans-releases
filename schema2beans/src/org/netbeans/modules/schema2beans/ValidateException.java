/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.schema2beans;

public class ValidateException extends Exception {
    protected Object failedBean;
    protected String failedPropertyName;
    protected FailureType failureType;

    public ValidateException(String msg, String failedPropertyName, Object failedBean) {
        super(msg);
        this.failedBean = failedBean;
        this.failedPropertyName = failedPropertyName;
    }

    public ValidateException(String msg, FailureType ft,
                             String failedPropertyName, Object failedBean) {
        super(msg);
        this.failureType = ft;
        this.failedBean = failedBean;
        this.failedPropertyName = failedPropertyName;
    }

    public String getFailedPropertyName() {return failedPropertyName;}
    public Object getFailedBean() {return failedBean;}
    public FailureType getFailureType() {return failureType;}

    public static class FailureType {
        private final String name;

        private FailureType(String name) {this.name = name;}

        public String toString() { return name;}

        public static final FailureType NULL_VALUE = new FailureType("NULL_VALUE");
        public static final FailureType DATA_RESTRICTION = new FailureType("DATA_RESTRICTION");
        public static final FailureType ENUM_RESTRICTION = new FailureType("ENUM_RESTRICTION");
        public static final FailureType ALL_RESTRICTIONS = new FailureType("ALL_RESTRICTIONS");
        public static final FailureType MUTUALLY_EXCLUSIVE = new FailureType("MUTUALLY_EXCLUSIVE");
    }
    
}

