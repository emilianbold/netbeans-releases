/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.keyring.kde;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.keyring.KeyringProvider;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author psychollek
 */
@ServiceProvider(service=KeyringProvider.class, position=99)
public class KWalletProvider implements KeyringProvider{

    private static final Logger logger = Logger.getLogger(KWalletProvider.class.getName());
    private char[] handler = "0".toCharArray();

    @Override
    public boolean enabled(){
        if (Boolean.getBoolean("netbeans.keyring.no.native")) {
            logger.fine("native keyring integration disabled");
            return false;
        }
        if (new String(runCommand("isEnabled")).equals("true")){
            return updateHandler();
        }
        return false;
    };

    @Override
    public char[] read(String key){
        runCommand("close", runCommand("localWallet"), "true".toCharArray() );
        if (updateHandler()){
            char[] pwd = runCommand("readPassword", handler, getApplicationName(), key.toCharArray(), getApplicationName(true));
            runCommand("close", runCommand("localWallet"), "true".toCharArray() );
            return pwd.length > 0 ? pwd : null;
        }
        throw new KwalletException("read");
    };

    @Override
    public void save(String key, char[] password, String description){
        //description is forgoten ! kdewallet dosen't have any facility to store
        //it by default and I don't want to do it by adding new fields to kwallet
        runCommand("close", runCommand("localWallet"), "true".toCharArray() );
        if (updateHandler()){
            if (new String(runCommand("writePassword", handler , getApplicationName()
                    , key.toCharArray(), password , getApplicationName(true))
                    ).equals("-1")){
                throw new KwalletException("save");
            }
            runCommand("close", runCommand("localWallet"), "true".toCharArray() );
            return;
        }
        throw new KwalletException("save");
    };

    @Override
    public void delete(String key){
        runCommand("close", runCommand("localWallet"), "true".toCharArray() );
        if (updateHandler()){
            if (new String(runCommand("removeEntry" ,handler,
            getApplicationName() , key.toCharArray() , getApplicationName(true)
            )).equals("-1")){
                throw new KwalletException("delete");
            }
            runCommand("close", runCommand("localWallet"), "true".toCharArray() );
            return;
        }
        throw new KwalletException("delete");
    };

    private boolean updateHandler(){
        handler = new String(handler).equals("")? "0".toCharArray() : handler;
        if(new String(runCommand("isOpen",handler)).equals("true")){
            return true;
        }
        char[] localWallet = runCommand("localWallet");
        handler = runCommand("open", localWallet , "0".toCharArray() , getApplicationName(true));
        if(!(new String(handler)).equals("-1")){
            return true;
        }
        return false;
    }

    private char[] runCommand(String command,char[]... commandArgs){
        String[] argv = new String[commandArgs.length+4];
        argv[0] = "qdbus";
        argv[1] = "org.kde.kwalletd";
        argv[2] = "/modules/kwalletd";
        argv[3] = "org.kde.KWallet."+command;
        for (int i = 0; i < commandArgs.length; i++) {
            //unfortunatelly I cannot pass char[] to the exec in any way - so this poses a security issue with passwords in String() !
            //TODO: find a way to avoid changing char[] into String
            argv[i+4] = new String(commandArgs[i]);
        }

        Runtime rt = Runtime.getRuntime();
        String retVal = "";
        try {

            Process pr = rt.exec(argv);
            
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line;
            while((line = input.readLine()) != null) {
                if (!retVal.equals("")){
                    retVal = retVal.concat("\n");
                }
                retVal = retVal.concat(line);
            }
            input.close();
            input = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            while((line = input.readLine()) != null) {
                if (!retVal.equals("")){
                    retVal = retVal.concat("\n");
                }
                retVal = retVal.concat(line);
            }
            input.close();


            int exitVal = pr.waitFor();
            if(exitVal!=0){
                logger.log(Level.FINE,"application exit with code "+exitVal+" for commandString: "+Arrays.toString(argv));
            }

        } catch (InterruptedException ex) {
            logger.log(Level.FINE,
                    "exception thrown while invoking the command \""+Arrays.toString(argv)+"\"",
                    ex);
        } catch (IOException ex) {
            logger.log(Level.FINE,
                    "exception thrown while invoking the command \""+Arrays.toString(argv)+"\"",
                    ex);
        }
        return retVal.trim().toCharArray();
    }

    private char[] getApplicationName(){
        return getApplicationName(false);
    }

    private char[] getApplicationName(boolean version){
        String appName;
        try {
            appName = MessageFormat.format(NbBundle.getBundle("org.netbeans.core.windows.view.ui.Bundle").getString("CTL_MainWindow_Title_No_Project"),version ? System.getProperty("netbeans.buildnumber"):"");
        } catch (MissingResourceException x) {
            appName = "NetBeans"+(version? " "+System.getProperty("netbeans.buildnumber"):"");
        }
        return appName.toCharArray();
    }

    public class KwalletException extends RuntimeException{

        public KwalletException(String desc) {
            super("error while trying to access KWallet, during "+desc);
        }

    }

}
