/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.execute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * handling of output coming from maven commandline builds
 * @author Milos Kleint
 */
class CommandLineOutputHandler extends AbstractOutputHandler {

    //8 means 4 paralel builds, one for input, one for output.
    private static final RequestProcessor PROCESSOR = new RequestProcessor("Maven ComandLine Output Redirection", 8); //NOI18N
    private static final Logger LOG = Logger.getLogger(CommandLineOutputHandler.class.getName());
    private InputOutput inputOutput;
    private static final Pattern linePattern = Pattern.compile("\\[(DEBUG|INFO|WARNING|ERROR|FATAL)\\] (.*)"); // NOI18N
    static final Pattern startPatternM2 = Pattern.compile("\\[INFO\\] \\[([\\w]*):([\\w]*)[ ]?.*\\]"); // NOI18N
    static final Pattern startPatternM3 = Pattern.compile("\\[INFO\\] --- (\\S+):\\S+:(\\S+)(?: [(]\\S+[)])? @ \\S+ ---"); // ExecutionEventLogger.mojoStarted NOI18N
    private static final Pattern mavenSomethingPlugin = Pattern.compile("maven-(.+)-plugin"); // NOI18N
    private static final Pattern somethingMavenPlugin = Pattern.compile("(.+)-maven-plugin"); // NOI18N
    /** @see org.apache.maven.cli.ExecutionEventLogger#logReactorSummary */
    private static final Pattern reactorFailure = Pattern.compile("\\[INFO\\] (.+) [.]* FAILURE \\[.+\\]"); // NOI18N
    private OutputWriter stdOut;
    private String currentProject;
    private String currentTag;
    Task outTask;
    private Input inp;
    private ProgressHandle handle;
    /** {@link MavenProject#getName} of first project in reactor to fail, if any */
    String firstFailure;

    CommandLineOutputHandler(ProgressHandle hand) {
        super(hand);
        handle = hand;
    }

    public CommandLineOutputHandler(InputOutput io, Project proj, ProgressHandle hand, RunConfig config) {
        this(hand);
        inputOutput = io;
        stdOut = inputOutput.getOut();
//        logger = new Logger();
        initProcessorList(proj, config);
    }

    @Override
    protected final void checkSleepiness() {
        handle.progress(currentProject == null ? "" : currentTag == null ? currentProject : currentProject + " " + currentTag); // NOI18N
        super.checkSleepiness();
    }


    void setStdOut(InputStream inStr) {
        outTask = PROCESSOR.post(new Output(inStr));
    }

    void setStdIn(OutputStream in) {
        inp = new Input(in, inputOutput);
        PROCESSOR.post(inp);
    }

    void waitFor() {
        inp.stopInput();
//        if (inTask != null) {
//            inTask.waitFinished();
//        }
        if (outTask != null) {
            outTask.waitFinished();
        }
    }

    @Override
    protected InputOutput getIO() {
        return this.inputOutput;
    }

    private static final String SEC_MOJO_EXEC = "mojo-execute"; //NOI18N
    private void closeCurrentTag() {
        if (currentTag != null) {
            CommandLineOutputHandler.this.processEnd(getEventId(SEC_MOJO_EXEC, currentTag), stdOut);
            currentTag = null;
        }
    }

    private class Output implements Runnable {

        private BufferedReader str;
        private boolean skipLF = false;

        public Output(InputStream instream) {
            str = new BufferedReader(new InputStreamReader(instream));
        }

        private String readLine() throws IOException {
            char[] char1 = new char[1];
            boolean isReady = true;
            StringBuilder buf = new StringBuilder();
            while (isReady) {
                int ret = str.read(char1);
                if (ret != 1) {
                     if (ret == -1 && buf.length() == 0) {
                         return null;
                     }
                    return buf.toString();
                }
                if (skipLF) {
                    skipLF = false;
                    if (char1[0] == '\n') { //NOI18N
                        continue;
                    }
                }
                if (char1[0] == '\n') { //NOI18N
                    return buf.toString();
                }
                if (char1[0] == '\r') { //NOI18N
                    skipLF = true;
                    return buf.toString();
                }
                buf.append(char1[0]);
                isReady = str.ready();
                if (!isReady) {
                    synchronized (this) {
                        try {
                            wait(500);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            if (!str.ready()) {
                                break;
                            }
                            isReady = true;
                        }
                    }

                }
            }
            return "&^#INCOMPLINE:" + buf.toString(); //NOI18N

        }

