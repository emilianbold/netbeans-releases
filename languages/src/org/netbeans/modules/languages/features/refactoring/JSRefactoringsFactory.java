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

package org.netbeans.modules.languages.features.refactoring;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyledDocument;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.languages.features.DatabaseContext;
import org.netbeans.modules.languages.features.DatabaseDefinition;
import org.netbeans.modules.languages.features.DatabaseItem;
import org.netbeans.modules.languages.features.DatabaseManager;
import org.netbeans.modules.languages.features.DatabaseManager;
import org.netbeans.modules.languages.features.DatabaseUsage;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 *
 * @author Daniel Prusa
 */
public class JSRefactoringsFactory implements RefactoringPluginFactory {
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup lookup = refactoring.getRefactoringSource();
        ASTPath path = lookup.lookup(ASTPath.class);
        if (path == null) {
            return null;
        }
        if (refactoring instanceof WhereUsedQuery) {
            return new JSWhereUsedQueryPlugin((WhereUsedQuery) refactoring);
        } else if (refactoring instanceof RenameRefactoring) {
            return new JSRenameRefactoringPlugin((RenameRefactoring) refactoring);
        }
        return null;
    }
    
    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }
    
    private static String getItemText(StyledDocument doc, PositionBounds bounds, DatabaseItem item) {
        int tokenStart = bounds.getBegin().getOffset();
        int tokenEnd = bounds.getEnd().getOffset();
        int length = doc.getLength();
        int endLine = NbDocument.findLineNumber((StyledDocument) doc, length - 1);
        int lineNumber = NbDocument.findLineNumber(doc, bounds.getBegin().getOffset());
        int start = NbDocument.findLineOffset(doc, lineNumber);
        int end = lineNumber < endLine ? NbDocument.findLineOffset(doc, lineNumber + 1) : length;
        try {
            String str = doc.getText(start, end - start - 1);
            String text = tokenEnd > end ? str :
                str.substring(0, tokenStart - start) + "<b>" + // NOI18N
                str.substring(tokenStart - start, tokenEnd - start) + "</b>" + // NOI18N
                str.substring(tokenEnd - start);
            return text.trim();
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ex);
            return getItemName(item);
        }
    }
    
    private static String getItemName(DatabaseItem item) {
        if (item instanceof DatabaseDefinition)
            return ((DatabaseDefinition) item).getName(); // NOI18N
        else
        if (item instanceof DatabaseUsage)
            return ((DatabaseUsage) item).getName();
        else
            throw new IllegalArgumentException();
    }
    
    private static String getString(String id) {
        return NbBundle.getMessage(JSRefactoringsFactory.class, id);
    }
    
    public static class JSWhereUsedQueryPlugin implements RefactoringPlugin {
        
        private WhereUsedQuery  refactoring;
        private DatabaseItem    item;
        private DataObject      dataObject;
        private StyledDocument  doc;
        
        public JSWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
            this.refactoring = refactoring;
        }
        
        public Problem preCheck() {
            Lookup lookup = refactoring.getRefactoringSource();
            doc = (StyledDocument)lookup.lookup(Document.class);
            dataObject = NbEditorUtilities.getDataObject(doc);
            ASTPath path = (ASTPath)lookup.lookup(ASTPath.class);
            DatabaseContext root = DatabaseManager.getRoot((ASTNode) path.getRoot());
            if (root == null)
                return new Problem(true, getString("LBL_CannotFindUsages"));
            item = root.getDatabaseItem (path.getLeaf ().getOffset ());
            if (item == null)
                return new Problem(true, getString("LBL_CannotFindUsages"));
            return null;
        }
        
        public Problem checkParameters() {
            return null;
        }
        
        public Problem fastCheckParameters() {
            return null;
        }
        
        public void cancelRequest() {
        }
        
        public Problem prepare(RefactoringElementsBag elements) {
            elements.add(refactoring, new UsageElement(dataObject, doc, item));
            Iterator<DatabaseUsage> iter = item instanceof DatabaseDefinition ? 
                ((DatabaseDefinition) item).getUsages().iterator() :
                ((DatabaseUsage) item).getDefinition ().getUsages().iterator();
            while (iter.hasNext()) {
                elements.add(refactoring, new UsageElement(dataObject, doc, iter.next()));
            }
            return null;
        }
        
    } // JSWhereUsedQueryPlugin
    
    static class UsageElement extends SimpleRefactoringElementImplementation {
        
        private DataObject      dobj;
        private DatabaseItem    item;
        private PositionBounds  bounds = null;
        private StyledDocument  doc;
        
        public UsageElement(DataObject dobj, StyledDocument document, DatabaseItem item) {
            this.item = item;
            this.dobj = dobj;
            this.doc = document;
            getPosition(); // init bounds
        }
        
        public String getText() {
            return getItemText(doc, bounds, item);
        }
        
        public String getDisplayText() {
            return getText();
        }
        
        public void performChange() {
        }
        
        public Object getComposite() {
            return getParentFile();
        }
        
        public FileObject getParentFile() {
            return dobj.getPrimaryFile();
        }
        
        public PositionBounds getPosition() {
            if (bounds == null) {
                CloneableEditorSupport ces = findCloneableEditorSupport(dobj);
                int offset = item.getOffset(); //item.getNameOffset();
                PositionRef ref1 = ces.createPositionRef(offset, Bias.Forward);
                PositionRef ref2 = ces.createPositionRef(item.getEndOffset(), Bias.Forward); //(item.getNameEndOffset(), Bias.Forward);
                bounds = new PositionBounds(ref1, ref2);
            }
            return bounds;
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    } // UsageElement
    
    // ..........................................................................
    
    public static class JSRenameRefactoringPlugin implements RefactoringPlugin {
        
        private RenameRefactoring   refactoring;
        private DatabaseItem        item;
        private DataObject          dataObject;
        private StyledDocument      document;
        
        public JSRenameRefactoringPlugin(RenameRefactoring refactoring) {
            this.refactoring = refactoring;
        }
        
        public Problem preCheck() {
            Lookup lookup = refactoring.getRefactoringSource();
            ASTPath path = (ASTPath)lookup.lookup(ASTPath.class);
            document = (StyledDocument)lookup.lookup(StyledDocument.class);
            dataObject = NbEditorUtilities.getDataObject(document);
            DatabaseContext root = DatabaseManager.getRoot((ASTNode) path.getRoot());
            if (root == null)
                return new Problem(true, getString("LBL_CannotRename"));
            item = root.getDatabaseItem (path.getLeaf ().getOffset ());
            if (item == null)
                return new Problem(true, getString("LBL_CannotRename"));
            return null;
        }
        
        public Problem checkParameters() {
            String newName = refactoring.getNewName();
            String oldName = item instanceof DatabaseDefinition ?
                ((DatabaseDefinition) item).getName() :
                ((DatabaseUsage) item).getName();
            if (newName.equals(oldName)) {
                return new Problem(true, getString("LBL_NameNotChanged"));
            }
            if (newName == null || newName.length() == 0) {
                return new Problem(true, getString("LBL_NameNotSet"));
            }
            int length = newName.length();
            for (int x = 0; x < length; x++) {
                char c = newName.charAt(x);
                if (!Character.isLetter(c) && c != '_' && c != '$' && (x == 0 || !Character.isDigit(c))) {
                    String msg = new MessageFormat(NbBundle.getMessage(RenameRefactoringUI.class, "LBL_NotValidIdentifier")).format (
                        new Object[] {newName}
                    );
                    return new Problem(true, msg);
                }
            }
            return null;
        }
        
        public Problem fastCheckParameters() {
            return null;
        }
        
        public void cancelRequest() {
        }
        
        public Problem prepare(RefactoringElementsBag elements) {
            String newName = refactoring.getNewName();
            List<FileObject> refactoredFiles = new ArrayList<FileObject>();
            refactoredFiles.add(dataObject.getPrimaryFile());
            elements.add(refactoring, new RenameElement(dataObject, document, item, newName));
            Iterator<DatabaseUsage> iter = item instanceof DatabaseDefinition ? 
                ((DatabaseDefinition) item).getUsages().iterator() :
                ((DatabaseUsage) item).getDefinition ().getUsages().iterator();
            while (iter.hasNext()) {
                elements.add(refactoring, new RenameElement(dataObject, document, iter.next(), newName));
            }
            elements.registerTransaction(new JSCommit(refactoredFiles));
            return null;
        }
        
    } // JSRenameRefactoringPlugin
    
    static class RenameElement extends SimpleRefactoringElementImplementation {
        
        private DataObject      dobj;
        private StyledDocument  doc;
        private DatabaseItem    item;
        private String          newName;
        private PositionBounds  bounds = null;
        
        public RenameElement(DataObject dobj, StyledDocument doc, DatabaseItem item, String newName) {
            this.dobj = dobj;
            this.doc = doc;
            this.item = item;
            this.newName = newName;
            getPosition(); // init bounds
        }
        
        public String getText() {
            return getItemText(doc, bounds, item);
        }
        
        public String getDisplayText() {
            return getText();
        }
        
        public void performChange() {
            int offset = bounds.getBegin().getOffset();
            try {
                doc.remove(offset, item.getEndOffset() - item.getOffset());//(offset, item.getNameEndOffset() - item.getNameOffset());
                doc.insertString(offset, newName, null);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        
        public Object getComposite() {
            return getParentFile();
        }
        
        public FileObject getParentFile() {
            return dobj.getPrimaryFile();
        }
        
        public PositionBounds getPosition() {
            if (bounds == null) {
                CloneableEditorSupport ces = findCloneableEditorSupport(dobj);
                int offset = item.getOffset();//item.getNameOffset();
                PositionRef ref1 = ces.createPositionRef(offset, Bias.Forward);
                PositionRef ref2 = ces.createPositionRef(item.getEndOffset(), Bias.Forward);//ces.createPositionRef(item.getNameEndOffset(), Bias.Forward);
                bounds = new PositionBounds(ref1, ref2);
            }
            return bounds;
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    } // RenameElement
    
    // JSCommit .................................................................
    
    static class JSCommit implements Transaction {
        private ArrayList<BackupFacility.Handle> ids = new ArrayList();
        private List<FileObject> refactoredFiles;
        private boolean commited = false;
        
        public JSCommit(List<FileObject> refactoredFiles) {
            this.refactoredFiles = refactoredFiles;
        }
        
        public void commit() {
            try {
                if (commited) {
                    for (BackupFacility.Handle id:ids) {
                        try {
                            id.restore();
                        } catch (IOException ex) {
                            throw (RuntimeException) new RuntimeException().initCause(ex);
                        }
                    }
                } else {
                    commited = true;
                    for (FileObject file : refactoredFiles) {
                        ids.add(BackupFacility.getDefault().backup(file));
                    }
                }
                
            } catch (IOException ex) {
                throw (RuntimeException) new RuntimeException().initCause(ex);
            }
        }
        
        public void rollback() {
            for (BackupFacility.Handle id:ids) {
                try {
                    id.restore();
                } catch (IOException ex) {
                    throw (RuntimeException) new RuntimeException().initCause(ex);
                }
            }
        }
    } // JSCommit
    
}
