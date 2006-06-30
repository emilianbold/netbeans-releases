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

package org.netbeans.modules.editor.completion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.*;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.BaseDocument;

import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsNames;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.editor.Registry;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.spi.editor.completion.*;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Implementation of the completion processing.
 * The visual related processing is done in AWT thread together
 * with completion providers invocation and result set sorting.
 * <br>
 * The only thing that can be done outside of the AWT
 * is hiding of the completion/documentation/tooltip.
 *
 * <p>
 * The completion providers typically reschedule computation intensive
 * collecting of their result set into an extra thread to keep the GUI responsive.
 *
 * @author Dusan Balek, Miloslav Metelka
 */

public class CompletionImpl extends MouseAdapter implements DocumentListener,
CaretListener, KeyListener, FocusListener, ListSelectionListener, ChangeListener, SettingsChangeListener {
    
    private static final boolean debug = Boolean.getBoolean("netbeans.debug.editor.completion");

    private static CompletionImpl singleton = null;

    private static final String FOLDER_NAME = "CompletionProviders"; // NOI18N
    private static final String NO_SUGGESTIONS = NbBundle.getMessage(CompletionImpl.class, "completion-no-suggestions");
    private static final String PLEASE_WAIT = NbBundle.getMessage(CompletionImpl.class, "completion-please-wait");

    private static final String POPUP_HIDE = "popup-hide"; //NOI18N
    private static final String COMPLETION_SHOW = "completion-show"; //NOI18N
    private static final String DOC_SHOW = "doc-show"; //NOI18N
    private static final String TOOLTIP_SHOW = "tooltip-show"; //NOI18N
    
    private static final int PLEASE_WAIT_TIMEOUT = 250;
    
    public static CompletionImpl get() {
        if (singleton == null)
            singleton = new CompletionImpl();
        return singleton;
    }

    /** Text component being currently edited. Changed in AWT only. */
    private WeakReference/*<JTextComponent>*/ activeComponent = null;
    
    /** Document currently installed in the active component. Changed in AWT only. */
    private WeakReference/*<Document>*/ activeDocument = null;
    
    /** Map containing keystrokes that should be overriden by completion processing. Changed in AWT only. */
    private InputMap inputMap;
    
    /** Action map containing actions bound to keys through input map. Changed in AWT only. */
    private ActionMap actionMap;

    /** Layout of the completion pane/documentation/tooltip. Changed in AWT only. */
    private final CompletionLayout layout = new CompletionLayout();
    
    /* Completion providers registered for the active component (its mime-type). Changed in AWT only. */
    private CompletionProvider[] activeProviders = null;
    
    /** Mapping of mime-type to array of providers. Changed in AWT only. */
    private HashMap /*<String, CompletionProvider[]>*/ providersCache = new HashMap();

    /**
     * Result of the completion query.
     * <br>
     * It may be null which means that the query was cancelled.
     * <br>
     * Initiated in AWT and can be cleared from the thread that cancels the completion query.
     */
    private Result completionResult;
    
    /**
     * Result of the documentation query.
     * <br>
     * It may be null which means that the query was cancelled.
     * <br>
     * Initiated in AWT and can be cleared from the thread that cancels the documentation query.
     */
    private Result docResult;
    
    /**
     * Result of the tooltip query.
     * <br>
     * It may be null which means that the query was cancelled.
     * <br>
     * Initiated in AWT and can be cleared from the thread that cancels the tooltip query.
     */
    private Result toolTipResult;
    
    /** Timer for opening completion automatically. Changed in AWT only. */
    private Timer completionAutoPopupTimer;
    /** Timer for opening documentation window automatically. Changed in AWT only. */
    private Timer docAutoPopupTimer;
    /** Timer for opening Please Wait popup. Changed in AWT only. */
    private Timer pleaseWaitTimer;
    /** Whether it's initial or refreshed query. Changed in AWT only. */
    private boolean refreshedQuery = false;
    /** Whether it's explicit or automatic query. Changed in AWT only. */
    private boolean explicitQuery = false;
    
    private boolean tabCompletionWaiting = false;
    private LinkedList waitingEvents = new LinkedList();
    
    /** Ending offset of the recent insertion or removal. */
    private int modEndOffset;

    private CompletionImpl() {
        Registry.addChangeListener(this);
        completionAutoPopupTimer = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Result localCompletionResult;
                synchronized (this) {
                    localCompletionResult = completionResult;
                }
                if (localCompletionResult != null && !localCompletionResult.isQueryInvoked()) {
                    pleaseWaitTimer.restart();
                    queryResultSets(localCompletionResult.getResultSets());
                    localCompletionResult.queryInvoked();
                }
            }
        });
        completionAutoPopupTimer.setRepeats(false);
        
        docAutoPopupTimer = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDocumentation(true);
            }
        });
        docAutoPopupTimer.setRepeats(false);

        pleaseWaitTimer = new Timer(PLEASE_WAIT_TIMEOUT, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String waitText = PLEASE_WAIT;
                Result localCompletionResult;
                synchronized (this) {
                    localCompletionResult = completionResult;
                }
                if (localCompletionResult != null) {
                    for (Iterator it = localCompletionResult.getResultSets().iterator(); it.hasNext();) {
                        CompletionResultSetImpl resultSet = (CompletionResultSetImpl)it.next();
                        if (resultSet.getWaitText() != null) {
                            waitText = resultSet.getWaitText();
                            break;
                        }
                    }
                }
                layout.showCompletion(Collections.singletonList(waitText),
                        null, -1, CompletionImpl.this);
            }
        });
        pleaseWaitTimer.setRepeats(false);
        Settings.addSettingsChangeListener(this);
    }
    
    private JTextComponent getActiveComponent() {
        return activeComponent != null ? (JTextComponent)activeComponent.get() : null;
    }

    private Document getActiveDocument() {
        return activeDocument != null ? (Document)activeDocument.get() : null;
    }
    
    public int getSortType() {
        return CompletionResultSet.PRIORITY_SORT_TYPE; // [TODO] additional types
    }
    
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        // Ignore insertions done outside of the AWT (various content generation)
        if (!SwingUtilities.isEventDispatchThread()) {
            return;
        }
        // Check whether the insertion came from typing
        if (!DocumentUtilities.isTypingModification(e)) {
            return;
        }

        if (activeProviders != null) {
            try {
                modEndOffset = e.getOffset() + e.getLength();
                if (getActiveComponent().getCaretPosition() != modEndOffset)
                    return;

                String typedText = e.getDocument().getText(e.getOffset(), e.getLength());
                for (int i = 0; i < activeProviders.length; i++) {
                    int type = activeProviders[i].getAutoQueryTypes(getActiveComponent(), typedText);
                    boolean completionResultNull;
                    synchronized (this) {
                        completionResultNull = (completionResult == null);
                    }
                    if (completionResultNull && (type & CompletionProvider.COMPLETION_QUERY_TYPE) != 0 &&
                            CompletionSettings.INSTANCE.completionAutoPopup()) {
                        showCompletion(false, true);
                    }

                    boolean tooltipResultNull;
                    synchronized (this) {
                        tooltipResultNull = (toolTipResult == null);
                    }
                    if (tooltipResultNull && (type & CompletionProvider.TOOLTIP_QUERY_TYPE) != 0) {
                        showToolTip();
                    }
                }
            } catch (BadLocationException ex) {}
            if (completionAutoPopupTimer.isRunning())
                restartCompletionAutoPopupTimer();
        }
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        // Ignore insertions done outside of the AWT (various content generation)
        if (!SwingUtilities.isEventDispatchThread()) {
            return;
        }
        // Removals covered by caretUpdate()
        modEndOffset = e.getOffset();
    }
    
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
    }
    
    public synchronized void caretUpdate(javax.swing.event.CaretEvent e) {
        assert (SwingUtilities.isEventDispatchThread());

        if (activeProviders != null) {
            // Check whether there is an active result being computed but not yet displayed
            // Caret update should be notified AFTER document modifications
            // thank to document listener priorities
            Result localCompletionResult;
            synchronized (this) {
                localCompletionResult = completionResult;
            }
            if ((completionAutoPopupTimer.isRunning() || localCompletionResult != null)
                && !layout.isCompletionVisible()
                && e.getDot() != modEndOffset
            ) {
                hideCompletion();
            }

            completionRefresh();
            toolTipRefresh();
        }
    }

    public void keyPressed(KeyEvent e) {
        dispatchKeyEvent(e);
    }

    public void keyReleased(KeyEvent e) {
        dispatchKeyEvent(e);
    }

    public void keyTyped(KeyEvent e) {
        dispatchKeyEvent(e);
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        hideAll();
    }

    public void mouseClicked(MouseEvent e) {
        hideAll();
    }
    
    public void hideAll() {
        hideToolTip();
        hideCompletion();
        hideDocumentation();
    }

    /**
     * Called from AWT when selection in the completion list pane changes.
     */
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        assert (SwingUtilities.isEventDispatchThread());

        if (layout.isDocumentationVisible()) {
            restartDocumentationAutoPopupTimer();
        }
    }

    /**
     * Expected to be called from the AWT only.
     */
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        assert (SwingUtilities.isEventDispatchThread()); // expected in AWT only

        boolean cancel = false;
        JTextComponent component = Registry.getMostActiveComponent();
        if (component != getActiveComponent()) {
            activeProviders = getCompletionProvidersForComponent(component);
            if (debug) {
                StringBuffer sb = new StringBuffer("Completion PROVIDERS:\n"); // NOI18N
                if (activeProviders != null) {
                    for (int i = 0; i < activeProviders.length; i++) {
                        sb.append("providers["); // NOI18N
                        sb.append(i);
                        sb.append("]: "); // NOI18N
                        sb.append(activeProviders[i].getClass());
                        sb.append('\n');
                    }
                }
                System.err.println(sb.toString());
            }
            if (getActiveComponent() != null) {
                getActiveComponent().removeCaretListener(this);
                getActiveComponent().removeKeyListener(this);
                getActiveComponent().removeFocusListener(this);
                getActiveComponent().removeMouseListener(this);
            }
            if (component != null) {
// DIRTY HACK: Uncomment this after rewriting all modules to use the new Completion API.
//            if (activeProviders != null) {
                component.addCaretListener(this);
                component.addKeyListener(this);
                component.addFocusListener(this);
                component.addMouseListener(this);
// DIRTY HACK: Uncomment this after rewriting all modules to use the new Completion API.
//            }
            }
            activeComponent = component != null ? new WeakReference(component) : null;
            CompletionSettings.INSTANCE.notifyEditorComponentChange(getActiveComponent());
            layout.setEditorComponent(getActiveComponent());
            installKeybindings();
            cancel = true;
        }
        Document document = Registry.getMostActiveDocument();
        if (document != getActiveDocument()) {
            activeProviders = getCompletionProvidersForComponent(component);
            if (debug) {
                StringBuffer sb = new StringBuffer("Completion PROVIDERS:\n"); // NOI18N
                if (activeProviders != null) {
                    for (int i = 0; i < activeProviders.length; i++) {
                        sb.append("providers["); // NOI18N
                        sb.append(i);
                        sb.append("]: "); // NOI18N
                        sb.append(activeProviders[i].getClass());
                        sb.append('\n');
                    }
                }
                System.err.println(sb.toString());
            }
            if (getActiveDocument() != null)
                DocumentUtilities.removeDocumentListener(getActiveDocument(), this,
                        DocumentListenerPriority.AFTER_CARET_UPDATE);
            if (activeProviders != null)
                DocumentUtilities.addDocumentListener(document, this,
                        DocumentListenerPriority.AFTER_CARET_UPDATE);
            activeDocument = new WeakReference(document);
            cancel = true;
        }
        if (cancel)
            completionCancel();
    }
    
    private void restartCompletionAutoPopupTimer() {
        assert (SwingUtilities.isEventDispatchThread()); // expect in AWT only

        int completionDelay = CompletionSettings.INSTANCE.completionAutoPopupDelay();
        completionAutoPopupTimer.setInitialDelay(completionDelay);
        completionAutoPopupTimer.restart();
    }
    
    private void restartDocumentationAutoPopupTimer() {
        assert (SwingUtilities.isEventDispatchThread()); // expect in AWT only

        int docDelay = CompletionSettings.INSTANCE.documentationAutoPopupDelay();
        docAutoPopupTimer.setInitialDelay(docDelay);
        docAutoPopupTimer.restart();
    }
    
    private CompletionProvider[] getCompletionProvidersForComponent(JTextComponent component) {
        assert (SwingUtilities.isEventDispatchThread());

        if (component == null)
            return null;
        
        Object mimeTypeObj = component.getDocument().getProperty("mimeType");  //NOI18N
        String mimeType;
        
        if (mimeTypeObj instanceof String)
            mimeType = (String) mimeTypeObj;
        else {
            BaseKit kit = Utilities.getKit(component);
            
            if (kit == null) {
                return new CompletionProvider[0];
            }
            
            mimeType = kit.getContentType();
        }
        
        if (providersCache.containsKey(mimeType))
            return (CompletionProvider[])providersCache.get(mimeType);

        List list = new ArrayList();
        MimeLookup lookup = MimeLookup.getMimeLookup(mimeType);
        list.addAll(lookup.lookup(new Lookup.Template(CompletionProvider.class)).allInstances());
        int size = list.size();
        CompletionProvider[] ret = size == 0 ? null : (CompletionProvider[])list.toArray(new CompletionProvider[size]);
        providersCache.put(mimeType, ret);
        return ret;
    }
    
    private synchronized void dispatchKeyEvent(KeyEvent e) {
        if (e == null)
            return;
        if (tabCompletionWaiting) {
            waitingEvents.add(e);
            e.consume();
            return;
        }
        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
        Object obj = inputMap.get(ks);
        if (obj != null) {
            Action action = actionMap.get(obj);
            if (action != null) {
                action.actionPerformed(null);
                e.consume();
                return;
            }
        }
        if (layout.isCompletionVisible()) {
            CompletionItem item = layout.getSelectedCompletionItem();
            if (item != null) {
                item.processKeyEvent(e);
                if (e.isConsumed()) {
                    return;
                }
                // Call default action if ENTER was pressed
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED) {
                    e.consume();
                    item.defaultAction(getActiveComponent());
                    return;
                }
            } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN
                    || e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN
                    || e.getKeyCode() == KeyEvent.VK_HOME || e.getKeyCode() == KeyEvent.VK_END) {
                hideCompletion();                
            }
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
                e.consume();
                synchronized (this) {
                    if (!isAllResultsFinished(completionResult.getResultSets())) {
                        tabCompletionWaiting = true;
                    } else {
                        insertCommonPrefix();
                    }
                }
                return;
            }
        }
	layout.processKeyEvent(e);
    }
    
    void completionQuery(boolean delayQuery) {
        refreshedQuery = false;
        
        Result newCompletionResult = new Result(activeProviders.length);
        synchronized (this) {
            assert (completionResult == null);
            completionResult = newCompletionResult;
        }
        List completionResultSets = newCompletionResult.getResultSets();

        // Initialize the completion tasks
        for (int i = 0; i < activeProviders.length; i++) {
            CompletionTask compTask = activeProviders[i].createTask(
                    CompletionProvider.COMPLETION_QUERY_TYPE, getActiveComponent());
            if (compTask != null) {
                CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                        this, newCompletionResult, compTask, CompletionProvider.COMPLETION_QUERY_TYPE);
                completionResultSets.add(resultSet);
            }
        }
        
        // Query the tasks
        if (delayQuery) {
            restartCompletionAutoPopupTimer();
        } else {
            pleaseWaitTimer.restart();
            queryResultSets(completionResultSets);
            newCompletionResult.queryInvoked();
        }
    }

    /**
     * Called from caretUpdate() to refresh the completion result after caret move.
     * <br>
     * Must be called in AWT thread.
     */
    private void completionRefresh() {        
        Result localCompletionResult;
        synchronized (this) {
            localCompletionResult = completionResult;
        }
        if (localCompletionResult != null) {
            refreshedQuery = true;
            Result refreshResult = localCompletionResult.createRefreshResult();
            synchronized (this) {
                completionResult = refreshResult;
            }
            refreshResult.invokeRefresh();
        }
    }
    
    private void completionCancel() {
        Result oldCompletionResult;
        synchronized (this) {
            oldCompletionResult = completionResult;
            completionResult = null;
            if (tabCompletionWaiting) {
                tabCompletionWaiting = false;
                waitingEvents.clear();
            }
        }
        if (oldCompletionResult != null) {
            oldCompletionResult.cancel();
        }
    }
    
    /**
     * Called from dispatchKeyEvent() to insert prefix common to all items in the 
     * completion result after TAB.<br>
     * Must be called in AWT thread after all tasks of the current completionResult are finished.
     */
    private void insertCommonPrefix() {
        JTextComponent c = getActiveComponent();
        Result localCompletionResult;
        synchronized (this) {
            localCompletionResult = completionResult;
        }
        if (localCompletionResult != null) {
            CharSequence commonText = null;
            int anchorOffset = -1;
outer:      for (Iterator it = localCompletionResult.getResultSets().iterator(); it.hasNext();) {
                CompletionResultSetImpl resultSet = (CompletionResultSetImpl)it.next();
                if (anchorOffset == -1)
                    anchorOffset = resultSet.getAnchorOffset();
                for (Iterator itt = resultSet.getItems().iterator(); itt.hasNext();) {
                    CharSequence text = ((CompletionItem)itt.next()).getInsertPrefix();
                    if (text == null) {
                        commonText = null;
                        break outer;
                    }
                    if (commonText == null) {
                        commonText = text;
                    } else {
                        // Get the largest common part
                        int minLen = Math.min(text.length(), commonText.length());
                        for (int commonInd = 0; commonInd < minLen; commonInd++) {
                            if (text.charAt(commonInd) != commonText.charAt(commonInd)) {
                                if (commonInd == 0) {
                                    commonText = null;
                                    break outer; // no common text
                                }
                                commonText = commonText.subSequence(0, commonInd);
                                break;
                            }
                        }
                    }
                }
            }
            if (commonText != null) {
                int caretOffset = c.getCaret().getDot();
                if (anchorOffset > -1  && caretOffset - anchorOffset < commonText.length()) {
                    commonText = commonText.subSequence(caretOffset - anchorOffset, commonText.length());
                    // Insert the missing end part of the prefix
                    BaseDocument doc = (BaseDocument)getActiveDocument();
                    doc.atomicLock();
                    try {
                        doc.insertString(caretOffset, commonText.toString(), null);
                    } catch (BadLocationException e) {
                    } finally {
                        doc.atomicUnlock();
                    }
                }
            }
        }
        if (tabCompletionWaiting) {
            tabCompletionWaiting = false;
            while(!waitingEvents.isEmpty()) {
                KeyEvent e = (KeyEvent)waitingEvents.removeFirst();
                e = new KeyEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar(), e.getKeyLocation());
                c.dispatchEvent(e);
            }
        }
    }
    
    /**
     * May be called from any thread but it will be rescheduled into AWT.
     */
    public void showCompletion() {
        showCompletion(false, false);
    }
    
    private void showCompletion(boolean explicitQuery, boolean delayQuery) {
        if (!SwingUtilities.isEventDispatchThread()) {
            // Re-call this method in AWT if necessary
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.SHOW_COMPLETION));
            return;
        }

        this.explicitQuery = explicitQuery;
        if (activeProviders != null) {
            completionAutoPopupTimer.stop();
            completionCancel(); // cancel possibly pending query
            completionQuery(delayQuery);
        } else {
            // DIRTY HACK - remove this after rewriting all modules to use the new Completion API.
            org.netbeans.editor.ext.Completion completion = org.netbeans.editor.ext.ExtUtilities.getCompletion(getActiveComponent());
            if (completion != null) {
                completion.setPaneVisible(true);
            }
            return;
            // HACK end
        }
    }

    /** 
     * Request displaying of the completion pane.
     * Can be called from any thread - is called synchronously
     * from the thread that finished last unfinished result.
     */
    void requestShowCompletionPane(Result result) {
        pleaseWaitTimer.stop();
        
        // Compute total count of the result sets
        int sortedResultsSize = 0;
        List completionResultSets = result.getResultSets();
        for (int i = completionResultSets.size() - 1; i >= 0; i--) {
            CompletionResultSetImpl resultSet = (CompletionResultSetImpl)completionResultSets.get(i);
            sortedResultsSize += resultSet.getItems().size();
        }

        // Collect and sort the gathered completion items
        final List sortedResultItems = new ArrayList(sortedResultsSize);
        String title = null;
        int anchorOffset = -1;
        int addIndex = 0;
        for (int i = 0; i < completionResultSets.size(); i++) {
            CompletionResultSetImpl resultSet = (CompletionResultSetImpl)completionResultSets.get(i);
            List resultItems = resultSet.getItems();
            if (resultItems.size() > 0) {
                sortedResultItems.addAll(resultItems);
                if (title == null)
                    title = resultSet.getTitle();
                if (anchorOffset == -1)
                    anchorOffset = resultSet.getAnchorOffset();
            }
        }
        Collections.sort(sortedResultItems, CompletionItemComparator.get(getSortType()));
        
        // Request displaying of the completion pane in AWT thread
        final String displayTitle = title;
        final int displayAnchorOffset = anchorOffset;
        Runnable requestShowRunnable = new Runnable() {
            public void run() {
                int caretOffset = getActiveComponent().getCaretPosition();
                // completionResults = null;
                if (sortedResultItems.size() == 1 && !refreshedQuery && explicitQuery
                        && CompletionSettings.INSTANCE.completionInstantSubstitution()) {
                    try {
                        int[] block = Utilities.getIdentifierBlock(getActiveComponent(), caretOffset);
                        if (block == null || block[1] == caretOffset) { // NOI18N
                            CompletionItem item = (CompletionItem) sortedResultItems.get(0);
                            if (item.instantSubstitution(getActiveComponent())) {
                                return;
                            }
                        }
                    } catch (BadLocationException ex) {
                    }
                }

                List res = new ArrayList(sortedResultItems);
                boolean noSuggestions = false;
                if (res.size() == 0) {
                    res.add(NO_SUGGESTIONS);
                    noSuggestions = true;
                }
                layout.showCompletion(res, displayTitle, displayAnchorOffset, CompletionImpl.this);

                // Show documentation as well if set by default
                if (CompletionSettings.INSTANCE.documentationAutoPopup()) {
                    if (noSuggestions) {
                        hideDocumentation();
                    } else {
                        restartDocumentationAutoPopupTimer();
                    }
                }
            }
        };
        runInAWT(requestShowRunnable);
    }

    /**
     * May be called from any thread. The UI changes will be rescheduled into AWT.
     */
    public boolean hideCompletion() {
        completionCancel();
        // Invoke hideCompletionPane() in AWT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.HIDE_COMPLETION_PANE));
            return false;
        } else { // in AWT
            return hideCompletionPane();
        }
    }
    
    /**
     * Hide the completion pane. This must be called in AWT thread.
     */
    private boolean hideCompletionPane() {
        completionAutoPopupTimer.stop(); // Ensure the popup timer gets stopped
        pleaseWaitTimer.stop();
        boolean hidePerformed = layout.hideCompletion();
        if (hidePerformed && CompletionSettings.INSTANCE.documentationAutoPopup()) {
            hideDocumentation();
        }
        return hidePerformed;
    }
    
    public void showDocumentation() {
        showDocumentation(false);
    }
    
    /**
     * May be called from any thread but it will be rescheduled into AWT.
     */
    void showDocumentation(boolean clearHistory) {
        if (!SwingUtilities.isEventDispatchThread()) {
            // Re-call this method in AWT if necessary
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.SHOW_DOCUMENTATION));
            return;
        }

        if (activeProviders != null) {
            documentationCancel();
            if (clearHistory) {
                layout.clearDocumentationHistory();
            }
            documentationQuery();
        } else {
            // DIRTY HACK - remove this after rewriting all modules to use the new Completion API.
            org.netbeans.editor.ext.CompletionJavaDoc completionDoc = org.netbeans.editor.ext.ExtUtilities.getCompletionJavaDoc(getActiveComponent());
            if (completionDoc != null) {
                completionDoc.setJavaDocVisible(true);
            }
            return;
            // HACK end
        }
    }

    /**
     * Request displaying of the documentation pane.
     * Can be called from any thread - is called synchronously
     * from the thread that finished last unfinished result.
     */
    void requestShowDocumentationPane(Result result) {
        final CompletionResultSetImpl resultSet = findFirstValidResult(result.getResultSets());
        if (resultSet != null) {
            runInAWT(new Runnable() {
                public void run() {
                    synchronized (CompletionImpl.this) {
                        layout.showDocumentation(
                                resultSet.getDocumentation(), resultSet.getAnchorOffset());
                    }
                }
            });
        }
    }

    /**
     * May be called in AWT only.
     */
    private void documentationQuery() {
        Result newDocumentationResult = new Result(1); // Estimate for selected item only
        synchronized (this) {
            assert (docResult == null);
            docResult = newDocumentationResult;
        }
        List documentationResultSets = docResult.getResultSets();

        CompletionTask docTask;
        CompletionItem selectedItem = layout.getSelectedCompletionItem();
        if (selectedItem != null && (docTask = selectedItem.createDocumentationTask()) != null) { // attempt the documentation for selected item
            CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                    this, newDocumentationResult, docTask, CompletionProvider.DOCUMENTATION_QUERY_TYPE);
            documentationResultSets.add(resultSet);
        } else { // No item selected => Query all providers
            for (int i = 0; i < activeProviders.length; i++) {
                docTask = activeProviders[i].createTask(
                        CompletionProvider.DOCUMENTATION_QUERY_TYPE, getActiveComponent());
                if (docTask != null) {
                    CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                            this, newDocumentationResult, docTask, CompletionProvider.DOCUMENTATION_QUERY_TYPE);
                    documentationResultSets.add(resultSet);
                }
            }
        }

        queryResultSets(documentationResultSets);
        newDocumentationResult.queryInvoked();
    }

    private void documentationRefresh() {
        Result localDocumentationResult;
        synchronized (this) {
            localDocumentationResult = docResult;
        }
        if (localDocumentationResult != null) {
            Result refreshResult = localDocumentationResult.createRefreshResult();
            synchronized (this) {
                docResult = refreshResult;
            }
            refreshResult.invokeRefresh();
        }
    }

    private void documentationCancel() {
        Result oldDocumentationResult;
        synchronized (this) {
            oldDocumentationResult = docResult;
            docResult = null;
        }
        if (oldDocumentationResult != null) {
            oldDocumentationResult.cancel();
        }
    }
    
    /**
     * May be called from any thread. The UI changes will be rescheduled into AWT.
     */
    public boolean hideDocumentation() {
        documentationCancel();
        // Invoke hideDocumentationPane() in AWT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.HIDE_DOCUMENTATION_PANE));
            return false;
        } else { // in AWT
            return hideDocumentationPane();
        }
    }
    
    /**
     * May be called in AWT only.
     */
    boolean hideDocumentationPane() {
        // Ensure the documentation popup timer is stopped
        docAutoPopupTimer.stop();
        boolean hidePerformed = layout.hideDocumentation();
	// Also hide completion if documentation pops automatically
        if (hidePerformed && CompletionSettings.INSTANCE.documentationAutoPopup()) {
            hideCompletion();
        }
        return hidePerformed;
    }

    
    /**
     * May be called from any thread but it will be rescheduled into AWT.
     */
    public void showToolTip() {
        if (!SwingUtilities.isEventDispatchThread()) {
            // Re-call this method in AWT if necessary
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.SHOW_TOOL_TIP));
            return;
        }

        if (activeProviders != null) {
            toolTipCancel();
            toolTipQuery();
        }
    }

    /**
     * Request displaying of the tooltip pane.
     * Can be called from any thread - is called synchronously
     * from the thread that finished last unfinished result.
     */
    void requestShowToolTipPane(Result result) {
        final CompletionResultSetImpl resultSet = findFirstValidResult(result.getResultSets());
        runInAWT(new Runnable() {
            public void run() {
                if (resultSet != null) {
                    layout.showToolTip(
                            resultSet.getToolTip(), resultSet.getAnchorOffset());
                } else {
                    hideToolTip();
                }
            }
        });
    }

    /**
     * May be called in AWT only.
     */
    private void toolTipQuery() {
        Result newToolTipResult = new Result(1);
        synchronized (this) {
            assert (toolTipResult == null);
            toolTipResult = newToolTipResult;
        }
        List toolTipResultSets = newToolTipResult.getResultSets();

        CompletionTask toolTipTask;
        CompletionItem selectedItem = layout.getSelectedCompletionItem();
        if (selectedItem != null && (toolTipTask = selectedItem.createToolTipTask()) != null) {
            CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                    this, newToolTipResult, toolTipTask, CompletionProvider.TOOLTIP_QUERY_TYPE);
            toolTipResultSets.add(resultSet);
        } else {
            for (int i = 0; i < activeProviders.length; i++) {
                toolTipTask = activeProviders[i].createTask(
                        CompletionProvider.TOOLTIP_QUERY_TYPE, getActiveComponent());
                if (toolTipTask != null) {
                    CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                            this, newToolTipResult, toolTipTask, CompletionProvider.TOOLTIP_QUERY_TYPE);
                    toolTipResultSets.add(resultSet);
                }
            }
        }
        
        queryResultSets(toolTipResultSets);
        newToolTipResult.queryInvoked();
    }

    private void toolTipRefresh() {
        Result localToolTipResult;
        synchronized (this) {
            localToolTipResult = toolTipResult;
        }
        if (localToolTipResult != null) {
            Result refreshResult = localToolTipResult.createRefreshResult();
            synchronized (this) {
                toolTipResult = refreshResult;
            }
            refreshResult.invokeRefresh();
        }
    }

    /**
     * May be called from any thread.
     */
    private void toolTipCancel() {
        Result oldToolTipResult;
        synchronized (this) {
            oldToolTipResult = toolTipResult;
            toolTipResult = null;
        }
        if (oldToolTipResult != null) {
            oldToolTipResult.cancel();
        }
    }

    /**
     * May be called from any thread. The UI changes will be rescheduled into AWT.
     */
    public boolean hideToolTip() {
        toolTipCancel();
        // Invoke hideToolTipPane() in AWT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.HIDE_TOOL_TIP_PANE));
            return false;
        } else { // in AWT
            return hideToolTipPane();
        }
    }
    
    /**
     * May be called in AWT only.
     */
    boolean hideToolTipPane() {
        return layout.hideToolTip();
    }

    /** Attempt to find the editor keystroke for the given editor action. */
    private KeyStroke[] findEditorKeys(String editorActionName, KeyStroke defaultKey) {
        // This method is implemented due to the issue
        // #25715 - Attempt to search keymap for the keybinding that logically corresponds to the action
        KeyStroke[] ret = new KeyStroke[] { defaultKey };
        if (editorActionName != null && getActiveComponent() != null) {
            TextUI ui = getActiveComponent().getUI();
            Keymap km = getActiveComponent().getKeymap();
            if (ui != null && km != null) {
                EditorKit kit = ui.getEditorKit(getActiveComponent());
                if (kit instanceof BaseKit) {
                    Action a = ((BaseKit)kit).getActionByName(editorActionName);
                    if (a != null) {
                        KeyStroke[] keys = km.getKeyStrokesForAction(a);
                        if (keys != null && keys.length > 0) {
                            ret = keys;
                        } else {
                            // try kit's keymap
                            Keymap km2 = ((BaseKit)kit).getKeymap();
                            KeyStroke[] keys2 = km2.getKeyStrokesForAction(a);
                            if (keys2 != null && keys2.length > 0) {
                                ret = keys2;
                            }                            
                        }
                    }
                }
            }
        }
        return ret;
    }

    private void installKeybindings() {
        actionMap = new ActionMap();
        inputMap = new InputMap();
        
        // Register completion show
        KeyStroke[] keys = findEditorKeys(ExtKit.completionShowAction, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));
        for (int i = 0; i < keys.length; i++) {
            inputMap.put(keys[i], COMPLETION_SHOW);
        }
        actionMap.put(COMPLETION_SHOW, new CompletionShowAction());
        
        // Register documentation show
        keys = findEditorKeys(ExtKit.documentationShowAction, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)));
        for (int i = 0; i < keys.length; i++) {
            inputMap.put(keys[i], DOC_SHOW);
        }
        actionMap.put(DOC_SHOW, new DocShowAction());
        
        // Register tooltip show
        keys = findEditorKeys(ExtKit.completionTooltipShowAction, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_MASK));
        for (int i = 0; i < keys.length; i++) {
            inputMap.put(keys[i], TOOLTIP_SHOW);
        }
        actionMap.put(TOOLTIP_SHOW, new ToolTipShowAction());
    }
    
    /**
     * Notify that a particular completion result set has just been finished.
     * <br>
     * This method may be called from any thread.
     */
    void finishNotify(CompletionResultSetImpl finishedResult) {
        switch (finishedResult.getQueryType()) {
            case CompletionProvider.COMPLETION_QUERY_TYPE:
                Result localCompletionResult;
                synchronized (this) {
                    localCompletionResult = completionResult;
                }
                if (finishedResult.getResultId() == localCompletionResult) {
                    if (isAllResultsFinished(localCompletionResult.getResultSets())) {
                        requestShowCompletionPane(localCompletionResult);
                        synchronized (this) {
                            if (tabCompletionWaiting) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        insertCommonPrefix();
                                    }
                                });
                            }
                        }
                    }
                }
                break;

            case CompletionProvider.DOCUMENTATION_QUERY_TYPE:
                Result localDocumentationResult;
                synchronized (this) {
                    localDocumentationResult = docResult;
                }
                if (finishedResult.getResultId() == localDocumentationResult) {
                    if (isAllResultsFinished(localDocumentationResult.getResultSets())) {
                        requestShowDocumentationPane(localDocumentationResult);
                    }
                }
                break;

            case CompletionProvider.TOOLTIP_QUERY_TYPE:
                Result localToolTipResult;
                synchronized (this) {
                    localToolTipResult = toolTipResult;
                }
                if (finishedResult.getResultId() == localToolTipResult) {
                    if (isAllResultsFinished(localToolTipResult.getResultSets())) {
                        requestShowToolTipPane(localToolTipResult);
                    }
                }
                break;
                
            default:
                throw new IllegalStateException(); // Invalid query type
        }
    }
    
    private static boolean isAllResultsFinished(List/*<CompletionResultSetImpl>*/ resultSets) {
        for (int i = resultSets.size() - 1; i >= 0; i--) {
            CompletionResultSetImpl result = (CompletionResultSetImpl)resultSets.get(i);
            if (!result.isFinished()) {
                if (debug) {
                    System.err.println("CompletionTask: " + result.getTask() // NOI18N
                            + " not finished yet"); // NOI18N
                }
                return false;
            }
        }
        if (debug) {
            System.err.println("----- All tasks finished -----");
        }
        return true;
    }

    /**
     * Find first result that has non-null documentation or tooltip
     * depending on its query type.
     * <br>
     * The method assumes that all the resultSets are already finished.
     */
    private static CompletionResultSetImpl findFirstValidResult(List/*<CompletionResultSetImpl>*/ resultSets) {
        for (int i = 0; i < resultSets.size(); i++) {
            CompletionResultSetImpl result = (CompletionResultSetImpl)resultSets.get(i);
            switch (result.getQueryType()) {
                case CompletionProvider.DOCUMENTATION_QUERY_TYPE:
                    if (result.getDocumentation() != null) {
                        return result;
                    }
                    break;

                case CompletionProvider.TOOLTIP_QUERY_TYPE:
                    if (result.getToolTip() != null) {
                        return result;
                    }
                    break;
                    
                default:
                    throw new IllegalStateException();
            }
        }
        return null;
    }
    
    private static void runInAWT(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    // ..........................................................................
    
    CompletionLayout testGetCompletionLayout() {
        return layout;
    }
    
    void testSetActiveComponent(JTextComponent component) {
        activeComponent = new WeakReference(component);
    }
    
    // ..........................................................................
    
    private final class CompletionShowAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            showCompletion(true, false);
        }
    }

    private final class DocShowAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            showDocumentation(false);
        }
    }

    private final class ToolTipShowAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            showToolTip();
        }
    }

    private final class ParamRunnable implements Runnable {
        
        private static final int SHOW_COMPLETION = 0;
        private static final int SHOW_DOCUMENTATION = 1;
        private static final int SHOW_TOOL_TIP = 2;
        private static final int HIDE_COMPLETION_PANE = 3;
        private static final int HIDE_DOCUMENTATION_PANE = 4;
        private static final int HIDE_TOOL_TIP_PANE = 5;
        
        private final int opCode;
        
        ParamRunnable(int opCode) {
            this.opCode = opCode;
        }
        
        public void run() {
            switch (opCode) {
                case SHOW_COMPLETION:
                    showCompletion();
                    break;

                case SHOW_DOCUMENTATION:
                    showDocumentation();
                    break;
                    
                case SHOW_TOOL_TIP:
                    showToolTip();
                    break;
                    
                case HIDE_COMPLETION_PANE:
                    hideCompletionPane();
                    break;

                case HIDE_DOCUMENTATION_PANE:
                    hideDocumentationPane();
                    break;
                    
                case HIDE_TOOL_TIP_PANE:
                    hideToolTipPane();
                    break;
                    
                default:
                    throw new IllegalStateException();
            }
        }
    }
    
    private static void queryResultSets(List resultSets) {
        for (int i = 0; i < resultSets.size(); i++) {
            CompletionResultSetImpl resultSet = (CompletionResultSetImpl)resultSets.get(i); 
            resultSet.getTask().query(resultSet.getResultSet());
        }
    }
    
    private static void createRefreshResultSets(List resultSets, Result refreshResult) {
        List refreshResultSets = refreshResult.getResultSets();
        int size = resultSets.size();
        // Create new resultSets
        for (int i = 0; i < size; i++) {
            CompletionResultSetImpl result = (CompletionResultSetImpl)resultSets.get(i);
            result.markInactive();
            result = new CompletionResultSetImpl(result.getCompletionImpl(),
                    refreshResult, result.getTask(), result.getQueryType());
            refreshResultSets.add(result);
        }
    }
    
    private static void refreshResultSets(List resultSets, boolean beforeQuery) {
        try {
            int size = resultSets.size();
            for (int i = 0; i < size; i++) {
                CompletionResultSetImpl result = (CompletionResultSetImpl)resultSets.get(i);
                result.getTask().refresh(beforeQuery ? null : result.getResultSet());
                
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private static void cancelResultSets(List resultSets) {
        int size = resultSets.size();
        for (int i = 0; i < size; i++) {
            CompletionResultSetImpl result = (CompletionResultSetImpl)resultSets.get(i);
            result.markInactive();
            result.getTask().cancel();
        }
    }

    public void settingsChange(org.netbeans.editor.SettingsChangeEvent evt) {
        if( evt == null) {
            return;
        }
        String settingName = evt.getSettingName();
        if (SettingsNames.KEY_BINDING_LIST.equals(settingName) || settingName == null){
            Utilities.runInEventDispatchThread(new Runnable(){
                public void run(){
                    installKeybindings();
                }
            });
        }
    }
    
    /**
     * Result holding list of completion result sets.
     * <br>
     * Initially the result is in unprepared state which allows the holding
     * thread to add the result sets and start the tasks.
     * <br>
     * If another thread calls cancel() it has no effect except setting a flag
     * that is returned from the prepared() method.
     * <br>
     * If the result is finished then cancelling physically cancels the result sets.
     */
    static final class Result {
        
        private final List/*<CompletionResultSetImpl>*/ resultSets;
        
        private boolean invoked;                
        private boolean cancelled;
        private boolean beforeQuery = true;
        
        Result(int resultSetsSize) {
            resultSets = new ArrayList(resultSetsSize);
        }

        /**
         * Get the contained resultSets.
         *
         * @return non-null resultSets.
         */
        List/*<CompletionResultSetImpl>*/ getResultSets() {
            return resultSets;
        }

        /**
         * Cancel the resultSets.
         * <br>
         * If the result is not prepared a flag that the result
         * was cancelled is turned on (and later returned from prepared()).
         * <br>
         * Otherwise physical cancellation of the result sets is done.
         */
        void cancel() {
            boolean fin;
            synchronized (this) {
                assert (!cancelled);
                fin = invoked;
                if (!invoked) {
                    cancelled = true;
                }
            }
            
            if (fin) { // already invoked
                cancelResultSets(resultSets);
            }
        }
        
        synchronized boolean isQueryInvoked() {
            return invoked;
        }
        
        /**
         * Mark the queries were invoked on the tasks in the result sets.
         * @return true if the result was cancelled in the meantime.
         */
        boolean queryInvoked() {
            boolean canc;
            synchronized (this) {
                assert (!invoked);
                invoked = true;
                canc = cancelled;
                beforeQuery = false;
            }
            if (canc) {
                cancelResultSets(resultSets);
            }
            return canc;
        }
        
        /**
         * and return the new result set
         * containing the refreshed results.
         */
        Result createRefreshResult() {
            synchronized (this) {
                if (cancelled) {
                    return null;
                }
                if (beforeQuery) {
                    return this;
                }
                assert (invoked); // had to be invoked
                invoked = false;
            }
            Result refreshResult = new Result(getResultSets().size());
            refreshResult.beforeQuery = beforeQuery;
            createRefreshResultSets(resultSets, refreshResult);
            return refreshResult;
        }
        
        /**
         * Invoke refreshing of the result sets.
         * This method should be invoked on the result set returned from
         * {@link #createRefreshResult()}.
         */
        void invokeRefresh() {
            refreshResultSets(getResultSets(), beforeQuery);
            if (!beforeQuery)
                queryInvoked();
        }

    }
}
