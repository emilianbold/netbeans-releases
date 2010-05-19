/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.api;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.validation.adapters.DialogBuilder;
import org.netbeans.modules.javacard.spi.PlatformAndDeviceProvider;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.modules.javacard.spi.impl.TempPlatformAndDeviceProvider;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author tim
 */
public class BadPlatformOrDevicePanel extends JPanel implements ActionListener {
    private final PlatformAndDevicePanel pnl;
    private PlatformAndDeviceProvider provider = new TempPlatformAndDeviceProvider();
    private final JCheckBox box = new JCheckBox(NbBundle.getMessage(
            BadPlatformOrDevicePanel.class, "BadPlatformOrDevicePanel.jCheckBox1.text")); //NOI18N
    private static final String PREFS_KEY_DONT_SHOW_DLG = "dontShowBrokenPlatformDialog"; //NOI18N
    private final JLabel instructions = new JLabel(NbBundle.getMessage(
            BadPlatformOrDevicePanel.class, "BadPlatformOrDevicePanel.jLabel1.text")); //NOI18N
    public BadPlatformOrDevicePanel(String platform, String device, ProjectKind kind) {
        this (platform, device, true, kind);
    }

    public BadPlatformOrDevicePanel(String platform, String device, boolean showDontShowAgainCheckbox, ProjectKind kind) {
        super (new GridBagLayout());
        provider.setActiveDevice(device);
        provider.setPlatformName(platform);
        pnl = new PlatformAndDevicePanel(provider);
        pnl.setProjectKind(kind);
        box.setVisible(showDontShowAgainCheckbox);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        add (instructions, gbc);
        gbc.gridy = 1;
        add (pnl, gbc);
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.FIRST_LINE_END;
        int margin = Utilities.isMac() ? 12 : 5;
        setBorder (BorderFactory.createEmptyBorder(margin, margin, margin, margin));
    }

    public static PlatformAndDeviceProvider showDialog (String platformName, String deviceName, boolean showCheckbox, ProjectKind kind) {
        BadPlatformOrDevicePanel p = new BadPlatformOrDevicePanel (platformName, deviceName, showCheckbox, kind);
        if (new DialogBuilder(BadPlatformOrDevicePanel.class).
                setTitle(NbBundle.getMessage(
                BadPlatformOrDevicePanel.class, "TTL_FIX_PLATFORM")). //NOI18N
                setModal(true).
                setContent(p).
                setValidationGroup(p.getValidationGroup()).
                showDialog(DialogDescriptor.OK_OPTION)) {
            return p.provider;
        }
        return null;
    }

    public PlatformAndDeviceProvider getProvider() {
        return provider;
    }

    public ValidationGroup getValidationGroup() {
        return pnl.getValidationGroup();
    }

    public void actionPerformed(ActionEvent e) {
        NbPreferences.forModule(BadPlatformOrDevicePanel.class).putBoolean(
                PREFS_KEY_DONT_SHOW_DLG, box.isSelected());
    }

    public static boolean isShowBrokenPlatformDialog() {
        return Boolean.getBoolean ("JCProjectTest") ? false :
            NbPreferences.forModule(BadPlatformOrDevicePanel.class).getBoolean(
                PREFS_KEY_DONT_SHOW_DLG, true);
    }

}
