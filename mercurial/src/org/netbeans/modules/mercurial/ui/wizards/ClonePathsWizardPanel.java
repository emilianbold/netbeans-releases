/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.ui.wizards;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class ClonePathsWizardPanel implements WizardDescriptor.ValidatingPanel {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ClonePathsPanel component;
    private HgURL repositoryOrig;
    private Listener listener;
    private Document pullPathDoc, pushPathDoc;
    private boolean pullUrlValidated, pushUrlValidated;
    private HgURL pullUrl, pushUrl;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new ClonePathsPanel();
            initInteraction();
        }
        return component;
    }

    private void initInteraction() {
        listener = new Listener();

        pullPathDoc = component.defaultPullPathField.getDocument();
        pushPathDoc = component.defaultPushPathField.getDocument();

        pullPathDoc.addDocumentListener(listener);
        pushPathDoc.addDocumentListener(listener);

        component.defaultValuesButton.addActionListener(listener);
    }

    final class Listener implements ActionListener, DocumentListener {

        public void actionPerformed(ActionEvent e) {
            assert e.getSource() == component.defaultValuesButton;

            setTextFromRepository(component.defaultPullPathField);
            setTextFromRepository(component.defaultPushPathField);
        }

        public void insertUpdate(DocumentEvent e) {
            textChanged(e);
        }
        public void removeUpdate(DocumentEvent e) {
            textChanged(e);
        }
        public void changedUpdate(DocumentEvent e) {
            textChanged(e);
        }

        private void textChanged(DocumentEvent e) {
            Document doc = e.getDocument();

            boolean wasKnownToBeInvalid = isKnownToBeInvalid();

            if (doc == pullPathDoc) {
                pullUrlValidated = false;
                pullUrl = null;
            } else if (doc == pushPathDoc) {
                pushUrlValidated = false;
                pushUrl = null;
            } else {
                assert false;
            }

            if (isKnownToBeInvalid() != wasKnownToBeInvalid) {
                fireChangeEvent();
            }
        }

    }

    private boolean isKnownToBeInvalid() {
        return (pullUrlValidated && (pullUrl == null))
               || (pushUrlValidated && (pushUrl == null));

    }
    
    public boolean isValid() {
        return !isKnownToBeInvalid();
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ClonePathsWizardPanel.class);
    }
    
    private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>(3);
    public final void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }
    public final void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    protected final void fireChangeEvent() {
        if (!changeListeners.isEmpty()) {
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : changeListeners) {
                l.stateChanged(e);
            }
        }
    }

    public void validate() throws WizardValidationException {
        if (!pullUrlValidated) {
            pullUrlValidated = true;
            pullUrl = validateUrl(component.defaultPullPathField,
                                  "pull path invalid",                  //NOI18N
                                  "defaultPullPath.Invalid");           //NOI18N
        }
        if (!pushUrlValidated) {
            pushUrlValidated = true;
            pushUrl = validateUrl(component.defaultPushPathField,
                                  "push path invalid",                  //NOI18N
                                  "defaultPushPath.Invalid");           //NOI18N
        }
    }

    private HgURL validateUrl(JTextField field, String systemErrMsg, String errMsgKey) throws WizardValidationException {
        try {
            return new HgURL(field.getText().trim());
        } catch (URISyntaxException ex) {
            throw new WizardValidationException(component,
                                                systemErrMsg,
                                                getMessage(errMsgKey));
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            HgURL repository = (HgURL) ((WizardDescriptor) settings).getProperty("repository"); // NOI18N
            boolean repoistoryChanged = repositoryOrig == null || !repository.equals(repositoryOrig);
            repositoryOrig = repository;
            
            if(repoistoryChanged || component.defaultPullPathField.getText().equals(""))
                setTextFromRepository(component.defaultPullPathField);
            if(repoistoryChanged || component.defaultPushPathField.getText().equals(""))
                setTextFromRepository(component.defaultPushPathField);
        }
    }
    public void storeSettings(Object settings) {
        if (!pullUrlValidated || !pushUrlValidated) {
            return;
        };
        if (settings instanceof WizardDescriptor) {
            ((WizardDescriptor) settings).putProperty("defaultPullPath", pullUrl); // NOI18N
            ((WizardDescriptor) settings).putProperty("defaultPushPath", pushUrl); // NOI18N
        }
    }

    private void setTextFromRepository(JTextField textField) {
        textField.setText(repositoryOrig.toHgCommandUrlString());  //incl. username and password
    }

    private static String getMessage(String msgKey) {
        return NbBundle.getMessage(ClonePathsWizardPanel.class, msgKey);
    }

}
