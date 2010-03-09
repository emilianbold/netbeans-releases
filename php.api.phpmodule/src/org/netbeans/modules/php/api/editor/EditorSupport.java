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

package org.netbeans.modules.php.api.editor;

import java.util.Collection;
import org.openide.filesystems.FileObject;

/**
 * Helper editor class that can be found in the default lookup.
 * @since 1.13
 * @author Tomas Mysik
 */
public interface EditorSupport {

    /**
     * Get {@link PhpClass PHP classes} from the given {@link FileObject file object}.
     * @param fo {@link FileObject file object} source file to investigate
     * @return collection of {@link PhpClass PHP classes}, never <code>null</code>
     */
    Collection<PhpClass> getClasses(FileObject fo);

    /**
     * Collects files containg the given {@link PhpClass PHP class}.
     * @param sourceRoot directory representing source root or test root
     * @param phpClass {@link PhpClass PHP class} to search for
     * @return collection of {@link FileObject file objects} containing the {@link PhpClass PHP class}, never <code>null</code>
     * @see #getClasses(FileObject)
     */
    Collection<FileObject> filesForClass(FileObject sourceRoot, PhpClass phpClass);

    /**
     * Get {@link PhpElement PHP element} for the given file and offset.
     * @param fo file to search in
     * @param offset offset in the file
     * @return {@link PhpElement PHP element}, can be <code>null</code> if not in any
     * @since 1.17
     */
    PhpBaseElement getElement(FileObject fo, int offset);
}
