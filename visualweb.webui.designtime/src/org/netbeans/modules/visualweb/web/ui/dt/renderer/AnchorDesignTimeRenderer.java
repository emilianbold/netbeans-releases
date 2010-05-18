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

import com.sun.rave.web.ui.component.Anchor;
import com.sun.rave.web.ui.component.PropertySheetSection;
import com.sun.rave.web.ui.component.SkipHyperlink;
import com.sun.rave.web.ui.renderer.AnchorRenderer;
import com.sun.rave.web.ui.util.RenderingUtilities;
import java.io.IOException;
import java.net.URL;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * A delegating renderer for {@link org.netbeans.modules.visualweb.web.ui.component.Anchor} that
 * outputs an HTML named anchor, and an image with the anchor icon, only if
 * the anchor is not being used as an unparented helper component and it is
 * not a child of the utility SkipHyperlink.
 *
 * @author gjmurphy
 */
public class AnchorDesignTimeRenderer extends AbstractDesignTimeRenderer {

    static final String ANCHOR_ICON =
            "/org/netbeans/modules/visualweb/web/ui/dt/component/Anchor_C16.png"; //NOI18N

    boolean isTextDefaulted;

    public AnchorDesignTimeRenderer() {
        super(new AnchorRenderer());
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        UIComponent parent = component.getParent();
        if (component instanceof Anchor && parent != null && !SkipHyperlink.class.isAssignableFrom(parent.getClass())
                && !PropertySheetSection.class.isAssignableFrom(parent.getClass())) {
            Anchor anchor = (Anchor) component;
            ResponseWriter writer = context.getResponseWriter();
            writer.startElement("a", anchor); //NOI18N
            String id = anchor.getId();
            writer.writeAttribute("id", id, "id"); //NOI18N
            String style = anchor.getStyle();
            if (style != null)
                writer.writeAttribute("style", style, null); //NOI18N
            String styleClass = anchor.getStyleClass();
            if (styleClass != null)
                RenderingUtilities.renderStyleClass(context, writer, component, null);
            writer.writeAttribute("name", id, null); //NOI18N
            writer.startElement("img", anchor); //NOI18N
            URL url = this.getClass().getResource(ANCHOR_ICON); // NOI18N
            writer.writeURIAttribute("src", url, null); //NOI18N
            writer.endElement("img"); // NOI18N
            writer.endElement("a"); //NOI18N
        }
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
    }

    public void encodeChildren(FacesContext context, UIComponent component) {
    }

}
