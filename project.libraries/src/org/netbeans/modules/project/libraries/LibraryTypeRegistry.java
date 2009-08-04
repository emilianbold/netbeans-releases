/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.project.libraries;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

public final class LibraryTypeRegistry {

    private static final String REGISTRY = "org-netbeans-api-project-libraries/LibraryTypeProviders";              //NOI18N
    
    private static LibraryTypeRegistry instance;

    private final Lookup.Result<LibraryTypeProvider> result;
    private final ChangeSupport changeSupport;
    private volatile Set<? extends LibraryTypeProvider> usedLibraryTypes;

    private LibraryTypeRegistry () {
        this.changeSupport = new ChangeSupport(this);
        final Lookup lookup = Lookups.forPath(REGISTRY);
        assert lookup != null;
        result = lookup.lookupResult(LibraryTypeProvider.class);
        result.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                changeSupport.fireChange();
            }
        });
    }

    public LibraryTypeProvider[] getLibraryTypeProviders () {
        assert result != null;
        final Collection<? extends LibraryTypeProvider> instances = result.allInstances();        
        return instances.toArray(new LibraryTypeProvider[instances.size()]);
    }

    public LibraryTypeProvider getLibraryTypeProvider (String libraryType) {
        assert libraryType != null;
        final LibraryTypeProvider[] providers = getLibraryTypeProviders();
        for (LibraryTypeProvider provider : providers) {
            if (libraryType.equals(provider.getLibraryType())) {
                return provider;
            }
        }
        return null;
    }

    public boolean hasChanged() {
        final Set<? extends LibraryTypeProvider> oldTP = usedLibraryTypes;
        if (oldTP == null) {
            return true;
        }
        final LibraryTypeProvider[] providers = getLibraryTypeProviders();
        final Map<LibraryTypeProvider,LibraryTypeProvider> newTP = new IdentityHashMap<LibraryTypeProvider,LibraryTypeProvider>();
        for (LibraryTypeProvider provider : providers) {
            newTP.put(provider, provider);
        }
        usedLibraryTypes = newTP.keySet();
        return oldTP.equals(newTP.keySet());
    }

    public void addChangeListener (final ChangeListener listener) {
        assert listener != null;
        this.changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener (final ChangeListener listener) {
        assert listener != null;
        this.changeSupport.removeChangeListener(listener);
    }
    

    public static synchronized LibraryTypeRegistry getDefault () {
        if (instance == null) {
            instance = new LibraryTypeRegistry();
        }
        return instance;
    }

}
