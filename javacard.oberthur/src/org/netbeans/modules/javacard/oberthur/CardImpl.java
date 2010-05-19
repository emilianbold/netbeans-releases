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
package org.netbeans.modules.javacard.oberthur;

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.javacard.spi.AbstractCard;
import org.netbeans.modules.javacard.spi.CardState;
import org.netbeans.modules.javacard.spi.ConnectionWatchdog;
import org.netbeans.modules.javacard.spi.ICardCapability;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.capabilities.CapabilitiesProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.netbeans.modules.javacard.spi.capabilities.ContactedProtocol;
import org.netbeans.modules.javacard.spi.capabilities.PortKind;
import org.netbeans.modules.javacard.spi.capabilities.PortProvider;
import org.netbeans.modules.javacard.spi.capabilities.UrlCapability;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Simple implementation of a single smart card.
 *
 * @author Tim Boudreau
 */
final class CardImpl extends AbstractCard {
    public static final String SINGLE_CARD_ID = "card"; //NOI18N
    private static final String POLL_URL = "http://smartcard:80/SysInfo/getData"; //NOI18N
    private static final String HOST = "smartcard"; //NOI18N
    private volatile boolean connected;
    private final ConnectionWatchdog<CardImpl> watchdog = new ConnectionWatchdog<CardImpl>(
            this, new StateUpdaterCallback());

    CardImpl(JavacardPlatform pform) {
        super(pform, SINGLE_CARD_ID);
    }

    URL getPollUrl() throws MalformedURLException {
        return new URL (POLL_URL);
    }

    @Override
    protected void onBeforeFirstLookup() {
        initCapabilities(new Ports(), new Info(), new Caps(), new Urls());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    void setConnected(boolean connected) {
        if (this.connected != connected) {
            this.connected = connected;
            connected &= super.isValid();
            setState(connected ? CardState.RUNNING : CardState.NOT_RUNNING);
        }
    }

    void refreshStatus() {
        watchdog.refreshNow();
    }

    private final class Ports implements PortProvider {
        public Set<Integer> getClaimedPorts() {
            return Collections.singleton(80);
        }

        public Set<Integer> getPortsInUse() {
            return connected ? getPortsInUse() : Collections.<Integer>emptySet();
        }

        public String getHost() {
            return HOST;
        }

        public int getPort(PortKind role) {
            switch (role) {
                case HTTP:
                    return 80;
                default:
                    return -1;
            }
        }
    }

    private final class Info implements CardInfo {
        public String getSystemId() {
            return SINGLE_CARD_ID;
        }

        public String getDisplayName() {
            return NbBundle.getMessage(Info.class, "CARD_DISPLAY_NAME"); //NOI18N
        }

        public Image getIcon() {
            return ImageUtilities.loadImage("org/netbeans/modules/javacard/oberthur/otcard.png"); //NOI18N
        }

        public String getDescription() {
            PortProvider p = getCapability(PortProvider.class);
            return NbBundle.getMessage(Info.class, "CARD_DESCRIPTION", //NOI18N
                    p.getHost(), p.getClaimedPorts().iterator().next(),
                    getPlatform().getDisplayName());
        }
    }

    private final class Caps implements CapabilitiesProvider {
        public Set<Class<? extends ICardCapability>> getSupportedCapabilityTypes() {
            Set<Class<? extends ICardCapability>> result = new HashSet<Class<? extends ICardCapability>>();
            result.add (CardInfo.class);
            result.add (PortProvider.class);
            result.add (CapabilitiesProvider.class);
            result.add (UrlCapability.class);
            return result;
        }
    }

    private final class Urls implements UrlCapability {

        public ContactedProtocol getContactedProtocol() {
            return null;
        }

        public String getURL() {
            return "http://smartcard/";
        }

        public String getManagerURL() {
            return POLL_URL;
        }

        public String getListURL() {
            return null;
        }

    }
}
