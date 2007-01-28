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

package org.netbeans.modules.visualweb.faces.dt.renderer;

import com.sun.faces.renderkit.html_basic.MessageRenderer;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * A delegating renderer for {@link javax.faces.component.UIMessage}. If the message
 * component is not yet "for" another component, a summary instruction is rendered.
 * If it is "for" another component, a summary of the reference is rendered.
 *
 * @author gjmurphy
 */
public class MessageDesignTimeRenderer extends AbstractDesignTimeRenderer {

    static ComponentBundle bundle = ComponentBundle.getBundle(MessageDesignTimeRenderer.class);

    public MessageDesignTimeRenderer() {
        super(new MessageRenderer());
    }

    protected MessageDesignTimeRenderer(Renderer renderer) {
        super(renderer);
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        UIMessage messageComponent = (UIMessage) component;
        String forComponentId = messageComponent.getFor();
        String summary = null;
        String detail = null;
        if (forComponentId == null || forComponentId.length() == 0)
            summary = bundle.getMessage("Message.default.summary"); //NOI18N
        else
            summary = bundle.getMessage("Message.for.summary", new String[] {forComponentId}); //NOI18N
        if (forComponentId == null || forComponentId.length() == 0)
            detail = "";  //NOI18N
        else
            detail = bundle.getMessage("Message.for.detail", new String[] {forComponentId}); //NOI18N

        ResponseWriter writer = context.getResponseWriter();
        String style = (String) component.getAttributes().get("style"); //NOI18N
        String styleClass = (String) component.getAttributes().get("styleClass"); //NOI18N
        String severityStyle = (String) component.getAttributes().get("errorStyle"); //NOI18N
        String severityStyleClass = (String) component.getAttributes().get("errorClass"); //NOI18N
        String layout = (String) component.getAttributes().get("layout"); //NOI18N

        if (severityStyleClass != null)
            styleClass = severityStyleClass;
        if (severityStyle != null)
            style = severityStyle;

        boolean wroteTable = false;
        boolean wroteSpan = false;

        if ((layout != null) && (layout.equals("table"))) {  //NOI18N
            writer.startElement("table", component);  //NOI18N
            writer.writeAttribute("id", component.getClientId(context), "id");  //NOI18N
            writer.startElement("tr", component);  //NOI18N
            writer.startElement("td", component);  //NOI18N
            wroteTable = true;
        }


        if (styleClass != null || style != null) {
            writer.startElement("span", component);  //NOI18N
            wroteSpan = true;
            if (!wroteTable) {
                writer.writeAttribute("id", component.getClientId(context), "id");  //NOI18N
            }
            if (styleClass != null) {
                writer.writeAttribute("class", styleClass, "styleClass");  //NOI18N
            }
            if (style != null) {
                writer.writeAttribute("style", style, "style");  //NOI18N
            }
        }
        if (messageComponent.isShowSummary()) {
            writer.writeText("\t", null);  //NOI18N
            writer.writeText(summary, null);
            writer.writeText(" ", null);  //NOI18N
        }
        if (messageComponent.isShowDetail()) {
            writer.writeText(detail, null);
        }
        if (wroteSpan) {
            writer.endElement("span");  //NOI18N
        }
        if (wroteTable) {
            writer.endElement("td");  //NOI18N
            writer.endElement("tr");  //NOI18N
            writer.endElement("table");  //NOI18N
        }

    }
    
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
    }
    
}
