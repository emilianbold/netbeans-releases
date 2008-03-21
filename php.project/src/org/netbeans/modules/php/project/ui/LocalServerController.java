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

package org.netbeans.modules.php.project.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Component (non-UI) for local servers.
 * @author Tomas Mysik
 */
public class LocalServerController {

    private final JComboBox localServerComboBox;
    private final JButton localServerBrowseButton;
    private final WebFolderNameProvider webFolderNameProvider;
    private final String browseDialogTitle;
    final ChangeSupport changeSupport = new ChangeSupport(this);
    private /*final*/ MutableComboBoxModel localServerComboBoxModel;
    private final LocalServer.ComboBoxEditor localServerComboBoxEditor;

    public LocalServerController(JComboBox localServerComboBox, JButton localServerBrowseButton,
            WebFolderNameProvider webFolderNameProvider, String browseDialogTitle, LocalServer... defaultLocalServers) {
        assert localServerComboBox.isEditable() : "localServerComboBox has to be editable";

        this.localServerComboBox = localServerComboBox;
        this.localServerBrowseButton = localServerBrowseButton;

        this.webFolderNameProvider = webFolderNameProvider;
        this.browseDialogTitle = browseDialogTitle;
        localServerComboBoxModel = new LocalServer.ComboBoxModel(defaultLocalServers);
        localServerComboBoxEditor = new LocalServer.ComboBoxEditor();

        localServerComboBox.setModel(localServerComboBoxModel);
        localServerComboBox.setRenderer(new LocalServer.ComboBoxRenderer());
        localServerComboBox.setEditor(localServerComboBoxEditor);

        registerListeners();
    }

    private void registerListeners() {
        localServerComboBoxEditor.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                changeSupport.fireChange();
            }
        });
        localServerBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.browseLocalServerAction(localServerBrowseButton.getParent(), localServerComboBox,
                        localServerComboBoxModel, webFolderNameProvider.getWebFolderName(), browseDialogTitle);
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public LocalServer getLocalServer() {
        return (LocalServer) localServerComboBox.getSelectedItem();
    }

    public MutableComboBoxModel getLocalServerModel() {
        return localServerComboBoxModel;
    }

    public void setLocalServerModel(MutableComboBoxModel localServers) {
        localServerComboBoxModel = localServers;
        localServerComboBox.setModel(localServerComboBoxModel);
    }

    public void addLocalServer(LocalServer localServer) {
        localServerComboBox.addItem(localServer);
    }

    public void selectLocalServer(LocalServer localServer) {
        localServerComboBox.setSelectedItem(localServer);
    }

    // to enable/disable components
    public void setState(boolean enabled) {
        localServerComboBox.setEnabled(enabled);
        localServerBrowseButton.setEnabled(enabled);
    }

    /**
     * Validate given local server instance, its source root.
     * @param localServer local server to validate.
     * @param type the type for error messages.
     * @return error message or <code>null</null> if source root is ok.
     * @see Utils#validateProjectDirectory(java.lang.String, java.lang.String, boolean)
     */
    public static String validateLocalServer(final LocalServer localServer, String type, boolean allowNonEmpty) {
        if (!localServer.isEditable()) {
            return null;
        }
        String err = null;
        String sourcesLocation = localServer.getSrcRoot();
        File sources = FileUtil.normalizeFile(new File(sourcesLocation));
        if (sourcesLocation.trim().length() == 0
                || !Utils.isValidFileName(sources.getName())) {
            err = NbBundle.getMessage(LocalServerController.class, "MSG_Illegal" + type + "Name");
        } else {
            err = Utils.validateProjectDirectory(sourcesLocation, type, allowNonEmpty);
        }
        return err;
    }
}
