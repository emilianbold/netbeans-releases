/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.model.xam;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ServiceRef;

/**
 * @author ads
 */
public enum BpelElements {
    PROCESS("process"),                             // NOI18N
    ASSIGN("assign"),                               // NOI18N
    REPLY("reply"),                                 // NOI18N
    CATCH_ALL("catchAll"),                          // NOI18N
    CATCH("catch"),                                 // NOI18N
    COMPENSATE("compensate"),                       // NOI18N
    COMPENSATION_HANDLER( "compensationHandler"),   // NOI18N
    COPY("copy"),                                   // NOI18N
    CORRELATIONS("correlations"),                   // NOI18N
    CORRELATION("correlation"),                     // NOI18N
    CORRELATION_SETS("correlationSets"),            // NOI18N
    CORRELATION_SET( "correlationSet"),             // NOI18N
    EMPTY("empty"),                                 // NOI18N
    EVENT_HANDLERS("eventHandlers"),                // NOI18N
    FAULT_HANDLERS( "faultHandlers"),               // NOI18N
    FLOW("flow"),                                   // NOI18N
    FROM("from"),                                   // NOI18N
    INVOKE("invoke"),                               // NOI18N
    LINKS( "links"),                                // NOI18N
    LINK("link"),                                   // NOI18N
    ON_MESSAGE("onMessage"),                        // NOI18N
    PARTNERLINKS("partnerLinks"),                   // NOI18N
    PARTNER_LINK("partnerLink"),                    // NOI18N
    CORRELATIONS_WITH_PATTERN("correlations"),      // NOI18N
    CORRELATION_WITH_PATTERN("correlation"),        // NOI18N
    PICK("pick"),                                   // NOI18N    
    RECEIVE("receive"),                             // NOI18N
    SCOPE("scope"),                                 // NOI18N
    SEQUENCE("sequence"),                           // NOI18N
    SOURCES("sources"),                             // NOI18N
    TARGETS("targets"),                             // NOI18N
    SOURCE("source"),                               // NOI18N
    TARGET("target"),                               // NOI18N
    THROW("throw"),                                 // NOI18N
    TO("to"),                                       // NOI18N
    VARIABLES("variables"),                         // NOI18N
    VARIABLE("variable"),                           // NOI18N
    WAIT("wait"),                                   // NOI18N
    WHILE("while"),                                 // NOI18N
    ELSE("else"),                                   // NOI18N
    THEN("then"),                                   // NOI18N
    ON_EVENT("onEvent"),                            // NOI18N
    ON_ALARM_EVENT("onAlarm"),                      // NOI18N
    EXTENSIBLE_ASSIGN( "extensibleAssign"),         // NOI18N
    EXTENSION_ASSIGN_OPERATION( "extensionAssignOperation"), // NOI18N
    ON_ALARM_PICK("onAlarm"),                       // NOI18N
    TERMINATION_HANDLER("terminationHandler"),      // NOI18N
    CONDITION("condition"),                         // NOI18N
    FOR( "for" ),                                   // NOI18N
    UNTIL( "until"),                                // NOI18N
    FOR_EACH( "forEach"),                           // NOI18N
    IF( "if" ),                                     // NOI18N
    REPEAT_UNTIL( "repeatUntil" ),                  // NOI18N
    RETHROW( "rethrow"),                            // NOI18N
    EXIT( "exit" ),                                 // NOI18N
    VALIDATE( "validate" ),                         // NOI18N
    DOCUMENTATION( "documentation" ),               // NOI18N
    EXTENSIONS( "extensions" ),                     // NOI18N
    IMPORT( "import" ),                             // NOI18N
    EXTENSION( "extension" ),                       // NOI18N
    REPEAT_EVERY( "repeatEvery" ),                  // NOI18N
    TRANSITION_CONDITION("transitionCondition"),    // NOI18N
    JOIN_CONDITION( "joinCondition" ),              // NOI18N
    FROM_PART( "fromPart" ),                        // NOI18N
    FROM_PARTS( "fromParts" ),                      // NOI18N
    TO_PART( "toPart" ),                            // NOI18N
    TO_PARTS( "toParts" ),                          // NOI18N
    LITERAL("literal"),                             // NOI18N
    ELSE_IF( "elseif" ),                            // NOI18N
    COMPLETION_CONDITION( "completionCondition" ),  // NOI18N
    BRANCHES("branches"),                           // NOI18N
    START_COUNTER_VALUE( "startCounterValue" ),     // NOI18N
    FINAL_COUNTER_VALUE( "finalCounterValue" ),     // NOI18N
    MESSAGE_EXCHAGES( "messageExchanges" ),         // NOI18N
    MESSAGE_EXCHAGE( "messageExchange"),            // NOI18N
    SERVICE_REF( "service-ref", ServiceRef.SERVICE_REF_NS), // NOI18N 
    COMPENSATE_SCOPE( "compensateScope" ),          // NOI18N
    QUERY( "query" ),                               // NOI18N          
    ;
    
    BpelElements( String name ) {
        this ( name , null );
    }
    
    BpelElements( String name , String nsUri ) {
        myTag = name;
        myNS = nsUri;
    }

    /**
     * @return Name of tag.
     */
    public String getName() {
        return myTag;
    }
    
    /**
     * @return namespace if any 
     */
    public String getNamespace() {
        return myNS;
    }

    /**
     * @return QName of tag.
     */
    public QName getQName() {
        if ( getNamespace() == null ) {
            return new QName(BpelEntity.BUSINESS_PROCESS_NS_URI, getName());
        }
        else {
            return new QName( getNamespace() , getName() );
        }
    }

    /**
     * @return All set of qnames in BPEL.
     */
    public static Set<QName> allQNames() {
        return QNAMES;
    }

    private final String myTag;
    
    private final String myNS;
    
    private static final Set<QName> QNAMES = new HashSet<QName>();
    
    static {
        for (BpelElements v : values()) {
            QNAMES.add(v.getQName());
        }
    }
}
