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
package org.netbeans.modules.collab.channel.filesharing;

import com.sun.collablet.*;
import org.netbeans.modules.collab.channel.filesharing.eventhandler.LockRegionManager;
import org.netbeans.modules.collab.channel.filesharing.filehandler.RegionInfo;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData;

import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.nodes.Node;

import java.io.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.channel.filesharing.annotations.CollabRegionAnnotation;
import org.netbeans.modules.collab.channel.filesharing.annotations.RegionAnnotation1;
import org.netbeans.modules.collab.channel.filesharing.annotations.RegionAnnotation2;
import org.netbeans.modules.collab.channel.filesharing.annotations.RegionAnnotation3;
import org.netbeans.modules.collab.channel.filesharing.annotations.RegionAnnotation4;
import org.netbeans.modules.collab.channel.filesharing.annotations.RegionAnnotation5;
import org.netbeans.modules.collab.channel.filesharing.annotations.RegionAnnotation6;
import org.netbeans.modules.collab.channel.filesharing.annotations.RegionAnnotation7;
import org.netbeans.modules.collab.channel.filesharing.annotations.RegionAnnotation8;
import org.netbeans.modules.collab.channel.filesharing.annotations.RegionAnnotation9;
import org.netbeans.modules.collab.channel.filesharing.config.FilesharingNotifierConfig;
import org.netbeans.modules.collab.channel.filesharing.config.FilesharingProcessorConfig;
import org.netbeans.modules.collab.channel.filesharing.context.ProjectContext;
import org.netbeans.modules.collab.channel.filesharing.event.JoinFilesharingEnd;
import org.netbeans.modules.collab.channel.filesharing.event.PauseFilesharingEvent;
import org.netbeans.modules.collab.channel.filesharing.event.ProjectActionListEvent;
import org.netbeans.modules.collab.channel.filesharing.event.SendFileEvent;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.JoinEndTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.PauseTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.ProjectActionListTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.SendFileTimerTask;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandlerSupport;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroupManager;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabEvent;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabDocumentListener;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroup;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups;
import org.netbeans.modules.collab.channel.filesharing.msgbean.User;
import org.netbeans.modules.collab.channel.filesharing.msgbean.Users;
import org.netbeans.modules.collab.channel.filesharing.projecthandler.SharedProject;
import org.netbeans.modules.collab.channel.filesharing.projecthandler.SharedProjectManager;
import org.netbeans.modules.collab.channel.filesharing.ui.FilesystemExplorerPanel;
import org.netbeans.modules.collab.channel.filesharing.util.CollabQueue;
import org.netbeans.modules.collab.core.Debug;


