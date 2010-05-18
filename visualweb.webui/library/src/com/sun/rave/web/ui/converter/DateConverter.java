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

