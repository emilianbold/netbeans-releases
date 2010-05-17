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

import com.sun.rave.web.ui.component.util.event.HandlerContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
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
import javax.faces.render.Renderer;

/**
 *  <P>	This interface is declares the methods required to be a
 *	LayoutElement.  A LayoutElement is the building block of the tree
 *	structure which defines a layout for a particular component.  There are
 *	different implementations of LayoutElement that provide various
 *	different types of functionality and data.  Some examples are:</P>
 *
 *  <UL><LI>Conditional ({@link LayoutIf}), this allows portions of the
 *	    layout tree to be conditionally rendered.</LI>
 *	<LI>Iterative ({@link LayoutWhile}), this allows portions of the
 *	    layout tree to be iteratively rendered.</LI>
 *	<LI>UIComponent ({@link LayoutComponent}), this allows concrete
 *	    UIComponents to be used.  If the component doesn't already exist,
 *	    it will be created automatically.</LI>
 *	<LI>Facet place holders ({@link LayoutFacet}), this provides a means
 *	    to specify where a facet should be rendered.  It is not a facet
 *	    itself but where a facet should be drawn.  However, in addition,
 *	    it may specify a default value if no facet was provided.</LI></UL>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public interface LayoutElement extends java.io.Serializable {

    /**
     *	This method is used to add a LayoutElement.  LayoutElements should be
     *	added sequentially in the order in which they are to be rendered.
     */
    public void addChildLayoutElement(LayoutElement element);


    /**
     *	This method returns the child LayoutElements as a List.
     *
     *	@return List of LayoutElements
     */
    public List getChildLayoutElements();


    /**
     *	This method returns the parent LayoutElement.
     *
     *	@return	parent LayoutElement
     */
    public LayoutElement getParent();


    /**
     *	This method returns the LayoutDefinition.  If unable to, it will throw
     *	an Exception.
     *
     *	@return	The LayoutDefinition
     */
    public LayoutDefinition getLayoutDefinition();


    /**
     *	<P> This method retrieves the Handlers for the requested type.</P>
     *
     *	@param	type	The type of Handlers to retrieve.
     *
     *	@return	A List of Handlers.
     */
    public List getHandlers(String type);

    /**
     *	<P> This method associates 'type' with the given list of Handlers.</P>
     *
     *	@param	type	    The String type for the List of Handlers
     *	@param	handlers    The List of Handlers
     */
    public void setHandlers(String type, List handlers);

    /**
     *	Accessor method for id.  This should always return a non-null value,
     *	it may return "" if id does not apply.
     *
     *	@return a non-null id
     */
    public String getId(FacesContext context, UIComponent parent);

    /**
     *	<p> This method generally should not be used.  It does not resolve
     *	    expressions.  Instead use
     *	    {@link #getId(FacesContext, UIComponent)}.</p>
     *
     *	@return	The unevaluated id.
     */
    public String getUnevaluatedId();

    /**
     *	This method performs any encode action for this particular
     *	LayoutElement.
     *
     *	@param	context	    The FacesContext
     *	@param	component   The UIComponent
     */
    public void encode(FacesContext context, UIComponent component) throws IOException;

    /**
     *
     */
    public Object dispatchHandlers(HandlerContext handlerCtx, List handlers);

    /**
     *	<P> This method iterates over the handlers and executes each one.  A
     *	    HandlerContext will be created to pass to each Handler.  The
     *	    HandlerContext object is reused across all Handlers that are
     *	    invoked; the setHandler(Handler) method is invoked with the
     *	    correct Handler descriptor before the handler is executed.</P>
     *
     *	@param	context	    The FacesContext
     *	@param	eventType   The event type which is being fired
     *	@param	event	    An optional EventObject providing more detail
     *
     *	@return	By default, (null) is returned.  However, if any of the
     *		handlers produce a non-null return value, then the value from
     *		the last handler to produces a non-null return value is
     *		returned.
     */
    public Object dispatchHandlers(FacesContext context, String eventType, EventObject event);
}
