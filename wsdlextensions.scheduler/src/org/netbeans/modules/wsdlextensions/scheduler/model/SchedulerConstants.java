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

import java.text.SimpleDateFormat;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.openide.util.NbBundle;

/**
 *
 * @author sunsoabi_edwong
 */
public interface SchedulerConstants {
    
    public static final String SCHEDULER_NAMESPACE =
            "http://schemas.sun.com/jbi/wsdl-extensions/scheduler/";    //NOI18N
    public static final String SCHED_PREFIX = "sched";                  //NOI18N
    
    public static final String SCHED_BINDING = "binding";               //NOI18N
    public static final String SCHED_OPERATION = "operation";           //NOI18N
    public static final String SCHED_TRIGGER = "trigger";               //NOI18N
    public static final String SCHED_ACTIVEPERIOD = "active-period";    //NOI18N
    
    public static final String TRIGGER_INPUT_NAME = "inMsg";            //NOI18N
    
    public static final QName SCHED_BINDING_QNAME =
            new QName(SCHEDULER_NAMESPACE, SCHED_BINDING, SCHED_PREFIX);
    public static final QName SCHED_OPERATION_QNAME =
            new QName(SCHEDULER_NAMESPACE, SCHED_OPERATION, SCHED_PREFIX);
    public static final QName SCHED_TRIGGER_QNAME =
            new QName(SCHEDULER_NAMESPACE, SCHED_TRIGGER, SCHED_PREFIX);
    public static final QName SCHED_ACTIVEPERIOD_QNAME =
            new QName(SCHEDULER_NAMESPACE, SCHED_ACTIVEPERIOD, SCHED_PREFIX);

    public static final String DATE_FORMAT_KEY = "dateFormat";          //NOI18N
    public static final String TRIGGER_TYPE_KEY = "trigger.type";       //NOI18N
    public static final String TRIGGER_NOW_KEY = "trigger.now";         //NOI18N
    public static final String TRIGGER_NEVER_KEY = "trigger.never";     //NOI18N
    public static final String TIMEZONE_KEY = "timeZone";               //NOI18N
    
    public static final String GROUP_CHANGED = "GROUP_CHANGED";         //NOI18N
    public static final String DATEFORMAT_CHANGED =
            "DATEFORMAT_CHANGED";                                       //NOI18N
    public static final String START_CHANGED = "START_CHANGED";         //NOI18N
    public static final String END_CHANGED = "END_CHANGED";             //NOI18N
    public static final String TRIGGER_ADDED = "TRIGGER_ADDED";         //NOI18N
    public static final String TRIGGER_EDITED = "TRIGGER_EDITED";       //NOI18N
    public static final String TRIGGER_REMOVED = "TRIGGER_REMOVED";     //NOI18N
    
    public enum TriggerType {
        SIMPLE("simple", "SIMPLE_TYPE"),                                //NOI18N
        CRON("cron", "CRON_TYPE"),                                      //NOI18N
        HYBRID("hybrid", "HYBRID_TYPE");                                //NOI18N
        
        private String progName;
        private String displayName;
        private String baseName;
        
        TriggerType(String progName, String displayName) {
            this.progName = progName;
            this.displayName = NbBundle.getMessage(SchedulerConstants.class,
                    displayName);
            this.baseName = NbBundle.getMessage(SchedulerConstants.class,
                    "TRIGGER_NAME_PREFIX", this.displayName);
        }
        
        public String getI18nName() {
            return displayName;
        }
        
        public String getProgName() {
            return progName;
        }
        
        public String getBaseName() {
            return baseName;
        }
        
        public static TriggerType toEnum(String name) {
            for (TriggerType t : TriggerType.values()) {
                if (t.getProgName().equals(name)
                        || t.getI18nName().equals(name)
                        || t.getBaseName().equals(name)) {
                    return t;
                }
            }
            return null;
        }
    }
    
    public static final String NOW_VAL = "now";                         //NOI18N
    public static final String NEVER_VAL = "never";                     //NOI18N
    public static final String INDEFINITE_VAL = "indefinite";           //NOI18N
    
