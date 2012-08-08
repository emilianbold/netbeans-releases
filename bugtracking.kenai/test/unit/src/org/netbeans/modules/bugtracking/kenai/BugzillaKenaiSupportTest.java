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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.netbeans.modules.bugzilla.*;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.spi.BugtrackingFactory;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.kenai.KenaiRepository;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiProject;

/**
 *
 * @author tomas
 */
public class BugzillaKenaiSupportTest extends NbTestCase implements TestConstants {

    private Kenai instance;
    private TaskRepositoryManager trm;
    private BugzillaRepositoryConnector brc;

    public BugzillaKenaiSupportTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        String proxy;
        String port;
        try {
            System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
            System.setProperty("kenai.com.url","https://testjava.net");
            instance = KenaiManager.getDefault().createKenai("testjava.net", "https://testjava.net");
//            instance = KenaiManager.getDefault().getKenai("https://testjava.net");
            BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
            String username = br.readLine();
            String password = br.readLine();
            
            proxy = br.readLine();
            port = br.readLine();
            
            br.close();
//            instance.login(username, password.toCharArray());

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if(proxy != null) {
            System.setProperty("https.proxyHost", proxy);
            System.setProperty("https.proxyPort", port);           
        }
        
        // XXX MYLYN
//        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
//        try {
//            bcp.start(null);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        trm = new TaskRepositoryManager();
        brc = new BugzillaRepositoryConnector(new File(getWorkDir().getAbsolutePath(), "bugzillaconfiguration"));

        trm.addRepositoryConnector(brc);

//        WebUtil.init();
    }
    
    public void testGetRepositoryFromName () throws Throwable {
        KenaiProject prj = instance.getProject("nb-jnet-test");
        assertNotNull(prj);

        BugzillaConnector support = new BugzillaConnector();
        Repository repo = support.createRepository(KenaiProjectImpl.getInstance(prj));
        assertNotNull(repo);

        BugzillaRepository bugzillaRepository = getData(repo);
        
        trm.addRepository(bugzillaRepository.getTaskRepository());
        TestUtil.validate(brc, bugzillaRepository.getTaskRepository());
    }

    public void testGetRepositoryFromUrl () throws Throwable {
        KenaiProject prj = KenaiProject.forRepository("https://testjava.net/svn/nb-jnet-test~subversion");
        assertNotNull(prj);

        BugzillaConnector support = new BugzillaConnector();
        Repository repo = support.createRepository(KenaiProjectImpl.getInstance(prj));
        assertNotNull(repo);

        KenaiRepository kenaiRepository = getData(repo);
        String product = (String) getFieldValue(kenaiRepository, "product");
        assertEquals("nb-jnet-test", product);

        String urlParam = (String) getFieldValue(kenaiRepository, "urlParam");
        assertEquals("product=nb-jnet-test", urlParam);

        String host = (String) getFieldValue(kenaiRepository, "host");
        assertEquals("testjava.net", host);

        assertEquals("https://testjava.net/bugzilla", kenaiRepository.getTaskRepository().getUrl());

        trm.addRepository(kenaiRepository.getTaskRepository());
        TestUtil.validate(brc, kenaiRepository.getTaskRepository());
    }

