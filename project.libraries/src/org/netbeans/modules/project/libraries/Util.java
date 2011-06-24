/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.project.libraries;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.project.libraries.ui.ProxyLibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.NamedLibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public class Util {

    private static final Logger LOG = Logger.getLogger(Util.class.getName());
    private static Map<LibraryImplementation,FileObject> sources = new WeakHashMap<LibraryImplementation,FileObject>();

    private Util(){}

    public static @NonNull String getLocalizedName(LibraryImplementation impl) {
        if (supportsDisplayName(impl) && ((NamedLibraryImplementation)impl).getDisplayName() != null) {
            return ((NamedLibraryImplementation)impl).getDisplayName();
        }
        final FileObject src = sources.get(impl);
        if (src != null) {
            Object obj = src.getAttribute("displayName"); // NOI18N
            if (obj instanceof String) {
                return (String)obj;
            }
        }
        if (impl instanceof ProxyLibraryImplementation) {
            String proxiedName = getLocalizedName(((ProxyLibraryImplementation)impl).getOriginal());
            if (proxiedName != null) {
                return proxiedName;
            }
        }

        return getLocalizedString(impl.getLocalizingBundle(), impl.getName());
    }

    public static boolean supportsDisplayName(final @NonNull LibraryImplementation impl) {
        assert impl != null;
        if (impl instanceof ProxyLibraryImplementation) {
            return supportsDisplayName(((ProxyLibraryImplementation)impl).getOriginal());
        }
        return impl instanceof NamedLibraryImplementation;
    }

    public static @CheckForNull String getDisplayName (final @NonNull LibraryImplementation impl) {
        return supportsDisplayName(impl) ?
                ((NamedLibraryImplementation)impl).getDisplayName() :
                null;
    }

    public static boolean setDisplayName(
            final @NonNull LibraryImplementation impl,
            final @NullAllowed String name) {
        if (supportsDisplayName(impl)) {
            ((NamedLibraryImplementation)impl).setDisplayName(name);
            return true;

        } else {
            return false;
        }
    }

    public static void registerSource(
        final @NonNull LibraryImplementation impl,
        final @NonNull FileObject descriptorFile) {
        sources.put(impl, descriptorFile);
    }


    private static String getLocalizedString (
            final @NullAllowed String bundleResourceName,
            final @NullAllowed String key) {
        if (key == null) {
            return null;
        }
        if (bundleResourceName == null) {
            return key;
        }
        final ResourceBundle bundle;
        try {
            bundle = NbBundle.getBundle(bundleResourceName);
        } catch (MissingResourceException mre) {
            // Bundle should have existed.
            LOG.log(Level.INFO, "Wrong resource bundle", mre);      //NOI18N
            return key;
        }
        try {
            return bundle.getString (key);
        } catch (MissingResourceException mre) {
            // No problem, not specified.
            return key;
        }
    }
}
