/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.graph;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.netbeans.api.annotations.common.NonNull;
import static org.netbeans.modules.java.graph.DependencyGraphScene.VersionProvider.VERSION_CONFLICT;
import static org.netbeans.modules.java.graph.DependencyGraphScene.VersionProvider.VERSION_NO_CONFLICT;
import static org.netbeans.modules.java.graph.DependencyGraphScene.VersionProvider.VERSION_POTENTIAL_CONFLICT;
import org.openide.util.Parameters;

/**
 *
 * @author Milos Kleint 
 * @param <I> 
 */
public class GraphNode<I extends GraphNodeImplementation> {

    public static final int UNMANAGED = 0;
    public static final int MANAGED = 1;
    public static final int OVERRIDES_MANAGED = 2;

    private I dependencyNode, parentAfterFix;
    public double locX, locY, dispX, dispY; // for use from FruchtermanReingoldLayout
    private boolean fixed;
    private HashSet<I> dupl;
    private int level;
    private int managedState = UNMANAGED;

    /** 
     * Creates a new instance of GraphNode
     * @param dependencyNode 
     **/
    public GraphNode(@NonNull I dependencyNode) {
        Parameters.notNull("dependencyNode", dependencyNode);   //NOI18N
        this.dependencyNode = dependencyNode;
        dupl = new HashSet<>();
    }
        
    public I getDependencyNode() {
        return dependencyNode;
    }

    public String getTooltipText() {
        return dependencyNode.getTooltipText();
    }    
    
    public void addDuplicateOrConflict(I nd) {
        dupl.add(nd);
    }

    public Set<I> getDuplicatesOrConflicts() {
        return Collections.unmodifiableSet(dupl);
    }
    
    /** 
     * After changes in graph parent may change, so it's always better to
     * call this method instead of getDependencyNode().getParent()
     * 
     * @return 
     */
    public GraphNodeImplementation getParent() {
        if (parentAfterFix != null) {
            return parentAfterFix;
        }
        return dependencyNode.getParent();
    }

    public void setParent(I newParent) {
        parentAfterFix = newParent;
    }
    
    public void setDependencyNode(I ar) {        
        dependencyNode = ar;
    }

    public boolean isRoot() {
        return level == 0;
    }
    
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
    
    public boolean isFixed() {
        return fixed;
    }

    public void setPrimaryLevel(int i) {
        level = i;
    }
    
    public int getPrimaryLevel() {
        return level;
    }
    
    public int getManagedState() {
        return managedState;
    }

    public void setManagedState(int state) {
        this.managedState = state;
    }
    
    public int getConflictType(Function<I, Boolean> isConflict, BiFunction<I, I, Integer> compare) {
        int ret = VERSION_NO_CONFLICT;
        int result;
        for (I curDepN : dupl) {
            if (isConflict.apply(curDepN)) {
                result = compare.apply(dependencyNode, curDepN);
                if (result < 0) {
                    return VERSION_CONFLICT;
                }
                if (result > 0) {
                    ret = VERSION_POTENTIAL_CONFLICT;
                }
            }
        }
        return ret;
    }
    
    static int compareVersions (String v1, String v2) {
        String[] v1Elems = v1.split("\\.");
        String[] v2Elems = v2.split("\\.");
        for (int i = 0; i < Math.min(v1Elems.length, v2Elems.length); i++) {
            int res = v1Elems[i].compareTo(v2Elems[i]);
            if (res != 0) {
                return res;
            }
        }
        return v1Elems.length - v2Elems.length;
    }

    public boolean represents(I nd) {
        if (dependencyNode.equals(nd)) {
            return true;
        }
        for (I d : dupl) {
            if (nd.equals(d)) {
                return true;
            }
        }
        return false;
    }
    
}
