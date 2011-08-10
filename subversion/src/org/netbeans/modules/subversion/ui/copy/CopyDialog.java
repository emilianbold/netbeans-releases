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
package org.netbeans.modules.subversion.ui.copy;

import java.awt.Component;
import java.awt.Dialog;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.utils.SVNUrlUtils;

/**
 *
 * @author Tomas Stupka
 */
public abstract class CopyDialog {

    private DialogDescriptor dialogDescriptor;
    private JButton okButton, cancelButton;
    private JPanel panel;
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("(branches|tags)/(.+?)(/.*)?"); //NOI18N
    private static final String BRANCHES_FOLDER = "branches"; //NOI18N
    private static final String TRUNK_FOLDER = "trunk"; //NOI18N
    private static final String BRANCH_TEMPLATE = "[BRANCH_NAME]"; //NOI18N
    private Map<String, JComboBox> urlComboBoxes;
    
    CopyDialog(JPanel panel, String title, String okLabel) {                
        this.panel = panel;
        
        okButton = new JButton(okLabel);
        okButton.getAccessibleContext().setAccessibleDescription(okLabel);
        cancelButton = new JButton(org.openide.util.NbBundle.getMessage(CopyDialog.class, "CTL_Copy_Cancel"));                                      // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CopyDialog.class, "CTL_Copy_Cancel"));    // NOI18N
       
        dialogDescriptor = new DialogDescriptor(panel, title, true, new Object[] {okButton, cancelButton},
              okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(this.getClass()), null);
        okButton.setEnabled(false);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CopyDialog.class, "CTL_Title"));                // NOI18N
    }

    protected void resetUrlComboBoxes() {
        getUrlComboBoxes().clear();
    }
    
    protected void setupUrlComboBox (RepositoryFile repositoryFile, JComboBox cbo, String key) {
        if(cbo==null) {
            return;
        }
        List<String> recentFolders = new LinkedList<String>(Utils.getStringList(SvnModuleConfig.getDefault().getPreferences(), key));
        for (String template : getTemplates(repositoryFile)) {
            recentFolders.remove(template);
            recentFolders.add(0, template);
        }
        ComboBoxModel rootsModel = new DefaultComboBoxModel(new Vector<String>(recentFolders));
        cbo.setModel(rootsModel);        
        JTextComponent comp = (JTextComponent) cbo.getEditor().getEditorComponent();
        String txt = comp.getText();
        int pos = txt.indexOf(BRANCH_TEMPLATE);
        if (pos > -1) {
            comp.setCaretPosition(pos);
            comp.moveCaretPosition(pos + BRANCH_TEMPLATE.length());
        }
        cbo.setRenderer(new LocationRenderer(recentFolders));
                
        getUrlComboBoxes().put(key, cbo);
    }    
    
    private Map<String, JComboBox> getUrlComboBoxes() {
        if(urlComboBoxes == null) {
            urlComboBoxes = new HashMap<String, JComboBox>();
        }
        return urlComboBoxes;
    }
    
    protected JPanel getPanel() {
        return panel;
    }       
    
    boolean showDialog() {                        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CopyDialog.class, "CTL_Title"));                     // NOI18N        
        
        dialog.setVisible(true);
        boolean ret = dialogDescriptor.getValue()==okButton;
        if(ret) {
            storeValidValues();
        }
        return ret;        
    }        
    
    private void storeValidValues() {
        for (Iterator it = urlComboBoxes.keySet().iterator(); it.hasNext();) {
            String key = (String)  it.next();
            JComboBox cbo = (JComboBox) urlComboBoxes.get(key);
            Object item = cbo.getEditor().getItem();
            if(item != null && !item.equals("")) { // NOI18N
                Utils.insert(SvnModuleConfig.getDefault().getPreferences(), key, (String) item, 8);
            }            
        }                
    }       
    
    protected JButton getOKButton() {
        return okButton;
    }

    private Collection<String> getTemplates (RepositoryFile repositoryFile) {
        List<String> templates = new LinkedList<String>();
        String relativePath = SVNUrlUtils.getRelativePath(repositoryFile.getRepositoryUrl(), repositoryFile.getFileUrl());
        Matcher matcher = TEMPLATE_PATTERN.matcher(relativePath);
        if (matcher.matches()) {
            StringBuilder sb = new StringBuilder(relativePath);
            sb.replace(matcher.start(2), matcher.end(2), BRANCH_TEMPLATE);
            sb.replace(matcher.start(1), matcher.end(1), BRANCHES_FOLDER);
            templates.add(sb.toString());
            sb = new StringBuilder(relativePath);
            sb.replace(0, matcher.end(2), TRUNK_FOLDER);
            templates.add(sb.toString());
        }
        if (relativePath.startsWith(TRUNK_FOLDER + "/")) { //NOI18N
            templates.add(new StringBuilder(relativePath).replace(0, TRUNK_FOLDER.length(), BRANCHES_FOLDER + "/" + BRANCH_TEMPLATE).toString());
        }
        Collections.reverse(templates);
        return templates;
    }

    private static class LocationRenderer extends DefaultListCellRenderer {
        private final HashMap<String, String> empLocations;

        public LocationRenderer (List<String> recentFolders) {
            empLocations = new HashMap<String, String>(recentFolders.size());
            for (String loc : recentFolders) {
                Matcher m = TEMPLATE_PATTERN.matcher(loc);
                StringBuffer sb = new StringBuffer();
                if (m.matches()) {
                    m.appendReplacement(sb, m.group(1) + "/<strong>" + m.group(2) + "</strong>" + (m.group(3) == null ? "" : m.group(3))); //NOI18N
                    m.appendTail(sb);
                } else if (loc.startsWith(TRUNK_FOLDER + "/")) { //NOI18N
                    sb = new StringBuffer(loc).replace(0, TRUNK_FOLDER.length(), "<strong>" + TRUNK_FOLDER + "</strong>"); //NOI18N
                }
                if (sb.length() > 0) {
                    empLocations.put(loc, "<html>" + sb.toString() + "</html>"); //NOI18N
                }
            }
        }
        
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String html;
            if (value instanceof String && (html = empLocations.get(((String) value))) != null) {
                value = html;
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
