/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff;

import java.io.File;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.io.ReaderInputStream;

import org.netbeans.api.diff.Difference;

import org.netbeans.modules.diff.builtin.Patch;

/**
 * Patch Action. It asks for a patch file and applies it to the selected file.
 *
 * @author  Martin Entlicher
 */
public class PatchAction extends NodeAction {
    
    /** Creates a new instance of PatchAction */
    public PatchAction() {
    }
    
    public String getName() {
        return NbBundle.getMessage(PatchAction.class, "CTL_PatchActionName");
    }
    
    public boolean enable(Node[] nodes) {
        if (nodes.length == 1) {
            DataObject do1 = (DataObject) nodes[0].getCookie(DataObject.class);
            if (do1 != null) {
                if (do1 instanceof org.openide.loaders.InstanceDataObject) {
                    return false;
                }
                FileObject fo = do1.getPrimaryFile();
                //if (fo.isFolder()) return false;
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (fs.isDefault()) {
                        String packageName = fo.getPackageName('/');
                        return (packageName.startsWith("Templates") ||
                                packageName.startsWith("vcs/config") ||
                                packageName.startsWith("org/"));
                    } else return true;
                } catch (FileStateInvalidException fsiex) {
                    return false;
                }
            }
        }
        return false;
    }
    
    public void performAction(Node[] nodes) {
        DataObject do1 = (DataObject) nodes[0].getCookie(DataObject.class);
        if (do1 != null) {
            FileObject fo = do1.getPrimaryFile();
            File patch = getPatchFor(fo);
            if (patch == null) return ;
            Patch.FileDifferences[] fileDiffs;
            try {
                fileDiffs = Patch.parse(new FileReader(patch));
            } catch (IOException ioex) {
                ErrorManager.getDefault().notify(ErrorManager.getDefault().annotate(ioex,
                    NbBundle.getMessage(PatchAction.class, "EXC_PatchParsingFailed", ioex.getLocalizedMessage())));
                return ;
            }
            int numDiffs = 0;
            for (int i = 0; i < fileDiffs.length; i++) {
                numDiffs += fileDiffs[i].getDifferences().length;
            }
            if (numDiffs == 0) {
                TopManager.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(PatchAction.class, "MSG_NoDifferences", patch.getName())));
                return ;
            }
            /*
            System.out.println("Have diffs = "+fileDiffs+", length = "+fileDiffs.length);
            for (int i = 0; i < fileDiffs.length; i++) {
                System.out.println(" Difference "+i+" : "+fileDiffs[i]);
                Difference[] diffs = fileDiffs[i].getDifferences();
                for (int j = 0; j < diffs.length; j++) {
                    System.out.println("  Diff["+j+"] = "+diffs[j]);
                    System.out.println("TEXT1 = \n"+diffs[j].getFirstText()+"TEXT2 = \n"+diffs[j].getSecondText()+"");
                }
            }
            */
            applyFileDiffs(fileDiffs, fo);
            //createFileBackup(fo);
            //applyDiffsTo(diffs, fo);
        }
    }
    
    private File getPatchFor(FileObject fo) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String title = NbBundle.getMessage(PatchAction.class,
            (fo.isData()) ? "TITLE_SelectPatchForFile"
                          : "TITLE_SelectPatchForFolder", fo.getNameExt());
        chooser.setDialogTitle(title);
        chooser.setApproveButtonText(NbBundle.getMessage(PatchAction.class, "BTN_Patch"));
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(PatchAction.class, "BTN_Patch_mnc").charAt(0));
        chooser.setApproveButtonToolTipText(NbBundle.getMessage(PatchAction.class, "BTN_Patch_tooltip"));
        int ret = chooser.showDialog(new javax.swing.JFrame(title), null);
        if (ret == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
    
    private void applyFileDiffs(Patch.FileDifferences[] fileDiffs, FileObject fo) {
        ArrayList notFoundFileNames = new ArrayList();
        ArrayList appliedFiles = new ArrayList();
        HashMap backups = new HashMap();
        for (int i = 0; i < fileDiffs.length; i++) {
            //System.out.println("applyFileDiffs(): fileName = "+fileDiffs[i].getFileName());
            FileObject file;
            if (fo.isData()) file = fo;
            else file = findChild(fo, fileDiffs[i].getFileName());//fo.getFileObject(fileDiffs[i].getFileName());
            if (file == null) {
                notFoundFileNames.add(fo.getPackageNameExt(File.separatorChar, '.') +
                                      File.separator + fileDiffs[i].getFileName());
            } else {
                FileObject backup = createFileBackup(file);
                if (applyDiffsTo(fileDiffs[i].getDifferences(), file)) {
                    appliedFiles.add(file);
                    backups.put(file, backup);
                    file.refresh(true);
                }
            }
        }
        if (notFoundFileNames.size() > 0) {
            String files = "";
            for (int i = 0; i < notFoundFileNames.size(); i++) {
                files += notFoundFileNames.get(i).toString();
                if (i < notFoundFileNames.size() - 1) {
                    files += ", ";
                }
            }
            TopManager.getDefault().notify(new NotifyDescriptor.Message(
                NbBundle.getMessage(PatchAction.class, "MSG_NotFoundFiles", files)));
        }
        if (appliedFiles.size() > 0) {
            Object notifyResult = TopManager.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(PatchAction.class, "MSG_PatchAppliedSuccessfully"),
                    NotifyDescriptor.YES_NO_OPTION));
            if (NotifyDescriptor.YES_OPTION.equals(notifyResult)) {
                showDiffs(appliedFiles, backups);
            }
        }
    }
    
    private static FileObject findChild(FileObject folder, String child) {
        child = child.replace(File.separatorChar, '/');
        StringTokenizer tokenizer = new StringTokenizer(child, "/");
        FileObject ch = null;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            ch = folder.getFileObject(token);
            if (ch != null && ch.isFolder()) {
                folder = ch;
                ch = null;
            }
        }
        return ch;
    }
    
    private FileObject createFileBackup(FileObject fo) {
        FileObject parent = fo.getParent();
        FileLock lock = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            FileObject orig = parent.getFileObject(fo.getNameExt(), "orig");
            if (orig == null) {
                orig = parent.createData(fo.getNameExt(), "orig");
            }
            FileUtil.copy(in = fo.getInputStream(), out = orig.getOutputStream(lock = orig.lock()));
            return orig;
        } catch (IOException ioex) {
            return null;
        } finally {
            if (lock != null) lock.releaseLock();
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException ioex) {}
        }
    }
    
    private boolean applyDiffsTo(Difference[] diffs, FileObject fo) {
        //System.out.println("applyDiffsTo("+fo.getPackageNameExt('/', '.')+")");
        File tmp;
        try {
            tmp = File.createTempFile("patch", "tmp");
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
            return false;
        }
        tmp.deleteOnExit();
        InputStream in = null;
        OutputStream out = null;
        try {
            Reader patched = Patch.apply(diffs, new InputStreamReader(fo.getInputStream()));
            FileUtil.copy(in = new ReaderInputStream(patched), out = new FileOutputStream(tmp));
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ErrorManager.getDefault().annotate(ioex,
                NbBundle.getMessage(PatchAction.class, "EXC_PatchApplicationFailed", ioex.getLocalizedMessage(), fo.getNameExt())));
            tmp.delete();
            return false;
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException ioex) {}
        }
        FileLock lock = null;
        try {
            FileUtil.copy(in = new FileInputStream(tmp), out = fo.getOutputStream(lock = fo.lock()));
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ErrorManager.getDefault().annotate(ioex,
                NbBundle.getMessage(PatchAction.class, "EXC_CopyOfAppliedPatchFailed",
                                    fo.getNameExt())));
            return false;
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException ioex) {}
        }
        tmp.delete();
        return true;
        //TopManager.getDefault().notify(new NotifyDescriptor.Message(
        //    NbBundle.getMessage(PatchAction.class, "MSG_PatchAppliedSuccessfully")));
    }
    
    private void showDiffs(ArrayList files, HashMap backups) {
        for (int i = 0; i < files.size(); i++) {
            FileObject file = (FileObject) files.get(i);
            FileObject backup = (FileObject) backups.get(file);
            DiffAction.performAction(backup, file);
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PatchAction.class);
    }

}
