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

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openidex.search.SearchPattern;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;

/**
 * Class encapsulating basic search criteria.
 * 
 * @author  Marian Petras
 */
final class BasicSearchCriteria {

    /**
     * maximum size of file of unrecognized file that will be searched.
     * Files of uknown type that whose size exceed this limit will be considered
     * binary and will not be searched.
     */
    private static final int MAX_UNRECOGNIZED_FILE_SIZE = 5 * (1 << 20); //5 MiB

    private static int instanceCounter;
    private final int instanceId = instanceCounter++;
    private static final Logger LOG = Logger.getLogger(
            "org.netbeans.modules.search.BasicSearchCriteria");         //NOI18N

    /** array of searchable application/x-<em>suffix</em> MIME-type suffixes */
    private static final Collection<String> searchableXMimeTypes;

    /** List of currently processed searches. */
    private List<BufferedCharSequence> currentlyProcessedSequences =
            new ArrayList<BufferedCharSequence>(1);
    
    /** Termination flag */
    private boolean terminated = false;
    
    static {
        searchableXMimeTypes = new HashSet<String>(17);
        searchableXMimeTypes.add("csh");                                //NOI18N
        searchableXMimeTypes.add("httpd-eruby");                        //NOI18N
        searchableXMimeTypes.add("httpd-php");                          //NOI18N
        searchableXMimeTypes.add("httpd-php-source");                   //NOI18N
        searchableXMimeTypes.add("javascript");                         //NOI18N
        searchableXMimeTypes.add("latex");                              //NOI18N
        searchableXMimeTypes.add("php");                                //NOI18N
        searchableXMimeTypes.add("sh");                                 //NOI18N
        searchableXMimeTypes.add("tcl");                                //NOI18N
        searchableXMimeTypes.add("tex");                                //NOI18N
        searchableXMimeTypes.add("texinfo");                            //NOI18N
        searchableXMimeTypes.add("troff");                              //NOI18N
    }

    private String textPatternExpr;
    private String fileNamePatternExpr;
    private String replaceExpr;
    private String replaceString;
    private boolean wholeWords;
    private boolean caseSensitive;
    private boolean regexp;
    private boolean preserveCase;
    
    private boolean textPatternSpecified = false;
    private boolean fileNamePatternSpecified = false;
    
    private boolean textPatternValid = false;
    private boolean replacePatternValid = false;
    private boolean fileNamePatternValid = false;
    
    private Pattern textPattern;
    private Pattern fileNamePattern;
    
    private boolean criteriaUsable = false;
    
    private ChangeListener usabilityChangeListener;

    private static Pattern patternCR = Pattern.compile("\r"); //NOI18N
    private static Pattern patternLineSeparator =
                                     Pattern.compile("(?:\r\n|\n|\r)"); //NOI18N

    /**
     * holds {@code Charset} that was used for full-text search of the last
     * tested file
     */
    private Charset lastCharset = null;

    /**
     * Holds information about occurences of matching strings within individual
     * {@code FileObject}s.
     */
    private Map<FileObject, List<TextDetail>> detailsMap;

    /**
     * Holds a {@code DataObject} that will be used to create
     * a {@code TextDetail}. It should be set to {@code null} immediately after
     * all {@code TextDetail}s are created for given {@code DataObject}.
     *
     * @see #findDataObject(org.openide.filesystems.FileObject)
     * @see #freeDataObject()
     */
    private DataObject dataObject;

