/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.ui.project.activity;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public abstract class ActivityDisplayer {

    Date activityDate;
    final int maxWidth;
    private Map<String,Action> openBrowserActions;

    public ActivityDisplayer(Date activityDate, int maxWidth) {
        this.activityDate = activityDate;
        this.maxWidth = maxWidth;
    }

    public Date getActivityDate() {
        return activityDate;
    }

    public abstract JComponent getShortDescriptionComponent();

    public abstract JComponent getDetailsComponent();

    abstract String getUserName();

    public JComponent getTitleComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        Icon userIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/user.png", true); //NOI18N
        panel.add(new JLabel(getUserName(), userIcon, SwingConstants.LEFT), new GridBagConstraints());
        return panel;
    }

    /**
     * Override this default implementation to display activity specific icon
     */
    public Icon getActivityIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/unknown.png", true); //NOI18N
    }

    Action getOpenBrowserAction(String url) {
        Action action;
        if (openBrowserActions == null) {
            openBrowserActions = new HashMap<>();
            action = null;
        } else {
            action = openBrowserActions.get(url);
        }
        if (action == null) {
            action = Utils.getOpenBrowserAction(url);
            openBrowserActions.put(url, action);
        }
        return action;
    }

    /**
     * Creates an array of components for a formatted text composed from multiple parts
     * that cannot fit into one string (one text component) since some parts are
     * represented by different components (e.g. links). This method allows to use special
     * components as parameters for a simple format definition suitable for
     * internationalization. The method automatically creates labels for the textual
     * parts of the formatted text. For example a format like
     * "Build {0} of {1} ended" can use two link components as parameters and the
     * method will return a complete sequence of components to show the text (i.e.
     * the param components and the automatically created labels ordered properly).
     * @param bundleKey the bundle key for the format definition
     * @param parameters components or strings representing the parameters for the formatted text
     * @return array of components that can be put on a row to show the requested text
     */
    Component[] createMultipartTextComponents(String bundleKey, Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return new Component[0];
        }

        String[] paramPlaceholders = new String[parameters.length];
        for (int i=0; i < paramPlaceholders.length; i++) {
            Object param = parameters[i] instanceof Component ? new Object() : parameters[i];
            paramPlaceholders[i] = param.toString();
        }
        String translated = NbBundle.getMessage(getClass(), bundleKey, paramPlaceholders);
        int[] positions = new int[paramPlaceholders.length];
        for (int i=0; i < paramPlaceholders.length; i++) {
            positions[i] = parameters[i] instanceof Component ? translated.indexOf(paramPlaceholders[i]) : -1;
        }

        List<Component> components = new ArrayList<>(parameters.length*2 + 1);
        int pos = 0;
        int prevPos = 0;
        do {
            int pos1 = -1;
            int pos2 = -1;
            int compIndex = -1;
            if (pos+1 == translated.length()) {
                pos1 = prevPos;
                if (pos1 > 0 && translated.charAt(pos1) == ' ') {
                    pos1++;
                }
                pos2 = pos + 1;
            } else {
                for (int i=0; i < paramPlaceholders.length; i++) {
                    if (pos == positions[i]) {
                        compIndex = i;
                        pos1 = prevPos;
                        if (pos1 > 0 && translated.charAt(pos1) == ' ') {
                            pos1++;
                        }
                        pos2 = pos;
                        if (pos2 > 0 && translated.charAt(pos2-1) == ' ') {
                            pos2--;
                        }
                        pos += paramPlaceholders[i].length() - 1;
                        prevPos = pos + 1;
                        break;
                    }
                }
            }
            if (pos1 != -1 && pos2 != -1) {
                JLabel label = new JLabel();
                if (pos2 > pos1) {
                    label.setText(translated.substring(pos1, pos2));
                }
                components.add(label);
                if (compIndex > -1) {
                    components.add((Component)parameters[compIndex]);
                }
            }
        } while (++pos < translated.length());

        return components.toArray(new Component[components.size()]);
    }

    /**
     * Creates a component that shows a formatted text where special components can be
     * provided as parameters for parts of the text. Thus it's easy to internationalize
     * text formats like "Build {0} of {1} ended" in one definition and use components
     * instead of strings as parameters (strings can be provided too). The method creates
     * all necessary labels for the normal text as needed and lays them out together with
     * the provided param components according to the format.
     * Note this method creates one composite component as a result while the other
     * createMultipartTextComponents methods returns an array of the individual components
     * that can be laid out by the caller.
     * @param bundleKey the bundle key for the format definition
     * @param parameters components or strings representing the parameters for the formatted text
     * @return component that shows the requested formatted text 
     */
    JComponent createMultipartTextComponent(String bundleKey, Object... parameters) {
        Component[] components = createMultipartTextComponents(bundleKey, parameters);
        if (components == null || components.length == 0) {
            return new JPanel();
        }
        if (components.length == 1 && components[0] instanceof JComponent) {
            return (JComponent) components[0];
        }
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        for (Component comp : components) {
            panel.add(comp, gbc);
        }
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(new JLabel(), gbc);
        return panel;
    }
}
