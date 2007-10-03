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

package org.netbeans.modules.diff;

import java.awt.Component;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Cancellable;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;

//import org.netbeans.modules.diff.cmdline.DiffCommand;
//import org.netbeans.modules.vcscore.diff.AbstractDiff;

import org.netbeans.api.diff.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.queries.FileEncodingQuery;
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
            Project p = (Project) node.getLookup().lookup(Project.class);
            if (p != null) return p.getProjectDirectory();

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
        ArrayList<FileObject> fos = new ArrayList<FileObject>();
        for (int i = 0; i < nodes.length; i++) {
            FileObject fo = getFileFromNode(nodes[i]);
            if (fo != null) {
                fos.add(fo);
            }
        }
        if (fos.size() < 2) return ;
        final FileObject fo1 = fos.get(0);
        final FileObject fo2 = fos.get(1);
        performAction(fo1, fo2);
    }
    
    /**
     * Shows the diff between two FileObject objects.
     * This is expected not to be called in AWT thread.
     */
    public static void performAction(final FileObject fo1, final FileObject fo2) {
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
            if (type != null) {
                r1 = new InputStreamReader(fo1.getInputStream(), FileEncodingQuery.getEncoding(type));
                r2 = new InputStreamReader(fo2.getInputStream(), FileEncodingQuery.getEncoding(type));
            } else {
                r1 = new InputStreamReader(fo1.getInputStream(), FileEncodingQuery.getEncoding(fo1));
                r2 = new InputStreamReader(fo2.getInputStream(), FileEncodingQuery.getEncoding(fo2));
            }
            String mimeType;
            if (type != null) {
                mimeType = type.getMIMEType();
            } else {
                mimeType = fo1.getMIMEType();
            }
            
            final Thread victim = Thread.currentThread();
            Cancellable killer = new Cancellable() {
                public boolean cancel() {
                    victim.interrupt();
                    return true;
                }
            };
            String name = NbBundle.getMessage(DiffAction.class, "BK0001");
            ProgressHandle ph = ProgressHandleFactory.createHandle(name, killer);
            try {
                ph.start();
                tp = diff.createDiff(fo1.getNameExt(), FileUtil.getFileDisplayName(fo1),
                                     r1,
                                     fo2.getNameExt(), FileUtil.getFileDisplayName(fo2),
                                     r2, mimeType);
            } finally {
                ph.finish();
            }
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
            return ;
        } finally {
            try {
                if (r1 != null) r1.close();
            } catch (IOException ioex) {}
            try {
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
