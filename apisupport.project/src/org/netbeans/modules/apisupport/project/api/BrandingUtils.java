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

package org.netbeans.modules.apisupport.project.api;

import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.customizer.BasicBrandingModel;
import org.netbeans.modules.apisupport.project.ui.customizer.BrandingEditor;
import org.openide.util.NbBundle;

/**
 * Utility class to expose NB platform application branding editor.
 *
 * @since 1.39
 * @author S. Aubrecht
 */
public class BrandingUtils {

    private BrandingUtils() {
    }

    /**
     * Opens branding editor for given project. Must be invoked from EDT.
     * @param displayName Editor's display name.
     * @param p Project to be branded.
     * @param brandingPath Path relative to project's directory where branded are stored in.
     */
    public static void openBrandingEditor( String displayName, Project p, String brandingPath ) {
        if( !SwingUtilities.isEventDispatchThread() ) {
            throw new IllegalStateException("This method must be invoked from EDT."); //NOI18N
        }
        boolean contextAvailable = true;
        NbModuleProvider moduleProvider = p.getLookup().lookup(NbModuleProvider.class);
        if( null != moduleProvider 
                && !moduleProvider.prepareContext(NbBundle.getMessage(BrandingUtils.class, "Lbl_BrandingEditor")) ) { //NOI18N
            contextAvailable = false;
        }
        BasicBrandingModel model = new BasicBrandingModel(p, brandingPath);
        BrandingEditor.open( displayName, p, model, contextAvailable );
    }
}
