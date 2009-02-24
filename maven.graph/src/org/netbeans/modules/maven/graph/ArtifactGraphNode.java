/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.maven.graph;
import java.util.HashSet;
import java.util.Set;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Milos Kleint 
 */
public class ArtifactGraphNode {

    public static final int UNMANAGED = 0;
    public static final int MANAGED = 1;
    public static final int OVERRIDES_MANAGED = 2;

    private DependencyNode artifact;
    //for the layout
    double locX;
    double locY;
    double dispX;
    double dispY;
    private boolean fixed;
    private Widget widget;
    
    private boolean root;
    private HashSet<DependencyNode> dupl;
    private int level;
    private int managedState = UNMANAGED;

    /** Creates a new instance of ArtifactGraphNode */
    public ArtifactGraphNode(DependencyNode art) {
        artifact = art;
        dupl = new HashSet<DependencyNode>();
    }
    
    
    DependencyNode getArtifact() {
        return artifact;
    }
    
    void setArtifact(DependencyNode ar) {
        artifact = ar;
    }

    void addDuplicateOrConflict(DependencyNode nd) {
        dupl.add(nd);
    }

    Set<DependencyNode> getDuplicatesOrConflicts() {
        return dupl;
    }

    boolean represents(DependencyNode node) {
        if (artifact.equals(node)) {
            return true;
        }
        for (DependencyNode nd : dupl) {
            if (nd.equals(node)) {
                return true;
            }
        }
        return false;
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
    
    public boolean isVisible() {
        return widget != null ? widget.isVisible() : true;
    }

    void setPrimaryLevel(int i) {
        level = i;
    }
    
    public int getPrimaryLevel() {
        return level;
    }
    
    void setWidget(Widget wid) {
        widget = wid;
    }

    public int getManagedState() {
        return managedState;
    }

    public void setManagedState(int state) {
        this.managedState = state;
    }

}
