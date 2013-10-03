/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.spi;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * Provides access to a bugtracking Issue
 *
 * @author Tomas Stupka
 */
public abstract class IssueProvider<I> {

    /**
     * issue data were refreshed
     */
    public static final String EVENT_ISSUE_REFRESHED = "issue.data_changed"; // NOI18N

    static {
        SPIAccessorImpl.createAccesor();
    }

    public IssueStatusProvider getStatusProvider() {
        return null;
    }
    
    /**
     * Returns this issues display name
     * @return
     */
    public abstract String getDisplayName(I data);


    /**
     * Returns this issues tooltip
     * @return
     */
    public abstract String getTooltip(I data);

    /**
     * Returns this issues unique ID
     * @return
     */
    public abstract String getID(I data);
    
    /**
     * Returns the ID-s of all issues where this one could be consideret 
     * being superordinated to them. 
     * e.g. the blocks/depends relationship in Bugzilla, or subtask/parenttask in JIRA
     * 
     * 
     * @param data
     * @return 
     */
    public abstract String[] getSubtasks(I data);

    /**
     * Returns this issues summary
     * @return
     */
    public abstract String getSummary(I data);

    /**
     * Returns true if the issue isn't stored in a repository yet. Otherwise false.
     * @return
     */
    public abstract boolean isNew(I data);
    
    /**
     * Determines if the issue is considered finished 
     * in the means of the particular bugtracking.
     * 
     * @param data
     * @return true if finished, otherwise false
     */
    public abstract boolean isFinished(I data);

    /**
     * Refreshes this Issues data from its bugtracking repository
     *
     * @return true if the issue was refreshed, otherwise false
     * XXX obsolete since repositoryprovider.refreshIssues ?
     */
    public abstract boolean refresh(I data);

    /**
     * Add a comment to this issue and close it as fixed eventually.
     * 
     * @param comment
     * @param closeAsFixed 
     */
    // XXX throw exception
    // XXX provide way so that we know commit hooks are supported
    public abstract void addComment(I data, String comment, boolean closeAsFixed);

    /**
     * Attach a file to this issue
     * @param file
     * @param description 
     */
    // XXX throw exception; attach Patch or attachFile?
    // XXX provide way so that we know patch attachemnts are supported
    public abstract void attachPatch(I data, File file, String description);

    /**
     * Returns this issues controller
     * XXX we don't need this. use get component instead and get rid of the BugtrackingController
     * @return
     */
    public abstract BugtrackingController getController(I data);

    public abstract void removePropertyChangeListener(I data, PropertyChangeListener listener);

    public abstract void addPropertyChangeListener(I data, PropertyChangeListener listener);
    
    /**
     * Submits the issue. Override and implement if you support issue
     * submitting.
     *
     * @param data issue data
     * @return <code>true</code> if the task was successfully
     * submitted,<code>false</code> if the task was not submitted for any
     * reason.
     */
    public boolean submit (I data) {
        return false;
    }

    /**
     * Returns a {@link SchedulingProvider} in case the particular implementation  
     * support scheduling. 
     * 
     * @return 
     */
    public SchedulingProvider getSchedulingProvider() {
        return null;
    }
    
    /**
     * Provides access to private scheduling data for a given task. It is up to 
     * the particular implementation if the values eventually match with  
     * corresponding remote repository fields or if they are merely handed 
     * locally as user private.
     */
    public interface SchedulingProvider<I> {
        
        /**
         * Sets the due date
         * 
         * @param i
         * @param date 
         */
        public void setDueDate(I i, Date date);
        
        /**
         * Sets the schedule date. 
         * 
         * @param i
         * @param date 
         */
        public void setSchedule(I i, ScheduleDate date);
        
        /**
         * Sets the estimate in hours
         * 
         * @param i
         * @param hours 
         */
        public void setEstimate(I i, int hours); 
        
        /**
         * Returns the due date
         * 
         * @param i
         * @return 
         */
        public Date getDueDate(I i);
        
        /**
         * Returns the schedule date
         * 
         * @param i
         * @return 
         */
        public ScheduleDate getSchedule(I i);
        
        /**
         * Returns the estimate in hours
         * 
         * @param i
         * @return 
         */
        public int getEstimate(I i); 
    }
    
    /**
     * Represents the date period for which an issue is scheduled. 
     * This can be a specific day as well an interval of days. 
     */
    public static final class ScheduleDate {
       
        private final Date date;
        private final int interval;

        /**
         * Creates a ScheduleDate representing a specific day.
         * 
         * @param date 
         */
        public ScheduleDate(Date date) {
            this.date = date;
            this.interval = 0;
        }
        
        /**
         * Creates a ScheduleDate representing an interval of days.
         * 
         * @param startDate determines the day from which this issue is scheduled
         * @param interval determines the interval of days for which an issue is scheduled
         */
        public ScheduleDate(Date startDate, int interval) {
            this.date = startDate;
            this.interval = interval;
        }
        
        /**
         * Returns the start date of this ScheduleDate.
         * 
         * @return 
         */
        public Date getDate() {
            return date;
        }
        
        /**
         * Returns the amount of days given by this SheduleDate.
         * 
         * @return 
         */
        public int getInterval() {
            return interval;
        }
    }
}
