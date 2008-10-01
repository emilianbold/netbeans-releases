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
package org.netbeans.modules.collab.channel.filesharing;

import com.sun.collablet.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.*;

import java.awt.Image;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.api.project.Project;

import org.netbeans.modules.collab.channel.filesharing.config.FilesharingNotifierConfig;
import org.netbeans.modules.collab.channel.filesharing.config.FilesharingNotifierConfigManager;
import org.netbeans.modules.collab.channel.filesharing.config.FilesharingProcessorConfig;
import org.netbeans.modules.collab.channel.filesharing.config.FilesharingProcessorConfigManager;
import org.netbeans.modules.collab.channel.filesharing.context.UnlockRegionContext;
import org.netbeans.modules.collab.channel.filesharing.event.JoinFilesharingBegin;
import org.netbeans.modules.collab.channel.filesharing.event.UnlockRegionEvent;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.DocumentTabMarker;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingChannelListener;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.JoinBeginTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.SwingThreadTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.UnlockRegionTimerTask;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandlerFactory;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandlerResolver;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroupManager;
import org.netbeans.modules.collab.channel.filesharing.filesystem.CollabFilesystem;
import org.netbeans.modules.collab.channel.filesharing.mdc.ChannelListener;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventProcessor;
import org.netbeans.modules.collab.channel.filesharing.projecthandler.*;
import org.netbeans.modules.collab.channel.filesharing.ui.FilesharingCollabletFactorySettings;
import org.netbeans.modules.collab.channel.filesharing.ui.FilesystemExplorerPanel;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.core.Debug;


/**
 * Filesharing Channel
 *
 * @author Todd Fast, todd.fast@sun.com
 * @version 1.0
 */
