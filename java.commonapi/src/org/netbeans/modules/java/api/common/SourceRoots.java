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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.api.common;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Represents a project source roots. It can be used to obtain source roots as Ant properties, {@link FileObject}'s
 * or {@link URL}s.
 * All implementations have to be thread safe and have to listen to the changes
 * in Ant project metadata (see {@link #PROP_ROOT_PROPERTIES}) as well as
 * in project properties (see {@link #PROP_ROOTS}).
 * @author Tomas Zezula, Tomas Mysik
 * @since org.netbeans.modules.java.api.common/0 1.0
 */
public interface SourceRoots {

    /**
     * Property name of a event that has to be fired when Ant project metadata change.
     */
    String PROP_ROOT_PROPERTIES = SourceRoots.class.getName() + ".rootProperties"; //NOI18N
    /**
     * Property name of a event that has to be fired when project properties change.
     */
    String PROP_ROOTS = SourceRoots.class.getName() + ".roots"; //NOI18N

    /**
     * Default label for sources node used in {@link org.netbeans.spi.project.ui.LogicalViewProvider}.
     */
    String DEFAULT_SOURCE_LABEL = NbBundle.getMessage(SourceRoots.class, "NAME_src.dir");
    /**
     * Default label for tests node used in {@link org.netbeans.spi.project.ui.LogicalViewProvider}.
     */
    String DEFAULT_TEST_LABEL = NbBundle.getMessage(SourceRoots.class, "NAME_test.src.dir");

    /**
     * Returns the display names of source roots.
     * The returned array has the same length as an array returned by the {@link #getRootProperties()}.
     * It may contain empty {@link String}s but not <code>null</code>.
     * @return an array of source roots names.
     */
    String[] getRootNames();

    /**
     * Returns names of Ant properties in the <i>project.properties</i> file holding the source roots.
     * @return an array of String.
     */
    String[] getRootProperties();

    /**
     * Returns the source roots in the form of absolute paths.
     * @return an array of {@link FileObject}s.
     */
    FileObject[] getRoots();

    /**
     * Returns the source roots as {@link URL}s.
     * @return an array of {@link URL}.
     */
    URL[] getRootURLs();

    /**
     * Adds {@link PropertyChangeListener}, see class description for more information
     * about listening to the source roots changes.
     * @param listener a listener to add.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes {@link PropertyChangeListener}, see class description for more information
     * about listening to the source roots changes.
     * @param listener a listener to remove.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Replaces the current roots by the given ones.
     * @param roots the {@link URL}s of the new roots.
     * @param labels the names of the new roots.
     */
    void putRoots(final URL[] roots, final String[] labels);

    /**
     * Translates root name into display name of source/test root.
     * @param rootName the name of root got from {@link SourceRoots#getRootNames()}.
     * @param propName the name of a property the root is stored in.
     * @return the label to be displayed.
     */
    String getRootDisplayName(String rootName, String propName);

    /**
     * Creates initial display name of source/test root.
     * @param sourceRoot the source root.
     * @return the label to be displayed.
     */
    String createInitialDisplayName(File sourceRoot);

    /**
     * Returns <code>true</code> if the current {@link SourceRoots} instance represents source roots belonging to
     * the test compilation unit.
     * @return boolean <code>true</code> if the instance belongs to the test compilation unit, false otherwise.
     */
    boolean isTest();
}
