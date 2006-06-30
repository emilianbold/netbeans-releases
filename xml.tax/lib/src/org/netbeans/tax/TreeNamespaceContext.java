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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Context allows to determive namespace URI by its context prefix.
 * <p>
 * Default namespace prefix is "".
 * <p>
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeNamespaceContext {

    /**
     * Namespace context is defined by element nesting
     * so we seach for parent context by quering parent element namespace context.
     */
    private TreeElement element;

    /**
     * It hold only namespaces defined at peer element.
     * It avoids problems with the following scenario:
     * <pre>
     *   &lt;ns1:root xmlns:ns1="original ns1">
     *      &lt;ns2:child xmlns:ns2="original ns2">
     *        &lt;ns1:kid xmlns:ns1="context redefined ns1 binding">
     * </pre>
     */
    private static Map definedNS = new HashMap ();
    
    static {
        TreeNamespace namespace;
        
        namespace = TreeNamespace.XML_NAMESPACE;
        definedNS.put (namespace.getPrefix (), namespace);
        
        namespace = TreeNamespace.XMLNS_NAMESPACE;
        definedNS.put (namespace.getPrefix (), namespace);
    }
    
    
    //
    // init
    //
    
    /**
     * Creates new TreeNamespaceContext.
     * Only TreeElement can do so.
     */
    protected TreeNamespaceContext (TreeElement element) {
        this.element = element;
    }
    
    
    /**
     * Traverse over parents and parse their attributes until given prefix is resolved.
     * @param prefix namespace prefix ("" default)
     * @return null it is not defined
     */
    public String getURI (String prefix) {
        
        // well known prefixes are in this map
        
        TreeNamespace ns = (TreeNamespace)definedNS.get (prefix);
        if (ns != null) {
            return ns.getURI ();
        }
        
        // look for attributes that defines namespaces
        // take cate to default namespace definition attribute
        
        TreeNamedObjectMap attrs = element.getAttributes ();
        if (attrs != null) {
            Iterator it = attrs.iterator ();
            while (it.hasNext ()) {
                TreeAttribute next = (TreeAttribute) it.next ();
                TreeName name = next.getTreeName ();
                if ("xmlns".equals (name.getPrefix ())) { // NOI18N
                    if (prefix.equals (name.getName ())) {
                        return next.getValue ();
                    }
                } else if ("xmlns".equals (name.getQualifiedName ())) { // NOI18N
                    return next.getValue ();
                }
            }
        }
        
        // try my parent
        
        TreeParentNode parentNode = element.getParentNode ();
        if ( parentNode instanceof TreeElement ) {
            TreeElement parentElement = (TreeElement)parentNode;
            if (parentElement != null) {
                return parentElement.getNamespaceContext ().getURI (prefix);
            }
        }
        return null;
    }
    
}
