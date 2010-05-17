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
package com.sun.rave.web.ui.component.util.event;

import com.sun.rave.web.ui.component.util.descriptors.LayoutElement;

import java.util.EventObject;

import javax.faces.context.FacesContext;


/**
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public interface HandlerContext {

    /**
     *	<P> Accessor for the FacesContext.</P>
     *
     *	@return FacesContext
     */
    public FacesContext getFacesContext();

    /**
     *	<P> Accessor for the LayoutElement associated with this Handler.  The
     *	    LayoutElement associated with this Handler is the LayoutElement
     *	    which declared the handler.  This provides a way for the handler
     *	    to obtain access to the LayoutElement which is responsible for it
     *	    being invoked.</P>
     */
    public LayoutElement getLayoutElement();

    /**
     *	<P> Accessor for the EventObject associated with this Handler.  This
     *	    may be null if an EventObject was not created for this handler.
     *	    An EventObject, if it does exist, may provide additional details
     *	    describing the context in which this Event is invoked.</P>
     */
    public EventObject getEventObject();

    /**
     *	<P> This method provides access to the EventType.  This is mostly
     *	    helpful for diagnostics, but may be used in a handler to determine
     *	    more information about the context in which the code is
     *	    executing.</P>
     */
    public String getEventType();

    /**
     *	<P> Accessor for the Handler descriptor for this Handler.  The Handler
     *	    descriptor object contains specific meta information describing
     *	    the invocation of this handler.  This includes details such as
     *	    input values, and where output values are to be set.</P>
     */
    public Handler getHandler();

    /**
     *	<P> Setter for the Handler descriptor for this Handler.</P>
     *
     *	@param	handler	    The Handler
     */
    public void setHandler(Handler handler);

    /**
     *	<P> Accessor for the Handler descriptor for this Handler.  The
     *	    HandlerDefinition descriptor contains meta information about the
     *	    actual Java handler that will handle the processing.  This
     *	    includes the inputs required, outputs produces, and the types for
     *	    both.</P>
     */
    public HandlerDefinition getHandlerDefinition();

    /**
     *	<P> This method returns the value for the named input.  Input values
     *	    are not stored in this Context itself, but in the Handler.  If
     *	    you are trying to set input values for a handler, you must create
     *	    a new Handler object and set its input values.</P>
     *
     *	@param	name	    The input name
     *
     *	@return	The value of the input (null if not found)
     */
    public Object getInputValue(String name);

    /**
     *	<P> This method retrieves an Output value. Output values must not be
     *	    stored in this Context itself (remember HandlerContext objects
     *	    are shared).  Output values are stored according to what is
     *	    specified in the HandlerDefintion.</P>
     *
     *	@param	name	    The output name
     *
     *	@return	The value of the output (null if not found)
     */
    public Object getOutputValue(String name);

    /**
     *	<P> This method sets an Output value. Output values must not be
     *	    stored in this Context itself (remember HandlerContext objects
     *	    are shared).  Output values are stored according to what is
     *	    specified in the HandlerDefintion.</P>
     */
    public void setOutputValue(String name, Object value);
}
