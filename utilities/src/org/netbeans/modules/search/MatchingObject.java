/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * Data structure holding a reference to the found object and information
 * whether occurences in the found object should be replaced or not.
 * 
 * @author  Marian Petras
 * @author  Tim Boudreau
 */
final class MatchingObject implements PropertyChangeListener {
    
    /** */
    private final ResultModel resultModel;
    /** */
    private final File file;
    /** */
    private final long timestamp;
    
    /**
     * matching object as returned by the {@code SearchGroup}
     * (usually a {@code DataObject})
     */
    final Object object;
    
    /**
     * holds information on whether the {@code object} is selected
     * to be replaced or not.
     * Unless {@link #selectedMatches} is non-{@code null}, this field's
     * value also applies to the object's subnodes (if any).
     * 
     * @see  #selectedMatches
     */
    private boolean selected = true;
    /**
     * holds information on whether the node representing this object
     * is expanded or collapsed
     * 
     * @see  #markExpanded(boolean)
     */
    private boolean expanded = false;
    /**
     * holds information about which matches should be replaced and which
     * should not be replaced.
     * Value {@code null} means that either all or none matches are selected,
     * depending on the value of field {@link #selected}.
     * 
     * @see  #selected
     */
    private boolean[] selectedMatches;
    /**
     * flag that indicates that the tree was not notified of this
     * {@code MatchingObject}'s children's selection change and that
     * it must be notified before the children nodes are made visible
     * 
     * @see  #markChildrenSelectionDirty()
     */
    private boolean childrenSelectionDirty;
    /** */
    private boolean valid = true;
    /** */
    private StringBuilder text;
    
    /**
     * {@code true} if the file's line terminator is other than {@code "\\n"}
     */
    boolean wasCrLf = false;
    
    /**
     * Creates a new {@code MatchingObject} with a reference to the found
     * object (returned by {@code SearchGroup}).
     * 
     * @param  object  found object returned by the {@code SearchGroup}
     *                 (usually a {@code DataObject}) - must not be {@code null}
     * @exception  java.lang.IllegalArgumentException
     *             if the passed {@code object} is {@code null}
     */
    MatchingObject(ResultModel resultModel, Object object) {
        if (resultModel == null) {
            throw new IllegalArgumentException("resultModel = null");   //NOI18N
        }
        if (object == null) {
            throw new IllegalArgumentException("object = null");        //NOI18N
        }
        
        this.resultModel = resultModel;
        this.object = object;
        
        FileObject fileObject = getFileObject();
        file = FileUtil.toFile(fileObject);
        timestamp = (file != null) ? file.lastModified() : 0L;
        valid = (timestamp != 0L);
        
        setUpDataObjValidityChecking();
    }
    
    /**
     */
    private void setUpDataObjValidityChecking() {
        final DataObject dataObj = (DataObject) object;
        if (dataObj.isValid()) {
            dataObj.addPropertyChangeListener(this);
        }
    }
    
    /**
     */
    void cleanup() {
        final DataObject dataObj = (DataObject) object;
        dataObj.removePropertyChangeListener(this);
    }
    
    public void propertyChange(PropertyChangeEvent e) {
        if (DataObject.PROP_VALID.equals(e.getPropertyName())
                && Boolean.FALSE.equals(e.getNewValue())) {
            assert e.getSource() == (DataObject) object;
            
            final DataObject dataObj = (DataObject) object;
            dataObj.removePropertyChangeListener(this);
            
            resultModel.objectBecameInvalid(this);
        }
    }
    
    /**
     * Is the {@code DataObject} encapsulated by this {@code MatchingObject}
     * valid?
     * 
     * @return  {@code true} if the {@code DataObject} is valid, false otherwise
     * @see  DataObject#isValid
     */
    boolean isObjectValid() {
        return ((DataObject) object).isValid();
    }
    
    private FileObject getFileObject() {
        return ((DataObject) object).getPrimaryFile();
    }
    
