/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.html;

import javax.swing.JToolBar;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import org.netbeans.editor.ext.ScrollJavaDocPane;
import java.awt.event.MouseAdapter;
import javax.swing.JButton;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import org.netbeans.editor.ext.CompletionJavaDoc;
import org.netbeans.editor.ext.ExtEditorUI;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */

public class HTMLScrollJavaDocPane extends ScrollJavaDocPane {
    
    
    private JToolBar toolbar;    
    private ImageIcon iBack, iForward, iShowWeb;
    private JButton bBack, bForward, bShowWeb;    
    private static final String BACK = "org/netbeans/modules/html/editor/resources/back.gif"; //NOI18N
    private static final String FORWARD = "org/netbeans/modules/html/editor/resources/forward.gif"; //NOI18N
    private static final String SHOW_WEB = "org/netbeans/modules/html/editor/resources/htmlView.gif"; //NOI18N
    private  CompletionJavaDoc cjd;
    
    /** Creates a new instance of NbScrollCompletionPane */
    public HTMLScrollJavaDocPane(ExtEditorUI extEditorUI) {
        super(extEditorUI);
        cjd = extEditorUI.getCompletionJavaDoc();
    }

    
    protected ImageIcon resolveIcon(String res){
        return new ImageIcon(org.openide.util.Utilities.loadImage (res));
    }
    
    
    protected void installTitleComponent() {
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        toolbar.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 2, 1, 2)));
        toolbar.setLayout(new GridBagLayout());
        GridBagConstraints gdc = new GridBagConstraints();
        gdc.gridx = 0;
        gdc.gridy = 0;
        gdc.anchor = GridBagConstraints.WEST;
        
        iBack = resolveIcon(BACK);
        if (iBack !=null){
            
            bBack = new BrowserButton(iBack);
            bBack.addMouseListener(new MouseEventListener(bBack));
            bBack.setEnabled(false);
            bBack.setContentAreaFilled(false);
            bBack.setMargin(new Insets(0, 0, 0, 0));
            bBack.setToolTipText(NbBundle.getBundle(this.getClass()).getString("HINT_javadoc_browser_back_button")); //NOI18N
            toolbar.add(bBack, gdc);
        }
        
        gdc.gridx = 1;
        gdc.gridy = 0;
        gdc.anchor = GridBagConstraints.WEST;
        
        iForward = resolveIcon(FORWARD);
        if (iForward !=null){
            bForward = new BrowserButton(iForward);
            bForward.addMouseListener(new MouseEventListener(bForward));
            bForward.setEnabled(false);
            bForward.setContentAreaFilled(false);
            bForward.setToolTipText(NbBundle.getBundle(this.getClass()).getString("HINT_javadoc_browser_forward_button")); //NOI18N
            bForward.setMargin(new Insets(0, 0, 0, 0));
            toolbar.add(bForward, gdc);
        }
        
        gdc.gridx = 2;
        gdc.gridy = 0;
        gdc.weightx = 1.0;
        gdc.anchor = GridBagConstraints.WEST;
        
        iShowWeb = resolveIcon(SHOW_WEB);
        if (iShowWeb !=null){
            
            bShowWeb = new BrowserButton(iShowWeb);
            bShowWeb.setToolTipText(NbBundle.getBundle(this.getClass()).getString("HINT_javadoc_browser_show_web_button")); //NOI18N
            bShowWeb.addMouseListener(new MouseEventListener(bShowWeb));
            bShowWeb.setContentAreaFilled(false);
            bShowWeb.setMargin(new Insets(0, 0, 0, 0));
            toolbar.add(bShowWeb, gdc);
        }
        
        add(toolbar);
    }
    
    public void setBounds(Rectangle r){
        super.setBounds(r);
        scrollPane.setBounds(r.x, 25, r.width, r.height - 25);
        toolbar.setBounds(r.x+1, 1, r.width-2, 24);
    }

    public void setForwardEnabled(boolean enable) {
        bForward.setEnabled(enable);
    }
    
    public void setBackEnabled(boolean enable) {
        bBack.setEnabled(enable);
    }

    public void setShowWebEnabled(boolean enable) {
        bShowWeb.setEnabled(enable);
    }
    
    class MouseEventListener extends MouseAdapter {
        JButton button;
        MouseEventListener(JButton button) {
            this.button = button;
        }
        
        public void mouseEntered(MouseEvent ev) {
            if (button.isEnabled()){
                button.setContentAreaFilled(true);
                button.setBorderPainted(true);
            }
        }
        public void mouseExited(MouseEvent ev) {
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
        }
        
        public void mouseClicked(MouseEvent evt) {
            if (button.equals(bBack)){
                cjd.backHistory();
            }else if(button.equals(bForward)){
                cjd.forwardHistory();
            }else if (button.equals(bShowWeb)){
                cjd.openInExternalBrowser();
            }
        }
    }
    
}
