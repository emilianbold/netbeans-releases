/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.wsdlextensions.scheduler.model.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel;

/**
 * Implements the scheduler model.
 * 
 * @author sunsoabi_edwong
 */
public class SchedulerModelImpl implements SchedulerModel, SchedulerConstants {
    
    private String group;
    private String dateFormat;
    private String mode;
    private String start;
    private String end;
    private String timezone;
    private List<TriggerDetail> triggers;
    private PropertyChangeSupport pcs;
    private boolean suppress;
    
    private static final String NULINE =
            System.getProperty("line.separator");                       //NOI18N
    
    public SchedulerModelImpl() {
        super();
        
        setTriggers(new ArrayList<TriggerDetail>());
        pcs = new PropertyChangeSupport(this);
    }
    
    public void setSuppressPropertyChangeEvent(boolean suppress) {
        this.suppress = suppress;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        String oldGroup = this.group;
        this.group = group;
        if (!suppress) {
            firePropertyChange(GROUP_CHANGED, oldGroup, group);
        }
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        String oldDateFormat = this.dateFormat;
        this.dateFormat = dateFormat;
        if (!suppress) {
            firePropertyChange(DATEFORMAT_CHANGED, oldDateFormat, dateFormat);
        }
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        String oldStart = this.start;
        this.start = start;
        if (!suppress) {
            firePropertyChange(START_CHANGED, oldStart, start);
        }
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        String oldEnd = this.end;
        this.end = end;
        if (!suppress) {
            firePropertyChange(END_CHANGED, oldEnd, this.end);
        }
    }

    public String getTimeZone() {
        return timezone;
    }

    public void setTimeZone(String timezone) {
        this.timezone = timezone;
    }

    public List<TriggerDetail> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<TriggerDetail> triggers) {
        this.triggers = triggers;
    }
    
    public TriggerDetail createTriggerDetail() {
        return new TriggerDetailImpl();
    }
    
    public void addTrigger(TriggerDetail td) {
        triggers.add(td);
        if (!suppress) {
            firePropertyChange(TRIGGER_ADDED, null,
                    new Integer(triggers.indexOf(td)));
        }
    }
    
    public void editTrigger(int index) {
        if (!suppress) {
            firePropertyChange(TRIGGER_EDITED, null, new Integer(index));
        }
    }
    
    public void removeTrigger(int index) {
        if ((index >= 0) && (index < triggers.size())) {
            TriggerDetail td = triggers.get(index);
            triggers.remove(td);
            if (!suppress) {
                firePropertyChange(TRIGGER_REMOVED, null, new Integer(index));
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    public void firePropertyChange(String propKey,
            Object oldVal, Object newVal) {
        if (suppress) {
            return;
        }
        pcs.firePropertyChange(propKey, oldVal, newVal);
    }
    
    @Override
    public String toString() {
        StringBuffer sb = (new StringBuffer())
                .append("group:").append(group).append(NULINE)          //NOI18N
                .append("dateFormat:").append(dateFormat).append(NULINE)//NOI18N
                .append("start:").append(start).append(NULINE)          //NOI18N
                .append("end:").append(end).append(NULINE);             //NOI18N
        if (timezone != null) {
            sb.append("timezone:").append(timezone).append(NULINE);     //NOI18N
        }
        sb.append("triggers==========").append(NULINE);                 //NOI18N
        if (triggers != null) {
            for (TriggerDetail trigger : triggers) {
                if (triggers.size() > 1) {
                    sb.append("=====").append(NULINE);                  //NOI18N
                }
                sb.append(trigger.toString());
            }
        }
        sb.append("==========").append(NULINE);                         //NOI18N
        return sb.toString();
    }

    public class TriggerDetailImpl implements TriggerDetail {

        private String name;
        private String type;
        private boolean enabled = true;
        private String description;
        private String repeat;
        private long interval;
        private String cronExpr;
        private long duration;
        private String message;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRepeat() {
            return repeat;
        }

        public void setRepeat(String repeat) {
            this.repeat = repeat;
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        public String getCronExpression() {
            return cronExpr;
        }

        public void setCronExpression(String expression) {
            cronExpr = expression;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public void setDuration(long duration) {
            this.duration = duration;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        
        @Override
        public String toString() {
            return new StringBuilder(toAttrNL(SchedulerAttribute.TYPE, type))
                .append(toAttrNL(SchedulerAttribute.NAME, name))
                .append(toAttrNL(SchedulerAttribute.ENABLED, enabled))
                .append(toAttrNL(SchedulerAttribute.DESCRIPTION, description))
                .append(toAttrNL(SchedulerAttribute.REPEAT, repeat))
                .append(toAttrNL(SchedulerAttribute.INTERVAL, interval))
                .append(toAttrNL(SchedulerAttribute.CRON_EXPR, cronExpr))
                .append(toAttrNL(SchedulerAttribute.DURATION, duration))
                .append(toAttrNL(SchedulerAttribute.MESSAGE, message))
                .toString();
        }
        
        private String toAttrNL(SchedulerAttribute label, long val) {
            return toAttrNL(label, "" + val);                           //NOI18N
        }
        
        private String toAttrNL(SchedulerAttribute label, boolean val) {
            return toAttrNL(label, "" + val);                           //NOI18N
        }
        
        private String toAttrNL(SchedulerAttribute label, String val) {
            return label.getName() + ":" + val + NULINE;                //NOI18N
        }
    }
}
