
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * TabbedWidget.java
 *
 * Created on March 28, 2007, 2:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import javax.swing.AbstractAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.view.layout.TableLayout;

/**
 *
 * @author Ajit Bhate
 */
public class TabbedPaneWidget extends Widget {
    
    private static final Color TAB_BORDER_COLOR = new Color(169, 197, 235);
    private static final Color SELECTED_TAB_COLOR = Color.WHITE;
    private static final Color TAB_COLOR = new Color(232,232,232);
        
    private Widget tabs;
    private Widget contentWidget;
    
    private ButtonWidget selectedTab;
    
    /**
     *
     * @param scene
     */
    public TabbedPaneWidget(Scene scene) {
        super(scene);
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 1));
        tabs = new Widget(scene);
        addChild(tabs);
        contentWidget = new Widget(scene);
        contentWidget.setLayout(LayoutFactory.createCardLayout(contentWidget));
        contentWidget.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 1, TAB_BORDER_COLOR));
        addChild(contentWidget);
    }
    
    /**
     * Adds given TabWidget to this TabbedPaneWidget
     * @param tabWidget the TabWidget to be added.
     */
    public void addTab(TabWidget tabWidget) {
        addTab(tabWidget.getTitle(),tabWidget.getIcon(),tabWidget.getComponentWidget());
    }

    /**
     *
     * @param tabTitle
     * @param tabIcon
     * @param tabComponent
     */
    public void addTab(String tabTitle, Image tabIcon, final Widget tabComponent) {
        contentWidget.addChild(tabComponent);
        final ButtonWidget tab = new ButtonWidget(getScene(), tabIcon, tabTitle) {
            protected boolean isAimingAllowed() {
                return false;
            }
        };
//        tab.setLayout(LayoutFactory.createOverlayLayout());
//        tab.getLabelWidget().setAlignment(LabelWidget.Alignment.CENTER);
        tab.setBorder(new TabBorder(this,tab));
        tab.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                if(LayoutFactory.getActiveCard(contentWidget) != tabComponent) {
                    if(selectedTab!=null)
                        selectedTab.setLabelFont(getScene().getFont());
                    selectedTab = tab;
                    selectedTab.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
                    LayoutFactory.setActiveCard(contentWidget, tabComponent);
                    contentWidget.revalidate(true);
                }
            }
        });
        tabs.addChild(tab);
        tabs.setLayout(new TableLayout(tabs.getChildren().size(), 0, 0, 20));
        if(selectedTab==null) {
            selectedTab = tab;
            selectedTab.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
            LayoutFactory.setActiveCard(contentWidget, tabComponent);
        }
    }
    
    private static class TabBorder implements Border {
        private static final int radius = 2;
        private Insets insets = new Insets(2*radius, 3*radius, radius, 3*radius);
        private final TabbedPaneWidget tabbedPane;
        private final Widget tab;
        
        public TabBorder(TabbedPaneWidget tabbedPane,Widget tab) {
            this.tabbedPane=tabbedPane;
            this.tab=tab;
        }
        
        public Insets getInsets() {
            return insets;
        }
        
        public void paint(Graphics2D g2, Rectangle rect) {
            Paint oldPaint = g2.getPaint();
            
            Arc2D arc = new Arc2D.Double(rect.x - radius + 0.5f, rect.y + rect.height - radius *2 +0.5f,
                    radius*2, radius*2, -90, 90, Arc2D.OPEN);
            GeneralPath gp = new GeneralPath(arc);
            arc = new Arc2D.Double(rect.x+radius+0.5f, rect.y+0.5f,
                    radius*4, radius*4, 180, -90, Arc2D.OPEN);
            gp.append(arc,true);
            arc = new Arc2D.Double(rect.x + rect.width - radius*6 +1f, rect.y+0.5f,
                    radius*4, radius*4, 90, -90, Arc2D.OPEN);
            gp.append(arc,true);
            arc = new Arc2D.Double(rect.x + rect.width - radius*2 +1f, rect.y + rect.height - radius*2 +0.5f,
                    radius*2, radius*2, 180, 90, Arc2D.OPEN);
            gp.append(arc,true);
            if (tabbedPane.selectedTab==tab) {
                g2.setPaint(SELECTED_TAB_COLOR);
                g2.fill(gp);
            } else {
                g2.setPaint(TAB_COLOR);
                g2.fill(gp);
                gp.closePath();
            }
            g2.setPaint(TAB_BORDER_COLOR);
            if (tab.getState().isFocused()) {
                Stroke s = g2.getStroke ();
                g2.setStroke (new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_MITER, new float[] {2,2}, 0));
                g2.drawRect (rect.x+radius*3, rect.y+radius*2,rect.width-radius*6,rect.height-radius*4);
                g2.setStroke (s);
            }
            g2.draw(gp);
            
            g2.setPaint(oldPaint);
        }
        
        public boolean isOpaque() {
            return true;
        }
        
    }
}