public class FilesharingCollablet extends Object implements InteractiveCollablet, FilesharingConstants,
    PropertyChangeListener {
    private static Reference/*<FilesharingContext>*/activatedComponentContext = null;

    /* staticContexts only for HotKey operation */
    private static HashMap staticContexts = new HashMap();
    public static JEditorPane currentEditorPane = null;
    private static HashMap fsChannels = new HashMap();

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* context */
    private FilesharingContext context = null;

    /* component */
    private FilesystemExplorerPanel component;

    /* scrollPane*/
    private JScrollPane scrollPane;

    /* icon */
    private Icon icon;

    /* propertyChangeSupport */
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /* conversation */
    private Conversation conversation = null;

    /* to test if this channel is valid */
    private boolean valid = false;

    /* channel listener */
    private ChannelListener channelListener = null;

    /* timertasks */
    private List timerTasks = new ArrayList();

    /* filesystem */
    private FileSystem filesystem = null;
    private EventNotifier channelEventNotifier = null;

    /**
     *
     * @param conversation
     */
    public FilesharingCollablet(Conversation conversation) {
        super();
        this.conversation = conversation;

        String currentVersion = "1.0"; //NoI18n
        context = new FilesharingContext(currentVersion, this);

        //addContext(getConversation(),this.context);
        try {
            init();
        } catch (CollabException ce) {
            Debug.errorManager.notify(ce);
        }

        addPropertyChangeListener(this);
        conversation.addPropertyChangeListener(this);

        synchronized (fsChannels) {
            fsChannels.put(getConversation(), this);
        }
    }

    /**
     *
     * @return display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(FilesharingCollabletFactory.class, "LBL_FilesharingCollablet_DisplayName"); // NOI18N
    }

    /**
     *
     * @return image icon
     */
    public Icon getIcon() {
        if (icon == null) {
            //Image image=(Image)UIManager.get("Nb.Explorer.Folder.icon");
            //New icons for filesharing
            Image image = ImageUtilities.loadImage(
                    "org/netbeans/modules/collab/channel/filesharing/resources/filesharing_png.gif", true
                ); // NOI18N

            if (image != null) {
                icon = new ImageIcon(image);
            }
        }

        return icon;
    }

    /**
     *
     * @return conversation
     */
    public Conversation getConversation() {
        return this.conversation;
    }

    /**
     *
     *
     */
    public void init() throws CollabException {
        //just to make sure the factory objects are loaded

        /*CollabFileHandlerFactory[] filehanderFactories =
                CollabFileHandlerResolver.getDefault().getMimeResolvers();*/
        CollabFileHandlerResolver fileHandlerResolver = CollabFileHandlerResolver.newInstance();
        CollabFileHandlerFactory[] filehanderFactories = fileHandlerResolver.getMimeResolvers();

        if ((filehanderFactories == null) || (filehanderFactories.length == 0)) {
            //indicates there is error or no registered filehandler Factories, 
            //return gracefully		
            return;
        }

        getContext().setConversation(getConversation());
        getContext().findIsReadOnlyConversation(getConversation());

        //init notifier & processor config
        //there is only one instance each created for whole session
        FilesharingNotifierConfig notifierConfig = FilesharingNotifierConfigManager.getDefault().getNotifierConfig(
                getContext().getCurrentVersion(), true
            );
        getContext().setNotifierConfig(notifierConfig);

        FilesharingProcessorConfig processorConfig = FilesharingProcessorConfigManager.getDefault().getProcessorConfig(
                getContext().getCurrentVersion(), true
            );
        getContext().setProcessorConfig(processorConfig);

        initCollabFilesystem(getUniqueFilesystemId());

        valid = true;

        //create a notifer first
        EventProcessor channelEventProcessor = FilesharingEventProcessorFactory.getDefault().createEventProcessor(
                processorConfig, getContext()
            );
        channelEventNotifier = FilesharingEventNotifierFactory.getDefault().createEventNotifier(
                notifierConfig, channelEventProcessor
            );
        context.setChannelEventNotifier(channelEventNotifier);

        //register channel listener
        channelListener = new FilesharingChannelListener(getContext(), channelEventNotifier);

        //create a timertask to send file changes
        EventProcessor sendFileEventProcessor = FilesharingEventProcessorFactory.getDefault().createEventProcessor(
                processorConfig, getContext()
            );
        EventNotifier sendFileEventNotifier = FilesharingEventNotifierFactory.getDefault().createEventNotifier(
                notifierConfig, sendFileEventProcessor
            );

        //disabled send change timer task, to enable uncomment

        /*FileChangeTimerTask sendFileChangeTimerTask =
                new FileChangeTimerTask(sendFileEventNotifier,
                        new SendChange(new EventContext(
                                SendChange.getEventID(), null)),
                        getContext());
        sendFileChangeTimerTask.scheduleAtFixedRate(
        FilesharingTimerTask.INITIAL_DELAY,
                FilesharingTimerTask.PERIOD +
                FilesharingTimerTask.INTER_DELAY*2);
        timerTasks.add(sendFileChangeTimerTask);*/

        //If a conversation is readOnly do not attach Unlock Timer
        if (!getContext().isReadOnlyConversation()) {
            //create a timertask to send unlock messages
            EventProcessor unlockRegionEventProcessor = FilesharingEventProcessorFactory.getDefault()
                                                                                        .createEventProcessor(
                    processorConfig, getContext()
                );
            EventNotifier unlockRegionEventNotifier = FilesharingEventNotifierFactory.getDefault().createEventNotifier(
                    notifierConfig, unlockRegionEventProcessor
                );
            TimerTask sendUnlockRegionTimerTask = new SwingThreadTask(
                    new UnlockRegionTimerTask(
                        unlockRegionEventNotifier,
                        new UnlockRegionEvent(new UnlockRegionContext(UnlockRegionEvent.getEventID(), null, null)),
                        getContext()
                    )
                );
            getContext().addTimerTask(SEND_UNLOCK_TIMER_TASK, sendUnlockRegionTimerTask);
            getContext().scheduleAtFixedRate(
                sendUnlockRegionTimerTask, FilesharingTimerTask.INITIAL_DELAY,
                FilesharingTimerTask.PERIOD + (FilesharingTimerTask.INTER_DELAY * 3)
            );
            timerTasks.add(sendUnlockRegionTimerTask);
        }

        //If a conversation is public do not send begin join messages
        //if(!getConversation().isPublic())
        if (!getContext().isReadOnlyConversation()) {
            /* send join-begin message after a delay */
            JoinBeginTimerTask sendJoinBeginTimerTask = new JoinBeginTimerTask(
                    channelEventNotifier,
                    new JoinFilesharingBegin(new EventContext(JoinFilesharingBegin.getEventID(), null)), getContext()
                );
            getContext().addTimerTask(SEND_JOINBEGIN_TIMER_TASK, sendJoinBeginTimerTask);
            sendJoinBeginTimerTask.schedule(FilesharingTimerTask.JOIN_BEGIN_DELAY*3);
            timerTasks.add(sendJoinBeginTimerTask);
        }

        //add TC listener
        DocumentTabMarker dtm = new DocumentTabMarker();
        dtm.installListeners();

        //set manager for sharedFileGroups
        SharedFileGroupManager sharedFileGroupManager = new SharedFileGroupManager(getContext());
        getContext().setSharedFileGroupManager(sharedFileGroupManager);

        //set manager for sharedProjects
        SharedProjectManager sharedProjectManager = new SharedProjectManager(getContext());
        getContext().setSharedProjectManager(sharedProjectManager);

        getContext().addUser(context.getLoginUser());
    }

    /**
     *
     *
     */
    public void close() {
        //		CollabFilesystem filesystem = context.getCollabFilesystem();
        //remove context
        removeContext(getConversation());

        for (int i = 0; i < timerTasks.size(); i++) {
            if (timerTasks.get(i) instanceof FilesharingTimerTask) {
                FilesharingTimerTask timerTask = (FilesharingTimerTask) timerTasks.get(i);
                timerTask.cancel();
            }
        }

        getContext().cancelAllTimerTask();

        channelListener.channelClosed();

        valid = false;

        //remove all filehandler references		
        try {
            getContext().removeAllFileHandlerRef();
        } catch (CollabException e) {
            Debug.logDebugException("Exception removing files during leave",
                    e, true);
        }

        //invalidate context
        getContext().setValid(false);

        synchronized (fsChannels) {
            fsChannels.remove(getConversation());
        }
    }

    /**
     *
     * @throws CollabException
     * @return component
     */
    public synchronized JComponent getComponent() throws CollabException {
        if (component == null) {
            component = new FilesystemExplorerPanel(getContext());

            if (getContext().isReadOnlyConversation()) {
                Debug.log("FilesharingCollablet", "FilesharingCollablet, diabling DropFile."); //NoI18n				
                component.setDropFile(false);
            }
        }

        return component;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Message handling methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * acceptMessage
     *
     * @return status
     * @param message
     */
    public boolean acceptMessage(CollabMessage message) {
        //return true;
        // TODO: How do we discriminate chat messages from any other message?
        // TODO: Temporary impl
        return FILESHARING_NAMESPACE.equals(message.getHeader(COLLAB_CHANNEL_TYPE)); //NoI18n    
    }

    /**
     *
     * @param message
     * @throws CollabException
     * @return status
     */
    public boolean handleMessage(CollabMessage message)
    throws CollabException {
        String loginUser = context.getLoginUser();

        return channelListener.handleMessage(message, loginUser);
    }

    /**
     * getContext
     *
     * @return context
     */
    public FilesharingContext getContext() {
        return this.context;
    }

    /**
     * getActivatedComponentContext
     *
     * @return context
     */
    public static FilesharingContext getActivatedComponentContext() {
        if (activatedComponentContext == null) return null;
        return (FilesharingContext)activatedComponentContext.get();
    }

    /**
     * getContext
     *
     * @param conversationName
     * @return context
     */
    public static FilesharingContext getContext(Conversation conversation) {
        FilesharingContext context = null;

        synchronized (staticContexts) {
            Debug.log(
                "FilesharingCollablet",
                "FilesharingCollablet, getContext for " + //NoI18n
                "conv name: " + conversation.getDisplayName()
            ); //NoI18n
            Debug.log(
                "FilesharingCollablet",
                "FilesharingCollablet, # of " + //NoI18n
                "staticContexts: " + staticContexts.size()
            ); //NoI18n
            context = (FilesharingContext) staticContexts.get(conversation);
        }

        return context;
    }

    /**
     * addContext
     *
     * @param conversationName
     * @param context
     */
    public static void addContext(Conversation conversation, FilesharingContext context) {
        synchronized (staticContexts) {
            removeContext(conversation);
            staticContexts.put(conversation, context);
        }
    }

    /**
     * addContext
     *
     * @param conversationName
     */
    public static void removeContext(Conversation conversation) {
        synchronized (staticContexts) {
            if (staticContexts.containsKey(conversation)) {
                staticContexts.remove(conversation);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Support methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @return conversation id
     */
    public synchronized String getConversationID() {
        String conversationID = conversation.getIdentifier();

        int index = conversationID.lastIndexOf("@");
        conversationID = conversationID.substring(0, index);

        return conversationID;
    }

    /**
     * create UniqueFilesystemId
     *
     * @return unique filesystemid
     */
    public String getUniqueFilesystemId() {
        if (getConversation().isPublic()) {
            return getContext().getLoginUser() + "_" + getConversationID() + "_" +
            Calendar.getInstance().getTimeInMillis();
        } else {
            return getContext().getLoginUser() + "_" + getConversationID();
        }
    }

    /**
     *
     * @param filesytemID
     */
    public void initCollabFilesystem(String filesytemID) {
        getContext().setFilesystemID(filesytemID);

        try {
            //create a notifer first
            EventProcessor fileChangeEventProcessor = FilesharingEventProcessorFactory.getDefault()
                                                                                      .createEventProcessor(
                    getContext().getProcessorConfig(), getContext()
                );
            EventNotifier fileChangeEventNotifier = FilesharingEventNotifierFactory.getDefault().createEventNotifier(
                    getContext().getNotifierConfig(), fileChangeEventProcessor
                );

            getCollabFilesystem(); //this will init	

            getContext().setCollabFilesystem(filesystem);
            getContext().setFileChangeEventNotifier(fileChangeEventNotifier);
        } catch (CollabException e) {
            Debug.debugNotify(e);
        }
    }

    /**
     * getCollabFilesystem
     *
     * @return filesystem
     */
    public FileSystem getCollabFilesystem() {
        if (filesystem == null) {
            filesystem = new CollabFilesystem(getContext(), getContext().getFilesystemID());
        }

        return filesystem;
    }

    /**
     *
     *
     */
    private void createCollabFileRoot(File collabFileRoot) {
        int max = FilesharingCollabletFactorySettings.getDefault().getMaxSharedFileFolders().intValue();

        if (max < 1) {
            max = Integer.MAX_VALUE;
        }

        CollabSession[] sessions = getContext().getConversation().getCollabSession().getManager().getSessions();
        int numOfSessions = sessions.length;
        int convs = 0;

        for (int i = 0; i < numOfSessions; i++) {
            convs += sessions[i].getConversations().length;
        }

        File shared = new File(getSharedSystemFolder());

        if (shared.exists()) {
            File[] dirs = shared.listFiles();

            while ((dirs != null) && (dirs.length >= max) && (max > convs)) {
                deleteOldestDirectory(dirs);
                dirs = shared.listFiles();
            }
        }

        collabFileRoot.mkdirs();
    }

    /**
     *
     * @return filesystem root
     */
    public String getCollabRoot(String filesystemID) {
        return getSharedSystemFolder() + File.separator + filesystemID; // NOI18N
    }

    /**
     *
     *
     */
    private String getSharedSystemFolder() {
        String userHome = System.getProperty("netbeans.user"); // NOI18N
        String sharedFiles = userHome + File.separator + "collab" + // NOI18N
            File.separator + "shared_files"; // NOI18N

        return sharedFiles;
    }

    /**
     *
     *
     */
    private void deleteOldestDirectory(File[] dirs) {
        long lastModified = 0;
        long oldest = 0;
        File oldestFolder = new File("temp");

        for (int i = 0; i < dirs.length; i++) {
            lastModified = dirs[i].lastModified();

            if (lastModified == 0) {
                continue;
            }

            if (i == 0) {
                oldest = lastModified;
                oldestFolder = dirs[i];
            } else {
                if (lastModified < oldest) {
                    oldest = lastModified;
                    oldestFolder = dirs[i];
                }
            }
        }

        try {
            deleteFolder(oldestFolder);
        } catch (Exception e) {
            Debug.debugNotify(e);
            e.printStackTrace();
        }
    }

    /**
     * delete file/folder
     *
     * @param file/folder
     */
    public void deleteFolder(File file) {
        if (file == null) {
            return;
        }

        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();

            for (int i = 0; i < files.length; i++) {
                deleteFolder(files[i]);
            }

            //Delete self
            file.delete();
        }
    }

    /**
     *
     * @return
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * getConversations
     *
     * @param project
     * @return conversation
     */
    public synchronized static Conversation[] getConversations(Project project) {
        synchronized (fsChannels) {
            List conversations = new ArrayList();

            for (Iterator it = fsChannels.values().iterator(); it.hasNext();) {
                FilesharingCollablet channel = (FilesharingCollablet) it.next();
                Debug.out.println(
                    "FSChannel, getConversations, channel: " + channel.getConversation().getDisplayName()
                );

                if (channel.getContext().getSharedProjectManager().isSharedOriginal(project)) {
                    Debug.out.println(
                        "FSChannel, getConversations, project match: " + project.getProjectDirectory().getPath()
                    );
                    conversations.add(channel.getConversation());
                }
            }

            return (Conversation[]) conversations.toArray(new Conversation[0]);
        }
    }

    /**
     * isShared
     *
     * @param fileObject
     * @return true if shared
     */
    public synchronized static boolean isShared(FileObject fileObject)
    throws CollabException {
        synchronized (fsChannels) {
            List conversations = new ArrayList();

            for (Iterator it = fsChannels.values().iterator(); it.hasNext();) {
                FilesharingCollablet channel = (FilesharingCollablet) it.next();

                if (channel.getContext().getSharedFileGroupManager().isShared(fileObject)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * getContext
     *
     * @param fileObject
     * @return if shared return context
     */
    public synchronized static FilesharingContext getContext(FileObject fileObject)
    throws CollabException {
        synchronized (fsChannels) {
            List conversations = new ArrayList();

            for (Iterator it = fsChannels.values().iterator(); it.hasNext();) {
                FilesharingCollablet channel = (FilesharingCollablet) it.next();

                if (channel.getContext().getSharedFileGroupManager().isShared(fileObject)) {
                    return channel.getContext();
                }
            }

            return null;
        }
    }

    /**
     * getAllConversations
     *
     * @param projectName
     */
    public synchronized static Conversation[] getAllConversations() {
        synchronized (fsChannels) {
            List conversations = new ArrayList();

            for (Iterator it = fsChannels.values().iterator(); it.hasNext();) {
                FilesharingCollablet channel = (FilesharingCollablet) it.next();
                conversations.add(channel.getConversation());
            }

            return (Conversation[]) conversations.toArray(new Conversation[0]);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property change support
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        // In order to ensure that the channel's shared files are picked up
        // in preference to any local file when it is in scope
        if (event.getPropertyName().equals("conversationActivated")) // NOI18N
         {
            Debug.log("FilesharingCollablet", "FilesharingCollablet, conversationActivated");

            Object newValue = event.getNewValue();

            if ((newValue != null) && newValue instanceof Boolean) {
                if (((Boolean) newValue).booleanValue()) {
                    activatedComponentContext = new WeakReference(getContext());
                }
            }
        }

        if (event.getSource() instanceof Conversation) {
            String propertyName = event.getPropertyName();

            if (Conversation.PROP_PARTICIPANTS.equals(propertyName)) {
                boolean joined = event.getNewValue() != null;
                CollabPrincipal principal = joined ? (CollabPrincipal) event.getNewValue()
                                                   : (CollabPrincipal) event.getOldValue();

                try {
                    //push send-file if public conversation and this user is the manager
                    if (
                        joined && getConversation().isPublic() &&
                            (getConversation().getPrivilege() == ConversationPrivilege.MANAGE)
                    ) {
                        String userName = principal.getName();
                        int joinUserPrevilige;

                        try {
                            joinUserPrevilige = getConversation().getPrivilege(principal);
                        } catch (CollabException ce) {
                            Debug.log(
                                "FilesharingCollablet",
                                "FilesharingCollablet, " + "couldn't find previlige for user: " + userName
                            );

                            return;
                        }

                        Debug.log(
                            "FilesharingCollablet",
                            "FilesharingCollablet, " + userName + " joined, has previlige: " + joinUserPrevilige
                        );

                        if (joinUserPrevilige != ConversationPrivilege.READ) {
                            Debug.log("FilesharingCollablet", "FilesharingCollablet, " + " do not sync up");

                            return;
                        }

                        Debug.log(
                            "FilesharingCollablet",
                            "FilesharingCollablet, " + userName + " joined, user:" + context.getLoginUser() +
                            " has manage previlige, sync happens now"
                        );

                        //do sync operation
                        getContext().doSyncOperation(false);
                    } else {
                        Debug.log(
                            "FilesharingCollablet",
                            "FilesharingCollablet, " + principal.getDisplayName() + " joined, but user:" +
                            context.getLoginUser() + " has no manage previlige"
                        );
                    }
                } catch (Exception e) {
                    // TODO: Do something appropriate here
                    Debug.errorManager.notify(e);
                }
            }
        }
    }

    /**
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     *
     * @param selection
     */
    public void fireNodeSelectionChange(Node[] selection) {
        changeSupport.firePropertyChange(PROP_SELECTED_NODES, null, selection);
    }

    /**
     *
     * @return changeSupport
     */
    public PropertyChangeSupport getChangeSupport() {
        return changeSupport;
    }
}
