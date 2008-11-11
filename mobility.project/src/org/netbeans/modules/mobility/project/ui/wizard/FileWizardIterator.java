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

package org.netbeans.modules.mobility.project.ui.wizard;
import java.text.MessageFormat;
import java.util.NoSuchElementException;

import javax.swing.event.ChangeListener;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;

import org.netbeans.api.java.classpath.ClassPath;

import java.io.IOException;
import javax.swing.*;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Utilities;

public class FileWizardIterator implements TemplateWizard.Iterator {
    private static final long serialVersionUID = -1987345825459L;
    
    protected static final String JAVA_EXTENSION = "java"; // NOI18N
    
    /** Singleton instance of FileWizardIterator, should it be ever needed.
     */
    protected static FileWizardIterator instance;
    
    /** Index of the current panel. Panels are numbered from 0 to PANEL_COUNT - 1.
     */
    protected transient int panelIndex = 0;
    
    protected transient WizardDescriptor.Panel[] panels;
    
    protected transient TemplateWizard wizardInstance;
    
    /** Returns FileWizardIterator singleton instance. This method is used
     * for constructing the instance from filesystem.attributes.
     */
    public static synchronized FileWizardIterator singleton() {
        if (instance == null) {
            instance = new FileWizardIterator();
        }
        return instance;
    }
    // ========================= TemplateWizard.Iterator ============================
    
    /** Instantiates the template using informations provided by
     * the wizard.
     *
     * @param wiz the wizard
     * @return set of data objects that has been created (should contain
     *  at least one
     * @exception IOException if the instantiation fails
     */
    public java.util.Set<DataObject> instantiate(final TemplateWizard wiz) throws IOException, IllegalArgumentException {
        wizardInstance = wiz;
        final DataObject obj = instantiateTemplate(wiz.getTemplate(), wiz.getTargetFolder(), wiz.getTargetName());
        
        final Object isMidletObject = Templates.getTemplate(wizardInstance).getAttribute(MIDPTargetChooserPanelGUI.IS_MIDLET_TEMPLATE_ATTRIBUTE); // NOI18N
        boolean isMIDlet = false;
        if (isMidletObject instanceof Boolean)
            isMIDlet = ((Boolean) isMidletObject).booleanValue();
        
        if (isMIDlet) {
            final Project p = Templates.getProject(wizardInstance);
            final AntProjectHelper h = p.getLookup().lookup(AntProjectHelper.class);
            if (p instanceof J2MEProject  &&  h != null) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            J2MEProjectGenerator.addMIDletProperty(p, h,
                                    (String) wiz.getProperty(MIDPTargetChooserPanel.MIDLET_NAME),
                                    (String) wiz.getProperty(MIDPTargetChooserPanel.MIDLET_CLASSNAME),
                                    (String) wiz.getProperty(MIDPTargetChooserPanel.MIDLET_ICON)
                                    );
                            ProjectManager.getDefault().saveProject(p);
                        } catch (IOException e) {
                            e.printStackTrace(); // TODO
                        }
                    }
                });
            }
        }
        return java.util.Collections.singleton(obj);
    }
    
    public WizardDescriptor.Panel current() {
        return panels[panelIndex];
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public boolean hasNext() {
        return false;
    }
    
    public boolean hasPrevious() {
        return false;
    }
    
    public void nextPanel() {
        throw new NoSuchElementException();
    }
    
    public void previousPanel() {
        throw new NoSuchElementException();
    }
    
    /** Add a listener to changes of the current panel.
     * The listener is notified when the possibility to move forward/backward changes.
     * @param l the listener to add
     */
    public void addChangeListener(@SuppressWarnings("unused")
	final ChangeListener l) {
    }
    
    /** Remove a listener to changes of the current panel.
     * @param l the listener to remove
     */
    public void removeChangeListener(@SuppressWarnings("unused")
	final ChangeListener l) {
    }
    
    public void initialize(final TemplateWizard wizard) {
        wizardInstance = wizard;
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new MIDPTargetChooserPanel(),
            };
        }
        panelIndex = 0;
        updateStepsList();
    }
    
    public void uninitialize(@SuppressWarnings("unused")
	final TemplateWizard wiz) {
        wizardInstance = null;
        panels = null;
        panelIndex = -1;
    }
    
    // ========================= IMPLEMENTATION ============================
    
    /** Instantiates the template. Currently it just delegates to the template DataObject's
     * createFromTemplate method.
     */
    private DataObject instantiateTemplate(final DataObject tpl, final DataFolder target, String name) throws IOException {
        if (name == null) {
            name = getDefaultName(tpl, target);
        }
        
        String message;
        message = checkValidPackageName(target);
        if (message == null)
            message = checkTargetName(target, name);
        if (message != null)
            throw (IllegalStateException)ErrorManager.getDefault().annotate(
                    new IllegalStateException(message),
                    ErrorManager.USER, null, message,
                    null, null);
        
        return tpl.createFromTemplate (target, name);
    }
    
    private static boolean isValidPackageName(final String s) {
        // valid package is an empty one, or well-formed java identifier :-)
        if ("".equals(s)) // NOI18N
            return true;
        try {
            Utilities.isJavaIdentifier(s);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
    
    public static String checkValidPackageName(final DataFolder targetFolder)
    throws IllegalStateException {
        final FileObject folder = targetFolder.getPrimaryFile();
        final ClassPath cp = ClassPath.getClassPath(folder,ClassPath.SOURCE);
        String msg = null;
        if (cp != null) {
            final String fullTarget = cp.getResourceName(folder, '.',false);
            if (isValidPackageName(fullTarget)) {
                return null;
            } 
            msg = MessageFormat.format(getString("ERR_File_IllegalFolderName"), // NOI18N
                    new Object[] {  folder.getPath(),
					                fullTarget});
        } else {
            msg = getString("ERR_File_NotInSourcePath"); // NOI18N
        }
        return msg;
        // checking for java-compatible name - both the folder name and the target name
        // must be acceptable.
    }
    
    /**
     * @param folder target folder for java file
     * @param desiredName name to check
     * @return true if the desiredName is OK
     */
    public static String checkTargetName(final DataFolder folder, final String desiredName) {
        if (!Utilities.isJavaIdentifier(desiredName)) {
            final String msg = MessageFormat.format(getString("ERR_File_IllegalTargetName"), // NOI18N
                    new Object[] {
                desiredName
            });
            return msg;
        }
        
        final FileObject f = folder.getPrimaryFile();
        // check whether the name already exists:
        if (f.getFileObject(desiredName, JAVA_EXTENSION) != null) {
            final String msg = MessageFormat.format(getString("ERR_File_TargetExists"), // NOI18N
                    new Object[] {
                desiredName
            });
            return msg;
        }
        return null;
    }
    
    private String getDefaultName(final DataObject template, final DataFolder targetFolder) {
        final String desiredName = org.openide.filesystems.FileUtil.findFreeFileName(targetFolder.getPrimaryFile(),
                template.getName(), JAVA_EXTENSION);
        return desiredName;
    }
    
    static String getString(final String key) {
        return NbBundle.getMessage(FileWizardIterator.class, key);
    }
    
    static char getMnemonic(final String key) {
        return getString(key).charAt(0);
    }
    
    private void updateStepsList() {
        final JComponent component = (JComponent) current().getComponent();
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[] {// NOI18N
            NbBundle.getMessage(MIDPTargetChooserPanel.class, "TITLE_File"), // NOI18N
        }); // NOI18N
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(panelIndex)); // NOI18N
    }
    
}