        public @Override void run() {
            CommandLineOutputHandler.this.processStart(getEventId(PRJ_EXECUTE,null), stdOut);
            try {

                String line = readLine();
                while (line != null) {
                    if (line.startsWith("&^#INCOMPLINE:")) { //NOI18N
                        stdOut.print(line.substring("&^#INCOMPLINE:".length())); //NOI18N
                        line = readLine();
                        continue;
                    }
                    if (line.startsWith("[INFO] Final Memory:")) { //NOI18N
                        // previous value [INFO] --------------- is too early, the compilation errors don't get processed in this case.
                        //heuristics..
                        closeCurrentTag();
                    }
                    String tag = null;
                    Matcher match = startPatternM3.matcher(line);
                    if (match.matches()) {
                        String mojoArtifact = match.group(1);
                        // XXX M3 reports artifactId of mojo whereas M2 reports goalPrefix; do not want to force every OutputProcessor to handle both
                        // XXX consider searching index on ArtifactInfo.PLUGIN_PREFIX instead
                        Matcher match2 = mavenSomethingPlugin.matcher(mojoArtifact);
                        if (match2.matches()) {
                            mojoArtifact = match2.group(1);
                        } else {
                            match2 = somethingMavenPlugin.matcher(mojoArtifact);
                            if (match2.matches()) {
                                mojoArtifact = match2.group(1);
                            }
                        }
                        tag = mojoArtifact + ':' + match.group(2);
                    } else {
                        match = startPatternM2.matcher(line);
                        if (match.matches()) {
                            tag = match.group(1) + ':' + match.group(2);
                        }
                    }
                    if (tag != null) {
                        closeCurrentTag();
                        currentTag = tag;
                        CommandLineOutputHandler.this.processStart(getEventId(SEC_MOJO_EXEC, tag), stdOut);
                        checkSleepiness();
                    } else {
                        match = linePattern.matcher(line);
                        if (match.matches()) {
                            String levelS = match.group(1);
                            Level level = Level.valueOf(levelS);
                            String text = match.group(2);
                            processLine(text, stdOut, level);
                            if (level == Level.INFO) {
                                checkProgress(text);
                            }
                        } else {
                            // oh well..
                            processLine(line, stdOut, Level.INFO);
                        }
                    }
                    if (firstFailure == null) {
                        match = reactorFailure.matcher(line);
                        if (match.matches()) {
                            firstFailure = match.group(1);
                        }
                    }
                    line = readLine();
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(CommandLineOutputHandler.class.getName()).log(java.util.logging.Level.FINE, null, ex);
            } finally {
                CommandLineOutputHandler.this.processEnd(getEventId(PRJ_EXECUTE, null), stdOut);
                try {
                    str.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    static class Input implements Runnable {

        private InputOutput inputOutput;
        private OutputStream str;
        private boolean stopIn = false;

        public Input(OutputStream out, InputOutput inputOutput) {
            str = out;
            this.inputOutput = inputOutput;
        }

        public void stopInput() {
            stopIn = true;
            try {
                inputOutput.getIn().close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public @Override void run() {
            Reader in = inputOutput.getIn();
            try {
                while (true) {
                    int read = in.read();
                    if (read != -1) {
                        str.write(read);
                        str.flush();
                    } else {
                        str.close();
                        return;
                    }
                    if (stopIn) {
                        return;
                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    str.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * #192200: try to indicate progress esp. in a reactor build.
     * @see org.apache.maven.cli.ExecutionEventLogger
     */
    private void checkProgress(String text) {
        switch (state) {
        case INITIAL:
            if (text.equals("Reactor Build Order:")) { // NOI18N
                state = ProgressState.GOT_REACTOR_BUILD_ORDER;
            }
            break;
        case GOT_REACTOR_BUILD_ORDER:
            if (text.trim().isEmpty()) {
                state = ProgressState.GETTING_REACTOR_PROJECTS;
            } else {
                state = ProgressState.INITIAL; // ???
            }
            break;
        case GETTING_REACTOR_PROJECTS:
            if (text.trim().isEmpty()) {
                state = ProgressState.NORMAL;
                reactorSize++; // so we do not show 100% completion while building last project
                handle.switchToDeterminate(reactorSize);
                LOG.log(java.util.logging.Level.FINE, "reactor size: {0}", reactorSize);
            } else {
                reactorSize++;
            }
            break;
        case NORMAL:
            if (forkCount == 0 && text.matches("-+")) { // NOI18N
                state = ProgressState.GOT_DASHES;
            } else if (text.startsWith(">>> ")) { // NOI18N
                forkCount++;
                LOG.log(java.util.logging.Level.FINE, "fork count up to {0}", forkCount);
            } else if (forkCount > 0 && text.startsWith("<<< ")) { // NOI18N
                forkCount--;
                LOG.log(java.util.logging.Level.FINE, "fork count down to {0}", forkCount);
            }
            break;
        case GOT_DASHES:
            if (text.startsWith("Building ") && !text.startsWith("Building in ") || text.startsWith("Skipping ")) { // NOI18N
                currentProject = text.substring(9);
                closeCurrentTag();
                handle.progress(currentProject, Math.min(++projectCount, reactorSize));
                LOG.log(java.util.logging.Level.FINE, "got project #{0}: {1}", new Object[] {projectCount, currentProject});
            }
            state = ProgressState.NORMAL;
            break;
        default:
            assert false : state;
        }
    }
    enum ProgressState {
        INITIAL,
        GOT_REACTOR_BUILD_ORDER,
        GETTING_REACTOR_PROJECTS,
        NORMAL,
        GOT_DASHES,
    }
    private ProgressState state = ProgressState.INITIAL;
    private int forkCount;
    private int reactorSize;
    private int projectCount;

}
