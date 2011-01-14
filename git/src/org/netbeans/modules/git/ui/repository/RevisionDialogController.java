/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.repository;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.git.utils.GitUtils;

/**
 *
 * @author ondra
 */
public class RevisionDialogController implements ActionListener, DocumentListener, PropertyChangeListener {
    private final RevisionDialog panel;
    private final File repository;
    private final RevisionInfoPanelController infoPanelController;
    private final PropertyChangeSupport support;
    public static final String PROP_VALID = "RevisionDialogController.valid"; //NOI18N
    private boolean valid = true;
    private final Timer t;
    private boolean internally;
    private final File[] roots;
    private String revision;

    public RevisionDialogController (File repository, File[] roots) {
        this(repository, roots, GitUtils.HEAD);
    }
    
    public RevisionDialogController (File repository, File[] roots, String initialRevision) {
        infoPanelController = new RevisionInfoPanelController(repository);
        panel = new RevisionDialog(infoPanelController.getPanel(), initialRevision);
        this.repository = repository;
        this.roots = roots;
        this.support = new PropertyChangeSupport(this);
        this.t = new Timer(500, this);
        t.stop();
        infoPanelController.loadInfo(revision = panel.revisionField.getText());
        attachListeners();
    }

    public RevisionDialog getPanel () {
        return panel;
    }

    public void setEnabled (boolean enabled) {
        panel.btnSelectRevision.setEnabled(enabled);
        panel.revisionField.setEnabled(enabled);
    }

    public String getRevision () {
        return revision;
    }
    
    public void addPropertyChangeListener (PropertyChangeListener list) {
        support.addPropertyChangeListener(list);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener list) {
        support.removePropertyChangeListener(list);
    }

    private void attachListeners () {
        panel.btnSelectRevision.addActionListener(this);
        panel.revisionField.getDocument().addDocumentListener(this);
        infoPanelController.addPropertyChangeListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.btnSelectRevision) {
            openRevisionPicker();
        } else if (e.getSource() == t) {
            t.stop();
            infoPanelController.loadInfo(revision);
        }
    }

    private void openRevisionPicker () {
        RevisionPicker picker = new RevisionPicker(repository, roots);
        if (picker.open()) {
            Revision selectedRevision = picker.getRevision();
            internally = true;
            try {
                panel.revisionField.setText(selectedRevision.toString());
                panel.revisionField.setCaretPosition(0);
            } finally {
                internally = false;
            }
            if (!selectedRevision.getName().equals(revision)) {
                revision = selectedRevision.getName();
                updateRevision();
            }
        }
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        revisionChanged();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        revisionChanged();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }

    private void setValid (boolean flag) {
        boolean oldValue = valid;
        valid = flag;
        if (valid != oldValue) {
            support.firePropertyChange(PROP_VALID, oldValue, valid);
        }
    }

    private void revisionChanged () {
        if (!internally) {
            revision = panel.revisionField.getText();
            updateRevision();
        }
    }
    
    private void updateRevision () {
        setValid(false);
        t.restart();
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName() == RevisionInfoPanelController.PROP_VALID) {
            setValid(Boolean.TRUE.equals(evt.getNewValue()));
        }
    }
}
