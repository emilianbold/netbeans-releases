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

package org.netbeans.modules.soa.ldap.browser.fulltree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InterruptedNamingException;
import javax.naming.InvalidNameException;
import javax.naming.LimitExceededException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.swing.SwingUtilities;
import org.netbeans.modules.soa.ldap.LDAPConnection;
import org.netbeans.modules.soa.ldap.LDAPUtils;
import org.netbeans.modules.soa.ldap.properties.ConnectionProperties;

/**
 *
 * @author anjeleevich
 */
class LoadChildrenRunnable implements Runnable {
    private boolean rootFlag;

    private LdapName ownerName;

    private LDAPTreeModel ldapTreeModel;
    private LDAPTreeNode ownerNode;
    private LDAPConnection connection;

    private LDAPTreeNode[] loadedNodes = new LDAPTreeNode[100];
    private int loadedNodesSize = 0;

    private final Object sync = new Object();

    private EDTRunnable edtRunnable = null;

    private int fetchSize;

    private ConnectionProperties connectionProperties;

    public LoadChildrenRunnable(LDAPTreeNode ownerNode) {
        this.ownerNode = ownerNode;
        this.rootFlag = (ownerNode.getParent() == null);
        this.ldapTreeModel = ownerNode.getLDAPTreeModel();
        this.ownerName = ownerNode.getLDAPName();
        this.connection = ldapTreeModel.getLDAPConnection();
        this.connectionProperties = connection.getProperties();
        this.fetchSize = connection.getProperties().getBrowseCountLimit();
    }

    public void run() {
        boolean sort = true;
        boolean group = true;

        if (!connectionProperties.hasBaseDN() && ownerName.size() == 0) {
            sort = loadRoot();
            group = true;
        } else {
            load();
        }

        synchronized (sync) {
            if (edtRunnable == null) {
                edtRunnable = new EDTRunnable();
                SwingUtilities.invokeLater(edtRunnable);
            }
            edtRunnable.setOrdering();
        }

        // sort
        if (sort) {
            Arrays.sort(loadedNodes, 0, loadedNodesSize);
        }

        int groupSize = 1;
        if (group) {
            while (loadedNodesSize > groupSize * 100) {
                groupSize *= 100;
            }
        }

        List<AbstractLDAPTreeNode> result
                = new ArrayList<AbstractLDAPTreeNode>(100);

        if ((groupSize == 1) || (rootFlag && loadedNodesSize < 10000)) {
            for (int i = 0; i < loadedNodesSize; i++) {
                result.add(loadedNodes[i]);
            }
        } else {
            for (int groupStart = 0; groupStart < loadedNodesSize;
                groupStart += groupSize)
            {
                int groupEnd = Math.min(groupStart + groupSize, loadedNodesSize);
                result.add(createGroup(groupStart, groupEnd));
            }
        }

        loadedNodes = null;

        synchronized (sync) {
            if (edtRunnable == null) {
                edtRunnable = new EDTRunnable();
                SwingUtilities.invokeLater(edtRunnable);
            }
            edtRunnable.setResult(result);
        }
    }

