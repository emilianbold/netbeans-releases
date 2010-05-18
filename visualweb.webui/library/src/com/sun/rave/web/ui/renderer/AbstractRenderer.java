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
package com.sun.rave.web.ui.renderer;

import com.sun.rave.web.ui.component.Page;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.model.Markup;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.faces.application.Application;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;


/**
 * <p>Abstract base class for concrete implementations of
 * <code>javax.faces.render.Renderer</code> for JavaServer Faces
 * component libraries.</p>
 */

public abstract class AbstractRenderer extends Renderer {
    
    // ------------------------------------------------------ Manifest Constants
    
    
    /**
     * <p>Base naem of the resource bundle we will use for localization.</p>
     */
    protected static final String BUNDLE =
            "com.sun.rave.web.ui.renderer.Bundle"; // NOI18N
    
    
    /**
     * <p>The list of attribute names in the HTML 4.01 Specification that
     * correspond to the entity type <em>%events;</em>.</p>
     */
    public static final String EVENTS_ATTRIBUTES[] =
    { "onClick", "onDblClick", "onChange", // NOI18N
      "onMouseDown", "onMouseUp", "onMouseOver", "onMouseMove", "onMouseOut", // NOI18N
              "onKeyPress", "onKeyDown", "onKeyUp", // NOI18N
    };
    
    
    /**
     * <p>The list of attribute names in the HTML 4.01 Specification that
     * correspond to the entity type <em>%i18n;</em>.</p>
     */
    public static final String I18N_ATTRIBUTES[] =
    { "dir", "lang", }; // NOI18N
    
    
    // -------------------------------------------------------- Static Variables
    
    
    
    // ---------------------------------------------------------- Public Methods
    
    
    /**
     * <p>Decode any new state of the specified <code>UIComponent</code>
     * from the request contained in the specified <code>FacesContext</code>,
     * and store that state on the <code>UIComponent</code>.</p>
     *
     * <p>The default implementation calls <code>setSubmittedValue()</code>
     * on components that implement EditableValueHolder (i.e. input fields)</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be decoded
     *
     * @exception NullPointerException if <code>context</code> or
     *  <code>component</code> is <code>null</code>
     */
    public void decode(FacesContext context, UIComponent component) {
        
        // Enforce NPE requirements in the Javadocs
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        
        // Save submitted value on EditableValueHolder components
        // unless they are disabled or read only
        if (component instanceof EditableValueHolder) {
            setSubmittedValue(context, component);
        }
        
    }
    
    
    /**
     * <p>Render the beginning of the specified <code>UIComponent</code>
     * to the output stream or writer associated with the response we are
     * creating.</p>
     *
     * <p>The default implementation calls <code>renderStart()</code> and
     * <code>renderAttributes()</code>.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be decoded
     *
     * @exception NullPointerException if <code>context</code> or
     *  <code>component</code> is <code>null</code>
     *
     * @exception IOException if an input/output error occurs
     */
    public void encodeBegin(FacesContext context, UIComponent component)
    throws IOException {
        
        // Enforce NPE requirements in the Javadocs
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        
/*
        if (LogUtil.finestEnabled()) {
            LogUtil.finest("encodeBegin(id=" + component.getId() +
                      ", family=" + component.getFamily() +
                      ", rendererType=" + component.getRendererType() + ")");
        }
 */
        
        // Render the element and attributes for this component
        if (component.isRendered()) {
            ResponseWriter writer = context.getResponseWriter();
            renderStart(context, component, writer);
            renderAttributes(context, component, writer);
        }
        
    }
    
    
    /**
     * <p>Render the children of the specified <code>UIComponent</code>
     * to the output stream or writer associated with the response we are
     * creating.</p>
     *
     * <p>The default implementation iterates through the children of
     * this component and renders them.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be decoded
     *
     * @exception NullPointerException if <code>context</code> or
     *  <code>component</code> is <code>null</code>
     *
     * @exception IOException if an input/output error occurs
     */
    // We shouldn't bother with a default implementation - this is exactly
    // what happens when you rendersChildren = false. Why duplicate the
    // code here?
    
