/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.jellytools.modules.ruby;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JLabelOperator;

/**
 *
 * @author Ivan Sidorkin <ivansidorkin@netbeans.org>
 */
public class NewFileNameLocationStepOperator extends org.netbeans.jellytools.NewFileNameLocationStepOperator {

    private JLabelOperator _lblObjectName;

    /** Returns operator for first label with "Name"
     * @return JLabelOperator
     */
    @Override
    public JLabelOperator lblObjectName() {
        if (_lblObjectName == null) {
            final String nameLabel = Bundle.getString("org.netbeans.modules.properties.Bundle", "PROP_name");
           // final String nameAndLocationLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "LBL_JavaTargetChooserPanelGUI_Name");
            final String nameAndLocationLabel = Bundle.getString("org.netbeans.modules.ruby.rubyproject.templates.Bundle", "LBL_RubyTargetChooserPanelGUI_Name");
            _lblObjectName = new JLabelOperator(this, new JLabelOperator.JLabelFinder(new ComponentChooser() {

                public boolean checkComponent(Component comp) {
                    JLabel jLabel = (JLabel) comp;
                    String text = jLabel.getText();
                    if (text == null || nameAndLocationLabel.equals(text)) {
                        return false;
                    } else if (text.indexOf(nameLabel) > -1 && (jLabel.getLabelFor() == null || jLabel.getLabelFor() instanceof JTextField)) {
                        return true;
                    }
                    return false;
                }

                public String getDescription() {
                    return "JLabel containing Name and associated with text field";
                }
            }));
        }
        return _lblObjectName;
    }
}
