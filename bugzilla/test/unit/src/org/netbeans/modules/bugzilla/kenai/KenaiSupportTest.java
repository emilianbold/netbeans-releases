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

package org.netbeans.modules.bugzilla.kenai;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.modules.bugzilla.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiProject;

/**
 *
 * @author tomas
 */
public class KenaiSupportTest extends NbTestCase implements TestConstants {

    private Kenai instance;
    private TaskRepositoryManager trm;
    private BugzillaRepositoryConnector brc;

    public KenaiSupportTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        try {
            System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
            System.setProperty("kenai.com.url","http://testkenai.com");
            instance = Kenai.getDefault();
            BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
            String username = br.readLine();
            String password = br.readLine();
            br.close();
            instance.login(username, password.toCharArray());

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        trm = new TaskRepositoryManager();
        brc = new BugzillaRepositoryConnector();

        trm.addRepositoryConnector(brc);

        WebUtil.init();
    }
    
//    public void testGetRepositoryFromName () throws Throwable {
//        KenaiProject prj = instance.getProject("golden-project-1");
//        assertNotNull(prj);
//
//        KenaiSupportImpl support = new KenaiSupportImpl();
//        BugzillaRepository repo = (BugzillaRepository) support.createRepository(prj);
//        assertNotNull(repo);
//
//        trm.addRepository(repo.getTaskRepository());
//        TestUtil.validate(brc, repo.getTaskRepository());
//    }

//    public void testGetRepositoryFromUrl () throws Throwable {
//        KenaiProject prj = KenaiProject.forRepository("https://testkenai.com/svn/golden-project-1~source-code-repository-svn");
//        assertNotNull(prj);
//
//        KenaiSupportImpl support = new KenaiSupportImpl();
//        KenaiRepository repo = (KenaiRepository) support.createRepository(prj);
//        assertNotNull(repo);
//
//        String product = (String) getFieldValue(repo, "product");
//        assertEquals("golden-project-1", product);
//
//        String urlParam = (String) getFieldValue(repo, "urlParam");
//        assertEquals("product=golden-project-1", urlParam);
//
//        String host = (String) getFieldValue(repo, "host");
//        assertEquals("testkenai.com", host);
//
//        assertEquals("https://testkenai.com/bugzilla", repo.getTaskRepository().getUrl());
//
//        trm.addRepository(repo.getTaskRepository());
//        TestUtil.validate(brc, repo.getTaskRepository());
//    }

