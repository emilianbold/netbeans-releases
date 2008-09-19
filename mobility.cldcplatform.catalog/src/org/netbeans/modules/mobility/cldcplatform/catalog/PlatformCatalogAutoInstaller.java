/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import org.netbeans.modules.mobility.cldcplatform.wizard.InstallerIterator;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.execution.ExecutionEngine;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Adam Sotona
 */
public class PlatformCatalogAutoInstaller implements Runnable, FileChangeListener, LookupListener {
    
    private boolean running = false;
    private final FileObject fo;
    private final Lookup.Result res;
    private static final String TEMPLATE = "Templates/Services/Platforms/org-netbeans-api-java-Platform/javaplatform.xml";  //NOI18N
    private static final String STORAGE = "Services/Platforms/org-netbeans-api-java-Platform";  //NOI18N
    
    /**
     * Creates a new instance of PlatformCatalogAutoInstaller
     */
    public PlatformCatalogAutoInstaller() {
        fo = Repository.getDefault().getDefaultFileSystem().findResource("platform_installers"); //NOI18N
        assert fo != null;
        fo.addFileChangeListener(this);
        res = Lookup.getDefault().lookup(new Lookup.Template<ModuleInfo>(ModuleInfo.class));
        res.allInstances();
        res.addLookupListener(this);
        res.allInstances();
        Repository.getDefault().getDefaultFileSystem().findResource("Modules").addFileChangeListener(this); //NOI18N
    }
    
    public void run() {
        boolean onceMore = false;
        synchronized (this) {
            if (running) return;
            running = true;
        }
        Enumeration en;
        fo.refresh(true);
        boolean success = false;
        while ((en = fo.getData(false)).hasMoreElements()) try {
            final FileObject instFo = (FileObject)en.nextElement();
            final File inst = FileUtil.toFile(instFo);
            if (Utilities.getOperatingSystem() == Utilities.OS_LINUX || Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                Runtime.getRuntime().exec(new String[] {"chmod", "+x", inst.getAbsolutePath()}).waitFor();// NOI18N,
            }
            final InputOutput io = IOProvider.getDefault().getIO(inst.getName(), false);
            io.setErrSeparated(false);
            io.setErrVisible(true);
            io.setInputVisible(true);
            io.setOutputVisible(true);
            io.setFocusTaken(true);
            success = ExecutionEngine.getDefault().execute(inst.getName(), new Runnable() {
                public void run() {
                    try {
                        /*
                         *Fix for #131598 - java.io.IOException: Cannot run program
                         * "C:\Users\tester\.netbeans\dev\config\platform_installers\
                         * Sun-Java-Wireless-Toolkit252-for-CLDC-for-Windows_200710311754.exe":
                         * CreateProcess error=740, The requ
                         */
                        String[] args ;
                        if ( Utilities.getOperatingSystem() == Utilities.OS_WINVISTA ){
                            args = new String[] { "cmd.exe" ,"/c" , inst.getAbsolutePath()}; // NOI18N
                        }
                        else {
                            args = new String[] {inst.getAbsolutePath()};
                        }
                        final Process p = Runtime.getRuntime().exec(  args);
                        RequestProcessor.getDefault().post(new StreamPumper(io.getIn(), new OutputStreamWriter(p.getOutputStream())));
                        RequestProcessor.getDefault().post(new StreamPumper(new InputStreamReader(p.getInputStream()), io.getOut()));
                        RequestProcessor.getDefault().post(new StreamPumper(new InputStreamReader(p.getErrorStream()), io.getErr()));
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
            ex.printStackTrace();
            onceMore = true;
        }
        if (success) launchAddPlatformWizard();
        if (onceMore) {
            RequestProcessor.getDefault().post(this, 400);
        }
        synchronized (this) {
            running = false;
        }
    }
    
    private void launchAddPlatformWizard() {
        try {
            final WizardDescriptor wiz = new WizardDescriptor(InstallerIterator.getDefault());
            final DataObject template = DataObject.find(
                    Repository.getDefault().getDefaultFileSystem().findResource(TEMPLATE));
            wiz.putProperty("targetTemplate", template);    //NOI18N
            final DataFolder folder = DataFolder.findFolder(
                    Repository.getDefault().getDefaultFileSystem().findResource(STORAGE));
            wiz.putProperty("targetFolder",folder); //NOI18N
            wiz.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
            wiz.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
            wiz.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
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
    
    public void fileFolderCreated(@SuppressWarnings("unused")
	final FileEvent fe) {}
    
    public void fileDataCreated(@SuppressWarnings("unused")
	final FileEvent fe) {
        if (!running) RequestProcessor.getDefault().post(this);
    }
    
    public void fileChanged(@SuppressWarnings("unused")
	final FileEvent fe) {}
    
    public void fileDeleted(@SuppressWarnings("unused")
	final FileEvent fe) {}
    
    public void fileRenamed(@SuppressWarnings("unused")
	final FileRenameEvent fe) {}
    
    public void fileAttributeChanged(@SuppressWarnings("unused")
	final FileAttributeEvent fe) {}
    
    public void resultChanged(@SuppressWarnings("unused")
	final LookupEvent ev) {
        if (!running) RequestProcessor.getDefault().post(this);
    }
    
    private static class StreamPumper implements Runnable {
        final private Reader in;
        final private Writer out;
        public StreamPumper(Reader in, Writer out) {
            this.in = in;
            this.out = new BufferedWriter(out);
        }
        public void run() {
            try {
                int i;
                while ((i = in.read()) >=0) {
                    out.write(i);
                    out.flush();
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            } finally  {
                try {
                    out.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            }
        }
        
    }
}
