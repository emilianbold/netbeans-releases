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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * This class does the actual refactoring changes for one form - updates the
 * form, regenerates code, updates properties file.
 * 
 * @author Tomas Pavek
 */
public class FormRefactoringUpdate extends SimpleRefactoringElementImplementation implements Transaction {

    private RefactoringInfo refInfo;

    private RefactoringElementImplementation previewElement;

    private FileObject changingFile;

    private FormDataObject formDataObject; // has the changedFile as primary file at the beginning, but may get a different one later (e.g. if moved)

    private FormEditor formEditor;

    private boolean loadingFailed;

    private boolean guardedCodeChanging; // whether a change in guarded code is requested by java refactoring

    private boolean transactionDone;

    private List<RefactoringElementImplementation> preFileChanges;
//    private List<Transaction> preTransactions;
//    private List<Transaction> postTransactions;

    public FormRefactoringUpdate(RefactoringInfo refInfo, FileObject changedFile) {
        this.refInfo = refInfo;
        this.changingFile = changedFile;
        try {
            DataObject dobj = DataObject.find(changedFile);
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
//    public void addAdditionalTransaction(Transaction t, boolean before) {
//        if (before) {
//            if (preTransactions == null) {
//                preTransactions = new LinkedList<Transaction>();
//            }
//            preTransactions.add(t);
//        } else {
//            if (postTransactions == null) {
//                postTransactions = new LinkedList<Transaction>();
//            }
//            postTransactions.add(t);
//        }
//    }

    // Transaction
    public void commit() {
//        if (preTransactions != null) {
//            for (Transaction t : preTransactions) {
//                t.commit();
//            }
//        }

        switch (refInfo.getChangeType()) {
        case VARIABLE_RENAME:
            renameMetaComponent();
            transactionDone = true;
            break;
        case CLASS_RENAME:
            if (!refInfo.getPrimaryFile().equals(changingFile)) {
                componentClassRename();
                transactionDone = true;
            }
//            classRename();
            break;
        case CLASS_MOVE:
            if (!refInfo.getPrimaryFile().equals(changingFile)) {
                componentChange(refInfo.getOldName(), refInfo.getNewName());
                transactionDone = true;
            }
//            classMove();
            break;
//        case PACKAGE_RENAME:
//        case FOLDER_RENAME:
//            packageRename();
//            break;
        }

//        if (postTransactions != null) {
//            for (Transaction t : postTransactions) {
//                t.commit();
//            }
//        }
    }

    // Transaction
    public void rollback() {
    }

    // RefactoringElementImplementation
    public void performChange() {
        if (transactionDone) {
            return;
        }

        if (preFileChanges != null) {
            for (RefactoringElementImplementation change : preFileChanges) {
                change.performChange();
            }
        }

        switch (refInfo.getChangeType()) {
        case CLASS_RENAME:
            if (refInfo.getPrimaryFile().equals(changingFile)) {
                formRename();
            }
            break;
        case CLASS_MOVE:
            if (refInfo.getPrimaryFile().equals(changingFile) && formEditor != null) {
                formMove();
            }
            break;
        case PACKAGE_RENAME:
        case FOLDER_RENAME:
            packageRename();
            break;
        }
    }

    private void renameMetaComponent() {
        if (formEditor != null) {
            RADComponent metacomp = formEditor.getFormModel().findRADComponent(refInfo.getOldName());
            if (metacomp != null) {
                metacomp.setName(refInfo.getNewName());
                updateForm(false);
            }
        }
    }

/*    private void classRename() {
        if (refInfo.getPrimaryFile().equals(changingFile)) {
            // the form is being renamed
            // needs to be regenerated (use of MyForm.this)
            if (formEditor != null) {
                // changedFile survives the renaming
                if (changingFile.getName().equals(refInfo.getNewName())) {
                    formRename(false);
                } else { // dataobject not renamed yet...
                    changingFile.addFileChangeListener(new FileChangeAdapter() {
                        public void fileRenamed(FileRenameEvent fe) {
                            changingFile.removeFileChangeListener(this);
                            formRename(true);
                        }
                    });
                }
            }
        } else { // a component class used in this form is renamed
            FileObject renamedFile = refInfo.getPrimaryFile();
            String pkg = ClassPath.getClassPath(renamedFile, ClassPath.SOURCE)
                    .getResourceName(renamedFile.getParent(), '.', false);
            String oldClassName = (pkg != null && pkg.length() > 0)
                    ? pkg + "." + refInfo.getOldName() // NOI18N
                    : refInfo.getOldName();
            String newClassName = (pkg != null && pkg.length() > 0)
                    ? pkg + "." + refInfo.getNewName() // NOI18N
                    : refInfo.getNewName();
            componentChange(oldClassName, newClassName);
        }
    } */

    private void formRename(/*boolean saveAll*/) {
        ResourceSupport.formRenamed(formEditor.getFormModel(), refInfo.getOldName());
        updateForm(true);
    }

    private void componentClassRename() {
        FileObject renamedFile = refInfo.getPrimaryFile();
        String pkg = ClassPath.getClassPath(renamedFile, ClassPath.SOURCE)
                .getResourceName(renamedFile.getParent(), '.', false);
        String oldClassName = (pkg != null && pkg.length() > 0)
                ? pkg + "." + refInfo.getOldName() // NOI18N
                : refInfo.getOldName();
        String newClassName = (pkg != null && pkg.length() > 0)
                ? pkg + "." + refInfo.getNewName() // NOI18N
                : refInfo.getNewName();
        componentChange(oldClassName, newClassName);
    }

/*    private void classMove() {
        if (refInfo.getPrimaryFile().equals(changingFile)) {
            // the form is being moved
            if (formEditor != null) {
                // changedFile FileObject is abandoned, we must watch DataObject
                if (!formDataObject.getPrimaryFile().equals(changingFile)) {
                    formMove(false);
                } else { // not moved yet
                    formDataObject.addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent ev) {
                            if (DataObject.PROP_PRIMARY_FILE.equals(ev.getPropertyName())
                                    && !formDataObject.getPrimaryFile().equals(changingFile)) {
                                formDataObject.removePropertyChangeListener(this);
                                formMove(true);
                            }
                        }
                    });
                }
            }
        } else { // a component class used in this form is moved
            componentChange(refInfo.getOldName(), refInfo.getNewName());
        }
    } */

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
        return "Post-refactoring:Update GUI form and regenerate code in guarded blocks";
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

    private static final String COMPONENT_MARK = "<Component "; // NOI18N
    private static final String CLASS_MARK = " class=\""; // NOI18N

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
        try {
            lock = formFile.lock();
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }

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
            if (!shortName) {
                oldStr = new String[] { oldName };
                newStr = new String[] { newName };
            } else { // will use different algorithm
                oldStr = newStr = null;
            }
        }

        java.io.OutputStream os = null;
        final String encoding = "UTF-8"; // NOI18N
        try {
            String outString;
            InputStream is = formFile.getInputStream();
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
                outString = rep.toString();
                if (!rep.anythingChanged()) {
                    return false;
                }
            } else {
                // The replaced name is short with no '.', so it is too risky
                // to do plain search/replace over the entire file content.
                // Search only in the specific attribute of the component element.
                // TODO: also in custom code fragments...
                StringBuilder buf = new StringBuilder((int)formFile.getSize());
                String line = reader.readLine();
                while (line != null) {
                    if (line.trim().startsWith(COMPONENT_MARK)) {
                        int i = line.indexOf(CLASS_MARK);
                        if (i > 0) {
                            int i2 = line.indexOf(oldName);
                            if (i2 == i +  CLASS_MARK.length()) {
                                int i3 = i2 + oldName.length();
                                char c = line.charAt(i3);
                                if (c == '\"' || c == '.') { // NOI18N
                                    line = line.substring(0, i2) + newName + line.substring(i3);
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
                outString = buf.toString();
            }
            is.close();
            os = formFile.getOutputStream(lock);
            os.write(outString.getBytes(encoding));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) { // ignore
            }
            lock.releaseLock();
        }
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
                   && !Character.isJavaIdentifierPart(lastChar);
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
