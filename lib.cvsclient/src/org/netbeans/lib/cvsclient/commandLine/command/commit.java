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

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.commandLine.command;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.commit.*;
import org.netbeans.lib.cvsclient.commandLine.*;

/**
 * Implements the commit command
 * @author  Robert Greig
 */
public class commit extends AbstractCommandProvider {

    public String[] getSynonyms() {
        return new String[] { "ci", "com", "put" }; // NOI18N
    }
    
    /**
     * @param editor The editor passed in via -e global option.
     */
    private static String getEditorProcess(String editor) {
        if (editor == null) {
            if (System.getProperty("os.name").startsWith("Windows")) {
                editor = "notepad.exe";
            }
            else {
                editor = null;//"vi"; - do not use 'vi'. It's not executed correctly in the terminal.
            }
            editor = System.getProperty("cvs.editor", editor);
        }
        return editor;
    }

    private static File createTempFile(File[] args, File tmpDir) throws IOException {
        File template = null;
        BufferedReader templateReader = null;
        BufferedWriter writer = null;
        try {
            File tempFile = File.createTempFile("cvsTemplate", "txt", tmpDir);
            writer = new BufferedWriter(new FileWriter(tempFile));

            if (args != null && args.length > 0) {
                // Get the template file from the first argument
                template = new File(args[0].getParentFile(), "CVS" +
                                                             File.separator +
                                                             "Template");
                if (template.exists()) {
                    templateReader = new BufferedReader(
                            new FileReader(template));

                    String line = null;
                    while ((line = templateReader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
            writer.write("CVS: ----------------------------------------------------------------------");
            writer.newLine();
            writer.write("CVS: Enter Log.  Lines beginning with `CVS:' are removed automatically");
            writer.newLine();
            writer.write("CVS: ");
            writer.newLine();
            // TODO: fix this bit
            writer.write("CVS: Committing in .");
            writer.newLine();
            writer.write("CVS: ");
            writer.newLine();
            writer.write("CVS: Modified Files:");
            writer.newLine();
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    // TODO: don't write out the full path of files
                    writer.write("CVS:  " + args[i].getPath());
                }
            }
            writer.write("CVS: ----------------------------------------------------------------------");
            writer.newLine();
            return tempFile;
        }
        finally {
            if (templateReader != null) {
                templateReader.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static String createMessage(File[] args, GlobalOptions gopt) {
        File temp = null;
        BufferedReader reader = null;
        try {
            temp = createTempFile(args, gopt.getTempDir());
            // we now have a temp file with the appropriate text in it. Just
            // get the appropriate process to edit it.
            // TODO maybe make this more sophisticated, e.g. the cvs.editor
            // property allows certain fields to specify arguments, where the
            // actual filename goes etc.
            String editorProcess = getEditorProcess(gopt.getEditor());
            if (editorProcess == null) return null;
            final Process proc = Runtime.getRuntime().
                    exec(new String[] { editorProcess, temp.getPath() });
            int returnCode = -1;

            try {
                returnCode = proc.waitFor();
            }
            catch (InterruptedException e) {
                // So somebody else interrupted us.
            }

            if (returnCode != 0) {
                return null;
            }
            else {
                // TODO: need to add the bit that tests whether the file
                // has been changed so that we can bring up the abort etc.
                // message just like real CVS.
                reader = new BufferedReader(new FileReader(temp));
                String line;
                StringBuffer message = new StringBuffer((int)temp.length());
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("CVS:")) {
                        message.append(line);
                        message.append('\n');
                    }
                }
                return message.toString();
            }
        }
        catch (IOException e) {
            // OK something went wrong so just don't bother returning a
            // message
            System.err.println("Error: " + e);
            e.printStackTrace();
            return null;
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (temp != null) {
                    temp.delete();
                }
            }
            catch (Exception e) {
                // we're clearly in real trouble so just dump the
                // exception to standard err and get out of here
                System.err.println("Fatal error: " + e);
                e.printStackTrace();
            }
        }
    }

    public Command createCommand(String[] args, int index, GlobalOptions gopt, String workDir) {
        CommitCommand command = new CommitCommand();
        command.setBuilder(null);
        final String getOptString = command.getOptString();
        GetOpt go = new GetOpt(args, getOptString);
        int ch = -1;
        go.optIndexSet(index);
        boolean usagePrint = false;
        while ((ch = go.getopt()) != go.optEOF) {
            boolean ok = command.setCVSCommand((char)ch, go.optArgGet());
            if (!ok) {
                usagePrint = true;
            }
        }
        if (usagePrint) {
            throw new IllegalArgumentException(getUsage());
        }

        int fileArgsIndex = go.optIndexGet();

        File[] fileArgs = null;

        // test if we have been passed any file arguments
        if (fileArgsIndex < args.length) {
            fileArgs = new File[args.length - fileArgsIndex];
            // send the arguments as absolute paths
            if (workDir == null) {
                workDir = System.getProperty("user.dir");
            }
            File workingDir = new File(workDir);
            for (int i = fileArgsIndex; i < args.length; i++) {
                fileArgs[i - fileArgsIndex] = new File(workingDir, args[i]);
            }
            command.setFiles(fileArgs);
        }

        // now only bring up the editor if the message has not been set using
        // the -m option
        if (command.getMessage() == null && command.getLogMessageFromFile() == null) {
            String message = createMessage(fileArgs, gopt);
            if (message == null) {
                throw new IllegalArgumentException(java.util.ResourceBundle.getBundle(commit.class.getPackage().getName()+".Bundle").getString("commit.messageNotSpecified"));
            }
            command.setMessage(message);
        }

        return command;
    }
    
}
