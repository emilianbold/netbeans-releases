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

package org.netbeans.modules.editor.html;

import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.html.HTMLFormatter;
import org.netbeans.modules.editor.FormatterIndentEngine;

public class HTMLIndentEngine extends FormatterIndentEngine {
    
    private static final long serialVersionUID = -4461748971820599978L;    
    
    
    /** Creates a new instance of HTMLIndentEngine */
    public HTMLIndentEngine() {
        setAcceptedMimeTypes(new String[] { HTMLKit.HTML_MIME_TYPE });
    }
    
    protected ExtFormatter createFormatter() {
        return new HTMLFormatter(HTMLKit.class);
    }

}
