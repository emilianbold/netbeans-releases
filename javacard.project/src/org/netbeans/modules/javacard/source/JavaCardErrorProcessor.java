package org.netbeans.modules.javacard.source;

import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.util.AbstractTypeProcessor;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;

/**
 *
 * @author lahvac
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions(JavaCardErrorProcessor.ENABLE_OPTION)
public class JavaCardErrorProcessor extends AbstractTypeProcessor {

    static final String ENABLE_OPTION = "netbeans_private_enable_javacard_processor";
    private static final Set<TypeKind> UNSUPPORTED_PRIMITIVES_CLASSIC_PROJECTS =
            EnumSet.of (TypeKind.DOUBLE, TypeKind.FLOAT, TypeKind.LONG, TypeKind.CHAR);
    private static final Set<TypeKind> UNSUPPORTED_PRIMITIVES_EXTENDED_PROJECTS =
            EnumSet.of (TypeKind.DOUBLE, TypeKind.FLOAT);


    private ProcessingEnvironment env;

    @Override
    public void init(ProcessingEnvironment env) {
        this.env = env;
    }

    @Override
    public void typeProcess(TypeElement te, TreePath tp) {
        //enable before committing:
//        if (!env.getOptions().containsKey(ENABLE_OPTION)) return;
        Logger.getLogger(JavaCardErrorProcessor.class.getName()).log(Level.FINE, "invoked on: {0}", te.getQualifiedName());
        new ScannerImpl().scan(tp, null);
    }

    private class ScannerImpl extends TreePathScanner<Void, Void> {
        @Override
        public Void visitPrimitiveType(PrimitiveTypeTree node, Void p) {
            if (UNSUPPORTED_PRIMITIVES_CLASSIC_PROJECTS.contains(node.getPrimitiveTypeKind())) {
                Trees.instance(env).printMessage(Kind.ERROR, "Cannot use " + node.getPrimitiveTypeKind() + " in javacard", node, getCurrentPath().getCompilationUnit());
            }
            return super.visitPrimitiveType(node, p);
        }
    }

}
