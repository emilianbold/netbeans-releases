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
package org.netbeans.modules.bpel.editors.api;

import org.netbeans.modules.soa.ui.SoaConstants;
import org.openide.util.NbBundle;

/**
 * Keeps different reusable constants.
 *
 * @author nk160297
 */
public interface BpelEditorConstants extends SoaConstants {
    
    String BPEL_SOURCEMV_PREFFERED_ID = "bpelsource"; // NOI18N
    String BPEL_DESIGNMV_PREFFERED_ID = "orch-designer"; // NOI18N
    String BPEL_MAPPERMV_PREFFERED_ID = "bpel-business-rules"; // NOI18N
    String BPEL_LOGGINGMV_PREFFERED_ID = "bpel-logging-alerting"; // NOI18N
    
    enum AlarmType {
        INVALID(BpelEditorConstants.INVALID),
        NOT_ASSIGNED(BpelEditorConstants.NOT_ASSIGNED),
        FOR_TIME,
        UNTIL_TIME;
        
        private String myDisplayName;
        
        private AlarmType() {
        }
        
        private AlarmType(String displayName) {
            myDisplayName = displayName;
        }
        
        public String getDisplayName() {
            if (myDisplayName == null) {
                myDisplayName = NbBundle.getMessage(
                        BpelEditorConstants.class, this.toString());
            }
            return myDisplayName;
        }
        
        /**
         * Returns enumeration via its string representation.
         * @param str string that represent enumeration.
         * @return enumeration.
         */
        public static AlarmType forString(String str) {
            if ( str == null || str.length() == 0){
                return AlarmType.NOT_ASSIGNED;
            }
            AlarmType[] values = AlarmType.values();
            for (AlarmType alarmPickType : values) {
                if (alarmPickType.getDisplayName().equals(str)) {
                    return alarmPickType;
                }
            }
            return AlarmType.INVALID;
        }
    }
    
    enum AlarmEventType {
        INVALID(BpelEditorConstants.INVALID),
        NOT_ASSIGNED(BpelEditorConstants.NOT_ASSIGNED),
        FOR_TIME,
        UNTIL_TIME,
        REPEAT_TIME,
        FOR_REPEAT_TIME,
        UNTIL_REPEAT_TIME;
        
        private String myDisplayName;
        
        private AlarmEventType() {
        }
        
        private AlarmEventType(String displayName) {
            myDisplayName = displayName;
        }
        
        public String getDisplayName() {
            if (myDisplayName == null) {
                myDisplayName = NbBundle.getMessage(
                        BpelEditorConstants.class, this.toString());
            }
            return myDisplayName;
        }
        
        /**
         * Returns enumeration via its string representation.
         * @param str string that represent enumeration.
         * @return enumeration.
         */
        public static AlarmEventType forString(String str) {
            if ( str == null || str.length() == 0){
                return AlarmEventType.NOT_ASSIGNED;
            }
            AlarmEventType[] values = AlarmEventType.values();
            for (AlarmEventType alarmEventType : values) {
                if (alarmEventType.getDisplayName().equals(str)) {
                    return alarmEventType;
                }
            }
            return AlarmEventType.INVALID;
        }
    }
}
