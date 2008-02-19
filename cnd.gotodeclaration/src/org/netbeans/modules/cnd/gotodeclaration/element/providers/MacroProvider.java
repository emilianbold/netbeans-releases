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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementProvider;
import org.netbeans.modules.cnd.gotodeclaration.util.NameMatcher;
import org.openide.util.NbBundle;

/**
 *
 * @author vk155633
 */
public class MacroProvider extends BaseProvider implements ElementProvider {

private static class MacroDelegate extends ProviderDelegate {


        public String name() {
            return "C/C++ Macros"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(MacroProvider.class, "MACRO_PROVIDER_DISPLAY_NAME"); // NOI18N
        }

        protected void processProject(CsmProject project, ResultSet result, NameMatcher comparator) {
            if( TRACE ) System.err.printf("MacroProvider.processProject %s\n", project.getName());
            processFiles(project.getAllFiles(), result, comparator);
        }

        private void processFiles(Collection<CsmFile> files, ResultSet result, NameMatcher comparator) {
            for( CsmFile file : files ) {
                if( isCancelled() ) {
                    return;
                }
                for( CsmMacro macro : file.getMacros() ) {
                    if( isCancelled() ) {
                        return;
                    }
                    if( comparator.matches(macro.getName().toString()) ) {
                        result.add(new MacroElementDescriptor(macro));
                    }
                }
            }
        }


    }

    @Override
    protected ProviderDelegate createDelegate() {
        return new MacroDelegate();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MacroProvider.class, "MACRO_PROVIDER_DISPLAY_NAME"); // NOI18N
    }

    public String name() {
        return "C/C++ Macros"; // NOI18N
    }
    

    
}
