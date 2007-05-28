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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * This class does the actual refactoring changes for one form - updates the
 * form, regenerates code, updates properties file.
 * 
 * @author Tomas Pavek
 */
public class FormRefactoringUpdate implements Transaction {

    private RefactoringInfo refInfo;
    private FileObject changedFile;
    private FormEditor formEditor;

    public FormRefactoringUpdate(RefactoringInfo refInfo, FileObject changedFile) {
        this.refInfo = refInfo;
        this.changedFile = changedFile;
    }

    public void commit() {
        switch (refInfo.getChangeType()) {
        case VARIABLE_RENAME:
            renameMetaComponent();
            break;
        case CLASS_RENAME:
            renameClass();
            break;
        }
    }

    public void rollback() {
    }

    private void renameMetaComponent() {
//        FormEditor formEditor = loadForm(changedFile);
        if (formEditor != null) {
//            System.out.println("...updating form...");
            RADComponent metacomp = formEditor.getFormModel().findRADComponent(refInfo.getOldName());
            if (metacomp != null) {
                metacomp.setName(refInfo.getNewName());
                updateForm(false);
            }
        }
    }

    private void renameClass() {
        if (refInfo.getPrimaryFile().equals(changedFile)) {
            // the form has been renamed
            if (formEditor != null) { // needs to be regenerated (use of MyForm.this)
                if (formEditor.getFormDataObject().getName().equals(refInfo.getNewName())) {
                    renameForm(false);
                } else { // dataobject not renamed yet...
                    formEditor.getFormDataObject().addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent ev) {
                            if (DataObject.PROP_NAME.equals(ev.getPropertyName())) {
                                renameForm(true);
                                formEditor.getFormDataObject().removePropertyChangeListener(this);
                            }
                        }
                    });
                }
            }
        } else { // a component class used in this form has been renamed
            // TBD
        }
    }

    private void renameForm(boolean saveAll) {
        ResourceSupport.formRenamed(formEditor.getFormModel(), refInfo.getOldName());
        updateForm(saveAll);
    }

    private void updateForm(boolean saveAll) {
        // hack: regenerate code immediately (there might be no change)
        formEditor.getFormModel().fireFormChanged(true);
        FormEditorSupport fes = formEditor.getFormDataObject().getFormEditorSupport();
        try {
            if (!fes.isOpened()) {
                // the form is not opened, just loaded aside to do this refactoring
                // update (not held from FormEditorSupport); so we must save the
                // form always - it would not get save with refactoring
                formEditor.saveFormData(); // TODO should save form only if there was a change
                if (saveAll) { // a post-refactoring change that would not be saved by refactoring
                    fes.saveSourceOnly();
                }
                formEditor.closeForm();
            } else if (saveAll) { // a post-refactoring change that would not be saved
                fes.saveDocument();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    boolean loadForm() {
        FormDataObject formDataObject = null;
        try {
            DataObject dobj = DataObject.find(changedFile);
            if (dobj instanceof FormDataObject) {
                formDataObject = (FormDataObject) dobj;
            }
        } catch(DataObjectNotFoundException ex) {
        }
        if (formDataObject != null) {
            formEditor = formDataObject.getFormEditorSupport().getFormEditor();
            if (formEditor == null) {
                formEditor = new FormEditor(formDataObject);
            }
            if (!formEditor.isFormLoaded()) {
                formEditor.loadForm();
            }
            if (!formEditor.isFormLoaded()) {
                formEditor = null; // some error
            }
        }
        return formEditor != null;
    }
}
