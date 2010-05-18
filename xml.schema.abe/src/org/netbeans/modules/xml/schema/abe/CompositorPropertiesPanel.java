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
