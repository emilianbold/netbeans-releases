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

import com.sun.rave.web.ui.component.Form;
import com.sun.rave.web.ui.model.Markup;
import com.sun.rave.web.ui.model.ScriptMarkup;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;


/**
 * <p>Renderer for a {@link Form} component.</p>
 */

public class FormRenderer extends AbstractRenderer {


    // ======================================================== Static Variables


    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] =
    { "enctype", "accessKey", "onReset", "onSubmit", "target" }; //NOI18N
    
    private static final String SUBMISSION_COMPONENT_HIDDEN_FIELD = "_submissionComponentId";  //NOI18N
    private static final String FORM_HIDDEN_FIELD = "_hidden";  //NOI18N

    
    // -------------------------------------------------------- Renderer Methods


    /**
     * <p>Record a flag indicating whether this was the form (of the several
     * forms on the current page) that was submitted. Also, if the submission 
     * component id is known, then set the submitted virtual form if 
     * appropriate.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be decoded
     *
     * @exception NullPointerException if <code>context</code> or
     *  <code>component</code> is <code>null</code>
     */
    public void decode(FacesContext context, UIComponent component) {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        Form form = (Form) component;
        Map map = context.getExternalContext().getRequestParameterMap();
        boolean b = map.containsKey(form.getClientId(context) + FORM_HIDDEN_FIELD);
        form.setSubmitted(b);
        if (LogUtil.fineEnabled()) {
            LogUtil.fine("Form(id=" + form.getId() + ",submitted=" +
                      form.isSubmitted() + ")");
        }
        
        String hiddenFieldClientId = SUBMISSION_COMPONENT_HIDDEN_FIELD;
        String submissionComponentId = (String)map.get(hiddenFieldClientId);
        if (submissionComponentId != null) {
           Form.VirtualFormDescriptor vfd = form.getVirtualFormComponentSubmits(submissionComponentId);
           if (vfd != null) {
               form.setSubmittedVirtualForm(vfd);
           }
        }
        
    }


    /**
     * <p>Render the appropriate element start for the outermost
     * element.</p>
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

        // Start the appropriate element
        Form form = (Form) component;
        if (LogUtil.fineEnabled()) {
            LogUtil.fine("Form(id=" + form.getId() + ")"); //NOI18N
        }
        writer.startElement("form", form); //NOI18N

        //reapply any submitted values erased by the virtual forms mechanism
        form.restoreNonParticipatingSubmittedValues();
    }


    /**
     * <p>Render the appropriate element attributes.</p>
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

        Form form = (Form) component;

        // Render the core attributes for the "form" element
        addCoreAttributes(context, form, writer, "form"); //NOI18N
        writer.writeAttribute("method", "post", null); //NOI18N
        writer.writeAttribute("action", action(context), null); //NOI18N
        addStringAttributes(context, form, writer, EVENTS_ATTRIBUTES);
        addStringAttributes(context, form, writer, stringAttributes);
        
        if (!form.isAutoComplete()) { 
            //only render it if it's false
            writer.writeAttribute("autocomplete", "off", null); // NOI18N
        }
        // Render a newline for pretty printing
        writer.write("\n"); //NOI18N

    }


    /**
     * <p>Render the appropriate element end.</p>
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

        Form form = (Form) component;
        List list = null;
        int n = 0;

        writer.write("\n"); //NOI18N
        // Render the hidden field noting this form as being submitted
        RenderingUtilities.renderHiddenField(component, writer,
                                             form.getClientId(context) + FORM_HIDDEN_FIELD, 
                                             form.getClientId(context) + FORM_HIDDEN_FIELD);
        
        writer.write("\n"); //NOI18N
        context.getApplication().getViewHandler().writeState(context);
        writer.write("\n"); //NOI18N
        // Render the end of the form element
        writer.endElement("form"); //NOI18N
         writer.write("\n"); //NOI18N

        if (LogUtil.finestEnabled()) {
            LogUtil.finest("  Rendering completed"); //NOI18N
        }
    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Return the URI to which this form should be submitted.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private String action(FacesContext context) {

        String viewId = context.getViewRoot().getViewId();
        String url =
            context.getApplication().getViewHandler().
            getActionURL(context, viewId);
        return context.getExternalContext().encodeActionURL(url);

    }


    /**
     * <p>Return the name of a JavaScript function that will be called
     * by the specified event handler.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param form {@link Form} being rendered
     * @param handler Name of the event handler that will call
     */
    private String function(FacesContext context, Form form, String handler) {

        String clientId = form.getClientId(context);
        return handler + "_" + clientId.replace(':', '_');  //NOI18N

    }


    /**
     * <p>Create and return the markup for the specified event handler.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param form {@link Form} being rendered
     * @param name Name of the event handler to be rendered
     * @param list List of code elements to be included
     */
    private Markup handler(FacesContext context, Form form,
                           String name, List list) {

        String code = null;
        Markup markup = new ScriptMarkup();
        markup.writeRaw("function " + //NOI18N
                        function(context, form, name) + //NOI18N
                        "(form) {\n", null); //NOI18N
        code = (String) form.getAttributes().get(name);
        if (code != null) {
            markup.writeRaw("    ", null); //NOI18N
            markup.writeRaw(code, null);
            if (!code.endsWith(";")) { //NOI18N
                markup.writeRaw(";", null); //NOI18N
            }
            markup.writeRaw("\n", null); //NOI18N
        }
        for (int i = 0; i < list.size(); i++) {
            code = ((String) list.get(i)).trim();
            markup.writeRaw("    ", null); //NOI18N
            markup.writeRaw(code, null);
            if (!code.endsWith(";")) {
                markup.writeRaw(";", null); //NOI18N
            }
            markup.writeRaw("\n", null); //NOI18N
        }
        markup.writeRaw("    return true;\n", null); //NOI18N
        markup.writeRaw("}\n", null); //NOI18N
        return markup;

    }
}
