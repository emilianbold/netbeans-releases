/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.indent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author vita
 */
public final class ProxyPreferences extends AbstractPreferences {

    public ProxyPreferences(Preferences... delegates) {
        this("", null, delegates); //NOI18N
    }

    @Override
    protected void putSpi(String key, String value) {
        delegates[0].put(key, value);
    }

    @Override
    protected String getSpi(String key) {
        for(Preferences d : delegates) {
            String value = d.get(key, null);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    protected void removeSpi(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        Set<String> keys = new HashSet<String>();
        for(Preferences d : delegates) {
            keys.addAll(Arrays.asList(d.keys()));
        }
        return keys.toArray(new String[ keys.size() ]);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        Set<String> names = new HashSet<String>();
        for(Preferences d : delegates) {
            names.addAll(Arrays.asList(d.childrenNames()));
        }
        return names.toArray(new String[ names.size() ]);
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        Preferences [] nueDelegates = new Preferences[delegates.length];
        for(int i = 0; i < delegates.length; i++) {
            nueDelegates[i] = delegates[i].node(name);
        }
        return new ProxyPreferences(name, this, nueDelegates);
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
        delegates[0].sync();
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        delegates[0].flush();
    }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private final Preferences [] delegates;

    private ProxyPreferences(String name, ProxyPreferences parent, Preferences... delegates) {
        super(parent, name); //NOI18N
        assert delegates.length > 0 : "There must be at least one delegate"; //NOI18N
        this.delegates = delegates;
    }
}
