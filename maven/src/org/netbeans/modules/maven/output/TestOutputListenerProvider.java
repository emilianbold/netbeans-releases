/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.maven.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputUtils;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;


/**
 * processing test (surefire) output
 * @author  Milos Kleint
 */
public class TestOutputListenerProvider implements OutputProcessor {
        
    private static final String[] TESTGOALS = new String[] {
        "mojo-execute#surefire:test" //NOI18N
    };
    private Pattern failSeparatePattern;
    private Pattern failWindowsPattern1;
    private Pattern failWindowsPattern2;
    private Pattern outDirPattern;
    private Pattern outDirPattern2;
    private Pattern runningPattern;
    
    private static Logger LOG = Logger.getLogger(TestOutputListenerProvider.class.getName());

    
    String outputDir;
    String runningTestClass;
    private String delayedLine;
    
    /** Creates a new instance of TestOutputListenerProvider */
    public TestOutputListenerProvider() {
        failSeparatePattern = Pattern.compile("(?:\\[surefire\\] )?Tests run.*[<]* FAILURE[!]*[\\s]*", Pattern.DOTALL); //NOI18N
        failWindowsPattern1 = Pattern.compile("(?:\\[surefire\\] )?Tests run.*", Pattern.DOTALL); //NOI18N
        failWindowsPattern2 = Pattern.compile(".*[<]* FAILURE [!]*.*", Pattern.DOTALL); //NOI18N
        runningPattern = Pattern.compile("(?:\\[surefire\\] )?Running (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern = Pattern.compile(".*Surefire report directory\\: (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern2 = Pattern.compile(".*Setting reports dir\\: (.*)", Pattern.DOTALL); //NOI18N
    }
    
    public String[] getWatchedGoals() {
        return TESTGOALS;
    }
    
    public void processLine(String line, OutputVisitor visitor) {
        if (delayedLine != null) {
            Matcher match = failWindowsPattern2.matcher(line);
            if (match.matches()) {
                visitor.setOutputListener(new TestOutputListener(runningTestClass, outputDir), true);
            }
            visitor.setLine(delayedLine + "\n" + line);
            delayedLine = null;
            return;
        }
        Matcher match = outDirPattern.matcher(line);
        if (match.matches()) {
            outputDir = match.group(1);
            return;
        }
        match = outDirPattern2.matcher(line);
        if (match.matches()) {
            outputDir = match.group(1);
            return;
        }
        match = runningPattern.matcher(line);
        if (match.matches()) {
            runningTestClass = match.group(1);
            return;
        }
        match = failSeparatePattern.matcher(line);
        if (match.matches()) {
            visitor.setOutputListener(new TestOutputListener(runningTestClass, outputDir), true);
            return;
        }
        match = failWindowsPattern1.matcher(line);
        if (match.matches()) {
            //we should not get here but possibly can on windows..
            visitor.skipLine();
            delayedLine = line;
        }
        
    }
    
    public String[] getRegisteredOutputSequences() {
        return TESTGOALS;
    }
    
    public void sequenceStart(String sequenceId, OutputVisitor visitor) {
    }
    
    public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
    }
    
    public void sequenceFail(String sequenceId, OutputVisitor visitor) {
    }
    
    private static class TestOutputListener implements OutputListener {
        private String testname;
        private String outputDir;
        private Pattern testNamePattern = Pattern.compile(".*\\((.*)\\).*<<< (?:FAILURE)?(?:ERROR)?!\\s*"); //NOI18N
        
        public TestOutputListener(String test, String outDir) {
            testname = test;
            outputDir = outDir;
        }
        /** Called when a line is selected.
         * @param ev the event describing the line
         */
        public void outputLineSelected(OutputEvent ev) {
        }
        
        /** Called when some sort of action is performed on a line.
         * @param ev the event describing the line
         */
        public void outputLineAction(OutputEvent ev) {
            FileObject outDir = null;
            if (outputDir != null) {
                File fl = FileUtil.normalizeFile(new File(outputDir));
                FileUtil.refreshFor(fl);
                outDir = FileUtil.toFileObject(fl);
            } 
            if (outDir == null) {
                LOG.info("Cannot find path " + outputDir + " to follow link in Output Window."); //NOI18N
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TestOutputListenerProvider.class, "MSG_CannotFollowLink1"));
                return;
            }
            outDir.refresh();
            FileObject report = outDir.getFileObject(testname + ".txt"); //NOI18N
            Project prj = FileOwnerQuery.getOwner(outDir);
            if (prj != null) {
                NbMavenProjectImpl nbprj = prj.getLookup().lookup(NbMavenProjectImpl.class);
                File testDir = new File(nbprj.getOriginalMavenProject().getBuild().getTestSourceDirectory());

                if (report != null) {
                    String nm = testname.lastIndexOf('.') > -1  //NOI18N
                            ? testname.substring(testname.lastIndexOf('.'))  //NOI18N
                            : testname;
                    openLog(report, nm, testDir);
                } else {
                    LOG.info("Cannot find report path " + outputDir + testname + ".txt to follow link in Output Window."); //NOI18N
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TestOutputListenerProvider.class, "MSG_CannotFollowLink2"));
                }
            }
        }
        
        /** Called when a line is cleared from the buffer of known lines.
         * @param ev the event describing the line
         */
        public void outputLineCleared(OutputEvent ev) {
        }
        
        private void openLog(FileObject fo, String title, File testDir) {
            try {
                IOProvider.getDefault().getIO(title, false).getOut().reset();
            } catch (Exception exc) {
                ErrorManager.getDefault().notify(exc);
            }
            InputOutput io = IOProvider.getDefault().getIO(title, false);
            io.select();
            BufferedReader reader = null;
            OutputWriter writer = io.getOut();
            String line = null;
            try {
                reader = new BufferedReader(new InputStreamReader(fo.getInputStream()));
                ClassPath classPath = null;
                while ((line = reader.readLine()) != null) {
                    Matcher m = testNamePattern.matcher(line);
                    if (m.matches()) {
                        String testClassName = m.group(1).replace('.', File.separatorChar) + ".java"; //NOI18N
                        File testClassFile = new File(testDir, testClassName);
                        FileObject testFileObject = FileUtil.toFileObject(testClassFile);
                        classPath = ClassPath.getClassPath(testFileObject, ClassPath.EXECUTE);
                    }
                    if (classPath != null) {
                        OutputListener list = OutputUtils.matchStackTraceLine(line, classPath);
                        if (list != null) {
                            writer.println(line, list, true);
                        } else {
                            writer.println(line);
                        }
                    } else {
                        writer.println(line);
                    }
                }
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            } finally {
                writer.close();
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    }
}
