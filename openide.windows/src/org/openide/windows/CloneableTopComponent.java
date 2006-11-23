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

package org.openide.windows;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.io.NbMarshalledObject;

/** A top component which may be cloned.
* Typically cloning is harmless, i.e. the data contents (if any)
* of the component are the same, and the new component is merely
* a different presentation.
* Also, a list of all cloned components is kept.
*
* @author Jaroslav Tulach
*/
public abstract class CloneableTopComponent extends TopComponent implements Externalizable, TopComponent.Cloneable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4893753008783256289L;

    // say what? --jglick

    /* Empty set that should save work with testing like
    * <pre>
    * if (ref == null || ref.isEmpty ()) {
    *   CloneableTopComponent c = new CloneableTopComponent (obj);
    *   ref = c.getReference ();
    * }
    * </pre>
    * Instead one can always set <CODE>ref = Ref.EMPTY</CODE> and test only if
    * <CODE>ref.isEmpty</CODE> returns <CODE>true</CODE>.
    */

    /** Empty clone-sister list.
    */
    public static final Ref EMPTY = new Ref();

    /** reference with list of components */
    private Ref ref;

    /** Create a cloneable top component.
    */
    public CloneableTopComponent() {
    }

    /** Clone the top component and register the clone.
    * @return the new component
    */
    public final Object clone() {
        return cloneComponent();
    }

    /** Clone the top component and register the clone.
    * Simply calls createClonedObject () and registers the component to
    * Ref.
    *
    * @return the new cloneable top component
    */
    public final CloneableTopComponent cloneTopComponent() {
        CloneableTopComponent top = createClonedObject();

        // register the component if it has not been registered before
        top.setReference(getReference());

        return top;
    }

    /** Clone the top component and register the clone.
    * @return the new component
    */
    public final TopComponent cloneComponent() {
        return cloneTopComponent();
    }

    /** Called from {@link #clone} to actually create a new component from this one.
    * The default implementation only clones the object by calling {@link Object#clone}.
    * Subclasses may leave this as is, assuming they have no special needs for the cloned
    * data besides copying it from one object to the other. If they do, the superclass
    * method should be called, and the returned object modified appropriately.
    * @return a copy of this object
    */
    protected CloneableTopComponent createClonedObject() {
        try {
            // clones the component using serialization
            NbMarshalledObject o = new NbMarshalledObject(this);
            return (CloneableTopComponent) o.get();
        } catch (IOException ex) {
            throw new AssertionError(ex);
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex);
        }
    }

    /** Get a list of all components which are clone-sisters of this one.
    *
    * @return the clone registry for this component's group
    */
    public synchronized final Ref getReference() {
        if (ref == null) {
            ref = new Ref(this);
        }

        return ref;
    }

    /** Changes the reference to which this components belongs.
    * @param another the new reference this component should belong
    */
    public synchronized final void setReference(Ref another) {
        if (another == EMPTY) {
            throw new IllegalArgumentException(
                NbBundle.getBundle(CloneableTopComponent.class).getString("EXC_CannotAssign")
            );
        }

        if (ref != null) {
            // Remove from old ref, we are going to belong to 'another' reference.
            ref.removeComponent(this);
        }

        // Register with the new reference.
        another.register(this);

        // Finally set the field.
        ref = another;
    }

    /** Overrides superclass method, adds unregistering from references.
     * @see Ref */
    protected void componentClosed() {
        super.componentClosed();

        if (!isOpened()) {
            getReference().unregister(this);
        }
    }

    /**
     * Unregisters this component from its clone list.
     * {@inheritDoc}
     */
    public boolean canClose() {
        if (!isOpened()) {
            return false;
        }

        return getReference().unregister(this);
    }

    @SuppressWarnings("deprecation")
    public boolean canClose(Workspace workspace, boolean last) {
        if (last) {
            return getReference().unregister(this);
        }

        return true;
    }

    /** Called when the last component in a clone group is closing.
    * The default implementation just returns <code>true</code>.
    * Subclasses may specify some hooks to run.
    * @return <CODE>true</CODE> if the component is ready to be
    *    closed, <CODE>false</CODE> to cancel
    */
    protected boolean closeLast() {
        return true;
    }

    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        super.readExternal(oi);

        if (serialVersion != 0) {
            // since serialVersion > 0
            // the reference object is also stored
            Ref ref = (Ref) oi.readObject();

            if (ref != null) {
                setReference(ref);
            }
        }
    }

    public void writeExternal(ObjectOutput oo) throws java.io.IOException {
        super.writeExternal(oo);

        oo.writeObject(ref);
    }

    /** Keeps track of a group of sister clones.
    * <P>
    * <B>Warning:</B>
    * For proper use
    * subclasses should have method readResolve () and implement it
    * in right way to deal with separate serialization of TopComponent.
    */
    public static class Ref implements Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 5543148876020730556L;

        /** manipulation lock */
        private static final Object LOCK = new Object();

        /** Set of registered components. */
        private transient /*final*/ Set<CloneableTopComponent> componentSet = new HashSet<CloneableTopComponent>(7);

        /** Default constructor for creating empty reference.
        */
        protected Ref() {
        }

        /** Constructor.
        * @param c the component to refer to
        */
        private Ref(CloneableTopComponent c) {
            synchronized (LOCK) {
                componentSet.add(c);
            }
        }

        /** Enumeration of all registered components.
        * @return enumeration of CloneableTopComponent
        */
        public Enumeration<CloneableTopComponent> getComponents() {
            Set<CloneableTopComponent> components;

            synchronized (LOCK) {
                components = new HashSet<CloneableTopComponent>(componentSet);
            }

            return Collections.enumeration(components);
        }

        /** Test whether there is any component in this set.
        * @return <CODE>true</CODE> if the reference set is empty
        */
        public boolean isEmpty() {
            synchronized (LOCK) {
                return componentSet.isEmpty();
            }
        }

        /** Retrieve an arbitrary component from the set.
        * @return some component from the list of registered ones
        * @exception NoSuchElementException if the set is empty
         * @deprecated Use {@link #getArbitraryComponent} instead.
         *             It doesn't throw a runtime exception.
        */
        public CloneableTopComponent getAnyComponent() {
            synchronized (LOCK) {
                return componentSet.iterator().next();
            }
        }

        /** Gets arbitrary component from the set. Preferrably returns currently
         * active component if found in the set.
         * @return arbitratry <code>CloneableTopComponent</code> from the set
         *         or <code>null</code> if the set is empty
         * @since 3.41 */
        public CloneableTopComponent getArbitraryComponent() {
            TopComponent activated = WindowManager.getDefault().getRegistry().getActivated();

            synchronized (LOCK) {
                // prefer already active component
                if (componentSet.contains(activated)) {
                    return (CloneableTopComponent) activated;
                }

                Iterator<CloneableTopComponent> it = componentSet.iterator();

                if (it.hasNext()) {
                    return it.next();
                } else {
                    return null;
                }
            }
        }

        /** Register new component.
        * @param c the component to register
        */
        private final void register(CloneableTopComponent c) {
            synchronized (LOCK) {
                componentSet.add(c);
            }
        }

        /** Unregister the component. If this is the last asks if it is
        * allowed to unregister it.
        *
        * @param c the component to unregister
        * @return true if the component agreed to be unregister
        */
        private final boolean unregister(CloneableTopComponent c) {
            int componentCount;

            synchronized (LOCK) {
                if (!componentSet.contains(c)) {
                    return true;
                }

                componentCount = componentSet.size();
            }

            if ((componentCount > 1) || c.closeLast()) {
                removeComponent(c);

                return true;
            } else {
                return false;
            }
        }

        private void removeComponent(CloneableTopComponent c) {
            synchronized (LOCK) {
                componentSet.remove(c);
            }
        }

        /** Adds also initializing of <code>componentSet</code> field. */
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();

            synchronized (LOCK) {
                componentSet = new HashSet<CloneableTopComponent>(7);
            }
        }
    }
     // end of Ref
}
