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

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Describes the scheduler model.
 * 
 * @author sunsoabi_edwong
 */
public interface SchedulerModel {
    
    void setSuppressPropertyChangeEvent(boolean suppress);
    
    String getGroup();
    void setGroup(String group);
    
    String getDateFormat();
    void setDateFormat(String dateFormat);
    
    String getMode();
    void setMode(String mode);
    
    String getStart();
    void setStart(String start);
    
    String getEnd();
    void setEnd(String end);
    
    String getTimeZone();
    void setTimeZone(String timezone);
    
    List<TriggerDetail> getTriggers();
    void setTriggers(List<TriggerDetail> triggers);
    TriggerDetail createTriggerDetail();
    void addTrigger(TriggerDetail td);
    void editTrigger(int index);
    void removeTrigger(int index);
    
    public interface TriggerDetail {
        
        String getName();
        void setName(String name);
        
        String getType();
        void setType(String type);
        
        boolean isEnabled();
        void setEnabled(boolean enabled);
        
        String getDescription();
        void setDescription(String description);
        
        String getRepeat();
        void setRepeat(String repeat);
        
        long getInterval();
        void setInterval(long interval);
        
        String getCronExpression();
        void setCronExpression(String expression);
        
        long getDuration();
        void setDuration(long duration);
        
        String getMessage();
        void setMessage(String message);
    }
    
    void addPropertyChangeListener(PropertyChangeListener pcl);
    void removePropertyChangeListener(PropertyChangeListener pcl);
    void firePropertyChange(String propKey, Object oldVal, Object newVal);
}
