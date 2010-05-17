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
package com.sun.rave.web.ui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Iterator;
import com.sun.rave.web.ui.model.scheduler.RepeatInterval; 
import com.sun.rave.web.ui.model.scheduler.RepeatUnit; 

/**
 *
 * @author avk
 */
public class ScheduledEvent implements Serializable {
    
    /**
     * The start time, as a java.util.Date
     */
    private Date startTime = null;
    
   /**
     * The end time, as a java.util.Date
     */
    private Date endTime = null;
    
    private ArrayList dateList = null; 
    
     /**
     * Whether the event is repeating or not
     */
    private boolean repeatingEvent = false; 
    
    private final static boolean DEBUG = false;

    /**
     * Retrieves the start time, as a java.util.Date
     * @return The start time, as a java.util.Date
     */
    public Date getStartTime() {

        return this.startTime;
    }

    /**
     * Sets the start time
     * @param startTime The start time, as a java.util.Date
     */
    public void setStartTime(Date startTime) {
        dateList = null;
        this.startTime = startTime;
    }

    /**
     * The end time, as a java.util.Date
     * @return The end time, as a java.util.Date
     */
    public Date getEndTime() {

        return this.endTime;
    }

    /**
     * Setter for The end time, as a java.util.Date
     * @param endTimeThe end time, as a java.util.Date
     */
    public void setEndTime(Date endTime) {

        this.endTime = endTime;
        dateList = null;
    }
    
    public String toString() { 
        StringBuffer buffer = new StringBuffer(128); 
        buffer.append(this.getClass().getName());
        buffer.append(": Start time: "); 
        buffer.append(startTime.toString()); 
        if(endTime != null) { 
             buffer.append("\tEnd time: "); 
             buffer.append(endTime.toString()); 
             buffer.append(" ");
        } 
        else { 
            buffer.append("\tNo end time. ");
        } 
        if(isRepeatingEvent()) { 
            buffer.append("\tThis is a repeating event. "); 
            buffer.append("\t Repeat frequency (Calendar.field): "); 
            buffer.append(String.valueOf(frequency)); 
            if(duration != null) { 
                buffer.append("\tLimited duration of repeats."); 
                buffer.append("\tDuration is "); 
                buffer.append(String.valueOf(duration)); 
                buffer.append(" of unit (in Calendar.field) "); 
                buffer.append(String.valueOf(durationUnit));
            }
        } 
        else { 
            buffer.append("\tThis is not a repeating event. "); 
        }
        return buffer.toString(); 
    } 

   
    /**
     * If true, indicates that this is a repeating event
     * @return true it this is a repeating event, false otherwise
     */
    public boolean isRepeatingEvent() {

        return this.repeatingEvent;
    }

    /**
     * Invoke this method with the value true to indicate that the event
     * is repeating, false if it is not repeating
     * @param repeatingEvent whether the event is repeating
     */
    public void setRepeatingEvent(boolean repeatingEvent) {

        this.repeatingEvent = repeatingEvent;
    }

    /**
     * Holds value of property frequency.
     */
    private RepeatInterval frequency = null;

    /**
     * <p>Get the repeat frequency. The value must be the Integer value 
     * of a calendar field identifier (Calendar.HOUR_OF_DAY, etc). See 
     * <code>java.util.Calendar</code> for details. </p>
     * <p>To specify that the event repeats weekely... </p>
     * @return Value of property frequency.
     */
    public RepeatInterval getRepeatInterval() {

        return this.frequency;
    }

    /**
     * <p>Setter for the repeat frequency. The new value must be the 
     * Integer value 
     * of a calendar field identifier (Calendar.HOUR_OF_DAY, etc). See 
     * <code>java.util.Calendar</code> for details. </p>
     * <p>To specify that the event repeats weekly... </p>
     * @param frequency New value of property frequency.
     */
    public void setRepeatInterval(RepeatInterval frequency) {

        this.frequency = frequency;
        dateList = null;
    }

    /**
     *  * <p>The repeat frequency. The value must be the Integer value 
     * of a calendar field identifier (Calendar.HOUR_OF_DAY, etc). See 
     * <code>java.util.Calendar</code> for details. </p>
     * <p>To specify that the event repeats for three months... </p>
     */
    private RepeatUnit durationUnit = null;

    /**
      * <p>Get the unit (hours, weeks, days, etc) for the duration interval 
     * of the event. The value must be the Integer value 
     * of a calendar field identifier (Calendar.HOUR_OF_DAY, etc). See 
     * <code>java.util.Calendar</code> for details. </p>
      * <p>To specify that the event repeats for three months... </p>
     * @return Value of property durationUnit.
     */
    public RepeatUnit getDurationUnit() {

        return this.durationUnit;
    }

