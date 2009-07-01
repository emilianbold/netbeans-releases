/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.api.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
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
     * The {@link java.beans.PropertyChangeEvent#getOldValue()} will be a component
     * losing the focus {@link FocusEvent#getOppositeComponent()}.
     * The {@link java.beans.PropertyChangeEvent#getNewValue()} will be the text component gaining the focus.
     */
    public static final String FOCUS_GAINED_PROPERTY = "focusGained"; //NOI18N
    
    /**
     * Fired when a registered focused component has lost the focus.
     * <br/>
     * The focused component will remain the first in the components list.
     * <br/>
     * The {@link java.beans.PropertyChangeEvent#getOldValue()} will be the text component
     * losing the focus and the {@link java.beans.PropertyChangeEvent#getNewValue()}
     * will be the component gaining the focus {@link FocusEvent#getOppositeComponent()}.
     */
    public static final String FOCUS_LOST_PROPERTY = "focusLost"; //NOI18N
    
    /**
     * Fired when document property of the focused component changes
     * i.e. someone has called {@link JTextComponent#setDocument(Document)}.
     * <br/>
     * The {@link java.beans.PropertyChangeEvent#getOldValue()} will be the original document
     * of the focused text component and the {@link java.beans.PropertyChangeEvent#getNewValue()}
     * will be the new document set to the focused text component.
     */
    public static final String FOCUSED_DOCUMENT_PROPERTY = "focusedDocument"; //NOI18N

    /**
     * Fired when a component (returned previously from {@link #componentList()})
     * is removed from component hierarchy (so it's likely that the component will be released completely
     * and garbage-collected).
     * <br/>
     * Such component will no longer be returned from {@link #componentList()}
     * or {@link #lastFocusedComponent()}.
     * <br/>
     * The {@link java.beans.PropertyChangeEvent#getOldValue()} will be the removed
     * component.
     * <br/>
     * The {@link java.beans.PropertyChangeEvent#getNewValue()} returns <code>null</code>
     */
    public static final String COMPONENT_REMOVED_PROPERTY = "componentRemoved"; //NOI18N

    /**
     * Fired when the last focused component (returned previously from {@link #lastFocusedComponent()})
     * was removed from component hierarchy (so it's likely that the component will be released completely
     * and garbage-collected).
     * <br/>
     * Such component will no longer be returned from {@link #componentList()}
     * or {@link #lastFocusedComponent()}.
     * <br/>
     * The {@link java.beans.PropertyChangeEvent#getOldValue()} will be the removed
     * last focused component and the {@link java.beans.PropertyChangeEvent#getNewValue()}
     * will be the component that would currently be returned from {@link #lastFocusedComponent()}.
     * <br/>
     * If {@link java.beans.PropertyChangeEvent#getNewValue()} returns <code>null</code>
     * then there are no longer any registered components
     * ({@link #componentList()} would return empty list). If the client
     * holds per-last-focused-component data it should clear them.
     */
    public static final String LAST_FOCUSED_REMOVED_PROPERTY = "lastFocusedRemoved"; //NOI18N

    /**
     * Double linked list of weak references to text components.
     */
    private static Item items;
    
    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(EditorRegistry.class);
    
    private static Class ignoredAncestorClass;


    /**
     * Return last focused text component (from the ones included in the registry).
     * <br/>
     * It may or may not currently have a focus.
     * 
     * @return last focused text component or null if no text components
     *  were registered yet or all the registered components were closed.
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
            Item item = items.next;
            while (item != null) {
                c = item.get();
                if (c != null) {
                    l.add(c);
                    item = item.next;
                } else
                    item = removeFromItemList(item);
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
    static void register(JTextComponent c) {
        ArrayList<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        synchronized (EditorRegistry.class) {
            assert (c != null);
            if (item(c) == null) { // Not registered yet
                Item item = new Item(c);
                c.putClientProperty(Item.class, item);
                c.addFocusListener(FocusL.INSTANCE);
                c.addAncestorListener(AncestorL.INSTANCE);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "EditorRegistry.register(): " + dumpComponent(c) + '\n'); //NOI18N
                }
                // By default do not add the component to be last in the item list
                // at this point since e.g. the component from warmup task(s) would show up
                // in the item list and they would never be removed
                // since they have no ancestor and they do not become focused ever.
                if (c.isFocusOwner()) { // If the focus owner then simulate the focus was gained
                    _focusGained(c, null, events); // opposite could eventually be got from Focus Manager
                } else if (c.isDisplayable()) { // Simulate that addNotify() was called
                    itemMadeDisplayable(item);
                }
            }
        }
        fireEvents(events);
    }
    
    static synchronized void setIgnoredAncestorClass(Class ignoredAncestorClass) {
        EditorRegistry.ignoredAncestorClass = ignoredAncestorClass;
    }

    static void notifyClose(JComponent c) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "EditorRegistry.notifyClose(): " + dumpComponent(c) + '\n'); //NOI18N
        }
        ArrayList<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        synchronized (EditorRegistry.class) {
            // Go through the present items and remove those that have the "c" as parent.
            Item item = items;
            while (item != null) {
                JTextComponent textComponent = item.get();
                if (textComponent == null || (item.ignoreAncestorChange && c.isAncestorOf(textComponent))) {
                    // Explicitly call focusLost() before physical removal from the registry.
                    // In practice this notification happens first before focusLost() from focus listener.
                    if (textComponent != null) {
                        _focusLost(textComponent, null, events); // Checks if the component is focused and does nothing otherwise
                        item = removeFromRegistry(item, events);
                    } else { // Null text component - just remove the item
                        item = removeFromItemList(item);
                    }
                } else {
                    item = item.next;
                }
            }
        }
        fireEvents(events);
    }

    static void focusGained(JTextComponent c, Component origFocused) {
        ArrayList<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        EditorRegistry._focusGained(c, origFocused, events);
        fireEvents(events);
    }

    private synchronized static void _focusGained(JTextComponent c, Component origFocused, List<PropertyChangeEvent> events) {
        Item item = item(c);
        assert (item != null) : "Not registered!"; // NOI18N

        // Move the item to head of the list
        removeFromItemList(item);
        addToItemListAsFirst(item);
        item.focused = true;

        c.addPropertyChangeListener(PropertyDocL.INSTANCE);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, FOCUS_GAINED_PROPERTY + ": " + dumpComponent(c) + '\n'); //NOI18N
            logItemListFinest();
        }
        events.add(new PropertyChangeEvent(EditorRegistry.class, FOCUS_GAINED_PROPERTY, origFocused, c));
    }
    
    static void focusLost(JTextComponent c, Component newFocused) {
        ArrayList<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        EditorRegistry._focusLost(c, newFocused, events);
        fireEvents(events);
    }

    private static synchronized void _focusLost(JTextComponent c, Component newFocused, List<PropertyChangeEvent> events) {
        Item item = item(c);
        assert (item != null) : "Not registered!"; // NOI18N
        // For explicit close notifications: in practice the closing comes first before focus lost.
        if (item.focused) {
            item.focused = false;
            if (!item.ignoreAncestorChange && firstValidComponent() != c) {
                throw new IllegalStateException("Invalid ordering of focusLost()"); //NOI18N
            }
            c.removePropertyChangeListener(PropertyDocL.INSTANCE);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, FOCUS_LOST_PROPERTY + ": " + dumpComponent(c) + '\n'); //NOI18N
                logItemListFinest();
            }
            events.add(new PropertyChangeEvent(EditorRegistry.class, FOCUS_LOST_PROPERTY, c, newFocused));
        }
    }
    
    static void itemMadeDisplayable(Item item) {
        // If the component was removed from the component hierarchy and then
        // returned back to the hierarchy it will be readded to the end of the component list.
        // If the item is not removed yet then the addAsLast() will do nothing.
        addToItemListAsLast(item);
        JTextComponent c = item.get();
        if (c == null)
            throw new IllegalStateException("Component should be non-null"); //NOI18N
        
        // Remember whether component should not be removed from registry upon removeNotify()
        item.ignoreAncestorChange = (SwingUtilities.getAncestorOfClass(ignoredAncestorClass, c) != null);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.fine("ancestorAdded: " + dumpComponent(item.get()) + '\n'); //NOI18N
            logItemListFinest();
        }
    }
    
    static void focusedDocumentChange(JTextComponent c, Document oldDoc, Document newDoc) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, FOCUSED_DOCUMENT_PROPERTY + ": " + dumpComponent(c) //NOI18N
                    + "\n    OLDDoc=" + oldDoc + "\n    NEWDoc=" + newDoc + '\n'); //NOI18N
        }
        pcs.firePropertyChange(FOCUSED_DOCUMENT_PROPERTY, oldDoc, newDoc);
    }
    
    private static JTextComponent firstValidComponent() {
        JTextComponent c = null;
        while (items != null && (c = items.get()) == null) {
            removeFromItemList(items);
        }
        return c;
    }
    
    static Item item(JComponent c) {
        return (Item)c.getClientProperty(Item.class);

    }
    
    private static void addToItemListAsLast(Item item) {
        if (item.linked)
            return;
        item.linked = true;
        if (items == null) {
            items = item;
        } else {
            Item i = items;
            while (i.next != null)
                i = i.next;
            i.next = item;
            item.previous = i;
        }
        // Assuming item.next == null (done in removeItem() too).
        if (LOG.isLoggable(Level.FINEST)) { // Consistency checking
            checkItemListConsistency();
        }
    }

    private static void addToItemListAsFirst(Item item) {
        if (item.linked)
            return;
        item.linked = true;
        item.next = items;
        if (items != null)
            items.previous = item;
        items = item;
        if (LOG.isLoggable(Level.FINEST)) { // Consistency checking
            checkItemListConsistency();
        }
    }

    /**
     * Remove given entry and return a next one.
     */
    private static Item removeFromItemList(Item item) {
        if (!item.linked)
            return null;
        item.linked = false;
        Item next = item.next;
        if (item.previous == null) { // Head
            assert (items == item);
            items = next;
        } else { // Not head
            item.previous.next = next;
        }
        if (next != null)
            next.previous = item.previous;
        item.next = item.previous = null;
        if (LOG.isLoggable(Level.FINEST)) { // Consistency checking
            checkItemListConsistency();
        }
        return next;
    }
    
    /**
     * Remove the given item from registry and return the next one.
     * 
     * @param item item to remove.
     * @return next item in registry.
     */
    static Item removeFromRegistry(Item item, List<PropertyChangeEvent> events) {
        boolean lastFocused = (items == item);
        // Remove component from item chain
        JTextComponent component = item.get();
        item = removeFromItemList(item);
        if (component != null) {
            // unregister the listeneres
            component.putClientProperty(Item.class, null);
            component.removeFocusListener(FocusL.INSTANCE);
            component.removeAncestorListener(AncestorL.INSTANCE);

            events.add(new PropertyChangeEvent(EditorRegistry.class, COMPONENT_REMOVED_PROPERTY, component, null));
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.fine("Component removed: " + dumpComponent(component) + '\n'); //NOI18N
                logItemListFinest();
            }
            if (lastFocused) {
                events.add(new PropertyChangeEvent(EditorRegistry.class, LAST_FOCUSED_REMOVED_PROPERTY, component, lastFocusedComponent()));
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Fired LAST_FOCUSED_REMOVED_PROPERTY for " + dumpComponent(component) + '\n'); //NOI18N
                }
            }
        }
        return item;
    }

    static void checkItemListConsistency() {
        Item item = items;
        Item previous = null;
        while (item != null) {
            if (!item.linked)
                throw new IllegalStateException("item=" + item + " is in list but item.linked is false."); //NOI18N
            if (item.previous != previous)
                throw new IllegalStateException("Invalid previous of item=" + item); //NOI18N
            if (item.ignoreAncestorChange && (item.runningTimer != null))
                throw new IllegalStateException("item=" + item + " has running timer."); //NOI18N
            if (item.focused && item != items)
                throw new IllegalStateException("Non-first component has focused flag."); //NOI18N

            previous = item;
            item = item.next;
        }
    }

    static void fireEvents(List<PropertyChangeEvent> events) {
        for(PropertyChangeEvent e : events) {
            pcs.firePropertyChange(e);
        }
    }
    
    static void logItemListFinest() {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest(dumpItemList());
        }
    }
    
    private static String dumpItemList() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("---------- EditorRegistry Dump START ----------\n"); //NOI18N
        int i = 0;
        Item item = items;
        while (item != null) {
            ArrayUtilities.appendBracketedIndex(sb, i, 1);
            sb.append(' '); //NOI18N
            if (item.focused)
                sb.append("Focused, "); //NOI18N
            if (item.ignoreAncestorChange)
                sb.append("IgnoreAncestorChange, "); //NOI18N
            sb.append(dumpComponent(item.get()));
            sb.append('\n'); //NOI18N
            item = item.next;
            i++;
        }
        sb.append("---------- EditorRegistry Dump END ----------\n"); //NOI18N
        return sb.toString();
    }
    
    static String dumpComponent(JComponent c) {
        Object streamDesc = null;
        if (c instanceof JTextComponent) {
            Document doc = ((JTextComponent)c).getDocument();
            if (doc != null) {
                streamDesc = doc.getProperty(Document.StreamDescriptionProperty);
            }
        }
        return "component[IHC=" + System.identityHashCode(c) //NOI18N
                + "]:" + ((streamDesc != null) ? streamDesc : c); //NOI18N
    }
    
    /**
     * Item of a single linked list of text component references.
     */
    private static final class Item extends WeakReference<JTextComponent> {
        
        Item(JTextComponent c) {
            super(c);
        }
        
        /**
         * Whether the item is contained in the item list - used for quicker checking
         * than checking next/last vars (and possibly items var).
         */
        boolean linked;
        
        /**
         * Whether this item is currently treated as focused.
         */
        boolean focused;
        
        /**
         * Next item in items double linked list.
         */
        Item next;
        
        /**
         * Previous item in items double linked list.
         */
        Item previous;
        
        /**
         * Whether component should not be removed from registry upon removeNotify()
         * since TabbedAdapter in NB winsys removes the component upon tab switching.
         */
        boolean ignoreAncestorChange;
        
        /**
         * Timer for removal of component from registry after removeNotify() was called on component.
         */
        Timer runningTimer;

        @Override
        public String toString() {
            return "component=" + get() + ", linked=" + linked + //NOI18N
                    ", hasPrevious=" + (previous != null) + ", hasNext=" + (next != null) + //NOI18N
                    ", ignoreAncestorChange=" + ignoreAncestorChange + //NOI18N
                    ", hasTimer=" + (runningTimer != null); //NOI18N
        }

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
            if ("document".equals(evt.getPropertyName())) { //NOI18N
                focusedDocumentChange((JTextComponent)evt.getSource(),
                        (Document)evt.getOldValue(), (Document)evt.getNewValue());
            }
        }

    }
    
    private static final class AncestorL implements AncestorListener {
        
        static final AncestorL INSTANCE = new AncestorL();
        
        private static final int BEFORE_REMOVE_DELAY = 2000; // 2000ms delay

        public void ancestorAdded(AncestorEvent event) {
            Item item = item(event.getComponent());
            if (item.runningTimer != null) {
                item.runningTimer.stop();
                item.runningTimer = null;
            }
            itemMadeDisplayable(item);
        }

        public void ancestorMoved(AncestorEvent event) {
        }

        public void ancestorRemoved(AncestorEvent event) {
            final JComponent component = event.getComponent();
            Item item = item(component);
            // In case the ancestor has class of certain type
            // the ancestor removal is not significant and the registry expects
            // that the closing of the component will be notified explicitly.
            if (LOG.isLoggable(Level.FINER)) {
                LOG.fine("ancestorRemoved for " + dumpComponent(component) + //NOI18N
                        "; ignoreAncestorChange=" + item.ignoreAncestorChange + '\n'); //NOI18N
            }
            if (!item.ignoreAncestorChange) {
                // Only start timer when ancestor changes are not ignored.
                item.runningTimer = new Timer(BEFORE_REMOVE_DELAY,
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            ArrayList<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
                            synchronized (EditorRegistry.class) {
                                Item item = item(component);
                                item.runningTimer.stop();
                                item.runningTimer = null;
                                removeFromRegistry(item, events);
                            }
                            fireEvents(events);
                        }
                    }
                );
                item.runningTimer.start();
            }
        }
        
    }

    private static final class PackageAccessor extends EditorApiPackageAccessor {

        @Override
        public void register(JTextComponent c) {
            EditorRegistry.register(c);
        }

        @Override
        public void setIgnoredAncestorClass(Class ignoredAncestorClass) {
            EditorRegistry.setIgnoredAncestorClass(ignoredAncestorClass);
        }

        @Override
        public void notifyClose(JComponent c) {
            EditorRegistry.notifyClose(c);
        }
        
    }
}
