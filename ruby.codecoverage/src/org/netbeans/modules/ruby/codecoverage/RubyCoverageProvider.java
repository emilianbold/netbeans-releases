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
package org.netbeans.modules.ruby.codecoverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.text.Document;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.gsf.codecoverage.api.CoverageActionFactory;
import org.netbeans.modules.gsf.codecoverage.api.CoverageManager;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProviderHelper;
import org.netbeans.modules.gsf.codecoverage.api.CoverageType;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Code coverage for Ruby, built on top of RCov.
 *
 * @author Tor Norbye
 */
public final class RubyCoverageProvider implements CoverageProvider {

    private Map<String, String> hitCounts;
    private Map<String, String> fullNames;
    private long timestamp;
    private Set<String> mimeTypes;
    private Project project;
    private Boolean enabled;
    private Boolean aggregating;

    public RubyCoverageProvider(Project project) {
        this.project = project;

        mimeTypes = new HashSet<String>();
        mimeTypes.add(RubyInstallation.RUBY_MIME_TYPE);
        // Find out if RCov supports RHTML;
        //mimeTypes.add(RubyInstallation.RHTML_MIME_TYPE);
    }

    public static RubyCoverageProvider get(Project project) {
        return project.getLookup().lookup(RubyCoverageProvider.class);
    }

    public boolean supportsHitCounts() {
        return true;
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

        timestamp = 0;

        if (on) {
            GemManager gemManager = RubyPlatform.gemManagerFor(project);
            if (gemManager == null || !gemManager.isGemInstalled("rcov")) {
                NotifyDescriptor nd =
                        new NotifyDescriptor.Message(NbBundle.getMessage(RubyCoverageProvider.class, "RcovNotInstalled"),
                        NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                return;
            }
        } else {
            hitCounts = null;
            fullNames = null;
        }

        enabled = on;
        CoverageProviderHelper.setEnabled(project, on);
    }

    private static List<LineCount> getLineCounts(String lines) {
        int size = lines.length() / 5;
        List<LineCount> lineCounts = new ArrayList<LineCount>(size);

        int start = 0;
        int i = start;
        int length = lines.length();
        int line = 0;
        int startLine = -1;
        while (i < length) {
            char c = lines.charAt(i);
            if (c == ':') {
                line = Integer.valueOf(lines.substring(start, i));
                start = i + 1;
            } else if (c == ',') {
                int count = Integer.valueOf(lines.substring(start, i));
                if (startLine != -1 && startLine < line) {
                    for (int l = startLine; l <= line; l++) {
                        lineCounts.add(new LineCount(l, count));
                    }
                    startLine = -1;
                } else {
                    lineCounts.add(new LineCount(line, count));
                }
                start = i + 1;
            } else if (c == ' ') {
                start = i + 1;
            } else if (c == '>') {
                startLine = Integer.valueOf(lines.substring(start, i));
                start = i + 1;
            }
            i++;
        }

        return lineCounts;
    }

    private static FileCoverageSummary createSummary(Project project, String fileName, List<LineCount> counts) {
        // Compute coverage:
        int lineCount = 0;
        int notExecuted = 0;
        int partialCount = 0;
        int inferredCount = 0;
        for (LineCount lc : counts) {
            if (lc.lineno > lineCount) {
                lineCount = lc.lineno;
            }
            if (lc.count == -2) {
                notExecuted++;
            } else if (lc.count == -1) {
                inferredCount++;
            }
        }

        int executedCount = lineCount - notExecuted;
        FileObject file;
        File f = new File(fileName);
        if (f.exists()) {
            file = FileUtil.toFileObject(f);
        } else {
            file = project.getProjectDirectory().getFileObject(fileName.replace('\\', '/'));
        }
        if (file == null) {
            Sources sources = project.getLookup().lookup(Sources.class);
            if (sources != null) {
                // From RubyBaseProject, downstream
                String SOURCES_TYPE_RUBY = "ruby"; // NOI18N
                for (SourceGroup sg : sources.getSourceGroups(SOURCES_TYPE_RUBY)) { // NOI18N
                    FileObject root = sg.getRootFolder();
                    if (fileName.indexOf('\\') != -1) {
                        file = root.getFileObject(fileName.replace("\\", "/")); // NOI18N
                    } else {
                        file = root.getFileObject(fileName);
                    }
                    if (file != null) {
                        break;
                    }
                }
            }
        }

        FileCoverageSummary result = new FileCoverageSummary(file, fileName, lineCount, executedCount,
                inferredCount, partialCount);

        return result;
    }

    public synchronized List<FileCoverageSummary> getResults() {
        List<FileCoverageSummary> results = new ArrayList<FileCoverageSummary>();

        update();

        if (hitCounts == null) {
            return null;
        }

        for (Map.Entry<String, String> entry : hitCounts.entrySet()) {
            String fileName = entry.getKey();
            List<LineCount> counts = getLineCounts(entry.getValue());

            FileCoverageSummary result = createSummary(project, fileName, counts);
            results.add(result);
        }

        return results;
    }

    public static Action createCoverageAction(Project project) {
        InstallRCovAction installRCovAction = new InstallRCovAction(project);
        if (!installRCovAction.isEnabled()) {
            installRCovAction = null;
        }

        // PENDING:
        // Add other actions here? For example, checkbox to control whether we also show the
        // HTML report after running it; include callsites in the report;
        // include bogo profiling data; do coverage diff, etc.

        return CoverageActionFactory.createCollectorAction(installRCovAction, null);
    }

    public synchronized void clear() {
        File file = getRubyCoverageFile();
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

            int[] result = new int[max + 1];
            for (int i = 0; i < max + 1; i++) {
                result[i] = -1;
            }
            for (LineCount lineCount : hits) {
                assert lineCount.lineno >= 0;
                result[lineCount.lineno] = lineCount.count;
            }

            return new RubyFileCoverageDetails(fo, result, project, path, hits, timestamp);
        }

        return null;
    }

