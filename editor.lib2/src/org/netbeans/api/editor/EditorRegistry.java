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

package org.netbeans.api.editor;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.modules.editor.lib2.EditorApiPackageAccessor;

/**
 * Registry maintaining {@link JTextComponent}s in most-recently-used order.
 * <br/>
 * The particular text component needs to register itself first (to avoid dealing
 * with all the JTextFields etc.). Then the registry will attach
 * a focus listener to the text component and once the component gains
 * the focus it will move to the head of the components list.
 * <br/>
 * The registry will also fire a change in case a document property
 * of the focused component changes (by calling component.setDocument()).
 *
 * @author Miloslav Metelka
 */
public final class EditorRegistry {
    
    private EditorRegistry() {
        // No instances
    }
    
    static {
        EditorApiPackageAccessor.register(new PackageAccessor());
    }
    
    // -J-Dorg.netbeans.api.editor.EditorRegistry.level=FINEST
    private static final Logger LOG = Logger.getLogger(EditorRegistry.class.getName());

    /**
     * Fired when focus was delivered to a registered text component.
     * <br/>
     * The focused component will become the first in the components list.
     * <br/>
     * The {@link PropertyEvent#getOldValue()} will be the a component
     * losing the focus {@link FocusEvent#getOppositeComponent()}.
     * The {@link PropertyEvent#getNewValue()} will be the text component gaining the focus.
     */
    public static final String FOCUS_GAINED_PROPERTY = "focusGained";
    
    /**
     * Fired when a registered focused component has lost the focus.
     * <br/>
     * The focused component will remain the first in the components list.
     * <br/>
     * The {@link PropertyEvent#getOldValue()} will be the text component
     * losing the focus and the {@link PropertyEvent#getNewValue()}
     * will be the component gaining the focus {@link FocusEvent#getOppositeComponent()}.
     */
    public static final String FOCUS_LOST_PROPERTY = "focusLost";
    
    /**
     * Fired when document property of the focused component changes
     * i.e. someone has called {@link JTextComponent#setDocument(Document)}.
     * <br/>
     * The {@link PropertyEvent#getOldValue()} will be the original document
     * of the focused text component and the {@link PropertyEvent#getNewValue()}
     * will be the new document set to the focused text component.
     */
    public static final String FOCUSED_DOCUMENT_PROPERTY = "focusedDocument";

    /**
     * Double linked list of weak references to text components.
     */
    private static Item textComponentRefs;
    
    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(EditorRegistry.class);


    /**
     * Return last focused text component (from the ones included in the registry).
     * <br/>
     * It may or may not currently have a focus.
     * 
     * @return last focused text component or null if no text components
     *  were registered yet.
     */
    public static synchronized JTextComponent lastFocusedComponent() {
        return firstValidComponent();
    }
    
    /**
     * Return the last focused component if it currently has a focus
     * or return null if none of the registered components currently have the focus.
     * <br/>
     * @return focused component or null if none of the registered components
     *  is currently focused.
     */
    public static synchronized JTextComponent focusedComponent() {
        JTextComponent c = firstValidComponent();
        return (c != null && c.isFocusOwner()) ? c : null;
    }
    
    /**
     * Get list of all components present in the registry starting with the most active
     * and ending with least active component.
     * <br/>
     * The list is a snapshot of the current state and it may be modified
     * by the caller if desired.
     * 
     * @return non-null list containing all the registered components in MRU order.
     */
    public static synchronized List<? extends JTextComponent> componentList() {
        List<JTextComponent> l;
        JTextComponent c = firstValidComponent();
        if (c != null) {
            l = new ArrayList<JTextComponent>();
            l.add(c);
            // Add remaining ones (eliminate empty items)
            Item item = textComponentRefs.next;
            while (item != null) {
                c = item.get();
                if (c != null) {
                    l.add(c);
                    item = item.next;
                } else
                    item = removeItem(item);
            }

        } else // No valid items
            l = Collections.emptyList();
        return l;
    }
    
