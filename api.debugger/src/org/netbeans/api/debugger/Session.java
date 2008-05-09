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

package org.netbeans.api.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.netbeans.spi.debugger.ContextProvider;


/** Session visually represents one process or application. It should
 * be simple bean with properties like process ID, session name, etc.
 * All other functionality is deleagted to current debugger engine.
 *
 * <p><br><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tbody><tr bgcolor="#ccccff">
 * <td colspan="2"><font size="+2"><b>Description </b></font></td>
 * </tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Functionality</b></font></td><td>
 *
 * <b>Properties:</b>
 *    Session has two standard read only properties - name ({@link #getName}) and
 *    location name ({@link #getLocationName}).
 *
 * <br><br>
 * <b>Management of languages and engines:</b>
 *    Debugger Core supports debugging in different languages. It means that 
 *    each session can be debugged using different languages and 
 *    {@link org.netbeans.api.debugger.DebuggerEngine}s. Session manages list 
 *    of supported languages ({@link #getSupportedLanguages}) and current 
 *    language ({@link #getCurrentLanguage}). Current language can be changed
 *    ({@link #setCurrentLanguage}).
 *    Each language corresponds to one 
 *    {@link org.netbeans.api.debugger.DebuggerEngine} 
 *    ({@link #getEngineForLanguage}). So, the current language
 *    defines current debuggger engine ({@link #getCurrentEngine})
 *
 *    A support for a new debugger language can be added during a start of
 *    debugging only. See 
 *    {@link org.netbeans.api.debugger.DebuggerManager#startDebugging} and
 *    {@link org.netbeans.spi.debugger.DebuggerEngineProvider}
 *
 * <br><br>
 * <b>Support for aditional services:</b>
 *    Session is final class. The standard method how to 
 *    extend its functionality is using lookup methods ({@link #lookup} and 
 *    {@link #lookupFirst}).
 *    There are two ways how to register some service provider for some
 *    type of Session:
 *    <ul>
 *      <li>Register 'live' instance of service provider during creation of 
 *        new instance of Session (see method
 *        {@link org.netbeans.spi.debugger.SessionProvider#getServices}).
 *      </li>
 *      <li>Register service provider in Manifest-inf/debugger/&lt;type ID&gt;
 *        folder. See Debugger SPI for more information about
 *        registration.</li>
 *    </ul>
 *
 * <br>
 * <b>Support for listening:</b>
 *    Session propagates all changes to
 *    {@link java.beans.PropertyChangeListener}.
 *
 * <br> 
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Clinents / Providers</b></font></td><td>
 *
 * This class is final, so it does not have any external provider.
 * Debugger Core and UI modules are clients of this class.
 *
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Lifecycle</b></font></td><td>
 *
 * A new instance of Session class can be created and registerred to 
 * {@link org.netbeans.api.debugger.DebuggerManager} during the process
 * of starting of debugging (see
 * {@link org.netbeans.api.debugger.DebuggerManager#startDebugging}).
 *
 * Session is removed automatically from 
 * {@link org.netbeans.api.debugger.DebuggerManager} when the 
 * number of "supported languages" ({@link #getSupportedLanguages}) is zero.
 *
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Evolution</b></font></td><td>
 *
 * No method should be removed from this class, but some functionality can
 * be added in future.
 *
 * </td></tr></tbody></table>
 *
 * @author Jan Jancura
 */
public final class Session implements ContextProvider {
    
    /** Name of property for current language. */
    public static final String PROP_CURRENT_LANGUAGE = "currentLanguage";
    
    /** Name of property for the set of supported languages. */
    public static final String PROP_SUPPORTED_LANGUAGES = "supportedLanguages";

    
    // variables ...............................................................
    
    private String              name;
    private String              locationName;
    private DebuggerEngine      currentDebuggerEngine;
    private String              currentLanguage;
    private String[]            languages;
    private DebuggerEngine[]    engines;
//    private Listener            listener;
    private PropertyChangeSupport pcs;
    private Lookup              lookup;
    Lookup                      privateLookup;

    
    // initialization ..........................................................
    
