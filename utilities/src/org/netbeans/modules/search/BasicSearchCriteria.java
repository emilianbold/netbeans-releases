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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openidex.search.SearchPattern;

/**
 * Class encapsulating basic search criteria.
 * 
 * @author  Marian Petras
 */
final class BasicSearchCriteria {

    /**
     * Maximum number of matches reported for one line.
     * It eliminates huge memory consumption for single letter searches on long lines.
     */
    private static final int MAX_REPORTED_OCCURENCES_ON_LINE = 5;

    /**
     * Maximum number of matches reported for one file.
     * It eliminates huge memory consumption for single letter searches in long files.
     */
    private static final int MAX_REPORTED_OCCURENCES_IN_FILE = 200;
    
    /** array of searchable application/x-<em>suffix</em> MIME-type suffixes */
    private static final Collection<String> searchableXMimeTypes;
    
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
    private boolean wholeWords;
    private boolean caseSensitive;
    private boolean regexp;
    
    private boolean textPatternSpecified = false;
    private boolean fileNamePatternSpecified = false;
    
    private boolean textPatternValid = false;
    private boolean fileNamePatternValid = false;
    
    private Pattern textPattern;
    private Pattern fileNamePattern;
    
    private boolean criteriaUsable = false;
    
    private ChangeListener usabilityChangeListener;

    /**
     * Holds information about occurences of matching strings within individual
     * {@code DataObject}s.
     */
    private Map<DataObject, List<TextDetail>> detailsMap;

    BasicSearchCriteria() {
    }
    
