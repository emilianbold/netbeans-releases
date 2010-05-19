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

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InterruptedNamingException;
import javax.naming.InvalidNameException;
import javax.naming.LimitExceededException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import javax.swing.SwingUtilities;
import org.netbeans.modules.soa.ldap.LDAPConnection;
import org.netbeans.modules.soa.ldap.LDAPUtils;
import org.netbeans.modules.soa.ldap.properties.ConnectionProperties;

/**
 *
 * @author anjeleevich
 */
public class SearchRunnable implements Runnable {
    private ConnectionProperties connectionProperties;
    private LDAPSearchTreeModel model;
    private LDAPConnection connection;
    private String filter;

    private final String userNameAttributeId;
    private final String groupNameAttributeId;

    private final boolean needUserName;
    private final boolean needGroupName;

    private final Object sync = new Object();
    private int fetchSize;
    
    // syncRunnable is used to pass changes to AWT event dispatching thread
    private SyncRunnable syncRunnable = null;

    public SearchRunnable(LDAPSearchTreeModel model) {
        this.model = model;
        this.connection = model.getConnection();
        this.connectionProperties = connection.getProperties();
        this.filter = model.getFilter();
        this.fetchSize = connection.getProperties().getSearchCountLimit();
        this.userNameAttributeId = model.getUserNameAttribute();
        this.groupNameAttributeId = model.getGroupNameAttribute();

        needUserName = (userNameAttributeId != null)
                && (userNameAttributeId.length() > 0);
        needGroupName = (groupNameAttributeId != null)
                && (groupNameAttributeId.length() > 0);
    }

