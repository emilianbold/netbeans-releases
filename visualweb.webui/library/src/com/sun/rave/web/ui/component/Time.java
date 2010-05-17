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
package com.sun.rave.web.ui.component;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent; 
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.IntegerConverter;
import javax.faces.event.ValueChangeEvent; 
import com.sun.rave.web.ui.model.ClockTime;
import com.sun.rave.web.ui.model.Option; 
import com.sun.rave.web.ui.model.OptionTitle; 
import com.sun.rave.web.ui.theme.Theme; 
import com.sun.rave.web.ui.util.ThemeUtilities;


/**
 *
 * @author avk
 */
public class Time extends TimeBase implements NamingContainer {
    
    public static final String HOUR_FACET = "hour";
    public static final String HOUR_ID = "_hour";
    
    public static final String MINUTES_FACET = "minutes";
    public static final String MINUTES_ID = "_minutes";
    
    private static final String TIME_SUBMITTED = "com.sun.rave.web.ui.TimeSubmitted"; 
    
    private static final boolean DEBUG = false;
    
    public DropDown getHourMenu() {
        return getMenu(HOUR_ID, HOUR_FACET, getHourItems());
    }
    
    public DropDown getMinutesMenu() {
        return getMenu(MINUTES_ID, MINUTES_FACET, getMinuteItems());
    }
    
    private DropDown getMenu(String id, String facet, Option[] options) {
        
        if(DEBUG) log("getMenu() for facet " + facet);
        
        // Check if the page author has defined a label facet
        UIComponent comp = getFacet(facet);
        if(comp != null && comp instanceof DropDown) {
            if(DEBUG) log("Found facet"); 
            return (DropDown)comp;
        }       
          
        DropDown menu = createDropDown(id, facet, options);
        menu.setDisabled(isDisabled());
        menu.setRequired(isRequired());
        return menu;
    }
    
    // Component creation methods
    private DropDown createDropDown(String id, String facetName, Option[] items) {
        
        if(DEBUG) log("createDropDown() for facet " + facetName);
        
        DropDown dropDown = new DropDown();
        dropDown.setId(getId().concat(id));
        dropDown.setItems(items);
        dropDown.setConverter(new IntegerConverter());
        
        if(getTabIndex() > 0) {
            dropDown.setTabIndex(getTabIndex());
        }
        
        getFacets().put(facetName, dropDown);
        return dropDown;
    }
    
    /**
     * <p>Convenience method to return at Option[] with all of the hours
     * defined in 24 hourObject format.</p>
     * 
     * @return An Option[] containing all the hours
     */
    private Option[] getHourItems() {
        Option[] hours = new Option[25];
        
        hours[0] = new Option(new Integer(-1), " ");
        
        int counter = 0;
        while(counter < 10) { 
            hours[counter + 1] = new Option(new Integer(counter), "0" + counter);
            ++counter;
        }
        while(counter < 24) {
            hours[counter + 1] = new Option(new Integer(counter), 
                                            String.valueOf(counter));
             ++counter;
        }
        return hours;
    }
    
    /**
     * <p>Convenience method to return at Option[] with all of the mintes (in
     * 5 minuteObject increments) for an hourObject.</p>
     * 
     * @return An Option[] containing all the minutes
     */
    private Option[] getMinuteItems() {
        Option[] minutes = new Option[13];
        
        minutes[0] = new Option(new Integer(-1), " ");
        minutes[1] = new Option(new Integer(0), "00");
        minutes[2] = new Option(new Integer(5), "05");
        
        for (int i = 2; i < 12; i++) {         
            minutes[i + 1] = new Option(new Integer(5 * i), String.valueOf(5*i));
        }
        
        return minutes;
    }
    
