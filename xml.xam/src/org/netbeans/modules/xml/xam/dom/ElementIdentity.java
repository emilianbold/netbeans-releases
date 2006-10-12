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

package org.netbeans.modules.xml.xam.dom;

import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used by the XDMModel for identification of elements from 2 documents,
 * by their establised attributes
 *
 * @author Ayub Khan
 */
public interface ElementIdentity {
	
	public List getIdentifiers();
	
	/* 
	 * add element identifiers like "id" "name", "ref" etc.,
	 *
	 * @param identifier
	 **/
	public void addIdentifier(String identifier);
	
	/* 
	 * callback for comparing e1 and e2. By default
	 * compares element localnames, then their namespace uri's, followed 
	 * by documents established identifying attributes for comparison
	 *
	 * @param e1
	 * @param e2
	 **/
	public boolean compareElement(Element e1, Element e2, Document doc1, Document doc2);
}
