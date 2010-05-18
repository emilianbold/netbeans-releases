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
 * MemberTypesEditor.java
 *
 * Created on December 22, 2005, 12:58 PM
 */

package org.netbeans.modules.xml.schema.ui.basic.editors;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.ui.nodes.schema.UnionNode;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.Named;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class MemberTypesEditor  extends PropertyEditorSupport
        implements ExPropertyEditor {
    
    private MemberTypesPanel panel;
    private DialogDescriptor descriptor;
    private SchemaComponent component;
    
    /**
     * Creates a new instance of MemberTypesEditor
     */
    public MemberTypesEditor(SchemaComponent component) {
        this.component = component;
    }
    
    public String getAsText() {
        Object val = super.getValue();
        if (val == null){
            return null;
        }
        if(val instanceof Collection) {
            boolean first = true;
            String str = "";
            for(Object elem: (Collection)val) { 
                if (elem instanceof NamedComponentReference){
                    Object obj =  ((NamedComponentReference)elem).get();
                    if (obj instanceof Named){
                        if(first) {
                            str = str.concat(((Named)obj).getName());
                            first = false;
                        } else {
                            str = str.concat(" ").concat(((Named)obj).getName());
                        }
                    }
                }
            }
            return str;
        }
        // TODO how to display invalid values?
        return val.toString();
    }
    
    @SuppressWarnings("unchecked")
    public Collection<NamedComponentReference<GlobalSimpleType>> getValue() {
        return (Collection<NamedComponentReference<GlobalSimpleType>>) super.getValue();
    }

    private Collection<NamedComponentReference<GlobalSimpleType>> getCurrentSelection(
            List<GlobalSimpleType> newSelectionList) {
        if(newSelectionList != null && !newSelectionList.isEmpty()) {
                Collection<NamedComponentReference<GlobalSimpleType>> newSelectionRef = 
                        new ArrayList<NamedComponentReference<GlobalSimpleType>>();
                HashMap<GlobalSimpleType,NamedComponentReference<GlobalSimpleType>> gstToRefMap = 
                        new HashMap<GlobalSimpleType,NamedComponentReference<GlobalSimpleType>>();
                Collection<NamedComponentReference<GlobalSimpleType>> oldSelectionRef = getValue();
                if(oldSelectionRef!=null) {
                    for(NamedComponentReference<GlobalSimpleType> ref:oldSelectionRef) {
                        GlobalSimpleType gst = ref.get();
                        gstToRefMap.put(gst,ref);
                    }
                }
                for (Object obj:newSelectionList) {
                    if (gstToRefMap.containsKey(obj)) {
                        newSelectionRef.add(gstToRefMap.get(obj));
                    } else {
                        newSelectionRef.add(component.getModel().getFactory().
                                createGlobalReference((GlobalSimpleType)obj,
                                GlobalSimpleType.class, component));
                    }
                }
                return newSelectionRef;
        }
        return null;
    }
    
    public Component getCustomEditor() {
        Collection<NamedComponentReference<GlobalSimpleType>> currentSelectionRef = getValue();
        Collection<GlobalSimpleType> currentSelection = null;
        if(currentSelectionRef!=null) {
            currentSelection = new ArrayList<GlobalSimpleType>();
            for(NamedComponentReference ref :currentSelectionRef) {
                currentSelection.add(((GlobalSimpleType)ref.get()));
            }
        }
        panel =  new MemberTypesPanel(component.getModel(), currentSelection);
        descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(MemberTypesEditor.class,
                "LBL_Custom_Property_Editor_Title",
                new Object[] {NbBundle.getMessage(UnionNode.class, "LBL_UnionNode_TypeDisplayName")
                        , NbBundle.getMessage(UnionNode.class, "PROP_MemberTypes_DisplayName")}),
                true,
                new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    try {
                        setValue(getCurrentSelection(panel.getCurrentSelection()));
                    } catch (IllegalArgumentException iae) {
                        ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                                iae.getMessage(), iae.getLocalizedMessage(), 
                                null, new java.util.Date());
                        throw iae;
                    }
                }
            }
        }
        );
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setPreferredSize(new Dimension(400,300));
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