    public void testCreateRepositoryFromLocation () throws Throwable {
        Repository repository = createRepository("http://testjava.net/bugzilla/buglist.cgi?product=someproject");
        KenaiRepository kenaiRepository = getData(repository);
        
        assertNotNull(repository);
        assertTrue(KenaiUtil.isKenai(repository));
        assertEquals("someproject", getFieldValue(kenaiRepository, "product"));
        assertEquals("product=someproject", getFieldValue(kenaiRepository, "urlParam"));
        assertEquals("testjava.net", getFieldValue(kenaiRepository, "host"));
        assertEquals("https://testjava.net/bugzilla", kenaiRepository.getTaskRepository().getUrl());

        repository = createRepository("https://testjava.net/bugzilla/buglist.cgi?product=someproject");
        kenaiRepository = getData(repository);
        assertNotNull(repository);
        assertTrue(KenaiUtil.isKenai(repository));
        assertEquals("someproject", getFieldValue(kenaiRepository, "product"));
        assertEquals("product=someproject", getFieldValue(kenaiRepository, "urlParam"));
        assertEquals("testjava.net", getFieldValue(kenaiRepository, "host"));
        assertEquals("https://testjava.net/bugzilla", kenaiRepository.getTaskRepository().getUrl());

        repository = createRepository("http://testjava.net/bugzilla/buglist.cgi?product=someproject&vole=tyvole&etwas=1");
        kenaiRepository = getData(repository);
        assertNotNull(repository);
        assertTrue(KenaiUtil.isKenai(repository));
        assertEquals("someproject", getFieldValue(kenaiRepository, "product"));
        assertEquals("product=someproject", getFieldValue(kenaiRepository, "urlParam"));
        assertEquals("testjava.net", getFieldValue(kenaiRepository, "host"));
        assertEquals("https://testjava.net/bugzilla", kenaiRepository.getTaskRepository().getUrl());

        repository = createRepository("http://testjava.net/bugzilla/buglist.cgi?vole=tyvole&etwas=1&product=someproject");
        kenaiRepository = getData(repository);
        assertNotNull(repository);
        assertTrue(KenaiUtil.isKenai(repository));
        assertEquals("someproject", getFieldValue(kenaiRepository, "product"));
        assertEquals("product=someproject", getFieldValue(kenaiRepository, "urlParam"));
        assertEquals("testjava.net", getFieldValue(kenaiRepository, "host"));
        assertEquals("https://testjava.net/bugzilla", kenaiRepository.getTaskRepository().getUrl());

        repository = createRepository("http://testjava.net/bugzilla/buglist.cgi?vole=tyvole&etwas=1&product=someproject&vole=tyvole&etwas=1");
        kenaiRepository = getData(repository);
        assertNotNull(repository);
        assertTrue(KenaiUtil.isKenai(repository));
        assertEquals("someproject", getFieldValue(kenaiRepository, "product"));
        assertEquals("product=someproject", getFieldValue(kenaiRepository, "urlParam"));
        assertEquals("testjava.net", getFieldValue(kenaiRepository, "host"));
        assertEquals("https://testjava.net/bugzilla", kenaiRepository.getTaskRepository().getUrl());

        repository = createRepository("http://testjava.net/bgzll/buglist.cgi?product=someproject");
        kenaiRepository = getData(repository);
        assertNotNull(repository);
        assertTrue(KenaiUtil.isKenai(repository));
        assertEquals("someproject", getFieldValue(kenaiRepository, "product"));
        assertEquals("product=someproject", getFieldValue(kenaiRepository, "urlParam"));
        assertEquals("testjava.net", getFieldValue(kenaiRepository, "host"));
        assertEquals("https://testjava.net/bgzll", kenaiRepository.getTaskRepository().getUrl());

        repository = createRepository("http://kekskenai.com/bgzll/buglist.cgi?product=someproject");
        assertNotNull(repository);
        kenaiRepository = getData(repository);
        assertTrue(KenaiUtil.isKenai(repository));
        assertEquals("someproject", getFieldValue(kenaiRepository, "product"));
        assertEquals("product=someproject", getFieldValue(kenaiRepository, "urlParam"));
        assertEquals("kekskenai.com", getFieldValue(kenaiRepository, "host"));
        assertEquals("https://kekskenai.com/bgzll", kenaiRepository.getTaskRepository().getUrl());

        repository = createRepository("http://testjava.net/buglist.cgi?product=someproject");
        kenaiRepository = getData(repository);
        assertNotNull(repository);
        assertTrue(KenaiUtil.isKenai(repository));
        assertEquals("someproject", getFieldValue(kenaiRepository, "product"));
        assertEquals("product=someproject", getFieldValue(kenaiRepository, "urlParam"));
        assertEquals("testjava.net", getFieldValue(kenaiRepository, "host"));
        assertEquals("https://testjava.net", kenaiRepository.getTaskRepository().getUrl());

        repository = createRepository("http://testjava.net:8080/bugzilla/buglist.cgi?product=someproject");
        kenaiRepository = getData(repository);
        assertNotNull(repository);
        assertTrue(KenaiUtil.isKenai(repository));
        assertEquals("someproject", getFieldValue(kenaiRepository, "product"));
        assertEquals("product=someproject", getFieldValue(kenaiRepository, "urlParam"));
        assertEquals("testjava.net", getFieldValue(kenaiRepository, "host"));
        assertEquals("https://testjava.net:8080/bugzilla", kenaiRepository.getTaskRepository().getUrl());

        repository = createRepository("http://testjava.net:8080/buglist.cgi?product=someproject");
        kenaiRepository = getData(repository);
        assertNotNull(repository);
        assertTrue(KenaiUtil.isKenai(repository));
        assertEquals("someproject", getFieldValue(kenaiRepository, "product"));
        assertEquals("product=someproject", getFieldValue(kenaiRepository, "urlParam"));
        assertEquals("testjava.net", getFieldValue(kenaiRepository, "host"));
        assertEquals("https://testjava.net:8080", kenaiRepository.getTaskRepository().getUrl());

        repository = createRepository("http://testjava.net/bugzilla/cece.cgi?product=someproject");
        assertNull(repository);

        repository = createRepository("http://kekskenai.com/bgzll/buglist.cgi?produ=someproject");
        assertNull(repository);

        repository = createRepository("http://kekskenai.com/bgzll/buglist.cgi?productik=someproject");
        assertNull(repository);

    }

    private Repository createRepository(String location) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
        BugzillaConnector support = new BugzillaConnector();
        Method m = support.getClass().getDeclaredMethod("createKenaiRepository", org.netbeans.modules.bugtracking.kenai.spi.KenaiProject.class, String.class, String.class);
        m.setAccessible(true);
        KenaiRepository kr = (KenaiRepository) m.invoke(support, getKenaiProject(), "Kenai repo", location);
        return kr != null ? BugzillaUtil.getRepository(kr) : null;
    }

    private Object getFieldValue(Object obj, String name) throws NoSuchFieldException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    private org.netbeans.modules.bugtracking.kenai.spi.KenaiProject getKenaiProject() throws KenaiException {
        return KenaiProjectImpl.getInstance(instance.getProject("nb-jnet-test"));
    }

    private KenaiRepository getData(Repository repo) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field f = repo.getClass().getDeclaredField("impl");
        f.setAccessible(true);
        RepositoryImpl<BugzillaRepository, BugzillaQuery, BugzillaIssue> impl = (RepositoryImpl<BugzillaRepository, BugzillaQuery, BugzillaIssue>) f.get(repo);
        Method m = impl.getClass().getDeclaredMethod("getData");
        m.setAccessible(true);
        return (KenaiRepository) m.invoke(impl);
    }

}
