/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.CvsFileNode;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.diff.builtin.visualizer.TextDiffVisualizer;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.windows.WindowManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;

import javax.swing.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.HashSet;

/**
 * Exports <b>local</b> differencies between the current working copy and repository
 * version we started from.
 *  
 * @author Petr Kuzel
 */
public class ExportDiffAction extends AbstractSystemAction {
    
    private static final int enabledForStatus =
            FileInformation.STATUS_VERSIONED_CONFLICT |  
            FileInformation.STATUS_VERSIONED_MERGE | 
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;

    public ExportDiffAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected String getBaseName() {
        return "CTL_MenuItem_ExportDiff";
    }

    /**
     * Diff action should disabled only if current selection contains only files and these
     * files are not all changed locally. This scenario is not supported by {@link org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction}
     * so this method is overriden to return custom set of files to process.
     *
     * @return File[] all changed files in the current context
     */
    protected File [] getFilesToProcess() {
        CvsModuleConfig config = CvsModuleConfig.getDefault();
        CvsFileNode [] nodes = CvsVersioningSystem.getInstance().getFileTableModel(
                super.getFilesToProcess(), enabledForStatus).getNodes();
        Set modifiedFiles = new HashSet();
        for (int i = 0; i < nodes.length; i++) {
            File file = nodes[i].getFile();
            if (!config.isExcludedFromCommit(file.getAbsolutePath())) {
                modifiedFiles.add(file);
            }
        }
        return (File[]) modifiedFiles.toArray(new File[modifiedFiles.size()]);
    }

    public boolean isEnabled() {
        return super.isEnabled() && Lookup.getDefault().lookup(DiffProvider.class) != null;
    }

    public void performCvsAction(ActionEvent ev) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Diff Patch");
        chooser.setMultiSelectionEnabled(false);
        javax.swing.filechooser.FileFilter[] old = chooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            javax.swing.filechooser.FileFilter fileFilter = old[i];
            chooser.removeChoosableFileFilter(fileFilter);

        }
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith("diff") || f.getName().endsWith("patch") || f.isDirectory();  // NOI18N
            }
            public String getDescription() {
                return "Patch Files (*.diff, *.patch)";
            }
        });

        int ret = chooser.showDialog(WindowManager.getDefault().getMainWindow(), "Export");
        if (ret == JFileChooser.APPROVE_OPTION) {
            File destination = chooser.getSelectedFile();
            String name = destination.getName();
            boolean requiredExt = false;
            requiredExt |= name.endsWith(".diff");  // NOI18N
            requiredExt |= name.endsWith(".dif");   // NOI18N
            requiredExt |= name.endsWith(".patch"); // NOI18N
            if (requiredExt == false) {
                File parent = destination.getParentFile();
                destination = new File(parent, name + ".patch"); // NOI18N
            }

            final File out = destination;
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    async(out);
                }
            });
        }
    }

    private void async(File destination) {
        boolean success = false;
        OutputStream out = null;
        ProgressHandle progress = ProgressHandleFactory.createHandle("Exporting diff");
        try {
            out = new BufferedOutputStream(new FileOutputStream(destination));
            Context context = getContext();
            File [] files = DiffExecutor.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
            progress.start(files.length + 1);
            for (int i = 0; i < files.length; i++) {
                progress.progress(i);
                File file = files[i];
                Setup setup = new Setup(file, Setup.DIFFTYPE_LOCAL);
                exportDiff(setup, out);
            }
            success = true;
        } catch (IOException ex) {
            ErrorManager.getDefault().annotate(ex, "Diff patch export failed!");
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);   // stack trace to log
            ErrorManager.getDefault().notify(ErrorManager.USER, ex);  // message to user
        } finally {
            progress.finish();
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException alreadyClsoed) {
                }
            }
            if (success) {
                StatusDisplayer.getDefault().setStatusText("Diff patch export done!");
            } else {
                destination.delete();
            }

        }
    }


    /** Writes contextual diff into given stream.*/
    private void exportDiff(Setup setup, OutputStream out) throws IOException {
        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
        Reader r1 = setup.getFirstSource().createReader();
        if (r1 == null) r1 = new StringReader("");
        Reader r2 = setup.getSecondSource().createReader();
        if (r2 == null) r2 = new StringReader("");
        Difference[] differences = diff.computeDiff(r1, r2);

        File file = setup.getBaseFile();
        String name = file.getAbsolutePath();
        r1 = setup.getFirstSource().createReader();
        if (r1 == null) r1 = new StringReader("");
        r2 = setup.getSecondSource().createReader();
        if (r2 == null) r2 = new StringReader("");
        TextDiffVisualizer.TextDiffInfo info = new TextDiffVisualizer.TextDiffInfo(
            name, // + " " + setup.getFirstSource().getTitle(), // NOI18N
            name, // + " " + setup.getSecondSource().getTitle(),  // NOI18N
            null,
            null,
            r1,
            r2,
            differences
        );
        info.setContextMode(true, 3);
        InputStream is = TextDiffVisualizer.differenceToContextDiffText(info);
        while(true) {
            int i = is.read();
            if (i == -1) break;
            out.write(i);
        }
    }

}
