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

package org.netbeans.modules.web.javascript.debugger.console;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.web.webkit.debugging.api.console.Console;
import org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 */
public class BrowserConsoleLogger implements Console.Listener {

    private Project project;

    public BrowserConsoleLogger(Project project) {
        this.project = project;
    }
    
    @NbBundle.Messages({"BrowserConsoleLoggerTitle=Browser Log"})
    public InputOutput getOutputLogger() {
       return IOProvider.getDefault().getIO(Bundle.BrowserConsoleLoggerTitle(), false); 
    }
    
    @Override
    public void messageAdded(ConsoleMessage message) {
        try {
            logMessage(message);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
    public static String getCurrentTime() {
        return formatter.format(new Date(System.currentTimeMillis()));
    }
    
    private void logMessage(ConsoleMessage msg) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(getCurrentTime());
        sb.append(" ");
        sb.append(msg.getText());
        sb.append(" ("+msg.getLevel()+","+msg.getSource()+","+msg.getType()+")");
        getOutputLogger().getOut().println(sb.toString());
        
        boolean first = true;
        if (msg.getStackTrace() != null) {
            for (ConsoleMessage.StackFrame sf : msg.getStackTrace()) {
                sb = new StringBuilder();
                if (first) {
                    sb.append("  caused by ");
                    first = false;
                } else {
                    sb.append("  ");
                }
                getOutputLogger().getOut().print(sb);
                sb = new StringBuilder();
                
                // TODO: could we convert for example
                // "http://localhost:89/SimpleLiveHTMLTest/js/app.js:8:9" into 
                // a more readable: "js/app.js:8:9"? We could do this for all project
                // files.
                
                sb.append(sf.getURLString()+":"+sf.getLine()+":"+sf.getColumn()+" ("+sf.getFunctionName()+")");
                getOutputLogger().getOut().println(sb.toString(), new MyListener(sf.getURLString(), sf.getLine(), sf.getColumn()));
            }
        }
        sb = new StringBuilder();
        if (first && msg.getURLString() != null && msg.getURLString().length() > 0) {
            sb.append("  at "+msg.getURLString());
            if (msg.getLine() != -1) {
                sb.append(":"+msg.getLine());
            }        
            getOutputLogger().getOut().println(sb.toString(), new MyListener(msg.getURLString(), msg.getLine(), -1));
        }
    }
    
    private class MyListener implements OutputListener {

        private String url;
        private int line;
        private int column;

        public MyListener(String url, int line, int column) {
            this.url = url;
            this.line = line;
            this.column = column;
        }
        
        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            MiscEditorUtil.getLine(project, url, line-1).show(Line.ShowOpenType.OPEN, 
                    Line.ShowVisibilityType.FOCUS, column != -1 ? column -1 : -1);
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
    
    }
    
}
