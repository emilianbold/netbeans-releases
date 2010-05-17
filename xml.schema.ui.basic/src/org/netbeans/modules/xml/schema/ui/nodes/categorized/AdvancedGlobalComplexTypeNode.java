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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.schema.model.SimpleContentDefinition;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentDefinition;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.ui.nodes.schema.GlobalComplexTypeNode;
import org.netbeans.modules.xml.schema.ui.nodes.NewTypesFactory;
import org.netbeans.modules.xml.schema.ui.nodes.RefreshableChildren;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.ComplexTypeCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AdvancedGlobalComplexTypeNode extends GlobalComplexTypeNode {
    /**
     *
     *
     */
    public AdvancedGlobalComplexTypeNode(SchemaUIContext context,
            SchemaComponentReference<GlobalComplexType> reference,
            Children children) {
        super(context,reference,children);
    }
    
    
    /**
     *
     *
     */
    protected NewTypesFactory getNewTypesFactory() {
        return new AdvancedNewTypesFactory();
    }
    
    
    //  Override SCN.childrenDeleted() to handle grandchildren without nodes
    //  super childrenDeleted() can handle the following
    //   CT with sequence
//            <xsd:complexType name="ct1">
//                <xsd:sequence>
//                </xsd:sequence>
//            </xsd:complexType>
//
    // But, cannot handle this: CT with sequence within CC and CCDef
//            <xsd:complexType name="ct2"
//                ><xsd:complexContent>
//                        <xsd:extension base="tns:ct1">
//                            <xsd:sequence>
//                            </xsd:sequence>
//                        </xsd:extension>
//                </xsd:complexContent>
//            </xsd:complexType>
//            </xsd:schema>
    
    
    public void childrenDeleted(ComponentEvent evt) {
        super.childrenDeleted(evt);
        if (isValid()) {
            Object source = evt.getSource();
            GlobalComplexType type = getReference().get();
            ComplexTypeDefinition definition = type.getDefinition();
            if(source == type || source == definition) {
                fireDisplayNameChange(null,getDisplayName());
            }
            if(definition instanceof ComplexContent) {
                if(source == ((ComplexContent)definition).getLocalDefinition()) {
                    ((RefreshableChildren) getChildren()).refreshChildren();
                }
            } else if(definition instanceof SimpleContent) {
                if(source == ((SimpleContent)definition).getLocalDefinition()) {
                    ((RefreshableChildren) getChildren()).refreshChildren();
                }
            }
        }
    }
    
    public void childrenAdded(ComponentEvent evt) {
        if (! isValid()) return;
        super.childrenAdded(evt);
        Object source = evt.getSource();
        GlobalComplexType type = getReference().get();
        ComplexTypeDefinition definition = type.getDefinition();
        if(source == type || source == definition) {
            fireDisplayNameChange(null,getDisplayName());
        }
        if(definition instanceof ComplexContent) {
            if(source == definition ||
                    source == ((ComplexContent)definition).getLocalDefinition()) {
                ((RefreshableChildren) getChildren()).refreshChildren();
            }
        } else if(definition instanceof SimpleContent) {
            if(source == definition ||
                    source == ((SimpleContent)definition).getLocalDefinition()) {
                ((RefreshableChildren) getChildren()).refreshChildren();
            }
        }
    }
    
    public void valueChanged(ComponentEvent evt) {
        if (! isValid()) return;
        super.valueChanged(evt);
        Object source = evt.getSource();
        GlobalComplexType type = getReference().get();
        ComplexTypeDefinition definition = type.getDefinition();
        if(definition instanceof ComplexContent) {
            ComplexContentDefinition contentDef =
                    ((ComplexContent)definition).getLocalDefinition();
            if(source == contentDef) {
                fireDisplayNameChange(null,getDisplayName());
                if(contentDef instanceof ComplexExtension) {
                    ((RefreshableChildren) getChildren()).refreshChildren();
                }
            }
        } else if(definition instanceof SimpleContent) {
            SimpleContentDefinition contentDef =
                    ((SimpleContent)definition).getLocalDefinition();
            if(source == contentDef) {
                fireDisplayNameChange(null,getDisplayName());
                if(contentDef instanceof SimpleExtension) {
                    ((RefreshableChildren) getChildren()).refreshChildren();
                }
            }
        }
    }
    
    @Override
    public boolean hasCustomizer() {
        return isEditable();
    }
    
    public CustomizerProvider getCustomizerProvider() {
        return new CustomizerProvider() {
            
            public Customizer getCustomizer() {
                return new ComplexTypeCustomizer<GlobalComplexType>(getReference());
            }
        };
    }
    
    public String getHtmlDisplayName() {
        String gtName = getSuperDefinitionName();
        if(gtName == null) return super.getHtmlDisplayName();
        String retValue = getDefaultDisplayName();
        ComplexTypeDefinition definition = getReference().get().getDefinition();
        String supertypeLabel = null;
        if(definition instanceof ComplexContent &&
                ((ComplexContent)definition).getLocalDefinition()
                instanceof ComplexContentRestriction ||
                definition instanceof SimpleContent &&
                ((SimpleContent)definition).getLocalDefinition()
                instanceof SimpleContentRestriction) {
            supertypeLabel = NbBundle.getMessage(AdvancedGlobalComplexTypeNode.class,
                    "LBL_ComplexType_Restriction",gtName);
        } else if(definition instanceof ComplexContent &&
                ((ComplexContent)definition).getLocalDefinition()
                instanceof ComplexExtension ||
                definition instanceof SimpleContent &&
                ((SimpleContent)definition).getLocalDefinition()
                instanceof SimpleExtension) {
            supertypeLabel = NbBundle.getMessage(AdvancedGlobalComplexTypeNode.class,
                    "LBL_ComplexType_Extension",gtName);
        }
        if(supertypeLabel!=null) {
            retValue = retValue+"<font color='#999999'> ("+supertypeLabel+")</font>";
        }
        return applyHighlights(retValue);
    }
}
