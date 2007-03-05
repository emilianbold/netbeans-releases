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
package com.sun.rave.web.ui.component.util.descriptors;

import com.sun.rave.web.ui.component.util.event.AfterLoopEvent;
import com.sun.rave.web.ui.component.util.event.BeforeLoopEvent;
import com.sun.rave.web.ui.util.PermissionChecker;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;


/**
 *  <P>	This class defines a LayoutWhile {@link LayoutElement}.  The
 *	LayoutWhile provides the functionality necessary to iteratively
 *	display a portion of the layout tree.  The condition is a boolean
 *	equation and may use "$...{...}" type expressions to substitute
 *	values.</P>
 *
 *  @see com.sun.rave.web.ui.util.VariableResolver
 *  @see com.sun.rave.web.ui.util.PermissionChecker
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutWhile extends LayoutIf implements LayoutElement {

    /**
     *	Constructor
     */
    public LayoutWhile(LayoutElement parent, String condition) {
	super(parent, condition);
    }


    /**
     *	<P> This method always returns true.  The condition is checked in
     *	    {@link #shouldContinue(UIComponent)} instead of here because
     *	    the {@link #encode(FacesContext, UIComponent)} method
     *	    evaluates the condition and calls the super.  Performing the check
     *	    here would cause the condition to be evaluated twice.</P>
     *
     *	@param	context	    The FacesContext
     *	@param	component   The UIComponent
     *
     *	@return	true
     */
    protected boolean encodeThis(FacesContext context, UIComponent component) {
	return true;
    }

    /**
     *	<P> This method returns true if the condition of this LayoutWhile is
     *	    met, false otherwise.  This provides the functionality for
     *	    iteratively displaying a portion of the layout tree.</P>
     *
     *	@param	component   The UIComponent
     *
     *	@return	true if children are to be rendered, false otherwise.
     */
    protected boolean shouldContinue(UIComponent component) {
	PermissionChecker checker =
	    new PermissionChecker(this, component, getCondition());
//	return checker.evaluate();
	return checker.hasPermission();
    }

    /**
     *	<P> This implementation overrides the parent <code>encode</code>
     *	    method.  It does this to cause the encode process to loop while
     *	    {@link #shouldContinue(UIComponent)} returns
     *	    true.  Currently there is no infinite loop checking, so be
     *	    careful.</P>
     *
     *	@param	context	    The FacesContext
     *	@param	component   The UIComponent
     */
    public void encode(FacesContext context, UIComponent component) throws IOException {
	Object result = dispatchHandlers(context, BEFORE_LOOP,
	    new BeforeLoopEvent((UIComponent)component));
	while (shouldContinue(component)) {
	    super.encode(context, component);
	}
	result = dispatchHandlers(context, AFTER_LOOP,
	    new AfterLoopEvent((UIComponent)component));
    }

    /**
     *	<P> This is the event "type" for
     *	    {@link com.sun.rave.web.ui.component.util.event.Handler} elements to be
     *	    invoked after this LayoutWhile is processed (outside loop).</P>
     */
     public static final String AFTER_LOOP =	"afterLoop";

    /**
     *	<P> This is the event "type" for
     *	    {@link com.sun.rave.web.ui.component.util.event.Handler} elements to be
     *	    invoked before this LayoutWhile is processed (outside loop).</P>
     */
     public static final String BEFORE_LOOP =	"beforeLoop";
}
