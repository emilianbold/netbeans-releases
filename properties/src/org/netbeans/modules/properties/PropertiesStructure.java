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


import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.swing.text.BadLocationException;

import org.openide.text.PositionBounds;


/** 
 * Element structure for one .properties file tightly 
 * bound with that file's document.
 *
 * @author Petr Jiricka
 */
public class PropertiesStructure extends Element {

    /** Map of <code>Element.KeyElem</code> to <code>Element.ItemElem</code>. */
    private Map items;

    /** If active, contains link to its handler (parent) */
    private StructHandler handler;

    /** Generated serial version UID. */
    static final long serialVersionUID =-78380271920882131L;
    
    
    /** Constructs a new PropertiesStructure for the given bounds and items. */
    public PropertiesStructure(PositionBounds bounds, Map items) {
        super(bounds);
        // set this structure as a parent for all elements
        for(Iterator it = items.values().iterator(); it.hasNext();)
            ((Element.ItemElem)it.next()).setParent(this);
        this.items = items;
    }

    
    /** Updates the current structure by the new structure obtained by reparsing the document.
     * Looks for changes between the structures and according to them calls update methods.
     */
    public void update(PropertiesStructure struct) {
        synchronized(getParent()) {
            boolean structChanged = false;
            Element.ItemElem curItem;
            Element.ItemElem oldItem;

            Map new_items = struct.items;
            Map changed  = new HashMap();
            Map inserted = new HashMap();
            Map deleted  = new HashMap();

            for(Iterator it = new_items.values().iterator(); it.hasNext(); ) {
                curItem = (Element.ItemElem)it.next();
                curItem.setParent(this);
                oldItem = getItem(curItem.getKey());
                if (oldItem == null) {
                    inserted.put(curItem.getKey(), curItem);
                } else {
                    if (!curItem.equals(oldItem))
                        changed.put(curItem.getKey(), curItem);
                    items.remove(oldItem.getKey());
                }
            }

            deleted = items;
            if((deleted.size() > 0) || (inserted.size() > 0))
                structChanged = true;

            // assign the new structure
            items = new_items;

            // notification
            if(structChanged)
                structureChanged(changed, inserted, deleted);
            else {
                // notify about changes in all items
                for (Iterator it = changed.values().iterator(); it.hasNext(); )
                    itemChanged((Element.ItemElem)it.next());
            }
        }
    }

    /** Sets the parent of this element. */
    void setParent(StructHandler parent) {
        handler = parent;
    }

    /** Gets parent for this properties structure. 
     * @return <code>StructureHandler</code> instance. */
    public StructHandler getParent() {
        if(handler == null)
            throw new IllegalStateException();
        return handler;
    }

    /** Gets bundle structure of bundles where this .properties file belongs to. */
    private BundleStructure getParentBundleStructure() {
        return ((PropertiesDataObject)getParent().getEntry().getDataObject()).getBundleStructure();
    }

    /** Prints all structure to document.
     * @return the structure dump */
    public String printString() {
        StringBuffer sb = new StringBuffer();
        Element.ItemElem item;
        for (Iterator it = items.values().iterator(); it.hasNext(); ) {
            item = (Element.ItemElem)it.next();
            sb.append(item.printString());
        }
        
        return sb.toString();
    }

    /** Overrides superclass method.
     * @return the formatted structure dump */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Element.ItemElem item;
        for(Iterator it = items.values().iterator(); it.hasNext(); ) {
            item = (Element.ItemElem)it.next();
            sb.append(item.toString());
            sb.append("- - -\n"); // NOI18N
        }
        
        return sb.toString();
    }

    /** Retrieves an item by key (property name) or null if does not exist. */
    public Element.ItemElem getItem(String key) {
        return (Element.ItemElem)items.get(key);
    }

    /** Renames an item.
     * @return true if the item has been renamed successfully, false if another item with the same name exists.
     */                         
    public boolean renameItem(String oldKey, String newKey) {
        synchronized(getParent()) {
            Element.ItemElem item = getItem(newKey);
            if (item == null) {
                item = getItem(oldKey);
                if (item == null)
                    return false;
                item.setKey(newKey);
                return true;
            }
            else
                return false;
        }
    }

    /** Deletes an item from the structure, if exists.
     * @return <code>true<code> if the item has been deleted successfully, <code>false</code> otherwise */
    public boolean deleteItem(String key) {
        synchronized(getParent()) {
            Element.ItemElem item = getItem(key);
            
            if (item == null)
                return false;
            try {
                item.getBounds().setText(""); // NOI18N
                return true;
            } catch (IOException e) {
                // PENDING
                return false;
            } catch (BadLocationException e) {
                // PENDING
                return false;
            }
        }
    }

    /** Adds an item to the end of the file, or before the terminating comment, if exists.
     * @return <code>true</code> if the item has been added successfully, <code>false</code> otherwise */
    public boolean addItem(String key, String value, String comment) {
        Element.ItemElem item = getItem(key);
        if (item != null)
            return false;

        // construct the new element
        item = new Element.ItemElem(null,
                                    new Element.KeyElem    (null, key),
                                    new Element.ValueElem  (null, value),
                                    new Element.CommentElem(null, comment));
        // find the position where to add it
        try {
            synchronized(getParent()) {
                PositionBounds pos = getSuitablePositionBoundsForInsert();
                pos.insertAfter(item.printString());
                return true;
            }
        }
        catch (IOException e) {
            // PENDING
            return false;
        }
        catch (BadLocationException e) {
            // PENDING
            return false;
        }
    }

    /** Gets suitable positon for inserting of next item into document.
     * @return <code>PositionBounds</code> after which a new item may be inserted by insertAfter method 
     * @see org.openide.text.PositionBounds#insertAfter */
    private PositionBounds getSuitablePositionBoundsForInsert() {
        Element.ItemElem e = null;
        
        for(Iterator it = items.values().iterator(); it.hasNext();)
            e = (Element.ItemElem)it.next();
        
        if (e == null)
            return getBounds();
        else {
            e.print();
            return e.getBounds();
        }
    }

    /** Returns iterator thropugh all items, including empty ones */
    public Iterator allItems() {
        return items.values().iterator();
    }

    /** Notification that the given item has changed (its value or comment) */
    void itemChanged(Element.ItemElem elem) {
        getParentBundleStructure().itemChanged(elem);
    }

    /** Notification that the structure has changed (no specific information). */
    void structureChanged() {
        getParentBundleStructure().oneFileChanged(getParent());
    }

    /** Notification that the structure has changed (items have been added or
     * deleted, also includes changing an item's key). */
    void structureChanged(Map changed, Map inserted, Map deleted) {
        getParentBundleStructure().oneFileChanged(getParent(), changed, inserted, deleted);
    }

    /** Notification that an item's key has changed. Subcase of structureChanged().
     * Think twice when using this - don't I need to reparse all files ? */
    void itemKeyChanged(String oldKey, Element.ItemElem newElem) {
        // structural change information - watch: there may be two properties of the same name !
        // maybe this is unnecessary
        Map changed  = new HashMap();
        Map inserted = new HashMap();
        Map deleted  = new HashMap();

        // old key
        Element.ItemElem item = getItem(oldKey);
        if(item == null)
            // old key deleted
            deleted.put(oldKey, new Element.ItemElem( null, new Element.KeyElem(null, oldKey),
                new Element.ValueElem(null, "") , new Element.CommentElem(null, "")));
        else
            // old key changed
            changed.put(item.getKey(), item);

        // new key
        inserted.put(newElem.getKey(), newElem);

        structureChanged(changed, inserted, deleted);
    }
}