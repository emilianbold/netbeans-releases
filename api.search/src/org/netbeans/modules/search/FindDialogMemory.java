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
 * Software is Sun Microsystems, Inc. Portions Copyright 2005-2007 Sun
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Registry for everything related to the Find dialog.
 * It is <em>not</em> designed to be persistent across invocations
 * of the IDE.
 *
 * @author Marian Petras
 * @author kaktus
 */
public final class FindDialogMemory {

    /** maximum count of stored file name patterns */
    private static final int maxFileNamePatternCount = 10;
    /** maximum count of stored replacement expressions */
    private static final int maxReplExprCount = 10;

    /** singleton instance of this class */
    private static FindDialogMemory singleton;

    /**
     * storage of last used file name patterns
     * (initially {@code null})
     */
    private List<String> fileNamePatterns;
    /**
     * storage of last used replacement expressions
     * (initially {@code null})
     */
    private List<String> replExpressions;

    /**
     * Storage of last used Whole Words option.
     */
    private boolean wholeWords;

    /**
     * Storage of last used Case Sensitive option.
     */
    private boolean caseSensitive;

    /**
     * Storage of last used Preserve Case option.
     */
    private boolean preserveCase;
    
    /**
     * Storage of last used Regular Expression option.
     */
    private boolean regularExpression;

    /**
     * whether a full text pattern was used last time
     */
    private boolean textPatternSpecified;

    /**
     * ID of seach scope type.
     */
    private String scopeTypeId;

    /**
     * whether file name pattern was used last time
     */
    private boolean fileNamePatternSpecified;

    /**
     * Storage of last used Search in Archives option.
     */
    private boolean searchInArchives;

    /**
     * Storage of last used Search in Generated sources option.
     */
    private boolean searchInGenerated;

    /**
     * Storage of last used Path Regular Expression option.
     */
    private boolean filePathRegex;

    /**
     * Storage for last used Use Ignore List option.
     */
    private boolean useIgnoreList;

    /**
     * Storage for Ignore List.
     */
    private List<String> ignoreList;

    /**
     * Storage for Text pattern sandbox content.
     */
    private String textSandboxContent;

    /**
     * Storage for Path pattern sandbox content.
     */
    private String pathSandboxContent;

    /** Widths of results outline columns */
    private String resultsColumnWidths;
    private String resultsColumnWidthsDetails;
    private String resultsColumnWidthsReplacing;

    /** Tree or flat result view mode. */
    private String resultsViewMode;

    /** Last selected search provider. */
    private String provider;

    private boolean openInNewTab;

    /** Preferences node for storing history info */
    private static Preferences prefs;
    /** Name of preferences node where we persist history */
    private static final String PREFS_NODE = "FindDialogMemory";  //NOI18N
    private static final String PROP_WHOLE_WORDS = "whole_words";  //NOI18N
    private static final String PROP_CASE_SENSITIVE = "case_sensitive";  //NOI18N
    private static final String PROP_PRESERVE_CASE = "preserve_case";  //NOI18N
    private static final String PROP_REGULAR_EXPRESSION = "regular_expression";  //NOI18N
    private static final String PROP_SCOPE_TYPE_ID = "scope_type_id"; //NOI18N
    private static final String PROP_FILENAME_PATTERN_SPECIFIED = "filename_specified";  //NOI18N
    private static final String PROP_FILENAME_PATTERN_PREFIX = "filename_pattern_";  //NOI18N
    private static final String PROP_REPLACE_PATTERN_PREFIX = "replace_pattern_";  //NOI18N
    private static final String PROP_SEARCH_IN_ARCHIVES = "search_in_archives"; //NOI18N
    private static final String PROP_SEARCH_IN_GENERATED = "search_in_generated"; //NOI18N
    private static final String PROP_FILE_PATH_REGEX = "file_path_regex"; //NOI18N
    private static final String PROP_USE_IGNORE_LIST = "use_ignore_list"; //NOI18N
    private static final String PROP_IGNORE_LIST_PREFIX = "ignore_list_"; //NOI18N
    private static final String PROP_TEXT_SANDBOX_CONTENT = "text_sandbox_content"; //NOI18N
    private static final String PROP_PATH_SANDBOX_CONTENT = "path_sandbox_content"; //NOI18N
    private static final String PROP_RESULTS_COLUMN_WIDTHS = "results_column_widths"; //NOI18N
    private static final String PROP_RESULTS_COLUMN_WIDTHS_DETAILS = "results_column_widths_details"; //NOI18N
    private static final String PROP_RESULTS_COLUMN_WIDTHS_REPLACING = "results_column_widths_replacing"; //NOI18N
    private static final String PROP_RESULTS_VIEW_MODE = "results_view_mode"; //NOI18N
    private static final String PROP_PROVIDER = "provider"; //NOI18N
    private static final String PROP_OPEN_IN_NEW_TAB = "open_in_new_tab"; //NOI18N
    /** Creates a new instance of FindDialogMemory */
    private FindDialogMemory() {
        prefs = NbPreferences.forModule(FindDialogMemory.class).node(PREFS_NODE);
        load();
    }

