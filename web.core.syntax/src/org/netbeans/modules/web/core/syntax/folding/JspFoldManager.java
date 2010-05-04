/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.core.syntax.folding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.web.core.syntax.JspKit;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.SyntaxElement;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * This class is an implementation of @see org.netbeans.spi.editor.fold.FoldManager
 * responsible for creating, deleting and updating code folds.
 *
 * @author  mfukala@netbeans.org
 */
public class JspFoldManager implements FoldManager {

    private FoldOperation operation;
    private static final int FOLD_UPDATE_DELAY = 1000;
    private boolean documentDirty = true;
    private BaseDocument doc;
    private final List<Fold> currentFolds = new ArrayList<Fold>(20);
    private Preferences prefs;

    //typically only one folding task (on the edited gile) runs so
    //the RequestProcessor's throughtput 1 should be enough,
    //but sometimes (opening many files together or restarting
    //the IDE with serialized editor panes)
    //the throughtput should be bigger to leverage the multicore CPUs.
    private final RequestProcessor RP = new RequestProcessor(JspFoldManager.class.getSimpleName(), 
            Runtime.getRuntime().availableProcessors());
    
    private final Task FOLDS_UPDATE_TASK = RP.create(new Runnable() {
        public void run() {
            try {
                documentDirty = false;
                updateFolds();
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    });

    public JspFoldManager() {
        prefs = MimeLookup.getLookup(JspKit.JSP_MIME_TYPE).lookup(Preferences.class);
    }

    protected FoldOperation getOperation() {
        return operation;
    }

    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    public void initFolds(FoldHierarchyTransaction transaction) {
        //filter first initFolds call when the EditorPane has PlainDocument content
        Document document = getOperation().getHierarchy().getComponent().getDocument();
        if (document instanceof BaseDocument) {
            this.doc = (BaseDocument) document;
            //start folds updater timer
            //put off the initial fold search due to the processor overhead during page opening
            restartTimer();
        }
    }

    private void restartTimer() {
        FOLDS_UPDATE_TASK.schedule(FOLD_UPDATE_DELAY);
    }

    public void release() {
    }

    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        restartTimer();
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        restartTimer();
    }

    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        //do nothing - the updates are catched in insertUpdate and removeUpdate methods
    }

    public void removeEmptyNotify(Fold epmtyFold) {
    }

    public void removeDamagedNotify(Fold damagedFold) {
    }

    public void expandNotify(Fold expandedFold) {
    }

