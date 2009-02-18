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

package org.netbeans.modules.maven.navigator;

import java.awt.Image;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.maven.model.pom.Activation;
import org.netbeans.modules.maven.model.pom.ActivationCustom;
import org.netbeans.modules.maven.model.pom.ActivationFile;
import org.netbeans.modules.maven.model.pom.ActivationOS;
import org.netbeans.modules.maven.model.pom.ActivationProperty;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.CiManagement;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Contributor;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.DeploymentRepository;
import org.netbeans.modules.maven.model.pom.Developer;
import org.netbeans.modules.maven.model.pom.DistributionManagement;
import org.netbeans.modules.maven.model.pom.Exclusion;
import org.netbeans.modules.maven.model.pom.Extension;
import org.netbeans.modules.maven.model.pom.IssueManagement;
import org.netbeans.modules.maven.model.pom.License;
import org.netbeans.modules.maven.model.pom.MailingList;
import org.netbeans.modules.maven.model.pom.ModelList;
import org.netbeans.modules.maven.model.pom.Notifier;
import org.netbeans.modules.maven.model.pom.Organization;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.POMQNames;
import org.netbeans.modules.maven.model.pom.Parent;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Prerequisites;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.ReportPlugin;
import org.netbeans.modules.maven.model.pom.ReportSet;
import org.netbeans.modules.maven.model.pom.Reporting;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.model.pom.RepositoryPolicy;
import org.netbeans.modules.maven.model.pom.Resource;
import org.netbeans.modules.maven.model.pom.Scm;
import org.netbeans.modules.maven.model.pom.Site;
import org.netbeans.modules.maven.model.pom.StringList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
public class POMModelVisitor implements org.netbeans.modules.maven.model.pom.POMComponentVisitor {

    private Map<POMQName, Node> childs = new LinkedHashMap<POMQName, Node>();
    private int count = 0;
    private POMQNames names;

    public POMModelVisitor(POMQNames names) {
        this.names = names;
    }

    public void reset() {
         childs = new LinkedHashMap<POMQName, Node>();
         count = 0;
    }

    Node[] getChildNodes() {
        return childs.values().toArray(new Node[0]);
    }

    public void visit(Project target) {
        Project t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.MODELVERSION, "Model Version", t != null ? t.getModelVersion() : null);
        checkChildString(names.GROUPID, "GroupId", t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, "ArtifactId", t != null ? t.getArtifactId() : null);
        checkChildString(names.PACKAGING, "Packaging", t != null ? t.getPackaging() : null);
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.VERSION, "Version", t != null ? t.getVersion() : null);
        checkChildString(names.DESCRIPTION, "Description", t != null ? t.getDescription() : null);
        checkChildString(names.URL, "Url", t != null ? t.getURL() : null);
        checkChildString(names.INCEPTIONYEAR, "Inception Year", t != null ? t.getInceptionYear() : null);

        checkChildObject(names.ISSUEMANAGEMENT, IssueManagement.class, "IssueManagement", t != null ? t.getIssueManagement() : null);
        checkChildObject(names.CIMANAGEMENT, CiManagement.class, "CiManagement", t != null ? t.getCiManagement() : null);
        checkChildObject(names.SCM, Scm.class, "Scm", t != null ? t.getScm() : null);
        checkChildObject(names.ORGANIZATION, Organization.class, "Organization", t != null ? t.getOrganization() : null);

        this.<MailingList>checkListObject(names.MAILINGLISTS, MailingList.class, "Mailing Lists",
                t != null ? t.getMailingLists() : null,
                new IdentityKeyGenerator<MailingList>());

        this.<Developer>checkListObject(names.DEVELOPERS, Developer.class, "Developers",
                t != null ? t.getDevelopers() : null,
                new IdentityKeyGenerator<Developer>());
        this.<Contributor>checkListObject(names.CONTRIBUTORS, Contributor.class, "Contributors",
                t != null ? t.getContributors() : null,
                new IdentityKeyGenerator<Contributor>());

        this.<License>checkListObject(names.LICENSES, License.class, "Licenses",
                t != null ? t.getLicenses() : null,
                new IdentityKeyGenerator<License>());

        this.<Dependency>checkListObject(names.DEPENDENCIES, Dependency.class, "Dependencies",
                t != null ? t.getDependencies() : null,
                new KeyGenerator<Dependency>() {
                    public Object generate(Dependency c) {
                        return c.getGroupId() + ":" + c.getArtifactId();
                    }
                });

        this.<Repository>checkListObject(names.REPOSITORIES, Repository.class, "Repositories",
                t != null ? t.getRepositories() : null,
                new KeyGenerator<Repository>() {
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                });