    /**
     * <p>Get the time-zone as a string.</p>
     */
    public String getOffset() {
        
        java.util.Calendar calendar = getCalendar();
        TimeZone timeZone = calendar.getTimeZone();
        
        StringBuffer gmtTimeZone = new StringBuffer(8); 
        
        int value = calendar.get(java.util.Calendar.ZONE_OFFSET) +
                calendar.get(java.util.Calendar.DST_OFFSET);
        
        if (value < 0) {
            // GMT - hh:mm
            gmtTimeZone.append('-');
            value = -value;
        } else {
            // GMT + hh:mm
            gmtTimeZone.append('+');
        }
        
        // determine the offset hours
        int num = value / (1000 * 60 * 60);
        
        if (num < 10) {
            // display offset as GMT + 0h:mm
            gmtTimeZone.append("0");
        }
        
        // add the hh: part
        gmtTimeZone.append(num)
        .append(":");
        
        // determine the offset minutes
        num = (value % (1000 * 60 * 60)) / (1000 * 60);
        if (num < 10) {
            // display as hh:0m
            gmtTimeZone.append("0");
        }
        
        // append the minutes
        gmtTimeZone.append(num);
        
        return gmtTimeZone.toString(); 
    }
    
    /**
     * <p>Returns a new Calendar instance corresponding to the user's current
     * locale and the developer specified time zone (if any).</p>
     *
     * @return java.util.Calendar A new Calendar instance with the correct
     * locale and time zone.
     */
    public java.util.Calendar getCalendar() {
        java.util.Calendar calendar = null;
        Locale locale =
                FacesContext.getCurrentInstance().getViewRoot().getLocale();
        if(locale == null) {
            locale = locale.getDefault();
        }
        
        
        TimeZone timeZone = getTimeZone();
        
        if(timeZone == null) {
            calendar = java.util.Calendar.getInstance(locale);
        } else {
            calendar = java.util.Calendar.getInstance(timeZone, locale);
        }
        return calendar;
    }

    /**
     * Holds value of property hourTooltipKey.
     */
    private String hourTooltipKey;

    /**
     * Getter for property hourTooltipKey.
     * @return Value of property hourTooltipKey.
     */
    public String getHourTooltipKey() {

        return this.hourTooltipKey;
    }

    /**
     * Setter for property hourTooltipKey.
     * @param hourTooltipKey New value of property hourTooltipKey.
     */
    public void setHourTooltipKey(String hourTooltipKey) {

        this.hourTooltipKey = hourTooltipKey;
    }

    /**
     * Holds value of property minutesTooltipKey.
     */
    private String minutesTooltipKey;

    /**
     * Getter for property minutesTooltipKey.
     * @return Value of property minutesTooltipKey.
     */
    public String getMinutesTooltipKey() {

        return this.minutesTooltipKey;
    }

    /**
     * Setter for property minutesTooltipKey.
     * @param minutesTooltipKey New value of property minutesTooltipKey.
     */
    public void setMinutesTooltipKey(String minutesTooltipKey) {

        this.minutesTooltipKey = minutesTooltipKey;
    }
    
