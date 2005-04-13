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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
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
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

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
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public String getName() {
        return NbBundle.getMessage(DiffAction.class, "CTL_DiffActionName");
    }
    
    static FileObject getFileFromNode(Node node) {
        FileObject fo = (FileObject) node.getLookup().lookup(FileObject.class);
        if (fo == null) {
            DataObject dobj = (DataObject) node.getCookie(DataObject.class);
            if (dobj instanceof DataShadow) {
                dobj = ((DataShadow) dobj).getOriginal();
            }
            if (dobj != null) {
                fo = dobj.getPrimaryFile();
            }
        }
        return fo;
    }
    
    public boolean enable(Node[] nodes) {
        //System.out.println("DiffAction.enable() = "+(nodes.length == 2));
        if (nodes.length == 2) {
            FileObject fo1 = getFileFromNode(nodes[0]);
            FileObject fo2 = getFileFromNode(nodes[1]);
            if (fo1 != null && fo2 != null) {
                if (fo1.isData() && fo2.isData()) {
                    Diff d = Diff.getDefault();
                    return d != null;
                }
            }
        }
        return false;
    }
    
    /**
     * This action should not be run in AWT thread, because it opens streams
     * to files.
     * @return true not to run in AWT thread!
     */
    protected boolean asynchronous() {
        return true;
    }
    
    public void performAction(Node[] nodes) {
        ArrayList fos = new ArrayList();
        for (int i = 0; i < nodes.length; i++) {
            FileObject fo = getFileFromNode(nodes[i]);
            if (fo != null) {
                fos.add(fo);
            }
        }
        if (fos.size() < 2) return ;
        final FileObject fo1 = (FileObject) fos.get(0);
        final FileObject fo2 = (FileObject) fos.get(1);
        performAction(fo1, fo2);
    }
    
    /**
     * Shows the diff between two FileObject objects.
     * This is expected not to be called in AWT thread.
     */
    public static void performAction(FileObject fo1, FileObject fo2) {
        performAction(fo1, fo2, null);
    }
    /**
     * Shows the diff between two FileObject objects.
     * This is expected not to be called in AWT thread.
     * @param type Use the type of that FileObject to load both files.
     */
    static void performAction(FileObject fo1, FileObject fo2, FileObject type) {
        //System.out.println("performAction("+fo1+", "+fo2+")");
        //doDiff(fo1, fo2);
        Diff diff = Diff.getDefault();
        //System.out.println("dv = "+dv);
        if (diff == null) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(DiffAction.class,
                    "MSG_NoDiffVisualizer")));
            return ;
        }
        Component tp;
        Reader r1 = null;
        Reader r2 = null;
        try {
            EncodedReaderFactory rf = EncodedReaderFactory.getDefault();
            if (type != null) {
                r1 = rf.getReader(fo1, rf.getEncoding(type), type);
                r2 = rf.getReader(fo2, rf.getEncoding(type), type);
            } else {
                r1 = rf.getReader(fo1, rf.getEncoding(fo1), fo2.getExt());
                r2 = rf.getReader(fo2, rf.getEncoding(fo2), fo1.getExt());
            }
            String mimeType;
            if (type != null) {
                mimeType = type.getMIMEType();
            } else {
                mimeType = fo1.getMIMEType();
            }
            tp = diff.createDiff(fo1.getNameExt(), FileUtil.getFileDisplayName(fo1),
                                 r1,
                                 fo2.getNameExt(), FileUtil.getFileDisplayName(fo2),
                                 r2, mimeType);
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
            return ;
        } finally {
            try {
                if (r1 != null) r1.close();
                if (r2 != null) r2.close();
            } catch (IOException ioex) {}
        }
        //System.out.println("tp = "+tp);
        if (tp != null) {
            final Component ftp = tp;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (ftp instanceof TopComponent) {
                        ((TopComponent) ftp).open();
                        ((TopComponent) ftp).requestActive();
                    } else {
                        ftp.setVisible(true);
                        ftp.requestFocusInWindow();
                    }
                }
            });
        }
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DiffAction.class);
    }

}
