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
package org.netbeans.modules.xml.schema.abe;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import javax.swing.JPanel;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;
import org.netbeans.modules.xml.schema.abe.nodes.ContentModelNode;
import org.openide.nodes.Node;

/**
 *
 * @author girix
 */
public class ContentModelPanel extends ElementPanel{
    private static final long serialVersionUID = 7526472295622776147L;
    /**
	 * Creates a new instance of ContentModelPanel
	 */
    public ContentModelPanel(InstanceUIContext context,
            ContentModel contentModel, ContainerPanel parentCompositorPanel) {
        super(context, contentModel, parentCompositorPanel);
    }
    
    protected StartTagPanel getNewStartTagPanel(ElementPanel elementPanel, InstanceUIContext context) {
        return new StartTagPanel(elementPanel, context){
            private static final long serialVersionUID = 7526472295622776147L;
            public void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                Stroke oldStroke = g2d.getStroke();
                //Color oldColor = g2d.getColor();
                Stroke drawingStroke2 =
                        new BasicStroke(
                        2,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10,
                        new float[] {2},
                        0
                        );
                
                g2d.setStroke((drawingStroke2));
                super.paintComponent(g);
                g2d.setStroke(oldStroke);
            }
            
            public JPanel getNewAXIContainerPropertiesPanel(){
                JPanel jp = new JPanel();
                jp.setOpaque(false);
                return jp;
            }
        };
    }
    
    protected void makeNBNode() {
        contentModelNode = new ContentModelNode((ContentModel) getAXIContainer(), context);
        if(getAXIContainer().isReadOnly())
            ((ABEAbstractNode)contentModelNode).setReadOnly(true);
    }
    
    public ABEAbstractNode getNBNode() {
        return contentModelNode;
    }
    
////////////////////////////////////////////////////////////////////////////
// Instance members
////////////////////////////////////////////////////////////////////////////
    
    private ABEAbstractNode contentModelNode;	
}