    public boolean loadRoot() {
        DirContext dirContext = null;

        boolean sort = true;
        
        try {
            dirContext = connectionProperties.createDirContext();

            Attributes attributes = dirContext.getAttributes("", // NOI18N
                    new String[] { "namingcontexts" }); // NOI18N
            Attribute namingContextsAttribute = attributes
                    .get("namingcontexts"); // NOI18N

            if (namingContextsAttribute == null) {
                return sort;
            }

            List<LdapName> names = new ArrayList<LdapName>();

            for (int i = 0; i < namingContextsAttribute.size(); i++) {
                Object namingContext = namingContextsAttribute.get(i);
                String namingContextString = (namingContext == null)
                        ? null
                        : namingContext.toString();

                LdapName ldapName = null;
                try {
                    ldapName = new LdapName(namingContextString);
                } catch (Exception ex) {
                    // ignore
                }

                if (ldapName != null && ldapName.size() > 0) {
                    names.add(ldapName);
                }
            }

            boolean relatedNamesFlag = false;

            for (int i = names.size() - 1; i >= 0; i--) {
                LdapName name1 = names.get(i);
                boolean removeFlag = false;

                for (int j = i - 1; j >= 0; j--) {
                    LdapName name2 = names.get(j);

                    if (name1.equals(name2)) {
                        removeFlag = true;
                        break;
                    }

                    relatedNamesFlag = relatedNamesFlag
                            || name1.startsWith(name2)
                            || name2.startsWith(name1);
                }

                if (removeFlag) {
                    names.remove(i);
                }
            }

            sort = !relatedNamesFlag;

            if (!relatedNamesFlag) {
                for (LdapName ldapName : names) {
                    LDAPTreeNode node = null;

                    for (int j = 0; j < ldapName.size(); j++) {
                        boolean last = j + 1 == ldapName.size();
                        Rdn rdn = ldapName.getRdn(j);

                        if (node == null) {
                            for (int k = 0; (k < loadedNodesSize); k++) {
                                if (loadedNodes[k].compareTo(rdn) == 0) {
                                    node = loadedNodes[k];
                                    break;
                                }
                            }

                            if (node == null) {
                                node = new LDAPTreeNode(ldapTreeModel, rdn,
                                        new String[] {}, null, null, last);
                                add(node);
                            }
                        } else {
                            int n1 = 0;
                            int n2 = node.getChildCount() - 1;

                            LDAPTreeNode child = null;

                            while (n1 <= n2) {
                                int n = n1 + n2 >>> 1;
                                LDAPTreeNode midNode = (LDAPTreeNode) node
                                        .getChildAt(n);
                                int cmp = midNode.compareTo(rdn);
                                if (cmp < 0) {
                                    n1 = n + 1;
                                } else if (cmp > 0) {
                                    n2 = n - 1;
                                } else {
                                    child = midNode;
                                    break;
                                }
                            }

                            if (child == null) {
                                child = new LDAPTreeNode(ldapTreeModel, rdn,
                                        new String[] {}, null, null, last);
                                node.insert(child, n1);
                            }

                            node = child;
                        }
                    }
                }
            } else {
                for (LdapName ldapName : names) {
                    List<Rdn> rdnsList = ldapName.getRdns();
                    Rdn[] rdns = rdnsList.toArray(new Rdn[rdnsList.size()]);

                    LDAPTreeNode node = new LDAPTreeNode(ldapTreeModel, rdns,
                            new String[] {}, null, null, true);

                    add(node);
                }
            }
        } catch (InterruptedNamingException ex) {
            // do nothing
        } catch (NamingException ex) {
            Logger.getLogger(LoadChildrenRunnable.class.getName())
                    .log(Level.INFO, ex.getMessage(), ex);
        } finally {
            LDAPUtils.close(dirContext);
        }

        return sort;
    }

    private LDAPFolderNode createGroup(int groupStart, int groupEnd) {
        LDAPFolderNode folderNode = new LDAPFolderNode(ldapTreeModel,
                groupStart, groupEnd);

        int groupSize = groupEnd - groupStart;

        int subgroupSize = 1;

        while (groupSize > subgroupSize * 100) {
            subgroupSize *= 100;
        }

        if (subgroupSize == 1) {
            for (int i = groupStart; i < groupEnd; i++) {
                folderNode.add(loadedNodes[i]);
            }
        } else {
            for (int subgroupStart = groupStart; subgroupStart < groupEnd;
                    subgroupStart += subgroupSize)
            {
                int subgroupEnd = Math.min(subgroupStart + subgroupSize, 
                        groupEnd);
                folderNode.add(createGroup(subgroupStart, subgroupEnd));
            }
        }

        return folderNode;
    }

