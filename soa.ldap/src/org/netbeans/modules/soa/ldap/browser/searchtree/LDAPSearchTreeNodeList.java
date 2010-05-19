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

package org.netbeans.modules.soa.ldap.browser.searchtree;

import javax.naming.ldap.Rdn;

/**
 *
 * @author anjeleevich
 */
class LDAPSearchTreeNodeList {
    private LDAPSearchTreeNode[] nodes = null;
    private int size = 0;

    boolean contains(Rdn rdn) {
        return get(rdn) != null;
    }

    boolean contains(LDAPSearchTreeNode node) {
        return contains(node.getRdn());
    }

    LDAPSearchTreeNode get(Rdn rdn) {
        int n1 = 0;
        int n2 = size - 1;

        while (n1 <= n2) {
            int n = (n1 + n2) >>> 1;

            LDAPSearchTreeNode node = nodes[n];
            int cmp = node.getRdn().compareTo(rdn);

            if (cmp < 0) {
                n1 = n + 1;
            } else if (cmp > 0) {
                n2 = n - 1;
            } else {
                return node;
            }
        }

        return null;
    }

    /*
     * merges to lists
     * returns array of indeces
     */
    int[] addAll(LDAPSearchTreeNodeList list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Unable to add empty list");
        }

        int thisSize = size();
        int listSize = list.size();

        LDAPSearchTreeNode[] result = new LDAPSearchTreeNode[listSize
                + thisSize];
        int[] indeces = new int[listSize];

        if (thisSize == 0) {
            System.arraycopy(list.nodes, 0, result, 0, listSize);
            for (int i = 0; i < listSize; i++) {
                indeces[i] = i;
            }
        } else {
            // merges sorted arrays, arrays are not empty

            int thisIndex = 0;
            int listIndex = 0;

            LDAPSearchTreeNode thisNode = this.nodes[0];
            LDAPSearchTreeNode listNode = list.nodes[0];

            int resultIndex = 0;

            while (resultIndex < result.length) {
                int cmp = thisNode.compareTo(listNode);
                
                if (cmp == 0) {
                    throw new IllegalStateException(
                            "Lists contain the same element: "
                            + thisNode.getLDAPName() + ", "
                            + listNode.getLDAPName());
                }

                if (cmp < 0) {
                    result[resultIndex++] = thisNode;

                    thisIndex++;
                    if (thisIndex < thisSize) {
                        thisNode = this.nodes[thisIndex];
                    } else {
                        // this list nodes have been all prcessed
                        // let's copy argument list node to result

                        while (resultIndex < result.length) {
                            result[resultIndex] = list.nodes[listIndex];
                            indeces[listIndex] = resultIndex;
                            
                            resultIndex++;
                            listIndex++;
                        }
                    }
                } else {
                    result[resultIndex] = listNode;
                    indeces[listIndex] = resultIndex;

                    resultIndex++;
                    listIndex++;
                    
                    if (listIndex < listSize) {
                        listNode = list.nodes[listIndex];
                    } else {
                        // argument list have been processed
                        // copy this list nodes to result

                        while (resultIndex < result.length) {
                            result[resultIndex++] = this.nodes[thisIndex++];
                        }
                    }
                }
            }
        }

        this.nodes = result;
        this.size = result.length;

        return indeces;
    }

    void clean() {
        nodes = null;
        size = 0;
    }

    LDAPSearchTreeNode get(int i) {
        if (i >= size) {
            throw new IndexOutOfBoundsException();
        }
        
        return nodes[i];
    }

    int size() {
        return size;
    }

    boolean isEmpty() {
        return (size == 0);
    }

    int indexOf(LDAPSearchTreeNode node) {
        Rdn rdn = node.getRdn();

        int n1 = 0;
        int n2 = size - 1;

        while (n1 <= n2) {
            int n = (n1 + n2) >>> 1;

            LDAPSearchTreeNode midNode = nodes[n];
            int cmp = midNode.getRdn().compareTo(rdn);

            if (cmp < 0) {
                n1 = n + 1;
            } else if (cmp > 0) {
                n2 = n - 1;
            } else {
                return (node == midNode) ? n : -1;
            }
        }

        return -1;
    }

    void add(LDAPSearchTreeNode node) {
        Rdn rdn = node.getRdn();

        int n1 = 0;
        int n2 = size - 1;

        while (n1 <= n2) {
            int n = (n1 + n2) >>> 1;

            int cmp = nodes[n].getRdn().compareTo(rdn);

            if (cmp < 0) {
                n1 = n + 1;
            } else if (cmp > 0) {
                n2 = n - 1;
            } else {
                throw new IllegalArgumentException(
                        "Node already in list: " + rdn); // NOI18N
            }
        }

        if (nodes == null) {
            nodes = new LDAPSearchTreeNode[8];
            nodes[0] = node;
            size = 1;
        } else {
            // n1 - insertion index
            // n2 - tail size

            n2 = size - n1;

            if (nodes.length == size) {
                LDAPSearchTreeNode[] newNodes = new LDAPSearchTreeNode[
                        size * 3 / 2 + 1];

                if (n1 > 0) {
                    System.arraycopy(nodes, 0, newNodes, 0, n1);
                }

                if (n2 > 0) {
                    System.arraycopy(nodes, n1, newNodes, n1 + 1, n2);
                }

                nodes = newNodes;
            } else if (n2 > 0){
                System.arraycopy(nodes, n1, nodes, n1 + 1, n2);
            }

            nodes[n1] = node;
            size++;
        }
    }
}