    /**
      * <p>Set the unit (hours, weeks, days, etc) for the duration interval 
     * of the event. The value must be the Integer value 
     * of a calendar field identifier (Calendar.HOUR_OF_DAY, etc). See 
     * <code>java.util.Calendar</code> for details. </p>
     * 
     * <p>To specify that the event repeats for three months... </p>
     * @param durationUnit New value of property durationUnit.
     */
    public void setDurationUnit(RepeatUnit durationUnit) {

        this.durationUnit = durationUnit;
        dateList = null;
    }

    /**
     * Holds value of property duration.
     */
    private Integer duration = null;

    /**
       * <p>Get the number of units (see DurationUnit) for the duration interval 
     * of the event. The value must be the Integer value 
     * of a calendar field identifier (Calendar.HOUR_OF_DAY, etc). See 
     * <code>java.util.Calendar</code> for details. </p>
     * <p>To specify that the event repeats for three months... </p>
     * @return Value of property duration.
     */
    public Integer getDuration() {

        return this.duration;
    }

    /**
     *  <p>Set the number of units (see DurationUnit) for the duration interval 
     * of the event. The value must be the Integer value 
     * of a calendar field identifier (Calendar.HOUR_OF_DAY, etc). See 
     * <code>java.util.Calendar</code> for details. </p>
     * <p>To specify that the event repeats weekely... </p>
     * @param duration New value of property duration.
     */
    public void setDuration(Integer duration) {

        this.duration = duration;
        dateList = null;
    }

    public boolean equals(Object object) { 
        if(object == null) { 
            return false; 
        } 
        if((object instanceof ScheduledEvent)) { 
            return false; 
        } 
        ScheduledEvent event = (ScheduledEvent)object;
        if(event.getStartTime() == getStartTime() &&
           event.getEndTime() == getEndTime() &&
           event.getDuration() == getDuration() && 
           event.getDurationUnit() == getDurationUnit() && 
           event.getRepeatInterval() == event.getRepeatInterval()) { 
            return true; 
        } 
        return false; 
    }
    
    /** Returns an iterator of dates which mark the start of scheduled event.
     * If no time has been set, an empty iterator is returned.
     * If a time has been set and the event is not repeating, an iterator
     * with a single date corresponding to the start time is returned
     * provided it is before the date specified in untilDate.
     * If the event is repeating, all start times before untilDate are
     * returned.
     * @return a java.util.Iterator whose items are java.util.Calendar
     */
    public Iterator getDates(Calendar untilDate) {
        return getDates(null, untilDate); 
    } 
    
    public Iterator getDates(Calendar fromDate, Calendar untilDate) { 
        
        if(dateList != null) { 
            return dateList.iterator();
        } 
        
        dateList = new ArrayList();
        Date date = getStartTime();
               
        if(DEBUG) {
            if(date != null) {
                log("First event on " + date.toString()); //NOI18N
            } else {
                log("No events scheduled"); //NOI18N
            }
        }
        
        Date from = null;
        if(fromDate != null) {
            if(DEBUG) log("Start date is " + fromDate.getTime().toString());//NOI18N
             from = fromDate.getTime();
        }
        else if(DEBUG) { 
           log("No start date");  
        } 
         
        if(DEBUG) log("End date is " + untilDate.getTime().toString()); 
        
        if(date != null && date.before(untilDate.getTime())) {
            
            Calendar startDate = (Calendar)(untilDate.clone());
            startDate.setTime(date);
                    
            dateList.add(startDate.clone());
            if(DEBUG) log("Added date " + date.toString()); 
            
            if(isRepeatingEvent()) { 
                
                int interval = getRepeatInterval().getCalendarField().intValue(); 
                if(interval > -1) {
                    
                    if(DEBUG) log("Repeating event"); 
                
                    Calendar endCalendar = (Calendar)(untilDate.clone());
                    
                    Integer duration = getDuration();
                    RepeatUnit repeatUnit = getDurationUnit(); 
                    Integer durationUnit = null; 
                    if(repeatUnit != null) { 
                        durationUnit = repeatUnit.getCalendarField();
                    }
                    if(duration != null && durationUnit != null) {
                        int durationValue = duration.intValue();
                        durationValue--;
                        
                        int durationField = durationUnit.intValue();
                        if(durationValue > 0) {
                            endCalendar = (Calendar)(startDate.clone());
                            endCalendar.add(durationField, durationValue);
                            endCalendar.getTime();
                        }
                    }
                   
                    Date end = endCalendar.getTime(); 
                    if(DEBUG) log("Using end date " + end.toString()); 
                    
                    Date current = startDate.getTime(); 
                    
                    while(current.before(end)) {
                        
                        startDate.add(interval, 1);
                        current = startDate.getTime();
                        if(from != null) {
                            if(current.after(from)) {
                                dateList.add(startDate.clone());
                                if(DEBUG) log("Added date " + current.toString());
                            }
                        } else {
                            dateList.add(startDate.clone());
                            if(DEBUG) log("Added date " + current.toString());              
                        }
                    }
                }
            }
        }
        return dateList.iterator();
    }
    
    private void log(String s) {
        System.out.println(this.getClass().getName() + "::" + s);
    }
    
}
