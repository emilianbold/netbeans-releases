/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.search;

import org.openide.filesystems.FileUtil;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

/**
 * Data structure holding a reference to the found object and information
 * whether occurences in the found object should be replaced or not.
 * 
 * @author  Marian Petras
 * @author  Tim Boudreau
 */
final class MatchingObject
        implements Comparable<MatchingObject>, PropertyChangeListener {

    /** */
    private static final Logger LOG =
            Logger.getLogger(MatchingObject.class.getName());
    /** */
    private final ResultModel resultModel;
    /** */
    private final FileObject fileObject;
    /** */
    private final long timestamp;
    
    /**
     * matching object as returned by the {@code SearchGroup}
     * (usually either a {@code DataObject} or a {@code FileObject})
     */
    private final Object object;
    /**
     * charset used for full-text search of the object.
     * It is {@code null} if the object was not full-text searched.
     */
    private final Charset charset;
    
    /**
     * holds information on whether the {@code object} is selected
     * to be replaced or not.
     * Unless {@link #matchesSelection} is non-{@code null}, this field's
     * value also applies to the object's subnodes (if any).
     * 
     * @see  #matchesSelection
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
    private boolean[] matchesSelection;
    /** holds number of selected (checked) matches */
    private int selectedMatchesCount;
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
     * Creates a new {@code MatchingObject} with a reference to the found
     * object (returned by {@code SearchGroup}).
     * 
     * @param  object  found object returned by the {@code SearchGroup}
     *                 (usually a {@code DataObject}) - must not be {@code null}
     * @param  charset  charset used for full-text search of the object,
     *                  or {@code null} if the object was not full-text searched
     * @exception  java.lang.IllegalArgumentException
     *             if the passed {@code object} is {@code null}
     */
    MatchingObject(ResultModel resultModel, Object object, Charset charset) {
        if (resultModel == null) {
            throw new IllegalArgumentException("resultModel = null");   //NOI18N
        }
        if (object == null) {
            throw new IllegalArgumentException("object = null");        //NOI18N
        }
        
        this.resultModel = resultModel;
        this.object = object;
        this.charset = charset;
        
        fileObject = fileObject();
        timestamp =  fileObject.lastModified().getTime();        
        valid = (timestamp != 0L);
        
        setUpDataObjValidityChecking();
    }
    
    /**
     */
    private void setUpDataObjValidityChecking() {
        final DataObject dataObj = (DataObject) getDataObject();
        if (dataObj != null && dataObj.isValid()) {
            dataObj.addPropertyChangeListener(this);
        }
    }
    
    /**
     */
    void cleanup() {
        final DataObject dataObj = (DataObject) getDataObject();
        if(dataObj != null) {
            dataObj.removePropertyChangeListener(this);
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (DataObject.PROP_VALID.equals(e.getPropertyName())
                && Boolean.FALSE.equals(e.getNewValue())) {
            final DataObject dataObject = (DataObject) getDataObject();
            if(dataObject != null) {
                assert e.getSource() == dataObject;
                dataObject.removePropertyChangeListener(this);
            }
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
        DataObject data = getDataObject();
        return valid && data != null ? data.isValid() : false; // #190819
    }
    
    private FileObject fileObject() {
        return object instanceof FileObject ?
            (FileObject) object :
            ((DataObject) getDataObject()).getPrimaryFile();
    }

    /**
     */
    FileObject getFileObject() {
        return fileObject;
    }
    
    /**
     */
    void setSelected(boolean selected) {
        if (selected == this.selected) {
            return;
        }
        
        this.selected = selected;
        matchesSelection = null;
    }
    
    /**
     */
    boolean isSelected() {
        return selected;
    }
    
    /**
     */
    boolean isUniformSelection() {
        return matchesSelection == null;
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
        if (matchesSelection == null) {
            return Boolean.valueOf(selected);
        }
        
        final boolean firstMatchSelection = matchesSelection[0];
        for (int i = 1; i < matchesSelection.length; i++) {
            if (matchesSelection[i] != firstMatchSelection) {
                return null;
            }
        }
        return Boolean.valueOf(firstMatchSelection);
    }
    
    /**
     * 
     * @return  {@code true} if the subnode's selection change caused change
     *          of this object's node's selection, {@code false} otherwise
     */
    boolean toggleSubnodeSelection(ResultModel resultModel, int index) {
        /* uniform selection */
        if (matchesSelection == null) {
            int detailsCount = resultModel.getDetailsCount(this);
            if (detailsCount == 1) {
                selected = !selected;
                return true;
            } else {
                matchesSelection = new boolean[detailsCount];
                Arrays.fill(matchesSelection, selected);
                matchesSelection[index] = !selected;

                boolean wasSelected = selected;
                selectedMatchesCount = wasSelected ? detailsCount - 1 : 1;
                selected = true;
                return (selected != wasSelected);
            }
        }

        /* some subnodes selected, some unselected */
        assert selected;
        assert (selectedMatchesCount > 0)
               && (selectedMatchesCount < matchesSelection.length);
        boolean wasSubnodeSelected = matchesSelection[index];
        if (wasSubnodeSelected) {
            if (--selectedMatchesCount == 0) {
                matchesSelection = null;
                selected = false;
                return true;
            }
        } else {
            if (++selectedMatchesCount == matchesSelection.length) {
                matchesSelection = null;
                return false;
            }
        }

        matchesSelection[index] = !wasSubnodeSelected;
        return false;
    }
    
    /**
     */
    boolean isSubnodeSelected(int index) {
        // See #189617, #177812, #129232
        if(matchesSelection == null) {
            return selected;
        }
        if((index >= 0) && (index < matchesSelection.length)) {
            return matchesSelection[index];
        }
        LOG.log(Level.FINE,
          "Illegal index={0} in the case matchesSelection.length={1}", // NOI18N
          new Object[] { index, matchesSelection.length });
        return false; // An associated checkbox won't be selected.
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
    
    
    /** Get the name (not the path) of the file */
    String getName() {        
        return getFileObject().getNameExt();
    }

    String getHtmlDisplayName() {
        String name = null;
        if(object instanceof FileObject) {
            // TODO return HTML text instead of plain text
            return getFileObject().getNameExt();
        }
        DataObject data = (DataObject)getDataObject();
        if(data != null) {
            name = data.getNodeDelegate().getHtmlDisplayName();
        }
        return name;
    }

    /**
     */
    long getTimestamp() {
        return timestamp;
    }
    
    /**
     */
    String getDescription() { 
        return getFileObject().getParent().getPath();
    }

    /**
     */
    String getText() throws IOException {
         StringBuilder txt = text(false);
         return (txt != null)?  txt.toString() : null;
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
 
    private StringBuilder text(boolean refreshCache) throws IOException {
        assert !EventQueue.isDispatchThread();

        if (refreshCache || (text == null)) {     
            text = new StringBuilder(charset == null ? getFileObject().asText() : getFileObject().asText(charset.name()));            
        }      
        return text;
    }

    @Override
    public int compareTo(MatchingObject o) {
            if(o == null) {
                return Integer.MAX_VALUE;
            }
            return getName().compareToIgnoreCase(o.getName()); // locale?
    }

    /**
     * @return the object
     */
    public DataObject getDataObject() {
        try {
            if (object instanceof DataObject) {
                return (DataObject) object;
            } else if (object instanceof FileObject) {
                return DataObject.find((FileObject) object);
            }
            throw new IOException("Unknown object in search: " +
                                  object);// NOI18N
        } catch (IOException ex) {       
            valid = false;
            return null;
        }
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
            descr = status.getDescription(getFileObject().getPath());
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
        log(FINER, "getInvalidityStatus()");                            //NOI18N
        FileObject f = getFileObject();
        if (!f.isValid()) {
            log(FINEST, " - DELETED");            
            return InvalidityStatus.DELETED;
        }
        if (f.isFolder()) {
            log(FINEST, " - BECAME_DIR");            
            return InvalidityStatus.BECAME_DIR;
        }
        
        long stamp = f.lastModified().getTime();
        if (stamp > resultModel.getCreationTime()) {
            log(SEVERE, "file's timestamp changed since start of the search");
            if (LOG.isLoggable(FINEST)) {
                final java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTimeInMillis(stamp);
                log(FINEST, " - file stamp:           " + stamp + " (" + cal.getTime() + ')');
                cal.setTimeInMillis(resultModel.getCreationTime());
                log(FINEST, " - result model created: " + resultModel.getCreationTime() + " (" + cal.getTime() + ')');
            }            
            return InvalidityStatus.CHANGED;
        }
        
        if (f.getSize() > Integer.MAX_VALUE) {            
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
        final boolean shouldReplaceNone = (uniformSelection == Boolean.FALSE);

        if (shouldReplaceNone) {
            return null;
        }
       
        StringBuilder content = text(true);   //refresh the cache, reads the file      
        List<TextDetail> textMatches = 
                resultModel.basicCriteria.getTextDetails(getFileObject());

        int offsetShift = 0;
        for (int i=0; i < textMatches.size(); i++) {
            if ((matchesSelection != null) && !matchesSelection[i]){
                continue;
            }
            TextDetail textDetail = textMatches.get(i);
            String matchedSubstring = content.substring(textDetail.getStartOffset() + offsetShift, textDetail.getEndOffset() + offsetShift);
            if (!matchedSubstring.equals(textDetail.getMatchedText())) {
                log(SEVERE, "file match part differs from the expected match");  //NOI18N
                if (LOG.isLoggable(FINEST)) {
                    log(SEVERE, " - expected line: \""                           //NOI18N
                                + textDetail.getMatchedText()
                                + '"');
                    log(SEVERE, " - file line:     \""                           //NOI18N
                                + matchedSubstring
                                + '"');
                }
                return InvalidityStatus.CHANGED;
            }

            String replacedString = resultModel.basicCriteria.getReplaceExpr();
            if (resultModel.basicCriteria.isRegexp()){
                Matcher m = resultModel.basicCriteria.getTextPattern().matcher(matchedSubstring);
                replacedString = m.replaceFirst(resultModel.basicCriteria.getReplaceString());
            } else if (resultModel.basicCriteria.isPreserveCase()) {
                replacedString = adaptCase(replacedString, matchedSubstring);
            }
            
            content.replace(textDetail.getStartOffset() + offsetShift, textDetail.getEndOffset() + offsetShift, replacedString);
            offsetShift += replacedString.length() - matchedSubstring.length();
        }
        return null;
    }
    
    /** Modify case of a string according to a case pattern. Used in "Search
     *  and replace" action when "Preserve case" option is checked. 
     * 
     * Code copied from method {@link 
     * org.netbeans.modules.editor.lib2.search.DocumentFinder#preserveCaseImpl
     * DocumentFinder.preserveCaseImpl}
     * in module editor.lib2.
     * 
     * @param value String that should modified.
     * @param casePattern Case pattern.
     * @return 
     */
    public static String adaptCase(String value, String casePattern) {
                                                
        if (casePattern.equals(casePattern.toUpperCase())) {
            return value.toUpperCase();
        } else if (casePattern.equals(casePattern.toLowerCase())) {
            return value.toLowerCase();
        } else if (Character.isUpperCase(casePattern.charAt(0))) {
            return Character.toUpperCase(value.charAt(0)) + value.substring(1);
        } else if (Character.isLowerCase(casePattern.charAt(0))) {
            if (casePattern.substring(1).equals(casePattern.substring(1).toUpperCase())) {
                return Character.toLowerCase(value.charAt(0)) + value.substring(1).toUpperCase();
            } else {
                return Character.toLowerCase(value.charAt(0)) + value.substring(1);
            }
        } else {
            return value;
        }
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
            Writer writer = null;
            try {
                writer = new OutputStreamWriter(
                        fileObject.getOutputStream(fileLock),
                        charset);
                writer.write(makeStringToWrite());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } else {
            System.err.println("Would write to " + getFileObject().getPath());//NOI18N
            System.err.println(text);
        }
    }

    /**
     */
    private String makeStringToWrite() {
        return makeStringToWrite(text);
    }
    
    /**
     */
    static String makeStringToWrite(StringBuilder text) {
        return text.toString();
    }

    /**
     */
    private void log(Level logLevel, String msg) {
        String id = (getDataObject() instanceof DataObject)
                    ? ((DataObject) getDataObject()).getName()
                    : getDataObject().toString();
        if (LOG.isLoggable(logLevel)) {
            LOG.log(logLevel, "{0}: {1}", new Object[]{id, msg});       //NOI18N
        }
    }
    
    /** Returns name of this node.
     * @return name of this node.
     */
    @Override
    public String toString() {
        return super.toString() + "[" + getName()+ "]"; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MatchingObject other = (MatchingObject) obj;
        if (this.fileObject != other.fileObject && (this.fileObject == null || !this.fileObject.equals(other.fileObject))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + (this.fileObject != null ? this.fileObject.hashCode() : 0);
        return hash;
    }

    /** Get number of matches in this matching object.  */
    int getMatchesCount() {
        return resultModel.getDetailsCount(this);
    }

    String getRelativeSearchPath() {

        FileObject searchRoot = resultModel.getCommonSearchFolder();
        FileObject fileFolder = fileObject.getParent();

        String fileDisplayName = FileUtil.getFileDisplayName(fileFolder);

        if (searchRoot == null) {
            return fileDisplayName;
        } else {
            return FileUtil.getRelativePath(searchRoot, fileFolder);
        }
    }

    /** Get file display name, e.g. for JTree tooltip. */
    String getFileDisplayName() {
        return FileUtil.getFileDisplayName(fileObject);
    }
}
