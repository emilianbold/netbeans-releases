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
package org.netbeans.modules.xslt.tmap.nodes.properties;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.soa.ui.form.Reusable;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.nodes.TMapComponentNode;
import org.netbeans.modules.xslt.tmap.nodes.TransformNode;
import org.netbeans.modules.xslt.tmap.ui.editors.OperationPropertyCustomizer;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author Vitaly Bychkov
 */
public class SourcePropEditor extends PropertyEditorSupport
        implements ExPropertyEditor, Reusable {

    private static OperationPropertyCustomizer customizer = null;
    protected PropertyEnv myPropertyEnv = null;
    private TMapComponentNode myParentNode;

    /**
     * Allows to use single instance of editor for differen properties
     */
    /** Creates a new instance of OperationPropEditor */
    public SourcePropEditor() {
    }

    @Override
    public String getAsText() {
        Object value = super.getValue();
        return value == null ? "" : String.valueOf(value);
    }

    @Override
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        setValue(text);
    }

    @Override
    public boolean supportsCustomEditor() {
        return false;
    }

    @Override
    public String[] getTags() {
        if (myPropertyEnv != null ) {
//            System.out.println("my prop env is not null: "+myPropertyEnv);
            TMapComponentNode parentNode = getParentNode();
            if (parentNode instanceof TransformNode) {
                Transform transform = ((TransformNode)parentNode).getReference().getReference();
                if (transform == null) {
                    return super.getTags();
                }
                
                TMapComponent parentComp = transform.getParent();
                if (!(parentComp instanceof Operation)) {
                    return super.getTags();
                }
                
                List<Variable> vars = ((Operation)parentComp).getVariables();
                if (vars == null) {
                    return super.getTags();
                }
                List<String> varParts = new ArrayList<String>();
                for (Variable var : vars) {
                    if (var == null) {
                        continue;
                    }
                    Reference<Message> messRef = var.getMessage();
                    Message mess = messRef == null ? null : messRef.get();
                    if (mess == null ) {
                        continue;
                    }
                    Collection<Part> parts = mess.getParts();
                    if (parts == null) {
                        continue;
                    }
                    for (Part part : parts) {
                        String partName = part == null ? null : part.getName();
                        if (partName == null) {
                            continue;
                        }
                        varParts.add("$"+var.getName()+"."+partName);
                    }
                }
                return varParts.toArray(new String[varParts.size()]);
            }
        }
//        else {
//            System.out.println("my prop env is null");
//        }
        return super.getTags();
    }
    
//    @Override
//    public Component getCustomEditor() {
////        customizer = PropertyUtils.propertyCustomizerPool.getObjectByClass(PortTypePropertyCustomizer.class);
//        customizer = new OperationPropertyCustomizer(myPropertyEnv);
////        customizer.init(myPropertyEnv);
//        return customizer;
//    }

    public TMapComponentNode getParentNode() {
        if (myParentNode == null) {
            Object[] beans = myPropertyEnv.getBeans();
            if (beans != null && beans.length != 0) {
                Object bean = beans[0];
                if (bean != null && bean instanceof TMapComponentNode) {
                    myParentNode = (TMapComponentNode)bean;
                }
            }
        }
        return myParentNode;
    }

    public void attachEnv(PropertyEnv newPropertyEnv) {
        myPropertyEnv = newPropertyEnv;
    }
}