    BasicSearchCriteria() {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "#{0}: <init>()", instanceId);              //NOI18N
        }
    }
    
    /**
     * Copy-constructor.
     * 
     * @param  template  template to create a copy from
     */
    BasicSearchCriteria(BasicSearchCriteria template) {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "#{0}: <init>(template)", instanceId);      //NOI18N
        }

        /* check-boxes: */
        setCaseSensitive(template.caseSensitive);
        setWholeWords(template.wholeWords);
        setRegexp(template.regexp);
        setPreserveCase(template.preserveCase);

        /* combo-boxes: */
        setTextPattern(template.textPatternExpr);
        setFileNamePattern(template.fileNamePatternExpr);
        setReplaceExpr(template.replaceExpr);
    }
    
    /**
     * Returns a {@link Pattern} object corresponding to the substring pattern
     * specified in the criteria.
     * 
     * @return  {@code Pattern} object, or {@code null} if no pattern has been
     *          specified
     */
    Pattern getTextPattern() {
        if (!textPatternValid) {
            return null;
        }
        
        if (textPattern != null) {
            return textPattern;
        }
        
        /* So now we know that the pattern is valid but not compiled. */
        if (regexp) {
            textPatternValid = compileRegexpPattern();
        } else {
            compileSimpleTextPattern();
            textPatternValid = (textPattern != null);
        }
        assert textPattern != null;
        return textPattern;     //may be null in case of invalid pattern
    }
    
    String getTextPatternExpr() {
        return textPatternExpr != null ? textPatternExpr : "";          //NOI18N
    }
    
    /**
     * Sets a text pattern. Whether it is considered a simple pattern or
     * a regexp pattern, is determined by the current <em>regexp</em> setting
     * (see {@link #setRegexp(boolean)}).
     * 
     * @param  pattern  pattern to be set
     */
    void setTextPattern(String pattern) {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER,
                    "setTextPattern({0}{1}",
                    new Object[]{pattern, ')'});               //NOI18N
        }
        if ((pattern != null) && (pattern.length() == 0)) {
            pattern = null;
        }
        if ((pattern == null) && (textPatternExpr == null)
               || (pattern != null) && pattern.equals(textPatternExpr)) {
            LOG.finest(" - no change");                                 //NOI18N
            return;
        }
        
        if (pattern == null) {
            textPatternExpr = null;
            textPattern = null;
            textPatternSpecified = false;
            textPatternValid = false;
        } else {
            textPatternExpr = pattern;
            textPatternSpecified = true;
            if (!regexp) {
                textPattern = null;
                textPatternValid = true;
            } else {
                textPatternValid = compileRegexpPattern();
                assert (textPattern != null) || !textPatternValid;
            }
        }
        replacePatternValid = validateReplacePattern();
        updateUsability();
    }
    
    /**
     * Tries to compile the regular expression pattern, thus checking its
     * validity. In case of success, the compiled pattern is stored
     * to {@link #textPattern}, otherwise the field is set to {@code null}.
     *
     * <p>Actually, this method defines a pattern used in searching, i.e. it
     * defines behaviour of the searching. It should be the same as behavior of
     * the Find action (Ctrl+F) in the Editor to avoid any confusions
     * (see Bug #175101).
     * Hence, this implementation should specify default flags in
     * the call of the method {@link Pattern#compile(java.lang.String, int)
     * java.util.regex.Pattern.compile(String regex, int flags)} that are the
     * same as in the implementation of the Find action
     * (i.e in the method {@code getFinder} of the class
     * {@code org.netbeans.modules.editor.lib2.search.DocumentFinder}).
     * </p>
     * 
     * @return  {@code true} if the regexp pattern expression was valid;
     *          {@code false} otherwise
     */
    private boolean compileRegexpPattern() {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "#{0}: compileRegexpPattern()", instanceId);//NOI18N
        }
        assert regexp;
        assert textPatternExpr != null;
        try {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST, " - textPatternExpr = \"{0}{1}",
                        new Object[]{textPatternExpr, '"'});  //NOI18N
            }
            int flags = 0;
            if (!caseSensitive) {
                flags |= Pattern.CASE_INSENSITIVE;
                flags |= Pattern.UNICODE_CASE;
            }
            flags |= Pattern.MULTILINE; // #175101
            textPattern = Pattern.compile(textPatternExpr, flags);
            return true;
        } catch (PatternSyntaxException ex) {
            LOG.finest(" - invalid regexp - " +
                       "setting 'textPattern' to <null>");  //NOI18N
            textPattern = null;
            return false;
        }
    }

    private boolean validateReplacePattern(){
        if (regexp && textPatternValid){
            int groups = getTextPattern().matcher("").groupCount();
            String tmpSearch = "";
            for(int i=1; i <= groups; i++){
                tmpSearch += "(" + i + ")";
            }
            try{
                Pattern.compile(tmpSearch).matcher("123456789").
                                                      replaceFirst(replaceExpr);
            }catch(Exception e){
                return false;
            }
        }
        return true;
    }

    /**
     * Translates the simple text pattern to a regular expression pattern
     * and compiles it. The compiled pattern is stored to field
     * {@link #textPattern}.
     */
    private void compileSimpleTextPattern() {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "#{0}: compileRegexpPattern()", instanceId);//NOI18N
        }
        assert textPatternExpr != null;
        try {
            int flags = 0;
            if (!caseSensitive) {
                flags |= Pattern.CASE_INSENSITIVE;
                flags |= Pattern.UNICODE_CASE;
            }
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST, " - textPatternExpr = \"{0}{1}",
                        new Object[]{textPatternExpr, '"'});  //NOI18N
            }
	    String searchRegexp = RegexpMaker.makeRegexp(textPatternExpr,
                                                         wholeWords);
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST, " - regexp = \"{0}{1}",
                        new Object[]{searchRegexp, '"'});      //NOI18N
            }
            textPattern = Pattern.compile(searchRegexp, flags);
        } catch (PatternSyntaxException ex) {
            LOG.finest(" - invalid regexp");                           //NOI18N
            assert false;
            textPattern = null;
        }
    }
    
    boolean isRegexp() {
        return regexp;
    }
    
    boolean isPreserveCase() {
        return preserveCase;
    }
    
    void setPreserveCase(boolean preserveCase) {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "setPreservecase({0}{1}",
                    new Object[]{preserveCase, ')'});                     //NOI18N
        }
        if (preserveCase == this.preserveCase) {
            LOG.finest(" - no change");                                //NOI18N
            return;
        }
        
        this.preserveCase = preserveCase;
        
        if (!regexp) {
            textPattern = null;
        }        
    }
    
    void setRegexp(boolean regexp) {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "setRegexp({0}{1}",
                    new Object[]{regexp, ')'});                         //NOI18N
        }
        if (regexp == this.regexp) {
            LOG.finest(" - no change");                                //NOI18N
            return;
        }
        
        this.regexp = regexp;
        
        if (textPatternExpr != null) {
            if (regexp) {
                textPatternValid = compileRegexpPattern();
            } else {
                textPatternValid = true;
                textPattern = null;
            }
        }
        replacePatternValid = validateReplacePattern();
        updateUsability();
    }
    
    boolean isWholeWords() {
        return wholeWords;
    }
    
    void setWholeWords(boolean wholeWords) {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "setWholeWords({0}{1}",
                    new Object[]{wholeWords, ')'});                     //NOI18N
        }
        if (wholeWords == this.wholeWords) {
            LOG.finest(" - no change");                                //NOI18N
            return;
        }
        
        this.wholeWords = wholeWords;
        
        if (!regexp) {
            textPattern = null;
        }
    }
    
    boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    void setCaseSensitive(boolean caseSensitive) {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "setCaseSensitive({0}{1}",
                    new Object[]{caseSensitive, ')'});       //NOI18N
        }
        if (caseSensitive == this.caseSensitive) {
            LOG.finest(" - no change");                                 //NOI18N
            return;
        }
        
        this.caseSensitive = caseSensitive;
        
        textPattern = null;
    }

    boolean isFullText() {
        return textPatternValid;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * Returns a {@link Pattern} object corresponding to the file name pattern
     * or set of patterns specified.
     * 
     * @return  {@code Pattern} object, or {@code null} if no pattern has been
     *          specified
     */
    Pattern getFileNamePattern() {
        if (!fileNamePatternValid) {
            return null;
        }
        
        assert (fileNamePatternExpr != null) &&
               (fileNamePatternExpr.length() != 0);
        
        if (fileNamePattern != null) {
            return fileNamePattern;
        }
        
        /* So now we know that the pattern is valid but not compiled. */
        compileSimpleFileNamePattern();
        assert fileNamePattern != null;
        return fileNamePattern;
    }
    
    String getFileNamePatternExpr() {
        return fileNamePatternExpr != null ? fileNamePatternExpr : "";  //NOI18N
    }
    
    void setFileNamePattern(String pattern) {
        if ((pattern != null) && (pattern.length() == 0)) {
            pattern = null;
        }
        if ((pattern == null) && (fileNamePatternExpr == null)
                || (pattern != null) && pattern.equals(fileNamePatternExpr)) {
            return;
        }
        
        if (pattern == null) {
            fileNamePatternExpr = null;
            fileNamePattern = null;
            fileNamePatternSpecified = false;
            fileNamePatternValid = false;
        } else {
            fileNamePatternExpr = pattern;
            fileNamePattern = null;
            fileNamePatternSpecified =
                    checkFileNamePattern(fileNamePatternExpr);
            fileNamePatternValid = fileNamePatternSpecified;
        }
        updateUsability();
    }
    
    /**
     * Translates the file name pattern to a regular expression pattern
     * and compiles it. The compiled pattern is stored to field
     * {@link #fileNamePattern}.
     */
    private void compileSimpleFileNamePattern() {
        assert fileNamePatternExpr != null;
        try {
            fileNamePattern =
               Pattern.compile(RegexpMaker.makeMultiRegexp(fileNamePatternExpr),
                               Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException ex) {
            assert false;
            fileNamePattern = null;
        }
    }
    
    /**
     * Checks validity of the given file name pattern.
     * The pattern is claimed to be valid if it contains at least one
     * non-separator character. Separator characters are {@code ' '} (space)
     * and {@code ','} (comma).
     * 
     * @param  fileNamePatternExpr  pattern to be checked
     * @return  {@code true} if the pattern is valid, {@code false} otherwise
     */
    private static boolean checkFileNamePattern(String fileNamePatternExpr) {
        if (fileNamePatternExpr.length() == 0) {
            return false;                               //trivial case
        }
        
        for (char c : fileNamePatternExpr.toCharArray()) {
            if ((c != ',') && (c != ' ')) {
                return true;
            }
        }
        return false;
    }

    //--------------------------------------------------------------------------

    boolean isSearchAndReplace() {
        return replaceExpr != null;
    }
    
    /**
     * Returns the replacement expression.
     * 
     * @return  replace expression, or {@code null} if no replace expression has
     *          been specified
     */
    String getReplaceExpr() {
        return replaceExpr;
    }

    /**
     * Returns the replacement string.
     *
     * @return  replace string, or {@code null} if no replace string has been
     *          specified
     */
    String getReplaceString() {
        if ((replaceString == null) && (replaceExpr != null)){
            String[] sGroups =
                   replaceExpr.split("\\\\\\\\", replaceExpr.length()); //NOI18N
            String res = "";                         //NOI18N
            for(int i=0;i<sGroups.length;i++){
                String tmp = sGroups[i];
                tmp = tmp.replace("\\" + "r", "\r"); //NOI18N
                tmp = tmp.replace("\\" + "n", "\n"); //NOI18N
                tmp = tmp.replace("\\" + "t", "\t"); //NOI18N
                res += tmp;
                if (i != sGroups.length - 1){
                    res += "\\\\";                   //NOI18N
                }
            }
            this.replaceString = res;
        }
        return replaceString;
    }

    /**
     * Sets a replacement string/expression.
     *
     * @param  replaceExpr  string to replace matches with, or {@code null}
     *                        if no replacing should be performed
     */
    void setReplaceExpr(String replaceExpr) {
        this.replaceExpr = replaceExpr;
        this.replaceString = null;
        this.replacePatternValid = validateReplacePattern();
    }

    //--------------------------------------------------------------------------
    
    private void updateUsability() {
        boolean wasUsable = criteriaUsable;
        criteriaUsable = isUsable();
        if (criteriaUsable != wasUsable) {
            fireUsabilityChanged();
        }
    }
    
    boolean isUsable() {
        return (textPatternSpecified || 
                (!isSearchAndReplace() && fileNamePatternSpecified)) &&
                !isInvalid();
    }
    
    private boolean isInvalid() {
        return isTextPatternInvalid() || isFileNamePatternInvalid();
    }
    
    void setUsabilityChangeListener(ChangeListener l) {
        this.usabilityChangeListener = l;
    }
    
    private void fireUsabilityChanged() {
        if (usabilityChangeListener != null) {
            usabilityChangeListener.stateChanged(new ChangeEvent(this));
        }
    }
    
    boolean isTextPatternUsable() {
        return textPatternSpecified && textPatternValid;
    }
    
    boolean isTextPatternInvalid() {
        return textPatternSpecified && !textPatternValid;
    }

    boolean isReplacePatternInvalid() {
        return !replacePatternValid;
    }

    boolean isFileNamePatternUsable() {
        return fileNamePatternSpecified && fileNamePatternValid;
    }

    boolean isFileNamePatternInvalid() {
        return fileNamePatternSpecified && !fileNamePatternValid;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * Called when the criteria in the Find dialog are confirmed by the user
     * and the search is about to be started.
     * Makes sure everything is ready for searching, e.g. regexp patterns
     * are compiled.
     */
    void onOk() {
        LOG.finer("onOk()");                                            //NOI18N
        if (textPatternValid && (textPattern == null)) {
            if (regexp){
                compileRegexpPattern();
            }else{
                compileSimpleTextPattern();
            }
        }
        if (fileNamePatternValid && (fileNamePattern == null)) {
            compileSimpleFileNamePattern();
        }
        
        assert !textPatternValid || (textPattern != null);
        assert !fileNamePatternValid || (fileNamePattern != null);
        synchronized (this) {
            terminated = false;
            currentlyProcessedSequences.clear();
        }
    }
    
    /**
     * Checks whether the given {@code DataObject} matches this criteria.
     * If the check includes full-text search, charset used for the search
     * is remembered. It may be later obtained using method
     * {@link #getUsedCharset}. If the check did not include full-text search,
     * the charset is {@code null}-ed.
     * 
     * @param  dataObj  {@code DataObject} to be checked
     * @return  {@code true} if the {@code DataObject} matches the given
     *          criteria, {@code false} otherwise
     */
    boolean matches(DataObject dataObj) {
        return matches(dataObj.getPrimaryFile());
    }

    boolean matches(FileObject fileObj) {
        lastCharset = null;

        if (!fileObj.isValid()) {
            return false;
        }
        
        /* If replacing, skip read-only files  (including files in archives): */
        if (!fileObj.canWrite() && isSearchAndReplace()) {
            LOG.log(Level.INFO, "Read-only file {0} was skipped", fileObj);
            return false;
        }
        
        if (fileObj.isFolder() || !fileObj.isValid() ||
                (isFullText() && !isTextFile(fileObj))) {
            return false;
        }

        /* Check the file name: */
        if (fileNamePatternValid 
                && !fileNamePattern.matcher(fileObj.getNameExt()).matches()) {
            return false;
        }
        
        /* Check the file's content: */
        if (textPatternValid
                && !checkFileContent(fileObj)) {
            return false;
        }
        
        return true;
    }

    /**
     * Returns {@code Charset} used for decoding of the last tested file.
     * 
     * @return  {@code Charset} used for decoding of the last tested file,
     *          or {@code null} if no file has been tested since creation
     *          of this object or if the last tested file was not full-text
     *          searched
     */
    Charset getLastUsedCharset() {
        return lastCharset;
    }

    /**
     * Checks whether the given file is a text file.
     * The current implementation does the check by the file's MIME-type.
     *
     * @param  fileObj  file to be checked
     * @return  {@code true} if the file is a text file;
     *          {@code false} if it is a binary file
     */
    private static boolean isTextFile(FileObject fileObj) {
        String mimeType = fileObj.getMIMEType();
        
        if (mimeType.equals("content/unknown")) {                       //NOI18N
            return fileObj.getSize() <= MAX_UNRECOGNIZED_FILE_SIZE;
        }

        if (mimeType.startsWith("text/")) {                             //NOI18N
            return true;
        }

        if (mimeType.startsWith("application/")) {                      //NOI18N
            final String subtype = mimeType.substring(12);
            return subtype.equals("rtf")                                //NOI18N
                   || subtype.equals("sgml")                            //NOI18N
                   || subtype.startsWith("xml-")                        //NOI18N
                   || subtype.endsWith("+xml")                          //NOI18N
                   || subtype.startsWith("x-")                          //NOI18N
                      && searchableXMimeTypes.contains(subtype.substring(2));
        }

        return false;
    }
    
    /**
     * Checks whether the file's content matches the text pattern.
     * 
     * @param  fo the file whose content is to be checked
     * @return  {@code true} if the file contains at least one substring
     *          matching the pattern, {@code false} otherwise
     */
    private boolean checkFileContent(FileObject fo) {
        assert fo != null;
        assureMemory(fo);
        lastCharset = FileEncodingQuery.getEncoding(fo);
        SearchPattern sp = createSearchPattern();
        BufferedCharSequence bcs = null;
        try {         
            InputStream stream = fo.getInputStream();
            bcs = new BufferedCharSequence(stream, lastCharset.newDecoder(), fo.getSize());
            registerProcessedSequence(bcs);
            ArrayList<TextDetail> txtDetails = getTextDetails(bcs, fo, sp);
            unregisterProcessedSequence(bcs);
            if (txtDetails.isEmpty()){                
                return false;
            }            
            getDetailsMap().put(fo, txtDetails);
            freeDataObject();
            return true;
        }
        catch(DataObjectNotFoundException e){
            LOG.log(Level.SEVERE,
                    "Unable to get data object for the {0}", fo); // NOI18N
            LOG.throwing(BasicSearchCriteria.class.getName(),
                    "checkFileContent", e); // NOI18N
        }
        catch (FileNotFoundException e) {
            LOG.log(Level.SEVERE,
                    "Unable to get input stream for the {0}", fo); // NOI18N
            LOG.throwing(BasicSearchCriteria.class.getName(),
                    "checkFileContent", e); // NOI18N
        }
        catch(BufferedCharSequence.SourceIOException e){
            LOG.log(Level.SEVERE,
                    "IOException during process for the {0}", fo); // NOI18N
            LOG.throwing(BasicSearchCriteria.class.getName(),
                    "checkFileContent", e); // NOI18N
        }
        catch(Exception e){
            LOG.log(Level.SEVERE,
                    "Unexpected Exception during process for the {0}",
                    fo); // NOI18N
            LOG.throwing(BasicSearchCriteria.class.getName(),
                    "checkFileContent", e); // NOI18N
        }
        finally {
            if(bcs != null) {
                try {
                    bcs.close();
                } catch (IOException ex) {
                    // do nothing
                }
            }
        }
        return false;
    }

    /** Assure that there is enough available memory for searching in file.
     * Idea and code copied from org.openidex.search.DataObjectSearchGroup.
     * Uses rough estimate of memory requirements based on file size. 
     */
    private void assureMemory(FileObject fo) {

        long fileSize = fo.getSize();
        long requiredEstimate = fileSize * 3;
        if ((int) requiredEstimate < 0) {
            // Integer overflow.
            throwNoMemoryExeption(fo, fileSize);
        }
        if (getAvailableMemory() < requiredEstimate) {
            System.gc();
            try {
                byte[] gcProvocation = new byte[(int) requiredEstimate];
                gcProvocation[(int) fileSize] = 42;
                gcProvocation = null;
            } catch (OutOfMemoryError ooe) {
                throwNoMemoryExeption(fo, fileSize);
            }
        }
    }

    /** Get approximation of available memory in bytes. */
    private long getAvailableMemory() {

        Runtime rt = Runtime.getRuntime();

        long max = rt.maxMemory();
        long free = rt.freeMemory();
        long total = rt.totalMemory();

        long available = (max - total) + free;
        return available;
    }

    /** Throw an exception telling that there is not enough memory for searching
     * in a file. 
     */
    private void throwNoMemoryExeption(FileObject fo, long fileSize) {
        String text = NbBundle.getMessage(
                BasicSearchCriteria.class, "TEXT_MSG_NOT_ENOUGH_MEMORY",
                fo.getNameExt(), fileSize / 1024);
        throw new RuntimeException(text);
    }

    private ArrayList<TextDetail> getTextDetails(BufferedCharSequence bcs,
                                                 FileObject fo,
                                                 SearchPattern sp)
                                throws BufferedCharSequence.SourceIOException,
                                       DataObjectNotFoundException {

        ArrayList<TextDetail> txtDetails = new ArrayList<TextDetail>();
        FindState fs = new FindState(bcs);

        final int limit = ResultModel.Limit.MATCHES_COUNT_LIMIT.getValue();
        Matcher matcher = textPattern.matcher(bcs);
        while (matcher.find() && txtDetails.size() < limit) {
            int matcherStart = matcher.start();

            int column = fs.calcColumn(matcherStart);
            int lineNumber = fs.getLineNumber();
            String lineText = fs.getLineText();

            findDataObject(fo);
            TextDetail det = newTextDetail(sp, matcher);
            det.associate(lineNumber, column, lineText);

            txtDetails.add(det);
        } // while (matcher.find())
        txtDetails.trimToSize();
        return txtDetails;
    }

    private TextDetail newTextDetail(SearchPattern searchPattern,
                                     Matcher matcher) {
        String group = matcher.group();
        int start = matcher.start();
        int end = matcher.end();
        int countCR = countCR(group);
        int markLength = end - start - countCR;
        assert dataObject != null;
        TextDetail det = new TextDetail(dataObject, searchPattern);
        det.setMatchedText(group);
        det.setStartOffset(start);
        det.setEndOffset(end);
        det.setMarkLength(markLength);
        return det;
    }

    /**
     * Counts up a number of CRs in the specified string.
     * @param s the string.
     * @return a number of CRs.
     */
    private int countCR(String s) {
        Matcher matcherCR = patternCR.matcher(s);
        int countCR=0;
        while(matcherCR.find()) {
            countCR++;
        }
        return countCR;
    }

    /**
     *
     * @param fo {@code FileObject} to create the nodes for.
     * @return {@codeDetailNode}s representing the matches,
     *          or <code>null</code> if no matching string is known for the
     *          specified {@code FileObject}
     * @see  DetailNode
     */
    public Node[] getDetails(FileObject fo) {
        List<TextDetail> details = getDetailsMap().get(fo);
        if (details == null) {
            return null;
        }

        List<Node> detailNodes = new ArrayList<Node>(details.size());
        for (TextDetail txtDetail : details) {
            detailNodes.add(new TextDetail.DetailNode(txtDetail));        
        }
        
        return detailNodes.toArray(new Node[detailNodes.size()]);
    }

    /** Gets details map. */
    private Map<FileObject, List<TextDetail>> getDetailsMap() {
        if (detailsMap != null) {
            return detailsMap;
        }
        
        synchronized(this) {
            if (detailsMap == null) {
                detailsMap = new HashMap<FileObject, List<TextDetail>>(20);
            }
        }
        
        return detailsMap;
    }
    
    /**
     * @param  node representing a <code>DataObject</code> with matches
     * @return  <code>DetailNode</code>s representing the matches,
     *          or <code>null</code> if the specified node does not represent
     *          a <code>DataObject</code> or if no matching string is known for
     *          the specified object
     */
// vvg: a FileObject should be stored in the node's cooke !?
//    public Node[] getDetails(Node node) {
//        DataObject data = node.getCookie(DataObject.class);
//
//        if (data == null) {
//            return null;
//        }
//
//        return getDetails(data);
//    }
    
    /**
     */
    int getDetailsCount(FileObject resultObject) {
        List<TextDetail> details = getDetailsMap().get(resultObject);
        return (details != null) ? details.size() : 0;
    }
    
    /**
     * Returns a list of the {@code TextDetail}s associated with the specified
     * {@code FileObject}.
     *
     * @param fo the {@code FileObject}.
     * @return a list of the {@code TextDetail}s if any, otherwise {@code null}.
     */
    List<TextDetail> getTextDetails(FileObject fo) {
        List<TextDetail> obtained = getDetailsMap().get(fo);
        return (obtained != null) ? new ArrayList<TextDetail>(obtained) : null;
    }

    private SearchPattern createSearchPattern() {
        return SearchPattern.create(textPatternExpr,
                                    wholeWords, 
                                    caseSensitive, 
                                    regexp);
    }

    private void findDataObject(FileObject fo)
                                            throws DataObjectNotFoundException {
        if(dataObject == null) {
            dataObject = DataObject.find(fo);
        }
    }

    private void freeDataObject() {
        dataObject = null;
    }

    /**
     * Utility class providing optimal calculating of the column.
     */
    private class FindState {
        int lineNumber = 1;
        int lineStartOffset = 0;
        int prevCR = 0;

        BufferedCharSequence bcs;

        FindState(BufferedCharSequence bcs) {
           this.bcs = bcs;
        }

        int getLineNumber() {
            return lineNumber;
        }

        String getLineText() {
            return bcs.getLineText(lineStartOffset);
        }

        int calcColumn(int matcherStart) {
            try {
                while (bcs.position() < matcherStart) {
                    char curChar = bcs.nextChar();
                    switch (curChar) {
                        case BufferedCharSequence.UnicodeLineTerminator.LF:
                        case BufferedCharSequence.UnicodeLineTerminator.PS:
                        case BufferedCharSequence.UnicodeLineTerminator.LS:
                        case BufferedCharSequence.UnicodeLineTerminator.FF:
                        case BufferedCharSequence.UnicodeLineTerminator.NEL:
                            lineNumber++;
                            lineStartOffset = bcs.position();
                            prevCR = 0;
                            break;
                        case BufferedCharSequence.UnicodeLineTerminator.CR:
                            prevCR++;
                            char nextChar = bcs.charAt(bcs.position());
                            if (nextChar !=
                                BufferedCharSequence.UnicodeLineTerminator.LF) {

                                lineNumber++;
                                lineStartOffset = bcs.position();
                                prevCR = 0;
                            }
                            break;
                        default:
                            prevCR = 0;
                    }
                }
            } catch (IndexOutOfBoundsException ioobe) {
                // It is OK. It means that EOF is reached, i.e.
                // bcs.position() >= bcs.length()
            }
            int column = matcherStart - lineStartOffset + 1 - prevCR;
            return column;
        }
    } // FindState

    /** Register BufferedCharSequence that is being processed by this object.
     * It is used when user needs to terminate the current search. */
    private synchronized void registerProcessedSequence(
            BufferedCharSequence bcs) throws IOException {
        if (terminated) {
            bcs.close();
        } else {
            currentlyProcessedSequences.add(bcs);
        }
    }

    /** Unregister a BufferedCharSequence after it was processed. */
    private synchronized void unregisterProcessedSequence(
            BufferedCharSequence bcc) {
        currentlyProcessedSequences.remove(bcc);
    }

    /** Stop all searches that are processed by this instance. */
    synchronized void terminateCurrentSearches() throws IOException {
        for (BufferedCharSequence bcs: currentlyProcessedSequences) {
            bcs.close();
        }
        currentlyProcessedSequences.clear();
        terminated = true;
    }
}
