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
import java.util.Date;
import java.util.Locale;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;

import com.sun.rave.web.ui.converter.DateConverter;
import com.sun.rave.web.ui.validator.DateInRangeValidator;


/**
 * <p>Defines a Calendar component.</p>
 */
public class Calendar extends CalendarBase implements DateManager, NamingContainer {

    private static final String DATE_PICKER_LINK_FACET = "datePickerLink";
    private static final String DATE_PICKER_LINK_ID = "_datePickerLink";
    private static final String DATE_PICKER_FACET = "datePicker";
    private static final String DATE_PICKER_ID = "_datePicker";
    private static final String JAVASCRIPT_OBJECT = "_jsObject";
    public static final String PATTERN_ID = "_pattern"; 
    
    private DateConverter dateConverter = null; 
    
    /** Creates a new instance of Calendar */
    public Calendar() {
    }
    
    /**
     * This method returns the ImageHyperlink that serves as the "button" to
     * show or hide the calendar date picker display.
     *
     * @param context The current FacesContext.
     * @return The ImageHyperlink to show or hide the calendar date picker.
     */
    public ImageHyperlink getDatePickerLink(FacesContext context) { 

        UIComponent component = getFacet(DATE_PICKER_LINK_FACET); 
        
        ImageHyperlink datePickerLink;
        if (component instanceof ImageHyperlink) {
            datePickerLink = (ImageHyperlink)component;
        } else {
            datePickerLink = new ImageHyperlink();
            getFacets().put(DATE_PICKER_LINK_FACET, datePickerLink);
        }
        
        datePickerLink.setId(DATE_PICKER_LINK_ID);            
        datePickerLink.setAlign("middle");

        // render the image hyperlink to show/hide the calendar
        StringBuffer js = new StringBuffer(200);
        js.append("javascript: ")
            .append(getJavaScriptObjectName(context))
            .append(".toggle(); return false;");

        // Don't set Javascript as the URL -- bugtraq #6306848.
        datePickerLink.setOnClick(js.toString());


        // We should do this, but unfortunately the component can't be enabled
        // from the client-side yet. 
        //component.getAttributes().put("disabled", new Boolean(isDisabled()));
        
        return datePickerLink;
    }
    
    public CalendarMonth getDatePicker() { 
        
        UIComponent comp = getFacet(DATE_PICKER_FACET);      
        if (comp == null || !(comp instanceof CalendarMonth)) {
            CalendarMonth datePicker = new CalendarMonth();
            datePicker.setPopup(true); 
            datePicker.setId(DATE_PICKER_ID);
            getFacets().put(DATE_PICKER_FACET, datePicker);
            comp = datePicker;
        }
        ((CalendarMonth)comp).setJavaScriptObjectName
                    (getJavaScriptObjectName(FacesContext.getCurrentInstance()));
        return (CalendarMonth)comp;
    }
    
     public String getJavaScriptObjectName(FacesContext context) {
        return getClientId(context).replace(':', '_').concat(JAVASCRIPT_OBJECT);
    }  
    
    public Converter getConverter() {     
        
        // We add the validator at this point, if needed...
        Validator[] validators = getValidators();
        int len = validators.length; 
        boolean found = false; 
        for(int i=0; i<len; ++i) { 
            if(validators[i] instanceof DateInRangeValidator) { 
                found = true;
                break; 
            } 
        } 
        if(!found) {
            addValidator(new DateInRangeValidator());
        }
        Converter converter = super.getConverter();
        
        if (converter == null) {
            if(dateConverter == null) { 
                dateConverter = new DateConverter();
            } 
            converter = dateConverter; 
        }   
        return converter;
    }

    public String getReadOnlyValueString(FacesContext context) {
        if(getValue() == null) { 
            return "-"; 
        } 
        else { 
            return super.getReadOnlyValueString(context);
        }
    }
    
    public DateFormat getDateFormat() {
        return getDatePicker().getDateFormat();
    }
    
    // Since the value of the minDate attribute could change, we can't
    // cache this in an attribute.
    public Date getFirstAvailableDate() {
         Date minDate = getMinDate();
        if(minDate == null) {
            java.util.Calendar calendar = getDatePicker().getCalendar();
            calendar.add(java.util.Calendar.YEAR, -100);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0); 
            calendar.set(java.util.Calendar.MINUTE, 0); 
            calendar.set(java.util.Calendar.SECOND, 0); 
            calendar.set(java.util.Calendar.MILLISECOND, 0); 
            minDate = calendar.getTime();
        }
        return minDate;
    }
    
    public Date getLastAvailableDate() {
           Date maxDate = getMaxDate();
        if(maxDate == null) {
            Date minDate = getFirstAvailableDate();
            java.util.Calendar calendar = getDatePicker().getCalendar();
            calendar.setTime(minDate);
            calendar.add(java.util.Calendar.YEAR, 200);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23); 
            calendar.set(java.util.Calendar.MINUTE, 59); 
            calendar.set(java.util.Calendar.SECOND, 59); 
            calendar.set(java.util.Calendar.MILLISECOND, 999); 
            maxDate = calendar.getTime();
        }
        return maxDate;
    }

    // <RAVE>
    public void setDateFormatPattern(String dateFormatPattern) {
        // Whenever dateFormatPattern changes, uncache the pattern from the
        // CalendarMonth facet by deleting it (if any). This allows the change
        // to immediately affect the displayed selectedDate at design-time.
        getFacets().remove(DATE_PICKER_FACET);
        super.setDateFormatPattern(dateFormatPattern);
    }
    // </RAVE>
}

