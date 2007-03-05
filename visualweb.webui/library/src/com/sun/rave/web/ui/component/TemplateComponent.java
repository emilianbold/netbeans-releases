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
import com.sun.rave.web.ui.component.util.descriptors.LayoutDefinition;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 *  <P>	This interface defines additional methods in addition to those defined
 *	by UIComponent that are needed to work with a TemplateRenderer.</P>
 *
 *  <P>	JSF did not define an interface for UIComponent, so I cannot extend an
 *	interface here.  This means that casting is needed to use UIComponent
 *	features from a TemplateComponent.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public interface TemplateComponent extends ChildManager, NamingContainer {

    /**
     *	This method will find the request child UIComponent by id.  If it is
     *	not found, it will attempt to create it if it can find a LayoutElement
     *	describing it.
     *
     *	@param	context	    The FacesContext
     *	@param	id	    The UIComponent id to search for
     *
     *	@return	The requested UIComponent
     */
    public UIComponent getChild(FacesContext context, String id);

    /**
     *	This method returns the LayoutDefinition associated with this component.
     *
     *	@param	context	The FacesContext
     *
     *	@return	LayoutDefinition associated with this component.
     */
    public LayoutDefinition getLayoutDefinition(FacesContext context);

    /**
     *	This method returns the LayoutDefinitionKey for this component.
     *
     *	@return	key	The key to use in the LayoutDefinitionManager
     */
    public String getLayoutDefinitionKey();

    /**
     *	This method sets the LayoutDefinition key for this component.
     *
     *	@param	key The key to use in the LayoutDefinitionManager
     */
    public void setLayoutDefinitionKey(String key);
}
