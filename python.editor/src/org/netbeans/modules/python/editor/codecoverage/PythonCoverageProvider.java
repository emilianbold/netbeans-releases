/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.python.editor.codecoverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.codecoverage.api.CoverageManager;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProviderHelper;
import org.netbeans.modules.gsf.codecoverage.api.CoverageType;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.editor.lexer.PythonLexerUtils;
import org.netbeans.modules.python.editor.lexer.PythonTokenId;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * Code coverage for Python
 *
 * @todo Uhm... It looks like the hit count is ALWAYS 1 - so store and parse in a more
 *   compressed format!
 *
 * @author Tor Norbye
 */
public final class PythonCoverageProvider implements CoverageProvider {
    private static final int COUNT_INFERRED = -1;
    private static final int COUNT_NOT_COVERED = -2;
    private static final int COUNT_UNKNOWN = -3;
    private Map<String, String> hitCounts;
    private Map<String, String> fullNames;
    private long timestamp;
    private Project project;
    private Set<String> mimeTypes = Collections.singleton(PythonTokenId.PYTHON_MIME_TYPE);
    private Boolean enabled;
    private Boolean aggregating;

    public PythonCoverageProvider(Project project) {
        this.project = project;
    }

    public static PythonCoverageProvider get(Project project) {
        return project.getLookup().lookup(PythonCoverageProvider.class);
    }

    public synchronized List<FileCoverageSummary> getResults() {
        List<FileCoverageSummary> results = new ArrayList<FileCoverageSummary>();

        update();

        if (hitCounts == null) {
            return null;
        }

        for (Map.Entry<String, String> entry : hitCounts.entrySet()) {
            String fileName = entry.getKey();
            FileObject file;
            File f = new File(fileName);
            if (f.exists()) {
                file = FileUtil.toFileObject(f);
            } else {
                file = project.getProjectDirectory().getFileObject(fileName.replace('\\', '/'));
            }
            if (file != null) {
                Project p = FileOwnerQuery.getOwner(file);
                if (p != project) {
                    continue;
                }
            }

            // Compute coverage:
            List<LineCount> counts = getLineCounts(entry.getValue());
            int lineCount = 0;
            int executed = 0;
            //int notExecuted = 0;
            //int inferred = 0;
            for (LineCount lc : counts) {
                if (lc.lineno > lineCount) {
                    lineCount = lc.lineno;
                }
                if (lc.count > 0) {
                    executed++;
                    //} else if (lc.count == COUNT_NOT_COVERED) {
                    //    notExecuted++;
                    //} else if (lc.count == COUNT_INFERRED) {
                    //    inferred++;
                }
            }

            //int executed = lineCount - notExecuted;

            FileCoverageSummary result = new FileCoverageSummary(file, fileName, lineCount, executed);
            results.add(result);
        }

        return results;
    }

    public boolean supportsAggregation() {
        return true;
    }

    public synchronized boolean isAggregating() {
        if (aggregating == null) {
            aggregating = CoverageProviderHelper.isAggregating(project);
        }
        return aggregating;
    }

    public synchronized void setAggregating(boolean on) {
        if (aggregating != null && on == isAggregating()) {
            return;
        }

        aggregating = on;

        CoverageProviderHelper.setAggregating(project, on);
    }

    public synchronized boolean isEnabled() {
        if (enabled == null) {
            enabled = CoverageProviderHelper.isEnabled(project);
        }
        return enabled;
    }

    public synchronized void setEnabled(boolean on) {
        if (enabled != null && on == isEnabled()) {
            return;
        }

        enabled = on;
        timestamp = 0;

        if (!on) {
            hitCounts = null;
            fullNames = null;
        }

        CoverageProviderHelper.setEnabled(project, on);
    }

    public synchronized void clear() {
        File file = getPythonCoverageFile();
        if (file.exists()) {
            file.delete();
        }

        file = getNbCoverageFile();
        if (file.exists()) {
            file.delete();
        }

        hitCounts = null;
        fullNames = null;
        timestamp = 0;
    }

