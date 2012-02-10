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
package org.netbeans.modules.java.hints.providers.code;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.java.hints.providers.spi.CustomizerProvider;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class ReflectiveCustomizerProvider implements CustomizerProvider {
    private final String hintClassName;
    private final String hintId;
    private final List<OptionDescriptor> options;

    public ReflectiveCustomizerProvider(String hintClassName, String hintId, List<OptionDescriptor> options) {
        this.hintClassName = hintClassName;
        this.hintId = hintId;
        this.options = options;
    }

    @Override
    public JComponent getCustomizer(Preferences prefs) {
        return new CustomizerImpl(prefs, hintClassName, hintId, options);
    }

    private static final class CustomizerImpl extends JPanel {

        public CustomizerImpl(Preferences prefs, String hintClassName, String hintId, List<OptionDescriptor> options) {
            try {
                setLayout(new GridBagLayout());

                int c = 0;

                for (OptionDescriptor option : options) {
                    JCheckBox checkBox = new JCheckBox();

                    org.openide.awt.Mnemonics.setLocalizedText(checkBox, option.displayName);
                    checkBox.setToolTipText(option.tooltip);
                    checkBox.addActionListener(new ActionListenerImpl(checkBox, option.preferencesKey, prefs));

                    checkBox.setSelected(prefs.getBoolean(option.preferencesKey, option.defaultValue));

                    GridBagConstraints constraints = new GridBagConstraints();

                    constraints.anchor = GridBagConstraints.NORTHWEST;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    constraints.gridheight = 1;
                    constraints.gridwidth = 1;
                    constraints.gridx = 0;
                    constraints.gridy = c++;
                    constraints.weightx = 1;
                    constraints.weighty = 0;

                    add(checkBox, constraints);
                }

                GridBagConstraints constraints = new GridBagConstraints();

                constraints.anchor = GridBagConstraints.NORTHWEST;
                constraints.fill = GridBagConstraints.BOTH;
                constraints.gridheight = 1;
                constraints.gridwidth = 1;
                constraints.gridx = 0;
                constraints.gridy = c++;
                constraints.weightx = 1;
                constraints.weighty = 1;

                add(new JPanel(), constraints);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        private static final class ActionListenerImpl implements ActionListener {
            
            private final JCheckBox checkBox;
            private final String key;
            private final Preferences prefs;

            public ActionListenerImpl(JCheckBox checkBox, String key, Preferences prefs) {
                this.checkBox = checkBox;
                this.key = key;
                this.prefs = prefs;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.putBoolean(key, checkBox.isSelected());
            }

        }
        
    }

    public static final class OptionDescriptor {
        public final String preferencesKey;
        public final boolean defaultValue;
        public final String displayName;
        public final String tooltip;

        public OptionDescriptor(String preferencesKey, boolean defaultValue, String displayName, String tooltip) {
            this.preferencesKey = preferencesKey;
            this.defaultValue = defaultValue;
            this.displayName = displayName;
            this.tooltip = tooltip;
        }

    }

}
