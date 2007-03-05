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
package com.sun.rave.web.ui.validator;

import java.text.MessageFormat;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;
import com.sun.rave.web.ui.component.DateManager;


/**
 *  <p>	Use this validator to check the number of characters in a string when
 *	you need to set the validation messages.</p>
 *
 * @author avk
 */
public class DateInRangeValidator implements Validator {

    /**
     * <p>The converter id for this converter.</p>
     */
    public static final String VALIDATOR_ID = "com.sun.rave.web.ui.DateInRange";
    
    private static final boolean DEBUG = false;
    
    /** Creates a new instance of StringLengthValidator */
    public DateInRangeValidator() {
    }
    
    /**
     *	<p> Validate the value with regard to a <code>UIComponent</code> and a
     *	    <code>FacesContext</code>.</p>
     *
     *	@param	context	    The FacesContext
     *	@param	component   The component to be validated
     *	@param	value	    The submitted value of the component
     *
     * @exception ValidatorException if the value is not valid
     */ 
    public void validate(FacesContext context,
                         UIComponent  component,
                         Object value) throws ValidatorException {

        //log("validate()" + String.valueOf(value)); 
        if((context == null) || (component == null)) {
            //if(DEBUG) log("\tContext or component is null");
            throw new NullPointerException();
        }
       
        if(!(value instanceof Date)) { 
            return;
        }
    
        DateManager dateManager = null; 
        if(component instanceof DateManager) { 
            dateManager = (DateManager)component; 
        } 
        else if(component.getParent() instanceof DateManager) { 
            dateManager = (DateManager)(component.getParent()); 
        } 
        if(dateManager == null) { 
            //log("Didn't find a DateManager " + component.getClass().toString()); 
            return; 
        } 
        
        Date date = (Date)value;
        Date minDate = dateManager.getFirstAvailableDate();
        if(minDate != null && date.before(minDate)) {
            //log("Date is before minDAte!"); 
            FacesMessage msg = getFacesMessage(component, context, minDate, 
                                               "DateInRangeValidator.after");   
            throw new ValidatorException(msg);
        }
        Date maxDate = dateManager.getLastAvailableDate();
        if(maxDate != null && maxDate.before(date)) {
            //log("Date is after maxDAte!"); 
            FacesMessage msg = getFacesMessage(component, context, maxDate, 
                                               "DateInRangeValidator.before");   
            throw new ValidatorException(msg);
        
        }
    }
    
    private FacesMessage getFacesMessage(UIComponent component,
                                         FacesContext context,
                                         Date date, String key) { 
        
         String message = ThemeUtilities.getTheme(context).getMessage(key);
         String arg = ConversionUtilities.convertValueToString(component, date); 
         MessageFormat mf = new MessageFormat(message,
                                              context.getViewRoot().getLocale());
         Object[] params = { arg };
         return new FacesMessage(mf.format(params));
    }
         
    private void log(String s) { 
        System.out.println(this.getClass().getName() + "::" + s); //NOI18N
    }
  }
