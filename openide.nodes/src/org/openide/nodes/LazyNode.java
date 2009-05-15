/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.openide.nodes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.Action;

/** Lazy delegating node.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class LazyNode extends FilterNode {
    private Map<String,?> map;

    LazyNode(Map<String,?> map) {
        this(new ChFactory(), map);
    }
    private LazyNode(ChFactory factory, Map<String,?> map) {
        super(new AbstractNode(Children.create(factory, true)));
        factory.node = this;
        this.map = map;
        
        AbstractNode an = (AbstractNode)getOriginal();

        an.setName((String) map.get("name")); // NOI18N
        an.setDisplayName((String) map.get("displayName")); // NOI18N
        an.setShortDescription((String) map.get("shortDescription")); // NOI18N
        String iconBase = (String) map.get("iconResource"); // NOI18N
        if (iconBase != null) {
            an.setIconBaseWithExtension(iconBase);
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        return switchToOriginal().getActions(context);
    }

    final Node switchToOriginal() {
        final Node[] n = new Node[]{null};
        synchronized (this) {
            if (map == null) {
                return getOriginal();
            }
            n[0] = (Node)map.get("original"); // NOI18N
            if (n[0] == null) {
                throw new IllegalArgumentException("Original Node from map " + map + " is null");
            }
            map = null;
        }
        Children.MUTEX.postWriteRequest(new Runnable() {

            public void run() {
                changeOriginal(n[0], true);
            }
        });
        return n[0];
    }

    private static final class ChFactory extends ChildFactory<Object> {
        LazyNode node;

        @Override
        protected boolean createKeys(List<Object> toPopulate) {
            LazyNode n = node;
            node = null;
            if (n != null) {
                n.switchToOriginal();
                return true;
            } else {
                return false;
            }
        }
    }
}
