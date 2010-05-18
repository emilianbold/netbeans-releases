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
package org.openide.filesystems;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.openide.filesystems.declmime.MIMEResolverImpl;
import org.openide.util.Parameters;

/**
 * This class is intended as superclass for individual resolvers.
 * All registered subclasses of MIMEResolver are looked up and asked one by one
 * to resolve MIME type of passed FileObject. Resolving is finished right after
 * a resolver is able to resolve the FileObject or if all registered
 * resolvers returned null (not recognized).
 * <p>
 * Resolvers are registered if they have their record in the Lookup area.
 * E.g. in form : org-some-package-JavaResolver.instance file.
 * <p>
 * MIME resolvers can also be registered in the <code>Services/MIMEResolver</code>
 * folder as <code>*.xml</code> files obeying a <a href="doc-files/HOWTO-MIME.html">certain format</a>.
 * These will be interpreted before resolvers in lookup (in the order specified in that folder).
 *
 * @author  rmatous
 */
public abstract class MIMEResolver {

    private String[] resolvableMIMETypes = null;

    /** Creates a new MIMEResolver.
     * @param mimeTypes an array of MIME types which can be resolved by this resolver.
     * It should contain all MIME types which {@link #findMIMEType} can return.
     * If something is missing, this resolver can be ignored, when searching for that
     * missing MIME type (see {@link FileUtil#getMIMEType(FileObject, String...)}).
     * @since 7.13
     */
    public MIMEResolver(String... mimeTypes) {
        Parameters.notNull("mimeTypes", mimeTypes);  //NOI18N
        if(mimeTypes.length == 0) {
            throw new IllegalArgumentException("The mimeTypes parameter cannot be empty array.");  //NOI18N
        }
        for (String mimeType : mimeTypes) {
            if(mimeType == null || mimeType.length() == 0) {
                throw new IllegalArgumentException("The item in mimeTypes parameter cannot be null nor empty String.");  //NOI18N
            }
        }
        resolvableMIMETypes = mimeTypes;
    }

    /** Creates a new MIMEResolver. 
     * @deprecated Use {@link #MIMEResolver(String...)} instead. Declaring MIME
     * types which can only be resolved by this resolver helps to speed up IDE.
     */
    @Deprecated
    public MIMEResolver() {
    }

    /**
     * Resolves FileObject and returns recognized MIME type
     * @param fo is FileObject which should be resolved (This FileObject is not
     * thread safe. Also this FileObject should not be cached for later use)
     * @return  recognized MIME type or null if not recognized
     */
    public abstract String findMIMEType(FileObject fo);
    
    /** Returns an array of MIME types which can be resolved by this resolver.
     * @return a non-empty array of MIME types
     */
    String[] getMIMETypes() {
        return resolvableMIMETypes;
    }

    /** Internal support for implementors of MIME resolver UIs. 
     * 
     * @since 7.34
     */
    public abstract class UIHelpers {

        /** Throws an exception. Allows instantiation only by known subclasses.
         * @throws IllegalStateException
         */
        protected UIHelpers() {
            if (getClass().getName().equals("org.netbeans.core.ui.options.filetypes.FileAssociationsModel")) { // NOI18N
                // only core.ui is allowed to use methods of this class.
                return;
            }
            throw new IllegalStateException();
        }

        /**
         * Stores declarative resolver corresponding to specified mapping of MIME type
         * and set of extensions. This resolver has the highest priority. Usually
         * it resides in userdir/config/Servicer/MIMEResolver.
         * <p><strong>Not intended for use by modules outside the NetBeans Platform.</strong>
         * @param mimeToExtensions mapping of MIME type to set of extensions like
         * {@code {image/jpeg=[jpg, jpeg], image/gif=[]}}
         * @since org.openide.filesystems 7.34
         */
        protected final void storeUserDefinedResolver(final Map<String, Set<String>> mimeToExtensions) {
            MIMEResolverImpl.storeUserDefinedResolver(mimeToExtensions);
        }

        /**
         * Lists registered MIMEResolver instances in reverse order,
         * i.e. first are ones with lower priority (position attribute higher)
         * and last are ones with highest prority (position attribute lower).
         * <p><strong>Not intended for use by modules outside the NetBeans Platform.</strong>
         * @return list of all registered MIME resolver definitions in reverse order
         * @since org.openide.filesystems 7.34
         */
        protected final Collection<? extends FileObject> getOrderedResolvers() {
            return MIMEResolverImpl.getOrderedResolvers();
        }

        /**
         * Checks whether a given resolver is user-defined.
         * <p><strong>Not intended for use by modules outside the NetBeans Platform.</strong>
         * @param mimeResolverFO resolver definition
         * @return true if the specified file is a user-defined MIME resolver, false otherwise
         * @since org.openide.filesystems 7.34
         */
        protected final boolean isUserDefined(FileObject mimeResolverFO) {
            return MIMEResolverImpl.isUserDefined(mimeResolverFO);
        }

        /**
         * Returns mapping of MIME type to set of extensions.
         * <p><strong>Not intended for use by modules outside the NetBeans Platform.</strong>
         * @param fo MIMEResolver definition
         * @return mapping of MIME type to set of extensions like
         * {@code {image/jpeg=[jpg, jpeg], image/gif=[]}} (never null but may be empty)
         * @since org.openide.filesystems 7.34
         */
        protected final Map<String, Set<String>> getMIMEToExtensions(FileObject fo) {
            return MIMEResolverImpl.getMIMEToExtensions(fo);
        }
    }

}
