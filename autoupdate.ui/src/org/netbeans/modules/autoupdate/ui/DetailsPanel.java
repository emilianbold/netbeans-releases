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
package org.netbeans.modules.autoupdate.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 *
 * @author  Jiri Rechtacek, Radek Matous
 */
public class DetailsPanel extends JTextPane  {
    private JScrollPane scrollPane;
    private HeaderPanel header;
    private JLabel title;
    private JButton button;
    private JButton button2;
    private JPanel rightCornerHeader;
    
    public DetailsPanel() {
        initComponents2();
        HTMLEditorKit htmlkit = new HTMLEditorKit();
        // override the Swing default CSS to make the HTMLEditorKit use the
        // same font as the rest of the UI.
        
        // XXX the style sheet is shared by all HTMLEditorKits.  We must
        // detect if it has been tweaked by ourselves or someone else
        // (code completion javadoc popup for example) and avoid doing the
        // same thing again
        
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
        addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    assert hlevt.getURL() != null;
                    Utilities.showURL(hlevt.getURL());
                }
            }
        });
        setEditable(false);
        setPreferredSize(new Dimension(300, 80));
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
        getScrollPane().setCorner(JScrollPane.UPPER_RIGHT_CORNER, value != null ? rightCornerHeader : null);
        if (value != null) {                            
            title.setText("<html><h3>"+value+"</h3></html>");
        }
    }
    public void setActionListener(Action action) {
        button.setVisible(action != null);
        button.setEnabled(action != null);
        if (action != null) {
            button.setAction(action);
        }
    }
    
    public void setActionListener2(Action action) {
        button2.setVisible(action != null);
        button2.setEnabled(action != null);
        if (action != null) {
            button2.setAction(action);
        }
    }
    

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        title.setEnabled(enabled);
        button.setEnabled(enabled);
    }
 
    
    javax.swing.JEditorPane getDetails() {
        return this;
    }
    
    HeaderPanel getHeader() {
        return header;
    }
    
    private void initComponents2() {
        header = new HeaderPanel();
        title = header.getTitle();
        button = header.getButton();
        button2 = header.getButton2();
        Border outsideBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray);        
        Border insideBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
        header.setBorder(compoundBorder);
        button.setVisible(false);
        button2.setVisible(false);        
        rightCornerHeader  = new JPanel();        
        rightCornerHeader.setBorder(compoundBorder);
        
        header.setBackground(UnitTable.getDarkerColor(getBackground()));
        rightCornerHeader.setBackground(UnitTable.getDarkerColor(getBackground()));
        setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 0));
    }
}