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

import java.util.*;

import javax.swing.text.Document;
import javax.swing.JEditorPane;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorKit;

import org.netbeans.modules.editor.*;

import org.netbeans.modules.xml.core.XMLDataObject;

import org.netbeans.modules.xml.text.syntax.javacc.lib.*;
import org.netbeans.modules.xml.text.syntax.javacc.*;

/**
 * Editor kit implementation for xml content type
 *
 * @author Libor Kramolis
 * @author Petr Kuzel
 * @version jj
 */
public class XMLKit extends UniKit {

    /** Serial Version UID */
    private static final long serialVersionUID =5326735092324267367L;
    
    /** Create new instance of syntax coloring parser */
    public Syntax createSyntax(Document doc) {
        return new JJEditorSyntax(
            new XMLSyntaxTokenManager(null).new Bridge(),
            new XMLSyntaxTokenMapper(),
            XMLTokenContext.contextPath
        );
    }

    public Document createDefaultDocument() {
        return new NbEditorDocument (this.getClass());
    }

    // hack to be settings browseable
    public static Map settings;

    public static void setMap(Map map) {
        settings = map;
    }

    public Map getMap() {
        return settings;
    }

    public String getContentType() {
        return XMLDataObject.MIME_TYPE;
    }

}
