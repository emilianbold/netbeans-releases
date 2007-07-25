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

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * This class does the actual refactoring changes for one form - updates the
 * form, regenerates code, updates properties files for i18n, etc. Multiple
 * different instances (updates) can be created and executed for one refactoring
 * (all kept in RefacoringInfo).
 * 
 * @author Tomas Pavek
 */
public class FormRefactoringUpdate extends SimpleRefactoringElementImplementation implements Transaction {

    /**
     * Information about the performed refactoring.
     */
    private RefactoringInfo refInfo;

    /**
     * RefactoringElement used in the preview, but doing nothing.
     */
    private RefactoringElementImplementation previewElement;

    /**
     * Java file of a form affected by the refactoring.
     */
    private FileObject changingFile;

    /**
     * DataObject of the changed file. Has changedFile as primary file at the
     * beginning, but may get a different one later (e.g. if moved).
     */
    private FormDataObject formDataObject;

    /**
     * FormEditor of the updated form. Either taken from the FormDataObject
     * (typically when already opened), or created temporarily just to do the
     * update. See prepareForm method.
     */
    private FormEditor formEditor;

    private boolean loadingFailed;

    /**
     * Whether a change in guarded code was requested by java refactoring.
     */
    private boolean guardedCodeChanging;

    private boolean transactionDone;

    private boolean formFileRenameDone;

    private List<RefactoringElementImplementation> preFileChanges;

    private List<BackupFacility.Handle> backups;

    // -----

    public FormRefactoringUpdate(RefactoringInfo refInfo, FileObject changingFile) {
        this.refInfo = refInfo;
        this.changingFile = changingFile;
        try {
            DataObject dobj = DataObject.find(changingFile);
            if (dobj instanceof FormDataObject) {
                formDataObject = (FormDataObject) dobj;
            }
        } catch(DataObjectNotFoundException ex) {
            assert false;
        }
    }

    FormDataObject getFormDataObject() {
        return formDataObject;
    }

    RefactoringElementImplementation getPreviewElement(/*String displayText*/) {
        if (previewElement == null) {
            previewElement = new PreviewElement(changingFile/*, displayText*/);
        }
        return previewElement;
    }

    void setGaurdedCodeChanging(boolean b) {
        guardedCodeChanging = b;
    }

    boolean isGuardedCodeChanging() {
        return guardedCodeChanging;
    }

    public void addPrecedingFileChange(RefactoringElementImplementation change) {
        if (preFileChanges == null) {
            preFileChanges = new LinkedList<RefactoringElementImplementation>();
        }
        preFileChanges.add(change);
    }

    // -----

    // Transaction (registered via RefactoringElementsBag.registerTransaction)
    public void commit() {
        if (previewElement != null && !previewElement.isEnabled()) {
            return;
        }

        // As "transactions" we do updates for changes affecting only the
        // content of the source file, not changing the file's name or location.
        // Our transaction is called after retouche commits its changes to the
        // source. After all transactions are done, the source file is saved
        // automatically.

        switch (refInfo.getChangeType()) {
        case VARIABLE_RENAME:
            renameMetaComponent(refInfo.getOldName(), refInfo.getNewName());
            transactionDone = true;
            break;
        case CLASS_RENAME: // renaming a component class used in the form
            if (!refInfo.getPrimaryFile().equals(changingFile)) {
                componentClassRename(refInfo.getOldName(), refInfo.getNewName());
                transactionDone = true;
            }
            break;
        case CLASS_MOVE: // moving a component class used in the form
            if (!refInfo.getPrimaryFile().equals(changingFile)) {
                componentChange(refInfo.getOldName(), refInfo.getNewName());
                transactionDone = true;
            }
            break;
            //do nothing otherwise - could be just redundantly registered by the guarded handler
        }
    }

