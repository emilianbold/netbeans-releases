/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.osgi.customizer;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.ComboBoxModel;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.PackageView;

/**
 *
 * @author dafe
 */
public final class InstructionsConverter {

    private static final String DELIMITER = ",";

    public static final Integer EXPORT_PACKAGE = 1;
    public static final Integer PRIVATE_PACKAGE = 2;

    public static Map<Integer, String> computeExportInstructions (Map<String, Boolean> items) {
        Map<Integer, String> instructionsMap = new HashMap<Integer, String>(2);
        StringBuilder exportIns = new StringBuilder();
        int mapSize = items.size();
        int counter = 0;
        for (Map.Entry<String, Boolean> entry : items.entrySet()) {
            if (entry.getValue()) {
                exportIns.append(entry.getKey());
                if (counter < mapSize - 1) {
                    exportIns.append(DELIMITER);
                }
            }
            counter++;
        }

        instructionsMap.put(EXPORT_PACKAGE, exportIns.toString());
        instructionsMap.put(PRIVATE_PACKAGE, "*");

        return instructionsMap;
    }

    public static Map<String, Boolean> computeExportList (Map<Integer, String> exportInstructions, Project project) {
        StringTokenizer strTok = new StringTokenizer(exportInstructions.get(EXPORT_PACKAGE), DELIMITER);
        Map<String, Boolean> pkgMap = new HashMap<String, Boolean>();
        
        SourceGroup[] groups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (groups != null && groups.length > 0) {
            ComboBoxModel cbm = PackageView.createListView(groups[0]);
            for (int i = 0; i < cbm.getSize(); i++) {
                pkgMap.put(cbm.getElementAt(i).toString(), Boolean.FALSE);
            }
        }

        while(strTok.hasMoreTokens()) {
            String cur = strTok.nextToken();
            pkgMap.remove(cur);
            pkgMap.put(cur, Boolean.TRUE);
        }

        return pkgMap;
    }

    public static String computeEmbedInstruction (Map<String, Boolean> items) {
        // TBD
        return null;
    }

    public static Map<String, Boolean> computeEmbedList (String embedInstruction) {
        // TBD
        return null;
    }


}
