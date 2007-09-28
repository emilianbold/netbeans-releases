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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.completion.cplusplus;

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
 * @author Vladimir Voskresensky
 *
 * this is copy of NbScrollJavaDocPane
 */

public class CCScrollDocPane extends ScrollJavaDocPane {

    private JToolBar toolbar;    
    private ImageIcon iBack, iForward, iGoToSource, iShowWeb;
    private JButton bBack, bForward, bGoToSource, bShowWeb;    
    private static final String BACK = "org/netbeans/modules/cnd/completion/cplusplus/resources/back.gif"; //NOI18N
    private static final String FORWARD = "org/netbeans/modules/cnd/completion/cplusplus/resources/forward.gif"; //NOI18N
    private static final String GOTO_SOURCE = "org/netbeans/modules/cnd/completion/cplusplus/resources/gotosource.gif"; //NOI18N
    private static final String SHOW_WEB = "org/netbeans/modules/cnd/completion/cplusplus/resources/htmlView.gif"; //NOI18N
    private  CompletionJavaDoc cjd;
    
    /** Creates a new instance of NbScrollCompletionPane */
    public CCScrollDocPane(ExtEditorUI extEditorUI) {
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
            bBack.setToolTipText(NbBundle.getBundle(CCScrollDocPane.class).getString("HINT_javadoc_browser_back_button")); //NOI18N
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
            bForward.setToolTipText(NbBundle.getBundle(CCScrollDocPane.class).getString("HINT_javadoc_browser_forward_button")); //NOI18N
            bForward.setMargin(new Insets(0, 0, 0, 0));
            toolbar.add(bForward, gdc);
        }
        
        gdc.gridx = 2;
        gdc.gridy = 0;
        gdc.anchor = GridBagConstraints.WEST;
        
        iShowWeb = resolveIcon(SHOW_WEB);
        if (iShowWeb !=null){
            
            bShowWeb = new BrowserButton(iShowWeb);
            bShowWeb.setToolTipText(NbBundle.getBundle(CCScrollDocPane.class).getString("HINT_javadoc_browser_show_web_button")); //NOI18N
            bShowWeb.addMouseListener(new MouseEventListener(bShowWeb));
            bShowWeb.setContentAreaFilled(false);
            bShowWeb.setMargin(new Insets(0, 0, 0, 0));
            toolbar.add(bShowWeb, gdc);
        }
        
        gdc.gridx = 3;
        gdc.gridy = 0;
        gdc.weightx = 1.0;
        gdc.anchor = GridBagConstraints.WEST;        
        
        iGoToSource = resolveIcon(GOTO_SOURCE);
        if (iGoToSource !=null){
            bGoToSource = new BrowserButton(iGoToSource);
            bGoToSource.setToolTipText(NbBundle.getBundle(CCScrollDocPane.class).getString("HINT_javadoc_browser_goto_source_button")); //NOI18N
            bGoToSource.addMouseListener(new MouseEventListener(bGoToSource));
            bGoToSource.setContentAreaFilled(false);
            bGoToSource.setMargin(new Insets(0, 0, 0, 0));
            toolbar.add(bGoToSource, gdc);
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
    
    public void setGoToSourceEnabled(boolean enable){
        bGoToSource.setEnabled(enable);
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
            }else if(button.equals(bGoToSource)){
                cjd.goToSource();
            }else if (button.equals(bShowWeb)){
                cjd.openInExternalBrowser();
            }
        }
    }
    
    
}
