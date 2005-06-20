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

package org.netbeans.modules.editor.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.awt.event.*;
import java.awt.*;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.*;

import org.netbeans.editor.BaseKit;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.editor.Registry;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.spi.editor.completion.*;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */

public class CompletionImpl extends MouseAdapter implements DocumentListener,
CaretListener, KeyListener, FocusListener, ListSelectionListener, ChangeListener {
    
    private static final boolean debug = Boolean.getBoolean("netbeans.debug.editor.completion");

    private static CompletionImpl singleton = null;

    private static final String FOLDER_NAME = "CompletionProviders"; // NOI18N
    private static final String NO_SUGGESTIONS = NbBundle.getMessage(CompletionImpl.class, "completion-no-suggestions");
    private static final String PLEASE_WAIT = NbBundle.getMessage(CompletionImpl.class, "completion-please-wait");

    private static final String POPUP_HIDE = "popup-hide"; //NOI18N
    private static final String COMPLETION_SHOW = "completion-show"; //NOI18N
    private static final String DOC_SHOW = "doc-show"; //NOI18N
    private static final String TOOLTIP_SHOW = "tool-tip-show"; //NOI18N
    
    public static CompletionImpl get() {
        if (singleton == null)
            singleton = new CompletionImpl();
        return singleton;
    }
    
    private static final Comparator BY_IMPORTANCE_COMPLETION_ITEM_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            assertCompletionItem(o1);
            assertCompletionItem(o2);
            if (o1 == o2)
                return 0;
            CompletionItem i1 = (CompletionItem)o1;
            CompletionItem i2 = (CompletionItem)o2;
            int importanceDiff = i1.getSortPriority() - i2.getSortPriority();
            if (importanceDiff != 0)
                return importanceDiff;
            int alphabeticalDiff = compareText(i1.getSortText(), i2.getSortText());
            if (alphabeticalDiff != 0)
                return alphabeticalDiff;
            return -1;
        }
    };
    private static final Comparator ALPHABETICAL_COMPLETION_ITEM_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            assertCompletionItem(o1);
            assertCompletionItem(o2);
            if (o1 == o2)
                return 0;
            CompletionItem i1 = (CompletionItem)o1;
            CompletionItem i2 = (CompletionItem)o2;
            int alphabeticalDiff = compareText(i1.getSortText(), i2.getSortText());
            if (alphabeticalDiff != 0)
                return alphabeticalDiff;
            int importanceDiff = i1.getSortPriority() - i2.getSortPriority();
            if (importanceDiff != 0)
                return importanceDiff;
            return -1;
        }
    };
    
    private static void assertCompletionItem(Object o) {
        assert (o instanceof CompletionItem)
            : "Non CompletionItem instance " // NOI18N
                + o + ":" + ((o != null) ? o.getClass().getName() : "<null>") // NOI18N
                + " appeared in the code completion result list"; // NOI18N
    }
    
    private static int compareText(CharSequence text1, CharSequence text2) {
        int len = Math.min(text1.length(), text2.length());
        for (int i = 0; i < len; i++) {
            char ch1 = text1.charAt(i);
            char ch2 = text2.charAt(i);
            if (ch1 != ch2) {
                return ch1 - ch2;
            }
        }
        return text1.length() - text2.length();
    }

    private Document activeDocument = null;
    private JTextComponent activeComponent = null;
    private ActionMap actionMap;
    private InputMap inputMap;
    private CompletionLayout layout = new CompletionLayout();
    
    private CompletionProvider[] activeProviders = null;
    private HashMap /*<Class, CompletionProvider[]>*/ providersCache = new HashMap();
    
    /** List of completion result set implementations (containing the tasks as well) */
    private List completionResults = null;
    /** List of documentation result set implementations (containing the tasks as well) */
    private List docResults = null;
    /** List of tool tip result set implementations (containing the tasks as well) */
    private List toolTipResults = null;
    
    private Timer completionAutoPopupTimer;
    private Timer docAutoPopupTimer;
    private Timer waitTimer;
    private boolean instantSubstitution = true;
    private boolean isOriginalQuery = false;

    private CompletionImpl() {
        Registry.addChangeListener(this);
        completionAutoPopupTimer = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                completionQuery();
                showCompletion();
            }
        });
        completionAutoPopupTimer.setRepeats(false);
        
        docAutoPopupTimer = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                docCancel();
                layout.clearDocumentationHistory();
                docQuery();
            }
        });
        docAutoPopupTimer.setRepeats(false);

        waitTimer = new Timer(800, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                layout.showCompletion(Collections.singletonList(PLEASE_WAIT),
                        null, -1, CompletionImpl.this);
            }
        });
        waitTimer.setRepeats(false);
    }
    
    public int getSortType() {
        return CompletionResultSet.PRIORITY_SORT_TYPE; // [TODO] additional types
    }
    
    public synchronized void insertUpdate(javax.swing.event.DocumentEvent e) {
        // Ignore insertions done outside of the AWT (various content generation)
        if (!SwingUtilities.isEventDispatchThread()) {
            return;
        }

        if (activeProviders != null) {
            try {
                if (activeComponent.getCaretPosition() != e.getOffset() + e.getLength())
                    return;
                String typedText = e.getDocument().getText(e.getOffset(), e.getLength());
                for (int i = 0; i < activeProviders.length; i++) {
                    int type = activeProviders[i].getAutoQueryTypes(activeComponent, typedText);
                    if (completionResults == null && (type & CompletionProvider.COMPLETION_QUERY_TYPE) != 0) {
                        restartCompletionAutoPopupTimer();
                    }
                    if (toolTipResults == null && (type & CompletionProvider.TOOLTIP_QUERY_TYPE) != 0) {
                        toolTipQuery();
                    }
                }
            } catch (BadLocationException ex) {}
            if (completionAutoPopupTimer.isRunning())
                restartCompletionAutoPopupTimer();
        }
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
    }
    
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
    }
    
    public synchronized void caretUpdate(javax.swing.event.CaretEvent e) {
        if (activeProviders != null) {
            if (completionResults != null)
                completionRefresh();
            if (toolTipResults != null)
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
    
    public synchronized void hideAll() {
        hideToolTip();
        hideCompletion();
        hideDoc();
    }

    public synchronized void valueChanged(javax.swing.event.ListSelectionEvent e) {
        if (layout.isDocumentationVisible()) {
            restartDocumentationAutoPopupTimer();
        }
    }

    public synchronized void stateChanged(javax.swing.event.ChangeEvent e) {
        boolean cancel = false;
        JTextComponent component = Registry.getMostActiveComponent();
        if (component != activeComponent) {
            activeProviders = getCompletionProvidersForKitClass(Utilities.getKitClass(component));
            if (debug) {
                StringBuffer sb = new StringBuffer("Completion PROVIDERS:\n");
                if (activeProviders != null) {
                    for (int i = 0; i < activeProviders.length; i++) {
                        sb.append("providers[");
                        sb.append(i);
                        sb.append("]: ");
                        sb.append(activeProviders[i].getClass());
                        sb.append('\n');
                    }
                }
                System.err.println(sb.toString());
            }
            if (activeComponent != null) {
                activeComponent.removeCaretListener(this);
                activeComponent.removeKeyListener(this);
                activeComponent.removeFocusListener(this);
                activeComponent.removeMouseListener(this);
            }
            if (activeProviders != null) {
                component.addCaretListener(this);
                component.addKeyListener(this);
                component.addFocusListener(this);
                component.addMouseListener(this);
            }
            activeComponent = component;
            CompletionSettings.INSTANCE.notifyEditorComponentChange(activeComponent);
            layout.setEditorComponent(activeComponent);
            installKeybindings();
            cancel = true;
        }
        Document document = Registry.getMostActiveDocument();
        if (document != activeDocument) {
            if (activeDocument != null)
                DocumentUtilities.removeDocumentListener(activeDocument, this,
                        DocumentListenerPriority.AFTER_CARET_UPDATE);
            if (activeProviders != null)
                DocumentUtilities.addDocumentListener(document, this,
                        DocumentListenerPriority.AFTER_CARET_UPDATE);
            activeDocument = document;
            cancel = true;
        }
        if (cancel)
            completionCancel();
    }
    
    private void restartCompletionAutoPopupTimer() {
        completionAutoPopupTimer.setDelay(CompletionSettings.INSTANCE.completionAutoPopupDelay());
        completionAutoPopupTimer.restart();
    }
    
    private void restartDocumentationAutoPopupTimer() {
        docAutoPopupTimer.setDelay(CompletionSettings.INSTANCE.documentationAutoPopupDelay());
        docAutoPopupTimer.restart();
    }
    
    private CompletionProvider[] getCompletionProvidersForKitClass(Class kitClass) {
        if (kitClass == null)
            return null;
        synchronized (providersCache) {
            if (providersCache.containsKey(kitClass))
                return (CompletionProvider[])providersCache.get(kitClass);
            BaseKit kit = BaseKit.getKit(kitClass);
            if (kit == null)
                return null;
            String name = kit.getContentType();
            if (name == null)
                return null;
            BaseOptions bo = BaseOptions.getOptions(kitClass);
            if (bo == null)
                return null;
            List list = new ArrayList();
            List files = bo.getOrderedMultiPropertyFolderFiles(FOLDER_NAME);
            for (Iterator it = files.iterator(); it.hasNext();) {
                Object file = it.next();
                if (file instanceof DataObject) {
                    DataObject dob = (DataObject) file;
                    InstanceCookie ic = (InstanceCookie)dob.getCookie(InstanceCookie.class);
                    if (ic != null){
                        try {
                            if (CompletionProvider.class.isAssignableFrom(ic.instanceClass())) {
                                list.add(ic.instanceCreate());
                            }
                        } catch (IOException ioe){
                            ioe.printStackTrace();
                        } catch (ClassNotFoundException cnfe){
                            cnfe.printStackTrace();
                        }
                    }
                }
            }
            int size = list.size();
            CompletionProvider[] ret = size == 0 ? null : (CompletionProvider[])list.toArray(new CompletionProvider[size]);
            providersCache.put(kitClass, ret);
            return ret;
        }
    }
    
    private synchronized void dispatchKeyEvent(KeyEvent e) {
        if (e == null)
            return;
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
                    item.defaultAction(activeComponent);
                    return;
                }
                
            } else if (e.getID() == KeyEvent.KEY_PRESSED) {
                hideCompletion();
                return;
            }
            layout.completionProcessKeyEvent(e);
            if (e.isConsumed()) {
                return;
            }
        }
        layout.documentationProcessKeyEvent(e);
    }
    
    private void refreshResults(List results) {
        int size = results.size();
        // Create new results
        for (int i = 0; i < size; i++) {
            CompletionResultSetImpl result = (CompletionResultSetImpl)results.get(i);
            result.markInactive();
            result = new CompletionResultSetImpl(this, result.getTask(), result.getQueryType());
            results.set(i, result);
        }
        // Refresh the tasks on the new results
        for (int i = 0; i < size; i++) {
            CompletionResultSetImpl result = (CompletionResultSetImpl)results.get(i);
            result.getTask().refresh(result.getResultSet());
        }
    }
    
    private void cancelResults(List results) {
        int size = results.size();
        for (int i = 0; i < size; i++) {
            CompletionResultSetImpl result = (CompletionResultSetImpl)results.get(i);
            result.markInactive();
            result.getTask().cancel();
        }
    }
    
    private void completionQuery() {
        waitTimer.restart();
        isOriginalQuery = true;
        assert (completionResults == null);
        completionResults = new ArrayList(activeProviders.length);
        for (int i = 0; i < activeProviders.length; i++) {
            CompletionTask compTask = activeProviders[i].createTask(
                    CompletionProvider.COMPLETION_QUERY_TYPE, activeComponent);
            if (compTask != null) {
                CompletionResultSetImpl resultSetImpl = new CompletionResultSetImpl(
                        this, compTask, CompletionProvider.COMPLETION_QUERY_TYPE);
                completionResults.add(resultSetImpl);
            }
        }
        
        // Query the tasks
        for (int i = 0; i < completionResults.size(); i++) {
            CompletionResultSetImpl result = (CompletionResultSetImpl)completionResults.get(i); 
            result.getTask().query(result.getResultSet());
        }
    }
    
    private void completionRefresh() {
        isOriginalQuery = false;
        refreshResults(completionResults);
    }
    
    private void completionCancel() {
        if (completionResults != null) {
            cancelResults(completionResults);
            completionResults = null;
        }
    }
    
    public void showCompletion() {
        if (activeProviders != null) {
            completionCancel(); // cancel possibly pending query
            completionQuery();
        }
    }
    
    private void requestShowCompletionPane() {
        waitTimer.stop();
        
        // Collect and sort the results
        final TreeSet sortedResultItems = new TreeSet(BY_IMPORTANCE_COMPLETION_ITEM_COMPARATOR);
        String title = null;
        int anchorOffset = -1;
        for (int i = 0; i < completionResults.size(); i++) {
            CompletionResultSetImpl result = (CompletionResultSetImpl)completionResults.get(i);
            List resultItems = result.getItems();
            if (resultItems != null) {
                sortedResultItems.addAll(resultItems);
                if (title == null)
                    title = result.getTitle();
                if (anchorOffset == -1)
                    anchorOffset = result.getAnchorOffset();
            }
        }

        // Request displaying of the completion pane in AWT thread
        final String displayTitle = title;
        final int displayAnchorOffset = anchorOffset;
        Runnable requestShowRunnable = new Runnable() {
            public void run() {
                synchronized (CompletionImpl.this) {
                    int caretOffset = activeComponent.getCaretPosition();
                    // completionResults = null;
                    if (sortedResultItems.size() == 1 && instantSubstitution && isOriginalQuery) {
                        try {
                            int[] block = Utilities.getIdentifierBlock(activeComponent, caretOffset);
                            if (block == null || block[1] == caretOffset) { // NOI18N
                                CompletionItem item = (CompletionItem) sortedResultItems.first();
                                if (item.instantSubstitution(activeComponent)) {
                                    return;
                                }
                            }
                        } catch (BadLocationException ex) {
                        }
                    }

                    List res = new ArrayList(sortedResultItems);
                    if (res.size() == 0) {
                        res.add(NO_SUGGESTIONS);
                    }
                    layout.showCompletion(res, displayTitle, displayAnchorOffset, CompletionImpl.this);
                    
                    // Show documentation as well if set by default
                    if (CompletionSettings.INSTANCE.documentationAutoPopup()) {
                        restartDocumentationAutoPopupTimer();
                    }
                }
            }
        };
        runInAWT(requestShowRunnable);
    }

    public boolean hideCompletion() {
        completionCancel();
        boolean hidePerformed = layout.hideCompletion();
        if (hidePerformed && CompletionSettings.INSTANCE.documentationAutoPopup()) {
            hideDoc();
        }
        return hidePerformed;
    }
    
    public void showDoc() {
        if (activeProviders != null) {
            docCancel();
            docQuery();
        }
    }

    private void docQuery() {
        CompletionItem selectedItem = layout.getSelectedCompletionItem();
        if (selectedItem != null) {
            CompletionTask docTask = selectedItem.createDocumentationTask();
            if (docTask != null) {
                CompletionResultSetImpl result = new CompletionResultSetImpl(
                        this, docTask, CompletionProvider.DOCUMENTATION_QUERY_TYPE);
                assert (docResults == null);
                docResults = Collections.singletonList(result);
                docTask.query(result.getResultSet());
                return;
            }
        }
        docResults = new ArrayList(activeProviders.length);
        for (int i = 0; i < activeProviders.length; i++) {
            CompletionTask docTask = activeProviders[i].createTask(
                    CompletionProvider.DOCUMENTATION_QUERY_TYPE, activeComponent);
            if (docTask != null) {
                CompletionResultSetImpl result = new CompletionResultSetImpl(
                        this, docTask, CompletionProvider.DOCUMENTATION_QUERY_TYPE);
                docResults.add(result);
                docTask.query(result.getResultSet());
            }
        }
    }

    private void docRefresh() {
        refreshResults(docResults);
    }

    private void docCancel() {
        if (docResults != null) {
            cancelResults(docResults);
            docResults = null;
        }
    }
    
    private void runInAWT(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public boolean hideDoc() {
        docCancel();
        return layout.hideDocumentation();
    }

    public void showToolTip() {
        if (activeProviders != null) {
            toolTipCancel();
            toolTipQuery();
        }
    }

    private void toolTipQuery() {
        if (layout.isCompletionVisible()) {
            CompletionItem selectedItem = layout.getSelectedCompletionItem();
            if (selectedItem != null) {
                CompletionTask toolTipTask = selectedItem.createToolTipTask();
                if (toolTipTask != null) {
                    CompletionResultSetImpl result = new CompletionResultSetImpl(
                            this, toolTipTask, CompletionProvider.TOOLTIP_QUERY_TYPE);
                    toolTipResults = Collections.singletonList(result);
                    toolTipTask.query(result.getResultSet());
                    return;
                }
            }
        }
        toolTipResults = new ArrayList(activeProviders.length);
        for (int i = 0; i < activeProviders.length; i++) {
            CompletionTask toolTipTask = activeProviders[i].createTask(
                    CompletionProvider.TOOLTIP_QUERY_TYPE, activeComponent);
            if (toolTipTask != null) {
                CompletionResultSetImpl result = new CompletionResultSetImpl(
                        this, toolTipTask, CompletionProvider.TOOLTIP_QUERY_TYPE);
                toolTipResults.add(result);
                toolTipTask.query(result.getResultSet());
            }
        }
    }

    private void toolTipRefresh() {
        refreshResults(toolTipResults);
    }

    private void toolTipCancel() {
        if (toolTipResults != null) {
            cancelResults(toolTipResults);
            toolTipResults = null;
        }
    }

    public boolean hideToolTip() {
        toolTipCancel();
        return layout.hideToolTip();
    }
    
    /** Attempt to find the editor keystroke for the given editor action. */
    private KeyStroke[] findEditorKeys(String editorActionName, KeyStroke defaultKey) {
        // This method is implemented due to the issue
        // #25715 - Attempt to search keymap for the keybinding that logically corresponds to the action
        KeyStroke[] ret = new KeyStroke[] { defaultKey };
        if (editorActionName != null && activeComponent != null) {
            TextUI ui = activeComponent.getUI();
            Keymap km = activeComponent.getKeymap();
            if (ui != null && km != null) {
                EditorKit kit = ui.getEditorKit(activeComponent);
                if (kit instanceof BaseKit) {
                    Action a = ((BaseKit)kit).getActionByName(editorActionName);
                    if (a != null) {
                        KeyStroke[] keys = km.getKeyStrokesForAction(a);
                        if (keys != null && keys.length > 0) {
                            ret = keys;
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
        // Register escape key
        KeyStroke[] keys = findEditorKeys(ExtKit.escapeAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        for (int i = 0; i < keys.length; i++) {
            inputMap.put(keys[i], POPUP_HIDE);
        }
        actionMap.put(POPUP_HIDE, new PopupHideAction());
        // Register completion show
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK), COMPLETION_SHOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, InputEvent.CTRL_MASK), COMPLETION_SHOW);
        actionMap.put(COMPLETION_SHOW, new CompletionShowAction());
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)), DOC_SHOW);
        actionMap.put(DOC_SHOW, new DocShowAction());
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_MASK), TOOLTIP_SHOW);
        actionMap.put(TOOLTIP_SHOW, new ToolTipShowAction());
    }
    
    void finishNotify(CompletionResultSetImpl finishedResult) {
        switch (finishedResult.getQueryType()) {
            case CompletionProvider.COMPLETION_QUERY_TYPE:
                if (completionResults != null) {
                    // Check whether there are any unfinished completion tasks
                    if (isAllResultsFinished(completionResults)) {
                        requestShowCompletionPane();
                    }
                }
                break;

            case CompletionProvider.DOCUMENTATION_QUERY_TYPE:
                if (docResults != null) {
                    // Check whether there are any unfinished documentation tasks
                    if (isAllResultsFinished(docResults)) {
                        final CompletionResultSetImpl result = findFirstValidResult(docResults);
                        if (result != null) {
                            runInAWT(new Runnable() {
                                public void run() {
                                    synchronized (CompletionImpl.this) {
                                        layout.showDocumentation(
                                            result.getDocumentation(), result.getAnchorOffset());
                                    }
                                }
                            });
                        }
                    }
                }
                break;

            case CompletionProvider.TOOLTIP_QUERY_TYPE:
                if (toolTipResults != null) {
                    // Check whether there are any unfinished toolTip tasks
                    if (isAllResultsFinished(toolTipResults)) {
                        final CompletionResultSetImpl result = findFirstValidResult(toolTipResults);
                        runInAWT(new Runnable() {
                            public void run() {
                                if (result != null) {
                                    layout.showToolTip(
                                        result.getToolTip(), result.getAnchorOffset());
                                } else {
                                    layout.hideToolTip();
                                }
                            }
                        });
                    }
                }
                break;
                
            default:
                throw new IllegalStateException(); // Invalid query type
        }
    }
    
    private boolean isAllResultsFinished(List/*<CompletionResultSetImpl>*/ results) {
        for (int i = results.size() - 1; i >= 0; i--) {
            CompletionResultSetImpl result = (CompletionResultSetImpl)results.get(i);
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
     * The method assumes that all the results are already finished.
     */
    private CompletionResultSetImpl findFirstValidResult(List/*<CompletionResultSetImpl>*/ results) {
        for (int i = 0; i < results.size(); i++) {
            CompletionResultSetImpl result = (CompletionResultSetImpl)results.get(i);
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
    
    private class CompletionShowAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            showCompletion();
        }
    }

    private class DocShowAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            showDoc();
        }
    }

    private class ToolTipShowAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            showToolTip();
        }
    }

    private class PopupHideAction extends AbstractAction {
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            if (hideCompletion())
                return;
            if (hideDoc())
                return;
            hideToolTip();
        }
    }
}
