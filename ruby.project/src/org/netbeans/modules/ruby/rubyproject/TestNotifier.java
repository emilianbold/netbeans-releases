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
package org.netbeans.modules.ruby.rubyproject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.ruby.rubyproject.execution.OutputRecognizer;
import org.netbeans.modules.ruby.rubyproject.execution.OutputRecognizer.FileLocation;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/**
 * Notifier which listens on test output and if it sees what looks like a a failure,
 * alerts to the problem in the editor status bar; otherwise it reports successes
 * in the status bar.
 *
 * @author Tor Norbye
 */
public class TestNotifier extends OutputRecognizer implements Runnable {
    /** Turn off notification? */
    private static final boolean QUIET = Boolean.getBoolean("ruby.quiet.tests"); // NOI18N
    
    private final Pattern[] PATTERNS = new Pattern[] {
        // The final \\s? in the patterns is to allow \r on Windows
        
        // Test::Unit:   test/unit/testresult.rb#
        Pattern.compile("\\d+ tests, \\d+ assertions, \\d+ failures, \\d+ errors\\s?"), // NOI18N
                
        // RSpec: see {rspec}/lib/spec/runner/formatter/base_text_formatter.rb#dump_summary
        Pattern.compile("\\d+ examples?, \\d+ failures?(, \\d+ not implemented)?\\s?") // NOI18N
    };
    
    private String message;

    @Override
    public FileLocation processLine(String line) {
        if (QUIET) {
            return null;
        }

        for (Pattern pattern : PATTERNS) {
            Matcher match = pattern.matcher(line);
            if (match.matches()) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TestNotifier.class, "TestsCompleted", line));
                // TODO - fail on "not implemented" too?
                if ((line.indexOf(" 0 failures") == -1) || (line.indexOf(" 0 errors") == -1)) { // NOI18N
                    message = NbBundle.getMessage(AutoTestSupport.class, "TestsFailed", line);
                    run();
                }
            }
        }
        return null;
    }

    public void run() {
        if (message == null) {
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this);
            return;
        }
        org.openide.nodes.Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes == null) {
            return;
        }
        for (org.openide.nodes.Node node : nodes) {
            EditorCookie ec = node.getCookie(EditorCookie.class);
            if (ec == null) {
                continue;
            }
            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes == null) {
                continue;
            }
            for (JEditorPane pane : panes) {
                if (pane.isShowing()) {
                    org.netbeans.editor.Utilities.setStatusBoldText(pane, message);
                    return;
                }
            }
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

}
