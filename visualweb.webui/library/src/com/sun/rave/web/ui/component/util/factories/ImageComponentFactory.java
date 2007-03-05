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
import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIGraphic;
import javax.faces.context.FacesContext;


/**
 *  <P>	This factory is responsible for creating a {@link ImageComponent}
 *	UIComponent.</P>
 *
 *  @author Rick Ratta
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class ImageComponentFactory extends ComponentFactoryBase {

    /**
     *	<P> This is the factory method responsible for creating the
     *	    {@link ImageComponent} UIComponent.</P>
     *
     *	@param	context	    The FacesContext
     *
     *	@param	descriptor  The {@link LayoutComponent} descriptor that is
     *			    associated with the requested {@link ImageComponent}.
     *
     *	@param	parent	    The parent UIComponent
     *
     *	@return	The newly created {@link ImageComponent}.
     */
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
	// Create the UIComponent
	UIGraphic image = new ImageComponent();

	// This needs to be done here (before setOptions) so that $...{...}
	// expressions can be resolved...
	if (parent != null) {
//	    parent.getChildren().add(image);
	    addChild(context, descriptor, parent, image);
	}

	// Set all the attributes / properties (allow these to override theme)
	setOptions(context, descriptor, image);

	// Return the value
	return image;
    }
}
