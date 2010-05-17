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
import java.util.Vector;
import org.netbeans.modules.collab.channel.filesharing.annotations.*;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;

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

	private boolean isEndOpen = false;

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
    ) throws CollabException {
		this(fh.getDocument(), regionName, regionBegin, regionEnd, guarded);
		this.fh=fh;
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
            this.regionBegin = NbDocument.createPosition(getDocument(), regionBegin, Position.Bias.Backward);
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
            int beginLineIndex=getDocument().getDefaultRootElement().getElementIndex(getBeginOffset());
            Element beginLine=getDocument().getDefaultRootElement().getElement(beginLineIndex);
            int endLineIndex=getDocument().getDefaultRootElement().getElementIndex(getEndOffset());
            Element endLine=getDocument().getDefaultRootElement().getElement(endLineIndex);              
            Debug.log("CollabFileHandlerSupport","CRS, getContent " +
                "begin : " + getBeginOffset()+
                "end : " + getEndOffset()+
                "actual line begin: "+beginLine.getStartOffset()+
                "actual line end: "+endLine.getEndOffset()); //NoI18n 
			
            //int length = getEndOffset() - getBeginOffset();			
			int beginOffset = getBeginOffset();
			int endOffset = getEndOffset();
			//if(endOffset<endLine.getEndOffset())
			//	endOffset = endLine.getEndOffset();			
			int length = endOffset - beginOffset;
			
            content = getDocument().getText(beginOffset/*getBeginOffset()*/, length);
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
     * createRegionAnnotation
     *
     * @param   message
     * @return   Annotation
     */
    public Annotation createRegionAnnotation(int style, String annotationMessage) { 
        CollabRegionAnnotation regionAnnotation = null; 
        switch(style) {
            case -1: regionAnnotation = new RegionHistoryAnnotation(); 
                break; 
            case 0: regionAnnotation = new RegionAnnotation1(); 
                break; 
            case 1: regionAnnotation = new RegionAnnotation2(); 
                break; 
            case 2: regionAnnotation = new RegionAnnotation3(); 
                break;                   
            case 3: regionAnnotation = new RegionAnnotation4(); 
                break;                   
            case 4: regionAnnotation = new RegionAnnotation5(); 
                break;                   
            case 5: regionAnnotation = new RegionAnnotation6(); 
                break;                   
            case 6: regionAnnotation = new RegionAnnotation7(); 
                break;                   
            case 7: regionAnnotation = new RegionAnnotation8(); 
                break;                   
            case 8: regionAnnotation = new RegionAnnotation9(); 
                break;                                           
            default: regionAnnotation = new RegionAnnotation1(); 
                 break;                   
        } 
        regionAnnotation.setShortDescription(annotationMessage); 
        return regionAnnotation; 
    } 
    
    /** 
     * addAnnotation
     *
     * @param dataObject
     * @param annotation
     */
    public void addAnnotation(
        DataObject dataObject, CollabFileHandler fileHandler, int style,
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

	void setEndOpenRegion(boolean isEndOpen) {
		this.isEndOpen=isEndOpen;
	}

	boolean isEndOpenRegion() {
		return isEndOpen;
	}

    /** Represents a simple guarded section.
    * It consists of one contiguous block.
    */
    public final class SimpleSection extends Object {
        /** Text range of the guarded section. */
        StyledDocument doc;
        String name;
        Position beginOffset;
        Position endOffset;
        boolean valid = true;

        /** Creates new section.
        * @param name Name of the new section.
        * @param bounds The range of the section.
        */
        SimpleSection(StyledDocument doc, String name, int beginOffset, int endOffset) {
            super();
            this.doc = doc;
            this.name = name;
            try {
                this.beginOffset = NbDocument.createPosition(getDocument(),
                        beginOffset,Position.Bias.Forward);
                this.endOffset = NbDocument.createPosition(getDocument(),
                        endOffset,Position.Bias.Forward);
                if(this.beginOffset==null || this.endOffset==null) {
                    throw new IllegalArgumentException("Region creation failed for: "+regionName);
                }
            } catch(javax.swing.text.BadLocationException ex) {
                throw new IllegalArgumentException("Region creation failed for: "+regionName);
            }
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
            doc.insertString(getBegin(), "", null); // NOI18N
        }

        /** Marks the section as guarded.
        * @param doc The styled document where this section placed in.
        */
        void markGuarded(StyledDocument doc) {
            markGuarded(doc, getBegin(), getPositionAfter(), true);
        }

        /** Unmarks the section as guarded.
        * @param doc The styled document where this section placed in.
        */
        void unmarkGuarded(StyledDocument doc) {
            markGuarded(doc, getBegin(), getPositionAfter(), false);
        }

        /** Gets the begin of section. To this position is set the cursor
        * when section is open in the editor.
        * @return the begin position of section.
        */
        public int getBegin() {
            return beginOffset.getOffset();
        }

        /** Gets the text contained in the section.
        * @return The text contained in the section.
        */
        public String getText() {
            StringBuffer buf = new StringBuffer();

            try {
                buf.append(doc.getText(getBegin(), (getPositionAfter()-1)-getBegin()));
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
        public int setText(final String text) throws CollabException {
            final int[] ret = {0};
            final CollabException[] ce = {null};
            
            NbDocument.runAtomic(CollabRegionSupport.this.fileDocument, new Runnable() {
                public void run() {
                    try {
                        int p1 = getBegin();
                        int p2 = getPositionAfter()-1;

			//now do setText			
                        int len = setText(text, p1, p2);
			
			//Now fix the corruption (if any) after update
			String currText=getText();			
			if(text.length()!=currText.length())
			{
				Debug.out.println("Document corrupted by update, \n\nprevious: ["+
					text.replaceAll("\n", "~n")+"], \ncurrent : ["+currText.replaceAll("\n", "~n")+"]\n");				
				int pLen=text.length();
				int cLen=currText.length();
				if(pLen < cLen)
				{
					int beginRm=p1+pLen;
					int rmLen=cLen-pLen;
					try {
						if(doc.getLength() >= beginRm+rmLen)
						{
							Debug.out.println("removing text: ["+doc.getText(beginRm, rmLen).replaceAll("\n", "~n")+"]");							
							doc.remove(beginRm, rmLen);
						}
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					}
				}
				else if(pLen > cLen)
				{
					int beginRm=p1+cLen;
					try {
						String addText="\n";
						Debug.out.println("adding text: ["+addText.replaceAll("\n", "~n")+"]");							
						doc.insertString(beginRm, addText, null);
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					}					
				}
			}
			ret[0] = len;
                    } catch (CollabException ce) {
                        
                    }
                }
            
            });
            
            // rethrow internal exception, if any
            if (ce[0] != null) throw ce[0];

            return ret[0];
        }
		
        /**
         * set region content
         *
         * @param text
         * @throws CollabException
         * @return
         */
        private int setText(final String text, int p1, int p2) throws CollabException {
            if (Debug.isEnabled())
                Debug.log("CollabRegionSupport", "CRS:: updateText: p1: " + p1 + " p2:" + p2 + " text: [" + text + "]");

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
						int np1=p1 + 1;						
                        //Debug.out.println("CFHS:: before insert: docLen: "+ doc.getLength()+": [" + doc.getText(0, doc.getLength()) + "]");
                        doc.insertString(np1, text, null);
                        //Debug.out.println("CFHS:: after insert: docLen: "+ doc.getLength()+":[" + doc.getText(0, doc.getLength()) + "]");

                        // [MaM] compute length of inserted string
                        len = doc.getLength() - docLen;


						int sfBegin=np1 + len;
						int sfSize=p2 - np1;
                        NbDocument.unmarkGuarded(doc, sfBegin, sfSize);
                        //Debug.out.println("CFHS:: removing text: [" + doc.getText(sfBegin, sfSize) + "]");
                        doc.remove(sfBegin, sfSize);

                        NbDocument.unmarkGuarded(doc, p1, 1);
                        //Debug.out.println("CFHS:: removing text: [" + doc.getText(p1, 1) + "]");
                        doc.remove(p1, 1);
						
                    } else {
                        // zero or exactly one character:
                        // adjust the positions if they are
                        // biased to not absorb the text inserted at the start/end
                        // it would be ridiculous not to have text set by setText
                        // be part of the bounds.
                        //Debug.out.println("CFHS:: before insert: [" + doc.getText(0, doc.getLength()) + "]");
                        doc.insertString(p1, text, null);
                        //Debug.out.println("CFHS:: after insert: [" + doc.getText(0, doc.getLength()) + "]");

                        // [MaM] compute length of inserted string
                        len = doc.getLength() - docLen;

                        if (p2 > p1) {
                            //Debug.out.println("CFHS:: removing text: [" + doc.getText(p1 + len, p2 - p1) + "]");
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
            return endOffset.getOffset();
        }

        /**
         *
         * @param pos
         * @param allowHoles
         * @return
         */
        public boolean contains(int pos, boolean allowHoles) {
            return (getBegin() <= pos) && (getPositionAfter() >= pos);
        }

        /**
         *
         * @return
         */
        public int getPositionBefore() {
            return beginOffset.getOffset();
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
    }
}
