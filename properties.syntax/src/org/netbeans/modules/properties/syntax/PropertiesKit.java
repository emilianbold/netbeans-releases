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

package org.netbeans.modules.properties.syntax;

import org.netbeans.editor.Syntax;
import org.netbeans.modules.editor.NbEditorKit;
import javax.swing.text.Document;

/**
* Editor kit implementation for text/properties content type
*
* @author Miloslav Metelka, Karel Gardas
* @version 0.01
*/

public class PropertiesKit extends NbEditorKit {

    public static final String PROPERTIES_MIME_TYPE = "text/x-properties"; // NOI18N    

    static final long serialVersionUID =3229768447965508461L;
    
    public String getContentType() {
        return PROPERTIES_MIME_TYPE;
    }
    
    /** Create new instance of syntax coloring parser */
    public Syntax createSyntax(Document doc) {
        return new PropertiesSyntax();
    }

}

/*
 * <<Log>>
 *  6    Jaga      1.4.1.0     3/15/00  Miloslav Metelka Structural change
 *  5    Gandalf   1.4         1/12/00  Petr Jiricka    Syntax coloring API 
 *       fixes
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         11/12/99 Miloslav Metelka NbEditorBaseKit as 
 *       parent
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */

