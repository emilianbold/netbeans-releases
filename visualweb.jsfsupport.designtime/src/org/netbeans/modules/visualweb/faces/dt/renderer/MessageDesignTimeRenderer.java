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
