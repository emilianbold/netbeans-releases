/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.platform.gems;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.UIResource;
import org.openide.util.NbBundle;

final class GemUISupport {

    private GemUISupport() {
        // forbid instances
    }

    /**
     * Compound multi-line HTML description suitable of the given Gem.
     *
     * @param gem Gem to be described
     * @return multi-line HTML description
     */
    static String getGemHTMLDescriptionForTextPane(final Gem gem) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>"); // NOI18N
        sb.append("<h2>"); // NOI18N
        sb.append(gem.getName());
        sb.append("</h2>\n"); // NOI18N

        String installedAsString = gem.getInstalledVersionsAsString();
        String availableAsString = gem.getAvailableVersionsAsString();
        if (installedAsString != null && availableAsString != null) {
            // It's an update gem
            sb.append("<h3>"); // NOI18N
            sb.append(getMessage("GemUISupport.InstalledVersion"));
            sb.append("</h3>"); // NOI18N
            sb.append(installedAsString);

            sb.append("<h3>"); // NOI18N
            sb.append(getMessage("GemUISupport.AvailableVersion"));
            sb.append("</h3>"); // NOI18N
            sb.append(availableAsString);
            sb.append("<br>"); // NOI18N
        } else {
            sb.append("<h3>"); // NOI18N
            String version = installedAsString;
            if (version == null) {
                version = availableAsString;
            }
            if (version.indexOf(',') == -1) {
                sb.append(getMessage("GemUISupport.Version"));
            } else {
                sb.append(getMessage("GemUISupport.Versions"));
            }
            sb.append("</h3>"); // NOI18N
            sb.append(version);
        }

        if (gem.getDescription() != null) {
            sb.append("<h3>"); // NOI18N
            sb.append(getMessage("GemUISupport.Description"));
            sb.append("</h3>"); // NOI18N
            sb.append(gem.getHTMLDescription());
        }

        sb.append("</html>"); // NOI18N
        return sb.toString();
    }

    /** {@link ListCellRenderer} for {@link Gem} instances. */
    static class GemListRenderer extends JLabel implements ListCellRenderer, UIResource {

        public GemListRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            StringBuilder label = new StringBuilder(100);
            Gem gem = ((Gem) value);
            label.append("<html><b>"); // NOI18N
            label.append(gem.getName());
            label.append("</b>"); // NOI18N

            if (gem.getInstalledVersionsAsString() != null) {
                label.append(" ("); // NOI18N
                label.append(gem.getInstalledVersionsAsString());
                if (gem.getAvailableVersionsAsString() != null) {
                    label.append(" => ").append(gem.getAvailableVersionsAsString()); // NOI18N
                }
                label.append(") "); // NOI18N
            }

            if (gem.getDescription() != null) {
                label.append(": "); // NOI18N
                label.append(gem.getDescription());
            }

            label.append("</html>"); // NOI18N
            setText(label.toString());
            return this;
        }

        // #93658: GTK needs name to render cell renderer "natively"
        public @Override String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(GemUISupport.class, key);
    }
}
