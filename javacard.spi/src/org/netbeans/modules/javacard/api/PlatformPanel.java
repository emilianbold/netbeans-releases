/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.api;

import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.swing.layouts.SharedLayoutPanel;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.explorer.view.ChoiceView;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.PlatformAndDeviceProvider;
import org.openide.util.HelpCtx;

/**
 * Panel which allows the user to select the platform they will run with.
 *
 * @author Tim Boudreau
 */
final class PlatformPanel extends SharedLayoutPanel implements ActionListener {

    private final ChoiceView choice = new ChoiceView();
    private final JLabel lbl = new JLabel(NbBundle.getMessage(PlatformPanel.class,
            "TTL_JAVACARD_PLATFORM")); //NOI18N
    private final JButton button = new JButton(NbBundle.getMessage(PlatformPanel.class,
            "LBL_MANAGE_PLATFORMS")); //NOI18N

    public PlatformPanel() {
        this(null);
    }
    PlatformAndDeviceProvider props;

    public PlatformPanel(PlatformAndDeviceProvider props) {
        this.props = props;
        add(lbl);
        add(choice);
        add(button);

        button.addActionListener(this);
        Mnemonics.setLocalizedText(button, button.getText());
        Mnemonics.setLocalizedText(lbl, lbl.getText());
        lbl.setLabelFor(choice);
        if (props != null) {
            setPlatformFrom(props);
        }
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.SettingUpJavaCardPlatform"); //NOI18N
    }
    boolean initializing;

    public String getActiveDevice() {
        return props.getActiveDevice();
    }

    public String getPlatformName() {
        return props.getPlatformName();
    }

    public void setPlatformFrom(PlatformAndDeviceProvider props) {
        this.props = props;
    }

    private JavacardPlatform getSelectedPlatform() {
        Provider provider = (Provider) SwingUtilities.getAncestorOfClass(ExplorerManager.Provider.class, this);
        if (provider != null) {
            ExplorerManager mgr = provider.getExplorerManager();
            Node[] n = mgr.getSelectedNodes();
            return n.length == 0 ? null : n[0].getLookup().lookup(JavacardPlatform.class);
        }
        return null;
    }

    public void actionPerformed(ActionEvent e) {
        JavacardPlatform toSelect = getSelectedPlatform();
        PlatformsCustomizer.showCustomizer(toSelect);
    }
}
