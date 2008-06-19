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


package org.netbeans.editor.ext;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.text.JTextComponent;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;

import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.Settings;
import javax.swing.JButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.ImageIcon;
import java.net.URL;
import javax.swing.Icon;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridLayout;
import java.awt.ComponentOrientation;
import java.awt.GridBagLayout;
import javax.swing.BoxLayout;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import org.openide.util.NbBundle;

/**
 *  JScrollPane implementation of JavaDocPane.
 *
 *  @author  Martin Roskanin
 *  @since   03/2002
 */
public class ScrollJavaDocPane extends JPanel implements JavaDocPane, SettingsChangeListener{

    protected  ExtEditorUI extEditorUI;
    private JComponent view;
    private  CompletionJavaDoc cjd;
    protected JScrollPane scrollPane = new JScrollPane();
    Border lineBorder;
    
    /** Creates a new instance of ScrollJavaDocPane */
    public ScrollJavaDocPane(ExtEditorUI extEditorUI) {
        
//        new RuntimeException("ScrollJavaDocPane.<init>").printStackTrace();
        
        setLayout(null);
 
        this.extEditorUI = extEditorUI;
        
        // Add the completionJavaDoc view
        cjd = extEditorUI.getCompletionJavaDoc();
        if (cjd!=null){
            JavaDocView javaDocView = cjd.getJavaDocView();
            if (javaDocView instanceof JComponent) {
                if (javaDocView instanceof JEditorPane){
                    ((JEditorPane)javaDocView).addHyperlinkListener(createHyperlinkAction());
                }
                view = (JComponent)javaDocView;
                scrollPane.setViewportView(view);
            }
        
            Settings.addSettingsChangeListener(this);
            setMinimumSize(new Dimension(100,100)); //[PENDING] put it into the options
            setMaximumSize(getMaxPopupSize());        
        }else{
            setMinimumSize(new Dimension(0,0));
            setMaximumSize(new Dimension(0,0));        
        }
        super.setVisible(false);
        add(scrollPane);
        getAccessibleContext().setAccessibleDescription(
                NbBundle.getBundle(org.netbeans.editor.BaseKit.class).
                getString("ACSD_JAVADOC_javaDocPane")); //NOI18N
        
        // !!! virtual method called from contructor!!
        installTitleComponent();
        setBorder(new LineBorder(javax.swing.UIManager.getColor("controlDkShadow"))); //NOI18N
    }
    
    protected HyperlinkAction createHyperlinkAction(){
        return new HyperlinkAction();
    }
    
    public void setBounds(Rectangle r){
        super.setBounds(r);
        scrollPane.setBounds(r.x, 0, r.width+1, r.height );
    }
    
    public void setVisible(boolean visible){
        super.setVisible(visible);
        if (cjd!=null && !visible){
            cjd.clearHistory();
        }
    }
    
    protected ImageIcon resolveIcon(String res){
        ClassLoader loader = this.getClass().getClassLoader();
        URL resource = loader.getResource( res );
        if( resource == null ) resource = ClassLoader.getSystemResource( res );
        return  ( resource != null ) ? new ImageIcon( resource ) : null;
    }

    protected void installTitleComponent() {
    }
    
    private Dimension getMaxPopupSize(){
        Class kitClass = Utilities.getKitClass(extEditorUI.getComponent());
        if (kitClass != null) {
            return (Dimension)SettingsUtil.getValue(kitClass,
                      ExtSettingsNames.JAVADOC_PREFERRED_SIZE,
                      ExtSettingsDefaults.defaultJavaDocAutoPopupDelay);
            
        }
        return ExtSettingsDefaults.defaultJavaDocPreferredSize;
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        if (ExtSettingsNames.JAVADOC_PREFERRED_SIZE.equals(evt.getSettingName())){
            setMaximumSize(getMaxPopupSize());
        }
    }   
    
    public JComponent getComponent() {
        return this;
    }
    
    public void setForwardEnabled(boolean enable) {
    }
    
    public void setBackEnabled(boolean enable) {
    }
    
    public void setShowWebEnabled(boolean enable) {
    }    
    
    
    public JComponent getJavadocDisplayComponent() {
        return scrollPane;
    }
    
    public class BrowserButton extends JButton {
        public BrowserButton() {
            setBorderPainted(false);
            setFocusPainted(false);
        }
        
        public BrowserButton(String text){
            super(text);
            setBorderPainted(false);
            setFocusPainted(false);
        }
        
        public BrowserButton(Icon icon){
            super(icon);
            setBorderPainted(false);
            setFocusPainted(false);
        }
    }
    

    protected  class HyperlinkAction implements HyperlinkListener{
        
        public HyperlinkAction(){
            
        }
        
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e!=null && HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())){
                if (e.getDescription() != null){
                    Object obj = cjd.parseLink(e.getDescription(), null);
                    if (obj!=null){
                        cjd.setContent(obj, false);
                        cjd.addToHistory(obj);
                    } else {
                        obj = (e.getURL() == null) ? e.getDescription() : (Object)e.getURL();
                        cjd.setContent(obj, false);
                    }
                }
            }
        }
    }
    /*
    private class BackAction implements ActionListener{
        public void actionPerformed(ActionEvent evt) {
            if (cjd!=null){
                System.out.println("back");
                cjd.backHistory();
            }
        }
    }

    private class ForwardAction implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (cjd!=null){
                System.out.println("fwd");
                cjd.forwardHistory();
            }
        }
    }
    */
}
