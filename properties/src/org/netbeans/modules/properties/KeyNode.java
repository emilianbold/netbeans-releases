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


import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.openide.actions.*;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/** 
 * Node representing a key-value-comment item in one .properties file.
 *
 * @author Petr Jiricka
 */
public class KeyNode extends AbstractNode {

    /** Structure on top of which this element lives. */
    private PropertiesStructure propStructure;
    
    /** Key for the element. */
    private String itemKey;
    
    /** Generated Serialized Version UID. */
    static final long serialVersionUID = -7882925922830244768L;


    /** Constructor.
     * @param propStructure structure of .properties file to work with
     * @param itemKey key value of item in properties structure
     */
    public KeyNode (PropertiesStructure propStructure, String itemKey) {
        super(Children.LEAF);
        
        this.propStructure = propStructure;
        this.itemKey = itemKey;
        
        super.setName(UtilConvert.unicodesToChars(itemKey));
        
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
        
        setIconBase("org/netbeans/modules/properties/propertiesKey"); // NOI18N

        // Sets short description.
        setShortDescription();

        // Sets cookies (Open and Edit).
        PropertiesDataObject pdo = ((PropertiesDataObject)propStructure.getParent().getEntry().getDataObject());

        getCookieSet().add(pdo.getOpenSupport().new PropertiesOpenAt(propStructure.getParent().getEntry(), itemKey));
        getCookieSet().add(propStructure.getParent().getEntry().getPropertiesEditor().new PropertiesEditAt(itemKey));
    }

    /** Gets <code>Element.ItemElem</code> represented by this node.
     * @return item element
     */
    public Element.ItemElem getItem() {
        return propStructure.getItem(itemKey);
    }

    /** Gets help context. Overrides superclass method. */ 
    public HelpCtx getHelpCtx() {
        return new HelpCtx(Util.HELP_ID_ADDING);
    }
    
    /** Indicates whether the node may be destroyed. Overrides superclass method.
     * @return true.
     */
    public boolean canDestroy () {
        return true;
    }

    /** Destroyes the node. Overrides superclass method. */
    public void destroy () throws IOException {
        propStructure.deleteItem(itemKey);
        super.destroy ();
    }

    /** Indicates if node allows copying. Overrides superclass method.
     * @return true.
     */
    public final boolean canCopy () {
        return true;
    }

    /** Indicates if node allows cutting. Overrides superclass method.
     * @return true.
     */
    public final boolean canCut () {
        return true;
    }

    /** Indicates if node can be renamed. Overrides superclass method.
     * @returns true.
     */
    public final boolean canRename () {
        return true;
    }

    /** Sets name of the node. Overrides superclass method.
     * @param name new name for the object
     */
    public void setName(String name) {
        // The new name is same -> do nothing.
        if(name.equals(UtilConvert.unicodesToChars(itemKey)))
            return;
        
        String oldKey = itemKey;
        name = UtilConvert.charsToUnicodes(UtilConvert.escapePropertiesSpecialChars(name));
        itemKey = name;
        if (!propStructure.renameItem(oldKey, name)) {
            itemKey = oldKey;
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                NbBundle.getBundle(KeyNode.class).getString("MSG_CannotRenameKey"),
                NotifyDescriptor.ERROR_MESSAGE
            );
            TopManager.getDefault().notify(msg);
            return;
        }
        
        updateCookieNames();
    }

    /** Initializes sheet of properties. Overrides superclass method.
     * @return default sheet to use
     */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault ();
        Sheet.Set sheetSet = sheet.get (Sheet.PROPERTIES);

        Node.Property property;

        // Key property.
        property = new PropertySupport.ReadWrite (
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

                    KeyNode.this.setName((String)val);
                }
            };
        property.setName(Element.ItemElem.PROP_ITEM_KEY);
        sheetSet.put (property);

        // Value property
        property = new PropertySupport.ReadWrite (
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
        property.setName(Element.ItemElem.PROP_ITEM_VALUE);
        sheetSet.put (property);

        // Comment property
        property = new PropertySupport.ReadWrite (
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
        property.setName(Element.ItemElem.PROP_ITEM_COMMENT);
        sheetSet.put (property);

        return sheet;
    }

    /** Returns item as cookie in addition to "normal" cookies. Overrides superclass method. */
    public Node.Cookie getCookie(Class clazz) {
        if (clazz.isInstance(getItem())) 
            return getItem();
        
        if (clazz.equals(SaveCookie.class)) 
            return propStructure.getParent().getEntry().getCookie(clazz);
        
        return super.getCookie(clazz);
    }

    /** Sets short description. Helper method. Calls superclass <code>setShortDescription(String)</code> method. 
     * @see java.beans.FeatureDescriptor#setShortDecription(String) */
    private void setShortDescription() {
        String description;
        
        Element.ItemElem item = getItem();

        if(item != null)
            description = UtilConvert.unicodesToChars(item.getKey() + "=" + item.getValue());
        else {
            description = UtilConvert.unicodesToChars(itemKey);
        }
        
        setShortDescription(description);
    }

    /** Overrides superclass method. 
     * @return true */
    public boolean hasCustomizer() {
        return true;
    }
    
    /** Overrides superclass method. 
     * @return customizer for this key node */
    public Component getCustomizer() {
        return new PropertyPanel(getItem());
    }
    
    /** Updates the cookies for editing/viewing at a given position (position of key element representing by this node). Helper method. */
    private void updateCookieNames() {
        // Open cookie.
        Node.Cookie opener = getCookie(OpenCookie.class);
        if(opener instanceof PropertiesOpen.PropertiesOpenAt) {
            ((PropertiesOpen.PropertiesOpenAt)opener).setKey(itemKey);
        }

        // Edit cookie.
        Node.Cookie editor = getCookie(EditCookie.class);
        if(editor instanceof PropertiesEditorSupport.PropertiesEditAt) {
            ((PropertiesEditorSupport.PropertiesEditAt)editor).setKey(itemKey);
        }
    }
    
    /** Sets all actions for this node. Helper method.
     * @param actions new list of actions
     */
    private void setActions(SystemAction[] actions) {
        systemActions = actions;
    }
    
}