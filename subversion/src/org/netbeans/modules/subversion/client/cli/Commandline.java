/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.subversion.client.cli;

import java.util.logging.Logger;
import java.io.*;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.util.FileUtils;
import org.openide.util.Exceptions;

/**
 * Encapsulates svn shell process. 
 * 
 * @author Tomas Stupka
 */
class Commandline {
    
    private Process           cli;
    private BufferedReader    ctOutput;
    private BufferedReader    ctError;

    private String executable; 
    
    /**
     * Creates a new cleartool shell process.
     */
    public Commandline() {        
        executable = SvnModuleConfig.getDefault().getExecutableBinaryPath();
        if(executable == null || executable.trim().equals("")) {
            executable = "svn";
        }                        
    }        

    /**
     * Forcibly closes the cleartool console, just like using Ctrl-C.
     */
    public void interrupt() {
        try {
            destroy();
        } catch (IOException e) {
            // swallow, we are not interested
        }
    }
   
    private void destroy() throws IOException {
        if(cli != null) {
            cli.destroy();
        }
        if (ctOutput != null) {
            ctOutput.close();
        }
        if (ctError != null) {
            ctError.close();
        }                
        Logger.getLogger(Commandline.class.getName()).fine("cli: Process destroyed");
    }
    
    // XXX set env vars
    public void exec(SvnCommand command) throws IOException {        

        command.prepareCommand();
        String cmd = executable + " " + command.getStringCommand();
        Logger.getLogger(Commandline.class.getName()).fine("cli: Executing \"" + cmd + "\"");
        
        Logger.getLogger(Commandline.class.getName()).fine("cli: Creating process...");        
        try {
            cli = Runtime.getRuntime().exec(command.getCliArguments(executable));
            cli.waitFor();
            ctOutput = new BufferedReader(new InputStreamReader(cli.getInputStream()));
            ctError = new BufferedReader(new InputStreamReader(cli.getErrorStream()));
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            // XXX log and set invalid
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            // XXX log and set invalid
        }
        Logger.getLogger(Commandline.class.getName()).fine("cli: process created");
                
        command.commandStarted();

        try {
            // XXX need pumper
            while (isStreamReady(ctOutput)) {                
                String line = ctOutput.readLine();
                Logger.getLogger(Commandline.class.getName()).fine("cli: OUTPUT \"" + line + "\"");
                command.outputText(line);
            }
            while (isStreamReady(ctError)) {                
                String line = ctError.readLine();
                Logger.getLogger(Commandline.class.getName()).fine("cli: ERROR \"" + line + "\"");
                command.errorText(line);
            }
        } finally {
            command.commandFinished();
        }        
    }
    
    // return byte[]
    public InputStream execBinary(SvnCommand command) throws IOException {        

        command.prepareCommand();
        String cmd = executable + " " + command.getStringCommand();
        Logger.getLogger(Commandline.class.getName()).fine("cli: Executing \"" + cmd + "\"");
        
        Logger.getLogger(Commandline.class.getName()).fine("cli: Creating process...");        
        InputStream ret = null;        
        try {        
            try {
                cli = Runtime.getRuntime().exec(command.getCliArguments(executable));
                cli.waitFor();
                ctOutput = new BufferedReader(new InputStreamReader(cli.getInputStream()));
                ctError = new BufferedReader(new InputStreamReader(cli.getErrorStream()));
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                // XXX log and set invalid
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                // XXX log and set invalid
            }
            Logger.getLogger(Commandline.class.getName()).fine("cli: process created");

            command.commandStarted();
            

            // XXX need pumper
            // XXX merge with void exec()
            File tmp = File.createTempFile("svn-cat", null);
            FileUtils.copyStreamToFile(cli.getInputStream(), tmp);
            ret = new FileInputStream(tmp);
            
            while (isStreamReady(ctError)) {                
                String line = ctError.readLine();
                Logger.getLogger(Commandline.class.getName()).fine("cli: ERROR \"" + line + "\"");
                command.errorText(line);
            }
        } finally {
            command.commandFinished();
            if(ctOutput != null) ctOutput.close();
            if(ctError  != null) ctError.close();
        }        
        return ret;
    }

    private boolean isStreamReady(BufferedReader reader) throws IOException {
        if(reader == null) return false;
        if(reader.ready()) return true;
        Thread.yield();
        return reader.ready();
    }

    private void readAll(BufferedReader in) throws IOException {
        while (in.ready()) in.read();
    }
    
}