//            @SuppressWarnings("unchecked")
//            List<List> repositories = getValue(models, "getRepositories", Project.class);
//            addListNode(mods, repositories, new ChildrenCreator2() {
//                public Children createChildren(List value, List<POMModel> lineage) {
//                    @SuppressWarnings("unchecked")
//                    List<Repository> lst = value;
//                    return new RepositoryChildren(lst, lineage);
//                }
//
//                public String createName(Object value) {
//                    String[] name = getStringValue(new Object[] {value}, "getId", Repository.class);
//                    return name.length > 0 ? name[0] : "Repository";
//                }
//
//                public boolean objectsEqual(Object value1, Object value2) {
//                    Repository d1 = (Repository)value1;
//                    Repository d2 = (Repository)value2;
//                    return d1.getId() != null && d1.getId().equals(d2.getId());
//                }
//            }, "Repositories", nds);

//            @SuppressWarnings("unchecked")
//            List<List> dependencies = getValue(models, "getDependencies", Project.class);
//            addListNode(mods, dependencies, new ChildrenCreator2() {
//                public Children createChildren(List value, List<POMModel> lineage) {
//                    @SuppressWarnings("unchecked")
//                    List<Dependency> lst = value;
//                    return new DependencyChildren(lst, lineage);
//                }
//
//                public String createName(Object value) {
//                    String[] name = getStringValue(new Object[] {value}, "getArtifactId", Dependency.class);
//                    return name.length > 0 ? name[0] : "Dependency";
//                }
//
//                public boolean objectsEqual(Object value1, Object value2) {
//                    Dependency d1 = (Dependency)value1;
//                    Dependency d2 = (Dependency)value2;
//                    String grId1 = d1.getGroupId();
//                    String grId2 = d2.getGroupId();
//                    String artId1 = d1.getArtifactId();
//                    String artId2 = d2.getArtifactId();
//                    return (grId1 + ":" + artId1).equals(grId2 + ":" + artId2);
//                }
//            }, "Dependencies", nds);

//            @SuppressWarnings("unchecked")
//            List<List> licenses = getValue(models, "getLicenses", Project.class);
//            addListNode(mods, licenses, new ChildrenCreator() {
//                public Children createChildren(List value, List<POMModel> lineage) {
//                    @SuppressWarnings("unchecked")
//                    List<License> lst = value;
//                    return new LicenseChildren(lst, lineage);
//                }
//
//                public String createName(Object value) {
//                    String[] name = getStringValue(new Object[] {value}, "getName", License.class);
//                    return name.length > 0 ? name[0] : "License";
//                }
//            }, "Licenses", nds);

//            @SuppressWarnings("unchecked")
//            List<List> contributors = getValue(models, "getContributors", Project.class);
//            addListNode(mods, contributors, new ChildrenCreator() {
//                public Children createChildren(List value, List<POMModel> lineage) {
//                    @SuppressWarnings("unchecked")
//                    List<Contributor> lst = value;
//                    return new ContributorChildren(lst, lineage);
//                }
//
//                public String createName(Object value) {
//                    String[] name = getStringValue(new Object[] {value}, "getName", Contributor.class);
//                    return name.length > 0 ? name[0] : "Contributor";
//                }
//            }, "Contributors", nds);

