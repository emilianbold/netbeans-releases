/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.mixeddev.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.COMMA;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.LPAREN;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.RPAREN;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.stringize;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.transform;
import static org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport.createMethodInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaEntityInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public final class JNASupport {
    
    public static JavaEntityInfo getJNAEntity(Document doc, int offset) {
        return JavaContextSupport.resolveContext(doc, new ResolveJNAEntityTask(offset));
    }
    
    public static String getCppMethodSignature(JavaMethodInfo methodInfo) {
        if (methodInfo == null) {
            return null;
        }
        StringBuilder method = new StringBuilder();
        
        // Add method name
        method.append(methodInfo.getName());
        
        return method.toString();
    }
    
//<editor-fold defaultstate="collapsed" desc="Implementation">    
    private static class ResolveJNAEntityTask implements ResolveJavaContextTask<JavaEntityInfo> {
        
        private final int offset;
        
        private JavaEntityInfo result;
        
        public ResolveJNAEntityTask(int offset) {
            this.offset = offset;
        }
        
        @Override
        public boolean hasResult() {
            return result != null;
        }
        
        @Override
        public JavaEntityInfo getResult() {
            return result;
        }
        
        @Override
        public void cancel() {
            // Do nothing
        }
        
        @Override
        public void run(CompilationController controller) throws Exception {
            if (controller == null || controller.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                return;
            }
            // Looking for current element
            TreePath path = controller.getTreeUtilities().pathFor(offset);
            if (isMethod(path)) {
                TreePath parentPath = path.getParentPath();
                if (isInterface(parentPath)) {
                    if (findLibraryInterface(controller, parentPath)) {
                        result = createMethodInfo(controller, path);
                    }
                }
            }
        }
    }
    
    private static final String JNA_LIBRARY = "com.sun.jna.Library";
    
    private static boolean isMethod(TreePath path) {
        return path != null &&
            path.getLeaf().getKind() == Tree.Kind.METHOD;
    }
    
    private static boolean isClass(TreePath path) {
        return path != null && 
               path.getLeaf() != null &&
               path.getLeaf().getKind() == Tree.Kind.CLASS;
    }
    
    private static boolean isInterface(TreePath path) {
        return path != null && 
               path.getLeaf() != null &&
               path.getLeaf().getKind() == Tree.Kind.INTERFACE;
    }    
    
    private static boolean isClassOrInterface(TreePath path) {
        return isClass(path) || isInterface(path);
    }
    
    private static boolean findLibraryInterface(CompilationController controller, TreePath ifacePath) {
        TypeElement ifaceElement = (TypeElement) controller.getTrees().getElement(ifacePath);
        Set<String> handledEntities = new HashSet<String>();
        return findLibraryInterface(controller, ifaceElement, handledEntities);
    }
    
    private static boolean findLibraryInterface(CompilationController controller, TypeElement baseIfaceElement, Set<String> handled) {
        for (TypeMirror iface : baseIfaceElement.getInterfaces()) {
            if (iface.getKind() == TypeKind.DECLARED) {
                TypeElement ifaceElement = (TypeElement)((DeclaredType) iface).asElement();
                String qualifiedName = ifaceElement.getQualifiedName().toString();
                if (!handled.contains(qualifiedName)) {
                    handled.add(qualifiedName);
                    if (!JNA_LIBRARY.equals(qualifiedName)) {
                        return findLibraryInterface(controller, ifaceElement, handled);
                    } else {
                        return true;
                    }
                }
            }
        }
        return false; // this is not JNA Library        
    }
    
    private JNASupport() {
        throw new AssertionError("Not instantiable!");
    }
//</editor-fold>
}
