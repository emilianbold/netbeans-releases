/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.openide.text;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.modules.openide.text.Installer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.util.*;
import org.openide.windows.*;

/** Cloneable top component to hold the editor kit.
 */
public class CloneableEditor extends CloneableTopComponent implements CloneableEditorSupport.Pane {
    private static final String HELP_ID = "editing.editorwindow"; // !!! NOI18N
    static final long serialVersionUID = -185739563792410059L;

    private static final RequestProcessor RP = new RequestProcessor("org.openide.text Editor Initialization");
    
    /** Thread for postprocessing work in CloneableEditor.DoInitialize.initRest */
    private static final RequestProcessor RPPostprocessing = new RequestProcessor("org.openide.text Document Postprocessing");

    /** editor pane  */
    protected JEditorPane pane;

    /** Asociated editor support  */
    private CloneableEditorSupport support;

    /** Flag indicating it was initialized this <code>CloneableEditor</code> */
    private boolean initialized;

    /** Flag indicating progress of DoInitialize tasks */
    private boolean initVisualFinished;
    
    /** Flag indicating if AWT is waiting in getEditorPane on initVisual.
     It is static for now when we have only one worker thread/RP */
    private static boolean waitingOnInitVisual;

    /** Flag indicating if modal dialog for handling UQE is displayed.
     If it is yes we cannot handle call of getEditorPane from modal EQ because it results in deadlock. */
    private static boolean isModalDialog;
    
    /** Flag to detect if document loading in initNonVisual was canceled by user */
    private boolean isDocLoadingCanceled = false;

    /** Flag to detect if component is opened or closed to control return value
     * of getEditorPane. If component creation starts this flag is set to true and
     * getEditorPane waits till initialization finishes */
    private boolean isComponentOpened = false;
    
    /** Position of cursor. Used to keep the value between deserialization
     * and initialization time. */
    private int cursorPosition = -1;
    
    private final boolean[] CLOSE_LAST_LOCK = new boolean[1];

    private static List<AWTQuery> tbdList = Collections.synchronizedList(new ArrayList<AWTQuery>());

    private static List<AWTQuery> finishedList = Collections.synchronizedList(new ArrayList<AWTQuery>());
    // #20647. More important custom component.

    /** Custom editor component, which is used if specified by document
     * which implements <code>NbDocument.CustomEditor</code> interface.
     * @see NbDocument.CustomEditor#createEditor */
    private Component customComponent;
    private JToolBar customToolbar;
    private DoInitialize doInitialize;
    
    private static final Logger LOG = Logger.getLogger("org.openide.text.CloneableEditor"); // NOI18N
    
    /** For externalization of subclasses only  */
    public CloneableEditor() {
        this(null);
    }

    /** Creates new editor component associated with
    * support object.
    * @param support support that holds the document and operations above it
    */
    public CloneableEditor(CloneableEditorSupport support) {
        this(support, false);
    }

    /** Creates new editor component associated with
    * support object (possibly also with its 
    * {@link CloneableEditorSupport#CloneableEditorSupport(org.openide.text.CloneableEditorSupport.Env, org.openide.util.Lookup) lookup}.
    * 
    * @param support support that holds the document and operations above it
    * @param associateLookup true, if {@link #getLookup()} should return the lookup
    *   associated with {@link CloneableEditorSupport}.
    */
    public CloneableEditor(CloneableEditorSupport support, boolean associateLookup) {
        super();
        this.support = support;

        updateName();
        _setCloseOperation();
        setMinimumSize(new Dimension(10, 10));
        if (associateLookup) {
            associateLookup(support.getLookup());
        }
    }
    @SuppressWarnings("deprecation")
    private void _setCloseOperation() {
        setCloseOperation(CLOSE_EACH);
    }

    /** Gives access to {@link CloneableEditorSupport} object under
     * this <code>CloneableEditor</code> component.
     * @return the {@link CloneableEditorSupport} object
     *         that holds the document or <code>null</code>, what means
     *         this component is not in valid state yet and can be discarded */
    protected CloneableEditorSupport cloneableEditorSupport() {
        return support;
    }

    /** Overriden to explicitely set persistence type of CloneableEditor
     * to PERSISTENCE_ONLY_OPENED */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    /** Get context help for this editor pane.
     * If the registered editor kit provides a help ID in bean info
     * according to the protocol described for {@link HelpCtx#findHelp},
     * then that it used, else general help on the editor is provided.
     * @return context help
     */
    @Override
    public HelpCtx getHelpCtx() {
        Object kit = support.cesKit();
        HelpCtx fromKit = kit == null ? null : HelpCtx.findHelp(kit);

        if (fromKit != null) {
            return fromKit;
        } else {
            return new HelpCtx(HELP_ID);
        }
    }

    /**
     * Indicates whether this component can be closed.
     * Adds scheduling of "emptying" editor pane and removing all sub components.
     * {@inheritDoc}
     */
    @Override
    public boolean canClose() {
        boolean result = super.canClose();
        return result;
    }

