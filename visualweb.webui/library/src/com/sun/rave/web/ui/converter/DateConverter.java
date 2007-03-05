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
package com.sun.rave.web.ui.converter;

import java.io.Serializable; 
import java.text.DateFormat;
import java.util.Date;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import com.sun.rave.web.ui.component.DateManager;
import com.sun.rave.web.ui.util.ThemeUtilities;
import java.text.MessageFormat;
import javax.faces.application.FacesMessage;

/**
 *
 * @author avk
 */
public class DateConverter implements Converter, Serializable {
    
    private static final String INVALID_DATE_ID = "DateConverter.invalidDate"; //NOI18N
    
    public DateConverter() {
    }
    
    public String getAsString(FacesContext context, UIComponent component, Object o) throws ConverterException{
        try {
            return getDateManager(component).getDateFormat().format((Date)o);
        }
        catch(Exception ex) {
            throw new ConverterException(ex);
        }
    }
    
    
    public Object getAsObject(FacesContext context, UIComponent component, String s) throws ConverterException {
        if(s.length() == 0) {
            return null;
        }
        // <RAVE>
        // Generate errors for dates that don't strictly follow format 6347646
        DateFormat df = getDateManager(component).getDateFormat();
        // Save old state in case there is other code that relies on it
        boolean saveLenient = df.isLenient();
        df.setLenient(false);
        try {
            Date date = df.parse(s);
            return date;
        } catch(Exception ex) {
            FacesMessage facesMessage = null;
            try {
                String message = ThemeUtilities.getTheme(context).getMessage(INVALID_DATE_ID);
                MessageFormat mf = new MessageFormat(message,
                                              context.getViewRoot().getLocale());
                String example = getDateManager(component).getDateFormat().format(new Date());
                Object[] params = {s, example};
                facesMessage = new FacesMessage(mf.format(params));
            }
            catch (Exception e) {
                throw new ConverterException(ex);
            }
            throw new ConverterException(facesMessage);
        } finally {
            // Restore original state
            df.setLenient(saveLenient);
        }
        // </RAVE>
    }
    
    private DateManager getDateManager(UIComponent component) {
        DateManager dateManager = null;
        if(component instanceof DateManager) {
            dateManager = (DateManager)component;
        } else if(component.getParent() instanceof DateManager) {
            dateManager = (DateManager)(component.getParent());
        }
        if(dateManager == null) { 
            throw new RuntimeException("The DateConverter can only be used with components which implement DateManager"); //NOI18N
        } 
        return dateManager;
    }
}

