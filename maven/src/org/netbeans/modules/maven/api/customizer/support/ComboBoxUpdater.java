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

package org.netbeans.modules.maven.api.customizer.support;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 *
 * @author mkleint
 */
public abstract class ComboBoxUpdater<T> implements ActionListener, AncestorListener {

    private JComboBox component;
    private JLabel label;
    
    private boolean inherited = false;
    
    /** Creates a new instance of TextComponentUpdater */
    public ComboBoxUpdater(JComboBox comp, JLabel label) {
        component = comp;
        component.addAncestorListener(this);
        this.label = label;
    }
    
    public abstract T getValue();
    public abstract T getDefaultValue();
    public abstract void setValue(T value);

    private void setModelValue() {
        if (inherited) {
            inherited = false;
//            component.setBackground(DEFAULT);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            
            component.setToolTipText(""); //NOI18N
        }
        T val = (T)component.getSelectedItem();
        setValue(val == getDefaultValue() ? null : val);
        if (val == getDefaultValue()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    component.removeActionListener(ComboBoxUpdater.this);
                    setComboValue(getValue(), getDefaultValue(), component);
                    component.addActionListener(ComboBoxUpdater.this);
                }
            });
        }
    }
    
    public void actionPerformed(ActionEvent event) {
        setModelValue();
    }

    public void ancestorAdded(AncestorEvent event) {
        setComboValue(getValue(), getDefaultValue(), component);
        component.addActionListener(this);
    }

    public void ancestorRemoved(AncestorEvent event) {
        component.removeActionListener(this);
    }

    public void ancestorMoved(AncestorEvent event) {
    }
    
    private void setComboValue(T value, T projectValue, JComboBox field) {
        if (value != null) {
            field.setSelectedItem(value);
            component.setToolTipText(""); //NOI18N
            inherited = false;
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        } else if (projectValue != null) {
            field.setSelectedItem(projectValue);
//            field.setBackground(INHERITED);
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
            component.setToolTipText(org.openide.util.NbBundle.getMessage(ComboBoxUpdater.class, "HINT_inherited"));
            inherited = true;
        } else {
            field.setSelectedItem(field.getModel().getElementAt(0));
            component.setToolTipText(""); //NOI18N
            inherited = false;
            label.setFont(label.getFont().deriveFont(Font.BOLD));
      }
    }

}
