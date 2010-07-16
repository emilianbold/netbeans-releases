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

import java.awt.Image;
import java.util.Arrays;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.netbeans.modules.javacard.spi.capabilities.CapabilitiesProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tim Boudreau
 */
final class BrokenCard implements Card, CardInfo, CapabilitiesProvider {
    private final String name;
    private final JavacardPlatform platform;
    public BrokenCard(String name) {
        Parameters.notNull("Name", name); //NOI18N
        this.name = name;
        this.platform = new BrokenJavacardPlatform(name);
    }

    public BrokenCard(String name, JavacardPlatform platform) {
        Parameters.notNull("Name", name); //NOI18N
        this.name = name;
        this.platform = platform;
    }

    public Set<Integer> getPortsInUse() {
        return Collections.emptySet();
    }

    public Set<Integer> getPortsInActiveUse() {
        return getPortsInUse();
    }

    public boolean isValid() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == BrokenCard.class && ((BrokenCard) o).name.equals(name);
    }

    @Override
    public int hashCode() {
        return 23 * name.hashCode();
    }

    @SuppressWarnings("unchecked")
    public <T extends ICardCapability> T getCapability(Class<T> type) {
        return (T) (type.equals(CardInfo.class) ? this : null);
    }

    @SuppressWarnings("unchecked") //NOI18N
    public Set<Class<? extends ICardCapability>> getSupportedCapabilities() {
        return toSet(CardInfo.class);
    }

    @SuppressWarnings("unchecked") //NOI18N
    public Set<Class<? extends ICardCapability>> getEnabledCapabilities() {
        return toSet(CardInfo.class);
    }

    public boolean isCapabilityEnabled(Class<? extends ICardCapability> type) {
        return CardInfo.class.equals(type) ? true : false;
    }

    public boolean isCapabilitySupported(Class<? extends ICardCapability> type) {
        return isCapabilityEnabled(type);
    }

    public String getSystemId() {
        return name;
    }

    public Lookup getLookup() {
        return Lookups.singleton(this);
    }

    public Image getIcon() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    private static Set<Class<? extends ICardCapability>> toSet(Class<? extends ICardCapability>... types) {
        List<Class<? extends ICardCapability>> l = Arrays.asList(types);
        return new HashSet<Class<? extends ICardCapability>>(l);
    }

    public CardState getState() {
        return CardState.NEW;
    }

    public void addCardStateObserver(CardStateObserver obs) {
        //do nothing
    }

    public void removeCardStateObserver(CardStateObserver obs) {
        //do nothing
    }

    public JavacardPlatform getPlatform() {
        return platform;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(BrokenCard.class, "INVALID_CARD", name); //NOI18N
    }

    public Set<Class<? extends ICardCapability>> getSupportedCapabilityTypes() {
        Set<Class<? extends ICardCapability>> result = new HashSet<Class<? extends ICardCapability>>(1);
        result.add(CardInfo.class);
        return result;
    }
}
