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

/*
 * TitleWrapperPanel.java
 *
 * Created on June 20, 2006, 3:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author girix
 */
public abstract class TitleWrapperPanel extends ABEBaseDropPanel{
    private static final long serialVersionUID = 7526472295622776147L;
    ContainerPanel child;
    String titleString;
    AXIComponent countComponent;
    ExpandCollapseButton expandCollapseButton;
    JLabel countLabel;
    private boolean openByDefault;
    
    
    /** Creates a new instance of TitleWrapperPanel */
    public TitleWrapperPanel(ContainerPanel child, String titleString, AXIComponent
            countComponent, boolean openByDefault, InstanceUIContext context) {
        super(context);
        this.child = child;
        this.titleString = titleString;
        this.countComponent = countComponent;
        this.openByDefault = openByDefault;
        initialize();
        
        this.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                TitleWrapperPanel.this.child.dispatchEvent(e);
            }
            
            public void mousePressed(MouseEvent e) {
                TitleWrapperPanel.this.child.dispatchEvent(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                TitleWrapperPanel.this.child.dispatchEvent(e);
            }
        });
    }
    
    private void initialize(){
        setLayout(new BorderLayout());
        setOpaque(false);
        SpringLayout titlePanelLayout = new SpringLayout();
        AutoSizingPanel titlePanel = new AutoSizingPanel(this.context);
        titlePanel.setLayout(titlePanelLayout);
        titlePanel.setHorizontalScaling(true);
        titlePanel.setOpaque(false);
        
        expandCollapseButton = new ExpandCollapseButton("-");
        if(openByDefault)
            expandCollapseButton.setText("-");
        else
            expandCollapseButton.setText("+");
        
        expandCollapseButton.setWatchForComponent(child);
        
        titlePanel.add(expandCollapseButton);
        titlePanelLayout.putConstraint(SpringLayout.WEST, expandCollapseButton,
                0, SpringLayout.WEST, titlePanel);
        titlePanelLayout.putConstraint(SpringLayout.NORTH, expandCollapseButton,
                3, SpringLayout.NORTH, titlePanel);
        
        
        expandCollapseButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                String state = expandCollapseButton.getText();
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                try{
                    if(state.equals("-")){
                        //means the panle has to be collapsed
                        child.setVisible(false);
                    } else {
                        child.addAllChildren();
                        child.setVisible(true);
                    }
                    setCountString();
                }finally{
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        
        JLabel titleLabel = new JLabel(titleString);
        titleLabel.setDropTarget(new DropTarget(titleLabel, new DropTargetListener() {
            public void dragEnter(DropTargetDragEvent dtde) {
                TitleWrapperPanel.this.dragEnter(dtde);
            }
            public void dragExit(DropTargetEvent dte) {
                TitleWrapperPanel.this.dragExit(dte);
            }
            public void dragOver(DropTargetDragEvent dtde) {
                TitleWrapperPanel.this.dragOver(dtde);
            }
            public void drop(DropTargetDropEvent dtde) {
                TitleWrapperPanel.this.drop(dtde);
            }
            public void dropActionChanged(DropTargetDragEvent dtde) {
                TitleWrapperPanel.this.dropActionChanged(dtde);
            }
        }));
        Font font = titleLabel.getFont();
        //font = new Font(font.getFontName(), Font.BOLD, getFont().getSize());
        font = font.deriveFont((font.getStyle() | java.awt.Font.BOLD), font.getSize());
        titleLabel.setFont(font);
        titleLabel.setForeground(InstanceDesignConstants.TAG_NAME_COLOR);
        
        titlePanel.add(titleLabel);
        titlePanelLayout.putConstraint(SpringLayout.WEST, titleLabel,
                (TITLE_BEGIN_FUDGE - ExpandCollapseButton.WIDTH), SpringLayout.EAST, expandCollapseButton);
        titlePanelLayout.putConstraint(SpringLayout.NORTH, titleLabel,
                0, SpringLayout.NORTH, titlePanel);
        
        countLabel = getCountLabel();
        
        titlePanel.add(countLabel);
        titlePanelLayout.putConstraint(SpringLayout.WEST, countLabel,
                5, SpringLayout.EAST, titleLabel);
        titlePanelLayout.putConstraint(SpringLayout.NORTH, titleLabel,
                0, SpringLayout.NORTH, titlePanel);
        
        JSeparator separator = new JSeparator();
        
        //I add a new redundant panel explicitly for the separator to scale till the
        //right view end
        JPanel tempPanel = new JPanel(new BorderLayout());
        tempPanel.setOpaque(false);
        tempPanel.add(titlePanel, BorderLayout.CENTER);
        tempPanel.add(separator, BorderLayout.SOUTH);
        
        
        
        add(tempPanel, BorderLayout.NORTH);
        
        //add child now itself
        add(child, BorderLayout.CENTER);
        if(openByDefault)
            child.setVisible(true);
        else
            child.setVisible(false);
        setCountString();
        
        child.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                componentShown(e);
            }
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        setCountString();
                    }
                });
            }
        });
    }
    
    public JLabel getCountLabel(){
        int size = getChildrenItemsCount();
        countLabel = new JLabel(getCountString(size));
        countComponent.addPropertyChangeListener( new ModelEventMediator(this, countComponent) {
            public void _propertyChange(PropertyChangeEvent evt) {
                //for any event just refresh the label
                setCountString();
            }
        });
        countLabel.setForeground(Color.GRAY);
        return countLabel;
    }
    
    
    public void setCountString(){
        countLabel.setText(getCountString(getChildrenItemsCount()));
    }
    
    public abstract int getChildrenItemsCount();
    
    public String getCountString(int i){
        String hiddenStr = (child.isVisible() || i == 0) ? "" : " "+locHidden;
        String str = (i == 1) ? "["+i+" "+locItem+hiddenStr+"]" : "["+i+" "+locItems+hiddenStr+"]";
        return str;
    }
    
    
    
    private static final String locItem = NbBundle.getMessage(TitleWrapperPanel.class, "LBL_ITEM_STRING");
    private static final String locItems = NbBundle.getMessage(TitleWrapperPanel.class, "LBL_ITEMS_STRING");
    private static final String locHidden = NbBundle.getMessage(TitleWrapperPanel.class, "LBL_HIDDEN");
    public static final int TITLE_BEGIN_FUDGE = ExpandCollapseButton.WIDTH + 2;
    
    
    public void drop(DropTargetDropEvent event) {
        context.getNamespacePanel().drop(event);
    }
    
    public void dragExit(DropTargetEvent event) {
        context.getNamespacePanel().dragExit(event);
    }
    
    public void dragOver(DropTargetDragEvent event) {
        context.getNamespacePanel().dragOver(event);
    }
    
    public void dragEnter(DropTargetDragEvent event) {
        context.getNamespacePanel().dragEnter(event);
    }
    
    
    public void accept(UIVisitor visitor) {
    }
}
