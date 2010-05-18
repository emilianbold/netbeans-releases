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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.component.NamingContainer;
import javax.faces.convert.IntegerConverter;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.model.ScheduledEvent;
import com.sun.rave.web.ui.util.ThemeUtilities;


/**
 * <p><strong>This class needs to be rewritten. Do not release as API.</strong></p>
 */

public class CalendarMonth extends CalendarMonthBase
	implements NamingContainer {
    
    public static final String MONTH_MENU_ID = "monthMenu";
    public static final String YEAR_MENU_ID = "yearMenu";
    public static final String PREVIOUS_MONTH_LINK_ID = "previousMonthLink";
    public static final String NEXT_MONTH_LINK_ID = "nextMonthLink";
    public static final String DATE_LINK_ID = "dateLink";
    public static final String DATE_FIELD_ID = "dateField";
    public static final String DATE_FORMAT_ATTR = "dateFormatAttr"; 
    public static final String DATE_FORMAT_PATTERN_ATTR = "dateFormatPatternAttr"; 
    private static final String TIME_ZONE_ATTR = "timeZoneAttr"; 
        
    /**
     * <p>The java.util.Calendar object to use for this CalendarMonth component.</p>
     */
    protected java.util.Calendar calendar = null;
    
    private static final boolean DEBUG = false;
   
    public boolean isDateSelected(java.util.Calendar current, 
                                  java.util.Calendar endDate) {
        
        if(DEBUG) log("isDateSelected()"); 
        
        Object value = getValue(); 
        if(value == null) { 
            if(DEBUG) log("Value is null"); 
            return false; 
        }
        else if(value instanceof Date) {
            if(DEBUG) log("Value is date"); 
            Calendar calendar = getCalendar();
            calendar.setTime((Date)value);
            return compareDate(calendar, current);
        }
        else if(value instanceof ScheduledEvent) {        
            if(DEBUG) log("Value is ScheduledEvent");
            if(DEBUG) log("Checking dates before " + endDate.getTime().toString()); 
            Iterator dates = ((ScheduledEvent)value).getDates(endDate);
            Calendar calendar = null;
            
            while(dates.hasNext()) {
                calendar = (Calendar)(dates.next());
                if(compareDate(calendar, current)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }
    
    public boolean compareDate(java.util.Calendar selected, 
                                java.util.Calendar current) { 
       
        if(DEBUG) log("Rendered data is " + current.getTime().toString()); 
        if(DEBUG) log("Compare to " + selected.getTime().toString()); 
        if(selected.get(Calendar.YEAR) == current.get(Calendar.YEAR) &&
           selected.get(Calendar.MONTH) == current.get(Calendar.MONTH) &&
           selected.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH)) {
            if(DEBUG) log("Found match");
            return true;
        } 
        return false;
    } 
    
    /**
     * <p>Convenience function to return the locale of the current context.</p>
     */
    protected Locale getLocale() {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getViewRoot().getLocale();
    }
    
    /**
     * <p>Returns a new Calendar instance.</p>
     *
     * @return java.util.Calendar A new Calendar instance with the correct
     * locale and time zone.
     */
    public java.util.Calendar getCalendar() {
        if(calendar == null) { 
            initializeCalendar(); 
        }
        return (java.util.Calendar)(calendar.clone());
    }
    
    /**
     * <p>Returns a new Calendar instance.</p>
     *
     * @return java.util.Calendar A new Calendar instance with the correct
     * locale and time zone.
     */
    private void initializeCalendar() {
        
        if(DEBUG) log("initializeCalendar()"); 
        UIComponent parent = getParent();
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        if(parent instanceof DateManager) {
            TimeZone tz = ((DateManager)parent).getTimeZone();
            if(tz == null) {
                calendar = java.util.Calendar.getInstance(locale);
                getAttributes().put(TIME_ZONE_ATTR, calendar.getTimeZone());
            } else {
                calendar = java.util.Calendar.getInstance(tz, locale);
                getAttributes().put(TIME_ZONE_ATTR, tz);
            }
        } else {
            calendar = java.util.Calendar.getInstance(locale);
        }
        if(DEBUG) log("initializeCalendar() - END"); 
    }

    /** <p>Return the DateFormat object for this CalendarMonth.</p> */
    public DateFormat getDateFormat() {
        
        if(DEBUG) log("getDateFormat()"); 
        
        Object o = getAttributes().get(DATE_FORMAT_ATTR);
        DateFormat dateFormat = null; 
        if(o != null && o instanceof DateFormat) {
            dateFormat = (DateFormat)o; 
            if(DEBUG) log("DateFormat was already set"); 
        } 
       else {
            if(DEBUG) log("Dateformat not calculated");
            
            dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT, getLocale());
            // We need to set the locale and the timeZone of the dateFormat. 
            // I can't tell from the spec whether just setting the Calendar 
            // does this correctly (the Calendar does know both). 
            
            dateFormat.setCalendar(getCalendar());
            
            if(DEBUG) log("Set the Calendar"); 
            String pattern = null;
            UIComponent parent = getParent(); 
            if(parent != null && parent instanceof DateManager) {
                pattern = ((DateManager)parent).getDateFormatPattern();
            }
            
            if(pattern != null) {
                ((SimpleDateFormat)dateFormat).applyPattern(pattern);
                getAttributes().put(DATE_FORMAT_PATTERN_ATTR, pattern);
            } else {
                String defaultPattern =
                        ((SimpleDateFormat)dateFormat).toPattern();
                if(defaultPattern.indexOf("yyyy") == -1) {
                    defaultPattern = defaultPattern.replaceFirst("yy", "yyyy");
                }
                if(defaultPattern.indexOf("MM") == -1) {
                    defaultPattern = defaultPattern.replaceFirst("M", "MM");
                }
                if(defaultPattern.indexOf("dd") == -1) {
                    defaultPattern = defaultPattern.replaceFirst("d", "dd");
                }
                ((SimpleDateFormat)dateFormat).applyPattern(defaultPattern);
                getAttributes().put(DATE_FORMAT_PATTERN_ATTR, defaultPattern);
            }
            
            getAttributes().put(DATE_FORMAT_ATTR, dateFormat);
       }
        return dateFormat;
    }
    
     /** <p>Return the TimeZone object for this CalendarMonth.</p> */
    public TimeZone getTimeZone() {
        
        if(DEBUG) log("getDateFormat()"); 
        
        Object o = getAttributes().get(TIME_ZONE_ATTR);
        if(o != null && o instanceof TimeZone) {
            return (TimeZone)o;
        } 
        
        initializeCalendar(); 
        o = getAttributes().get(TIME_ZONE_ATTR);
        if(o != null && o instanceof TimeZone) {
            return (TimeZone)o;
        }
        return TimeZone.getDefault();
    }

    /**
     * <p>Get the DropDown menu instance to use for this CalendarMonths's year
     * menu.</p>
     * 
     * @return The DropDown instance to use for the year menu
     */
    public DropDown getMonthMenu() {    
         
        if(DEBUG) log("getMonthMenu()");
         
        UIComponent comp = getFacet(MONTH_MENU_ID);
        DropDown monthMenu = null; 
        
        if(comp == null || !(comp instanceof DropDown)) {
            
            monthMenu = new DropDown();
            monthMenu.setSubmitForm(true);
            
            monthMenu.setConverter(new IntegerConverter());
            monthMenu.setId(MONTH_MENU_ID);
            
            // The year menu is controlled by JavaScript when
            // this component is shown in popup mode. When used
            // in the Scheduler, we need to do the following
            // to control the behaviour on submit
            if(!isPopup()) {
                monthMenu.setImmediate(true);
                //yearMenu.addValueChangeListener(new MonthListener());
            }
            
            // add the year menu to the facet list
            getFacets().put(MONTH_MENU_ID, monthMenu);
        } else {
            monthMenu = (DropDown)comp;
        }
        
        if(DEBUG) log("getMonthMenu() - END");
        return monthMenu;
    }
    
        /**
     * <p>Get the JumpDropDown menu instance to use for thie
     * CalendarMonth's year menu.</p>
     *
     * @return The JumpDropDown instance to use for the year menu
     */
    public DropDown getYearMenu() {
         
        if(DEBUG) log("getYearMenu()");
         
        DropDown yearMenu = (DropDown) getFacets().get(YEAR_MENU_ID);
        
        if(yearMenu == null) {
            yearMenu = new DropDown();
            yearMenu.setSubmitForm(true);
            yearMenu.setId(YEAR_MENU_ID);
            yearMenu.setConverter(new IntegerConverter());          
    
            // The year menu is controlled by JavaScript when
            // this component is shown in popup mode. When used
            // in the Scheduler, we need to do the following 
            // to control the behaviour on submit
            if(!isPopup()) {
                yearMenu.setImmediate(true);
                //yearMenu.addValueChangeListener(new YearListener());
            }
            
            // add the year menu to the facet list            
            getFacets().put(YEAR_MENU_ID, yearMenu);
        }
        
        return yearMenu;
    }


    /**
     * <p>Get the IconHyperlink instance to use for the previous year
     * link.</p> 
     * @return The IconHyperlink instance to use for the previous year link
     */
    public IconHyperlink getPreviousMonthLink() {
        IconHyperlink link = (IconHyperlink)
            getFacets().get(PREVIOUS_MONTH_LINK_ID);
        
        if (link == null) {
            link = new IconHyperlink();
            link.setId(PREVIOUS_MONTH_LINK_ID);
            link.setIcon(ThemeImages.SCHEDULER_BACKWARD);
            link.setBorder(0);
            
            // The link is controlled by JavaScript when
            // this component is shown in popup mode. When used
            // in the Scheduler, we need to do the following
            // to control the behaviour on submit
            if(!isPopup()) {
                link.setImmediate(true);         
                link.addActionListener(new PreviousMonthListener());
            }

            getFacets().put(PREVIOUS_MONTH_LINK_ID, link);
        }
        
        return (IconHyperlink) link;
    }

    
    /**
     * <p>Get the IconHyperlink instance to use for the next year
     * link.</p> 
     * 
     * @return The IconHyperlink instance to use for the next year link
     */
    public IconHyperlink getNextMonthLink() {
        IconHyperlink link = (IconHyperlink)
            getFacets().get(NEXT_MONTH_LINK_ID);
        
        if (link == null) {
            link = new IconHyperlink();
            link.setId(NEXT_MONTH_LINK_ID);
            
            link.setIcon(ThemeImages.SCHEDULER_FORWARD);
            link.setBorder(0);
            
            // The link is controlled by JavaScript when
            // this component is shown in popup mode. When used
            // in the Scheduler, we need to do the following
            // to control the behaviour on submit
            if(!isPopup()) {
                link.addActionListener(new NextMonthListener());
                link.setImmediate(true);
            }

            getFacets().put(NEXT_MONTH_LINK_ID, link);
        }
        
        return link;
    }
    

    /** <p>Convience function to get the current Theme.</p> */
    protected Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }
    
    public void initCalendarControls(String jsName) {
        
        if(DEBUG) log("initCalendarControls()"); 
        
        StringBuffer js = new StringBuffer("javascript: ") //NOI18N
            .append(jsName)
            .append(".decreaseMonth(); return false;"); //NOI18N

        // Don't set Javascript as the URL -- bugtraq #6306848.
        ImageHyperlink link = getPreviousMonthLink();
        link.setIcon(ThemeImages.CALENDAR_BACKWARD);
        link.setOnClick(js.toString());
        
        js = new StringBuffer("javascript: ")
            .append(jsName)
            .append(".increaseMonth(); return false;");

        // Don't set Javascript as the URL -- bugtraq #6306848.
        link = getNextMonthLink();
        link.setIcon(ThemeImages.CALENDAR_FORWARD);
        link.setOnClick(js.toString());
        
        getMonthMenu().setOnChange(jsName.concat(".redrawCalendar(false); return false;"));
        getYearMenu().setOnChange(jsName.concat(".redrawCalendar(false); return false;"));
    }

    public void showNextMonth() {
        Integer month = getCurrentMonth();
        DropDown monthMenu = getMonthMenu();
        
        if(month.intValue() < 12) {
            int newMonth = month.intValue() + 1;     
            monthMenu.setSubmittedValue(new String[]{ String.valueOf(newMonth)});
        } else if(showNextYear()) {
            monthMenu.setSubmittedValue(new String[]{"1"});
        }
        // otherwise we do nothing
    }
    
    public void showPreviousMonth() {
       
        Integer month = getCurrentMonth();
        DropDown monthMenu = getMonthMenu();
        
        if(month.intValue() > 1) {
            int newMonth = month.intValue()-1; 
            monthMenu.setSubmittedValue(new String[]{String.valueOf(newMonth)});
        } 
        else if(showPreviousYear()) {
            monthMenu.setSubmittedValue(new String[]{"12"});
        }
        // otherwise we do nothing
    }
   
    private boolean showNextYear() {
        
        DropDown yearMenu = getYearMenu();
        int year = getCurrentYear().intValue();
        year++;
        Option[] options = yearMenu.getOptions();
        Integer lastYear = (Integer)(options[options.length -1].getValue());
        if(lastYear.intValue() >= year) {
            yearMenu.setSubmittedValue(new String[]{String.valueOf(year)});
            return true;
        }
        return false;
    }
    
    private boolean showPreviousYear() {
        
        DropDown yearMenu = getYearMenu();
        int year = getCurrentYear().intValue();
        year--;
        
        Option[] options = yearMenu.getOptions();
        Integer firstYear = (Integer)(options[0].getValue());
        if(firstYear.intValue() <= year) {
            yearMenu.setSubmittedValue(new String[]{String.valueOf(year)});
            return true;
        }
        return false;
    }

    public Integer getCurrentMonth() {
        DropDown monthMenu = getMonthMenu();
        Object value = monthMenu.getSubmittedValue();
        Integer month = null;
        if(value != null) {
            try {
                String[] vals = (String[])value;
                month = Integer.decode(vals[0]);
            } catch(Exception ex) {
                // do nothing
            }
        } else {
            value = monthMenu.getValue();
            if(value != null && value instanceof Integer) {
                month = ((Integer)value);
            }
        }
        return month;
    }
    
    public Integer getCurrentYear() {
        DropDown yearMenu = getYearMenu();
        Object value = yearMenu.getSubmittedValue();
        Integer year = null;
        if(value != null) {
            try {
                String[] vals = (String[])value;
                year = Integer.decode(vals[0]);
            } 
            catch(NumberFormatException ex) {
                // do nothing
            }
        } else {
            value = yearMenu.getValue();
            if(value != null && value instanceof Integer) {
                year = ((Integer)value);
            }
        }
        return year;      
     } 
     
    /**
     * Holds value of property javaScriptObject.
     */
    private String javaScriptObjectName = null;
    
    /*
    public void setDisplayDateField(int calendarField, int newValue) {
        
        if(DEBUG) log("setDisplayDateField()");
        Object o = getAttributes().get(DISPLAY_DATE_ATTR);
        if(o instanceof Calendar) {
            Calendar calendar = (Calendar)o;
            calendar.set(calendarField, newValue);
            calendar.getTime();
            if(DEBUG) log("\tNew time is " + calendar.getTime().toString());
        }
          
        if(DEBUG) log("setDisplayDateField() - END");
        FacesContext.getCurrentInstance().renderResponse();       
    }

    public void updateDisplayDateField(int calendarField, int newValue) { 
        
       if(DEBUG) log("updateDisplayDateField()");
        Object o = getAttributes().get(DISPLAY_DATE_ATTR);
        if(o instanceof Calendar) {
            Calendar calendar = (Calendar)o;
            calendar.add(calendarField, newValue);
            calendar.getTime();
            if(DEBUG) log("\tNew time is " + calendar.getTime().toString());
        }
          
        if(DEBUG) log("updateDisplayDateField() - END");
        FacesContext.getCurrentInstance().renderResponse();      
    }

     */

    /**
     * Getter for property javaScriptObject.
     * @return Value of property javaScriptObject.
     */
    public String getJavaScriptObjectName() {

        return this.javaScriptObjectName;
    }

    /**
     * Setter for property javaScriptObject.
     * @param javaScriptObject New value of property javaScriptObject.
     */
    public void setJavaScriptObjectName(String javaScriptObjectName) {

        this.javaScriptObjectName = javaScriptObjectName;
    }
    
    private void log(String s) { 
        System.out.println(this.getClass().getName() + "::" + s); 
    }

    public String getDateFormatPattern() { 
        
        if(DEBUG) log("calculatePattern()"); 
        Object o = getAttributes().get(DATE_FORMAT_PATTERN_ATTR); 
        String pattern = null; 
        if(o == null || !(o instanceof String)) { 
            getDateFormat(); 
            pattern = (String)(getAttributes().get(DATE_FORMAT_PATTERN_ATTR)); 
        } 
        else { 
            pattern = (String)o; 
        } 
        return pattern; 
    }
    
    // Cause the month display to move to the current value, not what the 
    // use was looking at last time... 
    public void displayValue() {
         
        if(DEBUG) log("displayValue()"); 
        DropDown monthMenu = getMonthMenu(); 
        DropDown yearMenu = getYearMenu(); 
        Object value = getValue(); 
        if(value == null) { 
           if(DEBUG) log("Value is null"); 
           monthMenu.setValue(null);
           yearMenu.setValue(null);    
        }
        else if(value instanceof Date) {
            if(DEBUG) log("Value is date"); 
            Calendar calendar = getCalendar();
            calendar.setTime((Date)value);
            int newMonth = calendar.get(Calendar.MONTH) + 1; 
            if(DEBUG) log("new month value " + String.valueOf(newMonth));          
            monthMenu.setValue(new Integer(newMonth)); 
            
            int newYear = calendar.get(Calendar.YEAR);
            if(DEBUG) log("new year value " + String.valueOf(newYear));
            yearMenu.setValue(new Integer(newYear)); 
        }
       else if(value instanceof ScheduledEvent) {
            if(DEBUG) log("Value is ScheduledEvent");
            Date date = ((ScheduledEvent)value).getStartTime();
            if(date != null) {
                Calendar calendar = getCalendar();
                calendar.setTime(date);
                int newMonth = calendar.get(Calendar.MONTH) + 1;
                if(DEBUG) log("new month value " + String.valueOf(newMonth));
                monthMenu.setValue(new Integer(newMonth));
                
                int newYear = calendar.get(Calendar.YEAR);
                if(DEBUG) log("new year value " + String.valueOf(newYear));
                yearMenu.setValue(new Integer(newYear));
            } else {
                if(DEBUG) log("Value is null");
                monthMenu.setValue(null);
                yearMenu.setValue(null);
            }           
       }
        monthMenu.setSubmittedValue(null);   
        yearMenu.setSubmittedValue(null);          
    }
}

