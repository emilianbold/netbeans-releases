/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
