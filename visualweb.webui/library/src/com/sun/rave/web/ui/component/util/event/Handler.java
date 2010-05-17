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

import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.util.TypeConverter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;


/**
 *  <P>	This class contains the information necessary to invoke a Handler.  The
 *	{@link HandlerDefinition} class provides a definition of how to invoke
 *	a Handler, this class uses that information with in conjuction with
 *	information provided in this class to execute the <strong>handler
 *	method</strong>.  This class typically will hold input values and
 *	specify where output should be stored.</P>
 *
 *  <P>	The <strong>handler method</strong> to be invoked must have the
 *	following method signature:</P>
 *
 *  <P> <BLOCKQUOTE>
 *	    </CODE>
 *		public void beginDisplay(HandlerContext handlerCtx)
 *	    </CODE>
 *	</BLOCKQUOTE></P>
 *
 *  <P>	<code>void</code> above can return a value.  Depending on the type of
 *	event, return values may be handled differently.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class Handler implements java.io.Serializable {

    /**
     *	Constructor
     */
    public Handler(HandlerDefinition handlerDef) {
	setHandlerDefinition(handlerDef);
    }

    /**
     *
     */
    public HandlerDefinition getHandlerDefinition() {
	return _handlerDef;
    }

    /**
     *	<P> This method sets the HandlerDefinition used by this Handler.</P>
     */
    protected void setHandlerDefinition(HandlerDefinition handler) {
	_handlerDef = handler;
    }

    /**
     *
     */
    public void setInputValue(String name, Object value) {
	_inputs.put(name, value);
    }

    /**
     *	<P> This method returns a Map of NVPs representing the input to this
     *	    handler.</P>
     */
    protected Map getInputMap() {
	return _inputs;
    }

    /**
     *	<P> This method simply returns the named input value, null if not
     *	    found.  It will not attempt to resolve $...{...} expressions or
     *	    do modifications of any kind.  If you are looking for a method to
     *	    do these types of operations, try:</P>
     *
     *		getInputValue(FacesContext, String).
     *
     *	@param	name	The name used to identify the input value.
     */
    public Object getInputValue(String name) {
	return _inputs.get(name);
    }

    /**
     *	<P> This method returns the value for the named input.  Input values
     *	    are not stored in this HandlerContext itself, but in the Handler.
     *	    If you are trying to set input values for a handler, you must
     *	    create a new Handler object and set its input values.</P>
     *
     *	<P> This method attempts to resolve $...{...} expressions.  It also
     *	    will return the default value if the value is null.  If you don't
     *	    want these things to happen, look at
     *	    Handler.getInputValue(String).</P>
     *
     *	@param	name	    The input name
     *
     *	@return	The value of the input (null if not found)
     */
    public Object getInputValue(HandlerContext ctx, String name) {
	// Make sure the requested name is valid
	IODescriptor inDesc = getHandlerDefinition().getInputDef(name);
	if (inDesc == null) {
	    throw new RuntimeException("Attempted to get input value '"+name+
		"', however, this is not a declared input parameter in "+
		"handler definition '"+getHandlerDefinition().getId()+
		"'!  Check your handler and/or the XML (near LayoutElement '"+
		ctx.getLayoutElement().getId(ctx.getFacesContext(), null)+"')");
	}

	// Get the value, and parse it
	Object value = getInputValue(name);
	if (value == null) {
	    if (inDesc.isRequired()) {
		throw new RuntimeException("'"+name+
		    "' is required for handler '"+
		    getHandlerDefinition().getId()+"'!");
	    }
	    value = inDesc.getDefault();
	}

	// Resolve any expressions
	EventObject event = ctx.getEventObject();
	UIComponent component = null;
	if (event instanceof UIComponentHolder) {
	    component = ((UIComponentHolder)event).getUIComponent();
	}
	if ((value != null) && (value instanceof String)) {
	    value = Util.resolveValue(ctx.getFacesContext(),
		    ctx.getLayoutElement(), component, ""+value);
	}

	// Make sure the value is the correct type...
	value = TypeConverter.asType(inDesc.getType(), value);

	return value;
    }

    /**
     *	<P> This method retrieves an output value.  Output values are stored
     *	    in the location specified by the OutputType in the Handler.</P>
     *
     *	@param	context	    The HandlerContext
     *	@param	name	    The output name
     *
     *	@return	The value of the output (null if not set)
     */
    public Object getOutputValue(HandlerContext context, String name) {
	// Make sure the requested name is valid
	HandlerDefinition handlerDef = getHandlerDefinition();
	IODescriptor outIODesc = handlerDef.getOutputDef(name);
	if (outIODesc == null) {
	    throw new RuntimeException("Attempted to get output value '"+
		name+"' from handler '"+handlerDef.getId()+
		"', however, this is not a declared output parameter!  "+
		"Check your handler and/or the XML.");
	}

	// Get the OutputMapping that describes how to store this output
	OutputMapping outputDesc = getOutput(name);

	// Return the value
	return outputDesc.getOutputType().getValue(context, outIODesc, outputDesc.getOutputKey());
    }

    /**
     *	<P> This method stores an output value.  Output values are stored
     *	    as specified by the OutputType in the Handler.</P>
     *
     *	@param	context	    The HandlerContext
     *	@param	name	    The name the Handler uses for the output
     *	@param	value	    The value to set
     */
    public void setOutputValue(HandlerContext context, String name, Object value) {
	// Make sure the requested name is valid
	HandlerDefinition handlerDef = getHandlerDefinition();
	IODescriptor outIODesc = handlerDef.getOutputDef(name);
	if (outIODesc == null) {
	    throw new RuntimeException("Attempted to set output value '"+
		name+"' from handler '"+handlerDef.getId()+
		"', however, this is not a declared output parameter!  "+
		"Check your handler and/or the XML.");
	}

	// Get the OutputMapping that describes how to store this output
	OutputMapping outputMapping = getOutput(name);
	if (outputMapping == null) {
	    // They did not Map the output, do nothing...
	    return;
	}

	// Make sure the value is the correct type...
	value = TypeConverter.asType(outIODesc.getType(), value);

	// Set the value
	EventObject event = context.getEventObject();
	UIComponent component = null;
	if (event instanceof UIComponentHolder) {
	    component = ((UIComponentHolder)event).getUIComponent();
	}
	outputMapping.getOutputType().setValue(
	    context, outIODesc, ""+Util.resolveValue(
		context.getFacesContext(),
		context.getLayoutElement(),
		component,
		outputMapping.getOutputKey()), value);
    }

    /**
     *	<P> This method adds a new OutputMapping to this handler.  An
     *	    OutputMapping allows the handler to return a value and have it
     *	    "mapped" to the location of your choice.  The "outputType"
     *	    corresponds to a registered OutputType (see OutputTypeManager).</P>
     *
     *	@param	outputName  The Handler's name for the output value
     *	@param	targetKey   The 'key' the OutputType uses to store the output
     *	@param	targetType  The OutputType implementation map the output
     */
    public void setOutputMapping(String outputName, String targetKey, String targetType) {
	// Ensure the data is trim
	if (targetKey != null) {
	    targetKey = targetKey.trim();
	    if (targetKey.length() == 0) {
		targetKey = null;
	    }
	}
	targetType = targetType.trim();

	try {
	    _outputs.put(outputName, new OutputMapping(outputName, targetKey, targetType));
	} catch (IllegalArgumentException ex) {
	    throw new RuntimeException(
		"Unable to create OutputMapping with given information: "+
		    "outputName='"+outputName+
		    "', targetKey='"+targetKey+
		    "', targetType="+targetType+"'", ex);
	}
    }

    /**
     *
     */
    public OutputMapping getOutput(String name) {
	return (OutputMapping)_outputs.get(name);
    }

    /**
     *	<p> This method determines if the handler is static.</p>
     */
    public boolean isStatic() {
	return getHandlerDefinition().isStatic();
    }

    /**
     *
     */
    public Object invoke(HandlerContext handlerContext) throws InstantiationException, IllegalAccessException, InvocationTargetException {
	Object retVal = null;
	HandlerDefinition handlerDef = getHandlerDefinition();
	Method method = handlerDef.getHandlerMethod();

	// First execute all child handlers
	// A copy is provided of the HandlerContext to avoid the Handler being
	// changed before we execute this Handler.
	Object result = handlerContext.getLayoutElement().dispatchHandlers(
		new HandlerContextImpl(handlerContext),
		handlerDef.getChildHandlers());

	// Only attempt to do this if there is a handler method, there
	// might only be child handlers
	if (method != null) {
	    Object instance = null;
	    if (!isStatic()) {
		// Get the class that contains the method
		instance = method.getDeclaringClass().newInstance();
	    }

	    // Invoke the Method
	    retVal = method.invoke(instance, new Object[] {handlerContext});
	    if (retVal != null) {
		result = retVal;
	    }
	}

	// Return the result (null if no result)
	return result;
    }


    private HandlerDefinition 	_handlerDef	= null;
    private Map			_inputs		= new HashMap();
    private Map			_outputs	= new HashMap();
}
