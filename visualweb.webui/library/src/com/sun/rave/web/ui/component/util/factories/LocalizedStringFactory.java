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
package com.sun.rave.web.ui.component.util.factories;

import com.sun.rave.web.ui.component.util.descriptors.LayoutComponent;
import com.sun.rave.web.ui.util.MessageUtil;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;


/**
 *  <P>	This factory is responsible for creating a UIComponent that contains a
 *	localized message.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class LocalizedStringFactory extends ComponentFactoryBase {

    /**
     *	This is the factory method responsible for creating the UIComponent.
     *
     *	@param	context	    The FacesContext
     *	@param	descriptor  The LayoutComponent descriptor that is associated
     *			    with the requested UIComponent.
     *	@param	parent	    The parent UIComponent
     *
     *	@return	The newly created HtmlOutputText
     */
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
	// First make sure we have all our input.
	String baseName = (String)getEvaluatedOption(
	    context, descriptor, parent, BASE_NAME, true);
	String key = (String)getEvaluatedOption(
	    context, descriptor, parent, KEY, true);

	// parameters are optional
	Object parameters = getEvaluatedOption(
	    context, descriptor, parent, PARAMETERS, false);
	Object args[] = null;
	if (parameters != null) {
	    // Convert from list to Object[]
	    if (parameters.getClass().isArray()) {
		args = (Object[]) parameters;
	    } else {
		// Not a list, just treat as a single value
		args = new Object[] {parameters.toString()};
	    }
	}

	// Get the Localized Message
	String value = MessageUtil.getMessage(context, baseName, key, args);

	// Create the UIComponent
	UIOutput output = new HtmlOutputText();

	// This needs to be done here (before setOptions) so that $...{...}
	// expressions can be resolved... may want to defer these?
	if (parent != null) {
//	    parent.getChildren().add(output);
	    addChild(context, descriptor, parent, output);
	}

	// Set the value
	output.setValue(value);

	// Set all the attributes / properties
	setOptions(context, descriptor, output);

	// Return the new UIComponent
	return output;
    }


    /**
     *	The key for the option representing the base name of the
     *	ResourceBundle.
     */
    public static final String	BASE_NAME	=   "baseName";

    /**
     *	The key for the option representing the ResourceBundle key.
     */
    public static final String	KEY		=   "key";

    /**
     *	The key for the option representing the ResourceBundle entries
     *	parameters.
     */
    public static final String	PARAMETERS	=   "parameters";
}
