/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class FeatureAction implements ActionListener, Runnable {

    private boolean success;
    private FileObject fo;
    private ProgressHandle handle;
    private JDialog dialog;

    private FeatureAction(FileObject fo) {
        this.fo = fo;
    }

    public static ActionListener create(FileObject fo) {
        return new FeatureAction(fo);
    }

    public void actionPerformed(ActionEvent e) {
        success = false;
        if (EventQueue.isDispatchThread()) {
            handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(FeatureAction.class, "MSG_Action", fo.getAttribute("displayName"))
            ); // NOI18N
            Frame[] arr = JFrame.getFrames();
            final Frame mainWindow = arr.length > 0 ? arr[0] : null;
            dialog = new JDialog(
                mainWindow,
                NbBundle.getMessage(FodDataObjectFactory.class, "CAP_Action"),
                true
            );
            dialog.getContentPane().add(new FodDataObjectFactoryPanel(handle, null, (String)fo.getAttribute("displayName")));
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.pack();
            dialog.setBounds(Utilities.findCenterBounds(dialog.getPreferredSize()));
            FoDFileSystem.LOG.log(Level.FINE, "Bounds {0}", dialog.getBounds());
        }
        RequestProcessor.Task t = RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        if (dialog != null) {
            dialog.setVisible(true);
        }
        t.waitFinished ();
        
        if (! success) {
            return ;
        }
        
        FileObject newFile = FileUtil.getConfigFile(fo.getPath());
        if (newFile == null) {
            throw new IllegalStateException("Cannot find file: " + fo.getPath());
        }
        
        Object obj = newFile.getAttribute("instanceCreate"); // NOI18N
        if (obj instanceof ActionListener) {
            ((ActionListener)obj).actionPerformed(e);
        }
    }

    public void run() {
        assert ! EventQueue.isDispatchThread () : "Cannot run in EQ!";
        FeatureInfo info = FoDFileSystem.getInstance().whichProvides(fo);
        if (handle != null) {
            handle.start();
        }
        success = ModulesInstaller.installModules(info);
        if (dialog != null) {
            dialog.setVisible(false);
        }
        if (handle != null) {
            handle.finish();
        }
        handle = null;
        dialog = null;
    }
}
