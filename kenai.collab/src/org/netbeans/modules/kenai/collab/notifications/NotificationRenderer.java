/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.kenai.collab.notifications;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.UIResource;
import javax.swing.text.html.HTMLDocument;

/**
 * Renderer for Notifications
 * Currently not used
 * @see Notification
 * @author Jan Becicka
 */
final class NotificationRenderer extends JEditorPane implements ListCellRenderer, UIResource {

    public NotificationRenderer() {
        super();
        setOpaque(false);
        setContentType("text/html");

        // add a CSS rule to force body tags to use the default label font
        // instead of the value in javax.swing.text.html.default.csss
        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)getDocument()).getStyleSheet().addRule(bodyRule);

        setBorder(null);
        setEditable(false);
        addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // #93658: GTK needs name to render cell renderer "natively"
        setName("ComboBox.listRenderer");
        // NOI18N
            // #49954: render a specially inserted package somehow.
            String pkgitem = (String) value;
            setText(pkgitem.trim());
            //setIcon(null);
        if (isSelected) {
            setOpaque(true);
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setOpaque(false);
            if (index % 2 == 0) {
                setBackground(new Color(235,235,235));
            } else {
                setBackground(list.getBackground());
            }
            setForeground(list.getForeground());
        }
        return this;
    }

    // #93658: GTK needs name to render cell renderer "natively"
    @Override
    public String getName() {
        String name = super.getName();
        return name == null ? "ComboBox.renderer" : name;
    }
}
