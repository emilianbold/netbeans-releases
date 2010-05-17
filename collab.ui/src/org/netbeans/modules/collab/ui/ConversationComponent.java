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
package org.netbeans.modules.collab.ui;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.beans.*;
import java.lang.ref.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import org.openide.*;
import org.openide.awt.Mnemonics;
import org.openide.explorer.view.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.windows.*;

import com.sun.collablet.*;
import com.sun.collablet.chat.ChatCollablet;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.options.HiddenCollabSettings;
import org.netbeans.modules.collab.ui.switcher.ViewSwitcherPane;

/**
 *
 *
 * @author todd
 */
public class ConversationComponent extends CloneableTopComponent implements PropertyChangeListener,
    NotificationListener {
    ////////////////////////////////////////////////////////////////////////////
    // Sample code
    ////////////////////////////////////////////////////////////////////////////

    /*
    // If you wish to keep any state between IDE restarts, put it here:
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        setSomeState((SomeType)in.readObject());
    }
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(getSomeState());
    }
    */
    /*
    // The above assumes that the SomeType is safely serializable, e.g. String or Date.
    // If it is some class of your own that might change incompatibly, use e.g.:
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        NbMarshalledObject read = (NbMarshalledObject)in.readObject();
        if (read != null) {
            try {
                setSomeState((SomeType)read.get());
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
                // If the problem would make this component inconsistent, use:
                // throw new SafeException(e);
            }
        }
    }
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        Object toWrite;
        try {
            toWrite = new NbMarshalledObject(getSomeState());
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            toWrite = null;
            // Again you may prefer to use:
            // throw new SafeException(e);
        }
        out.writeObject(toWrite);
    }
    */

    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L;
    private static final int NOTIFICATION_DELAY_LONG = 850;
    private static final int NOTIFICATION_DELAY_SHORT = 150;
    private static final String ACCOUNT_ICON = "org/netbeans/modules/collab/core/resources/account_png.gif";
    private static final String INVITE_ICON = "org/netbeans/modules/collab/ui/resources/user_png.gif";

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Conversation conversation;
    private Reference nodeRef;
    private JList participantList;
    private JSplitPane mainSplitPane;
    private JSplitPane channelChatSplitPane;
    private ChatCollablet chatChannel;
    private ViewSwitcherPane channelsPane;
    private final Object NOTIFICATION_LOCK = new Object();
    private NotificationThread notificationThread;
    private boolean activated;
    private boolean chatHadFocus;
    private PropertyChangeSupport channelChangeSupport = new PropertyChangeSupport(this);

    /**
     *
     *
     */
    public ConversationComponent(Node node, Conversation conversation) {
        super();

        this.conversation = conversation;
        this.nodeRef = new WeakReference(node);

        initialize();

        setIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
        updateDisplayName();

        // Set the active node
        if (node != null) {
            setActivatedNodes(new Node[] { node });
        }

        conversation.addPropertyChangeListener(this);

        // To update Find, Copy, etc. actions, add to constructor:
        //		ActionMap map = getActionMap();
        //		Action findBinding = new MyFindAction(this);
        //		map.put(((CallbackSystemAction)SystemAction.get(
        //			FindAction.class)).getActionMapKey(), findBinding);

        /*author Smitha Krishna Nagesh
          Adding the Cut/Copy/Paste actions into the action map*/

        //ActionMap map = getActionMap();
        //map.put(DefaultEditorKit.cutAction, new DefaultEditorKit.CutAction());
        //map.put(DefaultEditorKit.copyAction, new DefaultEditorKit.CopyAction());
        //map.put(DefaultEditorKit.pasteAction, new DefaultEditorKit.PasteAction());	
    }

    /**
     *
     *
     */
    private void initialize() {
        // Allow this component to be docked anywhere; this allows users
        // to dock it into the output area for example
        putClientProperty("TopComponentAllowDockAnywhere", // NOI18N
            Boolean.TRUE
        );

        // Set our layout--if we don't do this, nothing will appear in 
        // the component
        setLayout(new BorderLayout());
	
	
	setBorder(BorderFactory.createEmptyBorder(
		UIManager.getInt("SplitPane.dividerSize"), 
		UIManager.getInt("SplitPane.dividerSize"), 
		0, 
		UIManager.getInt("SplitPane.dividerSize"))
	);
	
        // Create a view for listing participants in the conversation
        JPanel participantContainerPanel = new JPanel();
        participantContainerPanel.setLayout(new BorderLayout());

        // Add the invitee search panel
        //		participantContainerPanel.add(
        //			new ParticipantSearchPanel(getConversation()),BorderLayout.SOUTH);
        JButton inviteButton = new JButton(new ImageIcon(Utilities.loadImage(INVITE_ICON)));
        Mnemonics.setLocalizedText(inviteButton, NbBundle.getMessage(ConversationComponent.class, "BTN_ConversationComponent_Invite")); // NOI18N
        inviteButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    getConversation().getCollabSession().getManager().getUserInterface().inviteUsers(getConversation());
                }
            }
        );

        // Create a toolbar to inset the invite button
        JToolBar iviteButtonInsetToolBar = new JToolBar(JToolBar.HORIZONTAL);
        iviteButtonInsetToolBar.setFloatable(false);
        iviteButtonInsetToolBar.setBorder(new CompoundBorder(
                new EmptyBorder(3, 0, 0, 0), 
                iviteButtonInsetToolBar.getBorder())
            );
        iviteButtonInsetToolBar.setLayout(new java.awt.GridLayout(1,1));
        iviteButtonInsetToolBar.add(inviteButton);
        participantContainerPanel.add(iviteButtonInsetToolBar, BorderLayout.SOUTH);

        // Quick inner class
        class ParticipantsRootNode extends AbstractNode {
            public ParticipantsRootNode() {
                super(new Children.Array());

                Children.Array children = (Children.Array) getChildren();
                children.add(
                    new Node[] { new ParticipantsNode(getConversation()), new InviteesNode(getConversation()) }
                );
            }
        }

        // Create a tree view to show the participant lists
        NodeTreeModel treeModel = new NodeTreeModel(new ParticipantsRootNode());
        JTree participantTree = new JTree(treeModel);
        participantTree.setShowsRootHandles(false);
        participantTree.setRootVisible(false);
        participantTree.setCellRenderer(new NodeRenderer());
        participantTree.expandRow(1);
        participantTree.expandRow(0);

        JScrollPane treeScrollPane = new JScrollPane(participantTree);
        participantContainerPanel.add(treeScrollPane, BorderLayout.CENTER);

        // Discover the channels
        Collablet[] channels = getConversation().getChannels();
        List otherChannels = new ArrayList();

        for (int i = 0; i < channels.length; i++) {
            if (channels[i] instanceof ChatCollablet) {
                chatChannel = (ChatCollablet) channels[i];

                // Listen to changes in the chat channel
                chatChannel.addPropertyChangeListener(this);

                // Allow us to fire changes to the channel
                getChannelChangeSupport().addPropertyChangeListener(chatChannel);
            } else {
                otherChannels.add(channels[i]);
            }
        }

        // Create tabs to contain channels
        //		channelsPane=new JTabbedPane(JTabbedPane.BOTTOM);
        channelsPane = new ViewSwitcherPane();

        // Initialize the other channels
        initializeChannels();

        // Creat a panel to hold the non-chat channel components
        JPanel channelPanel = new JPanel();
        channelPanel.setLayout(new BorderLayout());
        channelPanel.add(channelsPane, BorderLayout.CENTER);

        // Create a scroll pane to contain the chat channel component
        JScrollPane chatAreaPane = null;
        JComponent chatComponent = null;

        try {
            if (chatChannel != null) {
                chatComponent = chatChannel.getComponent();
            } else {
                chatComponent = new JLabel(
                        NbBundle.getMessage(ConversationComponent.class, "MSG_ConversationComponent_ChannelError")
                    );
                ((JLabel) chatComponent).setHorizontalAlignment(JLabel.CENTER);
            }
        } catch (CollabException e) {
            // Shouldn't happen
            Debug.debugNotify(e);

            return;
        }

        channelChatSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, channelPanel, chatComponent);

        //		channelChatSplitPane.setUI(new MySplitPaneUI());
        channelChatSplitPane.setOneTouchExpandable(true);

        //		splitPane.setResizeWeight(0.5f);
        channelChatSplitPane.setDividerLocation(0.4d);
        channelChatSplitPane.setBorder(BorderFactory.createEmptyBorder());

        // Restore the split position
        int chatChannelSplitPosition = HiddenCollabSettings.getDefault().getLastConversationChatChannelSplit();

        if (chatChannelSplitPosition != -1) {
            channelChatSplitPane.setDividerLocation(chatChannelSplitPosition);
        }

        //		JSplitPane mainSplitPane=new JSplitPane(
        //			JSplitPane.HORIZONTAL_SPLIT,true,
        //			participantContainerPanel,splitPane);
        ////		mainSplitPane.setUI(new MySplitPaneUI());
        //		mainSplitPane.setOneTouchExpandable(true);
        //		mainSplitPane.setResizeWeight(0.2f);
        //		mainSplitPane.setDividerSize(5);
        //		mainSplitPane.setBorder(BorderFactory.createEmptyBorder());
        //
        //		// This splitter needs some additional drawing on its border in order 
        //		// to make it look correct
        //		if (mainSplitPane.getUI() instanceof BasicSplitPaneUI)
        //		{
        //			BasicSplitPaneDivider divider=
        //				((BasicSplitPaneUI)mainSplitPane.getUI()).getDivider();
        //			divider.setBorder(new ThinBevelBorder(ThinBevelBorder.RAISED));
        //		}
        // Note, this is the workaround for bug 5081902.  Apparently, the bug
        // is a Swing bug in JSplitPane.
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, participantContainerPanel, channelChatSplitPane);

        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder());
	
        // Restore the split position
        int mainSplitPosition = HiddenCollabSettings.getDefault().getLastConversationMainSplit();

        if (mainSplitPosition != -1) {
	    mainSplitPane.setDividerLocation(mainSplitPosition);
        }

        add(mainSplitPane, BorderLayout.CENTER);

        //JPanel statusBar=new JPanel();
        //statusBar.setLayout(new BorderLayout());
        //statusBar.add(new JSeparator(),BorderLayout.NORTH);
        //statusBar.add(new JLabel(" You are participating in this conversation as Todd Fast <todd@localhost>"),BorderLayout.SOUTH);
        //
        //add(statusBar,BorderLayout.SOUTH);
    }

    //	private static class MySplitPaneUI extends BasicSplitPaneUI
    //	{
    //        public void installUI(JComponent c)
    //		{
    //            super.installUI(c);
    //            getDivider().setBorder(null);
    //        }
    //
    //        /** Creates the default divider. Overrides superclass method */
    //        public BasicSplitPaneDivider createDefaultDivider()
    //		{
    ////            return UIUtils.isXPLF() 
    ////                    ? new XPLFDivider(this)
    ////                    : new NestedSplitPaneDivider(this);
    //			return new XPLFDivider(this);
    //        }
    //	}
    //
    //	private static class XPLFDivider extends BasicSplitPaneDivider
    //	{
    //		public XPLFDivider(MySplitPaneUI ui)
    //		{
    //			super(ui);
    //			setBackground((Color)UIManager.get("nb_workplace_fill"));
    //		}
    //
    //		public void paint(java.awt.Graphics g)
    //		{
    //			Dimension size = getSize();
    //			g.setColor(getBackground());
    //			g.fillRect(0, 0, size.width, size.height);
    //			super.paint(g);
    //		}
    //	}

    /**
     * Please note, by this point, the chat channel has already been
     * initialized elsewhere. This method only initializes the other channels.
     *
     */
    protected void initializeChannels() {
        Collablet[] channels = getConversation().getChannels();
        boolean addedChannel = false;

        for (int i = 0; i < channels.length; i++) {
            // Since the chat channel is a special case, skip showing it
            // in the channel tab area.
            if (channels[i] instanceof InteractiveCollablet && !(channels[i] instanceof ChatCollablet)) {
                InteractiveCollablet channel = (InteractiveCollablet) channels[i];

                String displayName = channel.getDisplayName();
                Icon icon = channel.getIcon();

                // Try to get the channel component to display as a tab
                JComponent component = null;

                try {
                    component = channel.getComponent();
                } catch (CollabException e) {
                    Debug.debugNotify(e);

                    // Create an editor component to explain the problem
                    JEditorPane editor = new JEditorPane();
                    component = editor;
                    editor.setEditable(false);
                    editor.setEnabled(false);
                    editor.setBackground(new JLabel().getBackground());

                    String errorText = NbBundle.getMessage(
                            ConversationComponent.class, "MSG_ConversationComponent_ChannelError"
                        );
                    editor.setText(errorText);

                    // Attach a flag to the channel name to indicate a problem
                    displayName += NbBundle.getMessage(
                        ConversationComponent.class, "LBL_ConversationComponent_ChannelErrorFlag"
                    );
                }

                // Add the channel component as a tab
                //				if (icon!=null)
                //					getChannelsTabPane().addTab(displayName,icon,component);
                //				else
                //					getChannelsTabPane().addTab(displayName,component);
                getChannelsPane().addItem(displayName, icon, component, null);

                // Listen to changes in the channel
                channel.addPropertyChangeListener(this);

                // Allow us to send events to the channel
                getChannelChangeSupport().addPropertyChangeListener(channel);

                addedChannel = true;
            }
        }

        if (!addedChannel) {
            // Add a message indicating no channels were added
            JLabel noChannelsLabel = new JLabel(
                    NbBundle.getMessage(ConversationComponent.class, "MSG_ConversationComponent_NoChannels")
                ); // NOI18N
            noChannelsLabel.setHorizontalAlignment(JLabel.CENTER);

            //			getChannelsTabPane().add(
            //				NbBundle.getMessage(ConversationComponent.class,
            //					"LBL_ConversationComponent_NoChannelsTab"), // NOI18N
            //				noChannelsLabel);
            String displayName = NbBundle.getMessage(
                    ConversationComponent.class, "LBL_ConversationComponent_NoChannelsTab"
                ); // NOI18N
            getChannelsPane().addItem(displayName, null, noChannelsLabel, null);
        }
    }

    /**
     *
     *
     */
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    /**
     *
     *
     */
    public Conversation getConversation() {
        return conversation;
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	public List availableModes(List modes) 
    //	{
    //		Debug.out.println("Available modes: "+modes);
    //		for (Iterator i=modes.iterator(); i.hasNext(); )
    //			Debug.out.println(i.next());
    //		return modes;
    //	}

    /**
     *
     *
     */

    //	protected JTabbedPane getChannelsTabPane()
    protected ViewSwitcherPane getChannelsPane() {
        return channelsPane;
    }

    /**
     *
     *
     */

    //    public Action[] getActions()
    //	{
    //		Action[] supe = super.getActions();
    //		Action[] mine = new Action[supe.length + 1];
    //		System.arraycopy(supe, 0, mine, 0, supe.length);
    //		mine[supe.length] = SystemAction.get(SomeActionOfMine.class);
    //		return mine;
    //    }
    ////////////////////////////////////////////////////////////////////////////
    // Event notifications
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void addNotify() {
        super.addNotify();
    }

    /**
     *
     *
     */
    protected CloneableTopComponent createClonedObject() {
        //return new ConversationComponent((ConversationNode)nodeRef.get(),
        return new ConversationComponent((Node) nodeRef.get(), getConversation());
    }

    /**
     *
     *
     */
    public void open() {
        if (!isOpened()) {
            dockComponent();
        }

        super.open();
    }

    /**
     * Docks the conversation component into the mode specified or implicitly
     * selected by the user.
     *
     */
    protected void dockComponent() {
        // Try to dock into the last saved mode
        String modeName = HiddenCollabSettings.getDefault().getLastOpenedMode();

        if (modeName != null) {
            Mode mode = WindowManager.getDefault().findMode(modeName);

            if (mode != null) {
                mode.dockInto(this);
            }
        }
    }

    /**
     *
     *
     */
    public boolean canClose() {
        boolean result = super.canClose();

        if (result) {
            // Note, we need to save the TC's mode here because in 
            // componentClosed(), it is too late to find it because the
            // component has already been closed.
            Mode mode = WindowManager.getDefault().findMode(this);

            if (mode != null) {
                HiddenCollabSettings.getDefault().setLastOpenedMode(mode.getName());
            }

            HiddenCollabSettings.getDefault().setLastConversationMainSplit(
                    mainSplitPane.getDividerLocation()
            );
            HiddenCollabSettings.getDefault().setLastConversationChatChannelSplit(
                channelChatSplitPane.getDividerLocation()
            );
        }

        return result;
    }

    /**
     *
     *
     */
    public boolean isActivated() {
        synchronized (NOTIFICATION_LOCK) {
            return activated;
        }
    }

    /**
     *
     *
     */
    protected void componentActivated() {
        super.componentActivated();

        synchronized (NOTIFICATION_LOCK) {
            activated = true;
            endNotification();
        }

        //		ConversationNode node=(ConversationNode)nodeRef.get();
        Node node = (Node) nodeRef.get();

        if (node != null) {
            setActivatedNodes(new Node[] { node });
        }

        // Focus the chat component, but only if it was focused when we left
        if (chatHadFocus) {
            JComponent focusComponent = null;

            if (chatChannel != null) {
                focusComponent = chatChannel.getFocusableComponent();
            }

            if (focusComponent != null) {
                focusComponent.requestFocus();
            }
        }

        fireConversationActivated(true);
    }

    /**
     *
     *
     */
    protected void componentDeactivated() {
        super.componentDeactivated();

        synchronized (NOTIFICATION_LOCK) {
            activated = false;
        }

        // Remember whether the chat had focus
        JComponent focusComponent = null;

        if (chatChannel != null) {
            focusComponent = chatChannel.getFocusableComponent();
        }

        if (focusComponent != null) {
            chatHadFocus = focusComponent.hasFocus();
        }

        fireConversationActivated(false);
    }

    /**
     *
     *
     */
    protected void componentShowing() {
        super.componentShowing();
    }

    /**
     *
     *
     */
    protected void componentHidden() {
        super.componentHidden();
    }

    /**
     *
     *
     */
    protected void componentOpened() {
        super.componentOpened();

        endNotification();
    }

    /**
     *
     *
     */
    protected void componentClosed() {
        super.componentClosed();

        endNotification();

        // Prompt to confirm closing the conversation only if there are others
        // in the conversation.
        if (getConversation().isValid()) {
            boolean leave = true;

            if (getConversation().getParticipants().length > 1) {
                String leaveOption = NbBundle.getMessage(
                        ConversationComponent.class, "OPT_ConversationComponent_LeaveConversation"
                    ); // NOI18N
                String stayOption = NbBundle.getMessage(
                        ConversationComponent.class, "OPT_ConversationComponent_StayInConversation"
                    ); // NOI18N

                // Prompt user to leave conversation if closing window
                NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(
                            ConversationComponent.class, "MSG_ConversationComponent_CloseConversation",
                            getConversation().getDisplayName()
                        )
                    );
                descriptor.setOptions(new Object[] { leaveOption, stayOption }); // NOI18N

                DialogDisplayer.getDefault().notify(descriptor);

                leave = descriptor.getValue() == leaveOption;
            }

            if (leave) {
                getConversation().leave();
            }
        }
    }

    /**
         *
         *
         */
    public void propertyChange(final PropertyChangeEvent event) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        propertyChange(event);
                    }
                }
            );

            return;
        }

        if (event.getSource() instanceof Conversation &&
                Conversation.PROP_PARTICIPANTS.equals(event.getPropertyName())) {
            updateDisplayName();
        } else if (
            event.getSource() instanceof InteractiveCollablet &&
                Collablet.PROP_MODIFIED.equals(event.getPropertyName())
        ) {
            // TODO: Should we only notify of visible channel modifications?
            // For now, I assume so.
            notifyChannelModified((Collablet) event.getSource());
        } else if (
            event.getSource() instanceof InteractiveCollablet &&
                InteractiveCollablet.PROP_SELECTED_NODES.equals(event.getPropertyName())
        ) {
            // Forward any channel node selections to the property sheet
            try {
                Node[] nodes = (Node[]) event.getNewValue();

                if (nodes != null) {
                    setActivatedNodes(nodes);
                }
            } catch (Exception e) {
                // Ignore
                Debug.debugNotify(e);
            }
        }
    }

    /**
     *
     *
     */
    protected void updateDisplayName() {
        // Change the display name of this window
        setDisplayName(
            NbBundle.getMessage(
                ConversationComponent.class, "LBL_ConversationComponent_Name",
                new Object[] { getConversation().getDisplayName() }
            )
        );
    }

    ////////////////////////////////////////////////////////////////////////////
    // Notification methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected void notifyChannelModified(Collablet channel) {
        synchronized (NOTIFICATION_LOCK) {
            if (!isActivated()) {
                NotificationRegistry.getDefault().addComponent(this);
            }
        }
    }

    /**
     *
     *
     */
    protected void endNotification() {
        synchronized (NOTIFICATION_LOCK) {
            NotificationRegistry.getDefault().removeComponent(this);
            notificationStateChanged(false);
        }
    }

    /**
     *
     *
     */
    public void notificationStateChanged(boolean state) {
        if (state) {
            // Change the display name of this window
            setDisplayName(
                NbBundle.getMessage(
                    ConversationComponent.class, "LBL_ConversationComponent_Name_NotificationOn",
                    new Object[] { getConversation().getDisplayName() }
                )
            );
        } else {
            updateDisplayName();
        }

        Node node = (Node) nodeRef.get();

        if (node != null) {
            if (node instanceof ConversationNode) {
                ((ConversationNode) node).notify(state);
            } else if (node instanceof PublicConversationNode) {
                ((PublicConversationNode) node).notify(state);
            }
        }

        setIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
    }

    /**
     *
     *
     */
    public void notificationResumed() {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notificationSuspended() {
        notificationStateChanged(false);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property change support
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected PropertyChangeSupport getChannelChangeSupport() {
        return channelChangeSupport;
    }

    /**
     *
     *
     */
    protected void fireConversationActivated(boolean value) {
        try {
            getChannelChangeSupport().firePropertyChange("conversationActivated", !value, value);
        } catch (Exception e) {
            Debug.debugNotify(e);
        }
    }
}
