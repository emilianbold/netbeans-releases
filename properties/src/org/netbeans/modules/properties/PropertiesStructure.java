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
import javax.swing.text.BadLocationException;

import org.openide.text.PositionBounds;

/** General abstract structure for properties files, in its use similar to source hierarchy.
*   Interoperates with Document, PropertiesTableModel, Nodes and other display-specific models
*   Implementations of the model interfaces generally reference this structure.
*
* @author Petr Jiricka
*/
public class PropertiesStructure extends Element {

    /** Holds individual items - Element.ItemElem */
    private ArrayMapList items;

    /** If active, contains link to its handler (parent) */
    private StructHandler handler;

    static final long serialVersionUID =-78380271920882131L;
    /** Constructs a new PropertiesStructure for the given bounds and items. */
    public PropertiesStructure(PositionBounds bounds, ArrayMapList items) {
        super(bounds);
        // set this structure as a parent for all elements
        for (Iterator it = items.iterator(); it.hasNext(); )
            ((Element.ItemElem)it.next()).setParent(this);
        this.items = items;
    }

    /** Updates the current structure by the new structure obtained by reparsing the document.
    * Looks for changes between the structures and according to them calls update methods.
    */
    public synchronized void update(PropertiesStructure struct) {
        boolean structChanged = false;
        Element.ItemElem curItem;
        Element.ItemElem oldItem;

        ArrayMapList new_items = struct.items;
        ArrayMapList changed  = new ArrayMapList();
        ArrayMapList inserted = new ArrayMapList();
        ArrayMapList deleted  = new ArrayMapList();

        for (Iterator it = new_items.iterator(); it.hasNext(); ) {
            curItem = (Element.ItemElem)it.next();
            curItem.setParent(this);
            oldItem = getItem(curItem.getKey());
            if (oldItem == null) {
                inserted.add(curItem.getKey(), curItem);
            }
            else {
                if (!curItem.equals(oldItem))
                    changed.add(curItem.getKey(), curItem);
                items.remove(oldItem.getKey());
            }
        }

        deleted = items;
        if ((deleted.size() > 0) || (inserted.size() > 0))
            structChanged = true;


        // assign the new structure
        items = new_items;

        // notification
        if (structChanged)
            structureChanged(changed, inserted, deleted);
        else {
            // notify about changes in all items
            for (Iterator it = changed.iterator(); it.hasNext(); )
                itemChanged((Element.ItemElem)it.next());
        }
    }

    /** Sets the parent of this element. */
    void setParent(StructHandler parent) {
        handler = parent;
    }

    public StructHandler getParent() {
        if (handler == null)
            throw new InternalError();
        return handler;
    }

    private BundleStructure getParentBundleStructure() {
        return ((PropertiesDataObject)getParent().getEntry().getDataObject()).getBundleStructure();
    }

    /** Get a string representation of the element for printing.
    * @return the string
    */
    public String printString() {
        StringBuffer sb = new StringBuffer();
        Element.ItemElem item;
        for (Iterator it = items.iterator(); it.hasNext(); ) {
            item = (Element.ItemElem)it.next();
            sb.append(item.printString());
        }
        return sb.toString();
    }