    /**
     */
    void setSelected(boolean selected) {
        if (selected == this.selected) {
            return;
        }
        
        this.selected = selected;
        selectedMatches = null;
    }
    
    /**
     */
    boolean isSelected() {
        return selected;
    }
    
    /**
     */
    boolean isUniformSelection() {
        return selectedMatches == null;
    }
    
    /**
     * Checks selection of this object's subnodes.
     * 
     * @return  {@code Boolean.TRUE}  if all subnodes are selected,
     *          {@code Boolean.FALSE}  if all subnodes are unselected,
     *          {@code null} if some subnodes are selected and some are
     *                       unselected
     */
    Boolean checkSubnodesSelection() {
        if (selectedMatches == null) {
            return Boolean.valueOf(selected);
        }
        
        final boolean firstMatchSelection = selectedMatches[0];
        for (int i = 1; i < selectedMatches.length; i++) {
            if (selectedMatches[i] != firstMatchSelection) {
                return null;
            }
        }
        return Boolean.valueOf(firstMatchSelection);
    }
    
    /**
     */
    void toggleSubnodeSelection(ResultModel resultModel, int index) {
        if (selectedMatches == null) {
            selectedMatches = new boolean[resultModel.getDetailsCount(this)];
            Arrays.fill(selectedMatches, this.selected);
        }
        selectedMatches[index] = !selectedMatches[index];
    }
    
    /**
     */
    void setSubnodeSelected(int index,
                            boolean selected,
                            ResultModel resultModel) {
        if (selectedMatches == null) {
            if (selected == this.selected) {
                return;
            }
            selectedMatches = new boolean[resultModel.getDetailsCount(this)];
            Arrays.fill(selectedMatches, this.selected);
        }
        
        assert (index >= 0) && (index < selectedMatches.length);
        selectedMatches[index] = selected;
    }
    
    /**
     */
    boolean isSubnodeSelected(int index) {
        assert (selectedMatches == null)
               || ((index >= 0) && (index < selectedMatches.length));
        return (selectedMatches == null) ? selected
                                         : selectedMatches[index];
    }
    
    @Override
    public boolean equals(Object anotherObject) {
        return (anotherObject != null)
               && (anotherObject.getClass() == MatchingObject.class)
               && (((MatchingObject) anotherObject).object == this.object);
    }
    
    @Override
    public int hashCode() {
        return object.hashCode() + 1;
    }

    /**
     * Sets the {@link #childrenSelectionDirty} flag.
     * 
     * @see  #markChildrenSelectionClean
     */
    void markChildrenSelectionDirty() {
        childrenSelectionDirty = true;
    }
    
    /**
     * Clears the {@link #childrenSelectionDirty} flag.
     * 
     * @see  #markChildrenSelectionDirty()
     */
    void markChildrenSelectionClean() {
        childrenSelectionDirty = false;
    }
    
    /**
     * Is the {@link #childrenSelectionDirty} flag set?
     * 
     * @return  {@code true} if the flag is set, {@code false} otherwise
     * @see  #markChildrenSelectionDirty()
     */
    boolean isChildrenSelectionDirty() {
        return childrenSelectionDirty;
    }
    
    /**
     * Stores information whether the node representing this object is expanded
     * or collapsed.
     * 
     * @param  expanded  {@code true} if the node is expanded,
     *                   {@code false} if the node is collapsed
     * @see  #isExpanded()
     */
    void markExpanded(boolean expanded) {
        this.expanded = expanded;
    }
    
    /**
     * Provides information whether the node representing this object
     * is expanded or collapsed.
     * 
     * @return  {@code true} if the node is expanded,
     *          {@code false} if the node is collapsed
     * @see  #markExpanded
     */
    boolean isExpanded() {
        return expanded;
    }
    
    /**
     */
    File getFile() {
        return file;
    }
    
