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

package org.netbeans.modules.versioning.kenai;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiNotification;
import org.netbeans.modules.kenai.api.KenaiNotification.Modification;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;
import org.netbeans.modules.versioning.kenai.VCSKenaiAccessorImpl;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.KenaiUser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class VCSKenaiSupportImplTest extends NbTestCase {
    private String username;
    private String password;
    private Kenai kenai;

    public VCSKenaiSupportImplTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        try {
            System.setProperty("kenai.com.url","https://testkenai.com");
            kenai = KenaiManager.getDefault().createKenai("testkenai","https://testkenai.com");
            BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
            username = br.readLine();
            password = br.readLine();
            br.close();
            kenai.login(username, password.toCharArray());

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        MockServices.setServices(new Class[] {VCSSystem.class, VCSKenaiSupportImplTest.OpenProjectsTrampolineImpl.class});
        File f = new File(getWorkDir(), "koliba");
        if(!f.exists()) f.mkdirs();
        OpenProjectsTrampolineImpl trampoline = (OpenProjectsTrampolineImpl) Lookup.getDefault().lookup(OpenProjectsTrampoline.class);
        trampoline.project = f;

        Collection<? extends VersioningSystem> systems = Lookup.getDefault().lookupAll(VersioningSystem.class);
        for (VersioningSystem vs : systems) {
            if(vs.getClass().equals(VCSSystem.class)) {
                ((VCSSystem)vs).project = f;
                KenaiProject kp = kenai.getProject("koliba");
                ((VCSSystem)vs).kp = kp;
                break;
            }
        }
    }

    public void testIsKenai() throws KenaiException {
        VCSKenaiAccessorImpl support = new VCSKenaiAccessorImpl();
        assertFalse(support.isKenai("mirnixdirnix"));
        assertFalse(support.isKenai("http://mirnixdirnix.com/project"));
        KenaiFeature f = getSourceFeature(kenai.getProject("koliba"));
        assertTrue(support.isKenai(f.getLocation()));
    }

    public void testAuthetication() {
        VCSKenaiAccessorImpl support = new VCSKenaiAccessorImpl();
        PasswordAuthentication pa = support.getPasswordAuthentication("https://testkenai.com");
        assertNotNull(pa);
        assertEquals(username, pa.getUserName());
        if(!password.equals(new String(pa.getPassword()))) {
            fail("kenai support returned wrong password");
        }
    }

    public void testForName() {
        VCSKenaiAccessorImpl support = new VCSKenaiAccessorImpl();
        KenaiUser ku = support.forName(username + "@testkenai.com");
        assertNotNull(ku);
        assertEquals(username, ku.getUser());
    }

    public void testLogin() throws KenaiException {
        kenai.logout();
        VCSKenaiAccessorImpl support = new VCSKenaiAccessorImpl();
        assertFalse(support.isLogged("https://testkenai.com"));
        assertFalse(support.isUserOnline(username + "@testkenai.com"));

        kenai.login(username, password.toCharArray());

        assertTrue(support.isLogged("https://testkenai.com"));
        assertTrue(support.isUserOnline(username + "@testkenai.com"));
    }

    public void testHandleKenaiProjectEvent() throws URISyntaxException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, KenaiException {
        Date date = new Date(System.currentTimeMillis());
        KenaiProject kp = kenai.getProject("koliba");
        KenaiFeature feature = getSourceFeature(kp);

        Modification m =
                new Modification(
                    "koliba",
                    "1",
                    KenaiNotification.Modification.Type.NEW);
        List<Modification> modifications = new ArrayList<Modification>();
        modifications.add(m);

        KenaiNotification kn = new KenaiNotification(date, Type.SOURCE, new URI(feature.getLocation()), "Aramis", "svn", "svn", modifications);
        PropertyChangeEvent e = new PropertyChangeEvent(kp, KenaiProject.PROP_PROJECT_NOTIFICATION, null, kn);

        VCSKenaiAccessorImpl support = new VCSKenaiAccessorImpl();

        final List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        PropertyChangeEvent evt;
        support.addVCSNoficationListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                events.add(evt);
            }
        });

        Method method = VCSKenaiAccessorImpl.class.getDeclaredMethod("handleKenaiProjectEvent", new Class[]{PropertyChangeEvent.class, String.class});
        method.setAccessible(true);
        method.invoke(support, new Object[] {e, kp.getName()});

        assertEquals(1, events.size());
        evt = events.get(0);
        assertEquals(VCSKenaiAccessor.PROP_KENAI_VCS_NOTIFICATION, evt.getPropertyName());
        assertTrue(evt.getNewValue() instanceof VCSKenaiAccessor.VCSKenaiNotification);


    }

    private static KenaiFeature getSourceFeature(KenaiProject kp) throws KenaiException {
        KenaiFeature[] features = kp.getFeatures();
        KenaiFeature feature = null;
        for (KenaiFeature f : features) {
            if (f.getType() == Type.SOURCE) {
                feature = f;
                break;
            }
        }
        return feature;
    }

    public static class OpenProjectsTrampolineImpl implements OpenProjectsTrampoline {
        File project;
        public OpenProjectsTrampolineImpl() {
        }

        public Project[] getOpenProjectsAPI() {
            return new Project[] {new Project() {
                public FileObject getProjectDirectory() {
                    return FileUtil.toFileObject(project);
                }
                public Lookup getLookup() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }};
        }

        public void openAPI(Project[] projects, boolean openRequiredProjects, boolean showProgress) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void closeAPI(Project[] projects) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {
            
        }

        public Future<Project[]> openProjectsAPI() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {
            
        }

        public Project getMainProject() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setMainProject(Project project) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    public static class VCSSystem extends VersioningSystem {

        public VCSSystem() {
        }
        
        File project;
        KenaiProject kp;
        @Override
        public VCSInterceptor getVCSInterceptor() {
            return new TestVCSInterceptor(kp);
        }

        @Override
        public File getTopmostManagedAncestor(File file) {
            if(file.equals(project)) {
                return project.getParentFile();
            }
            return super.getTopmostManagedAncestor(file);
        }

    }

    public static class TestVCSInterceptor extends VCSInterceptor {
        KenaiProject kp;
        public TestVCSInterceptor(KenaiProject kp) {
            this.kp =kp;
        }

        @Override
        public Object getAttribute(File file, String attrName) {
            if(attrName.equals("ProvidedExtensions.VCSManaged")) {
                return true;
            } else if (attrName.equals("ProvidedExtensions.RemoteLocation")) {
                KenaiFeature feature;
                try {
                    feature = getSourceFeature(kp);
                } catch (KenaiException ex) {
                    throw new IllegalStateException(ex);
                }
                return feature.getLocation();
            }
            return null;
        }
    }
}
