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


package org.netbeans.modules.properties;


import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.util.datatransfer.*;
import org.openide.actions.*;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;


/** Standard node representing a key-value-comment item in the properties file.
*
* @author Petr Jiricka
*/
public class KeyNode extends AbstractNode {

    /** Structure on top of which this element lives. */
    private PropertiesStructure struct;
    
    /** Key for the element. */
    private String itemKey;
    
    /** Generated Serialized Version UID. */
    static final long serialVersionUID = -7882925922830244768L;

    /** Icon base for the KeyNode node. */
    static final String ITEMS_ICON_BASE = "org/netbeans/modules/properties/propertiesKey"; // NOI18N


    /** Create a data node for a given key.
    * The provided children object will be used to hold all child nodes.
    * @param entry entry to work with
    * @param ch children container for the node
    */
    public KeyNode (PropertiesStructure struct, String itemKey) {
        super (Children.LEAF);
        this.struct = struct;
        this.itemKey = itemKey;
        super.setName (UtilConvert.unicodesToChars(itemKey));
        setDefaultAction(SystemAction.get(OpenAction.class));
        setActions(
            new SystemAction[] {
                SystemAction.get(OpenAction.class),
                SystemAction.get(EditAction.class),
                SystemAction.get(FileSystemAction.class),
                null,
                SystemAction.get(CutAction.class),
                SystemAction.get(CopyAction.class),
                null,
                SystemAction.get(DeleteAction.class),
                SystemAction.get(RenameAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class)
            }
        );
        setIconBase (ITEMS_ICON_BASE);

        // edit as a viewcookie
        PropertiesDataObject pdo = ((PropertiesDataObject)struct.getParent().getEntry().getDataObject());

        getCookieSet().add(pdo.getOpenSupport().new PropertiesOpenAt(itemKey));
        getCookieSet().add(struct.getParent().getEntry().getPropertiesEditor().new PropertiesEditAt(itemKey));
    }

    /** Get the represented item.
     * @return the item
    */
    public Element.ItemElem getItem() {
        Element.ItemElem item = struct.getItem(itemKey);
        /*if (item == null)
          // PENDING   */
        return item;
    }


    /** Indicate whether the node may be destroyed.
     * @return true.
     */
    public boolean canDestroy () {
        return true;
    }

    /* Destroyes the node
    */
    public void destroy () throws IOException {
        struct.deleteItem(itemKey);
        super.destroy ();
    }

    /* Returns true if this node allows copying.
    * @returns true.
    */
    public final boolean canCopy () {
        return true;
    }

    /* Returns true if this node allows cutting.
    * @returns true.
    */
    public final boolean canCut () {
        return true;
    }

    /* Returns true if this node can be renamed.
    * @returns true.
    */
    public final boolean canRename () {
        return true;
    }

    /* Rename the node.
    * @param name new name for the object
    * @exception IllegalArgumentException if the rename failed
    */
    public void setName (String name) {
        String oldKey = itemKey;
        name = UtilConvert.charsToUnicodes(UtilConvert.escapePropertiesSpecialChars(name));
        itemKey = name;
        if (!struct.renameItem(oldKey, name)) {
            itemKey = oldKey;
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                               NbBundle.getBundle(KeyNode.class).getString("MSG_CannotRenameKey"),
                                               NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(msg);
            return;
        }
        updateCookieNames();
        // regenerate all children
        /*    Node par = getParentNode();
            PropertiesFileEntry.PropKeysChildren ch = (PropertiesFileEntry.PropKeysChildren)par.getChildren();
            ch.mySetKeys();*/
    }


    /** Updates the cookies for editing/viewing at a given position. */
    private void updateCookieNames() {
        // open cookie
        Node.Cookie opener = getCookie(OpenCookie.class);
        if (opener instanceof PropertiesOpen.PropertiesOpenAt) {
            ((PropertiesOpen.PropertiesOpenAt)opener).setKey(itemKey);
        }

        // view cookie
        Node.Cookie viewer = getCookie(ViewCookie.class);
        if (viewer instanceof PropertiesEditorSupport.PropertiesEditAt) {
            ((PropertiesEditorSupport.PropertiesEditAt)viewer).setKey(itemKey);
        }
    }


    /** Set all actions for this node.
    * @param actions new list of actions
    */
    public void setActions(SystemAction[] actions) {
        systemActions = actions;
    }

    /* Initializes sheet of properties. Allow subclasses to
    * overwrite it.
    * @return the default sheet to use
    */
    protected Sheet createSheet () {
        Sheet s = Sheet.createDefault ();
        Sheet.Set ss = s.get (Sheet.PROPERTIES);

        Node.Property p;

        // Key property
        p = new PropertySupport.ReadWrite (
                PROP_NAME,
                String.class,
                NbBundle.getBundle(KeyNode.class).getString("PROP_item_key"),
                NbBundle.getBundle(KeyNode.class).getString("HINT_item_key")
            ) {
                public Object getValue () {
                    return itemKey;
                }

                public void setValue (Object val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    if (!(val instanceof String))
                        throw new IllegalArgumentException();

                    KeyNode.this.setName ((String)val);
                }
            };
        p.setName (Element.ItemElem.PROP_ITEM_KEY);
        ss.put (p);

        // Value property
        p = new PropertySupport.ReadWrite (
                Element.ItemElem.PROP_ITEM_VALUE,
                String.class,
                NbBundle.getBundle(KeyNode.class).getString("PROP_item_value"),
                NbBundle.getBundle(KeyNode.class).getString("HINT_item_value")
            ) {
                public Object getValue () {
                    return getItem().getValue();
                }

                public void setValue (Object val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    if (!(val instanceof String))
                        throw new IllegalArgumentException();

                    getItem().setValue((String)val);
                }
            };
        p.setName (Element.ItemElem.PROP_ITEM_VALUE);
        ss.put (p);

        // Comment property
        p = new PropertySupport.ReadWrite (
                Element.ItemElem.PROP_ITEM_COMMENT,
                String.class,
                NbBundle.getBundle(KeyNode.class).getString("PROP_item_comment"),
                NbBundle.getBundle(KeyNode.class).getString("HINT_item_comment")
            ) {
                public Object getValue () {
                    return getItem().getComment();
                }

                public void setValue (Object val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    if (!(val instanceof String))
                        throw new IllegalArgumentException();

                    getItem().setComment((String)val);
                }
            };
        p.setName (Element.ItemElem.PROP_ITEM_COMMENT);
        ss.put (p);

        return s;
    }

    /** Returns all the item in addition to "normal" cookies. */
    public Node.Cookie getCookie(Class cls) {
        if (cls.isInstance(getItem())) return getItem();
        if (cls.equals(SaveCookie.class)) return struct.getParent().getEntry().getCookie(cls);
        return super.getCookie(cls);
    }

    /** Support for firing property change.
    * @param ev event describing the change
    */
    void fireChange (PropertyChangeEvent ev) {
        firePropertyChange (ev.getPropertyName (), ev.getOldValue (), ev.getNewValue ());
        if (ev.getPropertyName ().equals (DataObject.PROP_NAME)) {
            super.setName (itemKey);
            return;
        }
    }

}