    /** Overrides superclass method. In case it is called first time,
     * initializes this <code>CloneableEditor</code>. */
    @Override
    protected void componentShowing() {
        super.componentShowing();
        initialize();
    }

    /** Performs needed initialization  */
    private void initialize() {
        if (initialized || discard()) {
            return;
        }
        final QuietEditorPane tmp = new QuietEditorPane();

        tmp.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(CloneableEditor.class, "ACS_CloneableEditor_QuietEditorPane", this.getName())
        );
        tmp.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(
                CloneableEditor.class, "ACSD_CloneableEditor_QuietEditorPane",
                this.getAccessibleContext().getAccessibleDescription()
            )
        );
        tmp.putClientProperty("usedByCloneableEditor", true);

        this.pane = tmp;
        this.initialized = true;
        this.isComponentOpened = true;
        
        this.doInitialize = new DoInitialize(tmp);
    }

    
    final static Logger TIMER = Logger.getLogger("TIMER"); // NOI18N
    
    final boolean newInitialize() {
        if (Boolean.getBoolean("org.openide.text.CloneableEditor.oldInitialize")) { // NOI18N
            return false;
        }
        return !Boolean.TRUE.equals(getClientProperty("oldInitialize")); // NOI18N
    }

    /** Asks the associated {@link CloneableEditorSupport} to initialize
     * this editor via its {@link CloneableEditorSupport#initializeCloneableEditor(org.openide.text.CloneableEditor)}
     * method. By default called from the support on various occasions including
     * shortly after creation and 
     * after the {@link CloneableEditor} has been deserialized.
     * 
     * @since 6.37 
     */
    protected final void initializeBySupport() {
        cloneableEditorSupport().initializeCloneableEditor(this);
    }
    
    private void releasePane() {
        if (pane != null) {
            pane.putClientProperty("usedByCloneableEditor", false);
        }
        pane = null;
    }
    
    class AWTQuery implements Runnable {
        private UserQuestionException ex;
        private boolean isConfirmed ;

        public AWTQuery (UserQuestionException ex) {
            this.ex = ex;
        }

        public boolean confirmed () {
            return isConfirmed;
        }
        
        public void run() {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    ex.getLocalizedMessage(), NotifyDescriptor.YES_NO_OPTION
                );
            nd.setOptions(new Object[] { NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION });
            isModalDialog = true;
            Object res = DialogDisplayer.getDefault().notify(nd);
            isModalDialog = false;
            if (NotifyDescriptor.OK_OPTION.equals(res)) {
                isConfirmed = true;
            } else {
                isConfirmed = false;
            }
        }
    }
    
    class DoInitialize implements Runnable, ActionListener {
        private final QuietEditorPane tmp;
        private Document doc;
        private RequestProcessor.Task task;
        private int phase;
        private EditorKit kit;
        private JComponent tmpComp;
        /** Flag to avoid never ending wait in initDocument. It is to handle case
         *  when someone closes document ie. sets doc to null between initNonVisual
         *  and initDocument issue #136601.
         */
        private boolean initialized = false;
        /** Flag to avoid recursive call of initVisual. */
        private boolean isInInitVisual = false;

        private boolean confirmed = false;

        public DoInitialize(QuietEditorPane tmp) {
            this.tmp = tmp;
            this.tmpComp = initLoading();
            new Timer(1000, this).start();
            if (newInitialize()) {
                task = RP.create(this);
                task.setPriority(Thread.MIN_PRIORITY + 2);
                task.schedule(0);
            } else {
                run();
            }
        }
        
        private JComponent initLoading() {
            setLayout(new BorderLayout());

            JLabel loadingLbl = new JLabel(NbBundle.getMessage(CloneableEditor.class, "LBL_EditorLoading")); // NOI18N
            loadingLbl.setOpaque(true);
            loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
            loadingLbl.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
            loadingLbl.setVisible(false);
            add(loadingLbl, BorderLayout.CENTER);
            
            return loadingLbl;
        }

        public void actionPerformed(ActionEvent e) {
            tmpComp.setVisible(true);
            Timer t = (Timer)e.getSource();
            t.stop();
        }
        
        @SuppressWarnings("fallthrough")
        public void run() {
            long now = System.currentTimeMillis();
            
            int phaseNow = phase;
            switch (phase++) {
            case 0:
                synchronized (CLOSE_LAST_LOCK) {
                    CLOSE_LAST_LOCK[0] = true;
                }
                phase = initNonVisual(phase);
                synchronized (CLOSE_LAST_LOCK) {
                    if (!CLOSE_LAST_LOCK[0]) {
                        support.notifyClosed();
                        phase = Integer.MAX_VALUE;
                    }
                    CLOSE_LAST_LOCK[0] = false;
                }
                if (phase == Integer.MAX_VALUE) {
                    break;
                }
                if (newInitialize()) {
                    WindowManager.getDefault().invokeWhenUIReady(this);
                    break;
                }
            case 1:
                if (CloneableEditor.this.pane != this.tmp) {
                    //#138686: Cancel initialization when TC was closed in the meantime
                    //and pane is null or even different instance
                    phase = Integer.MAX_VALUE;
                    break;
                }
                if ((support.getDocument() == null) || (support.cesKit() == null)) {
                    //#138686, #161902, #149771: Cancel initialization when document was closed in the meantime
                    phase = Integer.MAX_VALUE;
                    break;
                }
                initVisual();
                if (newInitialize()) {
                    task.schedule(1000);
                    break;
                }
            case 2:
                initRest();
                break;
            default:
                throw new IllegalStateException("Wrong phase: " + phase + " for " + support);
            }
            
            if (phase >= 3) {
                CloneableEditor.this.doInitialize = null;
            }
            
            long howLong = System.currentTimeMillis() - now;
            if (TIMER.isLoggable(Level.FINE)) {
                String thread = SwingUtilities.isEventDispatchThread() ? "AWT" : "RP"; // NOI18N
                Document d = doc;
                Object who = d == null ? null : d.getProperty(Document.StreamDescriptionProperty);
                if (who == null) {
                    who = support.messageName();
                }
                TIMER.log(Level.FINE,  
                    "Open Editor, phase " + phaseNow + ", " + thread + " [ms]",
                    new Object[] { who, howLong}
                );
            }
        }
            
        private int initNonVisual (int phase) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE,"DoInitialize.initNonVisual Enter"
                + " Time:" + System.currentTimeMillis()
                + " Thread:" + Thread.currentThread().getName()
                + " ce:[" + Integer.toHexString(System.identityHashCode(CloneableEditor.this)) + "]"
                + " support:[" + Integer.toHexString(System.identityHashCode(support)) + "]"
                + " Name:" + CloneableEditor.this.getName());
            }
            Task prepareTask = support.prepareDocument();
            assert prepareTask != null : "Failed to get prepareTask";
            prepareTask.waitFinished();
            final Throwable ex = support.getPrepareDocumentRuntimeException();
            if (ex == null) {
                isDocLoadingCanceled = false;
            }
            if (support.asynchronousOpen()) {
                if (ex instanceof CloneableEditorSupport.DelegateIOExc) {
                    if (ex.getCause() instanceof UserQuestionException) {
                        class Query implements Runnable {
                            
                            private DoInitialize doInit;
                            boolean inAWT;
                            boolean finished;
                            
                            public Query (DoInitialize doInit) {
                                this.doInit = doInit;
                            }
                            
                            public void run() {
                                synchronized (this) {
                                    inAWT = true;
                                    notifyAll();
                                    if (finished) {
                                        return;
                                    }
                                }
                                try {
                                    doQuestion();
                                } finally {
                                    synchronized (this) {
                                        finished = true;
                                        notifyAll();
                                    }
                                }
                            }

                            private void doQuestion() {
                                UserQuestionException e = (UserQuestionException) ex.getCause();
                                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                                        e.getLocalizedMessage(), NotifyDescriptor.YES_NO_OPTION
                                    );
                                nd.setOptions(new Object[] { NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION });
                                isModalDialog = true;
                                Object res = DialogDisplayer.getDefault().notify(nd);
                                isModalDialog = false;
                                if (NotifyDescriptor.OK_OPTION.equals(res)) {
                                    doInit.confirmed = true;
                                } else {
                                    return;
                                }
                            }

                            public synchronized boolean awaitAWT() throws InterruptedException {
                                if (!inAWT) {
                                    wait(10000);
                                }
                                return inAWT;
                            }

                            public synchronized void waitRest() throws InterruptedException {
                                while (inAWT && !finished) {
                                    wait();
                                }
                            }
                        }
                        //If AWT is blocked by call of CloneableEditor.getEditorPane ie. by waiting on initVisual
                        //we cannot unblock it so we will confirm UQE for now.
                        if (waitingOnInitVisual) {
                            UserQuestionException e = (UserQuestionException) ex.getCause();
                            AWTQuery query = new AWTQuery(e);
                            tbdList.add(query);
                            synchronized (this) {
                                //Wake up AWT thread to process queries
                                notifyAll();
                                try {
                                    for (;;) {
                                        //Wait for processed query
                                        if (!finishedList.isEmpty()) {
                                            //Check if our query is already processed
                                            if (finishedList.remove(query)) {
                                                break;
                                            }
                                        }
                                        wait();
                                    }
                                } catch (InterruptedException exc) {
                                    Exceptions.printStackTrace(exc);
                                }
                            }
                            if (query.isConfirmed) {
                                confirmed = true;
                                try {
                                    e.confirmed();
                                } catch (IOException ex1) {
                                    Exceptions.printStackTrace(ex1);
                                }
                            }
                        } else {
                            Query query = new Query(this);
                            try {
                                SwingUtilities.invokeLater(query);
                                if (query.awaitAWT()) {
                                    query.waitRest();
                                } else {
                                    //#175956: If AWT is blocked so user could not answer UQE.
                                    //In such case open document anyway.
                                    confirmed = true;
                                }
                                synchronized (query) {
                                    query.finished = true;
                                }
                            } catch (InterruptedException exc) {
                                Exceptions.printStackTrace(exc);
                            }
                        }
                        if (confirmed) {
                            isDocLoadingCanceled = false;
                            UserQuestionException e = (UserQuestionException) ex.getCause();
                            try {
                                e.confirmed();
                            } catch (IOException ex1) {
                                Exceptions.printStackTrace(ex1);
                            }
                            prepareTask = support.prepareDocument();
                            assert prepareTask != null : "Failed to get prepareTask";
                            prepareTask.waitFinished();
                        } else {
                            cancelLoadingAndCloseEditor();
                            return Integer.MAX_VALUE;
                        }
                    } else {
                        Exceptions.printStackTrace(ex.getCause());
                        cancelLoadingAndCloseEditor();
                        return Integer.MAX_VALUE;
                    }
                }
            } else {
                if (ex instanceof CloneableEditorSupport.DelegateIOExc) {
                    if (ex.getCause() instanceof UserQuestionException) {
                        UserQuestionException e = (UserQuestionException) ex.getCause();
                        try {
                            e.confirmed();
                        } catch (IOException ioe) {
                        }
                        prepareTask = support.prepareDocument();
                        assert prepareTask != null : "Failed to get prepareTask";
                        prepareTask.waitFinished();
                    } else {
                        Exceptions.printStackTrace(ex.getCause());
                        cancelLoadingAndCloseEditor();
                        return Integer.MAX_VALUE;
                    }
                }
            }
            // Init action map: cut,copy,delete,paste actions.
            javax.swing.ActionMap am = getActionMap();

            //#43157 - editor actions need to be accessible from outside using the TopComponent.getLookup(ActionMap.class) call.
            // used in main menu enabling/disabling logic.
            javax.swing.ActionMap paneMap = tmp.getActionMap();
            am.setParent(paneMap);

            //#41223 set the defaults befor the custom editor + kit get initialized, giving them opportunity to
            // override defaults..
            paneMap.put(DefaultEditorKit.cutAction, getAction(DefaultEditorKit.cutAction));
            paneMap.put(DefaultEditorKit.copyAction, getAction(DefaultEditorKit.copyAction));
            paneMap.put("delete", getAction(DefaultEditorKit.deleteNextCharAction)); // NOI18N
            paneMap.put(DefaultEditorKit.pasteAction, getAction(DefaultEditorKit.pasteAction));

            EditorKit k = support.cesKit();
            if (newInitialize() && k instanceof Callable) {
                try {
                    ((Callable) k).call();
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
            
            synchronized (this) {
                //Last fix of #138686 makes sure doc is not gc'ed till we keep reference to prepareDocument task
                //so we can use support.getDocument instead of support.openDocument
                doc = support.getDocument();
                if (doc == null && LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE,"DoInitialize.initNonVisual CANNOT set kit because doc is null!"
                    + " Time:" + System.currentTimeMillis()
                    + " Thread:" + Thread.currentThread().getName()
                    + " ce:[" + Integer.toHexString(System.identityHashCode(CloneableEditor.this)) + "]"
                    + " this:[" + Integer.toHexString(System.identityHashCode(this)) + "]"
                    + " support:[" + Integer.toHexString(System.identityHashCode(support)) + "]"
                    + " Name:" + CloneableEditor.this.getName()
                    + " kit:" + kit);
                }
                kit = k;
                initialized = true;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE,"DoInitialize.initNonVisual doc and kit are set"
                    + " Time:" + System.currentTimeMillis()
                    + " Thread:" + Thread.currentThread().getName()
                    + " ce:[" + Integer.toHexString(System.identityHashCode(CloneableEditor.this)) + "]"
                    + " this:[" + Integer.toHexString(System.identityHashCode(this)) + "]"
                    + " support:[" + Integer.toHexString(System.identityHashCode(support)) + "]"
                    + " Name:" + CloneableEditor.this.getName()
                    + " doc:" + doc
                    + " kit:" + kit);
                    LOG.log(Level.FINE,"DoInitialize.initNonVisual Call notifyAll"
                    + " Time:" + System.currentTimeMillis()
                    + " Thread:" + Thread.currentThread().getName()
                    + " [" + Integer.toHexString(System.identityHashCode(CloneableEditor.this)) + "]"
                    + " Name:" + CloneableEditor.this.getName());
                }
                // It appears that the doc can be null - likely when CES.closeDocument()
                // gets called by another thread.
                if (doc == null) {
                    phase = Integer.MAX_VALUE; // Stop any further processing
                }
                notifyAll();
            }
            return phase;
        }
        
        private void cancelLoadingAndCloseEditor() {
            isDocLoadingCanceled = true;
            synchronized (this) {
                //Wake up AWT thread to cancel initVisual
                notifyAll();
            }
            //Cancel initialization sequence and close editor
            isComponentOpened = false;
            SwingUtilities.invokeLater(new Runnable () {
                @Override
                public void run () {
                    TopComponent toClose = ( TopComponent ) SwingUtilities.getAncestorOfClass( TopComponent.class, CloneableEditor.this );
                    if( null == toClose )
                        toClose = CloneableEditor.this;
                    toClose.close();
                }
            });
        }
        
        private void initCustomEditor() {
            if (doc instanceof NbDocument.CustomEditor) {
                NbDocument.CustomEditor ce = (NbDocument.CustomEditor) doc;
                customComponent = ce.createEditor(tmp);
                
                if (customComponent == null) {
                    throw new IllegalStateException(
                        "Document:" + doc // NOI18N
                         + " implementing NbDocument.CustomEditor may not" // NOI18N
                         + " return null component" // NOI18N
                    );
                }
            }            
        }

        private void initDecoration() {
            if (doc instanceof NbDocument.CustomToolbar) {
                NbDocument.CustomToolbar ce = (NbDocument.CustomToolbar) doc;
                customToolbar = ce.createToolbar(tmp);

                if (customToolbar == null) {
                    throw new IllegalStateException(
                        "Document:" + doc // NOI18N
                         +" implementing NbDocument.CustomToolbar may not" // NOI18N
                         +" return null toolbar"
                    ); // NOI18N
                }
            }            
        }
        
        private boolean initDocument() {
            EditorKit k;
            Document d;
            synchronized (this) {
                for (;;) {
                    if (!tbdList.isEmpty()) {
                        while (!tbdList.isEmpty()) {
                            AWTQuery query = tbdList.remove(0);
                            query.run();
                            finishedList.add(query);
                        }
                        notifyAll();
                    }
                    d = doc;
                    k = kit;
                    if (initialized) {
                        break;
                    }
                    if (isDocLoadingCanceled) {
                        //Reset pane here so getEditorPane will return null
                        releasePane();
                        return false;
                    }
                    try {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE,"DoInitialize.initDocument Starting wait"
                            + " Time:" + System.currentTimeMillis()
                            + " Thread:" + Thread.currentThread().getName()
                            + " ce:[" + Integer.toHexString(System.identityHashCode(CloneableEditor.this)) + "]"
                            + " this:[" + Integer.toHexString(System.identityHashCode(this)) + "]"
                            + " support:[" + Integer.toHexString(System.identityHashCode(support)) + "]"
                            + " Name:" + CloneableEditor.this.getName()
                            + " doc:" + d
                            + " kit:" + k);
                        }
                        wait();
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE,"DoInitialize.initDocument Wait finished"
                            + " Thread:" + Thread.currentThread().getName()
                            + " ce:[" + Integer.toHexString(System.identityHashCode(CloneableEditor.this)) + "]"
                            + " this:[" + Integer.toHexString(System.identityHashCode(this)) + "]"
                            + " support:[" + Integer.toHexString(System.identityHashCode(support)) + "]"
                            + " Name:" + CloneableEditor.this.getName()
                            + " doc:" + d
                            + " kit:" + k);
                        }
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            if (d == null || tmp.getDocument() == d) {
                return false;
            }
            tmp.setEditorKit(k);
            // #132669, do not fire prior setting the kit, which by itself sets a bogus document, etc.
            // if this is a problem please revert the change and initialize QuietEditorPane.working = FIRE
            // and reopen #132669
            tmp.setWorking(QuietEditorPane.FIRE);
            tmp.setDocument(d);
            return true;
        }
        
        final void initVisual() {
            if (isDocLoadingCanceled) {
                return;
            }
            //Do not allow recursive call
            if (isInInitVisual) {
                return;
            }
            isInInitVisual = true;
            // wait for document and init it
            if (!initDocument()) {
                isInInitVisual = false;
                return;
            }
            if (LOG.isLoggable(Level.FINE)) {
                /*Exception ex = new Exception();
                StringWriter sw = new StringWriter(500);
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);*/
                LOG.log(Level.FINE,"DoInitialize.initVisual Enter"
                + " Time:" + System.currentTimeMillis()
                + " Thread:" + Thread.currentThread().getName()
                + " ce:[" + Integer.toHexString(System.identityHashCode(CloneableEditor.this)) + "]"
                + " this:[" + Integer.toHexString(System.identityHashCode(this)) + "]"
                + " support:[" + Integer.toHexString(System.identityHashCode(support)) + "]"
                + " Name:" + CloneableEditor.this.getName());
                //+ " " + sw.toString());
            }
            initCustomEditor();
            if (customComponent != null) {
                add(support.wrapEditorComponent(customComponent), BorderLayout.CENTER);
            } else { // not custom editor

                // remove default JScrollPane border, borders are provided by window system
                JScrollPane noBorderPane = new JScrollPane(tmp);
                tmp.setBorder(null);
                add(support.wrapEditorComponent(noBorderPane), BorderLayout.CENTER);
            }

            initDecoration();
            if (customToolbar != null) {
                Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
                customToolbar.setBorder(b);
                add(customToolbar, BorderLayout.NORTH);
            }
            remove(tmpComp);

            tmp.setWorking(QuietEditorPane.ALL);

            // set the caret to right possition if this component was deserialized
            if (cursorPosition != -1) {
                Caret caret = tmp.getCaret();

                if (caret != null) {
                    caret.setDot(cursorPosition);
                }
            }
            ActionMap p = getActionMap().getParent();
            getActionMap().setParent(null);
            getActionMap().setParent(p);
            //#134910: If editor TopComponent is already activated request focus
            //to it again to get focus to correct subcomponent eg. QuietEditorPane which
            //is added above.
            if (shouldRequestFocus(tmp)) {
                LOG.log(Level.FINE, "requestFocusInWindow {0}", tmp);
                requestFocusInWindow();
            }
            //#162961, #167289: Force repaint of editor. Sometimes editor stays empty.
            SwingUtilities.invokeLater(new Runnable () {
                @Override
                public void run () {
                    revalidate();
                }
            });
            isInInitVisual = false;
            initVisualFinished = true;

            //#168415: Notify clients that pane creation is finished.
            CloneableEditorSupport ces = cloneableEditorSupport();
            if (ces != null) {
                ces.firePropertyChange(EditorCookie.Observable.PROP_OPENED_PANES, null, null);
            }
        }

        private boolean shouldRequestFocus(Component c) {
            final TopComponent active = getRegistry().getActivated();
            while (c != null) {
                if (c == active) {
                    return true;
                }
                c = c.getParent();
            }
            return false;
        }
        
        private void initRest() {
            //#132662 Post this task to another worker private thread
            //to avoid deadlock.
            RPPostprocessing.post(new Runnable() {
                public void run() {
                    support.ensureAnnotationsLoaded();
                }
            });
        }
    } // end of DoInitialize
    
    @Override
    protected CloneableTopComponent createClonedObject() {
        return support.createCloneableTopComponent();
    }

    /** Descendants overriding this method must either call
     * this implementation or fire the
     * {@link org.openide.cookies.EditorCookie.Observable#PROP_OPENED_PANES}
     * property change on their own.
     */
    @Override
    protected void componentOpened() {
        super.componentOpened();
        
        CloneableEditorSupport ces = cloneableEditorSupport();
        
        if (ces != null) {
            ces.firePropertyChange(EditorCookie.Observable.PROP_OPENED_PANES, null, null);
            Document d = ces.getDocument();
            if (d != null) {
                String mimeType = (String) d.getProperty("mimeType"); //NOI18N
                Installer.add(mimeType);
            }
        }
    }
    
    /** Descendants overriding this method must either call
     * this implementation or fire the
     * {@link org.openide.cookies.EditorCookie.Observable#PROP_OPENED_PANES}
     * property change on their own.
     */
    @Override
    protected void componentClosed() {
        // #23486: pane could not be initialized yet.
        if (pane != null) {
            // #114608 - commenting out setting of the empty document
//                        Document doc = support.createStyledDocument(pane.getEditorKit());
//                        pane.setDocument(doc);

            // #138611 - this calls kit.deinstall, which is what our kits expect,
            // calling it with null does not impact performance, because the pane
            // will not create new document and typically nobody listens on "editorKit" prop change
            pane.setEditorKit(null);
        }

        customComponent = null;
        customToolbar = null;
        releasePane();
        initialized = false;
        initVisualFinished = false;
        isComponentOpened = false;
        
        super.componentClosed();

        CloneableEditorSupport ces = cloneableEditorSupport();

        if (ces != null) {
            ces.firePropertyChange(EditorCookie.Observable.PROP_OPENED_PANES, null, null);
        }
        if (ces.getAnyEditor() == null) {
            ces.close(false);
        }
    }

    /** When closing last view, also close the document. 
     * Calls {@link #closeLast(boolean) closeLast(true)}.
     * @return <code>true</code> if close succeeded
     */
    @Override
    protected boolean closeLast() {
        return closeLast(true);
    }
    
    /** Utility method to close the document. 
     * 
     * @param ask verify and ask the user whether a document can be closed or not?
     * @return true if the document was successfully closed
     * @since 6.37
     */
    protected final boolean closeLast(boolean ask) {
        if (ask) {
            if (!support.canClose()) {
                // if we cannot close the last window
                return false;
            }
        }

        // close everything and do not ask
        synchronized (CLOSE_LAST_LOCK) {
            if (CLOSE_LAST_LOCK[0]) {
                CLOSE_LAST_LOCK[0] = false;
            } else {
                support.notifyClosed();
            }
        }

        if (support.getLastSelected() == this) {
            support.setLastSelected(null);
        }

        return true;
    }

    /** The undo/redo manager of the support.
     * @return the undo/redo manager shared by all editors for this support
     */
    @Override
    public UndoRedo getUndoRedo() {
        return support.getUndoRedo();
    }

    @Override
    public Action[] getActions() {
        List<Action> actions = new ArrayList<Action>(Arrays.asList(super.getActions()));
        // XXX nicer to use MimeLookup for type-specific actions, but not easy; see org.netbeans.modules.editor.impl.EditorActionsProvider
        actions.add(null);
        actions.addAll(Utilities.actionsForPath("Editors/TabActions"));
        return actions.toArray(new Action[actions.size()]);
    }

    /** Transfer the focus to the editor pane.
     */
    @Deprecated
    @Override
    public void requestFocus() {
        super.requestFocus();

        if (pane != null) {
            if ((customComponent != null) && !SwingUtilities.isDescendingFrom(pane, customComponent)) {
                customComponent.requestFocus();
            } else {
                pane.requestFocus();
            }
        }
    }

    /** Transfer the focus to the editor pane.
     */
    @Deprecated
    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();

        if (pane != null) {
            if ((customComponent != null) && !SwingUtilities.isDescendingFrom(pane, customComponent)) {
                return customComponent.requestFocusInWindow();
            } else {
                return pane.requestFocusInWindow();
            }
        }

        return false;
    }

    @Deprecated
    @Override
    public boolean requestDefaultFocus() {
        if ((customComponent != null) && !SwingUtilities.isDescendingFrom(pane, customComponent)) {
            return customComponent.requestFocusInWindow();
        } else if (pane != null) {
            return pane.requestFocusInWindow();
        }

        return false;
    }

    // XXX is this method really needed?
    /** @return Preferred size of editor top component  */
    @Override
    public Dimension getPreferredSize() {
        @SuppressWarnings("deprecation")
        Rectangle bounds = WindowManager.getDefault().getCurrentWorkspace().getBounds();

        return new Dimension(bounds.width / 2, bounds.height / 2);
    }

    @Override
    public void open() {
        boolean wasNull = getClientProperty( "TopComponentAllowDockAnywhere" ) == null; //NOI18N
        super.open();
        if( wasNull ) {
            //since we don't define a mode to dock this editor to, the window
            //system thinks we're an uknown component allowed to dock anywhere
            //but editor windows can dock into editor modes only, so let's clear
            //the 'special' flag
            putClientProperty( "TopComponentAllowDockAnywhere", null); //NOI18N
        }
    }

    private Action getAction(String key) {
        if (key == null) {
            return null;
        }

        // Try to find the action from kit.
        EditorKit kit = support.cesKit();

        if (kit == null) { // kit is cleared in closeDocument()

            return null;
        }

        Action[] actions = kit.getActions();

        for (int i = 0; i < actions.length; i++) {
            if (key.equals(actions[i].getValue(Action.NAME))) {
                return actions[i];
            }
        }

        return null;
    }
    
    /**
     * Overrides superclass method. Remembers last selected component of
     * support belonging to this component.
     *
     * Descendants overriding this method must call this implementation to set last
     * selected pane otherwise <code>CloneableEditorSupport.getRecentPane</code> and
     * <code>CloneableEditorSupport.getOpenedPanes</code> will be broken.
     *
     * @see #componentDeactivated
     */
    @Override
    protected void componentActivated() {
        support.setLastSelected(this);
    }
    
    /** Updates the name and tooltip of this <code>CloneableEditor</code>
     * {@link org.openide.windows.TopComponent TopCompoenent}
     * according to the support retrieved from {@link #cloneableEditorSupport}
     * method. The name and tooltip are in case of support presence
     * updated thru its {@link CloneableEditorSupport#messageName} and
     * {@link CloneableEditorSupport#messageToolTip} methods.
     * @see #cloneableEditorSupport() */
    public void updateName() {
        final CloneableEditorSupport ces = cloneableEditorSupport();

        if (ces != null) {
            Mutex.EVENT.writeAccess(
                new Runnable() {
                    public void run() {
                        String name = ces.messageHtmlName();
                        setHtmlDisplayName(name);
                        name = ces.messageName();
                        setDisplayName(name);
                        setName(name); // XXX compatibility

                        setToolTipText(ces.messageToolTip());
                    }
                }
            );
        }
    }

    // override for simple and consistent IDs
    @Override
    protected String preferredID() {
        final CloneableEditorSupport ces = cloneableEditorSupport();

        if (ces != null) {
            return ces.documentID();
        }

        return "";
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        // Save environent if support is non-null.
        // XXX #13685: When support is null, the tc will be discarded 
        // after deserialization.
        out.writeObject((support != null) ? support.cesEnv() : null);

        // #16461 Caret could be null?!,
        // hot fix - making it robust for that case.
        int pos = 0;

        // 19559 Even pane could be null! Better solution would be put
        // writeReplace method in place also, but it is a API change. For
        // the time be just robust here.
        JEditorPane p = pane;

        if (p != null) {
            Caret caret = p.getCaret();

            if (caret != null) {
                pos = caret.getDot();
            } else {
                if (p instanceof QuietEditorPane) {
                    int lastPos = ((QuietEditorPane) p).getLastPosition();

                    if (lastPos == -1) {
                        Logger.getLogger(CloneableEditor.class.getName()).log(Level.WARNING, null,
                                          new java.lang.IllegalStateException("Pane=" +
                                                                              p +
                                                                              "was not initialized yet!"));
                    } else {
                        pos = lastPos;
                    }
                } else {
                    Document doc = ((support != null) ? support.getDocument() : null);

                    // Relevant only if document is non-null?!
                    if (doc != null) {
                        Logger.getLogger(CloneableEditor.class.getName()).log(Level.WARNING, null,
                                          new java.lang.IllegalStateException("Caret is null in editor pane=" +
                                                                              p +
                                                                              "\nsupport=" +
                                                                              support +
                                                                              "\ndoc=" +
                                                                              doc));
                    }
                }
            }
        }

        out.writeObject(new Integer(pos));
        out.writeBoolean(getLookup() == support.getLookup());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        int offset;

        Object firstObject = in.readObject();

        // New deserialization that uses Env environment,
        // and which could be null(!) see writeExternal.
        if (firstObject instanceof CloneableOpenSupport.Env) {
            CloneableOpenSupport.Env env = (CloneableOpenSupport.Env) firstObject;
            CloneableOpenSupport os = env.findCloneableOpenSupport();
            support = (CloneableEditorSupport) os;
        }

        // load cursor position
        offset = ((Integer) in.readObject()).intValue();

        if (!discard()) {
            cursorPosition = offset;
        }

        updateName();
        isComponentOpened = true;
        if (in.available() > 0) {
            boolean associate = in.readBoolean();
            if (associate && support != null) {
                associateLookup(support.getLookup());
            }
        }
    }

    /**
     * Replaces serializing object. Overrides superclass method. Adds checking
     * for object validity. In case this object is invalid
     * throws {@link java.io.NotSerializableException NotSerializableException}.
     * @throws ObjectStreamException When problem during serialization occures.
     * @throws NotSerializableException When this <code>CloneableEditor</code>
     *               is invalid and doesn't want to be serialized. */
    @Override
    protected Object writeReplace() throws ObjectStreamException {
        if (discard()) {
            throw new NotSerializableException("Serializing component is invalid: " + this); // NOI18N
        }

        return super.writeReplace();
    }

    /**
     * Resolves deserialized object. Overrides superclass method. Adds checking
     * for object validity. In case this object is invalid
     * throws {@link java.io.InvalidObjectException InvalidObjectException}.
     * @throws ObjecStreamException When problem during serialization occures.
     * @throws InvalidObjectException When deserialized <code>CloneableEditor</code>
     *              is invalid and shouldn't be used. */
    protected Object readResolve() throws ObjectStreamException {
        if (discard()) {
            throw new java.io.InvalidObjectException("Deserialized component is invalid: " + this); // NOI18N
        } else {
            initializeBySupport();

            return this;
        }
    }

    /** This component should be discarded if the associated environment
    * is not valid.
    */
    private boolean discard() {
        return (support == null) || !support.cesEnv().isValid();
    }
    
    //
    // Implements the CloneableEditorSupport.Pane interface
    //
    public CloneableTopComponent getComponent() {
        return this;
    }

    /**
     * #168415: Returns true if creation of editor pane is finished. It is used
     * to avoid blocking AWT thread by call of getEditorPane.
     */
    boolean isEditorPaneReady () {
        assert SwingUtilities.isEventDispatchThread();
        return initVisualFinished;
    }

    /** Used from test only. Can be called out of AWT */
    boolean isEditorPaneReadyTest () {
        return initVisualFinished;
    }

    /** Returns editor pane. Returns null if document loading was canceled by user
     * through answering UserQuestionException or when CloneableEditor is closed.
     *
     * @return editor pane or null
     */
    public JEditorPane getEditorPane() {
        assert SwingUtilities.isEventDispatchThread();
        //User selected not to load document
        if (isDocLoadingCanceled || !isComponentOpened) {
            return null;
        }
        //#175528: This case should not happen as modal dialog handling UQE should
        //not be displayed during IDE start ie. during component deserialization.
        if (isModalDialog) {
            LOG.log(Level.WARNING,"AWT is blocked by modal dialog. Return null from CloneableEditor.getEditorPane."
            + " Please report this to IZ.");
            LOG.log(Level.WARNING,"support:" + support.getClass().getName());
            Exception ex = new Exception();
            StringWriter sw = new StringWriter(500);
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            LOG.log(Level.WARNING,sw.toString());
            return null;
        }
        initialize();
        DoInitialize d = doInitialize;
        if (d != null && !Thread.holdsLock(support.getLock())) {
            waitingOnInitVisual = true;
            d.initVisual();
            waitingOnInitVisual = false;
        }
        return pane;
    }

    /**
     * callback for the Pane implementation to adjust itself to the openAt() request.
     */
    public void ensureVisible() {
        open();
        requestVisible();
    }
}
