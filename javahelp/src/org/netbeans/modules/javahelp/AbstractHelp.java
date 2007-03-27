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

package org.netbeans.modules.javahelp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.help.HelpSet;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.javahelp.Help;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** An implementation of the JavaHelp system (a little more concrete).
* @author Jesse Glick
*/
public abstract class AbstractHelp extends Help implements HelpConstants {

    /** constructor for subclasses
     */
    protected AbstractHelp() {}
    
    /** the results of the search for helpsets
     */    
    private Lookup.Result<HelpSet> helpsets = null;
    /** Get all available help sets.
     * Pay attention to {@link #helpSetsChanged} to see
     * when this set will change.
     * @return a collection of HelpSet
     */    
    protected final Collection<? extends HelpSet> getHelpSets() {
        if (helpsets == null) {
            Installer.log.fine("searching for instances of HelpSet...");
            helpsets = Lookup.getDefault().lookupResult(HelpSet.class);
            helpsets.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    helpSetsChanged();
                }
            });
            fireChangeEvent(); // since someone may be listening to whether they are ready
        }
        Collection<? extends HelpSet> c = helpsets.allInstances();
        if (Installer.log.isLoggable(Level.FINE)) {
            List<String> l = new ArrayList<String>(Math.min(1, c.size()));
            for (HelpSet hs: c) {
                l.add(hs.getTitle());
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
        cs.addChangeListener(l);
    }
    
    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    private final ChangeSupport cs = new ChangeSupport(this);
    
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
        Installer.log.fine("Help.stateChanged");
        cs.fireChange();
    }
    
}
