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
package org.netbeans.modules.localhistory.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.localhistory.LocalHistorySettings;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 */
public final class LocalHistoryOptionsController extends OptionsPanelController implements DocumentListener, ActionListener {
    
    private final LocalHistoryOptionsPanel panel;
    private boolean noLabelValue;
    private String daysValue;
    
    public LocalHistoryOptionsController() {
        panel = new LocalHistoryOptionsPanel();
        panel.warningLabel.setVisible(false);
        panel.daysTextField.getDocument().addDocumentListener(this);
        panel.keepForeverCheckBox.addActionListener(this);
    }   
        
    public void update() {        
        panel.daysTextField.setText(daysValue = String.valueOf(LocalHistorySettings.getInstance().getTTL()));
        panel.noLabelCleanupCheckBox.setSelected(noLabelValue = !LocalHistorySettings.getInstance().getCleanUpLabeled());
        panel.keepForeverCheckBox.setSelected(LocalHistorySettings.getInstance().getKeepForever());
        updateForeverState();
    }

    public void applyChanges() {
        if(!isValid()) return;
        if(panel.keepForeverCheckBox.isSelected()) {
            LocalHistorySettings.getInstance().setKeepForever(true);
            LocalHistorySettings.getInstance().setTTL(Integer.parseInt(daysValue));
            LocalHistorySettings.getInstance().setCleanUpLabeled(!noLabelValue);
        } else {
            LocalHistorySettings.getInstance().setKeepForever(false);
            LocalHistorySettings.getInstance().setTTL(Integer.parseInt(panel.daysTextField.getText()));
            LocalHistorySettings.getInstance().setCleanUpLabeled(!panel.noLabelCleanupCheckBox.isSelected());
        }
    }

    public void cancel() {
        // do nothing
    }

    public boolean isValid() {
        boolean valid = true;
        try {       
            if(!panel.keepForeverCheckBox.isSelected()) {
                Integer.parseInt(panel.daysTextField.getText());
            } 
        } catch (NumberFormatException e) {
            valid = false;
        }
        panel.warningLabel.setVisible(!valid); 
        return valid;
    }

    public boolean isChanged() {       
        String ttl = Long.toString(LocalHistorySettings.getInstance().getTTL());        
        return !ttl.equals(panel.daysTextField.getText()) && 
               (panel.noLabelCleanupCheckBox.isSelected() != LocalHistorySettings.getInstance().getCleanUpLabeled()) &&
               (panel.keepForeverCheckBox.isSelected() != LocalHistorySettings.getInstance().getKeepForever());
    }

    public JComponent getComponent(Lookup masterLookup) {
        return panel;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        // do nothing
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        // do nothing
    }

    public void insertUpdate(DocumentEvent e) {
       isValid();
    }

    public void removeUpdate(DocumentEvent e) {
       isValid();
    }

    public void changedUpdate(DocumentEvent e) {
       isValid();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.keepForeverCheckBox) {
            updateForeverState();
        }
    }
    
    private void updateForeverState() {
        if(panel.keepForeverCheckBox.isSelected()) {
            panel.daysTextField.setEnabled(false);
            panel.noLabelCleanupCheckBox.setEnabled(false);
            panel.daysLabel1.setEnabled(false);
            panel.daysLabel2.setEnabled(false);
            panel.noLabelCleanupCheckBox.setEnabled(false);
            noLabelValue = panel.noLabelCleanupCheckBox.isSelected();
            daysValue = panel.daysTextField.getText();

            panel.noLabelCleanupCheckBox.setSelected(false);
            panel.daysTextField.setText("");
        } else {
            panel.daysTextField.setEnabled(true);
            panel.noLabelCleanupCheckBox.setEnabled(true);
            panel.daysLabel1.setEnabled(true);
            panel.daysLabel2.setEnabled(true);

            panel.noLabelCleanupCheckBox.setSelected(noLabelValue);
            panel.daysTextField.setText(daysValue);
        }
    }
}
