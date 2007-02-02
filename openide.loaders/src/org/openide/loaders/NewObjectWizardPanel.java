 /*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;


import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.*;

/** Implementaion of WizardDescriptor.Panel that can be used in create from template.
 *
 * @author Jiri Rechtacek
 */
final class NewObjectWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private NewObjectPanel newObjectPanelUI;
    /** listener to changes in the wizard */
    private ChangeListener listener;
    /** a folder in which will be new object created */
    DataFolder targetFolder;
    /** File extension of the template and of the created file -
     * it is used to test whether file already exists.
     */
    private String extension;
    
    private TemplateWizard wizard;
    
    private NewObjectPanel getPanelUI () {
        if (newObjectPanelUI == null) {
            newObjectPanelUI = new NewObjectPanel ();
            newObjectPanelUI.addChangeListener (listener);
        }
        return newObjectPanelUI;
    }
    
    /** Add a listener to changes of the panel's validity.
    * @param l the listener to add
    * @see #isValid
    */
    public void addChangeListener (ChangeListener l) {
        if (listener != null) throw new IllegalStateException ();
        if (newObjectPanelUI != null)
            newObjectPanelUI.addChangeListener (l);
        listener = l;
    }

    /** Remove a listener to changes of the panel's validity.
    * @param l the listener to remove
    */
    public void removeChangeListener (ChangeListener l) {
        listener = null;
        if (newObjectPanelUI != null)
            newObjectPanelUI.removeChangeListener (l);
    }

    /** Get the component displayed in this panel.
     *
     * Note; method can be called from any thread, but not concurrently
     * with other methods of this interface.
     *
     * @return the UI component of this wizard panel
     *
     */
    public java.awt.Component getComponent() {
        return getPanelUI ();
    }
    
    /** Help for this panel.
    * @return the help or <code>null</code> if no help is supplied
    */
    public org.openide.util.HelpCtx getHelp () {
        return new HelpCtx (NewObjectPanel.class);
    }

    /** Test whether the panel is finished and it is safe to proceed to the next one.
    * If the panel is valid, the "Next" (or "Finish") button will be enabled.
    * <p>The Attribute "isRemoteAndSlow" will be checked for on the targetFolder,
    * and if it is found and <b>true</b>, then 
    * targetFolder.getPrimaryFile().getFileObject will NOT be called.</p>
    * @return <code>true</code> if the user has entered satisfactory information
    */
    public boolean isValid () {
        String errorMsg = null;
        boolean isOK = true;
        // target filesystem should be writable
        if (!targetFolder.getPrimaryFile ().canWrite ()) {
            errorMsg = NbBundle.getMessage(TemplateWizard2.class, "MSG_fs_is_readonly");
            isOK = false;
        }
        if (isOK) {
            Object obj = targetFolder.getPrimaryFile().getAttribute( "isRemoteAndSlow" );//NOI18N
            boolean makeFileExistsChecks = true;
            if( obj instanceof Boolean )
                makeFileExistsChecks = ! ((Boolean) obj).booleanValue();
            if( makeFileExistsChecks ) {
                // test whether the selected name already exists            
                FileObject f = targetFolder.getPrimaryFile().getFileObject(getPanelUI ().getNewObjectName (), extension);
                if (f != null) {
                    errorMsg = NbBundle.getMessage(TemplateWizard2.class, "MSG_file_already_exist", f.getNameExt()); //NOI18N
                    isOK = false;
                }
                if ((Utilities.isWindows () || (Utilities.getOperatingSystem () == Utilities.OS_OS2))) {
                    if (TemplateWizard.checkCaseInsensitiveName (targetFolder.getPrimaryFile (), getPanelUI ().getNewObjectName (), extension)) {
                        errorMsg = NbBundle.getMessage(TemplateWizard2.class, "MSG_file_already_exist", getPanelUI ().getNewObjectName ()); // NOI18N
                        isOK = false;
                    }
                }
                
            }
        }
        wizard.putProperty("WizardPanel_errorMessage", errorMsg);//NOI18N
        return isOK;
    }
    
    /** Provides the wizard panel with the current data--either
     * the default data or already-modified settings, if the user used the previous and/or next buttons.
     * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
     * @param settings the object representing wizard panel state, as originally supplied to {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}
     */
    public void readSettings(WizardDescriptor settings) {
        this.wizard = (TemplateWizard)settings;
        DataObject template = wizard.getTemplate ();
        if (template != null) {
            extension = template.getPrimaryFile().getExt();
        }
        
        try {
            targetFolder = wizard.getTargetFolder();
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
        }

    }
    
    /** Provides the wizard panel with the opportunity to update the
     * settings with its current customized state.
     * Rather than updating its settings with every change in the GUI, it should collect them,
     * and then only save them when requested to by this method.
     * Also, the original settings passed to {@link #readSettings} should not be modified (mutated);
     * rather, the (copy) passed in here should be mutated according to the collected changes.
     * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
     * @param settings the object representing a settings of the wizard
     */
    public void storeSettings(WizardDescriptor settings) {
        String name = getPanelUI ().getNewObjectName ();
        if (name.equals (NewObjectPanel.defaultNewObjectName ())) {
            name = null;
        }
        if (wizard != null) {
            wizard.setTargetName (name);
            wizard = null;
        }
        
    }
    
    public boolean isFinishPanel () {
        return true;
    }
    
}
