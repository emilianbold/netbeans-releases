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
