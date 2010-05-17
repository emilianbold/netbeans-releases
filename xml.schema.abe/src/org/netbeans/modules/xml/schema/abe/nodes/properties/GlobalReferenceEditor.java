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

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.ContentModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 *
 * @author Ayub Khan
 */
public class GlobalReferenceEditor
		extends PropertyEditorSupport
        implements ExPropertyEditor {
    
    private DialogDescriptor descriptor;
    private AXIComponent component;
    private List<Class> filterTypes;
    private String referenceTypeDisplayName;
    private String typeDisplayName;
    private String propertyDisplayName;
	private AXIComponentSelectionPanel panel;
    
    
    /**
     * Creates a new instance of GlobalReferenceEditor
     */
    public GlobalReferenceEditor(AXIComponent component, 
            String typeDisplayName,
            String propertyDisplayName,
            String referenceTypeDisplayName,
            List<Class> filterTypes) {
        this.typeDisplayName = typeDisplayName;
        this.propertyDisplayName = propertyDisplayName;
        this.filterTypes = filterTypes;
        this.component = component;
        this.referenceTypeDisplayName = referenceTypeDisplayName;
    }
    
    public String getAsText() {
        Object val = getValue();
        if (val instanceof AXIType && component.getModel() != null)
			return ((AXIType)val).getName();
		else
			return null;
    }
    
    @SuppressWarnings("unchecked")
    public Component getCustomEditor() {
        Object currentGlobalReference = getValue();
        Collection<AXIComponent> exclude = null;
		for(Class filterType: filterTypes) {
			if(filterType.isInstance(component)) {
				exclude = new ArrayList<AXIComponent>();
				exclude.add(component);
			} else {
				AXIComponent parent = component.getParent();
				while (parent!=null) {
					if(filterType.isInstance(parent)) {
						exclude = new ArrayList<AXIComponent>();
						exclude.add(parent);
						break;
					}
					parent = parent.getParent();
				}
			}
		}
        panel = new AXIComponentSelectionPanel(component.getModel(),
                referenceTypeDisplayName, filterTypes, 
				currentGlobalReference, exclude);
        descriptor = new AXIComponentSelDialogDesc(panel,
                NbBundle.getMessage(GlobalReferenceEditor.class,
                "LBL_Custom_Property_Editor_Title",
                new Object[] {typeDisplayName, propertyDisplayName}),
                true,
                new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    try {
                        setValue(panel.getCurrentSelection());
                    } catch (IllegalArgumentException iae) {
                        ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                                iae.getMessage(), iae.getLocalizedMessage(), 
                                null, new java.util.Date());
                        throw iae;
                    }
                }
            }
        });
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        return dlg;
    }
    
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public void attachEnv(PropertyEnv env ) {
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this is not editable  
        desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
    }
}
