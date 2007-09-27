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


package org.netbeans.modules.iep.model.lib;

import javax.swing.tree.MutableTreeNode;


/**
 * This class provides a tree node for PdsComponenTypes and PdsComponenType.
 *
 * @author Bing Lu
 *
 * @since May 31, 2002
 */
class TcgComponentTypeTreeNode
    extends ListMapTreeNode {

    /**
     * Constructor
     *
     * @param listMap
     * @param userKey
     * @param userObject
     * @param allowsChildren
     *
     * @see org.netbeans.modules.iep.editor.tcg.util.ListMapTreeNode
     */
    public TcgComponentTypeTreeNode(ListMap listMap, Object userKey,
                                 Object userObject, boolean allowsChildren) {
        super(listMap, userKey, userObject, allowsChildren);
    }

    /*
     *  Override ListMapTreeNode's method to prevent objects of wrong type from being set
     */

    /**
     * DOCUMENT ME!
     *
     * @param newParent parent
     *
     * @todo Document: Setter for Parent attribute of the TcgComponentTypeTreeNode
     *       object
     */
    public void setParent(MutableTreeNode newParent) {

        if (newParent instanceof TcgComponentTypeTreeNode) {
            super.setParent(newParent);
        }
    }

    /*
     *  Override ListMapTreeNode's method to prevent objects of wrong type from being added
     */

    /**
     * DOCUMENT ME!
     *
     * @param child
     *
     * @todo Document this method
     */
    public void add(MutableTreeNode child) {

        if (child instanceof TcgComponentTypeTreeNode) {
            super.add((TcgComponentTypeTreeNode) child);
        }
    }

    /*
     *  Override ListMapTreeNode's method to prevent objects of wrong type from being added
     */

    /**
     * DOCUMENT ME!
     *
     * @param child
     * @param index
     *
     * @todo Document this method
     */
    public void insert(MutableTreeNode child, int index) {

        if (child instanceof TcgComponentTypeTreeNode) {
            super.insert(child, index);
        }
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