    /**
     */
    public static FindDialogMemory getDefault() {
        if (singleton == null) {
            singleton = new FindDialogMemory();
        }
        return singleton;
    }

    /** 
     *  Loads search history stored in previous system sessions.
     */
    private void load () {
        wholeWords = prefs.getBoolean(PROP_WHOLE_WORDS, false);
        caseSensitive = prefs.getBoolean(PROP_CASE_SENSITIVE, false);
        regularExpression = prefs.getBoolean(PROP_REGULAR_EXPRESSION, false);
        preserveCase = prefs.getBoolean(PROP_PRESERVE_CASE, false);
        scopeTypeId = prefs.get(PROP_SCOPE_TYPE_ID, "open projects");   //NOI18N
        fileNamePatternSpecified = prefs.getBoolean(PROP_FILENAME_PATTERN_SPECIFIED, false);
        searchInArchives = prefs.getBoolean(PROP_SEARCH_IN_ARCHIVES, false);
        searchInGenerated = prefs.getBoolean(PROP_SEARCH_IN_GENERATED, false);
        filePathRegex = prefs.getBoolean(PROP_FILE_PATH_REGEX, false);
        useIgnoreList = prefs.getBoolean(PROP_USE_IGNORE_LIST, false);
        textSandboxContent = prefs.get(PROP_TEXT_SANDBOX_CONTENT,
                getText("TextPatternSandbox.textPane.text.default"));   //NOI18N
        pathSandboxContent = prefs.get(PROP_PATH_SANDBOX_CONTENT,
                getText("PathPatternSandbox.textPane.text.default"));   //NOI18N
        resultsColumnWidths = prefs.get(PROP_RESULTS_COLUMN_WIDTHS,
                "100:-1:-1:-1:|0:");                                    //NOI18N
        resultsColumnWidthsDetails = prefs.get(PROP_RESULTS_COLUMN_WIDTHS_DETAILS,
                "100:-1:-1:-1:-1:|0:");                                 //NOI18N
        resultsColumnWidthsReplacing = prefs.get(PROP_RESULTS_COLUMN_WIDTHS_REPLACING,
                "100:-1:-1:-1:-1:|0:");                                 //NOI18N
        resultsViewMode = prefs.get(PROP_RESULTS_VIEW_MODE, null);
        provider = prefs.get(PROP_PROVIDER, null);
        openInNewTab = prefs.getBoolean(PROP_OPEN_IN_NEW_TAB, true);
        fileNamePatterns = new ArrayList<String>(maxFileNamePatternCount);
        replExpressions = new ArrayList<String>(maxReplExprCount);
        ignoreList = new ArrayList();
        for(int i=0; i < maxFileNamePatternCount; i++){
            String fileNamePattern = prefs.get(PROP_FILENAME_PATTERN_PREFIX + i, null);
            if (fileNamePattern != null)
                fileNamePatterns.add(fileNamePattern);
            String replacePattern = prefs.get(PROP_REPLACE_PATTERN_PREFIX + i, null);
            if (replacePattern != null)
                replExpressions.add(replacePattern);
        }
        int i = 0;
        while (true) {
            String item = prefs.get(PROP_IGNORE_LIST_PREFIX + i, null);
            if (item == null) {
                break;
            } else {
                ignoreList.add(item);
            }
            i++;
        }
    }