class PreviousMonthListener implements ActionListener, Serializable {
    
    public void processAction(ActionEvent event) {
        
	FacesContext.getCurrentInstance().renderResponse();
        UIComponent comp = event.getComponent();
        comp = comp.getParent();
        if(comp instanceof CalendarMonth) {
            ((CalendarMonth)comp).showPreviousMonth(); 
        }
    }   
}

class NextMonthListener implements ActionListener, Serializable {
    
    public void processAction(ActionEvent event) {
        
	FacesContext.getCurrentInstance().renderResponse();
        UIComponent comp = event.getComponent();
        comp = comp.getParent();
        if(comp instanceof CalendarMonth) {
            ((CalendarMonth)comp).showNextMonth();
        }
    }   
}

/*
class MonthListener implements ValueChangeListener, Serializable {
    
    public void processValueChange(ValueChangeEvent event) {
           
	FacesContext.getCurrentInstance().renderResponse();
        Object value = event.getNewValue();
        System.out.println(value.toString());
        if(value != null && value instanceof Integer) {
            int newvalue = ((Integer)value).intValue() -1;
            UIComponent comp = event.getComponent();
            comp = comp.getParent();
            if(comp instanceof CalendarMonth) {
                ((CalendarMonth)comp).setDisplayDateField(Calendar.MONTH, newvalue);
            }
        }
    }
} 

class YearListener implements ValueChangeListener, Serializable {
    
    public void processValueChange(ValueChangeEvent event) {
        
	FacesContext.getCurrentInstance().renderResponse();
        Object value = event.getNewValue();
        System.out.println(value.toString());
        if(value != null && value instanceof Integer) {
            int newvalue = ((Integer)value).intValue();
            UIComponent comp = event.getComponent();
            comp = comp.getParent();
            if(comp instanceof CalendarMonth) {
                ((CalendarMonth)comp).setDisplayDateField(Calendar.YEAR, newvalue);
            }
        }
    }   
}

 */
