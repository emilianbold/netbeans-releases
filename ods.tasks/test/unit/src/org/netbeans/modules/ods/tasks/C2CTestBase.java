/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only (\"GPL\") or the Common
 * Development and Distribution License(\"CDDL\") (collectively, the
 * \"License\"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the \"Classpath\" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * \"Portions Copyrighted [year] [name of copyright owner]\"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * \"[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license.\" If you do not indicate a
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
package org.netbeans.modules.ods.tasks;

import java.io.IOException;
import java.util.Iterator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ods.tasks.spi.C2CExtender;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class C2CTestBase extends NbTestCase {
    protected final AbstractRepositoryConnector rc;
    protected final TaskRepository repository;
    private final String url;
    private final C2CTestInfrastructure infra;
    
    public C2CTestBase(String n) {
        super(n);
        infra = Lookup.getDefault().lookup(C2CTestInfrastructure.class);
        assertNotNull("An implementation of the C2CTestInfrastructure must be present", infra);
        url = infra.initializeRepository();
        rc = C2CExtender.create();
        repository = new TaskRepository(rc.getConnectorKind(), repositoryURL());
        C2CExtender.assignTaskRepositoryLocationFactory(rc, new TaskRepositoryLocationFactory());
    }

    protected final void expectQuery(String url, Appendable response, String reply) {
        infra.expectQuery(url, response, reply);
    }
    
    protected final AbstractRepositoryConnector repositoryConnector() {
        return rc;
    }
    
    protected final TaskRepository repository() {
        return repository;
    }
    
    protected final String repositoryURL() {
        return url;
    }
    
    /** Compares two strings containing JSON structures for being the same
     * @param msg describe what is your expectation
     * @param exp the expected value
     * @param real the real value to compare with <code>exp</code> one
     */
    protected static void assertJSON(String msg, String exp, String real) throws IOException  {
        ObjectMapper m = new ObjectMapper();
        JsonNode expNode = m.readTree(exp);
        JsonNode realNode = m.readTree(real);
        
        
        assertEquals("Names length is the same", expNode.size(), realNode.size());
        
        StringBuilder same = new StringBuilder();
        JsonNode[] diff = { null, null };
        if (!assertSame(expNode, realNode, same, "", diff)) {
            fail(same.toString() + "\nFirst node:\n" + diff[0] + "\nSecond node:\n" + diff[1]);
        }
        
    }
    /**
     * Asserts value of a task attribute.
     * 
     * @param value expected value of the attribute
     * @param root the root to obtain the attribute from
     * @param attrId the name of attribute to seek in the root
     */
    protected static void assertAttribute(String value, TaskAttribute root, String attrId) {
        TaskAttribute v = root.getAttribute(attrId);
        assertNotNull("Attribute " + attrId + " should be present", v);
        assertEquals("Attribute " + attrId + " should have the right value", value, v.getValue());
    }
    
    private static boolean assertSame(
        JsonNode expNode, JsonNode realNode, StringBuilder same, String indent, JsonNode[] diff
    ) {
        Iterator<String> it = expNode.getFieldNames();
        while (it.hasNext()) {
            String n = it.next();
            
            JsonNode expN = expNode.get(n);
            JsonNode realN = realNode.get(n);
            
            same.append(indent);
            if (expN.equals(realN)) {
                same.append("Field ").append(n).append(" is the same\n").append(indent);
            } else {
                same.append("Differences in field ").append(n).append("\n");
                diff[0] = expN;
                diff[1] = realN;
                assertSame(expN, realN, same, indent + "  ", diff);
                return false;
            }
        }
        return true;
    }
}
