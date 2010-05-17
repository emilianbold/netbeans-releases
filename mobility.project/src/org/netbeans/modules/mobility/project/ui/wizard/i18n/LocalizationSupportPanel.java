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

package org.netbeans.modules.mobility.project.ui.wizard.i18n;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import java.util.StringTokenizer;
import org.openide.ErrorManager;
import java.io.IOException;

/**
 *
 * @author  breh
 */
public final class LocalizationSupportPanel implements WizardDescriptor.Panel, ChangeListener {
    
    
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private LocalizationSupportPanelGUI gui;
    private TemplateWizard templateWizard = null;
    
    public Component getComponent() {
        if (gui == null) {
            gui = new LocalizationSupportPanelGUI();
            gui.addChangeListener(this);
        }
        return gui;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(LocalizationSupportPanel.class);
    }
    
    
    public void addChangeListener(final ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(final ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for (final ChangeListener lit:listeners) {
            lit.stateChanged(e);
        }
    }
    
    public void readSettings( final Object settings ) {
        
        templateWizard = (TemplateWizard)settings;
        
        if (getComponent() != null ) {
            
            final Project project = Templates.getProject( templateWizard );
            
            // Try to preselect a folder
            // XXX The test should be rewritten if external project dirs are supported
            
            final FileObject preselectedTarget = Templates.getTargetFolder( templateWizard );
            
            // Init values
            gui.initValues( project, Templates.getTemplate( templateWizard ), preselectedTarget );
        }
    }
    
    
    private static String getPackage(final FileObject folder) {
        final ClassPath cp = ClassPath.getClassPath(folder,ClassPath.SOURCE);
        if (cp != null) {
            return cp.getResourceName(folder, '.',false);
        }
        return "";  // NOI18N
    }
    
    private static String getFullMessageResourceName(final String messageFilename, final String packageName) {
        final StringBuffer fullResourceName = new StringBuffer("/");      // NOI18N
        if ((packageName!=null) && (!packageName.equals(""))) {     // NOI18N
            fullResourceName.append(packageName.replace('.','/'));  // NOI18N
            fullResourceName.append('/');                           // NOI18N
        }
        fullResourceName.append(messageFilename);
        return fullResourceName.toString();
    }
    
    
    
    public void storeSettings(final Object settings) {
        if( isValid() ) {
            
            final FileObject rootFolder = gui.getRootFolder();
            final String packageFileName = gui.getPackageFileName();
            FileObject folder = rootFolder.getFileObject(packageFileName);
            if (folder == null) {
                try {
                    folder = FileUtil.createFolder(rootFolder, packageFileName);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    return;
                }
            }
            Templates.setTargetFolder(templateWizard, folder);
            Templates.setTargetName(templateWizard, gui.getTargetClassName());
            
            final String messageResource =  getFullMessageResourceName(gui.getTargetMessageFileNameExt(),getPackage(folder));
            
            ((WizardDescriptor) settings).putProperty(LocalizationSupportIterator.MESSAGE_BUNDLE_FILENAME, gui.getTargetMessageFileName());
            ((WizardDescriptor) settings).putProperty(LocalizationSupportIterator.MESSAGE_BUNDLE_RESOURCE_NAME_KEYWORD, messageResource);
            ((WizardDescriptor) settings).putProperty(LocalizationSupportIterator.DEFAULT_STRING_KEYWORD, gui.getTargetDefaultStringValue());
            ((WizardDescriptor) settings).putProperty(LocalizationSupportIterator.DEFAULT_ERROR_KEYWORD, gui.getTargetDefaultErrorMessageValue());
            
        }
        
    }
    
    
    private void appendMessage(final StringBuffer messageBuffer, final String key) {
        final String message = NbBundle.getMessage(this.getClass(), key);
        if ((message != null) && (message.length() > 0)) {
            if (messageBuffer.length() > 0) {
                // append space before next message
                messageBuffer.append(' '); // NOI18N
            }
            messageBuffer.append(message);
        }
    }
    
    private static boolean isValidJavaFolderName(final String packageFileName) {
        if (packageFileName == null)
            return false;
        final StringTokenizer st = new StringTokenizer(packageFileName, "/"); // NOI18N
        while (st.hasMoreElements()) {
            final String s = (String) st.nextElement();
            if (! Utilities.isJavaIdentifier(s))
                return false;
        }
        return true;
    }
    
    
    public boolean isValid() {
        final StringBuffer message = new StringBuffer();
        if (gui == null) {
            appendMessage(message,"ERR_File_NoGUI"); // NOI18N
        } else {
            if (gui.getTargetClassName() == null  ||  "".equals(gui.getTargetClassName())) { // NOI18N
                appendMessage(message,"ERR_File_NoTargetClassName"); // NOI18N
            } else if (! isValidJavaFolderName(gui.getPackageFileName())) {
                appendMessage(message,"ERR_File_NoTargetFolder"); // NOI18N
            } else if (gui.getCreatedClass() != null  &&  new File(gui.getCreatedClass()).exists()) {
                appendMessage(message,"ERR_File_ClassAlreadyExists"); // NOI18N
            } else if (! Utilities.isJavaIdentifier(gui.getTargetClassName())) {
                appendMessage(message,"ERR_File_InvalidClassName"); // NOI18N
            }
            if (gui.getTargetMessageFileName() == null  ||  "".equals(gui.getTargetMessageFileName())) { // NOI18N
                appendMessage(message,"ERR_File_NoTargetMessageFileName"); // NOI18N
            }  else if (gui.getCreatedMessageFile() != null  &&  new File(gui.getCreatedMessageFile()).exists()) {
                appendMessage(message,"ERR_File_MessageResourceAlreadyExists"); // NOI18N
            }
        }
        
        
        /*
        else if (! Utilities.isJavaIdentifier (gui.getClassName ()))
            message = "ERR_File_InvalidClassName"; // NOI18N
         */
        if (templateWizard != null) {
            templateWizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message.toString()); //NOI18N
        }
        return (message.length() == 0);
    }
    
    public void stateChanged(@SuppressWarnings("unused")
	final ChangeEvent e) {
        if (templateWizard != null) {
            templateWizard.setValid(isValid());
        }
        fireChange();
    }
    
    public boolean isFinishPanel() {
        return true;
    }
}
