/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.syntax;

import javax.swing.text.Document;
import javax.swing.JEditorPane;

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
