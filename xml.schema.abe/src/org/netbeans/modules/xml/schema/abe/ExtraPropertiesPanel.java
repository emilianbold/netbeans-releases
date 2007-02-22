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
/*
 * ExtraPropertiesPanel.java
 *
 * Created on June 12, 2006, 3:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SpringLayout;

/**
 *
 * @author girix
 */
public class ExtraPropertiesPanel extends AutoSizingPanel{
    private static final long serialVersionUID = 7526472295622776147L;
    SpringLayout layout;
    boolean leftToRight = true;
    /** Creates a new instance of ExtraPropertiesPanel */
    public ExtraPropertiesPanel(boolean leftToRight, InstanceUIContext context) {
        super(context);
        if(leftToRight){
            setFixedHeight(true);
            setHorizontalScaling(true);
            setFixedPanelHeight(AttributePanel.getAttributePanelHeight());
            setInterComponentSpacing(5);
        }else{
            setVerticalScaling(true);
            setInterComponentSpacing(-2);
        }
        setOpaque(false);
        this.leftToRight = leftToRight;
    }
    
    public void append(Component component, boolean transmitEvent2parent){
        if(leftToRight)
            appendRight(component, transmitEvent2parent);
        else
            appendBelow(component);
        
    }
    
    Component lastComponent;
    private void appendRight(Component component, boolean transmitEvent2Parent){
        boolean alreadyAdded = false;
        if(layout == null){
            layout = new SpringLayout();
            setLayout(layout);
            alreadyAdded = true;
            add(component);
            layout.putConstraint(SpringLayout.WEST, component, getInterComponentSpacing(),
                    SpringLayout.WEST, this);
            lastComponent = component;
        }
        if(!alreadyAdded){
            add(component);
            layout.putConstraint(SpringLayout.WEST, component, getInterComponentSpacing(),
                    SpringLayout.EAST, lastComponent);
        }
        layout.putConstraint(SpringLayout.NORTH, component, 0,
                SpringLayout.NORTH, this);
        lastComponent = component;
        if(transmitEvent2Parent)
            component.addMouseListener(new MouseAdapter(){
                public void mouseReleased(MouseEvent e) {
                    ExtraPropertiesPanel.this.dispatchEvent(e);
                }
                
                public void mouseClicked(MouseEvent e) {
                    ExtraPropertiesPanel.this.dispatchEvent(e);
                }
                
                public void mousePressed(MouseEvent e) {
                    ExtraPropertiesPanel.this.dispatchEvent(e);
                }
                
            });
    }
    
    private void appendBelow(Component component){
        boolean alreadyAdded = false;
        if(layout == null){
            layout = new SpringLayout();
            setLayout(layout);
            alreadyAdded = true;
            add(component);
            layout.putConstraint(SpringLayout.NORTH, component, getInterComponentSpacing(),
                    SpringLayout.NORTH, this);
            lastComponent = component;
        }
        if(!alreadyAdded){
            add(component);
            layout.putConstraint(SpringLayout.NORTH, component, getInterComponentSpacing(),
                    SpringLayout.SOUTH, lastComponent);
        }
        layout.putConstraint(SpringLayout.WEST, component, 0,
                SpringLayout.WEST, this);
        lastComponent = component;
        
        component.addMouseListener(new MouseAdapter(){
            public void mouseReleased(MouseEvent e) {
                ExtraPropertiesPanel.this.dispatchEvent(e);
            }
            
            public void mouseClicked(MouseEvent e) {
                ExtraPropertiesPanel.this.dispatchEvent(e);
            }
            
            public void mousePressed(MouseEvent e) {
                ExtraPropertiesPanel.this.dispatchEvent(e);
            }
            
        });
    }
    
    public  void cleanupAll(){
        for(Component child: getComponents()){
            remove(child);
        }
        layout = null;
    }
    
    
    
}
