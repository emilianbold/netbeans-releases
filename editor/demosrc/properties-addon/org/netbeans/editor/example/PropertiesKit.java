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

package org.netbeans.editor.example;

import org.netbeans.editor.Settings;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.ext.ExtKit;
import javax.swing.text.Document;

/**
* Editor kit implementation for text/properties content type
*
* @author Miloslav Metelka, Karel Gardas
* @version 0.01
*/

public class PropertiesKit extends ExtKit {
    static {
        Settings.addInitializer( new PropertiesSettingsInitializer( PropertiesKit.class ) );
        Settings.reset();
    }

    static final long serialVersionUID =3229768447965508461L;
    /** Create new instance of syntax coloring parser */
    public Syntax createSyntax(Document doc) {
        return new org.netbeans.modules.properties.syntax.PropertiesSyntax();
    }

}
