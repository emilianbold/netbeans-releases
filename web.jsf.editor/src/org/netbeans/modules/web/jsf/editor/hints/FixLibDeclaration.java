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

package org.netbeans.modules.web.jsf.editor.hints;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrary;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class FixLibDeclaration implements HintFix{
    private String nsPrefix;
    private FaceletsLibrary lib;
    private Document doc;

    public FixLibDeclaration(Document doc, String nsPrefix, FaceletsLibrary lib) {
        this.doc = doc;
        this.nsPrefix = nsPrefix;
        this.lib = lib;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(FixLibDeclaration.class, "MSG_FixLibDeclaration", nsPrefix, lib.getNamespace());
    }

    @Override
    public void implement() throws Exception {
        JsfUtils.importLibrary(doc, lib, nsPrefix);
    }

    @Override
    public boolean isSafe() {
        return true; // hope so...
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    public static List<FaceletsLibrary> getLibsByPrefix(Document doc, String prefix){
        List<FaceletsLibrary> libs = new ArrayList<FaceletsLibrary>();
        JsfSupport sup = JsfSupport.findFor(doc);

        if (sup != null){
            for (FaceletsLibrary lib : sup.getFaceletsLibraries().values()){
                if (prefix.equals(lib.getDefaultPrefix())){
                    libs.add(lib);
                }
            }
        }

        return libs;
    }
}
