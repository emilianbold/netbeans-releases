/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.ide.ergonomics.prof;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.modules.ide.ergonomics.fod.Feature2LayerMapping;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.modules.ide.ergonomics.fod.FoDFileSystem;
import org.netbeans.modules.ide.ergonomics.fod.ModulesInstaller;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

public final class AttachAction implements ActionListener {

    public void actionPerformed(final ActionEvent e) {
            Task task = RequestProcessor.getDefault().create(new Runnable() {

               public void run() {
                    FeatureInfo featureInfo = null;
                    for (FeatureInfo info : Feature2LayerMapping.featureTypesLookup().lookupAll(FeatureInfo.class)) {
                        if ("Attach Profiler".equals(info.getAttachTypeName())) { // NOI18N
                            featureInfo = info;
                            break;
                        }
                    }
                    if (featureInfo != null) {
                        Boolean success = doDialog(featureInfo);
                        if (success == null) return;
                        if (success) {
                            performRegular(e);
                        } else {
                            NotifyDescriptor descriptor = new NotifyDescriptor(
                                    "Error",
                                    "Error",
                                    NotifyDescriptor.ERROR_MESSAGE,
                                    NotifyDescriptor.INFORMATION_MESSAGE,
                                    new Object[] { NotifyDescriptor.OK_CANCEL_OPTION },
                                    "Error");
                            DialogDisplayer.getDefault().notify(descriptor);
                        }
                    }
                }
            });
            task.schedule(0);
    }

    final Boolean doDialog(final FeatureInfo featureInfo) {
        DialogDescriptor descriptor = new DialogDescriptor(
                "Activate?",
                "Not Available");
        Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        d.setVisible(true);
        d.setVisible(false);
        d.dispose();
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            return ModulesInstaller.installModules(null, featureInfo);
        }
        return null;
    }
    
    private void performRegular(final ActionEvent e) {
        try {
            FoDFileSystem.getInstance().waitFinished();
            FileObject delegate = Repository.getDefault().getDefaultFileSystem().findResource("Menu/Profile/org-netbeans-modules-profiler-actions-AttachAction.instance"); // NOI18N
            InstanceCookie cookie = DataObject.find(delegate).getCookie(InstanceCookie.class);
            final AbstractAction regularAction = (AbstractAction) cookie.instanceCreate();
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    regularAction.actionPerformed(e);
                }
            });
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