    public void encodeChildren(FacesContext context, UIComponent component)
    throws IOException {
        
        // Enforce NPE requirements in the Javadocs
        if (context == null || component == null) {
            throw new NullPointerException();
        }
        
/*
        if (LogUtil.finestEnabled()) {
            LogUtil.finest("encodeChildren(id=" + component.getId() +
                      ", family=" + component.getFamily() +
                      ", rendererType=" + component.getRendererType() + ")");
        }
 */
        
        if (component.isRendered()) {
            Iterator kids = component.getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                RenderingUtilities.renderComponent(kid, context);
            }
        }
        
    }
    
    
    /**
     * <p>Render the ending of the specified <code>UIComponent</code>
     * to the output stream or writer associated with the response we are
     * creating.</p>
     *
     * <p>The default implementation calls <code>renderEnd()</code>.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be decoded
     *
     * @exception NullPointerException if <code>context</code> or
     *  <code>component</code> is <code>null</code>
     *
     * @exception IOException if an input/output error occurs
     */
    public void encodeEnd(FacesContext context, UIComponent component)
    throws IOException {
        
        // Enforce NPE requirements in the Javadocs
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        
/*
        if (LogUtil.finestEnabled()) {
            LogUtil.finest("encodeEnd(id=" + component.getId() +
                      ", family=" + component.getFamily() +
                      ", rendererType=" + component.getRendererType() + ")");
        }
 */
        
        // Render the element closing for this component
        if (component.isRendered()) {
            ResponseWriter writer = context.getResponseWriter();
            renderEnd(context, component, writer);
        }
        
    }
    
    
    // --------------------------------------------------------- Package Methods
    
    
    // ------------------------------------------------------- Protected Methods
    
    
    
    
    /**
     * <p>Render any boolean attributes on the specified list that have
     * <code>true</code> values on the corresponding attribute of the
     * specified <code>UIComponent</code>.  Attribute names are
     * converted to lower case in the rendered output.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>EditableValueHolder</code> component whose
     *  submitted value is to be stored
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     * @param names List of attribute names to be passed through
     *
     * @exception IOException if an input/output error occurs
     */
    protected void addBooleanAttributes(FacesContext context,
            UIComponent component,
            ResponseWriter writer,
            String names[]) throws IOException {
        
        if (names == null) {
            return;
        }
        Map attributes = component.getAttributes();
        boolean flag;
        Object value;
        for (int i = 0; i < names.length; i++) {
            value = attributes.get(names[i]);
            if (value != null) {
                if (value instanceof String) {
                    flag = Boolean.valueOf((String) value).booleanValue();
                } else {
                    flag = Boolean.valueOf(value.toString()).booleanValue();
                }
                if (flag) {
                    writer.writeAttribute(names[i].toLowerCase(),
                            names[i].toLowerCase(), names[i]);
                    flag = false;
                }
            }
        }
        
    }
    
    
    // Core attributes that are simple pass throughs
    private static final String coreAttributes[] =
    { "style", "title" };
    
    
    
    /**
     * <p>Render the "core" set of attributes for this <code>UIComponent</code>.
     * The default implementation conditionally generates the following
     * attributes with values as specified.</p>
     * <ul>
     * <li><strong>id</strong> - If this component has a non-<code>null</code>
     *     <code>id</code> property, and the identifier does not start with
     *     <code>UIViewRoot.UNIQUE_ID_PREFIX</code>, render the
     *     <code>clientId</code>.</li>
     * <li><strong>class</strong> - If this component has a
     *     non-<code>null</code> <code>styleClass</code> attribute, render its
     *     value, combined with the syles parameter (if any).</li>
     * <li><strong>style</strong> - If this component has a
     *     non-<code>null</code> <code>style</code> attribute, render its
     *     value.</li>
     * <li><strong>title</strong> - If this component has a
     *     non-<code>null</code> <code>title</code> attribute, render its
     *     value.</li>
     * </ul>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>EditableValueHolder</code> component whose
     *  submitted value is to be stored
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     * @param styles Space-separated list of CSS style classes to add
     *  to the <code>class</code> attribute, or <code>null</code> for none
     *
     * @exception IOException if an input/output error occurs
     */
    protected void addCoreAttributes(FacesContext context,
            UIComponent component,
            ResponseWriter writer,
            String styles) throws IOException {
        
        String id = component.getId();
  
        writer.writeAttribute("id", component.getClientId(context), "id");
        
        RenderingUtilities.renderStyleClass(context, writer, component, styles);
        addStringAttributes(context, component, writer, coreAttributes);
        
    }
    
    

    
    /**
     * <p>Render any Integer attributes on the specified list that do not have
     * Integer.MIN_VALUE values on the corresponding attribute of the
     * specified <code>UIComponent</code>.  Attribute names are converted to
     * lower case in the rendered output.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>EditableValueHolder</code> component whose
     *  submitted value is to be stored
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     * @param names List of attribute names to be passed through
     *
     * @exception IOException if an input/output error occurs
     */
    protected void addIntegerAttributes(FacesContext context,
            UIComponent component,
            ResponseWriter writer,
            String names[]) throws IOException {
        
        if (names == null) {
            return;
        }
        Map attributes = component.getAttributes();
        boolean flag;
        Object value;
        for (int i = 0; i < names.length; i++) {
            value = attributes.get(names[i]);
            if ((value != null) && (value instanceof Integer)) {
                Integer ivalue = (Integer) value;
                if (!(ivalue.intValue() == Integer.MIN_VALUE)) {
                    writer.writeAttribute(names[i].toLowerCase(), ivalue, names[i]);
                }
            }
        }
        
    }
    
    
    /**
     * <p>Add any attributes on the specified list directly to the
     * specified <code>ResponseWriter</code> for which the specified
     * <code>UIComponent</code> has a non-<code>null</code> String value.
     * This method may be used to "pass through" commonly used attribute
     * name/value pairs with a minimum of code.  Attribute names are
     * converted to lower case in the rendered output.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>EditableValueHolder</code> component whose
     *  submitted value is to be stored
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     * @param names List of attribute names to be passed through
     *
     * @exception IOException if an input/output error occurs
     */
    protected static void addStringAttributes(FacesContext context,
            UIComponent component,
            ResponseWriter writer,
            String names[]) throws IOException {
        
        if (names == null) {
            return;
        }
        Map attributes = component.getAttributes();
        Object value;
        for (int i = 0; i < names.length; i++) {
            value = attributes.get(names[i]);
            if (value != null) {
                if (value instanceof String) {
                    writer.writeAttribute(names[i].toLowerCase(),
                            (String) value, names[i]);
                } else {
                    writer.writeAttribute(names[i].toLowerCase(),
                            value.toString(), names[i]);
                }
            }
        }
        
    }
    
    
    /**
     * <p>Return the <code>Application</code> instance for this
     * web application.</p>
     */
    protected Application getApplication() {
        
        return getFacesContext().getApplication();
        
    }
    
    
    /**
     * <p>Return the value to be stored, as an Object that has been
     * converted from the String representation (if necessary), or
     * <code>null</code> if the String representation is null.</p>
     *
     * @param context FacesContext for the current request
     * @param component Component whose value is being processed
     *  (must be a component that implements ValueHolder
     * @param value String representation of the value
     */
    protected Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        
        if (value == null) {
            return null;
        }
        Converter converter = ((ValueHolder) component).getConverter();
        if (converter == null) {
            ValueBinding vb = component.getValueBinding("value");
            if (vb != null) {
                Class clazz = vb.getType(context);
                if (clazz != null) {
                    converter =
                            getApplication().createConverter(clazz);
                }
            }
        }
        if (converter != null) {
            return converter.getAsObject(context, component, value);
        } else {
            return value;
        }
        
    }
    
    
    /**
     * <p>Return the value to be rendered, as a String (converted
     * if necessary), or <code>null</code> if the value is null.</p>
     *
     * @param context FacesContext for the current request
     * @param component Component whose value is to be retrieved (must be
     *  a component that implements ValueHolder)
     */
    protected String getAsString(FacesContext context, UIComponent component) {
        if (component instanceof EditableValueHolder) {
            Object submittedValue = ((EditableValueHolder) component).getSubmittedValue();
            if (submittedValue != null) {
                return (String) submittedValue;
            }
        }
        Object value = ((ValueHolder) component).getValue();
        if (value == null) {
            return null;
        }
        Converter converter = ((ValueHolder) component).getConverter();
        if (converter == null) {
            if (value instanceof String) {
                return (String) value;
            }
            converter = getApplication().createConverter(value.getClass());
        }
        if (converter != null) {
            return converter.getAsString(context, component, value);
        } else {
            return value.toString();
        }
    }
    
    
    /**
     * <p>Return the <code>ExternalContext</code> instance for the current
     * request.</p>
     */
    protected ExternalContext getExternalContext() {
        
        return (FacesContext.getCurrentInstance().getExternalContext());
        
    }
    
    
    /**
     * <p>Return the <code>FacesContext</code> instance for the current
     * request.</p>
     */
    protected FacesContext getFacesContext() {
        
        return (FacesContext.getCurrentInstance());
        
    }
    
    
    /**
     * <p>Retrieve the submitted value from the request parameters for
     * this request.  The default implementation retrieves the parameter
     * value that corresponds to the client identifier of this component.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> whose
     *  submitted value is to be retrieved
     */
    protected Object getSubmittedValue(FacesContext context, UIComponent component) {
        
        String clientId = component.getClientId(context);
        Map parameters = context.getExternalContext().getRequestParameterMap();
        return parameters.get(clientId);
        
    }
    
    /**
     * <p>Return <code>true</code> if the specified component is disabled.</p>
     *
     * @param component <code>UIComponent</code> to be checked
     */
    protected boolean isDisabled(UIComponent component) {
        
        Object disabled = component.getAttributes().get("disabled");
        if (disabled == null) {
            return (false);
        }
        if (disabled instanceof String) {
            return (Boolean.valueOf((String) disabled).booleanValue());
        } else {
            return (disabled.equals(Boolean.TRUE));
        }
        
    }
    
    
    /**
     * <p>Return <code>true</code> if we are we running in a portlet
     * environment, as opposed to a servlet based web application.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    protected boolean isPortlet(FacesContext context) {
        
        return false; // TODO - implement a dynamic check
        
    }
    
    
    /**
     * <p>Return <code>true</code> if the specified component is read only.</p>
     *
     * @param component <code>UIComponent</code> to be checked
     */
    protected boolean isReadOnly(UIComponent component) {
        
        Object readonly = component.getAttributes().get("readonly");
        if (readonly == null) {
            return (false);
        }
        if (readonly instanceof String) {
            return (Boolean.valueOf((String) readonly).booleanValue());
        } else {
            return (readonly.equals(Boolean.TRUE));
        }
        
    }
    
    
    /**
     * <p>Render the element attributes for the generated markup related to this
     * component.  Simple renderers that create a single markup element
     * for this component should override this method and include calls to
     * to <code>writeAttribute()</code> and <code>writeURIAttribute</code>
     * on the specified <code>ResponseWriter</code>.</p>
     *
     * <p>The default implementation does nothing.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>EditableValueHolder</code> component whose
     *  submitted value is to be stored
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        
    }
    
    
    /**
     * <p>Render the element end for the generated markup related to this
     * component.  Simple renderers that create a single markup element
     * for this component should override this method and include a call
     * to <code>endElement()</code> on the specified
     * <code>ResponseWriter</code>.</p>
     *
     * <p>The default implementation does nothing.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>EditableValueHolder</code> component whose
     *  submitted value is to be stored
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        
    }
    
     
    
    /**
     * <p>Render the specified markup to the current response.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> associated with this markup
     * @param writer <code>ResponseWriter</code> to which the markup
     *  should be rendered
     * @param markup {@link Markup} to be rendered
     */
    protected void renderMarkup(FacesContext context, UIComponent component,
            ResponseWriter writer, Markup markup)
            throws IOException {
        
        writer.write(markup.getMarkup());
        
    }
    
    
    /**
     * <p>Render the element start for the generated markup related to this
     * component.  Simple renderers that create a single markup element
     * for this component should override this method and include a call
     * to <code>startElement()</code> on the specified
     * <code>ResponseWriter</code>.</p>
     *
     * <p>The default implementation does nothing.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>EditableValueHolder</code> component whose
     *  submitted value is to be stored
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        
    }
    
    
    /**
     * <p>If a submitted value was included on this request, store it in the
     * component as appropriate.</p>
     *
     * <p>The default implementation determines whether this component
     * implements <code>EditableValueHolder</code>.  If so, it checks for a
     * request parameter with the same name as the <code>clientId</code>
     * of this <code>UIComponent</code>.  If there is such a parameter, its
     * value is passed (as a String) to the <code>setSubmittedValue()</code>
     * method on the <code>EditableValueHolder</code> component.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>EditableValueHolder</code> component whose
     *  submitted value is to be stored
     */
    protected void setSubmittedValue
            (FacesContext context, UIComponent component) {
        
        if (!(component instanceof EditableValueHolder)) {
            return;
        }
        component.getAttributes().put("submittedValue", // NOI18N
                getSubmittedValue(context, component));
        
    }
      
    
}
