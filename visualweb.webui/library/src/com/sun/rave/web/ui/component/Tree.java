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

import com.sun.rave.web.ui.util.LogUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.EditableValueHolder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.Validator;

import javax.servlet.http.Cookie;


/**
 *
 *  @author  Ken Paulsen (ken.paulsen@sun.com)
 */
public class Tree extends TreeBase implements EditableValueHolder {

    /**
     *	Constructor.
     */
    public Tree() {
	super();
	setLayoutDefinitionKey(LAYOUT_KEY);
    }
    
    public String getSelected() {
        return (String) getValue();
    }
    
    public void setSelected(String s) {
        setValue(s);
    }

    //////////////////////////////////////////////////////////////////////
    //	ValueHolder Methods
    //////////////////////////////////////////////////////////////////////

    /**
     *	<p> Return the <code>Converter</code> (if any) that is registered for
     *	    this <code>UIComponent</code>.</p>
     *
     *	<p> Not implemented for this component.</p>
     */
    public Converter getConverter() {
	return converter;
    }

    /**
     *	<p> Set the <code>Converter</code> (if any) that is registered for
     *	    this <code>UIComponent</code>.</p>
     *
     *	<p> Not implemented for this component.</p>
     *
     *	@param conv New <code>Converter</code> (or <code>null</code>)
     */
    public void setConverter(Converter conv) {
	converter = conv;
	// Do nothing... throw exception?
    }

    /**
     *	<p> Return the local value of this <code>UIComponent</code> (if any),
     *	    without evaluating any associated <code>ValueBinding</code>.</p>
     */
    public Object getLocalValue() {
	return value;
    }

    /**
     *	<p> Gets the value of this {@link UIComponent}.  First, consult the
     *	    local value property of this component.  If non-<code>null</code>
     *	    return it.  If non-null, see if we have a <code>ValueBinding</code>
     *	    for the <code>value</code> property.  If so, return the result of
     *	    evaluating the property, otherwise return null.</p>
     */
    public Object getValue() {
	if (value != null) {
	    return value;
	}
	ValueBinding vb = getValueBinding("value");
	if (vb != null) {
	    return (vb.getValue(getFacesContext()));
	} else {
	    return (null);
	}
//	return getSelectedTreeNode();
    }

    /**
     *	<p> Set the value of this {@link UIComponent} (if any).</p>
     *
     *	@param	val The new local value
     */
    public void setValue(Object val) {
	value = val;

	// Mark the local value as set.
	setLocalValueSet(true);
    }


    //////////////////////////////////////////////////////////////////////
    //	EditableValueHolder Methods
    //////////////////////////////////////////////////////////////////////

    /**
     *	<p> Return the submittedValue value of this component.  This method
     *	    should only be used by the <code>encodeBegin()</code> and/or
     *	    <code>encodeEnd()</code> methods of this component, or its
     *	    corresponding <code>Renderer</code>.</p>
     */
    public Object getSubmittedValue() {
	return submittedValue;
    }

    /**
     *	<p> Set the submittedValue value of this component.  This method should
     *	    only be used by the <code>decode()</code> and
     *	    <code>validate()</code> method of this component, or its
     *	    corresponding <code>Renderer</code>.</p>
     *
     *	@param	value	The new submitted value.
     */
    public void setSubmittedValue(Object value) {
	submittedValue = value;
    }

    /**
     *	<p> Return the "local value set" state for this component.  Calls to
     *	    <code>setValue()</code> automatically reset this property to
     *	    <code>true</code>.
     */
    public boolean isLocalValueSet() {
	return localValueSet;
    }

    /**
     *	<p> Sets the "local value set" state for this component.</p>
     */
    public void setLocalValueSet(boolean value) {
	localValueSet = value;
    }

    /**
     *	<p> Return a flag indicating whether the local value of this component
     *	    is valid (no conversion error has occurred).</p>
     */
    public boolean isValid() {
	return valid;
    }

    /**
     *	<p> Set a flag indicating whether the local value of this component
     *	    is valid (no conversion error has occurred).</p>
     *
     *	@param	value	The new valid flag.
     */
    public void setValid(boolean value) {
	valid = value;
    }

