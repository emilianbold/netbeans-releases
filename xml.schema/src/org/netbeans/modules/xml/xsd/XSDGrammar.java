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

package org.netbeans.modules.xml.xsd;

import java.util.*;

import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;



/**
 * Rather simple query implemetation based on XSD grammar.
 * It is produced by {@link XSDParser}.
 * Hints given by this grammar do not guarantee that valid XML document is created.
 *
 * @author  Ales Novak
 */
class XSDGrammar  implements GrammarQuery {
    
    /** All elements */
    private Map elements;
    /** All types */
    private Map types;
    /** namespace */
    private Namespace namespace;
    
    private Namespace schemaNamespace;
    private Namespace targetNamespace;

    /** Creates new XSDGrammar */
    XSDGrammar(Map elements, Map types, Namespace targetNamespace, Namespace schemaNamespace) {
        this.elements = elements;
        this.types = types;
        this.namespace = null;
        this.schemaNamespace = schemaNamespace;
        this.targetNamespace = targetNamespace;
    }

    /** @return null */
    public java.awt.Component getCustomizer(HintContext nodeCtx) {
        return null;
    }
    
    /** @return null */
    public org.openide.nodes.Node.Property[] getProperties(HintContext nodeCtx) {
        return null;
    }
    
    /** @return false */
    public boolean hasCustomizer(HintContext nodeCtx) {
        return false;
    }
    
    /** not implemented
     * @return true
     */
    public boolean isAllowed(Enumeration en) {
        return true;
    }
    
    public Enumeration queryAttributes(HintContext ownerElementCtx) {
        Thread.dumpStack();
        return Collections.enumeration(new ArrayList());
    }
    
    public GrammarResult queryDefault(HintContext parentNodeCtx) {
        Thread.dumpStack();
        return null;
    }
    
    public Enumeration queryElements(HintContext virtualElementCtx) {
        String parentName = computeUnprefixedName(virtualElementCtx.getParentNode().getNodeName());
        SchemaElement parent = (SchemaElement) elements.get(parentName);
        
        if (parent == null) {
            return Collections.enumeration(new ArrayList());
        }
        
        ArrayList list = new ArrayList(50);
        String previous = (virtualElementCtx.getPreviousSibling() == null ? null : computeUnprefixedName(virtualElementCtx.getPreviousSibling().getNodeName()));
        System.err.println("PREVIOUS: " + previous);
        //parent.setPrefix(getNamespace().getPrefix());

        resolveChildren(parent, list, getNamespace());
        if (previous != null) {
            System.err.println("list size: " + list.size());
            while (list.size() > 0) {
                SchemaElement e = (SchemaElement) list.get(0);
                String ename = e.getSAXAttributes().getValue("name");
                System.err.println("ENAME: " + ename);
                
                if (ename == null || (! ename.equalsIgnoreCase(previous))) {
                    list.remove(0);
                    System.err.println("REMOVED ENAME");
                } else {
                    list.remove(0);
                    System.err.println("BUILD DONE");
                    break;
                }
            }
        }
        
        return Collections.enumeration(list);
    }
    
    /** divide by : to two parts */
    private String computeUnprefixedName(String s) {
        assert namespace != null;
        assert s != null;
        
        int i = s.indexOf(':');
        if (i >= 0) {
            String ret = s.substring(i + 1);
            System.err.println("computeUN nsPref: " + namespace.getPrefix() + " name: " + s + " ret: " + ret);
            assert namespace.getPrefix().equals(s.substring(0, i));
            return ret;
        }
        return s;
    }
    
    private static void resolveChildren(SchemaElement parent, List list, Namespace ns) {
        Iterator it = parent.getSubelements();
        while (it.hasNext()) {
            SchemaElement e = (SchemaElement) it.next();
            if (e.isComposite()) {
                System.err.println("RESOLVE COMPOSITE: " + e.getQname());
                resolveChildren(e, list, ns);
            } else {
                System.err.println("ADDING NON COMPOSITE: " + e.getQname());
                e.setPrefix(ns.getPrefix());
                list.add(e);
            }
        }        
    }
    
    public Enumeration queryEntities(String prefix) {
        Thread.dumpStack();
        return Collections.enumeration(new ArrayList());
    }
    
    public Enumeration queryNotations(String prefix) {
        Thread.dumpStack();
        return Collections.enumeration(new ArrayList());
    }
    
    public Enumeration queryValues(HintContext virtualTextCtx) {
        Thread.dumpStack();
        return Collections.enumeration(new ArrayList());
    }
    
    /**
     * Getter for property namespace.
     * @return Value of property namespace.
     */
    public org.netbeans.modules.xml.xsd.Namespace getNamespace() {
        return namespace;
    }
    
    /**
     * Setter for property namespace.
     * @param namespace New value of property namespace.
     */
    public void setNamespace(org.netbeans.modules.xml.xsd.Namespace namespace) {
        this.namespace = namespace;
    }
    
}
