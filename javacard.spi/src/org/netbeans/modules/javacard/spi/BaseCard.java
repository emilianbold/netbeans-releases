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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.spi.capabilities.AntTargetInterceptor;
import org.netbeans.modules.javacard.spi.capabilities.ApduSupport;
import org.netbeans.modules.javacard.spi.capabilities.CapabilitiesProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardContentsProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardCustomizerProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.netbeans.modules.javacard.spi.capabilities.ClearEpromCapability;
import org.netbeans.modules.javacard.spi.capabilities.DebugCapability;
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
 * adding a StopCapability when the card is stopped and removing the StartCapability
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
        rp = new RequestProcessor("Java Card Instance " + systemId);
    }

    protected final Class<T> type() {
        return type;
    }

    protected abstract CardInfo createCardInfo(T t);

    protected AntTargetInterceptor createAntTargetInterceptor(T t) {
        return null;
    }

    protected ApduSupport createApduSupport(T t) {
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

    protected CardCustomizerProvider createCardCustomizerProvidert(T t) {
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
            maybeAddCapability(createCardCustomizerProvidert(props));
        }
        maybeAddEpromCapabilities();
        log("calling subclass initLookup()"); //NOI18N
        initLookup();
        log("exit onBeforeFirstLookup"); //NOI18N
    }

    @Override
    protected final Lookup createPreloadLookup() {
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
            maybeAddCapability(createResumeCapability(props));
        }
    }

    protected FileObject getEpromFile(boolean create) {
        FileObject fld = Utils.sfsFolderForDeviceEepromsForPlatformNamed(getSystemId(), create);
        FileObject result = null;
        if (fld != null) {
            result = fld.getFileObject(getSystemId(), JCConstants.EEPROM_FILE_EXTENSION);
            if (result == null && create) {
                if (result == null) {
                    try {
                        result = fld.createData(getSystemId(), JCConstants.EEPROM_FILE_EXTENSION);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Clear references to external processes, close sockets, etc.  Called
     * when the state is changed to NOT_RUNNING.
     */
    protected void clearProcessReferences() {
        //do nothing
    }

    @Override
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
                    maybeAddCapability(createCardCustomizerProvidert(t));
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
