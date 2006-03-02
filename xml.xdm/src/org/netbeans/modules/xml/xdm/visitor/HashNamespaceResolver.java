/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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