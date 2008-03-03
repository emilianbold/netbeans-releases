/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.editor.ext;

import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.WeakTimerListener;
import javax.swing.text.BadLocationException;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
* General Completion display formatting and services
*
* @author Miloslav Metelka
* @version 1.00
*/

public class Completion
    implements PropertyChangeListener, SettingsChangeListener, ActionListener {

        
        
    /** Editor UI supporting this completion */
    protected ExtEditorUI extEditorUI;

    /** Completion query providing query support for this completion */
    private CompletionQuery query;

    /** Last result retrieved for completion. It can become null
    * if the document was modified so the replacement position
    * would be invalid.
    */
    private CompletionQuery.Result lastResult;
    
    private boolean keyPressed = false;

    /** Completion view component displaying the completion help */
    private CompletionView view;

    /** Component (usually scroll-pane) holding the view and the title
    * and possibly other necessary components.
    */
    private ExtCompletionPane pane;
    
    private JavaDocPane javaDocPane;
    
    private JDCPopupPanel jdcPopupPanel;

    private boolean autoPopup;

    private int autoPopupDelay;

    private int refreshDelay;

    private boolean instantSubstitution;
    
    Timer timer;
    Timer docChangeTimer;

    private DocumentListener docL;
    private CaretListener caretL;

    private PropertyChangeListener docChangeL;
    
    private int caretPos=-1;

    // old providers was called serialy from AWT, emulate it by RP queue
    private static RequestProcessor serializingRequestProcessor;
    
    // sample property at static initialized, but allow dynamic disabling later
    private static final String PROP_DEBUG_COMPLETION = "editor.debug.completion";  // NOI18N
    private static final boolean DEBUG_COMPLETION = Boolean.getBoolean(PROP_DEBUG_COMPLETION);
    
    // Every asynchronous task can be splitted into several subtasks.
    // The task can between subtasks using simple test determine whether
    // it was not cancelled.
    // It emulates #28475 RequestProcessor enhancement request.
    private CancelableRunnable cancellable = new CancelableRunnable() {
        public void run() {}
    };
    public boolean provokedByAutoPopup;

    public Completion(ExtEditorUI extEditorUI) {
        this.extEditorUI = extEditorUI;

        // Initialize timer
        timer = new Timer(0, new WeakTimerListener(this)); // delay will be set later
        timer.setRepeats(false);

        docChangeTimer = new Timer(0, new WeakTimerListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
               refreshImpl( false );  //??? why do not we batch them by posting it?
            }
        }));
        docChangeTimer.setRepeats(false);
        
        // Create document listener
        class CompletionDocumentListener implements DocumentListener {
            
           private void processTimer(){
               docChangeTimer.stop();
               setKeyPressed(true);
               invalidateLastResult();
               docChangeTimer.setInitialDelay(refreshDelay);
               docChangeTimer.setDelay(refreshDelay);
               docChangeTimer.start();
           }
           
           public void insertUpdate(DocumentEvent evt) {
               trace("ENTRY insertUpdate"); // NOI18N
               processTimer();
           }

           public void removeUpdate(DocumentEvent evt) {
               trace("ENTRY removeUpdate"); // NOI18N
               processTimer();
           }

           public void changedUpdate(DocumentEvent evt) {
           }
        };        
        docL = new CompletionDocumentListener();


        class CompletionCaretListener implements CaretListener {
            public void caretUpdate( CaretEvent e ) {
                trace("ENTRY caretUpdate"); // NOI18N
                if (!isPaneVisible()){
                    // cancel timer if caret moved
                    cancelRequestImpl();
                }else{
                    //refresh completion only if a pane is already visible
                    refreshImpl( true );
                }
            }
        };        
        caretL = new CompletionCaretListener();
       
        Settings.addSettingsChangeListener(this);

        synchronized (extEditorUI.getComponentLock()) {
            // if component already installed in ExtEditorUI simulate installation
            JTextComponent component = extEditorUI.getComponent();
            if (component != null) {
                propertyChange(new PropertyChangeEvent(extEditorUI,
                                                       ExtEditorUI.COMPONENT_PROPERTY, null, component));
            }

            extEditorUI.addPropertyChangeListener(this);
        }
    }

    public void settingsChange(SettingsChangeEvent evt) {
        Class kitClass = Utilities.getKitClass(extEditorUI.getComponent());

        if (kitClass != null) {
            autoPopup = SettingsUtil.getBoolean(kitClass,
                                                ExtSettingsNames.COMPLETION_AUTO_POPUP,
                                                ExtSettingsDefaults.defaultCompletionAutoPopup);

            autoPopupDelay = SettingsUtil.getInteger(kitClass,
                             ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY,
                             ExtSettingsDefaults.defaultCompletionAutoPopupDelay);

            refreshDelay = SettingsUtil.getInteger(kitClass,
                                                   ExtSettingsNames.COMPLETION_REFRESH_DELAY,
                                                   ExtSettingsDefaults.defaultCompletionRefreshDelay);

            instantSubstitution = SettingsUtil.getBoolean(kitClass,
                                                   ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION,
                                                   ExtSettingsDefaults.defaultCompletionInstantSubstitution);
            
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (ExtEditorUI.COMPONENT_PROPERTY.equals(propName)) {
            JTextComponent component = (JTextComponent)evt.getNewValue();
            if (component != null) { // just installed

                settingsChange(null);
                
                BaseDocument doc = Utilities.getDocument(component);
                if (doc != null) {
                    doc.addDocumentListener(docL);
                }

                component.addCaretListener( caretL );
            } else { // just deinstalled
                
                setPaneVisible(false);
                
                component = (JTextComponent)evt.getOldValue();

                BaseDocument doc = Utilities.getDocument(component);
                if (doc != null) {
                    doc.removeDocumentListener(docL);
                }
                
                if( component != null ) {
                    component.removeCaretListener( caretL );
                }
            }

        } else if ("document".equals(propName)) { // NOI18N
            if (evt.getOldValue() instanceof BaseDocument) {
                ((BaseDocument)evt.getOldValue()).removeDocumentListener(docL);
            }
            if (evt.getNewValue() instanceof BaseDocument) {
                ((BaseDocument)evt.getNewValue()).addDocumentListener(docL);
            }

        }

    }

    public CompletionPane getPane() {
        return (CompletionPane) getExtPane();
    }

    public ExtCompletionPane getExtPane() {
        if (pane == null){
            pane = new ScrollCompletionPane(extEditorUI);
        }
        return pane;
    }
    
    protected CompletionView createView() {
        return new ListCompletionView();
    }

    public final CompletionView getView() {
        if (view == null) {
            view = createView();
        }
        return view;
    }

    protected CompletionQuery createQuery() {
        return null;
    }

    public final CompletionQuery getQuery() {
        if (query == null) {
            query = createQuery();
        }
        return query;
    }
    
    public JavaDocPane getJavaDocPane(){
        if (javaDocPane == null){
            javaDocPane = new ScrollJavaDocPane(extEditorUI);
        }
        return javaDocPane;
    }
    
    /**
     * Get panel holding all aids (completion and documentation panes).
     * @return JDCPopupPanel or <code>null</code>
     */
    public final JDCPopupPanel getJDCPopupPanelIfExists() {
        return jdcPopupPanel;
    }

    /**
     * Get panel holding all aids (completion and documentation panes).
     * @return JDCPopupPanel never <code>null</code>
     */ 
    public JDCPopupPanel getJDCPopupPanel(){
        if (jdcPopupPanel == null){
            jdcPopupPanel =  new JDCPopupPanel(extEditorUI, getExtPane(), this);
        }
        return jdcPopupPanel;
    }

    /** Get the result of the last valid completion query or null
    * if there's no valid result available.
    */
    public synchronized final CompletionQuery.Result getLastResult() {
        return lastResult;
    }

    /** Reset the result of the last valid completion query. This
    * is done for example after the document was modified.
    */
    public synchronized final void invalidateLastResult() {
        currentTask().cancel();
        lastResult = null;
        caretPos = -1;
    }
     
    private synchronized void setKeyPressed(boolean value) {
        keyPressed = value;
    }
    
    private synchronized boolean isKeyPressed() {
        return keyPressed;
    }

    public synchronized Object getSelectedValue() {
        if (lastResult != null) {
            int index = getView().getSelectedIndex();
            if (index >= 0 && index<lastResult.getData().size()) {
                return lastResult.getData().get(index);
            }
        }
        return null;
    }

    /** Return true if the completion should popup automatically */
    public boolean isAutoPopupEnabled() {
        return autoPopup;
    }

    /** Return true when the pane exists and is visible.
    * This is the preferred method of testing the visibility of the pane
    * instead of <tt>getPane().isVisible()</tt> that forces
    * the creation of the pane.
    */
    public boolean isPaneVisible() {
        return (pane != null && pane.isVisible());
    }

    /** Set the visibility of the view. This method should
    * be used mainly for hiding the completion pane. If used
    * with visible set to true it calls the <tt>popup(false)</tt>.
    */
    public void setPaneVisible(boolean visible) {
        trace("ENTRY setPaneVisible " + visible); // NOI18N
        if (visible) {
            if (extEditorUI.getComponent() != null) {
                popupImpl(false);
            }
        } else {
            if (pane != null) {
                cancelRequestImpl();
                invalidateLastResult();
                getJDCPopupPanel().setCompletionVisible(false);
                caretPos=-1;
            }
        }
    }
    
    public void completionCancel(){
        trace("ENTRY completionCancel"); // NOI18N
        if (pane != null){
            cancelRequestImpl();
            invalidateLastResult();
            caretPos=-1;
        }
    }

    /** Refresh the contents of the view if it's currently visible.
    * @param postRequest post the request instead of refreshing the view
    *   immediately. The <tt>ExtSettingsNames.COMPLETION_REFRESH_DELAY</tt>
    *   setting stores the number of milliseconds before the view is refreshed.
    */
    public void refresh(boolean postRequest) {
        trace("ENTRY refresh " + postRequest); // NOI18N
        refreshImpl(postRequest);
    }

    private synchronized void refreshImpl(final boolean postRequest) {

        // exit immediatelly
        if (isPaneVisible() == false) return;
        
        class RefreshTask implements Runnable {
            private final boolean batch;
            RefreshTask(boolean batch) {
                this.batch = batch;
            }
            public void run() {
                if (isPaneVisible()) {
                    timer.stop();
                    if (batch) {
                        timer.setInitialDelay(refreshDelay);
                        timer.setDelay(refreshDelay);
                        timer.start();
                    } else {
                        actionPerformed(null);
                    }
                }
            }            
        };
        
        SwingUtilities.invokeLater(new RefreshTask(postRequest));        
    }
    
    /** Get the help and show it in the view. If the view is already visible
    * perform the refresh of the view.
    * @param postRequest post the request instead of displaying the view
    *   immediately. The <tt>ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY</tt>
    *   setting stores the number of milliseconds before the view is displayed.
    *   If the user presses a key until the delay expires nothing is shown.
    *   This guarantees that the user which knows what to write will not be
    *   annoyed with the unnecessary help.
    */
    public void popup(boolean postRequest) {
        trace("ENTRY popup " + postRequest); // NOI18N
        popupImpl(postRequest);
    }

    private synchronized void popupImpl( boolean postRequest) {
        if (isPaneVisible()) {
            refreshImpl(postRequest);
        } else {
            timer.stop();
            if (postRequest) {
                timer.setInitialDelay(autoPopupDelay);
                timer.setDelay(autoPopupDelay);
                timer.start();
            } else {
                actionPerformed(null);
            }
        }        
    }
    
    /** Cancel last request for either displaying or refreshing
    * the pane. It resets the internal timer.
    */
    public void cancelRequest() {
        trace("ENTRY cancelRequest"); // NOI18N
        cancelRequestImpl();
    }
    
    private synchronized void cancelRequestImpl() {
        timer.stop();
    }

    /** Called to do either displaying or refreshing of the view.
    * This method can be called either directly or because of the timer has fired.
    * @param evt event describing the timer firing or null
    *   if the method was called directly because of the synchronous
    *   showing/refreshing the view.
    */
    public synchronized void actionPerformed(ActionEvent evt) {

        if (jdcPopupPanel == null) extEditorUI.getCompletionJavaDoc(); //init javaDoc
        
        final JTextComponent component = extEditorUI.getComponent();
        BaseDocument doc = Utilities.getDocument(component);

        if (component != null && doc != null) {
            
            provokedByAutoPopup = evt != null;

            try{
                if((caretPos!=-1) && (Utilities.getRowStart(component,component.getCaret().getDot()) !=
                    Utilities.getRowStart(component,caretPos)) && ((component.getCaret().getDot()-caretPos)>0) ){
                        getJDCPopupPanel().setCompletionVisible(false);
                        caretPos=-1;
                        return;
                }
            }catch(BadLocationException ble){
            }
            
            caretPos = component.getCaret().getDot();

            // show progress view
            class PendingTask extends CancelableRunnable {
                public void run() {
                    if (cancelled()) return;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (cancelled()) return;
                            performWait();
                        }
                    });
                }                
            };

            // perform query and show results
            class QueryTask extends CancelableRunnable {
                private final CancelableRunnable wait;
                private final boolean isPaneVisible;
                public QueryTask(CancelableRunnable wait, boolean isPaneVisible) {
                    this.wait = wait;
                    this.isPaneVisible = isPaneVisible;
                }
                public void run() {
                    if (cancelled()) return;
                    try {
                        performQuery(component);
                    } catch ( ThreadDeath td ) {
                        throw td;
                    } catch (Throwable exc){
                        exc.printStackTrace();
                    }finally {
                        wait.cancel();
                        if (cancelled()) return;
                        SwingUtilities.invokeLater( new Runnable() {
                            public void run() {
                                if (cancelled()) return;
                                CompletionQuery.Result res = lastResult;
                                if (res != null) {
                                    if (instantSubstitution && res.getData().size() == 1 &&
                                        !isPaneVisible && instantSubstitution(caretPos)){
                                            setPaneVisible(false);
                                            return;
                                    }
                                }
                                
                                performResults();
                            }
                        });
                    }
                }
                void cancel() {
                    super.cancel();
                    wait.cancel();
                }
            };
                        
            // update current task: cancel pending task and fire new one

            currentTask().cancel();
            
            RequestProcessor rp;
            boolean reentrantProvider = getQuery() instanceof CompletionQuery.SupportsSpeculativeInvocation;
            if (reentrantProvider) {
                rp = RequestProcessor.getDefault();
            } else {
                rp = getSerialiazingRequestProcessor();
            }

            CancelableRunnable wait = new PendingTask();            
            CancelableRunnable task = new QueryTask(wait, getPane().isVisible());
            currentTask(task);
            if (provokedByAutoPopup == false) {
                RequestProcessor.getDefault().post(wait, 100);
            }
            rp.post(task);
        }
    }

    /**
     * Show wait completion result. Always called from AWT.
     */
    private void performWait() {
        getPane().setTitle(NbBundle.getBundle(org.netbeans.editor.BaseKit.class).getString("ext.Completion.wait"));
        getView().setResult((CompletionQuery.Result)null);
        if (isPaneVisible()) {
            getJDCPopupPanel().refresh();
        } else {
            getJDCPopupPanel().setCompletionVisible(true);
        }        
    }
    
    /**
     * Execute complegtion query subtask
     */
    private void performQuery(final JTextComponent target) {

        BaseDocument doc = Utilities.getDocument(target);
        long start = System.currentTimeMillis();
        try {
            lastResult = getQuery().query( target, caretPos, doc.getSyntaxSupport());
        } finally {
            trace("performQuery took " + (System.currentTimeMillis() - start) + "ms"); // NOI18N
            setKeyPressed(false);
        }
    }

    /**
     * Show result popup. Always called from AWT.
     */
    protected void performResults() {
        // sample
        CompletionQuery.Result res = lastResult;
        if (res != null) {
            
            if (instantSubstitution && res.getData().size() == 1 &&
                !isPaneVisible() && instantSubstitution(caretPos)) return;

            getPane().setTitle(res.getTitle());
            getView().setResult(res);
            if (isPaneVisible()) {
                getJDCPopupPanel().refresh();
            } else {
                getJDCPopupPanel().setCompletionVisible(true);
            }
        } else {
            getJDCPopupPanel().setCompletionVisible(false);
            
            if (!isKeyPressed()) {
                caretPos=-1;
            } else {
                setKeyPressed(false);
            }
        }        
    }
    
    /** Performs instant text substitution, provided that result contains only one 
     *  item and completion has been invoked at the end of the word.
     *  @param caretPos offset position of the caret
     */
    public boolean instantSubstitution(int caretPos){
        trace("ENTRY instantSubstitution " + caretPos); // NOI18N
        return instantSubstitutionImpl(caretPos);
    }
    
    private synchronized boolean instantSubstitutionImpl(int caretPos){
        if (getLastResult() == null) return false;
        JTextComponent comp = extEditorUI.getComponent();
        try{
            if (comp != null) {
                int[] block = Utilities.getIdentifierBlock(comp,caretPos);
                if (block == null || block[1] == caretPos)
                    return getLastResult().substituteText(0, false);
            }
        }catch(BadLocationException ble){
        }
        return false;
    }

    
    /** Substitute the document's text with the text
    * that is appopriate for the selection
    * in the view. This function is usually triggered
    * upon pressing the Enter key.
    * @return true if the substitution was performed
    *  false if not.
    */
    public synchronized boolean substituteText( boolean shift ) {        
        trace("ENTRY substituteText " + shift); // NOI18N
        if (lastResult != null) {
            int index = getView().getSelectedIndex();
            if (index >= 0) {
                lastResult.substituteText(index, shift );
            }
            return true;
        } else {
            return false;
        }
    }
    
    public synchronized boolean substituteSimpleText() {
        return false;
    }

    /** Substitute the text with the longest common
    * part of all the entries appearing in the view.
    * This function is usually triggered
    * upon pressing the Tab key.
    * @return true if the substitution was performed
    *  false if not.
    */
    public synchronized boolean substituteCommonText() {
        trace("ENTRY substituteCommonText"); // NOI18N
        if (lastResult != null) {
            int index = getView().getSelectedIndex();
            if (index >= 0) {
                lastResult.substituteCommonText(index);
            }
            return true;
        } else {
            return false;
        }
    }

    
    // Task management ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /**
     * Make given task current.
     */
    private void currentTask(CancelableRunnable task) {
        cancellable = task;
    }
    
    /**
     * Get current task
     */
    private CancelableRunnable currentTask() {
        return cancellable;
    }
    
    /**
     * Multistage task can test its cancel status after every atomic
     * (non-cancellable) stage.
     */
    abstract class CancelableRunnable implements Runnable {
        private boolean cancelled = false;
                
        boolean cancelled() {
            return cancelled;
        }
        
        void cancel() {
            cancelled = true;
        }
    }

    /**
     * Get serializing request processor.
     */
    private synchronized RequestProcessor getSerialiazingRequestProcessor() {
        if (serializingRequestProcessor == null) {
            serializingRequestProcessor = new RequestProcessor("editor.completion", 1);// NOI18N
        }
        return serializingRequestProcessor;
    }
    
    // Debug support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private static void trace(String msg) {
        if (DEBUG_COMPLETION && Boolean.getBoolean(PROP_DEBUG_COMPLETION)) {
            synchronized (System.err) {
                System.err.println(msg);
            }
        }
    }
}
