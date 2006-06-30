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
package org.netbeans.modules.xml.text.syntax;

import javax.swing.text.Document;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorKit;

import org.netbeans.modules.editor.*;

import org.netbeans.modules.xml.core.DTDDataObject;

import org.netbeans.modules.xml.text.syntax.javacc.lib.*;
import org.netbeans.modules.xml.text.syntax.javacc.*;

/**
 * Editor kit implementation for dtd content type.
 * In inherits inband encoding handling from UniKit because
 * a DTD is an external entity that must be UTF-8 encoded
 * or it must begin with "<?xml ...?>" i.e. encoding can be retrieved by 
 * inband-encoding atribute assisted autodetection.
 *
 * @author Libor Kramolis
 * @author Petr Kuzel
 * @version jj
 */
public class DTDKit extends UniKit {

    /** Serial Version UID */
    private static final long serialVersionUID =-6140259975700590155L;
    
    /** Create new instance of syntax coloring parser */
    public Syntax createSyntax(Document doc) {
        return new JJEditorSyntax( 
            new DTDSyntaxTokenManager(null).new Bridge(),
            new DTDSyntaxTokenMapper(),
            DTDTokenContext.contextPath
        );
    }

    public Document createDefaultDocument() {
        return new NbEditorDocument (this.getClass());
    }

    public String getContentType() {
        return DTDDataObject.MIME_TYPE;
    }

}
