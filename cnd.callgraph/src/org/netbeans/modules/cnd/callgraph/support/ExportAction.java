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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.callgraph.support;

import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.visual.widget.Scene;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 */
public class ExportAction extends AbstractAction  implements Presenter.Popup {

    private static final String EXTENSION = "png"; // NOI18N
    private final Scene scene;
    private final JComponent parent;
    private final JMenuItem menuItem;
    
    public ExportAction(Scene scene, JComponent parent) {
        this.scene = scene;
        this.parent = parent;
        putValue(Action.NAME, getString("Export")); // NOI18N
        menuItem = new JMenuItem(this); 
        Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));

    }

    @Override
    public JMenuItem getPopupPresenter() {
        return menuItem;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        print();
    }

    private void print() {
        BufferedImage bi = new BufferedImage(scene.getBounds().width, scene.getBounds().height,
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = bi.createGraphics();
        scene.paint(graphics);
        graphics.dispose();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(getString("ExportGraph")); // NOI18N
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new MyFileFilter());
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith("."+EXTENSION)) { // NOI18N
            file = new File(file.getParentFile(), file.getName() + "."+EXTENSION); // NOI18N
        }
        if (file.exists()) {
            String message = getString("FileExistsMessage"); // NOI18N
            DialogDescriptor descriptor = new DialogDescriptor(
                    MessageFormat.format(message, new Object[]{file.getAbsolutePath()}),
                    getString("FileExists"), true, DialogDescriptor.YES_NO_OPTION, DialogDescriptor.NO_OPTION, null); // NOI18N
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            
            try {
                dialog.setVisible(true);
            } catch (Throwable th) {
                if (!(th.getCause() instanceof InterruptedException)) {
                    throw new RuntimeException(th);
                }
                descriptor.setValue(DialogDescriptor.CLOSED_OPTION);
            } finally {
                dialog.dispose();
            }
            
            if (descriptor.getValue() != DialogDescriptor.YES_OPTION) {
                return;
            }
        }

        try {
            ImageIO.write(bi, EXTENSION, file);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private String getString(String key) {
        return NbBundle.getMessage(getClass(), key);
    }

    private static class MyFileFilter extends FileFilter {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return file.getName().toLowerCase().endsWith("."+EXTENSION); // NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(ExportAction.class, "PNG"); // NOI18N
        }
    }

}
