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

import org.openide.loaders.*;
import org.openide.text.*;

import java.io.*;

import javax.swing.text.*;

import org.netbeans.modules.collab.channel.filesharing.ui.FilesharingCollabletFactorySettings;
import org.netbeans.modules.collab.core.Debug;


/**
 * Support class for all class implements CollabRegion
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabRegionSupport extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* region Name */
    protected String regionName;

    /* region Begin */
    protected Position regionBegin;

    /* region End */
    protected Position regionEnd;

    /* is guarded region */
    protected boolean guarded;

    /* create guarded section if guarded true */
    protected Object sect = null;

    /* FileHandler this region belongs*/
    protected CollabFileHandler fh = null;
    protected StyledDocument fileDocument = null;

    /* flag to indicate region change */
    protected boolean changed = false;

    /* interval count, initially set to value resolved through getInterval() */
    protected int leastRecentlyUsedCount = getInterval();

    /* isValid, if false then this region is invalid */
    protected boolean isValid = true;

    /**
     * constructor
     *
     * @param fh
     * @param regionName
     * @param regionBegin
     * @param regionEnd
     */
    public CollabRegionSupport(
        CollabFileHandler fh, String regionName, int regionBegin, int regionEnd, boolean guarded
    ) {
        super();
        this.fh = fh;
        this.regionName = regionName;
        this.guarded = guarded;

        try {
            if (fh != null) {
                this.fileDocument = fh.getDocument();
            }

            this.regionBegin = NbDocument.createPosition(getDocument(), regionBegin, Position.Bias.Forward);
            this.regionEnd = NbDocument.createPosition(getDocument(), regionEnd, Position.Bias.Forward);

            if ((this.regionBegin == null) || (this.regionEnd == null)) {
                throw new IllegalArgumentException("Region creation failed for: " + regionName);
            }
        } catch (javax.swing.text.BadLocationException ex) {
            throw new IllegalArgumentException("Region creation failed for: " + regionName);
        } catch (CollabException ce) {
            throw new IllegalArgumentException("Region creation failed for: " + regionName);
        }
    }

    /**
     * constructor
     *
     * @param fileDocument
     * @param regionName
     * @param regionBegin
     * @param regionEnd
     */
    public CollabRegionSupport(StyledDocument doc, String regionName, int regionBegin, int regionEnd, boolean guarded) {
        super();
        this.regionName = regionName;
        this.fileDocument = doc;
        this.guarded = guarded;

        try {
            this.regionBegin = NbDocument.createPosition(getDocument(), regionBegin, Position.Bias.Forward);
            this.regionEnd = NbDocument.createPosition(getDocument(), regionEnd, Position.Bias.Forward);

            if ((this.regionBegin == null) || (this.regionEnd == null)) {
                throw new IllegalArgumentException("Region creation failed for: " + regionName);
            }
        } catch (javax.swing.text.BadLocationException ex) {
            throw new IllegalArgumentException("Region creation failed for: " + regionName);
        }

        if (isGuarded()) {
            sect = new SimpleSection(getDocument(), regionName, regionBegin, regionEnd);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * getter for regionName
     *
     * @return regionName
     */
    public String getID() {
        return regionName;
    }

    /**
     * getter for guarded
     *
     * @return guarded
     */
    public boolean isGuarded() {
        return this.guarded;
    }

    /**
     * setter for guarded section
     *
     * @return guarded
     */
    public void setGuard(Object sect) {
        this.sect = sect;
        this.guarded = true;
    }

    /**
     * getter for guarded section
     *
     * @return guarded
     */
    public Object getGuard() {
        return this.sect;
    }

    /**
     * getter for regionBegin
     *
     * @return regionBegin
     */
    public int getBeginOffset() {
        if (isGuarded() && (sect != null) && sect instanceof SimpleSection) {
            return ((SimpleSection) sect).getBegin();
        } else {
            return regionBegin.getOffset();
        }
    }

    /**
     * getter for regionEnd
     *
     * @return regionEnd
     */
    public int getEndOffset() {
        if (isGuarded() && (sect != null) && sect instanceof SimpleSection) {
            return ((SimpleSection) sect).getPositionAfter() - 1;
        } else {
            return regionEnd.getOffset();
        }
    }

    /**
     * getter for regionBegin
     *
     * @return regionBegin
     */
    public void setBeginOffset(int regionBegin) {
        try {
            this.regionBegin = NbDocument.createPosition(getDocument(), regionBegin, Position.Bias.Forward);

            if (this.regionBegin == null) {
                throw new IllegalArgumentException("Region beginOffset update failed for: " + this.regionName);
            }
        } catch (javax.swing.text.BadLocationException ex) {
            throw new IllegalArgumentException("Region beginOffset update failed for: " + this.regionName);
        }
    }

    /**
     * getter for regionEnd
     *
     * @return regionEnd
     */
    public void setEndOffset(int regionEnd) {
        try {
            this.regionEnd = NbDocument.createPosition(getDocument(), regionEnd, Position.Bias.Forward);

            if (this.regionEnd == null) {
                throw new IllegalArgumentException("Region endOffset update failed for: " + this.regionName);
            }
        } catch (javax.swing.text.BadLocationException ex) {
            throw new IllegalArgumentException("Region endOffset update failed for: " + this.regionName);
        }
    }

    /**
    * setDocument
    *
    * @param        fileDocument
    */
    public void setDocument(StyledDocument fileDocument) {
        this.fileDocument = fileDocument;
    }

    /**
    * getDocument
    *
    * @return        fileDocument
    */
    public StyledDocument getDocument() {
        return this.fileDocument;
    }

    /**
     * getter for region content
     *
     * @throws CollabException
     * @return region content
     */
    public String getContent() throws CollabException {
        String content = null;

        try {
            int length = getEndOffset() - getBeginOffset();
            content = getDocument().getText(getBeginOffset(), length);
        } catch (javax.swing.text.BadLocationException ex) {
            throw new CollabException(ex);
        }

        return content;
    }

    /**
     * test if region changed
     *
     * @return true if region changed
     */
    public boolean isChanged() {
        return this.changed;
    }

    /**
     * API to change status
     *
     * @param change, if true then isChanged() returns true
     */
    public void updateStatusChanged(boolean change) {
        this.changed = change;
        this.leastRecentlyUsedCount = getInterval();
    }

    /**
     * test if region is ready to unlock (ready for removal)
     *
     * @return true if region is ready to unlock (ready for removal)
     */
    public synchronized boolean isReadyToUnlock() {
        leastRecentlyUsedCount--;

        if (leastRecentlyUsedCount <= 0) {
            leastRecentlyUsedCount = getInterval();

            return true;
        }

        return false;
    }

    /**
     * set ready to unlock (ready for removal)
     *
     */
    public synchronized void setReadyToUnlock() {
        leastRecentlyUsedCount = 0;
    }

    /**
     * getter for region unlock interval
     *
     * @return interval
     */
    public int getInterval() {
        int interval = FilesharingCollabletFactorySettings.getDefault().getLockTimeoutInterval().intValue();

        if ((interval < 3) || (interval > 60)) {
            interval = 3;
        }

        return interval;
    }

    /**
     * addAnnotation
     *
     * @param dataObject
     * @param annotation
     */
    public void addAnnotation(
        DataObject dataObject, CollabFileHandler fileHandler /*CollabRegionAnnotation lineAnnotation*/, int style,
        String annotationMessage
    ) throws CollabException {
        Debug.log(this, "CollabRegionSupport, adding Annotation for region: " + //NoI18n
            regionName
        );

        int beginLineIndex = getDocument().getDefaultRootElement().getElementIndex(getBeginOffset());
        int endLineIndex = getDocument().getDefaultRootElement().getElementIndex(getEndOffset() - 1);

        for (int lineIndex = beginLineIndex; lineIndex <= endLineIndex; lineIndex++) {
            Debug.log(this, "CollabRegionSupport, lineIndex: " + lineIndex); //NoI18n

            CollabLineRegion lineRegion = ((CollabFileHandlerSupport) fileHandler).getLineRegion(lineIndex);

            if (lineRegion == null) {
                continue;
            }

            lineRegion.addAnnotation(dataObject, fileHandler, style, annotationMessage);
        }
    }

    /**
     * removeAnnotation
     *
     */
    public void removeAnnotation() {
        if (fh == null) {
            return;
        }

        int beginLineIndex = getDocument().getDefaultRootElement().getElementIndex(getBeginOffset());
        int endLineIndex = getDocument().getDefaultRootElement().getElementIndex(getEndOffset() - 1);

        for (int lineIndex = beginLineIndex; lineIndex <= endLineIndex; lineIndex++) {
            Debug.log(this, "CollabRegionSupport, lineIndex: " + lineIndex); //NoI18n

            CollabLineRegion lineRegion = ((CollabFileHandlerSupport) fh).getLineRegion(lineIndex);

            if (lineRegion == null) {
                continue;
            }

            lineRegion.removeAnnotation();
        }
    }

    /**
     * setValid
     *
     * @param        status                                        if false handler is invalid
     * @throws CollabException
     */
    public void setValid(boolean valid) {
        this.isValid = valid;
    }

    /**
     * getValid
     *
     * @return        status                                        if false handler is invalid
     * @throws CollabException
     */
    public boolean isValid() {
        return this.isValid;
    }

    /** Represents a simple guarded section.
    * It consists of one contiguous block.
    */
    public final class SimpleSection extends Object {
        /** Text range of the guarded section. */
        StyledDocument doc;
        String name;
        int beginOffset;
        int endOffset;
        boolean valid = true;

        /** Creates new section.
        * @param name Name of the new section.
        * @param bounds The range of the section.
        */
        SimpleSection(StyledDocument doc, String name, int beginOffset, int endOffset) {
            super();
            this.doc = doc;
            this.name = name;
            this.beginOffset = beginOffset;
            this.endOffset = endOffset;
            markGuarded(CollabRegionSupport.this.getDocument());
        }

        /** Get the name of the section.
        * @return the name
        */
        public String getName() {
            return name;
        }

        /** Deletes the text in the section.
        * @exception BadLocationException
        * @exception IOException
        */
        void deleteText() throws BadLocationException, IOException {
            doc.insertString(beginOffset, "", null); // NOI18N
        }

        /** Marks the section as guarded.
        * @param doc The styled document where this section placed in.
        */
        void markGuarded(StyledDocument doc) {
            markGuarded(doc, beginOffset, endOffset, true);
        }

        /** Unmarks the section as guarded.
        * @param doc The styled document where this section placed in.
        */
        void unmarkGuarded(StyledDocument doc) {
            markGuarded(doc, beginOffset, endOffset, false);
        }

        /** Gets the begin of section. To this position is set the cursor
        * when section is open in the editor.
        * @return the begin position of section.
        */
        public int getBegin() {
            return beginOffset;
        }

        /** Gets the text contained in the section.
        * @return The text contained in the section.
        */
        public String getText() {
            StringBuffer buf = new StringBuffer();

            try {
                buf.append(doc.getText(beginOffset, endOffset - beginOffset));
            } catch (Exception e) {
            }

            return buf.toString();
        }

        /**
         * set region content
         *
         * @param text
         * @throws CollabException
         * @return
         */
        public int setText(String text) throws CollabException {
            int p1 = getBegin();
            int p2 = p1 + getText().length();

            if (p1 > 0) {
                p1 -= 1;
            }

            Debug.out.println("CFHS:: updateText: p1: " + p1 + " p2:" + p2 + " text: [" + text + "]");

            StyledDocument doc = CollabRegionSupport.this.fileDocument;

            //remove guard
            unmarkGuarded(doc);

            int len = text.length();

            try {
                if (len == 0) { // 1) set empty string

                    if (p2 > p1) {
                        doc.remove(p1, p2 - p1);
                    }
                } else { // 2) set non empty string

                    // [MaM] remember doclen to compute new length
                    // of the inserted string (the length changes
                    // because insertString removes \r characters
                    // from it)
                    int docLen = doc.getLength();

                    if ((p2 - p1) >= 2) {
                        Debug.out.println("CFHS:: before insert: [" + doc.getText(0, doc.getLength()) + "]");
                        doc.insertString(p1 + 1, text, null);
                        Debug.out.println("CFHS:: after insert: [" + doc.getText(0, doc.getLength()) + "]");

                        // [MaM] compute length of inserted string
                        len = doc.getLength() - docLen - 1;

                        NbDocument.unmarkGuarded(doc, p1 + 1 + len, p2 - p1 - 1);
                        Debug.out.println("CFHS:: removing text: [" + doc.getText(p1 + 1 + len, p2 - p1 - 1) + "]");
                        doc.remove(p1 + 1 + len, p2 - p1 - 1);

                        NbDocument.unmarkGuarded(doc, p1, 1);
                        Debug.out.println("CFHS:: removing text: [" + doc.getText(p1, 1) + "]");
                        doc.remove(p1, 1);
                    } else {
                        // zero or exactly one character:
                        // adjust the positions if they are
                        // biased to not absorb the text inserted at the start/end
                        // it would be ridiculous not to have text set by setText
                        // be part of the bounds.
                        Debug.out.println("CFHS:: before insert: [" + doc.getText(0, doc.getLength()) + "]");
                        doc.insertString(p1, text, null);
                        Debug.out.println("CFHS:: after insert: [" + doc.getText(0, doc.getLength()) + "]");

                        // [MaM] compute length of inserted string
                        len = doc.getLength() - docLen - 1;

                        if (p2 > p1) {
                            Debug.out.println("CFHS:: removing text: [" + doc.getText(p1 + len, p2 - p1) + "]");
                            doc.remove(p1 + len, p2 - p1);
                        }
                    }
                }
            } catch (javax.swing.text.BadLocationException e) {
                Debug.out.println("exception: " + e);
                e.printStackTrace(Debug.out);
            }

            return len;
        }

        /**
         *
         * @return
         */
        public int getPositionAfter() {
            return endOffset;
        }

        /**
         *
         * @param pos
         * @param allowHoles
         * @return
         */
        public boolean contains(int pos, boolean allowHoles) {
            return (beginOffset <= pos) && (endOffset >= pos);
        }

        /**
         *
         * @return
         */
        public int getPositionBefore() {
            return beginOffset;
        }

        /**
                 * Tests if the section is still valid - it is not removed from the
                 * source.
                 * @return
                 */
        public boolean isValid() {
            return valid;
        }

        /**
         * Removes the section from the Document, but retains the text contained
         * within. The method should be used to unprotect a region of code
         * instead of calling NbDocument.
         * @return true if the operation succeeded.
         */
        public boolean removeSection() {
            synchronized (this) {
                if (!valid) {
                    return false;
                }

                // get document should always return the document, when section
                // is deleted, because it is still valid (and valid is only
                // when document is loaded.
                unmarkGuarded(doc);
                valid = false;

                return true;
            }
        }

        /** Marks or unmarks the section as guarded.
        * @param doc The styled document where this section placed in.
        * @param bounds The rangeof text which should be marked or unmarked.
        * @param mark true means mark, false unmark.
        */
        void markGuarded(StyledDocument doc, int beginOffset, int endOffset, boolean mark) {
            int begin = beginOffset;
            int end = endOffset;

            if (mark) {
                NbDocument.markGuarded(doc, begin, end - begin);
            } else {
                NbDocument.unmarkGuarded(doc, begin, end - begin);
            }
        }

        /** Shifts a simpleSectoin.
        * @param length.
        */
        void shiftSection(int length) {
            this.beginOffset += length;

            if (this.beginOffset < 0) {
                this.beginOffset = 0;
            }

            this.endOffset += length;

            if (this.endOffset < 0) {
                this.endOffset = 0;
            }
        }
    }
}
