/*
 * TabbedWidget.java
 *
 * Created on March 28, 2007, 2:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Ajit Bhate
 */
public class TabbedPaneWidget extends Widget {
    
    private static final Color BORDER_COLOR = new Color(160,160,160);
    private static final Color SELECTED_TAB_COLOR = new Color(176,176,176);
    private static final Color TAB_COLOR = new Color(208,208,208);
        
    private Widget tabs;
    private Widget contentWidget;
    
    private Widget selectedTab;
    
    /**
     *
     * @param scene
     */
    public TabbedPaneWidget(Scene scene) {
        super(scene);
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        //setBorder(BorderFactory.createLineBorder(4));
        tabs = new Widget(scene);
        tabs.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        tabs.setBorder(BorderFactory.createEmptyBorder(2,0));
        addChild(tabs);
        contentWidget = new Widget(scene);
        contentWidget.setLayout(LayoutFactory.createCardLayout(contentWidget));
        addChild(contentWidget);
        contentWidget.setBorder(BorderFactory.createRoundedBorder(6, 6, 6, 6, null, BORDER_COLOR));
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
        final ButtonWidget tab = new ButtonWidget(getScene(), tabIcon, tabTitle);
        tab.setBorder(new TabBorder());
        tab.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                if(LayoutFactory.getActiveCard(contentWidget) != tabComponent) {
                    if(selectedTab!=null) {
                        TabBorder oldBorder = (TabBorder)selectedTab.getBorder();
                        oldBorder.setSelected(false);
                    }
                    TabBorder border = (TabBorder)tab.getBorder();
                    border.setSelected(true);
                    selectedTab = tab;
                    LayoutFactory.setActiveCard(contentWidget, tabComponent);
                    getScene().validate();
                }
            }
        });
        tabs.addChild(tab);
        if(selectedTab==null) {
            LayoutFactory.setActiveCard(contentWidget, tabComponent);
            selectedTab = tab;
            TabBorder border = (TabBorder)tab.getBorder();
            border.setSelected(true);
        }
    }
    
    private static class TabBorder implements Border {
        private boolean selected = false;
        private Insets insets = new Insets(2, 8, 2, 8);
        private static final int radius = 6;
        
        public TabBorder() {
        }
        
        public Insets getInsets() {
            return insets;
        }
        
        public void paint(Graphics2D g2, Rectangle rect) {
            Paint oldPaint = g2.getPaint();
            
            Arc2D arc = new Arc2D.Double(rect.x+0.5f, rect.y,
                    radius, radius, 180, -90, Arc2D.OPEN);
            GeneralPath gp = new GeneralPath(arc);
            arc = new Arc2D.Double(rect.x + rect.width - radius, rect.y,
                    radius, radius, 90, -90, Arc2D.OPEN);
            gp.append(arc,true);
            gp.lineTo(rect.x + rect.width, rect.y + rect.height);
            gp.lineTo(rect.x+0.5f, rect.y + rect.height);
            gp.closePath();
            if (selected) {
                g2.setPaint(new GradientPaint(
                        0, rect.y, SELECTED_TAB_COLOR,
                        0, rect.y + rect.height * 0.5f,
                        SELECTED_TAB_COLOR.brighter(), true));
            } else {
                g2.setPaint(new GradientPaint(
                        0, rect.y, TAB_COLOR,
                        0, rect.y + rect.height * 0.5f,
                        TAB_COLOR.brighter(), true));
            }
            g2.fill(gp);
            
            g2.setPaint(oldPaint);
        }
        
        public boolean isOpaque() {
            return true;
        }
        
        private void setSelected(boolean selected) {
            if(this.selected != selected) {
                this.selected = selected;
            }
        }
    }
}