    public Set<String> getMimeTypes() {
        return mimeTypes;
    }

    private File getNbCoverageDir() {
        return new File(FileUtil.toFile(project.getProjectDirectory().getFileObject("nbproject")), "private" + File.separator + "coverage"); // NOI18N
    }

    private File getNbCoverageFile() {
        return new File(getNbCoverageDir(), ".nbcoverage"); // NOI18N
    }

    private File getRubyCoverageFile() {
        return new File(getNbCoverageDir(), ".coverage"); // NOI18N
    }

    public synchronized void notifyProjectOpened() {
        CoverageManager.INSTANCE.setEnabled(project, true);
    }

    public void setAvailable(boolean b) {
        // TBD - instead of enabling, should I provide some other way to trigger a version check?
        CoverageManager.INSTANCE.setEnabled(project, true);
    }

    private synchronized void update() {
        File rubyCoverage = getRubyCoverageFile();
        if (!rubyCoverage.exists()) {
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
                        if (last != -1) {
                            base = file.substring(last + 1);
                        }

                        fullNames.put(base, file);
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

    /**
     * Wrap the given RubyExecutionDescriptor with a new descriptor that implements code coverage.
     * What this will essentially do is create a new descriptor which delegates to rcov to run the original
     * Ruby program, and also registers a post-execution hook which will refresh the coverage data
     * and then delegate to the original post execution hook.
     *
     * Unfortunately, it's not as simple as that ;-).  First, rcov produces binary data we can't read
     * (it's just Marshal.dump'ed binary Ruby data structures). Rather than try to read this data directly
     * (which could be error prone, especially in the presence of different versions of interpreters),
     * I therefore run the "rcov_wrapper.rb" script, which registers its own shutdown hook with Ruby,
     * and then delegates to rcov. In the shutdown hook, run after rcov is finished, it Marshal.load's
     * the data back and then writes the data in a plain ASCII format NetBeans can read directly.
     * (It would be nice if this was tied more closely into RCov's dumper routines.)
     *
     * Second, this approach really only works when we're launching Ruby programs directly (e.g. running
     * the user's program, or running a file, or running the Rails server, etc.)
     *
     * If we run Rake, we will only record coverage for the Rake program (and the Rakefile) itself.
     * Rake will launch external commands, which we wouldn'be be recording because rake runs Ruby
     * directly, not under RCov control. So what we really have to do is trick Rake into delegating
     * for us. This is done by the rake_wrapper.rb script. We preload it into rake (-r), and what the
     * script does is rewrite the Kernel.system() call. When Rake tries to launch a command, it wil
     * eventually wind up calling Kernel.system(), and our customized routine will take apart its arguments,
     * insert rcov in the middle as appropriate, and then call the real (aliased) Kernel.system.
     * Since this can end up calling many ruby processes, we have to aggregate the data across these
     * runs (with rcov --aggrevate) even if the user doesn't want to aggregate data from previous
     * runs.
     *
     * There is one final wrinkle: When you are executing tests under the NetBeans test runner,
     * rake is ALREADY running with preloaded routines (nb_test_mediator.rb, nb_test_runner.rb, etc.).
     * These don't happily coexist with the rake_wrapper. So in this case we remove this pre-existing
     * script from the command line, and instead add it to an environment variable that the rake_wrapper
     * will read and delegate to at the end.
     *
     * Finally, we also add output converters which try to remove from the output any stacktrace lines
     * referring to the wrapper scripts, to make test output etc. less confusing for the user.
     *
     * @param original The original execution descriptor that we want to add code coverage for
     * @param isRake Set to true if we are launching rake in this descriptor. Special rules apply.
     * @param includeName If non null, tell rcov to explicitly include this file name. This is used to
     *   override the usual file exclusion rules (which for example excludes test cases by default).
     * @return A new RubyExecutionDescriptor which performs the same task as the original parameter,
     *   but records code coverage as well.
     */
    public RubyExecutionDescriptor wrapWithCoverage(final RubyExecutionDescriptor original, boolean isRake, String includeName) {
        RubyPlatform platform = original.getPlatform();

        File rcov = new File(platform.getInterpreterFile().getParentFile(), "rcov"); // NOI18N
        if (!rcov.exists()) {
            // Ah, Windows. Goodie.
            rcov = new File(platform.getInterpreterFile().getParentFile(), "rcov.bat"); // NOI18N
            if (!rcov.exists()) {
                rcov = new File(platform.getInterpreterFile().getParentFile(), "rcov.cmd"); // NOI18N
                if (!rcov.exists()) {
                    Logger.getLogger(RubyCoverageProvider.class.getName()).log(Level.WARNING, "Warning: RCov not found at " + rcov.getPath());
                    return original;
                }
            }
        }

        File dir = getNbCoverageDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        InstalledFileLocator locator = InstalledFileLocator.getDefault();
        File script = locator.locate("coverage/rcov_wrapper.rb", "org-netbeans-modules-ruby-codecoverage.jar", false); // NOI18N
        assert script != null;

        String target = script.getPath();

        List<String> args = new ArrayList<String>(20);

        File nbCoverage = getNbCoverageFile();
        File rubyCoverage = getRubyCoverageFile();
        args.add(rubyCoverage.getPath());
        args.add(nbCoverage.getPath());

        boolean isAggregating = isAggregating();

        Map<String, String> additionalEnv = original.getAdditionalEnvironment();

        if (!isRake) {
            args.add(rcov.getPath());
            buildRcovArgs(null, args, isAggregating, includeName, original);
        }

        // Original interpreter and args etc
        args.add(original.getScript());

        String initial = original.getInitialArgsPlain();

        if (isRake) {
            // Copy existing env instead of just adding to it in case it's immutable (such as Collections.emptyMap())
            additionalEnv = new HashMap<String, String>(additionalEnv);

            File rakeWrapper = locator.locate("coverage/rake_wrapper.rb", "org-netbeans-modules-ruby-codecoverage.jar", false);
            assert rakeWrapper != null;
            additionalEnv.put("NB_RAKE_WRAPPER", rakeWrapper.getPath()); // NOI18N
            additionalEnv.put("NB_RCOV_PATH", rcov.getPath()); // NOI18N

            if (initial != null) {
                String[] ia = Utilities.parseParameters(initial);
                if (ia.length == 2 && "-r".equals(ia[0])) { // NOI18N
                    additionalEnv.put("NB_DELEGATED_SCRIPT", ia[1]); // NOI18N
                    initial = "-r \"" + rakeWrapper.getPath() + "\""; // NOI18N
                }
            }

            // Rake can call rcov several times - for example once for each unit / functional / fixture test
            // and we want to aggregate these as a single run. Thus, we have to manually simulate the aggregation
            // here and always pass aggregate=true on to rake for a single run.
            if (!isAggregating) {
                clear();
            }

            //Utilities.parseParameters(target)
            StringBuilder rcovArgs = new StringBuilder();
            buildRcovArgs(rcovArgs, null, true, includeName, original);
            additionalEnv.put("NB_RCOV_ARGS", rcovArgs.toString()); // NOI18N

            // Determine if it's the test runner... if so, I have to leave everything alone
            // (because it doesn't work to have two -r commands - so instead I've modified
            // the nb_testrunner to delegate to the rake_wrapper instead
            // If not, we use the rake wrapper as a preload for rake.
        }

        String[] additionalArgs = original.getAdditionalArgs();
        if (additionalArgs != null) {
            if (!isRake) {
                args.add("--"); // NOI18N
            }
            for (String arg : additionalArgs) {
                args.add(arg);
            }
        }

        additionalArgs = args.toArray(new String[args.size()]);

        RubyExecutionDescriptor descriptor = new RubyExecutionDescriptor(original);
        descriptor.addAdditionalEnv(additionalEnv);
        descriptor.initialArgs(initial);
        descriptor.script(target);
        descriptor.additionalArgs(additionalArgs);
        HideCoverageFramesConvertor hideWrapperConverter = new HideCoverageFramesConvertor();
        descriptor.addOutConvertor(hideWrapperConverter);
        descriptor.addErrConvertor(hideWrapperConverter);
        descriptor.postBuild(new Runnable() {

            public void run() {
                RubyCoverageProvider.this.update();
                CoverageManager.INSTANCE.resultsUpdated(project, RubyCoverageProvider.this);

                if (original.getPostBuild() != null) {
                    original.getPostBuild().run();
                }
            }
        });

        return descriptor;
    }

    // Remove stacktrace lines that refer to frames in the wrapper scripts (rcov, or rcov_wrapper)
    private static class HideCoverageFramesConvertor implements LineConvertor {

        public List<ConvertedLine> convert(String line) {
            if (line.contains("/ruby2/coverage/") || line.contains("/rcov") || // NOI18N
                    (File.separatorChar == '\\' && (line.contains("\\ruby2\\coverage\\") || line.contains("\\rcov")))) { // NOI18N
                return Collections.emptyList();
            }

            return null;
        }
    }

    private void buildRcovArgs(StringBuilder sb, List<String> args, boolean isAggregating, String includeName, RubyExecutionDescriptor original) {
        // Rcov args
        String dataFile = getRubyCoverageFile().getPath();

        if (isAggregating) {
            if (args != null) {
                args.add("--aggregate"); // NOI18N
                args.add(dataFile);
            } else {
                sb.append(" --aggregate \""); // NOI18N
                sb.append(dataFile);
                sb.append("\""); // NOI18N
            }
        }

        if (args != null) {
            args.add("--save"); // NOI18N
            args.add(dataFile);
        } else {
            sb.append(" --save \""); // NOI18N
            sb.append(dataFile);
            sb.append("\""); // NOI18N
        }

        if (args != null) {
            // Collect data only
            args.add("--no-html"); // NOI18N
        } else {
            sb.append(" --no-html"); // NOI18N
        }

        // Include coverage collection on the current file too, if applicable
        // (e.g. collection on test files)
        if (includeName == null && original.getFileObject() != null) {
            includeName = original.getFileObject().getNameExt();
        }
        if (includeName != null) {
            includeName = includeName.substring(Math.max(includeName.lastIndexOf('\\'), includeName.lastIndexOf('/')) + 1);
            if (args != null) {
                args.add("--include-file"); // NOI18N
                args.add(includeName);
            } else {
                sb.append(" --include-file \""); // NOI18N
                sb.append(includeName);
                sb.append("\""); // NOI18N
            }
        }

        StringBuilder exclude = new StringBuilder(100); // NOI18N
        // Skip mediator scripts etc.
        exclude.append("\\/ruby2\\/,/\\\\ruby2\\\\/"); // NOI18N
        // This shows up when running specs
        exclude.append(",\\bfcntl\\b"); // NOI18N
        // No need: removed by gem stuff below
        //exclude.append(",rcov"); // NOI18N
        exclude.append(",/\\bvendor\\//"); // NOI18N
        RubyPlatform platform = original.getPlatform();
        if (platform != null) {
            String home = platform.getHome().getPath();
            // Skip stuff in gems and in the libraries - only include stuff in the project
            exclude.append(',');
            // Escape /'s so we're passing a regex
            exclude.append(home.replace("/", "\\/")); // NOI18N
            if (File.separatorChar == '\\') {
                exclude.append(home.replace('\\', '/').replace("/", "\\/")); // NOI18N
            }
            if (platform.hasRubyGemsInstalled()) {
                GemManager gemManager = platform.getGemManager();
                if (gemManager != null) {
                    String gemHome = gemManager.getGemHome();
                    if (gemHome != null && !gemHome.startsWith(home)) {
                        exclude.append(',');
                        exclude.append(gemHome.replace("/", "\\/")); // NOI18N
                        if (File.separatorChar == '\\') {
                            exclude.append(gemHome.replace('\\', '/').replace("/", "\\/")); // NOI18N
                        }
                    }
                }
            }
        }

        if (args != null) {
            args.add("--exclude-only"); // NOI18N
            args.add(exclude.toString()); // NOI18N
        } else {
            sb.append(" --exclude-only \""); // NOI18N
            sb.append(exclude.toString()); // NOI18N
            sb.append("\"");
        }

        // If on rails:
        if (project.getClass().getSimpleName().contains("Rails")) { // NOI18N
            if (args != null) {
                args.add("--rails"); // NOI18N
            } else {
                sb.append(" --rails"); // NOI18N
            }
        }

        // If unit testing:
        //if (args != null) {
        //    args.add("--test-unit-only"); // NOI18N
        //} else {
        //    sb.append(" --test-unit-only"); // NOI18N
        //}
    }

    private static class RubyFileCoverageDetails implements FileCoverageDetails {

        private final int[] hitCounts;
        private final String fileName;
        private final List<LineCount> lineCounts;
        private Project project;
        private final long lastUpdated;
        private final FileObject fileObject;

        public RubyFileCoverageDetails(FileObject fileObject, int[] hitCounts, Project project, String fileName, List<LineCount> lineCounts, long lastUpdated) {
            this.fileObject = fileObject;
            this.hitCounts = hitCounts;
            this.project = project;
            this.fileName = fileName;
            this.lineCounts = lineCounts;
            this.lastUpdated = lastUpdated;
        }

        public int getLineCount() {
            return hitCounts.length;
        }

        public boolean hasHitCounts() {
            return true;
        }

        public FileCoverageSummary getSummary() {
            return createSummary(project, fileName, lineCounts);
        }

        public CoverageType getType(int lineNo) {
            int count = hitCounts[lineNo];
            switch (count) {
                case -3:
                    return CoverageType.UNKNOWN;
                case -2:
                    return CoverageType.NOT_COVERED;
                case -1:
                    return CoverageType.INFERRED;
                default:
                    return CoverageType.COVERED;
            }
        }

        public int getHitCount(int lineNo) {
            return hitCounts[lineNo];
        }

        public long lastUpdated() {
            return lastUpdated;
        }

        public FileObject getFile() {
            return fileObject;
        }
    }

    private static class LineCount {

        private final int lineno;
        private final int count;

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
}
