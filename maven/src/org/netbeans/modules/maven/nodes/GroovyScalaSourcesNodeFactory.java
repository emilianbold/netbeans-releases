/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.nodes;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.MavenSourcesImpl;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=139)
public class GroovyScalaSourcesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of SourcesNodeFactory */
    public GroovyScalaSourcesNodeFactory() {
    }
    
    public NodeList createNodes(Project project) {
        return  new NList(project);
    }
    
    private static class NList extends AbstractMavenNodeList<SourceGroup> implements ChangeListener {
        private Project project;
        private NList(Project prj) {
            project = prj;
        }
        
        public List<SourceGroup> keys() {
            //#169192 check roots against java roots and if the same don't show twice.
            Set<FileObject> javaroots = new HashSet<FileObject>();
            SourceGroup[] javasg = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (SourceGroup sg : javasg) {
                javaroots.add(sg.getRootFolder());
            }

            List<SourceGroup> list = new ArrayList<SourceGroup>();
            Sources srcs = ProjectUtils.getSources(project);
            SourceGroup[] groovygroup = srcs.getSourceGroups(MavenSourcesImpl.TYPE_GROOVY);
            for (int i = 0; i < groovygroup.length; i++) {
                if (!javaroots.contains(groovygroup[i].getRootFolder())) {
                    list.add(groovygroup[i]);
                }
            }
            SourceGroup[] scalagroup = srcs.getSourceGroups(MavenSourcesImpl.TYPE_SCALA);
            for (int i = 0; i < scalagroup.length; i++) {
                if (!javaroots.contains(scalagroup[i].getRootFolder())) {
                    list.add(scalagroup[i]);
                }
            }
            return list;
        }
        
        public Node node(SourceGroup group) {
            Node pack = PackageView.createPackageView(group);


            if (MavenSourcesImpl.NAME_SCALASOURCE.equals(group.getName()) ||
                MavenSourcesImpl.NAME_SCALATESTSOURCE.equals(group.getName())) {
                Lookup lkp = new ProxyLookup(Lookups.singleton(new ScalaPrivs()), pack.getLookup());
                pack = new FilterNode(pack, new FilterNode.Children(pack), lkp);
            }
            if (MavenSourcesImpl.NAME_GROOVYSOURCE.equals(group.getName()) ||
                MavenSourcesImpl.NAME_GROOVYTESTSOURCE.equals(group.getName())) {
                Lookup lkp = new ProxyLookup(Lookups.singleton(new GroovyPrivs()), pack.getLookup());
                pack = new FilterNode(pack, new FilterNode.Children(pack), lkp);
            }
            return pack;
        }
        
        @Override
        public void addNotify() {
            Sources srcs = ProjectUtils.getSources(project);
            srcs.addChangeListener(this);
        }
        
        @Override
        public void removeNotify() {
            Sources srcs = ProjectUtils.getSources(project);
            srcs.removeChangeListener(this);
        }

        public void stateChanged(ChangeEvent arg0) {
            //#167372 break the stack trace chain to prevent deadlocks.
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    fireChange();
                }
            });
        }
    }

    private static class ScalaPrivs implements PrivilegedTemplates {

        public String[] getPrivilegedTemplates() {
            return new String[] {
                "Templates/Scala/Class.scala", //NOI18N
                "Templates/Scala/Object.scala", //NOI18N
                "Templates/Scala/Trait.scala", //NOI18N
                "Templates/Other/Folder" //NOI18N
            };
        }
    }

    private static class GroovyPrivs implements PrivilegedTemplates {

        public String[] getPrivilegedTemplates() {
            return new String[] {
                "Templates/Groovy/GroovyClass.groovy", //NOI18N
                "Templates/Groovy/GroovyScript.groovy", //NOI18N
                "Templates/Other/Folder" //NOI18N
            };
        }
    }
}
