/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.support;

import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.ConsoleListener;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.ConsoleEvent;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import jdk.jshell.JShell;
import jdk.jshell.JShellAccessor;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.execution.ExecutorTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.model.ConsoleModel.SnippetHandle;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * The root object for any JShell session. A shell session consists of:
 * <ul>
 * <li>a Document, viewed by an editor
 * <li>an output window, where the JShell prints the results
 * <li>the JShell instance itself
 * <li>a process handle to the running target VM
 * <li>transient FileSystem where the sources for parsing are created
 * <li>ConsoleModel which divides the Document and maps JShell snippets to
 * document parts.
 * </ul>
 * <p/>
 * <b>Threading model:</b> The JShell executes single-threaded, so <b>all accesses</b>
 * to JShell must be serialized using {@link #post(java.lang.Runnable)}. The getter
 * for JShell asserts when accessed from outside such Runnable - do not abuse.
 * <p/>
 *
 * @author sdedic
 */
public class ShellSession {

    public static final String PROP_ACTIVE = "active";

    private final Document  consoleDocument;
    
    /**
     * The java platform and projectInfo may possibly change.
     */
    private final JavaPlatform    platform;
    private final ClasspathInfo   projectInfo;
    private final FileObject      workRoot;

    private ClasspathInfo cpInfo;
    private JShell shell;
    private ExecutorTask shellProcess;
    private InputOutput io;
    private ConsoleModel    model;
    
    /**
     * The shell output stream, possibly null.
     * Will be initialized during startup
     */
    private PrintStream shellControlOutput;
    
    private InputStream shellControlInput;
    private OutputStream controlCommandStream;
    private String  displayName;
    private volatile boolean closed;
    private FileObject consoleFile;
    private JShellEnvironment env;

    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    
    public ShellSession(JShellEnvironment env) {
        this(env.getDisplayName(), 
                       env.getConsoleDocument(), 
                       env.getClasspathInfo(),
                       env.getPlatform(),
                       env.getWorkRoot(), env.getConsoleFile());
        this.env = env;
    }

    private ShellSession(String displayName, Document doc, ClasspathInfo cpInfo, 
            JavaPlatform platform, FileObject workRoot, FileObject consoleFile) {
        this.consoleDocument = doc;
        this.projectInfo = cpInfo;
        this.displayName = displayName;
        this.platform = platform;
        this.consoleFile = consoleFile;
        this.workRoot = workRoot;
    }
    
    public boolean isActive() {
        return !closed;
    }

    public OutputStream getControlCommandStream() {
        return controlCommandStream;
    }

    public void close() throws Exception {
        closeSession();
    }
    
    private Task detach() {
        synchronized (this) {
            synchronized (allSessions) {
                Reference<ShellSession> refS = allSessions.get(consoleDocument);
                if (refS.get() == this) {
                    allSessions.remove(consoleDocument);
                } else {
                    return Task.EMPTY;
                }
            }
            shell = null;
            closed = true;
        }
        try {
            refreshGuardedSection();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        // leave the model
        model.detach();
        return sendJShellClose();
    }

    JShell getJShell() {
        assert evaluator.isRequestProcessorThread();
        if (shell == null) {
            initJShell();
        }
        return launcher.getJShell();
    }
    
    public FileObject getConsoleFile() {
        return consoleFile;
    }
    
    public void setShellCountrolOutput(PrintStream stm) {
        this.shellControlOutput = stm;
    }

    public PrintStream setShellCountrolOutput(Writer stm) {
        return this.shellControlOutput = new PrintStream(
            new WriterOutputStream(stm)
        );
    }

    private class OuterWriterFilter extends FilterWriter {
        public OuterWriterFilter(Writer out) {
            super(out);
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            super.write(cbuf, off, len); 
            
        }
    }
    
    public void reload() {
        
    }
    
    /**
     * Writer implementation over the document. Informs the ConsoleModel,
     * that an event worth parsing is coming.
     */
    private class DocumentOutput extends Writer {
        private Throwable exception;
        
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (!isActive()) {
                // do not write from a closed JShell
                return;
            }
            AtomicLockDocument ald = LineDocumentUtils.asRequired(consoleDocument, AtomicLockDocument.class);
            try {
                ald.runAtomic(()-> {
                    try {
                        int offset = consoleDocument.getLength();
                        model.getProtectionBypass().insertString(offset,
                                String.copyValueOf(cbuf, off, len), null);
//                    TokenHierarchy h = TokenHierarchy.get(consoleDocument);
//                    TokenSequence seq = h.tokenSequence();
//                    System.err.println(seq);
                    } catch (BadLocationException ex) {
                        exception = ex;
                    }
                });
                // juts for fun:
//                TokenHierarchy h = TokenHierarchy.get(consoleDocument);
//                TokenSequence seq = h.tokenSequence();
//                Token t;
//                System.err.println("Token stream:");
//                while (seq.moveNext()) {
//                    t = seq.token();
//                    System.err.println(t.id() + ": " + t.text() + " - " + t.length());
//                }
//                System.err.println(seq);
                
                if (exception != null) {
                    throw new IOException(exception);
                }
            } finally {
                exception = null;
            }
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            // FIXME: the JShell session should be terminated.
        }
    }
    
    private void initStreams() {
        shellControlOutput = new PrintStream(
            new WriterOutputStream(
                    // delegate to whatever Writer will be set
                    new Writer() {
                        @Override
                        public void write(char[] cbuf, int off, int len) throws IOException {
                            writer.write(cbuf, off, len);
                        }

                        @Override
                        public void flush() throws IOException {
                            writer.flush();
                        }

                        @Override
                        public void close() throws IOException {
                            writer.close();
                        }

                    })
        );
        if (shellControlInput == null) {
            PipedInputStream istm = new PipedInputStream();
            PipedOutputStream ostm;
            try {
                ostm = new PipedOutputStream(istm);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
            shellControlInput = istm;
            controlCommandStream = ostm;
        }
    }
    
    private Map<Snippet, Document>  snippetSources = new HashMap<>();
    
    public FileObject   snippetFile(SnippetHandle snippet, boolean editedSnippet) {
        if (launcher == null) {
            return null;
        }
        String resName = snippetFileName(snippet, editedSnippet);
        FileObject fob = workRoot.getFileObject(resName);
        if (fob != null && fob.isValid()) {
            return fob;
        }
        return createSnippetFile(snippet, resName);
    }
    
    private String snippetFileName(SnippetHandle snippet, boolean editedSnippet) {
        // this is the snippet being just edited.
        boolean editable = editedSnippet || snippet.getStatus() == Status.NONEXISTENT;
        return (editable ? "$$REPLEDIT.java" : JShellAccessor.snippetClass(snippet.getSnippet())) + ".java"; // hardcoded constant    }
    }
    
    private FileObject createSnippetFile(SnippetHandle info, String resName) {
        FileObject pkg;
        try {
            pkg = FileUtil.createFolder(workRoot, "REPL");
        } catch (IOException ex) {
            return null;
        }
        String fn = resName != null ? resName : snippetFileName(info, false); 
        String contents = info.getWrappedCode();

        if (contents == null) {
            return null;
        }
        while (true) {
            FileObject fob = pkg.getFileObject(fn);
            if (fob != null) {
                try {
                    fob.delete();
                } catch (IOException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
            }
            try (OutputStream ostm = pkg.createAndOpen(fn)) {
                try (OutputStreamWriter ows = new OutputStreamWriter(ostm, "UTF-8")) {
                    ows.append(contents);
                    ows.flush();
                }
                return pkg.getFileObject(fn);
            } catch (IOException ex) {
                // ???
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Stream backed by a Writer. Uses UTF-8 to decode characters from the stream.
     */
    private static class WriterOutputStream extends OutputStream {
        private boolean writeImmediately = true;
        
        private final CharsetDecoder decoder;
        private final ByteBuffer decoderIn = ByteBuffer.allocate(128);
        private final CharBuffer decoderOut;
        private final Writer writer;
        
        public WriterOutputStream(Writer out) {
            this.writer = out;
            this.decoder = Charset.forName("UTF-8").
                    newDecoder().
                    onMalformedInput(CodingErrorAction.REPLACE).
                    onUnmappableCharacter(CodingErrorAction.REPLACE).
                    replaceWith("?");
            this.decoderOut = CharBuffer.allocate(2048);
        }
        
        @Override
        public void write(int b) throws IOException {
            decoderIn.put((byte)b);
            processInput(false);
            if (writeImmediately) {
                flushOutput();
            }
        }
        
        public void write(final byte[] b, int off, int len) throws IOException {
            while (len > 0) {
                final int c = Math.min(len, decoderIn.remaining());
                decoderIn.put(b, off, c);
                processInput(false);
                len -= c;
                off += c;
            }
            if (writeImmediately) {
                flushOutput();
            }
        }
        
        private void flushOutput() throws IOException {
            if (decoderOut.position() > 0) {
                writer.write(decoderOut.array(), 0, decoderOut.position());
                decoderOut.rewind();
            }
        }

        @Override
        public void close() throws IOException {
            processInput(true);
            flushOutput();
            writer.close();
        }

        @Override
        public void flush() throws IOException {
            flushOutput();
            writer.flush();
        }        
        
        private void processInput(final boolean endOfInput) throws IOException {
            // Prepare decoderIn for reading
            decoderIn.flip();
            CoderResult coderResult;
            while (true) {
                coderResult = decoder.decode(decoderIn, decoderOut, endOfInput);
                if (coderResult.isOverflow()) {
                    flushOutput();
                } else if (coderResult.isUnderflow()) {
                    break;
                } else {
                    // The decoder is configured to replace malformed input and unmappable characters,
                    // so we should not get here.
                    throw new IOException("Unexpected coder result");
                }
            }
            // Discard the bytes that have been read
            decoderIn.compact();
        }
    }

    private volatile JShellLauncher launcher;
    
    private ExecutorTask    shellTask;
    
    public ExecutorTask getShellTask() {
        return shellTask;
    }
    
    public Task start() {
        ShellSession previous  = null;
        
        synchronized (allSessions) {
            Reference<ShellSession> sr = allSessions.get(env.getConsoleDocument());
            ShellSession s = null;
            
            if (sr != null) {
                previous = sr.get();
            }
        }
        if (previous != null) {
            previous.detach();
            AtomicLockDocument ald = LineDocumentUtils.asRequired(consoleDocument, AtomicLockDocument.class);
            ald.runAtomic(() -> {
                try {
                    consoleDocument.remove(0, consoleDocument.getLength());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
        init(previous);
        try {
            refreshGuardedSection();
        } catch (BadLocationException ex) {
        }
        synchronized (allSessions) {
            allSessions.put(consoleDocument, new WeakReference<>(this));
        }
        return evaluator.post(() -> {
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] { 
                env.getSnippetClassPath()
            });

            URL url = URLMapper.findURL(workRoot, URLMapper.INTERNAL);
            IndexingManager.getDefault().refreshIndexAndWait(url, null, true);
            boolean hasIndex = org.netbeans.modules.java.source.indexing.JavaIndex.hasSourceCache(url,true);
            
            getJShell();
            ModelAccessor.INSTANCE.beforeExecution(model);
            if (isActive()) {
                launcher.start();
            }
        });
    }
    
    public JShellLauncher getLauncher() {
        initJShell();
        return launcher;
    }
    
    private Writer writer = new DocumentOutput();

    public Writer getWriter() {
        return writer;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    private void initJShell() {
        if (shell != null) {
            return;
        }
        JShell shell;
        synchronized (this) {
            if (launcher == null) {
                initStreams();
                launcher = new JShellLauncher(
                    shellControlOutput,
                    shellControlOutput, 
                    new ByteArrayInputStream(new byte[0]),
                    new PrintStream(
                        new WriterOutputStream(io.getOut())
                    ),
                    new PrintStream(
                        new WriterOutputStream(io.getErr())
                    )
                );
                launcher.setClasspath(createClasspathString());
            }
        }
        shell = launcher.getJShell();
        synchronized (this) {
            if (launcher != null) {
                shell.onShutdown(sh -> closed());
                model.attach(shell);
                // must first give chance to the model to map the snippet to console contents
                model.forwardSnippetEvent(this::acceptSnippet);
            }
        }
    }
    
    private boolean erroneous;
    
    private void acceptSnippet(SnippetEvent e) {
        if (launcher == null) {
            return;
        }
        switch (e.status()) {
            case REJECTED:
                erroneous = true;
            case VALID:
            case RECOVERABLE_DEFINED:
            case RECOVERABLE_NOT_DEFINED:
            case NONEXISTENT:
                createSnippetFile(
                        model.getInfo(e.snippet()), 
                        null
                );
        }
    }
    
    private String createClasspathString() {
        File remoteProbeJar = InstalledFileLocator.getDefault().locate(
                "modules/ext/nb-jshell-probe.jar", "org.netbeans.libs.jshell", false);
        File replJar = InstalledFileLocator.getDefault().locate("modules/ext/nb-jshell.jar", "org.netbeans.libs.jshell", false);
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
        ClassPath compilePath = cpInfo.getClassPath(PathKind.COMPILE);
        
        FileObject[] roots = compilePath.getRoots();
        File[] urlFiles = new File[roots.length];
        int index = 0;
        for (FileObject fo : roots) {
            File f = FileUtil.toFile(fo);
            if (f != null) {
                urlFiles[index++] = f;
            }
        }
        String cp = addClassPath(
                toolsJar != null ? toClassPath(remoteProbeJar, replJar, toolsJar) : 
                                   toClassPath(remoteProbeJar, replJar),
                urlFiles) + System.getProperty("path.separator") + " "; // NOI18N avoid REPL bug
        
        return cp;
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

    private void closed() {
        this.closed = true;
        propSupport.firePropertyChange(PROP_ACTIVE, true, false);
        
    }

    private synchronized void init(ShellSession prev) {
        if (prev != null) {
            io = prev.getIO();
        } else {
            io = InputOutput.get(displayName, false);
            
        }
        evaluator = new RequestProcessor("Evaluator for " + displayName);
        initClasspath();
        model = ConsoleModel.create(consoleDocument, null, evaluator);
        model.addConsoleListener(new LexerEmbeddingAdapter());
        model.addConsoleListener(new GuardedSectionUpdater());

        // missing API to create a GuardedSectionManager against a plain document:
        AbstractGuardedSectionsProvider hack = new AbstractGuardedSectionsProvider(
                new GuardedEditorSupport() {
            @Override
                    public StyledDocument getDocument() {
                        return (StyledDocument)consoleDocument;
                    }
                }
            ) {
            
            @Override
            public char[] writeSections(List<GuardedSection> sections, char[] content) {
                return null;
            }
            
            @Override
            public AbstractGuardedSectionsProvider.Result readSections(char[] content) {
                return null;
            }
        };
        // this crates and registers a GuardedSectionManager in the doc properties
        hack.createGuardedReader(new ByteArrayInputStream(new byte[0]), Charset.defaultCharset());
        gsm = GuardedSectionManager.getInstance((StyledDocument)consoleDocument);
        gsProvider = hack;
        
    }
    private AbstractGuardedSectionsProvider gsProvider;
    private GuardedSectionManager gsm;

    private void initClasspath() {
        ClassPath snippetSource = ClassPathSupport.createProxyClassPath(
                projectInfo.getClassPath(PathKind.SOURCE),
                ClassPathSupport.createClassPath(workRoot)
        );

        this.cpInfo = ClasspathInfo.create(
                projectInfo.getClassPath(PathKind.BOOT),
                projectInfo.getClassPath(PathKind.COMPILE),
                snippetSource
        );
        
        this.consoleDocument.putProperty("java.classpathInfo", this.cpInfo);
    }

    public Document getConsoleDocument() {
        return consoleDocument;
    }

    public ClasspathInfo getClasspathInfo() {
        return cpInfo;
    }

    public JShell getShell() {
        initJShell();
        return shell;
    }

    public ExecutorTask getShellProcess() {
        return shellProcess;
    }

    public InputOutput getIO() {
        return io;
    }

    public static ShellSession createSession(
            JShellEnvironment env) {
        return new ShellSession(env);
    }

    public static ShellSession get(Document d) {
        if (d == null) {
            return null;
        }
        synchronized (allSessions) {
            Reference<ShellSession> sr = allSessions.get(d);
            return sr == null ? null : sr.get();
        }
    }

    public static Collection<ShellSession> allSessions() {
        Collection<Reference<ShellSession>> ll;
        synchronized (allSessions) {
            ll = allSessions.values();
        }
        Collection<ShellSession> res = new ArrayList<>(ll.size());
        for (Iterator<Reference<ShellSession>> it = ll.iterator(); it.hasNext(); ) {
            Reference<ShellSession> sr = it.next();
            ShellSession s = sr.get();
            if (s != null) {
                res.add(s);
            }
        }
        return res;
    }
    
    private void addNewline(int offset) {
        AtomicLockDocument ald = LineDocumentUtils.asRequired(consoleDocument, AtomicLockDocument.class);
        ald.runAtomic(() -> {
            try {
                DocumentUtilities.setTypingModification(consoleDocument, false);
                consoleDocument.insertString(offset, "\n", null);
            } catch (BadLocationException ex) {
                
            }
        });
    }
    
    public void evaluate(String command) throws IOException {
        if (!isActive()) {
            return;
        }
        post(() -> {
            String c = command;
            if (c == null) {
                ConsoleSection s = model.processInputSection();
                if (s == null) {
                    return;
                }
                c = s.getContents(consoleDocument);
                if (!c.endsWith("\n")) { // NOI18N
                    addNewline(s.getEnd());
                }
            }
            doExecuteCommands();
        });
    }

    /**
     * Executes commands in input buffer. Executes one by one, since some
     * snippets may be redundant and will not be reported at all by JShell, so
     * replacement of individual Snippets will be assisted by setting up a
     * start position of the to-be-executed snippet in the buffer.
     * <p/>
     * Since JShell stops executin after first error, a 'erroneous' flag is raised
     * by {@link #acceptSnippet} when it sees a REJECTED snippet (an error).
     */
    private void doExecuteCommands() {
        ConsoleSection sec = model.processInputSection();
        if (sec == null) {
            return;
        }

        // rely on JShell's own parsing from the input section
        ModelAccessor.INSTANCE.beforeExecution(model);
        // just for case:
        ConsoleSection exec = model.getExecutingSection();
        erroneous = false;
        try {
            final List<String> toExec = new ArrayList<>();
            if (exec.getType() == ConsoleSection.Type.COMMAND) {
                // execute entire section
                consoleDocument.render(() -> {
                    toExec.add(exec.getContents(consoleDocument));
                });
            } else {
                consoleDocument.render(() -> {
                    for (Rng r : exec.getAllSnippetBounds()) {
                        toExec.add(exec.getRangeContents(consoleDocument, r));
                    }
                });
            }
            Rng[] ranges = exec.getAllSnippetBounds();
            int index = 0;
            for (String s : toExec) {
                try {
                    ModelAccessor.INSTANCE.setSnippetOffset(model, exec.offsetToContents(ranges[index].start, true));
                    launcher.evaluate(s);
                    if (erroneous) {
                        break;
                    }
                } catch (IOException ex) {
                    // FIXME: report exception _before_ the input area, so
                    // it becomes a part of the command's output
                    Exceptions.printStackTrace(ex);
                }
                index++;
            }
        } finally {
            erroneous = false;
            ModelAccessor.INSTANCE.afterExecution(model);
        }
    }
    
    private Task sendJShellClose() {
        return evaluator.post(() -> {
            if (launcher != null) {
                launcher.closeState();
            }
            closed();
        });
    }
    
    public Task closeSession() throws IOException {
        io.getErr().close();
        io.getOut().close();
        io.getIn().close();
        return sendJShellClose();
    }
    
    interface Processor {
        public void handle(String data);
    }
    
    public ConsoleModel getModel() {
        return model;
    }
    
    private static class HR<T> extends WeakReference<T> {
        private T keepRef;
        
        public HR(T referent) {
            super(referent);
            this.keepRef = referent;
        }
    }

    private void refreshGuardedSection() throws BadLocationException {
        gsm.getGuardedSections().forEach((GuardedSection gs) -> gs.removeSection());
        if (!isActive()) {
            return;
        }
        ConsoleSection s = model.getInputSection();
        LineDocument ld = LineDocumentUtils.asRequired(consoleDocument, LineDocument.class);
        if (s == null) {
            // protected including the final newline, so an insertion at the end will
            // expand the guarded block automatically
            int l = consoleDocument.getLength() + 1;       
            gsm.protectSimpleRegion(ld.createPosition(0, Position.Bias.Forward),
                    ld.createPosition(l, Position.Bias.Forward),
                    "scrollback"); // NOI18N
        } else {
            int wr = s.getPartBegin() - 1;
            gsm.protectSimpleRegion(ld.createPosition(0, Position.Bias.Forward),
                    ld.createPosition(wr, Position.Bias.Backward),
                    "scrollback"); // NOI18N
        }
    }

    private static final Map<Document, Reference<ShellSession>> allSessions = new WeakHashMap<>();
    
    private class GuardedSectionUpdater implements ConsoleListener {
        @Override
        public void sectionCreated(ConsoleEvent e) {
            List<ConsoleSection> aff = e.getAffectedSections();
            for (ConsoleSection s : aff) {
                // if an input section has been created, the document BEFORE the section
                // should become guarded
                if (s == model.getLastInputSection()) {
                    // redefine the guarded block, if any, to span from the 
                    // start to the prompt end.
                    refresh();
                }
            }
        }
        
        private void refresh() {
            try {
                refreshGuardedSection();
            } catch (BadLocationException ex) {
                //
            }
        }
        
        @Override
        public void sectionUpdated(ConsoleEvent e) {
            for (ConsoleSection s : e.getAffectedSections()) {
                if (s == model.getLastInputSection()) {
                    refresh();
                    break;
                }
            }
        }
    }
    
    /**
     * All accesses to the shell must go through the request processor.
     */
    private RequestProcessor evaluator;
    
    public Task    post(Runnable r) {
        return evaluator.post(r);
    }
}
