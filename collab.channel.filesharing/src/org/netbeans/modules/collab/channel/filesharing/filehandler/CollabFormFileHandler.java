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

import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;

import java.io.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.Base64;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.StreamCopier;
import org.netbeans.modules.collab.channel.filesharing.msgbean.Content;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.SendFile;
import org.netbeans.modules.collab.channel.filesharing.msgbean.SendFileData;
import org.netbeans.modules.collab.core.Debug;

import org.netbeans.modules.form.*;


/**
 * FileHandler for Form files (non-editable files)
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabFormFileHandler extends CollabDefaultFileHandler implements CollabFileHandler {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* cookie variable */
    private EditorCookie cookie = null;

    /**
     * FormFileHandler constructor
     *
     */
    public CollabFormFileHandler() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // File handler methods
    ////////////////////////////////////////////////////////////////////////////	

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
        if (!isValid()) {
            return null;
        }

        setCurrentState(FilesharingContext.STATE_SENDFILE);

        SendFileData sendFileData = new SendFileData();
        FileData fileData = new FileData();
        sendFileData.setFileData(fileData);

        fileData.setFileName(getName());
        fileData.setContentType(getContentType());

        Content content = new Content();
        sendFileData.setContent(content);

        content.setEncoding(getEncoding());
        content.setDigest(getDigest());

        try {
            InputStream inputStream = getFileObject().getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamCopier.copyStream(inputStream, outputStream);

            String encodedFile = Base64.encode(outputStream.toByteArray());
            content.setData(encodedFile);
        } catch (IOException e) {
            throw new CollabException(e);
        }

        if (firstTimeSend) {
            firstTimeSend = false;

            //add listener for this document
            FileObject fileObject = getFileObject();
            fileObject.addFileChangeListener(new DefaultFileChangeListener());

            try {
                Object tmpcookie = getEditorCookie();
            } catch (java.lang.Throwable ex) {
                throw new CollabException(ex);
            }
        }

        return sendFileData;
    }

    /**
     * handles send-file message
     *
     * @param   messageOriginator   the sender of this message
    * @param   sendFileData                the send-file-data Node inside the message
     * @throws CollabException
     */
    public void handleSendFile(String messageOriginator, SendFileData sendFileData)
    throws CollabException {
        setCurrentState(FilesharingContext.STATE_RECEIVEDSENDFILE);

        //copy contents from message to files; add files to CollabFileSystem
        try {
            String fullPath = sendFileData.getFileData().getFileName();

            Content sendFileContent = sendFileData.getContent();

            byte[] fileContents = Base64.decode(sendFileContent.getData());
            String fileContent = new String(fileContents);

            FileObject file = getFileObject(); //do not create 

            if (file == null) {
                file = createFileObject(fileContent);

                //add listener for this document
                FileObject fileObject = getFileObject();
                fileObject.addFileChangeListener(new DefaultFileChangeListener());
            } else {
                inReceiveSendFile = true;
                updateFileObject(fileContent);
                getFileObject().refresh(false);

                Object tmpcookie = getEditorCookie();

                if (tmpcookie != null) {
                    if (tmpcookie instanceof FormEditorSupport) {
                        FormEditorSupport formCookie = (FormEditorSupport) tmpcookie;

                        if (formCookie.isOpened()) {
                            try {
                                formCookie.openFormEditor(true); //force open
                                formCookie.reloadForm();
                            } catch (Throwable th) {
                                th.printStackTrace(Debug.out);
                            }
                        } else {
                            Debug.out.println("Skipping reload, since not open," + " for file: " + getName());
                        }
                    }
                }

                inReceiveSendFile = false;
            }

            firstTimeSend = false;
        } catch (IllegalArgumentException iargs) {
            throw new CollabException(iargs);
        } catch (java.lang.Throwable ex) {
            throw new CollabException(ex);
        } finally {
            inReceiveSendFile = false;
            firstTimeSend = false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Document methods
    ////////////////////////////////////////////////////////////////////////////        

    /**
     * getter for contentType
     *
     * @return contentType
     */
    public String getContentType() {
        return CollabFormFileHandlerFactory.CONTENT_UNKNOWN;
    }

    /**
     * getter for EditorCookie
     * @throws CollabException
     * @return EditorCookie
     */
    public EditorCookie getEditorCookie() throws CollabException {
        if (cookie == null) {
            try {
                FileObject file = getFileObject();

                if (file == null) {
                    return null;
                }

                // Get the DataObject
                DataObject dataObject = DataObject.find(file);

                if (dataObject == null) {
                    return null;
                }

                // return the JavaEditor Cookie for the dataobject
                cookie = (EditorCookie) dataObject.getCookie(EditorCookie.class);
            } catch (org.openide.loaders.DataObjectNotFoundException notFound) {
                throw new CollabException(notFound);
            } catch (java.io.IOException io) {
                throw new CollabException(io);
            } catch (Exception ex) {
                throw new CollabException(ex);
            } catch (java.lang.Throwable ex) {
                throw new CollabException(ex);
            }
        }

        return cookie;
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

        Debug.log(this, //NoI18n
            "CollabFormFileHandler, isModified: " + isModified + " for file " + getName()
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
            "CollabFormFileHandler, saving document: " //NoI18n
             +getName() + " on update"
        ); //NoI18n			

        try {
            EditorCookie cookie = getEditorCookie();

            if (cookie != null) {
                cookie.saveDocument();
            }
        } catch (IOException iox) {
            Debug.log(
                "CollabFileHandlerSupport", //NoI18n
                "CollabFormFileHandler, Exception occured while saving the " + "document: " + getName() + " on update"
            ); //NoI18n

            return false;
        }

        return true;
    }
}
