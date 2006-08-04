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

package org.netbeans.modules.mobility.editor;
import org.netbeans.editor.BaseDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;

/**
 * Created by IntelliJ IDEA.
 * User: bohemius
 * Date: Jul 20, 2005
 * Time: 7:29:44 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Utilities {
    
    private Utilities()
    {
        // We don't want to create instance of this class
    }
    
    public static String getLine(final JTextComponent component) {
        return getLine(component,component.getCaret().getDot());
    }
    
    public static String getLine(final JTextComponent component, final int offset) {
        final BaseDocument workingDocument=org.netbeans.editor.Utilities.getDocument(component);
        try {
            final int lineStartOffset = org.netbeans.editor.Utilities.getRowStart(workingDocument, offset);
            final int lineEndOffset = org.netbeans.editor.Utilities.getRowEnd(workingDocument, offset);
            return String.valueOf(workingDocument.getChars(lineStartOffset, lineEndOffset - lineStartOffset));
        } catch (BadLocationException ble) {
            return "";
        }
    }
}
