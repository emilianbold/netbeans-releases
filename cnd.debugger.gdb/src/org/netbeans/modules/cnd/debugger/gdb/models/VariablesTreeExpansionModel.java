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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Copied from Java Debugger
 * @author Egor Ushakov
 */
public class VariablesTreeExpansionModel implements TreeExpansionModel {

    // CND debugger support re-creates all variables each step
    // so we can not use weakset here
    private Set<String> expandedLocalsNodes = new HashSet<String>();
    private Set<String> collapsedLocalsNodes = new HashSet<String>();

    // we recreate nodes each time
    // so it is neccessary to store locals and watches seprately
    private Set<String> expandedWatchNodes = new HashSet<String>();
    private Set<String> collapsedWatchNodes = new HashSet<String>();

    private Set<String> getNodeExpandCollection(AbstractVariable var) {
        if (var instanceof GdbWatchVariable) {
            return expandedWatchNodes;
        }
        if (var instanceof AbstractVariable.AbstractField) {
            return getNodeExpandCollection(((AbstractVariable.AbstractField)var).getAncestor());
        }
        return expandedLocalsNodes;
    }

    private Set<String> getNodeCollapseCollection(AbstractVariable var) {
        if (var instanceof GdbWatchVariable) {
            return collapsedWatchNodes;
        }
        if (var instanceof AbstractVariable.AbstractField) {
            return getNodeCollapseCollection(((AbstractVariable.AbstractField)var).getAncestor());
        }
        return collapsedLocalsNodes;
    }

    /**
     * Defines default state (collapsed, expanded) of given node.
     *
     * @param node a node
     * @return default state (collapsed, expanded) of given node
     */
    @Override
    public boolean isExpanded(Object node)
    throws UnknownTypeException {
        if (!(node instanceof AbstractVariable)) {
            throw new UnknownTypeException(node);
        }
        AbstractVariable var = (AbstractVariable)node;
        synchronized (this) {
            if (getNodeExpandCollection(var).contains(var.getFullName(true))) {
                return true;
            }
            if (getNodeCollapseCollection(var).contains(var.getFullName(true))) {
                return false;
            }
        }
        return false;
    }

    /**
     * Called when given node is expanded.
     *
     * @param node a expanded node
     */
    @Override
    public void nodeExpanded(Object node) {
        if (!(node instanceof AbstractVariable)) {
            return;
        }
        AbstractVariable var = (AbstractVariable)node;
        synchronized (this) {
            getNodeExpandCollection(var).add(var.getFullName(true));
            getNodeCollapseCollection(var).remove(var.getFullName(true));
        }
    }

    /**
     * Called when given node is collapsed.
     *
     * @param node a collapsed node
     */
    @Override
    public void nodeCollapsed(Object node) {
        if (!(node instanceof AbstractVariable)) {
            return;
        }
        AbstractVariable var = (AbstractVariable)node;
        synchronized (this) {
            getNodeCollapseCollection(var).add(var.getFullName(true));
            getNodeExpandCollection(var).remove(var.getFullName(true));
        }
    }
}