    /** Get the name (not the path) of the file */
    String getName() {
        return getFile().getName();
    }

    /**
     */
    long getTimestamp() {
        return timestamp;
    }
    
    /**
     */
    String getDescription() {
        return getFile().getParent();
    }

    /**
     */
    String getText() throws IOException {
        StringBuilder txt = text();
        if (txt != null) {
            return txt.toString();
        } else {
            return null;
        }
    }

    /**
     */
    FileLock lock() throws IOException {
        return getFileObject().lock();
    }
    
    /**
     * Reads the file if it has not been read already.
     * 
     * @author  TimBoudreau
     * @author  Marian Petras
     */
    private StringBuilder text() throws IOException {
        return text(false);
    }
    
    private StringBuilder text(boolean refreshCache) throws IOException {
        assert !EventQueue.isDispatchThread();
        
        if (refreshCache || (text == null)) {
            text = readText();
        }
        return text == null ? new StringBuilder() : text;
    }
    
    /**
     * Reads the text from the file.
     * 
     * @return  {@code StringBuilder} containing the text file's content,
     *          or {@code null} if reading was interrupted
     * @exception  java.io.IOException  if some error occured while reading
     *                                  the file
     */
    private StringBuilder readText() throws IOException {
        StringBuilder ret = null;
        
        ByteBuffer buf = getByteBuffer();
        if (buf != null) {
            Charset charset = BasicSearchCriteria.getCharset(getFileObject());
            CharBuffer cbuf = decodeByteBuffer(buf, charset);
            String terminator
                    = System.getProperty("line.separator");         //NOI18N

            if (!terminator.equals("\n")) {                         //NOI18N
                Matcher matcher = Pattern.compile(terminator).matcher(cbuf);
                if (matcher.find()) {
                    wasCrLf = true;
                    matcher.reset();
                    ret = new StringBuilder(
                                        matcher.replaceAll("\n"));  //NOI18N
                }
            }
            if (ret == null) {
                ret = new StringBuilder(cbuf);
            }
        }
        return ret;
    }

    /**
     * 
     * @author  Tim Boudreau
     */
    private ByteBuffer getByteBuffer() throws IOException {
        assert !EventQueue.isDispatchThread();
        
        File file = getFile();
        //XXX optimize with a single shared bytebuffer if performance
        //problems noted
        FileInputStream str = new FileInputStream(file);

        ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
        FileChannel channel = str.getChannel();
        try {
            channel.read(buffer, 0);
        } catch (ClosedByInterruptException cbie) {
            return null;        //this is actually okay
        } finally {
            channel.close();
        }
        buffer.rewind();
        return buffer;
    }
    
    /**
     * Describes invalidity status of this item.
     */
    enum InvalidityStatus {
        
        DELETED(true, "Inv_status_Err_deleted"),                        //NOI18N
        BECAME_DIR(true, "Inv_status_Err_became_dir"),                  //NOI18N
        CHANGED(false, "Inv_status_Err_changed"),                       //NOI18N
        TOO_BIG(false, "Inv_status_Err_too_big"),                       //NOI18N
        CANT_READ(false, "Inv_status_Err_cannot_read");                 //NOI18N
        
        /**
         * Is this invalidity the fatal one?
         * 
         * @see  #isFatal()
         */
        private final boolean fatal;
        /**
         * resource bundle key for the description
         * 
         * @see  #getDescription(String)
         */
        private final String descrBundleKey;
        
        /**
         * Creates an invalidity status.
         * 
         * @param  fatal  whether this status means that the invalidity is fatal
         * @see  #isFatal()
         */
        private InvalidityStatus(boolean fatal, String descrBundleKey) {
            this.fatal = fatal;
            this.descrBundleKey = descrBundleKey;
        }
        
        /**
         * Is this invalidity fatal such that the item should be removed
         * from the search results?
         */
        boolean isFatal() {
            return fatal;
        }
        