    // Transaction (registered via RefactoringElementsBag.registerTransaction)
    public void rollback() {
        if (previewElement != null && !previewElement.isEnabled()) {
            return;
        }
        undoFromBackups();
/*        switch (refInfo.getChangeType()) {
        case VARIABLE_RENAME:
            renameMetaComponent(refInfo.getNewName(), refInfo.getOldName());
            break;
        case CLASS_RENAME: // renaming a component class used in the form
            if (!refInfo.getPrimaryFile().equals(changingFile)) {
                componentClassRename(refInfo.getNewName(), refInfo.getOldName());
            }
            break;
        case CLASS_MOVE: // moving a component class used in the form
            if (!refInfo.getPrimaryFile().equals(changingFile)) {
                componentChange(refInfo.getNewName(), refInfo.getOldName());
            }
            break;
        } */
    }

    // RefactoringElementImplementation (registered via RefactoringElementsBag.addFileChange)
    public void performChange() {
        if (previewElement != null && !previewElement.isEnabled()) {
            return;
        }
        if (transactionDone) { // could be registered redundantly as file change
            processCustomCode();
            return;
        }

        // As "file changes" we do updates that react on changes of the source
        // file's name or location. We need the source file to be already
        // renamed/moved. The file changes are run after the "transactions".

        if (preFileChanges != null) {
            for (RefactoringElementImplementation change : preFileChanges) {
                change.performChange();
            }
        }

        switch (refInfo.getChangeType()) {
        case CLASS_RENAME: // renaming the form itself
            if (refInfo.getPrimaryFile().equals(changingFile)) {
                formRename();
            }
            break;
        case CLASS_MOVE: // moving the form itself
            if (refInfo.getPrimaryFile().equals(changingFile) && prepareForm(false)) {
                formMove();
            }
            break;
        case PACKAGE_RENAME:
        case FOLDER_RENAME:
            packageRename();
            break;
        }

        processCustomCode();
    }

    // RefactoringElementImplementation (registered via RefactoringElementsBag.addFileChange)
    public void undoChange() {
        if (previewElement != null && !previewElement.isEnabled()) {
            return;
        }
        if (transactionDone) { // could be registered redundantly as file change
            return;
        }

        undoFromBackups();

        if (preFileChanges != null) {
            for (RefactoringElementImplementation change : preFileChanges) {
                change.undoChange();
            }
        }
    }

    // -----

    private void renameMetaComponent(String oldName, String newName) {
        if (prepareForm(true)) {
            RADComponent metacomp = formEditor.getFormModel().findRADComponent(oldName);
            if (metacomp != null) {
                saveFormForUndo();
                saveResourcesForUndo();
                metacomp.setName(newName);
                updateForm(false);
            }
        }
    }

    private void formRename(/*boolean saveAll*/) {
        if (prepareForm(true)) {
            saveFormForUndo();
            saveResourcesForUndo();
            ResourceSupport.formRenamed(formEditor.getFormModel(), refInfo.getOldName());
            updateForm(true);
        }
    }

    private void componentClassRename(String oldName, String newName) {
        FileObject renamedFile = refInfo.getPrimaryFile();
        String pkg = ClassPath.getClassPath(renamedFile, ClassPath.SOURCE)
                .getResourceName(renamedFile.getParent(), '.', false);
        String oldClassName = (pkg != null && pkg.length() > 0)
                ? pkg + "." + oldName : oldName; // NOI18N
        String newClassName = (pkg != null && pkg.length() > 0)
                ? pkg + "." + newName : newName; // NOI18N
        componentChange(oldClassName, newClassName);
    }

