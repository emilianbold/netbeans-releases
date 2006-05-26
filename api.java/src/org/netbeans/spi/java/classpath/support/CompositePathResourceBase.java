/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.spi.java.classpath.support;

import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.ClassPathImplementation;

import java.net.URL;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

/**
 * This class provides a base class for PathResource implementations
 * @since org.netbeans.api.java/1 1.4
 */
public abstract class CompositePathResourceBase implements PathResourceImplementation {

    private URL[] roots;
    private ClassPathImplementation model;
    private ArrayList<PropertyChangeListener> pListeners;

    /**
     * Returns the roots of the PathResource
     * @return URL[]
     */
    public final URL[] getRoots() {
        if (this.roots == null) {
            synchronized (this) {
                if (this.roots == null) {
                    initContent ();
                    List<URL> result = new ArrayList<URL> ();
                    for (PathResourceImplementation pri : this.model.getResources()) {
                        result.addAll (Arrays.asList(pri.getRoots()));
                    }
                    this.roots = result.toArray (new URL [result.size()]);
                }
            }
        }
        return this.roots;
    }


    /**
     * Returns the ClassPathImplementation representing the content of this PathResourceImplementation
     * @return ClassPathImplementation
     */
    public final ClassPathImplementation getContent() {
		initContent ();
    	return this.model;
    }

    /**
     * Adds property change listener.
     * The listener is notified when the roots of the PathResource are changed.
     * @param listener
     */
    public synchronized final void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.pListeners == null)
            this.pListeners = new ArrayList<PropertyChangeListener> ();
        this.pListeners.add (listener);
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public synchronized final void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.pListeners == null)
            return;
        this.pListeners.remove (listener);
    }

    /**
     * Fires PropertyChangeEvent
     * @param propName name of property
     * @param oldValue old property value or null
     * @param newValue new property value or null
     */
    protected final void firePropertyChange (String propName, Object oldValue, Object newValue) {
        PropertyChangeListener[] _listeners;
        synchronized (this) {
            if (this.pListeners == null)
                return;
            _listeners = this.pListeners.toArray(new PropertyChangeListener[this.pListeners.size()]);
        }
        PropertyChangeEvent event = new PropertyChangeEvent (this, propName, oldValue, newValue);
        for (PropertyChangeListener l : _listeners) {
            l.propertyChange (event);
        }
    }

    /** Creates the array of the roots of PathResource.
     * Most PathResource (directory, jar) have single root,
     * but the PathResource can have more than one root to
     * represent more complex resources like libraries.
     * The returned value is cached.
     * @return ClassPathImplementation
     */
    protected abstract ClassPathImplementation createContent ();


	private synchronized void initContent () {
		if (this.model == null) {
			ClassPathImplementation cp = createContent ();
			assert cp != null;
			cp.addPropertyChangeListener (new PropertyChangeListener () {
				public void propertyChange (PropertyChangeEvent event) {
					roots = null;
					firePropertyChange (PROP_ROOTS, null,null);
				}
			});
            this.model = cp;
		}
	}
}
