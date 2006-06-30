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

