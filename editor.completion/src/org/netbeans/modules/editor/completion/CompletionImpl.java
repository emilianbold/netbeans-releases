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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.*;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsNames;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
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
CaretListener, KeyListener, FocusListener, ListSelectionListener, PropertyChangeListener, SettingsChangeListener {
    
    private static final boolean debug = Boolean.getBoolean("org.netbeans.modules.editor.completion.debug");
    private static final boolean alphaSort = Boolean.getBoolean("org.netbeans.modules.editor.completion.alphabeticalSort"); // [TODO] create an option

    private static final Logger UI_LOG = Logger.getLogger("org.netbeans.ui.editor.completion"); // NOI18N

    private static CompletionImpl singleton = null;

    private static final String NO_SUGGESTIONS = NbBundle.getMessage(CompletionImpl.class, "completion-no-suggestions");
    private static final String PLEASE_WAIT = NbBundle.getMessage(CompletionImpl.class, "completion-please-wait");

    private static final String COMPLETION_SHOW = "completion-show"; //NOI18N
    private static final String COMPLETION_ALL_SHOW = "completion-all-show"; //NOI18N
    private static final String DOC_SHOW = "doc-show"; //NOI18N
    private static final String TOOLTIP_SHOW = "tooltip-show"; //NOI18N
    
    private static final int PLEASE_WAIT_TIMEOUT = 750;
    private static final int PRESCAN = 50;
    
    public static CompletionImpl get() {
        if (singleton == null)
            singleton = new CompletionImpl();
        return singleton;
    }

    static LazyListModel.Filter filter = new LazyListModel.Filter() {
        public boolean accept(Object obj) {
            if (obj instanceof LazyCompletionItem)
                return ((LazyCompletionItem)obj).accept();
            return true;
        }
        public void scheduleUpdate(Runnable run) {
            SwingUtilities.invokeLater( run );
        }
    };
    
    /** Text component being currently edited. Changed in AWT only. */
    private WeakReference<JTextComponent> activeComponent = null;
    
    /** Document currently installed in the active component. Changed in AWT only. */
    private WeakReference<Document> activeDocument = null;
    
    /** Map containing keystrokes that should be overriden by completion processing. Changed in AWT only. */
    private InputMap inputMap;
    
    /** Action map containing actions bound to keys through input map. Changed in AWT only. */
    private ActionMap actionMap;

    /** Layout of the completion pane/documentation/tooltip. Changed in AWT only. */
    private final CompletionLayout layout = new CompletionLayout();
    
    /* Completion providers registered for the active component (its mime-type). Changed in AWT only. */
    private CompletionProvider[] activeProviders = null;
    
    /** Mapping of mime-type to array of providers. Changed in AWT only. */
    private HashMap<String, CompletionProvider[]> providersCache = new HashMap<String, CompletionProvider[]>();

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
    
    private WeakReference<CompletionItem> lastSelectedItem = null;
    
    /** Ending offset of the recent autopopup modification. */
    private int autoModEndOffset;
    
    private boolean pleaseWaitDisplayed = false;
    private String completionShortcut = null;
    
    private CompletionImpl() {
        EditorRegistry.addPropertyChangeListener(this);
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
                if (lastSelectedItem == null || lastSelectedItem.get() != layout.getSelectedCompletionItem())
                    showDocumentation();
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
                List<CompletionResultSetImpl> resultSets;
                if (localCompletionResult != null && (resultSets = localCompletionResult.getResultSets()) != null) {
                    for (Iterator it = resultSets.iterator(); it.hasNext();) {
                        CompletionResultSetImpl resultSet = (CompletionResultSetImpl)it.next();
                        if (resultSet != null && resultSet.getWaitText() != null) {
                            waitText = resultSet.getWaitText();
                            break;
                        }
                    }
                }
                layout.showCompletion(Collections.singletonList(waitText),
                        null, -1, CompletionImpl.this, null, 0);
                pleaseWaitDisplayed = true;
            }
        });
        pleaseWaitTimer.setRepeats(false);
        Settings.addSettingsChangeListener(this);
    }
    
    private JTextComponent getActiveComponent() {
        return activeComponent != null ? activeComponent.get() : null;
    }

    private Document getActiveDocument() {
        return activeDocument != null ? activeDocument.get() : null;
    }
    
    int getSortType() {
        return alphaSort ? CompletionResultSet.TEXT_SORT_TYPE : CompletionResultSet.PRIORITY_SORT_TYPE;
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
                int modEndOffset = e.getOffset() + e.getLength();
                if (getActiveComponent().getSelectionStart() != modEndOffset)
                    return;

                String typedText = e.getDocument().getText(e.getOffset(), e.getLength());
                for (int i = 0; i < activeProviders.length; i++) {
                    int type = activeProviders[i].getAutoQueryTypes(getActiveComponent(), typedText);
                    boolean completionResultNull;
                    synchronized (this) {
                        completionResultNull = (completionResult == null);
                    }
                    if ((type & CompletionProvider.COMPLETION_QUERY_TYPE) != 0 &&
                            CompletionSettings.INSTANCE.completionAutoPopup()) {
                        autoModEndOffset = modEndOffset;
                        if (completionResultNull)
                            showCompletion(false, true, CompletionProvider.COMPLETION_QUERY_TYPE);
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
                && (!layout.isCompletionVisible() || pleaseWaitDisplayed)
                && e.getDot() != autoModEndOffset) {
                hideCompletion(false);
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    completionRefresh();
                    toolTipRefresh();
                }
            });
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
        hideCompletion(true);
        hideDocumentation(true);
    }

    /**
     * Called from AWT when selection in the completion list pane changes.
     */
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        assert (SwingUtilities.isEventDispatchThread());

        if (layout.isDocumentationVisible() || CompletionSettings.INSTANCE.documentationAutoPopup()) {
            restartDocumentationAutoPopupTimer();
        }
    }

    /**
     * Expected to be called from the AWT only.
     */
    public void propertyChange(PropertyChangeEvent e) {
        assert (SwingUtilities.isEventDispatchThread()); // expected in AWT only

        boolean cancel = false;
        JTextComponent component = EditorRegistry.lastFocusedComponent();
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
                if (activeProviders != null) {
                    component.addCaretListener(this);
                    component.addKeyListener(this);
                    component.addFocusListener(this);
                    component.addMouseListener(this);
                }
            }
            activeComponent = component != null ? new WeakReference<JTextComponent>(component) : null;
            CompletionSettings.INSTANCE.notifyEditorComponentChange(getActiveComponent());
            layout.setEditorComponent(getActiveComponent());
            installKeybindings();
            cancel = true;
        }
        Document document = component.getDocument();
        if (component != null && document != getActiveDocument()) {
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
            activeDocument = new WeakReference<Document>(document);
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

        Lookup lookup = MimeLookup.getLookup(MimePath.get(mimeType));
        Collection<? extends CompletionProvider> col = lookup.lookupAll(CompletionProvider.class);
        int size = col.size();
        CompletionProvider[] ret = size == 0 ? null : col.toArray(new CompletionProvider[size]);
        providersCache.put(mimeType, ret);
        return ret;
    }
    
    private void dispatchKeyEvent(KeyEvent e) {
        if (e == null)
            return;
        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
        JTextComponent comp = getActiveComponent();
        boolean compEditable = (comp != null && comp.isEditable());
        Object obj = inputMap.get(ks);
        if (obj != null) {
            Action action = actionMap.get(obj);
            if (action != null) {
                if (compEditable)
                    action.actionPerformed(null);
                e.consume();
                return;
            }
        }
        if (layout.isCompletionVisible()) {
            CompletionItem item = layout.getSelectedCompletionItem();
            if (item != null) {
                    LogRecord r = new LogRecord(Level.FINE, "COMPL_KEY_SELECT"); // NOI18N
                    r.setParameters(new Object[] {e.getKeyChar(), layout.getSelectedIndex(), item.getClass().getSimpleName()});
                if (compEditable)
                    item.processKeyEvent(e);
                if (e.isConsumed()) {
                    uilog(r);
                    return;
                }
                // Call default action if ENTER was pressed
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED) {
                    e.consume();
                    r = new LogRecord(Level.FINE, "COMPL_KEY_SELECT_DEFAULT"); // NOI18N
                    r.setParameters(new Object[] {'\n', layout.getSelectedIndex(), item.getClass().getSimpleName()});
                    if (compEditable)
                        item.defaultAction(getActiveComponent());
                    uilog(r);
                    return;
                }
            } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN
                    || e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN
                    || e.getKeyCode() == KeyEvent.VK_HOME || e.getKeyCode() == KeyEvent.VK_END) {
                hideCompletion(false);                
            }
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
                e.consume();
                if (compEditable)
                    insertCommonPrefix();
                return;
            }
        }
        layout.processKeyEvent(e);
    }
    
    private void completionQuery(boolean delayQuery, int queryType) {
        refreshedQuery = false;
        
        Result newCompletionResult = new Result(activeProviders.length);
        synchronized (this) {
            assert (completionResult == null);
            completionResult = newCompletionResult;
        }
        List<CompletionResultSetImpl> completionResultSets = newCompletionResult.getResultSets();

        // Initialize the completion tasks
        for (int i = 0; i < activeProviders.length; i++) {
            CompletionTask compTask = activeProviders[i].createTask(
                    queryType, getActiveComponent());
            if (compTask != null) {
                CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                        this, newCompletionResult, compTask, queryType);
                completionResultSets.add(resultSet);
            }
        }
        
        if (completionResultSets.size() > 0) {
            // Query the tasks
            if (delayQuery) {
                restartCompletionAutoPopupTimer();
            } else {
                pleaseWaitTimer.restart();
                queryResultSets(completionResultSets);
                newCompletionResult.queryInvoked();
            }
        } else {
            completionCancel();
            layout.showCompletion(Collections.singletonList(NO_SUGGESTIONS), null, -1, CompletionImpl.this, null, 0);
            pleaseWaitDisplayed = false;
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
                List<? extends CompletionItem> resultItems = resultSet.getItems();
                if (resultItems.size() > 0) {
                    if (anchorOffset >= -1) {
                        if (anchorOffset > -1 && anchorOffset != resultSet.getAnchorOffset())
                            anchorOffset = -2;
                        else
                            anchorOffset = resultSet.getAnchorOffset();
                    }
                    for (Iterator itt = resultItems.iterator(); itt.hasNext();) {
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
            }
            if (commonText != null && anchorOffset >= 0) {
                int caretOffset = c.getSelectionStart();
                if (caretOffset - anchorOffset < commonText.length()) {
                    commonText = commonText.subSequence(caretOffset - anchorOffset, commonText.length());

                    Document doc = getActiveDocument();
                    BaseDocument baseDoc = null;
                    if(doc instanceof BaseDocument)
                        baseDoc = (BaseDocument)doc;
                        
                    // Insert the missing end part of the prefix
                    if(baseDoc != null)
                        baseDoc.atomicLock();
                    try {
                        doc.insertString(caretOffset, commonText.toString(), null);
                    } catch (BadLocationException e) {
                    } finally {
                        if(baseDoc != null)
                            baseDoc.atomicUnlock();
                    }
                }
            }
        }
    }
    
    /**
     * May be called from any thread but it will be rescheduled into AWT.
     */
    public void showCompletion() {
        showCompletion(false, false, CompletionProvider.COMPLETION_QUERY_TYPE);
    }

    private void showCompletion(boolean explicitQuery, boolean delayQuery, int queryType) {
        if (!SwingUtilities.isEventDispatchThread()) {
            // Re-call this method in AWT if necessary
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.SHOW_COMPLETION, explicitQuery, delayQuery, queryType));
            return;
        }

        getActiveComponent().putClientProperty("completion-active", Boolean.TRUE);
        LogRecord r = new LogRecord(Level.FINE, "COMPL_INVOCATION"); // NOI18N
        r.setParameters(new Object[] {explicitQuery});
        uilog(r);
        
        this.explicitQuery = explicitQuery;
        if (activeProviders != null) {
            completionAutoPopupTimer.stop();
            synchronized(this) {
                if (explicitQuery && completionResult != null)
                    queryType = CompletionProvider.COMPLETION_ALL_QUERY_TYPE;
            }
            completionCancel(); // cancel possibly pending query
            completionQuery(delayQuery, queryType);
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
        int qType = 0;
        List<CompletionResultSetImpl> completionResultSets = result.getResultSets();
        for (int i = completionResultSets.size() - 1; i >= 0; i--) {
            CompletionResultSetImpl resultSet = completionResultSets.get(i);
            sortedResultsSize += resultSet.getItems().size();
            qType = resultSet.getQueryType();
        }
        
        // Collect and sort the gathered completion items
        final List<CompletionItem> sortedResultItems = new ArrayList<CompletionItem>(sortedResultsSize);
        String title = null;
        int anchorOffset = -1;
        boolean hasAdditionalItems = false;
        int cnt = 0;
        for (int i = 0; i < completionResultSets.size(); i++) {
            CompletionResultSetImpl resultSet = (CompletionResultSetImpl)completionResultSets.get(i);
            List<? extends CompletionItem> resultItems = resultSet.getItems();
            if (resultItems.size() > 0) {
                if (cnt < PRESCAN) {
                    for (CompletionItem item : resultItems) {
                        if (cnt < PRESCAN && !filter.accept(item)) {
                            sortedResultsSize--;
                            continue;
                        }
                        sortedResultItems.add(item);
                        cnt++;
                    }
                } else {
                    sortedResultItems.addAll(resultItems);
                }
                if (title == null)
                    title = resultSet.getTitle();
                if (!hasAdditionalItems)
                    hasAdditionalItems = resultSet.hasAdditionalItems();
                if (anchorOffset == -1)
                    anchorOffset = resultSet.getAnchorOffset();
            }
        }

        final boolean noSuggestions = sortedResultsSize == 0;
        if (noSuggestions && qType == CompletionProvider.COMPLETION_QUERY_TYPE) {
            showCompletion(this.explicitQuery, false, CompletionProvider.COMPLETION_ALL_QUERY_TYPE);
            return;
        }

        Collections.sort(sortedResultItems, CompletionItemComparator.get(getSortType()));
        
        // Request displaying of the completion pane in AWT thread
        final String displayTitle = title;
        final int displayAnchorOffset = anchorOffset;
        final boolean displayAdditionalItems = hasAdditionalItems;
        Runnable requestShowRunnable = new Runnable() {
            public void run() {
                int caretOffset = getActiveComponent().getSelectionStart();
                // completionResults = null;
                if (sortedResultItems.size() == 1 && !refreshedQuery && explicitQuery
                        && CompletionSettings.INSTANCE.completionInstantSubstitution()
                        && getActiveComponent().isEditable()) {
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
                
                int selectedIndex = getCompletionPreSelectionIndex(sortedResultItems);
                layout.showCompletion(noSuggestions ? Collections.singletonList(NO_SUGGESTIONS) : sortedResultItems, displayTitle, displayAnchorOffset, CompletionImpl.this, displayAdditionalItems ? completionShortcut : null, selectedIndex);
                pleaseWaitDisplayed = false;

                // Show documentation as well if set by default
                if (CompletionSettings.INSTANCE.documentationAutoPopup()) {
                    if (noSuggestions) {
                        docAutoPopupTimer.stop(); // Ensure the popup timer gets stopped
                        documentationCancel();
                        layout.hideDocumentation();
                    } else {
                        restartDocumentationAutoPopupTimer();
                    }
                }
            }
        };
        runInAWT(requestShowRunnable);
    }
    
    private int getCompletionPreSelectionIndex(List<CompletionItem> items) {
        String prefix = null;
        if(getActiveDocument() instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument)getActiveDocument();
            int caretOffset = getActiveComponent().getSelectionStart();
            try {
                int[] block = Utilities.getIdentifierBlock(doc, caretOffset);
                if (block != null) {
                    block[1] = caretOffset;
                    prefix = doc.getText(block);
                }
            } catch (BadLocationException ble) {
            }
        }
        if (prefix != null && prefix.length() > 0) {
            int idx = 0;
            for (CompletionItem item : items) {
                if (item.getInsertPrefix().toString().startsWith(prefix))
                    return idx;
                idx++;
            }
        }
        return 0;
    }

    /**
     * May be called from any thread. The UI changes will be rescheduled into AWT.
     */
    public boolean hideCompletion() {
        return hideCompletion(true);
    }
    
    public boolean hideCompletion(boolean completionOnly) {
        completionCancel();
        // Invoke hideCompletionPane() in AWT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.HIDE_COMPLETION_PANE, completionOnly));
            return false;
        } else { // in AWT
            return hideCompletionPane(completionOnly);
        }
    }
    
    /**
     * Hide the completion pane. This must be called in AWT thread.
     */
    private boolean hideCompletionPane(boolean completionOnly) {
        completionAutoPopupTimer.stop(); // Ensure the popup timer gets stopped
        pleaseWaitTimer.stop();
        boolean hidePerformed = layout.hideCompletion();
        pleaseWaitDisplayed = false;
        if (!completionOnly && hidePerformed && CompletionSettings.INSTANCE.documentationAutoPopup()) {
            hideDocumentation(true);
        }
        getActiveComponent().putClientProperty("completion-active", Boolean.FALSE);
        return hidePerformed;
    }
    
    /**
     * May be called from any thread but it will be rescheduled into AWT.
     */
    public void showDocumentation() {
        if (!SwingUtilities.isEventDispatchThread()) {
            // Re-call this method in AWT if necessary
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.SHOW_DOCUMENTATION));
            return;
        }

        if (activeProviders != null) {
            documentationCancel();
            layout.clearDocumentationHistory();
            documentationQuery();
        }
    }

    /**
     * Request displaying of the documentation pane.
     * Can be called from any thread - is called synchronously
     * from the thread that finished last unfinished result.
     */
    void requestShowDocumentationPane(Result result) {
        final CompletionResultSetImpl resultSet = findFirstValidResult(result.getResultSets());
        runInAWT(new Runnable() {
            public void run() {
                synchronized (CompletionImpl.this) {
                    if (resultSet != null) {
                        layout.showDocumentation(
                                resultSet.getDocumentation(), resultSet.getAnchorOffset());
                    } else {
                        documentationCancel();
                        layout.hideDocumentation();
                    }
                }
            }
        });
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
        List<CompletionResultSetImpl> documentationResultSets = docResult.getResultSets();

        CompletionTask docTask;
        CompletionItem selectedItem = layout.getSelectedCompletionItem();
        if (selectedItem != null) {
            lastSelectedItem = new WeakReference<CompletionItem>(selectedItem);
            docTask = selectedItem.createDocumentationTask();
            if (docTask != null) { // attempt the documentation for selected item
                CompletionResultSetImpl resultSet = new CompletionResultSetImpl(
                        this, newDocumentationResult, docTask, CompletionProvider.DOCUMENTATION_QUERY_TYPE);
                documentationResultSets.add(resultSet);
            }
        } else { // No item selected => Query all providers
            lastSelectedItem = null;
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

        if (documentationResultSets.size() > 0) {
            queryResultSets(documentationResultSets);
            newDocumentationResult.queryInvoked();
        } else {
            documentationCancel();
            layout.hideDocumentation();
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
        return hideDocumentation(true);
    }
    
    boolean hideDocumentation(boolean documentationOnly) {
        documentationCancel();
        // Invoke hideDocumentationPane() in AWT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new ParamRunnable(ParamRunnable.HIDE_DOCUMENTATION_PANE, documentationOnly));
            return false;
        } else { // in AWT
            return hideDocumentationPane(documentationOnly);
        }
    }
    
    /**
     * May be called in AWT only.
     */
    boolean hideDocumentationPane(boolean documentationOnly) {
        // Ensure the documentation popup timer is stopped
        docAutoPopupTimer.stop();
        boolean hidePerformed = layout.hideDocumentation();
 // Also hide completion if documentation pops automatically
        if (!documentationOnly && hidePerformed && CompletionSettings.INSTANCE.documentationAutoPopup()) {
            hideCompletion(true);
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
        List<CompletionResultSetImpl> toolTipResultSets = newToolTipResult.getResultSets();

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
        completionShortcut = null;
        
        // Register completion show
        KeyStroke[] keys = findEditorKeys(ExtKit.completionShowAction, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));
        for (int i = 0; i < keys.length; i++) {
            inputMap.put(keys[i], COMPLETION_SHOW);
            if (completionShortcut == null) {
                completionShortcut = getKeyStrokeAsText(keys[i]);
            }
        }
        if (completionShortcut == null)
            completionShortcut = "\'Ctrl+SPACE\'";
        actionMap.put(COMPLETION_SHOW, new CompletionShowAction(CompletionProvider.COMPLETION_QUERY_TYPE));

        // Register all completion show
        keys = findEditorKeys(ExtKit.allCompletionShowAction, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, (InputEvent.CTRL_MASK | InputEvent.ALT_MASK)));
        for (int i = 0; i < keys.length; i++) {
            inputMap.put(keys[i], COMPLETION_ALL_SHOW);
        }
        actionMap.put(COMPLETION_ALL_SHOW, new CompletionShowAction(CompletionProvider.COMPLETION_ALL_QUERY_TYPE));

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
    
    private static String getKeyStrokeAsText (KeyStroke keyStroke) {
        int modifiers = keyStroke.getModifiers ();
        StringBuffer sb = new StringBuffer ();
        sb.append('\'');
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) > 0)
            sb.append ("Ctrl+"); //NOI18N
        if ((modifiers & InputEvent.ALT_DOWN_MASK) > 0)
            sb.append ("Alt+"); //NOI18N
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) > 0)
            sb.append ("Shift+"); //NOI18N
        if ((modifiers & InputEvent.META_DOWN_MASK) > 0)
            sb.append ("Meta+"); //NOI18N
        if (keyStroke.getKeyCode () != KeyEvent.VK_SHIFT &&
            keyStroke.getKeyCode () != KeyEvent.VK_CONTROL &&
            keyStroke.getKeyCode () != KeyEvent.VK_META &&
            keyStroke.getKeyCode () != KeyEvent.VK_ALT &&
            keyStroke.getKeyCode () != KeyEvent.VK_ALT_GRAPH
        )
            sb.append (org.openide.util.Utilities.keyToString (
                KeyStroke.getKeyStroke (keyStroke.getKeyCode (), 0)
            ));
        sb.append('\'');
        return sb.toString ();
    }

    /**
     * Notify that a particular completion result set has just been finished.
     * <br>
     * This method may be called from any thread.
     */
    void finishNotify(CompletionResultSetImpl finishedResult) {
        switch (finishedResult.getQueryType()) {
            case CompletionProvider.COMPLETION_QUERY_TYPE:
            case CompletionProvider.COMPLETION_ALL_QUERY_TYPE:
                Result localCompletionResult;
                synchronized (this) {
                    localCompletionResult = completionResult;
                }
                if (finishedResult.getResultId() == localCompletionResult) {
                    if (isAllResultsFinished(localCompletionResult.getResultSets())) {
                        requestShowCompletionPane(localCompletionResult);
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
    
    private static boolean isAllResultsFinished(List<CompletionResultSetImpl> resultSets) {
        for (int i = resultSets.size() - 1; i >= 0; i--) {
            CompletionResultSetImpl result = resultSets.get(i);
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
    private static CompletionResultSetImpl findFirstValidResult(List<CompletionResultSetImpl> resultSets) {
        for (int i = 0; i < resultSets.size(); i++) {
            CompletionResultSetImpl result = resultSets.get(i);
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
        activeComponent = new WeakReference<JTextComponent>(component);
    }
    
    // ..........................................................................
    
    private final class CompletionShowAction extends AbstractAction {
        private int queryType;
        
        private CompletionShowAction(int queryType) {
            this.queryType = queryType;
        }

        public void actionPerformed(ActionEvent e) {
            showCompletion(true, false, queryType);
        }
    }

    private final class DocShowAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            showDocumentation();
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
        private final boolean explicit;
        private final boolean delayQuery;
        private final int type;
        
        ParamRunnable(int opCode) {
            this(opCode, false);
        }
        
        ParamRunnable(int opCode, boolean explicit) {
            this(opCode, explicit, false, CompletionProvider.COMPLETION_QUERY_TYPE);
        }

        ParamRunnable(int opCode, boolean explicit, boolean delayQuery, int type) {
            this.opCode = opCode;
            this.explicit = explicit;
            this.delayQuery = delayQuery;
            this.type = type;
        }

        public void run() {
            switch (opCode) {
                case SHOW_COMPLETION:
                    showCompletion(explicitQuery, delayQuery, type);
                    break;

                case SHOW_DOCUMENTATION:
                    showDocumentation();
                    break;
                    
                case SHOW_TOOL_TIP:
                    showToolTip();
                    break;
                    
                case HIDE_COMPLETION_PANE:
                    hideCompletionPane(explicit);
                    break;

                case HIDE_DOCUMENTATION_PANE:
                    hideDocumentationPane(explicit);
                    break;
                    
                case HIDE_TOOL_TIP_PANE:
                    hideToolTipPane();
                    break;
                    
                default:
                    throw new IllegalStateException();
            }
        }
    }
    
    private static void queryResultSets(List<CompletionResultSetImpl> resultSets) {
        for (int i = 0; i < resultSets.size(); i++) {
            CompletionResultSetImpl resultSet = resultSets.get(i); 
            resultSet.getTask().query(resultSet.getResultSet());
        }
    }
    
    private static void createRefreshResultSets(List<CompletionResultSetImpl> resultSets, Result refreshResult) {
        List<CompletionResultSetImpl> refreshResultSets = refreshResult.getResultSets();
        int size = resultSets.size();
        // Create new resultSets
        for (int i = 0; i < size; i++) {
            CompletionResultSetImpl result = resultSets.get(i);
            result.markInactive();
            result = new CompletionResultSetImpl(result.getCompletionImpl(),
                    refreshResult, result.getTask(), result.getQueryType());
            refreshResultSets.add(result);
        }
    }
    
    private static void refreshResultSets(List<CompletionResultSetImpl> resultSets, boolean beforeQuery) {
        try {
            int size = resultSets.size();
            for (int i = 0; i < size; i++) {
                CompletionResultSetImpl result = resultSets.get(i);
                result.getTask().refresh(beforeQuery ? null : result.getResultSet());
                
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private static void cancelResultSets(List<CompletionResultSetImpl> resultSets) {
        int size = resultSets.size();
        for (int i = 0; i < size; i++) {
            CompletionResultSetImpl result = resultSets.get(i);
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
        
        private final List<CompletionResultSetImpl> resultSets;
        
        private boolean invoked;                
        private boolean cancelled;
        private boolean beforeQuery = true;
        
        Result(int resultSetsSize) {
            resultSets = new ArrayList<CompletionResultSetImpl>(resultSetsSize);
        }

        /**
         * Get the contained resultSets.
         *
         * @return non-null resultSets.
         */
        List<CompletionResultSetImpl> getResultSets() {
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
    
    public CompletionResultSetImpl createTestResultSet(CompletionTask task, int queryType) {
        return new CompletionResultSetImpl(this, "TestResult", task, queryType);
    }
    
    static void uilog(LogRecord rec) {
        rec.setResourceBundle(NbBundle.getBundle(CompletionImpl.class));
        rec.setResourceBundleName(CompletionImpl.class.getPackage().getName() + ".Bundle"); // NOI18N
        rec.setLoggerName(UI_LOG.getName());
        UI_LOG.log(rec);
    }
}
