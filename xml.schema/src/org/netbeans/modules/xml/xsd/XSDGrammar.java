/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xsd;

import java.util.*;
import javax.swing.Icon;

import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.netbeans.modules.xml.spi.dom.AbstractNode;



/**
 * Rather simple query implemetation based on XSD grammar.
 * It is produced by {@link XSDParser}.
 * Hints given by this grammar do not guarantee that valid XML document is created.
 *
 * @author  Petr Kuzel
 * @author  Ales Novak
 */
class XSDGrammar  implements GrammarQuery {
    
    /** All elements */
    private Map elements;
    /** All types */
    private Map types;
    /** namespace */
    private Namespace namespace;
    
    /** Creates new XSDGrammar */
    XSDGrammar(Map elements, Map types) {
        this.elements = elements;
        this.types = types;
        this.namespace = null;
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