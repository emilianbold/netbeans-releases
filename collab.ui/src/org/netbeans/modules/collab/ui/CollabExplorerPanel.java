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
package org.netbeans.modules.collab.ui;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;

import org.openide.*;
import org.openide.awt.*;
import org.openide.explorer.*;
import org.openide.explorer.view.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.util.io.*;
import org.openide.windows.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.CollabSessionCookie;
import org.netbeans.modules.collab.ui.actions.*;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class CollabExplorerPanel extends ExplorerPanel implements NotificationListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L; // DO NOT CHANGE!
    private static CollabExplorerPanel DEFAULT_INSTANCE;
    public static final String COMPONENT_LOGIN = "login"; // NOI18N
    public static final String COMPONENT_EXPLORER = "explorer"; // NOI18N
    public static final String COMPONENT_NO_MANAGER_NOTIFICATION = "noManager"; // NOI18N
    private static final Image NORMAL_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/collab/ui/resources/collab_png16.gif"
        );
    private static final Image ALERT_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/collab/ui/resources/conversation_notify_png.gif"
        );

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private RootNode rootNode;
    private LoginAccountPanel loginPanel;
    private NoManagerNotificationPanel noManagerPanel;
    private GlassPanel sessionPanel;
    private PresenceNotificationPane presenceNotificationPane;
    private SessionsTreeView treeView;
    private JTree treeViewJTree;
    private JComponent currentComponent;
    private JButton changeStatusButton;
    private PropertyChangeListener sessionStatusListener;

    /**
     *
     *
     */
    public CollabExplorerPanel() {
        super();
        initialize();
    }

    /**
     *
     *
     */
    protected CollabExplorerPanel(ExplorerManager manager) {
        super(manager);
        initialize();
    }

    /**
     *
     *
     */
    protected String preferredID() {
        return super.preferredID();
    }

    /**
     *
     *
     */
    protected void initialize() {
        setName(NbBundle.getMessage(CollabExplorerPanel.class, "TITLE_CollabExplorerPanel_Login")); // NOI18N
        setIcon(Utilities.loadImage("org/netbeans/modules/collab/ui/resources/collab_png16.gif")); // NOI18N

        getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(CollabExplorerPanel.class, "ACSD_CollabExplorerPanel_Name")
        ); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(CollabExplorerPanel.class, "ACSD_CollabExplorerPanel_Description")
        ); // NOI18N

        setLayout(new CardLayout());

        // Add the login panel
        loginPanel = new LoginAccountPanel();
        add(loginPanel, COMPONENT_LOGIN);

        // Create the session panel
        sessionPanel = new GlassPanel();
        sessionPanel.setLayout(new BorderLayout());
        sessionPanel.add(initializeToolbar(), BorderLayout.NORTH);

        // Add the session explorer view
        treeView = new SessionsTreeView();
        treeView.setRootVisible(false);
        treeViewJTree = ((SessionsTreeView) treeView).getJTree();
        sessionPanel.add(treeView, BorderLayout.CENTER);

        // Add the presence notification pane as the glass pane
        presenceNotificationPane = new PresenceNotificationPane(sessionPanel);
        sessionPanel.setGlassComponent(presenceNotificationPane);

        // TAF: Test of notification atop the entire IDE window
        //presenceNotificationPane.setVisible(true);
        //((JFrame)WindowManager.getDefault().getMainWindow()).setGlassPane(presenceNotificationPane);
        //presenceNotificationPane.setVisible(true);
        // Add the session panel to the component
        add(sessionPanel, COMPONENT_EXPLORER);

        // Add the no-manager notification panel
        noManagerPanel = new NoManagerNotificationPanel();
        add(noManagerPanel, COMPONENT_NO_MANAGER_NOTIFICATION);

        // Attach a listener to node selection changes so we can update the
        // change status button in response to the selected node's session's
        // current status
        getExplorerManager().addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    if (event.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                        updateChangeStatusButton();
                    }
                }
            }
        );

        // Create a listener to listen to changes in session status, so we 
        // can update the change status button in response
        sessionStatusListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getPropertyName().equals(CollabPrincipal.PROP_STATUS)) {
                            updateChangeStatusButton();
                        }
                    }
                };

        // Determine which component to show
        CollabManager manager = CollabManager.getDefault();

        if (manager != null) {
            showComponent(COMPONENT_LOGIN);

            // Add an event listener to show login pane as needed
            manager.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getPropertyName().equals(CollabManager.PROP_SESSIONS)) {
                            CollabManager manager = CollabManager.getDefault();

                            if ((manager != null) && (manager.getSessions().length == 0)) {
                                // Show the collab explorer
                                CollabExplorerPanel.getInstance().showComponent(CollabExplorerPanel.COMPONENT_LOGIN);
                            } else {
                                updateChangeStatusButton();
                            }
                        }
                    }
                }
            );
        } else {
            // Show the no-manager notification
            showComponent(COMPONENT_NO_MANAGER_NOTIFICATION);
        }

        // Attach ourselves as a notification listener (weakly)
        NotificationRegistry.getDefault().addNotificationListener(
            (NotificationListener) WeakListeners.create(
                NotificationListener.class, NotificationListener.class, this, NotificationRegistry.getDefault()
            )
        );
    }

    /**
     *get help ctx map id for context sensitive help
     *
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("collab_about_collab"); //NOI18n
    }

    /**
     *
     *
     */
    private JComponent initializeToolbar() {
        SystemAction action = null;

        action = SystemAction.get(LoginAction.class);

        JButton loginButton = new JButton(); //action.getName());
        Actions.connect(loginButton, action);

        // Strip the trailing elipses from the button
        //		String loginText=loginButton.getText();
        //		if (loginText.endsWith("..."))
        //			loginButton.setText(loginText.substring(0,loginText.length()-3));
        // Create a button for changing status via a popup menu
        action = SystemAction.get(ChangeStatusAction.class);
        changeStatusButton = new JButton(
                NbBundle.getMessage(CollabExplorerPanel.class, "LBL_CollabExplorerPanel_ChangeStatusButton")
            );
        changeStatusButton.setToolTipText(action.getName());
        changeStatusButton.addActionListener(new ChangeStatusActionListener());
        changeStatusButton.setIcon(new ImageIcon(Utilities.loadImage(ContactNode.OPEN_ICON)));

        action = SystemAction.get(AddContactAction.class);

        JButton addContactsButton = new JButton();
        Actions.connect(addContactsButton, action);

        action = SystemAction.get(AddPublicConversationAction.class);

        JButton subscribePublicConversationButton = new JButton();
        Actions.connect(subscribePublicConversationButton, action);

        action = SystemAction.get(CreateConversationAction.class);

        JButton createConversationButton = new JButton();
        Actions.connect(createConversationButton, action);

        JToolBar toolbar = new JToolBar();
        toolbar.setBorder(new CompoundBorder(new EmptyBorder(3, 5, 3, 0), toolbar.getBorder()));
        toolbar.setFloatable(false);

        //		toolbar.add(loginButton);
        //		toolbar.addSeparator();
        toolbar.add(changeStatusButton);
        toolbar.addSeparator();
        toolbar.add(addContactsButton);
        toolbar.add(subscribePublicConversationButton);
        toolbar.addSeparator();
        toolbar.add(createConversationButton);
        toolbar.add(Box.createHorizontalGlue());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JSeparator(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     *
     *
     */
    public void addNotify() {
        super.addNotify();

        // Create the root node a bit later in order to allow the 
        // CollabManager to be installed.  Otherwise, sometimes the UI
        // module is initialized before the JIM module and the root node
        // is orphaned because it cannot find the CollabManager.
        // TAF: It's not clear that this really helps, since we are still
        // seeing the out-of-sync node issue.  However, I am going to leave
        // this code here as it probably makes the most sense to happen here 
        // anyway.
        if (rootNode == null) {
            rootNode = new RootNode(this);
            getExplorerManager().setRootContext(rootNode);
        }
    }

    /**
     *
     *
     */
    public int getPersistenceType() {
        //        return PERSISTENCE_ALWAYS; //PERSISTENCE_ONLY_OPENED; // PERSISTENCE_NEVER
        return PERSISTENCE_ALWAYS;
    }

    /**
     *
     *
     */
    public void showComponent(final String componentID) {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    ((CardLayout) getLayout()).show(CollabExplorerPanel.this, componentID);

                    // Update the window title
                    String title = NbBundle.getMessage(CollabExplorerPanel.class, "TITLE_CollabExplorerPanel"); // NOI18N

                    if (componentID.equals(COMPONENT_LOGIN)) {
                        title = NbBundle.getMessage(CollabExplorerPanel.class, "TITLE_CollabExplorerPanel_Login"); // NOI18N
                        currentComponent = loginPanel;

                        // Notify the login panel that it's being shown
                        loginPanel.showNotify();
                    } else if (componentID.equals(COMPONENT_EXPLORER)) {
                        title = NbBundle.getMessage(CollabExplorerPanel.class, "TITLE_CollabExplorerPanel_ContactList"); // NOI18N
                        currentComponent = sessionPanel;
                    } else if (componentID.equals(COMPONENT_NO_MANAGER_NOTIFICATION)) {
                        currentComponent = noManagerPanel;
                    }

                    setName(title);

                    // Attempted workaround for bug 5071137: force a refresh 
                    // of the listener relationship between the node and the 
                    // collab manager when switch to the explorer view.
                    if (componentID.equals(COMPONENT_EXPLORER)) {
                        if (rootNode != null) {
                            rootNode.bug_5071137_workaround();
                        }

                        if (getExplorerManager().getSelectedNodes().length == 0) {
                            try {
                                Node root = getExplorerManager().getRootContext();
                                Children children = root.getChildren();
                                Node[] nodes = children.getNodes();

                                if (nodes.length > 0) {
                                    getExplorerManager().setSelectedNodes(new Node[] { nodes[0] });
                                }
                            } catch (PropertyVetoException e) {
                                // do nothing
                            }
                        }
                    }
                }
            }
        );
    }

    /**
     *
     *
     */
    public LoginAccountPanel getLoginAccountPanel() {
        return loginPanel;
    }

    /**
     *
     *
     */
    public TreeView getTreeView() {
        return treeView;
    }

    /**
     *
     *
     */
    protected JTree getTreeViewJTree() {
        return treeViewJTree;
    }

    /**
     *
     *
     */
    public RootNode getRootNode() {
        return rootNode;
    }

    /**
     *
     *
     */
    protected void updateTitle() {
        // Avoid calling super method
    }

    /**
     *
     *
     */
    public void open(Workspace workspace) {
        // Try to dock us into the current workspace before opening
        Workspace realWorkspace = (workspace == null) ? WindowManager.getDefault().getCurrentWorkspace() : workspace;

        if ((realWorkspace.findMode(this) == null) && (realWorkspace.findMode("collaboration") != null)) // NOI18N
         {
            realWorkspace.findMode("collaboration").dockInto(this); // NOI18N
        } else if ((realWorkspace.findMode(this) == null) && (realWorkspace.findMode("explorer") != null)) // NOI18N
         {
            realWorkspace.findMode("explorer").dockInto(this); // NOI18N
        }

        super.open(workspace);
    }

    /**
     *
     *
     */
    protected void componentOpened() {
        // Determine which component to show
        if (CollabManager.getDefault() != null) {
            if (CollabManager.getDefault().getSessions().length > 0) {
                showComponent(COMPONENT_EXPLORER);
            } else {
                showComponent(COMPONENT_LOGIN);
            }
        } else {
            showComponent(COMPONENT_NO_MANAGER_NOTIFICATION);
        }
    }

    /**
     *
     *
     */
    protected void componentClosed() {
        if (CollabManager.getDefault() == null) {
            return;
        }

        // Prompt user to log out of sessions if they are closing the 
        // collab explorer
        if (CollabManager.getDefault().getSessions().length > 0) {
            NotifyDescriptor.Confirmation descriptor = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(CollabExplorerPanel.class, "MSG_CollabExplorerPanel_PanelClosingLogout"), // NOI18N
                    NotifyDescriptor.YES_NO_OPTION
                );

            if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
                CollabManager.getDefault().invalidate();

                // Because of a bug in NetBeans, we must set the node selection
                // explicitly to nothing in order to prevent nodes from 
                // previous views of this component being carried over.
                try {
                    getExplorerManager().setSelectedNodes(new Node[0]);
                } catch (PropertyVetoException e) {
                    Debug.debugNotify(e);
                }
            }
        }
    }

    /**
     * This methods return the selected session, or null if no session is
     * selected, or more than one session is selected.  Note, this method
     * will return a session even if one is not selected if there is only one
     * session currently active.
     *
     */
    public CollabSession getSelectedCollabSession() {
        Node[] nodes = getExplorerManager().getSelectedNodes();
        CollabSession session = null;

        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                CollabSessionCookie cookie = (CollabSessionCookie) nodes[i].getCookie(CollabSessionCookie.class);

                if (cookie != null) {
                    // Ensure that all selected nodes have the same session
                    if (session == null) {
                        // Initialize the local variable
                        session = cookie.getCollabSession();
                    } else if (session != cookie.getCollabSession()) {
                        // We found a session that didn't match; return null
                        return null;
                    }
                }
            }
        }

        // Even if no node is selected, but there is only one session, return
        // the session as if it were selected
        if ((session == null) && (CollabManager.getDefault() != null)) {
            CollabSession[] sessions = CollabManager.getDefault().getSessions();

            if ((sessions != null) && (sessions.length == 1)) {
                session = sessions[0];
            }
        }

        return session;
    }

    /**
     * Update the change status button icon to be in sync with the current
     * session selection
     *
     */
    private void updateChangeStatusButton() {
        CollabSession session = getSelectedCollabSession();

        if (session != null) {
            // Take this opportunity to attach the status listener.  We do 
            // this here purely out of convenience; we could instead take
            // a more formal approach of listening to session property changes
            // via CollabManager.  This approach has the tiny advantage of only
            // bothering to attach listeners for sessions that are actually
            // clicked on by the user.
            session.getUserPrincipal().removePropertyChangeListener(sessionStatusListener);
            session.getUserPrincipal().addPropertyChangeListener(sessionStatusListener);

            // Update the change status button according to the currently 
            // selected session's status
            switch (session.getUserPrincipal().getStatus()) {
            case CollabPrincipal.STATUS_ONLINE:
                changeStatusButton.setIcon(new ImageIcon(Utilities.loadImage(ContactNode.OPEN_ICON)));
                changeStatusButton.setText(
                    NbBundle.getMessage(ChangeStatusAction.class, "LBL_ChangeStatusAction_ONLINE")
                ); // NOI18N

                break;

            case CollabPrincipal.STATUS_BUSY:
                changeStatusButton.setIcon(new ImageIcon(Utilities.loadImage(ContactNode.BUSY_ICON)));
                changeStatusButton.setText(
                    NbBundle.getMessage(ChangeStatusAction.class, "LBL_ChangeStatusAction_BUSY")
                ); // NOI18N

                break;

            case CollabPrincipal.STATUS_AWAY:
                changeStatusButton.setIcon(new ImageIcon(Utilities.loadImage(ContactNode.AWAY_ICON)));
                changeStatusButton.setText(
                    NbBundle.getMessage(ChangeStatusAction.class, "LBL_ChangeStatusAction_AWAY")
                ); // NOI18N

                break;

            case CollabPrincipal.STATUS_IDLE:
                changeStatusButton.setIcon(new ImageIcon(Utilities.loadImage(ContactNode.IDLE_ICON)));
                changeStatusButton.setText(
                    NbBundle.getMessage(ChangeStatusAction.class, "LBL_ChangeStatusAction_IDLE")
                ); // NOI18N

                break;

            default:
                changeStatusButton.setText(
                    NbBundle.getMessage(ChangeStatusAction.class, "LBL_ChangeStatusAction_INVISIBLE")
                ); // NOI18N
                changeStatusButton.setIcon(new ImageIcon(Utilities.loadImage(ContactNode.CLOSED_ICON)));
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Notification methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void showContactNotification(CollabPrincipal contact, int type) {
        presenceNotificationPane.showContactNotification(contact, type);
    }

    ////////////////////////////////////////////////////////////////////////////
    // NotificationListener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void notificationStateChanged(boolean state) {
        if (state) {
            setIcon(ALERT_IMAGE);
        } else {
            setIcon(NORMAL_IMAGE);
        }
    }

    /**
     *
     *
     */
    public void notificationSuspended() {
        notificationStateChanged(false);
    }

    /**
     *
     *
     */
    public void notificationResumed() {
        // Do nothing
    }

    ////////////////////////////////////////////////////////////////////////////
    // Serialization methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Singleton methods
    ////////////////////////////////////////////////////////////////////////////

    /**
    * Gets default instance. Don't use directly, it reserved for '.settings'
     * file only,i.e. deserialization routines, otherwise you can get non-
     * deserialized instance.
     *
     */
    public static synchronized CollabExplorerPanel getDefault() {
        if (DEFAULT_INSTANCE == null) {
            DEFAULT_INSTANCE = new CollabExplorerPanel();
        }

        return DEFAULT_INSTANCE;
    }

    /**
     * Finds default instance. Use in client code instead of
     * {@link #getDefault()}.
     *
     */
    public static synchronized CollabExplorerPanel getInstance() {
        if (DEFAULT_INSTANCE == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("CollaborationExplorer"); // NOI18N

            if (DEFAULT_INSTANCE == null) {
                Debug.errorManager.notify(
                    ErrorManager.INFORMATIONAL,
                    new IllegalStateException(
                        "Can not find CollaborationExplorer " + // NOI18N
                        "component for its ID. Returned " + tc
                    )
                ); // NOI18N
                DEFAULT_INSTANCE = new CollabExplorerPanel();
            }
        }

        return DEFAULT_INSTANCE;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public static final class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 1L; // DO NOT CHANGE!

        public Object readResolve() {
            return CollabExplorerPanel.getDefault();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Listens to the change status button and pops up a menu in response to
     * button presses
     *
     */
    private class ChangeStatusActionListener extends Object implements ActionListener {
        final SystemAction[] GROUPED_ACTIONS = new SystemAction[] {
                SystemAction.get(StatusOnlineAction.class), SystemAction.get(StatusBusyAction.class),
                SystemAction.get(StatusAwayAction.class), SystemAction.get(StatusInvisibleAction.class),
            };

        /**
         *
         *
         */
        public void actionPerformed(ActionEvent event) {
            JPopupMenu menu = new JPopupMenu();

            for (int i = 0; i < GROUPED_ACTIONS.length; i++) {
                SystemAction action = GROUPED_ACTIONS[i];

                if (action instanceof Presenter.Popup) {
                    JMenuItem item = ((Presenter.Popup) action).getPopupPresenter();
                    item.setIcon(action.getIcon());
                    menu.add(item);
                }
            }

            JButton button = (JButton) event.getSource();
            menu.show(button, 0, button.getHeight());
        }
    }
}
