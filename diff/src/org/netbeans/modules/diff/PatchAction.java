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
import java.io.Reader;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
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
                return do1.getPrimaryFile().isData();
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
            Difference[] diffs;
            try {
                diffs = Patch.parse(new FileReader(patch));
            } catch (IOException ioex) {
                ErrorManager.getDefault().notify(ErrorManager.getDefault().annotate(ioex,
                    NbBundle.getMessage(PatchAction.class, "EXC_PatchParsingFailed", ioex.getLocalizedMessage())));
                return ;
            }
            if (diffs.length == 0) {
                TopManager.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(PatchAction.class, "MSG_NoDifferences", patch.getName())));
                return ;
            }
            /*
            System.out.println("Have diffs = "+diffs+", length = "+diffs.length);
            for (int i = 0; i < diffs.length; i++) {
                System.out.println(" Difference "+i+" : "+diffs[i]);
            }
             */
            applyDiffsTo(diffs, fo);
        }
    }
    
    private File getPatchFor(FileObject fo) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(NbBundle.getMessage(PatchAction.class, "TITLE_SelectPatch", fo.getNameExt()));
        chooser.setApproveButtonText(NbBundle.getMessage(PatchAction.class, "BTN_Patch"));
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(PatchAction.class, "BTN_Patch_mnc").charAt(0));
        chooser.setApproveButtonToolTipText(NbBundle.getMessage(PatchAction.class, "BTN_Patch_tooltip"));
        int ret = chooser.showDialog(new javax.swing.JFrame(NbBundle.getMessage(PatchAction.class, "TITLE_SelectPatch")),
                                     null);
        if (ret == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
    
    private void applyDiffsTo(Difference[] diffs, FileObject fo) {
        File tmp;
        try {
            tmp = File.createTempFile("patch", "tmp");
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
            return ;
        }
        tmp.deleteOnExit();
        try {
            Reader patched = Patch.apply(diffs, new InputStreamReader(fo.getInputStream()));
            FileUtil.copy(new ReaderInputStream(patched), new FileOutputStream(tmp));
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ErrorManager.getDefault().annotate(ioex,
                NbBundle.getMessage(PatchAction.class, "EXC_PatchApplicationFailed", ioex.getLocalizedMessage())));
            tmp.delete();
            return ;
        }
        try {
            FileUtil.copy(new FileInputStream(tmp), fo.getOutputStream(fo.lock()));
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ErrorManager.getDefault().annotate(ioex,
                NbBundle.getMessage(PatchAction.class, "EXC_CopyOfAppliedPatchFailed",
                                    ioex.getLocalizedMessage(), tmp.getAbsolutePath())));
            return ;
        }
        tmp.delete();
        TopManager.getDefault().notify(new NotifyDescriptor.Message(
            NbBundle.getMessage(PatchAction.class, "MSG_PatchAppliedSuccessfully")));
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PatchAction.class);
    }

}
