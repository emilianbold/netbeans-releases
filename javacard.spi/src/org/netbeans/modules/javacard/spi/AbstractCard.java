/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.spi;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javacard.spi.capabilities.CapabilitiesProvider;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.WeakSet;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Convenience implementation of Card which provides standard handling for
 * CardStateObservers, and capability addition/removal.
 *
 * @author Tim Boudreau
 */
public abstract class AbstractCard implements Card {
    private CardState state = CardState.NEW;
    private final List<CardStateObserver> observers = Collections.synchronizedList(new LinkedList<CardStateObserver>());
    private final Object stateLock = new Object();
    private final InstanceContent content = new InstanceContent();
    private final Lookup lkp = new ProxyLookup(Lookups.singleton(this),
            new AbstractLookup(content));
    private final String systemId;
    private static volatile boolean shutdownHookAdded;
    private static Set<Process> processes =
            Collections.synchronizedSet(new WeakSet<Process>());
    private final JavacardPlatform platform;

    protected AbstractCard (JavacardPlatform platform, String systemId) {
        this.platform = platform;
        this.systemId = systemId;
    }

    /**
     * Create a dummy card for a case where a card is specified but no such
     * card actually exists.  The resulting card will return false from
     * isValid() and return no usable capabilities, just CardInfo.
     * @param name The ID/display name of the missing card
     * @return A fake card
     */
    public static Card createBrokenCard (String name) {
        return new BrokenCard(name);
    }

    /**
     * The default implementation only checks if the platform returns
     * isValid().
     * @return
     */
    public boolean isValid() {
        return platform != null && platform.isValid();
    }

    /**
     * Get the platform that owns this card
     * @return A platform
     */
    public final JavacardPlatform getPlatform() {
        return platform;
    }

