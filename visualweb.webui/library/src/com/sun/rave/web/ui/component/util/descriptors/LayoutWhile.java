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
