/*****************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.commandLine;

import java.io.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.commandLine.command.CommandProvider;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.event.CVSListener;

/**
 * An implementation of the standard CVS client utility (command line tool)
 * in Java
 * @author  Robert Greig
 */
public class CVSCommand {
    
    private static final String HELP_OPTIONS = "--help-options"; // NOI18N
    private static final String HELP_COMMANDS = "--help-commands"; // NOI18N
    private static final String HELP_SYNONYMS = "--help-synonyms"; // NOI18N
    
    /**
     * The path to the repository on the server
     */
    private String repository;

    /**
     * The local path to use to perform operations (the top level)
     */
    private String localPath;

    /**
     * The connection to the server
     */
    private Connection connection;

    /**
     * The client that manages interactions with the server
     */
    private Client client;

    /**
     * The global options being used. GlobalOptions are only global for a
     * particular command.
     */
    private GlobalOptions globalOptions;
    
    /**
     * The port number that is used to connect to the remote server.
     * It is taken into account only when it's value is greater then zero.
     */
    private int port = 0;

    /**
     * Execute a configured CVS command
     * @param command the command to execute
     * @throws CommandException if there is an error running the command
     */
    public boolean executeCommand(Command command, PrintStream stderr)
            throws CommandException, AuthenticationException {
        client.setErrorStream(stderr);
        return client.executeCommand(command, globalOptions);
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setGlobalOptions(GlobalOptions globalOptions) {
        this.globalOptions = globalOptions;
    }

    /**
     * Creates the connection and the client and connects.
     */
    private void connect(CVSRoot root, String password) throws IllegalArgumentException,
                                                               AuthenticationException,
                                                               CommandAbortedException {
        connection = ConnectionFactory.getConnection(root);
        if (CVSRoot.METHOD_PSERVER.equals(root.getMethod())) {
            ((PServerConnection) connection).setEncodedPassword(password);
            if (port > 0) ((PServerConnection) connection).setPort(port);
        }
        connection.open();
        
        client = new Client(connection, new StandardAdminHandler());
        client.setLocalPath(localPath);
    }
    
    private void addListener(CVSListener listener) {
        if (client != null) {
            // add a listener to the client
            client.getEventManager().addCVSListener(listener);
        }
    }

    private void close(PrintStream stderr) {
        try {
            connection.close();
        }
        catch (IOException e) {
            stderr.println("Unable to close connection: " + e);
            //e.printStackTrace();
        }
    }

    /**
     * Obtain the CVS root, either from the -D option cvs.root or from
     * the CVS directory
     * @return the CVSRoot string
     */
    private static String getCVSRoot(String workingDir) {
        String root = null;
        BufferedReader r = null;
        if (workingDir == null) {
            workingDir = System.getProperty("user.dir");
        }
        try {
            File f = new File(workingDir);
            File rootFile = new File(f, "CVS/Root");
            if (rootFile.exists()) {
                r = new BufferedReader(new FileReader(rootFile));
                root = r.readLine();
            }
        }
        catch (IOException e) {
            // ignore
        }
        finally {
            try {
                if (r != null)
                    r.close();
            }
            catch (IOException e) {
                System.err.println("Warning: could not close CVS/Root file!");
            }
        }
        if (root == null) {
            root = System.getProperty("cvs.root");
        }
        return root;
    }

    /**
     * Process global options passed into the application
     * @param args the argument list, complete
     * @param globalOptions the global options structure that will be passed
     * to the command
     */
    private static int processGlobalOptions(String[] args,
                                            GlobalOptions globalOptions,
                                            PrintStream stderr) {
        final String getOptString = globalOptions.getOptString();
        GetOpt go = new GetOpt(args, getOptString);
        int ch = -1;
        boolean usagePrint = false;
        while ((ch = go.getopt()) != go.optEOF) {
            //System.out.println("Global option '"+((char) ch)+"', '"+go.optArgGet()+"'");
            boolean success = globalOptions.setCVSCommand((char) ch, go.optArgGet());
            if (!success) usagePrint = true;
        }
        if (usagePrint) {
            showUsage(stderr);
            return -10;
        }
        return go.optIndexGet();
    }

    private static void showUsage(PrintStream stderr) {
        String usageStr = ResourceBundle.getBundle(CVSCommand.class.getPackage().getName()+".Bundle").getString("MSG_HelpUsage"); // NOI18N
        stderr.println(MessageFormat.format(usageStr, new Object[] { HELP_OPTIONS, HELP_COMMANDS, HELP_SYNONYMS }));
        //stderr.println("Usage: cvs [global options] command [command-options-and-arguments]");
        //stderr.println("       specify "+HELP_OPTIONS+" for a list of options");
        //stderr.println("       specify "+HELP_COMMANDS+" for a list of commands");
        //stderr.println("       specify "+HELP_SYNONYMS+" for a list of command synonyms");
    }

    /**
     * Perform the 'login' command, asking the user for a password. If the
     * login is successful, the password is written to a file. The file's
     * location is user.home, unless the cvs.passfile option is set.
     * @param userName the userName
     * @param hostName the host
     */
    private static boolean performLogin(String userName, String hostName,
                                        String repository, int port,
                                        GlobalOptions globalOptions) {
        PServerConnection c = new PServerConnection();
        c.setUserName(userName);
        String password = null;
        try {
            BufferedReader in = new BufferedReader(new
                    InputStreamReader(System.in));
            System.out.print("Enter password: ");
            password = in.readLine();
        }
        catch (IOException e) {
            System.err.println("Could not read password: " + e);
            return false;
        }

        String encodedPassword = StandardScrambler.getInstance().scramble(
                password);
        c.setEncodedPassword(encodedPassword);
        c.setHostName(hostName);
        c.setRepository(repository);
        if (port > 0) c.setPort(port);
        try {
            c.verify();
        }
        catch (AuthenticationException e) {
            System.err.println("Could not login to host " + hostName);
            return false;
        }
        // was successful, so write the appropriate file out
        // we look for cvs.passfile being set, but if not use user.dir
        // as the default
        String root = globalOptions.getCVSRoot();
        try {
            PasswordsFile.storePassword(root, encodedPassword);
            System.err.println("Logged in successfully to " + repository + " on host " + hostName);
            return true;
        } catch (IOException e) {
            System.err.println("Error: could not write password file.");
            return false;
        }

    }

    /**
     * Lookup the password in the .cvspass file. This file is looked for
     * in the user.home directory if the option cvs.passfile is not set
     * @param CVSRoot the CVS root for which the password is being searched
     * @return the password, scrambled
     */
    private static String lookupPassword(String CVSRoot, String CVSRootWithPort, PrintStream stderr) {
        File passFile = new File(System.getProperty("cvs.passfile",
                                                    System.getProperty("user.home") +
                                                    "/.cvspass"));

        BufferedReader reader = null;
        String password = null;

        try {
            reader = new BufferedReader(new FileReader(passFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("/1 ")) line = line.substring("/1 ".length());
                if (line.startsWith(CVSRoot+" ")) {
                    password = line.substring(CVSRoot.length() + 1);
                    break;
                } else if (line.startsWith(CVSRootWithPort+" ")) {
                    password = line.substring(CVSRootWithPort.length() + 1);
                    break;
                }
            }
        }
        catch (IOException e) {
            stderr.println("Could not read password for host: " + e);
            return null;
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    stderr.println("Warning: could not close password file.");
                }
            }
        }
        if (password == null) {
            stderr.println("Didn't find password for CVSROOT '"+CVSRoot+"'.");
        }
        return password;
    }

    /**
     * Execute the CVS command and exit JVM.
     */
    public static void main(String[] args) {
        if (processCommand(args, null, System.getProperty("user.dir"),
                           System.out, System.err)) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
    
    /**
     * Process the CVS command passed in args[] array with all necessary options.
     * The only difference from main() method is, that this method does not exit
     * the JVM and provides command output.
     * @param args The command with options
     * @param files The files to execute the command on.
     * @param localPath The local working directory
     * @param stdout The standard output of the command
     * @param stderr The error output of the command.
     */
    public static boolean processCommand(String[] args, File[] files, String localPath,
                                         PrintStream stdout, PrintStream stderr) {
        return processCommand(args, files, localPath, 0, stdout, stderr);
    }
    
    /**
     * Process the CVS command passed in args[] array with all necessary options.
     * The only difference from main() method is, that this method does not exit
     * the JVM and provides command output.
     * @param args The command with options
     * @param files The files to execute the command on.
     * @param localPath The local working directory
     * @param port The port number that is used to connect to the remote server.
     *             It is taken into account only when it's value is greater then zero.
     * @param stdout The standard output of the command
     * @param stderr The error output of the command.
     * @return whether the command was processed successfully
     */
    public static boolean processCommand(String[] args, File[] files, String localPath,
                                         int port, PrintStream stdout, PrintStream stderr) {
        assert stdout != null: "The output stream must be defined."; // NOI18N
        assert stderr != null: "The error stream must be defined."; // NOI18N
        // Provide help if requested
        if (args.length > 0) {
            if (HELP_OPTIONS.equals(args[0])) {
                printHelpOptions(stdout);
                return true;
            } else if (HELP_COMMANDS.equals(args[0])) {
                printHelpCommands(stdout);
                return true;
            } else if (HELP_SYNONYMS.equals(args[0])) {
                printHelpSynonyms(stdout);
                return true;
            }
        }
        try { // Adjust the local path
            localPath = new File(localPath).getCanonicalPath();
        } catch (IOException ioex) {}
        // Set up the CVSRoot. Note that it might still be null after this
        // call if the user has decided to set it with the -d command line
        // global option
        GlobalOptions globalOptions = new GlobalOptions();
        globalOptions.setCVSRoot(getCVSRoot(localPath));

        // Set up any global options specified. These occur before the
        // name of the command to run
        int commandIndex = -1;
        try {
            commandIndex = processGlobalOptions(args, globalOptions, stderr);
            if (commandIndex == -10) return true;
        }
        catch (IllegalArgumentException e) {
            stderr.println("Invalid argument: " + e);
            return false;
        }
        
        if (globalOptions.isShowHelp()) {
            printHelp(commandIndex, args, stdout, stderr);
            return true;
        }
        
        if (globalOptions.isShowVersion()) {
            printVersion(stdout, stderr);
            return true;
        }

        // if we don't have a CVS root by now, the user has messed up
        if (globalOptions.getCVSRoot() == null) {
            stderr.println("No CVS root is set. Use the cvs.root " +
                           "property, e.g. java -Dcvs.root=\":pserver:user@host:/usr/cvs\"" +
                           " or start the application in a directory containing a CVS subdirectory" +
                           " or use the -d command switch.");
            return false;
        }

        // parse the CVS root into its constituent parts
        CVSRoot root = null;
        final String cvsRoot = globalOptions.getCVSRoot();
        try {
            root = CVSRoot.parse(cvsRoot);
        }
        catch (IllegalArgumentException e) {
            stderr.println("Incorrect format for CVSRoot: " + cvsRoot +
                           "\nThe correct format is: "+
                           "[:method:][[user][:password]@][hostname:[port]]/path/to/repository" +
                           "\nwhere \"method\" is pserver.");
            return false;
        }

        // if we had some options without any command, then the user messed up
        if (commandIndex >= args.length) {
            showUsage(stderr);
            return false;
        }

        final String command = args[commandIndex];
        if (command.equals("login")) {
            if (CVSRoot.METHOD_PSERVER.equals(root.getMethod())) {
                return performLogin(root.getUserName(), root.getHostName(),
                                    root.getRepository(), root.getPort(),
                                    globalOptions);
            }
            else {
                stderr.println("login does not apply for connection type " +
                               "\'" + root.getMethod() + "\'");
                return false;
            }
        }

        // this is not login, but a 'real' cvs command, so construct it,
        // set the options, and then connect to the server and execute it

        Command c = null;
        try {
            c = CommandFactory.getDefault().createCommand(command, args, ++commandIndex, globalOptions, localPath);
        }
        catch (IllegalArgumentException e) {
            stderr.println("Illegal argument: " + e.getMessage());
            return false;
        }
        
        if (files != null && c instanceof BasicCommand) {
            ((BasicCommand) c).setFiles(files);
        }

        String password = null;

        if (CVSRoot.METHOD_PSERVER.equals(root.getMethod())) {
            password = root.getPassword();
            if (password != null) {
                password = StandardScrambler.getInstance().scramble(password);
            } else {
                if (port > 0) root.setPort(port);
                password = lookupPassword(cvsRoot, root.toString(), stderr);
                if (password == null) {
                    password = StandardScrambler.getInstance().scramble(""); // an empty password
                }
            }
        }
        CVSCommand cvsCommand = new CVSCommand();
        cvsCommand.setGlobalOptions(globalOptions);
        cvsCommand.setRepository(root.getRepository());
        if (port > 0) {
            cvsCommand.port = port;
        }
        // the local path is just the path where we executed the
        // command. This is the case for command-line CVS but not
        // usually for GUI front-ends
        cvsCommand.setLocalPath(localPath);
        try {
            cvsCommand.connect(root, password);
            
            CVSListener list;
            if (c instanceof ListenerProvider)
            {
                list = ((ListenerProvider)c).createCVSListener(stdout, stderr);
            } else {
                list = new BasicListener(stdout, stderr);
            }
            cvsCommand.addListener(list);
            boolean status = cvsCommand.executeCommand(c, stderr);
            return status;
        }
        catch (AuthenticationException aex) {
            stderr.println(aex.getLocalizedMessage());
            return false;
        }
        catch (CommandAbortedException caex) {
            stderr.println("Error: " + caex);
            Thread.currentThread().interrupt();
            return false;
        }
        catch (Exception t) {
            stderr.println("Error: " + t);
            t.printStackTrace(stderr);
            return false;
        }
        finally {
            if (cvsCommand != null) {
                cvsCommand.close(stderr);
            }
        }
    }
    
    private static void printHelpOptions(PrintStream stdout) {
        String options = ResourceBundle.getBundle(CVSCommand.class.getPackage().getName()+".Bundle").getString("MSG_HelpOptions"); // NOI18N
        stdout.println(options);
    }
    
    private static void printHelpCommands(PrintStream stdout) {
        String msg = ResourceBundle.getBundle(CVSCommand.class.getPackage().getName()+".Bundle").getString("MSG_CVSCommands"); // NOI18N
        stdout.println(msg);
        CommandProvider[] providers = CommandFactory.getDefault().getCommandProviders();
        Arrays.sort(providers, new CommandProvidersComparator());
        int maxNameLength = 0;
        for (int i = 0; i < providers.length; i++) {
            int l = providers[i].getName().length();
            if (maxNameLength < l) {
                maxNameLength = l;
            }
        }
        maxNameLength += 2; // Two spaces from the longest name
        for (int i = 0; i < providers.length; i++) {
            stdout.print("\t"+providers[i].getName());
            char spaces[] = new char[maxNameLength - providers[i].getName().length()];
            Arrays.fill(spaces, ' ');
            stdout.print(new String(spaces));
            providers[i].printShortDescription(stdout);
            stdout.println();
        }
    }
    
    private static void printHelpSynonyms(PrintStream stdout) {
        String msg = ResourceBundle.getBundle(CVSCommand.class.getPackage().getName()+".Bundle").getString("MSG_CVSSynonyms"); // NOI18N
        stdout.println(msg);
        CommandProvider[] providers = CommandFactory.getDefault().getCommandProviders();
        Arrays.sort(providers, new CommandProvidersComparator());
        int maxNameLength = 0;
        for (int i = 0; i < providers.length; i++) {
            int l = providers[i].getName().length();
            if (maxNameLength < l) {
                maxNameLength = l;
            }
        }
        maxNameLength += 2; // Two spaces from the longest name
        for (int i = 0; i < providers.length; i++) {
            String[] synonyms = providers[i].getSynonyms();
            if (synonyms.length > 0) {
                stdout.print("\t"+providers[i].getName());
                char spaces[] = new char[maxNameLength - providers[i].getName().length()];
                Arrays.fill(spaces, ' ');
                stdout.print(new String(spaces));
                for (int j = 0; j < synonyms.length; j++) {
                    stdout.print(synonyms[j]+" ");
                }
                stdout.println();
            }
        }
    }
    
    private static void printHelp(int commandIndex, String[] args,
                                  PrintStream stdout, PrintStream stderr) {
        if (commandIndex >= args.length) {
            showUsage(stdout);
        } else {
            String cmdName = args[commandIndex];
            CommandProvider provider = CommandFactory.getDefault().getCommandProvider(cmdName);
            if (provider == null) {
                printUnknownCommand(cmdName, stderr);
            } else {
                provider.printLongDescription(stdout);
            }
        }
    }
    
    private static void printVersion(PrintStream stdout, PrintStream stderr) {
        String version = CVSCommand.class.getPackage().getSpecificationVersion();
        stdout.println("Java Concurrent Versions System (JavaCVS) "+version+" (client)");
    }
    
    private static void printUnknownCommand(String commandName, PrintStream out) {
        String msg = ResourceBundle.getBundle(CVSCommand.class.getPackage().getName()+".Bundle").getString("MSG_UnknownCommand"); // NOI18N
        out.println(MessageFormat.format(msg, new Object[] { commandName }));
        printHelpCommands(out);
    }
    
    private static final class CommandProvidersComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            if (!(o1 instanceof CommandProvider) || !(o2 instanceof CommandProvider)) {
                throw new IllegalArgumentException("Can not compare objects "+o1+" and "+o2);
            }
            return ((CommandProvider) o1).getName().compareTo(((CommandProvider) o2).getName());
        }
        
    }
    
}
