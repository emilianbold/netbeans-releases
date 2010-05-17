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

/*
 * Main.java
 *
 * Created on May 9, 2004, 3:39 PM
 */
package org.netbeans.modules.mobility.project.ui.wizard.i18n;

import java.io.*;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.mobility.project.ui.wizard.*;
import org.openide.loaders.*;
import org.openide.*;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.netbeans.api.java.classpath.ClassPath;
import java.text.MessageFormat;
import org.openide.filesystems.*;
import java.util.*;

/**
 *
 * @author  Breh
 */
public class LocalizationSupportIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = -1789843546389L;
    
    
    protected static final String JAVA_EXTENSION = "java"; // NOI18N
    
    /** Singleton instance of FileWizardIterator, should it be ever needed.
     */
    protected static LocalizationSupportIterator instance;
    
    /** Index of the current panel. Panels are numbered from 0 to PANEL_COUNT - 1.
     */
    protected transient int panelIndex = 0;
    
    protected transient WizardDescriptor.Panel[] panels;
    
    protected transient TemplateWizard wizardInstance;
        
    /** Returns FileWizardIterator singleton instance. This method is used
     * for constructing the instance from filesystem.attributes.
     */
    public static synchronized LocalizationSupportIterator singleton() {
        if (instance == null) {
            instance = new LocalizationSupportIterator();
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
    
    public Set<DataObject> instantiate(final TemplateWizard wiz) throws IOException, IllegalArgumentException {
        
        InputStreamReader isr = null;
        OutputStreamWriter osw = null;
        FileLock lock = null;
        
        wizardInstance = wiz;
        
        // get information from wizard
        final String msgFileName = (String) wizardInstance.getProperty(MESSAGE_BUNDLE_FILENAME);
        final String msgResourceName = (String) wizardInstance.getProperty(MESSAGE_BUNDLE_RESOURCE_NAME_KEYWORD);
        final String defString = (String) wizardInstance.getProperty(DEFAULT_STRING_KEYWORD);
        final String defErrorMsg = (String) wizardInstance.getProperty(DEFAULT_ERROR_KEYWORD);
        
        
        final Set<DataObject> aSet = new HashSet<DataObject>();
        
        
        // create files
        final DataObject javaDO = instantiateTemplate(wiz.getTemplate(), wiz.getTargetFolder(), wiz.getTargetName(), true);
        final FileObject packageFO = wiz.getTemplate().getPrimaryFile().getParent();
        final DataObject resourceTemplateDO = DataObject.find(packageFO.getFileObject("LocalizationSupport","properties"));   // NOI18N
        // property file
        final DataObject resourceDO = instantiateTemplate(resourceTemplateDO, wiz.getTargetFolder(), msgFileName, false);
        aSet.add(javaDO);
        aSet.add(resourceDO);
        
        // replace strings in the class file
        try {
            
            final FileObject javaFO = javaDO.getPrimaryFile();
            // now the file
            isr = new InputStreamReader(javaFO.getInputStream());
            final char[] buff = new char[40];
            final StringBuffer sbf = new StringBuffer();
            int count = 0;
            while ((count = isr.read(buff)) >= 0) {
                sbf.append(buff,0,count);
            }
            isr.close();
            isr = null;
            
            // now replace my things
            String s = sbf.toString();
            s = s.replaceAll(MESSAGE_BUNDLE_RESOURCE_NAME_KEYWORD,msgResourceName);
            s = s.replaceAll(DEFAULT_STRING_KEYWORD, defString);
            s = s.replaceAll(DEFAULT_ERROR_KEYWORD, defErrorMsg);
            
            // now write it to disk
            lock = javaFO.lock();
            osw = new OutputStreamWriter(javaFO.getOutputStream(lock));
            osw.write(s);
        } finally {
            IOException ioex = null;
            try {
                if (isr != null) {
                    isr.close();
                }
            } catch (IOException ioe) {
                ioex = ioe;
            }
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException ioe) {
                ioex = ioe;
            }
            if (lock != null) {
                lock.releaseLock();
            }
            if (ioex != null) {
                throw ioex;
            }
        }
        // done
        
        // now what about to open the file ...
        
        return aSet;
    }
    
    
    public static final String MESSAGE_BUNDLE_FILENAME="MESSAGE_BUNDLE_FILENAME"; // NOI18N
    public static final String MESSAGE_BUNDLE_RESOURCE_NAME_KEYWORD="__MESSAGE_BUNDLE_RESOURCE_NAME__"; // NOI18N
    public static final String DEFAULT_STRING_KEYWORD="__DEFAULT_STRING__"; // NOI18N
    public static final String DEFAULT_ERROR_KEYWORD="__DEFAULT_ERROR__"; // NOI18N
    
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
                new LocalizationSupportPanel()
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
    
    private DataObject instantiateTemplate(final DataObject tpl, final DataFolder target, String name, final boolean isJava) throws IOException {
        if (name == null) {
            name = getDefaultName(tpl, target);
        }
        
        checkValidPackageName(target);
        if (isJava) {
            checkTargetName(name);
        }
        return tpl.createFromTemplate(target, name);
    }
    
    
    private boolean isValidPackageName(final String s) {
        // valid package is an empty one, or well-formed java identifier :-)
        if ("".equals(s)) // NOI18N
            return true;
	return Utilities.isJavaIdentifier(s);
    }
    
    private void checkValidPackageName(final DataFolder targetFolder)
    throws IllegalStateException {
        final FileObject folder = targetFolder.getPrimaryFile();
        final ClassPath cp = ClassPath.getClassPath(folder,ClassPath.SOURCE);
        String msg = null;
        if (cp != null) {
            final String fullTarget = cp.getResourceName(folder, '.',false);
            if (isValidPackageName(fullTarget)) {
                return;
            } 
            msg = MessageFormat.format(getString("ERR_File_IllegalFolderName"), // NOI18N
                    new Object[] {
                folder.getPath(),
                fullTarget});
        } else {
            msg = getString("ERR_File_NotInSourcePath"); // NOI18N
        }
        // checking for java-compatible name - both the folder name and the target name
        // must be acceptable.
        throw (IllegalStateException)ErrorManager.getDefault().annotate(
                new IllegalStateException(msg),
                ErrorManager.USER, null, msg,
                null, null);
    }
    
    /**
     * @param folder target folder for java file
     * @param desiredName name to check
     * @return true if the desiredName is OK
     */
    private boolean checkTargetName(final String desiredName) {
        if (!Utilities.isJavaIdentifier(desiredName)) {
            final String msg = MessageFormat.format(getString("ERR_File_IllegalTargetName"), // NOI18N
                    new Object[] {
                desiredName
            });
            notifyError(msg);
            return false;
        }
        
        return true;
    }
    
    private void notifyError(final String msg) {
        wizardInstance.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg); //NOI18N
        final IllegalStateException ex = new IllegalStateException(msg);
        ErrorManager.getDefault().annotate(ex, ErrorManager.USER, null, msg, null, null);
        throw ex;
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
            NbBundle.getMessage(LocalizationSupportIterator.class, "TITLE_File"), // NOI18N
        }); // NOI18N
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(panelIndex)); // NOI18N
    }
    
}
