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

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.BeanInfo;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;



/**
 *
 * @author Libor Kotouc
 */
public final class PaletteItemNode extends AbstractNode {
    
    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];
    
    String displayName;
    String description;
    Image icon16;
    Image icon32;
    private Lookup localLookup;
    
    PaletteItemNode(String displayName, String description, Image icon16, Image icon32, Lookup lookup) {
        this( displayName, description, icon16, icon32, lookup, new InstanceContent() );
    }
    
    private PaletteItemNode(String displayName, String description, Image icon16, Image icon32, Lookup lookup, InstanceContent content ) {
        super(Children.LEAF, new ProxyLookup( new Lookup[] { lookup, new AbstractLookup(content) } ) );
        
        content.add( this );
        this.displayName = displayName;
        this.description = description;
        this.icon16 = icon16;
        this.icon32 = icon32;
        this.localLookup = lookup;
    }
 
    public String getDisplayName() {
        return displayName;
    }

    public String getShortDescription() {
        return description;
    }

    public Image getIcon(int type) {

        Image icon = icon16;
        
        if (type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32)
            icon = icon32;
        
        return icon;
    }
    
    public boolean canRename() {
        return false;
    }

    // TODO properties
    public Node.PropertySet[] getPropertySets() {
        return NO_PROPERTIES;
    }

    public Transferable clipboardCopy() throws IOException {

        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        
        Lookup lookup = getLookup();
        ActiveEditorDrop drop = (ActiveEditorDrop) lookup.lookup(ActiveEditorDrop.class);
        if (drop == null) {
            String body = (String)lookup.lookup(String.class);
            drop = new ActiveEditorDropDefault(body);
        }
        
        ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(drop);
        t.put(s);

        return t;
    }

    private static class ActiveEditorDropTransferable extends ExTransferable.Single {
        
        private ActiveEditorDrop drop;

        ActiveEditorDropTransferable(ActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);
            
            this.drop = drop;
        }
               
        public Object getData () {
            return drop;
        }
        
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
