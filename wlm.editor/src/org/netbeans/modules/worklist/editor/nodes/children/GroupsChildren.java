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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.nodes.children;

import org.netbeans.modules.worklist.editor.nodes.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.wlm.model.api.AssignmentHolder;
import org.netbeans.modules.wlm.model.api.Group;
import org.netbeans.modules.wlm.model.api.TAssignment;
import org.netbeans.modules.wlm.model.api.TExcluded;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author anjeleevich
 */
public class GroupsChildren extends WLMChildren<AssignmentHolder> {

    public GroupsChildren(AssignmentHolder component, Lookup lookup) {
        super(component, lookup);
    }

    private List<Group> getAllGroups() {
        AssignmentHolder holder = getWLMComponent();
        TAssignment assignment = holder.getAssignment();

        ArrayList<Group> result = new ArrayList<Group>();

        List<Group> includedGroups = null;
        List<Group> excludedGroups = null;

        if (assignment != null) {
            includedGroups = assignment.getGroups();

            TExcluded excluded = assignment.getExcluded();
            if (excluded != null) {
                excludedGroups = excluded.getGroups();
            }
        }

        if (includedGroups != null) {
            result.addAll(includedGroups);
        }

        if (excludedGroups != null) {
            result.addAll(excludedGroups);
        }

        return result;
    }

    @Override
    protected Collection<? extends WLMComponent> createKeys() {
//        AssignmentHolder holder = getWLMComponent();
//
//        Collection<? extends WLMComponent> result = null;
//
//        TAssignment assignment = holder.getAssignment();
//        if (assignment != null) {
//            result = assignment.getGroups();
//        }
//
//        if (result == null) {
//            result = new ArrayList<WLMComponent>(0);
//        }
//
//        return result;

        return getAllGroups();
    }

    @Override
    protected Node[] createNodes(WLMComponent component) {
        if (component instanceof Group) {
            return new Node[] { new GroupNode((Group) component, getLookup()) };
        }
        
        return createDefaultNodes(component);
    }

    public WLMAcceptableDescendants getAcceptableDescendants() {
        return ACCEPTABLE_DESCENDANTS;
    }

    public static final WLMAcceptableDescendants ACCEPTABLE_DESCENDANTS
            = WLMAcceptableDescendants.create(WLMNodeType.GROUP);
}
