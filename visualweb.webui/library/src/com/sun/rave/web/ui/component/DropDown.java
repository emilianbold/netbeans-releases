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

import java.util.Iterator;
import java.util.Map;

import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;
import javax.faces.el.MethodBinding;

import com.sun.rave.web.ui.el.DropDownMethodBinding;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.RenderingUtilities;


/**
 * <p>Component that represents a drop down menu.</p>
 */

public class DropDown extends DropDownBase implements ActionSource {

    public final static String SUBMIT = "_submitter";
    private boolean fireAction = false; 
    
    private static final boolean DEBUG = false;
      

    // Override this because the generator screws up
    public MethodBinding getAction() {
       
        if(DEBUG) log("getAction()");
        
        if(super.getAction() == null && isNavigateToValue()) {        
            setAction(new DropDownMethodBinding());
        }
        
        if(DEBUG) log("\tAction is " + String.valueOf(super.getAction()));     
        return super.getAction();
    }
    
    /**
     * Getter for property Rows.
     * @return Value of property Rows.
     */
    public int getRows() {

        return 1; 
    }

    /**
     * Setter for property Rows.
     * @param DisplayRows New value of property DisplayRows.
     */
    public void setRows(int DisplayRows) {

        super.setRows(1); 
    }

    /**
     * Getter for property multiple
     * @return Value of property multiple
     */
    public boolean getMultiple() {

        return false; 
    }
    
    /**
     * Setter for property multiple
     * @param multiple New value of property multiple
     */
    public void setMultiple(boolean multiple) {

        super.setMultiple(false); 
    }
    
    /**
     * <p>Add a new {@link ActionListener} to the set of listeners interested
     * in being notified when {@link ActionEvent}s occur.</p>
     *
     * @param listener The {@link ActionListener} to be added
     *
     * @exception NullPointerException if <code>listener</code>
     *  is <code>null</code>
     */
    public void addActionListener(ActionListener listener) {
        // add the specified action listener
        addFacesListener(listener);
    }
    
    /**
     * <p>Return the set of registered {@link ActionListener}s for this
     * {@link ActionSource} instance.  If there are no registered listeners,
     * a zero-length array is returned.</p>
     */
    public ActionListener[] getActionListeners() {
        // return all ActionListener instances associated with this component
        ActionListener listeners[] =
                (ActionListener []) getFacesListeners(ActionListener.class);
        return listeners;
    }
    
    /**
     * <p>Remove an existing {@link ActionListener} (if any) from the set of
     * listeners interested in being notified when {@link ActionEvent}s
     * occur.</p>
     *
     * @param listener The {@link ActionListener} to be removed
     *
     * @exception NullPointerException if <code>listener</code>
     *  is <code>null</code>
     */
    public void removeActionListener(ActionListener listener) {
        // remove the specified ActionListener from the list of listeners
        removeFacesListener(listener);
    }
    
