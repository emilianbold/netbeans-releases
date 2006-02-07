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

package org.netbeans.modules.beans;

import java.awt.datatransfer.Transferable;
import java.beans.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.Format;

import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.ErrorManager;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.java.ui.nodes.SourceNodes;

import javax.jmi.reflect.JmiException;

/** Superclass of nodes representing bean patterns.
*
* @author Petr Hrebejk
*/


public abstract class PatternNode extends AbstractNode implements IconBases, PatternProperties, PropertyChangeListener {

    /** Default return value of getIconAffectingProperties method. */
    private static final String[] ICON_AFFECTING_PROPERTIES = new String[] {
                PROP_MODE
            };

    /** Array of the actions of the java methods, constructors and fields. */
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
                /*
                SystemAction.get(OpenAction.class),
                null,
                SystemAction.get(CutAction.class),
                SystemAction.get(CopyAction.class),
                null,
                */
                SystemAction.get(DeleteAction.class),
                SystemAction.get(RenameAction.class),
                null,
                //SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class),
            };


    /** Associated pattern. */
    protected Pattern pattern;

    /** Is this node read-only or are modifications permitted? */
    protected boolean writeable;

    /** Create a new pattern node.
    *
    * @param pattern pattern to represent
    * @param children child nodes
    * @param writeable <code>true</code> if this node should allow modifications.
    *        These include writable properties, clipboard operations, deletions, etc.
    */
    public PatternNode(Pattern pattern, Children children, boolean writeable) {
        super(children);
        this.pattern = pattern;
        this.writeable = writeable;
        
        setIconBaseWithExtension(resolveIconBase()+".gif");
        setActions(DEFAULT_ACTIONS);

        //this.pattern.addPropertyChangeListener(new WeakListeners.PropertyChange (this));
        this.pattern.addPropertyChangeListener( WeakListeners.propertyChange (this, this.pattern));
        displayFormat = null;
    }

    /* Gets the short description of this node.
    * @return A localized short description associated with this node.
    */
    public String getShortDescription() {
        return super.getShortDescription(); // If not ovewloaded in ancestors
    }

    /** Get the currently appropriate icon base.
    * Subclasses should make this sensitive to the state of the element--for example,
    * a private variable may have a different icon than a public one.
    * The icon will be automatically changed whenever a
    * {@link #getIconAffectingProperties relevant} change is made to the element.
    * @return icon base
    * @see AbstractNode#setIconBase
    */
    abstract protected String resolveIconBase();

    public javax.swing.Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    /** Get the names of all element properties which might affect the choice of icon.
    * The default implementation just returns {@link PatternProperties#PROP_MODE}.
    * @return the property names
    */
    protected String[] getIconAffectingProperties() {
        return ICON_AFFECTING_PROPERTIES;
    }

    /** Test whether this node can be renamed.
    * The default implementation assumes it can if this node is {@link #writeable}.
    *
    * @return <code>true</code> if this node can be renamed
    */
    public boolean canRename() {
        return writeable;
    }

    /** Test whether this node can be deleted.
    * The default implementation assumes it can if this node is {@link #writeable}.
    *
    * @return <code>true</code> if this node can be renamed
    */
    public boolean canDestroy () {
        return writeable;
    }

    /* Copy this node to the clipboard.
    *
    * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
    * @throws IOException if it could not copy
    */
    public Transferable clipboardCopy () throws IOException {
        //PENDING
        return super.clipboardCopy();
    }

    /* Cut this node to the clipboard.
    *
    * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
    * @throws IOException if it could not cut
    */
    public Transferable clipboardCut () throws IOException {
        if (!writeable)
            throw new IOException();

        //PENDING
        return super.clipboardCopy();
    }

    /** Test whether this node can be copied.
    * The default implementation returns <code>true</code>.
    * @return <code>true</code> if it can
    */
    public boolean canCopy () {
        return true;
    }

    /** Test whether this node can be cut.
    * The default implementation assumes it can if this node is {@link #writeable}.
    * @return <code>true</code> if it can
    */
    public boolean canCut () {
        return writeable;
    }

    /** Set all actions for this node.
    * @param actions new list of actions
    */
    public void setActions(SystemAction[] actions) {
        systemActions = actions;
    }

    /** Calls super.fireCookieChange. The reason why is redefined
    * is only to allow the access from this package.
    */
    void superFireCookieChange() {
        fireCookieChange();
    }

    /** Get a cookie from this node.
    * First tries the node itself, then {@link Pattern#getCookie}.
    * @param type the cookie class
    * @return the cookie or <code>null</code>
    */
    public Node.Cookie getCookie (Class type) {
        Node.Cookie c = super.getCookie(type);
        if (c == null)
            c = pattern.getCookie(type);

        return c;
    }

    /** Test for equality.
    * @return <code>true</code> if the represented {@link Pattern}s are equal
    */
    public boolean equals (Object o) {
        return (o instanceof PatternNode) && (pattern.equals (((PatternNode)o).pattern));
    }

    /** Get a hash code.
    * @return the hash code from the represented {@link Pattern}
    */
    public int hashCode () {
        return pattern.hashCode ();
    }

    /** Sets the name of the node */
    public final void setName( String name ) {
        try {
            JMIUtils.beginTrans(true);
            boolean rollback = true;
            try {
                pattern.patternAnalyser.setIgnore(true);
                setPatternName(name);
                rollback = false;
            } finally {
                pattern.patternAnalyser.setIgnore(false);
                JMIUtils.endTrans(rollback);
            }
            
            superSetName( name );
            
        } catch (JmiException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }

    }

    /** Called when node name is changed */
    public final void superSetName(String name) {
        super.setName( name );
    }

    /** Set's the name of pattern. Must be defined in descendants
    */
    protected abstract void setPatternName(String name) throws JmiException;

    /** Create a node property representing the pattern's name.
    * @param canW if <code>false</code>, property will be read-only
    * @return the property.
    */
    protected Node.Property createNameProperty(boolean canW) {
        return new PatternPropertySupport(PatternProperties.PROP_NAME, String.class, canW) {
                   /** Gets the value */
                   public Object getValue () {
                       return ((Pattern)pattern).getName();
                   }

                   /** Sets the value */
                   public void setValue(Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       String str = (String) val;
                       try {
                           JMIUtils.beginTrans(true);
                           boolean rollback = true;
                           try {
                               pattern.patternAnalyser.setIgnore(true);
                               setPatternName(str);
                               rollback = false;
                           } finally {
                               pattern.patternAnalyser.setIgnore(false);
                               JMIUtils.endTrans(rollback);
                           }
                       } catch (JmiException e) {
                           throw new InvocationTargetException(e);
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException();
                       }
                       superSetName(str);
                   }
               };
    }

    /** Called when the node has to be destroyed */
    public void destroy() throws IOException {
        try {
            JMIUtils.beginTrans(true);
            boolean rollback = true;
            try {
                pattern.destroy();
                rollback = false;
            } finally {
                JMIUtils.endTrans(rollback);
            }
        } catch (JmiException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
        super.destroy();
    }

    protected static String getFormattedMethodName(Method method) {
        String name = null;
        Format fmt = SourceNodes.createElementFormat("{n} ({p})"); // NOI18N
        try {
            if (method != null) {
                name = fmt.format (method);
            }
        } catch (IllegalArgumentException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }

        return name != null? name: PatternNode.getString("LAB_NoMethod"); // NOI18N
    }

    // ================== Pattern listener =================================

    public void propertyChange(PropertyChangeEvent evt) {
        setIconBaseWithExtension( resolveIconBase() + ".gif");
        superSetName( pattern.getName() );
        firePropertyChange( null, null, null );
    }

    // ================== Property support for element nodes =================

    /** Property support for element nodes properties.
    */
    static abstract class PatternPropertySupport extends PropertySupport {
        /** Constructs a new ElementProp - support for properties of
        * element hierarchy nodes.
        *
        * @param name The name of the property
        * @param type The class type of the property
        * @param canW The canWrite flag of the property
        */
        public PatternPropertySupport(String name, java.lang.Class type, boolean canW) {
            super(name, type,
                  getString("PROP_" + name),
                  getString("HINT_" + name),
                  true, canW);
        }

        /** Setter for the value. This implementation only tests
        * if the setting is possible.
        *
        * @param val the value of the property
        * @exception IllegalAccessException when this ElementProp was constructed
        *            like read-only.
        */

        public void setValue (Object val) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
            if (!canWrite())
                throw new IllegalAccessException(getString("MSG_Cannot_Write"));
        }

    }
    
    static String getString(String key) {
        return NbBundle.getBundle("org.netbeans.modules.beans.Bundle").getString(key);
    }

}
