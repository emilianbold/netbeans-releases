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
