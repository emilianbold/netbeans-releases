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

import com.sun.rave.web.ui.component.util.event.Handler;
import com.sun.rave.web.ui.component.util.event.HandlerContext;
import com.sun.rave.web.ui.component.util.event.HandlerDefinition;

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
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;


/**
 *  <p>	This class defines a LayoutAttribute.  A LayoutAttribute provides a
 *	means to write an attribute for the current markup tag.  A markup tag
 *	must be started, but not yet closed for this to work.</p>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutAttribute extends LayoutElementBase implements LayoutElement {

    /**
     *	<p> Constructor.</p>
     */
    public LayoutAttribute(LayoutElement parent, String name, String value, String property) {
	super(parent, name);
	_name = name;
	_value = value;
	_property = property;
    }

    /**
     *
     */
    public String getName() {
	return _name;
    }

    /**
     *
     */
    public String getValue() {
	return _value;
    }

    /**
     *
     */
    public String getProperty() {
	return _property;
    }

    /**
     *	<p> This method displays the text described by this component.  If the
     *	    text includes an EL expression, it will be evaluated.  It returns
     *	    false to avoid attempting to render children.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	component   The <code>UIComponent</code>
     *
     *	@return	false
     */
    protected boolean encodeThis(FacesContext context, UIComponent component) throws IOException {
	// Get the ResponseWriter
	ResponseWriter writer = context.getResponseWriter();

	// Render...
	Object value = resolveValue(context, component, getValue());
	if ((value != null) && !value.toString().trim().equals("")) {
	    String name = getName();
	    String prop = getProperty();
	    if (prop == null) {
		// Use the name if property is not supplied
		prop = name;
	    } else if (prop.equals("null")) {
		prop = null;
	    }
	    writer.writeAttribute(name, value, prop);
	}

	// No children
	return false;
    }

    private String _name	= null;
    private String _value	= null;
    private String _property	= null;
}
