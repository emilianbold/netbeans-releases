/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.scheduler.model;

import java.awt.event.KeyEvent;
import org.openide.util.NbBundle;

/**
 *
 * @author sunsoabi_edwong
 */
public interface CronConstants {

    public static final String FIELD_SEP = " ";                         //NOI18N
    public static final String LAX_FIELD_SEP = FIELD_SEP + "\t";        //NOI18N
    public static final String DELIM = ",";                             //NOI18N
    public static final String LAX_DELIM = DELIM + " \t";               //NOI18N
    public static final String PRETTY_DELIM = DELIM + " ";              //NOI18N
    public static final String LAST_MODIFIER = "L";                     //NOI18N
    public static final String WEEKDAY_MODIFIER = "W";                  //NOI18N
    public static final String EVERY_MODIFIER = "*";                    //NOI18N
    public static final String RANGE_MODIFIER = "-";                    //NOI18N
    public static final String INTERVAL_MODIFIER = "/";                 //NOI18N
    public static final String ORDINAL_MODIFIER = "#";                  //NOI18N
    public static final String NONSPECIFIC_MODIFIER = "?";              //NOI18N
    
    public static final String DEFAULT_EXPRESSION = new StringBuilder()
            .append("0").append(FIELD_SEP)                              //NOI18N, Second
            .append(EVERY_MODIFIER).append(FIELD_SEP)                   //Minute
            .append(EVERY_MODIFIER).append(FIELD_SEP)                   //Hour
            .append(EVERY_MODIFIER).append(FIELD_SEP)                   //Day
            .append(EVERY_MODIFIER).append(FIELD_SEP)                   //Month
            .append(NONSPECIFIC_MODIFIER)                               //DOW
            .toString();
    
        @SuppressWarnings("serial")
        public enum CronField {
        SECOND(0, "STR_SECOND", KeyEvent.VK_1, 0, 59, null),            //NOI18N
        MINUTE(1, "STR_MINUTE", KeyEvent.VK_2, 0, 59, null),            //NOI18N
        HOUR(2, "STR_HOUR", KeyEvent.VK_3, 0, 23, null),                //NOI18N
        DAY(3, "STR_DAY", KeyEvent.VK_4, 1, 31, null,                   //NOI18N
                true, true, true, false),
        MONTH(4, "STR_MONTH", KeyEvent.VK_5, 1, 12,                     //NOI18N
                new String[] {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", //NOI18N
                        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"}),     //NOI18N
        DAY_OF_WEEK(5, "STR_DAY_OF_WEEK", KeyEvent.VK_6, 1, 7,          //NOI18N
                new String[] {"SUN", "MON", "TUE", "WED",               //NOI18N
                        "THU", "FRI", "SAT"}, 5,                        //NOI18N
                        true, true, false, true),
        YEAR(6, "STR_YEAR", KeyEvent.VK_7, 1970, 2099, null);           //NOI18N
        
        private int order;
        private String dispName;
        private String fieldName;
        private int mnemonic;
        private int[] intRange;
        private String[] keys;
        private int maxOrdinality;
        private boolean nonSpecificAllowed;
        private boolean lastAllowed;
        private boolean weekdayAllowed;
        private boolean ordinalityAllowed;
        private String toolTip;
        
        private CronField(int order, String fieldNameKey, int mnemonic,
                int minVal, int maxVal, String[] keys) {
            this.order = order;
            fieldName = NbBundle.getMessage(CronConstants.class, fieldNameKey);
            this.mnemonic = mnemonic;
            Integer mnemonicNum = new Integer(mnemonic - KeyEvent.VK_0);
            dispName = NbBundle.getMessage(CronConstants.class,
                    "LBL_TAB_NAME", fieldName, mnemonicNum);            //NOI18N
            intRange = new int[] {minVal, maxVal};
            this.keys = keys;
            toolTip = NbBundle.getMessage(CronConstants.class,
                    "LBL_TAB_TOOLTIP", fieldName, mnemonicNum);         //NOI18N
        }
        
        private CronField(int order, String fieldNameKey, int mnemonic,
                int minVal, int maxVal, String[] keys,
                boolean nonSpecificAllowed, boolean lastAllowed,
                boolean weekdayAllowed, boolean ordinalityAllowed) {
            this(order, fieldNameKey, mnemonic, minVal, maxVal, keys);
            this.nonSpecificAllowed = nonSpecificAllowed;
            this.lastAllowed = lastAllowed;
            this.weekdayAllowed = weekdayAllowed;
            this.ordinalityAllowed = ordinalityAllowed;
        }
        
        private CronField(int order, String fieldNameKey, int mnemonic,
                int minVal, int maxVal, String[] keys, int maxOrdinality,
                boolean nonSpecificAllowed, boolean lastAllowed,
                boolean weekdayAllowed, boolean ordinalityAllowed) {
            this(order, fieldNameKey, mnemonic, minVal, maxVal, keys,
                    nonSpecificAllowed, lastAllowed, weekdayAllowed,
                    ordinalityAllowed);
            this.maxOrdinality = maxOrdinality;
        }
        
        public int getOrder() {
            return order;
        }
        
        public String getDisplayName() {
            return dispName;
        }
        
        public int getMnemonic() {
            return mnemonic;
        }
        
        public String getFieldName() {
            return fieldName;
        }
        
        public int[] getIntRange() {
            return intRange;
        }
        
        public String[] getKeys() {
            return keys;
        }
        
        public int getMaxOrdinality() {
            return maxOrdinality;
        }
        
        public boolean isNonSpecificAllowed() {
            return nonSpecificAllowed;
        }
        
        public boolean isLastAllowed() {
            return lastAllowed;
        }
        
        public boolean isWeekdayAllowed() {
            return weekdayAllowed;
        }
        
        public boolean isOrdinalityAllowed() {
            return ordinalityAllowed;
        }

        public String getToolTip() {
            return toolTip;
        }
        
        public static CronField toEnum(String s) {
            for (CronField e : CronField.values()) {
                if (e.getDisplayName().equals(s)) {
                    return e;
                }
            }
            return null;
        }
    }
}
