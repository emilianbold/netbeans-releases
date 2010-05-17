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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.apache.jmeter.module.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Hrebejk, mkuchtiak
 */
final class TargetChooserPanel implements WizardDescriptor.Panel {

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private TargetChooserPanelGUI gui;

    private Project project;
    private TemplateWizard templateWizard;
    
    //TODO how to add [,] to the regular expression?
    private static final Pattern INVALID_FILENAME_CHARACTERS = Pattern.compile("[`~!@#$%^&*()=+\\|{};:'\",<>/?]"); // NOI18N

    TargetChooserPanel(Project project) {
        this.project = project;
    }
    
    TemplateWizard getTemplateWizard() {
        return templateWizard;
    } 

    public Component getComponent() {
        if (gui == null) {
            gui = new TargetChooserPanelGUI(this, project);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        return null;
        //return new HelpCtx( this.getClass().getName() +"."+fileType.toString()); //NOI18N
    }

    public boolean isValid() {        
        boolean ok = ( gui != null && gui.getTargetName() != null);
        
        if (!ok) {
            templateWizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, null); // NOI18N
            return false;
        }
        
        String filename = gui.getTargetName();
        if (INVALID_FILENAME_CHARACTERS.matcher(filename).find()) {
            templateWizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(TargetChooserPanel.class, "MSG_invalid_filename", filename)); // NOI18N
            return false;
        }

        // check if the file name can be created
        String targetName=gui.getTargetName();
        java.io.File file = gui.getTargetFile();
        FileObject template = Templates.getTemplate( templateWizard );
        String ext = template.getExt ();
        
        String errorMessage = Utilities.canUseFileName (file, gui.getRelativeTargetFolder(), targetName, ext);
        if (errorMessage!=null)
            templateWizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage); // NOI18N
        else
            templateWizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, gui.getErrorMessage()); //NOI18N
        
        boolean valid = gui.isPanelValid() && errorMessage == null;

        if (valid && targetName.indexOf(".")>=0) {
            // warning when file name contains dots
            templateWizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                NbBundle.getMessage(TargetChooserPanel.class, "MSG_dotsInName",targetName+"."+ext));
        }
        return valid;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    protected void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings( Object settings ) {
        
        templateWizard = (TemplateWizard)settings;
        
        if ( gui != null ) {
            
            Project project = Templates.getProject( templateWizard );
            
            // Try to preselect a folder
            // XXX The test should be rewritten if external project dirs are supported
            
            FileObject preselectedTarget = Templates.getTargetFolder( templateWizard );
        
            // Init values
            gui.initValues( project, Templates.getTemplate( templateWizard ), preselectedTarget );
            
            templateWizard.putProperty ("NewFileWizard_Title", // NOI18N
              NbBundle.getMessage(TargetChooserPanel.class, "TITLE_JMXFile")); // NOI18N
        }
    }

    public void storeSettings(Object settings) {
        if ( WizardDescriptor.PREVIOUS_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        if ( WizardDescriptor.CANCEL_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        if( isValid() ) {
            File f = new File(gui.getCreatedFilePath());
            File ff = new File(f.getParentFile().getPath());
            if ( !ff.exists() ) {
                try {
                    FileUtil.createFolder(ff);
                } catch (IOException exc) {
                    Logger.getLogger("global").log(Level.INFO, null, exc);
                }
            }
            FileObject folder = FileUtil.toFileObject(ff);                

            Templates.setTargetFolder( (WizardDescriptor)settings, folder );
            Templates.setTargetName( (WizardDescriptor)settings, gui.getTargetName() );
        }
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }
}
