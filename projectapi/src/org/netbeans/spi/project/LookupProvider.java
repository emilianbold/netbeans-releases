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

package org.netbeans.spi.project;

import org.openide.util.Lookup;

/**
 * interface for inclusion of 3rd party content in project's lookup. Typically, if the 
 * project type allows composition of lookup from multiple sources, it will make a layer
 * location public where 3rd parties will register implementations of this interface.
 * @author mkleint
 * @since org.netbeans.modules.projectapi 1.12
 */
public interface LookupProvider {
    
    /**
     * implementations will be asked to create their additional project lookup based on the baseContext
     * passed as parameter. The content of baseLookup is undefined on this level, is a contract
     * of the actual project type. Can be complete lookup of the project type, a portion of it or
     * something completely different that won't appear in the final project lookup.
     * Each implementation is only asked once for it's lookup for a given project instance at the time 
     * when project's lookup is being created.
     * @param baseContext implementation shall decide what to return for a given project instance based on context
     *  passed in.
     * @return a {@link org.openide.util.Lookup} instance that is to be added to the project's lookup, never null.
     */ 
    Lookup createAdditionalLookup(Lookup baseContext);

    /**
     * annotation to register LookupProvider instances.
     * @since org.netbeans.modules.projectapi 1.21
     */
    public @interface Register {
        /**
         * token(s) denoting one or more project types, eg. org-netbeans-modules-maven or org-netbeans-modules-java-j2seproject
         * @return
         */
        String[] projectType();
    }

}
