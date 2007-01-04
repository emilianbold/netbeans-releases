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

package org.netbeans.modules.cnd.folding;

import java.io.Reader;
import java.util.List;
import org.netbeans.modules.cnd.editor.parser.FoldingParser;

/**
 * provider for Code Folding Parser
 * - for input document's stream construct list of CppFildRecords
 *
 * @author Vladimir Voskresensky
 */
public class APTFoldingProvider implements FoldingParser {
    
    /** we need public constructor for lookup */
    public APTFoldingProvider() {
    }

    public List/*<CppFoldRecord>*/ parse(String name, Reader source) {
        List/*<CppFoldRecord>*/ res = APTFoldingParser.parse(name, source);
        return res;
    }    
}
