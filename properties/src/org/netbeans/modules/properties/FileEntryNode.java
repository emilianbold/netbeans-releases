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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.openide.actions.InstantiateAction;
import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;


/** 
 * Standard node representing a <code>PresentableFileEntry</code>.
 * @author Petr Jiricka
 */
public class FileEntryNode extends AbstractNode {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7882925922830244768L;

    /** default base for icons for data objects */
    private static final String ICON_BASE = "/org/netbeans/core/resources/x"; // NOI18N

    /** Helper field. ResourceBundle for i18n-ing strings in this source. */
    private static ResourceBundle bundle;

    /** FileEntry of this node. */
    private PresentableFileEntry entry;


    /** Create a data node for a given file entry.
     * The provided children object will be used to hold all child nodes.
     * @param entry entry to work with
     * @param ch children container for the node
     */
    public FileEntryNode (PresentableFileEntry entry, Children ch) {
        super (ch);
        this.entry = entry;
        PropL propListener = new PropL ();
        PropertyChangeListener wl = new WeakListener.PropertyChange(propListener);
        entry.addPropertyChangeListener (wl);
        entry.getDataObject().addPropertyChangeListener (propListener);
        super.setName (entry.getName ());

        setIconBase (ICON_BASE);
    }


    /** Gets the represented entry.
     * @return the entry
     */
    public PresentableFileEntry getFileEntry() {
        return entry;
    }

    /** Indicate whether the node may be destroyed.
     * @return tests {@link DataObject#isDeleteAllowed}
     */
    public boolean canDestroy () {
        return entry.isDeleteAllowed ();
    }

    /** Destroyes the node. */
    public void destroy () throws IOException {
        entry.delete ();
        super.destroy ();
    }

    /** 
     * @returns true if this node allows copying.
     */
    public final boolean canCopy () {
        return entry.isCopyAllowed ();
    }

    /**
     * @returns true if this node allows cutting.
     */
    public final boolean canCut () {
        return entry.isMoveAllowed ();
    }

    /** Rename the data object.
     * @param name new name for the object
     * @exception IllegalArgumentException if the rename failed
     */
    public void setName (String name) {
        try {
            entry.renameEntry (name);
            super.setName (name);
        } catch (IOException ex) {
            throw new IllegalArgumentException (ex.getMessage ());
        }
    }

    /** Gets default action.
     * A file entry node may have a {@link InstantiateAction default action} if it represents a template.
     * @return an instantiation action if the underlying entry is a template. Otherwise the abstract node's default action is returned, possibly <code>null</code>.
     */
    public SystemAction getDefaultAction () {
        if (entry.isTemplate ()) {
            // PENDING - EntryInstantiateAction
            return SystemAction.get (InstantiateAction.class);
        } else {
            return super.getDefaultAction ();
        }
    }

    /** Get a cookie.
     * First of all {@link PresentableFileEntry#getCookie} is
     * called. If it produces non-<code>null</code> result, that is returned.
     * Otherwise the superclass is tried.
     * @return the cookie or <code>null</code>
     */
    public Node.Cookie getCookie (Class cl) {
        Node.Cookie c = entry.getCookie (cl);
        if (c != null) {
            return c;
        } else {
            return super.getCookie (cl);
        }
    }

    /** Initializes sheet of properties. Allows subclasses to
     * overwrite it.
     * @return the default sheet to use
     */
    protected Sheet createSheet () {
        Sheet s = Sheet.createDefault ();
        Sheet.Set ss = s.get (Sheet.PROPERTIES);

        Node.Property p;

        p = new PropertySupport.ReadWrite (
                PROP_NAME,
                String.class,
                FileEntryNode.getBundle().getString("PROP_name"),
                FileEntryNode.getBundle().getString("HINT_name")
            ) {
                public Object getValue () {
                    return entry.getName();
                }

                public void setValue (Object val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    if (!canWrite())
                        throw new IllegalAccessException();
                    if (!(val instanceof String))
                        throw new IllegalArgumentException();

                    FileEntryNode.this.setName ((String)val);
                }

                public boolean canWrite () {
                    return entry.isRenameAllowed();
                }
            };
        p.setName (DataObject.PROP_NAME);
        ss.put (p);

        try {
            p = new PropertySupport.Reflection (
                    entry, Boolean.TYPE, "isTemplate", "setTemplate" // NOI18N
                );
            p.setName (DataObject.PROP_TEMPLATE);
            p.setDisplayName (FileEntryNode.getBundle().getString("PROP_template"));
        p.setShortDescription (FileEntryNode.getBundle().getString("HINT_template"));
            ss.put (p);
        } catch (Exception ex) {
            throw new InternalError ();
        }
        return s;
    }


    /** Support for firing property change.
     * @param ev event describing the change
     */
    void fireChange (PropertyChangeEvent ev) {
        firePropertyChange (ev.getPropertyName (), ev.getOldValue (), ev.getNewValue ());
        if (ev.getPropertyName().equals(DataObject.PROP_NAME)) {
            super.setName (entry.getName ());
            return;
        }
        if (ev.getPropertyName().equals(Node.PROP_COOKIE)) {
            fireCookieChange();
        }
    }

    /** Property listener on data object that delegates all changes of
     * properties to this node.
     */
    private class PropL extends Object implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent ev) {
            fireChange (ev);
        }
    }
    
    /** Helper method for lazy initialization of <code>bundle</code> field. */
    private static ResourceBundle getBundle() {
        if(bundle == null) {
            bundle = NbBundle.getBundle(PropertiesModule.class);
        }
        
        return bundle;
    }
}