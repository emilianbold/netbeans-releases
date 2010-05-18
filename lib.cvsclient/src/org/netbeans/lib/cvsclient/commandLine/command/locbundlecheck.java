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
 * The Initial Developer of the Original Software is Milos Kleint.
 * Portions created by Milos Kleint are Copyright (C) 2000.
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

 * Contributor(s): Milos Kleint.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.commandLine.command;

import java.io.*;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.command.annotate.*;
import org.netbeans.lib.cvsclient.commandLine.*;
import org.netbeans.lib.cvsclient.commandLine.ListenerProvider;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.CVSListener;

/**
 * The locbundlecheck command
 * @author  Milos Kleint
 */
public class locbundlecheck extends CVSAdapter implements CommandProvider {
    
    /**
     * A constructor that is used to create the CommandProvider.
     */
    public locbundlecheck() {
    }
    
    public String getName() {
        return "locbundlecheck"; // NOI18N
    }
    
    public String[] getSynonyms() {
        return new String[] { "lbch", "lbcheck" }; // NOI18N
    }
    
    public String getUsage() {
        return ResourceBundle.getBundle(CommandProvider.class.getPackage().getName()+".Bundle").getString("locbundlecheck.usage"); // NOI18N
    }
    
    public void printShortDescription(PrintStream out) {
        String msg = ResourceBundle.getBundle(CommandProvider.class.getPackage().getName()+".Bundle").getString("locbundlecheck.shortDescription"); // NOI18N
        out.print(msg);
    }
    
    public void printLongDescription(PrintStream out) {
        String msg = ResourceBundle.getBundle(CommandProvider.class.getPackage().getName()+".Bundle").getString("locbundlecheck.longDescription"); // NOI18N
        out.println(msg);
    }
    
    public Command createCommand(String[] args, int index, GlobalOptions gopt, String workDir) {
        LocBundleAnnotateCommand command = new LocBundleAnnotateCommand();
        final String getOptString = command.getOptString();
        GetOpt go = new GetOpt(args, getOptString + "i:");
        int ch = -1;
        go.optIndexSet(index);
        boolean usagePrint = false;
        String arg;
        String localization = null;
        while ((ch = go.getopt()) != go.optEOF) {
            if (ch == 'i') {
                localization = go.optArgGet();
                command.setLocalization(localization);
            } else {
                boolean ok = command.setCVSCommand((char)ch, go.optArgGet());
                if (!ok) {
                    usagePrint = true;
                }
            }
        }
        if (usagePrint || localization == null) {
            throw new IllegalArgumentException(getUsage());
        }
        int fileArgsIndex = go.optIndexGet();
        // test if we have been passed any file arguments
        if (fileArgsIndex < args.length) {
            Collection fls = new ArrayList();
            
            // send the arguments as absolute paths
            if (workDir == null) {
                workDir = System.getProperty("user.dir");
            }
            command.setWorkDir(workDir);
            File workingDir = new File(workDir);
            for (int i = fileArgsIndex; i < args.length; i++) {
                File fl = new File(workingDir, args[i]);
//                System.out.println("file=" + fl);
                if (fl.exists() && fl.isDirectory()) {
                    addFilesInDir(fls, fl, localization);
                }
                else if (fl.exists() && fl.getName().endsWith(".properties"))
                {
                    addFiles(fls, fl, localization);
                }
                else 
                {
                    throw new IllegalArgumentException();
                }
            }
            if (fls.size() > 0)
            {
                File[] fileArgs = new File[fls.size()];
                fileArgs = (File[])fls.toArray(fileArgs);
                command.setFiles(fileArgs);
            } else {
                throw new IllegalArgumentException(ResourceBundle.getBundle("org/netbeans/lib/cvsclient/commandLine/command/Bundle").getString("locbundlecheck.no_file_spec"));
            }
        }
        return command;
    }
    
    private static void addFiles(Collection fileList, File origFile, String localization) {
        String origPath = origFile.getAbsolutePath();
        String enarg = origPath.substring(0, origPath.length() - ".properties".length()) + "_" + localization + ".properties";
        //System.out.println("enarg=" + enarg);
        File addfl = new File(enarg);
        fileList.add(origFile);
        if (addfl.exists()) {
            fileList.add(addfl);
        } else {
            //TODO
        }
    }
    
