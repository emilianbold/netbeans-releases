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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.design;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.HashMap;
import javax.swing.JScrollPane;

/**
 *
 * @author aa160298
 */
public class DesignViewLayout implements java.awt.LayoutManager {

    private DesignView designView;

    public DesignViewLayout(DesignView designView) {
        this.designView = designView;
    }

    public void addLayoutComponent(String name, Component comp) {

    }

    public void removeLayoutComponent(Component comp) {

    }

    public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(parent.getWidth(), parent.getHeight());
    }

    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            int w = parent.getWidth();
            int h = parent.getHeight();
            int w1 = 0;
            
            Component consumers = designView.getConsumersView();
            {
                JScrollPane sp = (JScrollPane) consumers.getParent().getParent();


                Dimension dim = consumers.getPreferredSize();

                Insets insets = sp.getBorder().getBorderInsets(consumers);
                
                w1 = dim.width + sp.getVerticalScrollBar().getWidth() + insets.left +  insets.right;
                
                sp.setBounds(BORDER, BORDER, w1, h - BORDER * 2);
            }
            int w2;
            Component providers = designView.getProvidersView();
            {
                JScrollPane sp = (JScrollPane) providers.getParent().getParent();


                Dimension dim = providers.getPreferredSize();
                Insets insets = sp.getBorder().getBorderInsets(providers);
                w2 =  dim.width + sp.getVerticalScrollBar().getWidth() + insets.left + insets.right;
                sp.setBounds(w - BORDER - w2, BORDER, w2, h - BORDER * 2);
            }
            

            Component process = designView.getProcessView();
            {
                JScrollPane sp = (JScrollPane) process.getParent().getParent();

                sp.setBounds(w1 + BORDER * 2, BORDER, w - w1 - w2 - BORDER * 4, h - BORDER * 2 );
            }

        }
    }
    private static final int BORDER = 3;
}
