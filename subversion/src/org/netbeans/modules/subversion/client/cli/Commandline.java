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
    
    /**
     * Creates a new cleartool shell process.
     */
    Commandline() {
        // XXX add path heuristics for mac, bubuntu & co.
        executable = SvnModuleConfig.getDefault().getExecutableBinaryPath();
        if(executable == null || executable.trim().equals("")) {
            executable = "svn";
        } else {
            executable = executable + "/svn";
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
        Subversion.LOG.fine("cli: Process destroyed");
    }
    
    public void exec(SvnCommand command) throws IOException {        

        command.prepareCommand();        
        
        String cmd = executable + " " + command.getStringCommand();
        Subversion.LOG.fine("cli: Executing \"" + cmd + "\"");
        
        Subversion.LOG.fine("cli: Creating process...");        
        command.commandStarted();
        
        try {
            cli = Runtime.getRuntime().exec(command.getCliArguments(executable), getEnvVar());
            
            ctOutput = new BufferedReader(new InputStreamReader(cli.getInputStream()));
            ctError = new BufferedReader(new InputStreamReader(cli.getErrorStream()));
        } catch (IOException ex) {
            Logger.getLogger(Commandline.class.getName()).log(Level.WARNING, null, ex);
            throw ex;
        }
        Subversion.LOG.fine("cli: process created");

        try {
            String line = null;                
            if(command.hasBinaryOutput()) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                int i = -1;
                while((i = ctOutput.read()) != -1) {
                    b.write(i);
                }
                command.output(b.toByteArray());
            } else {                    
                while ((line = ctOutput.readLine()) != null) {                                        
                    Subversion.LOG.fine("cli: OUTPUT \"" + line + "\"");
                    command.outputText(line);
                }    
            }
            while ((line = ctError.readLine()) != null) {                                    
                Subversion.LOG.info("cli: ERROR \"" + line + "\"");
                command.errorText(line);
            }                            

            try {
                cli.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(Commandline.class.getName()).log(Level.WARNING, null, ex);
                throw new IOException(ex.getMessage());
            }
            
        } finally {
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
