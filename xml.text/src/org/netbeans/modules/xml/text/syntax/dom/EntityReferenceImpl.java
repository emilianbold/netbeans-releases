/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.syntax.dom;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

/**
 * It should in future promote to EntityReference implementation.
 * Holds entity reference.
 * <p>
 * Difference from DOM: it's also created for well known entities and
 * character entities.
 *
 * @author Petr Kuzel
 */
public final class EntityReferenceImpl extends SyntaxNode implements EntityReference  {

    EntityReferenceImpl(XMLSyntaxSupport syntax, TokenItem token, int to) {
        super(syntax, token, to);
    }

    public String getNodeName() {
        TokenItem target = first.getNext();
        if (target != null) {
            String tokenImage = target.getImage();
            return tokenImage.substring(1, tokenImage.length()-1);
        } else {
            return "";  //??? or null
        }
    }

    public short getNodeType() {
        return Node.ENTITY_REFERENCE_NODE;
    }

    public String toString() {
        return "Ref(" + getNodeName() + ")";
    }
}

