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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.lang.model.element.ModuleElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
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
                            ModuleNode node = new ModuleNode(me.getQualifiedName().toString(), 0);
                            mods.add(node);
                            collect(node, me, mods, deps, 0);
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

    private void collect(
        @NonNull ModuleNode meNode, 
        @NonNull ModuleElement me, 
        @NonNull Collection<ModuleNode> mods, 
        @NonNull Collection<? super DependencyEdge> deps, 
        final int currentDepth) {
        Collection<Dependency> ns = collect(
                me,
                mods,
                deps,
                currentDepth+1);
        for (Dependency d : ns) {
            meNode.addChild(d.node);
            d.node.setParent(meNode);
            deps.add(new DependencyEdge(meNode, d.node, d.reqD.isPublic()));
        }
    }
    
    private Collection<Dependency> collect(
        @NonNull final ModuleElement me,
        @NonNull final Collection<ModuleNode> mods,
        @NonNull final Collection<? super DependencyEdge> deps,
        final int currentDepth) {
        List<Dependency> dependencies = new LinkedList<>();
        if (!me.isUnnamed()) {
            for (ModuleElement.Directive d : me.getDirectives()) {
                if (d.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                    ModuleElement.RequiresDirective reqD = (ModuleElement.RequiresDirective) d;                 
                    ModuleNode n = new ModuleNode(reqD.getDependency().getQualifiedName().toString(), currentDepth);
                    boolean unseen = mods.add(n);
                    dependencies.add(new Dependency(n, unseen, reqD));
                }
            }
            
            for (Dependency d : dependencies) {
                if(d.unseen) {
                    collect(d.node, d.reqD.getDependency(), mods, deps, currentDepth);
                }
            }
        }
        return dependencies;
    }
    
    private static class Dependency {
        final ModuleNode node;
        final boolean unseen;
        final ModuleElement.RequiresDirective reqD;

        public Dependency(ModuleNode node, boolean unseen, ModuleElement.RequiresDirective reqD) {
            this.node = node;
            this.unseen = unseen;
            this.reqD = reqD;
        }
    }
    
}
