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

import java.io.Writer;
import javax.swing.text.Document;
import org.netbeans.editor.Formatter;
import org.openide.text.IndentEngine;
import org.openide.util.HelpCtx;

/**
* Formatter wrapped around a generic indent engine.
*
* @author Miloslav Metelka
*/

public class IndentEngineFormatter extends Formatter {

    private IndentEngine indentEngine;

    /** Construct new formatter that delegates to the given indent engine.
     * @param kitClass class of the kit for which this formatter
     *  is being created.
     * @param indentEngine indentation engine to which this formatter
     *  delegates.
     */
    public IndentEngineFormatter(Class kitClass, IndentEngine indentEngine) {
        super(kitClass);

        this.indentEngine = indentEngine;
    }

    /** Get the indent engine to which this formatter delegates. */
    public IndentEngine getIndentEngine() {
        return indentEngine;
    }

    public int indentLine(Document doc, int offset) {
        return indentEngine.indentLine(doc, offset);
    }

    public int indentNewLine(Document doc, int offset) {
        return indentEngine.indentNewLine(doc, offset);
    }

    public Writer createWriter(Document doc, int offset, Writer writer) {
        return indentEngine.createWriter(doc, offset, writer);
    }

}

