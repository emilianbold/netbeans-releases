/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package modeldump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import modelutils.FileCodeModel;
import modelutils.FileCodeModelDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TypedefImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariableImpl;
//import org.openide.util.NotImplementedException;

/**
 *
 * @author ak119685
 */
public class FileCodeModelReader {
    
    /** Creates a new instance of FileCodeModelReader */
    public FileCodeModelReader() {
    }
    
    public FileCodeModel getModelFor(CsmFile file) {
        String absolutePath = file.getAbsolutePath().toString();
        ArrayList<FileCodeModelDeclaration> declarations = new ArrayList<FileCodeModelDeclaration>();
        
        Collection declarationList = file.getDeclarations();
        for (Iterator<CsmDeclaration> i = declarationList.iterator(); i.hasNext();) {
            declarations.add(getFileCodeModelDeclaration(i.next()));
        }
        
        FileCodeModel cm = new FileCodeModel(absolutePath, declarations);
        return cm;
    }
    
    public static FileCodeModelDeclaration getFileCodeModelDeclaration(CsmDeclaration declaration) {
        FileCodeModelDeclaration result = new FileCodeModelDeclaration();
        result.setKind(declaration.getKind().toString());
        
        Position declPosition = ((CsmOffsetable)declaration).getStartPosition();
        String declPositionStr = new String("" + declPosition.getLine() + ":" + declPosition.getColumn()); // NOI18N
        
        String declarationString = "";
        
        // TODO: Is it OK?

        if (declaration instanceof FunctionImpl) {
            FunctionImpl functionDeclaration = (FunctionImpl)declaration;
            Collection<ParameterImpl> params = functionDeclaration.getParameters();

            StringBuffer paramsStr = new StringBuffer();

            for (Iterator<ParameterImpl> j = params.iterator(); j.hasNext();) {
                ParameterImpl param = j.next();
                FileCodeModelDeclaration paramDecl = getFileCodeModelDeclaration(param);
                paramsStr.append(paramDecl.getDeclaration());
                
                if (j.hasNext()) {
                    paramsStr.append(", "); // NOI18N
                }
                
                result.addChild(paramDecl);
            }
            
            declarationString = functionDeclaration.getReturnType().getText() + " " + functionDeclaration.getQualifiedName() + "(" + paramsStr.toString() + ")"; // NOI18N
        } else if (declaration instanceof VariableImpl) {
            VariableImpl variable = (VariableImpl)declaration;
            CsmType type = variable.getType();
            
            if (type != null) {
                declarationString = type.getText() + " "; // NOI18N
            }
            
            declarationString += variable.getQualifiedName();
        } else if (TypedefImpl.class.equals(declaration.getClass())) {
            TypedefImpl typedefImpl = (TypedefImpl)declaration;
            declarationString = typedefImpl.getType().getText() + " " + typedefImpl.getQualifiedName().toString(); // NOI18N
        } else {
            declarationString = declaration.getQualifiedName().toString();
        }
        
        result.setDeclarationString(declarationString);
        result.setDeclarationPosition(declPositionStr);
        
        return result;
    }    
}
