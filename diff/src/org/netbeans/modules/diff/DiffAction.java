/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff;

import java.lang.reflect.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.options.SystemOption;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;

//import org.netbeans.modules.diff.cmdline.DiffCommand;
//import org.netbeans.modules.vcscore.diff.AbstractDiff;

import org.netbeans.api.diff.*;

//import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
//import org.netbeans.modules.diff.cmdline.CmdlineDiffProvider;
//import org.netbeans.modules.diff.builtin.visualizer.GraphicalDiffVisualizer;

//import org.netbeans.modules.diff.io.diff.*;

/**
 * Diff Action. It gets the default diff visualizer and diff provider if needed
 * and display the diff visual representation of two files selected in the IDE.
 *
 * @author  Martin Entlicher
 */
public class DiffAction extends NodeAction {

    /** Creates new DiffAction */
    public DiffAction() {
        //setupDefaultServices();
    }
    
    /*
    private void setupDefaultServices() {
        DiffManager manager = DiffManager.getDefault();
        //DiffProvider dp = new org.netbeans.modules.diff.cmdline.provider.CmdlineDiffProvider(settings.getDiffCmd());
        DiffProvider dp = new org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider();
        manager.setDefaultDiffProvider(dp);
        DiffVisualizer dv = new org.netbeans.modules.diff.builtin.visualizer.GraphicalDiffVisualizer();
        manager.setDefaultDiffVisualizer(dv);
    }
     */
    
    public String getName() {
        return NbBundle.getMessage(DiffAction.class, "CTL_DiffActionName");
    }
    
    public boolean enable(Node[] nodes) {
        //System.out.println("DiffAction.enable() = "+(nodes.length == 2));
        return nodes.length == 2;
    }
    
    public void performAction(Node[] nodes) {
        ArrayList fos = new ArrayList();
        for (int i = 0; i < nodes.length; i++) {
            DataObject dd = (DataObject) (nodes[i].getCookie(DataObject.class));
            if (dd != null) fos.add(dd.getPrimaryFile());
        }
        if (fos.size() < 2) return ;
        final FileObject fo1 = (FileObject) fos.get(0);
        final FileObject fo2 = (FileObject) fos.get(1);
        //System.out.println("performAction("+fo1+", "+fo2+")");
        //doDiff(fo1, fo2);
        DiffVisualizer dv = DiffManager.getDefault().getDefaultDiffVisualizer();
        System.out.println("dv = "+dv);
        if (dv == null) return ;
        List diffs = null;
        if (dv.needsProvider()) {
            DiffProvider dp = DiffManager.getDefault().getDefaultDiffProvider();
            System.out.println("dp = "+dp);
            if (dp == null) return ;
            try {
                diffs = dp.createDiff(fo1, fo2);
            } catch (IOException ioex) {
                TopManager.getDefault().notifyException(ioex);
                return ;
            }
        }
        TopComponent tp;
        try {
            tp = dv.showDiff(diffs, fo1, fo2);
        } catch (IOException ioex) {
            TopManager.getDefault().notifyException(ioex);
            return ;
        }
        System.out.println("tp = "+tp);
        if (tp != null) tp.open();
        /*
        RequestProcessor.postRequest(new Runnable() {
            public void run() {
                doDiffFiles(fo1, fo2);//new FileObject[] { fo1, fo2 });
            }
        });
         */
    }
    
    /*
    private void doDiffFiles(FileObject fo1, FileObject fo2) {
        DiffSettings settings = (DiffSettings) SystemOption.findObject(DiffSettings.class, true);
        DiffProvider provider = settings.getDefaultDiffProvider();
        AbstractDiff diff = new AbstractDiff();
        boolean diffStatus = provider.performDiff(fo1, fo2, diff);
        showDiff(diff, diffStatus, fo1, fo2);
    }
    
    private void showDiff(final AbstractDiff diff, final boolean diffStatus,
                          final FileObject fo1, final FileObject fo2) {
        if (diff.getNumActions() == 0) {
            if (diffStatus == true) {
                TopManager.getDefault ().notify (new NotifyDescriptor.Message(
                    NbBundle.getMessage(DiffAction.class, "MSG_NoDifferenceInFile", fo1.getNameExt(), fo2.getNameExt())));
                return ;
            } else {
                TopManager.getDefault ().notify (new NotifyDescriptor.Message(
                    NbBundle.getMessage(DiffAction.class, "MSG_DiffFailed", fo1.getNameExt(), fo2.getNameExt())));
                return ;
            }
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable () {
            public void run() {
                java.io.File file1 = org.openide.execution.NbClassPath.toFile(fo1);
                java.io.File file2 = org.openide.execution.NbClassPath.toFile(fo2);
                diff.open(NbBundle.getMessage(DiffAction.class, "CTL_DiffTitleFile",
                                              fo1.getNameExt(), fo2.getNameExt()),
                          fo1.getMIMEType(), file1.getAbsolutePath(), file2.getAbsolutePath(),
                          fo1.getPackageNameExt('/', '.'), fo2.getPackageNameExt('/', '.'));
            }
        });
    }
     */
    
    /*
    private void doDiffFiles(FileObject[] fos) {
        String[] fileNames = new String[fos.length];
        for (int i = 0; i < fos.length; i++) {
            FileSystem fs = null;
            try {
                fs = fos[i].getFileSystem();
            } catch (FileStateInvalidException exc) {
                TopManager.getDefault().notifyException(exc);
                fs = null;
            }
            if (fs == null) return ;
            File root = null;
            //if (fs instanceof LocalFileSystem) root = ((LocalFileSystem) fs).getRootDirectory();
            //if (fs instanceof VcsFileSystem) root = ((VcsFileSystem) fs).getRootDirectory();
            Method getRootMethod = null;
            try {
                getRootMethod = fs.getClass().getMethod("getRootDirectory", new Class[0]);
            } catch (NoSuchMethodException nmexc) {
            }
            if (getRootMethod != null) {
                try {
                    root = (File) getRootMethod.invoke(fs, new Object[0]);
                } catch (IllegalAccessException iaexc) {
                    TopManager.getDefault().notifyException(iaexc);
                } catch (InvocationTargetException itexc) {
                    TopManager.getDefault().notifyException(itexc);
                }
            }
            String packageName = fos[i].getPackageNameExt(java.io.File.separatorChar, '.');
            String path;
            if (root != null) {
                path = new File(root, packageName).getAbsolutePath();
            } else {
                path = packageName;
            }
            fileNames[i] = path;
        }
        performDiffOfFiles(fileNames[0], fileNames[1], fos[0].getMIMEType());
    }
    
    /** Use Java DIFF
    private void performDiffOfFiles(String f1, String f2) {
        DiffList d;
        try {
            FileDiff fd = FileDiff.FileDiff(f1, f2);
            d = fd.waitDiffs();
        } catch (java.io.IOException exc) {
            TopManager.getDefault().notifyException(exc);
            return ;
        } catch (InterruptedException intrexc) {
            TopManager.getDefault().notifyException(intrexc);
            return ;
        } catch (DiffException dexc) {
            TopManager.getDefault().notifyException(dexc);
            return ;
        }
        int n = d.size();
        for (int i = 0; i < n; i++) {
            DiffItem item = d.getDiffItem(i);
            System.out.println("item = "+item);
        }
    }
     */
    
    /** Perform the command-line diff *
    private void performDiffOfFiles(String f1, String f2, String MIMEType) {
        DiffCommand.showDiff("diff", f1, f2, MIMEType);
    }
     */
    
    public HelpCtx getHelpCtx() {
        return null;
    }

}
