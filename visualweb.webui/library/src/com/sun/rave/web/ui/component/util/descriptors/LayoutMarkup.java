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

import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.component.util.event.Handler;
import com.sun.rave.web.ui.component.util.event.HandlerContext;
import com.sun.rave.web.ui.component.util.event.HandlerDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;


/**
 *  <p>	This class defines a LayoutMarkup.  A LayoutMarkup provides a means to
 *	start a markup tag and associate the current UIComponent with it for
 *	tool support.  It also has the benefit of properly closing the markup
 *	tag for you.</p>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutMarkup extends LayoutElementBase implements LayoutElement {

    /**
     *	<P> Constructor.</P>
     */
    public LayoutMarkup(LayoutElement parent, String tag, String type) {
	super(parent, tag);
	_tag = tag;
	_type = type;

	// Add "afterEncode" handler to close the tag (if there is a close tag)
	if (!type.equals(TYPE_OPEN)) {
	    List handlers = new ArrayList();
	    handlers.add(afterEncodeHandler);
	    setHandlers(AFTER_ENCODE, handlers);
	}
    }

    /**
     *
     */
    public String getTag() {
	return _tag;
    }

    /**
     *
     */
    public String getType() {
	return _type;
    }

    /**
     *	<P> This method displays the text described by this component.  If the
     *	    text includes an EL expression, it will be evaluated.  It returns
     *	    true to render children.</P>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	component   The <code>UIComponent</code>
     *
     *	@return	false
     */
    protected boolean encodeThis(FacesContext context, UIComponent component) throws IOException {
	if (getType().equals(TYPE_CLOSE)) {
	    return true;
	}

	// Get the ResponseWriter
	ResponseWriter writer = context.getResponseWriter();

	// Render...
	Object value = resolveValue(context, component, getTag());
	if (value != null) {
	    writer.startElement(value.toString(), component);
	}

	// Always render children
	return true;
    }

    /**
     *	<p> This handler takes care of closing the tag.</p>
     * 
     *	@param	context	The HandlerContext.
     */
    public static void afterEncodeHandler(HandlerContext context) throws IOException {
	ResponseWriter writer = context.getFacesContext().getResponseWriter();
	LayoutMarkup markup = (LayoutMarkup) context.getLayoutElement();
	Object value = Util.resolveValue(context.getFacesContext(), markup,
		(UIComponent) context.getEventObject().getSource(),
		markup.getTag());
	if (value != null) {
	    writer.endElement(value.toString());
	}
    }

    /**
     * 
     */
    public static final HandlerDefinition afterEncodeHandlerDef =
	new HandlerDefinition("_markupAfterEncode");

    /**
     * 
     */
    public static final Handler afterEncodeHandler =
	new Handler(afterEncodeHandlerDef);

    static {
	afterEncodeHandlerDef.setHandlerMethod(
		LayoutMarkup.class.getName(), "afterEncodeHandler");
    }

    /**
     *	<p> This markup type writes out both the opening and closing tags.</p>
     */
    public static final String TYPE_BOTH   =	"both";

    /**
     *	<p> This markup type writes out the closing tag.</p>
     */
    public static final String TYPE_CLOSE   =	"close";

    /**
     *	<p> This markup type writes out the opening tag.</p>
     */
    public static final String TYPE_OPEN   =	"open";

    private String _tag   = null;
    private String _type   = null;
}
