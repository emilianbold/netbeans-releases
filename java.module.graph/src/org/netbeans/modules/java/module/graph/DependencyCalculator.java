/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.module.graph;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import javax.lang.model.element.ModuleElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class DependencyCalculator {

    private final FileObject moduleInfo;
    private Collection<? extends ModuleNode> nodes;
    private Collection<? extends DependencyEdge> edges;

    public DependencyCalculator(
        @NonNull final FileObject moduleInfo) {
        Parameters.notNull("moduleInfo", moduleInfo);
        this.moduleInfo = moduleInfo;
    }

    @NonNull
    Collection<? extends ModuleNode> getNodes() {
        init();
        assert nodes != null;
        return nodes;
    }

    @NonNull
    Collection<? extends DependencyEdge> getEdges() {
        init();
        assert edges != null;
        return edges;
    }

    private void init() {
        if (nodes == null) {
            assert edges == null;
            nodes = Collections.emptyList();
            edges = Collections.emptyList();
            final JavaSource js = JavaSource.forFileObject(moduleInfo);
            if (js != null) {
                try {
                    js.runUserActionTask((cc)-> {
                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        final List<? extends Tree> decls = cc.getCompilationUnit().getTypeDecls();
                        final ModuleElement me =  !decls.isEmpty() && decls.get(0).getKind() == Tree.Kind.MODULE ?
                                (ModuleElement) cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), decls.get(0))) :
                                null;
                        if (me != null) {
                            final Collection<ModuleNode> mods = new LinkedHashSet<>();
                            final Collection<DependencyEdge> deps = new ArrayList<>();
                            collect(me, mods, deps,0);
                            nodes = mods;
                            edges = deps;
                        }
                    }, true);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
    }

    private ModuleNode collect(
        @NonNull final ModuleElement me,
        @NonNull final Collection<? super ModuleNode> mods,
        @NonNull final Collection<? super DependencyEdge> deps,
        final int currentDepth) {
        final ModuleNode meNode = new ModuleNode(
                me.getQualifiedName().toString(),
                currentDepth);
        final boolean unseen = mods.add(meNode);
        if (unseen && !me.isUnnamed()) {
            for (ModuleElement.Directive d : me.getDirectives()) {
                if (d.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                    final ModuleElement.RequiresDirective reqD = (ModuleElement.RequiresDirective) d;
                    final ModuleNode targetNode = collect(
                        reqD.getDependency(),
                        mods,
                        deps,
                        currentDepth+1);
                    deps.add(new DependencyEdge(meNode, targetNode, reqD.isPublic()));
                }
            }
        }
        return meNode;
    }
}
