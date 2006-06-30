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

package org.netbeans.modules.editor;

import java.io.Writer;
import javax.swing.text.Document;
import org.netbeans.editor.Formatter;
import org.openide.text.IndentEngine;

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
