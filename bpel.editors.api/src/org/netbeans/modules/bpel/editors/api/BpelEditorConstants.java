/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
