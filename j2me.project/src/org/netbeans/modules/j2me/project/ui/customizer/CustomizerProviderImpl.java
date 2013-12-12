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

package org.netbeans.modules.j2me.project.ui.customizer;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2me.project.J2MEProject;
import org.netbeans.modules.java.api.common.project.ui.customizer.CustomizerProvider3;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 *
 * @author Tomas Zezula
 */
public final class CustomizerProviderImpl implements CustomizerProvider3 {
    
    private static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-j2me-project/Customizer";   //NOI18N

    private final J2MEProject project;
    private Dialog currentDialog;

    public CustomizerProviderImpl(@NonNull final J2MEProject project) {
        Parameters.notNull("project", project); //NOI18N
        this.project = project;
    }

    @Override
    public void showCustomizer() {
        showCustomizer(null, null);
    }

    @Override
    public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {
        if (currentDialog != null) {
            currentDialog.setVisible(true);
            return;
        } else {
            try {
                WaitCursor.show();
                final J2MEProjectProperties uiProperties = createProperties();
                final Lookup context = Lookups.fixed(new Object[] {
                    project,
                    uiProperties,
                    new SubCategoryProvider(preselectedCategory, preselectedSubCategory)
                });
                final OptionListener listener = new OptionListener(context);
                final StoreListener storeListener = new StoreListener(context);
                currentDialog = ProjectCustomizer.createCustomizerDialog(
                    CUSTOMIZER_FOLDER_PATH,
                    context,
                    preselectedCategory,
                    listener,
                    storeListener,
                    null);
                currentDialog.addWindowListener(listener);
                currentDialog.setTitle(NbBundle.getMessage(
                    CustomizerProviderImpl.class,
                    "LBL_Customizer_Title",
                    ProjectUtils.getInformation(project).getDisplayName()));
            } finally {
                WaitCursor.hide();
            }
            currentDialog.setVisible(true);
        }
    }

    @Override
    public void cancelCustomizer() {
        hide();
        clear();
    }

    private void hide() {
        if ( currentDialog != null ) {
            currentDialog.setVisible(false);
            currentDialog.dispose();
        }
    }

    private void clear() {
        if ( currentDialog != null ) {
            currentDialog = null;
        }
    }

    private J2MEProjectProperties createProperties() {
        return new J2MEProjectProperties(project);
    }

    private static class WaitCursor implements Runnable {

        private final boolean show;

        private WaitCursor(final boolean show) {
            this.show = show;
        }

        static void show() {
            Mutex.EVENT.readAccess(new WaitCursor(true));
        }

        static void hide() {
            Mutex.EVENT.readAccess(new WaitCursor(false));
        }

        @Override
        public void run() {
            final JFrame f = (JFrame) WindowManager.getDefault().getMainWindow();
            Component c = f.getGlassPane();
            c.setVisible(show);
            c.setCursor(
                show ?
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) :
                null);
        }
    }

    private final class OptionListener extends WindowAdapter implements ActionListener {

        private final Lookup ctx;

        OptionListener(@NonNull final Lookup context) {
            this.ctx = context;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            J2MEProjectProperties props = ctx.lookup(J2MEProjectProperties.class);
            props.collectData();
        }

        @Override
        public void windowClosing(WindowEvent e) {
            hide();
        }

        @Override
        public void windowClosed(WindowEvent e) {
            clear();
        }
    }

    private static final class StoreListener implements ActionListener {

        private final Lookup ctx;

        StoreListener (@NonNull final Lookup context) {
            this.ctx = context;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            J2MEProjectProperties props = ctx.lookup(J2MEProjectProperties.class);
            props.storeData();
        }

    }
    
    static final class SubCategoryProvider {
        private String subcategory;
        private String category;

        SubCategoryProvider(String category, String subcategory) {
            this.category = category;
            this.subcategory = subcategory;
        }

        public String getCategory() {
            return category;
        }

        public String getSubcategory() {
            return subcategory;
        }
    }

}
