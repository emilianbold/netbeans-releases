/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.jaxb.util;

//import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
//import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author gpatil
 */
public class JavaSourceUtil {
    public static CancellableTask createMarshalMethod(final boolean marshal, final String arg1, final String arg2){
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = null;
                
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        clazz = (ClassTree) typeDecl;
                        break;
                    }
                }

//                ModifiersTree methodModifiers = make.Modifiers(
//                        Collections.<Modifier>singleton(Modifier.PRIVATE),
//                        Collections.<AnnotationTree>emptyList()
//                        );
                // create method.      
                MethodTree newMethod = null;
                if (marshal) {
                    newMethod = getMarshalMethod(make, arg1, arg2);
                } else {
                    newMethod = getUnmarshalMethod(make, arg1, arg2);
                }
                ClassTree modifiedClazz = make.addClassMember(clazz, newMethod);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
            
            public void cancel() {
                //... cancel code
            }
        };
        
        return task;
    }
    
    
    private static MethodTree getMarshalMethod(TreeMaker make, String arg1, String arg2) throws IOException {        
        MethodTree method = make.Method(
                make.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE)),
                // XXX TODO get unique method name.
                "marshal", //NO I18N
                make.PrimitiveType(TypeKind.BOOLEAN),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                getMarshalMethodBody(arg1, arg2),
                null);
        return method;
    }

    private static MethodTree getUnmarshalMethod(TreeMaker make, String arg1, String arg2) throws IOException {        
        MethodTree method = make.Method(
                make.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE)),
                // XXX TODO get unique method name.                
                "unmarshal", //NO I18N
                make.PrimitiveType(TypeKind.BOOLEAN),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                getUnmarshalMethodBody(arg1, arg2),
                null);        
        return method;
    }
    
    private static String getMarshalMethodBody(String arg1, String arg2){
        String fileName = (arg2 == null) ? "File name here" : arg2 ;        
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");  
        sb.append("  Object obj = null;\n"); // No I18N
        sb.append("  try {\n"); // No I18N
        sb.append("      JAXBContext jaxbctx = JAXBContext.newInstance(\""); // No I18N
        sb.append(arg1); 
        sb.append("\"\n"); 
        sb.append("      Marshaller marshaller = jaxbctx.createMarshaller();\n"); // No I18N
        sb.append("      marshaller.marshal(new File(\""); // No I18N
        sb.append(arg2); 
        sb.append("\");\n"); 
        sb.append("  } catch (JAXBException e) {\n"); // No I18N
        sb.append("      e.printStackTrace();\n"); // No I18N
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }
    
    private static String getUnmarshalMethodBody(String arg1, String arg2){
        String fileName = arg2 == null ? "File name here" : arg2;        
        StringBuffer sb = new StringBuffer();
        sb.append( "{Object obj = null;\n" ); // No I18N
        sb.append( "try {\n" ); // No I18N
        sb.append( "JAXBContext jaxbctx = JAXBContext.newInstance(\"" +  // No I18N
                    arg1 + "\");\n" ); // No I18N
        sb.append( "Unmarshaller unmarshaller = jaxbctx.createUnmarshaller();\n" ); // No I18N
        sb.append( "obj = (Object) unmarshaller.unmarshal(new File(\"" +  // No I18N
                    fileName + "\"));\n" ); // No I18N
        sb.append( "} catch (JAXBException e) {\n" ); // No I18N
        sb.append( "            // TODO handle exception \n"); // No I18N
        sb.append( "        e.printStackTrace();\n" ); // No I18N
        sb.append( "}}" );
        
        return sb.toString();
    }
    
}
