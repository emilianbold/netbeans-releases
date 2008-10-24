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
package org.netbeans.modules.localhistory.options;

import java.beans.PropertyChangeListener;
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
public final class LocalHistoryOptionsController extends OptionsPanelController implements DocumentListener {
    
    private final LocalHistoryOptionsPanel panel;

    public LocalHistoryOptionsController() {
        panel = new LocalHistoryOptionsPanel();
        panel.warningLabel.setVisible(false);
        panel.daysTextField.getDocument().addDocumentListener(this);
    }   
        
    public void update() {        
        panel.daysTextField.setText(Long.toString(LocalHistorySettings.getInstance().getTTL()));
    }

    public void applyChanges() {
        if(!isValid()) return;
        LocalHistorySettings.getInstance().setTTL(Integer.parseInt(panel.daysTextField.getText()));
    }

    public void cancel() {
        // do nothing
    }

    public boolean isValid() {
        boolean valid = true;
        try {            
            Integer.parseInt(panel.daysTextField.getText());
        } catch (NumberFormatException e) {
            valid = false;
        }
        panel.warningLabel.setVisible(!valid); 
        return valid;
    }

    public boolean isChanged() {       
        String ttl = Long.toString(LocalHistorySettings.getInstance().getTTL());        
        return !ttl.equals(panel.daysTextField.getText());
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
}
