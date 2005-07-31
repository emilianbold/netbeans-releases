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
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import com.sun.collablet.CollabException;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.beans.*;

import java.io.*;

import java.net.*;

import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.FilesharingEventNotifierFactory;
import org.netbeans.modules.collab.channel.filesharing.FilesharingEventProcessorFactory;
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
import org.netbeans.modules.collab.channel.filesharing.annotations.RegionHistoryAnnotation;
import org.netbeans.modules.collab.channel.filesharing.context.LockRegionContext;
import org.netbeans.modules.collab.channel.filesharing.context.UnlockRegionContext;
import org.netbeans.modules.collab.channel.filesharing.event.LockRegionEvent;
import org.netbeans.modules.collab.channel.filesharing.event.UnlockRegionEvent;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.DocumentTabMarker;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.EditorComponentFocusListener;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingDocumentListener;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.SendFileTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.SwingThreadTask;
import org.netbeans.modules.collab.channel.filesharing.filesystem.CollabFilesystem;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventProcessor;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabDocumentListener;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.Base64;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.StreamCopier;
import org.netbeans.modules.collab.channel.filesharing.msgbean.Content;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileChanged;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileChangedData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LineRegion;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LineRegionFunction;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.RegionChanged;
import org.netbeans.modules.collab.channel.filesharing.msgbean.SendFile;
import org.netbeans.modules.collab.channel.filesharing.msgbean.SendFileData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.TextChange;
import org.netbeans.modules.collab.channel.filesharing.msgbean.TextRegion;
import org.netbeans.modules.collab.channel.filesharing.msgbean.TextRegionChanged;
import org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegion;
import org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegionData;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.core.Debug;