    /**
     * <p>The DropDown needs to override the standard decoding 
     * behaviour since it may also be an action source. We 
     * decode the component w.r.t. the value first, and 
     * validate it if the component is immediate. Then we 
     * fire an action event.</p>
     * @exception NullPointerException     
     */ 
    public void processDecodes(FacesContext context) {

        if(DEBUG) log("processDecodes()");
        // Skip processing if our rendered flag is false
        if(!isRendered()) {
            return;
        }
        
        // Decode the component and any children 
        // Do we really want to decode any children? 
        // Process all facets and children of this component
        Iterator childComponents = getFacetsAndChildren();
        while (childComponents.hasNext()) {
            UIComponent comp = (UIComponent) childComponents.next();
            comp.processDecodes(context);
        }

        // Get the value of this component
        try {
            decode(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }
        
        // Testing isSubmitter() alone is deceiving here.
	// The component may have been the submitter but its
	// submittedValue may still be null.
	// This true in the case of an OptionTitle list option
	// It submits but is treated as if there was no submit
	// by not setting the submittedValue. It doesn't explicitly
	// set it to null, in case the caller of the renderer decode
	// method, set it to something special, for example if the
	// DropDown is used as a sub component.
	//
	// However because this component did submit the form
	// we still need to call setLastClientElement
	// so after calling is submitter, still check submittedValue.
	// and return if it is null.
	//
	// Also not that the submittedValue check has been added to
	// validate, as in UIInput's validate method.
	//
        boolean isSubmitter = isSubmitter(context); 
        
        if(isSubmitter) { 
           // since this component submitted the form, we need to make it have
            // focus next time through. To do this, we will set an attribute
            // in the request map.
            RenderingUtilities.setLastClientID(context,
                getPrimaryElementID(context));
        }

        // Should we fire an action?
	//
	// Check submittedValue. Let the code fall through to
	// validate for an immediate action and it will just return.
	//
        fireAction = isSubmitter && isSubmitForm() &&
		getSubmittedValue() != null;
        
        // If we are supposed to fire an action and navigate to the value
        // of the component, we get the submitted value now and pass
        // it to the DropDownMethodBinding.
        if(fireAction  && isNavigateToValue()) {  
            if(DEBUG) log("\tHanding navigateToValue...");
            MethodBinding mb = getAction();
            if(DEBUG) { 
                if(mb != null) log("\tMethod binding is " + mb.toString()); 
                else log("\tMethod binding is null");
            }
          
            if(mb instanceof DropDownMethodBinding) {
                String outcome = null;
                Object values = getSubmittedValue();
                if(values instanceof String[]) { 
                    String[] stringValues = (String[])values;
                    if(stringValues.length > 0) { 
                        outcome = stringValues[0];
                        if(DEBUG) log("Outcome is " + outcome);
                    }
                }
           
                ((DropDownMethodBinding)mb).setValue(outcome);
                if(DEBUG) log("\tNavigate to " + outcome);             
            }
        }
        
        // Next, if the component is immediate, we validate the component        
        if(isImmediate()) {
            try {
                validate(context);
            } catch(RuntimeException e) {
                context.renderResponse();
                throw e;
            }         
            if (!isValid()) {
                context.renderResponse();
            }
        }       
    }
   
    /**
     * <p>In addition to to the default {@link UIComponent#broadcast} 
     * processing, pass the {@link ActionEvent} being broadcast to the method 
     * referenced by <code>actionListener</code> (if any), and to the default 
     * {@link ActionListener} registered on the {@link Application}.</p>
     *
     * @param event {@link FacesEvent} to be broadcast
     *
     * @exception AbortProcessingException Signal the JavaServer Faces 
     * implementation that no further processing on the current event should be 
     * performed @exception IllegalArgumentException if the implementation class
     * of this {@link FacesEvent} is not supported by this component
     * @exception NullPointerException if <code>event</code> is
     * <code>null</code>
     */
    public void broadcast(FacesEvent event) throws AbortProcessingException {        
        
        // Perform standard superclass processing
        super.broadcast(event);

        if (isSubmitForm() && (event instanceof ActionEvent)) {
            FacesContext context = getFacesContext();

            // Notify the specified action listener method (if any)
            MethodBinding mb = getActionListener();
            if (mb != null) {
                mb.invoke(context, new Object[] { event });
            }

            // Invoke the default ActionListener
            ActionListener listener =
              context.getApplication().getActionListener();
            if (listener != null) {                
                listener.processAction((ActionEvent) event);
            }
        }
    }

  
    /**
     * <p>Intercept <code>queueEvent</code> and, for {@link ActionEvent}s, mark 
     * the phaseId for the event to be <code>PhaseId.APPLY_REQUEST_VALUES</code>
     * if the <code>immediate</code> flag is true, 
     * <code>PhaseId.INVOKE_APPLICATION</code> otherwise.</p>
     */
    public void queueEvent(FacesEvent e) {
        // If this is an action event, we set the phase according to whether
        // the component is immediate or not. 
        if(isSubmitForm()) {
            if (e instanceof ActionEvent) {
                if (isImmediate()) {
                    e.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
                } else {
                    e.setPhaseId(PhaseId.INVOKE_APPLICATION);
                }
            }
        }
        
        super.queueEvent(e);
    }
    
      private boolean isSubmitter(FacesContext context) {
       
        if(DEBUG) log("isSubmitter()"); 
        String compID = getClientId(context).concat(SUBMIT); 
        Map requestParameters =
            context.getExternalContext().getRequestParameterMap();
        
        String submitter = (String)requestParameters.get(compID);
        if(DEBUG) log("\tValue of submitter field " + submitter); 
        return (submitter != null) ? submitter.equals("true") : false; 
    }

    public void validate(FacesContext context) {

	// From UIInput
	//
        // Submitted value == null means "the component was not submitted
	// at all";  validation should not continue
	//
	Object submittedValue = getSubmittedValue();
	if (submittedValue == null) {
	    return;
	}

        super.validate(context);
        
        if(isValid() && fireAction) { 
            if(DEBUG) log("\tQueue the component event");
            // queue an event
            queueEvent(new ActionEvent(this));      
        }
    }
}
