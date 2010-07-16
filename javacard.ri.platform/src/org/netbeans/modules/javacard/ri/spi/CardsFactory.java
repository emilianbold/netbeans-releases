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

package org.netbeans.modules.javacard.ri.spi;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.javacard.common.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.spi.Cards;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Factory for instances of Cards that provide cards for a given Java Card Platform
 * kind.  Instances of CardsFactory are registered in the default lookup (via
 * a module layer XML file, typically), in a folder corresponding to the platform
 * kind they work with.  The platform kind in is specified in a Java Card SDK's
 * <code>platform.properties</code> file.  For instance, if you wanted to
 * register a CardsFactory that will create Card instances for all platforms
 * which say their kind is <code>MyCard</code>, you would implement CardsFactory
 * and register an instance
 * of it in <code>org-netbeans-modules-javacard-spi/kinds/MyCard</code>.
 * <p/>
 * Using this interface, plus the support for SDKs which wrapper functionality
 * from the Java Card RI, you can have an SDK which reuses much of the functionality
 * of the RI (i.e. you do not need to implement JavacardPlatform yourself), but,
 * simply by specifying a different platform kind, provides its own support for
 * detecting attached cards and allowing the user to deploy to them.
 *
 * @author Tim Boudreau
 */
public abstract class CardsFactory {
    private Map<String, Reference<Cards>> cache = new HashMap<String, Reference<Cards>>();
    private final Object lock = new Object();
    public Cards getCards (FileObject source) {
        Cards result;
        synchronized(lock) {
            Reference<Cards> r = null;
            Set<String> toRemove = null;
            for (Map.Entry<String, Reference<Cards>> e : cache.entrySet()) {
                Cards c = e.getValue().get();
                if (c == null) {
                    if (toRemove == null) {
                        toRemove = new HashSet<String>();
                    }
                    toRemove.add(e.getKey());
                } else if (source.getPath().equals(e.getKey())) {
                    r = e.getValue();
                }
            }
            result = r == null ? null : r.get();
            if (result == null) {
                try {
                    DataObject dob = DataObject.find(source);
                    result = createCards (dob);
                    if (result != null) {
                        r = new WeakReference<Cards>(result);
                        cache.put(source.getPath(), r);
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return result;
    }

    /**
     * Create an instance of Cards for the passed DataObject.
     * @param source A DataObject for a file representing a Java Card Platform
     * whose getPlatformKind() returns the value this factory is registered for.
     * @return An instance of Cards or null
     */
    protected abstract Cards createCards (Lookup.Provider source);

    /**
     * Find an instance of CardsFactory for the specified platform kind,
     * as specified in the key <code>javacard.platform.kind</code> in that
     * platform's platform.properties file
     * @param platformKind The platform kind
     * @return A CardsFactory
     */
    public static CardsFactory find (String platformKind) {
        Lookup lkp = Lookups.forPath(CommonSystemFilesystemPaths.SFS_ADD_HANDLER_REGISTRATION_ROOT + platformKind);
        return lkp.lookup(CardsFactory.class);
    }
}
