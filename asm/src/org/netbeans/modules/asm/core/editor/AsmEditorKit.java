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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.asm.core.editor;

import java.io.StringReader;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Syntax;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.NbEditorKit;
import org.openide.util.Lookup;

import org.netbeans.modules.asm.model.AsmSyntaxProvider;
import org.netbeans.modules.asm.model.AsmModel;
import org.netbeans.modules.asm.model.AsmModelAccessor;
import org.netbeans.modules.asm.model.AsmModelProvider;
import org.netbeans.modules.asm.model.AsmSyntax;
import org.netbeans.modules.asm.model.AsmTypesProvider;
import org.netbeans.modules.asm.core.dataobjects.AsmObjectUtilities;


public class AsmEditorKit extends NbEditorKit {

    public static final String MIME_TYPE = "text/x-asm"; // NOI18N
    
    @Override
    public Syntax createSyntax(Document doc) {
        AsmModelAccessor acc = (AsmModelAccessor) doc.getProperty(AsmModelAccessor.class);

        if (acc == null) {
            
            AsmModelProvider modelProv = null;
            AsmSyntaxProvider syntProv = null;
            
            Collection<? extends AsmTypesProvider> idents = 
                 Lookup.getDefault().lookup(new Lookup.Template<AsmTypesProvider>(AsmTypesProvider.class)).allInstances();

            AsmTypesProvider.ResolverResult res = null;
                       
            String text = AsmObjectUtilities.getText(NbEditorUtilities.getFileObject(doc));
                      
            for (AsmTypesProvider ident : idents) {
                res = ident.resolve(new StringReader(text));
                if (res != null) {
                    modelProv = res.getModelProvider();
                    syntProv = res.getSyntaxProvider();

                    Logger.getLogger(AsmEditorKit.class.getName()).
                        log(Level.FINE, "Asm Regognized " + modelProv + " " + syntProv); // NOI18N
                }                                
            }

            if (res == null ||  modelProv  == null || syntProv == null) {
                return new EditorSyntax();
            }
            
            AsmModel model = modelProv.getModel();
            AsmSyntax synt = syntProv.getSyntax(model);
                                 
            acc = new AsmModelAccessorImpl(model, synt, doc);

            doc.putProperty(AsmModelAccessor.class, acc);
            doc.putProperty(AsmModel.class, model);                   
            doc.putProperty(Language.class, new AsmLanguageHierarchy(synt).language());                        
        }

        return new EditorSyntax();
    }

    @Override
    public String getContentType() {
        return MIME_TYPE;
    }

    @Override
    public void install(JEditorPane jEditorPane) {
        super.install(jEditorPane);
               
    }

    @Override
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {      
        return super.createSyntaxSupport(doc);
    }
}
