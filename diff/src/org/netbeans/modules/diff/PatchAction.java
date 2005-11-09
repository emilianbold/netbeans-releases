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

package org.netbeans.modules.diff;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.io.ReaderInputStream;

import org.netbeans.api.diff.Difference;

import org.netbeans.modules.diff.builtin.Patch;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.RequestProcessor;

/**
 * Patch Action. It asks for a patch file and applies it to the selected file.
 *
 * @author  Martin Entlicher
 */
public class PatchAction extends NodeAction {
    
    /**
     * For patch application use an encoding, that is able to convert all bytes
     * to characters so that there is no loss in the file content.
     */
    private static final String PATCHING_IO_ENCODING = "ISO-8859-1"; // NOI18N

    /** Creates a new instance of PatchAction */
    public PatchAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(PatchAction.class, "CTL_PatchActionName");
    }

    public boolean enable(Node[] nodes) {
        if (nodes.length == 1) {
            FileObject fo = DiffAction.getFileFromNode(nodes[0]);
            if (fo != null) {
                try {
                    // #63460
                    return fo.getURL().getProtocol().equals("file");  // NOI18N
                } catch (FileStateInvalidException fsiex) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * @return false to run in AWT thread.
     */
    protected boolean asynchronous() {
        return false;
    }
    
    public void performAction(Node[] nodes) {
        final FileObject fo = DiffAction.getFileFromNode(nodes[0]);
        if (fo != null) {
            final File patch = getPatchFor(fo);
            if (patch == null) return ;
            RequestProcessor.getDefault().post(new Runnable () {
                public void run() {
                    Patch.FileDifferences[] fileDiffs;
                    String patchContext = null;
                    try {
                        String encoding = PATCHING_IO_ENCODING;

                        // Export diff patches are for sure in utf-8 encoding,
                        String MAGIC = "# This patch file was generated by NetBeans IDE"; // NOI18N
                        BufferedReader r = new BufferedReader(new FileReader(patch));
                        String line = r.readLine();
                        if (MAGIC.equals(line)) {
                            encoding = "utf8"; // NOI18N
                            line = r.readLine();
                            String MAGIC2 = "paths are relative to: "; // NOI18N
                            int idx = line.indexOf(MAGIC2); 
                            if (idx != -1) {
                                patchContext = line.substring(idx + MAGIC2.length());
                            }
                        }
                        r.close();
                        
                        byte[] buffer = new byte[MAGIC.length()];
                        InputStream in = new FileInputStream(patch);
                        int read = in.read(buffer);
                        in.close();
                        if (read != -1 && MAGIC.equals(new String(buffer, "utf8"))) {  // NOI18N
                            encoding = "utf8"; // NOI18N
                        }
                        fileDiffs = Patch.parse(new InputStreamReader(new FileInputStream(patch), encoding));
                    } catch (IOException ioex) {
                        ErrorManager.getDefault().annotate(ioex,
                            NbBundle.getMessage(PatchAction.class, "EXC_PatchParsingFailed", ioex.getLocalizedMessage()));
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioex);
                        ErrorManager.getDefault().notify(ErrorManager.USER, ioex);
                        return ;
                    }
                    int numDiffs = 0;
                    for (int i = 0; i < fileDiffs.length; i++) {
                        numDiffs += fileDiffs[i].getDifferences().length;
                    }
                    if (numDiffs == 0) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
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
                    applyFileDiffs(fileDiffs, fo, patchContext);
                }
            });
        }
    }

    private File getPatchFor(FileObject fo) {
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, new File(System.getProperty("user.home")));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String title = NbBundle.getMessage(PatchAction.class,
            (fo.isData()) ? "TITLE_SelectPatchForFile"
                          : "TITLE_SelectPatchForFolder", fo.getNameExt());
        chooser.setDialogTitle(title);

        // setup filters, default one filters patch files
        FileFilter patchFilter = new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith("diff") || f.getName().endsWith("patch") || f.isDirectory();  // NOI18N
            }
            public String getDescription() {
                return "Patch Files (*.diff, *.patch)";
            }
        };
        chooser.addChoosableFileFilter(patchFilter);
        chooser.setFileFilter(patchFilter);

        chooser.setApproveButtonText(NbBundle.getMessage(PatchAction.class, "BTN_Patch"));
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(PatchAction.class, "BTN_Patch_mnc").charAt(0));
        chooser.setApproveButtonToolTipText(NbBundle.getMessage(PatchAction.class, "BTN_Patch_tooltip"));
        HelpCtx ctx = new HelpCtx(PatchAction.class.getName());
        DialogDescriptor descriptor = new DialogDescriptor( chooser, title, true, new Object[0], null, 0, ctx, null );
        final Dialog dialog = DialogDisplayer.getDefault().createDialog( descriptor );
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PatchAction.class, "ACSD_PatchDialog"));

        ChooserListener listener = new PatchAction.ChooserListener(dialog,chooser);
	chooser.addActionListener(listener);
        dialog.setVisible(true);

        return listener.getFile();
    }

    private void applyFileDiffs(Patch.FileDifferences[] fileDiffs, FileObject fo, String patchContext) {
        ArrayList notFoundFileNames = new ArrayList();
        ArrayList appliedFiles = new ArrayList();
        HashMap backups = new HashMap();
        boolean patchFailed = false;
        for (int i = 0; i < fileDiffs.length; i++) {
            //System.out.println("applyFileDiffs(): fileName = "+fileDiffs[i].getFileName());
            FileObject targetFileObject;
            if (fo.isData()) {
                targetFileObject = fo;
            } else {
                String indexName = fileDiffs[i].getIndexName();
                if (indexName != null) {
                    targetFileObject = fo.getFileObject(indexName);
                } else {
                    targetFileObject = findChild(fo, fileDiffs[i].getFileName());
                }
            }

            if (targetFileObject == null) {
                Difference[] diffs = fileDiffs[i].getDifferences();
                String filePath = fileDiffs[i].getIndexName();
                if (diffs.length == 1 && diffs[0].getFirstStart() == 0 && filePath != null) {
                    // create new targetFileObject
                    try {
                        targetFileObject = FileUtil.createData(fo, filePath);
                    } catch (IOException e) {
                        ErrorManager err = ErrorManager.getDefault();
                        err.annotate(e, "Patch can not create new file, skipping...");  // NOI18N
                        err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }

            if (targetFileObject == null) {
                String indexName = fileDiffs[i].getIndexName();
                if (indexName != null) {
                    notFoundFileNames.add(FileUtil.getFileDisplayName(fo) + '/' + indexName);
                } else {
                    notFoundFileNames.add("sourceHostPath:" + fileDiffs[i].getFileName());
                }
            } else {
                FileObject backup = createFileBackup(targetFileObject);
                if (applyDiffsTo(fileDiffs[i].getDifferences(), targetFileObject)) {
                    appliedFiles.add(targetFileObject);
                    backups.put(targetFileObject, backup);
                    targetFileObject.refresh(true);
                } else {
                    patchFailed = true;
                }
            }
        }

        if (notFoundFileNames.size() > 0) {
            if (notFoundFileNames.size() == fileDiffs.length) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        patchContext == null ? NbBundle.getMessage(PatchAction.class, "MSG_WrongPatch") :
                        NbBundle.getMessage(PatchAction.class, "MSG_WrongPatch_Hint", patchContext)));
            } else {
                StringBuffer files = new StringBuffer();
                for (int i = 0; i < notFoundFileNames.size(); i++) {
                    files.append(notFoundFileNames.get(i).toString());
                    if (i < notFoundFileNames.size() - 1) {
                        files.append(", ");
                    }
                }
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(PatchAction.class, "MSG_NotFoundFiles", files)));
            }
        }
        if (appliedFiles.size() > 0) {
            String message = patchFailed ? NbBundle.getMessage(PatchAction.class, "MSG_PatchAppliedPartially") : NbBundle.getMessage(PatchAction.class, "MSG_PatchAppliedSuccessfully");
            Object notifyResult = DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                    message,
                    NotifyDescriptor.YES_NO_OPTION));
            if (NotifyDescriptor.YES_OPTION.equals(notifyResult)) {
                showDiffs(appliedFiles, backups);
            }
            removeBackups(appliedFiles, backups);
        }
    }

    /**
     * Flaky heuristics to find remote path on local system.
     */
    private static FileObject findChild(FileObject folder, String child) {
        child = child.replace(File.separatorChar, '/');
        StringTokenizer tokenizer = new StringTokenizer(child, "/");
        FileObject ch = null;
        // FIXME it's for sure wrong, it can be confused by /path/DUPL/somethigs/DULP/some.file
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
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException ioex) {}
            if (lock != null) lock.releaseLock();
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
            Reader patched = Patch.apply(diffs, new InputStreamReader(fo.getInputStream(), PATCHING_IO_ENCODING));
            FileUtil.copy(in = new ReaderInputStream(patched, PATCHING_IO_ENCODING), out = new FileOutputStream(tmp));
        } catch (IOException ioex) {
//            ErrorManager.getDefault().notify(ErrorManager.getDefault().annotate(ioex,
//                NbBundle.getMessage(PatchAction.class, "EXC_PatchApplicationFailed", ioex.getLocalizedMessage(), fo.getNameExt())));
            String msg = NbBundle.getMessage(PatchAction.class, "EXC_PatchApplicationFailed", ioex.getLocalizedMessage(), fo.getNameExt());
            NotifyDescriptor dd = new NotifyDescriptor.Message(msg);
            DialogDisplayer.getDefault().notify (dd);
            ErrorManager.getDefault().log (msg);
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
            DiffAction.performAction(backup, file, file);
        }
    }

    /** Removes the backup copies of files upon the successful application 
     * of a patch (.orig files).
     * @param files a list of files, to which the patch was successfully applied
     * @param backups a map of a form original file -> backup file
     */
    private static void removeBackups(List files, Map backups) {
        StringBuffer filenames=new StringBuffer(), 
                     exceptions=new StringBuffer();
        for (int i = 0; i < files.size(); i++) {
            FileObject targetFileObject = (FileObject) files.get(i);
            FileObject backup=(FileObject) backups.get(targetFileObject);

            // delete files that become empry
            if (targetFileObject.getSize() == 0) {
                try {
                    targetFileObject.delete();
                } catch (IOException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, "Patch can not delete file, skipping...");
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }

            try {
                backup.delete();
            }
            catch (IOException ex) {
                filenames.append(FileUtil.getFileDisplayName(backup));
                filenames.append("\n");
                exceptions.append(ex.getLocalizedMessage());
                exceptions.append("\n");
            }
        }
        if (filenames.length()>0)
            ErrorManager.getDefault().notify(
                ErrorManager.getDefault().annotate(new IOException(),
                    NbBundle.getMessage(PatchAction.class, 
                        "EXC_CannotRemoveBackup", filenames, exceptions)));
    }
   
    public HelpCtx getHelpCtx() {
        return new HelpCtx(PatchAction.class);
    }

    class ChooserListener implements ActionListener{
        private Dialog dialog;
        private JFileChooser chooser;
        private File file = null;

        public ChooserListener(Dialog dialog,JFileChooser chooser){
            super();
            this.dialog = dialog;
            this.chooser = chooser;
        }

        public void actionPerformed(ActionEvent e){
            String command  = e.getActionCommand();
            if(command == JFileChooser.APPROVE_SELECTION){
                if(dialog != null) {
                    file = chooser.getSelectedFile();
                    dialog.setVisible(false);

                }
            }else{
                if(dialog != null){
                    file = null;
                    dialog.setVisible(false);
                }
            }
        }
        public File getFile(){
            return file;
        }
    }

}