    private void load() {
        SortedSet<String> objectClassesSet = new TreeSet<String>();

        DirContext dirContext = null;
        NamingEnumeration<SearchResult> results = null;

        String userNameAttribute = ldapTreeModel.getUserNameAttribute();
        String groupNameAttribute = ldapTreeModel.getGroupNameAttribute();

        Set<String> attributeNamesSet = new HashSet<String>();
        attributeNamesSet.add("objectClass"); // NOI18N

        boolean needsUserNameAttribute = (userNameAttribute != null)
                && (userNameAttribute.length() > 0);
        boolean needsGroupNameAttribute = (groupNameAttribute != null)
                && (groupNameAttribute.length() > 0);

        if (needsUserNameAttribute) {
            attributeNamesSet.add(userNameAttribute);
        }
        if (needsGroupNameAttribute) {
            attributeNamesSet.add(groupNameAttribute);
        }

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        controls.setReturningAttributes(attributeNamesSet.toArray(
                new String[attributeNamesSet.size()]));

        if (fetchSize > 0) {
            controls.setCountLimit(fetchSize);
        }

        try {
            dirContext = connectionProperties.createDirContext();

            results = dirContext.search(ownerName,
                    "(|(objectClass=*)(objectClass=ldapsubentry))", // NOI18N
                    controls);

            while (results.hasMore()) {
                objectClassesSet.clear();

                SearchResult result = results.next();

                String name = result.getName();

                Rdn childRdn = null;

                try {
                    childRdn = new Rdn(name);
                } catch (InvalidNameException ex) {
                    Logger.getLogger(LoadChildrenRunnable.class.getName())
                            .log(Level.INFO, ex.getMessage(), ex);
                    continue;
                }

                Object[] objectClasses = null;

                Attributes attributes = result.getAttributes();
                Attribute objectClassAttribute = attributes
                        .get("objectClass"); // NOI18N

                if (objectClassAttribute != null) {
                    int size = objectClassAttribute.size();

                    for (int i = 0; i < size; i++) {
                        Object objectClass = objectClassAttribute.get(i);
                        String objectClassString = (objectClass == null) ? null
                                : objectClass.toString();

                        if (objectClassString != null
                                && objectClassString.length() > 0)
                        {
                            objectClassesSet.add(objectClassString
                                    .toLowerCase());
                        }
                    }
                }

                String userNameAttributeValue = null;
                String groupNameAttributeValue = null;

                if (needsUserNameAttribute) {
                    Attribute attribute = attributes.get(userNameAttribute);
                    if (attribute != null && attribute.size() > 0) {
                        Object value = attribute.get();
                        if (value != null) {
                            userNameAttributeValue = value.toString();
                        }
                    }
                }

                if (needsGroupNameAttribute) {
                    Attribute attribute = attributes.get(groupNameAttribute);
                    if (attribute != null && attribute.size() > 0) {
                        Object value = attribute.get();
                        if (value != null) {
                            groupNameAttributeValue = value.toString();
                        }
                    }
                }

                add(new LDAPTreeNode(ldapTreeModel, childRdn, objectClassesSet
                        .toArray(new String[objectClassesSet.size()]),
                        userNameAttributeValue, groupNameAttributeValue));

                LDAPUtils.checkInterrupted();
            }
        } catch (InterruptedNamingException ex) {
            // do nothing
        } catch (LimitExceededException ex) {
            // do nothing
        } catch (NamingException ex) {
            Logger.getLogger(LoadChildrenRunnable.class.getName())
                    .log(Level.INFO, ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            // do nothing
        } finally {
            LDAPUtils.close(results);
            LDAPUtils.close(dirContext);
        }
    }

    private void add(LDAPTreeNode ldapTreeNode) {
        if (loadedNodesSize == loadedNodes.length) {
            LDAPTreeNode[] newLoadedNodes
                    = new LDAPTreeNode[loadedNodesSize * 3 / 2 + 1];
            System.arraycopy(loadedNodes, 0, newLoadedNodes, 0, 
                    loadedNodesSize);
            loadedNodes = newLoadedNodes;
        }
        loadedNodes[loadedNodesSize++] = ldapTreeNode;

        if (loadedNodesSize % 100 == 0) {
            synchronized (sync) {
                if (edtRunnable == null) {
                    edtRunnable = new EDTRunnable();
                    SwingUtilities.invokeLater(edtRunnable);
                }
                edtRunnable.setProgress(loadedNodesSize);
            }
        }
    }


    private class EDTRunnable implements Runnable {
        private int progress = 0;
        private boolean ordering = false;
        private List<AbstractLDAPTreeNode> result = null;

        public EDTRunnable() {
            
        }

        void setProgress(int progress) {
            this.progress = progress;
        }

        void setOrdering() {
            this.ordering = true;
        }

        public void setResult(List<AbstractLDAPTreeNode> result) {
            this.result = result;
        }

        public void run() {
            synchronized (sync) {
                edtRunnable = null;
            }

            if (result != null) {
                ownerNode.setLoadResults(result);
            } else if (ordering) {
                ownerNode.setLoadOrderering();
            } else if (progress > 0){
                ownerNode.setLoadProgress(progress);
            }

            result = null;
        }
    }
}
