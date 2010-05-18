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
package com.sun.rave.web.ui.component.util.handlers;

import com.sun.rave.web.ui.component.util.event.HandlerContext;
import com.sun.rave.web.ui.component.util.event.HandlerDefinition;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 *  <p>	This class contains {@link com.sun.rave.web.ui.component.util.event.Handler}
 *	methods that perform component functions.</p>
 *
 *  @author  Ken Paulsen (ken.paulsen@sun.com)
 */
public class ComponentHandlers {

    /**
     *	<p> Default Constructor.</p>
     */
    public ComponentHandlers() {
    }

    /**
     *	<p> This handler returns the children of the given
     *	    <code>UIComponent</code>.</p>
     *
     *	<p> Input value: "parent" -- Type: <code>UIComponent</code></p>
     *
     *	<p> Output value: "children" -- Type: <code>java.util.List</code></p>
     *	<p> Output value: "size"     -- Type: <code>java.lang.Integer</code></p>
     *
     *	@param	context	The HandlerContext.
     */
    public void getChildren(HandlerContext context) {
	UIComponent parent = (UIComponent)context.getInputValue("parent");
	List list = parent.getChildren();
	context.setOutputValue("children", list);
	context.setOutputValue("size", new Integer(list.size()));
    }

    /**
     *	<p> This handler sets a <code>UIComponent</code> attribute /
     *	    property.</p>
     *
     *	<p> Input value: "component" -- Type: <code>UIComponent</code></p>
     *	<p> Input value: "property" -- Type: <code>String</code></p>
     *	<p> Input value: "value" -- Type: <code>Object</code></p>
     *
     *	@param	context	The HandlerContext.
     */
    public void setComponentProperty(HandlerContext context) {
	UIComponent component = (UIComponent)context.getInputValue("component");
	String propName = (String)context.getInputValue("property");
	Object value = context.getInputValue("value");

	// Set the attribute or property value
	component.getAttributes().put(propName, value);
    }
}
