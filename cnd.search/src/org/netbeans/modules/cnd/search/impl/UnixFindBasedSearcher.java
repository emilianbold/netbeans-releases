/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.search.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.modules.cnd.search.MatchingFileData;
import org.netbeans.modules.cnd.search.SearchParams;
import org.netbeans.modules.cnd.search.Searcher;

/**
 *
 */
public final class UnixFindBasedSearcher implements Searcher {

    private static final Pattern grepOutPattern = Pattern.compile("^([0-9]+):(.*)"); // NOI18N
    private final SearchParams params;
    private final SearchRoot root;
    private String rootPath;
    private List<MatchingFileData.Entry> entries;

    public UnixFindBasedSearcher(SearchRoot root, SearchParams params) {
        this.params = params;
        this.root = root;
    }

    @Override
    public String getCommand() {
        return "find"; // NOI18N
    }

    @Override
    public String[] getCommandArguments() {
        List<String> args = new ArrayList<String>();
        rootPath = root.getFileObject().getPath();

        args.add(rootPath);
        args.add("-type"); // NOI18N
        args.add("f"); // NOI18N

        String fileNamePattern = params.getFileNamePattern();

        if (fileNamePattern != null && !fileNamePattern.isEmpty()) {
            args.add("-name"); // NOI18N
            args.add(fileNamePattern);
        }
        
        SearchPattern sp = params.getSearchPattern();
        
        String searchText = sp.getSearchExpression();
        if (searchText != null && !searchText.isEmpty()) {
            args.add("-exec"); // NOI18N
            args.add("grep"); // NOI18N
            if (!sp.isMatchCase()) {
                args.add("-i"); // NOI18N
            }
            if (sp.isWholeWords()) {
                args.add("-w"); // NOI18N
            }
            args.add("-n"); // NOI18N
            args.add(searchText);
            args.add("{}"); // NOI18N
            args.add(";"); // NOI18N
        }

        args.add("-ls"); // NOI18N

        return args.toArray(new String[args.size()]);
    }

    @Override
    public MatchingFileData processOutputLine(String line) {
        Matcher m = grepOutPattern.matcher(line);
        if (m.matches()) {
            Integer lineNo = Integer.parseInt(m.group(1));
            String context = m.group(2);

            if (entries == null) {
                entries = new ArrayList<MatchingFileData.Entry>(10);
            }

            entries.add(new MatchingFileData.Entry(lineNo, context));
            return null;
        }

        String[] data = line.split("[ \t]+", 11); // NOI18N

        if (data.length != 11) {
            return null;
        }

        String fname = data[10];
        if (fname.contains(" -> ")) { // NOI18N
            /// TODO ...
            fname = fname.substring(0, fname.indexOf(" -> ")); // NOI18N
        }

        MatchingFileData result = new MatchingFileData(params, fname);
        
        int fileSize = -1;
        try {
            fileSize = Integer.parseInt(data[6]);
        } catch (NumberFormatException ex) {
        }
        
        result.setFileSize(fileSize);

        result.setEntries(entries);
        entries = null;

        return result;
    }
}
