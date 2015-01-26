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
package org.netbeans.modules.cnd.spi.codemodel;

import java.net.URI;
import java.util.Collection;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMUnsavedFileImplementation;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public interface CMTranslationUnitImplementation {
    /**
     * \brief Retrieve a file handle within the given translation unit.
     *
     * @param file URI of the file.
     *
     * @return the file handle for the named file in the translation unit \p
     * tu, or a NULL file handle if the file was not a part of this translation
     * unit.
     */
    public CMFileImplementation getFile(URI file);

    /**
     * \brief Retrieves the source location associated with a given
     * file/line/column in a particular translation unit.
     * @param file
     * @param line
     * @param column
     * @return 
     */
    public CMSourceLocationImplementation getLocation(CMFileImplementation file, int line, int column);

    /**
     * \brief Retrieves the source location associated with a given character
     * offset in a particular translation unit.
     * @param file
     * @param offset
     * @return 
     */
    public CMSourceLocationImplementation getLocation(CMFileImplementation file, int offset);

    /**
     * \brief Map a source location to the cursor that describes the entity at
     * that location in the source code.
     *
     * getCursor() maps an arbitrary source location within a translation unit
     * down to the most specific cursor that describes the entity at that
     * location. For example, given an expression \c x + y, invoking
     * clang_getCursor() with a source location pointing to "x" will return the
     * cursor for "x"; similarly for "y". If the cursor points anywhere between
     * "x" or "y" (e.g., on the + or the whitespace around it),
     * clang_getCursor() will return a cursor referring to the "+" expression.
     *
     * @param loc source location
     * @return a cursor representing the entity at the given source location, or
     * a invalid NULL-cursor if no such entity can be found.
     */
    public CMCursorImplementation getCursor(CMSourceLocationImplementation loc);
    public CMCursorImplementation getCursor(CMFileImplementation impl, int offset);

    /**
     * \brief Retrieve the complete set of diagnostics associated with a
     * translation unit.
     *
     * @return
     */
    public CMDiagnosticSetImplementation getDiagnostics();

    /**
     * \brief Retrieve the cursor that represents the given translation unit.
     *
     * @return The translation unit cursor can be used to start traversing the
     * various declarations within the given translation unit.
     */
    public CMCursorImplementation getRootCursor();

    /**
     * @return Get the original translation unit source file path.
     */
    public CharSequence getMainFilePath();
    
    /**
     * @return Get compile args
     */
    public String[] getCompileArgs();

    /**
     * @return index this translation unit was created with
     */
    public CMIndexImplementation getIndex();
    
    /**
     * Retrieve range for the whole file
     * 
     * @param uri
     * @return range
     */
    public CMSourceRangeImplementation getRangeForFile(URI uri);
    
    /**
     * Dispose this translation unit and free all dependent memory
     */
    public void dispose();
    
    /**
     * Reparse this translation unit using the same compile args that were used for initial parse
     * @param unsaved  collection of unsaved files
     * @return 0 on success
     */
    public int reparse(Collection<CMUnsavedFileImplementation> unsaved);
    
    /**
     * @return unsaved files used to create this translation unit
     */
    public Collection<CMUnsavedFileImplementation> getUnsavedFiles();
}
