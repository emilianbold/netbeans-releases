/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;
import jdk.jshell.JShell;

/**
 *
 * @author lahvac
 */
public class JShellLauncher extends InternalJShell implements Supplier<String> {

    private String prefix = "";

    public JShellLauncher(PrintStream cmdout, PrintStream cmderr, InputStream userin, PrintStream userout, PrintStream usererr) {
        super(cmdout, cmderr, userin, userout, usererr);
    }

    protected String prompt(boolean continuation) {
        int index = state.snippets().size() + 1;
        if (continuation) {
            return ">> ";
        } else if (feedback() == Feedback.Concise) {
            return "[" + index + "] -> ";
        } else {
            return "\n[" + index + "] -> ";
        }
    }

    public void start() {
        fluff("Welcome to the Java REPL NetBeans integration");
        fluff("Type /help for help");
        ensureLive();
        cmdout.append(prompt(false));
    }
    
    public void stop() {
        closeState();
    }
    
    public void evaluate(String command) throws IOException {
        ensureLive();
        String trimmed = trimEnd(command);
        if (!trimmed.isEmpty()) {
            prefix = process(prefix, command);
        }
        cmdout.append(prompt(!prefix.isEmpty()));
    }
    
    public List<String> completion(String command) {
        return completions(prefix, command);
    }

    private void ensureLive() {
        if (!live) {
            resetState();
            live = true;
        }
    }

    public JShell getJShell() {
        ensureLive();
        return state;
    }

    @Override
    protected JShell.Builder createJShell() {
        return super.createJShell().javaVMOptionsProvider(this);
    }

    private String classpath;
    private OutputStream socketOut;
    private InputStream socketIn;


    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    @Override
    public String get() {
        return "-classpath " + classpath;
    }
    
    public static JShellLauncher createLauncher(InputStream in, OutputStream out,
            PrintStream userOutput, PrintStream userError) throws IOException {

        PrintStream wrappedOut = wrapOutputStream(out);
        PrintStream userPrintOut = userOutput == null ?
                wrappedOut : userOutput;
        PrintStream userPrintErr = userError == null ?
                wrappedOut : userError;
        JShellLauncher l = new JShellLauncher(
                wrappedOut,
                wrappedOut,
                new ByteArrayInputStream(new byte[0]),
                userPrintOut,
                userPrintErr);
        l.socketOut = out;
        l.socketIn = in;
        return l;
    }

    public static PrintStream wrapOutputStream(OutputStream out) {
        return new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                synchronized (out) {
                    out.write(("output:" + String.format("%02X", b) + "\n").getBytes("UTF-8"));
                }
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                synchronized (out) {
                    out.write("output:".getBytes("UTF-8"));
                    for (int i = 0; i < len; i++) {
                        out.write(String.format("%02X", b[off + i]).getBytes("UTF-8"));
                    }
                    out.write("\n".getBytes("UTF-8"));
                }
            }
        });
    }

    /*
    public void run() throws IOException {
        fluff("Welcome to the Java REPL NetBeans integration");
        fluff("Type /help for help");
        ensureLive();
        cmdout.append(prompt(false));

        synchronized (socketOut) {
            socketOut.write("done:\n".getBytes("UTF-8"));
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(socketIn));

        String line;

        try {
            while ((line = in.readLine()) != null) {
                int colon = line.indexOf(':');
                String command = line.substring(0, colon);
                String data = line.substring(colon + 1).replace("\\n", "\n").replace("\\\\", "\\");

                switch (command) {
                    case "execute":
                        try {
                            evaluate(data);
                        } finally {
                            synchronized (socketOut) {
                                socketOut.write("done:\n".getBytes("UTF-8"));
                            }
                        }
                        break;
                    case "completion":
                        try {
                            List<String> completions = completions(prefix, data);
                            synchronized (socketOut) {
                                for (String s : completions) {
                                    socketOut.write(("completion:" + s.replace("\\", "\\\\").replace("\n", "\\n") + "\n").getBytes("UTF-8"));
                                }
                            }
                        } finally {
                            synchronized (socketOut) {
                                socketOut.write("completion-done:\n".getBytes("UTF-8"));
                            }
                        }
                        break;
                    case "exit":
                        return;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String... args) throws IOException {
        Socket socket = new Socket(InetAddress.getLocalHost(), Integer.parseInt(args[0]));
        final OutputStream socketOut = socket.getOutputStream();
        PrintStream out = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                synchronized (socketOut) {
                    socketOut.write(("output:" + String.format("%02X", b) + "\n").getBytes("UTF-8"));
                }
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                synchronized (socketOut) {
                    socketOut.write("output:".getBytes("UTF-8"));
                    for (int i = 0; i < len; i++) {
                        socketOut.write(String.format("%02X", b[off + i]).getBytes("UTF-8"));
                    }
                    socketOut.write("\n".getBytes("UTF-8"));
                }
            }
        });
        JShellLauncher repl = new JShellLauncher(out, out, new ByteArrayInputStream(new byte[0]), out, out);

        synchronized (socketOut) {
            socketOut.write("done:\n".getBytes("UTF-8"));
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line;

        while ((line = in.readLine()) != null) {
            int colon = line.indexOf(':');
            String command = line.substring(0, colon);
            String data = line.substring(colon + 1).replace("\\n", "\n").replace("\\\\", "\\");

            switch (command) {
                case "execute":
                    try {
                        repl.evaluate(data);
                    } finally {
                        synchronized (socketOut) {
                            socketOut.write("done:\n".getBytes("UTF-8"));
                        }
                    }
                    break;
                case "completion":
                    try {
                        List<String> completions = repl.completions(repl.prefix, data);
                        synchronized (socketOut) {
                            for (String s : completions) {
                                socketOut.write(("completion:" + s.replace("\\", "\\\\").replace("\n", "\\n") + "\n").getBytes("UTF-8"));
                            }
                        }
                    } finally {
                        synchronized (socketOut) {
                            socketOut.write("completion-done:\n".getBytes("UTF-8"));
                        }
                    }
                    break;
                case "exit":
                    System.exit(0);
                    break;
            }
        }
    }
    */
}
