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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *  <P>	A HandlerDefinition defines a "handler" that may be invoked in the
 *	process of executing an event.  A HandlerDefinition has an
 *	<strong>id</strong>, <strong>java method</strong>, <strong>input
 *	definitions</strong>, <strong>output definitions</strong>, and
 *	<strong>child handlers</strong>.</P>
 *
 *  <P>	The <strong>java method</strong> to be invoked must have the
 *	following method signature:</P>
 *
 *  <P> <BLOCKQUOTE></CODE>
 *	    public void beginDisplay(HandlerContext handlerCtx)
 *	</CODE></BLOCKQUOTE></P>
 *
 *  <P>	<code>void</code> above can return a value.  Depending on the type of
 *	event, return values may be handled differently.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class HandlerDefinition implements java.io.Serializable {

    /**
     *	Constructor
     */
    public HandlerDefinition(String id) {
	_id = id;
    }

    /**
     *	This method returns the id for this handler.
     */
    public String getId() {
	return _id;
    }

    /**
     *	For future tool support
     */
    public String getDescription() {
	return _description;
    }

    /**
     *	For future tool support
     */
    public void setDescription(String desc) {
	_description = desc;
    }

    /**
     *	<P> This method sets the event handler (method) to be invoked.  The
     *	    method should be public and accept a prameter of type
     *	    "HandlerContext"  Example:</P>
     *
     *	<P> <BLOCKQUOTE>
     *		public void beginDisplay(HandlerContext handlerCtx)
     *	    </BLOCKQUOTE></P>
     *
     *	@param cls	    The full class name containing method
     *	@param methodName   The method name of the handler within class
     */
    public void setHandlerMethod(String cls, String methodName) {
	if ((cls == null) || (methodName == null)) {
	    throw new IllegalArgumentException(
		"Class name and method name must be non-null!");
	}
	_methodClass = cls;
	_methodName = methodName;
    }

    /**
     *
     */
    public void setHandlerMethod(Method method) {
	if (method != null) {
	    _methodName = method.getName();
	    _methodClass = method.getDeclaringClass().getName();
	} else {
	    _methodName = null;
	    _methodClass = null;
	}
	_method = method;
    }

    /**
     *	<p> This method determines if the handler is static.</p>
     */
    public boolean isStatic() {
	if (_static == null) {
	    _static = Boolean.valueOf(
		    Modifier.isStatic(getHandlerMethod().getModifiers()));
	}
	return _static.booleanValue();
    }

    /**
     *
     */
    public Method getHandlerMethod() {
	if (_method != null) {
	    // return cached Method
	    return _method;
	}

	// See if we have the info to find it
	if ((_methodClass != null) && (_methodName != null)) {
	    // Find the class
	    Class clzz = null;
	    try {
		clzz = Class.forName(_methodClass);
	    } catch (ClassNotFoundException ex) {
		throw new RuntimeException("'"+_methodClass+"' not found!", ex);
	    }

	    // Find the method on the class
	    Method method = null;
	    try {
		method = clzz.getMethod(_methodName, EVENT_ARGS);
	    } catch (NoSuchMethodException ex) {
		throw new RuntimeException(
			"Method '"+_methodName+"' not found!", ex);
	    }

	    // Cache the _method
	    _method = method;
	}

	// Return the Method if there is one
	return _method;
    }

    /**
     *	This method adds an IODescriptor to the list of input descriptors.
     *	These descriptors define the input parameters to this handler.
     *
     *	@param desc	The input IODescriptor to add
     */
    public void addInputDef(IODescriptor desc) {
	_inputDefs.put(desc.getName(), desc);
    }

    /**
     *	This method sets the input IODescriptors for this handler.
     *
     *	@param inputDefs	The Map of IODescriptors
     */
    public void setInputDefs(Map inputDefs) {
	if (inputDefs == null) {
	    throw new IllegalArgumentException(
		"inputDefs cannot be null!");
	}
	_inputDefs = inputDefs;
    }

    /**
     *	This method retrieves the Map of input IODescriptors.
     *
     *	@return The Map of IODescriptors
     */
    public Map getInputDefs() {
	return _inputDefs;
    }

    /**
     *	This method returns the requested IODescriptor, null if not found.
     */
    public IODescriptor getInputDef(String name) {
	return (IODescriptor)_inputDefs.get(name);
    }

    /**
     *	This method adds an IODescriptor to the list of output descriptors.
     *	These descriptors define the output parameters to this handler.
     *
     *	@param desc	The IODescriptor to add
     */
    public void addOutputDef(IODescriptor desc) {
	_outputDefs.put(desc.getName(), desc);
    }

    /**
     *	This method sets the output IODescriptors for this handler.
     *
     *	@param outputDefs    The Map of output IODescriptors
     */
    public void setOutputDefs(Map outputDefs) {
	if (outputDefs == null) {
	    throw new IllegalArgumentException(
		"outputDefs cannot be null!");
	}
	_outputDefs = outputDefs;
    }

    /**
     *	This method retrieves the Map of output IODescriptors.
     *
     *	@return The Map of output IODescriptors
     */
    public Map getOutputDefs() {
	return _outputDefs;
    }

    /**
     *	This method returns the requested IODescriptor, null if not found.
     */
    public IODescriptor getOutputDef(String name) {
	return (IODescriptor)_outputDefs.get(name);
    }

    /**
     *	This method adds a Handler to the list of child handlers.  Child
     *	Handlers are executed PRIOR to this handler executing.
     *
     *	@param desc	The Handler to add
     */
    public void addChildHandler(Handler desc) {
	_childHandlers.add(desc);
    }

    /**
     *	This method sets the List of child Handlers for this HandlerDefinition.
     *
     *	@param childHandlers	The List of child Handler objects
     */
    public void setChildHandlers(List childHandlers) {
	if (childHandlers == null) {
	    throw new IllegalArgumentException(
		"childHandlers cannot be null!");
	}
	_childHandlers = childHandlers;
    }

    /**
     *	This method retrieves the List of child Handler.
     *
     *	@return The List of child Handler for this handler.
     */
    public List getChildHandlers() {
	return _childHandlers;
    }


    public static final Class[] EVENT_ARGS = new Class[] {HandlerContext.class};

    private String		_id			= null;
    private String		_description		= null;
    private String		_methodClass		= null;
    private String		_methodName		= null;
    private transient Method	_method			= null;
    private Map			_inputDefs		= new HashMap(5);
    private Map			_outputDefs		= new HashMap(5);
    private List		_childHandlers		= new ArrayList(5);
    private transient Boolean	_static			= null;

    private static final long serialVersionUID = 0xA8B7C6D5E4F30211L; 
}
