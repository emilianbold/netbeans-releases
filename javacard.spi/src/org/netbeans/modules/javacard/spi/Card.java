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
package org.netbeans.modules.javacard.spi;

import java.util.Set;
import org.openide.util.Lookup;

/**
 * A card or device to which Java Card applications can be deployed.
 * Most interaction with cards is accomplished by requesting interfaces
 * in this package from the Card's Lookup (or, for convenience, getCapability()).
 * <p/>
 * Any Java Card platform has a set of one or more Cards which projects can
 * be deployed to and interacted with in the IDE.
 * <p/>
 * The set of interfaces available at runtime depends on the card's <i>state</i>.
 * For example, a Java Card RI virtual Card can be <i>started</i> &mdash; which
 * literally means launching an emulator process the IDE can connect to.
 * When <code>Card.getState().isNotRunning()</code> is true, an instance of
 * <code>StartCapability</code> should be available from the Card.  When the
 * emulator process is launched, the instance of <code>StartCapablity</code>
 * disappears and an instance of <code>StopCapability</code> appears.
 * The presence or absence of capabilities determines the enablement of actions
 * in the UI for a Card.
 * <p/>
 * Base classes such as <code><a href="AbstractCard.html">AbstractCard</a></code>
 * and <code><a href="BaseCard.html">BaseCard</a></code> handle most of the
 * complexities of adding and removing capabilities in a thread-safe way.
 * <p/>
 * All cards should provide an instance of CardInfo in their lookup.
 * Other interfaces are optional, depending on the kind of device being
 * deployed to &mdash; for example, an emulator can be started and stopped,
 * so it makes sense for an emulator to provide instances of
 * <code>StartStopCapability</code>.  On the other hand, an actual card reader
 * which pretends, to the local computer, to be a network interface cannot
 * be asked to unplug itself, so a <code>StartStopCapability</code> for such
 * a card would make no sense.
 * <p/>
 * Common capability types provided are
 * <ul>
 * <li><code>CapabilitiesProvider</code> This interface provides the list of
 * other capability types this card supports.  If you are implementing Card
 * directly, it is not necessary, but the abstract support subclasses of Card
 * provided in this library require it.</li>
 * <li><code>CardInfo</code> &mdash; This provides basic user-visible information
 * such as display name, description and other user-visible attributes, and
 * should always be provided</li>
 * <li><code>StartCapability</code> &mdash; Cards which run on an emulator,
 * whose process can be started and stopped should provide an instance of
 * StartCapability</li>
 * <li><code>StopCapability</code> &mdash; Cards which run on an emulator,
 * whose process can be stopped should provide an instance of
 * StopCapability</li>
 * <li><code>ResumeCapability</code> &mdash; Cards which run on an emulator which
 * can be resumed after being stopped can optionally provide instances of
 * this interface</li>
 * <li><code>PortProvider</code> &mdash; provides an interface for determining
 * what ports on the local or remote host this device uses</li>
 * <li><code>CardCustomizerProvider</code> &mdash; Allows a Card instance to
 * provide a customizer component</li>
 * <li><code>ApduSupport</code> &mdash; provides a URL to a card manager,
 * application, value for the Contactless protocol to be used, and other
 * miscellaneous properties somewhat specific to the RI</li>
 * </ul>
 *
 * @author Tim Boudreau
 */
public interface Card extends Lookup.Provider {
    /**
     * Convenience equivalent of getLookup().lookup(Class&lt;T&gt; type).
     * Use this to discover what capabilities a given Card instance supports.
     *
     * @param <T> The return type
     * @param type The return type
     * @return An object of type T or null
     */
    public <T extends ICardCapability> T getCapability(Class<T> type);
    /**
     * Get the set of capabilities which this card supports (which may not
     * be the same as what can be obtained at the moment - for example, a
     * running card <i>supports</i> a StartCapability, but should not expose
     * it if it is already started.
     *
     * @return A set of classes.
     */
    public Set<Class<? extends ICardCapability>> getSupportedCapabilities();
    /**
     * Get the set of capabilities that are currently available.
     *
     * @return A set of classes
     */
    public Set<Class<? extends ICardCapability>> getEnabledCapabilities();
    /**
     * Determine if the capability type is available at this time.
     *
     * @param type The type of ICardCapability desired
     * @return true if it is available
     */
    public boolean isCapabilityEnabled (Class<? extends ICardCapability> type);
    /**
     * Determine if, at any time, the passed capability type may be available
     * on this card instance.
     * @param type The type of capability desired
     * @return Whether or not this card instance supports this capability.  For
     * example, a remote card will probably not expose StartCapability or
     * StopCapability, because there is no way to start/stop a remote VM.
     */
    public boolean isCapabilitySupported (Class<? extends ICardCapability> type);
    /**
     * Get the current state of the card, which is an enum indicating if the
     * card is running, not running, in the process of being started or stopped,
     * if a transition to started or stopped is pending, etc.  Generally, the
     * state of the card determines the set of capabilities available from it,
     * and is also used to indicate to the user through the UI whether the card
     * is running or not.
     * <p/>
     * This method may be called from any thread and is expected to be thread
     * safe.
     * @return The current state of the card
     */
    public CardState getState();
    /**
     * Add an observer to detect changes in the card's state.  Changes to
     * the card's state should be fired asynchronously in the AWT event thread.
     * Any observer which depends on the current state of the card should
     * call card.getState(), as this may have changed by the time the observer's
     * methods are called.
     *
     * @param obs An observer
     */
    public void addCardStateObserver (CardStateObserver obs);
    /**
     * Remove a card state observer
     * @param obs An observer
     */
    public void removeCardStateObserver (CardStateObserver obs);
    /**
     * Get the Java Card platform which owns this Card.
     * @return A platform.  May not be null (deleted or otherwise defective
     * platforms should return false for JavacardPlatform.isValid())
     */
    public JavacardPlatform getPlatform();
    /**
     * Get the system ID of this card.  Conventionally, the system ID of this
     * card is the name of the file in the system filesystem which represents
     * this card.  No two cards owned by a single platform should have the same
     * system ID - these must be unique, so as to identify what card a project
     * will be deployed to.
     * @return A string ID which is unique to this card on this platform
     */
    public String getSystemId();
    /**
     * Determine whether this card is usable.  Generally this means it is
     * configured with settings that will allow the IDE to talk to the card,
     * the card in question actually exists (in the case of cards which may
     * be disconnected from a computer), and that the platform providing this
     * card also returns true from its isValid() method.
     * @return Whether or not this card can be communicated with and used by
     * the IDE
     */
    public boolean isValid();
}
