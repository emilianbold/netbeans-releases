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


package com.sun.rave.web.ui.appbase.renderer;

import java.beans.Beans;
import java.io.IOException;
import java.util.Iterator;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import javax.faces.render.Renderer;

/**
 * <p>Replacement renderer for the <code>&lt;h:commandLink&gt;</code>
 * component, which is not tied to the JSF standard form renderer (and
 * will therefore work inside a Braveheart form component).</p>
 */

public class CommandLinkRenderer extends Renderer {


    // -------------------------------------------------------- Static Variables


    /**
     * <p>Token for private names.</p>
     */
    private static final String TOKEN =
      "com_sun_rave_web_ui_appbase_renderer_CommandLinkRendererer";


     /**
      * <p>Pass through attribute names.</p>
      */
    private static String passThrough[] =
    { "accesskey", "charset", "dir", "hreflang", "lang", "onblur",
      /* "onclick", */ "ondblclick", "onfocus", "onkeydown",
      "onkeypress", "onkeyup", "onmousedown", "onmousemove",
      "onmouseout", "onmouseover", "onmouseup", "rel", "rev",
      "style", "tabindex", "target", "title", "type" };


    // -------------------------------------------------------- Renderer Methods


    /**
     * <p>Perform setup processing that will be required for decoding
     * the incoming request.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be processed
     *
     * @exception NullPointerException if <code>contxt</code>
     *  or <code>component</code> is <code>null</code>.
     */
    public void decode(FacesContext context, UIComponent component) {

        // Enforce spec NPE behaviors
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        // Skip this component if it is not relevant
        if (!component.isRendered() || isDisabled(component) ||
            isReadOnly(component)) {
            return;
        }

        // Set up the variables we will need
        UIForm form = null;
        UIComponent parent = component.getParent();
        while (parent != null) {
            if (parent instanceof UIForm) {
                form = (UIForm) parent;
                break;
            }
            parent = parent.getParent();
        }
        if (form == null) {
            return;
        }

        // Was this the component that submitted the form?
        String value = (String)
          context.getExternalContext().getRequestParameterMap().get(TOKEN);
        if ((value == null) || !value.equals(component.getClientId(context))) {
            return;
        }

        // Queue an ActionEvent from this component
        component.queueEvent(new ActionEvent(component));

    }


    /**
     * <p>Render the beginning of a hyperlink to submit this form.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be processed
     *
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>contxt</code>
     *  or <code>component</code> is <code>null</code>.
     */
    public void encodeBegin(FacesContext context, UIComponent component)
      throws IOException {

        // Enforce spec NPE behaviors
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        // Skip this component if it is not relevant
        if (!component.isRendered() || isDisabled(component) ||
            isReadOnly(component)) {
            return;
        }

        // At designtime we don't require a form. For example,
        // without this you cannot render CommandLinks in page fragments
        // since these typically don't include forms
        String formClientId = "";
        if (!Beans.isDesignTime()) {
            UIForm form = null;
            UIComponent parent = component.getParent();
            while (parent != null) {
                if (parent instanceof UIForm) {
                    form = (UIForm) parent;
                    break;
                }
                parent = parent.getParent();
            }
            if (form == null) {
                return;
            }
            formClientId = form.getClientId(context);
        }

        // If this is the first nested command link inside this form,
        // render a hidden variable to identify which link submitted.
        String key = formClientId + NamingContainer.SEPARATOR_CHAR + TOKEN;
        ResponseWriter writer = context.getResponseWriter();
        // FIXME - single outer span?
        if (context.getExternalContext().getRequestMap().get(key) == null) {
            writer.startElement("input", component); // NOI18N
            writer.writeAttribute("name", TOKEN, null); // NOI18N
            writer.writeAttribute("type", "hidden", null); // NOI18N
            writer.writeAttribute("value", "", null); // NOI18N
            writer.endElement("input"); // NOI18N
            context.getExternalContext().getRequestMap().put(key, Boolean.TRUE);
        }

        // Render the beginning of this hyperlink
        writer.startElement("a", component);
        if (component.getId() != null) {
            writer.writeAttribute("id", component.getClientId(context), "id"); // NOI18N
        }
        writer.writeAttribute("href", "#", null); // NOI18N
        String styleClass = (String)
          component.getAttributes().get("styleClass"); // NOI18N
        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, "styleClass"); // NOI18N
        }
        for (int i = 0; i < passThrough.length; i++) {
            Object value = component.getAttributes().get(passThrough[i]);
            if (value != null) {
                writer.writeAttribute(passThrough[i], value.toString(), passThrough[i]);
            }
        }

        // Render the JavaScript content of the "onclick" element
        StringBuffer sb = new StringBuffer();
        sb.append("document.forms['"); // NOI18N
        sb.append(formClientId);
        sb.append("']['"); // NOI18N
        sb.append(TOKEN);
        sb.append("'].value='"); // NOI18N
        sb.append(component.getClientId(context));
        sb.append("';"); // NOI18N
        Iterator kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            if (!(kid instanceof UIParameter)) {
                continue;
            }
            sb.append("document.forms['"); // NOI18N
            sb.append(formClientId);
            sb.append("']['"); // NOI18N
            sb.append((String) kid.getAttributes().get("name")); // NOI18N
            sb.append("'].value='"); // NOI18N
            sb.append((String) kid.getAttributes().get("value")); // NOI18N
            sb.append("';"); // NOI18N
        }
        sb.append("document.forms['"); // NOI18N
        sb.append(formClientId);
        sb.append("'].submit(); return false;"); // NOI18N
        writer.writeAttribute("onclick", sb.toString(), null); // NOI18N

        // Render the value (if any) as part of the hyperlink text
        Object value = component.getAttributes().get("value"); // NOI18N
        if (value != null) {
            if (value instanceof String) {
                writer.write((String) value);
            } else {
                writer.write(value.toString());
            }
        }

    }


    /**
     * <p>Render the ending of a hyperlink to submit this form.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be processed
     *
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>contxt</code>
     *  or <code>component</code> is <code>null</code>.
     */
    public void encodeEnd(FacesContext context, UIComponent component)
      throws IOException {

        // Enforce spec NPE behaviors
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        // Skip this component if it is not relevant
        if (!component.isRendered() || isDisabled(component) ||
            isReadOnly(component)) {
            return;
        }

        // Render the ending of this hyperlink
        ResponseWriter writer = context.getResponseWriter();
        writer.endElement("a"); // NOI18N

    }


    // -------------------------------------------------- Private Methods


    /**
     * <p>Return <code>true</code> if the specified component is disabled.</p>
     *
     * @param component <code>UIComponent</code> to be tested
     */
    private boolean isDisabled(UIComponent component) {

        Object value = component.getAttributes().get("disabled"); // NOI18N
        if (value != null) {
            if (value instanceof String) {
                return (Boolean.valueOf((String) value).booleanValue());
            } else {
                return (value.equals(Boolean.TRUE));
            }
        } else {
            return false;
        }

    }


    /**
     * <p>Return <code>true</code> if the specified component is read only.</p>
     *
     * @param component <code>UIComponent</code> to be tested
     */
    private boolean isReadOnly(UIComponent component) {

        Object value = component.getAttributes().get("readonly"); // NOI18N
        if (value != null) {
            if (value instanceof String) {
                return (Boolean.valueOf((String) value).booleanValue());
            } else {
                return (value.equals(Boolean.TRUE));
            }
        } else {
            return false;
        }

    }


}
