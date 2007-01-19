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

package org.netbeans.modules.xml.xdm.diff;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.w3c.dom.NamedNodeMap;

/*
 * This class is used by DiffFinder to compare 2 elements by identifying attributes
 *
 * @author Ayub Khan
 */
public class DefaultElementIdentity implements ElementIdentity {
    
    /**
     * Creates a new instance of DefaultElementIdentity
     */
    public DefaultElementIdentity() {
    }
    
    public List getIdentifiers() {
        return identifiers;
    }
    
    public void addIdentifier(String identifier) {
        if(!identifiers.contains(identifier))
            identifiers.add(identifier);
    }
    
    public boolean compareElement(org.w3c.dom.Element n1, org.w3c.dom.Element n2, org.w3c.dom.Document doc1, org.w3c.dom.Document doc2) {
        return compareElement(n1, n2, null, doc1, doc2);
    }
    
    protected boolean compareElement(org.w3c.dom.Element n1, org.w3c.dom.Element n2, org.w3c.dom.Node parent1, org.w3c.dom.Document doc1, org.w3c.dom.Document doc2) {
        String qName1 = n1.getLocalName();
        String qName2 = n2.getLocalName();
        String ns1 = ((Node)n1).getNamespaceURI((Document) doc1);
        String ns2 = ((Node)n2).getNamespaceURI((Document) doc2);
        
        if ( qName1.intern() !=  qName2.intern() )
            return false;
        if ( !(ns1 == null && ns2 == null) &&
                !(ns1 != null && ns2 != null && ns1.intern() == ns2.intern() ) )
            return false;
        
        if(parent1 == doc1) return true; //if root no need to compare other identifiers
        
        return compareAttr( n1, n2);
    }
    
    protected boolean compareAttr(org.w3c.dom.Element n1, org.w3c.dom.Element n2) {
        NamedNodeMap attrs1 = n1.getAttributes();
        NamedNodeMap attrs2 = n2.getAttributes();
        
        List<String> nameSet = getIdentifiers();
        if( nameSet.isEmpty() )
            return true;
        else if ( attrs1.getLength() == 0 && attrs2.getLength() == 0 )
            return true;
        
        int matchCount = 0;
        int unmatchCount = 0;
        for ( String name:nameSet ) {
            Node attr1 = (Node) attrs1.getNamedItem( name );
            Node attr2 = (Node) attrs2.getNamedItem( name );
            if ( attr1 == null && attr2 == null )
                continue;
            else if ( attr1 != null && attr2 != null ){
                if ( attr2.getNodeValue().intern() != attr1.getNodeValue().intern() )
                    unmatchCount++;
                else
                    matchCount++;
            } else
                unmatchCount++;
            //check for exact match
            if ( matchCount == 1 )
                return true;
            
            //check for rename
            if ( unmatchCount == 1 && attrs1.getLength() == attrs2.getLength() )
                return false;
        }
        
        //no attributes in attrs1 and attrs2 that match nameSet
        if ( matchCount == 0 && unmatchCount == 0 )
            return true;
        
        return false;
    }
    
    public void clear() {
        identifiers.clear();
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////////
    
    private List<String> identifiers = new ArrayList<String>();
}
