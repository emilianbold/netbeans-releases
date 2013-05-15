/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.tasks.tck;

import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.StateTransition;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.odcs.tasks.spi.C2CData;
import org.netbeans.modules.odcs.tasks.spi.C2CExtender;

/**
 *
 * @author Tomas Stupka
 */
public class C2CDataTck extends NbTestSuite {
    public C2CDataTck() {
        
//        addTestSuite(JSONDumper.class);
        addTestSuite(C2CDataTestCase.class);
        
        List<Method> notTested = new LinkedList<Method>();
        for (Method m : RepositoryConfiguration.class.getDeclaredMethods()) {
            if(m.getParameterTypes().length == 0) {
                addTest(new GetAttributeTestCase(m));
            } else if(!m.getName().startsWith("set") &&
                      !m.getName().startsWith("getMilestones") &&
                      !m.getName().startsWith("getComponents") && 
                      !m.getName().startsWith("computeValidStatuses") &&
                      !m.getName().startsWith("getValidIterationsForTask") &&
                      !m.getName().startsWith("getReleaseTags") &&
                      !m.getName().startsWith("computeValidResolutions") &&
                      !m.getName().startsWith("getConfigurationProperty")) 
            {
                // XXX 
                //      not tested RepositoryConfiguration methods : 
                //	public java.util.List com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration.getComponents(com.tasktop.c2c.server.tasks.domain.Product)
                //	public java.util.List com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration.getMilestones(com.tasktop.c2c.server.tasks.domain.Product)
                //	public java.util.List com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration.computeValidStatuses(com.tasktop.c2c.server.tasks.domain.TaskStatus)
                //	public java.util.List com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration.computeValidResolutions(com.tasktop.c2c.server.tasks.domain.TaskStatus)
                //	public java.util.List com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration.getValidIterationsForTask(com.tasktop.c2c.server.tasks.domain.Task)
                //	public java.util.List com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration.getReleaseTags(com.tasktop.c2c.server.tasks.domain.Product)
                //	public java.lang.String com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration.getConfigurationProperty(java.lang.String)
                notTested.add(m);
            }
        }
        if(!notTested.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(" not tested RepositoryConfiguration methods : \n");
            for (Method m : notTested) {
                sb.append('\t')
                  .append(m.toString())
                  .append('\n');
            }
            throw new IllegalStateException(sb.toString());
        }
        
    }
    
    public static class GetAttributeTestCase extends AbstractC2CDataTestCase {
        private final Method method;

        public GetAttributeTestCase(Method m) {
            super("test" + m.getName().substring(0,1).toUpperCase() + m.getName().substring(1, m.getName().length()));
            method = m;
        }
        
        @Override
        protected void runTest() throws Throwable {
            RepositoryConfiguration repoConf = getData().getRepositoryConfiguration();
            RepositoryConfiguration goldenRepoConf = getRepositoryConfiguration();
            Object goldenValue = method.invoke(goldenRepoConf);
            Object value = method.invoke(repoConf);
            if(goldenValue instanceof List) {
                assertValues((List) goldenValue, (List) value);
            } else {
                assertValue(goldenValue, value);
            }
        }

        private void assertValues(List goldenValues, List values) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            assertNotNull(values);
            assertEquals(goldenValues.size(), values.size());
            for (Object goldenValue : goldenValues) {
                int idx = values.indexOf(goldenValue);
                if(idx > -1) {
                    // .equals() methods typical based only on .getId(),
                    // but we want to check all getters
                    Object value = values.get(idx);
                    if(goldenValue != null) {
                        assertValue(goldenValue, value);
                    }
                } else {
                    // e.g. StateTransition doesn't implement equals !!!
                    boolean found = false;
                    if(goldenValue instanceof StateTransition) {
                        StateTransition gst = (StateTransition) goldenValue;
                        for (Object v : values) {
                            StateTransition st = (StateTransition) v;
                            if(equals(gst.getInitialStatus(),st.getInitialStatus()) && 
                               equals(gst.getNewStatus(), (st.getNewStatus())) && 
                               gst.isCommentRequired() == st.isCommentRequired()) 
                            {
                                found = true;
                                break;
                            }
                        }
                    } 
                    if(!found) {
                        fail(" method " + method + " did not return expected value " + goldenValue);
                    }
                }
                     
            }
        }
        
