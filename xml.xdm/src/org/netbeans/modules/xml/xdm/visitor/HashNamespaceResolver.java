/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.namespace.NamespaceContext;

public class HashNamespaceResolver implements NamespaceContext {
	private Map<String, String> prefixes; // namespace, prefix
	private Map<String, String> namespaces;  // prefix, namespace
	
	public HashNamespaceResolver(Map<String,String> nsTable) {
		namespaces = nsTable;
		prefixes = new HashMap<String,String>();
		for (Entry<String,String> e : namespaces.entrySet()) {
			prefixes.put(e.getValue(), e.getKey());
		}
	}
	
	public HashNamespaceResolver(Map<String,String> namespaces, Map<String,String> prefixes) {
            this.namespaces = namespaces;
            this.prefixes = prefixes;
        }
        
	public Iterator getPrefixes(String namespaceURI) {
		return Collections.singletonList(getPrefix(namespaceURI)).iterator();
	}
	
	public String getPrefix(String namespaceURI) {
		return prefixes.get(namespaceURI);
	}
	
	public String getNamespaceURI(String prefix) {
		return namespaces.get(prefix);
	}
	
}