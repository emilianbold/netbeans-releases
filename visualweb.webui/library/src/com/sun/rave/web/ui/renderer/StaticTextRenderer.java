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
package com.sun.rave.web.ui.renderer;

import com.sun.rave.web.ui.component.StaticText;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.ConversionUtilities;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;


/**
 * <p>Renderer for a {@link StaticText} component.</p>
 */

public class StaticTextRenderer extends AbstractRenderer {


    // ======================================================== Static Variables


    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] =
    { "onClick", "onDblClick", "onMouseUp", //NOI18N
      "onMouseDown", "onMouseMove", "onMouseOut", "onMouseOver"}; //NOI18N


      // -------------------------------------------------------- Renderer Methods


    /**
     * <p>Render the appropriate element start, depending on whether the
     * <code>for</code> property is set or not.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component StaticText component
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(FacesContext context, UIComponent component,
                               ResponseWriter writer) throws IOException {

         writer.startElement("span", component);
    }


    /**
     * <p>Render the appropriate element attributes, followed by the
     * label content, depending on whether the <code>for</code> property
     * is set or not.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component StaticText component
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, UIComponent component,
                                    ResponseWriter writer) throws IOException {

        StaticText st = (StaticText) component;
        addCoreAttributes(context, component, writer, null);
        addStringAttributes(context, component, writer, stringAttributes);
        if (st.getToolTip() != null) {
            writer.writeAttribute("title", st.getToolTip(), null);
        }
     }


    /**
     * <p>Render the appropriate element end, depending on whether the
     * <code>for</code> property is set or not.</p>
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
        StaticText staticText = (StaticText) component;

        String currentValue = 
                ConversionUtilities.convertValueToString(component, staticText.getText());
        String style = staticText.getStyle();
        String styleClass = staticText.getStyleClass();

        // <RAVE>
        // Object currentObj = getAsString(context, component);
        // if (currentObj != null) {
        //     if (currentObj instanceof String) {
        //         currentValue = (String) currentObj;
        //     } else {
        //         currentValue = currentObj.toString();
        //     }
        // }
        // </RAVE>
        if (currentValue != null) {
            java.util.ArrayList parameterList = new ArrayList();

            // get UIParameter children...

            java.util.Iterator kids = component.getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();

                //PENDING(rogerk) ignore if child is not UIParameter?

                if (!(kid instanceof UIParameter)) {
                    continue;
                }

                parameterList.add(((UIParameter) kid).getValue());
            }

            // If at least one substitution parameter was specified,
            // use the string as a MessageFormat instance.
            String message = null;
            if (parameterList.size() > 0) {
                message = MessageFormat.format
                    (currentValue, parameterList.toArray
                                   (new Object[parameterList.size()]));
            } else {
                message = currentValue;
            }

            if (message != null) {
                if (staticText.isEscape()) {
                    writer.writeText(message, "value");
                } else {
                    writer.write(message);
                }
            }
        }
        writer.endElement("span");
    }


    // --------------------------------------------------------- Private Methods



}