//            @SuppressWarnings("unchecked")
//            List<List> developers = getValue(models, "getDevelopers", Project.class);
//            addListNode(mods, developers, new ChildrenCreator() {
//                public Children createChildren(List value, List<POMModel> lineage) {
//                    @SuppressWarnings("unchecked")
//                    List<Developer> lst = value;
//                    return new DeveloperChildren(lst, lineage);
//                }
//
//                public String createName(Object value) {
//                    String[] name = getStringValue(new Object[] {value}, "getName", Developer.class);
//                    String[] id = getStringValue(new Object[] {value}, "getId", Developer.class);
//                    return name.length > 0 ? name[0] : (id.length > 0 ? id[0] : "Developer");
//                }
//            }, "Developers", nds);
//


        count++;
    }

    public void visit(Parent target) {
    }

    public void visit(Organization target) {
        Organization t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(DistributionManagement target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Site target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(DeploymentRepository target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Prerequisites target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Contributor target) {
        Contributor t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.EMAIL, "Email", t != null ? t.getEmail() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);
        checkChildString(names.ORGANIZATION, "Organization", t != null ? t.getOrganization() : null);
        checkChildString(names.ORGANIZATIONURL, "Organization Url", t != null ? t.getOrganizationUrl() : null);
        checkChildString(names.TIMEZONE, "Timezone", t != null ? t.getTimezone() : null);

        count++;    }

    public void visit(Scm target) {
        Scm t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.CONNECTION, "Connection", t != null ? t.getConnection() : null);
        checkChildString(names.DEVELOPERCONNECTION, "Developer Connection", t != null ? t.getDeveloperConnection() : null);
        checkChildString(names.TAG, "Tag", t != null ? t.getTag() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(IssueManagement target) {
        IssueManagement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.SYSTEM, "System", t != null ? t.getSystem() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(CiManagement target) {
        CiManagement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.SYSTEM, "System", t != null ? t.getSystem() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(Notifier target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Repository target) {
        Repository t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, "Id", t != null ? t.getId() : null);
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(RepositoryPolicy target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Profile target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(BuildBase target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Plugin target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Dependency target) {
        Dependency t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.GROUPID, "GroupId", t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, "ArtifactId", t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, "Version", t != null ? t.getVersion() : null);
        checkChildString(names.TYPE, "Type", t != null ? t.getType() : null);
        checkChildString(names.CLASSIFIER, "Classifier", t != null ? t.getClassifier() : null);
        checkChildString(names.SCOPE, "Scope", t != null ? t.getScope() : null);
        count++;

    }

    public void visit(Exclusion target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(PluginExecution target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Resource target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(PluginManagement target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Reporting target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(ReportPlugin target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(ReportSet target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Activation target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(ActivationProperty target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(ActivationOS target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(ActivationFile target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(ActivationCustom target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(DependencyManagement target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Build target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Extension target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(License target) {
        License t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);
        count++;
    }

    public void visit(MailingList target) {
        MailingList t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.SUBSCRIBE, "Subscribe", t != null ? t.getSubscribe() : null);
        checkChildString(names.UNSUBSCRIBE, "Unsubscribe", t != null ? t.getUnsubscribe() : null);
        checkChildString(names.POST, "Post", t != null ? t.getPost() : null);
        checkChildString(names.ARCHIVE, "Archive", t != null ? t.getArchive() : null);
        count++;
    }

    public void visit(Developer target) {
        Developer t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, "Id", t != null ? t.getId() : null);
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.EMAIL, "Email", t != null ? t.getEmail() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);
        checkChildString(names.ORGANIZATION, "Organization", t != null ? t.getOrganization() : null);
        checkChildString(names.ORGANIZATIONURL, "Organization Url", t != null ? t.getOrganizationUrl() : null);
        checkChildString(names.TIMEZONE, "Timezone", t != null ? t.getTimezone() : null);

        count++;
    }

    public void visit(POMExtensibilityElement target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(ModelList target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Configuration target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Properties target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(StringList target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(POMComponent target) {
    }


    @SuppressWarnings("unchecked")
    private void checkChildString(POMQName qname, String displayName, String value) {
        Node nd = childs.get(qname);
        if (nd == null) {
            nd = new SingleFieldNode(Lookups.singleton(new POMCutHolder()), displayName);
            childs.put(qname, nd);
        }
        fillValues(count, nd.getLookup().lookup(POMCutHolder.class), value);
    }

    private void checkChildObject(POMQName qname, Class type, String displayName, POMComponent value) {
        Node nd = childs.get(qname);
        if (nd == null) {
            POMCutHolder cutter = new POMCutHolder();
            nd = new ObjectNode(Lookups.singleton(cutter), new PomChildren(cutter, names, type), displayName);
            childs.put(qname, nd);
        }
        fillValues(count, nd.getLookup().lookup(POMCutHolder.class), value);
    }


    private <T extends POMComponent> void checkListObject(POMQName qname, Class type, String displayName, List<T> values, KeyGenerator<T> keygen) {
        Node nd = childs.get(qname);
        if (nd == null) {
            POMCutHolder cutter = new POMCutHolder();
            nd = new ListNode(Lookups.singleton(cutter), new PomListChildren<T>(cutter, names, type, keygen, true), displayName);
            childs.put(qname, nd);
        }
        fillValues(count, nd.getLookup().lookup(POMCutHolder.class), values);
    }


    private void fillValues(int current, POMCutHolder cutHolder, Object value) {
        while (cutHolder.getCutsSize() < current) {
            cutHolder.addCut(null);
        }
        cutHolder.addCut(value);
    }

//    private static Children createOverrideListChildren(ChildrenCreator subs, List<List> values) {
//        Children toRet = new Children.Array();
//        int count = 0;
//        for (List lst : values) {
//            if (lst != null && lst.size() > 0) {
//                for (Object o : lst) {
//                    List objectList = new ArrayList(Collections.nCopies(count, null));
//                    objectList.add(o);
//                    toRet.add(new Node[] {
//                        new ObjectNode(Lookup.EMPTY, subs.createChildren(objectList),  subs.createName(o), objectList)
//                    });
//                }
//                break;
//            }
//            count = count + 1;
//        }
//
//        return toRet;
//    }
//
//    private static Children createMergeListChildren(ChildrenCreator2 subs, List<POMModel> key, List<List> values) {
//        Children toRet = new Children.Array();
//        HashMap<Object, List> content = new HashMap<Object, List>();
//        List order = new ArrayList();
//
//        int count = 0;
//        for (List lst : values) {
//            if (lst != null && lst.size() > 0) {
//                for (Object o : lst) {
//                    processObjectList(o, content, count, subs);
//                            new ArrayList(Collections.nCopies(count, null));
//                }
//            }
//            count = count + 1;
//        }
//        for (Map.Entry<Object, List> entry : content.entrySet()) {
//            toRet.add(new Node[] {
//                new ObjectNode(Lookup.EMPTY, subs.createChildren(entry.getValue(), key), key, subs.createName(entry.getKey()), entry.getValue())
//            });
//        }
//
//        return toRet;
//    }


    private interface KeyGenerator<T extends POMComponent> {
        Object generate(T c);
    }

    private class IdentityKeyGenerator<T extends POMComponent> implements  KeyGenerator<T> {
        public Object generate(T c) {
            return c;
        }

    }

    static class POMCutHolder {
        private List cuts = new ArrayList();
        Object[] getCutValues() {
            return cuts.toArray();
        }

        String[] getCutValuesAsString() {
            String[] toRet = new String[cuts.size()];
            int i = 0;
            for (Object cut : cuts) {
                toRet[i] = (cut != null ? cut.toString() : null);
                i++;
            }
            return toRet;
        }

        @SuppressWarnings("unchecked")
        void addCut(Object obj) {
            cuts.add(obj);
        }

        int getCutsSize() {
            return cuts.size();
        }
    }


    private static class SingleFieldNode extends AbstractNode {

        private Image icon = ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/Maven2Icon.gif"); // NOI18N
        private String key;
        private SingleFieldNode(Lookup lkp, String key) {
            super(Children.LEAF, lkp);
            setName(key);
            this.key = key;
        }

        @Override
        public String getHtmlDisplayName() {
            String[] values = getLookup().lookup(POMCutHolder.class).getCutValuesAsString();

            String dispVal = POMModelPanel.getValidValue(values);
            if (dispVal == null) {
                dispVal = "&lt;Undefined&gt;";
            }
            boolean override = POMModelPanel.overridesParentValue(values);
            String overrideStart = override ? "<b>" : "";
            String overrideEnd = override ? "</b>" : "";
            boolean inherited = !POMModelPanel.isValueDefinedInCurrent(values);
            String inheritedStart = inherited ? "<i>" : "";
            String inheritedEnd = inherited ? "</i>" : "";

            String message = "<html>" +
                    inheritedStart + overrideStart +
                    key + " : " + dispVal +
                    overrideEnd + inheritedEnd;
            return message;
        }

        @Override
        public Image getIcon(int type) {
             return icon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    private static class ObjectNode extends AbstractNode {

        private Image icon = ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/Maven2Icon.gif"); // NOI18N
        private String key;
        private ObjectNode(Lookup lkp, Children children, String key) {
            super( children, lkp);
            setName(key);
            this.key = key;
        }

        @Override
        public String getHtmlDisplayName() {
            Object[] values = getLookup().lookup(POMCutHolder.class).getCutValues();
            String dispVal = POMModelPanel.definesValue(values) ? "" : "&lt;Undefined&gt;";
            boolean override = POMModelPanel.overridesParentValue(values);
            String overrideStart = override ? "<b>" : "";
            String overrideEnd = override ? "</b>" : "";
            boolean inherited = !POMModelPanel.isValueDefinedInCurrent(values);
            String inheritedStart = inherited ? "<i>" : "";
            String inheritedEnd = inherited ? "</i>" : "";

            String message = "<html>" +
                    inheritedStart + overrideStart +
                    key + " " + dispVal +
                    overrideEnd + inheritedEnd;

            return message;
        }

        @Override
        public Image getIcon(int type) {
             return icon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    private static class ListNode extends AbstractNode {

        private Image icon = ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/Maven2Icon.gif"); // NOI18N
        private String key;

        private ListNode(Lookup lkp, Children childs, String name) {
            super(childs , lkp);
            setName(name);
            this.key = name;
        }

        @Override
        public String getHtmlDisplayName() {
            //TODO - this needs different markings..
            Object[] values = getLookup().lookup(POMCutHolder.class).getCutValues();

            String dispVal = POMModelPanel.definesValue(values) ? "" : "&lt;Undefined&gt;";
            boolean override = POMModelPanel.overridesParentValue(values);
            String overrideStart = override ? "<b>" : "";
            String overrideEnd = override ? "</b>" : "";
            boolean inherited = !POMModelPanel.isValueDefinedInCurrent(values) && POMModelPanel.definesValue(values);
            String inheritedStart = inherited ? "<i>" : "";
            String inheritedEnd = inherited ? "</i>" : "";
            String message = "<html>" +
                    inheritedStart + overrideStart +
                    key + " " + dispVal +
                    overrideEnd + inheritedEnd;
            return message;
        }

        @Override
        public Image getIcon(int type) {
             return icon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    static class PomChildren extends Children.Keys<Object> {
        private Object[] one = new Object[] {new Object()};
        private POMCutHolder holder;
        private POMQNames names;
        private POMModelVisitor visitor;
        private Class type;
        public PomChildren(POMCutHolder holder, POMQNames names, Class type) {
            setKeys(one);
            this.holder = holder;
            this.names = names;
            this.type = type;
            visitor = new POMModelVisitor(names);
        }

        public void reshow() {
            this.refreshKey(one);
        }

        @Override
        protected Node[] createNodes(Object key) {
           visitor.reset();
            try {
                Method m = POMModelVisitor.class.getMethod("visit", type); //NOI18N
                for (Object comp : holder.getCutValues()) {
                    try {
                        m.invoke(visitor, comp);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return visitor.getChildNodes();
        }


    }

    private class PomListChildren<T extends POMComponent> extends Children.Keys<Object> {
        private Object[] one = new Object[] {new Object()};
        private POMCutHolder holder;
        private POMQNames names;
        private Class type;
        private KeyGenerator<T> keyGenerator;
        private boolean override;
        public PomListChildren(POMCutHolder holder, POMQNames names, Class type, KeyGenerator<T> generator, boolean override) {
            setKeys(one);
            this.holder = holder;
            this.names = names;
            this.type = type;
            this.keyGenerator = generator;
            this.override = override;
        }

        public void reshow() {
            this.refreshKey(one);
        }

        @Override
        protected Node[] createNodes(Object key) {
            List<Node> toRet = new ArrayList<Node>();
            LinkedHashMap<Object, List<T>> cut = new LinkedHashMap<Object, List<T>>();

            int level = 0;
            int firstLevel = -1;
            for (Object comp : holder.getCutValues()) {
                if (comp == null) {
                    continue;
                }
                if (override && firstLevel == -1) {
                    firstLevel = level;
                }
                @SuppressWarnings("unchecked")
                List<T> lst = (List<T>) comp;
                for (T c : lst) {
                    Object keyGen = keyGenerator.generate(c);
                    List<T> currentCut = cut.get(keyGen);
                    if (currentCut == null) {
                        currentCut = new ArrayList<T>();
                        cut.put(keyGen, currentCut);
                    }
                    fillValues(count, currentCut, c);
                }
                level++;
            }
            for (List<T> lst : cut.values()) {
                int cutLevel = 0;
                POMCutHolder cutHolder = new POMCutHolder();
                for (T c : lst) {
                    cutHolder.addCut(c);
                    cutLevel++;
                }
                toRet.add(new ObjectNode(Lookups.singleton(cutHolder), new PomChildren(cutHolder, names, type), type.getName()));
            }

            return toRet.toArray(new Node[0]);
        }

        private void fillValues(int current, List<T> list, T value) {
            while (list.size() < current) {
                list.add(null);
            }
            list.add(value);
        }
    }


}
