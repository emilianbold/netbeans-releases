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
 * CompositorPropertiesPanel.java
 *
 * Created on June 29, 2006, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import org.netbeans.modules.xml.axi.Compositor;
import org.openide.util.NbBundle;

/**
 *
 * @author girix
 */
public class CompositorPropertiesPanel extends ExtraPropertiesPanel{
    private static final long serialVersionUID = 7526472295622776147L;
    private Compositor compositor;
    
    //private InstanceUIContext context;
    
    /** Creates a new instance of CompositorPropertiesPanel */
    public CompositorPropertiesPanel(Compositor compositor, InstanceUIContext context) {
        super(true, context);
        this.compositor = compositor;
        //this.context = context;
        refreshItems();
        
        compositor.addPropertyChangeListener(new ModelEventMediator(this, compositor){
            public void _propertyChange(PropertyChangeEvent evt) {
                //String prop = evt.getPropertyName();
                //if(prop.equals(Compositor.PROP_MAXOCCURS) || prop.equals(Compositor.PROP_MINOCCURS))
                    refreshItems();
            }
        });
        
        if(compositor.getContentModel() != null){
            compositor.getContentModel().addPropertyChangeListener(new ModelEventMediator(this,  compositor.getContentModel()) {
                public void _propertyChange(PropertyChangeEvent evt) {
                    refreshItems();
                }
            });
        }
    }
    
    private void refreshItems() {
        cleanupAll();
        //add cardinality label
        if(compositor.supportsCardinality()){
            String str = UIUtilities.getConstraintsString(compositor.getMinOccurs(), compositor.getMaxOccurs());
            if(str != null){
                JLabel constraints = new JLabel(str);//new InplaceEditableLabel(str);
                Font font = constraints.getFont();
                font = new Font(font.getFontName(), Font.PLAIN, 
                        InstanceDesignConstants.PROPS_FONT_SIZE);
                constraints.setFont(font);
                constraints.setForeground(new Color(139, 139, 139));
                constraints.setToolTipText(NbBundle.getMessage(ElementPropertiesPanel.class,
                        "TTP_COMPOSITOR_CARDINALITY"));
                append(constraints, true);
            }
        }
        //add content model string
        if(compositor.getContentModel() != null){
            //add the content model label
            JLabel contentModelInfoLabel = UIUtilities.getContentModelInfoLabel(compositor, false, true, context);
            if(contentModelInfoLabel != null)
                append(contentModelInfoLabel, false);
        }
        revalidate();
        repaint();
        
    }
    
    
    
}
