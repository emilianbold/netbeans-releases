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

package org.netbeans.modules.palette;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Libor Kotouc
 */
class ActiveEditorDropDefaultProvider implements InstanceContent.Convertor {

    private static ActiveEditorDropDefaultProvider instance = new ActiveEditorDropDefaultProvider();

    /** Creates a new instance of ActiveEditorDropDefaultProvider */
    private ActiveEditorDropDefaultProvider() {
    }
    
    static ActiveEditorDropDefaultProvider getInstance() {
        return instance;
    }
    
    public Class type(Object obj) {
        //able to convert String instances only
        if (obj instanceof String)
            return ActiveEditorDrop.class;
        
        return null;
    }

    public String id(Object obj) {
        return obj.toString();
    }

    public String displayName(Object obj) {
        return ((Class)obj).getName();
    }

    public Object convert(Object obj) {
        Object drop = null;
        if (obj instanceof String)
            drop = getActiveEditorDrop((String)obj);

        return drop;
    }
    
    private ActiveEditorDrop getActiveEditorDrop(String body) {

        ActiveEditorDropDefault drop = new ActiveEditorDropDefault(body);
        return drop;
    }
    
    private static class ActiveEditorDropDefault implements ActiveEditorDrop {

        String body;

        public ActiveEditorDropDefault(String body) {
            this.body = body;
        }

        public boolean handleTransfer(JTextComponent targetComponent) {

            if (targetComponent == null)
                return false;

            try {
                Document doc = targetComponent.getDocument();
                Caret caret = targetComponent.getCaret();
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                doc.remove(p0, p1 - p0);

                //replace selected text by the inserted one
                int start = caret.getDot();
                doc.insertString(start, body, null);
            }
            catch (BadLocationException ble) {
                return false;
            }

            return true;
        }
    }
    
}