   /**
     * <p>Specialized decode behavior on top of that provided by the
     * superclass.  In addition to the standard
     * <code>processDecodes</code> behavior inherited from {@link
     * UIComponentBase}, calls <code>validate()</code> if the the
     * <code>immediate</code> property is true; if the component is
     * invalid afterwards or a <code>RuntimeException</code> is thrown,
     * calls {@link FacesContext#renderResponse}.  </p>
     * @exception NullPointerException     
     */ 
    public void processDecodes(FacesContext context) {

        if(DEBUG) log("processDecodes"); 
        
        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        setValid(true);
        getFacet(HOUR_FACET).processDecodes(context);
        getFacet(MINUTES_FACET).processDecodes(context); 
        setSubmittedValue(TIME_SUBMITTED);
         
        // There is nothing to decode other than the facets
        
        if (isImmediate()) {
            if(DEBUG) log("Time is immediate"); 
            runValidation(context);           
        }
    }

    
     /**
     * <p>Perform the following algorithm to validate the local value of
     * this {@link UIInput}.</p>
     * 
     * @param context The {@link FacesContext} for the current request
     *
     */
    public void validate(FacesContext context) {
        
        if(DEBUG) log("validate()"); 
        
        if (context == null) {
            throw new NullPointerException();
        }

        Object hourValue = getHourMenu().getValue(); 
        if(DEBUG) log("Hour value is " + String.valueOf(hourValue)); 
        
        Object minuteValue = getMinutesMenu().getValue(); 
        if(DEBUG) log("Minute value is " + String.valueOf(minuteValue)); 
        
        ClockTime newValue = null;
        
	try {
	    newValue = createClockTime(hourValue, minuteValue, context); 
            if(DEBUG) log("Created ClockTime"); 
	}
	catch(ConverterException ce) {
            FacesMessage message = ce.getFacesMessage();  
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(getClientId(context), message);
            setValid(false);
            context.renderResponse();
        }	
        catch(Exception ex) {
            // TODO - message
            FacesMessage message = new FacesMessage("Invalid input");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(getClientId(context), message);
            setValid(false);
            context.renderResponse();
        }	

	// If our value is valid, store the new value, erase the
        // "submitted" value, and emit a ValueChangeEvent if appropriate
	if (isValid()) {
            if(DEBUG) log("\tComponent is valid"); 
	    Object previous = getValue();
            setValue(newValue);
            if(DEBUG) log("\tNew value: " + String.valueOf(newValue)); 
            setSubmittedValue(null);
            if (compareValues(previous, newValue)) {
                queueEvent(new ValueChangeEvent(this, previous, newValue));
            }
        }
    }
    
    private void runValidation(FacesContext context) {
        
        if(DEBUG) log("runValidation()"); 
        
        try {
            validate(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }
        
        if (!isValid()) {
            if(DEBUG) log("\tnot valid"); 
            context.renderResponse();
        }
    }
    
    private ClockTime createClockTime(Object hourObject, Object minuteObject,
            FacesContext context) {
        
        if(DEBUG) log("CreateClockTime()");
        
        String messageKey = null;
        ClockTime time = null;
        
        if(hourObject instanceof Integer && minuteObject instanceof Integer) {
            
            if(DEBUG) log("Found integers");
            
            int hour = ((Integer)hourObject).intValue();
            int minute = ((Integer)minuteObject).intValue();
            
            if(hour == -1 && minute == -1) {
                if(DEBUG) log("No selections made");
                if(isRequired()) {
                    messageKey = "Time.required";
                } else {
                    return null;
                }
            }
            
            else if(hour == -1) {
                messageKey = "Time.enterHour";
            } else if(minute == -1) {
                messageKey = "Time.enterMinute";
            } else {
                time = new ClockTime();
                try {
                    if(DEBUG) log("Hour is " + hour);
                    if(DEBUG) log("Minute is " + minute);
                    time.setHour(new Integer(hour));
                    time.setMinute(new Integer(minute));
                } catch(Exception ex) {
                    if(DEBUG) {
                        ex.printStackTrace();
                    }
                    messageKey = "Time.invalidData";
                }
            }
        } else {
            if(isRequired()) {
                messageKey = "Time.required";
            } else {
                return null;
            }
            
        }
        
        if(messageKey != null) {
            if(DEBUG) log("Invalid input");
            String message =
                    ThemeUtilities.getTheme(context).getMessage(messageKey);
            throw new ConverterException(new FacesMessage(message));
        }
        return time;
    }
    
    private void log(String s) {
        System.out.println(this.getClass().getName() + "::" + s);
    }

    /*
    public void setValue(Object value) {
        if(DEBUG) log("setValue(" + String.valueOf(value) + ")");
        Thread.dumpStack();
        super.setValue(value); 
    }

    public Object getValue() {
        Object value = super.getValue(); 
         if(DEBUG) log("getValue() ->" + String.valueOf(value));
        return value;
    }
     */
}
