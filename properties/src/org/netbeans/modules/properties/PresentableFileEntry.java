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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.AbstractCollection;
import java.util.ResourceBundle;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.loaders.*;
import org.openide.*;
import org.openide.util.datatransfer.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.*;
import org.openide.util.enum.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.nodes.*;

/** 
 * Object that represents one FileEntry and has support for presentation of this entry as a node.
 * @author Jaroslav Tulach, Petr Jiricka
 */
public abstract class PresentableFileEntry extends FileEntry implements Node.Cookie {
    
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3328227388376142699L;
    
    /** The node delegate for this data object. */
    private transient Node nodeDelegate;
    
    /** Modified flag */
    private boolean modif = false;
    
    /** property change listener support */
    private transient PropertyChangeSupport changeSupport;
    
    /** listener for changes in the cookie set */
    private ChangeListener cookieL = new ChangeListener () {
        public void stateChanged (ChangeEvent ev) {
            firePropertyChange (Node.PROP_COOKIE, null, null);
        }
    };
    
    /** array of cookies for this entry */
    private transient CookieSet cookieSet;

    
    /** Creates new presentable file entry initially attached to a given file object.
     * @param obj the data object this entry belongs to
     * @param fo the file object for the entry
     */
    public PresentableFileEntry(MultiDataObject obj, FileObject fo) {
        super (obj, fo);
    }
    

    /** Creates a node delegate for this entry. */
    protected abstract Node createNodeDelegate();
    
    /** Get the node delegate. Either {@link #createNodeDelegate creates it} (if it does not
     * already exist) or
     * returns a previously created instance of it.
     *
     * @return the node delegate (without parent) for this data object
     */
    public final Node getNodeDelegate () {
        if (nodeDelegate == null) {
            // Changed like in DataObject.
            // JST:
            // changed to require write access because a lot of subclasses
            // in createNodeDelegate requires it neither, so this should
            // prevent deadlocks (because it uses only one lock and not two).
            Children.MUTEX.writeAccess(new Runnable() {
                public void run () {
                    if(nodeDelegate == null) {
                        nodeDelegate = createNodeDelegate();
                    }
                }
            });
        }
        return nodeDelegate;
    }
    
    /** Package private method to assign template attribute to a file.
     * Used also from FileEntry.
     *
     * @param fo the file
     * @param newTempl is template or not
     * @return true if the value change/false otherwise
     */
    private static boolean setTemplate (FileObject fo, boolean newTempl) throws IOException {
        boolean oldTempl = false;
        
        Object o = fo.getAttribute(DataObject.PROP_TEMPLATE);
        if ((o instanceof Boolean) && ((Boolean)o).booleanValue())
            oldTempl = true;
        if (oldTempl == newTempl)
            return false;
        
        fo.setAttribute(DataObject.PROP_TEMPLATE, (newTempl ? new Boolean(true) : null));
        
        return true;
    }
    
    /** Set the template status of this file object.
     * @param newTempl <code>true</code> if the object should be a template
     * @exception IOException if setting the template state fails
     */
    public final void setTemplate (boolean newTempl) throws IOException {
        if (!setTemplate (getFile(), newTempl)) {
            // no change in state
            return;
        }
        
        firePropertyChange(DataObject.PROP_TEMPLATE, new Boolean(!newTempl), new Boolean(newTempl));
    }
    
    /** Get the template status of this data object.
     * @return <code>true</code> if it is a template
     */
    public boolean isTemplate () {
        Object o = getFile().getAttribute(DataObject.PROP_TEMPLATE);
        boolean ret = false;
        if (o instanceof Boolean)
            ret = ((Boolean) o).booleanValue();
        return ret;
    }
    
    /** Renames underlying fileobject. This implementation return the
     * same file. Fires property change. Called when the DO is renamed, not the entry
     *
     * @param name new name
     * @return file object with renamed file
     */
    public FileObject rename (String name) throws IOException {
        String oldName = getName();
        FileObject fo = super.rename(name);
        firePropertyChange(DataObject.PROP_NAME, oldName, name);
        return fo;
    }
    
    /** Renames underlying fileobject. This implementation return the
     * same file. Fires property change. Called when the file entry is renamed, not the DO
     *
     * @param name new name
     * @return file object with renamed file
     */
    public FileObject renameEntry (String name) throws IOException {
        return rename(name);
    }
    
    /** Deletes file object and fires property change. */
    public void delete () throws IOException {
        super.delete();
        
        firePropertyChange(DataObject.PROP_VALID, Boolean.TRUE, Boolean.FALSE);
    }
    
    
    /** Test whether the object may be deleted.
     * @return <code>true</code> if it may
     */
    public abstract boolean isDeleteAllowed ();
    
