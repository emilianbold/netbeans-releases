/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javahelp;

import java.util.*;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.help.HelpSet;

import org.openide.util.*;

import org.netbeans.api.javahelp.Help;

/** An implementation of the JavaHelp system (a little more concrete).
* @author Jesse Glick
*/
public abstract class AbstractHelp extends Help implements HelpConstants {
    
    /** constructor for subclasses
     */    
    protected AbstractHelp() {}
    
    /** the results of the search for helpsets
     */    
    private Lookup.Result helpsets = null;
    /** Get all available help sets.
     * Pay attention to {@link #helpSetsChanged} to see
     * when this set will change.
     * @return a collection of HelpSet
     */    
    protected final Collection getHelpSets() {
        if (helpsets == null) {
            Installer.log.fine("searching for instances of HelpSet...");
            helpsets = Lookup.getDefault().lookup(new Lookup.Template(HelpSet.class));
            helpsets.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    helpSetsChanged();
                }
            });
            fireChangeEvent(); // since someone may be listening to whether they are ready
        }
        Collection c = helpsets.allInstances();
        if (Installer.log.isLoggable(Level.FINE)) {
            List l = new ArrayList(Math.min(1, c.size()));
            Iterator it = c.iterator();
            while (it.hasNext()) {
                l.add(((HelpSet)it.next()).getTitle());
            }
            Installer.log.fine("listing helpsets: " + l);
        }
        return c;
    }
    
    /** Are the help sets ready?
     * @return true if they have been loaded
     */
    protected final boolean helpSetsReady() {
        return helpsets != null;
    }

    /** Whether a given help set is supposed to be merged
     * into the master set.
     * @param hs the help set
     * @return true if so
     */    
    protected final boolean shouldMerge(HelpSet hs) {
        Boolean b = (Boolean)hs.getKeyData(HELPSET_MERGE_CONTEXT, HELPSET_MERGE_ATTR);
        return (b == null) || b.booleanValue();
    }
    
    /** Called when the set of available help sets changes.
     * Fires a change event to listeners; subclasses may
     * do extra cleanup.
     */
    protected void helpSetsChanged() {
        Installer.log.fine("helpSetsChanged");
        fireChangeEvent();
    }
    
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    /** all change listeners
     */    
    private final Set listeners = new HashSet(1); // Set<ChangeListener>
    
    /** Fire a change event to all listeners.
     */    
    private final void fireChangeEvent() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fireChangeEvent();
                }
            });
            return;
        }
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        Installer.log.fine("Help.stateChanged");
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
}