    private List<FoldInfo> generateFolds() {
        try {
            BaseDocument bdoc = (BaseDocument) getDocument();
            JspSyntaxSupport jspsup = JspSyntaxSupport.get(bdoc);
            ArrayList<FoldInfo> found = new ArrayList<FoldInfo>(getDocument().getLength() / 100); // ~an element per 100 chars ???
            SyntaxElement sel = jspsup.getElementChain(1);
            Stack stack = new Stack();
            int prevSelOffset = sel != null ? sel.getElementOffset() : 0;
            while (sel != null) {
                //check if the parsing should be cancelled (when there is a change in the parsed document)
                if (documentDirty) {
                    return null;
                }
                if (sel.getCompletionContext() == JspSyntaxSupport.COMMENT_COMPLETION_CONTEXT) {
                    found.add(new FoldInfo(doc, sel.getElementOffset(), sel.getElementOffset() + sel.getElementLength(), JspFoldTypes.COMMENT, JspFoldTypes.COMMENT_DESCRIPTION));
                } else if (sel.getCompletionContext() == JspSyntaxSupport.SCRIPTINGL_COMPLETION_CONTEXT) {
                    found.add(new FoldInfo(doc, sel.getElementOffset(), sel.getElementOffset() + sel.getElementLength(), JspFoldTypes.SCRIPTLET, JspFoldTypes.SCRIPTLET_DESCRIPTION));
                } else if (sel.getCompletionContext() == JspSyntaxSupport.TAG_COMPLETION_CONTEXT) {
                    //jsp open tag
                    TagSE tse = new TagSE((SyntaxElement.TagLikeElement) sel);
                    handleOpenTagElement(doc, tse, found, stack);
                } else if (sel.getCompletionContext() == JspSyntaxSupport.ENDTAG_COMPLETION_CONTEXT) {
                    //found jsp end tag
                    TagSE tse = new TagSE((SyntaxElement.TagLikeElement) sel);
                    handleEndTagElement(doc, tse, found, stack);
                }
                //start scanning for syntax elements after the offset where HTML scanning stopped
                //this is necessary since JSP syntax element's are divided by expression language
                //and the JSP aren't
                sel = sel.getNext();
                //loops detection
                if (sel != null) {
                    if (prevSelOffset >= sel.getElementOffset()) {
                        return Collections.EMPTY_LIST;
                    } else {
                        prevSelOffset = sel.getElementOffset();
                    }
                }
            }
            return found;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private void handleOpenTagElement(Document doc, TagSE tse, List found, Stack stack) throws BadLocationException {
        if (tse.isSingletonTag()) {
            //create element - do not put into stack
            found.add(new FoldInfo(doc, tse.getElementOffset(), tse.getElementOffset() + tse.getElementLength(), JspFoldTypes.TAG, getSingletonTagFoldName(tse.getTagName())));
        } else {
            stack.push(tse);
        }
    }

    private void handleEndTagElement(Document doc, TagSE tse, List found, Stack stack) throws BadLocationException {
        if (!stack.isEmpty()) {
            TagSE top = (TagSE) stack.peek();
            assert top.isOpenTag();
            if (tse.getTagName().equals(top.getTagName())) {
                //we found corresponding open jsp tag
                found.add(new FoldInfo(doc, top.getElementOffset(), tse.getElementOffset() + tse.getElementLength(), JspFoldTypes.TAG, getTagFoldName(top.getTagName())));
                stack.pop();
            } else {
                //I need to save the pop-ed elements for the case that there isn't
                //any matching start tag found
                ArrayList savedElements = new ArrayList();
                //this semaphore is used behind the loop to detect whether a
                //matching start has been found
                boolean foundStartTag = false;

                while (!stack.isEmpty()) {
                    TagSE start = (TagSE) stack.pop();
                    savedElements.add(start);
                    assert start.isOpenTag();
                    if (start.getTagName().equals(tse.getTagName())) {
                        //found a matching start tag
                        found.add(new FoldInfo(doc, start.getElementOffset(),
                                tse.getElementOffset() + tse.getElementLength(),
                                JspFoldTypes.TAG, getTagFoldName(start.getTagName())));

                        foundStartTag = true;
                        break; //break the while loop
                    }
                }
                if (!foundStartTag) {
                    //we didn't find any matching start tag =>
                    //return all elements back to the stack
                    for (int i = savedElements.size() - 1; i >= 0; i--) {
                        stack.push(savedElements.get(i));
                    }
                }
            }
        }
    }

    private String getSingletonTagFoldName(String tagName) {
        StringBuffer sb = new StringBuffer();
        sb.append("<");
        sb.append(tagName);
        sb.append("/>");
        return sb.toString();
    }

    private String getTagFoldName(String tagName) {
        StringBuffer sb = new StringBuffer();
        sb.append("<");
        sb.append(tagName);
        sb.append(">...</");
        sb.append(tagName);
        sb.append(">");
        return sb.toString();
    }

    private void mergeFolds(List<FoldInfo> generated, Set<Fold> zombies, Set<FoldInfo> newborns) throws BadLocationException {
        FoldHierarchy fh = getOperation().getHierarchy();
        //filter out one-line folds
        HashSet olfs = new HashSet();
        for (FoldInfo elem : generated) {
            if (isOneLineElement(elem)) {
                olfs.add(elem);
            }
        }
        generated.removeAll(olfs);

        //get existing folds
        List<Fold> existingFolds = (List<Fold>) FoldUtilities.findRecursive(fh.getRootFold());
        assert existingFolds != null : "Existing folds is null!"; // NOI18N

        //clean up the foreign folds
        existingFolds.retainAll(currentFolds);

        //...and generate a list of new folds and a list of folds to be removed
        //go through all the parsed elements and compare it with the list of existing folds
        Hashtable<Integer, FoldInfo> newbornsLinesCache = new Hashtable<Integer, FoldInfo>();
        HashSet<FoldInfo> duplicateNewborns = new HashSet<FoldInfo>();
        for (FoldInfo fi : generated) {
            //do not add more newborns with the same lineoffset
            int fiLineOffset = Utilities.getLineOffset((BaseDocument) getDocument(), fi.getStartOffset());
            FoldInfo found = newbornsLinesCache.get(new Integer(fiLineOffset));
            if (found != null) {
                //figure out whether the new element is a descendant of the already added one
                if (found.getEndOffset() < fi.getEndOffset()) {
                    //remove the descendant and add the current
                    duplicateNewborns.add(found);
                }
            }
            newbornsLinesCache.put(new Integer(fiLineOffset), fi); //add line mapping of the current element

            //try to find a fold for the fold info
            Fold fs = FoldUtilities.findNearestFold(fh, fi.getStartOffset());
            if (fs != null && fs.getStartOffset() == fi.getStartOffset() &&
                    fs.getEndOffset() == fi.getEndOffset() && currentFolds.contains(fs)) {
                //there is a fold with the same boundaries as the FoldInfo
                if (fi.foldType != fs.getType() || !(fi.description.equals(fs.getDescription()))) {
                    //the fold has different type or/and description => recreate
                    zombies.add(fs);
                    newborns.add(fi);
                }
            } else {
                //create a new fold
                newborns.add(fi);
            }
        }
        newborns.removeAll(duplicateNewborns);
        existingFolds.removeAll(zombies);

        Hashtable<Integer, Fold> linesToFoldsCache = new Hashtable<Integer, Fold>(); //needed by ***

        //remove not existing folds
        for (Fold f : existingFolds) {
            boolean found = false;
            for (FoldInfo fi : generated) {
                if (f.getStartOffset() == fi.getStartOffset() && f.getEndOffset() == fi.getEndOffset()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                zombies.add(f);
            } else {
                //store the fold lineoffset 2 fold mapping
                int lineoffset = Utilities.getLineOffset((BaseDocument) getDocument(), f.getStartOffset());
                linesToFoldsCache.put(new Integer(lineoffset), f);
            }
        }

        //*** check for all newborns if there isn't any existing fold
        //starting on the same line which is a descendant of this new fold
        //if so remove it.
        HashSet<FoldInfo> newbornsToRemove = new HashSet<FoldInfo>();
        for (FoldInfo fi : newborns) {
            Fold existing = linesToFoldsCache.get(new Integer(Utilities.getLineOffset((BaseDocument) getDocument(), fi.getStartOffset())));
            if (existing != null) {
                //test if the fold is my descendant
                if (existing.getEndOffset() < fi.getEndOffset()) {
                    //descendant - remove it
                    zombies.add(existing);
                } else {
                    //remove the newborn
                    newbornsToRemove.add(fi);
                }
            }
        }
        newborns.removeAll(newbornsToRemove);
    }

    private synchronized void updateFolds() throws BadLocationException {
        final FoldHierarchy hierarchy = getOperation().getHierarchy();

        final Set<Fold> zombies = new HashSet<Fold>();
        final Set<FoldInfo> newborns = new HashSet<FoldInfo>();

        //parse the document under document readlock and store
        //the folds offsets using positions.
        //this allows us to safely leave the readlock and
        //update the folds in AWT later
        final BadLocationException[] ble = new BadLocationException[1];
        getDocument().render(new Runnable() {

            public void run() {
                try {
                    hierarchy.lock();
                    //parse document - get all current folds
                    List<FoldInfo> generated = generateFolds();
                    if (generated == null) {
                        return; //parsing has been cancelled
                    }

                    //merge the old and new folds
                    mergeFolds(generated, zombies, newborns);
                } catch (BadLocationException ex) {
                    ble[0] = ex;
                } finally {
                    hierarchy.unlock();
                }
            }
        });
        if (ble[0] != null) {
            throw ble[0];
        }

        //update the folds hierarchy
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                //lock the document for changes
                (getDocument()).readLock();
                try {
                    //lock the hierarchy
                    hierarchy.lock();
                    try {
                        //open new transaction
                        FoldHierarchyTransaction fhTran = getOperation().openTransaction();
                        try {
                            //remove outdated folds
                            Iterator i = zombies.iterator();
                            while (i.hasNext()) {
                                Fold f = (Fold) i.next();
                                getOperation().removeFromHierarchy(f, fhTran);
                                currentFolds.remove(f);
                            }

                            //add new folds
                            Iterator newFolds = newborns.iterator();
                            while (newFolds.hasNext()) {
                                FoldInfo f = (FoldInfo) newFolds.next();
                                if (f.getStartOffset() >= 0 && f.getEndOffset() >= 0 && f.getStartOffset() < f.getEndOffset() && f.getEndOffset() <= getDocument().getLength()) {
                                    try {
                                        currentFolds.add(getOperation().addToHierarchy(f.foldType, f.description, isInitiallyCollapsed(f.foldType), f.getStartOffset(), f.getEndOffset(), 0, 0, null, fhTran));
                                    } catch (BadLocationException ignore) {
                                    }
                                }
                            }
                        } finally {
                            fhTran.commit();
                        }
                    } finally {
                        hierarchy.unlock();
                    }
                } finally {
                    (getDocument()).readUnlock();
                }
            }
        });

    }