    public synchronized FileCoverageDetails getDetails(FileObject fo, Document doc) {
        update();

        if (hitCounts == null) {
            return null;
        }
        String path = FileUtil.toFile(fo).getPath();
        if (path == null) {
            return null;
        }

        String lines = hitCounts.get(path);
        if (lines == null) {
            String name = fo.getNameExt();
            String fullName = fullNames.get(name);
            if (fullName != null && !fullName.equals(path)) {
                lines = hitCounts.get(fullName);
            }
        }

        if (lines != null) {
            List<LineCount> hits = getLineCounts(lines);
            int max = 0;
            for (LineCount lineCount : hits) {
                if (lineCount.lineno > max) {
                    max = lineCount.lineno;
                }
            }

            int[] result = new int[max];
            for (int i = 0; i < max; i++) {
                result[i] = COUNT_UNKNOWN;
            }
            for (LineCount lineCount : hits) {
                assert lineCount.lineno > 0;
                result[lineCount.lineno - 1] = lineCount.count;
            }

            inferCounts(result, doc);

            return new PythonFileCoverageDetails(result);
        }

        return null;
    }

    private File getNbCoverageDir() {
        return new File(FileUtil.toFile(project.getProjectDirectory().getFileObject("nbproject")), "private" + File.separator + "coverage"); // NOI18N
    }

    private File getNbCoverageFile() {
        return new File(getNbCoverageDir(), ".nbcoverage"); // NOI18N
    }

    private File getPythonCoverageFile() {
        return new File(getNbCoverageDir(), ".coverage"); // NOI18N
    }

    private List<LineCount> getLineCounts(String lines) {
        int size = lines.length() / 6;
        List<LineCount> lineCounts = new ArrayList<LineCount>(size);

        int start = 1;
        int i = start;
        int length = lines.length();
        int line = 0;
        while (i < length) {
            char c = lines.charAt(i);
            if (c == ':') {
                line = Integer.valueOf(lines.substring(start, i));
                start = i + 1;
            } else if (c == ',' || c == '}') {
                int count = Integer.valueOf(lines.substring(start, i));
                lineCounts.add(new LineCount(line, count));
                start = i + 1;
            } else if (c == ' ') {
                start = i + 1;
            }
            i++;
        }

        return lineCounts;
    }

    private boolean isExecutableToken(TokenId id) {
        return id != PythonTokenId.WHITESPACE && id != PythonTokenId.NEWLINE && id != PythonTokenId.COMMENT;
    }

    /**
     * Add inferred counts - look at execution lines and compare to document
     * contents to conclude that for example comments between two executed
     * lines should be inferred as executed
     */
    private void inferCounts(int[] result, Document document) {
        BaseDocument doc = (BaseDocument) document;
        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, 0);
        if (ts == null) {
            return;
        }

        boolean[] continued = new boolean[result.length];
        TokenId[] lineFirstTokens = new TokenId[result.length];
        computeLineDetails(result, document, lineFirstTokens, continued);

