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