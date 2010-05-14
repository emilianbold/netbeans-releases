/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.soa.palette.java.util;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeKind;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author gpatil
 */
public class JavaSourceUtil {
    public static JavacTreeModel createJavacTreeModel(JTextComponent target) {
        try {
            final JavacTreeModelImpl model = new JavacTreeModelImpl();
            JavaSource javaSource = JavaSource.forDocument(target.getDocument());
            javaSource.runUserActionTask(new CancellableTask<CompilationController> () {
                public void cancel() {}
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    model.setCompilationController(cc);
                    model.scan(cc.getCompilationUnit(), null);
                }
            }, true);
            
            return model;
        } catch (IOException ioe) {
            return null;
        }
    }

    public static String recommendMethodName(List<String> mns, String toFrom, 
            boolean jaxb, boolean marshal){
            StringBuffer sb = new StringBuffer();
            String ret;
            
            if (jaxb){
                sb.append("jaxb");//NOI18N
            }else{
                sb.append("otd");//NOI18N
            }
            
            if (marshal){
                sb.append("MarshalTo");//NOI18N
            }else{
                sb.append("UnmarshalFrom");//NOI18N
            }
            
            toFrom = toFrom.replace("[]", "ByteArray");//NOI18N
            
            sb.append(toFrom);
            
            ret = sb.toString();
            
            if (mns.contains(ret)){
                String tmp = ret;
                for (int i=0; i < 100; i++){
                    tmp = ret + i;
                    if (!mns.contains(tmp)){
                        ret = tmp;
                        break;
                    }
                }
            }
            
            return ret;
    }
    
    public static List<String> getMethodNames(JTextComponent src){
        List<String> ms = new ArrayList<String>();
        JavacTreeModel model = createJavacTreeModel(src);
        List<ExecutableElement> mt = model.getMethods();
        if (mt != null){
            for (ExecutableElement tree : mt){
                Name name = tree.getSimpleName();
                ms.add(name.toString());
            }
        }
        return ms;
    }
    
    public static String recommendConstructsMethodName(List<String> mns, boolean jaxb){
            StringBuffer sb = new StringBuffer();
            String ret;
            
            if (jaxb){
                sb.append("jaxb");//NOI18N
            }else{
                sb.append("otd");//NOI18N
            }
            
            sb.append("Constructs");  //NOI18N
            
            ret = sb.toString();
            
            if (mns.contains(ret)){
                String tmp = ret;
                for (int i=0; i < 100; i++){
                    tmp = ret + i;
                    if (!mns.contains(tmp)){
                        ret = tmp;
                        break;
                    }
                }
            }
            
            return ret;
    }
    
    public static String recommendMethodName(List<String> mns){
            StringBuffer sb = new StringBuffer();
            
            sb.append("mapper");//NOI18N
            
            String ret = sb.toString();
            
            if (mns.contains(ret)){
                String tmp = ret;
                for (int i=0; i < 100; i++){
                    tmp = ret + i;
                    if (!mns.contains(tmp)){
                        ret = tmp;
                        break;
                    }
                }
            }
            
            return ret;
    }
            
    public static Tree createType(TreeMaker make, WorkingCopy workingCopy, String typeName) throws Exception {
        TypeKind primitiveTypeKind = null;
        if ("void".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.VOID;
        } else if ("boolean".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.BOOLEAN;
        } else if ("byte".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.BYTE;
        } else if ("short".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.SHORT;
        } else if ("int".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.INT;
        } else if ("long".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.LONG;
        } else if ("char".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.CHAR;
        } else if ("float".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.FLOAT;
        } else if ("double".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.DOUBLE;
        }
        if (primitiveTypeKind != null) {
            return make.PrimitiveType(primitiveTypeKind);
        }
        Tree typeTree = createQualIdent(make, workingCopy, typeName);
        
        return typeTree;
    }
    
    public static IdentifierTree createQualIdent(TreeMaker make, WorkingCopy workingCopy, 
            String typeName) throws Exception {
        return make.Identifier(typeName);
    }
    
}
