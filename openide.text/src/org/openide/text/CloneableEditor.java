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
package org.openide.text;

import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;


/** Cloneable top component to hold the editor kit.
 */
public class CloneableEditor extends CloneableTopComponent implements CloneableEditorSupport.Pane {
    private static final String HELP_ID = "editing.editorwindow"; // !!! NOI18N
    static final long serialVersionUID = -185739563792410059L;

    /** editor pane  */
    protected JEditorPane pane;

    /** Asociated editor support  */
    private CloneableEditorSupport support;

    /** Flag indicating it was initialized this <code>CloneableEditor</code> */
    private boolean initialized;

    /** Position of cursor. Used to keep the value between deserialization
     * and initialization time. */
    private int cursorPosition = -1;

    // #20647. More important custom component.

    /** Custom editor component, which is used if specified by document
     * which implements <code>NbDocument.CustomEditor</code> interface.
     * @see NbDocument.CustomEditor#createEditor */
    private Component customComponent;
    private JToolBar customToolbar;

    /** For externalization of subclasses only  */
    public CloneableEditor() {
        this(null);
    }

    /** Creates new editor component associated with
    * support object.
    * @param support support that holds the document and operations above it
    */
    public CloneableEditor(CloneableEditorSupport support) {
        super();
        this.support = support;

        updateName();
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
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    /** Get context help for this editor pane.
     * If the registered editor kit provides a help ID in bean info
     * according to the protocol described for {@link HelpCtx#findHelp},
     * then that it used, else general help on the editor is provided.
     * @return context help
     */
    public HelpCtx getHelpCtx() {
        HelpCtx fromKit = HelpCtx.findHelp(support.kit());

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
    public boolean canClose() {
        boolean result = super.canClose();

        if (result) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        // #23486: pane could not be initialized yet.
                        if (pane != null) {
                            Document doc = support.createStyledDocument(pane.getEditorKit());
                            pane.setDocument(doc);
                            pane.setEditorKit(null);
                        }

                        removeAll();
                        initialized = false;
                    }
                }
            );
        }

        return result;
    }

    /** Overrides superclass method. In case it is called first time,
     * initializes this <code>CloneableEditor</code>. */
    protected void componentShowing() {
        super.componentShowing();
        initialize();
    }

    /** Performs needed initialization  */
    private void initialize() {
        if (initialized || discard()) {
            return;
        }

        initialized = true;

        Task prepareTask = support.prepareDocument();

        // load the doc synchronously
        prepareTask.waitFinished();

        Document doc = support.getDocument();

        setLayout(new BorderLayout());

        final QuietEditorPane pane = new QuietEditorPane();

        pane.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(CloneableEditor.class, "ACS_CloneableEditor_QuietEditorPane", this.getName())
        );
        pane.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(
                CloneableEditor.class, "ACSD_CloneableEditor_QuietEditorPane",
                this.getAccessibleContext().getAccessibleDescription()
            )
        );

        this.pane = pane;

        // Init action map: cut,copy,delete,paste actions.
        javax.swing.ActionMap am = getActionMap();

        //#43157 - editor actions need to be accessible from outside using the TopComponent.getLookup(ActionMap.class) call.
        // used in main menu enabling/disabling logic.
        javax.swing.ActionMap paneMap = pane.getActionMap();
        am.setParent(paneMap);

        //#41223 set the defaults befor the custom editor + kit get initialized, giving them opportunity to
        // override defaults..
        paneMap.put(DefaultEditorKit.cutAction, getAction(DefaultEditorKit.cutAction));
        paneMap.put(DefaultEditorKit.copyAction, getAction(DefaultEditorKit.copyAction));
        paneMap.put("delete", getAction(DefaultEditorKit.deleteNextCharAction)); // NOI18N
        paneMap.put(DefaultEditorKit.pasteAction, getAction(DefaultEditorKit.pasteAction));

        pane.setEditorKit(support.kit());

        pane.setDocument(doc);

        if (doc instanceof NbDocument.CustomEditor) {
            NbDocument.CustomEditor ce = (NbDocument.CustomEditor) doc;
            customComponent = ce.createEditor(pane);

            if (customComponent == null) {
                throw new IllegalStateException(
                    "Document:" + doc // NOI18N
                     +" implementing NbDocument.CustomEditor may not" // NOI18N
                     +" return null component"
                ); // NOI18N
            }

            add(customComponent, BorderLayout.CENTER);
        } else { // not custom editor

            // remove default JScrollPane border, borders are provided by window system
            JScrollPane noBorderPane = new JScrollPane(pane);
            pane.setBorder(null);
            add(noBorderPane, BorderLayout.CENTER);
        }

        if (doc instanceof NbDocument.CustomToolbar) {
            NbDocument.CustomToolbar ce = (NbDocument.CustomToolbar) doc;
            customToolbar = ce.createToolbar(pane);

            if (customToolbar == null) {
                throw new IllegalStateException(
                    "Document:" + doc // NOI18N
                     +" implementing NbDocument.CustomToolbar may not" // NOI18N
                     +" return null toolbar"
                ); // NOI18N
            }

            Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
            customToolbar.setBorder(b);
            add(customToolbar, BorderLayout.NORTH);
        }

        pane.setWorking(QuietEditorPane.ALL);

        // set the caret to right possition if this component was deserialized
        if (cursorPosition != -1) {
            Caret caret = pane.getCaret();

            if (caret != null) {
                caret.setDot(cursorPosition);
            }
        }

        support.ensureAnnotationsLoaded();
    }

    protected CloneableTopComponent createClonedObject() {
        return support.createCloneableTopComponent();
    }

    /** Descendants overriding this method must either call
     * this implementation or fire the
     * {@link org.openide.cookies.EditorCookie.Observable#PROP_OPENED_PANES}
     * property change on their own.
     */
    protected void componentOpened() {
        super.componentOpened();

        CloneableEditorSupport ces = cloneableEditorSupport();

        if (ces != null) {
            ces.firePropertyChange(EditorCookie.Observable.PROP_OPENED_PANES, null, null);
        }
    }

    /** Descendants overriding this method must either call
     * this implementation or fire the
     * {@link org.openide.cookies.EditorCookie.Observable#PROP_OPENED_PANES}
     * property change on their own.
     */
    protected void componentClosed() {
        super.componentClosed();

        CloneableEditorSupport ces = cloneableEditorSupport();

        if (ces != null) {
            ces.firePropertyChange(EditorCookie.Observable.PROP_OPENED_PANES, null, null);
        }
    }

    /** Overrides superclass version. Opens top component only if
     * it is in valid state.
     * (Editor top component may become invalid after deserialization).<br>
     * Also tries to open all other top components which are docked
     * in editor mode on given workspace, but not visible.<br>
     */
    public void open(Workspace workspace) {
        if (discard()) {
            ErrorManager.getDefault().log(
                ErrorManager.WARNING,
                "Can not open " + this + " component," // NOI18N
                 +" its support environment is not valid" // NOI18N
                 +" [support=" + support + ", env=" // NOI18N
                 +((support == null) ? null : support.env()) + "]"
            ); // NOI18N
        } else {
            Workspace realWorkspace = (workspace == null) ? WindowManager.getDefault().getCurrentWorkspace() : workspace;
            dockIfNeeded(realWorkspace);
            super.open(workspace);
        }
    }

    /** When closing last view, also close the document.
     * @return <code>true</code> if close succeeded
     */
    protected boolean closeLast() {
        if (!support.canClose()) {
            // if we cannot close the last window
            return false;
        }

        // close everything and do not ask
        support.notifyClosed();

        if (support.getLastSelected() == this) {
            support.setLastSelected(null);
        }

        return true;
    }

    /** The undo/redo manager of the support.
     * @return the undo/redo manager shared by all editors for this support
     */
    public UndoRedo getUndoRedo() {
        return support.getUndoRedo();
    }

    public SystemAction[] getSystemActions() {
        SystemAction[] sa = super.getSystemActions();

        try {
            ClassLoader l = (ClassLoader) org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);

            if (l == null) {
                l = getClass().getClassLoader();
            }

            Class c = Class.forName("org.openide.actions.FileSystemAction", true, l); // NOI18N
            SystemAction ra = (SystemAction) SystemAction.findObject(c, true);

            // initialize the SYSTEM_ACTIONS
            sa = SystemAction.linkActions(sa, new SystemAction[] { ra });
        } catch (Exception ex) {
            // ok, we no action like this I guess
        }

        return sa;
    }

    /** Transfer the focus to the editor pane.
     */
    public void requestFocus() {
        super.requestFocus();

        if ((customComponent != null) && !SwingUtilities.isDescendingFrom(pane, customComponent)) {
            customComponent.requestFocus();
        } else if (pane != null) {
            pane.requestFocus();
        }
    }

    /** Transfer the focus to the editor pane.
     */
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();

        if ((customComponent != null) && !SwingUtilities.isDescendingFrom(pane, customComponent)) {
            return customComponent.requestFocusInWindow();
        } else if (pane != null) {
            return pane.requestFocusInWindow();
        }

        return false;
    }

    public boolean requestDefaultFocus() {
        if ((customComponent != null) && !SwingUtilities.isDescendingFrom(pane, customComponent)) {
            return customComponent.requestFocusInWindow();
        } else if (pane != null) {
            return pane.requestFocusInWindow();
        }

        return false;
    }

    /** @return Preferred size of editor top component  */
    public Dimension getPreferredSize() {
        Rectangle bounds = WindowManager.getDefault().getCurrentWorkspace().getBounds();

        return new Dimension(bounds.width / 2, bounds.height / 2);
    }

    private Action getAction(String key) {
        if (key == null) {
            return null;
        }

        // Try to find the action from kit.
        EditorKit kit = support.kit();

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

    /** Overrides superclass method. Remembers last selected component of
     * support belonging to this component.
     * @see #componentDeactivated */
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
                        setDisplayName(ces.messageName());
                        setName(ces.messageName()); // XXX compatibility

                        setToolTipText(ces.messageToolTip());
                    }
                }
            );
        }
    }

    // override for simple and consistent IDs
    protected String preferredID() {
        final CloneableEditorSupport ces = cloneableEditorSupport();

        if (ces != null) {
            return ces.documentID();
        }

        return "";
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        // Save environent if support is non-null.
        // XXX #13685: When support is null, the tc will be discarded 
        // after deserialization.
        out.writeObject((support != null) ? support.env() : null);

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
                        ErrorManager.getDefault().notify(
                            ErrorManager.INFORMATIONAL,
                            new IllegalStateException("Pane=" + p + "was not initialized yet!" // NOI18N
                                
                            )
                        );
                    } else {
                        pos = lastPos;
                    }
                } else {
                    Document doc = ((support != null) ? support.getDocument() : null);

                    // Relevant only if document is non-null?!
                    if (doc != null) {
                        ErrorManager.getDefault().notify(
                            ErrorManager.INFORMATIONAL,
                            new IllegalStateException(
                                "Caret is null in editor pane=" + p // NOI18N
                                 +"\nsupport=" + support // NOI18N
                                 +"\ndoc=" + doc // NOI18N
                                
                            )
                        );
                    }
                }
            }
        }

        out.writeObject(new Integer(pos));
    }

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
    }

    /**
     * Replaces serializing object. Overrides superclass method. Adds checking
     * for object validity. In case this object is invalid
     * throws {@link java.io.NotSerializableException NotSerializableException}.
     * @throws ObjectStreamException When problem during serialization occures.
     * @throws NotSerializableException When this <code>CloneableEditor</code>
     *               is invalid and doesn't want to be serialized. */
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
            support.initializeCloneableEditor(this);

            return this;
        }
    }

    /** This component should be discarded if the associated environment
    * is not valid.
    */
    private boolean discard() {
        return (support == null) || !support.env().isValid();
    }

    /** Dock this top component to editor mode if it is not docked
     * in some mode at this time  */
    private void dockIfNeeded(Workspace workspace) {
        // dock into editor mode if possible
        Mode ourMode = workspace.findMode(this);

        if (ourMode == null) {
            editorMode(workspace).dockInto(this);
        }
    }

    private Mode editorMode(Workspace workspace) {
        Mode ourMode = workspace.findMode(this);

        if (ourMode == null) {
            ourMode = workspace.createMode(
                    CloneableEditorSupport.EDITOR_MODE, getName(),
                    CloneableEditorSupport.class.getResource("/org/openide/resources/editorMode.gif" // NOI18N
                    )
                );
        }

        return ourMode;
    }

    //
    // Implements the CloneableEditorSupport.Pane interface
    //
    public CloneableTopComponent getComponent() {
        return this;
    }

    public JEditorPane getEditorPane() {
        initialize();

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
