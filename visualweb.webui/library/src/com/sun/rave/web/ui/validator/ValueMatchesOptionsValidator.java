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
package com.sun.rave.web.ui.validator;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import com.sun.rave.web.ui.component.ListSelector;
import com.sun.rave.web.ui.model.list.ListItem;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 *  <p>	Use this validator to check the number of characters in a string when
 *	you need to set the validation messages.</p>
 *
 * @author avk
 */
public class ValueMatchesOptionsValidator implements Validator, Serializable {

    /**
     * <p>The converter id for this converter.</p>
     */
    public static final String VALIDATOR_ID = "com.sun.rave.web.ui.ValueMatchesOptions";
    /**
     * Error message used if the value is not in the option. 
     */
    private String message = null;
    
    private static final boolean DEBUG = false;
    
    /** Creates a new instance of StringLengthValidator */
    public ValueMatchesOptionsValidator() {
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
        
        if(DEBUG) log("validate()");
        
        if((context == null) || (component == null)) {
            String message = "Context or component is null";
            if(DEBUG) log("\t" + message);
            throw new NullPointerException(message);
        }
        
        if(!(component instanceof ListSelector)) {
            String message = this.getClass().getName() +
                    " can only be used with components which subclass " +
                    ListSelector.class.getName();
            if(DEBUG) log("\t" + message);
            throw new RuntimeException(message);
        }
        
        ListSelector list = (ListSelector)component;
        Object valuesAsArray = null;
        
        if(value instanceof List) {
            if(DEBUG) log("\tValue is list");
            valuesAsArray = ((List)value).toArray();
        } else if(value.getClass().isArray()) {
            if(DEBUG) log("\tValue is array");
            valuesAsArray = value;
        } else {
            if(DEBUG) log("\tValue is object");
            valuesAsArray = new Object[]{ value };
        }
        
        int numValues = Array.getLength(valuesAsArray);
        if( numValues == 0) {
            if(DEBUG) log("\tArray is empty - values are OK");
            return;
        }
        
        Object currentValue = null;
        Iterator itemsIterator = null;
        ListItem listItem = null;
        Object listObject = null;
        boolean foundValue = false;
        boolean error = false;
        
        for(int counter=0; counter< numValues; ++counter) {
            currentValue = Array.get(valuesAsArray, counter);
            itemsIterator = list.getListItems();
            foundValue = false;
            
            if(DEBUG) log("\tChecking: " + String.valueOf(currentValue));
            while(itemsIterator.hasNext()) {
                listObject = itemsIterator.next();
                if(!(listObject instanceof ListItem)) {
                    continue;
                }
                listItem = (ListItem)listObject;
                if(DEBUG) log("ListItem is " + listItem.getLabel());
                if(currentValue.equals(listItem.getValueObject())) {
                    if(DEBUG) log("Found match");
                    foundValue = true;
                    break;
                }
            }
            if(!foundValue) {
                if(DEBUG) log("No match found");
                error = true;
                break;
            }
        }
        
        if(error) {
            if(message == null) {
                Theme theme = ThemeUtilities.getTheme(context);
                message = ThemeUtilities.getTheme(context).getMessage
                        ("ListSelector.badValue");
            }
            throw new ValidatorException(new FacesMessage(message));
        }
    }

    private void log(String s) { 
        System.out.println(this.getClass().getName() + "::" + s); //NOI18N
    }

  
    /**
     * Getter for property message.
     * @return Value of property message.
     */
    public String getMessage() {

        return this.message;
    }

    /**
     * Setter for property message.
     * @param message New value of property message.
     */
    public void setMessage(String message) {

        this.message = message;
    } 
}