    private static void addFilesInDir(Collection fileList, File origFile, String localization) {
        File[] files = origFile.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].exists() && files[i].isDirectory())
                {
                    addFilesInDir(fileList, files[i], localization);
                } else if (files[i].exists() && "Bundle.properties".equals(files[i].getName())) {
                    addFiles(fileList, files[i], localization);
                }
            }
        }
    }
    
    private PrintStream out;
    private PrintStream err;
    private int realEnd = 0;
    
    private HashMap originalBundles;
    private HashMap localizedBundles;
    private String local;
    private String workDir;
    
    /**
     * A constructor that is used to create the CVSAdapter.
     */
    locbundlecheck(PrintStream stdout, PrintStream stderr, String localization, String workDir)
    {
        out = stdout;
        err = stderr;
        originalBundles = new HashMap();
        localizedBundles = new HashMap();
        local = localization;
        this.workDir = workDir;
    }

    
    public void fileInfoGenerated(org.netbeans.lib.cvsclient.event.FileInfoEvent e) {
//        out.println("annotated " + e.getInfoContainer().getFile());
        FileInfoContainer cont = e.getInfoContainer();
        if (cont.getFile().getName().indexOf("_" + local) >= 0) {
            localizedBundles.put(cont.getFile().getAbsolutePath(), cont);
        } else {
            originalBundles.put(cont.getFile().getAbsolutePath(), cont);
        }
//        out.println("orig size=" + originalBundles.keySet().size() + " loc size=" + localizedBundles.keySet().size());
        if (realEnd == 2) {
            // generate output.
//            out.println("generating output....");
            generateOutput();
        }
        
    }
    
    
    public void commandTerminated(org.netbeans.lib.cvsclient.event.TerminationEvent e) {
        if (realEnd == 0) {
            // now the event is triggered because of the validresponses request
            realEnd = 1;
            return;
        }
        realEnd = 2;
        // the second time it's the real end. waiting for the last info object to be received.
//        out.println("finish=" + e.isError());
    }
    
    private void generateOutput() {
        Iterator it = originalBundles.keySet().iterator();
        while (it.hasNext())
        {
            String origPath = (String)it.next();
            int dotIndex = origPath.lastIndexOf(".");
            if (dotIndex < 0) {
                throw new IllegalStateException(ResourceBundle.getBundle("org/netbeans/lib/cvsclient/commandLine/command/Bundle").getString("locbundlecheck.illegal_state"));
            }
            String locPath = origPath.substring(0, dotIndex) + "_" + local + origPath.substring(dotIndex);
//            System.out.println("locpath=" + locPath);
            AnnotateInformation origInfo = (AnnotateInformation)originalBundles.get(origPath);
            AnnotateInformation locInfo = (AnnotateInformation)localizedBundles.get(locPath);
            if (locInfo == null) {
                out.println(MessageFormat.format(ResourceBundle.getBundle("org/netbeans/lib/cvsclient/commandLine/command/Bundle").getString("locbundlecheck.noLocalizedFile"), 
                            new Object[] {origPath}));
                continue;
            }
            // remove from locl bundles to figure out what was removed in the original..
            localizedBundles.remove(locPath);
            HashMap origPropMap = createPropMap(origInfo);
            HashMap locPropMap = createPropMap(locInfo);
            String printFile = origPath;
            if (origPath.startsWith(workDir)) {
                printFile = origPath.substring(workDir.length());
                if (printFile.startsWith("/") || printFile.startsWith("\\") ) {
                    printFile = printFile.substring(1);
                }
            }
            out.println(MessageFormat.format(ResourceBundle.getBundle("org/netbeans/lib/cvsclient/commandLine/command/Bundle").getString("locbundlecheck.File"), 
                        new Object[] {printFile})); 
            Iterator propIt = origPropMap.keySet().iterator();
            while (propIt.hasNext()) {
                String prop = (String)propIt.next();
                AnnotateLine origLine = (AnnotateLine)origPropMap.get(prop);
                AnnotateLine locLine = (AnnotateLine)locPropMap.get(prop);
                if (locLine == null) {
                    out.println(MessageFormat.format(ResourceBundle.getBundle("org/netbeans/lib/cvsclient/commandLine/command/Bundle").getString("locbundlecheck.propMissing"), 
                                new Object[] {prop}));
                    continue;
                }
//                System.out.println("prop=" + prop);
//                System.out.println("orig date:" + origLine.getDate());
//                System.out.println("loc date:" + locLine.getDate());
                if (origLine.getDate().compareTo(locLine.getDate()) > 0) {
                    out.println(MessageFormat.format(ResourceBundle.getBundle("org/netbeans/lib/cvsclient/commandLine/command/Bundle").getString("locbundlecheck.prop_updated"), 
                                new Object[] {prop}));
                }
            }
            
        }
        if (localizedBundles.size() > 0) {
            Iterator locIt = localizedBundles.keySet().iterator();
            while (locIt.hasNext()) {
                String prop = (String)locIt.next();
                out.println(MessageFormat.format(ResourceBundle.getBundle("org/netbeans/lib/cvsclient/commandLine/command/Bundle").getString("locbundlecheck.prop_removed"), 
                            new Object[] {prop}));
            }
        }
    }
    
    private HashMap createPropMap(AnnotateInformation info) {
        HashMap propMap = new HashMap();
        AnnotateLine line = info.getFirstLine();
        while (line != null) {
            String content = line.getContent();
            if (content.startsWith("#")) {
                // ignore commented lines.
                line = info.getNextLine();                
                continue;
            }
            int index = content.indexOf('=');
            if (index > 0) {
                String key = content.substring(0, index);
                propMap.put(key, line);
            } else {
                //TODO.. for properties that span across multiple lines, one should take all lines into account
            }
            line = info.getNextLine();
        }
        return propMap;
    }
    
    private static class LocBundleAnnotateCommand extends AnnotateCommand implements ListenerProvider {
        private String loc;
        private String workDir;
        
        public CVSListener createCVSListener(PrintStream stdout, PrintStream stderr) {
            return new locbundlecheck(stdout, stderr, loc, workDir);
        }
        
        public void setLocalization(String loc) {
            this.loc = loc;
        }
        
        public void setWorkDir(String dir) {
            workDir = dir;
        }
        
    }    
    
    
    
}
