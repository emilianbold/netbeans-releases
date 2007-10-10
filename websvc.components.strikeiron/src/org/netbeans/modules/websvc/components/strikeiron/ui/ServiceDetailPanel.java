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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.components.strikeiron.ui;

import com.strikeiron.search.MarketPlaceService;
import java.awt.Container;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.modules.websvc.components.ServiceData;

/**
 *
 * @author nam
 */
public class ServiceDetailPanel extends JTextPane {
    private JScrollPane scrollPane;
    private HeaderPanel header;
    private JLabel title;
    private JTextField tfPackageName;
    private ServiceTableModel model;
    private ServiceData current;

    public ServiceDetailPanel() {
        initHtmlKit();
        header = new HeaderPanel();
        title = header.getTitle();
        tfPackageName = header.getPackageNameTF();
    }

    private void initHtmlKit() {
        HTMLEditorKit htmlkit = new HTMLEditorKit();
        StyleSheet css = htmlkit.getStyleSheet();
        
        if (css.getStyleSheets() == null) {
            StyleSheet css2 = new StyleSheet();
            Font f = new JList().getFont();
            int size = f.getSize();
            css2.addRule(new StringBuffer("body { font-size: ").append(size) // NOI18N
                    .append("; font-family: ").append(f.getName()).append("; }").toString()); // NOI18N
            css2.addStyleSheet(css);
            htmlkit.setStyleSheet(css2);
        }
        
        setEditorKit(htmlkit);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        getScrollPane();
    }
    
    JScrollPane getScrollPane() {
        if (scrollPane == null) {
            Container p = getParent();
            if (p instanceof JViewport) {
                Container gp = p.getParent();
                if (gp instanceof JScrollPane) {
                    scrollPane = (JScrollPane)gp;
                }
            }            
        }
        return scrollPane;
    }
    
    void setCurrentService(ServiceData service) {
        if (current != null) {
            current.setPackageName(tfPackageName.getText());
        }
        current = service;
        if (current != null) {
            getScrollPane().setColumnHeaderView(header);
            tfPackageName.setText(current.getPackageName());
            title.setText(current.getServiceName());
        }
    }
    
    ServiceData getCurrentService() {
        return current;
    }
    
    String getPackageName() {
        return tfPackageName.getText();
    }
    
    ServiceTableModel getModel() {
        if (model == null) {
            Container p = getScrollPane().getParent();
            while (p != null) {
                if (p instanceof FindServicelDialog) {
                    model = ((FindServicelDialog)p).getModel();
                    break;
                }
                p = p.getParent();
            }
        }
        return model;
    }
    
    public void setTitle(String value) {
        getScrollPane().setColumnHeaderView(value != null ? header : null);
        //getScrollPane().setCorner(JScrollPane.UPPER_RIGHT_CORNER, value != null ? rightCornerHeader : null);
        if (value != null) {                            
            title.setText("<html><h3>"+value+"</h3></html>");
        }
    }
    
}
