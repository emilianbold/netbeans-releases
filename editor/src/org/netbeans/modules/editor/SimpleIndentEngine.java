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

package org.netbeans.modules.editor;

import org.netbeans.editor.BaseKit;
import org.netbeans.editor.ext.ExtFormatter;
import org.openide.text.IndentEngine;
import org.openide.util.HelpCtx;

/**
* Java indentation engine that delegates to java formatter
*
* @author Miloslav Metelka
*/

public class SimpleIndentEngine extends FormatterIndentEngine {

    static final long serialVersionUID = -6445463074939516878L;

    public SimpleIndentEngine() {
    }

    protected ExtFormatter createFormatter() {
        return new ExtFormatter(BaseKit.class);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(SimpleIndentEngine.class);
    }

    protected boolean acceptMimeType(String mimeType) {
        return true;
    }

}

