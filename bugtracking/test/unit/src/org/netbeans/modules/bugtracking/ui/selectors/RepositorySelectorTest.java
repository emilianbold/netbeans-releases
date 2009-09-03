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

package org.netbeans.modules.bugtracking.ui.selectors;

import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class RepositorySelectorTest extends NbTestCase {

    public RepositorySelectorTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testRepositorySelectorBuilder() throws MalformedURLException, CoreException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String errorMsg = "tpyo";
        Repository repo = new MyRepository();

        SelectorPanel sp = new SelectorPanel();
        createEditDescriptor(sp, repo, errorMsg);

        String text = getErrroLabelText(sp);
        assertEquals(errorMsg, text);
    }

    private void createEditDescriptor(SelectorPanel sp, Repository repository, String error) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = sp.getClass().getDeclaredMethod("createEditDescriptor", Repository.class, String.class);
        m.setAccessible(true);
        m.invoke(sp, repository, error);
    }


    private String getErrroLabelText(SelectorPanel sp) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        RepositorySelectorBuilder builder = (RepositorySelectorBuilder) getField(sp, "builder");
        RepositoryFormPanel form = (RepositoryFormPanel) getField(builder, "repositoryFormsPanel");
        JLabel label = (JLabel) getField(form, "errorLabel");
        return label.getText();
    }

    private Object getField(Object o, String name) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    private class MyRepository extends Repository {

        @Override
        public Image getIcon() {
            return null;
        }

        @Override
        public String getID() {
            return "repoid";
        }

        @Override
        public String getDisplayName() {
            return "My repository";
        }

        @Override
        public String getTooltip() {
            return "My repository";
        }

        @Override
        public String getUrl() {
            return "http://foo.bar/bogus";
        }

        @Override
        public Issue getIssue(String id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BugtrackingController getController() {
            final JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("<html>" +
                            getDisplayName() + "</br>" +
                            getUrl() +
                          "</html>");
            panel.add(label);
            return new BugtrackingController() {

                @Override
                public JComponent getComponent() {
                    return panel;
                }

                @Override
                public HelpCtx getHelpCtx() {
                    return null;
                }

                @Override
                public boolean isValid() {
                    return false;
                }

                @Override
                public void applyChanges() throws IOException {

                }
            };
        }

        @Override
        public Query createQuery() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Issue createIssue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Query[] getQueries() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Issue[] simpleSearch(String criteria) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected IssueCache getIssueCache() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Lookup getLookup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<RepositoryUser> getUsers() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
