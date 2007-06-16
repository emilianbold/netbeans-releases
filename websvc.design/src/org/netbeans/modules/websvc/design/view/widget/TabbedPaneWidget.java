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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import javax.swing.AbstractAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.view.layout.TableLayout;

/**
 *
 * @author Ajit Bhate
 */
public class TabbedPaneWidget extends Widget {
    
    private static final Color TAB_BORDER_COLOR = Color.BLACK;
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
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
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
        final ButtonWidget tab = new ButtonWidget(getScene(), tabIcon, tabTitle);
        tab.setBorder(new TabBorder());
        tab.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                if(LayoutFactory.getActiveCard(contentWidget) != tabComponent) {
                    if(selectedTab!=null) {
                        selectedTab.getButton().getLabelWidget().setFont(getScene().getFont());
                        TabBorder oldBorder = (TabBorder)selectedTab.getBorder();
                        oldBorder.setSelected(false);
                    }
                    tab.getButton().getLabelWidget().setFont(getScene().getFont().deriveFont(Font.BOLD));
                    TabBorder border = (TabBorder)tab.getBorder();
                    border.setSelected(true);
                    selectedTab = tab;
                    LayoutFactory.setActiveCard(contentWidget, tabComponent);
                    getScene().validate();
                }
            }
        });
        tabs.addChild(tab);
        tabs.setLayout(new TableLayout(tabs.getChildren().size(), 1, 0, 20));
        if(selectedTab==null) {
            LayoutFactory.setActiveCard(contentWidget, tabComponent);
            selectedTab = tab;
            tab.getButton().getLabelWidget().setFont(getScene().getFont().deriveFont(Font.BOLD));
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
            Arc2D arc1 = new Arc2D.Double(rect.x + rect.width - radius-1, rect.y,
                    radius, radius, 90, -90, Arc2D.OPEN);
            gp.append(arc1,true);
            gp.lineTo(rect.x + rect.width-1, rect.y + rect.height);
            gp.lineTo(rect.x+0.5f, rect.y + rect.height);
            gp.closePath();
            if (selected) {
                g2.setPaint(SELECTED_TAB_COLOR);
                g2.fill(gp);
                GeneralPath gp1 = new GeneralPath();
                gp1.moveTo(rect.x+0.5f, rect.y + rect.height);
                gp1.append(arc, true);
                gp1.append(arc1, true);
                gp1.lineTo(rect.x + rect.width-1, rect.y + rect.height);
                g2.setPaint(TAB_BORDER_COLOR);
                g2.draw(gp1);
            } else {
                g2.setPaint(TAB_COLOR);
                g2.fill(gp);
                g2.setPaint(TAB_BORDER_COLOR);
                g2.draw(gp);
            }
            
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