    /**
     *	<p> Return a <code>MethodBinding</code> pointing at a method that will
     *	    be used to validate the current value of this component.   This
     *	    method will be called during the <em>Process Validations</em> or
     *	    <em>Apply Request Values</em> phases (depending on the value of
     *	    the <code>immediate</code> property). </p>
     *
     *	<p> Not implemented for this component.</p>
     */
    public MethodBinding getValidator() {
        return validatorBinding;
    }

    /**
     *	<p> Set a <code>MethodBinding</code> pointing at a method that will be
     *	    used to validate the current value of this component.  This method
     *	    will be called during the <em>Process Validations</em> or
     *	    <em>Apply Request Values</em> phases (depending on the value of
     *	    the <code>immediate</code> property). </p>
     *
     *	<p> Any method referenced by such an expression must be public, with a
     *	    return type of <code>void</code>, and accept parameters of type
     *	    <code>FacesContext</code>, <code>UIComponent</code>, and
     *	    <code>Object</code>.</p>
     *
     *	<p> Not implemented for this component.</p>
     *
     *	@param	valBinding  The new <code>MethodBinding</code> instance.
     */
    public void setValidator(MethodBinding valBinding) {
        validatorBinding = valBinding;
    }

    /**
     *	<p> Add a <code>Validator</code> instance to the set associated with
     *	    this component.</p>
     *
     *	<p> Not implemented for this component.</p>
     *
     *	@param	validator   The <code>Validator</code> to add.
     */
    public void addValidator(Validator validator) {
        if (validator == null) {
            throw new NullPointerException();
        }
        if (validators == null) {
            validators = new ArrayList();
        }
        validators.add(validator);
    }

    /**
     *	<p> Return the set of registered <code>Validator</code>s for this
     *	    component instance.  If there are no registered validators, a
     *	    zero-length array is returned.</p>
     *
     *	<p> Not implemented for this component.</p>
     */
    public Validator[] getValidators() {
        if (validators == null) {
            return (new Validator[0]);
        }
	return ((Validator[]) validators.toArray(
		    new Validator[validators.size()]));
    }

    /**
     *	<p> Remove a <code>Validator</code> instance from the set associated
     *	    with this component, if it was previously associated.  Otherwise,
     *	    do nothing.</p>
     *
     *	<p> Not implemented for this component.</p>
     *
     *	@param	validator   The <code>Validator</code> to remove.
     */
    public void removeValidator(Validator validator) {
        if (validators != null) {
            validators.remove(validator);
        }
    }

    /**
     *	<p> Return a <code>MethodBinding</code> instance method that will be
     *	    called after any registered <code>ValueChangeListener</code>s have
     *	    been notified of a value change.  This method will be called during
     *	    the <em>Process Validations</em> or <em>Apply Request Values</em>
     *	    phases (depending on the value of the <code>immediate</code>
     *	    property). </p>
     */
    public MethodBinding getValueChangeListener() {
	return valueChangeMethod;
    }

    /**
     *	<p> Set a <code>MethodBinding</code> instance method that will be
     *	    called after any registered <code>ValueChangeListener</code>s have
     *	    been notified of a value change.  This method will be called
     *	    during the <em>Process Validations</em> or <em>Apply Request
     *	    Values</em> phases (depending on the value of the
     *	    <code>immediate</code> property).</p>
     *
     *	@param	method	The new MethodBinding instance.
     */
    public void setValueChangeListener(MethodBinding method) {
	valueChangeMethod = method;
    }

    /**
     *	<p> Add a new <code>ValueChangeListener</code> to the set of listeners
     *	    interested in being notified when <code>ValueChangeEvent</code>s
     *	    occur.</p>
     *
     *	@param	listener    The <code>ValueChangeListener</code> to be added.
     */
    public void addValueChangeListener(ValueChangeListener listener) {
	addFacesListener(listener);
    }

    /**
     *	<p> Return the set of registered <code>ValueChangeListener</code>s for
     *	    this component instance.  If there are no registered listeners, a
     *	    zero-length array is returned.</p>
     */
    public ValueChangeListener[] getValueChangeListeners() {
	return (ValueChangeListener [])
	    getFacesListeners(ValueChangeListener.class);
    }

