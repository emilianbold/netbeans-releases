/*
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
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * InstallationWarmUp.java
 *
 * Created on September 21, 2005, 1:35 PM
 *
 */
package org.netbeans.modules.mobility.cldcplatform.catalog;

import java.awt.Dialog;
import java.io.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mobility.cldcplatform.wizard.InstallerIterator;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.execution.ExecutionEngine;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleInfo;
import org.openide.util.*;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * 
 */
public class PlatformCatalogAutoInstaller implements Runnable, FileChangeListener, LookupListener {

    private static final String TEMPLATE = "Templates/Services/Platforms/org-netbeans-api-java-Platform/javaplatform.xml";  //NOI18N
    private static final String STORAGE = "Services/Platforms/org-netbeans-api-java-Platform";  //NOI18N
    private static final int SCHEDULE_DELAY = 400;
    
    private static final RequestProcessor RP = new RequestProcessor(PlatformCatalogAutoInstaller.class);
    private final RequestProcessor.Task task = RP.create(this);
    private final FileObject fo;
    private final Lookup.Result res;

    /**
     * Creates a new instance of PlatformCatalogAutoInstaller
     */
    public PlatformCatalogAutoInstaller() {
        fo = FileUtil.getConfigFile("platform_installers"); //NOI18N
        assert fo != null;
        fo.addFileChangeListener(this);
        res = Lookup.getDefault().lookupResult(ModuleInfo.class);
        res.allInstances();
        res.addLookupListener(this);
        res.allInstances();
        FileUtil.getConfigFile("Modules").addFileChangeListener(this); //NOI18N
    }

    @Override
    public void run() {
        boolean onceMore = false;
        Enumeration en;
        fo.refresh(true);
        boolean success = false;
        while ((en = fo.getData(false)).hasMoreElements()) {
            try {
                final FileObject instFo = (FileObject) en.nextElement();
                final File inst = FileUtil.toFile(instFo);
                if (Utilities.getOperatingSystem() == Utilities.OS_LINUX || Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                    Runtime.getRuntime().exec(new String[]{"chmod", "+x", inst.getAbsolutePath()}).waitFor(); // NOI18N
                }
                final InputOutput io = IOProvider.getDefault().getIO(inst.getName(), false);
                io.setErrSeparated(false);
                io.setErrVisible(true);
                io.setInputVisible(true);
                io.setOutputVisible(true);
                io.setFocusTaken(true);
                success = ExecutionEngine.getDefault().execute(inst.getName(), new Runnable() {
                    @Override
                    public void run() {
                        try {
                            /*
                             * Fix for #131598 - java.io.IOException: Cannot run
                             * program
                             * "C:\Users\tester\.netbeans\dev\config\platform_installers\
                             * Sun-Java-Wireless-Toolkit252-for-CLDC-for-Windows_200710311754.exe":
                             * CreateProcess error=740, The requ
                             * 
                             * Due to #222517 changed to call cmd.exe on all Windows since NT, not just Vista.
                             */
                            String[] args;
                            if (Utilities.isWindows() && ((Utilities.getOperatingSystem() & (Utilities.OS_WIN95 | Utilities.OS_WIN98) ) == 0) ) {
                                args = new String[]{"cmd.exe", "/c", inst.getAbsolutePath()}; // NOI18N
                            } else {
                                args = new String[]{inst.getAbsolutePath()};
                            }
                            
                            final Process p = Runtime.getRuntime().exec(args);
                            RequestProcessor streamPumpsProcessor = new RequestProcessor("Platform Catalog Auto Installer Stream Pumps", 3); //NOI18N
                            streamPumpsProcessor.post(new StreamPumper(io.getIn(), new OutputStreamWriter(p.getOutputStream())));
                            streamPumpsProcessor.post(new StreamPumper(new InputStreamReader(p.getInputStream()), io.getOut()));
                            streamPumpsProcessor.post(new StreamPumper(new InputStreamReader(p.getErrorStream()), io.getErr()));
                            p.waitFor();
                        } catch (Exception e) {
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                }, null).result() == 0 | success;
                //ToDo ask if the installer should be deleted or moved somewhere
                if (!inst.delete()) {
                    instFo.delete();
                }
                fo.refresh(true);
            } catch (Exception ex) {
                Logger.getLogger(PlatformCatalogAutoInstaller.class.getName()).log(Level.WARNING,
                        "Exception occured during setting up the platform: ", ex); // NOI18N
                onceMore = true;
            }
        }
        if (success) {
            launchAddPlatformWizard();
        }
        if (onceMore) {
            task.schedule(SCHEDULE_DELAY);
        }
    }

    private void launchAddPlatformWizard() {
        try {
            final WizardDescriptor wiz = new WizardDescriptor(InstallerIterator.getDefault());
            final DataObject template = DataObject.find(FileUtil.getConfigFile(TEMPLATE));
            wiz.putProperty("targetTemplate", template); // NOI18N
            final DataFolder folder = DataFolder.findFolder(FileUtil.getConfigFile(STORAGE));
            wiz.putProperty("targetFolder", folder); // NOI18N
            wiz.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
            wiz.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
            wiz.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
            wiz.setTitle(NbBundle.getMessage(PlatformCatalogAutoInstaller.class, "CTL_AddPlatformTitle")); //NOI18N
            wiz.setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N
            final Dialog dlg = DialogDisplayer.getDefault().createDialog(wiz);
            try {
                dlg.setVisible(true);
                wiz.getValue();
            } finally {
                dlg.dispose();
            }
        } catch (DataObjectNotFoundException dfne) {
            ErrorManager.getDefault().notify(dfne);
        }
    }

    @Override
    public void fileFolderCreated(final FileEvent fe) {
    }

    @Override
    public void fileDataCreated(final FileEvent fe) {
        task.schedule(SCHEDULE_DELAY);
    }

    @Override
    public void fileChanged(final FileEvent fe) {
    }

    @Override
    public void fileDeleted(final FileEvent fe) {
    }

    @Override
    public void fileRenamed(final FileRenameEvent fe) {
    }

    @Override
    public void fileAttributeChanged(final FileAttributeEvent fe) {
    }

    @Override
    public void resultChanged(final LookupEvent ev) {
        task.schedule(SCHEDULE_DELAY);
    }

    private static class StreamPumper implements Runnable {
        final private Reader in;
        final private Writer out;

        public StreamPumper(Reader in, Writer out) {
            this.in = in;
            this.out = new BufferedWriter(out);
        }

        @Override
        public void run() {
            try {
                int i;
                while ((i = in.read()) >= 0) {
                    out.write(i);
                    out.flush();
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            } finally {
                try {
                    out.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            }
        }
    }
}