    /** Test whether the object may be copied.
     * @return <code>true</code> if it may
     */
    public abstract boolean isCopyAllowed ();
    
    /** Test whether the object may be moved.
     * @return <code>true</code> if it may
     */
    public abstract boolean isMoveAllowed ();
    
    /** Test whether the object may create shadows.
     * <p>The default implementation returns <code>true</code>.
     * @return <code>true</code> if it may
     */
    public boolean isShadowAllowed () {
        return true;
    }
    
    /** Test whether the object may be renamed.
     * @return <code>true</code> if it may
     */
    public abstract boolean isRenameAllowed ();
    
    
    /** Test whether the object is modified.
     * @return <code>true</code> if it is modified
     */
    public boolean isModified() {
        return modif;
    }
    
    /** Set whether the object is considered modified.
     * Also fires a change event.
     * If the new value is <code>true</code>, the data object is added into a {@link #getRegistry registry} of opened data objects.
     * If the new value is <code>false</code>,
     * the data object is removed from the registry.
     */
    public void setModified(boolean modif) {
        if (this.modif != modif) {
            this.modif = modif;
            firePropertyChange(DataObject.PROP_MODIFIED, new Boolean(!modif), new Boolean(modif));
        }
    }
    
    /** Get help context for this object.
     * @return the help context
     */
    public abstract HelpCtx getHelpCtx ();
    
    /** Get the name of the data object.
     * <p>The default implementation uses the name of the primary file.
     * @return the name
     */
    public String getName () {
        return getFile ().getName ();
    }
    
    /** Get the folder this data object is stored in.
     * @return the folder; <CODE>null</CODE> if the primary file
     *   is the {@link FileObject#isRoot root} of its filesystem
     */
    public final DataFolder getFolder () {
        FileObject fo = getFile ().getParent ();
        // could throw IllegalArgumentException but only if fo is not folder
        // => then there is a bug in filesystem implementation
        return fo == null ? null : DataFolder.findFolder (fo);
    }
    
    
    //
    // Property change support
    //
    
    /** @param l the listener
     */
    public synchronized void addPropertyChangeListener (PropertyChangeListener l) {
        getChangeSupport ().addPropertyChangeListener (l);
    }
    
    /** @param l the listener
     */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        getChangeSupport ().removePropertyChangeListener (l);
    }
    
    /** Fires property change notification to all listeners registered via
     * {@link #addPropertyChangeListener}.
     *
     * @param name of property
     * @param oldValue old value
     * @param newValue new value
     */
    protected final void firePropertyChange (String name, Object oldValue, Object newValue) {
        getChangeSupport ().firePropertyChange (name, oldValue, newValue);
    }
    
    /** Getter for standard property change support. This is used in
     * this class and by this method provided to subclasses.
     *
     * @return support
     */
    private synchronized final PropertyChangeSupport getChangeSupport () {
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport (this);
        }
        return changeSupport;
    }
    
    /** Set the set of cookies.
     * To the provided cookie set a listener is attached,
     * and any change to the set is propagated by
     * firing a change on {@link #PROP_COOKIE}.
     *
     * @param s the cookie set to use
     * @deprecated
     */
    protected final synchronized void setCookieSet (CookieSet s) {
        if (cookieSet != null) {
            cookieSet.removeChangeListener (cookieL);
        }
        
        s.addChangeListener (cookieL);
        cookieSet = s;
        
        firePropertyChange (Node.PROP_COOKIE, null, null);
    }
    
    /** Get the set of cookies.
     * If the set had been
     * previously set by {@link #setCookieSet}, that set
     * is returned. Otherwise an empty set is
     * returned.
     *
     * @return the cookie set (never <code>null</code>)
     */
    protected final CookieSet getCookieSet () {
        CookieSet s = cookieSet;
        if (s != null) return s;
        synchronized (this) {
            if (cookieSet != null) return cookieSet;
            
            // sets empty sheet and adds a listener to it
            setCookieSet (new CookieSet ());
            return cookieSet;
        }
    }
    
    /** Looks for a cookie in the current cookie set matching the requested class.
     * @param type the class to look for
     * @return an instance of that class, or <code>null</code> if this class of cookie
     *    is not supported
     */
    public Node.Cookie getCookie (Class type) {
        CookieSet c = cookieSet;
        if (c != null) {
            Node.Cookie cookie = c.getCookie (type);
            if (cookie != null) return cookie;
        }
        
        if (type.isInstance (this)) {
            return this;
        }
        return null;
    }
}