/**
 * Bean that holds channel context
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class FilesharingContext extends CollabContext implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* conversation */
    private Conversation conversation = null;

    /* filesharing channel content type */
    private final String contentType = ContentTypes.XML;
    private SharedFileGroupManager sharedFileGroupManager = null;
    private SharedProjectManager sharedProjectManager = null;

    /* user */
    private String loginUser = null;

    /* filesystemID */
    private String filesystemID = null;

    /* filesystem */
    private FileSystem filesystem = null;

    /* to test if this channel is valid */
    private boolean valid = true;

    /* message */
    private CollabMessage message = null;

    /* channel notifier */
    private EventNotifier channelEventNotifier = null;

    /* filechange notifier */
    private EventNotifier fileChangeEventNotifier = null;

    /* currentState */
    private int currentState = FilesharingContext.STATE_UNKNOWN;

    /* document listener map */
    private HashMap collabDocumentListenerMap = new HashMap();

    /* save join user */
    private String saveJoinUser = null;

    /* saved expected files for sync-up */
    private HashMap saveExpectedFileMap = new HashMap();

    /* flag to indicate the join user is this user */
    private boolean setJoinFlag = false;

    /* to indicate this user as moderator */
    private boolean isModerator = false;

    /* user to fileGroup name map */
    private HashMap userFileGroupMap = new HashMap();

    /* file to owner map for file annotations*/
    private HashMap ownerFileMap = new HashMap();

    /* file owners */
    private List fileOwners = new ArrayList();

    /* users in conversation */
    private List allUsers = new ArrayList();

    /* to indicate receive message in progress */
    private boolean receiveMessageInProgressFlag = false;

    /* flag set so that the originator of conversation can send join message */
    private boolean messageJoinSend = false;

    /* flag set to disable sending files during filesystem changes */
    private boolean isReadyToSendFile = false;

    /* flag set to disable sending files during pause */
    private boolean inPauseState = false;

    /* skipSendFile map*/
    private HashMap skipSendFileMap = new HashMap();

    /* skipSendDeleteFile map*/
    private HashMap skipSendDeleteFileMap = new HashMap();

    /* the timer */
    private java.util.Timer timer = null;

    /* notifierConfig */
    private FilesharingNotifierConfig notifierConfig = null;

    /* processor config */
    private FilesharingProcessorConfig processorConfig = null;

    /* queue */
    private CollabQueue queue = null;
    private CollabRegionAnnotation[] annotationTypes;
    private Map annotationStyles = new HashMap();
    private int lastAnnotationIndex = 0;
    private FilesystemExplorerPanel explorerPanel = null;
    private HashMap timertaskMap = new HashMap();

    /* userStyles */
    private HashMap userStyles = new HashMap();
    private int[] stylesArray = new int[22];
    private long joinTimeStamp = -1;
    private boolean readOnlyConversation = false;
    private FileChangeListener scfl;
    private FileSystem scfs = null;

    /**
         * @param version
         * @param channel
         */
    public FilesharingContext(String currentVersion, Collablet collablet) {
        super(currentVersion, collablet);

        //init queue
        queue = new CollabQueue(this);

        //init annotation types
        initAnnotationTypes();

        //init timestamp
        getJoinTimeStamp();
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * getConversation
     *
     * @return conversation
     */
    public Conversation getConversation() {
        return this.conversation;
    }

    /**
     * setConversation
     *
     * @param conversation
     */
    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    /**
     *
     * @return input content type
     */
    public String getInputContentType() {
        return this.contentType;
    }

    /**
     * getSharedFileGroupManager
     *
     * @return sharedFileGroupManager
     */
    public SharedFileGroupManager getSharedFileGroupManager() {
        return this.sharedFileGroupManager;
    }

    /**
     * setSharedFileGroupManager
     *
     * @param sharedFileGroupManager
     */
    public void setSharedFileGroupManager(SharedFileGroupManager sharedFileGroupManager) {
        this.sharedFileGroupManager = sharedFileGroupManager;
    }

    /**
     * getSharedProjectManager
     *
     * @return sharedProjectManager
     */
    public SharedProjectManager getSharedProjectManager() {
        return this.sharedProjectManager;
    }

    /**
     * setSharedProjectManager
     *
     * @param sharedProjectManager
     */
    public void setSharedProjectManager(SharedProjectManager sharedProjectManager) {
        this.sharedProjectManager = sharedProjectManager;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Message handling methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * getQueue
     *
     * @return queue
     */
    public CollabQueue getQueue() {
        return this.queue;
    }

    /**
     * getProcessorConfig
     *
     * @return notifier config
     */
    public FilesharingNotifierConfig getNotifierConfig() {
        return this.notifierConfig;
    }

    /**
     * setProcessorConfig
     *
     * @param notifierConfig
     */
    public void setNotifierConfig(FilesharingNotifierConfig notifierConfig) {
        this.notifierConfig = notifierConfig;
    }

    /**
     * getProcessorConfig
     *
     * @return processor config
     */
    public FilesharingProcessorConfig getProcessorConfig() {
        return this.processorConfig;
    }

    /**
     * setProcessorConfig
     *
     * @param processorConfig
     */
    public void setProcessorConfig(FilesharingProcessorConfig processorConfig) {
        this.processorConfig = processorConfig;
    }

    /**
     * getChannelEventNotifier
     *
     * @return event notifier for the channel events
     */
    public EventNotifier getChannelEventNotifier() {
        return this.channelEventNotifier;
    }

    /**
     * setChannelEventNotifier
     *
     * @param eventNotifier
     */
    public void setChannelEventNotifier(EventNotifier eventNotifier) {
        this.channelEventNotifier = eventNotifier;
    }

    /**
     * getFileChangeEventNotifier
     *
     * @return event notifier for the channel events
     */
    public EventNotifier getFileChangeEventNotifier() {
        return this.fileChangeEventNotifier;
    }

    /**
     * setFileChangeEventNotifier
     *
     * @param eventNotifier
     */
    public void setFileChangeEventNotifier(EventNotifier eventNotifier) {
        this.fileChangeEventNotifier = eventNotifier;
    }

    /**
     * getCollabFilesystem
     *
     * @return filesystem
     */
    public FileSystem getCollabFilesystem() {
        return filesystem;
    }

    /**
     * setCollabFilesystem
     *
     * @param filesystem
     */
    public void setCollabFilesystem(FileSystem filesystem) {
        this.filesystem = filesystem;
    }

    /**
     * setFilesystemExplorer
     *
     * @param explorerPanel
     */
    public void setFilesystemExplorer(FilesystemExplorerPanel explorerPanel) {
        this.explorerPanel = explorerPanel;
    }

    /**
     * getFilesystemExplorer
     *
     * @return explorerPanel
     */
    public FilesystemExplorerPanel getFilesystemExplorer() {
        return this.explorerPanel;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Support methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @return filesystem id
     */
    public String getFilesystemID() {
        return filesystemID;
    }

    /**
     *
     * @param filesystemID
     */
    public void setFilesystemID(String filesystemID) {
        this.filesystemID = filesystemID;
    }

    /**
     *
     *
     */
    public void sendMessage(String content) {
        // Send a message to the conference
        try {
            CollabMessage message = conversation.createMessage();

            // Prep the message with the content
            //message.setContentName("FILESHARING");
            message.setContentName(FILESHARING_NAMESPACE);
            message.setContent(content);
            message.setContentType(ContentTypes.TEXT);

            // Set the actual content type to a different, chat-specific header.
            // The message content type will always be text/plain
            message.setHeader(FILESHARING_CONTENT_TYPE_HEADER, getInputContentType());

            // TODO: Temp impl
            message.setHeader(COLLAB_CHANNEL_TYPE, FILESHARING_NAMESPACE); //NoI18n

            // TODO: Tag the message as belonging to the chat channel
            // Send the message
            conversation.sendMessage(message);
        } catch (CollabException e) {
            //Hack for empty conversation
            if (!e.getMessage().trim().equals("First invite a user")) //NoI18n
             {
                Debug.errorManager.notify(e);
            }
        }
    }

    /**
     *
     * @return message
     */
    public CollabMessage getMessage() {
        return this.message;
    }

    /**
     *
     * @return login user id
     */
    public String getLoginUser() {
        if (loginUser == null) {
            loginUser = conversation.getCollabSession().getUserPrincipal().getName();
        }

        return loginUser;
    }

    /**
     *
     * @return timer
     */
    public java.util.Timer getTimer() {
        return getTimer(false);
    }

    /**
     * @param forceCreation
     * @return timer
     */
    public java.util.Timer getTimer(boolean forceCreation) {
        if (forceCreation || (timer == null)) {
            //unfortunately conversation timer cannot be used, since it gets 
            //canceled during conversation at some point
            //return channel.getConversation().getTimer();
            //set demeon true
            this.timer = new java.util.Timer(true);
        }

        return this.timer;
    }

    /**
     * schedule
     *
     * @param task
     * @param delay
     */
    public void schedule(TimerTask task, long delay) {
        try {
            getTimer().schedule(task, delay);
        } catch (java.lang.IllegalStateException ise) {
            try {
                //force creation of timer
                getTimer(true).schedule(task, delay);
            } catch (java.lang.Throwable th) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "CollabFileHandlerSupport, timer null, cannot schedule " + //NoI18n
                    "task for delay: " + //NoI18n
                    delay
                );
            }
        } catch (java.lang.Throwable th) {
            try {
                //force creation of timer
                getTimer(true).schedule(task, delay);
            } catch (java.lang.Throwable th1) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "CollabFileHandlerSupport, timer null, cannot schedule " + //NoI18n
                    "task for delay: " + //NoI18n
                    delay
                );
            }
        }
    }

    /**
     * scheduleAtFixedRate
     *
     * @param task
     * @param delay
     * @param rate
     */
    public void scheduleAtFixedRate(TimerTask task, long delay, long rate) {
        try {
            getTimer().scheduleAtFixedRate(task, delay, rate);
        } catch (java.lang.IllegalStateException ise) {
            try {
                //force creation of timer
                getTimer(true).scheduleAtFixedRate(task, delay, rate);
            } catch (java.lang.Throwable th) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "CollabFileHandlerSupport, timer null, cannot schedule task " + "for delay: " + delay + ", rate: " +
                    rate
                ); //NoI18n
            }
        } catch (java.lang.Throwable th) {
            try {
                //force creation of timer
                getTimer(true).scheduleAtFixedRate(task, delay, rate);
            } catch (java.lang.Throwable th1) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "CollabFileHandlerSupport, timer null, cannot schedule task " + "for delay: " + delay + ", rate: " +
                    rate
                ); //NoI18n
            }
        }
    }

    /**
     *
     * @param fileGroups
     * @param sharedFileGroups
     */
    public void constructFileGroups(FileGroups fileGroups, SharedFileGroup[] sharedFileGroups) {
        for (int i = 0; i < sharedFileGroups.length; i++) {
            if (sharedFileGroups[i] == null) {
                continue;
            }

            FileGroup fileGroup = new FileGroup();
            fileGroup.setFileGroupName(sharedFileGroups[i].getName());

            User user = new User();
            user.setId(sharedFileGroups[i].getUser());
            fileGroup.setUser(user);

            CollabFileHandler[] fileHandlers = sharedFileGroups[i].getFileHandlers();
            String[] fileNames = new String[fileHandlers.length];

            for (int j = 0; j < fileHandlers.length; j++) {
                fileNames[j] = fileHandlers[j].getName();
            }

            fileGroup.setFileName(fileNames);
            fileGroups.addFileGroup(fileGroup);
        }
    }

    /**
     *
     * @param fileGroups
     * @param sharedFileGroup
     */
    public void constructFileGroups(FileGroups fileGroups, SharedFileGroup sharedFileGroup) {
        FileGroup fileGroup = new FileGroup();
        fileGroup.setFileGroupName(sharedFileGroup.getName());

        User user = new User();
        user.setId(sharedFileGroup.getUser());
        fileGroup.setUser(user);

        CollabFileHandler[] fileHandlers = sharedFileGroup.getFileHandlers();
        String[] fileNames = new String[fileHandlers.length];

        for (int j = 0; j < fileHandlers.length; j++) {
            fileNames[j] = fileHandlers[j].getName();
        }

        fileGroup.setFileName(fileNames);
        fileGroups.addFileGroup(fileGroup);
    }

    /**
     *
     * @param fromSharedFileGroups
     * @param toFileGroups
     */
    public void copyFileGroups(SharedFileGroup[] fromSharedFileGroups, FileGroups toFileGroups) {
        if ((fromSharedFileGroups == null) || (fromSharedFileGroups.length == 0)) {
            return;
        }

        for (int i = 0; i < fromSharedFileGroups.length; i++) {
            FileGroup fileGroup = new FileGroup();
            SharedFileGroup sharedFileGroup = fromSharedFileGroups[i];
            String fileGroupName = sharedFileGroup.getName();
            fileGroup.setFileGroupName(fileGroupName);

            User user = new User();
            user.setId(sharedFileGroup.getUser());
            fileGroup.setUser(user);

            CollabFileHandler[] fileHandlers = sharedFileGroup.getFileHandlers();
            String[] fileNames = new String[fileHandlers.length];

            for (int j = 0; j < fileHandlers.length; j++) {
                fileNames[j] = fileHandlers[j].getName();
            }

            toFileGroups.addFileGroup(fileGroup);
        }
    }

    /**
     *
     * @param userid
     * @return User object
     */
    public Users constructUsers(String userId) {
        Users users = new Users();
        User user = new User();
        user.setId(userId);
        users.addUser(user);

        return users;
    }

    /**
     *
     * @param userid
     * @return User object
     */
    public Users constructUsers(String[] userids) {
        Users users = new Users();

        for (int i = 0; i < userids.length; i++) {
            User user = new User();
            user.setId(userids[i]);
            users.addUser(user);
        }

        return users;
    }

    /**
     *
     * @param moderator object
     * @return String modeartor id
     */
    public String getModerator(Users users) {
        if (users != null) {
            User[] user = users.getUser();

            if ((user != null) && (user.length > 0)) {
                return user[0].getId();
            }
        }

        return null;
    }

    /**
     *
     * @param NewFileOwner object
     * @return String newFileOwner id
     */
    public String getNewFileOwner(Users users) {
        if (users != null) {
            User[] user = users.getUser();

            if ((user != null) && (user.length > 0)) {
                return user[0].getId();
            }
        }

        return null;
    }

    /**
     *
     * @param NewFileOwner object
     * @return String newFileOwner id
     */
    public String getNewModerator(Users users) {
        if (users != null) {
            User[] user = users.getUser();

            if ((user != null) && (user.length > 0)) {
                return user[0].getId();
            }
        }

        return null;
    }

    /**
     *
     * @param collabBean
     * @throws CollabException
     */
    public void sendMessage(CCollab collabBean) throws CollabException {
        // Send a message to the conference
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            collabBean.write(outputStream);
            sendMessage(new String(outputStream.toByteArray(), "UTF-8")); //NoI18n
        } catch (IOException iox) {
            throw new CollabException(iox);
        }
    }

    /**
     *
     * @param message
     * @throws CollabException
     * @return
     */
    public CCollab parse(CollabMessage message) throws CollabException {
        CCollab collab = null;
        StringBuffer buffer = new StringBuffer();

        try {
            CollabMessagePart[] parts = message.getParts();
            int countPartsWithoutFileSharing = 0;

            for (int i = 0; i < parts.length; i++) {
                String contentName = parts[i].getContentName();
                String contentType = parts[i].getContentType();
                buffer.append(parts[i].getContent());
            }
        } catch (Throwable e) {
            throw new CollabException(e);
        }

        String content = buffer.toString();

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
            collab = CCollab.read(inputStream);
        } catch (IOException iox) {
            throw new CollabException(iox);
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
            throw new CollabException(pce);
        } catch (org.xml.sax.SAXException pce) {
            throw new CollabException(pce);
        }

        return collab;
    }

    /**
     *
     * @return fileOwners
     */
    public String[] getSavedFileOwners() {
        synchronized (fileOwners) {
            return (String[]) getFileOwners().toArray(new String[0]);
        }
    }

    /**
     *
     * @return fileOwners
     */
    public List getFileOwners() {
        return fileOwners;
    }

    /**
     *
     * @param user
     */
    public void addFileOwner(String user) {
        synchronized (fileOwners) {
            if (!fileOwners.contains(user)) {
                fileOwners.add(user);
            }
        }
    }

    /**
     * removeFileOwner
     *
     * @param user
     * @return status
     */
    public boolean removeFileOwner(String user) {
        synchronized (fileOwners) {
            return fileOwners.remove(user);
        }
    }

    /**
     * removeAllFileOwner
     *
     * @return
     */
    public void removeAllFileOwner() {
        synchronized (fileOwners) {
            fileOwners.clear();
        }
    }

    /**
     *
     * @param fileGroups
     * @throws CollabException
     */
    public void addToFileOwnerMap(FileGroup[] fileGroups)
    throws CollabException {
        Debug.log(this, "FilesharingContext, addToFileOwnerMap size: " + fileGroups.length);

        for (int i = 0; i < fileGroups.length; i++) {
            String user = fileGroups[i].getUser().getId();
            Debug.log(this, "FilesharingContext, user: " + user);

            String[] fileNames = fileGroups[i].getFileName();

            for (int j = 0; j < fileNames.length; j++) {
                Debug.log(this, "FilesharingContext, fileNames[" + j + "]: " + fileNames[j]);
                addToFileOwnerMap(fileNames[j], user);
            }
        }
    }

    /**
     *
     * @param fileGroups
     * @throws CollabException
     */
    public void addToFileOwnerMap(SharedFileGroup sharedFileGroup)
    throws CollabException {
        CollabFileHandler[] fileHandlers = sharedFileGroup.getFileHandlers();
        String[] fileNames = new String[fileHandlers.length];

        for (int j = 0; j < fileHandlers.length; j++) {
            fileNames[j] = fileHandlers[j].getName();
        }

        String user = sharedFileGroup.getUser();

        if (fileNames != null) {
            for (int i = 0; i < fileNames.length; i++) {
                addToFileOwnerMap(fileNames[i], user);
            }
        }
    }

    /**
     *
     * @param fileName
     * @throws CollabException
     */
    public void addToFileOwnerMap(String fileName) throws CollabException {
        synchronized (ownerFileMap) {
            if (!ownerFileMap.containsKey(fileName)) {
                ownerFileMap.put(fileName, getLoginUser());
            }
        }
    }

    /**
     *
     * @param fileName
     * @throws CollabException
     */
    public void addToFileOwnerMap(String fileName, String user)
    throws CollabException {
        synchronized (ownerFileMap) {
            if (!ownerFileMap.containsKey(fileName)) {
                Debug.log(
                    this, "FilesharingContext, addToFileOwnerMap fileName: " + fileName + " user:" + getLoginUser()
                );
                ownerFileMap.put(fileName, user);
            }
        }
    }

    /**
     *
     * @param fileName
     * @return owner
     */
    public String getOwnerForFile(String fileName) {
        synchronized (ownerFileMap) {
            return (String) ownerFileMap.get(fileName);
        }
    }

    /**
     *
     * @param fileName
     * @return
     */
    public String removeFileOwnerMap(String fileName) {
        synchronized (ownerFileMap) {
            return (String) ownerFileMap.remove(fileName);
        }
    }

    /**
     *
     * @param SharedFileGroup
     * @return
     */
    public void removeFileOwnerMap(SharedFileGroup sharedFileGroup) {
        CollabFileHandler[] fileHandlers = sharedFileGroup.getFileHandlers();
        String[] fileNames = new String[fileHandlers.length];

        for (int j = 0; j < fileHandlers.length; j++) {
            fileNames[j] = fileHandlers[j].getName();
        }

        String user = sharedFileGroup.getUser();

        if (fileNames != null) {
            for (int i = 0; i < fileNames.length; i++) {
                removeFileOwnerMap(fileNames[i]);
            }
        }
    }

    /**
     *
     * @param fileGroups
     */
    public void saveExpectedFile(FileGroup[] fileGroups) {
        if (fileGroups == null) {
            return;
        }

        for (int i = 0; i < fileGroups.length; i++) {
            String fileGroupName = fileGroups[i].getFileGroupName();

            synchronized (saveExpectedFileMap) {
                saveExpectedFileMap.put(fileGroupName, fileGroups[i]);
            }
        }
    }

    /**
     *
     * @param user
     * @return
     */
    public boolean isModerator(String user) {
        return true;
    }

    /**
     *
     * @param user
     */
    public void addUser(String user) {
        synchronized (allUsers) {
            if (!allUsers.contains(user)) {
                allUsers.add(user);
            }
        }
    }

    /**
     *
     * @return
     */
    public String[] getUser() {
        synchronized (allUsers) {
            return (String[]) allUsers.toArray(new String[0]);
        }
    }

    /**
     *
     * @param user
     * @return
     */
    public boolean removeUser(String user) {
        synchronized (allUsers) {
            return allUsers.remove(user);
        }
    }

    public String getVersion() {
        return COLLAB_VERSION;
    }

    /**
     *
     * @param fileName
     * @param count
     * @return
     */
    public String createUniqueRegionName(String fileName, int count) {
        return getLoginUser() + fileName + String.valueOf(count);
    }

    /**
     *
     * @param user
     * @return
     */
    public boolean isFileOwner(String user, String fileName) {
        if ((user != null) && !user.equals("")) {
            String owner = getOwnerForFile(fileName);

            if ((owner != null) && !owner.equals("")) {
                return user.equals(owner);
            }
        }

        return false;
    }

    /**
     *
     * @param user
     * @return
     */
    public boolean isFileOwner(String user) {
        if ((user != null) && !user.equals("")) {
            return getLoginUser().equals(user);
        }

        return false;
    }

    /**
     *
     * @param users
     * @return
     */
    public boolean isFileOwner(String[] users) {
        if ((users != null) && (users.length > 0)) {
            for (int i = 0; i < users.length; i++) {
                String user = users[i];

                if (getLoginUser().equals(user)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     *
     * @param users
     * @return
     */
    public boolean isFileOwner(User[] users) {
        Debug.log(this, "FilesharingContext, isFileOwner : " + getLoginUser());

        if ((users != null) && (users.length > 0)) {
            for (int i = 0; i < users.length; i++) {
                String user = users[i].getId();
                Debug.log(this, "FilesharingContext, user : " + user);

                if ((user != null) && !user.trim().equals("") && getLoginUser().equals(user)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     *
     * @return
     */
    public String[] getUserSharedFileGroupNames(String user) {
        synchronized (userFileGroupMap) {
            List filegroups = (List) userFileGroupMap.get(user);

            if ((filegroups != null) && !filegroups.isEmpty()) {
                return (String[]) filegroups.toArray(new String[0]);
            }

            return null;
        }
    }

    /**
     *
     * @param name
     * @param object
     */
    public void addToUserSharedFileGroupNames(String user, String fileGroupName) {
        synchronized (userFileGroupMap) {
            if (!userFileGroupMap.containsKey(user)) {
                List aList = new ArrayList();
                aList.add(fileGroupName);
                userFileGroupMap.put(user, aList);
            } else {
                List aList = (List) userFileGroupMap.get(user);

                if ((aList != null) && !aList.contains(fileGroupName)) {
                    aList.add(fileGroupName);
                }
            }
        }
    }

    /**
     * removeFromUserFileGroup
     *
     * @param user
     * @param fileGroupName
     * @throws CollabException
     */
    public void removeFromUserFileGroup(String user, String fileGroupName)
    throws CollabException {
        synchronized (userFileGroupMap) {
            if (userFileGroupMap.containsKey(user)) {
                List aList = (List) userFileGroupMap.get(user);

                if ((aList != null) && aList.contains(fileGroupName)) {
                    aList.remove(fileGroupName);
                }

                if (aList.size() == 0) {
                    userFileGroupMap.remove(user);
                }
            }
        }
    }

    /**
     * removeAllUserFileGroup
     *
     * @throws CollabException
     */
    public void removeAllUserFileGroup() throws CollabException {
        synchronized (userFileGroupMap) {
            userFileGroupMap.clear();
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
     *
     * @param valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     *
     * @return
     */
    public boolean isSkipSendFile(String fileName) {
        synchronized (skipSendFileMap) {
            Debug.log(
                this, //NoI18n
                "FilesharingContext, isSkipSendFile: " + //NoI18n
                skipSendFileMap.containsKey(fileName) + " for file: " + //NoI18n
                fileName
            );

            return skipSendFileMap.containsKey(fileName);
        }
    }

    /**
     *
     * @return
     */
    public boolean isPauseState() {
        return inPauseState;
    }

    public void setInPauseState(boolean flag) {
        inPauseState = flag;
    }

    /**
     *
     * @return
     */
    public boolean isSkipSendDeleteFile(String fileName) {
        synchronized (skipSendDeleteFileMap) {
            Debug.log(
                this, //NoI18n
                "FilesharingContext, isSkipSendDeleteFile: " + //NoI18n
                skipSendDeleteFileMap.containsKey(fileName) + " for file: " + //NoI18n
                fileName
            );

            return skipSendDeleteFileMap.containsKey(fileName);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void setReceivedMessageState(boolean flag) {
        this.receiveMessageInProgressFlag = flag;
    }

    /**
     *
     *
     */
    private boolean receiveMessageInProgress() {
        return this.receiveMessageInProgressFlag;
    }

    /**
     *        remove All FileHandler References
     *
     * @param fileGroupName
     * @param user
     * @throws CollabException
     */
    public void removeAllFileHandlerRef() throws CollabException {
        SharedFileGroup[] sharedFileGroups = sharedFileGroupManager.getAllSharedFileGroup();

        for (int i = 0; i < sharedFileGroups.length; i++) {
            removeAllFileHandlerRef(sharedFileGroups[i], getLoginUser());
        }

        removeAllUserFileGroup();

        //clear all ref in managers
        sharedFileGroupManager.clear();
        sharedProjectManager.clear();

        //remove shared common filelistener
        Debug.out.println("removing scfl");

        if ((scfs != null) && (scfl != null)) {
            scfs.removeFileChangeListener(scfl);
        }
    }

    /**
     *        remove All FileHandler References
     *
     * @param fileGroupName
     * @param user
     * @throws CollabException
     */
    public void removeAllFileHandlerRef(SharedFileGroup sharedFileGroup, String user)
    throws CollabException {
        String fileGroupName = sharedFileGroup.getName();
        Debug.log("FilesharingContext", "In removeAllFileHandlerRef for file: " + fileGroupName);

        //remove file to owner map for this sfg
        removeFileOwnerMap(sharedFileGroup);

        CollabFileHandler[] fileHandlers = sharedFileGroup.getFileHandlers();
        String primaryFile = null;

        if (fileHandlers.length > 0) {
            primaryFile = fileHandlers[0].getName();
        }

        for (int i = 0; i < fileHandlers.length; i++) {
            String fileName = fileHandlers[i].getName();
            removeDocumentListener(fileName);
            sharedFileGroup.removeFileHandler(fileName);
        }

        sharedFileGroupManager.removeFromAllSharedFileGroup(sharedFileGroup);
        sharedFileGroupManager.removeFromOwnerSharedFileGroup(sharedFileGroup);

        if (primaryFile != null) { //remove fileowner if last file belong to this user is deleted

            String owner = getOwnerForFile(primaryFile);
            String[] userFileGroupNames = getUserSharedFileGroupNames(owner);

            if (
                (userFileGroupNames != null) && (userFileGroupNames.length == 1) &&
                    userFileGroupNames[0].equals(fileGroupName)
            ) {
                removeFileOwner(owner);
            }
        }

        //clear all owners if there is no sharedfiles in the system
        if (sharedFileGroupManager.getAllSharedFileGroup().length == 0) {
            removeAllFileOwner();
            removeAllUserFileGroup();
        }

        removeFromUserFileGroup(user, fileGroupName);
    }

    /**
     *        printAllData
     *
     */
    public void printAllData(String message) {
        if (!Debug.isAllowed(this)) {
            return;
        }

        Debug.log(
            this, "\n\n========Printing Context for user: " + //NoI18n	
            getLoginUser() + " " + message + "=========="
        ); //NoI18n
        sharedProjectManager.print();
        sharedFileGroupManager.print();

        synchronized (collabDocumentListenerMap) {
            Debug.log(this, "collabDocumentListenerMap: " + //NoI18n	
                collabDocumentListenerMap.keySet().toString()
            ); //NoI18n
        }

        synchronized (saveExpectedFileMap) {
            Debug.log(this, "saveExpectedFileMap: " + //NoI18n	
                saveExpectedFileMap.toString()
            );
        }

        synchronized (userFileGroupMap) {
            Debug.log(this, "userFileGroupMap: " + userFileGroupMap.toString()); //NoI18n
        }

        synchronized (ownerFileMap) {
            Debug.log(this, "ownerFileMap: " + ownerFileMap.toString()); //NoI18n
        }

        synchronized (fileOwners) {
            Debug.log(this, "fileOwners: " + fileOwners.toString()); //NoI18n
        }

        synchronized (allUsers) {
            Debug.log(this, "allUsers: " + allUsers.toString()); //NoI18n
        }

        synchronized (skipSendFileMap) {
            Debug.log(this, "skipSendFileMap: " + skipSendFileMap.toString()); //NoI18n
        }

        synchronized (skipSendDeleteFileMap) {
            Debug.log(this, "skipSendDeleteFileMap: " + //NoI18n	
                skipSendDeleteFileMap.keySet().toString()
            );
        }

        synchronized (timertaskMap) {
            Debug.log(this, "timertaskMap: " + timertaskMap.toString()); //NoI18n
        }

        synchronized (userStyles) {
            Debug.log(this, "userStyles: " + userStyles.toString()); //NoI18n	
        }

        Debug.log(
            this,
            "=================================Done============================" + //NoI18n
            "==========================\n\n"
        ); //NoI18n	
    }

    /**
     *
     * @param fileName
     * @param listener
     * @throws CollabException
     */
    public void addCollabDocumentListener(String fileName, CollabDocumentListener listener)
    throws CollabException {
        synchronized (collabDocumentListenerMap) {
            if (collabDocumentListenerMap.containsKey(fileName)) {
                collabDocumentListenerMap.remove(fileName);
            }

            collabDocumentListenerMap.put(fileName, listener);
        }
    }

    /**
     *
     * @param fileName
     * @throws CollabException
     * @return
     */
    public CollabDocumentListener getCollabDocumentListener(String fileName)
    throws CollabException {
        synchronized (collabDocumentListenerMap) {
            return (CollabDocumentListener) collabDocumentListenerMap.get(fileName);
        }
    }

    /**
     * removeDocumentListener
     *
     * @param fileName
     * @throws CollabException
     */
    public void removeDocumentListener(String fileName)
    throws CollabException {
        synchronized (collabDocumentListenerMap) {
            if (collabDocumentListenerMap.containsKey(fileName)) {
                collabDocumentListenerMap.remove(fileName);
            }
        }
    }

    /**
     *
     * @return
     */
    protected int getCurrentState() {
        return currentState;
    }

    /**
     *
     * @param currentState
     */
    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    /**
     *
     * @param currentState
     */
    public void setSkipSendFile(String fileName, boolean skipSendFile) {
        Debug.log(
            this, //NoI18n
            "FilesharingContext, setSkipSendFile: " + //NoI18n
            skipSendFile + " for file: " + fileName
        ); //NoI18n

        synchronized (skipSendFileMap) {
            if (skipSendFile) {
                skipSendFileMap.remove(fileName);
                skipSendFileMap.put(fileName, new Boolean(skipSendFile));
            } else {
                skipSendFileMap.remove(fileName);
            }
        }
    }

    /**
     *
     * @param currentState
     */
    public void setSkipSendDeleteFile(String fileName, boolean skipSendDeleteFile) {
        Debug.log(
            this, //NoI18n
            "FilesharingContext, setSkipSendDeleteFile: " + //NoI18n
            skipSendDeleteFile + " for file: " + fileName
        ); //NoI18n		

        synchronized (skipSendDeleteFileMap) {
            //remove previous entry
            if (skipSendDeleteFileMap.containsKey(fileName)) {
                skipSendDeleteFileMap.remove(fileName);
            }

            if (skipSendDeleteFile) {
                skipSendDeleteFileMap.put(fileName, new Boolean(skipSendDeleteFile));
            }
        }
    }

    /**
     * addTimerTask
     *
     */
    public void addTimerTask(String taskName, Object task) {
        addTimerTask(taskName, task.toString(), task);
    }

    /**
     * addTimerTask
     *
     */
    public boolean addTimerTask(String taskName, String subTaskName, Object task) {
        synchronized (timertaskMap) {
            HashMap tasksMap = null;

            if (!timertaskMap.containsKey(taskName)) {
                tasksMap = new HashMap();
            } else {
                tasksMap = (HashMap) timertaskMap.get(taskName);
            }

            if (tasksMap == null) {
                return false;
            }

            if ((subTaskName != null) && !subTaskName.trim().equals("")) {
                tasksMap.put(subTaskName, task);
            } else {
                tasksMap.put(task.toString(), task);
            }

            timertaskMap.put(taskName, tasksMap);
        }

        return true;
    }

    /**
     * getTimerTask
     *
     */
    public HashMap getTimerTask(String key) {
        synchronized (timertaskMap) {
            if (timertaskMap.containsKey(key)) {
                HashMap timerTasks = (HashMap) timertaskMap.get(key);

                return timerTasks;
            } else {
                return null;
            }
        }
    }

    /**
     * getTimerTask
     *
     */
    public TimerTask getTimerTask(String key, String subKey) {
        synchronized (timertaskMap) {
            if (timertaskMap.containsKey(key)) {
                HashMap timerTasks = (HashMap) timertaskMap.get(key);

                if ((subKey != null) && !subKey.trim().equals("")) {
                    return (TimerTask) timerTasks.get(subKey);
                }
            }
        }

        return null;
    }

    /**
     * cancelAllTimerTask
     *
     */
    public void cancelAllTimerTask() {
        Debug.log(this, "FilesharingContext, cancel all timer tasks ");

        String[] taskNames = null;

        synchronized (timertaskMap) {
            taskNames = (String[]) timertaskMap.keySet().toArray(new String[0]);
        }

        if (taskNames != null) {
            for (int i = 0; i < taskNames.length; i++) {
                cancelTimerTask(taskNames[i]);
            }
        }

        synchronized (timertaskMap) {
            timertaskMap.clear();
        }
    }

    /**
     * cancelTimerTask
     *
     */
    public void cancelTimerTask(String key) {
        cancelTimerTask(key, null);
    }

    /**
     * cancelTimerTask
     *
     */
    public void cancelTimerTask(String key, String subKey) {
        synchronized (timertaskMap) {
            if (timertaskMap.containsKey(key)) {
                HashMap timerTasks = (HashMap) timertaskMap.get(key);

                if (subKey != null) {
                    TimerTask timerTask = (TimerTask) timerTasks.get(subKey);

                    if (timerTask != null) {
                        timerTask.cancel();
                        timerTask = null;
                        timerTasks.remove(subKey);
                    }
                } else {
                    Debug.log(
                        this,
                        "FilesharingContext, current " + //NoI18n
                        "timerTasks length for taskName: " + key + " before cancel: " + //NoI18n
                        timerTasks.size()
                    );

                    Iterator it = timerTasks.keySet().iterator();

                    while (it.hasNext()) {
                        String taskName = (String) it.next();
                        Debug.log(this, "FilesharingContext, cancel " + "taskName:" + taskName);

                        TimerTask timerTask = (TimerTask) timerTasks.get(taskName);

                        if (
                            ((subKey == null) && (timerTask != null)) ||
                                ((subKey != null) && (timerTask != null) && subKey.trim().equals(taskName))
                        ) {
                            timerTask.cancel();
                            timerTask = null;
                        }
                    }

                    timertaskMap.remove(key);
                }
            }
        }
    }

    /**
     * cancelSendResumeMessageTimerTask
     *
     */
    public void cancelSendResumeMessageTimerTask() {
        cancelTimerTask(SEND_RESUME_TIMER_TASK);
    }

    /**
     * getSendFileMessageTimerTask
     *
     * @param        fileGroupName
     */
    public SendFileTimerTask getSendFileMessageTimerTask(String fileGroupName) {
        return (SendFileTimerTask) getTimerTask(SEND_SENDFILE_TIMER_TASK, fileGroupName);
    }

    /**
     * cancelSendFileMessageTimerTask
     *
     * @param        fileGroupName
     */
    public void cancelSendFileMessageTimerTask(String fileGroupName) {
        cancelTimerTask(SEND_SENDFILE_TIMER_TASK, fileGroupName);
    }

    /**
     * cancelSendFileMessageTimerTask
     *
     */
    public void cancelSendFileMessageTimerTask() {
        cancelTimerTask(SEND_SENDFILE_TIMER_TASK);
    }

    /**
     * cancelProjectActionListTimerTask
     *
     * @param        projectName
     */
    public void cancelProjectActionListTimerTask(String projectName) {
        cancelTimerTask(SEND_PROJECTACTIONLIST_TIMER_TASK, projectName);
    }

    /**
     * cancelProjectActionListTimerTask
     *
     */
    public void cancelProjectActionListTimerTask() {
        cancelTimerTask(SEND_PROJECTACTIONLIST_TIMER_TASK);
    }

    /**
     * cancelSendPauseMessageTimerTask
     *
     */
    public void cancelSendPauseMessageTimerTask() {
        cancelTimerTask(SEND_PAUSE_TIMER_TASK);
    }

    /**
     * cancelSendJoinBeginMessageTimerTask
     *
     */
    public void cancelSendJoinBeginMessageTimerTask() {
        cancelTimerTask(SEND_JOINBEGIN_TIMER_TASK);
    }

    /**
     * cancelSendJoinEndMessageTimerTask
     *
     */
    public void cancelSendJoinEndMessageTimerTask() {
        cancelTimerTask(SEND_JOINEND_TIMER_TASK);
    }

    /**
     * startSendFileMessageTimerTask
     *
     * @param        sendFileMap
     * @param        delay
     */
    public SendFileTimerTask[] startSendFileMessageTimerTask(HashMap sendFileMap, long delay) {
        String[] fileGroupNames = null;

        synchronized (sendFileMap) {
            fileGroupNames = (String[]) sendFileMap.keySet().toArray(new String[0]);
        }

        if ((fileGroupNames == null) || (fileGroupNames.length == 0)) {
            return null;
        }

        List sendFileMessageTimerTaskList = new ArrayList();

        for (int i = 0; i < fileGroupNames.length; i++) {
            String fileGroupName = fileGroupNames[i];
            SharedFileGroup sharedFileGroup = sharedFileGroupManager.getSharedFileGroup(fileGroupName);

            if (sharedFileGroup != null) {
                EventNotifier fileChangeEventNotifier = getFileChangeEventNotifier();
                CollabEvent sendFileEvent = new SendFileEvent(
                        new EventContext(SendFileEvent.getEventID(), sharedFileGroup)
                    );
                SendFileTimerTask sendFileMessageTimerTask = new SendFileTimerTask(
                        fileChangeEventNotifier, sendFileEvent, this
                    );
                addTimerTask(SEND_SENDFILE_TIMER_TASK, fileGroupName, sendFileMessageTimerTask);
                Debug.log(this, "FilesharingContext, scheduling to SendFileMessage"); //NoI18n
                sendFileMessageTimerTask.schedule(delay);

                synchronized (timertaskMap) {
                    Debug.log(
                        this,
                        "FilesharingContext, timertaskMap " + //NoI18n
                        "length after scheduling: " + timertaskMap.size()
                    ); //NoI18n
                }

                sendFileMessageTimerTaskList.add(sendFileMessageTimerTask);
            }
        }

        return (SendFileTimerTask[]) sendFileMessageTimerTaskList.toArray(new SendFileTimerTask[0]);
    }

    /**
     * startSendFileMessageTimerTask
     *
     * @param        fileGroupName
     * @param        delay
     */
    public SendFileTimerTask startSendFileMessageTimerTask(String fileGroupName, long delay) {
        Debug.log(this, "FilesharingContext, scheduling to SendFileMessage: " + fileGroupName + "after : " + delay); //NoI18n

        SharedFileGroup sharedFileGroup = sharedFileGroupManager.getSharedFileGroup(fileGroupName);

        if (sharedFileGroup != null) {
            EventNotifier fileChangeEventNotifier = getFileChangeEventNotifier();
            CollabEvent sendFileEvent = new SendFileEvent(
                    new EventContext(SendFileEvent.getEventID(), sharedFileGroup)
                );
            SendFileTimerTask sendFileMessageTimerTask = new SendFileTimerTask(
                    fileChangeEventNotifier, sendFileEvent, this
                );
            addTimerTask(SEND_SENDFILE_TIMER_TASK, fileGroupName, sendFileMessageTimerTask);
            Debug.log(this, "FilesharingContext, scheduling to SendFileMessage"); //NoI18n
            sendFileMessageTimerTask.schedule(delay);

            synchronized (timertaskMap) {
                Debug.log(
                    this,
                    "FilesharingContext, timertaskMap " + //NoI18n
                    "length after scheduling: " + timertaskMap.size()
                ); //NoI18n
            }

            return sendFileMessageTimerTask;
        }

        return null;
    }

    /**
     * startSendFileMessageTimerTask
     *
     * @param        ownerFilesOnly
     * @param        delay
     */
    public void startSendFileMessageTimerTask(boolean ownerFilesOnly, long delay) {
        SharedFileGroup[] sharedFileGroups = null;

        if (ownerFilesOnly) {
            sharedFileGroups = sharedFileGroupManager.getOwnerSharedFileGroup();
        } else {
            sharedFileGroups = sharedFileGroupManager.getAllSharedFileGroup();
        }

        if ((sharedFileGroups != null) && (sharedFileGroups.length > 0)) {
            //sendFileMessageTimerTasks = new ArrayList();
            for (int i = 0; i < sharedFileGroups.length; i++) {
                String fileGroupName = sharedFileGroups[i].getName();
                SendFileTimerTask sendFileTimerTask = startSendFileMessageTimerTask(fileGroupName, delay);

                if (sendFileTimerTask == null) {
                    continue;
                }
            }
        }

        //schedule to send Project actions
        if (ownerFilesOnly) {
            SharedProject[] sharedProjects = getSharedProjectManager().getOwnerSharedProjects(getLoginUser());

            for (int i = 0; i < sharedProjects.length; i++) {
                SharedProject sharedProject = sharedProjects[i];

                //Send Action List
                EventContext evContext = new ProjectContext(
                        ProjectActionListEvent.getEventID(), sharedProject.getProjectOwner(), sharedProject.getName(),
                        sharedProject.getProjectActions()
                    );
                final CollabEvent ce = new ProjectActionListEvent(evContext);

                /* send projectActionList message after a delay */
                ProjectActionListTimerTask sendProjectActionListTimerTask = new ProjectActionListTimerTask(
                        getChannelEventNotifier(), new ProjectActionListEvent(evContext), this
                    );
                addTimerTask(
                    SEND_PROJECTACTIONLIST_TIMER_TASK, sharedProject.getName(), sendProjectActionListTimerTask
                );
                sendProjectActionListTimerTask.schedule(FilesharingTimerTask.PERIOD * 3);
            }
        }
    }

    /**
     * setJoinUser
     *
     * @param        joinUser
     */
    public void setJoinUser(String joinUser) {
        this.saveJoinUser = joinUser;
    }

    /**
     * getJoinUser
     *
     * @return        saveJoinUser
     */
    public String getJoinUser() {
        return this.saveJoinUser;
    }

    /**
     * isJoinState
     *
     * @return        setJoinFlag
     */
    public boolean isJoinState() {
        return this.setJoinFlag;
    }

    /**
     * setJoinFlag
     *
     * @param        flag
     */
    public void setJoinFlag(boolean flag) {
        Debug.log(this, "FilesharingContext, setting isJoinState: " + //NoI18n
            flag + " for user: " + getLoginUser()
        ); //NoI18n
        this.setJoinFlag = flag;
    }

    /**
     * getSendPauseMessageTimerTask
     *
     */
    public PauseTimerTask[] getSendPauseMessageTimerTask() {
        return (PauseTimerTask[]) getTimerTask(SEND_PAUSE_TIMER_TASK).values().toArray(new PauseTimerTask[0]);
    }

    /**
     * clearExpectedFileMap
     *
     */
    public void clearExpectedFileMap() {
        synchronized (saveExpectedFileMap) {
            saveExpectedFileMap.clear();
        }
    }

    /**
     * getSaveExpectedFiles
     *
     * @return        saveExpectedFileMap
     */
    public HashMap getSaveExpectedFiles() {
        synchronized (saveExpectedFileMap) {
            return this.saveExpectedFileMap;
        }
    }

    /**
     * containsSaveExpectedFiles
     *
     * @param        fileGroupName
     * @return        flag
     */
    public boolean containsSaveExpectedFiles(String fileGroupName) {
        synchronized (saveExpectedFileMap) {
            return saveExpectedFileMap.containsKey(fileGroupName);
        }
    }

    /**
     * onJoinBegin
     *
     * @throws CollabException
     */
    public void onJoinBegin() throws CollabException {
        //Pause all operation including edit (only exception is fileOwner sendFile)
        disableExplorerPanel();

        SharedFileGroup[] allSharedFileGroups = sharedFileGroupManager.getAllSharedFileGroup();

        if (allSharedFileGroups != null) {
            for (int i = 0; i < allSharedFileGroups.length; i++) {
                CollabFileHandler[] fileHandlers = allSharedFileGroups[i].getFileHandlers();

                for (int j = 0; j < fileHandlers.length; j++) {
                    CollabFileHandler collabFileHandler = fileHandlers[j];

                    if (collabFileHandler != null) {
                        collabFileHandler.handlePause();
                    }
                }
            }
        }
    }

    /**
     * Pause all operation
     *
     * @throws CollabException
     */
    public void pauseAll() throws CollabException {
        pauseAll(false);
    }

    /**
     * Pause all operation, skip cancel sendjoin if specified
     *
     * param        noCancelSendJoinEnd
     * @throws CollabException
     */
    public void pauseAll(boolean noCancelSendJoinEnd) throws CollabException {
        inPauseState = true;

        //remove any pause timer task
        cancelSendPauseMessageTimerTask();

        //remove any resume timer task
        cancelSendResumeMessageTimerTask();

        //remove any join begin timer task
        cancelSendJoinBeginMessageTimerTask();

        //remove any join end timer task
        if (!noCancelSendJoinEnd) {
            cancelSendJoinEndMessageTimerTask();
        }

        //remove any sendFile timer task
        cancelSendFileMessageTimerTask();

        //Pause all operation including edit (only exception is fileOwner sendFile)
        disableExplorerPanel();

        SharedFileGroup[] allSharedFileGroups = sharedFileGroupManager.getAllSharedFileGroup();

        if (allSharedFileGroups != null) {
            for (int i = 0; i < allSharedFileGroups.length; i++) {
                CollabFileHandler[] fileHandlers = allSharedFileGroups[i].getFileHandlers();

                for (int j = 0; j < fileHandlers.length; j++) {
                    CollabFileHandler collabFileHandler = fileHandlers[j];

                    if (collabFileHandler != null) {
                        collabFileHandler.handlePause();
                    }
                }
            }
        }
    }

    /**
     * Resume all operation
     *
     * @throws CollabException
     */
    public void resumeAll() throws CollabException {
        inPauseState = false;

        //remove any pause timer task
        cancelSendPauseMessageTimerTask();

        //remove any resume timer task
        cancelSendResumeMessageTimerTask();

        //remove any join begin timer task
        cancelSendJoinBeginMessageTimerTask();

        //remove any join end timer task
        cancelSendJoinEndMessageTimerTask();

        //remove any sendFile timer task
        cancelSendFileMessageTimerTask();

        //Resume all edit operation
        SharedFileGroup[] sharedFileGroups = sharedFileGroupManager.getAllSharedFileGroup();

        if (sharedFileGroups != null) {
            for (int i = 0; i < sharedFileGroups.length; i++) {
                CollabFileHandler[] fileHandlers = sharedFileGroups[i].getFileHandlers();

                for (int j = 0; j < fileHandlers.length; j++) {
                    CollabFileHandler collabFileHandler = fileHandlers[j];

                    if (collabFileHandler != null) {
                        collabFileHandler.handleResume();
                    }
                }
            }
        }

        enableExplorerPanel();
    }

    protected void initAnnotationTypes() {
        RegionAnnotation1 annotation1 = new RegionAnnotation1();
        RegionAnnotation2 annotation2 = new RegionAnnotation2();
        RegionAnnotation3 annotation3 = new RegionAnnotation3();
        RegionAnnotation4 annotation4 = new RegionAnnotation4();
        RegionAnnotation5 annotation5 = new RegionAnnotation5();
        RegionAnnotation6 annotation6 = new RegionAnnotation6();
        RegionAnnotation7 annotation7 = new RegionAnnotation7();
        RegionAnnotation8 annotation8 = new RegionAnnotation8();
        RegionAnnotation9 annotation9 = new RegionAnnotation9();
        annotationTypes = new CollabRegionAnnotation[] {
                annotation1, annotation2, annotation3, annotation4, annotation5, annotation6, annotation7, annotation8,
                annotation9
            };
    }

    /**
     * allocate an annotation style to a user
     *
     */
    public CollabRegionAnnotation allocateAnnotation(String user) {
        CollabRegionAnnotation style = null;

        if (lastAnnotationIndex < annotationTypes.length) {
            style = annotationTypes[lastAnnotationIndex++];
        } else {
            style = annotationTypes[annotationTypes.length - 1];
        }

        getAnnotationStyles().put(user, style);

        return style;
    }

    public Map getAnnotationStyles() {
        return annotationStyles;
    }

    /**
     * get the annotation style allocated to a user
     *
     */
    public CollabRegionAnnotation getUserAnnotationStyle(String user) {
        if (getAnnotationStyles().containsKey(user)) {
            return (CollabRegionAnnotation) getAnnotationStyles().get(user);
        } else {
            return allocateAnnotation(user);
        }
    }

    /**
     *
     *
     */
    public CollabPrincipal getPrincipal(String id) {
        CollabPrincipal principal = null;

        int index = getConversation().getCollabSession().getUserPrincipal().getIdentifier().indexOf("@");
        String domain = "";
        if (index != -1) {
            domain = getConversation().getCollabSession().getUserPrincipal().getIdentifier().substring(index);
        }

        try {
            principal = getConversation().getCollabSession().getPrincipal(id + domain);
        } catch (CollabException ce) {
            Debug.errorManager.notify(ce);
        }

        return principal;
    }

    /*
     * enableExplorerPanel
     *
     */
    public void enableExplorerPanel() {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    if (explorerPanel != null) {
                        Debug.log(this, "FilesharingContext, enabling ExplorerPanel");
                        explorerPanel.enablePanel();
                    }
                }
            }
        );
    }

    /*
     * disableExplorerPanel
     *
     */
    public void disableExplorerPanel() {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    if (explorerPanel != null) {
                        Debug.log(this, "FilesharingContext, disabling ExplorerPanel");
                        explorerPanel.disablePanel();
                    }
                }
            }
        );
    }

    /*
     * createProjectNode
     *
     * @return projectNode
     */
    public Node createProjectNode(String name, String projectName)
    throws IOException {
        //getSharedProjectManager().
        //	createCollabProject(name, projectName);		
        if (explorerPanel != null) {
            return explorerPanel.createProjectNode(name, projectName);
        }

        return null;
    }

    /**
     * getUserStyle
     *
     * @return
     */
    public Integer getUserStyle(String user) {
        synchronized (userStyles) {
            Integer style = (Integer) userStyles.get(user);

            if (style == null) {
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, getUserStyle for user" + user); //NoI18n

                int count = 0;

                for (int i = 0; i < user.length(); i++) {
                    count += user.charAt(i);
                    Debug.log(
                        "CollabFileHandlerSupport",
                        "CollabFileHandlerSupport, char: " + //NoI18n
                        user.charAt(i) + "total char count: " + count
                    ); //NoI18n		
                }

                while (count > 10) {
                    count /= 10;
                }

                if (stylesArray[count] == 0) {
                    Debug.log(
                        "CollabFileHandlerSupport",
                        "CollabFileHandlerSupport, unique count found " + //NoI18n
                        "in first attempt, unique count: " + count
                    ); //NoI18n					
                    stylesArray[count] = 1;
                } else {
                    for (int i = 0; i < stylesArray.length; i++) {
                        if (stylesArray[i] == 0) {
                            Debug.log(
                                "CollabFileHandlerSupport",
                                "CollabFileHandlerSupport, unique " + //NoI18n
                                "count found in " + i + " attempt, unique count: " + count
                            ); //NoI18n
                            stylesArray[i] = 1;
                            count = i;

                            break;
                        }
                    }
                }

                style = new Integer(count);
                userStyles.put(user, style);
            }

            Debug.log(
                "CollabFileHandlerSupport",
                "CollabFileHandlerSupport, userStyle for user: " + user + //NoI18n
                " is: " + style.intValue()
            ); //NoI18n

            return style;
        }
    }

    /**
     * getJoinTimeStamp
     *
     * @return
     */
    public long getJoinTimeStamp() {
        Debug.log(this, "FilesharingContext, joinTimeStamp before" + joinTimeStamp);

        if (joinTimeStamp == -1) {
            //timestamp when the user first send/received file
            joinTimeStamp = System.currentTimeMillis();
        }

        Debug.log(this, "FilesharingContext, joinTimeStamp after" + joinTimeStamp);

        return joinTimeStamp;
    }

    /**
     * isReadOnlyConversation
     *
     * @return        flag
     */
    public boolean isReadOnlyConversation() {
        return this.readOnlyConversation;
    }

    /**
     * isReadOnlyConversation
     *
     * @return        flag
     */
    private void setIsReadOnlyConversation(boolean readOnlyConversation) {
        this.readOnlyConversation = readOnlyConversation;
    }

    /**
     * setIsReadOnlyConversation
     *
     * @return        flag
     */
    public void findIsReadOnlyConversation(Conversation conversation) {
        String conversationName = conversation.getIdentifier();

        if (conversation == null) {
            return;
        }

        String userID = conversation.getCollabSession().getUserPrincipal().getIdentifier();

        Debug.log(
            this,
            "FilesharingContext, isReadOnlyConversation: conversationName: " + conversationName + "userID: " + userID
        ); //NoI18n

        try {
            if (
                (conversation.getPrivilege() == ConversationPrivilege.MANAGE) ||
                    (conversation.getPrivilege() == ConversationPrivilege.WRITE)
            ) {
                setIsReadOnlyConversation(false);

                return;
            }

            setIsReadOnlyConversation(true);
        } catch (CollabException e) {
            setIsReadOnlyConversation(true);
        }
    }

    /**
     * closeFile
     *
     */
    public boolean closeFile(String fileName) {
        Debug.log("FilesharingContext", //NoI18n
            "closeFile: " + fileName
        ); //NoI18n	

        try {
            CollabFileHandler collabFileHandler = sharedFileGroupManager.getFileHandler(fileName);

            if (collabFileHandler != null) {
                EditorCookie cookie = ((CollabFileHandlerSupport) collabFileHandler).getEditorCookie();

                if (cookie != null) {
                    return cookie.close();
                }
            }
        } catch (Throwable th) {
            Debug.log("FilesharingContext", "FilesharingContext, closeFile() failed"); //NoI18n
            Debug.logDebugException("CollabFilesystem, closeFile() failed", th, true); //NoI18n
        }

        return false;
    }

    /**
     * doSyncOperation
     *
     * @param context
     */
    public void doSyncOperation() {
        doSyncOperation(true);
    }

    /**
     * doSyncOperation
     *
     * @param context
     */
    public void doSyncOperation(boolean sendInitialJoinEnd) {
        Debug.log(this, "SyncAction, sending sync"); //NoI18n				

        //send to all
        setJoinUser("");

        long pauseDelay = FilesharingTimerTask.PAUSE_DELAY;

        if (sendInitialJoinEnd) {
            JoinEndTimerTask sendJoinMessageTimerTask = new JoinEndTimerTask(
                    getChannelEventNotifier(),
                    new JoinFilesharingEnd(new EventContext(JoinFilesharingEnd.getEventID(), null)), this
                );
            addTimerTask(SEND_JOINEND_TIMER_TASK, sendJoinMessageTimerTask);
            sendJoinMessageTimerTask.schedule(FilesharingTimerTask.INTER_DELAY);

            try {
                Thread.sleep(FilesharingTimerTask.PERIOD * 5);
            } catch (java.lang.Throwable th) {
                //ignore
            }
        } else {
            pauseDelay = FilesharingTimerTask.INTER_DELAY;
        }

        PauseTimerTask sendPauseMessageTimerTask = new PauseTimerTask(
                getChannelEventNotifier(),
                new PauseFilesharingEvent(new EventContext(PauseFilesharingEvent.getEventID(), null)), this
            );
        addTimerTask(SEND_PAUSE_TIMER_TASK, sendPauseMessageTimerTask);
        sendPauseMessageTimerTask.schedule(pauseDelay);

        //sendMessage join-end
        long delay = FilesharingTimerTask.PERIOD;
        SharedFileGroup[] allSharedFileGroups = sharedFileGroupManager.getAllSharedFileGroup();

        if ((allSharedFileGroups != null) && (allSharedFileGroups.length > 0)) {
            int totalFileSize = 0;

            for (int i = 0; i < allSharedFileGroups.length; i++) {
                CollabFileHandler[] fileHandlers = allSharedFileGroups[i].getFileHandlers();

                for (int j = 0; j < fileHandlers.length; j++) {
                    CollabFileHandler collabFileHandler = fileHandlers[j];

                    if (collabFileHandler != null) {
                        Debug.log(this, "SyncAction, fileName: " + collabFileHandler.getName()); //NoI18n

                        try {
                            Debug.log(this, "SyncAction, fileSize: " + collabFileHandler.getFileSize()); //NoI18n
                            totalFileSize += collabFileHandler.getFileSize();
                        } catch (CollabException ce) {
                            //ignore
                            continue;
                        }
                    }
                }

                Debug.log(this, "SyncAction, totalFileSize: " + totalFileSize); //NoI18n						
                delay = CollabQueue.calculateDelay(totalFileSize);
                Debug.log(this, "SyncAction, calculated delay: " + delay); //NoI18n

                if (delay < FilesharingTimerTask.PERIOD) {
                    delay = FilesharingTimerTask.PERIOD;
                }
            }
        }

        delay += FilesharingTimerTask.PAUSE_DELAY;

        if (delay < (FilesharingTimerTask.PAUSE_DELAY * 2)) {
            delay = FilesharingTimerTask.PAUSE_DELAY * 2;
        }

        Debug.log(this, "SyncAction, JoinEndTimerTask scheduled after " + "millis: " + delay); //NoI18n				

        JoinEndTimerTask sendJoinMessageTimerTask1 = new JoinEndTimerTask(
                getChannelEventNotifier(),
                new JoinFilesharingEnd(new EventContext(JoinFilesharingEnd.getEventID(), null)), this
            );
        addTimerTask(SEND_JOINEND_TIMER_TASK, sendJoinMessageTimerTask1);
        sendJoinMessageTimerTask1.schedule(delay);
    }

    public void setSharedCommonFileListener(FileSystem scfs, FileChangeListener scfl) {
        this.scfs = scfs;
        this.scfl = scfl;
    }
}