        private void assertValue(Object goldenValue, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if(!equals(goldenValue, value)) {
                fail("(one of the) value(s) returned by " + method + " was expected to be [" + goldenValue + "] but was [" + value + "]");
            }
            if(goldenValue.getClass().isPrimitive() ||
               goldenValue.getClass().isEnum() ||
               goldenValue instanceof String) 
            {
                return;
            }
            if(goldenValue.getClass().isArray()) {
                throw new IllegalStateException("implement me!");
            }
            for (Method m : goldenValue.getClass().getDeclaredMethods()) {
                if(m.getParameterTypes().length == 0 && 
                   (m.getName().startsWith("get") || 
                    m.getName().startsWith("is"))) 
                {
                    Object gv = m.invoke(goldenValue);
                    Object v = m.invoke(value);                    
                    if(!equals(gv, v)) { 
                        fail("(one of the) value(s) returned by " + method + " returns via " + m + " [" + goldenValue + "] instead [" + value + "]");
                    }
                } else if(!m.getName().startsWith("get") && isOverriden(goldenValue.getClass().getSuperclass(), m)) {
                    throw new IllegalStateException("not yet tested method " + m + " on " + goldenValue.getClass().getName());
                }
            } 
        }

        private boolean equals(Object goldenValue, Object value) {
            if(goldenValue == null && value == null) {
                return true;
            }
            if(goldenValue == null || value == null) {
                return false;
            }
            return goldenValue.equals(value);
        }
        
