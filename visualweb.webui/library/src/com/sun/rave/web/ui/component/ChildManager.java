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
package com.sun.rave.web.ui.component;

import com.sun.rave.web.ui.component.util.descriptors.LayoutComponent;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 *  <P>	This interface defines a method to find or create a child
 *	<code>UIComponent</code>.  It is designed to be used in conjunction
 *	with <code>UIComponent</code> implementations.</P>
 *
 *  @see    com.sun.rave.web.ui.component.TemplateComponent
 *  @see    com.sun.rave.web.ui.component.util.Util
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public interface ChildManager {

    /**
     *	<P> This method will find the request child UIComponent by id (the id
     *	    is obtained from the given {@link LayoutComponent}).  If it is not
     *	    found, it will attempt to create it from the supplied
     *	    {@link LayoutComponent}.</P>
     *
     *	@param	context	    FacesContext
     *	@param	descriptor  {@link LayoutComponent} describing the UIComponent
     *
     *	@return	Requested UIComponent
     */
    public UIComponent getChild(FacesContext context, LayoutComponent descriptor);
}
