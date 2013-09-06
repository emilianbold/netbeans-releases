/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ui.util;

import java.awt.Component;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import static org.netbeans.modules.maven.j2ee.ui.util.Bundle.*;
import org.netbeans.modules.maven.j2ee.web.WebModuleImpl;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Martin Janicek
 */
public final class JavaEEVersionComboBoxUpdater extends ComboBoxUpdater<Profile> {

    private static final Set<Profile> WEB_PROFILES;
    private static final Set<Profile> FULL_PROFILES;

    private final Project project;
    private final ModelHandle2 handle;
    private final Profile defaultValue;

    static {
        WEB_PROFILES = new TreeSet<Profile>(Profile.UI_COMPARATOR);
        WEB_PROFILES.add(Profile.J2EE_13);
        WEB_PROFILES.add(Profile.J2EE_14);
        WEB_PROFILES.add(Profile.JAVA_EE_5);
        WEB_PROFILES.add(Profile.JAVA_EE_6_WEB);
        WEB_PROFILES.add(Profile.JAVA_EE_7_WEB);

        FULL_PROFILES = new TreeSet<Profile>(Profile.UI_COMPARATOR);
        FULL_PROFILES.add(Profile.J2EE_13);
        FULL_PROFILES.add(Profile.J2EE_14);
        FULL_PROFILES.add(Profile.JAVA_EE_5);
        FULL_PROFILES.add(Profile.JAVA_EE_6_FULL);
        FULL_PROFILES.add(Profile.JAVA_EE_7_FULL);
    }

    private JavaEEVersionComboBoxUpdater(Project project, ModelHandle2 handle, JComboBox javaeeCBox, JLabel javaeeLabel) {
        super(javaeeCBox, javaeeLabel);
        assert (handle != null);
        assert (javaeeCBox != null);

        final String packaging = handle.getPOMModel().getProject().getPackaging();
        if ("war".equals(packaging)) {
            javaeeCBox.setModel(new DefaultComboBoxModel(WEB_PROFILES.toArray()));
        } else {
            javaeeCBox.setModel(new DefaultComboBoxModel(FULL_PROFILES.toArray()));
        }

        this.project = project;
        this.handle = handle;
        this.defaultValue = getValue();

        final ListCellRenderer delegate = javaeeCBox.getRenderer();
        javaeeCBox.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return delegate.getListCellRendererComponent(list, ((Profile) value).getDisplayName(), index, isSelected, cellHasFocus);
            }

        });
    }

    /**
     * Factory method encapsulating ComboBoxUpdater creation. Typically client don't
     * want to do anything with a new instance so this makes more sense than creating
     * it using "new" keyword.
     *
     * @param handle Maven customizer handler
     * @param javaeeCBox Java EE version combo box for which we want to create updater
     * @param javaeeLabel Java EE label typically just before combo box
     */
    public static void create(Project project, ModelHandle2 handle, JComboBox javaeeCBox, JLabel javaeeLabel) {
        new JavaEEVersionComboBoxUpdater(project, handle, javaeeCBox, javaeeLabel);
    }

    @Override
    public Profile getDefaultValue() {
        return defaultValue;
    }

    @Override
    public Profile getValue() {
        Profile javaeeProfile = Profile.fromPropertiesString(handle.getRawAuxiliaryProperty(MavenJavaEEConstants.HINT_J2EE_VERSION, true));
        if (javaeeProfile != null) {
            return javaeeProfile;
        }

        WebModuleProviderImpl webModuleProvider = project.getLookup().lookup(WebModuleProviderImpl.class);
        if (webModuleProvider != null) {
            WebModuleImpl webModule = webModuleProvider.getModuleImpl();
            if (webModule != null) {
                return webModule.getJ2eeProfile();
            }
        }

        return null;
    }

    @Messages({
        "WARNING_ChangingJavaEEVersion=<html>You are changing Java EE version. <b>Please be aware about "
            + "possible consequences</b>. Your project might not be deployable anymore if the selected "
            + "server doesn't support choosen version.<br><br>Also note that changing this value doesn't "
            + "make any changes in your project configuration (pom.xml will still reffer to the original "
            + "Java EE jar file etc.)</html>."
    })
    @Override
    public void setValue(Profile profile) {
        if (WarningPanelSupport.isJavaEEChangeWarningActivated()) {
            WarningPanel panel = new WarningPanel(WARNING_ChangingJavaEEVersion());
            NotifyDescriptor dd = new NotifyDescriptor.Message(panel, NotifyDescriptor.OK_CANCEL_OPTION);
            DialogDisplayer.getDefault().notify(dd);

            if (dd.getValue() == NotifyDescriptor.CANCEL_OPTION) {
                return;
            }

            if (panel.disabledWarning()) {
                WarningPanelSupport.dontShowJavaEEChangeWarning();
            }
        }

        final String profileValue;
        if (profile != null) {
            profileValue = profile.toPropertiesString();
        } else {
            // If value is null, it means the default value was set --> see ComboBoxUpdater implementation for more details
            profileValue = defaultValue.toPropertiesString();
        }
        handle.setRawAuxiliaryProperty(MavenJavaEEConstants.HINT_J2EE_VERSION, profileValue, true);
    }
}
