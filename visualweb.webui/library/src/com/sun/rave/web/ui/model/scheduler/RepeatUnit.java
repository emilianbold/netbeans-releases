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
package com.sun.rave.web.ui.model.scheduler;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import javax.faces.context.FacesContext;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.ThemeUtilities;

import javax.faces.context.FacesContext;

// Delete the setters once you have reimplemented this not to
// use the default Serializable mechanism, but the same as
// in the converter....

public class RepeatUnit implements Serializable {

    public final static String HOURS = "HOURS";
    public final static String DAYS = "DAYS";
    public final static String WEEKS = "WEEKS";
    public final static String MONTHS = "MONTHS";

    private static final boolean DEBUG = false;

    private static RepeatUnit HOURS_RI = null;
    private static RepeatUnit DAYS_RI = null;
    private static RepeatUnit WEEKS_RI = null;
    private static RepeatUnit MONTHS_RI = null;
 
  
    private Integer calField = null;
    private String key = null; 
    private String representation = null;
    
    public RepeatUnit() { 
    }
    
    public RepeatUnit(int calFieldInt, String key, String rep) {
        if(DEBUG) log("Create new RU"); 
        this.calField = new Integer(calFieldInt); 
        this.key = key;
        this.representation = rep;     
        if(DEBUG) log("Representation is " + this.representation); 
    }
    
    public static RepeatUnit getInstance(String representation) {
        
        if(DEBUG) log("getInstance(" + representation + ")"); 
       
        if(representation.equals(HOURS)) {
            if(HOURS_RI == null) {
                HOURS_RI =  new RepeatUnit(Calendar.HOUR_OF_DAY, "Scheduler.hours", HOURS);
            }
            return  HOURS_RI;
        }
        if(representation.equals(DAYS)) {
            if(DAYS_RI == null) {
                DAYS_RI =  new RepeatUnit(Calendar.DATE, "Scheduler.days", DAYS);
            }
            return DAYS_RI;
        }
        if(representation.equals(WEEKS)) {
            if(WEEKS_RI == null) {
                WEEKS_RI =  new RepeatUnit(Calendar.WEEK_OF_YEAR, "Scheduler.weeks", WEEKS);
            }
            return WEEKS_RI; 
        }
        if(representation.equals(MONTHS)) {
            if(MONTHS_RI == null) {
                MONTHS_RI =  new RepeatUnit(Calendar.MONTH, "Scheduler.months", MONTHS);
            }
            return MONTHS_RI;
        }
        return null;
    }
    /**
     * Getter for property calendarField.
     * @return Value of property calendarField.
     */
    public Integer getCalendarField() {
        return calField;
    }
    
    /**
     * Setter for property calendarField.
     * @return Value of property calendarField.
     */
    public void setCalendarField(Integer calField) {
        this.calField = calField;
    }
   
    public void setKey(String key) { 
        this.key = key; 
    } 
    
    public String getKey() { 
        return key;        
    } 

    public void setRepresentation(String representation) { 
        this.representation = representation; 
    } 
    
    public String getRepresentation() { 
        return representation;        
    } 
    /**
     * Getter for property labelKey.
     * @return Value of property labelKey.
     */
    public String getLabel(FacesContext context) {
        return ThemeUtilities.getTheme(context).getMessage(key);
    }
    
    public boolean equals(Object object) { 
        if(object == null) { 
            return false; 
        }
        if(!(object instanceof RepeatUnit)) { 
            return false; 
        }
        return (((RepeatUnit)object).getRepresentation().equals(representation)); 
    } 
    
    private static void log(String s) { 
        System.out.println("RepeatUnit::" + s);
    }
}

