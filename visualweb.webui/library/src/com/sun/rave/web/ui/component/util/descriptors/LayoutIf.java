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

import com.sun.rave.web.ui.util.PermissionChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIViewRoot;
import javax.faces.render.Renderer;


/**
 *  <P>	This class defines a LayoutIf {@link LayoutElement}.  The LayoutIf
 *	provides the functionality necessary to conditionally display a portion
 *	of the layout tree.  The condition is a boolean equation and may use
 *	"$...{...}" type expressions to substitute in values.</P>
 *
 *  @see com.sun.rave.web.ui.util.VariableResolver
 *  @see com.sun.rave.web.ui.util.PermissionChecker
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutIf extends LayoutElementBase implements LayoutElement {

    /**
     *	Constructor
     */
    public LayoutIf(LayoutElement parent, String condition) {
	super(parent, null);
	_condition = condition;
    }


    /**
     *	Accessor for condition boolean equation.
     */
    public String getCondition() {
	return _condition;
    }


    /**
     *	This method returns true if the condition of this LayoutIf is met,
     *	false otherwise.  This provides the functionality for conditionally
     *	displaying a portion of the layout tree.
     *
     *	@param	context	    The FacesContext
     *	@param	component   The UIComponent
     *
     *	@return	true if children are to be rendered, false otherwise.
     */
    protected boolean encodeThis(FacesContext context, UIComponent component) {
	PermissionChecker checker =
	    new PermissionChecker(this, component, getCondition());
//	return checker.evaluate();
	return checker.hasPermission();
    }

    private String _condition = null;
}
