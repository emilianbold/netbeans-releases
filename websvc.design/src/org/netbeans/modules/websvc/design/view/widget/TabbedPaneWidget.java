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
        final ButtonWidget tab = new ButtonWidget(getScene(), tabIcon, tabTitle);
        tab.setBorder(new TabBorder(this,tab));
        tab.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                if(LayoutFactory.getActiveCard(contentWidget) != tabComponent) {
                    if(selectedTab!=null)
                        selectedTab.getLabelWidget().setFont(getScene().getFont());
                    selectedTab = tab;
                    selectedTab.getLabelWidget().setFont(getScene().getFont().deriveFont(Font.BOLD));
                    LayoutFactory.setActiveCard(contentWidget, tabComponent);
                    getScene().validate();
                }
            }
        });
        tabs.addChild(tab);
        tabs.setLayout(new TableLayout(tabs.getChildren().size(), 0, 0, 20));
        if(selectedTab==null) {
            selectedTab = tab;
            selectedTab.getLabelWidget().setFont(getScene().getFont().deriveFont(Font.BOLD));
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
            g2.draw(gp);
            
            g2.setPaint(oldPaint);
        }
        
        public boolean isOpaque() {
            return true;
        }
        
    }
}
