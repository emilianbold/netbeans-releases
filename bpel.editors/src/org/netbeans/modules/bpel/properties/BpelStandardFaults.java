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
package org.netbeans.modules.bpel.properties;

import javax.xml.namespace.QName;

/**
 *
 * @author nk160297
 */
public enum BpelStandardFaults {
    SELECTION_FAILURE("selectionFailure"),
    CONFLICTING_RECEIVE("conflictingReceive"), 
    CONFLICTING_REQUEST("conflictingRequest"), 
    MISMATCHED_ASSIGNMENT_FAILURE("mismatchedAssignmentFailure"), 
    JOIN_FAILURE("joinFailure"), 
    CORRELATION_VIOLATION("correlationViolation"), 
    UNINITIALIZED_VARIABLE("uninitializedVariable"), 
    INVALID_REPLY("invalidReply"), 
    MISSING_REPLY("missingReply"), 
    MISSING_REQUEST("missingRequest"), 
    SUBLANGUAGE_EXECUTION_FAULT("subLanguageExecutionFault"), 
    UNSUPPORTED_REFERENCE("unsupportedReference"), 
    INVALID_VARIABLES("invalidVariables"), 
    UNINITIALIZED_PARTNER_ROLE("uninitializedPartnerRole"), 
    SCOPE_INITIALIZATON_FAILURE("scopeInitializationFailure"), 
    INVALID_BRANCH_CONDITION("invalidBranchCondition"), 
    COMPLETION_CONDITION_FAILURE("completionConditionFailure");
    
    public static final String BPEL_2_0_NS =
            "http://docs.oasis-open.org/wsbpel/2.0/process/executable";	// NOI18N
    
    private QName myQName;
    
    BpelStandardFaults(String localName) {
        myQName = new QName(BPEL_2_0_NS, localName);
    }
    
    public QName getQName() {
        return myQName;
    }
    
}
