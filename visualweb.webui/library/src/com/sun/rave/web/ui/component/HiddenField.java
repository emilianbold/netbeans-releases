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

import javax.faces.context.FacesContext;
import com.sun.rave.web.ui.util.ConversionUtilities;

/**
 *
 * @author avk
 */
public class HiddenField extends HiddenFieldBase {

    private final static boolean DEBUG = false;

    /** Creates a new instance of HiddenField */
    public HiddenField() {
    }


    /**
     * <p>Return the value to be rendered as a string when the
     * component is readOnly. The default behaviour is to
     * invoke getValueAsString(). Override this method in case
     * a component needs specialized behaviour.</p>
     * @param context FacesContext for the current request
     * @return A String value of the component
     */
    public String getReadOnlyValueString(FacesContext context) {
        return getValueAsString(context);
    }

     /**
     * <p>Return the value to be rendered, as a String (converted
     * if necessary), or <code>null</code> if the value is null.</p>
     * @param context FacesContext for the current request
     * @return A String value of the component
     */
    public String getValueAsString(FacesContext context) { 

        if(DEBUG) log("getValueAsString()");
        
	// This is done in case the RENDER_RESPONSE is occuring
	// prematurely due to some error or an immediate condition
	// on a button. submittedValue is set to null when the
	// component has been validated. 
	// If the component has not passed through the PROCESS_VALIDATORS
	// phase then submittedValue will be non null if a value
	// was submitted for this component.
	// 
        Object submittedValue = getSubmittedValue();
        if (submittedValue != null) {
            if(DEBUG) { 
                log("Submitted value is not null " + //NOI18N
                    submittedValue.toString());
            }
            return (String) submittedValue;
        }
              
        String value = ConversionUtilities.convertValueToString(this, getValue());
        if(value == null) {
            value = new String();
        }
        if(DEBUG) log("Component value is " + value); 
        return value;
    } 

    /**
     * Return the converted value of newValue.
     * If newValue is null, return null.
     * If newValue is "", check the rendered value. If the
     * the value that was rendered was null, return null
     * else continue to convert.
     */
    protected Object getConvertedValue(FacesContext context, 
                                       Object newValue) 
        throws javax.faces.convert.ConverterException {

        if(DEBUG) log("getConvertedValue()");
            
        Object value = ConversionUtilities.convertRenderedValue(context, 
                                                                newValue, this);
        
        if(DEBUG) log("\tComponent is valid " + String.valueOf(isValid())); 
        if(DEBUG) log("\tValue is " + String.valueOf(value)); 
        return value;
    }
    
    protected void log(String s) { 
        System.out.println(this.getClass().getName() + "::" + s); 
    }
}