/**
 * Support class for all class implements CollabFileHandler
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public abstract class CollabFileHandlerSupport extends Object implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* holds CollabContext */
    protected FilesharingContext context = null;

    /* file Name */
    protected String fileName = null;

    /* sharedFileGroup */
    protected SharedFileGroup sharedFileGroup = null;

    /* fileObject */
    protected FileObject fileObject = null;

    /* contentType */
    protected String contentType = null;

    /* document listener */
    protected CollabDocumentListener documentListener = null;

    /* count to make regionName unique */
    protected int regionCount = 0;

    /* store filechange status */
    protected boolean fileChanged = false;

    /* current region that changed */
    protected CollabRegion currentUpdatedRegion = null;

    /* disable insertUpdate callback */
    protected boolean inReceiveSendFile = false;

    /* disable while inPauseState, and enable if received resume*/
    protected boolean inPauseState = false;

    /* disable insert/remove if set to true */
    protected boolean skipUpdate = true;

    /* skipUpdateAlways */
    protected boolean skipUpdateAlways = false;

    /* previousDocLength for adding a doc listener */
    protected int previousDocLength = -1;

    /* isValid, if false then this handler is invalid */
    protected boolean isValid = true;

    /* getReadyToSendFile */
    protected boolean getReadyToSendFile = false;

    /*disableReceiveSendFile*/
    protected boolean disableReceiveSendFile = false;

    /* filehandler state */
    protected int currentState = FilesharingContext.STATE_UNKNOWN;

    /* sendFile TimerTask */
    protected SendFileTimerTask sendFileTimerTask = null;

    /* scheduledSendFileGroupStatus */
    protected boolean scheduledSendFileGroupStatus = false;

    /* firstTimeSend */
    protected boolean firstTimeSend = true;

    /* lineRegionKey */
    protected String lineRegionKey = null;

    /* newLineCount */
    protected int newLineCount = 0;

    /* documentLock */
    protected String documentLock = "documentLock";

    /* editor cookie */
    protected EditorCookie editorCookie = null;

    /* done Reset Document */
    protected boolean doneResetDoc = false;

    /* inReceiveMessageUnlock */
    protected boolean inReceiveMessageUnlock = false;

    /* disableUnlockTimer */
    protected boolean disableUnlockTimer = false;

    /* editorObservableCookie */
    protected EditorCookie.Observable editorObservableCookie = null;

    /* cookieListener */
    protected CollabEditorCookieListener cookieListener = null;

    /* pauseEditorLock */
    protected EditorLock pauseEditorLock = null;

    /* userStyles */
    protected HashMap userStyles = new HashMap();
    protected byte[] fileContent = null;
    protected FileLock fileLock = null;
    protected OutputStream fileOutputStream = null;
    protected long joinTimeStamp = -1;
    protected boolean isSendFileContentOnly = false;
    protected HashMap focusListenerMap = new HashMap();
    protected boolean undoEditLock = false;
    protected RegionQueue queue = null;
    private CollabRegionContext rCtx = null;

    /**
     *
     *
     */
    public CollabFileHandlerSupport() {
        super();

        //init queue
        queue = new RegionQueue((CollabFileHandler) this);

        //init region context
        rCtx = new CollabRegionContext();
    }

    ////////////////////////////////////////////////////////////////////////////
    // File handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * handles send-file message
     *
     * @param   messageOriginator   the sender of this message
    * @param   sendFileData                the send-file-data Node inside the message
     * @throws CollabException
     */
    public void handleSendFile(final String messageOriginator, final SendFileData sendFileData)
    throws CollabException {
        //reset firstTimeSend
        //firstTimeSend=false;
        //copy contents from message to files; add files to CollabFileSystem
        EditorLock editorLock = null;

        try {
            String fullPath = sendFileData.getFileData().getFileName();
            String tmpContentType = sendFileData.getFileData().getContentType();

            if ((tmpContentType != null) && !tmpContentType.trim().equals("")) {
                setContentType(tmpContentType);
            }

            Content sendFileContent = sendFileData.getContent();
            byte[] fileContent = decodeBase64(sendFileContent.getData());

            inReceiveSendFile = true;

            //remove all annotations
            removeAllLineRegionAnnotation();

            //remove all regions if already exist
            rCtx.removeAllRegion();

            FileObject file = getFileObject(); //do not create 

            if (file == null)//if(firstTimeSend)		
             {
                firstTimeSend = false;
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, creating FileObject"); //NoI18n				

                //create or update if exist
                createFileObject(fileContent);

                //find initial guarded sections once
                findInitialGuardedSections();
            } else {
                String content = new String(fileContent);

                //editorLock = lockEditor(getEditorCookie());				
                updateDocument(content);
            }
        } catch (IllegalArgumentException iargs) {
            inReceiveSendFile = false;

            //unlockEditor(editorLock);			
            throw new CollabException(iargs);
        }

        //init listeners
        getEditorCookie();

        //set line region user
        setLineRegionKey(getContext().getLoginUser() + "_LINEREGION_USER");

        List newLineRegionList = new ArrayList();
        RegionInfo[] lineRegions = findLineRegion(sendFileData);

        for (int i = 0; i < lineRegions.length; i++) {
            RegionInfo region = lineRegions[i];
            String regionName = region.getID();
            int beginOffset = region.getbegin();
            int endOffset = region.getend();

            CollabRegion lineRegion = rCtx.createLineRegion(regionName, beginOffset, endOffset);

            if (lineRegion == null) {
                continue;
            }

            rCtx.addLineRegion(regionName, lineRegion);

            //add annotation
            lineRegion.removeAnnotation();

            if (regionName.startsWith("RA")) //NoI18n
             {
                String lineLockUser = regionName.substring(regionName.indexOf('[') + 1, regionName.indexOf(']'));
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, persist annotation " + //NoI18n
                    "for user: " + lineLockUser
                ); //NoI18n			

                if ((lineLockUser != null) && !lineLockUser.trim().equals("")) {
                    //int style = getUserStyle(lineLockUser);
                    int style = -1; //history annotation
                    String annotationMessage = NbBundle.getMessage(
                            CollabFileHandlerSupport.class, "MSG_CollabFileHandlerSupport_HistoryAnnotation", // NOI18N
                            getContext().getPrincipal(lineLockUser).getDisplayName()
                        );
                    addAnnotation(lineRegion, style, annotationMessage);
                }
            }
        }

        inReceiveSendFile = false;

        //unlockEditor(editorLock);		
    }

    /**
     * updateDocument
     *
     */
    protected void updateDocument(String content) throws CollabException {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, updateDocument"); //NoI18n		

        try {
            synchronized (getDocumentLock()) {
                getDocument().remove(0, getDocument().getLength());
                getDocument().insertString(0, content, null);
            }
        } catch (javax.swing.text.BadLocationException e) {
            //throw new CollabException(e);
        } finally {
            getContext().setSkipSendFile(getName(), false);
        }
    }

    /**
     * findLineRegion
     *
     */
    private RegionInfo[] findLineRegion(SendFileData sendFileData)
    throws CollabException {
        RegionInfo[] regions = null;
        boolean chooseLineRegionFunction = sendFileData.isChooseLineRegionFunction();

        if (chooseLineRegionFunction) {
            LineRegionFunction lineRegionFuntion = sendFileData.getLineRegionFunction();
            List regionList = new ArrayList();
            StyledDocument fileDocument = getDocument();
            String[] regionNames = createLineRegionNames(lineRegionFuntion);

            for (int i = 0; i < regionNames.length; i++) {
                javax.swing.text.Element currentElement = fileDocument.getDefaultRootElement().getElement(i);
                int beginOffset = currentElement.getStartOffset();
                int endOffset = currentElement.getEndOffset();
                RegionInfo regionInfo = new RegionInfo(
                        regionNames[i], getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE, beginOffset,
                        endOffset, 0
                    );
                regionList.add(regionInfo);
            }

            regions = (RegionInfo[]) regionList.toArray(new RegionInfo[0]);
        } else {
            regions = getSendFileRegion(sendFileData);
        }

        return regions;
    }

    /**
     * createLineRegionNames
     *
     */
    private String[] createLineRegionNames(LineRegionFunction lineRegionFunction) {
        String functionName = lineRegionFunction.getFunctionName();
        List returnList = new ArrayList();

        if ((functionName != null) && functionName.equals("simple_linear_function")) {
            String[] args = lineRegionFunction.getArguments();

            if ((args != null) && (args.length > 0)) {
                String prefix = args[0];
                String user = args[1];
                int count = Integer.parseInt(args[2]);

                for (int i = 0; i < count; i++) {
                    String lineRegionName = prefix + "[" + user + "]" + i; //NoI18n
                    returnList.add(lineRegionName);
                }
            }
        }

        return (String[]) returnList.toArray(new String[0]);
    }

    /**
     * findInitialGuardedSections
     *
     */
    protected void findInitialGuardedSections() throws CollabException {
        return;
    }

    /**
     * findInitialGuardedSections
     *
     */
    protected HashMap getInitialGuardedSections() throws CollabException {
        return null;
    }

    /**
     * handles lock-region message
     *
     * @param   messageOriginator   the sender of this message
    * @param   lockRegionData                the lock-region-data Node inside the message
     * @throws CollabException
     */
    public void handleLock(final String messageOriginator, final LockRegionData lockRegionData)
    throws CollabException {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        createLock(messageOriginator, lockRegionData);
                    } catch (Throwable th) {
                        Debug.log(
                            "CollabFileHandlerSupport",
                            "CollabFileHandlerSupport, " + //NoI18n
                            "cannot create received Lock "
                        ); //NoI18n
                        Debug.logDebugException(
                            "CollabFileHandlerSupport, " + //NoI18n
                            "cannot create received Lock", //NoI18n	
                            th, true
                        );
                    }
                }
            }
        );
    }

    /**
     * handles lock-region message
     *
     * @param   messageOriginator   the sender of this message
    * @param   lockRegionData                the lock-region-data Node inside the message
     * @throws CollabException
     */
    private void createLock(final String messageOriginator, final LockRegionData lockRegionData)
    throws CollabException {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, receivedMessageLock"); //NoI18n	

        //copy contents from message to files; add files to CollabFileSystem
        try {
            String fullPath = lockRegionData.getFileName();
            StyledDocument fileDocument = getDocument();

            int documentLength = fileDocument.getLength();
            RegionInfo region = getLockRegion(lockRegionData);
            String regionName = region.getID();
            int beginOffset = region.getbegin();
            int endOffset = region.getend();
            int length = endOffset - beginOffset;

            if (endOffset > documentLength) {
                endOffset = documentLength;
            }

            if (lockRegionData.getContent() != null) {
                Content content = lockRegionData.getContent();
                String text = new String(decodeBase64(content.getData()));
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, received lock for " + //NoI18n
                    "region with content : [" + text + "]"
                ); //NoI18n			

                LineRegion[] lineRegions = lockRegionData.getLineRegion();
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, # of lineRegions: " + //NoI18n
                    lineRegions.length
                );

                if (lineRegions.length > 0) {
                    //identify lineregion
                    List cLineRegionList = new ArrayList();

                    for (int i = 0; i < lineRegions.length; i++) {
                        LineRegion lineRegion = lineRegions[i];
                        String lineRegionName = lineRegion.getRegionName();
                        cLineRegionList.add(rCtx.getRegion(lineRegionName));
                    }

                    rCtx.resetLineRegion(regionName, cLineRegionList);

                    String firstLineName = lineRegions[0].getRegionName();
                    String lastLineName = lineRegions[lineRegions.length - 1].getRegionName();
                    Debug.log(
                        "CollabFileHandlerSupport", "CollabFileHandlerSupport, firstLineName: " + //NoI18n
                        firstLineName
                    );
                    Debug.log(
                        "CollabFileHandlerSupport", "CollabFileHandlerSupport, lastLineName: " + //NoI18n
                        lastLineName
                    );

                    CollabLineRegion firstLineRegion = (CollabLineRegion) rCtx.getLineRegion(firstLineName);
                    CollabLineRegion lastLineRegion = (CollabLineRegion) rCtx.getLineRegion(lastLineName);

                    if ((firstLineRegion == null) || (lastLineRegion == null)) {
                        return;
                    }

                    synchronized (getDocumentLock()) {
                        EditorLock editorLock = lockEditor(getEditorCookie());
                        beginOffset = firstLineRegion.getBeginOffset();
                        endOffset = lastLineRegion.getEndOffset();
                        Debug.log(
                            "CollabFileHandlerSupport", "CollabFileHandlerSupport, beginOffset: " + //NoI18n
                            beginOffset
                        );
                        Debug.log(
                            "CollabFileHandlerSupport", "CollabFileHandlerSupport, endOffset: " + //NoI18n
                            endOffset
                        );

                        CollabRegion simpleSection = createSimpleSection(beginOffset, endOffset, regionName);

                        if (simpleSection == null) //check with correction
                         { //cannot create section
                            simpleSection = createSimpleSection(beginOffset + 1, endOffset, regionName);

                            if (simpleSection == null) //check with correction
                             { //cannot create section
                                simpleSection = createSimpleSection(beginOffset, endOffset - 1, regionName);

                                if (simpleSection == null) //check with correction
                                 { //cannot create section
                                    simpleSection = createSimpleSection(beginOffset + 1, endOffset - 1, regionName);

                                    if (simpleSection == null) //check with correction
                                     { //cannot create section
                                        simpleSection = createSimpleSection(beginOffset + 1, endOffset - 1, regionName);
                                        unlockEditor(editorLock);

                                        return;
                                    }
                                }
                            }
                        }

                        rCtx.addRegion(messageOriginator, regionName, simpleSection);

                        //add annotation
                        String annotationMessage = NbBundle.getMessage(
                                CollabFileHandlerSupport.class, "MSG_CollabFileHandlerSupport_EditingAnnotation", // NOI18N
                                getContext().getPrincipal(messageOriginator).getDisplayName()
                            );
                        int style = getUserStyle(messageOriginator);
                        addLineRegionAnnotation(
                            (CollabLineRegion[]) cLineRegionList.toArray(new CollabLineRegion[0]), style,
                            annotationMessage
                        );
                        unlockEditor(editorLock);
                    }
                }
            }
        } catch (IllegalArgumentException iargs) {
            throw new CollabException(iargs);
        }
    }

    /**
     * addLineRegionAnnotation
     *
     * @param   lineRegions
     * @param   annotationMessage
     * @param   style
     */
    private void addLineRegionAnnotation(CollabLineRegion[] lineRegions, int style, String annotationMessage)
    throws CollabException {
        for (int i = 0; i < lineRegions.length; i++) {
            CollabLineRegion lineRegion = lineRegions[i];

            if (lineRegion != null) {
                lineRegion.removeAnnotation();
                addAnnotation(lineRegion, style, annotationMessage);
            }
        }
    }

    /**
     * addAnnotation
     *
     * @param   region
     * @param   annotationMessage
     * @param   style
     */
    private void addAnnotation(CollabRegion region, int style, String annotationMessage)
    throws CollabException {
        region.addAnnotation(
            FileshareUtil.getDataObject(getFileObject()), (CollabFileHandler) this, style, annotationMessage
        );
    }

    /**
     * createRegionAnnotation
     *
     * @param   message
    * @return   Annotation
     */
    public Annotation createRegionAnnotation(int style, String annotationMessage) {
        CollabRegionAnnotation regionAnnotation = null;

        switch (style) {
        case -1:
            regionAnnotation = new RegionHistoryAnnotation();

            break;

        case 0:
            regionAnnotation = new RegionAnnotation1();

            break;

        case 1:
            regionAnnotation = new RegionAnnotation2();

            break;

        case 2:
            regionAnnotation = new RegionAnnotation3();

            break;

        case 3:
            regionAnnotation = new RegionAnnotation4();

            break;

        case 4:
            regionAnnotation = new RegionAnnotation5();

            break;

        case 5:
            regionAnnotation = new RegionAnnotation6();

            break;

        case 6:
            regionAnnotation = new RegionAnnotation7();

            break;

        case 7:
            regionAnnotation = new RegionAnnotation8();

            break;

        case 8:
            regionAnnotation = new RegionAnnotation9();

            break;

        default:
            regionAnnotation = new RegionAnnotation1();

            break;
        }

        regionAnnotation.setShortDescription(annotationMessage);

        return regionAnnotation;
    }

    /**
     * handles sendChange message
     *
     * @param   messageOriginator   the sender of this message
    * @param   fileChangedData
     * @throws CollabException
     * @deprecated
     */
    public void handleSendChange(String messageOriginator, FileChangedData fileChangedData)
    throws CollabException {
    }

    /**
     * handles unlock-region message
     *
     * @param   messageOriginator   the sender of this message
    * @param   unlockRegionData        the unlock-region-data Node inside the message
     * @throws CollabException
     */
    public void handleUnlock(final String messageOriginator, final UnlockRegionData unlockRegionData)
    throws CollabException {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        releaseLock(messageOriginator, unlockRegionData);
                    } catch (Throwable th) {
                        Debug.log(
                            "CollabFileHandlerSupport", "CollabFileHandlerSupport, " + //NoI18n
                            "cannot release Lock "
                        ); //NoI18n
                        Debug.logDebugException(
                            "CollabFileHandlerSupport, " + //NoI18n
                            "cannot release Lock", //NoI18n	
                            th, true
                        );
                    }
                }
            }
        );
    }

    /**
     * release Lock
     *
     * @param   messageOriginator   the sender of this message
    * @param   unlockRegionData        the unlock-region-data Node inside the message
     * @throws CollabException
     */
    private void releaseLock(final String messageOriginator, final UnlockRegionData unlockRegionData)
    throws CollabException {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, receivedMessageUnlock"); //NoI18n
        inReceiveMessageUnlock = true;

        EditorLock editorLock = null;

        //copy contents from message to files; add files to CollabFileSystem
        try {
            String fullPath = unlockRegionData.getFileName();
            StyledDocument fileDocument = getDocument();

            int documentLength = fileDocument.getLength();
            RegionInfo region = getUnlockRegion(unlockRegionData);

            String regionName = region.getID();
            int beginOffset = region.getbegin();
            int endOffset = region.getend();
            int length = endOffset - beginOffset;

            CollabRegion sect = rCtx.getRegion(regionName);

            if (sect == null) {
                inReceiveMessageUnlock = false;

                return;
            }

            if (unlockRegionData.getContent() != null) {
                LineRegion[] lineRegions = unlockRegionData.getLineRegion();
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, # of lineRegions: " //NoI18n
                     +lineRegions.length
                );

                List newLineRegionNames = new ArrayList();

                if (lineRegions.length > 0) {
                    for (int i = 0; i < lineRegions.length; i++) {
                        LineRegion lineRegion = lineRegions[i];
                        String lineRegionName = lineRegion.getRegionName();
                        newLineRegionNames.add(lineRegionName);
                    }
                }

                Content content = unlockRegionData.getContent();
                String text = new String(decodeBase64(content.getData()));
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, received unlock for " + //NoI18n
                    "region with content : [" + text + "]"
                ); //NoI18n

                synchronized (getDocumentLock()) {
                    editorLock = lockEditor(getEditorCookie());

                    boolean originallyModified = isDocumentModified();
                    boolean status = doUpdateRegion(messageOriginator, sect, text);

                    if (status) {
                        List newLineRegionList = rCtx.doUpdateLineRegion(
                                regionName, beginOffset, endOffset, unlockRegionData,
                                (String[]) newLineRegionNames.toArray(new String[0])
                            );

                        //persist line annotation
                        String annotationMessage = NbBundle.getMessage(
                                CollabFileHandlerSupport.class, "MSG_CollabFileHandlerSupport_HistoryAnnotation", // NOI18N
                                getContext().getPrincipal(messageOriginator).getDisplayName()
                            );

                        //int style = getUserStyle(messageOriginator);	
                        int style = -1; //history annotation
                        addLineRegionAnnotation(
                            (CollabLineRegion[]) newLineRegionList.toArray(new CollabLineRegion[0]), style,
                            annotationMessage
                        );
                    }

                    if (!originallyModified) {
                        getContext().setSkipSendFile(getName(), true);
                        saveDocument();
                        getContext().setSkipSendFile(getName(), false);
                    }

                    unlockEditor(editorLock);
                }

                rCtx.removeRegion(messageOriginator, regionName);
            }
        } catch (IllegalArgumentException iargs) {
            unlockEditor(editorLock);
            inReceiveMessageUnlock = false;
            throw new CollabException(iargs);
        }

        inReceiveMessageUnlock = false;
    }

    /**
     * removes all region from the repository
     *
     * @throws CollabException
     */
    public void removeAllRegion() throws CollabException {
        rCtx.removeAllRegion();
    }

    /**
     * isDocumentModified
     *
     */
    public boolean isDocumentModified() throws CollabException {
        boolean isModified = false;
        EditorCookie cookie = getEditorCookie();

        if (cookie != null) {
            isModified = cookie.isModified();
        }

        Debug.log(
            "CollabFileHandlerSupport", //NoI18n
            "CollabFileHandlerSupport, isModified: " + isModified + " for file " + getName()
        ); //NoI18n		

        return isModified;
    }

    /**
     * saveDocument
     *
     */
    public boolean saveDocument() throws CollabException {
        Debug.log(
            "CollabFileHandlerSupport", //NoI18n
            "CollabFileHandlerSupport, saving document: " //NoI18n
             +getName() + " on update"
        ); //NoI18n			

        try {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        try {
                            editorCookie = getEditorCookie();
                        } catch (Throwable th) {
                            Debug.log(
                                "CollabFileHandlerSupport",
                                "CollabFileHandlerSupport, " + //NoI18n
                                "wait to getEditorCookie failed"
                            ); //NoI18n
                            Debug.logDebugException(
                                "CollabFileHandlerSupport, " + //NoI18n
                                "wait to getEditorCookie failed", //NoI18n	
                                th, true
                            );
                        }
                    }
                }
            );

            if (editorCookie != null) {
                editorCookie.saveDocument();
            }
        } catch (IOException iox) {
            Debug.log(
                "CollabFileHandlerSupport", //NoI18n
                "Exception occured while saving the document: " //NoI18n
                 +getName() + " on update"
            ); //NoI18n

            return false;
        }

        return true;
    }

    /**
     * getDocumentLock
     *
     */
    protected Object getDocumentLock() {
        return documentLock;
    }

    /**
     * lockEditor
     *
     */
    protected EditorLock lockEditor(EditorCookie editorCookie)
    throws CollabException {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, locking Editor"); //NoI18n	
        disableUnlockTimer(true);

        if (editorCookie == null) {
            return null;
        }

        JEditorPane[] editorPanes = editorCookie.getOpenedPanes();

        if (editorPanes == null) {
            return null;
        }

        Debug.log(
            "CollabFileHandlerSupport", "CollabFileHandlerSupport, # of editorPanes: " + //NoI18n
            editorPanes.length
        );

        EditorLock editorLock = new EditorLock(editorPanes);

        //hack - since editorCookie.getOpenedPanes(); returns null for opened_panes
        //Here it successfully returns the panes when user edits the java file
        for (int i = 0; i < editorPanes.length; i++) {
            JEditorPane editorPane = editorPanes[i];

            if (editorPane != null) {
                EditorComponentFocusListener focusListener = null;

                //remove old
                focusListener = (EditorComponentFocusListener) focusListenerMap.get(editorPane);

                if (focusListener != null) {
                    editorPane.removeFocusListener(focusListener);
                }

                //add new
                focusListener = new EditorComponentFocusListener(getName(), getContext().getChannel());
                editorPane.addFocusListener(focusListener);
                focusListenerMap.put(editorPane, focusListener);
            }
        }

        editorLock.lock();

        return editorLock;
    }

    /**
     * unlockEditor
     *
     */
    protected void unlockEditor(EditorLock editorLock) {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, unlocking Editor"); //NoI18n

        //do not enable edit for user readOnly conversation
        if (getContext().isReadOnlyConversation()) {
            return;
        }

        if (editorLock != null) {
            editorLock.releaseLock();

            String orig = getName().substring(getName().lastIndexOf(FILE_SEPERATOR) + 1);
            TopComponent tc = WindowManager.getDefault().findTopComponent(orig);

            if (tc == null) {
                tc = WindowManager.getDefault().findTopComponent(orig + " *");
            }

            if (tc == null) {
                tc = WindowManager.getDefault().findTopComponent(
                        NbBundle.getMessage(DocumentTabMarker.class, "FMT_Mark", orig)
                    );
            }

            if (tc != null) {
                Debug.out.println("TC not null");
                tc.requestActive();
            }
        }

        disableUnlockTimer(false);
    }

    /**
     * resetAllLock
     *
     */
    protected void resetAllLock() throws CollabException {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, resetAllLock"); //NoI18n

        EditorCookie editorCookie = getEditorCookie();

        if (editorCookie == null) {
            return;
        }

        JEditorPane[] editorPanes = editorCookie.getOpenedPanes();

        if (editorPanes == null) {
            return;
        }

        Debug.log(
            "CollabFileHandlerSupport", "CollabFileHandlerSupport, # of editorPanes: " + //NoI18n
            editorPanes.length
        );

        if (editorPanes != null) {
            for (int i = 0; i < editorPanes.length; i++) {
                if (editorPanes[i] != null) {
                    Debug.log(
                        "CollabFileHandlerSupport",
                        "CollabFileHandlerSupport, editorPane: " + //NoI18n
                        editorPanes[i].getName()
                    );
                    editorPanes[i].setEnabled(true);
                }
            }
        }

        disableUnlockTimer(false);
    }

    /**
     * handles pause - Pause edit operation
     *
     * @param collabBean
     * @throws CollabException
     */
    public void handlePause() throws CollabException {
        setCurrentState(FilesharingContext.STATE_PAUSE);
        inPauseState = true;
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        pauseEditorLock = lockEditor(getEditorCookie());
                    } catch (Throwable th) {
                        Debug.log(
                            "CollabFileHandlerSupport",
                            "CollabFileHandlerSupport, " + //NoI18n
                            "cannot create received Lock "
                        ); //NoI18n
                        Debug.logDebugException(
                            "CollabFileHandlerSupport, " + //NoI18n
                            "cannot create received Lock", //NoI18n	
                            th, true
                        );
                    }
                }
            }
        );
    }

    /**
     * handles resume - Resume edit operation
     *
     * @param collabBean
     * @throws CollabException
     */
    public void handleResume() throws CollabException {
        setCurrentState(FilesharingContext.STATE_RESUME);
        inPauseState = false;
        setSkipUpdate(false);

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        unlockEditor(pauseEditorLock);
                    } catch (Throwable th) {
                        Debug.log(
                            "CollabFileHandlerSupport",
                            "CollabFileHandlerSupport, " + //NoI18n
                            "cannot create received Lock "
                        ); //NoI18n
                        Debug.logDebugException(
                            "CollabFileHandlerSupport, " + //NoI18n
                            "cannot create received Lock", //NoI18n	
                            th, true
                        );
                    }
                }
            }
        );
    }

    /**
     * constructs file-changed-data Node
     *
     * @param fileChanged the file-changed Node
     * @return fileChangedData     the file-changed-data Node
     * @throws CollabException
     * @deprecated
     */
    public FileChangedData constructFileChangedData(FileChanged fileChanged)
    throws CollabException {
        return null;
    }

    /**
     * constructs regionChanged Node
     *
     * @param regionName
     * @param cregion CollabRegion
     * @param regionChanged the region-changed Node
     * @param content
     */
    public void constructRegionChanged(
        String regionName, CollabRegion cregion, RegionChanged regionChanged, Content content
    ) throws CollabException {
        TextRegionChanged textRegionChanged = new TextRegionChanged();
        regionChanged.setTextRegionChanged(textRegionChanged);

        TextRegion textRegion = new TextRegion();
        textRegionChanged.setTextRegion(textRegion);

        TextChange textChange = new TextChange();
        textRegionChanged.setTextChange(textChange);

        int length = cregion.getEndOffset() - cregion.getBeginOffset();
        RegionInfo regionInfo = new RegionInfo(
                cregion.getID(), getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE, cregion.getBeginOffset(),
                length, 0
            );
        constructRegion(regionInfo, textRegion);
        textChange.setContent(content);
    }

    /**
     * constructs region Node
     *
     * @param regionName
     * @param regionInfo RegionInfo
     * @param pregion Protocol region
     */
    public void constructRegion(RegionInfo regionInfo, Object pregion)
    throws CollabException {
        String regionName = regionInfo.getID();
        String mode = regionInfo.getMode();
        int beginOffset = 0;
        int endOffset = 0;
        StyledDocument fileDocument = getDocument();

        if (mode.equals(RegionInfo.LINE_RANGE)) {
            javax.swing.text.Element beginElement = fileDocument.getDefaultRootElement().getElement(
                    regionInfo.getbegin()
                );
            beginOffset = beginElement.getStartOffset();

            javax.swing.text.Element endElement = fileDocument.getDefaultRootElement().getElement(regionInfo.getend());
            endOffset = endElement.getEndOffset();
        } else {
            beginOffset = regionInfo.getbegin();
            endOffset = regionInfo.getend();

            int endCorrection = regionInfo.getCorrection();
            endOffset += endCorrection;

            if (endOffset < 0) {
                endOffset = 0;
            }
        }

        if (pregion instanceof TextRegion) {
            TextRegion textRegion = (TextRegion) pregion;
            int length = endOffset - beginOffset;
            textRegion.setRegionName(regionName);
            textRegion.setBeginOffset(new java.math.BigInteger(String.valueOf(beginOffset)));
            textRegion.setLength(new java.math.BigInteger(String.valueOf(length)));
        } else if (pregion instanceof LineRegion) {
            LineRegion lineRegion = (LineRegion) pregion;
            lineRegion.setRegionName(regionName);
        }
    }

    /**
     * getContent
     *
     * @param regionChanged the region-changed Node
     * @param content
     */
    public Content getContent(RegionChanged regionChanged)
    throws CollabException {
        TextRegionChanged textRegionChanged = regionChanged.getTextRegionChanged();
        TextChange textChange = textRegionChanged.getTextChange();
        Content content = null;

        if (textChange.getContent() != null) {
            content = textChange.getContent();
        }

        return content;
    }

    /**
     * getChangeRegion
     *
     * @param regionChanged the region-changed Node
     * @param regionName
     */
    public RegionInfo getChangeRegion(RegionChanged regionChanged)
    throws CollabException {
        TextRegionChanged textRegionChanged = regionChanged.getTextRegionChanged();
        TextRegion textRegion = textRegionChanged.getTextRegion();
        RegionInfo regionInfo = new RegionInfo(
                textRegion.getRegionName(), getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE,
                textRegion.getBeginOffset().intValue(), textRegion.getLength().intValue(), 0
            );

        return regionInfo;
    }

    /**
     * constructs lock-region-data Node
     *
     * @param   lockRegionData                the intial lock-region-data Node
     * @throws CollabException
     */
    public void constructLockRegionData(LockRegionData lockRegionData)
    throws CollabException {
        String regionName = getContext().createUniqueRegionName(getName(), regionCount++);

        StyledDocument fileDocument = getDocument();
        javax.swing.text.Element beginElement = fileDocument.getDefaultRootElement().getElement(0);
        int beginOffset = beginElement.getStartOffset();

        javax.swing.text.Element endElement = fileDocument.getDefaultRootElement().getElement(fileDocument.getLength());
        int endOffset = endElement.getEndOffset();
        RegionInfo regionInfo = new RegionInfo(
                regionName, getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE, beginOffset, endOffset, 0, null
            );
        constructLockRegionData(regionInfo, lockRegionData);
    }

    /**
     * constructs lock-region-data Node
     *
     * @param   regionInfo                        the RegionInfo bean
     * @param   lockRegionData                the intial lock-region-data Node
     * @throws CollabException
     * @see                RegionInfo
     */
    public void constructLockRegionData(RegionInfo regionInfo, LockRegionData lockRegionData)
    throws CollabException {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, construct LockRegionData"); //NoI18n
        setCurrentState(FilesharingContext.STATE_LOCK);
        lockRegionData.setFileName(getName());

        Object pregion = setLockRegion(lockRegionData);

        constructRegion(regionInfo, pregion);

        Content content = new Content();
        content.setEncoding(getEncoding());
        content.setDigest(getDigest());

        String text = null;
        String regionName = regionInfo.getID();
        CollabRegion cregion = (CollabRegion) rCtx.getRegion(regionName);
        text = cregion.getContent();
        Debug.log(
            "CollabFileHandlerSupport",
            "CollabFileHandlerSupport, construct lock for " + //NoI18n
            "region with content : [" + text + "]"
        ); //NoI18n	

        String encodedChange = encodeBase64(text.getBytes());
        content.setData(encodedChange);
        lockRegionData.setContent(content);

        int regionBegin = regionInfo.getbegin();
        int regionEnd = regionInfo.getend();
        regionEnd += regionInfo.getCorrection();

        int length = regionEnd - regionBegin;
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, regionBegin: " + regionBegin); //NoI18n
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, regionEnd: " + regionEnd); //NoI18n

        Vector lineRegions = regionInfo.getLineRegion();
        CollabLineRegion[] regions = (CollabLineRegion[]) lineRegions.toArray(new CollabLineRegion[0]);

        if (lineRegions == null) {
            synchronized (getDocumentLock()) {
                EditorLock editorLock = lockEditor(getEditorCookie());
                regions = rCtx.getContainingLineRegion(regionBegin, length - 1);
                unlockEditor(editorLock);
            }
        }

        Debug.log(
            "CollabFileHandlerSupport",
            "CollabFileHandlerSupport, # of containing LineRegions is: " + //NoI18n
            regions.length
        );

        if (regions != null) {
            LineRegion[] pregions = setLockLineRegion(lockRegionData, regions.length);

            List cLineRegionList = new ArrayList();

            for (int i = 0; i < regions.length; i++) {
                CollabLineRegion cLineRegion = regions[i];
                LineRegion pLineRegion = pregions[i];
                String lineRegionName = cLineRegion.getID();
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, construct lock for region: " + //NoI18n
                    lineRegionName
                );

                int beginOffset = cLineRegion.getBeginOffset();
                int endOffset = cLineRegion.getEndOffset();

                RegionInfo lineRegionInfo = new RegionInfo(
                        lineRegionName, getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE, beginOffset,
                        endOffset, 0
                    );
                constructRegion(lineRegionInfo, pLineRegion);
                lockRegionData.setLineRegion(i, pLineRegion);

                cLineRegionList.add(cLineRegion);
            }

            rCtx.resetLineRegion(regionName, cLineRegionList);
        }

        CollabRegion textRegion = (CollabRegion) rCtx.getRegion(regionName);
        textRegion.setValid(true);
    }

    /**
     * set lock-region-data with region
     *
     * @param   lockRegionData                the intial lock-region-data Node
     * @return region
     */
    protected Object setLockRegion(LockRegionData lockRegionData) {
        TextRegion textRegion = new TextRegion();
        lockRegionData.setTextRegion(textRegion);

        return textRegion;
    }

    /**
     * set lock-region-data with region
     *
     * @param   lockRegionData                the intial lock-region-data Node
     * @return region
     */
    protected LineRegion[] setLockLineRegion(LockRegionData lockRegionData, int totalRegionCount) {
        List regionInfoList = new ArrayList();

        for (int i = 0; i < totalRegionCount; i++) {
            LineRegion lineRegion = new LineRegion();
            regionInfoList.add(lineRegion);
        }

        LineRegion[] lineRegions = (LineRegion[]) regionInfoList.toArray(new LineRegion[0]);
        lockRegionData.setLineRegion(lineRegions);

        return lineRegions;
    }

    /**
     * get region
     *
     * @param   lockRegionData                the intial lock-region-data Node
     * @return region
     */
    protected RegionInfo getLockRegion(LockRegionData lockRegionData) {
        TextRegion textRegion = lockRegionData.getTextRegion();
        int regionBegin = textRegion.getBeginOffset().intValue();
        int length = textRegion.getLength().intValue();
        int regionEnd = regionBegin + length;
        RegionInfo regionInfo = new RegionInfo(
                textRegion.getRegionName(), getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE, regionBegin,
                regionEnd, 0
            );

        return regionInfo;
    }

    /**
     * set unlock-region-data with region
     *
     * @param   unlockRegionData                the intial lock-region-data Node
     * @throws CollabException
     */
    protected Object setUnlockRegion(UnlockRegionData unlockRegionData) {
        TextRegion textRegion = new TextRegion();
        unlockRegionData.setTextRegion(textRegion);

        return textRegion;
    }

    /**
     * set unlock-region-data with region
     *
     * @param   unlockRegionData
     * @return region
     */
    protected LineRegion[] setUnlockLineRegion(UnlockRegionData unlockRegionData, int totalRegionCount) {
        List regionInfoList = new ArrayList();

        for (int i = 0; i < totalRegionCount; i++) {
            LineRegion lineRegion = new LineRegion();
            regionInfoList.add(lineRegion);
        }

        LineRegion[] lineRegions = (LineRegion[]) regionInfoList.toArray(new LineRegion[0]);
        unlockRegionData.setLineRegion(lineRegions);

        return lineRegions;
    }

    /**
     * get region
     *
     * @param   unlockRegionData                the intial lock-region-data Node
     * @return region
     */
    protected RegionInfo getUnlockRegion(UnlockRegionData unlockRegionData) {
        TextRegion textRegion = unlockRegionData.getTextRegion();
        int regionBegin = textRegion.getBeginOffset().intValue();
        int length = textRegion.getLength().intValue();
        int regionEnd = regionBegin + length;
        RegionInfo regionInfo = new RegionInfo(
                textRegion.getRegionName(), getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE, regionBegin,
                regionEnd, 0
            );

        return regionInfo;
    }

    /**
     * constructs unlock-region-data Node
     *
     * @param   regionName                                the regionName
     * @param   unlockRegionData                the intial unlock-region-data Node
     * @throws CollabException
     */
    public void constructUnlockRegionData(String regionName, UnlockRegionData unlockRegionData)
    throws CollabException {
        Debug.log(
            "CollabFileHandlerSupport",
            "CollabFileHandlerSupport, construct " + //NoI18n
            "UnlockRegionData for user: " + //NoI18n
            getContext().getLoginUser()
        );
        setCurrentState(FilesharingContext.STATE_UNLOCK);

        String loginUser = getContext().getLoginUser();
        unlockRegionData.setFileName(getName());

        Object pregion = setUnlockRegion(unlockRegionData);

        CollabRegion cregion = (CollabRegion) rCtx.getRegion(regionName);
        cregion.setValid(false);

        RegionInfo regionInfo = new RegionInfo(
                regionName, getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE, cregion.getBeginOffset(),
                cregion.getEndOffset(), 0
            );
        constructRegion(regionInfo, pregion);

        Content content = new Content();
        content.setEncoding(getEncoding());
        content.setDigest(getDigest());

        String text = null;
        text = cregion.getContent();

        if (!(text.charAt(text.length() - 1) == '\n')) {
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, Adding line feed to text"); //NoI18n	
            text += "\n";
        }

        Debug.log(
            "CollabFileHandlerSupport",
            "CollabFileHandlerSupport, construct unlock for " + //NoI18n
            "region with content : [" + text + "]"
        ); //NoI18n		

        String encodedChange = encodeBase64(text.getBytes());
        content.setData(encodedChange);
        unlockRegionData.setContent(content);

        //add new lineregion
        int beginOffset = cregion.getBeginOffset();
        int endOffset = cregion.getEndOffset();

        List newLineRegionList = null;

        synchronized (getDocumentLock()) {
            EditorLock editorLock = lockEditor(getEditorCookie());

            try {
                newLineRegionList = rCtx.doUpdateLineRegion(regionName, beginOffset, endOffset, unlockRegionData, null);
            } catch (Throwable th) {
                unlockEditor(editorLock);
                throw new CollabException(th);
            }

            unlockEditor(editorLock);
        }

        //construct protocol line regions
        LineRegion[] pregions = setUnlockLineRegion(unlockRegionData, newLineRegionList.size());

        for (int i = 0; i < newLineRegionList.size(); i++) {
            CollabLineRegion cLineRegion = (CollabLineRegion) newLineRegionList.get(i);
            LineRegion pLineRegion = pregions[i];
            String lineRegionName = cLineRegion.getID();
            Debug.log(
                "CollabFileHandlerSupport", "CollabFileHandlerSupport, New newLineRegionList" + //NoI18n
                lineRegionName
            );

            int lineBeginOffset = cLineRegion.getBeginOffset();
            int lineEndOffset = cLineRegion.getEndOffset();

            Debug.log(
                "CollabFileHandlerSupport", "CollabFileHandlerSupport, adding region: " + //NoI18n 
                lineRegionName
            );

            RegionInfo lineRegionInfo = new RegionInfo(
                    lineRegionName, getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE, lineBeginOffset,
                    lineEndOffset, 0
                );
            constructRegion(lineRegionInfo, pLineRegion);
            unlockRegionData.setLineRegion(i, pLineRegion);
        }

        CollabRegion textRegion = (CollabRegion) rCtx.getRegion(regionName);
        textRegion.removeAnnotation();

        //persist line annotation
        String user = getContext().getLoginUser();
        String annotationMessage = NbBundle.getMessage(
                CollabFileHandlerSupport.class, "MSG_CollabFileHandlerSupport_HistoryAnnotation", // NOI18N
                getContext().getPrincipal(user).getDisplayName()
            );

        //int style = getUserStyle(user);
        int style = -1; //history annotation
        addLineRegionAnnotation(
            (CollabLineRegion[]) newLineRegionList.toArray(new CollabLineRegion[0]), style, annotationMessage
        );

        rCtx.removeRegion(loginUser, regionName);
    }

    /**
     * constructs unlock-region-data Node
     *
     * @param   unlockRegionData                the intial unlock-region-data Node
     * @throws CollabException
     */
    public boolean constructUnlockRegionData(UnlockRegion unlockRegion)
    throws CollabException {
        if (isDisableUnlockTimer()) {
            return false;
        }

        if (inReceiveMessageUnlock) //return if fileHandler in received unlock
         {
            return false;
        }

        Vector userRegions = (Vector) rCtx.getUserRegion(getContext().getLoginUser());

        if ((userRegions == null) || (userRegions.size() == 0)) {
            return false;
        }

        UnlockRegionData unlockRegionData = new UnlockRegionData();
        unlockRegion.addUnlockRegionData(unlockRegionData);
        unlockRegionData.setFileName(getName());

        int isReadyToUnlock1 = 0;

        for (int i = 0; i < userRegions.size(); i++) {
            final String regionName = (String) userRegions.get(i);
            Object tmpRegion = rCtx.getRegion(regionName);

            //skip line regions
            if (tmpRegion instanceof CollabLineRegion) {
                continue;
            }

            if (!(tmpRegion instanceof CollabRegion)) {
                continue;
            }

            CollabRegion region = (CollabRegion) tmpRegion;
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, region valid:" + //NoI18n
                region.isValid()
            );

            if (!region.isValid() || !region.isReadyToUnlock()) {
                continue;
            }

            region.setValid(false);

            TimerTask cleanupRegionTask = new TimerTask() {
                    public void run() {
                        try {
                            rCtx.removeRegion(getContext().getLoginUser(), regionName);
                        } catch (CollabException ce) {
                            Debug.log(
                                "CollabFileHandlerSupport", //NoI18n
                                "CollabFileHandlerSupport, cleanup failed for: " + //NoI18n
                                regionName
                            );
                        }
                    }
                };

            getContext().schedule(cleanupRegionTask, 5000);

            try {
                constructUnlockRegionData(regionName, unlockRegionData);
            } catch (Throwable th) {
                continue;
            }

            isReadyToUnlock1++;
        }

        if (isReadyToUnlock1 == 0) {
            return false;
        }

        setCurrentState(FilesharingContext.STATE_UNLOCK);

        return true;
    }

    /**
     * constructs send-file-data Node
     *
     * @param   sendFile                        the send-file Node
    * @param   syncOperation                is send-file for sync file during user join
     * @return        sendFileData                the send-file-data Node
     * @throws CollabException
     */
    public SendFileData constructSendFileData(SendFile sendFile)
    throws CollabException {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, construct SendFileData");

        if (!isValid()) {
            return null;
        }

        setCurrentState(FilesharingContext.STATE_SENDFILE);

        SendFileData sendFileData = new SendFileData();
        FileData fileData = new FileData();
        sendFileData.setFileData(fileData);

        fileData.setFileName(getName());
        fileData.setContentType(getContentType());

        //remove all non-lineregions if already exist
        rCtx.removeAllRegion(true);

        Content content = new Content();
        sendFileData.setContent(content);

        content.setEncoding(getEncoding());
        content.setDigest(getDigest());

        byte[] fileContent = null;
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, firstTimeSend: " + firstTimeSend); //NoI18n
        Debug.log(
            "CollabFileHandlerSupport",
            "CollabFileHandlerSupport, isRetrieveFileContentOnly: " + isRetrieveFileContentOnly()
        ); //NoI18n		

        boolean doSendLineRegion = false;

        if (firstTimeSend || isRetrieveFileContentOnly()) {
            if (firstTimeSend) {
                doSendLineRegion = false;

                //saveDocument
                saveDocument();

                //find initial guarded sections once
                findInitialGuardedSections();
            } else if (isRetrieveFileContentOnly()) {
                doSendLineRegion = true;
            }

            firstTimeSend = false;

            //modify content of file if necessary before send
            fileContent = doProcessSendFileContent(getFileContentBytes());
        } else {
            //get current content
            fileContent = getContent().getBytes();
            doSendLineRegion = true;
        }

        String encodedFileContent = encodeBase64(fileContent);
        content.setData(encodedFileContent);

        //lock editor if opened
        EditorLock editorLock = lockEditor(getEditorCookie());

        try {
            //set line region user
            setLineRegionKey(getContext().getLoginUser() + "_LINEREGION_USER");

            if (!doSendLineRegion) {
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, Sending lineRegionFunction"); //NoI18n			

                LineRegionFunction lineReginFunction = new LineRegionFunction();
                sendFileData.setLineRegionFunction(lineReginFunction);
                lineReginFunction.setFunctionName("simple_linear_function");

                int totalLineCount = getDocument().getDefaultRootElement().getElementCount();
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, totalLineCount: " + totalLineCount); //NoI18n

                String[] args = new String[] { "RI", getContext().getLoginUser(), new Integer(totalLineCount).toString() };
                lineReginFunction.setArguments(args);
                sendFileData.setChooseLineRegionFunction(true);

                RegionInfo[] lineRegions = null;

                try {
                    lineRegions = findLineRegion(sendFileData);
                    Debug.log(
                        "CollabFileHandlerSupport", "CollabFileHandlerSupport, lineRegions size: " +
                        lineRegions.length
                    ); //NoI18n
                } catch (Throwable th) {
                    Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, exception: " + th.getMessage()); //NoI18n
                    th.printStackTrace();
                }

                for (int i = 0; i < lineRegions.length; i++) {
                    RegionInfo region = lineRegions[i];
                    String regionName = region.getID();
                    int beginOffset = region.getbegin();
                    int endOffset = region.getend();

                    Debug.log("CollabFileHandlerSupport_LineRegion", "CollabFileHandlerSupport, region: " + regionName); //NoI18n				

                    CollabRegion lineRegion = rCtx.createLineRegion(regionName, beginOffset, endOffset);

                    if (lineRegion != null) {
                        rCtx.addLineRegion(regionName, lineRegion);
                    } else {
                        continue;
                    }
                }

                //invoke reset after 3000 sec
                resetAllLineRegionOffset(3000);
            } else {
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, Sending lineRegions"); //NoI18n		
                printAllRegionInfo();
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, regionCount: " + //NoI18n
                    rCtx.getLineRegionCount()
                );

                int totalLineRegions = rCtx.getLineRegionCount();
                LineRegion[] pregions = (LineRegion[]) setSendFileLineRegion(sendFileData, totalLineRegions);

                for (int i = 0; i < totalLineRegions; i++) {
                    CollabLineRegion region = (CollabLineRegion) rCtx.getLineRegion(i);
                    Debug.log(
                        "CollabFileHandlerSupport", "CollabFileHandlerSupport, region: " + //NoI18n
                        region.getID()
                    );

                    RegionInfo regionInfo = new RegionInfo(
                            region.getID(), getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE,
                            region.getBeginOffset(), region.getEndOffset(), 0
                        );
                    LineRegion pregion = pregions[i];
                    constructRegion(regionInfo, pregion);
                }

                sendFileData.setChooseLineRegionFunction(false);
            }
        } catch (CollabException ce) {
            unlockEditor(editorLock);
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, SendFile failed for file: " + getName()); //NoI18n

            return null;
        }

        unlockEditor(editorLock);

        return sendFileData;
    }

    /**
     * isRetrieveFileContentOnly
     *
     */
    public boolean isRetrieveFileContentOnly() {
        return this.isSendFileContentOnly;
    }

    /**
     * setRetrieveFileContentOnly
     *
     */
    public void setRetrieveFileContentOnly(boolean flag) {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, setRetrieveFileContentOnly: " + flag); //NoI18n
        this.isSendFileContentOnly = flag;
    }

    /**
     * set sendfile-data with region
     *
     * @param   sendFileRegionData                the intial lock-region-data Node
     * @return region
     */
    protected Object setSendFileLineRegion(SendFileData sendFileData, int totalRegionCount) {
        List regionInfoList = new ArrayList();

        for (int i = 0; i < totalRegionCount; i++) {
            LineRegion lineRegion = new LineRegion();
            regionInfoList.add(lineRegion);
        }

        LineRegion[] lineRegions = (LineRegion[]) regionInfoList.toArray(new LineRegion[0]);
        sendFileData.setLineRegion(lineRegions);

        return lineRegions;
    }

    /**
     * get region
     *
     * @param   lockRegionData                the intial lock-region-data Node
     * @return region
     */
    protected RegionInfo[] getSendFileRegion(SendFileData sendFileData)
    throws CollabException {
        List regionInfoList = new ArrayList();
        LineRegion[] lineRegions = sendFileData.getLineRegion();

        if (lineRegions != null) {
            StyledDocument fileDocument = getDocument();
            int lineRegionCount = lineRegions.length;

            if (lineRegionCount > fileDocument.getDefaultRootElement().getElementCount()) {
                lineRegionCount = fileDocument.getDefaultRootElement().getElementCount();
            }

            for (int i = 0; i < lineRegionCount; i++) {
                LineRegion lineRegion = lineRegions[i];

                try {
                    javax.swing.text.Element currentElement = fileDocument.getDefaultRootElement().getElement(i);
                    int regionBegin = currentElement.getStartOffset();
                    int regionEnd = currentElement.getEndOffset();
                    RegionInfo regionInfo = new RegionInfo(
                            lineRegion.getRegionName(), getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE,
                            regionBegin, regionEnd, 0
                        );
                    regionInfoList.add(regionInfo);
                } catch (Throwable th) {
                    break;
                }
            }

            return (RegionInfo[]) regionInfoList.toArray(new RegionInfo[0]);
        }

        return null;
    }

    /**
     * doProcessSendFileContent
     *
     * @param   content
     * @return        fileContent
     * @throws CollabException
     */
    public byte[] doProcessSendFileContent(byte[] content)
    throws CollabException {
        String fileContent = new String(content);

        //add 3 additional lines
        //fileContent+=CollabFileHandler.EMPTY_CONTENT;//NoI18n
        byte[] returnContent = fileContent.getBytes();

        inReceiveSendFile = true;

        //updateFileObject(returnContent);
        inReceiveSendFile = false;

        return returnContent;
    }

    /**
     * create a CollabFileHandler#simpleSection
     * @param beginOffset
     * @param endOffset
     * @param regionName
     * @throws CollabException
     * @return  CollabFileHandler#simpleSection
     */
    public CollabRegion createSimpleSection(int beginOffset, int endOffset, String regionName)
    throws CollabException {
        if (rCtx.getRegion().size() == 0) {
            NbDocument.unmarkGuarded(getDocument(), 0, getDocument().getLength());
        }

        //create guarded region
        return createRegion(regionName, beginOffset, endOffset, false, true);
    }

    /**
     * return true if can create region
     *
     * @param beginOffset
     * @param endOffset
     * @throws CollabException
     * @return
     */
    public boolean testCreateRegionLineBounds(int beginLine, int endLine)
    throws CollabException {
        StyledDocument fileDocument = getDocument();
        int beginOffset = fileDocument.getDefaultRootElement().getElement(beginLine).getStartOffset() - 1;

        if (beginOffset < 0) {
            beginOffset = 0;
        }

        int endOffset = fileDocument.getDefaultRootElement().getElement(endLine).getEndOffset();

        return testCreateRegion(beginOffset, endOffset);
    }

    /**
     * return true if can create region
     *
     * @param beginOffset
     * @param endOffset
     * @throws CollabException
     * @return
     */
    public boolean testCreateRegion(int beginOffset, int endOffset)
    throws CollabException {
        int length = endOffset - beginOffset;
        CollabRegion region = rCtx.getContainingRegion(beginOffset, length, true);

        if ((region != null) && !(region instanceof CollabLineRegion)) {
            return false;
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Document methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * get the file document content of a region
     *
     * @param   regionName                                the regionName
     * @return        document Content                content
     * @throws CollabException
     */
    public String getContent(String regionName) throws CollabException {
        StyledDocument fileDocument = getDocument();
        CollabRegion region = (CollabRegion) rCtx.getRegion(regionName);

        return region.getContent();
    }

    /**
     * get the file document content
     *
     * @return        document Content                content
     * @throws CollabException
     */
    public String getContent() throws CollabException {
        FileObject fileOject = getFileObject();

        try {
            StyledDocument fileDocument = getDocument();

            return fileDocument.getText(0, fileDocument.getLength());
        } catch (javax.swing.text.BadLocationException ex) {
            //throw new CollabException(ex);
        }

        return "";
    }

    /**
     *
     * @return contentTyoe
     */

    //public abstract String getContentType();

    /**
     * getter for contentType
     *
     * @return contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * setter for contentType
     *
     * @param        contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Management methods
    ////////////////////////////////////////////////////////////////////////////                

    /**
     * test if the file changed
     *
     * @return        true/false                                true if file changes
     */
    public boolean isChanged() throws CollabException {
        //logic to register doc listener if not present
        if (!fileChanged) {
            try {
                boolean isDocChanged = isChangedDocument(getDocument());

                if (isDocChanged) {
                    //register doc listener if doc length changed but there is no indication of change
                    // -commenting out
                    //doc listener is now added from TC listener
                    //registerDocumentListener();
                }
            } catch (CollabException ce) {
                if (ce.getCause() instanceof org.openide.loaders.DataObjectNotFoundException) {
                    //ignore, this exception
                } else if (ce.getCause() instanceof java.io.SyncFailedException) {
                    //ignore, this exception
                } else {
                    throw ce;
                }
            }
        }

        return fileChanged;
    }

    /**
     * setter for filechanged
     *
     * @param        status
     */
    public void updateStatusChanged(boolean status) {
        fileChanged = status;
    }

    /**
     * test if the file changed
     *
     * @return        true/false                                true if file changes
     */
    public boolean isChangedDocument(StyledDocument doc) {
        if (doc == null) {
            return false;
        }

        int docLength = doc.getLength();

        if (docLength != previousDocLength) {
            previousDocLength = docLength;

            return true;
        }

        return false;
    }

    /**
     * resetAnnotation
     *
     * @param        region
     */
    public void resetAnnotation(CollabRegion region) throws CollabException {
        //remove annotation
        region.removeAnnotation();

        //add annotation
        String user = getContext().getLoginUser();
        String annotationMessage = NbBundle.getMessage(
                CollabFileHandlerSupport.class, "MSG_CollabFileHandlerSupport_EditingAnnotation", // NOI18N
                getContext().getPrincipal(user).getDisplayName()
            );
        int style = getUserStyle(user);
        ((CollabRegion) region).addAnnotation(
            FileshareUtil.getDataObject(getFileObject()), (CollabFileHandler) this, style, annotationMessage
        );
    }

    /**
     * resetAnnotation
     *
     * @param   region
     * @param   annotationMessage
     * @param   style
     */
    public void resetAnnotation(CollabRegion region, int style, String annotationMessage)
    throws CollabException {
        CollabLineRegion[] lineRegions = null;

        synchronized (getDocumentLock()) {
            EditorLock editorLock = lockEditor(getEditorCookie());
            int regionBegin = region.getBeginOffset();
            ;

            int regionEnd = region.getEndOffset();
            int length = regionEnd - regionBegin;
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, regionBegin: " + regionBegin); //NoI18n
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, regionEnd: " + regionEnd); //NoI18n			
            lineRegions = rCtx.getContainingLineRegion(regionBegin, length - 1);
            unlockEditor(editorLock);
        }

        resetLineRegionAnnotation(lineRegions, style, annotationMessage);
    }

    /**
     * resetLineRegionAnnotation
     *
     * @param   lineRegions
     * @param   annotationMessage
     * @param   style
     */
    public void resetLineRegionAnnotation(CollabLineRegion[] lineRegions, int style, String annotationMessage)
    throws CollabException {
        for (int i = 0; i < lineRegions.length; i++) {
            CollabLineRegion lineRegion = lineRegions[i];

            if (lineRegion != null) {
                addAnnotation(lineRegion, style, annotationMessage);
            }
        }
    }

    /**
     * removeAllLineRegionAnnotation
     *
     * @param   lineRegions
     */
    public void removeAllLineRegionAnnotation() throws CollabException {
        int totalLineRegions = rCtx.getLineRegionCount();

        for (int i = 0; i < totalLineRegions; i++) {
            CollabLineRegion lineRegion = (CollabLineRegion) rCtx.getLineRegion(i);
            Debug.log(
                "CollabFileHandlerSupport",
                "CollabFileHandlerSupport, " + //NoI18n
                "remove annotation for: " + lineRegion.getID()
            ); //NoI18n

            if (lineRegion != null) {
                lineRegion.removeAnnotation();
            }
        }
    }

    /**
     * skip insertUpdate
     *
     * @param offset
     * @return true if skip
     */
    public boolean skipInsertUpdate(int offset) {
        Debug.log(
            "CollabFileHandlerSupport",
            "CollabFileHandlerSupport, " + //NoI18n
            "skipInsertUpdate for user: " + getContext().getLoginUser()
        ); //NoI18n
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " + "skipUpdateAlways: " + skipUpdateAlways); //NoI18n		
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " + "skipUpdate: " + skipUpdate); //NoI18n
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " + "inPauseState: " + inPauseState); //NoI18n
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " + "inReceiveSendFile: " + inReceiveSendFile); //NoI18n		

        //skip updateText related insert
        if (
            skipUpdateAlways //skip always
                 ||skipUpdate //skip if set to true
                 ||inPauseState //skip if received Pause
                 ||inReceiveSendFile //skip if in inReceiveSendFile
        ) {
            return true;
        }

        return false;
    }

    /**
     * skip removeUpdate
     *
     * @param offset
     * @param length
     * @return true if skip
     */
    public boolean skipRemoveUpdate(int offset, int length) {
        Debug.log(
            "CollabFileHandlerSupport",
            "CollabFileHandlerSupport, " + //NoI18n
            "skipRemoveUpdate for user: " + getContext().getLoginUser()
        ); //NoI18n
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " + "skipUpdateAlways: " + skipUpdateAlways); //NoI18n		
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " + "skipUpdate: " + skipUpdate); //NoI18n
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " + "inPauseState: " + inPauseState); //NoI18n
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " + "inReceiveSendFile: " + inReceiveSendFile); //NoI18n		

        //skip updateText related remove
        if (
            skipUpdateAlways //skip always
                 ||skipUpdate //skip if set to true
                 ||inPauseState //skip if received Pause
                 ||inReceiveSendFile //skip if in inReceiveSendFile
        ) {
            return true;
        }

        return false;
    }

    /**
     * skip Insert or Remove if set to true
     *
     * @param        status                                        if true skip insert/remove
     * @throws CollabException
     */
    public synchronized void setSkipUpdate(boolean skipUpdate) {
        Debug.log(
            "CollabFileHandlerSupport", //NoI18n
            "CollabFileHandlerSupport, setSkipUpdate: " + //NoI18n
            skipUpdate + " for user: " + getContext().getLoginUser()
        );
        this.skipUpdate = skipUpdate;
    }

    /**
     * setValid
     *
     * @param        status                                        if false handler is invalid
     */
    public void setValid(boolean valid) {
        this.isValid = valid;
    }

    /**
     * getValid
     *
     * @return        status                                        if false handler is invalid
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * setLineRegionKey
     *
     * @param        key
     * @throws CollabException
     */
    public void setLineRegionKey(String key) {
        this.lineRegionKey = key;
    }

    /**
     * getLineRegionKey
     *
     * @return        key
     */
    public String getLineRegionKey() {
        return this.lineRegionKey;
    }

    /**
     * create a New Region, sends a lock message
     *
     * @param insertUpdate
     * @param offset
     * @param length
     * @throws CollabException
     */
    public void createNewRegion(boolean insertUpdate, int offset, int length, String insertText)
    throws CollabException {
        if (length == 0) {
            return;
        }

        StyledDocument fileDocument = getDocument();
        int currLine = fileDocument.getDefaultRootElement().getElementIndex(offset);
        int beginLine = currLine;
        int endLine = currLine + 1;

        //do the correction for
        int endOffsetCorrection = 0;

        if (insertUpdate) {
            endOffsetCorrection = -length;
            endLine = fileDocument.getDefaultRootElement().getElementIndex(offset + length);
        } else {
            endOffsetCorrection = length;
            beginLine = currLine - 1;

            if (beginLine < 0) {
                beginLine = 0;
            }
        }

        int elementCount = fileDocument.getDefaultRootElement().getElementCount();

        if ((endLine >= elementCount) || (beginLine >= elementCount)) {
            return;
        }

        //Testing overlap 2 lines (beginLine, endLine)
        if (!testCreateRegionLineBounds(beginLine, endLine)) {
            beginLine = currLine;
            endLine = currLine;
        }

        createNewRegion(beginLine, endLine, endOffsetCorrection);
    }

    /**
     * create a New Region, sends a lock message
     *
     * @param beginLine
     * @param endLine
     * @param endOffsetCorrection
     * @throws CollabException
     */
    public void createNewRegion(int beginLine, int endLine, int endOffsetCorrection)
    throws CollabException {
        createNewRegion(beginLine, endLine, endOffsetCorrection, false);
    }

    /**
     * create a New Region, sends a lock message
     *
     * @param beginLine
     * @param endLine
     * @param endOffsetCorrection
     * @param includeMarginLines
     * @throws CollabException
     */
    public void createNewRegion(int beginLine, int endLine, int endOffsetCorrection, boolean includeMarginLines)
    throws CollabException {
        //Create a new region offset:currentline-1 to curentline +1
        String regionName = getContext().createUniqueRegionName(getName(), regionCount++);

        StyledDocument fileDocument = null;

        try {
            fileDocument = getDocument();
        } catch (CollabException ce) {
            ce.printStackTrace(Debug.out);

            return;
        }

        if (includeMarginLines) {
            if (beginLine > 0) {
                beginLine -= 1;
            }

            if (endLine < (fileDocument.getDefaultRootElement().getElementCount() - 2)) {
                endLine += 2;
            } else if (endLine < (fileDocument.getDefaultRootElement().getElementCount() - 1)) {
                endLine += 1;
            }

            if (endLine >= fileDocument.getDefaultRootElement().getElementCount()) {
                endLine = fileDocument.getDefaultRootElement().getElementCount() - 1;
            }

            Debug.log(
                "CollabFileHandlerSupport", //NoI18n
                "CollabFileHandlerSupport, " + "includeMarginLines beginLine: " + beginLine + " endLine: " + endLine
            );
        }

        int beginOffset = fileDocument.getDefaultRootElement().getElement(beginLine).getStartOffset() - 1;

        if (beginOffset < 0) {
            beginOffset = 0;
        }

        int endOffset = fileDocument.getDefaultRootElement().getElement(endLine).getEndOffset();

        CollabRegion textRegion = createRegion(regionName, beginOffset, endOffset, false);

        if (textRegion != null) {
            Vector lineRegions = new Vector(10);
            boolean foundFirstMatch = false;

            for (int i = beginLine; i < rCtx.getLineRegionCount(); i++) {
                CollabLineRegion lineRegion = rCtx.getLineRegion(i);

                if (lineRegion == null) {
                    continue;
                }

                int lineBeginOffset = lineRegion.getBeginOffset();
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, Check: lineBeginOffset: " + //NoI18n
                    lineBeginOffset + " endOffset: " + endOffset
                ); //NoI18n

                if ((lineBeginOffset >= beginOffset) && (lineBeginOffset < endOffset)) {
                    foundFirstMatch = true;
                    Debug.log(
                        "CollabFileHandlerSupport", "CollabFileHandlerSupport, Add line: " + //NoI18n
                        lineRegion.getID()
                    );
                    lineRegions.add(lineRegion);
                } else {
                    if (foundFirstMatch) //reached end of match
                     {
                        break;
                    }
                }
            }

            if (lineRegions.size() == 0) {
                int newBeginLine = beginLine - 1;

                if (newBeginLine < 0) {
                    newBeginLine = 0;
                }

                CollabLineRegion beginLineRegion = rCtx.getLineRegion(newBeginLine);

                if (beginLineRegion != null) {
                    Debug.log(
                        "CollabFileHandlerSupport",
                        "CollabFileHandlerSupport, Add line: " + //NoI18n
                        beginLineRegion.getID()
                    );
                    lineRegions.add(beginLineRegion);
                }

                int newEndLine = endLine + 1;

                if (newEndLine >= fileDocument.getDefaultRootElement().getElementCount()) {
                    newEndLine = endLine;
                }

                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport," + "no line regions match, so try to create regions from " +
                    newBeginLine + " to: " + newEndLine
                );

                CollabLineRegion endLineRegion = rCtx.getLineRegion(newEndLine);

                if (endLineRegion != null) {
                    Debug.log(
                        "CollabFileHandlerSupport",
                        "CollabFileHandlerSupport, Add line: " + //NoI18n
                        endLineRegion.getID()
                    );
                    lineRegions.add(endLineRegion);
                }
            }

            textRegion.setValid(false);
            rCtx.addRegion(getContext().getLoginUser(), regionName, textRegion);

            //add annotation
            String user = getContext().getLoginUser();
            String annotationMessage = NbBundle.getMessage(
                    CollabFileHandlerSupport.class, "MSG_CollabFileHandlerSupport_EditingAnnotation", // NOI18N
                    getContext().getPrincipal(user).getDisplayName()
                );
            int style = getUserStyle(user);

            //addAnnotation(textRegion, style, annotationMessage);
            addLineRegionAnnotation(
                (CollabLineRegion[]) lineRegions.toArray(new CollabLineRegion[0]), style, annotationMessage
            );

            Debug.log(
                "CollabFileHandlerSupport",
                "CollabFileHandlerSupport, createLock text : [" + //NoI18n
                textRegion.getContent() + "]"
            ); //NoI18n				

            RegionInfo regionInfo = new RegionInfo(
                    textRegion.getID(), getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE,
                    textRegion.getBeginOffset(), textRegion.getEndOffset(), endOffsetCorrection, lineRegions
                );

            LockRegionEvent lockRegionEvent = new LockRegionEvent(
                    new LockRegionContext(LockRegionEvent.getEventID(), getFileHandler(), regionInfo)
                );
            getContext().getChannelEventNotifier().notify(lockRegionEvent);

            textRegion.updateStatusChanged(true);
            currentUpdatedRegion = ((CollabRegion) textRegion);
        }
    }

    /**
     * creates a CollabRegion, a super-class for all regions
     *
     * @param regionName the regionName
     * @param beginOffset the beginOffset
     * @param endOffset the endOffset
     * @throws CollabException
     * @return
     */
    public CollabRegion createRegion(String regionName, int beginOffset, int endOffset)
    throws CollabException {
        return createRegion(regionName, beginOffset, endOffset, true, false);
    }

    /**
     * creates a CollabRegion, a super-class for all regions
     *
     * @param regionName the regionName
     * @param beginOffset the beginOffset
     * @param endOffset the endOffset
     * @param testOverlap
     * @throws CollabException
     * @return
     */
    public CollabRegion createRegion(String regionName, int beginOffset, int endOffset, boolean testOverlap)
    throws CollabException {
        return createRegion(regionName, beginOffset, endOffset, testOverlap, false);
    }

    /**
     * creates a CollabRegion, a super-class for all regions
     *
     * @param testOverlap
     * @param regionName the regionName
     * @param beginOffset the beginOffset
     * @param endOffset the endOffset
     * @param testOverlap
     * @param guarded
     * @throws CollabException
     * @return
     */
    public abstract CollabRegion createRegion(
        String regionName, int beginOffset, int endOffset, boolean testOverlap, boolean guarded
    ) throws CollabException;

    /**
     * test if the region exist in the repository
     *
     * @param offset
     * @param length
     * @param length of adjacency of change to this region
     * @return        true/false                                true if region exist
     */
    public boolean regionExist(int offset, int length, int adjacency)
    throws CollabException {
        return rCtx.regionExist(offset, length, adjacency);
    }

    /**
     * unlock all user regions(ready for removal)
     *
     */
    public void unlockAllUserRegions() {
        rCtx.unlockAllUserRegions();
    }

    /**
     * get a region from the repository
     *
     * @param   regionName                                the regionName
     * @return        region                                        CollabRegion
     */
    public CollabRegion getRegion(String regionName) {
        return rCtx.getRegion(regionName);
    }

    /**
     * return a region that contains the offset
     *
     * @param user
     * @param beginOffset
     * @param length
     * @param applyCorrection
     * @return
     */
    protected CollabRegion getContainingRegion(int offset, int length, boolean applyCorrection) {
        return rCtx.getContainingRegion(offset, length, applyCorrection);
    }

    /**
     * get a region from the repository
     *
     * @param   regionName                                the regionName
     * @return        CollabLineRegion
     */
    public CollabLineRegion getLineRegion(String regionName) {
        return rCtx.getLineRegion(regionName);
    }

    /**
     * get a region from the repository
     *
     * @param   regionIndex
     * @return        CollabLineRegion
     */
    public CollabLineRegion getLineRegion(int regionIndex) {
        return rCtx.getLineRegion(regionIndex);
    }

    /**
     * doUpdateRegion
     *
     * @param   messageOriginator   the sender of this message
     * @param sect
     * @param text
     * @throws CollabException
     */
    public boolean doUpdateRegion(String messageOriginator, CollabRegion sect, String text)
    throws CollabException {
        return rCtx.doUpdateRegion(messageOriginator, sect, text);
    }

    /**
     * remove a guarded section
     *
     * @param sect
     * @throws CollabException
     */
    public void removeSection(CollabRegion region) throws CollabException {
        if (region.isGuarded()) {
            ((CollabRegionSupport.SimpleSection) region.getGuard()).removeSection();

            if (rCtx.getRegion().size() == 1) {
                NbDocument.unmarkGuarded(getDocument(), 0, getDocument().getLength());
            }
        }
    }

    /**
         * update file status
         *
         * @param offset
         * @param length
         * @param insert
         * @throws CollabException
         */
    public void updateStatusFileChanged(int offset, int length, boolean insert)
    throws CollabException {
        rCtx.updateAllRegionBeginOffset(offset, length, insert);
        fileChanged = true;
    }

    /**
     * for debug
     *
     */
    public void printAllRegionInfo() {
        CollabRegion[] objArr = (CollabRegion[]) rCtx.getRegion().values().toArray(new CollabRegion[0]);

        for (int i = 0; i < objArr.length; i++) {
            CollabRegion sect = objArr[i];
            printRegionInfo(sect, "");
        }
    }

    /**
     * for debug
     *
     */
    public void printRegionInfo(CollabRegion sect, String message) {
        int beginOffset = 0;
        int endOffset = 0;
        String regionName = null;
        regionName = sect.getID();
        beginOffset = sect.getBeginOffset();
        endOffset = sect.getEndOffset();
        printRegionInfo(regionName, beginOffset, endOffset, message);
    }

    /**
     * for debug
     *
     */
    public void printRegionInfo(String regionName, int beginOffset, int endOffset, String message) {
        Debug.log("CollabFileHandlerSupport_RegionInfo", "\n\n" + message + "\n========================\n"); //NoI18n	
        Debug.log("CollabFileHandlerSupport_RegionInfo", "regionName: " + regionName); //NoI18n	
        Debug.log("CollabFileHandlerSupport_RegionInfo", "begin: " + beginOffset); //NoI18n	
        Debug.log("CollabFileHandlerSupport_RegionInfo", "end: " + endOffset); //NoI18n	
        Debug.log("CollabFileHandlerSupport_RegionInfo", "\n========================\n"); //NoI18n	
    }

    /**
     *
     * @return folder
     */
    public FileObject createFolder(String fullpath) throws CollabException {
        FileObject folder = FileUtil.toFileObject(
                ((CollabFilesystem) getContext().getCollabFilesystem()).getCollabRoot()
            ); //getContext().getCollabFilesystem().getRoot();

        try {
            int index = fullpath.lastIndexOf(FILE_SEPERATOR);

            if (index != -1) {
                String path = fullpath.substring(0, index);
                StringTokenizer st = new StringTokenizer(path, FILE_SEPERATOR);

                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    FileObject tmpFolder = folder.getFileObject(token);

                    if (tmpFolder == null) {
                        tmpFolder = folder.createFolder(token);
                    }

                    folder = tmpFolder;
                }
            }
        } catch (IOException e) {
            throw new CollabException(e);
        }

        return folder;
    }

    /**
     * getter for fileName
     *
     * @return        fileName                                name of file being handled
     */
    public String getName() {
        return fileName;
    }

    /**
     * getter for fileSize
     *
     * @return        fileSize                                size of file being handled
     */
    public long getFileSize() {
        return this.fileObject.getSize();
    }

    /**
     * getter for groupName where this file belongs
     *
     * @return        fileGroupName                        name of fileGroup
     */
    public SharedFileGroup getSharedFileGroup() {
        return sharedFileGroup;
    }

    /**
     * getter for groupName where this file belongs
     *
     * @return        fileGroupName                        name of fileGroup
     */
    public String getFileGroupName() {
        return getSharedFileGroup().getName();
    }

    /**
     * setter for fileName
     *
     * @param        fileName                                name of file being handled
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * setter for sharedFileGroup where this file belongs
     *
     * @param        fileGroupName                        name of fileGroup
     */
    public void setFileGroup(SharedFileGroup sharedFileGroup) {
        this.sharedFileGroup = sharedFileGroup;
    }

    /**
     * isSendFirstTime
     *
     * @return        isSendFirstTime
     */
    public boolean isSendFirstTime() {
        return this.firstTimeSend;
    }

    /**
     * getter for filehandler
     *
     * @return        filehandler
     */
    public abstract CollabFileHandler getFileHandler();

    /**
     * getter for file content encoding
     *
     * @return encoding
     */
    public String getEncoding() {
        return CollabFileHandler.ENCODING;
    }

    /**
     * getter for file content digest
     *
     * @return digest
     */
    public String getDigest() {
        return CollabFileHandler.DIGEST;
    }

    /**
     * setter for CollabContext
     *
     * @param        context
     */
    public void setContext(CollabContext context) {
        this.context = (FilesharingContext) context;
    }

    /**
     * getter for channel
     *
     * @return channel
     */
    public FilesharingContext getContext() {
        return this.context;
    }

    /**
     * setSkipUpdateAlways
     *
     * @param skipUpdateAlways
     */
    public void setSkipUpdateAlways(boolean skipUpdateAlways) {
        Debug.log(
            "CollabFileHandlerSupport", //NoI18n
            "CollabFileHandlerSupport, skipUpdateAlways to: " + //NoI18n	
            skipUpdateAlways
        );
        this.skipUpdateAlways = skipUpdateAlways;
    }

    /**
     *
     * @return
     */
    public int getCurrentState() {
        return this.currentState;
    }

    /**
     * setCurrentState
     *
     * @param currentState
     */
    public void setCurrentState(int currentState) {
        Debug.log(
            "CollabFileHandlerSupport", //NoI18n
            "CollabFileHandlerSupport, setCurrentState to: " + //NoI18n	
            currentState
        );
        setCurrentState(currentState, -1);
    }

    /**
     * setCurrentState
     *
     * @param currentState
     * @param delay
     */
    public void setCurrentState(final int currentState, long delay) {
        setCurrentState(currentState, delay, true);
    }

    /**
     * setCurrentState
     *
     * @param currentState
     * @param delay
     * @param saveUnconditionally
     */
    public void setCurrentState(final int currentState, long delay, final boolean saveUnconditionally) {
        setCurrentState(currentState, delay, saveUnconditionally, false);
    }

    /**
     * setCurrentState
     *
     * @param currentState
     * @param delay
     * @param saveUnconditionally
     * @param changeSkipUpdate
     */
    public void setCurrentState(
        final int currentState, long delay, final boolean saveUnconditionally, final boolean changeSkipUpdate
    ) {
        Debug.log(
            "CollabFileHandlerSupport", //NoI18n
            "CollabFileHandlerSupport, setCurrentState to: " + //NoI18n
            currentState + " for: " + delay + " millis"
        );
        this.currentState = currentState;

        if (delay == -1) {
            return;
        }

        if (changeSkipUpdate) {
            setSkipUpdate(true);
        }

        TimerTask resetStateTask = new TimerTask() {
                public void run() {
                    int currentState = CollabFileHandlerSupport.this.getCurrentState();
                    Debug.log(
                        "CollabFileHandlerSupport", //NoI18n
                        "CollabFileHandlerSupport, reset setCurrentState from: " + //NoI18n
                        currentState
                    );

                    try {
                        if (saveUnconditionally) {
                            getContext().setSkipSendFile(getName(), true);
                            saveDocument();

                            //sleep for 200 millis after save
                            try {
                                Thread.sleep(200);
                            } catch (java.lang.Throwable th) {
                                //ignore
                            }

                            getContext().setSkipSendFile(getName(), false);
                        }
                    } catch (CollabException ce) {
                        Debug.log(
                            "CollabFileHandlerSupport", //NoI18n
                            "CollabFileHandlerSupport, cannot saveDocument" + //NoI18n
                            getName()
                        );
                    }

                    if (changeSkipUpdate) {
                        setSkipUpdate(false);
                    }

                    CollabFileHandlerSupport.this.setCurrentState(FilesharingContext.STATE_UNKNOWN);
                }
            };

        getContext().schedule(resetStateTask, delay);
    }

    /**
     *
     * @return sendFileTimerTask
     */
    public SendFileTimerTask getSendFileTimerTask() {
        return this.sendFileTimerTask;
    }

    /**
     *
     * @param sendFileTimerTask
     */
    public void setSendFileTimerTask(SendFileTimerTask sendFileTimerTask) {
        this.sendFileTimerTask = sendFileTimerTask;
    }

    /**
     *
     * @return true if already scheduled
     */
    public boolean isScheduledSendFileGroup() {
        return this.scheduledSendFileGroupStatus;
    }

    /**
     *
     * @param status
     */
    public void setScheduledSendFileGroup(boolean status) {
        this.scheduledSendFileGroupStatus = status;
    }

    /**
     * setter for fileObject
     *
     * @param        fileObject                                fileObject of file being handled
     */
    public void setFileObject(FileObject file) {
        this.fileObject = file;
    }

    /**
     * getter for fileObject
     *
     * @return        fileObject                                fileObject of file being handled
     */
    public FileObject getFileObject() {
        return fileObject;
    }

    /**
     * create a fileObject with the given fileContent
     *
     * @param fileContent
     * @throws CollabException
     * @return fileObject
     */
    public FileObject createFileObject(String fileContent)
    throws CollabException {
        return createFileObject(fileContent.getBytes());
    }

    /**
     * create a fileObject with the given fileContent
     *
     * @param fileContent
     * @throws CollabException
     * @return fileObject
     */
    public FileObject createFileObject(byte[] fileContent)
    throws CollabException {
        getContext().setSkipSendFile(getName(), true);

        FileSystem filesystem = getContext().getCollabFilesystem();
        this.fileContent = fileContent;

        if (fileObject == null) {
            fileObject = filesystem.findResource(getName());

            if (fileObject == null) {
                try {
                    final FileSystem fs = getContext().getCollabFilesystem();
                    final String fs_path = ((CollabFilesystem) fs).getCollabRoot().getPath();
                    fs.runAtomicAction(
                        new FileSystem.AtomicAction() {
                            public void run() throws IOException {
                                FileObject folder = null;

                                try {
                                    folder = createFolder(getName());
                                } catch (CollabException ex) {
                                    //ErrorManager.getDefault().notify(ex);
                                    String r_folder = getName().substring(0, getName().lastIndexOf(FILE_SEPERATOR));
                                    Debug.log(
                                        "CollabFileHandlerSupport", "CollabFileHandlerSupport, r_folder: " + r_folder
                                    );

                                    URL url = new URL("file:///" + fs_path + File.separator + r_folder);
                                    Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, url: " + url);
                                    new File(fs_path + File.separator + r_folder).mkdirs();
                                    fs.refresh(false);
                                    folder = URLMapper.findFileObject(url);
                                }

                                if (folder == null) {
                                    throw new IOException("Create folder failed for path: " + getName());
                                }

                                int index = getName().lastIndexOf(FILE_SEPERATOR);

                                if (index != -1) {
                                    fileObject = createFile(folder, getName().substring(index + 1));
                                } else {
                                    fileObject = createFile(folder, getName());
                                }

                                try {
                                    updateFileObject(CollabFileHandlerSupport.this.fileContent);
                                } catch (CollabException ex) {
                                    ErrorManager.getDefault().notify(ex);
                                }
                            }
                        }
                    );
                } catch (FileStateInvalidException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                } finally {
                    getContext().setSkipSendFile(getName(), false);
                }
            } else {
                updateFileObject(fileContent);
            }
        }

        getContext().setSkipSendFile(getName(), false);

        return fileObject;
    }

    protected FileObject createFile(FileObject folder, String fileName) {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " + //NoI18n
            "createFile: " + fileName
        ); //NoI18n

        return createFile(folder, fileName, null);
    }

    protected FileObject createFile(FileObject folder, String fileName, String ext) {
        Debug.log(
            "CollabFileHandlerSupport",
            "CollabFileHandlerSupport, " + //NoI18n
            "createFile: " + fileName + " ext: " + ext
        ); //NoI18n		

        try {
            if ((ext != null) && (ext.trim().length() > 0)) {
                return folder.createData(fileName, ext);
            } else {
                return folder.createData(fileName);
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        return null;
    }

    /**
     * update given fileObject
     *
     * @param fileContent
     * @throws CollabException
     */
    public void updateFileObject(String fileContent) throws CollabException {
        updateFileObject(fileContent.getBytes());
    }

    /**
     * update given fileObject
     *
     * @param fileContent
     * @throws CollabException
     */
    public void updateFileObject(byte[] fileContent) throws CollabException {
        this.fileContent = fileContent;

        try {
            FileSystem fs = getContext().getCollabFilesystem();
            fs.runAtomicAction(
                new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                                CollabFileHandlerSupport.this.fileContent
                            );

                        try {
                            fileLock = fileObject.lock();
                        } catch (FileAlreadyLockedException ale) {
                            //ignore
                        }

                        fileOutputStream = fileObject.getOutputStream(fileLock);
                        StreamCopier.copyStream(inputStream, fileOutputStream);
                    }
                }
            );
        } catch (FileStateInvalidException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        } finally {
            if (fileLock != null) {
                fileLock.releaseLock();
            }

            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                throw new CollabException(e);
            }
        }

        getFileObject().refresh(false);
    }

    /**
     * return document object for this file
     *
     * @throws CollabException
     * @return document
     */
    public StyledDocument getDocument() throws CollabException {
        try {
            StyledDocument document = getEditorCookie().openDocument();

            return document;
        } catch (org.openide.loaders.DataObjectNotFoundException notFound) {
            throw new CollabException(notFound);
        } catch (java.io.IOException io) {
            throw new CollabException(io);
        }
    }

    /**
     * return editor cookie for this filehandler
     *
     * @throws CollabException
     * @return document
     */
    public EditorCookie getEditorCookie() throws CollabException {
        Debug.log(
            "CollabFileHandlerSupport", "CollabFileHandlerSupport, " + //NoI18n
            "geteditorCookie for file: " + getName()
        ); //NoI18n		

        try {
            if (editorCookie == null) {
                FileObject file = getFileObject();

                // Get the FileObject
                if (file == null) {
                    return null;
                }

                // Get the DataObject
                DataObject dataObject = DataObject.find(file);

                if (dataObject == null) {
                    throw new IllegalArgumentException("No DataObject found for file \"" + getName() + "\"");
                }

                // Get the editor cookie for the file
                editorCookie = (EditorCookie) dataObject.getCookie(EditorCookie.class);

                //add reset Document Reference Listener
                addResetDocumentRefListener(editorCookie, getEditorObservableCookie());
            }

            return editorCookie;
        } catch (org.openide.loaders.DataObjectNotFoundException notFound) {
            throw new CollabException(notFound);
        } catch (java.io.IOException io) {
            throw new CollabException(io);
        }
    }

    /**
     * getEditorObservableCookie
     *
     * @throws CollabException
     * @return document
     */
    public EditorCookie.Observable getEditorObservableCookie()
    throws CollabException {
        Debug.log(
            "CollabFileHandlerSupport",
            "CollabFileHandlerSupport, " + //NoI18n
            "getEditorObservableCookie for file: " + //NoI18n
            getName()
        );

        try {
            if (editorObservableCookie == null) {
                FileObject file = getFileObject();

                // Get the FileObject
                if (file == null) {
                    return null;
                }

                // Get the DataObject
                DataObject dataObject = DataObject.find(file);

                if (dataObject == null) {
                    throw new IllegalArgumentException("No DataObject found for file \"" + getName() + "\"");
                }

                //add PropertyChangeListener				
                editorObservableCookie = (EditorCookie.Observable) dataObject.getCookie(EditorCookie.Observable.class);
            }

            return editorObservableCookie;
        } catch (org.openide.loaders.DataObjectNotFoundException notFound) {
            throw new CollabException(notFound);
        } catch (java.io.IOException io) {
            throw new CollabException(io);
        }
    }

    /**
     * addResetDocumentRefListener
     *
     * @throws CollabException
     * @return document
     */
    public void addResetDocumentRefListener(EditorCookie editorCookie, EditorCookie.Observable editorObservableCookie)
    throws CollabException {
        Debug.log(
            "CollabFileHandlerSupport",
            "CollabFileHandlerSupport, " + "addResetDocumentRefListener for file: " + getName()
        );

        //remove PropertyChangeListener
        if ((editorObservableCookie != null) && (cookieListener != null)) {
            editorObservableCookie.removePropertyChangeListener(cookieListener);
        }

        //add PropertyChangeListener
        cookieListener = new CollabEditorCookieListener(editorCookie);

        if (editorObservableCookie != null) {
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, adding cookieListener"); //NoI18n					
            editorObservableCookie.addPropertyChangeListener(cookieListener);
        } else {
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, scheduling cookieListener"); //NoI18n
            getContext().cancelTimerTask(COOKIE_LISTENER_TIMER_TASK, getName());
            getContext().addTimerTask(COOKIE_LISTENER_TIMER_TASK, getName(), cookieListener);
            getContext().scheduleAtFixedRate(
                new SwingThreadTask(cookieListener), FilesharingTimerTask.INTER_DELAY, FilesharingTimerTask.PERIOD
            );
        }
    }

    /**
     * createEventNotifer
     *
     * @return EventNotifier
     * @throws CollabException
     */
    public EventNotifier createEventNotifer() throws CollabException {
        //create a notifer first
        EventProcessor eventProcessor = FilesharingEventProcessorFactory.getDefault().createEventProcessor(
                getContext().getProcessorConfig(), getContext()
            );
        EventNotifier eventNotifier = FilesharingEventNotifierFactory.getDefault().createEventNotifier(
                getContext().getNotifierConfig(), eventProcessor
            );

        return eventNotifier;
    }

    /**
     * add DocumentListener
     *
     * @return DocumentListener
     * @throws CollabException
     */
    public abstract CollabDocumentListener addDocumentListener()
    throws CollabException;

    /**
     * register DocumentListener
     *
     * @throws CollabException
     */
    public void registerDocumentListener() throws CollabException {
        if (documentListener != null) {
            //add listener for this document
            getDocument().removeDocumentListener(documentListener);
            documentListener = null;
        }

        try {
            //add listener for this document
            documentListener = addDocumentListener();
        } catch (Throwable th) {
            Debug.log(
                "CollabFileHandlerSupport", "CollabFileHandlerSupport, " + //NoI18n
                "cannot add documentlistener "
            ); //NoI18n
            Debug.logDebugException(
                "CollabFileHandlerSupport, " + //NoI18n
                "cannot add documentlistener", //NoI18n	
                th, true
            );
        }

        if (documentListener != null) {
            //save this listener
            getContext().addCollabDocumentListener(getName(), documentListener);
        }
    }

    /**
     * getFileContentBytes
     *
     * @return byte[]
     * @throws CollabException
     */
    public byte[] getFileContentBytes() throws CollabException {
        byte[] content = null;

        try {
            InputStream inputStream = getFileObject().getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamCopier.copyStream(inputStream, outputStream);
            content = outputStream.toByteArray();
        } catch (IOException iox) {
            throw new CollabException(iox);
        }

        return content;
    }

    /**
     * clear
     *
     * @throws CollabException
     */
    public void clear() throws CollabException {
        //remove PropertyChangeListener
        if ((editorObservableCookie != null) && (cookieListener != null)) {
            editorObservableCookie.removePropertyChangeListener(cookieListener);
        }

        getContext().cancelTimerTask(COOKIE_LISTENER_TIMER_TASK, getName());
        this.removeAllRegion();
    }

    /**
     * encode
     *
     * @param   text
     * @return        Base64 and UTF-8 encoded string
     */
    public String encode(String text) throws CollabException {
        String encodedText = null;

        try {
            encodedText = encodeBase64(text.getBytes("UTF-8"));
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new CollabException(uee);
        }

        return encodedText;
    }

    /**
     * encode Base64
     *
     * @param   content (byte[])
     * @return        Base64 encoded string
     */
    public String encodeBase64(byte[] content) throws CollabException {
        return Base64.encode(content);
    }

    /**
     * decode
     *
     * @param   text
     * @return        Base64 and UTF-8 decoded string
     */
    public String decode(String text) throws CollabException {
        String decodedText = null;

        try {
            byte[] fileContents = decodeBase64(text);
            decodedText = new String(fileContents, "UTF-8");
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new CollabException(uee);
        }

        return decodedText;
    }

    /**
     * decode Base64
     *
     * @param   text
     * @return        Base64 decoded string
     */
    public byte[] decodeBase64(String text) throws CollabException {
        return Base64.decode(text);
    }

    /**
     * documentViewChanged
     *
     */
    private void documentViewChanged() {
        EditorLock editorLock = null;

        try {
            editorLock = lockEditor(getEditorCookie());
        } catch (CollabException ce) {
            //ignore
        }

        resetDocumentRef();
        unlockEditor(editorLock);
    }

    /**
     * resetDocumentRef
     *
     */
    private boolean resetDocumentRef() {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, resetDocumentRef"); //NoI18n	
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, user: " + //NoI18n
            getContext().getLoginUser()
        );

        try {
            //register doc listener if doc length changed but there is no indication of change
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, register Listeners"); //NoI18n				

            //-commenting out
            //doc listener is now added from TC listener
            //registerDocumentListener();
            resetAllLineRegionOffset();
        } catch (CollabException ce) {
            return false;
        }

        return true;
    }

    /**
     * resetAllLineRegionOffset
     *
     */
    private void resetAllLineRegionOffset() throws CollabException {
        Debug.log("CollabFileHandlerSupport", //NoI18n
            "CollabFileHandlerSupport, resetAllLineRegionOffset"
        ); //NoI18n		

        StyledDocument fileDocument = getDocument();
        int totalElementCount = fileDocument.getDefaultRootElement().getElementCount();

        for (int i = 0; i < rCtx.getLineRegionCount(); i++) {
            CollabLineRegion lineRegion = rCtx.getLineRegion(i);

            if (lineRegion != null) {
                Debug.log(
                    "CollabFileHandlerSupport_LineRegion",
                    "CollabFileHandlerSupport, reset region: " + //NoI18n
                    lineRegion.getID()
                );

                if (i < totalElementCount) {
                    javax.swing.text.Element currentElement = fileDocument.getDefaultRootElement().getElement(i);
                    int beginOffset = currentElement.getStartOffset();
                    int endOffset = currentElement.getEndOffset();
                    Debug.log(
                        "CollabFileHandlerSupport_LineRegion",
                        "CollabFileHandlerSupport, beginOffset: " + //NoI18n
                        beginOffset
                    );
                    Debug.log(
                        "CollabFileHandlerSupport_LineRegion",
                        "CollabFileHandlerSupport, endOffset: " + //NoI18n
                        endOffset
                    );

                    try {
                        lineRegion.setBeginOffset(beginOffset);
                        lineRegion.setEndOffset(endOffset);
                    } catch (java.lang.IllegalArgumentException ia) {
                        //ignore
                    }
                }

                lineRegion.setDocument(fileDocument);
            }
        }
    }

    /**
     * resetAllLineRegionOffset
     *
     * @param delay
     */
    public void resetAllLineRegionOffset(long delay) {
        Debug.log(
            "CollabFileHandlerSupport", //NoI18n
            "CollabFileHandlerSupport, resetAllLineRegionOffset " + //NoI18n
            " after delay: " + delay + " millis"
        ); //NoI18n

        TimerTask resetOffsetTask = new TimerTask() {
                public void run() {
                    try {
                        resetAllLineRegionOffset();
                    } catch (CollabException ce) {
                        Debug.log(
                            "CollabFileHandlerSupport", //NoI18n
                            "CollabFileHandlerSupport, cannot resetAllLineRegionOffset" + //NoI18n
                            getName()
                        );
                    }
                }
            };

        getContext().schedule(resetOffsetTask, delay);
    }

    /**
     * disableUnlockTimer
     *
     * @param        status
     */
    protected void disableUnlockTimer(boolean status) {
        this.disableUnlockTimer = status;
    }

    /**
     * isDisableUnlockTimer
     *
     * @return        disableUnlockTimer
     */
    private boolean isDisableUnlockTimer() {
        return this.disableUnlockTimer;
    }

    /**
     * getUserStyle
     *
     * @return
     */
    public int getUserStyle(String user) {
        Integer style = (Integer) userStyles.get(user);

        if (style == null) {
            style = getContext().getUserStyle(user);
            userStyles.put(user, style);
        }

        return style.intValue();
    }

    /**
     * getJoinTimeStamp
     *
     * @return
     */
    public long getJoinTimeStamp() {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, joinTimeStamp before" + joinTimeStamp);

        if (joinTimeStamp == -1) {
            joinTimeStamp = getContext().getJoinTimeStamp();
        }

        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, joinTimeStamp after" + joinTimeStamp);

        return joinTimeStamp;
    }

    /**
     * getQueue
     *
     * @return queue
     */
    public RegionQueue getQueue() {
        return this.queue;
    }

    /**
     * toString
     *
     * @return fileName
     */
    public String toString() {
        return getName();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    //////////////////////////////////////////////////////////////////////////// 
    class CollabEditorCookieListener extends TimerTask implements PropertyChangeListener {
        ////////////////////////////////////////////////////////////////////////////
        // Instance variables
        ////////////////////////////////////////////////////////////////////////////

        /* holds cookie */
        private EditorCookie cookie = null;

        /* done Reset Document */
        private boolean doneResetDoc = false;

        /**
         * constructor
         *
         */
        public CollabEditorCookieListener(EditorCookie cookie)
        throws CollabException {
            super();
            this.cookie = cookie;
        }

        ////////////////////////////////////////////////////////////////////////////
        // methods
        ////////////////////////////////////////////////////////////////////////////

        /**
         *
         *
         */
        public void run() {
            if ((cookie != null) && !cookie.isModified()) {
                if (!doneResetDoc) {
                    Debug.log(
                        "CollabFileHandlerSupport", "CollabFileHandlerSupport, \n\nCollabEditorCookieListener::run"
                    );
                    Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, calling documentViewChange: ");

                    boolean isValid = false;

                    if (isValid()) {
                        CollabFileHandlerSupport.this.documentViewChanged();
                    }

                    doneResetDoc = true;
                }
            } else {
                doneResetDoc = false;
            }
        }

        /**
         * propertyChange
         *
         * @param        event
         */
        public void propertyChange(PropertyChangeEvent event) {
            if (EditorCookie.Observable.PROP_OPENED_PANES.equals(event.getPropertyName())) {
                if (isValid()) {
                    Debug.log(
                        "CollabFileHandlerSupport",
                        "CollabFileHandlerSupport, propertyChange: " + event.getPropertyName()
                    ); //NoI18n	
                    CollabFileHandlerSupport.this.documentViewChanged();
                }
            }
        }
    }

    class CollabRegionContext extends Object {
        ////////////////////////////////////////////////////////////////////////////
        // Instance variables
        ////////////////////////////////////////////////////////////////////////////

        /* repository to manage regions */
        protected HashMap regions = new HashMap();

        /* mapping of user to regions */
        protected HashMap collabUserRegionMap = new HashMap();

        /* mapping of collabregion to lineregion */
        protected HashMap collabRegionLineRegionMap = new HashMap();

        /**
         * constructor
         *
         */
        public CollabRegionContext() {
            super();
        }

        ////////////////////////////////////////////////////////////////////////////
        // methods
        ////////////////////////////////////////////////////////////////////////////

        /**
         * adds a new region into repository
         *
         * @param   user                                        the user who created the region
         * @param   regionName                                the regionName
         * @param   region                                        CollabRegion
         */
        public synchronized void addRegion(String user, String regionName, CollabRegion region) {
            if (!getRegion().containsKey(regionName)) {
                addRegion(regionName, region);

                if (!getUserRegion().containsKey(user)) {
                    Vector tmpList = new Vector();
                    tmpList.add(regionName);
                    getUserRegion().put(user, tmpList);
                } else {
                    ((Vector) getUserRegion().get(user)).add(regionName);
                }
            } else {
                throw new IllegalArgumentException("region: " + regionName + " already exist");
            }
        }

        /**
         * adds a new region into repository
         *
         * @param   regionName                                the regionName
         * @param   region                                        CollabRegion
         */
        public void addRegion(String regionName, CollabRegion region) {
            printRegionInfo(region, "Adding new region, region info for user: " + getContext().getLoginUser());

            if (!getRegion().containsKey(regionName)) {
                getRegion().put(regionName, region);
            } else {
                throw new IllegalArgumentException("region: " + regionName + " already exist");
            }
        }

        /**
         * adds a new lineregion into repository
         *
         * @param   regionName                                the regionName
         * @param   region                                        CollabRegion
         */
        public synchronized void addLineRegion(String regionName, CollabRegion region) {
            addRegion(getLineRegionKey(), regionName, region);
        }

        /**
         * removes all region from the repository
         *
         * @throws CollabException
         */
        public synchronized void removeAllRegion() throws CollabException {
            removeAllRegion(false);
        }

        /**
         * removes all region from the repository
         *
         * @throws CollabException
         */
        public synchronized void removeAllRegion(boolean retainLineRegions)
        throws CollabException {
            Iterator it = getUserRegion().keySet().iterator();

            while (it.hasNext()) {
                String user = (String) it.next();

                if (retainLineRegions && (user != null) && user.equals(getLineRegionKey())) {
                    continue;
                }

                Vector regionNames = (Vector) getUserRegion(user);

                if (regionNames != null) {
                    for (int i = 0; i < regionNames.size(); i++) {
                        removeRegion((String) regionNames.get(i));
                    }

                    regionNames.clear();
                }
            }

            if (!retainLineRegions) {
                getUserRegion().clear();
                getRegion().clear();
            }
        }

        /**
         * removes a region from the repository
         *
         * @param   user                                        the user who created the region
         * @param   regionName                                the regionName
         * @throws CollabException
         */
        public synchronized boolean removeRegion(String user, String regionName)
        throws CollabException {
            if (getUserRegion().containsKey(user)) {
                Vector regionNames = (Vector) getUserRegion().get(user);

                if (regionNames != null) {
                    regionNames.remove(regionName);

                    if (regionNames.isEmpty()) {
                        getUserRegion().remove(user);
                    }
                }
            } else {
                return false;
            }

            return removeRegion(regionName);
        }

        /**
         * remove a region
         *
         * @param regionName
         * @throws CollabException
         */
        public boolean removeRegion(String regionName)
        throws CollabException {
            if (getRegion().containsKey(regionName)) {
                CollabRegion sect = (CollabRegion) getRegion().get(regionName);
                printRegionInfo(sect, "Removing region, region info for user: " + getContext().getLoginUser());
                removeSection(sect);
                getRegion().remove(regionName);

                if (getRegion().containsKey(regionName)) {
                    return false;
                }
            } else {
                return false;
            }

            return true;
        }

        /**
         * removes a line region from the repository
         *
         * @param   regionName                                the regionName
         * @throws CollabException
         */
        public synchronized boolean removeLineRegion(String regionName)
        throws CollabException {
            return removeRegion(getLineRegionKey(), regionName);
        }

        /**
         * addUserRegion
         *
         * @param   user                                        the user who created the region
         * @param        regionList                                the list of user regions
         */
        public void replaceUserRegion(String user, Object regionList) {
            if (collabUserRegionMap.containsKey(user)) {
                collabUserRegionMap.remove(user);
            }

            collabUserRegionMap.put(user, regionList);
        }

        /**
         * get userRegionMap
         *
         * @param   user                                        the user who created the region
         * @return        regionList                                the list of user regions
         */
        public HashMap getUserRegion() {
            return collabUserRegionMap;
        }

        /**
         * get all region belonging to user from the repository
         *
         * @param   user                                        the user who created the region
         * @return        regionList                                the list of user regions
         */
        public Object getUserRegion(String user) {
            Debug.log(
                "CollabFileHandlerSupport_getLineRegion",
                "CollabFileHandlerSupport, In getUserRegion: " + collabUserRegionMap
            ); //NoI18n

            return collabUserRegionMap.get(user);
        }

        /**
         * get a region from the repository
         *
         * @param   regionName                                the regionName
         * @return        region                                        CollabRegion
         */
        public CollabRegion getRegion(String regionName) {
            if (!getRegion().containsKey(regionName)) {
                return null;
            }

            return (CollabRegion) getRegion().get(regionName);
        }

        /**
         * get a region from the repository
         *
         * @param   regionName                                the regionName
         * @return        CollabLineRegion
         */
        public CollabLineRegion getLineRegion(String regionName) {
            CollabLineRegion lineRegion = (CollabLineRegion) getRegion(regionName);

            return lineRegion;
        }

        /**
         * get a region from the repository
         *
         * @param   regionIndex
         * @return        CollabLineRegion
         */
        public CollabLineRegion getLineRegion(int regionIndex) {
            Debug.log("CollabFileHandlerSupport_getLineRegion", "CollabFileHandlerSupport, In getLineRegion"); //NoI18n

            Vector userRegions = (Vector) getUserRegion(getLineRegionKey());

            if ((userRegions == null) || (userRegions.size() == 0) || (regionIndex >= userRegions.size())) {
                Debug.log(
                    "CollabFileHandlerSupport_getLineRegion",
                    "CollabFileHandlerSupport, getLineRegion userRegions null: "
                ); //NoI18n

                return null;
            }

            String regionName = (String) userRegions.get(regionIndex);
            Debug.log(
                "CollabFileHandlerSupport_getLineRegion",
                "CollabFileHandlerSupport, getLineRegion regionName: " + //NoI18n
                regionName
            );

            CollabLineRegion lineRegion = (CollabLineRegion) getRegion(regionName);

            return lineRegion;
        }

        /**
         * get a region from the repository
         *
         * @param   regionIndex
         * @return        Vector containing CollabLineRegion's
         */
        public Vector getLineRegion() {
            return (Vector) getUserRegion(getLineRegionKey());
        }

        /**
         * get lineregion count
         *
         * @return        count of CollabLineRegion's
         */
        public int getLineRegionCount() {
            if (getLineRegion() != null) {
                return getLineRegion().size();
            } else {
                return 0;
            }
        }

        /**
         * get region map
         *
         * @return        region map
         */
        public HashMap getRegion() {
            return this.regions;
        }

        /**
         * test if the region exist in the repository
         *
         * @param   regionName                                the regionName
         * @return        true/false                                true if region exist
         */
        public boolean regionExist(String regionName) {
            return getRegion().containsKey(regionName);
        }

        /**
         * getLineRegions
         *
         * @param   regionName
         * @return        lineRegions
         */
        public List getLineRegions(String regionName) {
            return (List) collabRegionLineRegionMap.get(regionName);
        }

        /**
         * resetLineRegion
         *
         * @param   regionName
         * @return        newLines
         */
        public void resetLineRegion(String regionName, List newLines) {
            collabRegionLineRegionMap.remove(regionName);
            collabRegionLineRegionMap.put(regionName, newLines);
        }

        /**
         * return a region that contains the offset, no correction needed
         *
         * @param beginOffset
         * @param length
         * @return
         */
        public CollabLineRegion[] getContainingLineRegion(int beginOffset, int length)
        throws CollabException {
            int endOffset = beginOffset + length;
            List lineRegionList = new ArrayList();
            Vector userRegions = (Vector) getLineRegion();

            if ((userRegions == null) || (userRegions.size() == 0)) {
                return null;
            }

            Debug.log(
                "CollabFileHandlerSupport",
                "CollabFileHandlerSupport, total # of regions: " + //NoI18n
                userRegions.size()
            );
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, beginOffset: " + beginOffset); //NoI18n
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, endOffset: " + endOffset); //NoI18n

            StyledDocument fileDocument = getDocument();
            int beginLine = fileDocument.getDefaultRootElement().getElementIndex(beginOffset);
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, beginLine: " + beginLine); //NoI18n			

            int endLine = fileDocument.getDefaultRootElement().getElementIndex(endOffset - 1);
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, endLine: " + endLine); //NoI18n

            for (int lineIndex = beginLine; lineIndex <= endLine; lineIndex++) {
                if (lineIndex == userRegions.size()) {
                    break;
                }

                String regionName = (String) userRegions.get(lineIndex);
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, regionName>: " + regionName); //NoI18n

                CollabLineRegion lineRegion = (CollabLineRegion) getRegion(regionName);
                Debug.log(
                    "CollabFileHandlerSupport", "CollabFileHandlerSupport, regionName>>: " + //NoI18n
                    lineRegion.getID()
                );
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, check begin: " + //NoI18n
                    lineRegion.getBeginOffset()
                );
                Debug.log(
                    "CollabFileHandlerSupport", "CollabFileHandlerSupport, end: " + //NoI18n
                    lineRegion.getEndOffset()
                );
                lineRegionList.add(lineRegion);
            }

            return (CollabLineRegion[]) lineRegionList.toArray(new CollabLineRegion[0]);
        }

        /**
         * return a region that contains the offset, no correction needed
         *
         * @param beginOffset
         * @param length
         * @return
         */
        public Object getContainingRegion(int offset, int length) {
            return getContainingRegion(offset, length, false);
        }

        /**
         * return a region that contains the offset
         *
         * @param user
         * @param beginOffset
         * @param length
         * @param applyCorrection
         * @return
         */
        protected CollabRegion getContainingRegion(int offset, int length, boolean applyCorrection) {
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, getContainingRegion"); //NoI18n

            int beginOffset = offset;
            int endOffset = offset + length;
            Iterator it = getUserRegion().keySet().iterator();

            while (it.hasNext()) {
                String user = (String) it.next();

                if ((user != null) && user.equals(getLineRegionKey())) {
                    continue;
                }

                Vector userRegions = (Vector) getUserRegion(user);

                if ((userRegions == null) || (userRegions.size() == 0)) {
                    return null;
                }

                for (int i = 0; i < userRegions.size(); i++) {
                    String regionName = (String) userRegions.get(i);
                    CollabRegion sect = getRegion(regionName);

                    if (sect == null) {
                        return null;
                    }

                    CollabRegion match = getContainingRegion(sect, beginOffset, endOffset, applyCorrection);

                    if (match != null) {
                        return match;
                    }
                }
            }

            return null;
        }

        /**
         * returns region if region contains the beginOffset & endOffset
         *
         * @param sect
         * @param beginOffset
         * @param endOffset
         * @param applyCorrection
         * @return region
         */
        protected CollabRegion getContainingRegion(
            CollabRegion sect, int beginOffset, int endOffset, boolean applyCorrection
        ) {
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, check containingRegion"); //NoI18n
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, beginOffset: " + beginOffset); //NoI18n
            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, endOffset: " + endOffset); //NoI18n

            if (sect.isGuarded()) {
                CollabRegionSupport.SimpleSection simpleSection = (CollabRegionSupport.SimpleSection) sect.getGuard();

                int posBegin = simpleSection.getBegin();
                int posEnd = simpleSection.getPositionAfter() - 1;
                Debug.log(
                    "CollabFileHandlerSupport", "CollabFileHandlerSupport, section: " + //NoI18n
                    simpleSection.getName()
                );
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, posBegin: " + posBegin); //NoI18n
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, posEnd: " + posEnd); //NoI18n

                if (
                    ((beginOffset >= posBegin) && (beginOffset <= posEnd)) ||
                        ((endOffset >= posBegin) && (endOffset <= posEnd))
                ) {
                    return sect;
                }
            } else if (sect instanceof CollabRegion && !(sect instanceof CollabLineRegion)) {
                CollabRegion region = ((CollabRegion) sect);
                int posBegin = region.getBeginOffset();
                int posEnd = region.getEndOffset();
                Debug.log(
                    "CollabFileHandlerSupport", "CollabFileHandlerSupport, CollabRegion: " + //NoI18n
                    region.getID()
                );
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, posBegin: " + posBegin); //NoI18n
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, posEnd: " + posEnd); //NoI18n

                //correction
                if (applyCorrection) {
                    posEnd -= 1;
                }

                if (
                    ((beginOffset >= posBegin) && (beginOffset <= posEnd)) ||
                        ((endOffset >= posBegin) && (endOffset <= posEnd))
                ) {
                    return region;
                }
            }

            return null;
        }

        /**
         * returns adjacent region, meeting the adjacency length criteria
         *
         * @param offset
         * @param length
         * @param adjanceLength
         * @return
         */
        protected CollabRegion getContainingRegion(int offset, int length, int adjanceLength) {
            CollabRegion[] objArr = (CollabRegion[]) getRegion().values().toArray(new CollabRegion[0]);

            for (int i = 0; i < objArr.length; i++) {
                CollabRegion sect = objArr[i];

                if (!(sect instanceof CollabLineRegion)) {
                    CollabRegion region = ((CollabRegion) sect);
                    int posBegin = region.getBeginOffset();
                    int posEnd = region.getEndOffset();

                    if ((offset >= posBegin) && (offset <= posEnd)) {
                        return region;
                    }
                }
            }

            return null;
        }

        /**
         * update regions whose beginOffset > offset
         *
         * @param offset
         * @param length
         * @param insert
         * @throws CollabException
         */
        public void updateAllRegionBeginOffset(int offset, int length, boolean insert)
        throws CollabException {
            CollabRegion[] objArr = (CollabRegion[]) getRegion().values().toArray(new CollabRegion[0]);

            for (int i = 0; i < objArr.length; i++) {
                CollabRegion sect = objArr[i];

                if (sect.isGuarded()) {
                    CollabRegionSupport.SimpleSection simpleSection = (CollabRegionSupport.SimpleSection) sect.getGuard();

                    int posBegin = simpleSection.getBegin();

                    if (offset < posBegin) {
                        simpleSection.shiftSection(length);
                    }
                }
            }
        }

        /**
         * creates a CollabRegion, a super-class for all regions
         *
         * @param regionName the regionName
         * @param beginOffset the beginOffset
         * @param endOffset the endOffset
         * @throws CollabException
         * @return
         */
        public CollabRegion createLineRegion(String regionName, int beginOffset, int endOffset)
        throws CollabException {
            return new CollabLineRegion(getDocument(), regionName, beginOffset, endOffset);
        }

        /**
         * unlock all user regions(ready for removal)
         *
         */
        public void unlockAllUserRegions() {
            Vector userRegions = (Vector) getUserRegion(getContext().getLoginUser());

            if ((userRegions == null) || (userRegions.size() == 0)) {
                return;
            }

            for (int i = 0; i < userRegions.size(); i++) {
                String regionName = (String) userRegions.get(i);
                CollabRegion region = (CollabRegion) getRegion(regionName);
                region.setReadyToUnlock();

                UnlockRegionEvent unlockRegionEvent = new UnlockRegionEvent(
                        new UnlockRegionContext(UnlockRegionEvent.getEventID(), getFileHandler(), getFileGroupName())
                    );
                getContext().getChannelEventNotifier().notify(unlockRegionEvent);
            }
        }

        /**
         * doUpdateRegion
         *
         * @param   messageOriginator   the sender of this message
         * @param sect
         * @param text
         * @throws CollabException
         */
        public boolean doUpdateRegion(String messageOriginator, CollabRegion sect, String text)
        throws CollabException {
            Debug.out.println("sect.isGuarded(): " + sect.isGuarded());

            if (!sect.isGuarded()) {
                return false;
            }

            boolean updateRegionTextStatus = false;

            synchronized (getDocument()) {
                setSkipUpdate(true);

                if (currentUpdatedRegion != null) {
                    //reset region change before update
                    currentUpdatedRegion = null;
                }

                CollabRegionSupport.SimpleSection simpleSection = (CollabRegionSupport.SimpleSection) sect.getGuard();

                if (simpleSection != null) {
                    int regionBegin = simpleSection.getBegin();
                    int diff = text.length() - simpleSection.getText().length();

                    if (documentListener != null) {
                        ((FilesharingDocumentListener) documentListener).setSkipUpdate(true);
                    }

                    simpleSection.setText(text);

                    if (documentListener != null) {
                        ((FilesharingDocumentListener) documentListener).setSkipUpdate(false);
                    }

                    //update all other regio begin offset
                    updateAllRegionBeginOffset(regionBegin, diff, true);
                }

                if (currentUpdatedRegion != null) {
                    currentUpdatedRegion.updateStatusChanged(true);
                }

                setCurrentState(FilesharingContext.STATE_UNKNOWN);
                setSkipUpdate(false);
            }

            return true;
        }

        /**
         * doUpdateLineRegion
         *
         * @param   regionName                                the regionName
         * @param        beginOffset
         * @param        endOffset
         * @param   unlockRegionData                the intial unlock-region-data Node
         * @throws CollabException
         */
        public List doUpdateLineRegion(
            String regionName, int beginOffset, int endOffset, UnlockRegionData unlockRegionData,
            String[] newLineRegionNames
        ) throws CollabException {
            List lineRegions = getLineRegions(regionName);
            List newLineRegionList = new ArrayList();
            StyledDocument fileDocument = getDocument();

            if ((lineRegions != null) && (lineRegions.size() > 0)) {
                Vector currentLineRegion = getLineRegion();
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, currentLineRegion before update: " + //NoI18n
                    currentLineRegion
                );

                int beginLine = fileDocument.getDefaultRootElement().getElementIndex(beginOffset);
                int endLine = fileDocument.getDefaultRootElement().getElementIndex(endOffset - 1);
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, beginLine: " + beginLine); //NoI18n			
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, endLine: " + endLine); //NoI18n

                //save line region that was the begin
                CollabLineRegion firstLineRegion = (CollabLineRegion) lineRegions.get(0);
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, first line regionName: " + //NoI18n
                    firstLineRegion.getID()
                );

                int saveIndex = currentLineRegion.indexOf(firstLineRegion.getID());
                Debug.log(
                    "CollabFileHandlerSupport", "CollabFileHandlerSupport, first line saveIndex: " + //NoI18n 
                    saveIndex
                );

                if (saveIndex != -1) {
                    beginLine = saveIndex;
                    Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, beginLine: " + //NoI18n 
                        beginLine
                    );
                }

                if ((newLineRegionNames != null) && (newLineRegionNames.length > 0)) {
                    endLine = beginLine + (newLineRegionNames.length - 1);
                }

                //remove old line regions
                for (int i = 0; i < lineRegions.size(); i++) {
                    CollabLineRegion lineRegion = (CollabLineRegion) lineRegions.get(i);

                    if (lineRegion != null) {
                        Debug.log(
                            "CollabFileHandlerSupport",
                            "CollabFileHandlerSupport, regionName: " + //NoI18n
                            lineRegion.getID()
                        );

                        //remove annotation first
                        lineRegion.removeAnnotation();

                        removeLineRegion(lineRegion.getID());
                    }
                }

                int count = 0;
                String user = getContext().getLoginUser();

                for (int lineIndex = beginLine; lineIndex <= endLine; lineIndex++) {
                    javax.swing.text.Element currentElement = fileDocument.getDefaultRootElement().getElement(
                            lineIndex
                        );
                    String newLineRegionName = null;

                    if ((newLineRegionNames == null) || (newLineRegionNames.length == 0)) {
                        newLineRegionName = "RA[" + user + "]_" + getJoinTimeStamp() + "_" + newLineCount++; //NoI18n
                    } else {
                        if (count == newLineRegionNames.length) {
                            break;
                        }

                        newLineRegionName = newLineRegionNames[count++];
                    }

                    Debug.log(
                        "CollabFileHandlerSupport",
                        "CollabFileHandlerSupport, newLineRegionName: " + //NoI18n
                        newLineRegionName
                    );

                    int newLineBeginOffset = currentElement.getStartOffset();
                    int newLineEndOffset = currentElement.getEndOffset();

                    CollabRegion newLineRegion = createLineRegion(
                            newLineRegionName, newLineBeginOffset, newLineEndOffset
                        );

                    if (newLineRegion != null) {
                        newLineRegionList.add(newLineRegion);
                        Debug.log(
                            "CollabFileHandlerSupport",
                            "CollabFileHandlerSupport, currentLineRegion size: " + //NoI18n
                            currentLineRegion.size()
                        );
                        Debug.log(
                            "CollabFileHandlerSupport",
                            "CollabFileHandlerSupport, insert element " + //NoI18n 
                            newLineRegionName + " at: " + saveIndex
                        ); //NoI18n						
                        currentLineRegion.insertElementAt(newLineRegionName, saveIndex++);
                        addRegion(newLineRegionName, newLineRegion);
                    }
                }

                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, new list: " + getLineRegion()); //NoI18n		
                resetLineRegion(regionName, newLineRegionList);
                replaceUserRegion(getLineRegionKey(), currentLineRegion);
                Debug.log(
                    "CollabFileHandlerSupport",
                    "CollabFileHandlerSupport, currentLineRegion after update: " + //NoI18n
                    currentLineRegion
                );
            }

            return newLineRegionList;
        }

        /**
         * test if the region exist in the repository
         *
         * @param offset
         * @param length
         * @param length of adjacency of change to this region
         * @return        true/false                                true if region exist
         */
        public boolean regionExist(int offset, int length, int adjacency)
        throws CollabException {
            CollabRegion region = getContainingRegion(offset, length, adjacency);

            if ((region != null) && region instanceof CollabRegion && !(region instanceof CollabLineRegion)) {
                CollabRegion cRegion = (CollabRegion) region;
                currentUpdatedRegion = cRegion;

                if (!skipUpdate) {
                    //reset annotation
                    resetAnnotation(cRegion);

                    //update status changed
                    cRegion.updateStatusChanged(true);
                }

                return true;
            }

            return false;
        }

        /**
         * update region content
         *
         * @param messageOriginator
         * @param simpleSection
         * @param newBegin
         * @param length
         * @param text
         * @throws CollabException
         */
        public boolean updateRegionText(String messageOriginator, CollabRegion sect, String text)
        throws CollabException {
            CollabRegionSupport.SimpleSection simpleSection = (CollabRegionSupport.SimpleSection) sect.getGuard();
            int regionBegin = simpleSection.getBegin();
            int diff = text.length() - simpleSection.getText().length();

            if (documentListener != null) {
                ((FilesharingDocumentListener) documentListener).setSkipUpdate(true);
            }

            simpleSection.setText(text);

            if (documentListener != null) {
                ((FilesharingDocumentListener) documentListener).setSkipUpdate(false);
            }

            //update all other regio begin offset
            updateAllRegionBeginOffset(regionBegin, diff, true);

            return true;
        }
    }
}
