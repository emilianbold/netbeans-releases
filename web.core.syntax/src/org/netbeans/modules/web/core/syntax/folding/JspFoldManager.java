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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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
import org.openide.ErrorManager;

/**
 * This class is an implementation of @see org.netbeans.spi.editor.fold.FoldManager
 * responsible for creating, deleting and updating code folds.
 *
 * @author  Marek Fukala
 */
public class JspFoldManager implements FoldManager {

    private static final boolean SHOW_TIMES = Boolean.getBoolean("org.netbeans.modules.web.core.folding.measure");
    private FoldOperation operation;
    private JspSyntaxSupport sup;
    //timer performing periodicall folds update
    private Timer timer;
    private TimerTask timerTask;
    private static final int foldsUpdateInterval = 1000;
    private long foldsGenerationTime = -1;
    private boolean documentDirty = true;
    private BaseDocument doc = null;
    
    private List<Fold> myFolds = new  ArrayList<Fold>(20);

    private Preferences prefs;

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
        Document doc = getOperation().getHierarchy().getComponent().getDocument();
        if (doc instanceof BaseDocument) {
            this.doc = (BaseDocument) doc;

            sup = new JspSyntaxSupport(getDocument());

            //start folds updater timer
            //put off the initial fold search due to the processor overhead during page opening
            timer = new Timer();
            restartTimer();
        }
    }

    private void restartTimer() {
        documentDirty = true;
        //test whether the FoldManager.release() was called.
        //if so, then do not try to update folds anymore
        if (timer == null) {
            return;
        }

        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = createTimerTask();

        try {
            timer.schedule(timerTask, foldsUpdateInterval);
        }catch(IllegalStateException ise) {
            //If the timer thread has already been stopped, which may cause
            //during the ide shutdown and this thread still runs, it may happen
            //that the timer.schedule() call
            //throws java.lang.IllegalStateException: Timer already cancelled.
            //In such case, do nothing
        }
    }

    private TimerTask createTimerTask() {
        return new TimerTask() {

            public void run() {
                //set the update thread priority
                Thread thr = new Thread(new Runnable() {

                    public void run() {
                        try {
                            documentDirty = false;
                            updateFolds();
                        } catch (ParsingCancelledException pce) {
                            if (debug) {
                                System.out.println("parsing cancelled");
                            }
                        }
                    }
                });
                thr.setPriority(Thread.MIN_PRIORITY + 1);
                thr.start();
                //wait for the thread to die
                try {
                    thr.join();
                } catch (InterruptedException e) {
                    ;
                }
            }
        };
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

    private List generateFolds() throws BadLocationException, ParsingCancelledException {
        BaseDocument bdoc = (BaseDocument) getDocument();
        JspSyntaxSupport jspsup = JspSyntaxSupport.get(bdoc);

        ArrayList found = new ArrayList(getDocument().getLength() / 100); // ~an element per 100 chars ???
        SyntaxElement sel = jspsup.getElementChain(1);
        Stack stack = new Stack();
        int prevSelOffset = sel != null ? sel.getElementOffset() : 0;
        while (sel != null) {
            //check if the parsing should be cancelled (when there is a change in the parsed document)
            if (documentDirty) {
                throw new ParsingCancelledException();
            }

            if (debug) {
                System.out.println(sel);
            }
            if (sel.getCompletionContext() == JspSyntaxSupport.COMMENT_COMPLETION_CONTEXT) {
                found.add(new FoldInfo(sel.getElementOffset(), sel.getElementOffset() + sel.getElementLength(), JspFoldTypes.COMMENT, JspFoldTypes.COMMENT_DESCRIPTION));
            } else if (sel.getCompletionContext() == JspSyntaxSupport.SCRIPTINGL_COMPLETION_CONTEXT) {
                found.add(new FoldInfo(sel.getElementOffset(), sel.getElementOffset() + sel.getElementLength(), JspFoldTypes.SCRIPTLET, JspFoldTypes.SCRIPTLET_DESCRIPTION));
            } else if (sel.getCompletionContext() == JspSyntaxSupport.TAG_COMPLETION_CONTEXT) {
                //jsp open tag
                TagSE tse = new TagSE((SyntaxElement.TagLikeElement)sel);
                handleOpenTagElement(tse, found, stack);
            } else if (sel.getCompletionContext() == JspSyntaxSupport.ENDTAG_COMPLETION_CONTEXT) {
                //found jsp end tag
                TagSE tse = new TagSE((SyntaxElement.TagLikeElement)sel);
                handleEndTagElement(tse, found, stack);
            }
            //start scanning for syntax elements after the offset where HTML scanning stopped
            //this is necessary since JSP syntax element's are divided by expression language
            //and the JSP aren't
            sel = sel.getNext();

            //loops detection
            if (sel != null) {
                if (prevSelOffset >= sel.getElementOffset()) {
                    notifyLoop(bdoc, prevSelOffset);
                    return Collections.EMPTY_LIST;
                } else {
                    prevSelOffset = sel.getElementOffset();
                }
            }

        }

        return found;
    }

    private void notifyLoop(Document doc, int offset) throws BadLocationException {
        StringBuffer sb = new StringBuffer();
        sb.append("A loop in SyntaxElement-s detected around offset " + offset + " when scanning the document. Please report this and attach the dumped document content:\n");
        sb.append(">>>>>\n");
        sb.append(doc.getText(0, doc.getLength()));
        sb.append("\n<<<<<\n");

        ErrorManager.getDefault().log(ErrorManager.WARNING, sb.toString());//NOI18N
    }

    private void handleOpenTagElement(TagSE tse, List found, Stack stack) {
        if (tse.isSingletonTag()) {
            //create element - do not put into stack
            found.add(new FoldInfo(tse.getElementOffset(), tse.getElementOffset() + tse.getElementLength(), JspFoldTypes.TAG, getSingletonTagFoldName(tse.getTagName())));
        } else {
            stack.push(tse);
        }
    }

    private void handleEndTagElement(TagSE tse, List found, Stack stack) {
        if (!stack.isEmpty()) {
            TagSE top = (TagSE) stack.peek();
            assert top.isOpenTag();
            if (tse.getTagName().equals(top.getTagName())) {
                //we found corresponding open jsp tag
                found.add(new FoldInfo(top.getElementOffset(), tse.getElementOffset() + tse.getElementLength(), JspFoldTypes.TAG, getTagFoldName(top.getTagName())));
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
                        found.add(new FoldInfo(start.getElementOffset(),
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

    private synchronized void updateFolds() throws ParsingCancelledException {
        FoldHierarchy fh = getOperation().getHierarchy();

        //measure folds generation time
        long startTime = System.currentTimeMillis();

        try {
            //parse document and create a list of FoldInfo-s
            List generated = generateFolds();

            if (SHOW_TIMES) {
                System.out.println("[jsp folding] parsing of text of " + getDocument().getProperty(Document.TitleProperty) + " done in " + (System.currentTimeMillis() - startTime) + " millis.");
            }

//            //the timer is set to null when release() is called on this FoldManager => document is about to be closed
//            if(timer == null) {
//                if (debug) System.out.println("release() called -> cancelling folds update"); // NOI18N
//                return ;
//            }
            //filter out one-line folds
            Iterator itr = generated.iterator();
            HashSet olfs = new HashSet();
            while (itr.hasNext()) {
                FoldInfo elem = (FoldInfo) itr.next();
                if (isOneLineElement(elem)) {
                    olfs.add(elem);
                }
            }
            generated.removeAll(olfs);


            //get existing folds
            List existingFolds = FoldUtilities.findRecursive(fh.getRootFold());
            assert existingFolds != null : "Existing folds is null!"; // NOI18N

            //clean up the foreign folds
            existingFolds.retainAll(myFolds);
            
            //...and generate a list of new folds and a list of folds to be removed
            final HashSet/*<FoldInfo>*/ newborns = new HashSet(generated.size() / 2);
            final HashSet/*<Fold>*/ zombies = new HashSet(generated.size() / 2);

            //go through all the parsed elements and compare it with the list of existing folds
            Iterator genItr = generated.iterator();
            Hashtable newbornsLinesCache = new Hashtable();
            HashSet duplicateNewborns = new HashSet();
            while (genItr.hasNext()) {
                FoldInfo fi = (FoldInfo) genItr.next();
                if (debug) {
                    System.out.println("NEWBORN " + fi);
                }
                //do not add more newborns with the same lineoffset
                int fiLineOffset = Utilities.getLineOffset((BaseDocument) getDocument(), fi.startOffset);
                FoldInfo found = (FoldInfo) newbornsLinesCache.get(new Integer(fiLineOffset));
                if (found != null) {
                    //figure out whether the new element is a descendant of the already added one
                    if (found.endOffset < fi.endOffset) {
                        //remove the descendant and add the current
                        duplicateNewborns.add(found);
                    }
                }
                newbornsLinesCache.put(new Integer(fiLineOffset), fi); //add line mapping of the current element

                //try to find a fold for the fold info
                Fold fs = FoldUtilities.findNearestFold(fh, fi.startOffset);
                if (fs != null 
                        && fs.getStartOffset() == fi.startOffset 
                        && fs.getEndOffset() == fi.endOffset
                        && myFolds.contains(fs)) {
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

            Hashtable linesToFoldsCache = new Hashtable(); //needed by ***

            //remove not existing folds
            Iterator extItr = existingFolds.iterator();
            while (extItr.hasNext()) {
                Fold f = (Fold) extItr.next();
//                if(!zombies.contains(f)) { //check if not alread scheduled to remove
                Iterator genItr2 = generated.iterator();
                boolean found = false;
                while (genItr2.hasNext()) {
                    FoldInfo fi = (FoldInfo) genItr2.next();
                    if (f.getStartOffset() == fi.startOffset && f.getEndOffset() == fi.endOffset) {
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
//                }
            }

            //*** check for all newborns if there isn't any existing fold
            //starting on the same line which is a descendant of this new fold
            //if so remove it.
            Iterator newbornsItr = newborns.iterator();
            HashSet newbornsToRemove = new HashSet();
            while (newbornsItr.hasNext()) {
                FoldInfo fi = (FoldInfo) newbornsItr.next();
                Fold existing = (Fold) linesToFoldsCache.get(new Integer(Utilities.getLineOffset((BaseDocument) getDocument(), fi.startOffset)));
                if (existing != null) {
                    //test if the fold is my descendant
                    if (existing.getEndOffset() < fi.endOffset) {
                        //descendant - remove it
                        zombies.add(existing);
                    } else {
                        //remove the newborn
                        newbornsToRemove.add(fi);
                    }
                }
            }
            newborns.removeAll(newbornsToRemove);

            if (SHOW_TIMES) {
                System.out.println("[jsp folding] parsing and mangles with elements for " + getDocument().getProperty(Document.TitleProperty) + " done in " + (System.currentTimeMillis() - startTime) + " millis.");
            }

            //run folds update in event dispatching thread
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    if (debug) {
                        System.out.println("updating folds --> locking document!");
                    } // NOI18N
                    //lock the document for changes
                    (getDocument()).readLock();
                    try {
                        //lock the hierarchy
                        FoldHierarchy fh = getOperation().getHierarchy();
                        fh.lock();
                        try {
                            //open new transaction
                            FoldHierarchyTransaction fhTran = getOperation().openTransaction();
                            try {
                                //remove outdated folds
                                Iterator i = zombies.iterator();
                                while (i.hasNext()) {
                                    Fold f = (Fold) i.next();
                                    //test whether the size of the document is greater than zero,
                                    //if it is then this means that the document has been closed in editor.
                                    if (getDocument().getLength() == 0) {
                                        break;
                                    }

                                    if (debug) {
                                        System.out.println("- removing fold " + f);
                                    }
                                    getOperation().removeFromHierarchy(f, fhTran);
                                    myFolds.remove(f);
                                }

                                //add new folds
                                Iterator newFolds = newborns.iterator();
                                while (newFolds.hasNext()) {
                                    FoldInfo f = (FoldInfo) newFolds.next();
                                    //test whether the size of the document is greater than zero,
                                    //if it is then this means that the document has been closed in editor.
                                    if (getDocument().getLength() == 0) {
                                        break;
                                    }

                                    if (debug) {
                                        System.out.println("+ adding fold " + f);
                                    }
                                    if (f.startOffset >= 0 && f.endOffset >= 0 && f.startOffset < f.endOffset && f.endOffset <= getDocument().getLength()) {
                                        myFolds.add(getOperation().addToHierarchy(f.foldType, f.description, isInitiallyCollapsed(f.foldType),
                                                f.startOffset, f.endOffset, 0, 0, null, fhTran));
                                    }
                                }
                            } catch (BadLocationException ble) {
                                //when the document is closing the hierarchy returns different empty document, grrrr
                                Document fhDoc = getOperation().getHierarchy().getComponent().getDocument();
                                if (fhDoc.getLength() > 0) {
                                    ErrorManager.getDefault().notify(ble);
                                }
                            } finally {
                                fhTran.commit();
                            }
                        } finally {
                            fh.unlock();
                        }
                    } finally {
                        (getDocument()).readUnlock();
                    }
                    if (debug) {
                        System.out.println("document unlocked!");
                    } // NOI18N
                }
            });


        } catch (BadLocationException e) {
            //in case that the document is about to be closed
            //the BLE can be throws from some editor utility classes
            //so we can swallow it in this case
            Document fhDoc = getOperation().getHierarchy().getComponent().getDocument();
            if (fhDoc.getLength() > 0) {
                ErrorManager.getDefault().notify(e);
            }
        } catch (InterruptedException ie) {
            //do nothing
        } catch (InvocationTargetException ite) {
            ErrorManager.getDefault().notify(ite);
        } catch (ParsingCancelledException pce) {
            throw new ParsingCancelledException();
        } catch (Exception e) {
            //do not let exceptions like NPEs to fall through to the timer's task run method.
            //if this happens the timer is cancelled and cannot be used anymore
            ErrorManager.getDefault().notify(e);
        } finally {
            if (debug) {
                JspFoldUtils.printFolds(getOperation());
            } //DEBUG - print folds structure into console
        }

        //measure folds generation time
        long foldsGenerationTime = System.currentTimeMillis() - startTime;
        if (SHOW_TIMES) {
            System.out.println("jsp folding] folds for " + getDocument().getProperty(Document.TitleProperty) + " generated in " + foldsGenerationTime + " millis.");
        }

    }

    private boolean isInitiallyCollapsed(FoldType foldType) {
        String prefName = null;
        if(foldType == JspFoldTypes.TAG) {
            prefName = SimpleValueNames.CODE_FOLDING_COLLAPSE_TAGS;
        } else if(foldType == JspFoldTypes.COMMENT) {
            prefName = SimpleValueNames.CODE_FOLDING_COLLAPSE_JAVADOC;
        }
        if(prefName != null) {
            return prefs.getBoolean(prefName, false);
        } else {
            return false;
        }

    }

    private boolean isOneLineElement(FoldInfo fi) throws BadLocationException {
        return Utilities.getLineOffset((BaseDocument) getDocument(), fi.startOffset) == Utilities.getLineOffset((BaseDocument) getDocument(), fi.endOffset);
    }

    private boolean foldsBoundariesEquals(Fold f1, Fold f2) {
        return (f1.getStartOffset() == f2.getStartOffset() && f1.getEndOffset() == f2.getEndOffset());
    }

    private BaseDocument getDocument() {
        return this.doc;
    }

    /** Returns a time in milliseconds for how long code folds were generated.
     * This time doesn't involve running of any code from fold hirarchy.
     */
    public long getLastFoldsGenerationTime() {
        return foldsGenerationTime;
    }

    private static class FoldInfo {

        public int startOffset,  endOffset;
        public FoldType foldType = null;
        public String description = null;

        public FoldInfo(int startOffset, int endOffset, FoldType foldType, String description) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.foldType = foldType;
            this.description = description;
        }

        public String toString() {
            return "FoldInfo[start=" + startOffset + ", end=" + endOffset + ", descr=" + description + ", type=" + foldType + "]";
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

    private static class ParsingCancelledException extends Exception {

        public ParsingCancelledException() {
            super();
        }
    }
    //enable/disable debugging messages for this class
    private static final boolean debug = false;
    private static final boolean lightDebug = debug || false;
}
