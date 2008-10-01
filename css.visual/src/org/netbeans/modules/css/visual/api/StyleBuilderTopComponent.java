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
package org.netbeans.modules.css.visual.api;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.modules.css.visual.api.CssRuleContext;
import org.netbeans.modules.css.visual.api.StyleBuilderPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.css.editor.model.CssRule;
import org.netbeans.modules.css.visual.ui.StyleBuilderAction;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.Utilities;

/**
 * CssStyleBuilder TopComponent
 *
 * @author Marek Fukala
 */
public final class StyleBuilderTopComponent extends TopComponent {
    
    //StyleBuilder UI states
    /** Model is updating, show wait clocks.*/
    public static final int MODEL_UPDATING = 1;
    /** Model OK, UI works.*/
    public static final int MODEL_OK = 2;
    /** Model broken, error panel shown. */
    public static final int MODEL_ERROR = 3;
    /** Model OK, but no rule selected, show warning panel */
    public static final int OUT_OF_RULE = 4;
    
    
    private static final String DEFAULT_TC_NAME = NbBundle.getMessage(StyleBuilderAction.class, "CTL_CSSStyleBuilderTopComponent");
    
    private static StyleBuilderTopComponent instance;
    
    /** path to the icon used by the component and its open action */
    private static final String ICON_PATH = "org/netbeans/modules/css/resources/style_builder_view_toolbar.png"; //NOI18N
    
    private static final String PREFERRED_ID = "StyleBuilderTC"; //NOI18N
    
    private StyleBuilderPanel styleBuilderPanel = StyleBuilderPanel.createInstance();
    
    private JPanel BROKEN_MODEL_PANEL, NO_RULE_SELECTED_PANEL;
    
    private StyleBuilderTopComponent() {
        initComponents();
        
        setToolTipText(NbBundle.getMessage(StyleBuilderAction.class, "HINT_CSSStyleBuilderTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        
        NO_RULE_SELECTED_PANEL = makeMsgPanel(NbBundle.getMessage(StyleBuilderAction.class, "Out_Of_Rule"));
        BROKEN_MODEL_PANEL = makeMsgPanel(NbBundle.getMessage(StyleBuilderAction.class, "Broken_Model"));
        
        setPanelMode(OUT_OF_RULE);
    }
    
     public void setContent(CssRuleContext content){
        CssRule rule = content.selectedRule();
        setName((rule != null ? rule.name() + " - " : "") + DEFAULT_TC_NAME);//NOI18N
        styleBuilderPanel.setContent(content);
    }
    
    public void setPanelMode(int mode) {
        JPanel shownPanel = null;
        switch(mode) {
        case MODEL_OK:
            styleBuilderPanel.setCursor(null);
            removeAll();
            add(styleBuilderPanel, java.awt.BorderLayout.CENTER);
            break;
        case MODEL_ERROR:
            removeAll();
            setName(DEFAULT_TC_NAME);//set default TC name
            add(BROKEN_MODEL_PANEL, java.awt.BorderLayout.CENTER);
            break;
        case MODEL_UPDATING:
            styleBuilderPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            break;
        case OUT_OF_RULE:
            setName(DEFAULT_TC_NAME);//set default TC name
            removeAll();
            add(NO_RULE_SELECTED_PANEL, java.awt.BorderLayout.CENTER);
            break;
        default:
            throw new IllegalArgumentException("Invalid StyleBuilder mode = " + mode); //NOI18N
        }
        validate();
        repaint();
    }
      
    private JPanel makeMsgPanel(String message) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setLayout(new BorderLayout());
        JLabel msgLabel = new JLabel(message);
        p.add(msgLabel, BorderLayout.CENTER);
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        return p;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized StyleBuilderTopComponent getDefault() {
        if (instance == null) {
            instance = new StyleBuilderTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the CSSStyleBuilderTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized StyleBuilderTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(StyleBuilderTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");//NOI18N
            return getDefault();
        }
        if (win instanceof StyleBuilderTopComponent) {
            return (StyleBuilderTopComponent)win;
        }
        Logger.getLogger(StyleBuilderTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");//NOI18N
        return getDefault();
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    public void componentActivated() {
        super.componentActivated();
    }
    
    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return StyleBuilderTopComponent.getDefault();
        }
    }
    
   
}