    Session (
        String name,
        String locationName,
        String id,
        Object[] services,
        Lookup diLookup
    ) {
        this.name = name;
        this.locationName = locationName;
        this.languages = new String [0];
        this.engines = new DebuggerEngine [0];
        pcs = new PropertyChangeSupport (this);
        
        // create lookup
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 0, services.length);
        s [s.length - 1] = this;
        privateLookup = new Lookup.Compound (
            new Lookup.Instance (s),
            new Lookup.MetaInf (id)
        );
        this.lookup = new Lookup.Compound (
            diLookup,
            privateLookup
        );
    }

    
    // public interface ........................................................

    /**
     * Returns display name of this session.
     *
     * @return display name of this session
     */
    public String getName () {
        return name;
    }
    
    /**
     * Returns identifier of type of this session. This id is used for 
     * identification of engine during registration of services in 
     * Meta-inf/debugger.
     *
     * @return identifier of type of this engine
     */
//    public String getTypeID () {
//        return id;
//    }
    
    /**
     * Returns name of location this session is running on.
     *
     * @return name of location this session is running on
     */
    public String getLocationName () {
        return locationName;
    }
    
    /**
     * Returns current debugger engine for this session.
     *
     * @return current debugger engine for this session
     */
    public DebuggerEngine getCurrentEngine () {
        return currentDebuggerEngine;
    }
    
    /**
     * Returns current language for this session.
     *
     * @return current language for this session
     */
    public String getCurrentLanguage () {
        return currentLanguage;
    }
    
    /**
     * Returns set of all languages supported by this session.
     *
     * @return set of all languages supported by this session
     * @see org.netbeans.spi.debugger.DebuggerEngineProvider
     */
    public String[] getSupportedLanguages () {
        return languages;
    }
    
    /**
     * Returns list of services of given type from given folder.
     *
     * @param service a type of service to look for
     * @return list of services of given type
     */
    public <T> List<? extends T> lookup(String folder, Class<T> service) {
        return lookup.lookup (folder, service);
    }
    
    /**
     * Returns one service of given type from given folder.
     *
     * @param service a type of service to look for
     * @return ne service of given type
     */
    public <T> T lookupFirst(String folder, Class<T> service) {
        return lookup.lookupFirst (folder, service);
    }
    
    /**
     * Kills all registerred engines / languages. This utility method calls
     * <pre>doAction (DebuggerEngine.ACTION_KILL)</pre> method on all
     * registerred DebuggerEngines.
     */
    public void kill () {
        HashSet dead = new HashSet (Arrays.asList (engines));
        Iterator i = dead.iterator ();
        while (i.hasNext ())
            ((DebuggerEngine) i.next ()).getActionsManager ().
                doAction (ActionsManager.ACTION_KILL);
    }

    /**
     * Return DebuggerEngine registerred for given language or null.
     *
     * @return DebuggerEngine registerred for given language or null
     */
    public DebuggerEngine getEngineForLanguage (String language) {
        int i, k = languages.length;
        for (i = 0; i < k; i++)
            if (languages [i].equals (language))
                return engines [i];
        return null;
    }
    
    /**
     * Sets current language for this session. Language should be refisterred
     * for this session.
     *
     * @param language current language
     * @see org.netbeans.spi.debugger.DebuggerEngineProvider
     */
    public void setCurrentLanguage (String language) {
        int i, k = languages.length;
        for (i = 0; i < k; i++) {
            if (language.equals (languages [i])) {
                Object oldL = currentLanguage;
                currentLanguage = language;
                currentDebuggerEngine = engines [i];
                pcs.firePropertyChange (
                    PROP_CURRENT_LANGUAGE,
                    oldL, 
                    currentLanguage
                );
            }
        }
    }

    
    // support methods .........................................................
    
    Lookup getLookup () {
        return lookup;
    }
    
    void addLanguage (
        String language,
        DebuggerEngine engine
    ) {
        // is pair already added?
        int i, k = languages.length;
        for (i = 0; i < k; i++)
            if (language.equals (languages [i])) {
                engines [i] = engine;
                return;
            }
        
        // add pair
        String[] newLanguages = new String [languages.length + 1];
        DebuggerEngine[] newEngines = new DebuggerEngine [engines.length + 1];
        System.arraycopy (languages, 0, newLanguages, 0, languages.length);
        System.arraycopy (engines, 0, newEngines, 0, engines.length);
        newLanguages [languages.length] = language;
        newEngines [engines.length] = engine;
        Object oldL = languages;
        languages = newLanguages;
        engines = newEngines;
        DebuggerManager.getDebuggerManager ().addEngine (engine);
        pcs.firePropertyChange (
            PROP_SUPPORTED_LANGUAGES,
            oldL,
            languages
        );
//        engine.addEngineListener (
//            DebuggerEngineListener.PROP_ACTION_PERFORMED,
//            listener
//        );
        if (currentLanguage == null) {
            setCurrentLanguage (language);
        }
    }
    
    void removeEngine (
        DebuggerEngine engine
    ) {
        if (engines.length == 0) return;
        int i, k = engines.length;
        ArrayList newLanguages = new ArrayList ();
        ArrayList newEngines = new ArrayList ();
        for (i = 0; i < k; i++)
            if (!engine.equals (engines [i])) {
                newLanguages.add (languages [i]);
                newEngines.add (engines [i]);
            }
        String[] oldL = languages;
        languages = (String[]) newLanguages.toArray 
            (new String [newLanguages.size ()]);
        engines = (DebuggerEngine[]) newEngines.toArray 
            (new DebuggerEngine [newEngines.size ()]);
        DebuggerManager.getDebuggerManager ().removeEngine (engine);
        
        pcs.firePropertyChange (
            PROP_SUPPORTED_LANGUAGES,
            oldL,
            languages
        );
    }
    
    void removeLanguage (
        String language,
        DebuggerEngine engine
    ) {
        int i, k = languages.length;
        for (i = 0; i < k; i++)
            if (language.equals (languages [i])) {
                if (engines [i] != engine)
                    throw new IllegalArgumentException ();
                break;
            }
        if (i >= k) return;
        
        String[] newLanguages = new String [k - 1];
        DebuggerEngine[] newEngines = new DebuggerEngine [k - 1];
        if (i > 0) {
            System.arraycopy (languages, 0, newLanguages, 0, i);
            System.arraycopy (engines, 0, newEngines, 0, i);
        }
        System.arraycopy (languages, i + 1, newLanguages, i, k - i - 1);
        System.arraycopy (engines, i + 1, newEngines, i, k - i - 1);
        Object oldL = languages;
        languages = newLanguages;
        engines = newEngines;
        pcs.firePropertyChange (
            PROP_SUPPORTED_LANGUAGES,
            oldL,
            languages
        );
        
        k = engines.length;
        for (i = 0; i < k; i++)
            if (engines [i] == engine)
                return;
        DebuggerManager.getDebuggerManager ().removeEngine (engine);
    }

    /**
     * Returns string representation of this session.
     *
     * @return string representation of this session
     */
    public String toString () {
        return "" + getClass ().getName () + " " + getLocationName () + ":" +
            getName ();
    }
    
    
    // listener support ........................................................
    

    /**
     * Adds a property change listener.
     *
     * @param l the listener to add
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }
    
    /**
     * Removes a property change listener.
     *
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }
    
    /**
     * Adds a property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l the listener to add
     */
    public void addPropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.addPropertyChangeListener (propertyName, l);
    }
    
    /**
     * Removes a property change listener.
     *
     * @param propertyName a name of property to stop listening on
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.removePropertyChangeListener (propertyName, l);
    }
    

    // innerclasses ............................................................
    
//    private class Listener extends DebuggerEngineAdapter {
//        
//        public void actionPerformed (
//            DebuggerEngine engine, 
//            Object action, 
//            boolean success
//        ) {
//            if (action != DebuggerEngine.ACTION_KILL) return;
//            removeEngine (engine);
//        }
//        
//        public void actionStateChanged (
//            DebuggerEngine engine, 
//            Object action, 
//            boolean enabled
//        ) {
//        }
//        
//    }
}


