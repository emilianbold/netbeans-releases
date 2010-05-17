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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 *
 * @author anjeleevich
 */
public class MessagesUtils {
    

    public static NamedComponentReference<GlobalType> getDefaultTypeReference(
            WSDLModel model) 
    {
        GlobalSimpleType newType = null;
        
        Schema schema = SchemaModelFactory.getDefault()
                .getPrimitiveTypesModel().getSchema();
        
        for (GlobalSimpleType type : schema.getSimpleTypes()) {
            if ("string".equals(type.getName())) { // NOI18N
                newType = type;
                break;
            }
        }
        
        if (newType == null) return null;
        
        return model.getDefinitions().createSchemaReference(newType, 
                GlobalType.class);      
    }
    
    
    public static Collection<Message> getMessages(WSDLModel model) {
        Collection<Message> messages = model.getDefinitions().getMessages();
        return (messages != null) ? messages : new LinkedList<Message>();
    }
    
    
    public static Collection<Part> getParts(Message message) {
        Collection<Part> parts = message.getParts();
        return (parts != null) ? parts : new LinkedList<Part>();
    }

    
    public static String getPartTypeOrElementString(Part part) {
        String s = getPartTypeString(part);
        
        if (s == null) {
            s = getPartElementString(part);
        }
        
        if (s == null) {
            s = NbBundle.getMessage(MessagesUtils.class, "LBL_Undefined"); // NOI18N
        }
        
        return s;
    }
    
    
    public static String getPartTypeString(Part part) {
        if (part.getType() == null) return null;
        return convertQNameToString(part.getType().getQName());
    }
    
    
    public static String getPartElementString(Part part) {
        if (part.getElement() == null) return null;
        return convertQNameToString(part.getElement().getQName());
    }
    
    
    private static String convertQNameToString(QName qname) {
        if (qname == null) return null;
        return qname.getPrefix() + ":" + qname.getLocalPart(); // NOI18N
    }
    
    
    
    public static SchemaComponent extractSchemaComponent(Node node) {
        AXIComponent axiComponent = node.getLookup()
                .lookup(AXIComponent.class);

        SchemaComponent schemaComponent = null;

        if (axiComponent != null) {
            schemaComponent = axiComponent.getPeer();
        } else {
            SchemaComponentReference reference = 
                    node.getLookup().lookup(SchemaComponentReference.class);

            if (reference != null) {
                schemaComponent = reference.get();
            }

            if (schemaComponent == null) {
                schemaComponent = node.getLookup()
                        .lookup(SchemaComponent.class);
            }
        }

        if (schemaComponent != null 
                && (schemaComponent instanceof GlobalType 
                || schemaComponent instanceof GlobalElement)) 
        {
            return schemaComponent;
        }

        return null;
    }    
}
