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

import javax.swing.text.StyledDocument;

import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingDocumentListener;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabDocumentListener;


/**
 * FileHandler for XML files
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabXMLFileHandler extends CollabFileHandlerSupport implements CollabFileHandler {
    /**
     * XMLFileHandler constructor
     *
     */
    public CollabXMLFileHandler() {
        super();
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
        return CollabXMLFileHandlerFactory.XML_MIME_TYPE;
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
     * @return
     * @param testOverlap
     * @param regionName the regionName
     * @param beginOffset the beginOffset
     * @param endOffset the endOffset
     * @param testOverlap
     * @param remote
     * @throws CollabException
     */
    public CollabRegion createRegion(
        String regionName, int beginOffset, int endOffset, boolean testOverlap, boolean remote
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

        return new CollabXMLFileHandler.CollabXMLRegion(regionName, beginOffset, endOffset, remote);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////        
    public class CollabXMLRegion extends CollabRegionSupport implements CollabRegion {
        /**
         * constuctor
         *
         * @param regionName
         * @param regionBegin
         * @param regionEnd
         * @throws CollabException
         */
        public CollabXMLRegion(String regionName, int regionBegin, int regionEnd, boolean guarded)
        throws CollabException {
            super(CollabXMLFileHandler.this.getDocument(), regionName, regionBegin, regionEnd, guarded);
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
