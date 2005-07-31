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

import org.openide.filesystems.*;

import javax.swing.text.StyledDocument;

import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingDocumentListener;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabDocumentListener;
import org.netbeans.modules.collab.core.Debug;


/**
 * FileHandler for Text files
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabTextFileHandler extends CollabFileHandlerSupport implements CollabFileHandler {
    /**
     * TextFileHandler constructor
     *
     */
    public CollabTextFileHandler() {
        super();
        setContentType(CollabTextFileHandlerFactory.TEXT_MIME_TYPE);
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
        //return CollabTextFileHandlerFactory.TEXT_MIME_TYPE;
        return super.getContentType();
    }

    protected FileObject createFile(FileObject folder, String fileName) {
        Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " + //NoI18n
            "TEXT createFile: " + fileName
        ); //NoI18n		

        /*int index = fileName.lastIndexOf('.');
        if(index!=-1 && fileName.substring(index+1).equals("txt"))
        {
                return super.createFile(folder, fileName, null);
        }
        else
        {
                return super.createFile(folder, fileName, "txt");
        }*/
        if (getContentType().trim().equals(CollabFileHandler.TEXT_UNKNOWN)) {
            return super.createFile(folder, fileName, "txt");
        } else {
            return super.createFile(folder, fileName, null);
        }
    }

    /**
     * getter for filehandler
     *
     * @return        filehandler
     */
    public CollabFileHandler getFileHandler() {
        return this;
    }

    /**
     * add DocumentListener
     *
     * @return DocumentListener
     * @throws CollabException
     */
    public CollabDocumentListener addDocumentListener()
    throws CollabException {
        //add listener for this document
        StyledDocument fileDocument = getDocument();
        CollabDocumentListener listener = null;

        synchronized (fileDocument) {
            fileDocument.putProperty("COLLAB_FILEHANDLER_FILE_NAME", getName());
            listener = new FilesharingDocumentListener(fileDocument, createEventNotifer());
            fileDocument.addDocumentListener(listener);
        }

        return listener;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Management methods
    ////////////////////////////////////////////////////////////////////////////	

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
    public CollabRegion createRegion(
        String regionName, int beginOffset, int endOffset, boolean testOverlap, boolean guarded
    ) throws CollabException {
        if (testOverlap) {
            //Testing overlap, if false, cannot create region
            if (!testCreateRegion(beginOffset, endOffset)) {
                return null;
            }
        }

        //correction for adjacent regions
        if (beginOffset > 0) {
            beginOffset += 1;
        }

        StyledDocument doc = getDocument();

        return new CollabTextFileHandler.CollabTextRegion(regionName, beginOffset, endOffset, guarded);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////        
    public class CollabTextRegion extends CollabRegionSupport implements CollabRegion {
        /**
         *
         * @param regionName
         * @param regionBegin
         * @param regionEnd
         * @throws CollabException
         */
        public CollabTextRegion(String regionName, int regionBegin, int regionEnd, boolean guarded)
        throws CollabException {
            super(CollabTextFileHandler.this.getDocument(), regionName, regionBegin, regionEnd, guarded);
        }

        ////////////////////////////////////////////////////////////////////////////
        // methods
        ////////////////////////////////////////////////////////////////////////////                        

        /**
         * getter for region content
         *
         * @throws CollabException
         * @return region content
         */
        public String getContent() throws CollabException {
            return super.getContent();
        }
    }
}