    /**
     * Stores a file name pattern.
     * If the number of patterns would exceed the maximum
     * number of patterns that can be stored, the oldest
     * pattern is removed prior to storing the new pattern.
     * 
     * @param  pattern  pattern to be stored
     */
    public void storeFileNamePattern(String pattern) {
        int index = fileNamePatterns.indexOf(pattern);
        if (index != -1) {
            if (index == fileNamePatterns.size() - 1) {
                return;
            }

            fileNamePatterns.remove(index);
        } else if (fileNamePatterns.size() == maxFileNamePatternCount) {
            fileNamePatterns.remove(0);
        }
        fileNamePatterns.add(pattern);

        for(int i=0;i < fileNamePatterns.size();i++){
            prefs.put(PROP_FILENAME_PATTERN_PREFIX + i, fileNamePatterns.get(i));
        }
    }

    /**
     * Returns last used file name patterns in order
     * from the oldest ones to the most recently used ones.
     *
     * @return  list of the last used file name patterns, or an empty list
     *          if no file name patterns are stored
     */
    public List<String> getFileNamePatterns() {
        return (fileNamePatterns != null) ? fileNamePatterns
                                          : Collections.<String>emptyList();
    }

    /**
     * Stores a replacement expression.
     * If the number of replacement expressions would exceed the maximum
     * number of replacement expressions that can be stored, the oldest
     * expression is removed prior to storing the new expression.
     * 
     * @param  expression  replacement expression to be stored
     */
    void storeReplacementExpression(String expression) {
        int index = replExpressions.indexOf(expression);
        if (index != -1) {
            if (index == replExpressions.size() - 1) {
                return;
            }

            replExpressions.remove(index);
        } else if (replExpressions.size() == maxReplExprCount) {
            replExpressions.remove(0);
        }
        replExpressions.add(expression);

        for(int i=0;i < replExpressions.size();i++){
            prefs.put(PROP_REPLACE_PATTERN_PREFIX + i, replExpressions.get(i));
        }
    }

    /**
     * Returns last used replacement expressions in order
     * from the oldest ones to the most recently used ones.
     *
     * @return  list of last used replacement expressions, or an empty list
     *          if no replacement expressions are stored
     */
    List<String> getReplacementExpressions() {
        return (replExpressions != null) ? replExpressions
                                          : Collections.<String>emptyList();
    }

    public boolean isWholeWords() {
        return wholeWords;
    }

