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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.xml.schema.refactoring.ui.tree;

import java.beans.BeanInfo;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class XMLSchemaTreeElement implements TreeElement { 
    
    RefactoringElement element;
    Node node;
    SchemaComponent component;
       
    XMLSchemaTreeElement(RefactoringElement element) {
        this.element = element;
        this.component = element.getLookup().lookup(SchemaComponent.class);
        assert component instanceof SchemaComponent:"This TreeElement handles SchemaComponents only";
        SchemaComponent sc = SchemaComponent.class.cast(component);
        CategorizedSchemaNodeFactory nodeFactory = new CategorizedSchemaNodeFactory(sc.getModel(), Lookups.singleton(sc));
        this.node = nodeFactory.createNode(sc);
    }
    
    XMLSchemaTreeElement(Object element) {
        this.component = (SchemaComponent)element;
        CategorizedSchemaNodeFactory nodeFactory = new CategorizedSchemaNodeFactory((SchemaModel) component.getModel(), Lookups.singleton(component));
        this.node = nodeFactory.createNode(component);
    }
    
    public TreeElement getParent(boolean isLogical) {
         TreeElement result = null;
         if(component.getParent() != null)
             return TreeElementFactory.getTreeElement(component.getParent());
         else {
            FileObject fo = (FileObject) component.getModel().getModelSource().getLookup().lookup(FileObject.class);
            return TreeElementFactory.getTreeElement(fo);
        }
    }
                
         
 
    public Icon getIcon() {
       return new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
        
    }

    public String getText(boolean isLogical) {
        if(element != null ){
            String htmlDisplayName = node.getHtmlDisplayName();
            String usageTreeNodeLabel =
                            MessageFormat.format(
                            NbBundle.getMessage(
                            XMLSchemaTreeElement.class,
                            "LBL_Usage_Node"),
                            new Object[] {
                        node.getName(),
                        node.getShortDescription(),  // comp type
                        htmlDisplayName==null?"":htmlDisplayName
                    });
            return usageTreeNodeLabel;
        } else {
           // System.out.println("RefactoringElment is NULL!!!");
            return node.getName();
        }
       
       
    }

    public Object getUserObject() {
        if(element != null)
            return element;
        else
            return component;
    }
}