    /**
     *	<p> Remove an existing <code>ValueChangeListener</code> (if any) from
     *	    the set of listeners interested in being notified when
     *	    <code>ValueChangeEvent</code>s occur.</p>
     *
     *	@param	listener    The <code>ValueChangeListener</code> to be removed.
     */
    public void removeValueChangeListener(ValueChangeListener listener) {
	removeFacesListener(listener);
    }

    //////////////////////////////////////////////////////////////////////
    //	Other Methods
    //////////////////////////////////////////////////////////////////////

    /**
     *	<p> Decode any new state of this <code>UIComponent</code> from the
     *	    request contained in the specified <code>FacesContext</code>, and
     *	    store this state as needed.</p>
     *
     *	<p> During decoding, events may be enqueued for later processing (by
     *	    event listeners who have registered an interest),  by calling
     *	    <code>queueEvent()</code>.</p>
     *
     *	@param	context	{@link FacesContext} for the request we are processing.
     */
    public void decode(FacesContext context) {
	setValid(true);
	super.decode(context);
    }

    /**
     *	<p> In addition to to the default <code>UIComponent#broadcast</code>
     *	    processing, pass the <code>ValueChangeEvent</code> being broadcast
     *	    to the method referenced by <code>valueChangeListener</code>.</p>
     *
     *	@param	event	<code>FacesEvent</code> to be broadcast
     *
     *	@exception  AbortProcessingException	Signal the JSF implementation
     *	    that no further processing on the current event should be performed
     */
    public void broadcast(FacesEvent event) throws AbortProcessingException {
	// Perform standard superclass processing
	super.broadcast(event);

	if (event instanceof ValueChangeEvent) {
	    MethodBinding method = getValueChangeListener();
	    if (method != null) {
		FacesContext context = getFacesContext();
		method.invoke(context, new Object[] { event });
	    }
	}
    }

    /**
     *	<p> Perform the component tree processing required by the <em>Update
     *	    Model Values</em> phase of the request processing lifecycle for
     *	    all facets of this component, all children of this component,
     *	    and this component itself, as follows.</p>
     *
     *	    <ul><li>If the <code>rendered</code> property of this
     *		    <code>UIComponent</code> is <code>false</code>, skip
     *		    further processing.</li>
     *		<li>Call the <code>processUpdates()</code> method of all
     *		    facets and children of this {@link UIComponent}, in the
     *		    order determined by a call to
     *		    <code>getFacetsAndChildren()</code>.</li></ul>
     *
     *	@param	context	<code>FacesContext</code> for this request
     */
    public void processUpdates(FacesContext context) {
	// Skip processing if our rendered flag is false
	if (!isRendered()) {
	    return;
	}

	// Do the super stuff...
	super.processUpdates(context);

	// Save model stuff...
	try {
	    updateModel(context);
	} catch (RuntimeException ex) {
	    context.renderResponse();
	    throw new RuntimeException(ex);
	}
    }

    /**
     *	<p> Perform the following algorithm to update the model data
     *	    associated with this component, if any, as appropriate.</p>
     *
     *	    <ul><li>If the <code>valid</code> property of this component is
     *		    <code>false</code>, take no further action.</li>
     *		<li>If the <code>localValueSet</code> property of this
     *		    component is <code>false</code>, take no further action.</li>
     *		<li>If no <code>ValueBinding</code> for <code>value</code>
     *		    exists, take no further action.</li>
     *		<li>Call <code>setValue()</code> method of the
     *		    <code>ValueBinding</code> to update the value that the
     *		    <code>ValueBinding</code> points at.</li>
     *		<li>If the <code>setValue()</code> method returns successfully:
     *		    <ul><li>Clear the local value of this component.</li>
     *			<li>Set the <code>localValueSet</code> property of
     *			    this component to false.</li></ul></li>
     *		<li>If the <code>setValue()</code> method call fails:
     *		    <ul><li>Queue an error message by calling
     *			    <code>addMessage()</code> on the specified
     *			    <code>FacesContext</code> instance.</li>
     *			<li>Set the <code>valid</code> property of this
     *			    component to <code>false</code>.</li></ul></li>
     *	    </ul>
     *
     *	@param	context	<code>FacesContext</code> for the request we are
     *			processing.
     */
    public void updateModel(FacesContext context) {
	// Sanity Checks...
	if (context == null) {
	    throw new NullPointerException();
	}
	if (!isValid() || !isLocalValueSet()) {
	    return;
	}
	ValueBinding vb = getValueBinding("value");
	if (vb == null) {
	    return;
	}

	try {
	    vb.setValue(context, getLocalValue());
	    setValue(null);
	    setLocalValueSet(false);
	    return;
	} catch (Exception ex) {
	    String messageStr = ex.getMessage();
	    if (messageStr != null) {
		FacesMessage message = null;
		message = new FacesMessage(messageStr);
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		context.addMessage(getClientId(context), message);
	    }
	    setValid(false);
	    if (LogUtil.configEnabled()) {
		LogUtil.config("Unable to update Model!", ex); // NOI18N
	    }
	}
    }

