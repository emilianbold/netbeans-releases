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
import org.openide.text.*;

import java.io.*;

import java.util.*;

import javax.swing.text.StyledDocument;

import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingDocumentListener;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabDocumentListener;
import org.netbeans.modules.collab.channel.filesharing.msgbean.Content;
import org.netbeans.modules.collab.channel.filesharing.msgbean.JavaChange;
import org.netbeans.modules.collab.channel.filesharing.msgbean.JavaRegion;
import org.netbeans.modules.collab.channel.filesharing.msgbean.JavaRegionChanged;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.RegionChanged;
import org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegionData;
import org.netbeans.modules.collab.core.Debug;

import org.netbeans.modules.java.*;


/**
 * FileHandler for Java files
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabJavaFileHandler extends CollabFileHandlerSupport implements CollabFileHandler {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* guarded present in the document prior to sharing */
    private HashMap otherGuardedSections = new HashMap();

    /**
     * JavaFileHandler constructor
     *
     */
    public CollabJavaFileHandler() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // File handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * isDocumentModified
     *
     */
    public boolean isDocumentModified() throws CollabException {
        boolean isModified = false;
        JavaEditor cookie = getJavaEditorCookie();

        if (cookie != null) {
            isModified = cookie.isModified();
        }

        Debug.log(this, "CollabJavaHandler, isModified: " + isModified + " for file " + getName()); //NoI18n

        return isModified;
    }

    /**
     * saveDocument
     *
     */
    public boolean saveDocument() throws CollabException {
        Debug.log(this, //NoI18n
            "CollabJavaHandler, saving document: " + getName()
        ); //NoI18n

        try {
            JavaEditor cookie = getJavaEditorCookie();

            if (cookie != null) {
                cookie.saveDocument();
            }
        } catch (IOException iox) {
            Debug.log(
                this, //NoI18n
                "CollabJavaHandler, Exception occured while saving the document: " //NoI18n
                 +getName()
            ); //NoI18n

            return false;
        }

        return true;
    }

    /**
     * lockEditor
     *
     */
    protected EditorLock lockEditor(EditorCookie editorCookie)
    throws CollabException {
        Debug.log(this, "CollabJavaHandler, locking Editor"); //NoI18n

        if ((getInitialGuardedSections() != null) && (getInitialGuardedSections().size() > 0)) { //other guarded section, return
            Debug.log(this, "CollabJavaHandler, found initial GuardedSections");
            disableUnlockTimer(true);

            return null;
        } else {
            return super.lockEditor(editorCookie);
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
        JavaRegionChanged javaRegionChanged = regionChanged.getJavaRegionChanged();
        JavaChange javaChange = javaRegionChanged.getJavaChange();
        Content content = null;

        if (javaChange.getContent() != null) {
            content = javaChange.getContent();
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
        JavaRegionChanged javaRegionChanged = regionChanged.getJavaRegionChanged();
        JavaRegion javaRegion = javaRegionChanged.getJavaRegion();
        RegionInfo regionInfo = new RegionInfo(
                javaRegion.getRegionName(), getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE,
                javaRegion.getBeginOffset().intValue(), javaRegion.getLength().intValue(), 0
            );

        return regionInfo;
    }

    /**
     * set lock-region-data with region
     *
     * @param   lockRegionData                the intial lock-region-data Node
     * @throws CollabException
     */
    protected Object setLockRegion(LockRegionData lockRegionData) {
        JavaRegion javaRegion = new JavaRegion();
        lockRegionData.setJavaRegion(javaRegion);

        return javaRegion;
    }

    /**
     * get region
     *
     * @param   lockRegionData                the intial lock-region-data Node
     * @return region
     */
    protected RegionInfo getLockRegion(LockRegionData lockRegionData) {
        JavaRegion javaRegion = lockRegionData.getJavaRegion();
        int regionBegin = javaRegion.getBeginOffset().intValue();
        int length = javaRegion.getLength().intValue();
        int regionEnd = regionBegin + length;
        RegionInfo regionInfo = new RegionInfo(
                javaRegion.getRegionName(), getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE, regionBegin,
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
        JavaRegion javaRegion = new JavaRegion();
        unlockRegionData.setJavaRegion(javaRegion);

        return javaRegion;
    }

    /**
     * get region
     *
     * @param   unlockRegionData                the intial lock-region-data Node
     * @return region
     */
    protected RegionInfo getUnlockRegion(UnlockRegionData unlockRegionData) {
        JavaRegion javaRegion = unlockRegionData.getJavaRegion();
        int regionBegin = javaRegion.getBeginOffset().intValue();
        int length = javaRegion.getLength().intValue();
        int regionEnd = regionBegin + length;
        RegionInfo regionInfo = new RegionInfo(
                javaRegion.getRegionName(), getName(), getFileGroupName(), RegionInfo.CHAROFFSET_RANGE, regionBegin,
                regionEnd, 0
            );

        return regionInfo;
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
        //find initialGuardedSections
        findInitialGuardedSections();

        if ((getInitialGuardedSections() != null) && (getInitialGuardedSections().size() > 0)) { //other guarded section, return

            return content;
        }

        return content;
    }

    /**
     * test overlap before create a JavaEditor#simpleSection
     *
     * @param beginOffset
     * @param endOffset
     * @throws CollabException
     * @return
     */
    public boolean testOverlap(int beginOffset, int endOffset)
    throws CollabException {
        PositionBounds bounds = getJavaEditorCookie().createBounds(beginOffset, endOffset, true);

        return getJavaEditorCookie().testOverlap(bounds);
    }

    /**
     * create a JavaEditor#simpleSection
     * @param beginOffset
     * @param endOffset
     * @param regionName
     * @throws CollabException
     * @return  JavaEditor#simpleSection
     */
    public CollabRegion createSimpleSection(int beginOffset, int endOffset, String regionName)
    throws CollabException {
        CollabRegion region = null;

        try {
            if (testOverlap(beginOffset, endOffset)) {
                region = super.createSimpleSection(beginOffset, endOffset, regionName);
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }

        return region;
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
    public CollabRegion createRegion(
        String regionName, int beginOffset, int endOffset, boolean testOverlap, boolean guarded
    ) throws CollabException {
        Debug.log(this, "CollabJavaFileHandler, createRegion"); //NoI18n

        if (testOverlap) {
            //Testing overlap, if false, cannot create region
            if (!testCreateRegion(beginOffset, endOffset)) {
                Debug.log(this, "CollabJavaFileHandler, testCreateRegion: false"); //NoI18n

                return null;
            }

            Debug.log(this, "CollabJavaFileHandler, testCreateRegion: true"); //NoI18n
        }

        //correction for adjacent regions
        if (beginOffset > 0) {
            beginOffset += 1;
        }

        CollabRegion region = null;

        try {
            region = new CollabJavaFileHandler.CollabJavaRegion(regionName, beginOffset, endOffset, guarded);
        } catch (Throwable th) {
            //ignore
        }

        return region;
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
        Debug.log(this, "CollabJavaFileHandler, testCreateRegion"); //NoI18n

        //check for initial guarded sections
        if (!testOverlap(beginOffset, endOffset) || !testOverlap(beginOffset + 1, endOffset)) {
            Debug.log(this, "CollabJavaFileHandler, found initial guarded section"); //NoI18n

            return false;
        }

        int length = endOffset - beginOffset;
        CollabRegion region = getContainingRegion(beginOffset, length, true);

        if (region != null) {
            if (!region.isGuarded()) {
                Debug.log(this, "CollabJavaFileHandler, region instanceof CollabJavaRegion"); //NoI18n

                return false;
            } else {
                Debug.log(this, "CollabJavaFileHandler, region instanceof JavaEditor.SimpleSection"); //NoI18n

                return testOverlap(beginOffset, endOffset);
            }
        }

        return true;
    }

    /**
     * removes all region from the repository
     *
     * @throws CollabException
     */
    public synchronized void removeAllRegion() throws CollabException {
        super.removeAllRegion();
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
        CollabJavaRegion region = (CollabJavaRegion) getRegion(regionName);

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
            throw new CollabException(ex);
        }
    }

    /**
     * getter for contentType
     *
     * @return contentType
     */
    public String getContentType() {
        return CollabJavaFileHandlerFactory.JAVA_MIME_TYPE;
    }

    /**
     * getter for filehandler
     *
     * @return        filehandler
     */
    public CollabFileHandler getFileHandler() {
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * return document object for this file
     *
     * @throws CollabException
     * @return document
     */
    public StyledDocument getDocument() throws CollabException {
        try {
            FileObject file = getFileObject();

            // Get the DataObject
            DataObject dataObject = JavaDataObject.find(file);

            if (dataObject == null) {
                throw new IllegalArgumentException("No DataObject found for file \"" + getName() + "\"");
            }

            // Get the Swing document for the file
            JavaEditor cookie = (JavaEditor) dataObject.getCookie(JavaEditor.class);

            StyledDocument document = cookie.openDocument();

            return document;
        } catch (org.openide.loaders.DataObjectNotFoundException notFound) {
            throw new CollabException(notFound);
        } catch (java.io.IOException io) {
            throw new CollabException(io);
        }
    }

    /**
     * add DocumentListener
     *
     * @throws CollabException
     * @return DocumentListener
     */
    public CollabDocumentListener addDocumentListener()
    throws CollabException {
        //add listener for this document
        StyledDocument fileDocument = getDocument();
        FilesharingDocumentListener listener = null;

        synchronized (fileDocument) {
            fileDocument.putProperty("COLLAB_FILEHANDLER_FILE_NAME", getName());
            listener = new FilesharingDocumentListener(fileDocument, createEventNotifer());
            fileDocument.addDocumentListener(listener);
        }

        return listener;
    }

    /**
     * getter for JavaEditor
     * @throws CollabException
     * @return JavaEditor
     */
    public JavaEditor getJavaEditorCookie() throws CollabException {
        return (JavaEditor) getEditorCookie();
    }

    /**
     * return editor cookie for this filehandler
     *
     * @throws CollabException
     * @return cookie
     */
    public EditorCookie getEditorCookie() throws CollabException {
        Debug.log(this, "CollabJavaHandler, " + //NoI18n
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
                DataObject dataObject = JavaDataObject.find(file);

                if (dataObject == null) {
                    throw new IllegalArgumentException("No DataObject found for file \"" + getName() + "\"");
                }

                // Get the editor cookie for the file
                editorCookie = (JavaEditor) dataObject.getCookie(JavaEditor.class);

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
     * skip insertUpdate
     *
     * @param offset
     * @return true if skip
     */
    public boolean skipInsertUpdate(int offset) {
        Debug.log(this, "CollabJavaHandler, skipInsertUpdate"); //NoI18n

        return skipInitialGuarded(offset) || super.skipInsertUpdate(offset);
    }

    /**
     * skip removeUpdate
     *
     * @param offset
     * @param length
     * @return true if skip
     */
    public boolean skipRemoveUpdate(int offset, int length) {
        Debug.log(this, "CollabJavaHandler, skipRemoveUpdate"); //NoI18n

        return skipInitialGuarded(offset) || super.skipRemoveUpdate(offset, length);
    }

    /**
     * skip initialGuarded
     *
     * @param offset
     * @param length
     * @return true if skip
     */
    protected boolean skipInitialGuarded(int offset) {
        Debug.log(
            this,
            "CollabJavaHandler, skipInitialGuarded, " + "	otherGuardedSections size: " + otherGuardedSections.size()
        ); //NoI18n

        Iterator it = this.otherGuardedSections.values().iterator();

        while (it.hasNext()) {
            Object sect = it.next();

            if (sect instanceof JavaEditor.SimpleSection) {
                JavaEditor.SimpleSection section = (JavaEditor.SimpleSection) sect;
                String name = section.getName();
                int beginOffset = section.getBegin().getOffset();
                int endOffset = section.getPositionAfter().getOffset();

                if ((offset >= beginOffset) && (offset <= endOffset)) {
                    return true;
                }
            } else {
                JavaEditor.InteriorSection section = (JavaEditor.InteriorSection) sect;
                String name = section.getName();
                int beginOffset = section.getBegin().getOffset();
                int endOffset = section.getPositionAfter().getOffset();

                if ((offset >= beginOffset) && (offset <= endOffset)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * updateDocument
     *
     */
    protected void updateDocument(String content) throws CollabException {
        Debug.log(this, "CollabJavaFileHandler, updateDocument"); //NoI18n

        if ((getInitialGuardedSections() == null) || (getInitialGuardedSections().size() == 0)) {
            Debug.log(this, "CollabJavaFileHandler, no initial guarded sections"); //NoI18n			
            super.updateDocument(content);
        } else {
            Debug.log(this, "CollabJavaFileHandler, initial guarded sections exist"); //NoI18n			
            updateFileObject(content);
            getFileObject().refresh(false);
        }
    }

    /**
     * findInitialGuardedSections
     *
     */
    protected void findInitialGuardedSections() throws CollabException {
        Debug.log(this, "CollabJavaHandler, findInitialGuardedSections"); //NoI18n

        synchronized (getDocument()) {
            Iterator it = getJavaEditorCookie().getGuardedSections();

            while (it.hasNext()) {
                Object sect = it.next();

                if (sect instanceof JavaEditor.SimpleSection) {
                    JavaEditor.SimpleSection section = (JavaEditor.SimpleSection) sect;
                    String name = section.getName();
                    Debug.log(this, "CollabJavaHandler, simple_sect: " + name); //NoI18n
                    this.otherGuardedSections.put(name, section);
                } else {
                    JavaEditor.InteriorSection section = (JavaEditor.InteriorSection) sect;
                    String name = section.getName();
                    Debug.log(this, "CollabJavaHandler, inter_sect: " + name); //NoI18n
                    this.otherGuardedSections.put(name, section);
                }
            }
        }
    }

    /**
     * getInitialGuardedSections
     *
     */
    protected HashMap getInitialGuardedSections() throws CollabException {
        return this.otherGuardedSections;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////        
    public class CollabJavaRegion extends CollabRegionSupport implements CollabRegion {
        /**
         * constructor
         *
         * @param regionName
         * @param regionBegin
         * @param regionEnd
         * @throws CollabException
         */
        public CollabJavaRegion(String regionName, int regionBegin, int regionEnd, boolean guarded)
        throws CollabException {
            super(CollabJavaFileHandler.this.getDocument(), regionName, regionBegin, regionEnd, guarded);
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
