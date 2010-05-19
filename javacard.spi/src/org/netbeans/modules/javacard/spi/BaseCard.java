/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.spi.capabilities.AntTargetInterceptor;
import org.netbeans.modules.javacard.spi.capabilities.UrlCapability;
import org.netbeans.modules.javacard.spi.capabilities.CapabilitiesProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardContentsProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardCustomizerProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.netbeans.modules.javacard.spi.capabilities.ClearEpromCapability;
import org.netbeans.modules.javacard.spi.capabilities.DebugCapability;
import org.netbeans.modules.javacard.spi.capabilities.DeleteCapability;
import org.netbeans.modules.javacard.spi.capabilities.EpromFileCapability;
import org.netbeans.modules.javacard.spi.capabilities.PortProvider;
import org.netbeans.modules.javacard.spi.capabilities.ProfileCapability;
import org.netbeans.modules.javacard.spi.capabilities.ResumeCapability;
import org.netbeans.modules.javacard.spi.capabilities.StartCapability;
import org.netbeans.modules.javacard.spi.capabilities.StopCapability;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * Base class for Card implementations similar to the Java Card RI.  Useful if
 * you are implementing support for another platform which is fairly similar to
 * the way the Java Card RI works.
 * <p/>
 * A Card is mostly a container for ICardCapability objects which start, stop,
 * resume, etc. the card.  This class handles the mechanics of, for example,
 * adding a StartCapability when the card is stopped and removing the StartCapability
 * and replacing it with a StopCapability when the card is started.
 * (if the subclass returns non-null from createStopCapability() and
 * createStartCapability()).
 * <p/>
 * The class contains a set of factory methods for standard capability objects,
 * all of which return null.  Subclasses may override those that they want
 * to return non-null.
 * <p/>
 * Note that some capabilities go hand-in-hand - if you support StartCapability,
 * you probably also support StopCapability and perhaps ResumeCapability.
 *
 * @author Tim Boudreau
 */
public abstract class BaseCard<T extends CapabilitiesProvider> extends AbstractCard {
    protected final RequestProcessor rp;
    private final Class<T> type;
    protected BaseCard(JavacardPlatform platform, String systemId, Class<T> type) {
        super(platform, systemId);
        this.type = type;
        rp = new RequestProcessor("Java Card Instance " + systemId); //NOI18N
    }

    /**
     * Get the type, passed to the constructor, of the CapabilitiesProvider
     * for this card.  For example, the CapabilitiesProvider of RI Cards
     * reads a set of enum constants from DeclarableCapabilities to populate
     * the set of supported types.
     * @return The type
     */
    protected final Class<T> type() {
        return type;
    }

    /**
     * Create a CardInfo with display name, etc. for this card.  All subclasses
     * are expected to support CardInfo.
     * @param t the CapabilitiesProvider of this card
     * @return A CardInfo
     */
    protected abstract CardInfo createCardInfo(T t);

    protected AntTargetInterceptor createAntTargetInterceptor(T t) {
        return null;
    }

    protected UrlCapability createApduSupport(T t) {
        return null;
    }

    protected CardContentsProvider createCardContentProvider(T t) {
        return null;
    }

    protected ClearEpromCapability createClearEpromCapability(T t) {
        return null;
    }

    protected DebugCapability createDebugCapability(T t) {
        return null;
    }

    protected EpromFileCapability createEpromCapability(T t) {
        return null;
    }

    protected PortProvider createPortProvider(T t) {
        return null;
    }

    protected ProfileCapability createProfileCapability(T t) {
        return null;
    }

    protected ResumeCapability createResumeCapability(T t) {
        return null;
    }

    protected StartCapability createStartCapability(T t) {
        return null;
    }

    protected StopCapability createStopCapability(T t) {
        return null;
    }

    protected CardCustomizerProvider createCardCustomizerProvider(T t) {
        return null;
    }

    public DeleteCapability createDeleteCapability(T t) {
        return null;
    }

