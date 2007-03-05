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
package com.sun.rave.web.ui.component.util.factories;

import com.sun.rave.web.ui.component.util.descriptors.LayoutComponent;
import com.sun.rave.web.ui.component.Label;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;


/**
 *  <P>	This factory is responsible for creating a {@link Label}
 *	UIComponent.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class LabelFactory extends ComponentFactoryBase {

    /**
     *	<P> This is the factory method responsible for creating the
     *	    {@link Label} UIComponent.</P>
     *
     *	@param	context	    The FacesContext
     *
     *	@param	descriptor  The {@link LayoutComponent} descriptor that is
     *			    associated with the requested {@link Label}.
     *
     *	@param	parent	    The parent UIComponent
     *
     *	@return	The newly created {@link Label}.
     */
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
	// Create the UIComponent
	UIOutput label = new Label();

	// This needs to be done here (before setOptions) so that $...{...}
	// expressions can be resolved...
	if (parent != null) {
//	    parent.getChildren().add(label);
	    addChild(context, descriptor, parent, label);
	}

	// Set all the attributes / properties
	setOptions(context, descriptor, label);

	// Return the value
	return label;
    }
}
