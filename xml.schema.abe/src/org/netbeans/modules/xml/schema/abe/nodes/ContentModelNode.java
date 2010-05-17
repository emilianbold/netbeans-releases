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
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
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
            Node.Property name = new PropertySupport.Name(
                    this,
                    NbBundle.getMessage(ContentModelNode.class, "PROP_ContentModelNode_Name"),
                    NbBundle.getMessage(ContentModelNode.class, "PROP_ContentModelNode_NameDesc"));
            set.put(name);
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
    
    public boolean canRename() {
        if(canWrite())
            return true;
        return false;
    }
    
    protected String getTypeDisplayName() {
	return NbBundle.getMessage(AttributeNode.class,"LBL_ComplexType");
    }
}
