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

import static java.util.logging.Level.FINER;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.search.RegexpUtil;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchScopeOptions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Class encapsulating basic search criteria.
 *
 * @author Marian Petras
 */
public final class BasicSearchCriteria {

    private static int instanceCounter;
    private final int instanceId = instanceCounter++;
    private static final Logger LOG = Logger.getLogger(
            "org.netbeans.modules.search.BasicSearchCriteria");         //NOI18N
    private SearchPattern searchPattern = SearchPattern.create(null, false,
            false, false);
    private SearchScopeOptions searcherOptions = SearchScopeOptions.create();
    private String replaceExpr;
    private String replaceString;
    private boolean preserveCase;
    private boolean textPatternSpecified = false;
    private boolean fileNamePatternSpecified = false;
    private boolean textPatternValid = false;
    private boolean replacePatternValid = false;
    private boolean fileNamePatternValid = false;
    private Pattern textPattern;
    private Pattern fileNamePattern;
    private boolean useIgnoreList = false;
    private boolean criteriaUsable = false;
    private ChangeListener usabilityChangeListener;
    /**
     * Holds a {@code DataObject} that will be used to create a {@code TextDetail}.
     * It should be set to {@code null} immediately after all {@code TextDetail}s
     * are created for given {@code DataObject}.
     *
     * @see #findDataObject(org.openide.filesystems.FileObject)
     * @see #freeDataObject()
     */
    private DataObject dataObject;

