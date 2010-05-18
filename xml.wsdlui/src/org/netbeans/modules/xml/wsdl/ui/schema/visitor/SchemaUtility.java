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
 * SchemaUtility.java
 *
 * Created on April 17, 2006, 8:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.schema.visitor;

import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author radval
 */
public class SchemaUtility {
    
    /** Creates a new instance of SchemaUtility */
    public SchemaUtility() {
    }
    
    /**
     * Find Attribute given attribute QName
     * attribute QName should have namespace and local name
     * prefix of attribute QName is ignored
     **/
    public static Attribute findAttribute(QName attrQName, Element element) {
        Attribute attribute = null;
        SchemaElementAttributeFinderVisitor seaFinder = new SchemaElementAttributeFinderVisitor(element);
        element.accept(seaFinder);
        
        List<Attribute> attributes = seaFinder.getAttributes();
        Iterator<Attribute> it = attributes.iterator();
        
        while(it.hasNext()) {
            Attribute attr = it.next();
            if(attr instanceof Nameable) {
                Nameable namedAttr = (Nameable) attr;
                String attrName = namedAttr.getName();
                if (attrName.equals(attrQName.getLocalPart())) return attr;
                
                QName aq = QName.valueOf(attrName);
                String ns = aq.getNamespaceURI();
                String prefix = aq.getPrefix();
                if(ns == null || ns.trim().equals("") && prefix != null) {
                    ns = ((AbstractDocumentComponent) element).lookupNamespaceURI(prefix);
                }
                
                QName normalizedQName = new QName(ns, aq.getLocalPart());
                        
                if(attrQName.equals(normalizedQName))  {
                    attribute = attr;
                    break;
                }
            }
        }
        
        return attribute;
    }
}
