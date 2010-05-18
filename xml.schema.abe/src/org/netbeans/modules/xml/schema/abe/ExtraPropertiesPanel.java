/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