    /**
     * Set the state of this card.  Fires changes to observers,
     * replanned into the event thread if necessary.
     *
     * @param state The new state
     */
    protected final void setState (final CardState state) {
        CardState old;
        synchronized (stateLock) {
            old = this.state;
            this.state = state;
        }
        if (old != state) {
            try {
                onStateChanged (old, state);
            } finally {
                final CardState oldState = old;
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        //Note:  Since this code can be invokeLatered(), the
                        //value of the state may not still be in sync.  
                        CardStateObserver[] l = observers.toArray(new CardStateObserver[0]);
                        for (CardStateObserver lis : l) {
                            lis.onStateChange(AbstractCard.this, oldState, state);
                        }
                    }
                });
            }
        }
    }

    protected void onStateChanged (CardState old, CardState nue) {
        //do nothing
    }

    /**
     * Get a capability of this card
     * @param <T> The capability type
     * @param type The capability type
     * @return The capability, or null if not present
     */
    public final <T extends ICardCapability> T getCapability(Class<T> type) {
        return getLookup().lookup(type);
    }

    //XXX get rid of this since we have CapabilitiesProvider?
    @SuppressWarnings("unchecked") //NOI18N
    public final Set<Class<? extends ICardCapability>> getSupportedCapabilities() {
        CapabilitiesProvider prov = getLookup().lookup(CapabilitiesProvider.class);
        if (prov == null) {
            throw new IllegalStateException ("No CapabilitiesProvider found in lookup"); //NOI18N
        }
        Set<Class<? extends ICardCapability>> result = new HashSet<Class<? extends ICardCapability>>();
        result.addAll(prov.getSupportedCapabilityTypes());
        for (Class<?> c : getClass().getInterfaces()) {
            if (c.isAssignableFrom(ICardCapability.class)) {
                result.add((Class<? extends ICardCapability>) c);
            }
        }
        return result;
    }

    /**
     * Pass an initial set of capabilities.  Called when the card's lookup is
     * being constructed.
     * @param capabilities An array of capabilities
     */
    protected final void initCapabilities (ICardCapability... capabilities) {
        content.set(Arrays.asList(capabilities), null);
    }

    /**
     * Get the currently available set of capability types
     * @return
     */
    public final Set<Class<? extends ICardCapability>> getEnabledCapabilities() {
        Set<Class<? extends ICardCapability>> result = new HashSet<Class<? extends ICardCapability>>();
        for (Lookup.Item<ICardCapability> it : lkp.lookupResult(ICardCapability.class).allItems()) {
            result.add(it.getType());
        }
        return result;
    }

    /**
     * Determine if a particular capability type is enabled
     * @param type
     * @return
     */
    public final boolean isCapabilityEnabled(Class<? extends ICardCapability> type) {
        return getEnabledCapabilities().contains(type);
    }

    public final boolean isCapabilitySupported(Class<? extends ICardCapability> type) {
        return getSupportedCapabilities().contains(type);
    }

    public final CardState getState() {
        synchronized (stateLock) {
            return state;
        }
    }

    public final void addCardStateObserver(CardStateObserver obs) {
        observers.add(obs);
    }

    public final void removeCardStateObserver(CardStateObserver obs) {
        observers.remove(obs);
    }

    public final String getSystemId() {
        return systemId;
    }

    Lookup createPreloadLookup() {
        //XXX get this out of here
        return Lookup.EMPTY;
    }
    private volatile boolean initialized;
    private volatile boolean initializing;

    public final Lookup getLookup() {
        if (initializing) {
            return createPreloadLookup();
        }
        if (!initialized) {
            initializing = true;
            try {
                onBeforeFirstLookup();
                initialized = true;
            } finally {
                initializing = false;
            }
        }
        return lkp;
    }

    /**
     * Hook method to do additional initializing before the first time
     * the Lookup is fetched.
     */
    protected void onBeforeFirstLookup() {
        //do nothing
    }

    /**
     * Add a capability to this card.
     * @param c The capability
     * @throws AssertionError if assertions are enabled, and this call would 
     * result in two of the passed capability subclass in the card's
     * capabilities.
     */
    protected final void addCapability(ICardCapability c) {
        Logger log = Logger.getLogger (AbstractCard.class.getName());
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER, "Add Capability {0} to {1}", new Object[]{c, this}); //NOI18N
        }
        logAddition (c);
        if (!initializing) {
            Object old = getLookup().lookup(c.getClass());
            if (old != null) {
                content.remove(old);
            }
        }
        content.add(c);
        assert new HashSet<Object>(getLookup().lookupAll(c.getClass())).size() == getLookup().lookupAll(c.getClass()).size() :
            "Lookup should not contain multiple instances of " + c.getClass() + "(" + getLookup().lookupAll(c.getClass()); //NOI18N
    }

    void logAddition(ICardCapability c) {
    }

    /**
     * Remove a capability from this card
     * @param c The capability
     */
    protected final void removeCapability(ICardCapability c) {
        content.remove(c);
    }

    /**
     * Remove a capability by type
     * @param <T>
     * @param type
     */
    @SuppressWarnings("unchecked") //NOI18N
    protected <T extends Class<? extends ICardCapability>> void removeCapability(T type) {
        Object t = getLookup().lookup((Class<? extends ICardCapability>)type);
        if (t != null) {
            content.remove(t);
        }
    }

    /**
     * Register a process which should be shut down on VM shutdown
     * @param p
     */
    protected void registerProcess (final Process p) {
        processes.add(p);
        installShutdownHook();
    }

    private static void installShutdownHook() {
        if (!shutdownHookAdded) {
            shutdownHookAdded = true;
            Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown(),
                    "JavaCard Server Process Destroyer Shutdown Hook")); //NOI18N
        }
    }

    private static volatile boolean inShutdown;
    private static final class Shutdown implements Runnable {
        public void run() {
            inShutdown = true;
            for (Iterator<Process> i = processes.iterator(); i.hasNext();) {
                Process p = i.next();
                if (p != null) { //WeakSet can return null
                    p.destroy();
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractCard other = (AbstractCard) obj;
        if ((this.systemId == null) ? (other.systemId != null) : !this.systemId.equals(other.systemId)) {
            return false;
        }
        if (this.platform != other.platform && (this.platform == null || !this.platform.equals(other.platform))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.systemId != null ? this.systemId.hashCode() : 0);
        hash = 11 * hash + (this.platform != null ? this.platform.hashCode() : 0);
        return hash;
    }
}
