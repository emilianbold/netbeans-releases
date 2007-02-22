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
 * ElementPropertiesPanel.java
 *
 * Created on June 12, 2006, 5:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.axi.ContentModel;

/**
 *
 * @author girix
 */
public class ElementPropertiesPanel extends ExtraPropertiesPanel{
    private static final long serialVersionUID = 7526472295622776147L;
    private AbstractElement element;
    private InstanceUIContext context;
    private JLabel contentType;
    JLabel contentModelInfoLabel;
    int interComponentSpacingOrig = 0;
    /** Creates a new instance of ElementPropertiesPanel */
    public ElementPropertiesPanel(AbstractElement element, InstanceUIContext context) {
        super(true, context);
        this.element = element;
        this.context = context;
        interComponentSpacingOrig = getInterComponentSpacing();
        initialize();
        this.element.addPropertyChangeListener(new ModelEventMediator(this, this.element){
            public void _propertyChange(PropertyChangeEvent evt) {
                /*if( (evt.getPropertyName() == Element.PROP_MAXOCCURS)
                ||  (evt.getPropertyName() == Element.PROP_MINOCCURS) ){*/
                refreshItems();
                //}
            }
        });
        
        if(element instanceof Element){
            Element elm = ((Element)this.element);
            AXIType at = elm.getType();
            if((at instanceof ContentModel)){
                ((ContentModel)at).addPropertyChangeListener(new ModelEventMediator(this, ((ContentModel)at)){
                    public void _propertyChange(PropertyChangeEvent evt) {
                        refreshItems();
                    }
                });
            }
        }
    }
    
    private void initialize() {
        refreshItems();
    }
    
    private void refreshItems(){
        cleanupAll();
        setInterComponentSpacing(interComponentSpacingOrig);
        //add the constraints str
        JLabel constraints = null;
        if(element.supportsCardinality()){
            String str = UIUtilities.getConstraintsString(element.getMinOccurs(),
                    element.getMaxOccurs());
            if(str != null){
                constraints = new JLabel(str);//new InplaceEditableLabel(str);
                Font font = constraints.getFont();
                font = new Font(font.getFontName(), Font.PLAIN,
                        InstanceDesignConstants.PROPS_FONT_SIZE);
                constraints.setFont(font);
                constraints.setForeground(new Color(139, 139, 139));
                constraints.setToolTipText(NbBundle.getMessage(ElementPropertiesPanel.class,
                        "TTP_ELEMENT_CARDINALITY"));
            }
        }
        //add the content model info.
        if(!(element instanceof AnyElement)){
            contentModelInfoLabel = UIUtilities.getContentModelInfoLabel(this.element, false, true, context);
        }
        
        if( (constraints != null) && (contentModelInfoLabel != null) ){
            append(constraints, true);
            append(contentModelInfoLabel, false);
        }else if(( (constraints == null) && (contentModelInfoLabel == null) )){
            return;
        }else{
            Component comp = contentModelInfoLabel == null ? constraints :
                contentModelInfoLabel;
            boolean transmit = (contentModelInfoLabel == null);
            append(comp, transmit);
        }
        revalidate();
        repaint();
    }
}
