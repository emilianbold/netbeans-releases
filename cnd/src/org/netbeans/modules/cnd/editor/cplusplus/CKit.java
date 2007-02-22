/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/** C++ editor kit with appropriate document */ 
package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.Document;
import org.netbeans.editor.Syntax;
import org.netbeans.modules.cnd.MIMENames;

public class CKit extends CCKit {

    public String getContentType() {
        return MIMENames.C_MIME_TYPE;
    }

    /**
     * Create new instance of a C syntax coloring scanner.
     *
     * @param doc document to operate on
     */
    public Syntax createSyntax(Document doc) {
        return new CSyntax();
    }
}
