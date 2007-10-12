/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.ruby.rubyproject;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.StatusBar;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.rubyproject.execution.OutputRecognizer;
import org.netbeans.modules.ruby.rubyproject.execution.OutputRecognizer.ActionText;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 * Notifier which listens on test output and if it sees what looks like a failure,
 * alerts to the problem in the editor status bar; otherwise it reports successes
 * in the status bar.
 *
 * @author Tor Norbye
 */
public class TestNotifier extends OutputRecognizer implements Runnable {
    
    /** Most recent message shown in the editor */
    private static String mostRecentMessage;
    
    /** Should we accumulate scores? */
    private boolean accumulate;
    /** Should we only post errors in the editor footer if there are failures? */
    private boolean showSuccesses;
    
    /** Counts for the various types of errors we see in the output */
    private int examples;
    private int failures;
    private int notImplemented;
    private int tests;
    private int assertions;
    private int errors;
    private boolean seenTestUnit;
    private boolean seenRSpec;
    private boolean seenRake;
    
    /** 
     * Create a new TestNotifier.
     * <p>
     * @param accumulate If true, accumulate the amounts reported and report a final
     * summary of all. Otherwise, it will simply warn if there are errors and
     * forward the message to the editor.
     * @param showSuccesses If true, post results to the editor window even if
     * there are no failures
     */
    public TestNotifier(boolean accumulate, boolean showSuccesses) {
        this.accumulate = accumulate;
        this.showSuccesses = showSuccesses;
    }
    
    /** Turn off notification? */
    private static final boolean QUIET = Boolean.getBoolean("ruby.quiet.tests"); // NOI18N

    // Test::Unit:   test/unit/testresult.rb#
    private final static Pattern TEST_UNIT_PATTERN = 
        // The final \\s? in the patterns is to allow \r on Windows
        Pattern.compile("(\\d+) tests, (\\d+) assertions, (\\d+) failures, (\\d+) errors\\s?"); // NOI18N
         
    // RSpec: see {rspec}/lib/spec/runner/formatter/base_text_formatter.rb#dump_summary
    // "not implemented" changed to "pending" in rspec 1.0.8
    private final static Pattern RSPEC_PATTERN = 
        Pattern.compile("(\\d+) examples?, (\\d)+ failures?(, (\\d+) (pending|not implemented))?\\s?"); // NOI18N
    
    // Rake: lib/tasks/testing.rake:53:  raise "Test failures" unless exceptions.empty?
    private final static Pattern RAKE_PATTERN =
        Pattern.compile("Test failures\\s?"); // NOI18N
    
    private final Pattern[] PATTERNS = new Pattern[] { TEST_UNIT_PATTERN, RSPEC_PATTERN, RAKE_PATTERN };

    @Override
    public void start() {
        // Possibly clear editor from previous error message (#115073)
        String lastMessage = mostRecentMessage;
        if (lastMessage != null) {
            JTextComponent pane = EditorRegistry.lastFocusedComponent();
            if (pane != null) {
                if (pane.isShowing()) {
                    String text = Utilities.getStatusText(pane);
                    if (lastMessage.equals(text)) {
                        Utilities.clearStatusText(pane);
                    }
                }
            }
            mostRecentMessage = null;
        }

        resetResults();
    }
    
