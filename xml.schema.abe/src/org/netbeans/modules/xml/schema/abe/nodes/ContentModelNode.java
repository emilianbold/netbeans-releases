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
package org.netbeans.modules.xml.schema.abe.nodes;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.schema.abe.InstanceUIContext;
import org.netbeans.modules.xml.schema.abe.action.AttributeOnElementNewType;
import org.netbeans.modules.xml.schema.abe.action.CompositorOnElementNewType;
import org.netbeans.modules.xml.schema.abe.action.ElementOnElementNewType;
import org.netbeans.modules.xml.schema.abe.nodes.properties.BaseABENodeProperty;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ContentModelNode extends ABEAbstractNode {
    
    
    /**
     * Creates a new instance of ContentModelNode
     */
    public ContentModelNode(ContentModel contentModel, InstanceUIContext context) {
        super(contentModel, context);
    }
    
    public ContentModelNode(ContentModel contentModel) {
        super(contentModel,new ABENodeChildren(contentModel));
        setIconBaseWithExtension(
                "org/netbeans/modules/xml/schema/abe/resources/complextype.png");
        
    }
    
    protected void populateProperties(Sheet sheet) {
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        if(set == null) {
            set = sheet.createPropertiesSet();
        }
        
        try {
            BaseABENodeProperty property = new BaseABENodeProperty(
                    this,
                    String.class,
                    ContentModel.PROP_NAME,
                    NbBundle.getMessage(ContentModelNode.class, "PROP_ContentModelNode_Name"),
                    NbBundle.getMessage(ContentModelNode.class, "PROP_ContentModelNode_NameDesc"));
            set.put(property);
        } catch (Exception ex) {
        }
        
        sheet.put(set);
    }
    
    public String getName(){
        if((ContentModel) super.getAXIComponent() != null)
            return ((ContentModel) super.getAXIComponent()).getName();
        else
            return "";
    }
    
    public NewType[] getNewTypes() {
        if(getAXIComponent().isReadOnly())
            return new NewType[0];
        List<NewType> ntl = new ArrayList<NewType>();
        NewType nt = new ElementOnElementNewType(getContext());
        ntl.add(nt);
        nt = new AttributeOnElementNewType(getContext());
        ntl.add(nt);
        if( ((AXIContainer)getAXIComponent()).getCompositor() == null ){
            nt = new CompositorOnElementNewType(getContext(), Compositor.
                    CompositorType.SEQUENCE);
            ntl.add(nt);
            nt = new CompositorOnElementNewType(getContext(), Compositor.
                    CompositorType.CHOICE);
            ntl.add(nt);
            nt = new CompositorOnElementNewType(getContext(), Compositor.
                    CompositorType.ALL);
            ntl.add(nt);
        }
        return  ntl.toArray(new NewType[ntl.size()]);
    }
    
    protected String getTypeDisplayName() {
	return NbBundle.getMessage(AttributeNode.class,"LBL_ComplexType");
    }
}