    public void setWholeWords(boolean wholeWords) {
        this.wholeWords = wholeWords;
        prefs.putBoolean(PROP_WHOLE_WORDS, wholeWords);
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    public boolean isPreserveCase() {
        return preserveCase;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        prefs.putBoolean(PROP_CASE_SENSITIVE, caseSensitive);
    }

    public void setPreserveCase(boolean preserveCase) {
        this.preserveCase = preserveCase;
        prefs.putBoolean(PROP_PRESERVE_CASE, preserveCase);
    }
    
    public boolean isRegularExpression() {
        return regularExpression;
    }

    public void setRegularExpression(boolean regularExpression) {
        this.regularExpression = regularExpression;
        prefs.putBoolean(PROP_REGULAR_EXPRESSION, regularExpression);
    }

    public String getScopeTypeId() {
        return scopeTypeId;
    }

    public void setScopeTypeId(String scopeTypeId) {
        this.scopeTypeId = scopeTypeId;
        prefs.put(PROP_SCOPE_TYPE_ID, scopeTypeId);
    }

    public boolean isTextPatternSpecified() {
        return textPatternSpecified;
    }

    void setTextPatternSpecified(boolean specified) {
        textPatternSpecified = specified;
    }

    boolean isFileNamePatternSpecified() {
        return fileNamePatternSpecified;
    }

    public void setFileNamePatternSpecified(boolean specified) {
        fileNamePatternSpecified = specified;
        prefs.putBoolean(PROP_FILENAME_PATTERN_SPECIFIED, specified);
    }

    boolean isSearchInArchives() {
        return searchInArchives;
    }

    void setSearchInArchives(boolean searchInArchives) {
        this.searchInArchives = searchInArchives;
        prefs.putBoolean(PROP_SEARCH_IN_GENERATED, searchInArchives);
    }

    boolean isSearchInGenerated() {
        return searchInGenerated;
    }

    void setSearchInGenerated(boolean searchInGenerated) {
        this.searchInGenerated = searchInGenerated;
        prefs.putBoolean(PROP_SEARCH_IN_GENERATED, searchInGenerated);
    }

    boolean isFilePathRegex() {
        return filePathRegex;
    }

    void setFilePathRegex(boolean filePathRegex) {
        this.filePathRegex = filePathRegex;
        prefs.putBoolean(PROP_FILE_PATH_REGEX, filePathRegex);
    }

    boolean IsUseIgnoreList() {
        return useIgnoreList;
    }

    void setUseIgnoreList(boolean useIgnoreList) {
        this.useIgnoreList = useIgnoreList;
        prefs.putBoolean(PROP_USE_IGNORE_LIST, useIgnoreList);
    }

    String getTextSandboxContent() {
        return textSandboxContent;
    }

    void setTextSandboxContent(String textSandboxContent) {
        this.textSandboxContent = textSandboxContent;
        prefs.put(PROP_TEXT_SANDBOX_CONTENT, textSandboxContent);
    }

    String getPathSandboxContent() {
        return pathSandboxContent;
    }

    void setPathSandboxContent(String pathSandboxContent) {
        this.pathSandboxContent = pathSandboxContent;
        prefs.put(PROP_PATH_SANDBOX_CONTENT, pathSandboxContent);
    }

    List<String> getIgnoreList() {
        if (ignoreList == null) {
            return Collections.emptyList();
        } else {
            return ignoreList;
        }
    }

    void setIgnoreList(List<String> ignoreList) {
        this.ignoreList = ignoreList;
        int i = 0;
        while (prefs.get(PROP_IGNORE_LIST_PREFIX + i, null) != null) {
            prefs.remove(PROP_IGNORE_LIST_PREFIX + i);
            i++;
        }
        for (int j = 0; j < ignoreList.size(); j++) {
            prefs.put(PROP_IGNORE_LIST_PREFIX + j, ignoreList.get(j));
        }
    }

    private String getText(String key) {
        return NbBundle.getMessage(FindDialogMemory.class, key);
    }

    public String getResultsColumnWidths() {
        return resultsColumnWidths;
    }

    public void setResultsColumnWidths(String resultsColumnWidths) {
        this.resultsColumnWidths = resultsColumnWidths;
        prefs.put(PROP_RESULTS_COLUMN_WIDTHS, resultsColumnWidths);
    }

    public String getResultsColumnWidthsDetails() {
        return resultsColumnWidthsDetails;
    }

    public void setResultsColumnWidthsDetails(
            String resultsColumnWidthsDetails) {
        this.resultsColumnWidthsDetails = resultsColumnWidthsDetails;
        prefs.put(PROP_RESULTS_COLUMN_WIDTHS_DETAILS,
                resultsColumnWidthsDetails);
    }

    public String getResultsColumnWidthsReplacing() {
        return resultsColumnWidthsReplacing;
    }

    public void setResultsColumnWidthsReplacing(
            String resultsColumnWidthsReplacing) {
        this.resultsColumnWidthsReplacing = resultsColumnWidthsReplacing;
        prefs.put(PROP_RESULTS_COLUMN_WIDTHS_REPLACING,
                resultsColumnWidthsReplacing);
    }

    public String getResultsViewMode() {
        return resultsViewMode;
    }

    public void setResultsViewMode(String resultsViewMode) {
        this.resultsViewMode = resultsViewMode;
        prefs.put(PROP_RESULTS_VIEW_MODE, resultsViewMode);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
        prefs.put(PROP_PROVIDER, provider);
    }

    public boolean isOpenInNewTab() {
        return openInNewTab;
    }

    public void setOpenInNewTab(boolean openInNewTab) {
        this.openInNewTab = openInNewTab;
        prefs.putBoolean(PROP_OPEN_IN_NEW_TAB, openInNewTab);
    }
}
