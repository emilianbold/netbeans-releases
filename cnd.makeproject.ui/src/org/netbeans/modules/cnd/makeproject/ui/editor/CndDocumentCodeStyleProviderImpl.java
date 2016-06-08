/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.ui.editor;

import java.util.ArrayList;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.makeproject.api.CodeStyleWrapper;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectLookupProvider;
import org.netbeans.modules.cnd.spi.CndDocumentCodeStyleProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author alsimon
 */
final class CndDocumentCodeStyleProviderImpl implements CndDocumentCodeStyleProvider {
    
    @ServiceProvider(service = MakeProjectLookupProvider.class)
    public static class CndDocumentCodeStyleProviderFactory implements MakeProjectLookupProvider {

        @Override
        public void addLookup(MakeProject owner, ArrayList<Object> ic) {
            ic.add(new CndDocumentCodeStyleProviderImpl(owner));
        }
    }
    private final MakeProject project;

    private CndDocumentCodeStyleProviderImpl(MakeProject owner) {
        project = owner;
    }

    @Override
    public String getCurrentCodeStyle(String mimeType, Document doc) {
        if (project.isProjectFormattingStyle()) {
            CodeStyleWrapper style = project.getProjectFormattingStyle(mimeType);
            if (style != null) {
                return style.getStyleId();
            }
        }
        return null;
    }
    
}
