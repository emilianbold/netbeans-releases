/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.toolsui.api;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.dlight.toolsui.ToolsManagerPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author ak119685
 */
public final class ToolsCustomizerPanelDisplayer {

    private final Object[] extraOptions;
    private final ActionListener extraOptionsListener;
    private final ToolsManagerPanel customizer;

    public ToolsCustomizerPanelDisplayer(String preferredConfigurationName, Object[] extraOptions, ActionListener extraOptionsListener) {
        this.extraOptions = extraOptions;
        this.extraOptionsListener = extraOptionsListener;
        this.customizer = new ToolsManagerPanel(preferredConfigurationName);
    }

    public boolean showToolsCustomizer() {
        Object[] options = new Object[extraOptions == null ? 2 : 2 + extraOptions.length];
        int i = 0;

        if (extraOptions != null) {
            for (Object option : extraOptions) {
                options[i++] = option;
            }
        }

        options[i++] = DialogDescriptor.OK_OPTION;
        options[i++] = DialogDescriptor.CANCEL_OPTION;


        DialogDescriptor descriptor = new DialogDescriptor(
                customizer,
                NbBundle.getMessage(ToolsManagerPanel.class, "TXT_ToolsCustomizer"), // NOI18N
                true,
                options,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null, new OptionsListener(customizer, extraOptionsListener));

        descriptor.setClosingOptions(new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION});

        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);

        try {
            dlg.setVisible(true);
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                return customizer.apply();
            } else {
                return false;
            }
        } finally {
            dlg.dispose();
        }
    }

    private static class OptionsListener implements ActionListener {

        private final ActionListener delegate;
        private final ToolsManagerPanel customizer;

        public OptionsListener(ToolsManagerPanel customizer, ActionListener delegate) {
            this.customizer = customizer;
            this.delegate = delegate;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (delegate == null) {
                return;
            }

            e.setSource(customizer.getSelectedTool());
            delegate.actionPerformed(e);
        }
    }
}
