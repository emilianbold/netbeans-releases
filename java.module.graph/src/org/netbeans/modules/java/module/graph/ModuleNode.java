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
final class ModuleNode {
    private final String moduleName;
    private final int depth;
    private boolean fixed;
    double locX, locY, dispX, dispY; // for use from FruchtermanReingoldLayout

    ModuleNode(
        @NonNull final String moduleName,
        final int depth) {
        Parameters.notNull("moduleNode", moduleName);
        this.moduleName = moduleName;
        this.depth = depth;
    }

    @NonNull
    String getName() {
        return moduleName;
    }

    void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    boolean isFixed() {
        return fixed;
    }

    int getDepth() {
        return depth;
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
}
