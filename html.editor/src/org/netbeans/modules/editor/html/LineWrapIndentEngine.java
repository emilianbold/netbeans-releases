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
import org.netbeans.editor.ext.html.LineWrapFormatter;
import org.netbeans.modules.editor.FormatterIndentEngine;

/**
 * HTML indentation engine based on LineWrapFormatter.
 *
 * @author Petr Nejedly
 */

public class LineWrapIndentEngine extends FormatterIndentEngine {

    static final long serialVersionUID = -7936605291288152329L;


    public LineWrapIndentEngine() {
        setAcceptedMimeTypes(new String[] { HTMLKit.HTML_MIME_TYPE });
    }

    protected ExtFormatter createFormatter() {
        return new LineWrapFormatter(HTMLKit.class);
    }

}
