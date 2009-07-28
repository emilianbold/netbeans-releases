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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven;

import hidden.org.codehaus.plexus.util.IOUtil;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.profiles.ProfilesRoot;
import org.apache.maven.profiles.io.xpp3.ProfilesXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.build.model.ModelLineage;
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenSettingsSingleton;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.netbeans.modules.maven.model.profile.ProfilesModelFactory;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Anuradha G
 */
public class ProjectProfileHandlerImpl implements ProjectProfileHandler {

    private static final String PROFILES = "profiles";//NOI18N
    private static final String ACTIVEPROFILES = "activeProfiles";//NOI18N
    private static final String SEPERATOR = " ";//NOI18N
    private static final String NAMESPACE = null;//FIXME add propper namespase
    private List<String> privateProfiles = new ArrayList<String>();
    private List<String> sharedProfiles = new ArrayList<String>();
    private AuxiliaryConfiguration ac;
    private final NbMavenProjectImpl nmp;

    ProjectProfileHandlerImpl(NbMavenProjectImpl nmp, AuxiliaryConfiguration ac) {
        this.nmp = nmp;
        this.ac = ac;
        privateProfiles.addAll(retrieveActiveProfiles(ac, false));
        sharedProfiles.addAll(retrieveActiveProfiles(ac, true));
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllProfiles() {
        Set<String> profileIds = new HashSet<String>();
        //pom+profiles.xml profiles come first
        extractProfiles(profileIds, nmp.getPOMFile());
        //Add settings file Properties
        profileIds.addAll(MavenSettingsSingleton.getInstance().createUserSettingsModel().
                getProfilesAsMap().keySet());

        return new ArrayList<String>(profileIds);
    }

    public List<String> getActiveProfiles(boolean shared) {
       return new ArrayList<String>(shared ? sharedProfiles : privateProfiles);
    }
    public List<String> getMergedActiveProfiles(boolean shared) {
        Set<String> profileIds = new HashSet<String>();
        MavenProject mavenProject = nmp.getOriginalMavenProject();
        @SuppressWarnings("unchecked")
        List<Profile> profiles = mavenProject.getActiveProfiles();
        for (Profile profile : profiles) {
            profileIds.add(profile.getId());
        }
        //read from Settings.xml
        @SuppressWarnings("unchecked")
        List<String> profileStrings = MavenSettingsSingleton.getInstance().createUserSettingsModel().getActiveProfiles();
        for (String profile : profileStrings) {
            profileIds.add(profile);
        }
        
        File basedir = FileUtil.normalizeFile(mavenProject.getBasedir());
        FileObject fileObject = FileUtil.toFileObject(basedir);
        if (fileObject != null) {//144159
            //read from profiles.xml
            FileObject profilesFo = fileObject.getFileObject("profiles.xml");
            if (profilesFo != null) {
                ModelSource ms = Utilities.createModelSource(profilesFo);
                ProfilesModel pm = ProfilesModelFactory.getDefault().getModel(ms);
                if (State.VALID.equals(pm.getState())) {
                    List<String> actProfs = pm.getProfilesRoot().getActiveProfiles();
                    if (actProfs != null) {
                        profileIds.addAll(actProfs);
                    }
                }
            }
        }
        profileIds.addAll(getActiveProfiles(shared));
        return new ArrayList<String>(profileIds);
    }

    public void disableProfile(String id, boolean shared) {
        Element element = ac.getConfigurationFragment(PROFILES, NAMESPACE, shared);
        if (element == null) {

            String root = "project-private"; // NOI18N"

            Document doc = XMLUtil.createDocument(root, NAMESPACE, null, null);
            element = doc.createElementNS(NAMESPACE, PROFILES);
        }
        String activeProfiles = element.getAttributeNS(NAMESPACE, ACTIVEPROFILES);

        if (activeProfiles != null && activeProfiles.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(activeProfiles, SEPERATOR);
            Set<String> set = new HashSet<String>(tokenizer.countTokens());
            while (tokenizer.hasMoreTokens()) {
                set.add(tokenizer.nextToken());
            }
            set.remove(id);
            StringBuffer buffer = new StringBuffer();
            for (String profle : set) {
                buffer.append(profle).append(SEPERATOR);
            }
            element.setAttributeNS(NAMESPACE, ACTIVEPROFILES, buffer.toString().trim());
        }

        ac.putConfigurationFragment(element, shared);
        if(shared){
            sharedProfiles.remove(id);
        }else{
            privateProfiles.remove(id);
        }
    }

    public void enableProfile(String id, boolean shared) {
        Element element = ac.getConfigurationFragment(PROFILES, NAMESPACE, shared);
        if (element == null) {

            String root = "project-private"; // NOI18N"

            Document doc = XMLUtil.createDocument(root, NAMESPACE, null, null);
            element = doc.createElementNS(NAMESPACE, PROFILES);
        }


        String activeProfiles = element.getAttributeNS(NAMESPACE, ACTIVEPROFILES);
        element.setAttributeNS(NAMESPACE, ACTIVEPROFILES, activeProfiles + SEPERATOR + id);
        ac.putConfigurationFragment(element, shared);
        if(shared){
            if(!sharedProfiles.contains(id))
             sharedProfiles.add(id);
        }else{
            if(!privateProfiles.contains(id))
             privateProfiles.add(id);
        }
    }

    private static void extractProfiles(Set<String> profileIds, File file) {
        extractProfilesFromModelLineage(file, profileIds);
        File basedir = FileUtil.normalizeFile(file.getParentFile());
        FileObject fileObject = FileUtil.toFileObject(basedir);
        //read from profiles.xml
        if (fileObject != null) { //144159
            FileObject profiles = fileObject.getFileObject("profiles.xml");
            if (profiles != null) {
                // using xpp3 reader because xam based model needs project instance (for encoding)
                // and might deadlock
                ProfilesXpp3Reader reader = new ProfilesXpp3Reader();
                InputStream in = null;
                try {
                    in = profiles.getInputStream();
                    ProfilesRoot root = reader.read(in);
                    List<org.apache.maven.profiles.Profile> profs = root.getProfiles();
                    for (org.apache.maven.profiles.Profile prf : profs) {
                        profileIds.add(prf.getId());
                    }
                } catch (Exception e) {
                    Logger.getLogger(ProjectProfileHandlerImpl.class.getName()).log(Level.FINE, "Error while retrieving profiles from profiles.xml file. Ignore.", e); //NOI18N
                } finally {
                    IOUtil.close(in);
                }
            }
        }
    }
    
    private static void extractProfilesFromModelLineage(File file, Set<String> profileIds) {
        try {
            ModelLineage lineage = EmbedderFactory.createModelLineage(file, EmbedderFactory.getOnlineEmbedder(), true);
            Iterator it = lineage.modelIterator();
            while (it.hasNext()) {
                Model mdl = (Model) it.next();
                List mdlProfiles = mdl.getProfiles();
                if (mdlProfiles != null) {
                    Iterator it2 = mdlProfiles.iterator();
                    while (it2.hasNext()) {
                        Profile prf = (Profile) it2.next();
                        profileIds.add(prf.getId());
                    }
                }
            }
            if (lineage != null && lineage.getOriginatingModel() != null) {
                @SuppressWarnings("unchecked")
                List<String> modules = lineage.getOriginatingModel().getModules();
                File basedir = FileUtil.normalizeFile(file.getParentFile());
                for (String module : modules) {
                    File childPom = FileUtil.normalizeFile(new File(basedir, module));
                    if (childPom.exists() && !childPom.isFile()) {
                        childPom = new File(childPom, "pom.xml"); //NOI18N
                    }
                    if (childPom.isFile()) {
                            extractProfilesFromModelLineage(childPom, profileIds);
                    }
                }
            }
        } catch (ProjectBuildingException ex) {
            Logger.getLogger(ProjectProfileHandlerImpl.class.getName()).log(Level.FINE, "Error reading model lineage", ex);//NOI18N
        }
    }

    private List<String> retrieveActiveProfiles(AuxiliaryConfiguration ac, boolean shared) {

        Set<String> prifileides = new HashSet<String>();
        Element element = ac.getConfigurationFragment(PROFILES, NAMESPACE, shared);
        if (element != null) {

            String activeProfiles = element.getAttributeNS(NAMESPACE, ACTIVEPROFILES);

            if (activeProfiles != null && activeProfiles.length() > 0) {
                StringTokenizer tokenizer = new StringTokenizer(activeProfiles, SEPERATOR);

                while (tokenizer.hasMoreTokens()) {
                    prifileides.add(tokenizer.nextToken());
                }
            }
        }
        return new ArrayList<String>(prifileides);
    }



 
}
