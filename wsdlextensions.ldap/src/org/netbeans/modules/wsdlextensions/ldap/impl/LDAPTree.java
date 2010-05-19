/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.wsdlextensions.ldap.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.naming.directory.DirContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.modules.wsdlextensions.ldap.utils.LdapConnection;

/**
 *
 * @author tianlize
 */
public class LDAPTree {

    private LdapConnection conn;

    public void initiate(LdapConnection conn) {
        this.conn = conn;
    }

    private String getCreatedDn(String[] str, int i, int j) {
        if (i < 0 | i > str.length | j > str.length | j < 0 | i > j) {
            return null;
        }
        String ret = str[i];
        int k = i + 1;
        while (k < j - 1) {
            ret += "," + str[k];
            k++;
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public DefaultTreeModel getTreeModel() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(conn.getDn());
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        Hashtable existNodes = new Hashtable();
        int baseDNLength = conn.getDn().split(",").length + 1;
        ArrayList dnList = conn.getDNs();
        Iterator it = dnList.iterator();
        while (it.hasNext()) {
            String dnStr = ((String) it.next()).replace(" ", "");
            String[] nodes = dnStr.split(",");
            int j = nodes.length;
            DefaultMutableTreeNode a = rootNode;
            for (int i = j - baseDNLength; i >= 0; i--) {
                String nodeDN = getCreatedDn(nodes, i, j - 1);
                if (existNodes.containsKey(new String(nodeDN))) {
                    a = (DefaultMutableTreeNode) existNodes.get(new String(
                            nodeDN));
                    continue;
                }
                DefaultMutableTreeNode b = new DefaultMutableTreeNode(nodes[i]);
                existNodes.put(new String(nodeDN), b);
                treeModel.insertNodeInto(b, a, a.getChildCount());
                a = b;
                b = null;
            }
            a = null;
        }
        return treeModel;
    }
    @SuppressWarnings("unchecked")
    public DefaultTreeModel getTreeModel(List dns, String rootDn) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootDn);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        Hashtable existNodes = new Hashtable();
        int baseDNLength = rootDn.split(",").length + 1;
        
        Iterator it = dns.iterator();
        while (it.hasNext()) {
            String dnStr = ((String) it.next()).replace(" ", "");
            String[] nodes = dnStr.split(",");
            int j = nodes.length;
            DefaultMutableTreeNode a = rootNode;
            for (int i = j - baseDNLength; i >= 0; i--) {
                String nodeDN = getCreatedDn(nodes, i, j - 1);
                if (existNodes.containsKey(new String(nodeDN))) {
                    a = (DefaultMutableTreeNode) existNodes.get(new String(
                            nodeDN));
                    continue;
                }
                DefaultMutableTreeNode b = new DefaultMutableTreeNode(nodes[i]);
                existNodes.put(new String(nodeDN), b);
                treeModel.insertNodeInto(b, a, a.getChildCount());
                a = b;
                b = null;
            }
            a = null;
        }
        return treeModel;
    }
}