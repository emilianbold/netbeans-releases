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

package org.netbeans.modules.iep.editor.wizard.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author radval
 */
public class DatabaseTableWizardConstants {

    public static final String PROP_SELECTED_TABLES = "PROP_SELECTED_TABLES";
    
    public static final String PROP_SELECTED_COLUMNS = "PROP_SELECTED_COLUMNS";
    
    public static final String PROP_SELECTED_DB_CONNECTION = "PROP_SELECTED_DB_CONNECTION";
    
    public static final String PROP_JOIN_CONDITION = "PROP_JOIN_CONDITION";
    
    public static final String PROP_POLLING_INTERVAL = "PROP_POLLING_INTERVAL";
    
    public static final String PROP_POLLING_INTERVAL_TIME_UNIT = "PROP_POLLING_INTERVAL_TIME_UNIT";
    
    public static final String PROP_POLLING_RECORD_SIZE = "PROP_POLLING_RECORD_SIZE";
    
    public static final String PROP_JNDI_NAME = "PROP_JNDI_NAME";
    
    public static final String PROP_IS_DELETE_RECORDS = "PROP_IS_DELETE_RECORDS";
    
    public static final String NANOSECOND = "nanosecond"; //NO I18N
    
    public static final String MICROSECOND = "microsecond"; //NO I18N
    
    public static final String MILLISECOND = "millisecond"; //NO I18N
    
    public static final String SECOND = "second"; //NO I18N
    
    public static final String MINUTE = "minute"; //NO I18N
    
    public static final String HOUR = "hour"; //NO I18N
    
    public static final String DAY = "day"; //NO I18N
    
    public static final String WEEK = "week"; //NO I18N
    
    public static final String MONTH = "month"; //NO I18N
    
    public static final String YEAR = "year"; //NO I18N
    
    private static List<TimeUnitInfo> mTimeUnitInfoList = new ArrayList<TimeUnitInfo>();
    
    public static TimeUnitInfo TIMEUNIT_NANOSECOND = new TimeUnitInfo("nanosecond", DatabaseTableWizardConstants.NANOSECOND);
    public static TimeUnitInfo TIMEUNIT_MICROSECOND = new TimeUnitInfo("microsecond", DatabaseTableWizardConstants.MICROSECOND);
    public static TimeUnitInfo TIMEUNIT_MILLISECOND = new TimeUnitInfo("millisecond", DatabaseTableWizardConstants.MILLISECOND);
    public static TimeUnitInfo TIMEUNIT_SECOND = new TimeUnitInfo("second", DatabaseTableWizardConstants.SECOND);
    public static TimeUnitInfo TIMEUNIT_MINUTE = new TimeUnitInfo("minute", DatabaseTableWizardConstants.MINUTE);
    public static TimeUnitInfo TIMEUNIT_HOUR = new TimeUnitInfo("hour", DatabaseTableWizardConstants.HOUR);
    public static TimeUnitInfo TIMEUNIT_DAY = new TimeUnitInfo("day", DatabaseTableWizardConstants.DAY);
    public static TimeUnitInfo TIMEUNIT_WEEK = new TimeUnitInfo("week", DatabaseTableWizardConstants.WEEK);
    
    static {
        mTimeUnitInfoList.add(TIMEUNIT_NANOSECOND);
        mTimeUnitInfoList.add(TIMEUNIT_MICROSECOND);
        mTimeUnitInfoList.add(TIMEUNIT_MILLISECOND);
        mTimeUnitInfoList.add(TIMEUNIT_SECOND);
        mTimeUnitInfoList.add(TIMEUNIT_MINUTE);
        mTimeUnitInfoList.add(TIMEUNIT_HOUR);
        mTimeUnitInfoList.add(TIMEUNIT_DAY);
        mTimeUnitInfoList.add(TIMEUNIT_WEEK);
//        mTimeUnitInfoList.add(new TimeUnitInfo("months", DatabaseTableWizardConstants.MONTH));
//        mTimeUnitInfoList.add(new TimeUnitInfo("years", DatabaseTableWizardConstants.YEAR));
    }
    
    public static List<TimeUnitInfo> getTimeUnitInfos() {
        return mTimeUnitInfoList;        
    }
    
    public static List<String> getTimeUnitInfosDisplayName() {
        List<String> timeUnitDisplayNames = new ArrayList<String>();
        Iterator<TimeUnitInfo> it = mTimeUnitInfoList.iterator();
        while(it.hasNext()) {
            TimeUnitInfo info = it.next();
            timeUnitDisplayNames.add(info.getDisplayName());
        }
        
        return timeUnitDisplayNames;
    }
    
    public static List<String> getTimeUnitInfosCodeName() {
        List<String> timeUnitCodeNames = new ArrayList<String>();
        Iterator<TimeUnitInfo> it = mTimeUnitInfoList.iterator();
        while(it.hasNext()) {
            TimeUnitInfo info = it.next();
            timeUnitCodeNames.add(info.getCodeName());
        }
        
        return timeUnitCodeNames;
    }
    
    public static class TimeUnitInfo {
        private String mDisplayName;
        
        private String mCodeName;
        
        TimeUnitInfo(String displayName, String codeName) {
            this.mDisplayName = displayName;
            this.mCodeName = codeName;
        }
        
        
        public String getDisplayName() {
            return this.mDisplayName;
        }
        
        public String getCodeName() {
            return this.mCodeName;
        }
        
        public String toString() {
            return this.mCodeName;
        }
    }
}
