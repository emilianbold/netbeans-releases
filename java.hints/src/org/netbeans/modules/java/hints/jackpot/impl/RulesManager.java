/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.spi.ClassPathBasedHintProvider;
import org.netbeans.modules.java.hints.jackpot.spi.ElementBasedHintProvider;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata;
import org.netbeans.modules.java.hints.jackpot.spi.HintProvider;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 *
 * @author lahvac
 */
public class RulesManager {

    public final Map<HintMetadata, Collection<? extends HintDescription>> allHints = new HashMap<HintMetadata, Collection<? extends HintDescription>>();

    private static final RulesManager INSTANCE = new RulesManager();

    public static RulesManager getInstance() {
        return INSTANCE;
    }

    private RulesManager() {
        reload();
    }
    
    public void reload() {
        for (HintProvider p : Lookup.getDefault().lookupAll(HintProvider.class)) {
            Map<HintMetadata, ? extends Collection<? extends HintDescription>> pHints = p.computeHints();

            allHints.putAll(pHints);
        }
    }

    public static void computeElementBasedHintsXXX(final CompilationInfo info, AtomicBoolean cancel, final List<HintDescription> outHints) {
        computeElementBasedHintsXXX(info, cancel, Lookup.getDefault().lookupAll(ElementBasedHintProvider.class), Lookup.getDefault().lookupAll(ClassPathBasedHintProvider.class), outHints);
    }

    public static void computeElementBasedHintsXXX(final CompilationInfo info, AtomicBoolean cancel, final Collection<? extends ElementBasedHintProvider> providers, final Collection<? extends ClassPathBasedHintProvider> cpBasedProviders, final List<HintDescription> outHints) {
        for (ElementBasedHintProvider provider : providers) {
            outHints.addAll(provider.computeHints(info));
        }

        ClasspathInfo cpInfo = info.getClasspathInfo();
        List<ClassPath> cps = new LinkedList<ClassPath>();
        
        cps.add(cpInfo.getClassPath(PathKind.BOOT));
        cps.add(cpInfo.getClassPath(PathKind.COMPILE));
        cps.add(cpInfo.getClassPath(PathKind.SOURCE));

        ClassPath compound = ClassPathSupport.createProxyClassPath(cps.toArray(new ClassPath[0]));

        for (ClassPathBasedHintProvider p : cpBasedProviders) {
            outHints.addAll(p.computeHints(compound));
        }

    }

    /** Gets preferences node, which stores the options for given hint. 
     * The preferences node is created
     * by calling <code>NbPreferences.forModule(this.getClass()).node(profile).node(getId());</code>
     * @param hintId id of the hint
     * @param profile Profile to get the node for. May be null for current profile
     * @return Preferences node for given hint.
     */
    //XXX: move to HintsSettings
    public static Preferences getPreferences(String hintId, String profile) {
        Map<String, Preferences> override = HintsSettings.getPreferencesOverride();

        if (override != null) {
            Preferences p = override.get(hintId);

            if (p != null) {
                return p;
            }
        }

        profile = profile == null ? HintsSettings.getCurrentProfileId() : profile;
        return NbPreferences.forModule(RulesManager.class).node(profile).node(hintId);
    }
}
