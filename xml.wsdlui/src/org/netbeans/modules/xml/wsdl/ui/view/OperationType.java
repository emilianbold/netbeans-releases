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

/*
 * OperationType.java
 *
 * Created on September 5, 2006, 10:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view;

/**
 *
 * @author radval
 */
public class OperationType {
        public static final String OPERATION_REQUEST_REPLY = "OPERATION_REQUEST_REPLY"; //NOI18N
        public static final String OPERATION_ONE_WAY = "OPERATION_ONE_WAY"; //NOI18N
        public static final String OPERATION_SOLICIT_RESPONSE = "OPERATION_SOLICIT_RESPONSE"; //NOI18N
        public static final String OPERATION_NOTIFICATION = "OPERATION_NOTIFICATION"; //NOI18N
        
        private String mOperationType;
        
        private String mOperationTypeName;
        
        OperationType(String operationType, String operationTypeName) {
            this.mOperationType = operationType;
            this.mOperationTypeName = operationTypeName;
        }
        
        public String getOperationType() {
            return this.mOperationType;
        }
        
        public String getOperationTypeName() {
            return this.mOperationTypeName;
        }
        
        public String toString() {
            return getOperationTypeName();
        }
    }
