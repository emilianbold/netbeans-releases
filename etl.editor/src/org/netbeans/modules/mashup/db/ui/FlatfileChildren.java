/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.mashup.db.ui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * List of children of a containing node. Remember to document what your permitted keys
 * are!
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class FlatfileChildren extends Children.Keys {

    /** Optional holder for the keys, to be used when changing them dynamically. */
    protected List myKeys;

    /** Constructs default instance of FlatfileChildren */
    public FlatfileChildren() {
        myKeys = null;
    }

    /** AddNotify */
    protected void addNotify() {
        super.addNotify();
        if (myKeys != null) {
            return;
        }
        myKeys = new LinkedList();
        // add whatever keys you need
        setKeys(myKeys);
    }

    /** Remove Notify */
    protected void removeNotify() {
        myKeys = null;
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }

    /**
     * Create Node
     * 
     * @param key key
     * @return array of nodes
     */
    protected Node[] createNodes(Object key) {
        return null;
    }

    /**
     * Optional accessor method for the keys, for use by the container node or maybe
     * subclasses.
     */
    /*
     * protected addKey(Object newKey) { // Make sure some keys already exist:
     * addNotify(); myKeys.add(newKey); // Ensure that the node(s) is displayed:
     * refreshKey(newKey); }
     */

    /**
     * Optional accessor method for keys, for use by the container node or maybe
     * subclasses.
     */
    /*
     * protected void setKeys(Collection keys) { myKeys = new LinkedList();
     * myKeys.addAll(keys); super.setKeys(keys); }
     */

    // Could also write e.g. removeKey to be used by the nodes in this children.
    // Or, could listen to changes in their status (NodeAdapter.nodeDestroyed)
    // and automatically remove them from the keys list here. Etc.
}

