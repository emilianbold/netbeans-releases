/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.spi.phpmodule;

import org.openide.filesystems.FileObject;

/**
 * Provides support for extending a PHP module's visibility, that is,
 * it allows to recommend to hide any file or folder (suitable mainly
 * for any kind of caches, private config files etc.).
 * <p>
 * Please note that such files/folders are not only recommended to be invisible
 * in different user views but are likely not e.g. scanned and indexed too.
 *
 * @author Tomas Mysik
 * @since 1.4
 * @see org.netbeans.api.queries.VisibilityQuery
 * @see org.netbeans.spi.queries.VisibilityQueryImplementation
 */
public abstract class PhpModuleVisibilityExtender {

    /**
     * Check whether a file is recommended to be visible.
     * @param fileObject a file which should be checked
     * @return <code>true</code> if it is recommended to show this file
     */
    public abstract boolean isVisible(FileObject fileObject);
}