    /**
     * Add a property change listener for either of the following properties:
     * <ul>
     *   <li>{@link #FOCUS_GAINED_PROPERTY}</li>
     *   <li>{@link #FOCUS_LOST_PROPERTY}</li>
     *   <li>{@link #FOCUSED_DOCUMENT_PROPERTY}</li>
     * </ul>.
     * <br/>
     * All the firing should occur in AWT thread only
     * (assuming the JTextComponent.setDocument() is done properly in AWT).
     * 
     * @param l non-null listener to add.
     */
    public static void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public static void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    /**
     * Add a given text component to the registry. The registry will weakly
     * reference the given component for its whole lifetime
     * until it will be garbage collected.
     * 
     * @param c non-null text component to be registered.
     */
    static synchronized void register(JTextComponent c) {
        assert (c != null);
        if (c.getClientProperty(Item.class) == null) { // Not registered yet
            Item item = new Item(c);
            c.putClientProperty(Item.class, item);
            c.addFocusListener(FocusL.INSTANCE);
            // Add to end of list
            if (textComponentRefs == null)
                textComponentRefs = item;
            else {
                Item i = textComponentRefs;
                while (i.next != null)
                    i = i.next;
                i.next = item;
                item.previous = i;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "REGISTERED new component as last item:\n" + dumpItemList());
            }

            // If focused (rare since usually registered early when not focused yet)
            if (c.isFocusOwner()) {
                focusGained(c, null); // opposite could eventually be got from Focus Manager
            }
        }
    }
    
    static synchronized void focusGained(JTextComponent c, Component origFocused) {
        Item item = (Item)c.getClientProperty(Item.class);
        assert (item != null) : "Not registered!"; // NOI18N
        assert (item.next != null || item.previous != null || textComponentRefs == item)
                : "Already released!"; // NOI18N
        moveToHead(item);
        c.addPropertyChangeListener(PropertyDocL.INSTANCE);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, FOCUS_GAINED_PROPERTY + ": " + dumpComponent(c) + '\n');
        }
        firePropertyChange(FOCUS_GAINED_PROPERTY, origFocused, c);
    }
    
    static void focusLost(JTextComponent c, Component newFocused) {
        c.removePropertyChangeListener(PropertyDocL.INSTANCE);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, FOCUS_LOST_PROPERTY + ": " + dumpComponent(c) + '\n');
        }
        firePropertyChange(FOCUS_LOST_PROPERTY, c, newFocused);
    }
    
    static void focusedDocumentChange(JTextComponent c, Document oldDoc, Document newDoc) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, FOCUSED_DOCUMENT_PROPERTY + ": " + dumpComponent(c)
                    + "\n    OLDDoc=" + oldDoc + "\n    NEWDoc=" + newDoc + '\n');
        }
        firePropertyChange(FOCUSED_DOCUMENT_PROPERTY, oldDoc, newDoc);
    }
    
    private static JTextComponent firstValidComponent() {
        JTextComponent c = null;
        while (textComponentRefs != null && (c = textComponentRefs.get()) == null) {
            removeItem(textComponentRefs);
        }
        return c;
    }
    
    /**
     * Remove given entry and return a next one.
     */
    private static Item removeItem(Item item) {
        Item next = item.next;
        if (item.previous == null) { // Head
            assert (textComponentRefs == item);
            textComponentRefs = next;
        } else { // Not head
            item.previous.next = next;
        }
        if (next != null)
            next.previous = item.previous;
        item.next = item.previous = null;
        return next;
    }
    
    private static void moveToHead(Item item) {
        if (LOG.isLoggable(Level.FINEST)) { // Debugging
            isItemInList(item);
        }
        removeItem(item);
        item.next = textComponentRefs;
        if (textComponentRefs != null)
            textComponentRefs.previous = item;
        textComponentRefs = item;
        if (LOG.isLoggable(Level.FINEST)) { // Debugging
            isItemInList(item);
            checkItemListConsistency();
        }
    }
    
    private static void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    private static boolean isItemInList(Item item) {
        Item i = textComponentRefs;
        while (i != null) {
            if (i == item)
                return true;
            i = i.next;
        }
        return false;
    }
    
    private static void checkItemListConsistency() {
        Item item = textComponentRefs;
        Item previous = null;
        while (item != null) {
            assert item.previous == previous;
            previous = item;
            item = item.next;
        }
        if (previous != null)
            assert previous.next == null;
    }

    private static String dumpItemList() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        Item item = textComponentRefs;
        while (item != null) {
            ArrayUtilities.appendBracketedIndex(sb, i, 1);
            sb.append(dumpComponent(item.get()));
            sb.append('\n');
            item = item.next;
            i++;
        }
        sb.append('\n'); // One extra delimiting newline
        return sb.toString();
    }
    
    private static String dumpComponent(JTextComponent c) {
        return "c[IHC=" + System.identityHashCode(c)
                + "]=" + c;
    }
    
    /**
     * Item of a single linked list of text component references.
     */
    private static final class Item extends WeakReference<JTextComponent> {
        
        Item(JTextComponent c) {
            super(c);
        }
        
        Item next;
        
        Item previous;

    }
    
    private static final class FocusL implements FocusListener {
        
        static final FocusL INSTANCE = new FocusL();

        public void focusGained(FocusEvent e) {
            EditorRegistry.focusGained((JTextComponent)e.getSource(), e.getOppositeComponent());
            
        }

        public void focusLost(FocusEvent e) {
            EditorRegistry.focusLost((JTextComponent)e.getSource(), e.getOppositeComponent());
        }
        
    }
    
    private static final class PropertyDocL implements PropertyChangeListener {
        
        static final PropertyDocL INSTANCE = new PropertyDocL();

        public void propertyChange(PropertyChangeEvent evt) {
            if ("document".equals(evt.getPropertyName())) {
                focusedDocumentChange((JTextComponent)evt.getSource(), (Document)evt.getOldValue(), (Document)evt.getNewValue());
            }
        }

    }

    private static final class PackageAccessor extends EditorApiPackageAccessor {

        public void register(JTextComponent c) {
            EditorRegistry.register(c);
        }
        
    }
}
