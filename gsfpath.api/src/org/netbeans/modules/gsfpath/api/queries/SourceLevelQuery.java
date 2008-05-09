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

package org.netbeans.modules.gsfpath.api.queries;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.gsfpath.spi.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Returns source level of the given Java source file if it is known.
 * @see org.netbeans.modules.gsfpath.spi.queries.SourceLevelQueryImplementation
 * @author David Konecny
 * @since org.netbeans.modules.gsfpath.api/1 1.5
 */
public class SourceLevelQuery {

    private static final Logger LOGGER = Logger.getLogger(SourceLevelQuery.class.getName());

    private static final Pattern SOURCE_LEVEL = Pattern.compile("\\d+\\.\\d+");

    private static final Lookup.Result<? extends SourceLevelQueryImplementation> implementations =
        Lookup.getDefault().lookupResult (SourceLevelQueryImplementation.class);

    private SourceLevelQuery() {
    }

    /**
     * Returns source level of the given Java file, Java package or source folder. For acceptable return values
     * see the documentation of <code>-source</code> command line switch of 
     * <code>javac</code> compiler .
     * @param javaFile Java source file, Java package or source folder in question
     * @return source level of the Java file, e.g. "1.3", "1.4" or "1.5", or null
     *     if it is not known
     */
    public static String getSourceLevel(FileObject javaFile) {
        for  (SourceLevelQueryImplementation sqi : implementations.allInstances()) {
            String s = sqi.getSourceLevel(javaFile);
            if (s != null) {
                if (!SOURCE_LEVEL.matcher(s).matches()) {
                    LOGGER.log(Level.WARNING, "#83994: Ignoring bogus source level {0} for {1} from {2}", new Object[] {s, javaFile, sqi});
                    continue;
                }
                LOGGER.log(Level.FINE, "Found source level {0} for {1} from {2}", new Object[] {s, javaFile, sqi});
                return s;
            }
        }
        LOGGER.log(Level.FINE, "No source level found for {0}", javaFile);
        return null;
    }

}