    /**
     * Copy-constructor.
     * 
     * @param  template  template to create a copy from
     */
    BasicSearchCriteria(BasicSearchCriteria template) {

        /* check-boxes: */
        setCaseSensitive(template.caseSensitive);
        setWholeWords(template.wholeWords);
        setRegexp(template.regexp);

        /* combo-boxes: */
        setTextPattern(template.textPatternExpr);
        setFileNamePattern(template.fileNamePatternExpr);
        setReplaceString(template.replaceExpr);
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
            assert false;//valid pattern for a regexp should be already compiled
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
        if ((pattern != null) && (pattern.length() == 0)) {
            pattern = null;
        }
        if ((pattern == null) && (textPatternExpr == null)
               || (pattern != null) && pattern.equals(textPatternExpr)) {
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
        updateUsability();
    }
    
    /**
     * Tries to compile the regular expression pattern, thus checking its
     * validity. In case of success, the compiled pattern is stored
     * to {@link #textPattern}, otherwise the field is set to {@code null}.
     * 
     * @return  {@code true} if the regexp pattern expression was valid;
     *          {@code false} otherwise
     */
    private boolean compileRegexpPattern() {
        assert regexp;
        assert textPatternExpr != null;
        try {
            textPattern = Pattern.compile(textPatternExpr);
            return true;
        } catch (PatternSyntaxException ex) {
            textPattern = null;
            return false;
        }
    }
    
    /**
     * Translates the simple text pattern to a regular expression pattern
     * and compiles it. The compiled pattern is stored to field
     * {@link #textPattern}.
     */
    private void compileSimpleTextPattern() {
        assert !regexp;
        assert textPatternExpr != null;
        try {
            int flags = 0;
            if (!caseSensitive) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
	    String regexp = RegexpMaker.makeRegexp(textPatternExpr);
	    if (wholeWords) {
                regexp = "\\b" + regexp + "\\b";                        //NOI18N
	    }
            textPattern = Pattern.compile(regexp, flags);
        } catch (PatternSyntaxException ex) {
            assert false;
            textPattern = null;
        }
    }
    
    boolean isRegexp() {
        return regexp;
    }
    
    void setRegexp(boolean regexp) {
        if (regexp == this.regexp) {
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
        updateUsability();
    }
    
    boolean isWholeWords() {
        return wholeWords;
    }
    
    void setWholeWords(boolean wholeWords) {
        if (wholeWords == this.wholeWords) {
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
        if (caseSensitive == this.caseSensitive) {
            return;
        }
        
        this.caseSensitive = caseSensitive;
        
        if (!regexp) {
            textPattern = null;
        }
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
        
        assert (fileNamePatternExpr != null) && (fileNamePatternExpr.length() != 0);
        
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
            fileNamePatternSpecified = checkFileNamePattern(fileNamePatternExpr);
            fileNamePatternValid = fileNamePatternSpecified;
        }
        updateUsability();
    }
    
    /**
     * Translates the simple text pattern to a regular expression pattern
     * and compiles it. The compiled pattern is stored to field
     * {@link #textPattern}.
     */
    private void compileSimpleFileNamePattern() {
        assert fileNamePatternExpr != null;
        try {
            fileNamePattern = Pattern.compile(RegexpMaker.makeMultiRegexp(fileNamePatternExpr),
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
     * Returns the replacement string/expression.
     * 
     * @return  replace string, or {@code null} if no replace string has been
     *          specified
     */
    String getReplaceExpr() {
        return replaceExpr;
    }

    /**
     * Sets a replacement string/expression.
     *
     * @param  replaceString  string to replace matches with, or {@code null}
     *                        if no replacing should be performed
     */
    void setReplaceString(String replaceString) {
        this.replaceExpr = replaceString;
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
        return (textPatternSpecified || fileNamePatternSpecified)
               && !isInvalid();
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
        if (textPatternValid && (textPattern == null)) {
            assert !regexp;             //should have been already compiled
            compileSimpleTextPattern();
        }
        if (fileNamePatternValid && (fileNamePattern == null)) {
            compileSimpleFileNamePattern();
        }
        
        assert !textPatternValid || (textPattern != null);
        assert !fileNamePatternValid || (fileNamePattern != null);
    }
    
    boolean matches(DataObject dataObj) {
        if (!dataObj.isValid()) {
            return false;
        }
        
        FileObject fileObj = dataObj.getPrimaryFile();
        if (fileObj.isFolder() || !fileObj.isValid() || !isTextFile(fileObj)) {
            return false;
        }
        
        /* Check the file name: */
        if (fileNamePatternValid 
                && !fileNamePattern.matcher(fileObj.getNameExt()).matches()) {
            return false;
        }
        
        /* Check the file's content: */
        if (textPatternValid
                && !checkFileContent(fileObj, dataObj)) {
            return false;
        }
        
        return true;
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
        
        if (mimeType.equals("content/unknown")                          //NOI18N
                || mimeType.startsWith("text/")) {                      //NOI18N
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
     * @param  fileObj  file whose content is to be checked
     * @param  dataObj  {@code DataObject} corresponding to the file
     * @return  {@code true} if the file contains at least one substring
     *          matching the pattern, {@code false} otherwise
     */
    private boolean checkFileContent(FileObject fileObj, DataObject dataObj) {
        boolean firstMatch = true;
        SearchPattern searchPattern = null;
        ArrayList<TextDetail> txtDetails = null;

        LineNumberReader reader = null;
        try {
            reader = getFileObjectReader(fileObj);
            
            int matchesInFile = 0;
            loopOverLines:
            do {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                int matchesOnLine = 0;
                Matcher matcher = textPattern.matcher(line);
                while (matcher.find()) {
                    if (firstMatch) {
                        searchPattern = createSearchPattern();
                        txtDetails = new ArrayList<TextDetail>(5);
                        firstMatch = false;
                    }
                    TextDetail det = new TextDetail(dataObj, searchPattern);
                    det.setLine(reader.getLineNumber());
                    det.setLineText(line);
                    int start = matcher.start();
                    int len = matcher.end() - start;
                    det.setColumn(start + 1);
                    det.setMarkLength(len);
                    txtDetails.add(det);

                    if (++matchesOnLine == MAX_REPORTED_OCCURENCES_ON_LINE) {
                        break;
                    }
                }
            } while (matchesInFile < MAX_REPORTED_OCCURENCES_IN_FILE);
            if (txtDetails != null) {
                txtDetails.trimToSize();
                getDetailsMap().put(dataObj, txtDetails);
                return true;
            } else {
                return false;
            }
        } catch (FileNotFoundException fnfe) {
            return false;
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(
                            ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }

    /**
     * 
     * @exception  java.io.FileNotFoundException
     *               if file determined by the {@code FileObject} does not exist
     */
    private LineNumberReader getFileObjectReader(FileObject fileObj)
                                                throws FileNotFoundException {
        InputStream is = fileObj.getInputStream();//throws FileNotFoundException
        Charset charset = getCharset(fileObj);
        return new LineNumberReader(new InputStreamReader(is, charset));
    }

    static Charset getCharset(FileObject fileObj) {
        return FileEncodingQuery.getEncoding(fileObj);
    }
    
    /**
     * @param  resultObject  <code>DataObject</code> to create the nodes for
     * @return  <code>DetailNode</code>s representing the matches,
     *          or <code>null</code> if no matching string is known for the
     *          specified object
     * @see  DetailNode
     */
    public Node[] getDetails(Object resultObject) {
        List<TextDetail> details = getDetailsMap().get(resultObject);
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
    private Map<DataObject, List<TextDetail>> getDetailsMap() {
        if (detailsMap != null) {
            return detailsMap;
        }
        
        synchronized(this) {
            if (detailsMap == null) {
                detailsMap = new HashMap<DataObject, List<TextDetail>>(20);
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
    public Node[] getDetails(Node node) {
        DataObject dataObject = node.getCookie(DataObject.class);
        
        if(dataObject == null) 
            return null;
        
        return getDetails(dataObject);
    }
    
    /**
     */
    public int getDetailsCount(Object resultObject) {
        List<TextDetail> details = getDetailsMap().get(resultObject);
        return (details != null) ? details.size() : 0;
    }
    
    /**
     */
    public List<TextDetail> getTextDetails(Object resultObject) {
        List<TextDetail> obtained = getDetailsMap().get(resultObject);
        return (obtained != null) ? new ArrayList<TextDetail>(obtained) : null;
    }

    private SearchPattern createSearchPattern() {
        return SearchPattern.create(textPatternExpr,
                                    wholeWords, 
                                    caseSensitive, 
                                    regexp);
    }
    
}