    private void formMove(/*final boolean saveAll*/) {
        final FormEditorSupport fes = formDataObject.getFormEditorSupport();
        if (fes.isOpened()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    formEditor = fes.reloadFormEditor();
                    formMove2(/*saveAll*/);
                }
            });
        } else {
            assert !formEditor.isFormLoaded();
            formMove2(/*saveAll*/);
        }
    }

    private void formMove2(/*boolean saveAll*/) {
        if (prepareForm(true)) {
            saveFormForUndo();
            saveResourcesForUndo();
            ResourceSupport.formMoved(formEditor.getFormModel(), changingFile.getParent());
            updateForm(true);
        }
    }

    private void componentChange(String oldClassName, String newClassName) {
        FormEditorSupport fes = formDataObject.getFormEditorSupport();
        if (fes.isOpened()) {
            fes.closeFormEditor();
        }
        replaceClassOrPkgName(oldClassName, newClassName, false);
    }

    private void packageRename() {
        FormEditorSupport fes = formDataObject.getFormEditorSupport();
        if (fes.isOpened()) {
            fes.closeFormEditor();
        }
        if (replaceClassOrPkgName(refInfo.getOldName(), refInfo.getNewName(), true)
                && !isGuardedCodeChanging()) {
            // some package references in resource were changed in the form file
            // (not class names since no change in guarded code came from java
            // refactoring) and because no component has changed we can load the
            // form and regenerate to get the new resource names into code
            updateForm(true);
        }
    }

    /**
     * Tries to update the fragments of custom code in the .form file according
     * to the refactoring change. The implementation is quite simple and 
     * super-ugly. It goes through the form file, finds relevant attributes,
     * and blindly replaces given "old name" with a "new name". Should mostly
     * work when a component variable or class is renamed. Should be enough
     * though, since the usage of custom code is quite limited.
     */
    private void processCustomCode() {
        if (isGuardedCodeChanging() && !formFileRenameDone) {
            String oldName = refInfo.getOldName();
            String newName = refInfo.getNewName();
            if (oldName != null && newName != null) {
                boolean replaced = replaceClassOrPkgName(oldName, newName, false);
                if (replaced) {
                    final FormEditorSupport fes = formDataObject.getFormEditorSupport();
                    if (fes.isOpened()) { // need to regenerate the code
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                formEditor = fes.reloadFormEditor();
                                updateForm(true);
                            }
                        });
                    } else if (prepareForm(true)) {
                        updateForm(true);
                    }
                }
                formFileRenameDone = false; // not to block redo
            }
        }
    }

    // -----

    /**
     * Regenerate code and save.
     */
    private void updateForm(boolean saveAll) {
        if (!prepareForm(true)) {
            return;
        }
        // hack: regenerate code immediately
        formEditor.getFormModel().fireFormChanged(true);
        FormEditorSupport fes = getFormDataObject().getFormEditorSupport();
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

    boolean prepareForm(boolean load) {
        if (formEditor == null) {
            if (formDataObject != null) {
                formEditor = formDataObject.getFormEditorSupport().getFormEditor();
                if (formEditor == null) { // create a disconnected form editor
                    formEditor = new FormEditor(formDataObject);
                }
            }
        }
        if (formEditor != null) {
            if (formEditor.isFormLoaded() || !load) {
                return true;
            } else if (!loadingFailed) {
                if (formEditor.loadForm()) {
                    return true;
                } else {
                    loadingFailed = true;
                }
            }
        }
        return false;
    }

    private void saveFormForUndo() {
        saveForUndo(formDataObject.getFormFile());
        // java file is backed up by java refactoring
    }

    private void saveResourcesForUndo() {
        for (FileObject file : ResourceSupport.getAutomatedResourceFiles(formEditor.getFormModel())) {
            saveForUndo(file);
        }
    }

    private void saveForUndo(FileObject file) {
        try {
            BackupFacility.Handle id = BackupFacility.getDefault().backup(file);
            if (backups == null) {
                backups = new ArrayList<BackupFacility.Handle>();
            }
            backups.add(id);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void undoFromBackups() {
        if (backups != null) {
            try {
                for (BackupFacility.Handle id : backups) {
                    id.restore();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            backups.clear();
        }
    }

    // -----

    private static class PreviewElement extends SimpleRefactoringElementImplementation {
        private FileObject file;
//        private String displayText;

        PreviewElement(FileObject file/*, String displayText*/) {
            this.file = file;
//            this.displayText = displayText;
        }

        public String getText() {
            return "GUI form update";
        }

        public String getDisplayText() {
//            return displayText;
            return "Update GUI form and regenerate code in guarded blocks";
        }

        public void performChange() {
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public FileObject getParentFile() {
            return file;
        }

        public PositionBounds getPosition() {
            return null;
        }
    }

    // -----

    // RefactoringElementImplementation
    public String getText() {
        return "GUI form update";
    }

    // RefactoringElementImplementation
    public String getDisplayText() {
//            return displayText;
        return "Post-refactoring: Update GUI form and regenerate code in guarded blocks";
    }

    // RefactoringElementImplementation
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    // RefactoringElementImplementation
    public FileObject getParentFile() {
        return changingFile;
    }

    // RefactoringElementImplementation
    public PositionBounds getPosition() {
        return null;
    }

   // -----

    /**
     * Elements and attributes that are used to search in when trying to replace
     * a non-FQN name (identifier) in custom code of a form.
     */
    private static final String[] FORM_ELEMENTS_ATTRS = {
        "<Component ", " class=\"", // NOI18N
        "<AuxValue name=\"JavaCodeGenerator_", " value=\"", // NOI18N
        "<Property ", " preCode=\"", // NOI18N
        "<Property ", " postCode=\"", // NOI18N
        "<Connection ", " code=\"" // NOI18N
    };

    /**
     * Replace the class or package name directly in the form file. It is
     * important not to cause other diff in the file.
     * ... A provisional strawman solution using textual replace, yuck ...
     * But should work fine with the *current* form file format and with
     * fully qualified class names, also covering the user's code (though
     * users will probably not use FQN.)
     * @return whether anything was changed in teh form file
     */
    private boolean replaceClassOrPkgName(String oldName, String newName, boolean pkgName) {
        FileObject formFile = formDataObject.getFormFile();
        FileLock lock = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            lock = formFile.lock();

            String[] oldStr;
            String[] newStr;
            boolean shortName;
            if (pkgName) {
                oldName = oldName + "."; // NOI18N
                newName = newName + "."; // NOI18N
                oldStr = new String[] { oldName, oldName.replace('.', '/') };
                newStr = new String[] { newName, newName.replace('.', '/') };
                shortName = false;
            } else {
                shortName = !oldName.contains("."); // NOI18N
                oldStr = new String[] { oldName };
                newStr = new String[] { newName };
            }

            String encoding = "UTF-8"; // NOI18N
            String outString;
            is = formFile.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));
            if (!shortName) {
                // With fully qualified name we can safely do plain textual
                // search/replace over the file and get all changes covered
                // (component class name elements, custom code, property editors,
                // also icons and resource bundles if package name is changed, etc).
                NameReplacer rep = new NameReplacer(oldStr, newStr, (int)formFile.getSize());
                String line = reader.readLine();
                while (line != null) {
                    rep.append(line);
                    line = reader.readLine();
                    if (line != null) {
                        rep.append("\n"); // NOI18N
                    }
                }
                if (!rep.anythingChanged()) {
                    return false;
                }
                outString = rep.toString();
            } else {
                // The replaced name is short with no '.', so it is too risky
                // to do plain search/replace over the entire file content.
                // Search only in the specific elements and attributes.
                StringBuilder buf = new StringBuilder((int)formFile.getSize());
                boolean anyChange = false;
                String line = reader.readLine();
                while (line != null) {
                    String trimLine = line.trim();
                    for (int i=0; i < FORM_ELEMENTS_ATTRS.length; i+=2) {
                        if (trimLine.startsWith(FORM_ELEMENTS_ATTRS[i])) {
                            String attr = FORM_ELEMENTS_ATTRS[i+1];
                            int idx = line.indexOf(attr);
                            if (idx > 0) {
                                // get the value of the attribute - string enclosed in ""
                                int idx1 = idx1 = idx + attr.length();
                                if (!attr.endsWith("\"")) { // NOI18N
                                    while (idx1 < line.length() && line.charAt(idx1) != '\"') { // NOI18N
                                        idx1++;
                                    }
                                    idx1++;
                                }
                                int idx2 = idx1;
                                while (idx2 < line.length() && line.charAt(idx2) != '\"') { // NOI18N
                                    idx2++;
                                }
                                if (idx1 < line.length() && idx2 < line.length()) {
                                    String sub = line.substring(idx1, idx2);
                                    if (sub.contains(oldName)) {
                                        NameReplacer rep = new NameReplacer(oldStr, newStr, idx2 - idx1);
                                        rep.append(sub);
                                        if (rep.anythingChanged()) {
                                            line = line.substring(0, idx1) + rep.toString() + line.substring(idx2);
                                            anyChange = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    buf.append(line);
                    line = reader.readLine();
                    if (line != null) {
                        buf.append("\n"); // NOI18N
                    }
                }
                if (!anyChange) {
                    return false;
                }
                outString = buf.toString();
            }

            saveForUndo(formFile);

            is.close();
            is = null;

            os = formFile.getOutputStream(lock);
            os.write(outString.getBytes(encoding));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) { // ignore
            }
            lock.releaseLock();
        }
        formFileRenameDone = true; // we don't need to do processCustomCode
        return true;
    }

    private static class NameReplacer {
        private String[] toReplace;
        private String[] replaceWith;
        private int[] matchCounts;

        private StringBuilder buffer;
        private StringBuilder pendingChars;
        private char lastChar;

        private boolean anyChange;

        public NameReplacer(String[] toReplace, String[] replaceWith, int len) {
            this.toReplace = toReplace;
            for (String s : toReplace) {
                assert s != null && s.length() > 0;
            }
            for (String s : replaceWith) {
                assert s != null && s.length() > 0;
            }
            this.replaceWith = replaceWith;
            this.pendingChars = new StringBuilder(50);
            this.buffer = new StringBuilder(len);
            matchCounts = new int[toReplace.length];
        }

        public void append(String str) {
            for (int i=0; i < str.length(); i++) {
                append(str.charAt(i));
            }
        }

        public String toString() {
            for (int i=0; i < toReplace.length; i++) {
               String template = toReplace[i];
               int count = matchCounts[i];
               if (count == template.length()) {
                   replace(i);
                   break;
               }
            }
            writePendingChars();
            return buffer.toString();
        }

        public boolean anythingChanged() {
            return anyChange;
        }

        private void append(char c) {
            int completeMatch = -1; // index of template string
            boolean charMatch = false;
            for (int i=0; i < toReplace.length; i++) {
               String template = toReplace[i];
               int count = matchCounts[i];
               if (count == template.length()) {
                   if (canEndHere(c)) { // so the name is not just a subset of a longer name
                       completeMatch = i;
                       break;
                   } else {
                       matchCounts[i] = 0;
                       continue;
                   }
               }
               if (template.charAt(count) == c) {
                   if (count > 0 || canStartHere()) { // not to start in the middle of a longer name
                       matchCounts[i] = count+1;
                       charMatch = true;
                   }
               } else {
                   matchCounts[i] = 0;
               }
            }

            if (completeMatch >= 0) {
                replace(completeMatch);
                buffer.append(c); // the first char after can't match (names can't follow without a gap)
            } else if (charMatch) {
                pendingChars.append(c);
            } else {
                writePendingChars();
                buffer.append(c);
            }

            lastChar = c;
        }

        private boolean canStartHere() {
            return lastChar != '.' && lastChar != '/'
                   && (lastChar <= ' ' || !Character.isJavaIdentifierPart(lastChar));
                   // surprisingly 0 is considered as valid char
        }

        private boolean canEndHere(char next) {
            return lastChar == '.' || lastChar == '/'
                   || !Character.isJavaIdentifierPart(next);
        }

        private void replace(int completeMatch) {
            int preCount = pendingChars.length() - matchCounts[completeMatch];
            if (preCount > 0) {
                buffer.append(pendingChars.substring(0, preCount));
            }
            buffer.append(replaceWith[completeMatch]);
            for (int i=0; i < matchCounts.length; i++) {
                matchCounts[i] = 0;
            }
            pendingChars.delete(0, pendingChars.length());
            anyChange = true;
        }

        private void writePendingChars() {
            if (pendingChars.length() > 0) {
                buffer.append(pendingChars.toString());
                pendingChars.delete(0, pendingChars.length());
            }
        }
    }
}
