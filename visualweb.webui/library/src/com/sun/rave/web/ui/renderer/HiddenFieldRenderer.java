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
package com.sun.rave.web.ui.renderer;

import java.io.IOException;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;
import com.sun.rave.web.ui.component.Field;
import com.sun.rave.web.ui.component.HiddenField;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.MessageUtil;

/**
 * <p>Renderer for HiddenFieldRenderer {@link HiddenField} component.</p>
 */

public class HiddenFieldRenderer extends Renderer {

    private static final boolean DEBUG = false;

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        
        if(!(component instanceof HiddenField)) {
            Object[] params = { component.toString(),
                    this.getClass().getName(),
                    HiddenField.class.getName() };
                    String message = MessageUtil.getMessage
                            ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
                            "Renderer.component", params);              //NOI18N
                    throw new FacesException(message);
        }
        
        HiddenField field = (HiddenField)component;
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("input", field); //NOI18N
        writer.writeAttribute("type", "hidden", null); //NOI18N
        String id = field.getClientId(context);
        writer.writeAttribute("id", id , null); //NOI18N
        writer.writeAttribute("name", id, null); //NOI18N

	// Record the value that is rendered.
	// Note that getValueAsString conforms to JSF conventions
	// for NULL values, in that it returns "" if the component
	// value is NULL. This value cannot be trusted since
	// the fidelity of the data must be preserved, i.e. if the
	// value is null, it must remain null if the component is unchanged
	// by the user..
	//
	// What should be done in the case of submittedValue != null ?
	// This call to getValue may not be value is used by
	// getValueAsString, it may use the submittedValue.
	// Then should the previously set rendered value be 
	// preserved ?
	// 
	// If submittedValue is not null then the component's
	// model value or local value has not been updated
	// therefore assume that this is an immediate or premature
	// render response. Therefore just assume that if the rendered
	// value was null, the saved information is still valid.
	// 
	if (((HiddenField)component).getSubmittedValue() == null) {
	    ConversionUtilities.setRenderedValue(component,
		((HiddenField)component).getValue());
	}

	// Still call the component's getValueAsString method
	// in order to render it.
	//
	String value = field.getValueAsString(context);
        writer.writeAttribute("value", value, "value"); //NOI18N
        
        if(field.isDisabled()) {
            writer.writeAttribute("disabled", "disabled", null); //NOI18N
        }
        writer.endElement("input");
    }
    
    public void decode(FacesContext context, UIComponent component) {
        decodeInput(context, component);
    }
    
    /**
     * Decode the component component
     * @param context The FacesContext associated with this request
     * @param component The TextField component to decode
     */
    static void decodeInput(FacesContext context, UIComponent component) {
        
        if(DEBUG) log("decodeInput()"); 
        
        String id = component.getClientId(context); 
        Map params = context.getExternalContext().getRequestParameterMap();
        Object valueObject = params.get(id);
        String value = null; 
        
        if(valueObject == null && component instanceof Field) { 
            id = id.concat(((Field)component).INPUT_ID);
            valueObject = params.get(id); 
        } 
       
        if(valueObject != null) { 
            value = (String)valueObject;
            if(DEBUG) log("Submitted value is " + value); 
       
            if(component instanceof Field && ((Field)component).isTrim()) {
                value = value.toString().trim();
                if(DEBUG) log("Trimmed value is " + String.valueOf(value)); 
            }
        } 
        else if(DEBUG) log("\tNo relevant input parameter");
      
        ((EditableValueHolder)component).setSubmittedValue(value);
    }
    
    static protected void log(String s) {
        System.out.println( s);
    }
}
