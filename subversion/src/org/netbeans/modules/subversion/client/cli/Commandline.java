/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;

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
    private boolean canceled = false;
    
    /**
     * Creates a new cleartool shell process.
     */
    Commandline() {
        executable = SvnModuleConfig.getDefault().getExecutableBinaryPath();
        if(executable == null || executable.trim().equals("")) {
            executable = "svn";
        } else {
            File f = new File(executable);
            if(f.isDirectory()) {
                executable = executable + "/svn";
            } 
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
        canceled = true;
        if(cli != null) {
            try { cli.getErrorStream().close(); } catch (IOException iOException) { }
            try { cli.getInputStream().close(); } catch (IOException iOException) { }
            try { cli.getOutputStream().close(); } catch (IOException iOException) { }            
            cli.destroy();             
        }        
        Subversion.LOG.fine("cli: Process destroyed");
    }

    void exec(SvnCommand command) throws IOException {
        canceled = false;
        command.prepareCommand();        
        
        String cmd = executable + " " + command.getStringCommand();
        Subversion.LOG.fine("cli: Executing \"" + cmd + "\"");
        
        Subversion.LOG.fine("cli: Creating process...");        
        command.commandStarted();
        try {
            cli = Runtime.getRuntime().exec(command.getCliArguments(executable), getEnvVar());
            if(canceled) return;
            ctError = new BufferedReader(new InputStreamReader(cli.getErrorStream()));

            Subversion.LOG.fine("cli: process created");

            String line = null;                
            if(command.hasBinaryOutput()) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                int i = -1;
                if(canceled) return;
                Subversion.LOG.fine("cli: ready for binary OUTPUT \"");                
                while(!canceled && (i = cli.getInputStream().read()) != -1) {
                    b.write(i);
                }
                if(Subversion.LOG.isLoggable(Level.FINER)) Subversion.LOG.finer("cli: BIN OUTPUT \"" + (new String(b.toByteArray())) + "\"");
                command.output(b.toByteArray());
            } else {             
                if(canceled) return;
                Subversion.LOG.fine("cli: ready for OUTPUT \"");                     
                ctOutput = new BufferedReader(new InputStreamReader(cli.getInputStream()));
                while (!canceled && (line = ctOutput.readLine()) != null) {                                        
                    Subversion.LOG.fine("cli: OUTPUT \"" + line + "\"");
                    command.outputText(line);
                }    
            }
            
            while (!canceled && (line = ctError.readLine()) != null) {                                    
                Subversion.LOG.info("cli: ERROR \"" + line + "\"");
                command.errorText(line);
            }     
            if(canceled) return;
            cli.waitFor();
            command.commandCompleted(cli.exitValue());
        } catch (InterruptedException ie) {
            Subversion.LOG.log(Level.INFO, " command interrupted: [" + command.getStringCommand() + "]", ie);
        } catch (InterruptedIOException ie) {
            Subversion.LOG.log(Level.INFO, " command interrupted: [" + command.getStringCommand() + "]", ie);
        } catch (Throwable t) {
            if(canceled) {
                Subversion.LOG.fine(t.getMessage());
            } else {
                if(t instanceof IOException) {
                    throw (IOException) t;
                } else {
                    IOException ioe = new IOException();
                    ioe.initCause(t);
                    throw ioe;
                }
            }
        } finally {
            if(cli != null) {
                try { cli.getErrorStream().close(); } catch (IOException iOException) { }
                try { cli.getInputStream().close(); } catch (IOException iOException) { }
                try { cli.getOutputStream().close(); } catch (IOException iOException) { }
            }            
            ctError = null;
            ctOutput = null;
            Subversion.LOG.fine("cli: process finnished");            
            command.commandFinished();
        }        
    }    

    private String[] getEnvVar() {
        Map vars = System.getenv();            
        List ret = new ArrayList(vars.keySet().size());           
        for (Iterator it = vars.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();                
            if(key.equals("LC_ALL")) {
                ret.add("LC_ALL=");
            } else if(key.equals("LC_MESSAGES")) {
                ret.add("LC_MESSAGES=C");
            } else if(key.equals("LC_TIME")) {
                ret.add("LC_TIME=C");
            } else {
                ret.add(key + "=" + vars.get(key));                        
            }		                
        }                       
        if(!vars.containsKey("LC_ALL"))      ret.add("LC_ALL=");
        if(!vars.containsKey("LC_MESSAGES")) ret.add("LC_MESSAGES=C");
        if(!vars.containsKey("LC_TIME"))     ret.add("LC_TIME=C");            
        return (String[]) ret.toArray(new String[ret.size()]);
    }	    
    
}
