/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.module.graph;

import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class DependencyEdge {
    private final ModuleNode source;
    private final ModuleNode target;
    private final boolean pub;

    DependencyEdge(
            @NonNull final ModuleNode source,
            @NonNull final ModuleNode target,
            final boolean pubReq) {
        Parameters.notNull("source", source);   //NOI18N
        Parameters.notNull("target", target);   //NOI18N
        this.source = source;
        this.target = target;
        this.pub = pubReq;
    }

    @NonNull
    ModuleNode getSource() {
        return source;
    }

    @NonNull
    ModuleNode getTarget() {
        return target;
    }

    boolean isPublic() {
        return pub;
    }

}