        private boolean isOverriden(Class clazz, Method m) {
            if(clazz == null) {
                return false;
            }
            for(Method om : clazz.getDeclaredMethods()) {
                if(om.equals(m)) {
                    return true;
                }
            }
            return isOverriden(clazz.getSuperclass(), m);
        }
            
    }
    
    public static class C2CDataTestCase extends AbstractC2CDataTestCase {

        public C2CDataTestCase(String n) {
            super(n);
        }
        
        public void testGetC2CData() throws Exception {
            StringBuilder sb = new StringBuilder();

            // load configuration 
            expectQuery(repositoryURL() + "/repositoryContext", sb, REPOSITORY_CONFIGURATION_RESPONSE);
            C2CData data = getData(true, sb);
            // and assert all values are populated
            assertData(data, false);

            // clear the configuration, 
            clearData(data);
            // access configuration without forcing server refresh 
            expectQuery(repositoryURL() + "/repositoryContext", sb, REPOSITORY_CONFIGURATION_RESPONSE);
            data = getData(false, sb);
            // and assert that all values are emty
            assertData(data, true);

            // access configuration by forcing server refresh 
            data = getData(true, sb);
            // and assert that all values are populated
            assertData(data, false);
        }
    }
    
    private static abstract class AbstractC2CDataTestCase extends C2CTestBase {
        private RepositoryConfiguration repositoryConfiguration;
        public AbstractC2CDataTestCase(String n) {
            super(n);
        }

        protected C2CData getData(boolean forceRefresh, StringBuilder sb) {
            C2CData data = C2CExtender.getData(rc, repository, forceRefresh);
            assertTrue("No JSON query should have sent.", sb.length() == 0);        
            assertNotNull(data);
            return data;
        }

        protected C2CData getData() {
            StringBuilder sb = new StringBuilder();
            expectQuery(repositoryURL() + "/repositoryContext", sb, REPOSITORY_CONFIGURATION_RESPONSE);
            return getData(true, sb);
        }

        protected void assertData(C2CData data, boolean empty) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            assertNotNull(data.getRepositoryConfiguration());
            
            for (Method m : RepositoryConfiguration.class.getDeclaredMethods()) {
                if(m.getParameterTypes().length == 0 && 
                   (m.getName().startsWith("get") ||
                    m.getName().startsWith("is"))) 
                {
                    
                    if(empty && m.getName().equals("getMarkupLanguage")) { // throws NPE if ConfigurationProperties == null
                        assertNull(data.getRepositoryConfiguration().getConfigurationProperties());
                        return;
                    }
                    Object value = m.invoke(data.getRepositoryConfiguration());
                    
                    if(value instanceof Collection) {
                        if(empty) {
                            if(!((Collection) value).isEmpty()) {
                                fail("method " + m + " should return no values, did insted " + value);
                            }
                        } else {
                            if(value == null) {
                                fail("method " + m + " should return something, but was null instead");
                            } 
                            if(((Collection) value).isEmpty()) {
                                fail("method " + m + " should return something, but was empty instead");
                            }
                        }
                        assertDataCollection((Collection) value, empty);
                    } else {
                        assertDefaultValue(value, empty);
                    }
                } else {
                    
                }
            }
        }

        private void assertDataCollection(Collection c, boolean isEmpty) {
            if(isEmpty) {
                assertTrue(c.isEmpty());
            } else {
                assertNotNull(c);
                assertFalse(c.isEmpty());
            }
        }

        private void assertDefaultValue(Object d, boolean isEmpty) {
            if(isEmpty) {
                assertNull(d);
            } else {
                assertNotNull(d);
            }
        }

        protected void clearData(C2CData data) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            for (Method m : RepositoryConfiguration.class.getDeclaredMethods()) {
                if(m.getName().startsWith("set")) {
                    m.invoke(data.getRepositoryConfiguration(), (Object) null);
                }
            }
        }

        protected synchronized RepositoryConfiguration getRepositoryConfiguration() throws IOException, ClassNotFoundException {
            if(repositoryConfiguration == null) {
                ObjectInputStream is = null;
                try {
                    is = new ObjectInputStream(this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/odcs/tasks/tck/repositoryConfiguration"));
                    int size = is.readInt();
                    repositoryConfiguration = (RepositoryConfiguration) is.readObject();
                } finally {
                    if (is != null) {
                        try { is.close(); } catch (IOException e) { }
                    }
                }
            }
            return repositoryConfiguration;
        }    

        final String REPOSITORY_CONFIGURATION_RESPONSE = 
            "{\"repositoryConfiguration\":"
                + "{\"users\":["
                    + "{\"loginName\":\"protester@oracle.com\",\"realname\":\"Tester Protester\",\"gravatarHash\":\"063a55b94edfbc51aab5d9b8b3316207\",\"id\":1},"
                    + "{\"loginName\":\"rester@oracle.com\",\"realname\":\"Resting Tester\",\"gravatarHash\":\"eb3b1d3818c31fd5b61a9e58da70f685\",\"id\":2}],"
                + "\"components\":[{\"name\":\"Another Component\",\"description\":\"Some Another Component\",\"product\":{\"id\":22},\"id\":42},{\"name\":\"Component1\",\"description\":\"The First Component\",\"product\":{\"id\":2},\"id\":6},{\"name\":\"Component2\",\"description\":\"The Second Component\",\"product\":{\"id\":2},\"id\":7},{\"name\":\"Component3\",\"description\":\"The Third Component\",\"product\":{\"id\":2},\"id\":8},{\"name\":\"Default\",\"description\":\"default component\",\"product\":{\"id\":1},\"initialOwner\":{\"loginName\":\"protester@oracle.com\",\"realname\":\"Tester Protester\",\"gravatarHash\":\"063a55b94edfbc51aab5d9b8b3316207\",\"id\":1},\"id\":1},{\"name\":\"Some Component\",\"description\":\"another component\",\"product\":{\"id\":1},\"id\":22}],"
                + "\"markupLanguage\":\"Textile\","
                + "\"url\":\"" + repositoryURL() + "\","
                + "\"milestones\":[{\"product\":{\"id\":1},\"value\":\"---\",\"sortkey\":0,\"id\":1},{\"product\":{\"id\":2},\"value\":\"---\",\"sortkey\":0,\"id\":3},{\"product\":{\"id\":22},\"value\":\"---\",\"sortkey\":0,\"id\":23},{\"product\":{\"id\":1},\"value\":\"0.0.1\",\"sortkey\":1,\"id\":2}],"
                + "\"customFields\":[{\"name\":\"OtherCustomField\",\"description\":\"Other Custom Field\",\"fieldType\":\"TEXT\",\"availableForNewTasks\":true,\"obsolete\":false,\"id\":84},{\"name\":\"customField\",\"description\":\"Custom Field\",\"fieldType\":\"TEXT\",\"availableForNewTasks\":false,\"obsolete\":false,\"id\":63}],"
                + "\"priorities\":[{\"active\":false,\"value\":\"Highest\",\"sortkey\":100,\"id\":1},{\"active\":false,\"value\":\"High\",\"sortkey\":200,\"id\":2},{\"active\":false,\"value\":\"Normal\",\"sortkey\":300,\"id\":3},{\"active\":false,\"value\":\"Low\",\"sortkey\":400,\"id\":4},{\"active\":false,\"value\":\"Lowest\",\"sortkey\":500,\"id\":5}],"
                + "\"severities\":[{\"value\":\"blocker\",\"sortkey\":100,\"id\":1},{\"value\":\"critical\",\"sortkey\":200,\"id\":2},{\"value\":\"major\",\"sortkey\":300,\"id\":3},{\"value\":\"normal\",\"sortkey\":400,\"id\":4},{\"value\":\"minor\",\"sortkey\":500,\"id\":5},{\"value\":\"trivial\",\"sortkey\":600,\"id\":6},{\"value\":\"enhancement\",\"sortkey\":700,\"id\":7}],"
                + "\"resolutions\":[{\"value\":\"\",\"sortkey\":100,\"id\":1},{\"value\":\"FIXED\",\"sortkey\":200,\"id\":2},{\"value\":\"INVALID\",\"sortkey\":300,\"id\":3},{\"value\":\"WONTFIX\",\"sortkey\":400,\"id\":4},{\"value\":\"DUPLICATE\",\"sortkey\":500,\"id\":5},{\"value\":\"WORKSFORME\",\"sortkey\":600,\"id\":6},{\"value\":\"NEEDINFO\",\"sortkey\":700,\"id\":7}],"
                + "\"statuses\":[{\"open\":true,\"active\":true,\"value\":\"UNCONFIRMED\",\"sortkey\":100,\"id\":1},{\"open\":true,\"active\":true,\"value\":\"NEW\",\"sortkey\":200,\"id\":2},{\"open\":true,\"active\":true,\"value\":\"ASSIGNED\",\"sortkey\":300,\"id\":3},{\"open\":true,\"active\":true,\"value\":\"REOPENED\",\"sortkey\":400,\"id\":4},{\"open\":false,\"active\":true,\"value\":\"RESOLVED\",\"sortkey\":500,\"id\":5},{\"open\":false,\"active\":true,\"value\":\"VERIFIED\",\"sortkey\":600,\"id\":6},{\"open\":false,\"active\":true,\"value\":\"CLOSED\",\"sortkey\":700,\"id\":7}],"
                + "\"configurationProperties\":{\"MARKUP_LANGUAGE\":\"Textile\"},"
                + "\"products\":["
                    + "{\"name\":\"Another Product\","
                     + "\"description\":\"Some Another Product\","
                     + "\"components\":["
                        + "{\"name\":\"Another Component\","
                        + "\"description\":\"Some Another Component\","
                        + "\"product\":{\"id\":22},\"id\":42}],"
                     + "\"milestones\":["
                        + "{\"product\":{\"id\":22},\"value\":\"---\",\"sortkey\":0,\"id\":23}],"
                     + "\"releaseTags\":[],"
                     + "\"isActive\":true,"
                     + "\"defaultMilestone\":"
                        + "{\"product\":{\"id\":22},\"value\":\"---\",\"sortkey\":0,\"id\":23},"
                     + "\"defaultComponent\":"
                        + "{\"name\":\"Another Component\","
                        + "\"description\":\"Some Another Component\","
                        + "\"product\":{\"id\":22},\"id\":42},\"id\":22},"
                    + "{\"name\":\"Default\","
                     + "\"description\":\"default product\","
                     + "\"components\":["
                        + "{\"name\":\"Default\","
                        + "\"description\":\"default component\","
                        + "\"product\":{\"id\":1},"
                        + "\"initialOwner\":{\"loginName\":\"protester@oracle.com\",\"realname\":\"Tester Protester\",\"gravatarHash\":\"063a55b94edfbc51aab5d9b8b3316207\",\"id\":1},\"id\":1},"
                        + "{\"name\":\"Some Component\","
                        + "\"description\":\"another component\","
                        + "\"product\":{\"id\":1},\"id\":22}],"
                     + "\"milestones\":["
                        + "{\"product\":{\"id\":1},\"value\":\"---\",\"sortkey\":0,\"id\":1},"
                        + "{\"product\":{\"id\":1},\"value\":\"0.0.1\",\"sortkey\":1,\"id\":2}],"
                     + "\"releaseTags\":[\"bfgnhg\",\"cdscds\"],"
                     + "\"isActive\":true,"
                     + "\"defaultMilestone\":{\"product\":{\"id\":1},\"value\":\"0.0.1\",\"sortkey\":1,\"id\":2},"
                     + "\"defaultComponent\":{\"name\":\"Default\",\"description\":\"default component\",\"product\":{\"id\":1},"
                     + "\"initialOwner\":{\"loginName\":\"protester@oracle.com\",\"realname\":\"Tester Protester\",\"gravatarHash\":\"063a55b94edfbc51aab5d9b8b3316207\",\"id\":1},\"id\":1},\"id\":1},"
                    + "{\"name\":\"Unit Test Product\","
                     + "\"description\":\"Product used for unit tests from NetBeans\","
                     + "\"components\":["
                        + "{\"name\":\"Component3\","
                        + "\"description\":\"The Third Component\","
                        + "\"product\":{\"id\":2},\"id\":8},"
                        + "{\"name\":\"Component1\","
                        + "\"description\":\"The First Component\","
                        + "\"product\":{\"id\":2},\"id\":6},"
                        + "{\"name\":\"Component2\","
                        + "\"description\":\"The Second Component\","
                        + "\"product\":{\"id\":2},\"id\":7}],"
                     + "\"milestones\":["
                        + "{\"product\":{\"id\":2},\"value\":\"---\",\"sortkey\":0,\"id\":3}],"
                     + "\"releaseTags\":[],"
                     + "\"isActive\":true,"
                     + "\"defaultMilestone\":{\"product\":{\"id\":2},\"value\":\"---\",\"sortkey\":0,\"id\":3},"
                     + "\"defaultComponent\":"
                        + "{\"name\":\"Component3\","
                        + "\"description\":\"The Third Component\","
                        + "\"product\":{\"id\":2},\"id\":8},\"id\":2}],"
                + "\"keywords\":["
                    + "{\"name\":\"Plan\",\"description\":\"Plan\",\"id\":1},"
                    + "{\"name\":\"Release\",\"description\":\"Release\",\"id\":2},"
                    + "{\"name\":\"Epic\",\"description\":\"Epic\",\"id\":3}],"
                 + "\"taskTypes\":[\"Task\",\"Defect\",\"Feature\"],"
                 + "\"iterations\":["
                    + "{\"value\":\"---\",\"isActive\":false,\"sortkey\":0,\"id\":1},"
                    + "{\"value\":\"1\",\"isActive\":true,\"sortkey\":10,\"id\":2},"
                    + "{\"value\":\"2\",\"isActive\":true,\"sortkey\":20,\"id\":3},"
                    + "{\"value\":\"3\",\"isActive\":true,\"sortkey\":30,\"id\":4},"
                    + "{\"value\":\"4\",\"isActive\":true,\"sortkey\":40,\"id\":5},"
                    + "{\"value\":\"5\",\"isActive\":true,\"sortkey\":50,\"id\":6},"
                    + "{\"value\":\"6\",\"isActive\":true,\"sortkey\":60,\"id\":7},"
                    + "{\"value\":\"7\",\"isActive\":true,\"sortkey\":70,\"id\":8},"
                    + "{\"value\":\"8\",\"isActive\":true,\"sortkey\":80,\"id\":9},"
                    + "{\"value\":\"9\",\"isActive\":true,\"sortkey\":90,\"id\":10},"
                    + "{\"value\":\"10\",\"isActive\":true,\"sortkey\":100,\"id\":11},"
                    + "{\"value\":\"11\",\"isActive\":false,\"sortkey\":101,\"id\":12}],"
                + "\"defaultType\":\"Task\","
                + "\"activeIterations\":["
                    + "{\"value\":\"1\",\"isActive\":true,\"sortkey\":10,\"id\":2},"
                    + "{\"value\":\"2\",\"isActive\":true,\"sortkey\":20,\"id\":3},"
                    + "{\"value\":\"3\",\"isActive\":true,\"sortkey\":30,\"id\":4},"
                    + "{\"value\":\"4\",\"isActive\":true,\"sortkey\":40,\"id\":5},"
                    + "{\"value\":\"5\",\"isActive\":true,\"sortkey\":50,\"id\":6},"
                    + "{\"value\":\"6\",\"isActive\":true,\"sortkey\":60,\"id\":7},"
                    + "{\"value\":\"7\",\"isActive\":true,\"sortkey\":70,\"id\":8},"
                    + "{\"value\":\"8\",\"isActive\":true,\"sortkey\":80,\"id\":9},"
                    + "{\"value\":\"9\",\"isActive\":true,\"sortkey\":90,\"id\":10},"
                    + "{\"value\":\"10\",\"isActive\":true,\"sortkey\":100,\"id\":11}],"
                + "\"defaultIteration\":{\"value\":\"---\",\"isActive\":false,\"sortkey\":0,\"id\":1},"
                + "\"defaultStatus\":{\"open\":true,\"active\":true,\"value\":\"UNCONFIRMED\",\"sortkey\":100,\"id\":1},"
                + "\"defaultPriority\":{\"active\":false,\"value\":\"Normal\",\"sortkey\":300,\"id\":3},"
                + "\"defaultSeverity\":{\"value\":\"normal\",\"sortkey\":400,\"id\":4},"
                + "\"defaultProduct\":"
                    + "{\"name\":\"Another Product\","
                     + "\"description\":\"Some Another Product\","
                     + "\"components\":["
                        + "{\"name\":\"Another Component\","
                        + "\"description\":\"Some Another Component\","
                        + "\"product\":{\"id\":22},\"id\":42}],"
                    + "\"milestones\":[{\"product\":{\"id\":22},\"value\":\"---\",\"sortkey\":0,\"id\":23}],"
                    + "\"releaseTags\":[],"
                    + "\"isActive\":true,"
                    + "\"defaultMilestone\":{\"product\":{\"id\":22},\"value\":\"---\",\"sortkey\":0,\"id\":23},"
                    + "\"defaultComponent\":"
                        + "{\"name\":\"Another Component\","
                        + "\"description\":\"Some Another Component\","
                        + "\"product\":{\"id\":22},\"id\":42},\"id\":22},"
                    + "\"defaultResolution\":{\"value\":\"FIXED\",\"sortkey\":200,\"id\":2},"
                + "\"stateTransitions\":["
                    + "{\"initialStatus\":null,\"newStatus\":\"UNCONFIRMED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":null,\"newStatus\":\"NEW\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"REOPENED\",\"newStatus\":\"NEW\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"ASSIGNED\",\"newStatus\":\"NEW\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"UNCONFIRMED\",\"newStatus\":\"NEW\",\"commentRequired\":false},"
                    + "{\"initialStatus\":null,\"newStatus\":\"ASSIGNED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"REOPENED\",\"newStatus\":\"ASSIGNED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"NEW\",\"newStatus\":\"ASSIGNED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"UNCONFIRMED\",\"newStatus\":\"ASSIGNED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"CLOSED\",\"newStatus\":\"REOPENED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"VERIFIED\",\"newStatus\":\"REOPENED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"RESOLVED\",\"newStatus\":\"REOPENED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"CLOSED\",\"newStatus\":\"RESOLVED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"VERIFIED\",\"newStatus\":\"RESOLVED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"REOPENED\",\"newStatus\":\"RESOLVED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"ASSIGNED\",\"newStatus\":\"RESOLVED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"NEW\",\"newStatus\":\"RESOLVED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"UNCONFIRMED\",\"newStatus\":\"RESOLVED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"RESOLVED\",\"newStatus\":\"VERIFIED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"VERIFIED\",\"newStatus\":\"CLOSED\",\"commentRequired\":false},"
                    + "{\"initialStatus\":\"RESOLVED\",\"newStatus\":\"CLOSED\",\"commentRequired\":false}],"
                + "\"savedTaskQueries\":["
                    + "{\"name\":\"tt10\",\"queryString\":\"(summary CONTAINS 'test' OR description CONTAINS 'test' OR comment CONTAINS 'test') AND (summary CONTAINS 'test' OR description CONTAINS 'test' OR comment CONTAINS 'test') AND (summary CONTAINS 'test' OR description CONTAINS 'test' OR comment CONTAINS 'test')\",\"defaultSort\":{\"sortField\":\"taskId\",\"sortOrder\":\"ASCENDING\"},\"id\":42},"
                    + "{\"name\":\"Q1\",\"queryString\":\"productName = 'Unit Test Product' AND (componentName = 'Component1' OR componentName = 'Component3')\",\"id\":2},{\"name\":\"Test Tasks\",\"queryString\":\"(summary CONTAINS 'test' OR description CONTAINS 'test' OR comment CONTAINS 'test')\",\"id\":21}]}}";    
        
        
    }
    
    public static class JSONDumper extends AbstractC2CDataTestCase {
        public JSONDumper(String n) {
            super(n);
        }
        public void testDump() throws IOException, URISyntaxException {
            StringBuilder sb = new StringBuilder();
            C2CDataTestCase t = new C2CDataTestCase("dump");
            t.expectQuery(t.repositoryURL() + "/repositoryContext", sb, t.REPOSITORY_CONFIGURATION_RESPONSE);
            C2CData data = t.getData(false, sb);
            ObjectOutputStream os = null;
            try {
                os = new ObjectOutputStream(new FileOutputStream(new File("/tmp/repositoryConfiguration")));
                os.writeInt(1); // one configuration
                os.writeObject(data.getRepositoryConfiguration());
            } finally {
                if (os != null) {
                    try { os.close(); } catch (IOException e) { }
                }
            }
        }
    }
    
}
