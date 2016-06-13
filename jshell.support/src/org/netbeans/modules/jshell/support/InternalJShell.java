/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.netbeans.modules.jshell.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

import jdk.internal.jshell.debug.InternalDebugControl;
import jdk.jshell.DeclarationSnippet;
import jdk.jshell.Diag;
import jdk.jshell.EvalException;
import jdk.jshell.ExpressionSnippet;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Subscription;
import jdk.jshell.MethodSnippet;
import jdk.jshell.PersistentSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.Snippet.SubKind;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.SourceCodeAnalysis.Suggestion;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.UnresolvedReferenceException;
import jdk.jshell.VarSnippet;

/**
 *
 * @author Robert Field
 */
public class InternalJShell {

    private static final Pattern LINEBREAK = Pattern.compile("\\R");
    private static final Pattern HISTORY_ALL_FILENAME = Pattern.compile(
            "((?<cmd>(all|history))(\\z|\\p{javaWhitespace}+))?(?<filename>.*)");

    final PrintStream cmdout;
    final PrintStream cmderr;
    final InputStream userin;
    final PrintStream userout;
    final PrintStream usererr;

    public InternalJShell(PrintStream cmdout, PrintStream cmderr,
            InputStream userin, PrintStream userout, PrintStream usererr) {
        this.cmdout = cmdout;
        this.cmderr = cmderr;
        this.userin = userin;
        this.userout = userout;
        this.usererr = usererr;
    }

    protected boolean interactiveOutput = true;
    volatile boolean live = false;

    protected final List<String> history = new ArrayList<>();

    SourceCodeAnalysis analysis;
    JShell state = null;
    Subscription shutdownSubscription = null;

    private boolean debug = false;
    private boolean displayPrompt = true;
    public boolean testPrompt = false;
    private Feedback feedback = Feedback.Default;
    private String cmdlineClasspath = null;
    private String cmdlineStartup = null;
    private String editor = null;

    static final Preferences PREFS = Preferences.userRoot().node("tool/REPL");

    static final String STARTUP_KEY = "STARTUP";

    static final String DEFAULT_STARTUP =
            "\n" +
            "import java.util.*;\n" +
            "import java.io.*;\n" +
            "import java.math.*;\n" +
            "import java.net.*;\n" +
            "import java.util.concurrent.*;\n" +
            "import java.util.prefs.*;\n" +
            "import java.util.regex.*;\n" +
            "void printf(String format, Object... args) { System.out.printf(format, args); }\n";

    // Tool id (tid) mapping
    NameSpace mainNamespace;
    NameSpace startNamespace;
    NameSpace errorNamespace;
    NameSpace currentNameSpace;
    Map<Snippet,SnippetInfo> mapSnippet;

    void debug(String format, Object... args) {
        if (debug) {
            cmderr.printf(format + "\n", args);
        }
    }

    void fluff(String format, Object... args) {
        if (feedback() != Feedback.Off && feedback() != Feedback.Concise) {
            hard(format, args);
        }
    }

    void concise(String format, Object... args) {
        if (feedback() == Feedback.Concise) {
            hard(format, args);
        }
    }

    void hard(String format, Object... args) {
        cmdout.printf("|  " + format + "\n", args);
    }

