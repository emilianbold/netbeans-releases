/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;

/**
 *
 * @author Martin
 */
public class DOMBreakpoint extends AbstractBreakpoint {

    public enum Type {
        
        SUBTREE_MODIFIED(Debugger.DOM_BREAKPOINT_SUBTREE),
        ATTRIBUTE_MODIFIED(Debugger.DOM_BREAKPOINT_ATTRIBUTE),
        NODE_REMOVED(Debugger.DOM_BREAKPOINT_NODE);
        
        private String typeString;
        
        private Type(String typeString) {
            this.typeString = typeString;
        }
        
        public String getTypeString() {
            return typeString;
        }
    }
    
    private boolean onSubtreeModification;
    private boolean onAttributeModification;
    private boolean onNodeRemoval;
    private Set<Type> types;
    private final Node node;
    
    public DOMBreakpoint(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public boolean isOnSubtreeModification() {
        return onSubtreeModification;
    }

    public void setOnSubtreeModification(boolean onSubtreeModification) {
        this.onSubtreeModification = onSubtreeModification;
    }

    public boolean isOnAttributeModification() {
        return onAttributeModification;
    }

    public void setOnAttributeModification(boolean onAttributeModification) {
        this.onAttributeModification = onAttributeModification;
    }

    public boolean isOnNodeRemoval() {
        return onNodeRemoval;
    }

    public void setOnNodeRemoval(boolean onNodeRemoval) {
        this.onNodeRemoval = onNodeRemoval;
    }
    
    public synchronized Set<Type> getTypes() {
        if (types == null) {
            types = createTypes();
        }
        return types;
    }
    
    private Set<Type> createTypes() {
        Set<Type> ts = EnumSet.noneOf(Type.class);
        if (isOnSubtreeModification()) {
            ts.add(Type.SUBTREE_MODIFIED);
        }
        if (isOnAttributeModification()) {
            ts.add(Type.ATTRIBUTE_MODIFIED);
        }
        if (isOnNodeRemoval()) {
            ts.add(Type.NODE_REMOVED);
        }
        return Collections.unmodifiableSet(ts);
    }
}
