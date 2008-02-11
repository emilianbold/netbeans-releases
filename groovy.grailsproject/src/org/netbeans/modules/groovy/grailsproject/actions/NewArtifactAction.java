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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.WizardDescriptor;
import org.netbeans.modules.groovy.grailsproject.ui.wizards.NewArtifactWizardIterator;
import org.openide.DialogDisplayer;
import java.awt.Dialog;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import java.util.logging.Logger;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author schmidtm
 */
public class NewArtifactAction extends AbstractAction {
    
    GrailsProject project;
    SourceCategory cat;
    private  final Logger LOG = Logger.getLogger(NewArtifactAction.class.getName());

    public NewArtifactAction(GrailsProject project, SourceCategory cat, String desc) {
        super (desc);
        this.project = project;
        this.cat = cat;
    }

    public void actionPerformed(ActionEvent e) {
        
        // LOG.log(Level.WARNING, "hitting actionPerformed()");
        
        assert cat != null;
        assert project != null;
        
        WizardDescriptor wiz =  null;
        
        switch(cat){
            case DOMAIN:
                wiz = new WizardDescriptor(new NewArtifactWizardIterator(project, cat));
                break;
            case CONTROLLERS:
                wiz = new WizardDescriptor(new NewArtifactWizardIterator(project, cat));
                break;
            case SERVICES:
                wiz = new WizardDescriptor(new NewArtifactWizardIterator(project, cat));
                break;
            default:
                return;
            }
        
        assert wiz != null;
        
        wiz.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
        wiz.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
        wiz.putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N

        wiz.setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(wiz);
        
        try {
                dlg.setVisible(true);
                if (wiz.getValue() == WizardDescriptor.FINISH_OPTION) {
                    Set result = wiz.getInstantiatedObjects();
                    
                    if (result != null) {
                        for (Object fo : result) {
                            try {
                                if(fo != null) {
                                    DataObject dObj = DataObject.find((FileObject) fo);
                                    OpenCookie ok = dObj.getLookup().lookup(OpenCookie.class);
                                    if (ok != null) {
                                        ok.open();
                                    }
                                }
                            } catch (DataObjectNotFoundException ex) {
                                LOG.log(Level.WARNING, "DataObjectNotFoundException: " + ex.getMessage());
                            }
                        }

                    }


                }
            } finally {
                dlg.dispose();
            }
    }

}
