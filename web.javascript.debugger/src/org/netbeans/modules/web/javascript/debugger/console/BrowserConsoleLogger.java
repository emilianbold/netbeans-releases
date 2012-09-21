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

import java.awt.Color;
import java.awt.SystemColor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.UIManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.api.ServerURLMapping;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.web.javascript.debugger.browser.ProjectContext;
import org.netbeans.modules.web.webkit.debugging.api.console.Console;
import org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 */
public class BrowserConsoleLogger implements Console.Listener {
    
    private static final String LEVEL_ERROR = "error";      // NOI18N
    private static final String LEVEL_DEBUG = "debug";      // NOI18N

    private ProjectContext pc;
    private InputOutput io;
    private Color colorStdBrighter;
    /** The last logged message. */
    private ConsoleMessage lastMessage;
    //private Color colorErrBrighter;

    public BrowserConsoleLogger(ProjectContext pc) {
        this.pc = pc;
        initIO();
    }
    
    @NbBundle.Messages({"BrowserConsoleLoggerTitle=Browser Log"})
    private void initIO() {
        io = IOProvider.getDefault().getIO(Bundle.BrowserConsoleLoggerTitle(), false);
        if (IOColors.isSupported(io) && IOColorPrint.isSupported(io)) {
            Color colorStd = IOColors.getColor(io, IOColors.OutputType.OUTPUT);
            //Color colorErr = IOColors.getColor(io, IOColors.OutputType.ERROR);
            Color background = UIManager.getDefaults().getColor("nb.output.background");    // NOI18N
            if (background == null) {
                background = SystemColor.window;
            }
            colorStdBrighter = shiftTowards(colorStd, background);
            //colorErrBrighter = shiftTowards(colorErr, background);
        }
    }
    
    private static Color shiftTowards(Color c, Color b) {
        return new Color((c.getRed() + b.getRed())/2, (c.getGreen() + b.getGreen())/2, (c.getBlue() + b.getBlue())/2);
    }
    
