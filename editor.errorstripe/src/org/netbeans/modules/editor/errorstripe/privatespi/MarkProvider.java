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

package org.netbeans.modules.editor.errorstripe.privatespi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;

/**Provider of list of {@link Mark}. The provider is supposed to report marks
 * found in a document. The provider can also tell whether the current list of marks
 * is up to date with the current state of the document. The provider is supposed
 * to fire a property change event if the list of marks or up-to-date property
 * are changed.
 *
 * @author Jan Lahoda
 */
public abstract class MarkProvider {
    
    /**Name of property which should be fired when the list of {@link Mark}s changes.
     */
    public static final String PROP_MARKS = "marks"; // NOI18N
    
    private PropertyChangeSupport pcs;
    
    /** Creates a new instance of MarkProvider */
    public MarkProvider() {
        pcs = new PropertyChangeSupport(this);
    }
    
    /**Return list of {@link Mark}s that are to be shown in the Error Stripe.
     *
     * @return list of {@link Mark}s
     */
    public abstract List/*<Mark>*/ getMarks();
    
    /**Register a {@link PropertyChangeListener}.
     *
     * @param l listener to register
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    /**Unregister a {@link PropertyChangeListener}.
     *
     * @param l listener to register
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    /**Fire property change event to all registered listener. Subclasses should call
     * this method when they need to fire the {@link java.beans.PropertyChangeEvent}
     * because property {@link #PROP_UP_TO_DATE} or {@link #PROP_MARKS} have changed.
     *
     * @param name name of the property ({@link #PROP_UP_TO_DATE} or {@link #PROP_MARKS})
     * @param old  previous value of the property or null if unknown
     * @param nue  current value of the property or null if unknown
     */
    protected final void firePropertyChange(String name, Object old, Object nue) {
        pcs.firePropertyChange(name, old, nue);
    }
    
}
