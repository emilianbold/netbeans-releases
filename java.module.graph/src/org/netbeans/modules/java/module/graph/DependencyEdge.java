/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.module.graph;

import java.util.Objects;
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
    private final boolean trans;

    DependencyEdge(
            @NonNull final ModuleNode source,
            @NonNull final ModuleNode target,
            final boolean pubReq,
            final boolean trans) {
        Parameters.notNull("source", source);   //NOI18N
        Parameters.notNull("target", target);   //NOI18N
        this.source = source;
        this.target = target;
        this.pub = pubReq;
        this.trans = trans;
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
    
    boolean isTrasitive() {
        return trans;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DependencyEdge other = (DependencyEdge) obj;
        if (this.pub != other.pub) {
            return false;
        }
        if (this.trans != other.trans) {
            return false;
        }
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.target, other.target)) {
            return false;
        }
        return true;
    }

    
}
