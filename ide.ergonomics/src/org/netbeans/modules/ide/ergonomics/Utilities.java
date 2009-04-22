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

package org.netbeans.modules.ide.ergonomics;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.ide.ergonomics.fod.ConfigurationPanel;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Set of static useful methods.
 *
 * @author Pavel Flaska
 */
public class Utilities {

    private Utilities() {
    }

    public static final boolean featureNotFoundDialog(final FeatureInfo featureInfo, final String featureName) {
        String notFound = NbBundle.getMessage(Utilities.class, "LBL_FeatureNotFound");
        return featureDialog(featureInfo, notFound, featureName);
    }
    public static final boolean featureDialog(final FeatureInfo featureInfo, final String notFoundMessage, final String featureName) {
        final boolean[] result = new boolean[] { false };
        final DialogDescriptor[] descriptor = new DialogDescriptor[1];
        final Callable<JComponent> call = new Callable<JComponent>() {
            public JComponent call() throws Exception {
                result[0] = true;
                descriptor[0].setValue(DialogDescriptor.CLOSED_OPTION);
                return new JPanel();
            }
        };
        descriptor[0] = Mutex.EVENT.readAccess(new Mutex.Action<DialogDescriptor>() {
            public DialogDescriptor run() {
                return new DialogDescriptor(new ConfigurationPanel(featureName, call, featureInfo, true), notFoundMessage);
            }
        });
        descriptor[0].setOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
        final Dialog d = DialogDisplayer.getDefault().createDialog(descriptor[0]);
        descriptor[0].addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent arg0) {
                d.setVisible(false);
                d.dispose();
            }
        });
        d.setVisible(true);
        return result[0];
    }
}
