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
package org.netbeans.modules.visualweb.web.ui.dt.renderer;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * A delegating renderer for panel-like components. Outputs default minimum
 * CSS width and height settings if panel contains no children.
 *
 * @author gjmurphy
 */
public abstract class PanelDesignTimeRenderer extends AbstractDesignTimeRenderer {

    /** Creates a new instance of PanelDesignTimeRenderer */
    public PanelDesignTimeRenderer(Renderer renderer) {
        super(renderer);
    }

    abstract protected String getContainerElementName(UIComponent component);

    abstract protected String getShadowText(UIComponent component);

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (component.getChildCount() == 0) {
            ResponseWriter writer = context.getResponseWriter();
            writer.startElement(getContainerElementName(component), component);
            String id = component.getId();
            writer.writeAttribute("id", id, "id"); //NOI18N

            // Calculate CSS height and width style settings
            UIComponent parentComponent = component.getParent();
            StringBuffer sb = new StringBuffer();
            String style = (String) component.getAttributes().get("style");
            if ((style != null) && (style.length() > 0)) {
                sb.append(style);
                sb.append("; "); // NOI18N
            }
            if (style == null || style.indexOf("width:") == -1) {
                sb.append("width: 128px; "); // NOI18N
            }
            if (style == null || style.indexOf("height:") == -1) {
                if ("span".equals(getContainerElementName(component))) {
                    sb.append("height: 24px; "); // NOI18N
                } else {
                    sb.append("height: 128px; "); // NOI18N
                }
            }
            writer.writeAttribute("style", sb.toString(), null); //NOI18N
            sb.setLength(0);

            // Calculate CSS style classes
            String styleClass = (String) component.getAttributes().get("styleClass");
            if ((styleClass != null) && (styleClass.length() > 0)) {
                sb.append(styleClass);
                sb.append(" ");
            }
            sb.append(UNINITITIALIZED_STYLE_CLASS);
            sb.append(" ");
            sb.append(BORDER_STYLE_CLASS);
            writer.writeAttribute("class", sb.toString(), null); // NOI18

            writer.writeText(getShadowText(component), null);
            writer.endElement(getContainerElementName(component));

            return;
        }

        super.encodeBegin(context, component);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        if (component.getChildCount() == 0) {
            return;
        }

        super.encodeEnd(context, component);
    }

}
