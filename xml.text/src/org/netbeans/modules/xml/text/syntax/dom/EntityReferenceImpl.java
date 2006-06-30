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

