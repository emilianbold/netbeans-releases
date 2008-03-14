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

package org.netbeans.modules.j2ee.common.project.ui;

import javax.swing.event.ChangeEvent;
import java.awt.Component;
import java.io.File;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 * @since 1.21
 */
public class PanelSharability implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    public static final String WIZARD_SHARED_LIBRARIES = "sharedLibraries"; // NOI18N

    public static final String WIZARD_SERVER_LIBRARY = "serverLibrary"; // NOI18N

    private static final String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final String projectLocationProperty;

    private final String serverIdProperty;

    private final boolean finish;

    private PanelSharabilityVisual panel;

    private WizardDescriptor descriptor;

    private File projectLocation;

    public PanelSharability(String projectLocationProperty, String serverIdProperty, boolean finish) {
        this.projectLocationProperty = projectLocationProperty;
        this.serverIdProperty = serverIdProperty;
        this.finish = finish;
    }

    public boolean isFinishPanel() {
        return finish;
    }

    public Component getComponent() {
        return getPanel();
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public HelpCtx getHelp() {
        return new HelpCtx(PanelSharability.class);
    }

    public boolean isValid() {
        descriptor.putProperty(PROP_ERROR_MESSAGE, "");

        final PanelSharabilityVisual currentPanel = getPanel();
        if (currentPanel != null) {
            if (currentPanel.getSharableProject().isSelected()) {
                String location = currentPanel.getSharedLibarariesLocation();
                if (new File(location).isAbsolute()) {
                    descriptor.putProperty(PROP_ERROR_MESSAGE, decorateMessage(
                            NbBundle.getMessage(PanelSharability.class, "PanelSharability.absolutePathWarning.text")));

                } else {
                    if (projectLocation != null) {
                        File projectLoc = FileUtil.normalizeFile(projectLocation);
                        File libLoc = PropertyUtils.resolveFile(projectLoc, location);
                        if (!CollocationQuery.areCollocated(projectLoc, libLoc)) {
                            descriptor.putProperty(PROP_ERROR_MESSAGE, decorateMessage(
                                    NbBundle.getMessage(PanelSharability.class, "PanelSharability.relativePathWarning.text")));
                        }
                    }
                }
            }
            if (currentPanel.getSharableProject().isSelected() && currentPanel.getServerLibraryCheckbox().isEnabled()
                && currentPanel.getServerLibraryCheckbox().isSelected()) {

                    descriptor.putProperty(PROP_ERROR_MESSAGE, decorateMessage(
                        NbBundle.getMessage(PanelSharability.class, "PanelSharability.licenseWarning.text")));
            }
        }

        return true;
    }

    public void readSettings(Object settings) {
        descriptor = (WizardDescriptor) settings;

        projectLocation = (File) descriptor.getProperty(projectLocationProperty);
        panel.setProjectLocation(projectLocation);
        panel.setServerInstance((String) descriptor.getProperty(serverIdProperty));
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;

        d.putProperty(WIZARD_SHARED_LIBRARIES, panel.getSharedLibarariesLocation());
        d.putProperty(WIZARD_SERVER_LIBRARY, panel.getServerLibraryName());
    }

    private synchronized PanelSharabilityVisual getPanel() {
        if (panel == null) {
            assert SwingUtilities.isEventDispatchThread() : "Not called in EDT api bug 122184"; //NOI18N
            panel = new PanelSharabilityVisual(this);

            panel.addChangeListener(new PanelListener());
        }
        return panel;
    }

    private String decorateMessage(String message) {
        if (message != null) {
            return "<html>" + message.replaceAll("<",  "&lt;").replaceAll(">",  "&gt;") + "</html>"; // NIO18N
        }
        return null;
    }

    private class PanelListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            changeSupport.fireChange();
        }
    }
}
