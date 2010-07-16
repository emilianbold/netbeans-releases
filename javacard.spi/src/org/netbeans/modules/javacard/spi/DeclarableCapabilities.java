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

import org.netbeans.modules.javacard.spi.capabilities.ClearEpromCapability;
import org.netbeans.modules.javacard.spi.capabilities.StopCapability;
import org.netbeans.modules.javacard.spi.capabilities.ResumeCapability;
import org.netbeans.modules.javacard.spi.capabilities.StartCapability;
import org.netbeans.modules.javacard.spi.capabilities.DebugCapability;
import org.netbeans.modules.javacard.spi.capabilities.EpromFileCapability;
import org.netbeans.modules.javacard.spi.capabilities.ProfileCapability;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.javacard.spi.capabilities.AntTargetInterceptor;
import org.netbeans.modules.javacard.spi.capabilities.UrlCapability;
import org.netbeans.modules.javacard.spi.capabilities.CardContentsProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardCustomizerProvider;
import org.netbeans.modules.javacard.spi.capabilities.DeleteCapability;
import org.netbeans.modules.javacard.spi.capabilities.PortProvider;

/**
 * Enum of capabilities which can be specified in a device properties file,
 * to indicate whether or not the Card object should expose capabilities for
 * them.  Corresponding types are made available in the Card's Lookup.
 *
 * @author Tim
 */
public enum DeclarableCapabilities {
    START,
    STOP,
    RESUME,
    DEBUG,
    PROFILE,
    EPROM_FILE,
    CLEAR_EPROM,
    URL,
    CONTENTS,
    PORTS,
    CUSTOMIZER,
    INTERCEPTOR,
    DELETE,
    ;
    /**
     * Parse a string (for example, from a .jcard file) into a set of
     * capabilities.
     * @param s The string, a comma delimited list of enum constants as strings,
     * e.g. "START,STOP,DEBUG"
     * @return A set of capabilities
     */
    public static final Set<? extends DeclarableCapabilities> forString(String s) {
        String[] caps = s.split(","); //NOI18N
        Set<DeclarableCapabilities> result = new HashSet<DeclarableCapabilities>(
                DeclarableCapabilities.values().length);
        for (String c : caps) {
            for (DeclarableCapabilities cc : values()) {
                if (cc.name().equals(c.trim())) {
                    result.add (cc);
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Convert a set of capabilities into a string that can be written into
     * a .jcard file and read back using ControllableCapabilities.forString().
     * @param set
     * @return
     */
    public static String toString (Set<? extends DeclarableCapabilities> set) {
        StringBuilder sb = new StringBuilder();
        for (DeclarableCapabilities c : set) {
            if (sb.length() > 0) {
                sb.append (',');
            }
            sb.append (c.name());
        }
        return sb.toString();
    }

    /**
     * Get a set of ICardCapability types for a set of capabilities
     * @param set A set of capabilities
     * @return A set of ICardCapability subtypes
     */
    public static Set<Class<? extends ICardCapability>> types (Set<? extends DeclarableCapabilities> set) {
        Set<Class<? extends ICardCapability>> result = new HashSet<Class<? extends ICardCapability>>(set.size());
        for (DeclarableCapabilities c : set) {
            result.add (c.type());
        }
        return result;
    }

    /**
     * Get the capability interface type corresponding to this enum
     * constant
     * @return A class, such as DebugCapability for ControllableCapabilities.DEBUG
     */
    public Class<? extends ICardCapability> type() {
        switch (this) {
            case DEBUG :
                return DebugCapability.class;
            case PROFILE :
                return ProfileCapability.class;
            case RESUME :
                return ResumeCapability.class;
            case START :
                return StartCapability.class;
            case STOP :
                return StopCapability.class;
            case EPROM_FILE :
                return EpromFileCapability.class;
            case CLEAR_EPROM :
                return ClearEpromCapability.class;
            case URL :
                return UrlCapability.class;
            case CONTENTS :
                return CardContentsProvider.class;
            case PORTS :
                return PortProvider.class;
            case CUSTOMIZER :
                return CardCustomizerProvider.class;
            case INTERCEPTOR :
                return AntTargetInterceptor.class;
            case DELETE :
                return DeleteCapability.class;
            default :
                throw new AssertionError("" + this); //NOI18N
        }
    }
}