    public void testCreateRepositoryFromLocation () throws Throwable {
        KenaiRepository repo = createRepository("http://testkenai.com/bugzilla/buglist.cgi?product=someproject");
        assertNotNull(repo);
        assertEquals("someproject", getFieldValue(repo, "product"));
        assertEquals("product=someproject", getFieldValue(repo, "urlParam"));
        assertEquals("testkenai.com", getFieldValue(repo, "host"));
        assertEquals("https://testkenai.com/bugzilla", repo.getTaskRepository().getUrl());

        repo = createRepository("https://testkenai.com/bugzilla/buglist.cgi?product=someproject");
        assertNotNull(repo);
        assertEquals("someproject", getFieldValue(repo, "product"));
        assertEquals("product=someproject", getFieldValue(repo, "urlParam"));
        assertEquals("testkenai.com", getFieldValue(repo, "host"));
        assertEquals("https://testkenai.com/bugzilla", repo.getTaskRepository().getUrl());

        repo = createRepository("http://testkenai.com/bugzilla/buglist.cgi?product=someproject&vole=tyvole&etwas=1");
        assertNotNull(repo);
        assertEquals("someproject", getFieldValue(repo, "product"));
        assertNotNull(repo);
        assertEquals("product=someproject", getFieldValue(repo, "urlParam"));
        assertEquals("testkenai.com", getFieldValue(repo, "host"));
        assertEquals("https://testkenai.com/bugzilla", repo.getTaskRepository().getUrl());

        repo = createRepository("http://testkenai.com/bugzilla/buglist.cgi?vole=tyvole&etwas=1&product=someproject");
        assertNotNull(repo);
        assertEquals("someproject", getFieldValue(repo, "product"));
        assertEquals("product=someproject", getFieldValue(repo, "urlParam"));
        assertEquals("testkenai.com", getFieldValue(repo, "host"));
        assertEquals("https://testkenai.com/bugzilla", repo.getTaskRepository().getUrl());

        repo = createRepository("http://testkenai.com/bugzilla/buglist.cgi?vole=tyvole&etwas=1&product=someproject&vole=tyvole&etwas=1");
        assertNotNull(repo);
        assertEquals("someproject", getFieldValue(repo, "product"));
        assertEquals("product=someproject", getFieldValue(repo, "urlParam"));
        assertEquals("testkenai.com", getFieldValue(repo, "host"));
        assertEquals("https://testkenai.com/bugzilla", repo.getTaskRepository().getUrl());

        repo = createRepository("http://testkenai.com/bgzll/buglist.cgi?product=someproject");
        assertNotNull(repo);
        assertEquals("someproject", getFieldValue(repo, "product"));
        assertEquals("product=someproject", getFieldValue(repo, "urlParam"));
        assertEquals("testkenai.com", getFieldValue(repo, "host"));
        assertEquals("https://testkenai.com/bgzll", repo.getTaskRepository().getUrl());

        repo = createRepository("http://kekskenai.com/bgzll/buglist.cgi?product=someproject");
        assertNotNull(repo);
        assertEquals("someproject", getFieldValue(repo, "product"));
        assertEquals("product=someproject", getFieldValue(repo, "urlParam"));
        assertEquals("kekskenai.com", getFieldValue(repo, "host"));
        assertEquals("https://kekskenai.com/bgzll", repo.getTaskRepository().getUrl());

        repo = createRepository("http://testkenai.com/buglist.cgi?product=someproject");
        assertNotNull(repo);
        assertEquals("someproject", getFieldValue(repo, "product"));
        assertEquals("product=someproject", getFieldValue(repo, "urlParam"));
        assertEquals("testkenai.com", getFieldValue(repo, "host"));
        assertEquals("https://testkenai.com", repo.getTaskRepository().getUrl());

        repo = createRepository("http://testkenai.com:8080/bugzilla/buglist.cgi?product=someproject");
        assertNotNull(repo);
        assertEquals("someproject", getFieldValue(repo, "product"));
        assertEquals("product=someproject", getFieldValue(repo, "urlParam"));
        assertEquals("testkenai.com", getFieldValue(repo, "host"));
        assertEquals("https://testkenai.com:8080/bugzilla", repo.getTaskRepository().getUrl());

        repo = createRepository("http://testkenai.com:8080/buglist.cgi?product=someproject");
        assertNotNull(repo);
        assertEquals("someproject", getFieldValue(repo, "product"));
        assertEquals("product=someproject", getFieldValue(repo, "urlParam"));
        assertEquals("testkenai.com", getFieldValue(repo, "host"));
        assertEquals("https://testkenai.com:8080", repo.getTaskRepository().getUrl());

        repo = createRepository("http://testkenai.com/bugzilla/cece.cgi?product=someproject");
        assertNull(repo);

        repo = createRepository("http://kekskenai.com/bgzll/buglist.cgi?produ=someproject");
        assertNull(repo);

        repo = createRepository("http://kekskenai.com/bgzll/buglist.cgi?productik=someproject");
        assertNull(repo);

    }

    private KenaiRepository createRepository(String location) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        KenaiSupportImpl support = new KenaiSupportImpl();
        Method m = support.getClass().getDeclaredMethod("createKenaiRepository", String.class, String.class);
        m.setAccessible(true);
        return (KenaiRepository) m.invoke(support, "Kenai repo", location);
    }


    private Object getFieldValue(Object obj, String name) throws NoSuchFieldException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }



}