    private boolean isInitiallyCollapsed(FoldType foldType) {
        String prefName = null;
        if (foldType == JspFoldTypes.TAG) {
            prefName = SimpleValueNames.CODE_FOLDING_COLLAPSE_TAGS;
        } else if (foldType == JspFoldTypes.COMMENT) {
            prefName = SimpleValueNames.CODE_FOLDING_COLLAPSE_JAVADOC;
        }
        if (prefName != null) {
            return prefs.getBoolean(prefName, false);
        } else {
            return false;
        }

    }

    private boolean isOneLineElement(FoldInfo fi) throws BadLocationException {
        return Utilities.getLineOffset((BaseDocument) getDocument(), fi.getStartOffset()) == Utilities.getLineOffset((BaseDocument) getDocument(), fi.getEndOffset());
    }

    private BaseDocument getDocument() {
        return this.doc;
    }

    private static class FoldInfo {

        private Position startOffset, endOffset;
        private FoldType foldType;
        private String description;

        public FoldInfo(Document doc, int startOffset, int endOffset, FoldType foldType, String description) throws BadLocationException {
            this.startOffset = doc.createPosition(startOffset);
            this.endOffset = doc.createPosition(endOffset);
            this.foldType = foldType;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public int getEndOffset() {
            return endOffset.getOffset();
        }

        public FoldType getFoldType() {
            return foldType;
        }

        public int getStartOffset() {
            return startOffset.getOffset();
        }

        @Override
        public String toString() {
            return "FoldInfo[start=" + startOffset + ", end=" + endOffset +
                    ", descr=" + description + ", type=" + foldType + "]"; //NOI18N
        }
    }

    private static class TagSE {

        private org.netbeans.modules.web.core.syntax.SyntaxElement.TagLikeElement jspse = null;

        public TagSE(SyntaxElement.TagLikeElement se) {
            this.jspse = se;
        }

        public int getElementOffset() {
            return jspse.getElementOffset();
        }

        public int getElementLength() {
            return jspse.getElementLength();
        }

        public int getType() {
            return jspse.getCompletionContext();
        }

        public boolean isOpenTag() {
            return jspse.getCompletionContext() == JspSyntaxSupport.TAG_COMPLETION_CONTEXT;
        }

        public String getTagName() {
            return jspse.getName();
        }

        public boolean isSingletonTag() {
            if (!isOpenTag()) {
                return false;
            } else {
                return ((org.netbeans.modules.web.core.syntax.SyntaxElement.Tag) jspse).isClosed();
            }
        }
    }
}