    @Override
    public void messageAdded(ConsoleMessage message) {
        try {
            lastMessage = message;
            logMessage(message);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void messagesCleared() {
// each new page loaded in the browser send this message;
// it is little bit too agressive - I would prefere user to decide
// when they want to clear the log
//        try {
//            getOutputLogger().getOut().reset();
//        } catch (IOException ex) {}
    }

    @Override
    public void messageRepeatCountUpdated(int count) {
        try {
            logMessage(lastMessage);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
    public static String getCurrentTime() {
        return formatter.format(new Date(System.currentTimeMillis()));
    }
    
    private void logMessage(ConsoleMessage msg) throws IOException {
        String level = msg.getLevel();
        boolean isErr = LEVEL_ERROR.equals(level);
        String time = getCurrentTime();
        
        String logInfo = createLogInfo(time, level, msg.getSource(), msg.getType());
        OutputWriter ow = isErr ? io.getErr() : io.getOut();
        String lines[] = msg.getText().replace("\r", "").split("\n");
        for (int i = 0; i < lines.length; i++) {
            String singleMessageLine = lines[i];
            if (colorStdBrighter == null && i == lines.length-1) {
                singleMessageLine += logInfo;
            }
            Object res[] = tryToConvertLineToHyperlink(singleMessageLine);
            MyListener l = null;
            String newMessage1 = null;
            String newMessage2 = null;
            if (res != null) {
                l = (MyListener)res[0];
                newMessage1 = (String)res[1];
                newMessage2 = (String)res[2];
            }
            if (l != null && l.isValidHyperlink()) {
                if (colorStdBrighter != null && i == lines.length-1) {
                    newMessage2 += logInfo;
                }
                ow.print(newMessage1);
                ow.println(newMessage2, l);
            } else {
                ow.print(singleMessageLine);
                if (colorStdBrighter != null && i == lines.length-1) {
                    //if (isErr) {
                    //    IOColorPrint.print(io, logInfo, colorErrBrighter);
                    //} else {
                        IOColorPrint.print(io, logInfo, colorStdBrighter);
                    //}
                } else {
                    ow.println("");
                }
            }
        }
        
        boolean doPrintStackTrace = LEVEL_ERROR.equals(level) ||
                                    LEVEL_DEBUG.equals(level);
        
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if (doPrintStackTrace && msg.getStackTrace() != null) {
            for (ConsoleMessage.StackFrame sf : msg.getStackTrace()) {
                String indent;
                if (first) {
                    indent = "    at ";
                    first = false;
                } else {
                    indent = "    at ";
                }
                ow.print(indent);
                ow.print(sf.getFunctionName());
                sb = new StringBuilder();
                
                String urlStr = sf.getURLString();
                urlStr = getProjectPath(urlStr);
                sb.append(" ("+urlStr+":"+sf.getLine()+":"+sf.getColumn()+")");
                MyListener l = new MyListener(sf.getURLString(), sf.getLine(), sf.getColumn());
                if (l.isValidHyperlink()) {
                    ow.println(sb.toString(), l);
                } else {
                    ow.println(sb.toString());
                }
            }
        }
        if (first && msg.getURLString() != null && msg.getURLString().length() > 0) {
            ow.print("  at ");
            String url = msg.getURLString();
            String file = getProjectPath(url);
            sb = new StringBuilder(file);
            int line = msg.getLine();
            if (line != -1 && line != 0) {
                sb.append(":");
                sb.append(line);
            }        
            MyListener l = new MyListener(url, line, -1);
            if (l.isValidHyperlink()) {
                ow.println(sb.toString(), l);
            } else {
                ow.println(sb.toString());
            }
        }
        if (io.isClosed() || isErr) {
            io.select();
        }
    }
    
    // XXX: exact this algorithm is also in 
    // javascript.jstestdriver/src/org/netbeans/modules/javascript/jstestdriver/JSTestDriverSupport.java
    // keep them in sync
    private Object[] tryToConvertLineToHyperlink(String line) {
        // pattern is "at ...... (file:line:column)"
        // file can be also http:// url
        if (!line.endsWith(")")) {
            return null;
        }
        int start = line.lastIndexOf('(');
        if (start == -1) {
            return null;
        }
        int lineNumberEnd = line.lastIndexOf(':');
        if (lineNumberEnd == -1) {
            return null;
        }
        int fileEnd = line.lastIndexOf(':', lineNumberEnd-1);
        if (fileEnd == -1) {
            return null;
        }
        int lineNumber = -1;
        int columnNumber = -1;
        try {
            lineNumber = Integer.parseInt(line.substring(fileEnd+1, lineNumberEnd));
            columnNumber = Integer.parseInt(line.substring(lineNumberEnd+1, line.length()-1));
        } catch (NumberFormatException e) {
            //ignore
        }
        if (columnNumber != -1 && lineNumber == -1) {
            // perhaps stack trace had only line number:
            lineNumber = columnNumber;
        }
        if (lineNumber == -1) {
            return null;
        }
        String file = line.substring(start+1, fileEnd);
        if (file.length() == 0) {
            return null;
        }
        String s1 = line.substring(0, start);
        String s2 = "(" +  // NOI18N
                getProjectPath(file) + 
            line.substring(fileEnd, line.length());
        MyListener l = new MyListener(file, lineNumber, columnNumber);
        return new Object[]{l,s1,s2};
    }
    
    
    private static final String LOG_IGNORED = "log";    // NOI18N
    private static final String CONSOLE_API = "console-api";    // NOI18N
    private static final String TIME_SEPARATOR = " | "; // NOI18N
    private static String createLogInfo(String time, String level, String source, String type) {
        //String logInfo = " ("+time+" | "+level+","+msg.getSource()+","+msg.getType()+")\n";
        StringBuilder logInfoBuilder = new StringBuilder(" (");
        logInfoBuilder.append(time);
        boolean separator = false;
        if (!LOG_IGNORED.equals(level)) {
            separator = true;
            logInfoBuilder.append(TIME_SEPARATOR);
            logInfoBuilder.append(level);
        }
        if (!CONSOLE_API.equals(source)) {
            if (separator) {
                logInfoBuilder.append(", ");
            } else {
                logInfoBuilder.append(TIME_SEPARATOR);
            }
            logInfoBuilder.append(source);
        }
        if (!LOG_IGNORED.equals(type)) {
            if (separator) {
                logInfoBuilder.append(", ");
            } else {
                logInfoBuilder.append(TIME_SEPARATOR);
            }
            logInfoBuilder.append(type);
        }
        logInfoBuilder.append(")\n");
        return logInfoBuilder.toString();
    }
    
    /**
     * Try to find a more readable project-relative path.<p>
     * E.g.: "http://localhost:89/SimpleLiveHTMLTest/js/app.js:8:9"
     * is turned into: "js/app.js:8:9"
     * @param urlStr The URL
     * @return a project-relative path, or the original URL.
     */
    private String getProjectPath(String urlStr) {
        try {
            URL url = new URL(urlStr);
            Project project = pc.getProject();
            if (project != null) {
                FileObject fo = ServerURLMapping.fromServer(project, url);
                if (fo != null) {
                    String relPath = FileUtil.getRelativePath(project.getProjectDirectory(), fo);
                    if (relPath != null) {
                        urlStr = relPath;
                    }
                }
            }
        } catch (MalformedURLException murl) {}
        return urlStr;
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
            Line l = getLine();
            if (l != null) {
                l.show(Line.ShowOpenType.OPEN, 
                    Line.ShowVisibilityType.FOCUS, column != -1 ? column -1 : -1);
            }
        }
        private Line getLine() {
            Project project = pc.getProject();
            return MiscEditorUtil.getLine(project, url, line-1);
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
        
        public boolean isValidHyperlink() {
            return getLine() != null;
        }
    
    }
    
}