    protected void log(String toLog) {
        log(Level.FINE, toLog);
    }

    protected void log(Level l, String toLog) {
        Logger logger = Logger.getLogger(getClass().getName());
        if (logger.isLoggable(l)) {
            logger.log(l, toLog);
        }
    }

    private void maybeAddCapability(ICardCapability cap) {
        if (cap != null) {
            addCapability(cap);
        }
    }

    private void allowNullInitCapabilities(ICardCapability... capabilities) {
        List<ICardCapability> l = new LinkedList<ICardCapability>();
        for (ICardCapability c : capabilities) {
            if (c != null) {
                l.add(c);
            }
        }
        initCapabilities(l.toArray(new ICardCapability[0]));
    }

    @Override
    void logAddition(ICardCapability c) {
        boolean asserts = false;
        assert asserts = true;
        if (asserts) {
            Set<Class<? extends ICardCapability>> s = 
                    new HashSet<Class<? extends ICardCapability>>(
                    getCapability(type()).getSupportedCapabilityTypes());
            int sz = s.size();
            s.removeAll(Arrays.asList(c.getClass().getInterfaces()));
            if (s.size() == sz) {
                Logger.getLogger(BaseCard.class.getName()).log(Level.WARNING,
                    "Adding a capability not in the list of supported capabilities: " + //NOI18N
                    c.getClass().getName() + "; supported types: " + //NOI18N
                    getCapability(type()).getSupportedCapabilityTypes());
            }
        }
    }

    /**
     * Create some object which subclasses CapabilitiesProvider, and which
     * loads, in some way, a description of the Card which can provide
     * whatever information is needed to implement other capabilities.
     * @return An object which subclasses CapabilitiesProvider
     */
    protected abstract T loadData();

    @Override
    protected synchronized final void onBeforeFirstLookup() {
        //Keep synchronized - otherwise, theoretically two threads could
        //enter and both initialize
        log("onBeforeFirstLookup on " + this); //NOI18N

        T props = loadData();
        //XXX add Apdu conditionally if certain properties defined
        allowNullInitCapabilities(props, createCardInfo(props),
                createPortProvider(props), createAntTargetInterceptor(props),
                createCardContentProvider(props), createApduSupport(props));
        Set<Class<? extends ICardCapability>> declaredCapabilities = props.getSupportedCapabilityTypes();
        //Check state - while RI cards cannot be started w/o accessing the lookup,
        //an implementation could theoretically already be running on startup and
        //detect this somehow
        CardState state = getState();
        if (declaredCapabilities.contains(StartCapability.class) && (state.isNotRunning() || state.isTransitionToStop())) {
            maybeAddCapability(createStartCapability(props));
        }
        if (declaredCapabilities.contains(StopCapability.class) && (state.isRunning() || state.isTransitionToStart())) {
            maybeAddCapability(createStopCapability(props));
        }
        if (declaredCapabilities.contains(DebugCapability.class)) {
            maybeAddCapability(createDebugCapability(props));
        }
        if (declaredCapabilities.contains(ProfileCapability.class)) {
            maybeAddCapability(createProfileCapability(props));
        }
        if (state.isNotRunning() && declaredCapabilities.contains(CardCustomizerProvider.class)) {
            maybeAddCapability(createCardCustomizerProvider(props));
        }
        if (declaredCapabilities.contains(DeleteCapability.class)) {
            maybeAddCapability(createDeleteCapability(props));
        }
        maybeAddEpromCapabilities();
        log("calling subclass initLookup()"); //NOI18N
        initLookup();
        log("exit onBeforeFirstLookup"); //NOI18N
    }

    @Override
    final Lookup createPreloadLookup() {
        return Lookups.fixed(loadData());
    }

    /**
     * Initialize any capabilities this subclass adds here.  Called from
     * onBeforeFirstLookup(), the first time getLookup() is called.
     */
    protected void initLookup() {
        //do nothing
    }
    