    /** Get a value string of the element.
    * @return the string
    */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Element.ItemElem item;
        for (Iterator it = items.iterator(); it.hasNext(); ) {
            item = (Element.ItemElem)it.next();
            sb.append(item.toString());
            sb.append("- - -\n");
        }
        return sb.toString();
    }

    /** Adds an item to the end, should be only used by the parser (no notification)
    *  Used by the update actions.
    */
    public void parserAddItem(Element.ItemElem item) {
        item.setParent(this);
        items.add(item.getKey(), item);
    }

    /** Retrieves an item by key (property name) or null if does not exist. */
    public Element.ItemElem getItem(String key) {
        return (Element.ItemElem)items.get(key);
    }

    /** Renames an item.
    * @return true if the item has been renamed successfully, false if another item with the same name exists.
    */                         
    public synchronized boolean renameItem(String oldKey, String newKey) {
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

    /** Deletes an item from the structure, if exists.
    * @return true if the item has been deleted successfully, false if it didn't exist.
    */                         
    public synchronized boolean deleteItem(String key) {
        /*System.out.println("PropertiesStructore: deleteItem(" + key + ")");
        System.out.println("------------------STRUCTURE------------------");
        System.out.println(toString());
        System.out.println("------------------END STRUCTURE------------------");*/
        if (key == null)
            key = "";
        if (key.length() == 0)
            return false;
        Element.ItemElem item = getItem(key);
        //System.out.println("-------------item --------------------\n" + item);
        if (item == null)
            return false;
        try {
            item.getBounds().setText("");
            items.remove(key);
            ArrayMapList deleted = new ArrayMapList();
            deleted.add(key, item);
            //System.out.println("-------------changing structure--------------------");
            structureChanged(new ArrayMapList(), new ArrayMapList(), deleted);
            /*System.out.println("------------------STRUCTURE AFTER------------------");
            System.out.println(toString());
            System.out.println("------------------END STRUCTURE AFTER------------------");*/
            return true;
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

    /** Adds an item to the end of the file, or before the terminating comment, if exists.
    * @return true if the item has been added successfully, false if another item with the same name exists.
    */                         
    public synchronized boolean addItem(String key, String value, String comment) {
        if (key == null)
            key = "";
        if (value == null)
            value = "";
        if (comment == null)
            comment = "";
        if (key.length() == 0 /*&& value.length() == 0 && comment.length() == 0*/)
            return false;

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
            synchronized (getParent()) {
                PositionBounds pos = getSuitablePositionBoundsForInsert();
                pos.insertAfter(item.printString());
                getParent().reparseNowBlocking();
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

    /** Returns PositionBounds after which a new item may be inserted by insertAfter(String) */
    private PositionBounds getSuitablePositionBoundsForInsert() {
        Element.ItemElem e = null;
        for (Iterator nonEmpty = nonEmptyItems(); nonEmpty.hasNext();)
            e = (Element.ItemElem)nonEmpty.next();
        if (e == null)
            return getBounds();
        else {
            e.print();
            return e.getBounds();
        }
    }

    /** Returns an iterator iterating through items which have non-null key.
    * The method was changed since its creation and now should be named
    * nonNullItems to better fit the functionality.
    */
    public Iterator nonEmptyItems() {
        return new Iterator() {
                   // iterator which relies on the list's iterator
                   private Iterator innerIt;
                   /** Next non-empty element in the underlying iterator */
                   private Element.ItemElem nextElem;

                   {
                       innerIt = items.iterator();
                       fetchNext();
                   }

                   /** Fetches internally the next non-empty element */
                   private void fetchNext() {
                       do {
                           if (innerIt.hasNext())
                               nextElem = (Element.ItemElem)innerIt.next();
                           else
                               nextElem = null;
                       }
                       while (nextElem != null && nextElem.key == null); // key can be an empty string
                   }

                   public boolean hasNext() {
                       return nextElem != null;
                   }

                   public Object next() {
                       Object ne = nextElem;
                       fetchNext();
                       return ne;
                   }

                   public void remove() {
                       throw new UnsupportedOperationException();
                   }

               };
    }

    /** Returns iterator thropugh all items, including empty ones */
    public Iterator allItems() {
        return items.iterator();
    }

    /** Notification that the given item has changed (its value or comment) */
    void itemChanged(Element.ItemElem elem) {
        getParentBundleStructure().itemChanged(elem);
    }

    /** Notification that the structure has changed (no specific information). */
    void structureChanged() {
        getParentBundleStructure().oneFileChanged(getParent());
    }

    /** Notification that the structure has changed (items have been added or deleted,
    * also includes changing an item's key). */
    void structureChanged(ArrayMapList changed, ArrayMapList inserted, ArrayMapList deleted) {
        getParentBundleStructure().oneFileChanged(getParent(), changed, inserted, deleted);
    }

    /** Notification that an item's key has changed. Subcase of structureChanged().
    * Think twice when using this - don't I need to reparse all files ?
    */
    void itemKeyChanged(String oldKey, Element.ItemElem newElem) {
        // update the element in the structure, because now it is in with the wrong key
        int index = items.indexOf(oldKey);
        if (index < 0)
            throw new InternalError();
        items.set(index, newElem.getKey(), newElem);

        // structural change information - watch: there may be two properties of the same name !
        // maybe this is unnecessary
        ArrayMapList changed  = new ArrayMapList();
        ArrayMapList inserted = new ArrayMapList();
        ArrayMapList deleted  = new ArrayMapList();

        // old key
        Element.ItemElem item = getItem(oldKey);
        if (item == null)
            // old key deleted
            deleted.add(oldKey, new Element.ItemElem( null, new Element.KeyElem(null, oldKey),
                        new Element.ValueElem(null, "") , new Element.CommentElem(null, "")));
        else
            // old key changed
            changed.add(item.getKey(), item);

        // new key
        inserted.add(newElem.getKey(), newElem);

        structureChanged(changed, inserted, deleted);
    }
}

/*
 * <<Log>>
 */