    static String trimEnd(String s) {
        int last = s.length() - 1;
        int i = last;
        while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
            --i;
        }
        if (i != last) {
            return s.substring(0, i + 1);
        } else {
            return s;
        }
    }

    protected JShell.Builder createJShell() {
        return JShell.builder()
                .in(userin)
                .out(userout)
                .err(usererr)
                .tempVariableNameGenerator(()-> "$" + currentNameSpace.tidNext())
                .idGenerator((sn, i) -> (currentNameSpace == startNamespace || state.status(sn).isActive)
                        ? currentNameSpace.tid(sn)
                        : errorNamespace.tid(sn));
    }
    
    protected JShell createJShellInstance() {
        return createJShell().build();
    }

    protected void resetState() {
        closeState();
        
        // Initialize tool id mapping
        mainNamespace = new NameSpace("main", "");
        startNamespace = new NameSpace("start", "s");
        errorNamespace = new NameSpace("error", "e");
        mapSnippet = new LinkedHashMap<>();
        currentNameSpace = startNamespace;

        state = createJShellInstance();
        analysis = state.sourceCodeAnalysis();
        shutdownSubscription = state.onShutdown((JShell deadState) -> {
            if (deadState == state && !live) {
                hard("State engine terminated.  See /history");
                live = false;
            }
        });
        live = true;

        if (cmdlineClasspath != null) {
            state.addToClasspath(cmdlineClasspath);
        }


        String start;
        if (cmdlineStartup == null) {
            start = PREFS.get(STARTUP_KEY, "<nada>");
            if (start.equals("<nada>")) {
                start = DEFAULT_STARTUP;
                PREFS.put(STARTUP_KEY, DEFAULT_STARTUP);
            }
        } else {
            start = cmdlineStartup;
        }
        try (Reader r = new StringReader(start)) {
            setupState();
            run(r);
        } catch (Exception | InternalError ex) {
            hard("Unexpected exception reading start-up: %s\n", ex);
        } finally {
            currentNameSpace = mainNamespace;
        }
    }
    
    protected void setupState() {
    }

    protected void closeState() {
        live = false;
        JShell oldState = state;
        if (oldState != null) {
            oldState.unsubscribe(shutdownSubscription); // No notification
            oldState.close();
        }
    }

    void run(Reader r) {
        boolean oldFluff = interactiveOutput;
        interactiveOutput = false;
        String incomplete = "";
        try (BufferedReader in = new BufferedReader(r)) {
            String raw;

            while (live && (raw = in.readLine()) != null) {
                incomplete = process(incomplete, raw);
            }
        } catch (IOException ex) {
            hard("Unexpected exception: %s\n", ex);
        } finally {
            interactiveOutput = oldFluff;
        }
    }

    protected String process(String prefix, String process) {
        String trimmed = trimEnd(process);
        if (!trimmed.isEmpty()) {
            history.add(process);
            String line = prefix + trimmed;

            // No commands in the middle of unprocessed source
            if (prefix.isEmpty() && line.startsWith("/") && !line.startsWith("//") && !line.startsWith("/*")) {
                processCommand(line.trim());
                return "";
            } else {
                return processSourceCatchingReset(line);
            }
        }
        return prefix;
    }

    private String processSourceCatchingReset(String src) {
        try {
            return processSource(src);
        } catch (IllegalStateException ex) {
            hard("Resetting...");
            live = false; // Make double sure
            return "";
        }
    }

    private void processCommand(String cmd) {
        try {
            //handle "/[number]"
            cmdUseHistoryEntry(Integer.parseInt(cmd.substring(1)));
            return ;
        } catch (NumberFormatException ex) {
            //ignore
        }
        String arg = "";
        int idx = cmd.indexOf(' ');
        if (idx > 0) {
            arg = cmd.substring(idx + 1).trim();
            cmd = cmd.substring(0, idx);
        }
        Command command = commands.get(cmd);
        if (command == null || command.kind == CommandKind.HELP_ONLY) {
            hard("No such command: %s", cmd);
            fluff("Type /help for help.");
        } else {
            command.run.accept(arg);
        }
    }

    private static String resolveUserHome(String path) {
        if (path.replace(File.separatorChar, '/').startsWith("~/"))
            path = System.getProperty("user.home") + path.substring(1);
        return path;
    }

    static final class Command {
        public final String[] aliases;
        public final String params;
        public final String description;
        public final Consumer<String> run;
        public final CompletionProvider completions;
        public final CommandKind kind;

        public Command(String command, String alias, String params, String description, Consumer<String> run, CompletionProvider completions) {
            this(command, alias, params, description, run, completions, CommandKind.NORMAL);
        }

        public Command(String command, String alias, String params, String description, Consumer<String> run, CompletionProvider completions, CommandKind kind) {
            this.aliases = alias != null ? new String[] {command, alias} : new String[] {command};
            this.params = params;
            this.description = description;
            this.run = run;
            this.completions = completions;
            this.kind = kind;
        }

    }

    interface CompletionProvider {
        List<Suggestion> completionSuggestions(String input, int cursor, int[] anchor);
    }

    enum CommandKind {
        NORMAL,
        HIDDEN,
        HELP_ONLY;
    }

    static final class FixedCompletionProvider implements CompletionProvider {

        private final String[] alternatives;

        public FixedCompletionProvider(String... alternatives) {
            this.alternatives = alternatives;
        }

        @Override
        public List<Suggestion> completionSuggestions(String input, int cursor, int[] anchor) {
            List<Suggestion> result = new ArrayList<>();

            for (String alternative : alternatives) {
                if (alternative.startsWith(input)) {
                    result.add(new Suggestion(alternative, false));
                }
            }

            anchor[0] = 0;

            return result;
        }

    }

    private static final CompletionProvider EMPTY_COMPLETION_PROVIDER = new FixedCompletionProvider();
    private static final CompletionProvider FILE_COMPLETION_PROVIDER = fileCompletions(p -> true);
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private void registerCommand(Command cmd) {
        for (String str : cmd.aliases) {
            commands.put(str, cmd);
        }
    }
    private static CompletionProvider fileCompletions(Predicate<Path> accept) {
        return (code, cursor, anchor) -> {
            int lastSlash = code.lastIndexOf('/'); //XXX
            String path = code.substring(0, lastSlash + 1);
            String prefix = lastSlash != (-1) ? code.substring(lastSlash + 1) : code;
            Path current = Paths.get(resolveUserHome(path));
            List<Suggestion> result = new ArrayList<>();
            try (DirectoryStream<Path> dir = Files.newDirectoryStream(current)) {
                for (Path e : dir) {
                    if (!accept.test(e) || !e.getFileName().toString().startsWith(prefix))
                        continue;
                    result.add(new Suggestion(e.getFileName() + (Files.isDirectory(e) ? "/" : ""), false));
                }
            } catch (IOException ex) {
                //ignore...
            }
            if (path.isEmpty()) {
                for (Path root : FileSystems.getDefault().getRootDirectories()) {
                    if (!accept.test(root) || !root.toString().startsWith(prefix))
                        continue;
                    result.add(new Suggestion(root.toString(), false));
                }
            }
            anchor[0] = path.length();
            return result;
        };
    }

    private static CompletionProvider classPathCompletion() {
        return fileCompletions(p -> Files.isDirectory(p) ||
                                    p.getFileName().toString().endsWith(".zip") ||
                                    p.getFileName().toString().endsWith(".jar"));
    }

    private CompletionProvider editCompletion() {
        return (prefix, cursor, anchor) -> {
            anchor[0] = 0;
            return state.snippets()
                        .stream()
                        .flatMap(k -> (k instanceof DeclarationSnippet)
                                ? Stream.of(String.valueOf(k.id()), ((DeclarationSnippet) k).name())
                                : Stream.of(String.valueOf(k.id())))
                        .filter(k -> k.startsWith(prefix))
                        .map(k -> new Suggestion(k, false))
                        .collect(Collectors.toList());
        };
    }

    private static CompletionProvider saveCompletion() {
        CompletionProvider keyCompletion = new FixedCompletionProvider("all ", "history ");
        return (code, cursor, anchor) -> {
            List<Suggestion> result = new ArrayList<>();
            int space = code.indexOf(' ');
            if (space == (-1)) {
                result.addAll(keyCompletion.completionSuggestions(code, cursor, anchor));
            }
            result.addAll(FILE_COMPLETION_PROVIDER.completionSuggestions(code.substring(space + 1), cursor - space - 1, anchor));
            anchor[0] += space + 1;
            return result;
        };
    }

    {
        registerCommand(new Command("/list", "/l", "[all]", "list the source you have typed",
                                    arg -> cmdList(arg),
                                    new FixedCompletionProvider("all")));
//        registerCommand(new Command("/seteditor", null, "<executable>", "set the external editor command to use",
//                                    arg -> cmdSetEditor(arg),
//                                    EMPTY_COMPLETION_PROVIDER));
        registerCommand(new Command("/drop", "/d", "<name or id>", "delete a source entry referenced by name or id",
                                    arg -> cmdDrop(arg),
                                    editCompletion()));
        registerCommand(new Command("/save", "/s", "[all|history] <file>", "save the source you have typed",
                                    arg -> cmdSave(arg),
                                    saveCompletion()));
        registerCommand(new Command("/open", "/o", "<file>", "open a file as source input",
                                    arg -> cmdOpen(arg),
                                    FILE_COMPLETION_PROVIDER));
        registerCommand(new Command("/vars", "/v", null, "list the declared variables and their values",
                                    arg -> cmdVars(),
                                    EMPTY_COMPLETION_PROVIDER));
        registerCommand(new Command("/methods", "/m", null, "list the declared methods and their signatures",
                                    arg -> cmdMethods(),
                                    EMPTY_COMPLETION_PROVIDER));
        registerCommand(new Command("/classes", "/c", null, "list the declared classes",
                                    arg -> cmdClasses(),
                                    EMPTY_COMPLETION_PROVIDER));
        registerCommand(new Command("/reset", "/r", null, "reset everything in the REPL",
                                    arg -> cmdReset(),
                                    EMPTY_COMPLETION_PROVIDER));
        registerCommand(new Command("/feedback", "/f", "<level>", "feedback information: off, concise, normal, verbose, default, or ?",
                                    arg -> cmdFeedback(arg),
                                    new FixedCompletionProvider("off", "concise", "normal", "verbose", "default", "?")));
        registerCommand(new Command("/prompt", "/p", null, "toggle display of a prompt",
                                    arg -> cmdPrompt(),
                                    EMPTY_COMPLETION_PROVIDER));
        registerCommand(new Command("/classpath", "/cp", "<path>", "add a path to the classpath",
                                    arg -> cmdClasspath(arg),
                                    classPathCompletion()));
        registerCommand(new Command("/history", "/h", null, "history of what you have typed",
                                    arg -> cmdHistory(),
                                    EMPTY_COMPLETION_PROVIDER));
//        registerCommand(new Command("/setstart", null, "<file>", "read file and set as the new start-up definitions",
//                                    arg -> cmdSetStart(arg),
//                                    FILE_COMPLETION_PROVIDER));
//        registerCommand(new Command("/savestart", null, "<file>", "save the default start-up definitions to the file",
//                                    arg -> cmdSaveStart(arg),
//                                    FILE_COMPLETION_PROVIDER));
        registerCommand(new Command("/debug", "/db", "", "toggle debugging of the REPL",
                                    arg -> cmdDebug(arg),
                                    EMPTY_COMPLETION_PROVIDER,
                                    CommandKind.HIDDEN));
        registerCommand(new Command("/help", "/?", "", "this help message",
                                    arg -> cmdHelp(),
                                    EMPTY_COMPLETION_PROVIDER));
        registerCommand(new Command("/!", null, "", "re-run last snippet",
                                    arg -> cmdUseHistoryEntry(-1),
                                    EMPTY_COMPLETION_PROVIDER));
        registerCommand(new Command("/<n>", null, "", "re-run n-th snippet",
                                    arg -> { throw new IllegalStateException(); },
                                    EMPTY_COMPLETION_PROVIDER,
                                    CommandKind.HELP_ONLY));
        registerCommand(new Command("/-<n>", null, "", "re-run n-th previous snippet",
                                    arg -> { throw new IllegalStateException(); },
                                    EMPTY_COMPLETION_PROVIDER,
                                    CommandKind.HELP_ONLY));
    }

    public List<Suggestion> commandCompletionSuggestions(String code, int cursor, int[] anchor) {
        code = code.substring(0, cursor);
        int space = code.indexOf(' ');
        List<Suggestion> result = new ArrayList<>();

        if (space == (-1)) {
            for (Command cmd : new LinkedHashSet<>(commands.values())) {
                if (cmd.kind == CommandKind.HIDDEN || cmd.kind == CommandKind.HELP_ONLY)
                    continue;
                String key = cmd.aliases[0];
                if (key.startsWith(code)) {
                    result.add(new Suggestion(key + " ", false));
                }
            }
            anchor[0] = 0;
        } else {
            String arg = code.substring(space + 1);
            String cmd = code.substring(0, space);
            Command command = commands.get(cmd);
            if (command != null) {
                result.addAll(command.completions.completionSuggestions(arg, cursor - space, anchor));
                anchor[0] += space + 1;
            }
        }

        Collections.sort(result, (s1, s2) -> s1.continuation.compareTo(s2.continuation));
        return result;
    }

    public String commandDocumentation(String code, int cursor) {
        code = code.substring(0, cursor);
        int space = code.indexOf(' ');

        if (space != (-1)) {
            String cmd = code.substring(0, space);
            Command command = commands.get(cmd);
            if (command != null) {
                return command.description;
            }
        }

        return null;
    }

    void cmdSetEditor(String arg) {
        if (arg == null || arg.isEmpty()) {
            hard("/seteditor requires a path argument");
        } else {
            editor = arg;
            fluff("Editor set to: %s", arg);
        }
    }

    void cmdClasspath(String arg) {
        if (arg == null || arg.isEmpty()) {
            hard("/classpath requires a path argument");
        } else {
            state.addToClasspath(resolveUserHome(arg));
//            fluff("Path %s added to classpath", arg);
        }
    }

    void cmdDebug(String arg) {
        if (arg == null || arg.isEmpty()) {
            debug = !debug;
            InternalDebugControl.setDebugFlags(state, debug ? InternalDebugControl.DBG_GEN : 0);
            fluff("Debugging %s", debug ? "on" : "off");
        } else {
            int flags = 0;
            for (char ch : arg.toCharArray()) {
                switch (ch) {
                    case '0':
                        flags = 0;
                        debug = false;
                        fluff("Debugging off");
                        break;
                    case 'r':
                        debug = true;
                        fluff("REPL tool debugging on");
                        break;
                    case 'g':
                        flags |= InternalDebugControl.DBG_GEN;
                        fluff("General debugging on");
                        break;
                    case 'f':
                        flags |= InternalDebugControl.DBG_FMGR;
                        fluff("File manager debugging on");
                        break;
                    case 'c':
                        flags |= InternalDebugControl.DBG_COMPA;
                        fluff("Completion analysis debugging on");
                        break;
                    case 'd':
                        flags |= InternalDebugControl.DBG_DEP;
                        fluff("Dependency debugging on");
                        break;
                    case 'e':
                        flags |= InternalDebugControl.DBG_EVNT;
                        fluff("Event debugging on");
                        break;
                    default:
                        hard("Unknown debugging option: %c", ch);
                        fluff("Use: 0 r g f c d");
                        break;
                }
            }
            InternalDebugControl.setDebugFlags(state, flags);
        }
    }

    private void cmdFeedback(String arg) {
        switch (arg) {
            case "":
            case "d":
            case "default":
                feedback = Feedback.Default;
                break;
            case "o":
            case "off":
                feedback = Feedback.Off;
                break;
            case "c":
            case "concise":
                feedback = Feedback.Concise;
                break;
            case "n":
            case "normal":
                feedback = Feedback.Normal;
                break;
            case "v":
            case "verbose":
                feedback = Feedback.Verbose;
                break;
            default:
                hard("Follow /feedback with of the following:");
                hard("  off       (errors and critical output only)");
                hard("  concise");
                hard("  normal");
                hard("  verbose");
                hard("  default");
                hard("You may also use just the first letter, for example: /f c");
                hard("In interactive mode 'default' is the same as 'normal', from a file it is the same as 'off'");
                return;
        }
        fluff("Feedback mode: %s", feedback.name().toLowerCase());
    }

    void cmdHelp() {
        int synopsisLen = 0;
        Map<String, String> synopsis2Description = new LinkedHashMap<>();
        for (Command cmd : new LinkedHashSet<>(commands.values())) {
            if (cmd.kind == CommandKind.HIDDEN)
                continue;
            StringBuilder synopsis = new StringBuilder();
            if (cmd.aliases.length > 1) {
                synopsis.append(String.format("%-3s or ", cmd.aliases[1]));
            } else {
                synopsis.append("       ");
            }
            synopsis.append(cmd.aliases[0]);
            if (cmd.params != null)
                synopsis.append(" ").append(cmd.params);
            synopsis2Description.put(synopsis.toString(), cmd.description);
            synopsisLen = Math.max(synopsisLen, synopsis.length());
        }
        cmdout.println("Type a Java language expression, statement, or declaration.");
        cmdout.println("Or type one of the following commands:\n");
        for (Entry<String, String> e : synopsis2Description.entrySet()) {
            cmdout.print(String.format("%-" + synopsisLen + "s", e.getKey()));
            cmdout.print(" -- ");
            cmdout.println(e.getValue());
        }
        cmdout.println();
        cmdout.println("Supported shortcuts include:");
        cmdout.println("<tab>       -- show possible completions for the current text");
        cmdout.println("Shift-<tab> -- for current method or constructor invocation, show a synopsis of the method/constructor");
    }

    private void cmdHistory() {
        cmdout.println();
        for (String s : history) {
            // No number prefix, confusing with snippet ids
            cmdout.printf("%s\n", s);
        }
    }

    private Set<Snippet> argToKeySet(String arg) {
        Set<Snippet> keySet = new LinkedHashSet<>();
        if (arg.isEmpty()) {
            // Default is all user snippets
            for (Snippet sn : state.snippets()) {
                if (notInStartUp(sn)) {
                    keySet.add(sn);
                }
            }
        } else {
            for (Snippet sn : state.snippets()) {
                if (sn.id().equals(arg)) {
                    keySet.add(sn);
                    break;
                }
            }
            if (Character.isDigit(arg.charAt(0)) && keySet.isEmpty()) {
                hard("No id %s found.  See /list", arg);
                return null;
            }
            for (Snippet key : state.snippets()) {
                switch (key.kind()) {
                    case METHOD:
                    case VAR:
                    case TYPE_DECL:
                        if (((DeclarationSnippet) key).name().equals(arg)) {
                            keySet.add(key);
                        }
                        break;
                }
            }
            if (keySet.isEmpty()) {
                hard("No definition named %s found.  See /classes /methods or /vars", arg);
                return null;
            }
        }
        return keySet;
    }

    private void cmdDrop(String arg) {
        if (arg.isEmpty()) {
            hard("In the /drop argument, please specify an import, variable, method, or class to drop.");
            hard("Specify by id or name. Use /list to see ids. Use /reset to reset all state.");
            return;
        }
        Set<Snippet> keySet = argToKeySet(arg);
        if (keySet == null) {
            return;
        }
        Iterator<Snippet> it = keySet.iterator();
        while (it.hasNext()) {
            if (!(it.next() instanceof PersistentSnippet)) {
                it.remove();
            }
        }
        if (keySet.isEmpty()) {
            hard("The argument did not specify an import, variable, method, or class to drop.");
            return;
        }
        if (keySet.size() > 1) {
            hard("The argument references more than one import, variable, method, or class.");
            hard("Try again with one of the ids below:");
            for (Snippet sn : keySet) {
                cmdout.printf("%4s : %s\n", sn.id(), sn.source().replace("\n", "\n       "));
            }
            return;
        }
        PersistentSnippet key = (PersistentSnippet) keySet.iterator().next();
        state.drop(key).forEach(e -> handleEvent(e));
    }

    private void cmdList(String arg) {
        boolean all = false;
        switch (arg) {
            case "all":
                all = true;
                break;
            case "history":
                cmdHistory();
                return;
            case "":
                break;
            default:
                hard("Invalid /list argument: %s", arg);
                return;
        }
        boolean hasOutput = false;
        for (Snippet sn : state.snippets()) {
            if (all || (notInStartUp(sn) && state.status(sn).isActive)) {
                if (!hasOutput) {
                    cmdout.println();
                    hasOutput = true;
                }
                cmdout.printf("%4s : %s\n", sn.id(), sn.source().replace("\n", "\n       "));

            }
        }
    }

    private void cmdOpen(String filename) {
        if (filename.isEmpty()) {
            hard("The /open command requires a filename argument.");
        } else {
            try (Reader r = new FileReader(filename)){
                run(r);
            } catch (FileNotFoundException e) {
                hard("File '%s' is not found: %s", filename, e.getMessage());
            } catch (Exception e) {
                hard("Exception while reading file: %s", e);
            }
        }
    }

    private void cmdPrompt() {
        displayPrompt = !displayPrompt;
        fluff("Prompt will %sdisplay. Use /prompt to toggle.", displayPrompt ? "" : "NOT ");
        concise("Prompt: %s", displayPrompt ? "on" : "off");
    }

    private void cmdReset() {
        live = false;
        fluff("Resetting state.");
    }

    private void cmdSave(String arg_filename) {
        Matcher mat = HISTORY_ALL_FILENAME.matcher(arg_filename);
        if (!mat.find()) {
            hard("Malformed argument to the /save command: %s", arg_filename);
            return;
        }
        boolean useHistory = false;
        boolean saveAll = false;
        String cmd = mat.group("cmd");
        if (cmd != null) switch (cmd) {
            case "all":
                saveAll = true;
                break;
            case "history":
                useHistory = true;
                break;
        }
        String filename = mat.group("filename");
        if (filename == null ||filename.isEmpty()) {
            hard("The /save command requires a filename argument.");
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resolveUserHome(filename)))) {
            if (useHistory) {
                for (String s : history) {
                    writer.write(s);
                    writer.write("\n");
                }
            } else {
                for (Snippet sn : state.snippets()) {
                    if (saveAll || notInStartUp(sn)) {
                        writer.write(sn.source());
                        writer.write("\n");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            hard("File '%s' for save is not accessible: %s", filename, e.getMessage());
        } catch (Exception e) {
            hard("Exception while saving: %s", e);
        }
    }

    private void cmdSetStart(String filename) {
        if (filename.isEmpty()) {
            hard("The /setstart command requires a filename argument.");
        } else {
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(resolveUserHome(filename)));
                String init = new String(encoded);
                PREFS.put(STARTUP_KEY, init);
            } catch (AccessDeniedException e) {
                hard("File '%s' for /setstart is not accessible.", filename);
            } catch (NoSuchFileException e) {
                hard("File '%s' for /setstart is not found.", filename);
            } catch (Exception e) {
                hard("Exception while reading start set file: %s", e);
            }
        }
    }

    private void cmdSaveStart(String filename) {
        if (filename.isEmpty()) {
            hard("The /savestart command requires a filename argument.");
        } else {
            try {
                Files.write(Paths.get(resolveUserHome(filename)), DEFAULT_STARTUP.getBytes());
            } catch (AccessDeniedException e) {
                hard("File '%s' for /savestart is not accessible.", filename);
            } catch (NoSuchFileException e) {
                hard("File '%s' for /savestart cannot be located.", filename);
            } catch (Exception e) {
                hard("Exception while saving default startup file: %s", e);
            }
        }
    }

    private void cmdVars() {
        for (VarSnippet vk : state.variables()) {
            String val = state.status(vk) == Status.VALID
                    ? state.varValue(vk)
                    : "(not-active)";
            hard("  %s %s = %s", vk.typeName(), vk.name(), val);
        }
    }

    private void cmdMethods() {
        for (MethodSnippet mk : state.methods()) {
            hard("  %s %s", mk.name(), mk.signature());
        }
    }

    private void cmdClasses() {
        for (TypeDeclSnippet ck : state.types()) {
            String kind;
            switch (ck.subKind()) {
                case INTERFACE_SUBKIND:
                    kind = "interface";
                    break;
                case CLASS_SUBKIND:
                    kind = "class";
                    break;
                case ENUM_SUBKIND:
                    kind = "enum";
                    break;
                case ANNOTATION_TYPE_SUBKIND:
                    kind = "@interface";
                    break;
                default:
                    assert false : "Wrong kind" + ck.subKind();
                    kind = "class";
                    break;
            }
            hard("  %s %s", kind, ck.name());
        }
    }

    private void cmdUseHistoryEntry(int index) {
        List<Snippet> keys = state.snippets();
        if (index < 0)
            index += keys.size();
        else
            index--;
        if (index >= 0 && index < keys.size()) {
            String source = keys.get(index).source();
            cmdout.printf("%s\n", source);
            history.set(history.size() - 1, source);
            processSourceCatchingReset(source);
        } else {
            hard("Cannot find snippet %d", index + 1);
        }
    }

    /**
     * Filter diagnostics for only errors (no warnings, ...)
     * @param diagnostics input list
     * @return filtered list
     */
    List<Diag> errorsOnly(List<Diag> diagnostics) {
        return diagnostics.stream()
                .filter(d -> d.isError())
                .collect(toList());
    }

    void printDiagnostics(String source, List<Diag> diagnostics, boolean embed) {
        String padding = embed? "    " : "";
        for (Diag diag : diagnostics) {
            //assert diag.getSource().equals(source);

            if (!embed) {
                if (diag.isError()) {
                    hard("Error:");
                } else {
                    hard("Warning:");
                }
            }

            for (String line : diag.getMessage(null).split("\\r?\\n")) {
                if (!line.trim().startsWith("location:")) {
                    hard("%s%s", padding, line);
                }
            }

            int pstart = (int) diag.getStartPosition();
            int pend = (int) diag.getEndPosition();
            Matcher m = LINEBREAK.matcher(source);
            int pstartl = 0;
            int pendl = -2;
            while (m.find(pstartl)) {
                pendl = m.start();
                if (pendl >= pstart) {
                    break;
                } else {
                    pstartl = m.end();
                }
            }
            if (pendl < pstart) {
                pendl = source.length();
            }
            fluff("%s%s", padding, source.substring(pstartl, pendl));

            StringBuilder sb = new StringBuilder();
            int start = pstart - pstartl;
            for (int i = 0; i < start; ++i) {
                sb.append(' ');
            }
            sb.append('^');
            boolean multiline = pend > pendl;
            int end = (multiline ? pendl : pend) - pstartl - 1;
            if (end > start) {
                for (int i = start + 1; i < end; ++i) {
                    sb.append('-');
                }
                if (multiline) {
                    sb.append("-...");
                } else {
                    sb.append('^');
                }
            }
            fluff("%s%s", padding, sb.toString());

            debug("printDiagnostics start-pos = %d ==> %d -- wrap = %s", diag.getStartPosition(), start, this);
            debug("Code: %s", diag.getCode());
            debug("Pos: %d (%d - %d)", diag.getPosition(),
                    diag.getStartPosition(), diag.getEndPosition());
        }
    }
    
    private String processSource(String srcInput) throws IllegalStateException {
        while (true) {
            CompletionInfo an = analysis.analyzeCompletion(srcInput);
            if (!an.completeness.isComplete) {
                return an.remaining;
            }
            boolean failed = processCompleteSource(an.source);
            
            if (failed || an.remaining.isEmpty()) {
                return "";
            }
            srcInput = an.remaining;
        }
    }
    //where
    private boolean processCompleteSource(String source) throws IllegalStateException {
        debug("Compiling: %s", source);
        boolean failed = false;
        List<SnippetEvent> events = state.eval(source);
        for (SnippetEvent e : events) {
            failed |= handleEvent(e);
        }
        return failed;
    }

    private boolean handleEvent(SnippetEvent ste) {
        Snippet sn = ste.snippet();
        if (sn == null) {
            debug("Event with null key: %s", ste);
            return false;
        }
        List<Diag> diagnostics = state.diagnostics(sn);
        String source = sn.source();
        if (ste.causeSnippet() == null) {
            // main event
            printDiagnostics(source, diagnostics, false);
            if (ste.status().isActive) {
                if (ste.exception() != null) {
                    if (ste.exception() instanceof EvalException) {
                        printEvalException((EvalException) ste.exception());
                        return true;
                    } else if (ste.exception() instanceof UnresolvedReferenceException) {
                        printUnresolved((UnresolvedReferenceException) ste.exception());
                    } else {
                        hard("Unexpected execution exception: %s", ste.exception());
                        return true;
                    }
                } else {
                    displayDeclarationAndValue(ste, false, ste.value());
                }
            } else if (ste.status() == Status.REJECTED) {
                if (diagnostics.isEmpty()) {
                    hard("Failed.");
                }
                return true;
            }
        } else if (ste.status() == Status.REJECTED) {
            //TODO -- I don't believe updates can cause failures any more
            hard("Caused failure of dependent %s --", ((DeclarationSnippet) sn).name());
            printDiagnostics(source, diagnostics, true);
        } else {
            // Update
            SubKind subkind = sn.subKind();
            if (sn instanceof DeclarationSnippet
                    && (feedback() == Feedback.Verbose
                    || ste.status() == Status.OVERWRITTEN
                    || subkind == SubKind.VAR_DECLARATION_SUBKIND
                    || subkind == SubKind.VAR_DECLARATION_WITH_INITIALIZER_SUBKIND)) {
                // Under the conditions, display update information
                displayDeclarationAndValue(ste, true, null);
                List<Diag> other = errorsOnly(diagnostics);
                if (other.size() > 0) {
                    printDiagnostics(source, other, true);
                }
            }
        }
        return false;
    }

    @SuppressWarnings("fallthrough")
    private void displayDeclarationAndValue(SnippetEvent ste, boolean update, String value) {
        Snippet key = ste.snippet();
        String declared;
        Status status = ste.status();
        switch (status) {
            case VALID:
            case RECOVERABLE_DEFINED:
            case RECOVERABLE_NOT_DEFINED:
                if (ste.previousStatus().isActive) {
                    declared = ste.isSignatureChange()
                        ? "Replaced"
                        : "Modified";
                } else {
                    declared = "Added";
                }
                break;
            case OVERWRITTEN:
                declared = "Overwrote";
                break;
            case DROPPED:
                declared = "Dropped";
                break;
            case REJECTED:
                declared = "Rejected";
                break;
            case NONEXISTENT:
            default:
                // Should not occur
                declared = ste.previousStatus().toString() + "=>" + status.toString();
        }
        if (update) {
            declared = "  Update " + declared.toLowerCase();
        }
        String however;
        if (key instanceof DeclarationSnippet && (status == Status.RECOVERABLE_DEFINED || status == Status.RECOVERABLE_NOT_DEFINED)) {
            String cannotUntil = (status == Status.RECOVERABLE_NOT_DEFINED)
                    ? " cannot be referenced until"
                    : " cannot be invoked until";
            however = (update? " which" : ", however, it") + cannotUntil + unresolved((DeclarationSnippet) key);
        } else {
            however = "";
        }
        switch (key.subKind()) {
            case CLASS_SUBKIND:
                fluff("%s class %s%s", declared, ((TypeDeclSnippet) key).name(), however);
                break;
            case INTERFACE_SUBKIND:
                fluff("%s interface %s%s", declared, ((TypeDeclSnippet) key).name(), however);
                break;
            case ENUM_SUBKIND:
                fluff("%s enum %s%s", declared, ((TypeDeclSnippet) key).name(), however);
                break;
            case ANNOTATION_TYPE_SUBKIND:
                fluff("%s annotation interface %s%s", declared, ((TypeDeclSnippet) key).name(), however);
                break;
            case METHOD_SUBKIND:
                fluff("%s method %s(%s)%s", declared, ((MethodSnippet) key).name(),
                        ((MethodSnippet) key).parameterTypes(), however);
                break;
            case VAR_DECLARATION_SUBKIND:
                if (!update) {
                    VarSnippet vk = (VarSnippet) key;
                    if (status == Status.RECOVERABLE_NOT_DEFINED) {
                        fluff("%s variable %s%s", declared, vk.name(), however);
                    } else {
                        fluff("%s variable %s of type %s%s", declared, vk.name(), vk.typeName(), however);
                    }
                    break;
                }
            // Fall through
            case VAR_DECLARATION_WITH_INITIALIZER_SUBKIND: {
                VarSnippet vk = (VarSnippet) key;
                if (status == Status.RECOVERABLE_NOT_DEFINED) {
                    if (!update) {
                        fluff("%s variable %s%s", declared, vk.name(), however);
                        break;
                    }
                } else if (update) {
                    if (ste.isSignatureChange()) {
                        hard("%s variable %s, reset to null", declared, vk.name());
                    }
                } else {
                    fluff("%s variable %s of type %s with initial value %s",
                            declared, vk.name(), vk.typeName(), value);
                    concise("%s : %s", vk.name(), value);
                }
                break;
            }
            case TEMP_VAR_EXPRESSION_SUBKIND: {
                VarSnippet vk = (VarSnippet) key;
                if (update) {
                    hard("%s temporary variable %s, reset to null", declared, vk.name());
                 } else {
                    fluff("Expression value is: %s", (value));
                    fluff("  assigned to temporary variable %s of type %s", vk.name(), vk.typeName());
                    concise("%s : %s", vk.name(), value);
                }
                break;
            }
            case OTHER_EXPRESSION_SUBKIND:
                fluff("Expression value is: %s", (value));
                break;
            case VAR_VALUE_SUBKIND: {
                ExpressionSnippet ek = (ExpressionSnippet) key;
                fluff("Variable %s of type %s has value %s", ek.name(), ek.typeName(), (value));
                concise("%s : %s", ek.name(), value);
                break;
            }
            case ASSIGNMENT_SUBKIND: {
                ExpressionSnippet ek = (ExpressionSnippet) key;
                fluff("Variable %s has been assigned the value %s", ek.name(), (value));
                concise("%s : %s", ek.name(), value);
                break;
            }
        }
    }
    //where
    void printStackTrace(StackTraceElement[] stes) {
        for (StackTraceElement ste : stes) {
            StringBuilder sb = new StringBuilder();
            String cn = ste.getClassName();
            if (!cn.isEmpty()) {
                int dot = cn.lastIndexOf('.');
                if (dot > 0) {
                    sb.append(cn.substring(dot + 1));
                } else {
                    sb.append(cn);
                }
                sb.append(".");
            }
            if (!ste.getMethodName().isEmpty()) {
                sb.append(ste.getMethodName());
                sb.append(" ");
            }
            String fileName = ste.getFileName();
            int lineNumber = ste.getLineNumber();
            String loc = ste.isNativeMethod()
                    ? "Native Method"
                    : fileName == null
                            ? "Unknown Source"
                            : lineNumber >= 0
                                    ? fileName + ":" + lineNumber
                                    : fileName;
            hard("      at %s(%s)", sb, loc);

        }
    }
    //where
    void printUnresolved(UnresolvedReferenceException ex) {
        DeclarationSnippet corralled =  ex.getSnippet();
        List<Diag> otherErrors = errorsOnly(state.diagnostics(corralled));
        StringBuilder sb = new StringBuilder();
        if (otherErrors.size() > 0) {
            if (state.unresolvedDependencies(corralled).size() > 0) {
                sb.append(" and");
            }
            if (otherErrors.size() == 1) {
                sb.append(" this error is addressed --");
            } else {
                sb.append(" these errors are addressed --");
            }
        } else {
            sb.append(".");
        }

        hard("Attempted to call %s which cannot be invoked until%s", corralled.name(),
                unresolved(corralled), sb.toString());
        if (otherErrors.size() > 0) {
            printDiagnostics(corralled.source(), otherErrors, true);
        }
    }
    //where
    void printEvalException(EvalException ex) {
        if (ex.getMessage() == null) {
            hard("%s thrown", ex.getExceptionClassName());
        } else {
            hard("%s thrown: %s", ex.getExceptionClassName(), ex.getMessage());
        }
        printStackTrace(ex.getStackTrace());
    }
    //where
    String unresolved(DeclarationSnippet key) {
        List<String> unr = state.unresolvedDependencies(key);
        StringBuilder sb = new StringBuilder();
        int fromLast = unr.size();
        if (fromLast > 0) {
            sb.append(" ");
        }
        for (String u : unr) {
            --fromLast;
            sb.append(u);
            if (fromLast == 0) {
                // No suffix
            } else if (fromLast == 1) {
                sb.append(", and ");
            } else {
                sb.append(", ");
            }
        }
        switch (unr.size()) {
            case 0:
                break;
            case 1:
                sb.append(" is declared");
                break;
            default:
                sb.append(" are declared");
                break;
        }
        return sb.toString();
    }

    protected List<String> completions(String prefix, String process) {
        int[] anchor = new int[1];
        List<String> result = new ArrayList<>();
        for (Suggestion s : analysis.completionSuggestions(prefix + process, prefix.length() + process.length(), anchor)) {
            result.add(s.continuation + "\n" + (anchor[0] - prefix.length()));
        }
        return result;
    }

    enum Feedback {
        Default,
        Off,
        Concise,
        Normal,
        Verbose
    }

    Feedback feedback() {
        if (feedback == Feedback.Default) {
            return interactiveOutput ? Feedback.Normal : Feedback.Off;
        }
        return feedback;
    }

    boolean notInStartUp(Snippet sn) {
        return mapSnippet.get(sn).space != startNamespace;
    }

    class NameSpace {
        final String spaceName;
        final String prefix;
        private int nextNum;

        NameSpace(String spaceName, String prefix) {
            this.spaceName = spaceName;
            this.prefix = prefix;
            this.nextNum = 1;
        }

        String tid(Snippet sn) {
            String tid = prefix + nextNum++;
            mapSnippet.put(sn, new SnippetInfo(sn, this, tid));
            return tid;
        }

        String tidNext() {
            return prefix + nextNum;
        }
    }

    static class SnippetInfo {
        final Snippet snippet;
        final NameSpace space;
        final String tid;

        SnippetInfo(Snippet snippet, NameSpace space, String tid) {
            this.snippet = snippet;
            this.space = space;
            this.tid = tid;
        }
    }

    protected String prompt(boolean continuation) {
        return !continuation ? feedback() == Feedback.Concise ? "-> "
                                                              : "\n-> "
                             : ">> ";
    }

}