    @Override
    public ActionText processLine(String outputLine) {
        if (QUIET) {
            return null;
        }

        String line = outputLine;
        if (Util.containsAnsiColors(outputLine)) {
            line = Util.stripAnsiColors(outputLine);
        }

        for (Pattern pattern : PATTERNS) {
            Matcher match = pattern.matcher(line);
            if (match.matches()) {
                if (!accumulate) {
                    resetResults();
                }

                addTotals(pattern, match);
                
                if (isError() || isWarning() || showSuccesses) {
                    // Display in editor - asynchronously since it must be done
                    // from the event dispatch thread
                    run();
                } else {
                    String summary = getSummary();
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TestNotifier.class, "TestsCompleted", summary));
                }
            }
        }

        if (line != outputLine) {
            return new ActionText(new String[] { line }, null, null, null);
        }
        return null;
    }
    
    private void resetResults() {
        examples = 0;
        failures = 0;
        notImplemented = 0;
        tests = 0;
        assertions = 0;
        errors = 0;
        seenTestUnit = false;
        seenRake = false;
        seenRake = false;
    }
    
    public void run() {
        String summary = getSummary();
        String message;
        if (isError()) {
            message = NbBundle.getMessage(AutoTestSupport.class, "TestsFailed", summary);
        } else {
            message = NbBundle.getMessage(AutoTestSupport.class, "TestsCompleted", summary);
        }
        JTextComponent pane = EditorRegistry.lastFocusedComponent();
        if (pane != null) {
            if (pane.isShowing()) {
                mostRecentMessage = message;
                if (isError()) {
                    org.netbeans.editor.Utilities.setStatusBoldText(pane, message);
                } else {
                    // Attempt to show the text in green
                    EditorUI eui = Utilities.getEditorUI(pane);
                    if (eui != null) {
                        StatusBar statusBar = eui.getStatusBar();
                        if (statusBar != null) {
                            Coloring coloring = null;
                            if (isWarning()) {
                                // Not implemented rspecs: show yellow rather than green
                                coloring = new Coloring(SettingsDefaults.defaultFont, Color.BLACK, Color.YELLOW);
                            } else {
                                coloring = new Coloring(SettingsDefaults.defaultFont, Color.BLACK, Color.GREEN);
                            }
                            statusBar.setText(StatusBar.CELL_MAIN, message, coloring);
                            return;
                        }
                    }

                    org.netbeans.editor.Utilities.setStatusText(pane, message);
                }
            }
        } else {
            // Can't find an editor window - just show in status bar
             StatusDisplayer.getDefault().setStatusText(message);
        }
    }
    
    /** For unit tests only.
     * @todo Use a mock object for the status displayer and test processLine itself instead
     */
    boolean recognizeLine(String line) {
        for (Pattern pattern : PATTERNS) {
            Matcher match = pattern.matcher(line);
            if (match.matches()) {
                return true;
            }
        }
        
        return false;
    }

    private void addTotals(Pattern pattern, Matcher matcher) {
        assert PATTERNS.length == 3; // If you add more patterns, make sure you update the below logic
        if (pattern == TEST_UNIT_PATTERN) {
            seenTestUnit = true;
            tests += Integer.parseInt(matcher.group(1));
            assertions += Integer.parseInt(matcher.group(2));
            failures += Integer.parseInt(matcher.group(3));
            errors += Integer.parseInt(matcher.group(4));
        } else if (pattern == RSPEC_PATTERN) {
            seenRSpec = true;
            examples += Integer.parseInt(matcher.group(1));
            failures += Integer.parseInt(matcher.group(2));
            if (matcher.group(4) != null) {
                notImplemented += Integer.parseInt(matcher.group(4));
            }
        } else {
            assert pattern == RAKE_PATTERN;
            seenRake = true;
            errors += 1;
        }
    }

    private void appendSummary(StringBuilder sb, String s) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(s);
    }
    
    private String getCountDescription(String oneKey, String manyKey, int count) {
        String countString = Integer.toString(count);
        
        return NbBundle.getMessage(TestNotifier.class, count == 1 ? oneKey : manyKey, countString);
    }
    
    boolean isError() {
        return errors > 0 || failures > 0;
    }
    
    boolean isWarning() {
        return notImplemented > 0;
    }
    
    String getSummary() {
        StringBuilder sb = new StringBuilder(80);
        if (seenTestUnit || tests > 0) {
            appendSummary(sb, getCountDescription("OneTest", "ManyTests", tests));
        }
        if (seenTestUnit || assertions > 0) {
            appendSummary(sb, getCountDescription("OneAssert", "ManyAssert", assertions));
        }
        if (seenRSpec || examples > 0) {
            appendSummary(sb, getCountDescription("OneExample", "ManyExamples", examples));
        }
        if (!seenRake || seenTestUnit || seenRSpec) {
            appendSummary(sb, getCountDescription("OneFailure", "ManyFailures", failures));
        }
        if (seenTestUnit || errors > 0) {
            appendSummary(sb, getCountDescription("OneError", "ManyErrors", errors));
        }
        if (notImplemented > 0) {
            appendSummary(sb, getCountDescription("OneNotImpl", "ManyNotImpl", notImplemented));
        }
        
        return sb.toString();
    }
}
