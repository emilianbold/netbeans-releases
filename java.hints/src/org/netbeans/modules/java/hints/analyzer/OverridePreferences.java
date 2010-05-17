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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.analyzer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author lahvac
 */
public class OverridePreferences extends AbstractPreferences {
    
    private final Preferences delegateTo;
    private final Map<String, String> data;
    private final Set<String> removed;
    
    public OverridePreferences(Preferences delegateTo) {
        super(null, "");
        this.delegateTo = delegateTo;
        this.data = new HashMap<String, String>();
        this.removed = new HashSet<String>();
    }

    protected void putSpi(String key, String value) {
        data.put(key, value);
        removed.remove(key);
    }

    protected String getSpi(String key) {
        if (data.containsKey(key)) {
            return data.get(key);
        } else {
            if (removed.contains(key)) {
                return null;
            } else {
                return delegateTo.get(key, null);
            }
        }
    }

    protected void removeSpi(String key) {
        data.remove(key);
        removed.add(key);
    }

    protected void removeNodeSpi() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected String[] keysSpi() throws BackingStoreException {
        Set<String> keys = new HashSet<String>(Arrays.asList(delegateTo.keys()));
        
        keys.removeAll(removed);
        keys.addAll(data.keySet());

        return keys.toArray(new String[0]);
    }

    protected String[] childrenNamesSpi() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected AbstractPreferences childSpi(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void syncSpi() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void flushSpi() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
