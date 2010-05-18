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
package com.sun.rave.web.ui.renderer.template;

import com.sun.rave.web.ui.component.TemplateComponent;
import com.sun.rave.web.ui.component.util.descriptors.LayoutDefinition;
import com.sun.rave.web.ui.component.util.descriptors.LayoutElement;
import com.sun.rave.web.ui.component.util.descriptors.Resource;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.render.Renderer;


/**
 *  <p>	This renderer is a generic "template-based" renderer.  It uses a
 *	{@link LayoutElement} tree as its template and walks this tree.  This
 *	renderer will actually delegate the encode functionality to the
 *	{@link LayoutDefinition} object, which is the top of the LayoutElement
 *	in the tree.</p>
 *
 *  <p>	This renderer also has the feature of registering {@link Resource}
 *	objects to the Request scope prior to rendering its output.  This
 *	allows {@link Resource} objects such as ResourceBundles to be added
 *	to the Request scope for easy access.</p>
 *
 *  @see    LayoutDefinition
 *  @see    LayoutElement
 *  @see    Resource
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class TemplateRenderer extends Renderer {

    /**
     *	<p> This method returns true.  This method indicates that this
     *	    <code>Renderer</code> will assume resposibilty for rendering its
     *	    own children.</p>
     *
     *	@return true
     *
     *	@see #encodeChildren(FacesContext, UIComponent)
     */
    public boolean getRendersChildren() {
	return true;
    }


    /**
     *	<p> This method initializes the Resources so they will be available for
     *	    children.  It then calls encodeBegin on the superclass.</p>
     *
     *	@param	context	    The FacesContext
     *	@param	component   The UIComponent, should be a
     *			    {@link TemplateComponent}
     */
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
	// Make sure we have a TemplateComponent
	if (!(component instanceof TemplateComponent)) {
	    throw new IllegalArgumentException(
		"TemplateRenderer requires that its UIComponent be an "+
		"instance of TemplateComponent!");
	}
	TemplateComponent tempComp = (TemplateComponent)component;
	LayoutDefinition def = tempComp.getLayoutDefinition(context);

	// First ensure that our Resources are available
	Iterator it = def.getResources().iterator();
	Resource resource = null;
	while (it.hasNext()) {
	    resource = (Resource)it.next();
	    // Just calling getResource() puts it in the Request scope
	    resource.getFactory().getResource(context, resource);
	}

	// Call the super class
	super.encodeBegin(context, component);
    }

    /**
     *	<p> This method prevents the super class's default functionality of
     *	    rendering the child UIComponents.  This <code>Renderer</code>
     *	    implementation requires that the children be explicitly
     *	    rendered.  This method does nothing.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	component   The <code>UIComponent</code>
     */
    public void encodeChildren(FacesContext context, UIComponent component) {
	// Do nothing...
    }

    /**
     *	<p> This method performs the rendering for the TemplateRenderer.  It
     *	    expects that component be an instanceof {@link TemplateComponent}.
     *	    It obtains the {@link LayoutDefinition} from the
     *	    {@link TemplateComponent}, initializes the {@link Resource}
     *	    objects defined by the {@link LayoutDefinition} (if any), and
     *	    finally delegates the encoding to the
     *	    {@link LayoutDefinition#encode(FacesContext, TemplateComponent)}
     *	    method of the {@link LayoutDefinition}.</p>
     *
     *	@param	context	    The FacesContext object
     *	@param	component   The {@link TemplateComponent}
     */
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
	// Sanity Check...
	if (!component.isRendered()) {
	    return;
	}

	// Get the LayoutDefinition and begin rendering
	TemplateComponent tempComp = (TemplateComponent)component;
	LayoutDefinition def = tempComp.getLayoutDefinition(context);

	// The following "encode" method does all the rendering
	def.encode(context, (UIComponent) tempComp);
    }

    /**
     *	<p> Decode any new state of the specified UIComponent from the request
     *	    contained in the specified FacesContext, and store that state on
     *	    the UIComponent.</p>
     *
     *	<p> During decoding, events may be queued for later processing (by
     *	    event listeners that have registered an interest), by calling
     *	    <code>queueEvent()</code> on the associated UIComponent.</p>
     *
     *	<p> This implementation of this method invokes the super class and
     *	    then any handlers that have been registered to process decode
     *	    functionality.  The execution of these handlers is delegated to
     *	    the LayoutDefinition.</p>
     *
     * @param	context	    FacesContext for the request we are processing
     * @param	component   UIComponent to be decoded.
     *
     * @exception NullPointerException if <code>context</code>
     *  or <code>component</code> is <code>null</code>
     */
    public void decode(FacesContext context, UIComponent component) {
	// Call the super first
	super.decode(context, component);

	// Call any decode handlers
	TemplateComponent tempComp = (TemplateComponent)component;
	LayoutDefinition def = tempComp.getLayoutDefinition(context);
	def.decode(context, component);
    }
}
