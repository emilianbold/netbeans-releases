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

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import org.netbeans.modules.openide.filesystems.declmime.MIMEResolverImpl;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 * Use the {@link #findMIMEType(org.openide.filesystems.FileObject)} to invoke
 * the mime resolving infrastructure.
 * All registered mime resolvers are looked up and asked one by one
 * to resolve MIME type of passed in {@link FileObject}. Resolving is finished right after
 * a resolver is able to resolve the FileObject or if all registered
 * resolvers returned <code>null</code> (not recognized).
 * <p>
 * Use {@link ExtensionRegistration}, 
 * {@link MIMEResolver.NamespaceRegistration} or {@link MIMEResolver.Registration}
 * to register declarative resolvers. 
 * <p>
 * In the rarely case, when declarative resolvers are not sufficient, you can
 * register subclass of {@link MIMEResolver} directly by using {@link ServiceProvider}
 * annotation.
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
    
    /** factory method for {@link MIMEResolver.Registration} */
    static MIMEResolver create(FileObject fo) throws IOException {
        byte[] arr = (byte[]) fo.getAttribute("bytes"); // NOI18N
        if (arr != null) {
            return MIMEResolverImpl.forStream(arr);
        }
        String mimeType = (String)fo.getAttribute("mimeType"); // NOI18N
        List<String> exts = new ArrayList<String>();
        int cnt = 0;
        for (;;) {
            String ext = (String) fo.getAttribute("ext." + cnt++); // NOI18N
            if (ext == null) {
                break;
            }
            exts.add(ext);
        }
        if (!exts.isEmpty()) {
            return MIMEResolverImpl.forExts(mimeType, exts);
        }
        throw new IllegalArgumentException("" + fo); // NOI18N
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

    /** Often a mime type can be deduced just by looking at a file extension.
     * If that is your case, this annotation is for you. It associates
     * extension(s) with provided mime type.
     * @since 7.58
     */
    @Retention(RetentionPolicy.SOURCE)
    public @interface ExtensionRegistration {
        /** Display name to present this type of objects to the user.
         */
        public String displayName();
        /** Mime type to be assigned to files with {@link #extension}.
         */
        public String mimeType();
        /** One or few extensions that should be recognized as given
         * {@link #mimeType}.
         */
        public String[] extension();
        /** In case ordering of mime resolvers is important, one can 
         * specify it by defining their {@link LayerBuilder#position() position}.
         */        
        public int position() default Integer.MAX_VALUE;
    }

    public @interface NamespaceRegistration {
        public String mimeType();
        public String[] namespace();
    }
    
    /** Registration that allows effective, declarative registration of 
     * complex {@link MIMEResolver mime resolvers}. The <code>value</code>
     * attribute of the annotation should be a relative reference to
     * an XML like <a href="doc-files/HOWTO-MIME.html">document</a> describing
     * the rules that will be interpreted by the mime recognizing infrastructure.
     * <pre>
     * {@code @}{@link NbBundle.Messages}({
     *    "NICE_NAME=Nice name!"
     * })
     * {@code @}MIMEResolver.Registration(
     *   displayName="#NICE_NAME"
     *   resource="<a href="doc-files/HOWTO-MIME.html">your-resolver-definition.xml</a>"
     * )
     * class AnyClassYouHave {
     *   // ...
     * }
     * </pre>
     * The definition is pre-processed during compile time in order to eliminate
     * XML parsing during execution.
     * 
     * @since 7.58
     */
    @Retention(RetentionPolicy.SOURCE)
    public @interface Registration {
        /** {@link LayerBuilder#absolutizeResource(javax.lang.model.element.Element, java.lang.String) Relative path} 
         * to resource XML file describing
         * the <a href="doc-files/HOWTO-MIME.html">mime recognition rules</a>.
         */
        public String resource();

        /** In case ordering of mime resolvers is important, one can 
         * specify it by defining their {@link LayerBuilder#position() position}.
         */        
        public int position() default Integer.MAX_VALUE;
        
        /** Display name to present this type of objects to the user.
         */
        public String displayName();
    }
}