    public static final String INDEFINITE_I18N_VAL = NbBundle.getMessage(
            SchedulerConstants.class, "INDEFINITE_STR");                //NOI18N
    
    public static final SimpleDateFormat LOCALE_DATEFORMAT =
            new SimpleDateFormat();

    public enum TimeUnit {
        SECONDS(1000, "SECONDS_STR"),                                   //NOI18N
        MINUTES(60 * SECONDS.getFactor(), "MINUTES_STR"),               //NOI18N
        HOURS(60 * MINUTES.getFactor(), "HOURS_STR"),                   //NOI18N
        DAYS(24 * HOURS.getFactor(), "DAYS_STR"),                       //NOI18N
        WEEKS(7 * DAYS.getFactor(), "WEEKS_STR");                       //NOI18N
        
        int factor;
        String displayName;
        TimeUnit(int factor, String displayName) {
            this.factor = factor;
            this.displayName = NbBundle.getMessage(SchedulerConstants.class,
                    displayName);
        }
        
        public int getFactor() {
            return factor;
        }
        
        public String getI18nName() {
            return displayName;
        }
        
        public static TimeUnit toEnum(String name) {
            for (TimeUnit t : TimeUnit.values()) {
                if (t.getI18nName().equals(name)) {
                    return t;
                }
            }
            return null;
        }
    }
    
    public enum OperationMode {
        STATIC(1, "static", "STATIC_STR", "StaticSchedule"),            //NOI18N
        DYNAMIC(2, "dynamic", "DYNAMIC_STR", "DynamicSchedule");        //NOI18N
        
        int intVal;
        String progName;
        String i18nName;
        String templateName;
        
        OperationMode(int intVal, String progName, String i18nKey,
                String templateName) {
            this.intVal = intVal;
            this.progName = progName;
            this.i18nName = NbBundle.getMessage(SchedulerConstants.class,
                    i18nKey);
            this.templateName = templateName;
        }
        
        public int intValue() {
            return intVal;
        }
        
        public String getProgName() {
            return progName;
        }
        
        public String getI18nName() {
            return i18nName;
        }
        
        public String getTemplateName() {
            return templateName;
        }
        
        public static OperationMode toEnum(String name) {
            for (OperationMode o : OperationMode.values()) {
                if (o.getProgName().equals(name)
                        || o.getI18nName().equals(name)
                        || o.getTemplateName().equals(name)) {
                    return o;
                }
            }
            return null;
        }
    }
    
    public enum SchedulerAttribute implements Attribute {
        GROUP("group"),                                                 //NOI18N
        DATEFORMAT("date-format"),                                      //NOI18N
        MODE("mode"),                                                   //NOI18N
        NAME("name"),                                                   //NOI18N
        TYPE("type"),                                                   //NOI18N
        ENABLED("enabled", boolean.class),                              //NOI18N
        DESCRIPTION("description"),                                     //NOI18N
        REPEAT("repeat"),                                               //NOI18N
        INTERVAL("interval", long.class),                               //NOI18N
        MESSAGE("message"),                                             //NOI18N
        STARTING("starting"),                                           //NOI18N
        ENDING("ending"),                                               //NOI18N
        CRON_EXPR("cron-expr"),                                         //NOI18N
        TIMEZONE("timezone"),                                           //NOI18N
        DURATION("duration", long.class);                               //NOI18N
        
        private String name;
        private Class type;
        private Class memType;
        
        SchedulerAttribute(String name) {
            this(name, String.class);
        }
        
        SchedulerAttribute(String name, Class type) {
            this(name, type, null);
        }
        
        SchedulerAttribute(String name, Class type, Class memType) {
            this.name = name;
            this.type = type;
            this.memType = memType;
        }
        
        public String getName() {
            return name;
        }

        public Class getType() {
            return type;
        }

        public Class getMemberType() {
            return memType;
        }
        
        public static SchedulerAttribute toEnum(String name) {
            for (SchedulerAttribute s : SchedulerAttribute.values()) {
                if (s.getName().equals(name)) {
                    return s;
                }
            }
            return null;
        }
    }
}
