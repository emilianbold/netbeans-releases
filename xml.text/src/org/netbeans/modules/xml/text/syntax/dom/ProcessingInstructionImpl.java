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

import org.netbeans.editor.TokenItem;

import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
/**
 * Read-only PI DOM node.
 *
 * @author  Petr Kuzel
 */
public final class ProcessingInstructionImpl extends SyntaxNode {


    /** Creates a new instance of ProcessingInstructionImpl */
    public ProcessingInstructionImpl(XMLSyntaxSupport syntax, TokenItem from, int to) {
        super(syntax, from, to);
    }

    /**
     * A code representing the type of the underlying object, as defined above.
     */
    public short getNodeType() {
        return Node.PROCESSING_INSTRUCTION_NODE;
    }
    
    /**
     * The target of this processing instruction. XML defines this as being 
     * the first token following the markup that begins the processing 
     * instruction.
     * @return implementation may return "xml" as it consider it a PI
     */
    public String getTarget() {
        TokenItem target = first.getNext();
        if (target != null) {
            return target.getImage();
        } else {
            return "";  //??? or null
        }
    }

    public String getNodeName() {
        return getTarget();
    }
    
    /**
     * The content of this processing instruction. This is from the first non 
     * white space character after the target to the character immediately 
     * preceding the <code>?&gt;</code>.
     * @return may return ""
     */
    public String getData() {
        StringBuffer buf = new StringBuffer();
        TokenItem next = first.getNext();
        while (next != null && next.getTokenID() != XMLDefaultTokenContext.PI_CONTENT) {
            next = next.getNext();
        }
        if (next == null) return "";  //??? or null
        do {
            buf.append(next.getImage());
            next = next.getNext();
        } while (next != null && next.getTokenID() == XMLDefaultTokenContext.PI_CONTENT);
        return buf.toString();
    }

    public String getNodeValue() {
        return getData();
    }
    
    /**
     * Once again we are read-only implemetation!
     */
    public void setData(String data) throws DOMException {
        throw new ROException();
    }

}