        /**
         * Provides human-readable description of this invalidity status.
         * 
         * @param  path  path or name of file that has this invalidity status
         * @return  description of the invalidity status with the given path
         *          or name embedded
         */
        String getDescription(String path) {
            return NbBundle.getMessage(getClass(), descrBundleKey, path);
        }
        
    }
    
    /**
     */
    InvalidityStatus checkValidity() {
        InvalidityStatus status = getInvalidityStatus();
        if (status != null) {
            valid = false;
        }
        return status;
    }
    
    /**
     */
    String getInvalidityDescription() {
        String descr;
        
        InvalidityStatus status = getInvalidityStatus();
        if (status != null) {
            descr = status.getDescription(getFile().getPath());
        } else {
            descr = null;
        }
        return descr;
    }
    
    /**
     * Check validity status of this item.
     * 
     * @return  an invalidity status of this item if it is invalid,
     *          or {@code null} if this item is valid
     * @author  Tim Boudreau
     * @author  Marian Petras
     */
    private InvalidityStatus getInvalidityStatus() {
        File f = getFile();
        if (!f.exists()) {
            return InvalidityStatus.DELETED;
        }
        
        if (f.isDirectory()) {
            return InvalidityStatus.BECAME_DIR;
        }
        
        long stamp = f.lastModified();
        if (stamp > resultModel.getCreationTime()) {
            return InvalidityStatus.CHANGED;
        }
        
        if (f.length() > Integer.MAX_VALUE) {
            return InvalidityStatus.TOO_BIG;
        }
        
        if (!f.canRead()) {
            return InvalidityStatus.CANT_READ;
        }
        
        return null;
    }
    
    /**
     */
    boolean isValid() {
        return valid;
    }

    /**
     */
    public InvalidityStatus replace() throws IOException {
        assert !EventQueue.isDispatchThread();
        assert isSelected();
        
        Boolean uniformSelection = checkSubnodesSelection();
        final boolean shouldReplaceAll = (uniformSelection == Boolean.TRUE);
        final boolean shouldReplaceNone = (uniformSelection == Boolean.FALSE);
        
        if (shouldReplaceNone) {
            return null;
        }
        
        StringBuilder content = text(true);   //refresh the cache, reads the file
        
        List<TextDetail> textMatches = resultModel.basicCriteria
                                                    .getTextDetails(object);
        int matchIndex = 0;

        int currLineOffset = 0;
        int currLine = 1;

        int inlineMatchNumber = 0;      //order of a match in a line
        int inlineOffsetShift = 0;      //shift of offsets caused by replacements

        mainloop:
        for (TextDetail textDetail : textMatches) {
            int matchLine = textDetail.getLine();

            while (currLine < matchLine) {
                int lfOffset = content.indexOf("\n",currLineOffset);//NOI18N
                if (lfOffset == -1) {
                    assert false;       //PENDING - should notify user
                    break mainloop;
                }

                currLineOffset = lfOffset + 1;       //skips "\n"
                currLine++;
                inlineMatchNumber = 0;
                inlineOffsetShift = 0;
            }

            if (!isSubnodeSelected(matchIndex++)) {
                continue;
            }

            if (++inlineMatchNumber == 1) { //first selected match on a line
                boolean check = false;
                assert check = true;    //side-effect - turns the check on
                if (check) {
                    int lineEndOffset = content.indexOf("\n",       //NOI18N
                                                        currLineOffset);
                    String fileLine = (lineEndOffset != -1)
                                      ? content.substring(currLineOffset,
                                                          lineEndOffset)
                                      : content.substring(currLineOffset);
                    if (!fileLine.equals(textDetail.getLineText())) {
                        return InvalidityStatus.CHANGED;
                    }
                }
            }

            int matchLength = textDetail.getMarkLength();
            int matchOffset = currLineOffset + inlineOffsetShift
                              + (textDetail.getColumn() - 1);
            int matchEndOffset = matchOffset + matchLength;
            if (!content.substring(matchOffset, matchEndOffset)
                .equals(textDetail.getLineText().substring(
                                textDetail.getColumn() - 1,
                                textDetail.getColumn() - 1 + matchLength))) {
                return InvalidityStatus.CHANGED;
            }
            
            content.replace(matchOffset, matchEndOffset,
                            resultModel.replaceString);
            inlineOffsetShift += resultModel.replaceString.length()
                                 - matchLength;
        }
        return null;
    }
    
