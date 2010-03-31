/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * JSFTestMultiviewDescriptor.java
 *
 * Created on February 7, 2007, 6:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.openide.DialogDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author Joelle Lam
 */
public class JSFPageFlowMultiviewDescriptor implements MultiViewDescription, Serializable {

    private static final long serialVersionUID = -3101808890387485990L;
    //    private static final long serialVersionUID = -6305897237371751567L;
    //        static final long serialVersionUID = -6305897237371751567L;
    private JSFConfigEditorContext context;
    private static final String PAGEFLOW = NbBundle.getMessage(JSFPageFlowMultiviewDescriptor.class, "LBL_PageFlow");

    /**
     * This is the multiview descripture which defines a new pane in the faces configuration xml multiview editor.
     */
    public JSFPageFlowMultiviewDescriptor() {
    }

    /**
     * This is the multiview descripture which defines a new pane in the faces configuration xml multiview editor.
     * @param context the JSFConfigEditorContext
     **/
    public JSFPageFlowMultiviewDescriptor(JSFConfigEditorContext context) {
        this.context = context;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    public String getDisplayName() {
        return PAGEFLOW;
    }
    private static final Image JSFConfigIcon = ImageUtilities.loadImage("org/netbeans/modules/web/jsf/resources/JSFConfigIcon.png"); // NOI18N

    public Image getIcon() {
        //        return PageFlowImage;
        return JSFConfigIcon;
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    public String preferredID() {
        return PAGEFLOW;
    }

    public MultiViewElement createElement() {
        return new PageFlowElement(context);
    }

    static class PageFlowElement implements MultiViewElement, Serializable {

        //        private transient JScrollPane panel;
        private transient PageFlowView tc;
        private transient JComponent toolbar;
        private static final long serialVersionUID = 5454879177214643L;
        private JSFConfigEditorContext context;

        public PageFlowElement(JSFConfigEditorContext context) {
            this.context = context;
            init();
        }

        private void init() {
            getTopComponent().setName(context.getFacesConfigFile().getName());
        }

        public JComponent getVisualRepresentation() {
            return tc;
        }

        public JComponent getToolbarRepresentation() {
            if (toolbar == null) {
                toolbar = getTopComponent().getToolbarRepresentation();
            }
            return toolbar;
        }

        private PageFlowView getTopComponent() {
            if (tc == null) {
                tc = new PageFlowView(this, context);
            }
            return tc;
        }

        public Action[] getActions() {
            Action[] a = tc.getActions();

            try {
                ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);

                if (l == null) {
                    l = getClass().getClassLoader();
                }

                Class<? extends SystemAction> c = Class.forName("org.openide.actions.FileSystemAction", true, l).asSubclass(SystemAction.class); // NOI18N
                SystemAction ra = SystemAction.findObject(c, true);

                Action[] a2 = new Action[a.length + 1];
                System.arraycopy(a, 0, a2, 0, a.length);
                a2[a.length] = ra;
                return a2;
            } catch (Exception ex) {
                // ok, we no action like this I guess
            }
            return new Action[]{};
        }

        public Lookup getLookup() {
            return tc.getLookup();
        }

        public void componentOpened() {
            tc.registerListeners();
//            tc.startBackgroundPinAddingProcess();
            LOG.finest("PageFlowEditor componentOpened");
        }

        public void componentClosed() {
            long time = System.currentTimeMillis();
            final FileObject storageFile = PageFlowView.getStorageFile(context.getFacesConfigFile());

            if (storageFile != null && storageFile.isValid()) {
                tc.serializeNodeLocations(storageFile);
            } else {
                DialogDescriptor dialog;
                if (storageFile != null) {
                    dialog = new DialogDescriptor(NbBundle.getMessage(JSFPageFlowMultiviewDescriptor.class, "MSG_NoFileToSave", storageFile), NbBundle.getMessage(JSFPageFlowMultiviewDescriptor.class, "TLE_NoFileToSave"));
                } else {
                    dialog = new DialogDescriptor(NbBundle.getMessage(JSFPageFlowMultiviewDescriptor.class, "MSG_NoProjectToSave"), NbBundle.getMessage(JSFPageFlowMultiviewDescriptor.class, "TLE_NoFileToSave"));
                }
                dialog.setOptions(new Object[]{DialogDescriptor.OK_OPTION});
                java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
                d.setVisible(true);
            }

            tc.unregstierListeners();
            PageFlowToolbarUtilities.removePageFlowView(tc);
           // tc.clearGraph();
            tc.destroyScene();
            toolbar = null;
            tc = null;

            LOG.finest("PageFlowEditor componentClosed took: "+ (System.currentTimeMillis() - time) + " ms");
        }

        public void componentShowing() {
            LOG.finest("PageFlowEditor componentShowing");
            tc.getPageFlowController().flushGraphIfDirty();
        }

        public void componentHidden() {
            LOG.finest("PageFlowEditor componentHidden");
        }

        public void componentActivated() {
            //tc.requestFocusInWindow();
            LOG.finest("PageFlowView componentActivated");
            tc.requestActive();
        }

        public void componentDeactivated() {
            LOG.finest("PageFlowView Deactivated");
        }
        private MultiViewElementCallback callback;

        public MultiViewElementCallback getMultiViewCallback() {
            return callback;
        }

        public void setMultiViewCallback(MultiViewElementCallback callback) {
            this.callback = callback;
            context.setMultiViewTopComponent(callback.getTopComponent());
        }

        public CloseOperationState canCloseElement() {
            return MultiViewFactory.createUnsafeCloseState("ID_FACES_CONFIG_CLOSING", MultiViewFactory.NOOP_CLOSE_ACTION, MultiViewFactory.NOOP_CLOSE_ACTION);
        }

        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            tc.serializeNodeLocations(PageFlowView.getStorageFile(context.getFacesConfigFile()));
            out.writeObject(context);
            LOG.finest("writeObject");
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            Object object = in.readObject();
            if (!(object instanceof JSFConfigEditorContext)) {
                throw new ClassNotFoundException("JSFConfigEditorContext expected but not found");
            }
            context = (JSFConfigEditorContext) object;
            /* deserialization of node locations is completed in the PageFlowView constructor (in init() ) */
            init();
            LOG.finest("readObject");
        }

        public UndoRedo getUndoRedo() {
            return context.getUndoRedo();
        }
    }
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.web.jsf.navigation");
}
