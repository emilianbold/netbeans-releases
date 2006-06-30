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