    private void maybeAddEpromCapabilities() {
        T props = getCapability(type());
        Set<Class<? extends ICardCapability>> capabilityTypes = props.getSupportedCapabilityTypes();
        boolean epromSupported = capabilityTypes.contains(EpromFileCapability.class);
        boolean clearEpromSupported = capabilityTypes.contains(ClearEpromCapability.class);
        FileObject epromFile = null;
        if (epromSupported) {
            EpromFileCapability eprom = createEpromCapability(props);
            if (eprom != null) {
                addCapability(eprom);
                epromFile = eprom.getEpromFile();
            }
        }
        if (epromFile != null && clearEpromSupported) {
            maybeAddCapability(createClearEpromCapability(props));
        }
        boolean resumeSupported = capabilityTypes.contains(ResumeCapability.class);
        if ((resumeSupported && epromFile != null && epromSupported) || (resumeSupported && !epromSupported)) {
            ResumeCapability resume = createResumeCapability(props);
            maybeAddCapability(resume);
        }
    }

    /**
     * Clear references to external processes, close sockets, etc.  Called
     * when the state is changed to NOT_RUNNING.
     */
    protected void clearProcessReferences() {
        //do nothing
    }

    /**
     * Adds and removes capabilities based on the current state.  Any
     * implementation should call super(), unless it wants to assume complete
     * responsibility for ensuring that the set of capabilities is appropriate
     * to the state.
     * <p/>
     * The default implementation handles common state transitions, based on
     * the set of supported capabilities - i.e., if the state goes from a
     * not-started state to a started state, this method will automatically
     * remove any StartCapability from this card, and add a StopCapability if
     * createStopCapability() returns non-null.
     * @param old The previous state
     * @param nue The new state
     */
    @Override
    @SuppressWarnings("fallthrough") //NOI18N
    protected void onStateChanged(CardState old, CardState nue) {
        if (old == nue) return;
        log(this + " stateChange " + old + "->" + nue); //NOI18N
        CardInfo info = getCapability(CardInfo.class);
        String name = info == null || info.getDisplayName() == null ? getSystemId() : info.getDisplayName();
        StatusDisplayer.getDefault().setStatusText(nue.statusMessage(name));
        T t = getCapability(type());
        switch (nue) {
            case BEFORE_RESUMING:
            case BEFORE_STARTING:
            case STARTING:
            case RESUMING:
                removeCapability(ClearEpromCapability.class);
                removeCapability(StartCapability.class);
                removeCapability(ResumeCapability.class);
                removeCapability(CardCustomizerProvider.class);
                break;
            case RUNNING:
            case RUNNING_IN_DEBUG_MODE: {
                CapabilitiesProvider props = getCapability(CapabilitiesProvider.class);
                Set<Class<? extends ICardCapability>> declaredCapabilities = props.getSupportedCapabilityTypes();
                if (declaredCapabilities.contains(StopCapability.class)) {
                    maybeAddCapability(createStopCapability(t));
                }
                removeCapability(CardCustomizerProvider.class);
                break;
            }
            case NOT_RUNNING:
                CapabilitiesProvider props = getCapability(CapabilitiesProvider.class);
                Set<Class<? extends ICardCapability>> declaredCapabilities = props.getSupportedCapabilityTypes();
                clearProcessReferences();
                maybeAddEpromCapabilities();
                if (declaredCapabilities.contains(StartCapability.class)) {
                    maybeAddCapability(createStartCapability(t));
                }
                if (declaredCapabilities.contains(CardCustomizerProvider.class)) {
                    maybeAddCapability(createCardCustomizerProvider(t));
                }
            case STOPPING:
                //Do this here as well as in BEFORE_STOPPING, to handle
                //the case that the remote VM is killed
                removeCapability(StopCapability.class);
                break;
            case BEFORE_STOPPING:
                removeCapability(StopCapability.class);
                break;
            case NEW:
            default:
                throw new AssertionError("Cannot set state to " + nue); //NOI18N
        }
    }
}