    /**
     *	<p> Perform the component tree processing required by the <em>Apply
     *	    Request Values</em> phase of the request processing lifecycle for
     *	    all facets of this component, all children of this component, and
     *	    this component itself, as follows.</p>
     *
     *	    <ul><li>If the <code>rendered</code> property of this
     *		    <code>UIComponent</code> is <code>false</code>, skip
     *		    further processing.</li>
     *		<li>Call the <code>processDecodes()</code> method of all
     *		    facets and children of this <code>UIComponent</code>, in the
     *		    order determined by a call to
     *		    <code>getFacetsAndChildren()</code>.</li>
     *		<li>Call the <code>decode()</code> method of this
     *		    component.</li>
     *		<li>If a <code>RuntimeException</code> is thrown during decode
     *		    processing, call <code>FacesContext.renderResponse</code>
     *		    and re-throw the exception.</li></ul>
     *
     *	@param	context	<code>FacesContext</code> for the request.
     */
    public void processDecodes(FacesContext context) {
	// Skip processing if our rendered flag is false
	if (!isRendered()) {
	    return;
	}

	super.processDecodes(context);

	if (isImmediate()) {
	    executeValidate(context);
	}
    }

    /**
     *	<p> In addition to the standard <code>processValidators</code> behavior
     *	    inherited from <code>UIComponentBases</code>, calls
     *	    <code>validate()</code> if the <code>immediate</code> property is
     *	    false (which is the default);  if the component is invalid
     *	    afterwards, calls <code>FacesContext.renderResponse</code>.  If a
     *	    <code>RuntimeException</code> is thrown during validation
     *	    processing, calls <code>FacesContext.renderResponse</code> and
     *	    re-throws the exception.</p>
     */
    public void processValidators(FacesContext context) {
	if (context == null) {
	    throw new NullPointerException();
	}

	// Skip processing if our rendered flag is false
	if (!isRendered()) {
	    return;
	}

	super.processValidators(context);

	if (!isImmediate()) {
	    executeValidate(context);
	}
    }

    /**
     *	Executes validation logic.
     */
    private void executeValidate(FacesContext context) {
	try {
	    validate(context);
	} catch (RuntimeException e) {
	    context.renderResponse();
	    throw e;
	}

	if (!isValid()) {
	    context.renderResponse();
	}
    }

    /**
     *	<p> Perform the following algorithm to validate the local value of
     *	    this <code>UIInput</code>.</p>
     *
     *	    <ul><li>Retrieve the submitted value with
     *		    <code>getSubmittedValue()</code>. If this returns null,
     *		    exit without further processing.  (This indicates that no
     *		    value was submitted for this component.)</li>
     *
     *		<li>Convert the submitted value into a "local value" of the
     *		    appropriate data type by calling
     *		    <code>getConvertedValue</code>.</li>
     *
     *		<li>Validate the property by calling
     *		    <code>validateValue</code>.</li>
     *
     *		<li>If the <code>valid</code> property of this component is
     *		    still <code>true</code>, retrieve the previous value of
     *		    the component (with <code>getValue()</code>), store the new
     *		    local value using <code>setValue()</code>, and reset the
     *		    submitted value to null.  If the local value is different
     *		    from the previous value of this component, fire a
     *		    <code>ValueChangeEvent</code> to be broadcast to all
     *		    interested listeners.</li></ul>
     *
     *	@param	context	<code>FacesContext</code> for the current request.
     */
    public void validate(FacesContext context) {
	if (context == null) {
	    throw new NullPointerException();
	}

	// Submitted value == null means "the component was not submitted
	// at all";  validation should not continue
	Object submittedValue = getSubmittedValue();
	if (submittedValue == null) {
	    return;
	}

	Object newValue = submittedValue;
/*
FIXME: Decide if we ever want to the Tree to support Converters
	try {
	    newValue = getConvertedValue(context, submittedValue);
	}
	catch (ConverterException ce) {
	    addConversionErrorMessage(context, ce, submittedValue);
	    setValid(false);
	}
*/

	// Validate the value (check for required for now)
	validateValue(context, newValue);

	// If our value is valid, store the new value, erase the
	// "submitted" value, and emit a ValueChangeEvent if appropriate
	if (isValid()) {
	    Object previous = getValue();
	    setValue(newValue);
	    setSubmittedValue(null);
	    if (isDifferent(previous, newValue)) {
		queueEvent(new ValueChangeEvent(this, previous, newValue));
	    }
	}
    }

