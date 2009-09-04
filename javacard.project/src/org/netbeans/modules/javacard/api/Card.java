/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.api;

import org.netbeans.modules.javacard.card.BrokenCard;
import org.netbeans.modules.javacard.card.ReferenceImplementation;
import org.openide.util.Mutex;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javacard.constants.JavacardDeviceKeyNames;
import org.netbeans.modules.javacard.constants.JavacardPlatformKeyNames;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Represents one card or device controlled by a JavacardPlatform.
 */
public abstract class Card {
    private CardState state = CardState.JUST_CREATED;
    private final List<CardStateObserver> observers = Collections.synchronizedList(new LinkedList<CardStateObserver>());
    private final Object stateLock = new Object();
    private final JavacardPlatform platform;
    private final String id;

    /**
     * Create a new Card
     * @param platform  The platform it runs on
     * @param id An ID which should be unique among all cards on the given platform
     */
    protected Card (JavacardPlatform platform, String id) {
        this.platform = platform;
        this.id = id;
    }

    /**
     * Create an instance of Card for the specified platform with the
     * specified properties.
     * <p>
     * This method will look up all types of CardInstanceFactory registered
     * in the default Lookup and use the one which is annotated with
     * PlatformKind with a value matching 
     * platform.getPlatformKind().  In this way a module which is compatible
     * with the RI or RC's card definitions does not have to implement
     * Card - JavacardPlatforms and Cards are decoupled from each other.
     * <p>
     * If no appropriate CardInstanceFactory is found, this method will
     * return a "broken" instance of Card which displays an error message,
     * and will log the reason.
     * <p>
     * Platform implementations should be careful about casting the result
     *
     * @param platform
     * @param properties
     * @return
     */
    public static Card create (JavacardPlatform platform, Properties properties) {
        Parameters.notNull ("platform", platform); //NOI18N
        Parameters.notNull ("properties", properties); //NOI18N
        String kind = platform.getPlatformKind();
        if (kind == null || "".equals(kind)) {
            Logger.getLogger(Card.class.getName()).log(Level.FINE,
                    platform + " properties do not define '" + //NOI18N
                    JavacardPlatformKeyNames.PLATFORM_KIND + "': \n" + //NOI18N
                    properties);
            String name = properties.getProperty (JavacardDeviceKeyNames.DEVICE_DISPLAY_NAME);
            return new BrokenCard (name != null ? name : platform.getDisplayName());
        }
        Lookup.Result<CardInstanceFactory> res = Lookup.getDefault().lookupResult (
                CardInstanceFactory.class);
        CardInstanceFactory<?> factory = null;
        for (Lookup.Item<CardInstanceFactory> item : res.allItems()) {
            Class <? extends CardInstanceFactory> type = item.getType();
            PlatformKind thePKind = type.getAnnotation(PlatformKind.class);
            if (thePKind == null) {
                Logger.getLogger (Card.class.getName()).log (
                        Level.WARNING, type.getName() +
                        " does not annotate itself with " +  //NOI18N
                        PlatformKind.class.getName());
            } else {
                if (kind.equals(thePKind.kind())) {
                    factory = item.getInstance();
                    break;
                }
            }
        }
        if (factory == null) {
            Logger.getLogger (Card.class.getName()).log (Level.WARNING, "No " + //NOI18N
                    "registered CardInstanceFactory for kind " + kind); //NOI18N
            return new BrokenCard(kind);
        } else {
            return factory.create(platform, properties);
        }
    }

    public abstract String getServerURL();

    public abstract String getCardManagerURL();

    /**
     * Get the set of all port numbers that any registered server claims
     * @return A set of integers representing port numbers
     */
    public abstract Set<Integer> getPortsInUse();

    /**
     * Get the set of all port numbers that any registered server claims which
     * currently have a running process using them
     * @return representing port numbers
     */
    public abstract Set<Integer> getPortsInActiveUse();

    /**
     * Start the device, possibly in debug mode
     * @param forDebug If true, start the device in debug mode (if possible)
     * @param args Any startup arguments, or null
     */
    public abstract Condition startServer(boolean forDebug, Object... args);

    /**
     * Stop this device
     */
    public abstract void stopServer();

    /**
     * Resume this device
     */
    public abstract void resumeServer();

    public final boolean isDebugMode() {
        return getState() == CardState.RUNNING_IN_DEBUG_MODE;
    }

    /**
     * Get a human readable display name for this device.  By default, this simply
     * returns getId(), but can be overridden if desired
     * @return
     */
    public String getDisplayName() {
        return getId();
    }

    /**
     * Add an observer which can be notified of state changes (not running, running, stopping, etc.).
     * Listeners are notified on the AWT event queue regardless of what thread changes state.  There
     * is therefore no guarantee that the new state an observer is passed still matches the return
     * value of this card's getState() method.
     * @param l a listener
     */
    public final void addCardStateObserver(CardStateObserver l) {
        observers.add (l);
    }

    /**
     * Remove an observer which can be notified of state changes (not running, running, stopping, etc.).
     * Listeners are notified on the AWT event queue regardless of what thread changes state.  There
     * is therefore no guarantee that the new state an observer is passed still matches the return
     * value of this card's getState() method.
     * @param l a listener
     */
    public final void removeCardStateObserver(CardStateObserver l) {
        observers.remove (l);
    }

    /**
     * Determine if this is an instance of the Java Card&trade; Reference Implementation.
     * @return
     */
    public final boolean isReferenceImplementation() {
        return this instanceof ReferenceImplementation;
    }

    /**
     * Determine if the card is definitively running.  This means the current state is RUNNING or RUNNING_IN_DEBUG_MODE.
     * States STARTING, RESUMING and STOPPING will return false.
     * @return
     */
    public final boolean isRunning() {
        return getState().isRunning();
    }

    /**
     * Determine if the card is definitively not running (i.e. this method will return false
     * if the state is STARTING, RESUMING or STOPPING).
     * @return
     */
    public boolean isNotRunning() {
        CardState currState = getState();
        return (currState == CardState.NOT_RUNNING) ||
               (currState == CardState.JUST_CREATED);
    }        

    protected final void setState (final CardState state) {
        CardState old;
        synchronized (stateLock) {
            old = this.state;
            this.state = state;
        }
        if (old != state) {
            onStateChanged (old, state);
            final CardState oldState = old;
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    CardStateObserver[] l = observers.toArray(new CardStateObserver[0]);
                    for (CardStateObserver lis : l) {
                        lis.onStateChange(Card.this, oldState, state);
                    }
                }
            });
        }
    }

    protected void onStateChanged (CardState old, CardState nue) {
        //do nothing
    }

    public final CardState getState() {
        synchronized (stateLock) {
            return state;
        }
    }

    public boolean isValid() {
        return !(this instanceof BrokenCard);
    }

    public final JavacardPlatform getPlatform() {
        return platform;
    }

    public final String getId() {
        return id;
    }

    /**
     * Determine if this card is remote or not.  This is relevant in the
     * case of emulators which may be on the local machine (actions should
     * check for and possibly start an instance) vs. remote (actions should
     * assume the remote card manager is running and fail gracefully if the
     * remote host is not accessible).
     * @return The default implementation returns false;  other implementations
     * can override as appropriate.
     */
    public boolean isRemote() {
        return false;
    }
}
