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

package org.netbeans.modules.bugzilla.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.modules.bugzilla.*;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;

/**
 *
 * @author tomas
 */
public class ExceptionHandlerTest extends NbTestCase implements TestConstants {
    public static final String EXCEPTION_HANDLER_CLASS_NAME = "org.netbeans.modules.bugzilla.commands.BugzillaExecutor$ExceptionHandler";
    private TaskRepositoryManager trm;
    private BugzillaRepositoryConnector brc;
    
    public ExceptionHandlerTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
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

    public void testIsLoginHandler() throws Throwable {
        BugzillaRepository repository = new BugzillaRepository("bgzll", REPO_URL, "XXX", "XXX", null, null);
        assertHandler(repository, "LoginHandler");

        repository = new BugzillaRepository("bgzll", REPO_URL, REPO_USER, "XXX", null, null);
        assertHandler(repository, "LoginHandler");
        
    }

    public void testIsNotFoundHandler() throws Throwable {
        BugzillaRepository repository = new BugzillaRepository("bgzll", "http://crap", null, null, null, null);
        assertHandler(repository, "NotFoundHandler");
    }

    public void testIsDefaultHandler() throws Throwable {
        BugzillaRepository repository = new BugzillaRepository("bgzll", null, null, null, null, null);
        assertHandler(repository, "DefaultHandler");

        repository = new BugzillaRepository("bgzll", "crap", null, null, null, null);
        assertHandler(repository, "DefaultHandler");

        // XXX need more tests
    }

    private void assertHandler(BugzillaRepository repository, String name) throws Throwable {
        try {
            brc.getClientManager().getClient(repository.getTaskRepository(), NULL_PROGRESS_MONITOR).validate(NULL_PROGRESS_MONITOR);
        } catch (CoreException ex) {
            assertEquals(EXCEPTION_HANDLER_CLASS_NAME + "$" + name, getHandler(repository, ex).getClass().getName());
        } catch (Exception ex) {
            TestUtil.handleException(ex);
        }
    }

    private Object getHandler(BugzillaRepository repository, CoreException ce) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
        BugzillaExecutor executor = repository.getExecutor();
        Class c = Class.forName(EXCEPTION_HANDLER_CLASS_NAME);
        Method m = c.getDeclaredMethod("createHandler", CoreException.class, BugzillaExecutor.class, BugzillaRepository.class);
        m.setAccessible(true);
        return  m.invoke(executor, new Object[]{ce, executor, repository});
    }
}
