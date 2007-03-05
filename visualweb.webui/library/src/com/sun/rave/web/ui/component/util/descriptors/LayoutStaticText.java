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

import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.component.util.event.AfterCreateEvent;
import com.sun.rave.web.ui.component.util.event.BeforeCreateEvent;

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
 *  <P>	This class defines a LayoutStaticText.  A LayoutStaticText describes a
 *	text to be output to the screen.  This element is NOT a
 *	<code>UIComponent</code>.</P>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutStaticText extends LayoutElementBase implements LayoutElement {

    /**
     *	<P> Constructor.</P>
     */
    public LayoutStaticText(LayoutElement parent, String id, String value) {
	super(parent, id);
	_value = value;
    }

    /**
     *
     */
    public String getValue() {
	return _value;
    }

    /**
     *	<P> This method displays the text described by this component.  If the
     *	    text includes an EL expression, it will be evaluated.  It returns
     *	    false to avoid attempting to render children.</P>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	component   The <code>UIComponent</code>
     *
     *	@return	false
     */
    protected boolean encodeThis(FacesContext context, UIComponent component) throws IOException {
	// Get the ResponseWriter
	ResponseWriter writer = context.getResponseWriter();

	// Render the child UIComponent
//	if (staticText.isEscape()) {
//	    writer.writeText(getValue(), "value");
//	} else {
	    // This code depends on the side-effect of Util.setOption
	    // converting the string to a ValueBinding if needed.  The
	    // "__value" is arbitrary.
	    Object value = Util.setOption(
		context, "__value", getValue(),
		getLayoutDefinition(), component);
	    if (value instanceof ValueBinding) {
		value = ((ValueBinding)value).getValue(context);
	    }
	    if (value != null) {
		writer.write(value.toString());
	    }
//	}

	// No children
	return false;
    }

    private String _value   = null;
}
