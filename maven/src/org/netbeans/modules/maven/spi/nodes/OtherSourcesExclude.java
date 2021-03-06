/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.spi.nodes;

import java.nio.file.Path;
import java.util.Set;

/**
 * Extension point to tell the Other sources project subnode to exclude the given folder name in
 * src/main and src/test. It is assumed that implementing this interface is associated 
 * with implementation of a NodeFactory providing special handling for such folder.
 * To be registered in project lookup.
 * Related to {@link JavaLikeRootProvider} but only handling the Other sources node display.
 * 
 * Note: the api is a bit simplistic for performance reasons, eg. if user reconfigures the src/main/webapp content to a different location in pom.xml we silently assume that
 * src/main/webapp doesn't exist then.
 * @author mkleint
 * @since 2.88
 */
public interface OtherSourcesExclude {

    /**
     * Paths of sub-folders of src/main and src/test that should be excluded in Other
     * Sources node.
     *
     * @since 2.106
     *
     * @return {@link Path}s of sub-folders which should be excluded
     */
    Set<Path> excludedFolders();
}
