/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.module.graph;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.openide.util.Parameters;
import org.netbeans.modules.java.graph.GraphNodeImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
final class ModuleNode implements GraphNodeImplementation {
    private final String moduleName;
    private final boolean unnamed;
    private List<ModuleNode> children;
    private ModuleNode parent;
    private final String toolTipText;
    
    ModuleNode(
        @NonNull final String moduleName,
        final boolean unnamed,
        @NonNull final FileObject moduleInfo) {
        Parameters.notNull("moduleNode", moduleName);
        this.moduleName = moduleName;
        this.unnamed = unnamed;
        this.toolTipText = unnamed ?
                getUnnamedModuleToolTip(moduleInfo) :
                null;
        assert !unnamed || moduleName.isEmpty();
    }

    @NonNull
    @Override
    public String getName() {
        return unnamed ?
                NbBundle.getMessage(ModuleNode.class, "LBL_UnnamedModule") :
                moduleName;
    }
    
    boolean isUnnamed() {
        return unnamed;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ModuleNode)) {
            return false;
        }
        final ModuleNode otherNode = (ModuleNode) other;
        return moduleName == null ?
                otherNode.moduleName == null :
                moduleName.equals(otherNode.moduleName);
    }

    @Override
    public int hashCode() {
        return moduleName == null ?
            0:
            moduleName.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Module: %s", getName());  //NOI18N
    }

    @Override
    public synchronized List<ModuleNode> getChildren() {
        return children != null ? Collections.unmodifiableList(children) : null;
    }

    @Override
    public GraphNodeImplementation getParent() {
        return parent;
    }

    @Override
    public String getTooltipText() {
        return toolTipText != null ?
                toolTipText :
                getName();
    }

    @Override
    public String getQualifiedName() {
        return getName();        
    }

    synchronized void addChild(ModuleNode child) {
        if(children == null) {
            children = new LinkedList<>();
        }
        children.add(child);
    }

    void setParent(ModuleNode parent) {
        this.parent = parent;
    }
    
    @NonNull
    private static String getUnnamedModuleToolTip(@NonNull final FileObject moduleInfo) {
        return Optional.ofNullable(ClassPath.getClassPath(moduleInfo, JavaClassPathConstants.MODULE_CLASS_PATH))
                .map((cp) -> {
                    return cp.entries().stream()
                            .map((e) -> FileUtil.archiveOrDirForURL(e.getURL()))
                            .filter((f) -> f != null)
                            .map((f) -> f.getName())
                            .collect(Collectors.joining(File.pathSeparator));
                })
                .orElse("");    //NOI18N
    }
}
