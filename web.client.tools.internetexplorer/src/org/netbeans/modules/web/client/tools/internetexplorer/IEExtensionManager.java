/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.client.tools.internetexplorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.web.client.tools.common.launcher.Launcher;
import org.netbeans.modules.web.client.tools.common.launcher.Launcher.LaunchDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 *
 * The Internet Explorer extension installation manager
 *  
 * @author jdeva
 */
public class IEExtensionManager {
    //Microsoft Script Debugger download URL
    private static final String MS_SCRIPT_DEBUGGER_URI = 
            "http://www.microsoft.com/downloads/details.aspx?FamilyID=2f465be0-94fd-4569-b3c4-dffdf19ccd99"; // NOI18N
    
    //Registry key strings
    private static final String PDM_REGISTRY_KEY = "HKLM\\SOFTWARE\\Classes\\CLSID\\{78a51822-51f4-11d0-8f20-00805f2cd064}";    // NOI18N
    private static final String MDM_REGISTRY_KEY = "HKLM\\SOFTWARE\\Classes\\CLSID\\{0C0A3666-30C9-11D0-8F20-00805F2CD064}";    // NOI18N
    private static final String BHO_REGISTRY_KEY = "HKLM\\SOFTWARE\\Classes\\CLSID\\{25CE9541-A839-46B4-81C6-1FAE46AD2EDE}";    // NOI18N
    private static final String BHO_PROC32_REGISTRY_KEY = BHO_REGISTRY_KEY + "\\InprocServer32";    // NOI18N    
    
    //Reg.exe 
    private static final String REG_EXE = "reg.exe";    // NOI18N
    private static final String REG_OPERATION = "query"; // NOI18N
    private static final String REG_OPTION = "/ve"; // NOI18N
    //Regsvr32.exe
    private static String REGSERVER_EXE = "regsvr32.exe";   // NOI18N
    private static String REGSERVER_OPTION = "/s";  // NOI18N
    
    private static final String BHO_RELATIVE_PATH = "native/NetBeansExtension.dll"; // NOI18N
    private static final String MODULE_CODEBASE = "org.netbeans.modules.web.client.tools.internetexplorer"; // NOI18N
    
    public static boolean checkRequiredComponents(HtmlBrowser.Factory browser) {
        try {
            //Check for MDM and PDM in the reqistry using reg.exe
            while(!queryRegistry(MDM_REGISTRY_KEY) || !queryRegistry(PDM_REGISTRY_KEY)) {
                //Wait for user to install the script debugger and press ok
                launchBrowser(browser, MS_SCRIPT_DEBUGGER_URI);
                if(!displayScriptDebuggerInstallDialog()) {
                    return false;
                }
            }

            File bhoFile = InstalledFileLocator.getDefault().locate(BHO_RELATIVE_PATH, MODULE_CODEBASE, false);
            String bhoFilePath = bhoFile.getCanonicalPath();
            //Check for Netbeans BHO
            if(!queryForBHO(bhoFilePath)) {
                //Wait for user to agree for registering our BHO
                if(displayBHORegisterDialog()) {
                    if (registerBHO(bhoFilePath))
                        return true;
                    //registration fails
                    handleFailedRegistration();
                }
            } else {
                return true;
            }
        } catch (IOException ioe) {
            Log.getLogger().log(Level.INFO, ioe.getLocalizedMessage());
        }
        return false;
    }
    
    private static void handleFailedRegistration() {
        final String message = NbBundle.getMessage(IEExtensionManager.class, "UNABLE_TO_REGISTER");                    
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                String os = System.getProperty("os.name").toLowerCase();
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message (
                        (os != null && os.contains("vista"))?
                            message + "\n" + NbBundle.getMessage(IEExtensionManager.class, "UNABLE_TO_REGISTER_EXT"):
                            message, NotifyDescriptor.ERROR_MESSAGE));
            }
        });        
    }

    private static boolean queryForBHO(String bhoFilePath) {
        //Query for BHO registry key and then check the path
        if(queryRegistry(BHO_REGISTRY_KEY)) {
            String result = execute(new String[]{REG_EXE, REG_OPERATION, BHO_PROC32_REGISTRY_KEY, REG_OPTION});
            if(result.contains(bhoFilePath)){
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean queryRegistry(String regKey) {
        return execute(new String[]{REG_EXE, REG_OPERATION, regKey, REG_OPTION}) != null ? true : false;
    }
    
    private static boolean registerBHO(String dllName){
        return execute(new String[]{REGSERVER_EXE, REGSERVER_OPTION, dllName}) != null ? true : false;
    }
    
    private static String execute(String[] args) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            //Clear input/error streams
            String result = readStream(process.getInputStream());
            //Wait and return true if exit value is 0
            if(process.waitFor() == 0) {
                return result;
            }
        }catch(IOException ioe) {
            Log.getLogger().log(Level.INFO, ioe.getLocalizedMessage());
        }catch(InterruptedException ie) {
            Log.getLogger().log(Level.INFO, ie.getLocalizedMessage());
        }
        return null;
    }
    
    private static String readStream(InputStream is) {
        StringBuffer result = new StringBuffer();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        try {
            String line;
            do {
                line = br.readLine();
                if(line != null) {
                    result.append(line);
                }
            }while(line != null);
        } catch (IOException ioe) {
            Log.getLogger().log(Level.INFO, ioe.getLocalizedMessage());            
        }
        return result.toString();
    }
    
    private static boolean displayScriptDebuggerInstallDialog() {
        String dialogText = NbBundle.getMessage(IEExtensionManager.class, "SCRIPT_DEBUGGER_INSTALL_TEXT");
        String dialogTitle = NbBundle.getMessage(IEExtensionManager.class, "SCRIPT_DEBUGGER_INSTALL_TITLE");

        return displayConfirmationDialog(dialogText, dialogTitle);
    }    
    
    private static boolean displayBHORegisterDialog() {
        String dialogText = NbBundle.getMessage(IEExtensionManager.class, "BHO_REGISTER_TEXT");
        String dialogTitle = NbBundle.getMessage(IEExtensionManager.class, "BHO_REGISTER_TITLE");

         NotifyDescriptor d =
             new NotifyDescriptor.Confirmation(dialogText, dialogTitle, NotifyDescriptor.OK_CANCEL_OPTION);
         return DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION;
    }
    
   private static boolean displayConfirmationDialog(String dialogText, String dialogTitle) {
        final JTextArea messageTextArea = new JTextArea(dialogText);
        messageTextArea.setColumns(65);
        messageTextArea.setEditable(false);
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setBackground(UIManager.getColor("Panel.background")); // NOI18N

        NotifyDescriptor d =
            new NotifyDescriptor.Confirmation(new JScrollPane(messageTextArea), dialogTitle, NotifyDescriptor.OK_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION;
   }     

    // XXX Copied from JSAbstractDebugger
    protected static String getBrowserExecutable(HtmlBrowser.Factory browser) {
        if (browser != null) {
            try {
                Method method = browser.getClass().getMethod("getBrowserExecutable");
                NbProcessDescriptor processDescriptor = (NbProcessDescriptor) method.invoke(browser);
                return processDescriptor.getProcessName();
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return "firefox"; // NOI18N

    }

    private static void launchBrowser(HtmlBrowser.Factory browser, String uri) throws IOException {
        LaunchDescriptor launchDescriptor = new LaunchDescriptor(getBrowserExecutable(browser));
        List<String> uriList = new ArrayList<String>();
        uriList.add(uri);
        launchDescriptor.setURI(uriList);
        Launcher.launch(launchDescriptor);
    }    
}
    
