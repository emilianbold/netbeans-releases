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

import java.text.MessageFormat;
import javax.faces.application.FacesMessage;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 *  <p>	Use this validator to check the number of characters in a string when
 *	you need to set the validation messages.</p>
 *
 * @author avk
 */
public class StringLengthValidator implements Validator, StateHolder {

    /**
     * <p>The converter id for this converter.</p>
     */
    public static final String VALIDATOR_ID = "com.sun.rave.web.ui.StringLength";
 
    /**
     * The message to use in case the value is too short. May include 
     * {0} for the minimum value.
     */
    private String tooShortMessage = null;    
    /**
    * The message to use in case the value is too long. May include 
     * {0} for the maximum value.
     */
    private String tooLongMessage;
    private int minLength = 0; 
    private int maxLength = 0; 
    
    private boolean minimumSet = false; 
    
    private static final boolean DEBUG = false;
    
    /** Creates a new instance of StringLengthValidator */
    public StringLengthValidator() {
    }
    
     
    /**
     * Creates a new instance of StringLengthValidator.
     * @param max The maximum number of characters allowed in the string
     */
    public StringLengthValidator(int max) {
        maxLength = max;
    }
    
    /**
     * Creates a new instance of StringLengthValidator.
     * @param max The maximum number of characters allowed in the string
     * @param min The minimum number of characters allowed in the string
     */
    public StringLengthValidator(int max, int min) {
        this(max);
        minLength = min;
        minimumSet = true;
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
        
        if(DEBUG) log("validate(" + String.valueOf(value) +")");

        if((context == null) || (component == null)) {
            if(DEBUG) log("\tContext or component is null");
            throw new NullPointerException();
        }
       
        String string;   
        if(value == null) { 
            if(DEBUG) log("\tValue is null!");
            string = new String();
        }
        else { 
            string = (String)value; 
          
        }
        if(DEBUG) log("\tValue is !" + string + "!");
        if(string.length() > maxLength) {
            if(DEBUG) log("\tString is longer than maxlength");
            if(tooLongMessage == null) { 
                Theme theme = ThemeUtilities.getTheme(context); 
                tooLongMessage = theme.getMessage
                        ("StringLengthValidator.itemTooLong"); 
            }
            MessageFormat mf =
                    new MessageFormat(tooLongMessage,
                    context.getViewRoot().getLocale());
            Object[] params = { String.valueOf(maxLength) };
            FacesMessage msg = new FacesMessage(mf.format(params));
            throw new ValidatorException(msg);
            
        }
        if(minimumSet && string.length() < minLength) {
             if(DEBUG) log("\tString is shorter than minlength");
            if(tooShortMessage == null) { 
                Theme theme = ThemeUtilities.getTheme(context); 
                tooShortMessage = theme.getMessage
                        ("StringLengthValidator.itemTooLong"); 
            }
            MessageFormat mf =
                    new MessageFormat(tooShortMessage,
                    context.getViewRoot().getLocale());
            Object[] params = { String.valueOf(minLength) };
            FacesMessage msg = new FacesMessage(mf.format(params));
            throw new ValidatorException(msg);
        }
    }

    private String integerToString(UIComponent component, Integer toConvert) {
	String result = null;
	Converter converter = null;
	FacesContext context = FacesContext.getCurrentInstance();

	converter = (Converter)
	    context.getApplication().createConverter("javax.faces.Number");
	result = converter.getAsString(context, component, toConvert);
	return result;
    }

    /**
     * Saves the state of the component into an object
     * @param context the FacesContext
     * @return the Object representing the state of the component
     */
    public Object saveState(FacesContext context) {

        Object values[] = new Object[5];
        values[0] = new Integer(maxLength);
        values[1] = new Integer(minLength);
        values[2] = minimumSet ? Boolean.TRUE : Boolean.FALSE;
        values[3] = tooLongMessage; 
        values[4] = tooShortMessage; 
        return (values);
    }

    /**
     * Restore the state of the component.
     * @param context The FacesContext
     * @param state the Object representing the state of the component
     */
    public void restoreState(FacesContext context, Object state) {

        Object values[] = (Object[]) state;
        maxLength = ((Integer) values[0]).intValue();
        minLength = ((Integer) values[1]).intValue();
        minimumSet = ((Boolean) values[2]).booleanValue();
        if(values[3] != null) tooLongMessage = values[3].toString();
        if(values[4] != null) tooShortMessage = values[4].toString();
    }

    private boolean transientValue = false;

    
    /**
     * Returns false, this component needs to save state.
     * @return false
     */
    public boolean isTransient() {
        return false; 
    }

    /**
     * Does nothing
     */
    public void setTransient(boolean transientValue) {
        return;
    }

    /**
     * Get the message to be used if the string is longer than the maxmimum number of characters.
     * @return the message to be used if the string is longer than the maxmimum number of characters
     */
    public String getTooLongMessage() {

        return this.tooLongMessage;
    }

    /**
     * Set the message to be used if the string is longer than the maximum number of characters.
     * @param tooLongMessage the message to be used if the string is longer than the maxmimum number of characters
     */
    public void setTooLongMessage(String tooLongMessage) {

        this.tooLongMessage = tooLongMessage;
    }

    /**
     * Get the message to be used if the string is shorter than the minimum number of characters.
     * @return the message to be used if the string is shorter than the minimum number of characters
     */
    public String getTooShortMessage() {

        return this.tooShortMessage;
    }

    /**
     * Set the message to be used if the string is shorter than the minimum number of characters.
     * @param tooShortMessage the message to be used if the string is shorter than the minimum number of characters
     */
    public void setTooShortMessage(String tooShortMessage) {

        this.tooShortMessage = tooShortMessage;
    }
    
    private void log(String s) { 
        System.out.println(this.getClass().getName() + "::" + s); //NOI18N
    }
  
}
