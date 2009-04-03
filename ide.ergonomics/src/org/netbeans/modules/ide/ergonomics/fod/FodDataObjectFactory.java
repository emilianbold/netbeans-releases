/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.EventQueue;
import java.awt.Frame;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.Lookup;
import org.openide.util.WeakSet;

/** Support for special dataobjects that can dynamically FoD objects.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FodDataObjectFactory implements DataObject.Factory {
    private static MultiFileLoader delegate;
    private static final Set<FileObject> ignore = new WeakSet<FileObject>();

    private FileObject definition;
    
    private FodDataObjectFactory(FileObject fo) {
        this.definition = fo;
    }


    public static DataObject.Factory create(FileObject fo) {
        return new FodDataObjectFactory(fo);
    }

    public DataObject findDataObject(FileObject fo, Set<? super FileObject> recognized) throws IOException {
        if (fo.isFolder()) {
            return null;
        }
        if (fo.getMIMEType().endsWith("+xml")) {
            OpenAdvancedAction.registerCandidate(fo);
            return null;
        }
        if (ignore.contains(fo)) {
            return null;
        }
        if (delegate == null) {
            Enumeration<DataLoader> en = DataLoaderPool.getDefault().allLoaders();
            while (en.hasMoreElements()) {
                DataLoader d = en.nextElement();
                if (d instanceof MultiFileLoader) {
                    delegate = (MultiFileLoader)d;
                }
            }
            assert delegate instanceof MultiFileLoader;
        }
        return new Cookies(fo, delegate);
    }

    private final class Cookies extends MultiDataObject
    implements OpenCookie, EditCookie, Runnable, ChangeListener {
        private final FileObject fo;
        private final ChangeListener weakL;
        private boolean success;
        private boolean open;
        private ProgressHandle handle;
        private JDialog dialog;
        
        private Cookies(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
            super(fo, loader);
            this.fo = fo;
            this.weakL = WeakListeners.change(this, FeatureManager.getInstance());
            FeatureManager.getInstance().addChangeListener(weakL);
        }

        @Override
        protected Node createNodeDelegate() {
            DataNode dn = new DataNode(this, Children.LEAF);
            dn.setIconBaseWithExtension("org/netbeans/modules/ide/ergonomics/fod/file.png");
            return dn;
        }

        public Lookup getLookup() {
            return getCookieSet().getLookup();
        }

        public void open() {
            delegate(true);
        }

        public void edit() {
            delegate(false);
        }

        private void delegate(boolean open) {
            if (dialog != null) {
                return;
            }

            if (EventQueue.isDispatchThread()) {
                handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(FodDataObjectFactory.class, "MSG_Opening_File", fo.getPath()));
                Frame[] arr = JFrame.getFrames();
                final Frame mainWindow = arr.length > 0 ? arr[0] : null;
                dialog = new JDialog(mainWindow, NbBundle.getMessage(FodDataObjectFactory.class, "CAP_Opening_File"), true);
                dialog.getContentPane().add(new FodDataObjectFactoryPanel(handle, fo, null));
                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dialog.pack();
                dialog.setBounds(Utilities.findCenterBounds(dialog.getPreferredSize()));
                FoDFileSystem.LOG.log(Level.FINE, "Bounds {0}", dialog.getBounds());
            } else {
                dialog = null;
            }

            FoDFileSystem.LOG.log(Level.FINER, "Opening file {0}", this);
            this.open = open;
            Task task = RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
            if (dialog != null) {
                dialog.setVisible(true);
            }
            task.waitFinished ();
        }


        private void finishOpen() {
            if (success) {
                ignore.add(getPrimaryFile());
                try {
                    DataObject obj = DataObject.find(fo);
                    FoDFileSystem.LOG.log(Level.FINER, "finishOpen {0}", obj);
                    Class<?> what = open ? OpenCookie.class : EditCookie.class;
                    Object oc = obj.getLookup().lookup(what);
                    if (oc == this) {
                        obj.setValid(false);
                        obj = DataObject.find(fo);
                        oc = obj.getLookup().lookup(what);
                    }
                    if (oc instanceof OpenCookie) {
                        ((OpenCookie)oc).open();
                    }
                    if (oc instanceof EditCookie) {
                        ((EditCookie)oc).edit();
                    }
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        public void run() {
            FeatureInfo info = FoDFileSystem.getInstance().whichProvides(definition);
            FeatureManager.logUI("ERGO_FILE_OPEN", info.clusterName);
            FindComponentModules findModules = new FindComponentModules(info);
            Collection<UpdateElement> toInstall = findModules.getModulesForInstall ();
            Collection<UpdateElement> toEnable = findModules.getModulesForEnable ();
            if (toInstall != null && ! toInstall.isEmpty ()) {
                ModulesInstaller installer = new ModulesInstaller(toInstall, findModules);
                installer.getInstallTask ().waitFinished ();
                success = true;
            } else if (toEnable != null && ! toEnable.isEmpty ()) {
                ModulesActivator enabler = new ModulesActivator (toEnable, findModules);
                if (handle != null) {
                    enabler.assignEnableHandle(handle);
                }
                enabler.getEnableTask ().waitFinished ();
                success = true;
            } else if (toEnable.isEmpty() && toInstall.isEmpty()) {
                success = true;
                handle = null;
            }

            finishOpen();

            if (dialog != null) {
                if (handle != null) {
                    handle.finish();
                }
                dialog.setVisible(false);
                dialog = null;
                handle = null;
            }
        }

        public void stateChanged(ChangeEvent e) {
            FeatureInfo info = FoDFileSystem.getInstance().whichProvides(definition);
            FoDFileSystem.LOG.log(Level.FINER, "Refresh state of {0}", this);
            ignore.add(getPrimaryFile());
            if (info == null || info.isEnabled()) {
                dispose();
            }
        }
    } // end Cookies
}
