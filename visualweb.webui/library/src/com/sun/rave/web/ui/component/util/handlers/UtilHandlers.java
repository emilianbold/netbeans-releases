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
package com.sun.rave.web.ui.component.util.handlers;

import com.sun.rave.web.ui.component.util.event.HandlerContext;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 *  <p>	This class contains {@link com.sun.rave.web.ui.component.util.event.Handler}
 *	methods that perform common utility-type functions.</p>
 *
 *  @author  Ken Paulsen (ken.paulsen@sun.com)
 */
public class UtilHandlers {

    /**
     *	<P> Default Constructor.</P>
     */
    public UtilHandlers() {
    }

    /**
     *	<p> This handler writes using <CODE>System.out.println</CODE>.  It
     *	    requires that <code>value</code> be supplied as a String input
     *	    parameter.</p>
     *
     *	@param	context	The HandlerContext.
     */
    public void println(HandlerContext context) {
	String value = (String)context.getInputValue("value");
	System.out.println(value);
    }

    /**
     *	<p> This handler decrements a number by 1.  This handler requires
     *	    "number" to be supplied as an Integer input value.  It sets an
     *	    output value "value" to number-1.</p>
     *
     *	@param	context	The HandlerContext.
     */
    public void dec(HandlerContext context) {
	Integer value = (Integer)context.getInputValue("number");
	context.setOutputValue("value", new Integer(value.intValue()-1));
    }

    /**
     *	<p> This handler increments a number by 1.  This handler requires
     *	    "number" to be supplied as an Integer input value.  It sets an
     *	    output value "value" to number+1.</p>
     *
     *	@param	context	The HandlerContext.
     */
    public void inc(HandlerContext context) {
	Integer value = (Integer)context.getInputValue("number");
	context.setOutputValue("value", new Integer(value.intValue()+1));
    }

    /**
     *	<p> This handler sets a request attribute.  It requires "key" and
     *	    "value" input values to be passed in.</p>
     *
     *	@param	context	The HandlerContext.
     */
    public void setAttribute(HandlerContext context) {
	String key = (String)context.getInputValue("key");
	Object value = context.getInputValue("value");
	context.getFacesContext().getExternalContext().
	    getRequestMap().put(key, value);
    }

    /**
     *	<p> This method returns an <code>Iterator</code> for the given
     *	    <code>List</code>.  The <code>List</code> input value key is:
     *	    "list".  The output value key for the <code>Iterator</code> is:
     *	    "iterator".</p>
     *
     *	@param	context	The HandlerContext.
     */
    public void getIterator(HandlerContext context) {
	List list = (List)context.getInputValue("list");
	context.setOutputValue("iterator", list.iterator());
    }

    /**
     *	<p> This method returns a <code>Boolean</code> value representing
     *	    whether another value exists for the given <code>Iterator</code>.
     *	    The <code>Iterator</code> input value key is: "iterator".  The
     *	    output value key is "hasNext".</p>
     *
     *	@param	context	The HandlerContext.
     */
    public void iteratorHasNext(HandlerContext context) {
	Iterator it = (Iterator)context.getInputValue("iterator");
	context.setOutputValue("hasNext", Boolean.valueOf(it.hasNext()));
    }

    /**
     *	<p> This method returns the next object in the <code>List</code> that
     *	    the given <code>Iterator</code> is iterating over.  The
     *	    <code>Iterator</code> input value key is: "iterator".  The
     *	    output value key is "next".</p>
     *
     *	@param	context	The HandlerContext.
     */
    public void iteratorNext(HandlerContext context) {
	Iterator it = (Iterator)context.getInputValue("iterator");
	context.setOutputValue("next", it.next());
    }

    /**
     *	<p> This method creates a List.  Optionally you may supply "size" to
     *	    create a List of blank "" values of the specified size.  The
     *	    output value from this handler is "result".</p>
     *
     *	@param	context	The HandlerContext
     */
    public void createList(HandlerContext context) {
	int size = ((Integer)context.getInputValue("size")).intValue();
	List list = new ArrayList(size);
	for (int count=0; count<size; count++) {
	    list.add("");
	}
	context.setOutputValue("result", list);
    }

    /**
     *	<p> This method returns true.  It does not take any input or provide
     *	    any output values.</p>
     *
     *	@param context	The {@link HandlerContext}
     */
    public boolean returnTrue(HandlerContext context) {
	return true;
    }

    /**
     *	<p> This method returns false.  It does not take any input or provide
     *	    any output values.</p>
     *
     *	@param context	The {@link HandlerContext}
     */
    public boolean returnFalse(HandlerContext context) {
	return false;
    }

    /**
     *	<p> This method enables you to retrieve the clientId for the given
     *	    <code>UIComponent</code>.</p>
     *
     *	@param context	The {@link HandlerContext}
     */
    public void getClientId(HandlerContext context) {
	UIComponent comp = (UIComponent) context.getInputValue("component");
	context.setOutputValue("clientId",
		comp.getClientId(context.getFacesContext()));
    }

    /**
     *	<p> This method enables you to retrieve the id or clientId for the given
     *	    <code>Object</code> which is expected to be a
     *	    <code>UIComponent</code> or a <code>String</code> that already
     *	    represents the clientId.</p>
     *
     *	@param context	The {@link HandlerContext}
     */
    public void getId(HandlerContext context) {
	Object obj = context.getInputValue("object");
	if (obj == null) {
	    return;
	}

	String clientId = null;
	String id = null;
	if (obj instanceof UIComponent) {
	    clientId = ((UIComponent) obj).getClientId(context.getFacesContext());
	    id = ((UIComponent) obj).getId();
	} else {
	    clientId = obj.toString();
	    id = clientId.substring(clientId.lastIndexOf(':')+1);
	}
	context.setOutputValue("id", id);
	context.setOutputValue("clientId", clientId);
    }

    /**
     * Render skip hyperlink -- see bugtarq #6329543.
     *
     * @param context The {@link HandlerContext}
     */
    public static void startSkipHyperlink(HandlerContext context)
            throws IOException {
        UIComponent component = (UIComponent) context.getInputValue("component"); //NOI18N
        String baseId = (String) context.getInputValue("baseID"); //NOI18N

        FacesContext fc = context.getFacesContext();
        RenderingUtilities.renderSkipLink("skipHyperlink", null, null, //NOI18N
            ThemeUtilities.getTheme(fc).getMessage("tree.skipTagAltText"), //NOI18N 
            null, component, fc);
    }

    /**
     * Render skip anchor -- see bugtarq #6329543.
     *
     * @param context The {@link HandlerContext}
     */
    public static void endSkipHyperlink(HandlerContext context) 
            throws IOException {
        UIComponent component = (UIComponent) context.getInputValue("component"); //NOI18N
        String baseId = (String) context.getInputValue("baseID"); //NOI18N

        RenderingUtilities.renderAnchor("skipHyperlink", component, //NOI18N
            context.getFacesContext());
    }
}