        // coverage.py only records a hit on the LAST line of a multiline statement.
        // For example, you may have
        //    outstr = (outstr
        //              + string.hexdigits[(o >> 4) & 0xF]
        //              + string.hexdigits[o & 0xF])
        // ...and only the LAST line here is marked executable. Go and fix up that
        for (int lineno = result.length - 1; lineno >= 0; lineno--) {
            if (result[lineno] >= 0 && continued[lineno]) {
                for (lineno--; lineno >= 0; lineno--) {
                    if (result[lineno] == COUNT_UNKNOWN) {
                        result[lineno] = COUNT_INFERRED;
                        if (!continued[lineno]) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        // (1) If I have comment lines immediately before an executed line,
        //   mark those as executed as well.
        for (int lineno = 0; lineno < result.length; lineno++) {
            TokenId id = lineFirstTokens[lineno];
            if (id == PythonTokenId.DEF || id == PythonTokenId.CLASS) {
                for (int prev = lineno - 1; prev >= 0; prev--) {
                    if (lineFirstTokens[prev] == PythonTokenId.COMMENT) {
                        if (result[prev] == COUNT_UNKNOWN) {
                            result[prev] = COUNT_INFERRED;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        // (2) If I can find non-executable lines between executed lines
        //   mark all those as inferred.
        // ... unless that next executed line is a "class" or "def",
        // since these lines are probably the dividers between unrelated
        // functions.
        // (3) If I can find executable lines that are NOT continued from
        //  an executed line, then mark all such lines as a block until
        //  I get to an executed line.
        for (int lineno = 0; lineno < result.length; lineno++) {
            int count = result[lineno];
            if (count == COUNT_UNKNOWN) {
                if (isExecutableToken(lineFirstTokens[lineno])) {
                    // There's code here.
                    // If this line is not continued, mark it, and all lines
                    // up to the next known or inferred line, as not covered
                    if (!continued[lineno]) {
                        for (; lineno < result.length; lineno++) {
                            if (result[lineno] == COUNT_UNKNOWN) {
                                result[lineno] = COUNT_NOT_COVERED;
                            } else {
                                break;
                            }
                        }
                    }
                } else {
                    // Look ahead to the next known count, and iff it is
                    // executable (and not a def/class) mark all these lines
                    // as inferred.
                    boolean markInferred = true;
                    int i = lineno + 1;
                    for (; i < result.length; i++) {
                        int nextCount = result[i];
                        if (nextCount >= 0 || nextCount == COUNT_INFERRED) {
                            if (lineFirstTokens[i] == PythonTokenId.DEF || lineFirstTokens[i] == PythonTokenId.CLASS) {
                                markInferred = false;
                            }
                            break;
                        } else if (nextCount == COUNT_NOT_COVERED) {
                            markInferred = false;
                            break;
                        }
                    }
                    if (markInferred) {
                        for (int line = lineno; line < i; line++) {
                            result[line] = COUNT_INFERRED;
                        }
                        lineno = i;
                        continue;
                    }
                }
            }
        }
    }

    private void computeLineDetails(int[] result, Document document, TokenId[] lineFirstTokens, boolean[] continued) {
        BaseDocument doc = (BaseDocument) document;
        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, 0);
        if (ts == null) {
            return;
        }

        try {
            // Look for gaps in the lines and see if lines in between are executable
            int balance = 0;
            int currentOffset = 0;
            boolean prevWasContinue = false;
            for (int lineno = 0; lineno < result.length; lineno++) {
                // Update the line balance
                int begin = Utilities.getRowStart(doc, currentOffset);
                int end = Utilities.getRowEnd(doc, currentOffset);
                int nonWhiteOffset = Utilities.getRowFirstNonWhite(doc, begin);
                if (nonWhiteOffset != -1) {
                    begin = nonWhiteOffset;
                }

                ts.move(begin);

                if (ts.moveNext()) {
                    Token<? extends PythonTokenId> token = ts.token();
                    TokenId id = token.id();
                    lineFirstTokens[lineno] = id;
                    continued[lineno] = prevWasContinue || balance > 0;

                    do {
                        token = ts.token();
                        id = token.id();

                        if (id == PythonTokenId.LPAREN || id == PythonTokenId.LBRACE || id == PythonTokenId.LBRACKET) {
                            balance++;
                        } else if (id == PythonTokenId.RPAREN || id == PythonTokenId.RBRACE || id == PythonTokenId.RBRACKET) {
                            balance--;
                        } else if (id == PythonTokenId.NONUNARY_OP || id == PythonTokenId.ESC) {
                            prevWasContinue = true;
                        } else if (id != PythonTokenId.WHITESPACE && id != PythonTokenId.NEWLINE && id != PythonTokenId.COMMENT) {
                            prevWasContinue = false;
                        }
                    } while (ts.moveNext() && (ts.offset() <= end));
                } else {
                    lineFirstTokens[lineno] = PythonTokenId.WHITESPACE;
                    if (lineno > 0) {
                        continued[lineno] = continued[lineno - 1];
                    } else {
                        continued[lineno] = false;
                    }
                }

                currentOffset = Utilities.getRowEnd(doc, currentOffset) + 1;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    private synchronized void update() {
        File pythonCoverage = getPythonCoverageFile();
        if (!pythonCoverage.exists()) {
            // No recorded data! Done.
            return;
        }

        File nbCoverage = getNbCoverageFile();

        // Read & Parse the corresponding data structure into memory
        if (nbCoverage.exists() && timestamp < nbCoverage.lastModified()) {
            timestamp = nbCoverage.lastModified();
            hitCounts = new HashMap<String, String>();
            fullNames = new HashMap<String, String>();

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(nbCoverage));
                while (true) {
                    try {
                        String file = br.readLine();
                        String lines = br.readLine();

                        if (file == null || lines == null) {
                            break;
                        }

                        int last = Math.max(file.lastIndexOf('\\'), file.lastIndexOf('/'));
                        String base = file;
                        if (last != COUNT_INFERRED) {
                            base = file.substring(last + 1);
                        }

                        fullNames.put(base, file);

                        assert lines.startsWith("{");
                        assert lines.endsWith("}");

                        hitCounts.put(file, lines);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    public PythonExecution wrapWithCoverage(PythonExecution original) {
        InstalledFileLocator locator = InstalledFileLocator.getDefault();
        // Set COVERAGE_FILE to ${getPythonCoverageFile()}
        // Run with "-x"
        File coverageScript = locator.locate("coverage/coverage.py", "org-netbeans-modules-python-codecoverage.jar", false);
        assert coverageScript != null;

        File wrapper = locator.locate("coverage/coverage_wrapper.py", "org-netbeans-modules-python-codecoverage.jar", false);
        assert wrapper != null;

        PythonExecution execution = new PythonExecution(original);

        List<String> wrapperArgs = new ArrayList<String>();
        // TODO - path munging on Windows?
        File pythonCoverage = getPythonCoverageFile();
        File nbCoverage = getNbCoverageFile();
        File dir = getNbCoverageDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        wrapperArgs.add(pythonCoverage.getPath());
        wrapperArgs.add(nbCoverage.getPath());
        wrapperArgs.add(coverageScript.getPath());

        if (!CoverageManager.INSTANCE.isAggregating(project)) {
            wrapperArgs.add("-e"); // NOI18N
        }
        wrapperArgs.add("-x"); // NOI18N

        execution.setWrapperCommand(wrapper.getPath(),
                wrapperArgs.toArray(new String[wrapperArgs.size()]),
                new String[]{"COVERAGE_FILE=" + pythonCoverage.getPath()}); // NOI18N

        execution.setPostExecutionHook(new Runnable() {
            public void run() {
                // Process the data immediately since it's available when we need it...
                PythonCoverageProvider.this.update();
                CoverageManager.INSTANCE.resultsUpdated(project, PythonCoverageProvider.this);
            }
        });

        return execution;
    }

    public synchronized void notifyProjectOpened() {
        CoverageManager.INSTANCE.setEnabled(project, true);
    }

    public boolean supportsHitCounts() {
        return false;
    }

    public Set<String> getMimeTypes() {
        return mimeTypes;
    }

    private static class LineCount {
        private final int lineno;
        private int count;

        public LineCount(int lineno, int count) {
            this.lineno = lineno;
            this.count = count;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LineCount other = (LineCount) obj;
            if (this.lineno != other.lineno) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + this.lineno;
            return hash;
        }
    }

    private static class PythonFileCoverageDetails implements FileCoverageDetails {
        private int[] hitCounts;

        public PythonFileCoverageDetails(int[] hitCounts) {
            this.hitCounts = hitCounts;
        }

        public int getLineCount() {
            return hitCounts.length;
        }

        public boolean hasHitCounts() {
            return false;
        }

        public FileCoverageSummary getSummary() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public CoverageType getType(int lineNo) {
            int count = hitCounts[lineNo];
            switch (count) {
                case COUNT_UNKNOWN:
                    return CoverageType.UNKNOWN;
                case COUNT_NOT_COVERED:
                    return CoverageType.NOT_COVERED;
                case COUNT_INFERRED:
                    return CoverageType.INFERRED;
                default:
                    return CoverageType.COVERED;
            }
        }

        public int getHitCount(int lineNo) {
            return hitCounts[lineNo];
        }
    }
}
