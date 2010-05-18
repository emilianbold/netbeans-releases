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
package org.netbeans.modules.javacard.project;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.netbeans.modules.javacard.project.customizer.AppletProjectProperties;
import org.netbeans.modules.javacard.project.customizer.ClassicAppletProjectProperties;
import org.netbeans.modules.javacard.project.customizer.WebProjectProperties;
import org.netbeans.modules.javacard.spi.ProjectKind;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import org.openide.util.HelpCtx;

public class JCCustomizerProvider implements CustomizerProvider {

    private final JCProject project;
    private JCProjectProperties uiProperties;
    private final PropertyEvaluator evaluator;
    private final AntProjectHelper antHelper;

    private Reference<Dialog> dialog;

    public JCCustomizerProvider(JCProject project, PropertyEvaluator evaluator,
            GeneratedFilesHelper genFileHelper, AntProjectHelper antHelper) {
        this.project = project;
        this.evaluator = evaluator;
        this.antHelper = antHelper;
    }

    private Dialog getDialog() {
        return dialog == null ? null : dialog.get();
    }

    public void showCustomizer() {
        Dialog dlg = getDialog();
        if (dlg == null) {
            JCProject realProject = project.getLookup().lookup(JCProject.class);
            assert realProject != null;
            ProjectKind kind = realProject.kind();
            assert kind != null;
            uiProperties = createProjectProperties(kind,
                    realProject,
                    evaluator, antHelper);
            Lookup context = Lookups.fixed(project, uiProperties);
            OptionsListener listener = new OptionsListener();
            dlg = ProjectCustomizer.createCustomizerDialog(
                    project.kind().customizerPath(), context, null,
                    listener, new HelpCtx(
                    "org.netbeans.modules.javacard." + //NOI18N
                    "JavaCard3ProjectPropertiesDialogBox")); //NOI18N
            dlg.addWindowListener(listener);
            this.dialog = new SoftReference<Dialog>(dlg);
        }
        dlg.setVisible(true);
    }
    
    /** 
     * Listens to the actions on the Customizer's option buttons 
     */
    private class OptionsListener extends WindowAdapter 
            implements ActionListener {
    
        /*
         * Listening to OK button
         */
        public void actionPerformed(ActionEvent e) {
            uiProperties.storeProperties();
            Dialog dlg = getDialog();
            // Close & dispose the the dialog
            if ( dlg != null ) {
                dlg.setVisible(false);
                dlg.dispose();
            }
        }        
        
        /*
         * Listening to window events
         */
        @Override
        public void windowClosed(WindowEvent e) {
            dialog = null;
        }    
        
        @Override
        public void windowClosing(WindowEvent e) {
            Dialog dlg = getDialog();
            if ( dlg != null ) {
                dlg.setVisible(false);
                dlg.dispose();
            }
        }
    }

    public static JCProjectProperties createProjectProperties(ProjectKind kind, 
            JCProject project, PropertyEvaluator evaluator,
            AntProjectHelper antHelper) {
        switch (kind) {
            case WEB:
                return new WebProjectProperties(project, evaluator, antHelper);
            case EXTENDED_APPLET:
                return new AppletProjectProperties(project);
            case CLASSIC_APPLET:
            case CLASSIC_LIBRARY:
                return new ClassicAppletProjectProperties(project);
            case EXTENSION_LIBRARY:
                return new JCProjectProperties(project);
            default:
                throw new AssertionError();
        }
    }
}
