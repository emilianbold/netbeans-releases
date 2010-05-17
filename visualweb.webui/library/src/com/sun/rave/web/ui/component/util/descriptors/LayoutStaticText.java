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
