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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xslt.project.wizard.element;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xslt.project.XsltproConstants;
import org.netbeans.modules.xslt.tmap.ui.editors.FileDialog;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformEditorPanel extends JPanel {
    private FileObject myCurrFolder;
    private Project myProject;
    /** A list of event listeners for this component. */
    protected EventListenerList myListenerList = new EventListenerList();
    
    public TransformEditorPanel(FileObject folder, Project project) {
        super();
        myCurrFolder = folder;
        myProject = project;
        initComponents();
        initListeners();
    }

    public void addActionListener(ActionListener l) {
        myListenerList.add(ActionListener.class, l);
    }
    
    public void removeActionListener(ActionListener l) {
        myListenerList.remove(ActionListener.class, l);
    }

    private void fireActionPerformed(ActionEvent ev) {
        assert SwingUtilities.isEventDispatchThread();
        ActionListener[] listeners = myListenerList.getListeners(ActionListener.class);
        for (ActionListener l : listeners) {
            l.actionPerformed(ev);
        }
    } 
    
    private void initComponents() {
        GridBagConstraints c = new GridBagConstraints();
        myTextField = new JTextField();
        myChooseButton = createChooseButton();
        
        setLayout(new GridBagLayout());

        a11y(myTextField, "ACSN_LBL_Transform", "ACSD_LBL_Transform"); // NOI18N
        myTextField.setOpaque(true);
        myTextField.setBorder(new EmptyBorder(new java.awt.Insets(0, 1, 0, 0)));
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        add(myTextField, c);        
        
        myChooseButton.setFocusCycleRoot(true);
        myChooseButton.setMargin(new java.awt.Insets(0, MEDIUM_SIZE, 0, MEDIUM_SIZE));
        myChooseButton.setMaximumSize(new java.awt.Dimension(16, 16));
        myChooseButton.setMinimumSize(new java.awt.Dimension(16, 16));
        myChooseButton.setPreferredSize(new java.awt.Dimension(16, 16));

        c.weightx = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(myChooseButton, new GridBagConstraints());        
    }

    private void initListeners() {
        assert myTextField != null;
        myTextField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fireActionPerformed(e);
            }
        });
    
    }
    
    public JTextField getTextField() {
        return myTextField;
    }
    
    protected final JButton createChooseButton() {

        JButton browseButton = createButton(
                new ButtonAction(
                i18n(TransformEditorPanel.class, "LBL_CIRCLES"), // NOI18N
                i18n(TransformEditorPanel.class, "TLT_Browse")) { // NOI18N


                    public void actionPerformed(ActionEvent event) {

                        FileFilter xsltFileFilter = new FileFilter() {

                            public boolean accept(File f) {
                                if (f.isDirectory()) {
                                    return true;
                                }
                                String extension = FileUtil.getExtension(f.getName());
                                if (XsltproConstants.XSLT_EXTENSION.equals(extension) || XsltproConstants.XSLT_EXTENSION2.equals(extension)) {
                                    return true;
                                }
                                return false;
                            }

                            public String getDescription() {
                                return i18n(TransformEditorPanel.class, "LBL_Transformation_Filter_Descr"); // NOI18N

                            }
                        };

                        JFileChooser fileChooser = new JFileChooser(FileUtil.toFile(myCurrFolder),
                                new FileDialog(myProject));
                        fileChooser.setFileFilter(xsltFileFilter);

                        int result = fileChooser.showOpenDialog(TransformEditorPanel.this);
                        File selectedFile = fileChooser.getSelectedFile();

                        if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                            String relPath = "";
                            selectedFile = FileUtil.normalizeFile(selectedFile);

                            if (!selectedFile.exists()) {
                                relPath = FileUtil.getRelativePath(myCurrFolder,
                                        FileUtil.toFileObject(selectedFile.getParentFile()));
                                relPath = (relPath == null || "".equals(relPath)
                                        ? "" : relPath + "/") + selectedFile.getName();
                            } else {
                                relPath = FileUtil.getRelativePath(myCurrFolder,
                                        FileUtil.toFileObject(selectedFile));
                            }
                            myTextField.setText(relPath);
                        }
                        
                        fireActionPerformed(event);
                    }
                });
        return browseButton;
    }

    private JButton myChooseButton;
    private JTextField myTextField;
}
