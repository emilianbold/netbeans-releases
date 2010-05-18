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
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import com.sun.collablet.CollabException;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.netbeans.modules.collab.channel.filesharing.FilesharingCollablet;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.windows.TopComponent;

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
import org.netbeans.modules.collab.channel.filesharing.context.LockRegionContext;
import org.netbeans.modules.collab.channel.filesharing.context.UnlockRegionContext;
import org.netbeans.modules.collab.channel.filesharing.eventhandler.LockRegionManager;
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
public abstract class CollabFileHandlerSupport extends Object implements FilesharingConstants, KeyListener, MouseListener {
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
    private EditorLock createRegionEditorLock=null;

    /* Lock Region Manager Map */
    private HashMap lrmanagers = new HashMap();

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

            if (file == null) {
                firstTimeSend = false;
                Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, creating FileObject"); //NoI18n				

                //create or update if exist
                createFileObject(fileContent);

                //find initial guarded sections once
                findInitialGuardedSections();
            } else {
                String content = new String(fileContent);
                updateDocument(content);
            }
        } catch (IllegalArgumentException iargs) {
            inReceiveSendFile = false;
            throw new CollabException(iargs);
        }

        //init listeners
        getEditorCookie();

        //set line region user
        setLineRegionKey(getContext().getLoginUser() + "_LINEREGION_USER");

        List newLineRegionList = new ArrayList();
        RegionInfo[] lineRegions = findLineRegion(sendFileData);

		Debug.log("CollabFileHandlerSupport", "CFHS, RCV create LineRegion: ");
        for (int i = 0; i < lineRegions.length; i++) {
            RegionInfo region = lineRegions[i];
            String regionName = region.getID();
            int beginOffset = region.getbegin();
            int endOffset = region.getend();
			
			try {				
				Debug.log("CollabFileHandlerSupport", 
					regionName+", begin: " + beginOffset+" end: "+endOffset+
					getDocument().getText(beginOffset, endOffset-beginOffset)); //NoI18n	
				Debug.log("CollabFileHandlerSupport", 
						" begin index: " + getDocument().getDefaultRootElement().getElementIndex(beginOffset)+
						" end index: "+ getDocument().getDefaultRootElement().getElementIndex(endOffset)); //NoI18n				
			} catch (CollabException ex) {
				ex.printStackTrace();
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			} //NoI18n			

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
                    int style = -1; //history annotation
                    String annotationMessage = NbBundle.getMessage(
                            CollabFileHandlerSupport.class, "MSG_CollabFileHandlerSupport_HistoryAnnotation", // NOI18N
                            getContext().getPrincipal(lineLockUser).getDisplayName()
                        );
                    addAnnotation(lineRegion, style, annotationMessage);
                }
            }
        }

        resetAllLineRegionOffset();

        inReceiveSendFile = false;
    }

    /**
     * updateDocument
     *
     */
    protected void updateDocument(String content) throws CollabException {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, updateDocument"); //NoI18n
        EditorLock editorLock = null;

        try {
            synchronized (getDocumentLock()) {
                editorLock = lockEditor();
                getDocument().remove(0, getDocument().getLength());
                getDocument().insertString(0, content, null);
                unlockEditor(editorLock);
            }
        } catch (javax.swing.text.BadLocationException e) {
            //throw new CollabException(e);
            Debug.logDebugException("CollabFileHandlerSupport, " +//NoI18n
                    "cannot update document", e, true);//NoI18n        
        } finally {
            getContext().setSkipSendFile(getName(), false);
            unlockEditor(editorLock);
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
                int endOffset = currentElement.getEndOffset()-1;
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
        EditorLock editorLock = null;

        try {
            String fullPath = lockRegionData.getFileName();
            StyledDocument fileDocument = getDocument();

            synchronized(getDocumentLock()) {
                editorLock = lockEditor(); 

                int documentLength = fileDocument.getLength();
                RegionInfo region = getLockRegion(lockRegionData);
                String regionName = region.getID();
                int beginOffset = region.getbegin();
                int endOffset = region.getend();
                int length = endOffset - beginOffset;

                if (endOffset > documentLength) {
                    endOffset = documentLength;
                }

                CollabLineRegion[] cLineRegions=getCollabLineRegion(lockRegionData);
                Debug.log("CollabFileHandlerSupport","CollabFileHandlerSupport, # of lineRegions: " + //NoI18n
                    cLineRegions.length);
                if(cLineRegions.length>0) {
                    //identify lineregion
                    List cLineRegionList = new ArrayList();
                    cLineRegionList.addAll(Arrays.asList(cLineRegions));
                    rCtx.resetLineRegion(regionName, cLineRegionList, false); //do not remove old lines

                    String firstLineName = cLineRegions[0].getID();
                    String lastLineName = cLineRegions[cLineRegions.length-1].getID();
                    Debug.log("CollabFileHandlerSupport","CollabFileHandlerSupport, firstLineName: " + //NoI18n
                            firstLineName);
                    Debug.log("CollabFileHandlerSupport","CollabFileHandlerSupport, lastLineName: " + //NoI18n
                            lastLineName);
                    CollabLineRegion firstLineRegion = (CollabLineRegion)rCtx.getLineRegion(firstLineName);
                    CollabLineRegion lastLineRegion = (CollabLineRegion)rCtx.getLineRegion(lastLineName);

                    if(firstLineRegion==null || lastLineRegion==null) return;

                    beginOffset = firstLineRegion.getBeginOffset();
                    endOffset = lastLineRegion.getEndOffset();
                    Debug.log("CollabFileHandlerSupport","CollabFileHandlerSupport, beginOffset: " + //NoI18n
                            beginOffset);
                    Debug.log("CollabFileHandlerSupport","CollabFileHandlerSupport, endOffset: " + //NoI18n
                            endOffset);
                    
                    CollabRegion simpleSection = createSimpleSection(beginOffset, endOffset, regionName);
                    /*if(simpleSection==null) { //check with correction: cannot create section 
                        simpleSection = createSimpleSection(beginOffset+1, endOffset, regionName); 

                        if (simpleSection == null) //check with correction
                         { //cannot create section
                            simpleSection = createSimpleSection(beginOffset, endOffset - 1, regionName);

                            if (simpleSection == null) //check with correction
                             { //cannot create section
                                simpleSection = createSimpleSection(beginOffset+1, endOffset - 1, regionName);

                                if (simpleSection == null) //check with correction
                                 { //cannot create section
                                    unlockEditor(editorLock);
                                    return;
                                }
                            }
                        }
                    }*/
					if (simpleSection == null) //check with correction
					 { //cannot create section
						Debug.log("CollabFileHandlerSupport","CFHS, " +
                            "create section null for: "+regionName); //NoI18n
						unlockEditor(editorLock);
						return;
					}					

                    Debug.log("CollabFileHandlerSupport","CFHS, receivedLock " +
                            "for text : [" + simpleSection.getContent()+"]"); //NoI18n
                    rCtx.addRegion(messageOriginator, regionName, simpleSection);
                    
                    //Set parent
                    for(int i=0;i<cLineRegionList.size();i++) {
                        CollabLineRegion liner=(CollabLineRegion)cLineRegionList.get(i);
                        if(liner!=null) {
                            Debug.log("CollabFileHandlerSupport","CFHS, setAssigned " +
                                    "true for: " + liner.getID()); //NoI18n
                            liner.setAssigned(simpleSection, true);
                        }
                    }
                    
                    //add annotation
                    String annotationMessage=NbBundle.getMessage( 
                        CollabFileHandlerSupport.class, 
                        "MSG_CollabFileHandlerSupport_EditingAnnotation", // NOI18N 
                        getContext().getPrincipal(messageOriginator).getDisplayName());
                    int style = getUserStyle(messageOriginator);
                    addLineRegionAnnotation((CollabLineRegion[])
                        cLineRegionList.toArray(new CollabLineRegion[0]),
                        style, annotationMessage);
                 }
            }
        } catch (IllegalArgumentException iargs) {
            throw new CollabException(iargs);
        } finally {
            unlockEditor(editorLock);
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
        region.addAnnotation(FileshareUtil.getDataObject(getFileObject()),
                (CollabFileHandler) this, style, annotationMessage);
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
        String regionName = null;

        //copy contents from message to files; add files to CollabFileSystem
        try {
            String fullPath = unlockRegionData.getFileName();
            StyledDocument fileDocument = getDocument();

            int documentLength = fileDocument.getLength();
            RegionInfo region = getUnlockRegion(unlockRegionData);

            regionName = region.getID();
            Debug.log("CollabFileHandlerSupport","CFHS, releaseLock regionName: "+regionName); //NoI18n
            int beginOffset = region.getbegin();
            int endOffset = region.getend();

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
                    "CFHS, received unlock for " + //NoI18n
                    "region: "+regionName+" with update : [" + text +"]");//NoI18n

                synchronized (getDocumentLock()) {
                    editorLock = lockEditor();

                    boolean originallyModified = isDocumentModified();

                    //remove previous annotation before update
                    CollabRegion cregion = getRegion(regionName);
                    cregion.removeAnnotation();
                    beginOffset = sect.getBeginOffset();
                    Debug.log("CollabFileHandlerSupport","CFHS, region before " +
                            "update: [" + cregion.getContent() +"]"); //NoI18n
                    boolean status = doUpdateRegion(messageOriginator, sect, text);
                    Debug.log("CollabFileHandlerSupport","CFHS, region after " +
                            "update: [" + cregion.getContent() +"]"); //NoI18n
                    endOffset = sect.getEndOffset();

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
                }
            }
        } catch (IllegalArgumentException iargs) {
            throw new CollabException(iargs);
        } finally {
            try {
                if(regionName!=null) {
                    CollabRegion r=getRegion(regionName);
                    if(r!=null) r.setValid(false);
                    removeRegion(messageOriginator, regionName);
                }
            } catch(Exception e) {
                Debug.logDebugException("CFHS, exception removing region: "+
                        regionName, e, true);
            }
            unlockEditor(editorLock);
            inReceiveMessageUnlock=false;
        }
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
     * removes region 
     * 
     * @param user 
     * @param regionName 
     * 
     * @throws CollabException 
     */      
    public void removeRegion(String user, String regionName)  throws CollabException {
        rCtx.removeRegion(user, regionName);
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
     * findTopComponent
     *
     * @return TopComponent
     */
    public TopComponent findTopComponent() {
        String[] paths=getName().split("/");
        String tcName=paths[paths.length-1];
        TopComponent.Registry reg = TopComponent.getRegistry();
        Set opened=reg.getOpened();
        for (Iterator it = opened.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            String displayName=tc.getDisplayName();
            if (displayName!=null && displayName.indexOf(tcName) != -1 && 
                    displayName.length()>(tcName.length()+8)) {//*[Shared] len>8
                return tc;
            }
        }
        return null;
    }

    /**
     * lockFileForCreateRegion
     *
     */
    public void lockFileForCreateRegion() {
        setCurrentState(FilesharingContext.STATE_LOCK);
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                try {
                    createRegionEditorLock = lockEditor();
                } catch(Throwable th) {
                    Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " +//NoI18n
                            "cannot create received Lock ");//NoI18n
                    Debug.logDebugException("CollabFileHandlerSupport, " +//NoI18n
                            "cannot create received Lock", th, true);//NoI18n
                }
            }
        });
    }

    /**
     * lockEditor
     *
     */
    public final EditorLock lockEditor() throws CollabException {
        return doLockEditor();
    }

    /**
     * lockEditor
     *
     */
    protected EditorLock doLockEditor() throws CollabException {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, locking Editor"); //NoI18n	
        disableUnlockTimer(true);

        JEditorPane[] editorPanes = getEditorPanes();

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

    public void unlockFileForCreateRegion() {
        setCurrentState(FilesharingContext.STATE_UNKNOWN);
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                try {
                    unlockEditor(createRegionEditorLock);
                } catch(Throwable th) {
                    Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " +//NoI18n
                            "cannot create received Lock ");//NoI18n
                    Debug.logDebugException("CollabFileHandlerSupport, " +//NoI18n
                            "cannot create received Lock", th, true);//NoI18n
                }
            }
        });
    }

    /**
     * unlockEditor
     *
     */
    public void unlockEditor(EditorLock editorLock) {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, unlocking Editor"); //NoI18n

        //do not enable edit for user readOnly conversation
        if (getContext().isReadOnlyConversation()) {
            return;
        }

        if (editorLock != null) {
            editorLock.releaseLock();
            /*TopComponent tc=findTopComponent();
            if(tc!=null) tc.requestActive();*/
        }

	// Hack, to set focus to users current file editorPane
        Debug.log("CollabFileHandlerSupport","CFHS, request Focus"+
            "after createLock for user: "+getContext().getLoginUser());
        if (FilesharingCollablet.currentEditorPane!=null) {
            FilesharingCollablet.currentEditorPane.requestFocusInWindow();
        }
	
        disableUnlockTimer(false);
    }

    /**
     * resetAllLock
     *
     */
    protected void resetAllLock() throws CollabException {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, resetAllLock"); //NoI18n

        JEditorPane[] editorPanes = getEditorPanes();

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
                        pauseEditorLock = lockEditor();
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

        String regionName = regionInfo.getID();
        CollabRegion cregion = (CollabRegion) rCtx.getRegion(regionName);

        int regionBegin = regionInfo.getbegin();
        int regionEnd = regionInfo.getend();
        regionEnd += regionInfo.getCorrection();

        int length = regionEnd - regionBegin;
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, regionBegin: " + regionBegin); //NoI18n
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, regionEnd: " + regionEnd); //NoI18n

        Vector lineRegions = regionInfo.getLineRegion();
        CollabLineRegion[] regions = null;

        if (lineRegions == null) {
            synchronized (getDocumentLock()) {
                EditorLock editorLock = null;
                try {
                    editorLock = lockEditor();
                    regions = rCtx.getContainingLineRegion(regionBegin, length/*-1*/);
                    unlockEditor(editorLock);
                } catch(Exception e) {
                    e.printStackTrace(Debug.out);
                } finally {
                    unlockEditor(editorLock);
                }
            }
        } else {
            regions=(CollabLineRegion[])lineRegions.toArray(new CollabLineRegion[0]);
        }

        if (regions != null) {
            Debug.log("CollabFileHandlerSupport","CollabFileHandlerSupport, # of containing LineRegions is: " +  //NoI18n
                regions.length);
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

            rCtx.resetLineRegion(regionName, cLineRegionList, false); //do not remove old lines
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
    public RegionInfo getLockRegion(LockRegionData lockRegionData) {
        TextRegion textRegion = lockRegionData.getTextRegion();
        int regionBegin = textRegion.getBeginOffset().intValue();
        int length = textRegion.getLength().intValue();
        int regionEnd = regionBegin + length;
        CollabLineRegion[] cLineRegionList=getCollabLineRegion(lockRegionData);
        Vector cLineRegions=new Vector();
        cLineRegions.addAll(Arrays.asList(cLineRegionList));
        RegionInfo regionInfo = new RegionInfo(
                textRegion.getRegionName(), getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE, regionBegin,
                regionEnd, 0, cLineRegions);
        return regionInfo;
    }
    
    
    /**
     * getCollabLineRegion
     *
     * @param   lockRegionData              the intial lock-region-data Node
     * @return collablineregions
     */
    public CollabLineRegion[] getCollabLineRegion(LockRegionData lockRegionData) {
        List cLineRegionList = new ArrayList();
        LineRegion[] lineRegions = lockRegionData.getLineRegion();
        if(lineRegions.length>0) {
            for(int i=0;i<lineRegions.length;i++) {
                LineRegion lineRegion = lineRegions[i];
                String lineRegionName = lineRegion.getRegionName();
                CollabLineRegion cLineRegion = (CollabLineRegion)rCtx.getRegion(lineRegionName);
                
                if(cLineRegion!=null) cLineRegionList.add(cLineRegion);
            }
        }
        return (CollabLineRegion[])cLineRegionList.toArray(new CollabLineRegion[0]);
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
            "UnlockRegionData region: " + regionName + " for user: " + //NoI18n
            getContext().getLoginUser()
        );
        setCurrentState(FilesharingContext.STATE_UNLOCK);

        String loginUser = getContext().getLoginUser();
        unlockRegionData.setFileName(getName());

        Object pregion = setUnlockRegion(unlockRegionData);

        EditorLock editorLock = null; 
        try { 
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
            synchronized(getDocumentLock()) {
                editorLock = lockEditor();
                Debug.log("CollabFileHandlerSupport","CFHS, region begin: "+cregion.getBeginOffset());
                Debug.log("CollabFileHandlerSupport","CFHS, region end: "+cregion.getEndOffset());

                text = cregion.getContent();

//                String encodedChange = encodeBase64(text.getBytes());
//                content.setData(encodedChange);
//                unlockRegionData.setContent(content);

                //add new lineregion
                int beginOffset = cregion.getBeginOffset();
                int endOffset = cregion.getEndOffset();

                List newLineRegionList = null;

                newLineRegionList = rCtx.doUpdateLineRegion(regionName, beginOffset,
                        endOffset, unlockRegionData, null); 

                //construct protocol line regions
                LineRegion[] pregions = setUnlockLineRegion(unlockRegionData, newLineRegionList.size());

				StringBuffer lineTexts = new StringBuffer();
                for (int i = 0; i < newLineRegionList.size(); i++) {
                    CollabLineRegion cLineRegion = (CollabLineRegion) newLineRegionList.get(i);
                    LineRegion pLineRegion = pregions[i];
                    String lineRegionName = cLineRegion.getID();
                    Debug.log(
                        "CollabFileHandlerSupport", "CollabFileHandlerSupport, New newLineRegionList" + //NoI18n
                        lineRegionName
                    );
                    Debug.log("CollabFileHandlerSupport", "CFHS, line content" + 
                        cLineRegion.getContent());//NoI18n					
					lineTexts.append(cLineRegion.getContent());					

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

                int style = -1; //history annotation
                addLineRegionAnnotation(
                    (CollabLineRegion[]) newLineRegionList.toArray(new CollabLineRegion[0]), style, annotationMessage
                );
				
				//text=lineTexts.toString();
				//if(text.endsWith("\n"))
				//	text=text.substring(0, text.length()-1);
				if(!((CollabRegionSupport)textRegion).isEndOpenRegion() && text.endsWith("\n"))
					text=text.substring(0, text.length()-1);
					
                Debug.log("CollabFileHandlerSupport","CFHS, , construct unlock for " + //NoI18n
                        "region: "+regionName+" with content : [" + text +"]"); //NoI18n				
                String encodedChange = encodeBase64(text.getBytes());
                content.setData(encodedChange);
                unlockRegionData.setContent(content);				
            }
        } catch(Throwable th) {
			Debug.log("CollabFileHandlerSupport", "CFHS, exception: "+th.getMessage());
            throw new CollabException(th);
        } finally{
            unlockEditor(editorLock);
        }
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

	    //Hack, to set focus to users current file editorPane
            Debug.log("CollabFileHandlerSupport","CFHS, request Focus"+
                "after createLock for user: "+getContext().getLoginUser());
            if (FilesharingCollablet.currentEditorPane != null) {
                FilesharingCollablet.currentEditorPane.requestFocusInWindow();
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
                            removeRegion(getContext().getLoginUser(), regionName);
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
				isReadyToUnlock1++;
				Debug.log("CollabFileHandlerSupport", "CFHS, after constructUnlockRegionData: " + isReadyToUnlock1); //NoI18n
				break;//process only one unlock region at a time
            } catch (Throwable th) {
				Debug.log("CollabFileHandlerSupport", "CFHS, exception: " + th.getMessage()); //NoI18n
                continue;
            }

            //isReadyToUnlock1++;
        }

		Debug.log("CollabFileHandlerSupport", "CFHS, isReadyToUnlock1: " + isReadyToUnlock1); //NoI18n
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
        EditorLock editorLock = lockEditor();

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

				Debug.log("CollabFileHandlerSupport", "CFHS, SND create LineRegion: ");
                for (int i = 0; i < lineRegions.length; i++) {
                    RegionInfo region = lineRegions[i];
                    String regionName = region.getID();
                    int beginOffset = region.getbegin();
                    int endOffset = region.getend();
					try {
						Debug.log("CollabFileHandlerSupport", 
							regionName+", begin: " + beginOffset+" end: "+endOffset+
							getDocument().getText(beginOffset, endOffset-beginOffset)); //NoI18n	
						Debug.log("CollabFileHandlerSupport", 
							" begin index: " + getDocument().getDefaultRootElement().getElementIndex(beginOffset)+
							" end index: "+ getDocument().getDefaultRootElement().getElementIndex(endOffset)); //NoI18n
					} catch (CollabException ex) {
						ex.printStackTrace();
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					} //NoI18n			

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
     * isRegionOverlap
     *
     * return true if can region exists or there is a overlap found
     *
     * @param beginOffset
     * @param endOffset
     * @throws CollabException
     * @return
     */
    public boolean isRegionOverlap(CollabLineRegion beginLine, CollabLineRegion endLine) throws CollabException {
        if(beginLine==null || endLine==null) return false;
        Debug.log("CollabFileHandlerSupport","CFHS::isRegionOverlap:: " +
                "beginLine: "+beginLine.getID()+" isAssigned: "+beginLine.isAssigned());
        Debug.log("CollabFileHandlerSupport","CFHS::isRegionOverlap:: " +
                "endLine: "+endLine.getID()+" isAssigned: "+endLine.isAssigned());
        return (beginLine.isAssigned() || endLine.isAssigned());
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
            Debug.logDebugException("CollabFileHandlerSupport, " +//NoI18n
                    "getContent failed", ex, true); //NoI18n
        }

        return "";
    }

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

        EditorLock editorLock=null;
        try {
            StyledDocument fileDocument = fileDocument=getDocument();
            synchronized(getDocumentLock()) {
                editorLock = lockEditor();

                /* refactored into findUnAssignedLines()
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
                //adjust beginLine to avoid delete first line
                if(beginLine==1) beginLine=0;

                int beginOffset = fileDocument.getDefaultRootElement().getElement(beginLine).getStartOffset() - 1;

                if (beginOffset < 0) {
                    beginOffset = 0;
                }

                int endOffset = fileDocument.getDefaultRootElement().getElement(endLine).getEndOffset();

                Vector lineRegions = new Vector(10);
                boolean foundFirstMatch = false;
                CollabLineRegion endLineNeighbour=null;
				CollabLineRegion beginLineNeighbour=null;
                Debug.log("CollabFileHandlerSupport","CFHS, Check lines are between:" +
                        " beginOffset: " + beginOffset + " endOffset: " + endOffset); //NoI18n

                for (int i = beginLine; i < rCtx.getLineRegionCount(); i++) {
                    CollabLineRegion lineRegion = rCtx.getLineRegion(i);

                    if (lineRegion == null) {
                        continue;
                    }

                    int lineBeginOffset = lineRegion.getBeginOffset();
                    Debug.log("CollabFileHandlerSupport","CFHS, "+ "line ["+i+"]: "+
                            lineRegion.getID()+" isAssigned(): " + lineRegion.isAssigned()+
                            " lineBeginOffset: "+lineBeginOffset); //NoI18n
                    if(!lineRegion.isAssigned() && lineBeginOffset>=beginOffset && lineBeginOffset<endOffset) {
                        foundFirstMatch=true;
                        Debug.log("CollabFileHandlerSupport","CFHS, Add line: " + //NoI18n
                                lineRegion.getID());
                        lineRegions.add(lineRegion);
                    } else {
                        if (foundFirstMatch) { //reached end of match
                            endLineNeighbour=lineRegion;
                            break;
                        }
						else
							beginLineNeighbour=lineRegion;
                    }
                }*/
				
				Vector lineRegions=
					findUnAssignedLines(beginLine, endLine, 
						endOffsetCorrection, includeMarginLines);
                if (lineRegions.size() == 0) {
                    Debug.log("CollabFileHandlerSupport","CFHS," +
                            "Cannot create lock, no line regions match, so try to " +
                            "create regions from " + beginLine + " to: " +endLine);
                    return;
                }
				CollabLineRegion beginLineNeighbour=(CollabLineRegion) lineRegions.firstElement();
				lineRegions.removeElement(beginLineNeighbour);
				CollabLineRegion endLineNeighbour=(CollabLineRegion) lineRegions.lastElement();
				lineRegions.removeElement(endLineNeighbour);
				
/*refactored to findNewRegionBeginOffset() and findNewRegionEndOffset()
                //Calculate offset based on unassigned lineregions only
                if(lineRegions==null || lineRegions.size()==0) return;
                CollabLineRegion beginLineRegion=(CollabLineRegion)lineRegions.get(0);
                if(beginLineRegion==null) return;
                //beginOffset = fileDocument.getDefaultRootElement().
                //        getElement(beginLineRegion.getLineIndex()).getStartOffset()-1;
				beginOffset=beginLineRegion.getBeginOffset();
				if(beginOffset!=0 && beginOffset+1==beginLineRegion.getBeginOffset())//correct beginoffset
					beginOffset = beginLineRegion.getBeginOffset();
				if(beginOffset<0) beginOffset=0;				
                CollabLineRegion endLineRegion=
                        (CollabLineRegion)lineRegions.get(lineRegions.size()-1);
                if(endLineRegion==null) return;
                //endOffset = fileDocument.getDefaultRootElement().
                //        getElement(endLineRegion.getLineIndex()).getEndOffset();
				if(endOffset<endLineRegion.getEndOffset())
					endOffset = endLineRegion.getEndOffset();

                //adjust endoffset for new inserted lines or delete first line
                if(endLineNeighbour!=null && (endOffset+1<endLineNeighbour.getBeginOffset() ||
                        endOffset>=endLineNeighbour.getBeginOffset())) {
                    Debug.log("CollabFileHandlerSupport","CFHS, elR: " +
                            endLineRegion.getID()+" end: "+ endOffset);//NoI18n
                    Debug.log("CollabFileHandlerSupport","CFHS, elN: "+endLineNeighbour.getID()+
                            " begin: "+endLineNeighbour.getBeginOffset());//NoI18n
                    endOffset=endLineNeighbour.getBeginOffset()-1;
                    if(endOffset<0) endOffset=0;
                    Debug.log("CollabFileHandlerSupport","CFHS, adjusted endOffset: "+
                            endOffset);//NoI18n
                }

                int docLength=fileDocument.getLength();
                if(endOffset>docLength) endOffset=docLength;*/
				
                Debug.log("CollabFileHandlerSupport","CFHS, " +
                        "create new region beginLineNeighbour end: "+ 
						(beginLineNeighbour!=null?beginLineNeighbour.getEndOffset():-1) +
                        " endLineNeighbour begin: " +
						(endLineNeighbour!=null?endLineNeighbour.getBeginOffset():-1));//NoI18n
                Debug.log("CollabFileHandlerSupport","CFHS, lines size: " + lineRegions.size()); //NoI18n
				
				int beginOffset=
					findNewRegionBeginOffset(lineRegions, beginLineNeighbour);
				if(beginOffset==-1) return;
				
				int endOffset=
					findNewRegionEndOffset(lineRegions, endLineNeighbour);	
				if(endOffset==-1) return;

                //Create new region
                Debug.log("CollabFileHandlerSupport","CFHS, " +
                        "create new region beginOffset: "+ beginOffset +
                        " endOffset: "+endOffset);//NoI18n
                //Debug.log("CollabFileHandlerSupport","CFHS, lines: " + lineRegions); //NoI18n
				if(endOffset==0) return;
                CollabRegion textRegion = createRegion(regionName, beginOffset, endOffset, false);
                if(textRegion==null) return;

                textRegion.setValid(false);
                rCtx.addRegion(getContext().getLoginUser(), regionName, textRegion);

                //Set parent
                for(int i=0;i<lineRegions.size();i++) {
                    CollabLineRegion liner=(CollabLineRegion)lineRegions.get(i);
                    if(liner!=null) {
                        Debug.log("CollabFileHandlerSupport","CFHS, setAssigned " +
                                "true for: " + liner.getID()); //NoI18n
                        liner.setAssigned(textRegion, true);
                    }
                }

                //add annotation
                String user = getContext().getLoginUser();
                String annotationMessage = NbBundle.getMessage(
                        CollabFileHandlerSupport.class, "MSG_CollabFileHandlerSupport_EditingAnnotation", // NOI18N
                        getContext().getPrincipal(user).getDisplayName()
                    );
                int style = getUserStyle(user);
                List tmpList=new ArrayList();
                tmpList.addAll(lineRegions);
                addLineRegionAnnotation(
                        (CollabLineRegion[]) tmpList.toArray(new CollabLineRegion[0]),
                        style, annotationMessage);

				if(textRegion.getContent().endsWith("\n"))
					((CollabRegionSupport)textRegion).setEndOpenRegion(true);
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
        } catch(CollabException ce) {
            ce.printStackTrace(Debug.out);
        } finally {
            unlockEditor(editorLock);

	    //Hack - Now save the editorpane that had the users focus
            registerInputListener();
/*
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    try {
                        TopComponent tc=findTopComponent();
                        if(tc!=null) tc.requestActive();
                    } catch(Throwable th) {
                        Debug.logDebugException("CFHS, " +//NoI18n
                                "cannot request Active after createnewregion", //NoI18n
                                th, true);
                    }
                }
            });
  */
        } 
    }
	
    private Vector findUnAssignedLines(int beginLine, int endLine, 
			int endOffsetCorrection, boolean includeMarginLines)
		throws CollabException 
	{
		CollabLineRegion beginLineNeighbour=null;
		CollabLineRegion endLineNeighbour=null;
		StyledDocument fileDocument=getDocument();
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
		//adjust beginLine to avoid delete first line
		if(beginLine==1) beginLine=0;

		int beginOffset = fileDocument.getDefaultRootElement().getElement(beginLine).getStartOffset() - 1;

		if (beginOffset < 0) {
			beginOffset = 0;
		}

		int endOffset = fileDocument.getDefaultRootElement().getElement(endLine).getEndOffset();

		Vector lineRegions = new Vector(10);
		boolean foundFirstMatch = false;
		Debug.log("CollabFileHandlerSupport","CFHS, Check lines are between:" +
				" beginOffset: " + beginOffset + " endOffset: " + endOffset); //NoI18n

		if(beginLine>0)
			beginLineNeighbour=rCtx.getLineRegion(beginLine-1);
		
		for (int i = beginLine; i < rCtx.getLineRegionCount(); i++) {
			CollabLineRegion lineRegion = rCtx.getLineRegion(i);

			if (lineRegion == null) {
				continue;
			}

			int lineBeginOffset = lineRegion.getBeginOffset();
			Debug.log("CollabFileHandlerSupport","CFHS, "+ "line ["+i+"]: "+
					lineRegion.getID()+" isAssigned(): " + lineRegion.isAssigned()+
					" lineBeginOffset: "+lineBeginOffset); //NoI18n
			if(!lineRegion.isAssigned() && lineBeginOffset>=beginOffset && lineBeginOffset<endOffset) {
				foundFirstMatch=true;
				Debug.log("CollabFileHandlerSupport","CFHS, Add line: " + //NoI18n
						lineRegion.getID());
				lineRegions.add(lineRegion);
			} else {
				if (foundFirstMatch) { //reached end of match
					endLineNeighbour=lineRegion;
					break;
				}
				else
					beginLineNeighbour=lineRegion;
			}
		}
		//add the neighbouring begin and end lines
		lineRegions.insertElementAt(beginLineNeighbour, 0);
		lineRegions.add(endLineNeighbour);
		
		return lineRegions;
	}	
	
	private int findNewRegionBeginOffset(final Vector lineRegions,
			final CollabLineRegion beginLineNeighbour)
	{
		int beginOffset=0;
		
		if(lineRegions==null || lineRegions.size()==0) return -1;
		
		if(beginLineNeighbour!=null)
		{
			Debug.log("CollabFileHandlerSupport","CFHS, blN: "+beginLineNeighbour.getID()+
					" end: "+beginLineNeighbour.getEndOffset());//NoI18n			
			beginOffset=beginLineNeighbour.getEndOffset();
		}
		/*CollabLineRegion beginLineRegion=(CollabLineRegion)lineRegions.get(0);
		if(beginLineRegion==null) return -1;
		beginOffset=beginLineRegion.getBeginOffset();
		if(beginOffset!=0 && beginOffset+1==beginLineRegion.getBeginOffset())//correct beginoffset
			beginOffset = beginLineRegion.getBeginOffset();

		if(beginOffset<0) beginOffset=0;*/
		
		if(beginOffset<0) beginOffset=0;
		
		return beginOffset;
	}

	private int findNewRegionEndOffset(final Vector lineRegions, 
			final CollabLineRegion endLineNeighbour) 
		throws CollabException
	{
		int docLength=getDocument().getLength();		
		int endOffset=docLength;
		
		if(lineRegions==null || lineRegions.size()==0) return -1;
				
		if(endLineNeighbour!=null)
		{
			Debug.log("CollabFileHandlerSupport","CFHS, elN: "+endLineNeighbour.getID()+
					" begin: "+endLineNeighbour.getBeginOffset());//NoI18n
			endOffset=endLineNeighbour.getBeginOffset()-1;
		}
		/*CollabLineRegion endLineRegion=
				(CollabLineRegion)lineRegions.get(lineRegions.size()-1);
		if(endLineRegion==null) return -1;
		if(endOffset<endLineRegion.getEndOffset())
			endOffset = endLineRegion.getEndOffset();

		//adjust endoffset for new inserted lines or delete first line
		if(endLineNeighbour!=null && (endOffset+1<endLineNeighbour.getBeginOffset() ||
				endOffset>=endLineNeighbour.getBeginOffset())) {
			Debug.log("CollabFileHandlerSupport","CFHS, elR: " +
					endLineRegion.getID()+" end: "+ endOffset);//NoI18n
			Debug.log("CollabFileHandlerSupport","CFHS, elN: "+endLineNeighbour.getID()+
					" begin: "+endLineNeighbour.getBeginOffset());//NoI18n
			endOffset=endLineNeighbour.getBeginOffset()-1;
			if(endOffset<0) endOffset=0;
			Debug.log("CollabFileHandlerSupport","CFHS, adjusted endOffset: "+
					endOffset);//NoI18n
		}*/

		if(endOffset>docLength) endOffset=docLength;
		
		return endOffset;
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
     * @param user
     * @param offset
     * @param length
     * @param length of adjacency of change to this region
     * @return true if region exist
     */
    public boolean isUserRegionExist(String user, int offset, int length, int adjacency)
    throws CollabException {
        return rCtx.isUserRegionExist(user, offset, length, adjacency);
    }
	
    /**
     * test if the region exist in the repository
     *
     * @param user
     * @param offset
     * @param length
     * @param length of adjacency of change to this region
     * @return true if region exist
     */
    public CollabRegion getContainingUserRegion(String user, int offset, int length, int adjacency)
		throws CollabException 
	{
        return rCtx.getContainingUserRegion(user, offset, length, adjacency);
    }	
    
    /**
     * test if the region exist in the repository
     *
     * @param offset
     * @param length
     * @param length of adjacency of change to this region
     * @return true if region exist
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

                if (file == null) return null;

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
	//Hack - Now save the editorpane that had the users focus
        registerInputListener();
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

        //clear annotations
        for(int i=0;i<rCtx.getLineRegionCount();i++) {
            CollabLineRegion lineRegion = rCtx.getLineRegion(i);
            if(lineRegion!=null) lineRegion.removeAnnotation();
        }
        
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
            editorLock = lockEditor();
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
        Element rootElement = fileDocument.getDefaultRootElement();
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
                    javax.swing.text.Element currentElement = rootElement.getElement(i);
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
			Debug.logDebugException("CollabFileHandlerSupport, " +//NoI18n
                            "resetallbeginoffset failed", ia, true);//NoI18n
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
     * getLineRegions
     *
     * @param   regionName
     * @return        lineRegions
     */
    public List getLineRegions(String regionName) {
        return rCtx.getLineRegions(regionName);
    }

    public String createUniqueLockID(String userID, String regionID) {
        return rCtx.createUniqueLockID(userID, regionID);
    }

    public LockRegionManager createLockRegionManager(String userID, LockRegion lockRegion) {
        return rCtx.createLockRegionManager(userID, lockRegion);
    }        

    public LockRegionManager createLockRegionManager(String userID, RegionInfo regionInfo) {
        return rCtx.createLockRegionManager(userID, regionInfo);
    }

    public void removeLockRegionManager(String lockID) {
        rCtx.removeLockManager(lockID);
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
    
    //Key Events
    /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
        setCurrentEditorPane(findCurrentEditorPane());
	if(Debug.isEnabled()) displayInfo(e, "KEY TYPED: ");
    }

    /** Handle the key pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
        setCurrentEditorPane(findCurrentEditorPane());
        if(Debug.isEnabled()) displayInfo(e, "KEY PRESSED: ");
    }
          
    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) {}      
  
    protected void displayInfo(KeyEvent e, String s) {
        String keyString=null;
        String modString=null;
        String actionString=null;
        String locationString=null;
        //You should only rely on the key char if the event
        //is a key typed event.
        int id = e.getID();
        if (id == KeyEvent.KEY_TYPED) {
            char c = e.getKeyChar();
            keyString = "key character = '" + c + "'";
        } else {
            int keyCode = e.getKeyCode();
            keyString = "key code = " + keyCode
                    + " ("
                    + KeyEvent.getKeyText(keyCode)
                    + ")";
        }
        
        int modifiers = e.getModifiersEx();
        modString = "modifiers = " + modifiers;
        String tmpString = KeyEvent.getModifiersExText(modifiers);
        if (tmpString.length() > 0) {
            modString += " (" + tmpString + ")";
        } else {
            modString += " (no modifiers)";
        }
        
        actionString = "action key? ";
        if (e.isActionKey()) {
            actionString += "YES";
        } else {
            actionString += "NO";
        }
        
        locationString = "key location: ";
        int location = e.getKeyLocation();
        if (location == KeyEvent.KEY_LOCATION_STANDARD) {
            locationString += "standard";
        } else if (location == KeyEvent.KEY_LOCATION_LEFT) {
            locationString += "left";
        } else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
            locationString += "right";
        } else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
            locationString += "numpad";
        } else { // (location == KeyEvent.KEY_LOCATION_UNKNOWN)
            locationString += "unknown";
        }
        
        Debug.log("CollabFileHandlerSupport", "CFHS, " +
                "keyString: "+keyString+
                "actionString: "+actionString+
                "locationString: "+locationString);
    }
    
    //Mouse events
    public void mousePressed(MouseEvent e) {
        setCurrentEditorPane(findCurrentEditorPane());
        if(Debug.isEnabled())
            saySomething("Mouse pressed; # of clicks: "
                    + e.getClickCount(), e);
    }
    
    public void mouseClicked(MouseEvent e) {
        setCurrentEditorPane(findCurrentEditorPane());
        if(Debug.isEnabled())
            saySomething("Mouse clicked (# of clicks: "
                    + e.getClickCount() + ")", e);
    }
    
    public void mouseReleased(MouseEvent e) {}
    
    public void mouseEntered(MouseEvent e) {}
    
    public void mouseExited(MouseEvent e) {}
    
    void saySomething(String eventDescription, MouseEvent e) {
        Debug.log("CollabFileHandlerSupport", "CFHS, " +
                eventDescription + " detected on "
                + e.getComponent().getClass().getName());
    }
    
    private void registerInputListener()
    throws CollabException {
        JEditorPane[] editorPanes = getEditorPanes();;
        if(editorPanes==null)
            return;
        Debug.log("CollabFileHandlerSupport","CFHS, save editor pane for focus: " +
                "# of editorPanes: "+ editorPanes.length);
        for(int i=0;i<editorPanes.length;i++) {
            JEditorPane editorPane = editorPanes[i];
            if(editorPane!=null) {
                //Now remove and add key and mouse listeners for this editorpane
                Debug.log("CollabFileHandlerSupport","CFHS, isFocusOwner: "+
                        editorPane.isFocusOwner()+" for user: "+getContext().getLoginUser());
                editorPane.removeMouseListener(this);
                editorPane.removeKeyListener(this);
                editorPane.addMouseListener(this);
                editorPane.addKeyListener(this);
            }
        }
        //get the last editor pane and set as the current editorpane
        JEditorPane editorPane=findCurrentEditorPane(editorPanes);
        if(editorPane!=null) {
            Debug.log("CollabFileHandlerSupport","CFHS, isFocusOwner: "+
                    editorPane.isFocusOwner()+" for user: "+getContext().getLoginUser());
            setCurrentEditorPane(editorPane);
        }
    }
    
    private JEditorPane findCurrentEditorPane() {
        try{
            return findCurrentEditorPane(getEditorPanes());
        } catch(CollabException ce){
            return null;
        }
    }
    
    private JEditorPane findCurrentEditorPane(JEditorPane[] editorPanes) {
        if(editorPanes!=null && editorPanes.length>0)
            return editorPanes[editorPanes.length-1];
        else
            return null;
    }
    
    private JEditorPane[] getEditorPanes()
    throws CollabException {
        final EditorCookie ec=getEditorCookie();
        if(ec!=null)
            return (JEditorPane[])Mutex.EVENT.readAccess(new Mutex.Action() {
                public Object run() {
                    return ec.getOpenedPanes();
                }
            });
        return null;
    }
    
    private void setCurrentEditorPane(JEditorPane editorPane) {
        FilesharingCollablet.currentEditorPane=editorPane;
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
                    Vector regionNames=((Vector)getUserRegion().get(user));
                    if(!regionNames.contains(regionName)) regionNames.add(regionName);
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
            String[] users=(String[])getUserRegion().keySet().toArray(new String[0]);
            for(int j=0;j<users.length;j++) {
                String user = users[j];
                
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
         * replaceLineRegions 
         *
         * @param       regionList the list of user regions
         */
        private void replaceLineRegions(Object regionList) {
            String user = getLineRegionKey();
            synchronized(collabUserRegionMap) {
                if (collabUserRegionMap.containsKey(user)) {
                    collabUserRegionMap.remove(user);
                }
                collabUserRegionMap.put(user, regionList);
            }
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
            printUserRegions();
            return this.regions;
        }
        
        /**
         * get region map
         *
         * @return      region map
         */
        public void printUserRegions() {
            if(Debug.isEnabled()) {
                synchronized(this.regions) {
                    String[] users=(String[])getUserRegion().keySet().toArray(new String[0]);
                    for(int j=0;j<users.length;j++) {
                        //String user = (String) it.next();
                        String user = users[j];
                        if(user!=null && user.equals(getLineRegionKey())) continue;
                        Debug.log("CollabFileHandlerSupport_LineRegion","CFHS, regions user: " + user);//NoI18n 

                        Vector userRegions = (Vector)getUserRegion(user);
                        if(userRegions==null||userRegions.size()==0) continue;   
                        
                        for(int i=0;i<userRegions.size();i++) {
                            String regionName = (String)userRegions.get(i);
                            Debug.log("CollabFileHandlerSupport_LineRegion","CFHS, regions: "+//NoI18n
                                    regionName);
                        }
                    }
                }
            }
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
         * @param   newLines for this region
         * @param   if old lineregions need to be removed for this file completely
         * @return        newLines
         */
        public void resetLineRegion(String regionName, List newLines, boolean removeOldLines) {
            synchronized(collabRegionLineRegionMap) {
                if(removeOldLines) {
                    List oldLines=getLineRegions(regionName);
                    if(oldLines!=null) {
                        for(int i=0;i<oldLines.size();i++) {
                            CollabLineRegion liner=(CollabLineRegion)oldLines.get(i);
                            Debug.log("CollabFileHandlerSupport","CFHS, removing " +
                                    "old line: "+liner.getID()); //NoI18n
                            try {
                                removeLineRegion(liner.getID());
                            } catch(Exception e) {
                                Debug.logDebugException("exception removing " +
                                        "old line region", e, true);
                            }
                        }
                    }
                } 
                collabRegionLineRegionMap.remove(regionName);
                collabRegionLineRegionMap.put(regionName, newLines);
            }
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
            
            String[] users=(String[])getUserRegion().keySet().toArray(new String[0]);
            for(int j=0;j<users.length;j++) {
                String user = users[j];
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

                if((beginOffset>=posBegin && beginOffset<=posEnd) ||
                        ((endOffset >= posBegin) && (endOffset <= posEnd))) {
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
            Debug.log("CollabFileHandlerSupport","CFHS::sect.isGuarded(): "+sect.isGuarded());
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
                    
                    if(Debug.isEnabled())
                        Debug.log("CollabFileHandlerSupport","CFHS, " + "doUpdateRegion set text: [" + text+"]");
                    if(Debug.isEnabled()) Debug.log("CollabFileHandlerSupport","CFHS, " +
                            "simpleSection before update: [" + simpleSection.getText()+"]");

                    simpleSection.setText(text);
                    
                    if(Debug.isEnabled()) Debug.log("CollabFileHandlerSupport","CFHS, " +
                            "simpleSection after update: [" + simpleSection.getText()+"]");
                    
                    if (documentListener != null) {
                        ((FilesharingDocumentListener) documentListener).setSkipUpdate(false);
                    }
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
                Vector currentLineRegions = getLineRegion();
                Debug.log("CollabFileHandlerSupport","CFHS, currentLineRegions [" +
                        currentLineRegions.size()+"]: before update: " +
                        currentLineRegions);//NoI18n

                int beginLine = fileDocument.getDefaultRootElement().getElementIndex(beginOffset);
                //int endLine = fileDocument.getDefaultRootElement().getElementIndex(endOffset - 1);
				int endLine = fileDocument.getDefaultRootElement().getElementIndex(endOffset);
                Debug.log("CollabFileHandlerSupport","CFHS, beginLine: " + beginLine); //NoI18n
                Debug.log("CollabFileHandlerSupport","CFHS, endLine: " + endLine); //NoI18n

                //save line region that was the begin
                CollabLineRegion firstLineRegion = (CollabLineRegion) lineRegions.get(0);
                Debug.log("CollabFileHandlerSupport","CFHS, first line regionName: " + //NoI18n
                    firstLineRegion.getID());
                
                int saveIndex=currentLineRegions.indexOf(firstLineRegion.getID());
                Debug.log("CollabFileHandlerSupport","CFHS, first line saveIndex: " + //NoI18n
                        saveIndex);
                
                if ((newLineRegionNames != null) && (newLineRegionNames.length > 0)) {
                    endLine = beginLine + (newLineRegionNames.length - 1);
                }
                
                if(beginLine<0) beginLine=0;
                int docLineCount=fileDocument.getDefaultRootElement().getElementCount();
                if(endLine>=docLineCount)  endLine=docLineCount-1;
                
                //remove old line regions
                for (int i = 0; i < lineRegions.size(); i++) {
                    CollabLineRegion lineRegion = (CollabLineRegion) lineRegions.get(i);

                    if (lineRegion != null) {
                        lineRegion.removeAnnotation();
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

                    Debug.log("CollabFileHandlerSupport","CFHS, newLineRegionName: " + //NoI18n
                            newLineRegionName);

                    int newLineBeginOffset = currentElement.getStartOffset();
                    int newLineEndOffset = currentElement.getEndOffset();

                    CollabRegion newLineRegion = createLineRegion(
                            newLineRegionName, newLineBeginOffset, newLineEndOffset
                        );

                    if (newLineRegion != null) {
                        newLineRegionList.add(newLineRegion);
                        Debug.log("CollabFileHandlerSupport", "CFHS, currentLineRegions size: " + //NoI18n
                                currentLineRegions.size());                                              

                        Debug.log(
                            "CollabFileHandlerSupport",
                            "CollabFileHandlerSupport, insert element " + //NoI18n 
                            newLineRegionName + " at: " + saveIndex
                        ); //NoI18n						
                        currentLineRegions.insertElementAt(newLineRegionName, saveIndex++);
                        addRegion(newLineRegionName, newLineRegion);
                    }
                }
                
                Debug.log("CollabFileHandlerSupport","CFHS, new list: " + getLineRegion()); //NoI18n
                resetLineRegion(regionName, newLineRegionList, true);//remove old lines also
                replaceLineRegions(currentLineRegions);
                Debug.log("CollabFileHandlerSupport","CFHS, currentLineRegions [" +
                        currentLineRegions.size()+"]: before update: " +
                        currentLineRegions);//NoI18n
            }

            return newLineRegionList;
        }

        /**
         * test if the region exist in the repository
         *
         * @param user
         * @param offset
         * @param length
         * @param length of adjacency of change to this region
         * @return        true/false                                true if region exist
         */
        public boolean isUserRegionExist(String user, int offset, int length, int adjacency)
        throws CollabException {
            /*boolean foundUserRegion=false;
            CollabRegion region=getContainingRegion(offset, length, adjacency);
            if(region!=null && region.isValid() && region instanceof CollabRegion &&
                    !(region instanceof CollabLineRegion)) {
                Vector userRegions = (Vector)getUserRegion(user);
                if(userRegions!=null) {
                    for(int i=0;i<userRegions.size();i++) {
                        String regionName = (String)userRegions.get(i);
                        if(regionName!=null && regionName.equals(region.getID()))
                            foundUserRegion=true;
                    }
                }
                
                currentUpdatedRegion=region;
                if(!skipUpdate) {
                    //update status changed
                    region.updateStatusChanged(true);
                }
                return foundUserRegion;
            }
            return foundUserRegion;*/
			CollabRegion findUserRegion=getContainingUserRegion(user, offset, length, adjacency);
			if(findUserRegion!=null)
				return true;

			return false;
        }
		
        /**
         * get user region in the repository
         *
         * @param user
         * @param offset
         * @param length
         * @param length of adjacency of change to this region
         * @return        true/false                                true if region exist
         */
        public CollabRegion getContainingUserRegion(String user, int offset, int length, int adjacency)
			throws CollabException 
		{
            CollabRegion findUserRegion=null;
            CollabRegion region=getContainingRegion(offset, length, adjacency);
            if(region!=null && region.isValid() && region instanceof CollabRegion &&
                    !(region instanceof CollabLineRegion)) {
                Vector userRegions = (Vector)getUserRegion(user);
                if(userRegions!=null) {
                    for(int i=0;i<userRegions.size();i++) {
                        String regionName = (String)userRegions.get(i);
                        if(regionName!=null && regionName.equals(region.getID()))
                            findUserRegion=region;
                    }
                }
                
                currentUpdatedRegion=region;
                if(!skipUpdate) {
                    //update status changed
                    region.updateStatusChanged(true);
                }
                return findUserRegion;
            }
            return findUserRegion;
        }		
        
        /**
         * test if the region exist in the repository
         *
         * @param offset
         * @param length
         * @param length of adjacency of change to this region
         * @return      true/false                              true if region exist
         */ 
       public boolean regionExist(int offset, int length, int adjacency)
        throws CollabException {
            CollabRegion region = getContainingRegion(offset, length, adjacency);

            if ((region != null) && region.isValid() &&
                    region instanceof CollabRegion && !(region instanceof CollabLineRegion)) {
                CollabRegion cRegion = (CollabRegion) region;
                currentUpdatedRegion = cRegion;

                if (!skipUpdate) {
                    //update status changed
                    cRegion.updateStatusChanged(true);
                }

                return true;
            }

            return false;
        }

        public String createUniqueLockID(String userID, String regionID) {
            return userID+getName()+regionID;
        }

        public LockRegionManager createLockRegionManager(String userID, LockRegion lockRegion) {
            LockRegionManager lrm=null;
            LockRegionData[] lockRegionData = lockRegion.getLockRegionData();
            if (lockRegionData!=null && lockRegionData.length>0) {
                RegionInfo regionInfo = getLockRegion(lockRegionData[0]);
                lrm=createLockRegionManager(userID, regionInfo);
            }
            return lrm;
        }        

        public LockRegionManager createLockRegionManager(String userID, RegionInfo regionInfo) {
            LockRegionManager lrm = null;
            String lockID = createUniqueLockID(userID, regionInfo.getID());
            synchronized(lrmanagers) {
                if (lrmanagers.containsKey(lockID)) {
                    lrm = (LockRegionManager)lrmanagers.get(lockID);
                } else {
                    lrm = new LockRegionManager(lockID, getContext(), userID, regionInfo);
                    lrmanagers.put(lockID, lrm);
                }
            }
            return lrm;
        }

        public void removeLockManager(String lockID) {
            synchronized (lrmanagers) {
                if (lrmanagers.containsKey(lockID)) {
                    lrmanagers.remove(lockID);
                }
            }
        }
    }
}