    /**
     *	<p> Return <code>true</code> if the objects are not equal.</p>
     *
     *	@param	val1    Value 1
     *	@param	val1	Value 2
     *
     *	@return	true if the 2 values are not equal
     */
    protected boolean isDifferent(Object val1, Object val2) {
	if (val1 == val2) {
	    // Same object, they're equal
	    return false;
	}
	if (val1 == null) {
	    // Not equal, and one is null
	    return true;
	}
	return !val1.equals(val2);
    }

    protected void validateValue(FacesContext context, Object newValue) {
	if (!isValid()) {
	    return;
	}
	if (isRequired() && ((newValue == null)
		    || (newValue.toString().trim().equals("")))) {
// FIXME: Add a message
// FacesMessage message =
//	message.setSeverity(FacesMessage.SEVERITY_ERROR);
// context.addMessage(getClientId(context), message);
	    setValid(false);
	}

// FIXME: Decide if we ever want to the Tree to support Validators (See UIInput)
    }


    /**
     *	<p> This method accepts the {@link TreeNode} which is to be selected.
     *	    The previous {@link TreeNode} that was selected will unselected.
     *	    No state is saved with this operation, the state is maintained on
     *	    the client.</p>
     *
     *	@deprecated Use #setValue(Object)
     *
     *	@param	treeNode    The {@link TreeNode} to be selected.
     */
    public void selectTreeNode(TreeNode treeNode) {
	setValue(treeNode);
//	selectTreeNode(treeNode.getClientId(FacesContext.getCurrentInstance()));
    }

    /**
     *	<p> This method accepts the clientId of a {@link TreeNode} which is to
     *	    be selected.  The previous {@link TreeNode} that was selected will
     *	    unselected.  No state is saved with this operation, the state is
     *	    maintained on the client-side.</p>
     *
     *	@deprecated Use #setValue(Object)
     *
     *	@param	clientId    Client id of the {@link TreeNode} to be selected.
     */
    public void selectTreeNode(String clientId) {
	setValue(clientId);
//	FacesContext context = FacesContext.getCurrentInstance();
//	context.getExternalContext().getRequestMap().put(getClientId(context)+SELECTED_SUFFIX, clientId);
    }

    /**
     *	<p> This method returns the {@link TreeNode} client ID that is
     *	    selected according the browser cookie.  This method is generally
     *	    only useful during the decode process.</p>
     *
     *	@return	The selected tree node (according to the cookie).
     */
    public String getCookieSelectedTreeNode() {
	FacesContext context = FacesContext.getCurrentInstance();	
	String treeCID = getClientId(context);

	// If it's stull null, look at cookies...
	Cookie cookie = getCookie(context, treeCID + COOKIE_SUFFIX);
        
	if (cookie != null) {
	    return cookie.getValue();
	}

	// Not found, return null
	return null;
    }
    
    /**
     *	<p> This method will return the {@link TreeNode} client ID that is
     *	    selected according the browser cookie.  This method is only 
     *      useful during the decode process as the cookie will typically be
     *      reset to null immediately after the request is processed.</p>
     *
     *	@return	The selected tree node (according to the cookie).
     */
    public String getCookieExpandNode() {
        FacesContext context = FacesContext.getCurrentInstance();
        String treeCID = getClientId(context);
        Cookie cookie = getCookie(context, treeCID + COOKIE_SUFFIX_EXPAND);        
        
	if (cookie != null) {            
	    return cookie.getValue();
	}

	// Not found, return null
	return null;
    }
    
