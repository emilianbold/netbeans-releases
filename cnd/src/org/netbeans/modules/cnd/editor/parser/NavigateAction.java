/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package  org.netbeans.modules.cnd.editor.parser;


import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openide.nodes.Node;

import org.openide.explorer.ExplorerManager;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * loosly based on java/src/org/netbeans/modules/java/ui/NavigationAction.java
 */
public class NavigateAction extends CookieAction {

    /**
     * Cookies required for this action to be activated.
     */
    private static final Class[] REQUIRED_COOKIES = new Class[] {
//        SourceCookie.Editor.class
    };
    
    static String getString(String key) {
        return NbBundle.getMessage(NavigateAction.class, key);
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    /**
     * Return the display name for the action.
     */
    public String getName() {
        return getString("LAB_NavigateAction"); // NOI18N
    }

    /**
     * The action accepts only one node with a java source on it.
     */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
        return REQUIRED_COOKIES;
    }
    
    /**
     * performAction will activate the associated choice navigation component.
     */
    protected void performAction(Node[] activatedNodes) {
        // PENDING: activate the presenter -- what presenter ??
    }
    
    public java.awt.Component getToolbarPresenter() {
        java.awt.Component c = new ToolbarPresenter();
        return c;
    }
    
    private static class ToolbarPresenter extends JPanel implements ExplorerManager.Provider {
        
        private static final int FIXED_WIDTH = 220;

        NavigationView      explorerView;
        JLabel              comboLabel;
        ExplorerManager     manager;
        
        ToolbarPresenter() {
            initComponents();
        }
        
        public void addNotify() {
            if (manager == null)
                manager = ExplorerManager.find(this);
            super.addNotify();
        }
        
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
        private void initComponents() {
            setLayout(new BorderLayout(12, 0));

            //if (System.getProperty("os.name", "").toLowerCase().indexOf("windows") == -1)
            //{
                explorerView = new NavigationView();
                explorerView.setMaximumRowCount(15);

                char mnemChar = NavigateAction.getString("LAB_NavigationList_Mnem").charAt(0);
                //explorerView.setToolTipText(NavigateAction.getString("HINT_NavigationList") + " (Alt+" + mnemChar + ')');

                comboLabel = new JLabel();
    //            comboLabel.setText(NavigateAction.getString("LAB_NavigationList"));
                comboLabel.setDisplayedMnemonic(mnemChar);
    //            comboLabel.setToolTipText(NavigateAction.getString("HINT_NavigationList"));
                comboLabel.setLabelFor(explorerView);

                add(explorerView, BorderLayout.CENTER);
                add(comboLabel, BorderLayout.WEST);
            //}
        }
        
        public java.awt.Dimension getMinimumSize() {
            // minimum width to prevent excessive horizontal shrinking
            return new java.awt.Dimension(FIXED_WIDTH, getPreferredSize().height);
        }
        
        public java.awt.Dimension getMaximumSize() {
            // minimum width to prevent moving of the remaining contents of the toolbar
            return new java.awt.Dimension(FIXED_WIDTH, getPreferredSize().height);
        }
    }
}