    /** debug flag */
    private static final boolean REALLY_WRITE = true;

    /**
     */
    void write(final FileLock fileLock) throws IOException {
        if (text == null) {
            throw new IllegalStateException("Buffer is gone");          //NOI18N
        }
        
        if (REALLY_WRITE) {
            if (wasCrLf) {
                String terminator
                        = System.getProperty("line.separator");         //NOI18N
                //XXX use constant - i.e. on mac, only \r, etc.
                text = new StringBuilder(
                        text.toString().replace("\n", terminator));     //NOI18N
            }
            final FileObject fileObject = getFileObject();
            Writer writer = null;
            try {
                writer = new OutputStreamWriter(
                        fileObject.getOutputStream(fileLock),
                        BasicSearchCriteria.getCharset(fileObject));
                writer.write(text.toString());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } else {
            System.err.println("Would write to " + getFile().getPath());//NOI18N
            System.err.println(text);
        }
    }

    /**
     * Decodes a given {@code ByteBuffer} with a given charset decoder.
     * This is a workaround for a broken
     * {@link Charset.decode(ByteBuffer) Charset#decode(java.nio.ByteBuffer}
     * method in JDK 1.5.x.
     * 
     * @param  in  {@code ByteBuffer} to be decoded
     * @param  charset  charset whose decoder will be used for decoding
     * @return  {@code CharBuffer} containing chars produced by the decoder
     * @see  <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/nio/charset/Charset.html#decode(java.nio.ByteBuffer)">Charset.decode(ByteBuffer)</a>
     * @see  <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6221056">JDK bug #6221056</a>
     * @see  <a href="http://www.netbeans.org/issues/show_bug.cgi?id=103193">NetBeans bug #103193</a>
     * @see  <a href="http://www.netbeans.org/issues/show_bug.cgi?id=103067">NetBeans bug #103067</a>
     */
    private CharBuffer decodeByteBuffer(final ByteBuffer in,
                                        final Charset charset)
            throws CharacterCodingException {
        
        final CharsetDecoder decoder = charset.newDecoder()
                                       .onMalformedInput(CodingErrorAction.REPLACE)
                                       .onUnmappableCharacter(CodingErrorAction.REPLACE);
        
	int remaining = in.remaining();
        if (remaining == 0) {
            return CharBuffer.allocate(0);
        }
        
        int n = (int) (remaining * decoder.averageCharsPerByte());
        if (n < 16) {
            n = 16;            //make sure some CharBuffer is allocated
                               //even when decoding small number of bytes
                               //and averageCharsPerByte() is less than 1
        }
	CharBuffer out = CharBuffer.allocate(n);
        
	decoder.reset();
	for (;;) {
	    CoderResult cr = in.hasRemaining()
                             ? decoder.decode(in, out, true)
                             : CoderResult.UNDERFLOW;
	    if (cr.isUnderflow()) {
		cr = decoder.flush(out);
            }
	    if (cr.isUnderflow()) {
		break;
            }
	    if (cr.isOverflow()) {
		CharBuffer o = CharBuffer.allocate(n <<= 1);
		out.flip();
		o.put(out);
		out = o;
		continue;
	    }
	    cr.throwException();
	}
	out.flip();
	return out;
    }
    
    /** Returns name of this node.
     * @return name of this node.
     */
    @Override
    public String toString() {
        return super.toString() + "[" + getName()+ "]"; // NOI18N
    }
}
