/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                        final Process p = Runtime.getRuntime().exec(inst.getAbsolutePath());
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
            wiz.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
            wiz.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
            wiz.putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
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
