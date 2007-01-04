/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package modeldump;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import modelutils.FileCodeModel;
import modelutils.FileCodeModelDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.modelimpl.csm.ClassForwardDeclarationImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FieldImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TypedefImpl;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDirectiveImpl;
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
        String absolutePath = file.getAbsolutePath();
        ArrayList<FileCodeModelDeclaration> declarations = new ArrayList<FileCodeModelDeclaration>();
        
        List declarationList = file.getDeclarations();
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
        String declPositionStr = new String("" + declPosition.getLine() + ":" + declPosition.getColumn());
        
        String declarationString = "";
        
        // TODO: Is it OK?

        if (declaration instanceof FunctionImpl) {
            FunctionImpl functionDeclaration = (FunctionImpl)declaration;
            List<ParameterImpl> params = functionDeclaration.getParameters();

            StringBuffer paramsStr = new StringBuffer();

            for (Iterator<ParameterImpl> j = params.iterator(); j.hasNext();) {
                ParameterImpl param = j.next();
                FileCodeModelDeclaration paramDecl = getFileCodeModelDeclaration(param);
                paramsStr.append(paramDecl.getDeclaration());
                
                if (j.hasNext()) {
                    paramsStr.append(", ");
                }
                
                result.addChild(paramDecl);
            }
            
            declarationString = functionDeclaration.getReturnType().getText() + " " + functionDeclaration.getQualifiedName() + "(" + paramsStr.toString() + ")";
        } else if (declaration instanceof VariableImpl) {
            VariableImpl variable = (VariableImpl)declaration;
            CsmType type = variable.getType();
            
            if (type != null) {
                declarationString = type.getText() + " ";
            }
            
            declarationString += variable.getQualifiedName();
        } else if (TypedefImpl.class.equals(declaration.getClass())) {
            TypedefImpl typedefImpl = (TypedefImpl)declaration;
            declarationString = typedefImpl.getType().getText() + " " + typedefImpl.getQualifiedName().toString();
        } else {
            declarationString = declaration.getQualifiedName();
        }
        
        result.setDeclarationString(declarationString);
        result.setDeclarationPosition(declPositionStr);
        
        return result;
    }    
}
