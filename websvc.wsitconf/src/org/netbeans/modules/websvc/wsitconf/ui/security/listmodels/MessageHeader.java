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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui.security.listmodels;

import org.openide.util.NbBundle;

public class MessageHeader extends TargetElement {

    public static final String ADDRESSING_TO=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_To");  //NOI18N
    public static final String ADDRESSING_FROM=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_From");  //NOI18N
    public static final String ADDRESSING_FAULTTO=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_FaultTo");  //NOI18N
    public static final String ADDRESSING_REPLYTO=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_ReplyTo");  //NOI18N
    public static final String ADDRESSING_MESSAGEID=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_MessageId");  //NOI18N
    public static final String ADDRESSING_RELATESTO=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_RelatesTo");  //NOI18N
    public static final String ADDRESSING_ACTION=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_Action");  //NOI18N
    public static final String RM_ACKREQUESTED=NbBundle.getMessage(MessageHeader.class, "COMBO_RM_AckRequested");  //NOI18N
    public static final String RM_SEQUENCEACK=NbBundle.getMessage(MessageHeader.class, "COMBO_RM_SequenceAck");  //NOI18N
    public static final String RM_SEQUENCE=NbBundle.getMessage(MessageHeader.class, "COMBO_RM_Sequence");  //NOI18N

    public static final String[] ALL_HEADERS  = new String[] { 
        ADDRESSING_TO, 
        ADDRESSING_FROM, 
        ADDRESSING_FAULTTO, 
        ADDRESSING_REPLYTO, 
        ADDRESSING_MESSAGEID,
        ADDRESSING_RELATESTO,
        ADDRESSING_ACTION,
        RM_ACKREQUESTED,
        RM_SEQUENCEACK,
        RM_SEQUENCE
    };

//    public enum ADDRESSING_HEADERS { 
//        ADDRESSING_TO, 
//        ADDRESSING_FROM, 
//        ADDRESSING_FAULTTO, 
//        ADDRESSING_REPLYTO, 
//        ADDRESSING_MESSAGEID,
//        ADDRESSING_RELATESTO,
//        ADDRESSING_ACTION,
//    };
//
//    public enum RM_HEADERS { 
//        RM_ACKREQUESTED,
//        RM_SEQUENCEACK,
//        RM_SEQUENCE
//    };
    
    public MessageHeader(String header) {
        super(header);
    }
 
}