/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.components.strikeiron.ui;

import java.awt.Container;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 *
 * @author nam
 */
public class ServiceDetailPanel extends JTextPane {
    private JScrollPane scrollPane;
    private HeaderPanel header;
    private JLabel title;

    public ServiceDetailPanel() {
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
        header = new HeaderPanel();
        title = header.getTitle();
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
    
    public void setTitle(String value) {
        getScrollPane().setColumnHeaderView(value != null ? header : null);
        //getScrollPane().setCorner(JScrollPane.UPPER_RIGHT_CORNER, value != null ? rightCornerHeader : null);
        if (value != null) {                            
            title.setText("<html><h3>"+value+"</h3></html>");
        }
    }
    
}
