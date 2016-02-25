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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.repl.REPL;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class NBRepl implements REPL {

    private static final SpecificationVersion MIN_SUPPORTED = new SpecificationVersion("1.8");
    private static final SpecificationVersion HAS_REPL = new SpecificationVersion("1.9");
    private /*XXX*/OutputStream commandOut;
    private /*XXX*/Process process;
    private final Map<String, Processor> commands = new HashMap<>();
    private final URL[] roots;
    private ShellSession session;
    private PrintStream userOut;
    private PrintStream userError;
    
    public NBRepl(JavaPlatform platform, final PrintWriter output, 
            PrintStream userOut, 
            PrintStream userError, 
            URL[] roots) throws IOException {
        this.roots = roots;
        if (!isSupported(platform)) {
            if (isSupported(JavaPlatformManager.getDefault().getDefaultPlatform())) {
                output.append("JDK \"" + platform.getDisplayName() + "\" is not supported for REPL, using the default platform instead.\n");
                platform = JavaPlatformManager.getDefault().getDefaultPlatform();
            } else {
                boolean found = false;
                for (JavaPlatform other : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                    if (isSupported(other)) {
                        output.append("JDK \"" + platform.getDisplayName() + "\" is not supported for REPL, using " + other.getDisplayName() + " instead.\n");
                        platform = other;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    output.append("JDK \"" + platform.getDisplayName() + "\" is not supported for REPL, no supported JDK found, please install JDK8 or newer.\n");
                    return ;
                }
            }
        }
        
        this.userError = userError;
        this.userOut = userOut;

        if (platform.getSpecification().getVersion().compareTo(HAS_REPL) >= 0) {
//            if (run(platform, output, REMOTE_REPL_KIND.TOOL))
//                return ;
//
//            output.append("Debug: cannot use REPL in the platform, using REPL shipped with the IDE!");

            if (run(platform, output, REMOTE_REPL_KIND.REPL_TOOL))
                return ;

            output.append("Debug: REPL shipped with JDK not compatible with the platform, using fallback!");
        }

        if (!run(platform, output, REMOTE_REPL_KIND.LANGTOOLS_REPL_TOOL)) {
            output.append("Cannot start REPL, see IDE log for more details.\n");
        }
    }

    private boolean run(JavaPlatform platform, final PrintWriter output, REMOTE_REPL_KIND remoteReplKind) throws UnknownHostException, IOException {
        final CountDownLatch finished = new CountDownLatch(1);

        commands.put("done", new Processor() {
            @Override public void handle(String line) {
                finished.countDown();
            }
        });
        commands.put("output", new Processor() {
            @Override public void handle(String data) {
                StringBuilder content = new StringBuilder();
                for (int i = 0; i < data.length(); i += 2) {
                    content.append((char) Integer.parseInt(data.substring(i, i + 2), 16));
                }
                output.append(content);
            }
        });

        ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLocalHost()); //XXX: local host?
        File remoteProbeJar = InstalledFileLocator.getDefault().locate("modules/lib/nbrepl-probe.jar", "org.netbeans.modules.java.repl", false);
        File replJar = InstalledFileLocator.getDefault().locate("modules/ext/" + remoteReplKind.fileName, "org.netbeans.modules.jdk.jshell", false);
        File toolsJar = null;

        for (FileObject jdkInstallDir : platform.getInstallFolders()) {
            FileObject toolsJarFO = jdkInstallDir.getFileObject("lib/tools.jar");

            if (toolsJarFO == null) {
                toolsJarFO = jdkInstallDir.getFileObject("../lib/tools.jar");
            }
            if (toolsJarFO != null) {
                toolsJar = FileUtil.toFile(toolsJarFO);
            }
        }
        File[] urlFiles = new File[roots == null ? 0 : roots.length];
        int index = 0;
        for (URL u : roots) {
            File f = FileUtil.archiveOrDirForURL(u);
            // possibly null, but will be handled later
            urlFiles[index++] = f;
        }

        String cp = addClassPath(
                toolsJar != null ? toClassPath(remoteProbeJar, replJar, toolsJar) : 
                                   toClassPath(remoteProbeJar, replJar),
                urlFiles) + System.getProperty("path.separator") + " "; // NOI18N avoid REPL bug
        FileObject javaExecutableFO = platform.findTool("java");
        File javaExecutable = FileUtil.toFile(javaExecutableFO);
        ProcessBuilder pb = new ProcessBuilder(javaExecutable.getAbsolutePath(), "-classpath", cp, "org.netbeans.modules.java.repl.tool.RemoteReplProbe", String.valueOf(serverSocket.getLocalPort()));

        pb.inheritIO();

        this.process = pb.start();

        PipedOutputStream cOut = new PipedOutputStream();
        final PipedInputStream cIn = new PipedInputStream();
        cOut.connect(cIn);

        this.commandOut = cOut;

        PipedOutputStream replOutput = new PipedOutputStream();
        final PipedInputStream replOutput2 = new PipedInputStream();
        replOutput.connect(replOutput2);
                
        launcher = JShellLauncher.createLauncher(cIn, replOutput, userOut, userError);
        launcher.setClasspath(cp);
        
        new Thread() {
            @Override
            public void run() {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(replOutput2));
                    String line;

                    while ((line = in.readLine()) != null) {
                        int colon = line.indexOf(':');
                        String command = line.substring(0, colon);
                        String data = line.substring(colon + 1).replace("\\n", "\n").replace("\\\\", "\\");
                        Processor proc = commands.get(command);
                        if (proc != null) {
                            proc.handle(data);
                        }
                    }
                    System.err.println("Terminating Receiver");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }.start();
        
        new Thread(){
           public void run() {
               try {
                   launcher.run();
                   System.err.println("Terminating Shell");
               } catch (IOException ex) {
                   Exceptions.printStackTrace(ex);
               }
           } 
        }.start();
        
        return process.isAlive();
    }
    
    static RequestProcessor  RP = new RequestProcessor(NBRepl.class);
    
    private JShellLauncher launcher;

    private static enum REMOTE_REPL_KIND {
        LANGTOOLS_REPL_TOOL("jshell.jar"),
        REPL_TOOL("nbrepl-repl-tool.jar"),
        TOOL("nbrepl-tool.jar");

        private final String fileName;

        private REMOTE_REPL_KIND(String fileName) {
            this.fileName = fileName;
        }

    }

    private static boolean isSupported(JavaPlatform platform) {
        return platform.getSpecification().getVersion().compareTo(MIN_SUPPORTED) >= 0;
    }
    
    private static String addClassPath(String prefix, File... files) {
        String suffix = toClassPath(files);
        if (prefix != null && !prefix.isEmpty()) {
            return prefix + System.getProperty("path.separator") + suffix;
        }
        return suffix;
    }

    private static String toClassPath(File... files) {
        String sep = "";
        StringBuilder cp = new StringBuilder();

        for (File f : files) {
            if (f == null) continue;
            cp.append(sep);
            cp.append(f.getAbsolutePath());
            sep = System.getProperty("path.separator");
        }

        return cp.toString();
    }

    @Override
    public void evaluate(String command) throws IOException {
        final CountDownLatch finished = new CountDownLatch(1);

        commands.put("done", new Processor() {
            @Override public void handle(String line) {
                finished.countDown();
            }
        });
        commandOut.write(("execute:" + command.replace("\\", "\\\\").replace("\n", "\\n") + "\n").getBytes("UTF-8"));
        try {
            finished.await();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            commands.remove("done");
        }
    }

    @Override
    public void close() throws Exception {
        commandOut.write("exit:\n".getBytes("UTF-8"));
        process.waitFor();
    }

    @Override
    public List<? extends CompletionItemImpl> completions(String code, final int commandStart) {
        final List<CompletionItemImpl> completions = new ArrayList<>();
        final CountDownLatch finished = new CountDownLatch(1);
        
        commands.put("completion-done", new Processor() {
            @Override public void handle(String line) {
                finished.countDown();
            }
        });
        commands.put("completion", new Processor() {
            @Override public void handle(String line) {
                List<String> elements = new ArrayList<>(Arrays.asList(line.split("\n")));
                completions.add(new CompletionItemImpl(elements.get(0), Integer.parseInt(elements.get(1)) + commandStart));
            }
        });
        try {
            commandOut.write(("completion:" + code.replace("\\", "\\\\").replace("\n", "\\n") + "\n").getBytes("UTF-8"));
            finished.await();
            return completions;
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        } finally {
            commands.remove("completion-done");
            commands.remove("completion");
        }
    }

    interface Processor {
        public void handle(String data);
    }

    public static final class CompletionItemImpl implements CompletionItem {

        private final String completion;
        private final int anchor;

        public CompletionItemImpl(String completion, int anchor) {
            this.completion = completion;
            this.anchor = anchor;
        }

        public String getCompletion() {
            return completion;
        }

        @Override
        public void defaultAction(final JTextComponent component) {
            Completion.get().hideCompletion();
            Completion.get().hideDocumentation();
            try {
                final Document doc = (Document) component.getDocument();
                Runnable apply = new Runnable() {
                    @Override public void run() {
                        try {
                            doc.remove(anchor, component.getCaretPosition() - anchor);
                            doc.insertString(anchor, completion, null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                };
                if (doc instanceof StyledDocument) {
                    NbDocument.runAtomicAsUser((StyledDocument) doc, apply);
                } else {
                    apply.run();
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(completion, "", g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(null, completion, "", g, defaultFont, defaultColor, width, height, selected);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return null;
        }

        @Override
        public CompletionTask createToolTipTask() {
            return null;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            defaultAction(component);
            return true;
        }

        @Override
        public int getSortPriority() {
            return 0;
        }

        @Override
        public CharSequence getSortText() {
            return completion;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return completion;
        }

    }

    @ServiceProvider(service=Factory.class, position=100)
    public static class FactoryImpl implements Factory {

        @Override
        public REPL createREPL(JavaPlatform platform, PrintWriter output, 
                PrintStream userOut, PrintStream userErr, URL[] roots) {
            try {
                return new NBRepl(platform, output, userOut, userErr, roots);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

    }
}