    public void run() {
        DirContext dirContext = null;

        boolean abortedFlag = false;

        try {
            dirContext = connectionProperties.createDirContext();

            LDAPUtils.checkInterrupted();

            if (connectionProperties.hasBaseDN()) {
                search(dirContext, LDAPUtils.createEmptyLdapName()); // NOI18N
            } else {
                Attributes attributes = dirContext.getAttributes("", // NOI18N
                        new String[] { "namingcontexts" }); // NOI18N
                Attribute namingContextsAttribute = attributes
                        .get("namingcontexts"); // NOI18N

                if (namingContextsAttribute != null) {
                    for (int i = 0; i < namingContextsAttribute.size(); i++) {
                        Object namingContext = namingContextsAttribute.get(i);
                        String namingContextString = (namingContext == null)
                                ? null
                                : namingContext.toString();

                        LdapName namingContextLdapName = null;
                        try {
                            namingContextLdapName
                                    = new LdapName(namingContextString);
                        } catch (Exception ex) {
                            // do nothing
                        }

                        if (namingContextLdapName != null) {
                            search(dirContext, namingContextLdapName);
                        }
                    }
                }
            }
        } catch (InterruptedNamingException ex) {
            abortedFlag = true;
        } catch (NamingException ex) {
            Logger.getLogger(SearchRunnable.class.getName())
                    .log(Level.INFO, ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            abortedFlag = true;
        } finally {
            LDAPUtils.close(dirContext);
        }

        sync((abortedFlag) ? LDAPSearchState.ABORTED
                : LDAPSearchState.COMPLETE);
    }

    private void search(DirContext dirContext, LdapName namingContext) throws
            InterruptedException
    {
        Set<String> attributesSet = new HashSet<String>();
        attributesSet.add("objectClass"); // NOI18N

        if (needUserName) {
            attributesSet.add(userNameAttributeId);
        }

        if (needGroupName) {
            attributesSet.add(groupNameAttributeId);
        }

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(
                attributesSet.toArray(new String[attributesSet.size()]));

        if (fetchSize > 0) {
            searchControls.setCountLimit(fetchSize);
        }

        NamingEnumeration<SearchResult> results = null;

        SortedSet<String> objectClassesSet = new TreeSet<String>();

        try {
            results = dirContext.search(namingContext, filter, searchControls);

            LDAPUtils.checkInterrupted();

            while (results.hasMore()) {
                SearchResult result = results.next();

                String name = result.getName();

                LdapName ldapName = null;
                
                try {
                    ldapName = new LdapName(name);
                    ldapName.addAll(0, namingContext);
                } catch (InvalidNameException ex) {
                    Logger.getLogger(SearchRunnable.class.getName())
                            .log(Level.INFO, ex.getMessage(), ex);
                }

                if (ldapName != null) {
                    AddEntryResult addResult = model.addEntry(ldapName);

                    LDAPSearchTreeNode newResultNode = addResult
                            .getNewResultNode();

                    for (LDAPSearchTreeNode newNode : addResult.getNewNodes()) {
                        if (newNode == newResultNode) {
                            loadAttributes(result.getAttributes(),
                                    newResultNode, objectClassesSet);
                        } else {
                            loadAttribute(dirContext, newNode, attributesSet,
                                    objectClassesSet);
                        }
                    }

                    if (addResult.getUnpublishedResultCount() >= 100) {
                        sync(LDAPSearchState.IN_PROGRESS);
                    }
                }

                LDAPUtils.checkInterrupted();
            }
        } catch (InterruptedNamingException ex) {
            throw new InterruptedException();
        } catch (LimitExceededException ex) {
            // do nothing
        } catch (NamingException ex) {
            Logger.getLogger(SearchRunnable.class.getName())
                    .log(Level.INFO, ex.getMessage(), ex);
        } finally {
            LDAPUtils.close(results);
        }

        sync(LDAPSearchState.IN_PROGRESS);
    }

    private void loadAttribute(DirContext dirContext,
            LDAPSearchTreeNode node, Set<String> attributesSet,
            SortedSet<String> tempSet)
                    throws InterruptedException
    {
        try {
            Attributes attributes = dirContext.getAttributes(node.getLDAPName(),
                    attributesSet.toArray(new String[attributesSet.size()]));
            loadAttributes(attributes, node, tempSet);
        } catch (InterruptedNamingException ex) {
            throw new InterruptedException();
        } catch (NameNotFoundException ex) {
            Logger.getLogger(SearchRunnable.class.getName()).log(Level.INFO,
                    ex.getMessage(), ex);
        } catch (NamingException ex) {
            Logger.getLogger(SearchRunnable.class.getName()).log(Level.INFO,
                    ex.getMessage(), ex);
        }
//
//        model.setAttributes(node, tempSet
//                .toArray(new String[tempSet.size()]));
    }

    private void loadAttributes(Attributes attributes,
            LDAPSearchTreeNode node,
            SortedSet<String> tempSet)
                    throws InterruptedException
    {
        tempSet.clear();

        String userNameAttributeValue = null;
        String groupNameAttributeValue = null;

        try {
            if (attributes != null) {
                Attribute attribute = attributes.get("objectClass"); // NOI18N
                if (attribute != null) {
                    int size = attribute.size();
                    for (int i = 0; i < size; i++) {
                        Object objectClass = attribute.get(i);
                        String objectClassString = (objectClass == null) ? null
                                : objectClass.toString();

                        if (objectClassString != null
                                && objectClassString.length() > 0)
                        {
                            tempSet.add(objectClassString.toLowerCase());
                        }
                    }
                }

                if (needUserName) {
                    attribute = attributes.get(userNameAttributeId);
                    if (attribute != null && attribute.size() > 0) {
                        Object value = attribute.get();
                        if (value != null) {
                            userNameAttributeValue = value.toString();
                        }

                        if (userNameAttributeValue.length() == 0) {
                            userNameAttributeValue = null;
                        }
                    }
                }

                if (needGroupName) {
                    attribute = attributes.get(groupNameAttributeId);
                    if (attribute != null && attribute.size() > 0) {
                        Object value = attribute.get();
                        if (value != null) {
                            groupNameAttributeValue = value.toString();
                        }

                        if (groupNameAttributeValue.length() == 0) {
                            groupNameAttributeValue = null;
                        }
                    }
                }
            }
        } catch (InterruptedNamingException ex) {
            throw new InterruptedException();
        } catch (NamingException ex) {
            Logger.getLogger(SearchRunnable.class.getName())
                    .log(Level.INFO, ex.getMessage(), ex);
        }

        model.setAttributes(node, tempSet
                .toArray(new String[tempSet.size()]),
                userNameAttributeValue,
                groupNameAttributeValue);
    }

    private void sync(LDAPSearchState state) {
        synchronized (sync) {
            if (syncRunnable == null) {
                syncRunnable = new SyncRunnable(state);
                SwingUtilities.invokeLater(syncRunnable);
            } else {
                syncRunnable.setSearchState(state);
            }
        }
    }

    private class SyncRunnable implements Runnable {
        private LDAPSearchState searchState;

        SyncRunnable(LDAPSearchState state) {
            this.searchState = state;
        }

        void setSearchState(LDAPSearchState state) {
            this.searchState = state;
        }

        public void run() {
            synchronized (sync) {
                syncRunnable = null;
            }

            model.sync();
            
            if (searchState != null) {
                model.setSearchState(searchState);
            }
        }
    }
}
