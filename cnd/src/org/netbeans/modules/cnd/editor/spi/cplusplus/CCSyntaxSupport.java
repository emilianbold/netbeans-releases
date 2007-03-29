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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.cnd.editor.spi.cplusplus;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;

/**
 * 
 * @author Vladimir Voskresensky
 */
public class CCSyntaxSupport extends ExtSyntaxSupport {
    
    /** Creates a new instance of CCSyntaxSupport */
    public CCSyntaxSupport(BaseDocument doc) {
        super(doc);
    }
    
    /** Return the position of the last command separator before
    * the given position.
    */
    public int getLastCommandSeparator(int pos) throws BadLocationException {
        int stLine = Utilities.getRowFirstNonWhite(getDocument(), pos);
        if (stLine != -1 && stLine < pos) { 
            return stLine;
        }       
        return pos;
    }    
}