    private Cookie getCookie(FacesContext context, String name) {
        ExternalContext extCtx = context.getExternalContext();	
	
	return (Cookie) extCtx.getRequestCookieMap().get(name);
    }

    //////////////////////////////////////////////////////////////////////
    //	ValueHolder Methods
    //////////////////////////////////////////////////////////////////////

    /**
     *
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[8];
        values[0] = super.saveState(context);
        values[1] = saveAttachedState(context, converter);
        values[2] = value;
        values[3] = localValueSet ? Boolean.TRUE : Boolean.FALSE;
        values[4] = this.valid ? Boolean.TRUE : Boolean.FALSE;
        values[5] = saveAttachedState(context, validators);
        values[6] = saveAttachedState(context, validatorBinding);
        values[7] = saveAttachedState(context, valueChangeMethod);

        return (values);
    }


    /**
     *
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;

        super.restoreState(context, values[0]);
        converter = (Converter) restoreAttachedState(context, values[1]);
        value = values[2];
        localValueSet = ((Boolean) values[3]).booleanValue();
        valid = ((Boolean) values[4]).booleanValue();

	List restoredValidators = null;
	Iterator iter = null;
	if (null != (restoredValidators = (List)
		     restoreAttachedState(context, values[5]))) {
	    // if there were some validators registered prior to this
	    // method being invoked, merge them with the list to be
	    // restored.
	    if (null != validators) {
		iter = restoredValidators.iterator();
		while (iter.hasNext()) {
		    validators.add(iter.next());
		}
	    }
	    else {
		validators = restoredValidators;
	    }
	}

        validatorBinding = (MethodBinding) restoreAttachedState(context,
								values[6]);
        valueChangeMethod = (MethodBinding) restoreAttachedState(context,
								 values[7]);
    }


    /**
     *	<p> Converter.</p>
     */
    private Converter converter = null;

    /**
     *	<p> The set of {@link Validator}s associated with this
     *	    <code>UIComponent</code>.</p>
     */
    private List validators = null;

    /**
     *
     */
    private MethodBinding validatorBinding = null;

    /**
     *	<p> The submittedValue value of this component.</p>
     */
    private Object submittedValue = null;

    /**
     *	<p> Toggle indicating validity of this component.</p>
     */
    private boolean valid = true;

    /**
     *	<p> The "localValueSet" state for this component.</p>
     */
    private boolean localValueSet;

    /**
     *	<p> The "valueChange" MethodBinding for this component.
     */
    private MethodBinding valueChangeMethod = null;

    /**
     *	<p> The value of the <code>Tree</code>.  This should be a String
     *	    representing the client id of the selected
     *	    <code>TreeNode</code>.</p>
     */
    private Object value = null;

    /**
     *	<p> This is the {@link com.sun.rave.web.ui.theme.Theme} key used to retrieve
     *	    the JavaScript needed for this component.</p>
     *
     *	@see com.sun.rave.web.ui.theme.Theme#getPathToJSFile(String)
     */
    public static final String	JAVA_SCRIPT_THEME_KEY  =    "tree";

    /**
     *	<p> This is the location of the XML file that declares the layout for
     *	    the PanelGroup. (layout/tree.xml)</p>
     */
    public static final String	LAYOUT_KEY	=	"layout/tree.xml";

    /**
     *	<p> This is the suffix appended to the client id when forming a request
     *	    attribute key.  The value associated with the generated key
     *	    indicates which node should be selected.  The renderer uses this
     *	    information to generate JavaScript to select this node, overriding
     *	    the previous selection.</p>
     */
    public static final String	SELECTED_SUFFIX	=	"_select";

    /**
     *	<p> This is the suffix appended to the client id to form the key to the
     *	    cookie Map needed to retrieve the tree selection.</p>
     */
    public static final String	COOKIE_SUFFIX	=	"-hi";
    
    /**
     *	<p> This is the suffix appended to the client id to form the key to the
     *	    cookie Map needed to retrieve the node that may need to be
     *      expanded (because it was just selected).</p>
     */
    public static final String COOKIE_SUFFIX_EXPAND = "-expand";
}
