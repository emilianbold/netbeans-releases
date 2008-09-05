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
package org.openide.filesystems;

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
     * @deprecated Use {@link MIMEResolver(String...)} instead. Declaring MIME
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
}
