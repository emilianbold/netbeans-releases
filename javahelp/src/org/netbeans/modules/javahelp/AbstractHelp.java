/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.javahelp;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import javax.help.HelpSet;
import javax.help.HelpUtilities;
import javax.help.NavigatorView;
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
        assert (c = selectSafeHelpSets(c)) != null;
        return c;
    }

    /** Filter out damaged help sets.
     *
     * Added because problems with incorrectly indexed help sets are reported
     * sometimes. See #127368.
     * When these errors are fixed, this method (and related methods)
     * can be probably removed.
     *
     * This method is called only if assertations are enabled.
     */
    private static Collection<? extends HelpSet> selectSafeHelpSets(
            Collection<? extends HelpSet> sets) {
        Collection<HelpSet> safeSets = new ArrayList<HelpSet>(sets.size());
        for (HelpSet hs : sets) {
            if (isSafe(hs)) {
                safeSets.add(hs);
            }
        }
        return safeSets;
    }

    /** Return true if a help set is safe to be added to list of help sets.
     * This can be used to filter out damaged help sets.
     */
    private static boolean isSafe(HelpSet hs) {
        for (NavigatorView nv : hs.getNavigatorViews()) {
            if ("Search".equals(nv.getName())) {                        //NOI18N
                String engine;
                engine = (String) nv.getParameters().get("engine");     //NOI18N
                if (engine == null) {
                    engine = HelpUtilities.getDefaultQueryEngine();
                }
                assert engine != null;
                try {
                    checkSearchEngineCanBeCreated(engine, hs, nv);
                } catch (Exception e) {
                    Installer.log.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    return false;
                }
            }
        }
        return true;
    }

    /** Ensure that search engine for a help set can be created.
     * If not, an exception is thrown.
     */
    private static void checkSearchEngineCanBeCreated(String engine,
            HelpSet hs, NavigatorView nv) throws Exception {

        URL base = hs.getHelpSetURL();
        ClassLoader loader = hs.getLoader();
        Class klass;

        try {
            if (loader == null) {
                klass = Class.forName(engine);
            } else {
                klass = loader.loadClass(engine);
            }
        } catch (Throwable t) {
            String p = "Could not load engine named {0} "               //NOI18N
                    + "for Help Set {1} with url {2}";                  //NOI18N
            String msg = MessageFormat.format(p, engine, hs.getTitle(), base);
            throw new Exception(msg, t);
        }
        Constructor konstructor = null;
        try {
            @SuppressWarnings("UseOfObsoleteCollectionType")
            Class types[] = {URL.class, Hashtable.class};
            konstructor = klass.getConstructor(types);
        } catch (Throwable t) {
            String p = "Could not find constructor for {0} "            //NOI18N
                    + "for Help Set {1} with url {2}";                  //NOI18N
            String msg = MessageFormat.format(p, engine, hs.getTitle(), base);
            throw new Exception(msg, t);
        }
        try {
            Object args[] = {base, nv.getParameters()};
            konstructor.newInstance(args);
        } catch (Throwable t) {
            String p = "Exception while creating engine {0} "           //NOI18N
                    + "for Help Set {1} with url {2}";                  //NOI18N
            String msg = MessageFormat.format(p, engine, hs.getTitle(), base);
            throw new Exception(msg, t);
        }
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