    BasicSearchCriteria() {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "#{0}: <init>()", instanceId);               //NOI18N
        }
    }

    /**
     * Copy-constructor.
     *
     * @param template template to create a copy from
     */
    BasicSearchCriteria(BasicSearchCriteria template) {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "#{0}: <init>(template)", instanceId);       //NOI18N
        }

        /*
         * check-boxes:
         */
        setCaseSensitive(template.searchPattern.isMatchCase());
        setWholeWords(template.searchPattern.isWholeWords());
        setRegexp(template.searchPattern.isRegExp());
        setPreserveCase(template.preserveCase);
        setSearchInArchives(template.searcherOptions.isSearchInArchives());
        setSearchInGenerated(template.searcherOptions.isSearchInGenerated());
        setFileNameRegexp(template.searcherOptions.isRegexp());
        setUseIgnoreList(template.useIgnoreList);

        /*
         * combo-boxes:
         */
        setTextPattern(template.searchPattern.getSearchExpression());
        setFileNamePattern(template.searcherOptions.getPattern());
        setReplaceExpr(template.replaceExpr);
    }

    /**
     * Returns a {@link Pattern} object corresponding to the substring pattern
     * specified in the criteria.
     *
     * @return {@code Pattern} object, or {@code null} if no pattern has been
     * specified
     */
    Pattern getTextPattern() {

        if (!textPatternValid || !textPatternSpecified) {
            return null;
        }
        if (textPattern != null) {
            return textPattern;
        }

        try {
            return TextRegexpUtil.makeTextPattern(searchPattern);
        } catch (PatternSyntaxException e) {
            textPatternValid = false;
            return null;
        }
    }

    String getTextPatternExpr() {
        return searchPattern.getSearchExpression() != null
                ? searchPattern.getSearchExpression()
                : "";                                                   //NOI18N
    }

    /**
     * Sets a text pattern. Whether it is considered a simple pattern or a
     * regexp pattern, is determined by the current <em>regexp</em> setting (see {@link #setRegexp(boolean)}).
     *
     * @param pattern pattern to be set
     */
    void setTextPattern(String pattern) {

        searchPattern = searchPattern.changeSearchExpression(pattern);

        if (pattern == null || pattern.equals("")) {
            textPattern = null;
            textPatternSpecified = false;
            textPatternValid = false;
        } else {
            textPatternSpecified = true;
            updateTextPattern();
        }

        replacePatternValid = validateReplacePattern();
        updateUsability();
    }

    private void updateFileNamePattern() {
        try {
            if (fileNamePatternSpecified) {
                fileNamePattern = RegexpUtil.makeFileNamePattern(
                        searcherOptions);
                fileNamePatternValid = true;
            }
        } catch (PatternSyntaxException e) {
            fileNamePattern = null;
            fileNamePatternValid = false;
        }
    }

    private void updateTextPattern() throws NullPointerException {
        try {
            if (textPatternSpecified) {
                textPattern = TextRegexpUtil.makeTextPattern(searchPattern);
                textPatternValid = true;
            }
        } catch (PatternSyntaxException e) {
            textPatternValid = false;
        }
    }

    /**
     * Tries to compile the regular expression pattern, thus checking its
     * validity. In case of success, the compiled pattern is stored to {@link #textPattern},
     * otherwise the field is set to {@code null}.
     *
     * <p>Actually, this method defines a pattern used in searching, i.e. it
     * defines behaviour of the searching. It should be the same as behavior of
     * the Find action (Ctrl+F) in the Editor to avoid any confusions (see Bug
     * #175101). Hence, this implementation should specify default flags in the
     * call of the method {@link Pattern#compile(java.lang.String, int)
     * java.util.regex.Pattern.compile(String regex, int flags)} that are the
     * same as in the implementation of the Find action (i.e in the method {@code getFinder}
     * of the class {@code org.netbeans.modules.editor.lib2.search.DocumentFinder}).
     * </p>
     *
     * @return {@code true} if the regexp pattern expression was valid; {@code false}
     * otherwise
     */
    private boolean validateReplacePattern() {
        if (searchPattern.isRegExp() && textPatternValid
                && textPatternSpecified) {
            int groups = getTextPattern().matcher("").groupCount();
            String tmpSearch = "";
            for (int i = 1; i <= groups; i++) {
                tmpSearch += "(" + i + ")";
            }
            try {
                Pattern.compile(tmpSearch).matcher("123456789").
                        replaceFirst(replaceExpr);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    boolean isRegexp() {
        return searchPattern.isRegExp();
    }

    boolean isPreserveCase() {
        return preserveCase;
    }

    void setPreserveCase(boolean preserveCase) {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "setPreservecase({0}{1}",
                    new Object[]{preserveCase, ')'});                   //NOI18N
        }
        if (preserveCase == this.preserveCase) {
            LOG.finest(" - no change");                                 //NOI18N
            return;
        }

        this.preserveCase = preserveCase;

        if (!searchPattern.isRegExp()) {
            textPattern = null;
        }
    }

    public boolean isFileNameRegexp() {
        return searcherOptions.isRegexp();
    }

    public void setFileNameRegexp(boolean fileNameRegexp) {

        if (this.searcherOptions.isRegexp() != fileNameRegexp) {
            searcherOptions.setRegexp(fileNameRegexp);
            updateFileNamePattern();
            updateUsability();
        }
    }

    public boolean isSearchInArchives() {
        return searcherOptions.isSearchInArchives();
    }

    public void setSearchInArchives(boolean searchInArchives) {
        this.searcherOptions.setSearchInArchives(searchInArchives);
    }

    public boolean isSearchInGenerated() {
        return searcherOptions.isSearchInGenerated();
    }

    public void setSearchInGenerated(boolean searchInGenerated) {
        this.searcherOptions.setSearchInGenerated(searchInGenerated);
    }

    public boolean isUseIgnoreList() {
        return useIgnoreList;
    }

    public void setUseIgnoreList(boolean useIgnoreList) {
        this.useIgnoreList = useIgnoreList;
    }

    void setRegexp(boolean regexp) {

        searchPattern = searchPattern.changeRegExp(regexp);
        updateTextPattern();
        replacePatternValid = validateReplacePattern();
        updateUsability();
    }

    boolean isWholeWords() {
        return searchPattern.isWholeWords();
    }

    void setWholeWords(boolean wholeWords) {

        searchPattern = searchPattern.changeWholeWords(wholeWords);
        updateTextPattern();
    }

    boolean isCaseSensitive() {
        return searchPattern.isMatchCase();
    }

    void setCaseSensitive(boolean caseSensitive) {

        searchPattern = searchPattern.changeMatchCase(caseSensitive);
        updateTextPattern();
    }

    boolean isFullText() {
        return textPatternValid;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns a {@link Pattern} object corresponding to the file name pattern
     * or set of patterns specified.
     *
     * @return {@code Pattern} object, or {@code null} if no pattern has been
     * specified
     */
    Pattern getFileNamePattern() {
        if (!fileNamePatternValid || !fileNamePatternSpecified) {
            return null;
        }

        if (fileNamePattern == null) {
            updateFileNamePattern();
            return fileNamePattern;
        } else {
            return fileNamePattern;
        }
    }

    String getFileNamePatternExpr() {
        return searcherOptions.getPattern();
    }

    void setFileNamePattern(String pattern) {

        searcherOptions.setPattern(pattern);
        if (searcherOptions.getPattern().isEmpty()) {
            fileNamePatternSpecified = false;
        } else {
            fileNamePatternSpecified = true;
            updateFileNamePattern();
        }
        updateUsability();
    }

    //--------------------------------------------------------------------------
    boolean isSearchAndReplace() {
        return replaceExpr != null;
    }

    /**
     * Returns the replacement expression.
     *
     * @return replace expression, or {@code null} if no replace expression has
     * been specified
     */
    String getReplaceExpr() {
        return replaceExpr;
    }

    /**
     * Returns the replacement string.
     *
     * @return replace string, or {@code null} if no replace string has been
     * specified
     */
    String getReplaceString() {
        if ((replaceString == null) && (replaceExpr != null)) {
            String[] sGroups =
                    replaceExpr.split("\\\\\\\\", replaceExpr.length());//NOI18N
            String res = "";                         //NOI18N
            for (int i = 0; i < sGroups.length; i++) {
                String tmp = sGroups[i];
                tmp = tmp.replace("\\" + "r", "\r"); //NOI18N
                tmp = tmp.replace("\\" + "n", "\n"); //NOI18N
                tmp = tmp.replace("\\" + "t", "\t"); //NOI18N
                res += tmp;
                if (i != sGroups.length - 1) {
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
     * @param replaceExpr string to replace matches with, or {@code null} if no
     * replacing should be performed
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
        return (textPatternSpecified
                || (!isSearchAndReplace() && fileNamePatternSpecified))
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
     * Called when the criteria in the Find dialog are confirmed by the user and
     * the search is about to be started. Makes sure everything is ready for
     * searching, e.g. regexp patterns are compiled.
     */
    void onOk() {
        LOG.finer("onOk()");                                            //NOI18N
        if (textPatternValid && (textPattern == null)) {
            textPattern = TextRegexpUtil.makeTextPattern(searchPattern);
        }
        if (fileNamePatternValid && (fileNamePattern == null)) {
            fileNamePattern = RegexpUtil.makeFileNamePattern(searcherOptions);
        }

        assert !textPatternValid || (textPattern != null);
        assert !fileNamePatternValid || (fileNamePattern != null);
    }

    boolean isTextPatternValidAndSpecified() {
        return textPatternValid && textPatternSpecified;
    }

    /**
     * Get underlying search pattern.
     *
     * @return Current search pattern, never null.
     */
    SearchPattern getSearchPattern() {
        return this.searchPattern;
    }

    /**
     * Get underlying searcher options.
     *
     * @return Current searcher options, with no custom filters specififed.
     * Never returns null.
     */
    SearchScopeOptions getSearcherOptions() {
        return this.searcherOptions;
    }
}
