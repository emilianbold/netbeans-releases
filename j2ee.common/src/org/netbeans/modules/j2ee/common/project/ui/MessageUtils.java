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

package org.netbeans.modules.j2ee.common.project.ui;

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public final class MessageUtils {

    private static final Color ERROR_COLOR;

    private static final Color WARNING_COLOR;

    static {
        // taken from WizardDescriptor
        Color errorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (errorForeground == null) {
            errorForeground = new Color(255, 0, 0);
        }

        ERROR_COLOR = errorForeground;

        Color warningForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
        if (warningForeground == null) {
            warningForeground = new Color(51, 51, 51);
        }

        WARNING_COLOR = warningForeground;
    }

    private MessageUtils() {
        super();
    }

    public static void setMessage(JLabel label, MessageType type, String message) {
        Parameters.notNull("type", type);
        Parameters.notNull("message", message);

        label.setForeground(type.getColor());
        setMessage(label, type.getIcon(), message);
    }

    public static void clear(JLabel label) {
        setMessage(label, (Icon) null, (String) null);
    }

    private static void setMessage(JLabel label, Icon icon, String message) {
        label.setIcon(message == null ? null : icon);
        label.setText(message);
        label.setToolTipText(message);
    }

    public static enum MessageType {

        ERROR  {
            protected Icon getIcon() {
                return new ImageIcon(Utilities.loadImage(
                        "org/netbeans/modules/j2ee/common/project/ui/resources/error.gif")); // NOI18N
            }

            protected Color getColor() {
                return ERROR_COLOR;
            }
        },

        WARNING {
            protected Icon getIcon() {
                return new ImageIcon(Utilities.loadImage(
                        "org/netbeans/modules/j2ee/common/project/ui/resources/warning.gif")); // NOI18N
            }

            protected Color getColor() {
                return WARNING_COLOR;
            }
        };

        protected abstract Icon getIcon();

        protected abstract Color getColor();

    }
}
