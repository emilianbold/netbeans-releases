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

package org.netbeans.modules.bugtracking.util;

import java.awt.EventQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.bugtracking.dummies.DummyBugtrackingConnector;
import org.netbeans.modules.bugtracking.dummies.DummyBugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.dummies.DummyKenaiRepositories;
import org.netbeans.modules.bugtracking.dummies.DummyNode;
import org.netbeans.modules.bugtracking.dummies.DummyTopComponentRegistry;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.RepositoryComboSupport.Progress;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import static org.junit.Assert.*;
import static org.netbeans.modules.bugtracking.util.RepositoryComboSupport.LOADING_REPOSITORIES;
import static org.netbeans.modules.bugtracking.util.RepositoryComboSupport.SELECT_REPOSITORY;
import static org.netbeans.modules.bugtracking.util.RepositoryComboSupportTest.ThreadType.AWT;
import static org.netbeans.modules.bugtracking.util.RepositoryComboSupportTest.ThreadType.NON_AWT;

/**
 *
 * @author Marian Petras
 */
public class RepositoryComboSupportTest {

    private volatile JComboBox comboBox;
    private volatile RepositoryComboSupport comboSupport;

    @BeforeClass
    public static void setLookup() {
        System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
    }

    @Before
    public void createJComboBox() {
        comboBox = new JComboBox();
    }

    @After
    public void tidyUp() {
        comboBox = null;
        comboSupport = null;
        getBugtrackingConnector().reset();
        getTopComponentRegistry().reset();
    }

    private static DummyBugtrackingConnector getBugtrackingConnector() {
        return Lookup.getDefault().lookup(DummyBugtrackingConnector.class);
    }

    private static DummyTopComponentRegistry getTopComponentRegistry() {
        return Lookup.getDefault().lookup(DummyTopComponentRegistry.class);
    }

    abstract class AbstractRepositoryComboTest {

        protected final DummyBugtrackingConnector connector = getBugtrackingConnector();

        protected Node node1 = new DummyNode("node1");
        protected Node node2 = new DummyNode("node2");
        protected Node node3 = new DummyNode("node3");
        protected Node repoNode1;
        protected Node repoNode2;
        protected Node repoNode3;
        protected Repository repository1;
        protected Repository repository2;
        protected Repository repository3;

        protected void createRepository1() {
            repository1 = connector.createRepository("alpha");
            repoNode1 = new DummyNode("node1", repository1);
        }

        protected void createRepository2() {
            repository2 = connector.createRepository("beta");
            repoNode2 = new DummyNode("node2", repository2);
        }

        protected void createRepository3() {
            repository3 = connector.createRepository("gamma");
            repoNode3 = new DummyNode("node3", repository3);
        }

        protected void createRepositories() {
            createRepository1();
            createRepository2();
            createRepository3();
        }

        protected void setUpEnvironment() {
            //the default implementation does nothing
        }

        abstract RepositoryComboSupport setupComboSupport(JComboBox comboBox);

        protected void scheduleTests(ProgressTester progressTester) {
            progressTester.scheduleTest          (Progress.STARTED, AWT,
                                                    new ComboBoxItemsTest(
                                                            LOADING_REPOSITORIES));
            progressTester.scheduleTest          (Progress.WILL_LOAD_REPOS, NON_AWT);
            progressTester.scheduleTest          (Progress.LOADED_REPOS, NON_AWT);
            progressTester.scheduleTest          (Progress.WILL_SCHEDULE_DISPLAY_OF_REPOS, NON_AWT);
            progressTester.scheduleSuspendingTest(Progress.SCHEDULED_DISPLAY_OF_REPOS, NON_AWT);
            progressTester.scheduleTest          (Progress.WILL_DISPLAY_REPOS, AWT);
        }

    }

    abstract class SingleRepoComboTest extends AbstractRepositoryComboTest {
        @Override
        protected void createRepositories() {
            createRepository1();
        }
    }

    @Test(timeout=10000)
    public void testNoRepoAvailable() throws InterruptedException {
        printTestName("testNoRepoAvailable");
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            @Override
            protected void createRepositories() {
                //do not create any repository
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, true);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                RepositoryComboSupport.NO_REPOSITORIES),
                                                        new SelectedItemTest(
                                                                RepositoryComboSupport.NO_REPOSITORIES));
            }
        });
    }

    /**
     * Tests that the routine for determination if initial repository selection
     * is skipped (not performed) if there is only one repository available
     * and method {@code setup()} was passed {@code true} as its third argument.
     *
     * @see #testSingleRepoNoNodeFalse
     * @see #testSingleRepoNoMatchingNodeFalse
     * @see #testSingleRepoMatchingNodeFalse
     */
    @Test(timeout=10000)
    public void testSingleRepoTrue() throws InterruptedException {
        printTestName("testSingleRepoNoNodeTrue");
        runRepositoryComboTest(new SingleRepoComboTest() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, true);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                repository1),
                                                        new SelectedItemTest(
                                                                repository1));
            }
        });
    }

    /**
     * Tests that the routine for determination if initial repository selection
     * is performed (not skipped) if there is only one repository available
     * and method {@code setup()} was passed {@code false} as its third
     * argument. It also tests that the routine correctly determines no
     * repository should be preselected if there is no node selected.
     */
    @Test(timeout=10000)
    public void testSingleRepoNoNodeFalse() throws InterruptedException {
        printTestName("testSingleRepoNoNodeFalse");
        runRepositoryComboTest(new SingleRepoComboTest() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                SELECT_REPOSITORY,
                                                                repository1),
                                                        new SelectedItemTest(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
            }
        });
    }

    /**
     * Tests that the routine for determination if initial repository selection
     * is performed (not skipped) if there is only one repository available
     * and method {@code setup()} was passed {@code false} as its third
     * argument. It also tests that the routine correctly determines no
     * repository should be preselected if there is no node selected that
     * would refer to the repository.
     */
    @Test(timeout=10000)
    public void testSingleRepoNoMatchingNodeFalse() throws InterruptedException {
        printTestName("testSingleRepoNoMatchingNodeFalse");
        runRepositoryComboTest(new SingleRepoComboTest() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(node1);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                SELECT_REPOSITORY,
                                                                repository1),
                                                        new SelectedItemTest(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
            }
        });
    }

    /**
     * Tests that the routine for determination if initial repository selection
     * is performed (not skipped) if there is only one repository available
     * and method {@code setup()} was passed {@code false} as its third
     * argument. It also tests that the routine correctly determines that
     * the repository should be preselected.
     */
    @Test(timeout=10000)
    public void testSingleRepoMatchingNodeFalse() throws InterruptedException {
        printTestName("testSingleRepoMatchingNodeFalse");
        runRepositoryComboTest(new SingleRepoComboTest() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(repoNode1);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                SELECT_REPOSITORY,
                                                                repository1),
                                                        new SelectedItemTest(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SCHEDULE_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleSuspendingTest(Progress.SCHEDULED_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SELECT_DEFAULT_REPO, AWT);
                progressTester.scheduleResumingTest  (Progress.SELECTED_DEFAULT_REPO, AWT,
                                                        new ComboBoxItemsTest(
                                                                repository1),
                                                        new SelectedItemTest(
                                                                repository1));
            }
        });
    }

    /**
     * This test and test {@code testTwoReposNoMatchingNodeTrue} verify that
     * value of parameter {@code selectRepoIfSingle} of method
     * {@code setup()} has no impact if there are two repositories.
     */
    @Test(timeout=10000)
    public void testMoreReposNoMatchingNodeFalse() throws InterruptedException {
        printTestName("testMoreReposNoMatchingNodeFalse");
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(node1);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                SELECT_REPOSITORY,
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
            }
        });
    }

    /**
     * This test and test {@code testTwoReposNoMatchingNodeFalse} verify that
     * value of parameter {@code selectRepoIfSingle} of method
     * {@code setup()} has no impact if there are two repositories.
     */
    @Test(timeout=10000)
    public void testMoreReposNoMatchingNodeTrue() throws InterruptedException {
        printTestName("testMoreReposNoMatchingNodeTrue");
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(node1);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, true);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                SELECT_REPOSITORY,
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
            }
        });
    }

    /**
     * Tests that the correct repository is preselected in the combo-box if
     * there is one node selected and the node refers to some issue tracking
     * repository.
     */
    @Test(timeout=10000)
    public void testMoreReposMatchingNode() throws InterruptedException {
        printTestName("testMoreReposMatchingNode");
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(repoNode2);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                SELECT_REPOSITORY,
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SCHEDULE_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleSuspendingTest(Progress.SCHEDULED_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SELECT_DEFAULT_REPO, AWT);
                progressTester.scheduleResumingTest  (Progress.SELECTED_DEFAULT_REPO, AWT,
                                                        new ComboBoxItemsTest(
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                repository2));
            }
        });
    }

    /**
     * Tests that the correct repository is preselected in the combo-box if
     * there are multiple nodes selected but all the nodes refer to the same
     * issue tracking repository.
     */
    @Test(timeout=10000)
    public void testMoreReposMoreMatchingNodesSameRepo() throws InterruptedException {
        printTestName("testMoreReposMoreMatchingNodesSameRepo");
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            private Node repoNode2_2;
            @Override
            protected void createRepositories() {
                super.createRepositories();
                repoNode2_2 = new DummyNode("beta 2", repository2);
            }
            @Override
            protected void setUpEnvironment() {
                selectNodes(repoNode2);
                selectNodes(repoNode2_2);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, true);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                SELECT_REPOSITORY,
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SCHEDULE_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleSuspendingTest(Progress.SCHEDULED_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SELECT_DEFAULT_REPO, AWT);
                progressTester.scheduleResumingTest  (Progress.SELECTED_DEFAULT_REPO, AWT,
                                                        new ComboBoxItemsTest(
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                repository2));
            }
        });
    }

    /**
     * Tests that no repository is preselected in the combo-box if multiple
     * nodes are selected and the nodes refer to several different issue
     * tracking repositories.
     */
    @Test(timeout=10000)
    public void testMoreReposMatchingNodesDifferentRepos() throws InterruptedException {
        printTestName("testMoreReposMatchingNodesDifferentRepos");
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(repoNode2, repoNode3);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                SELECT_REPOSITORY,
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
            }
        });
    }

    /**
     * Tests that no repository is preselected in the combo-box if multiple
     * nodes are selected but not all of the nodes refer to the some repository.
     */
    @Test(timeout=10000)
    public void testMoreReposMatchingNodesRepoAndNull() throws InterruptedException {
        printTestName("testMoreReposMatchingNodesRepoAndNull");
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(repoNode2, node3);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                SELECT_REPOSITORY,
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SCHEDULE_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleSuspendingTest(Progress.SCHEDULED_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SELECT_DEFAULT_REPO, AWT);
                progressTester.scheduleResumingTest  (Progress.SELECTED_DEFAULT_REPO, AWT,
                                                        new ComboBoxItemsTest(
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                repository2));
            }
        });
    }

    /**
     * Tests that setup of the combo-box is done by one job scheduled to the EDT
     * (event-dispatch thread) if all information (list of repositories,
     * plus the repository to be preselected, if any) is available at the moment
     * the EDT is about to display the list of available repositories.
     */
    @Test(timeout=10000)
    public void testMoreReposNoMatchingNodeAwtRetarded() throws InterruptedException {
        printTestName("testMoreReposNoMatchingNodeAwtRetarded");
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                progressTester.scheduleTest          (Progress.STARTED, AWT,
                                                        new ComboBoxItemsTest(
                                                                LOADING_REPOSITORIES));
                progressTester.scheduleTest          (Progress.WILL_LOAD_REPOS, NON_AWT);
                progressTester.scheduleTest          (Progress.LOADED_REPOS, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SCHEDULE_DISPLAY_OF_REPOS, NON_AWT);
                progressTester.scheduleSuspendingTest(Progress.WILL_DISPLAY_REPOS, AWT);
                progressTester.scheduleTest          (Progress.SCHEDULED_DISPLAY_OF_REPOS, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleResumingTest  (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                SELECT_REPOSITORY,
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                SELECT_REPOSITORY));
            }
        });
    }

    /**
     * Tests that the combo-box is filled and the default repository preselected
     * in one shot if all information is available at the moment the AWT thread
     * is about to display the list of available repositories.
     */
    @Test(timeout=10000)
    public void testMoreReposMatchingNodeAwtRetarded() throws InterruptedException {
        printTestName("testMoreReposMatchingNodeAwtRetarded");
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(repoNode2);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                progressTester.scheduleTest          (Progress.STARTED, AWT,
                                                        new ComboBoxItemsTest(
                                                                LOADING_REPOSITORIES));
                progressTester.scheduleTest          (Progress.WILL_LOAD_REPOS, NON_AWT);
                progressTester.scheduleTest          (Progress.LOADED_REPOS, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SCHEDULE_DISPLAY_OF_REPOS, NON_AWT);
                progressTester.scheduleSuspendingTest(Progress.WILL_DISPLAY_REPOS, AWT);
                progressTester.scheduleTest          (Progress.SCHEDULED_DISPLAY_OF_REPOS, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SCHEDULE_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleResumingTest  (Progress.SCHEDULED_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                repository2));
            }
        });
    }

    @Test(timeout=10000)
    public void testDefaultRepoExplicitlySet() throws InterruptedException {
        printTestName("testDefaultRepoExplicitlySet");
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, repository2);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                repository2));
            }
        });
    }

    private void printTestName(String testName) {
        System.out.println();
        System.out.println("--- " + testName + " ---");
    }

    private void runRepositoryComboTest(AbstractRepositoryComboTest test)
                                                throws InterruptedException {
        test.createRepositories();
        test.setUpEnvironment();
        comboSupport = test.setupComboSupport(comboBox);
        assertNotNull(comboSupport);
        assertSame(Progress.INITIALIZED, comboSupport.getProgress());

        final Object testLock = new Object();
        ProgressTester progressListener = new ProgressTester(testLock);
        test.scheduleTests(progressListener);
        progressListener.startListening();
        synchronized (testLock) {
            /*
             * testLock.notify() in ProgressTester must not be called
             * before testLock.wait() here - so we must schedule start
             * of the RepositoryComboSupport while we are holding the testLock,
             * i.e. in the synchronized block.
             */
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    comboSupport.start();
                }
            });
            testLock.wait();
        }

        if (progressListener.failure != null) {
            if (progressListener.failure instanceof Error) {
                throw (Error) progressListener.failure;
            }
            if (progressListener.failure instanceof RuntimeException) {
                throw (RuntimeException) progressListener.failure;
            }
            assert false;
            fail(progressListener.failure.getClass().getName() + ": "
                 + progressListener.failure.getMessage());
        }
    }

    private static void selectNodes(Node... nodes) {
        if ((nodes == null) || (nodes.length == 0)) {
            throw new IllegalArgumentException("specify a non-empty list of nodes");
        }
        getTopComponentRegistry().setNodes(nodes);
    }

    public static final class TestLookup extends AbstractLookup {
        public TestLookup() {
            this(new InstanceContent());
        }
        private TestLookup(InstanceContent ic) {
            super(ic);
            ic.add(new DummyKenaiRepositories());
            ic.add(new DummyBugtrackingConnector());
            ic.add(new DummyTopComponentRegistry());
            ic.add(new DummyBugtrackingOwnerSupport());
        }
    }

    static enum ThreadType {
        AWT(true, "an AWT"),
        NON_AWT(false, "a non-AWT");

        private final boolean boolValue;
        private final String displayName;
        ThreadType(boolean boolValue, String displayName) {
            this.boolValue = boolValue;
            this.displayName = displayName;
        }
        boolean booleanValue() {
            return boolValue;
        }
        static ThreadType forBoolean(boolean booleanValue) {
            return (booleanValue == AWT.boolValue) ? AWT : NON_AWT;
        }
        String getDisplayName() {
            return displayName;
        }
    }

    final class ComboBoxItemsTest implements Runnable {

        private final Object[] expectedItems;

        ComboBoxItemsTest(Object... expectedItems) {
            this.expectedItems = expectedItems;
        }

        public void run() {
            System.out.println("Comparing combo-box items.");
            Object[] actualItems = getActualItems();
            assertArrayEquals("Different content of the combo-box expected",
                              expectedItems, actualItems);
        }

        private Object[] getActualItems() {
            Object[] result = new Object[comboBox.getItemCount()];
            for (int i = 0; i < result.length; i++) {
                result[i] = comboBox.getItemAt(i);
            }
            return result;
        }

    }

    final class SelectedItemTest implements Runnable {

        private final Object expectedSelectedItem;

        SelectedItemTest(Object expectedSelectedItem) {
            this.expectedSelectedItem = expectedSelectedItem;
        }

        public void run() {
            System.out.println("Checking selected combo-box item.");
            assertSame("Different item should be selected in the combo-box",
                       expectedSelectedItem, comboBox.getSelectedItem());
        }

    }

    static final class ProgressTest {
        static final Boolean SUSPENDING = Boolean.TRUE;
        static final Boolean RESUMING = Boolean.FALSE;
        private final Progress progressState;
        private final ThreadType threadType;
        private final Runnable[] additionalTests;
        private final Boolean synchrType;
        private final StackTraceElement[] callStack;
        ProgressTest(Progress progressState,
                     ThreadType threadType) {
            this(progressState, threadType, (Runnable[]) null);
        }
        ProgressTest(Progress progressState,
                     ThreadType threadType,
                     Runnable... additionalTests) {
            this(null, progressState, threadType, additionalTests);
        }
        ProgressTest(Boolean synchronizationType,
                     Progress progressState,
                     ThreadType threadType,
                     Runnable... additionalTests) {
            this.progressState = progressState;
            this.threadType = threadType;
            this.additionalTests
                    = (additionalTests != null) && (additionalTests.length != 0)
                      ? additionalTests
                      : null;
            this.synchrType = synchronizationType;
            this.callStack = Thread.currentThread().getStackTrace();
        }
        boolean isSuspending() {
            return synchrType == SUSPENDING;
        }
        boolean isResuming() {
            return synchrType == RESUMING;
        }
        @Override
        public String toString() {
            return "ProgressTest(" + getParamString() + ')';
        }

        private String getParamString() {
            StringBuilder buf = new StringBuilder(100);
            buf.append(progressState).append(", ").append(threadType);
            if (additionalTests != null) {
                for (Runnable additionalTest : additionalTests) {
                    buf.append(", ");
                    buf.append(additionalTest.getClass().getSimpleName());
                }
            }
            if (isSuspending()) {
                buf.append(", suspending");
            } else if (isResuming()) {
                buf.append(", resuming");
            }
            return buf.toString();
        }
    }

    private final class ProgressTester implements ChangeListener {

        private final Object testLock;
        private final Object suspendedThreadLock = new Object();
        private final Queue<ProgressTest> progressTestQueue
                                  = new ConcurrentLinkedQueue<ProgressTest>();
        private volatile boolean listening;
        private volatile Throwable failure;
        private ProgressTest pendingSuspendingProgressTest;
        private boolean pendingSuspendingProgressTestResumeExecuted;

        /**
         * {@code ThreadType} of the last unpaired suspending test.
         * &quot;unpaired suspending test&quot; means a suspending test
         * for which a corresponding resuming test has not been scheduled yet.
         */
        private ThreadType unpairedSuspendingTestType;

        ProgressTester(Object testLock) {
            this.testLock = testLock;
        }

        private void scheduleTest(Progress progressState,
                                  ThreadType threadType) {
            scheduleTest(progressState, threadType, (Runnable[]) null);
        }

        private void scheduleTest(Progress progressState,
                                  ThreadType threadType,
                                  Runnable... additionalTests) {
            checkNonSuspendingTestType(threadType);
            scheduleTest(new ProgressTest(progressState, threadType,
                                          additionalTests));
        }

        /**
         * Schedules a progress test which, after executed, suspends the current
         * thread (unless the test failed). This allows that other tests
         * being executed in other threads finish before next tests
         * for the current are started.
         * 
         * @param  progressState
         * @param  threadType
         * @see  #scheduleResumingTest
         */
        private void scheduleSuspendingTest(Progress progressState,
                                            ThreadType threadType) {
            scheduleSuspendingTest(progressState, threadType, (Runnable[]) null);
        }

        /**
         * Schedules a progress test which, after executed, suspends the current
         * thread (unless the test failed). This allows that other tests
         * being executed in other threads finish before next tests
         * for the current are started. If the third parameter is specified
         * (non-null), the given routine is executed after the given test
         * passes (and before the current thread is suspended, of course).
         *
         * @param  progressState
         * @param  threadType
         * @param  additionalTest
         * @see  #scheduleResumingTest
         */
        private void scheduleSuspendingTest(Progress progressState,
                                            ThreadType threadType,
                                            Runnable... additionalTests) {
            if (unpairedSuspendingTestType != null) {
                throw new IllegalStateException(
                        "Cannot schedule a suspending test until a resuming" +
                        " test is scheduled for the previously scheduled" +
                        " suspending test");
            }
            scheduleTest(new ProgressTest(ProgressTest.SUSPENDING,
                                          progressState, threadType,
                                          additionalTests));
            unpairedSuspendingTestType = threadType;
        }

        /**
         * 
         * @param progressState
         * @param threadType
         */
        private void scheduleResumingTest(Progress progressState,
                                          ThreadType threadType) {
            scheduleResumingTest(progressState, threadType, (Runnable[]) null);
        }

        /**
         * 
         * @param progressState
         * @param threadType
         * @param additionalTest
         */
        private void scheduleResumingTest(Progress progressState,
                                          ThreadType threadType,
                                          Runnable... additionalTests) {
            if (unpairedSuspendingTestType == null) {
                throw new IllegalStateException(
                        "Cannot schedule a resuming test because no" +
                        " corresponding suspending test has been scheduled.");
            }
            checkNonSuspendingTestType(threadType);
            scheduleTest(new ProgressTest(ProgressTest.RESUMING,
                                          progressState, threadType,
                                          additionalTests));
            unpairedSuspendingTestType = null;
        }

        private void scheduleTest(ProgressTest progressTest) {
            progressTestQueue.add(progressTest);
        }

        private void checkNonSuspendingTestType(ThreadType threadType) {
            if (unpairedSuspendingTestType == null) {
                return;
            }

            if (threadType == unpairedSuspendingTestType) {
                throw new IllegalStateException(
                        "Cannot schedule a ProgressTest of the same thread" +
                        " type as that of the unpaired suspending thread.");
            }
        }

        public void stateChanged(ChangeEvent e) {
            ProgressTest progressTest;
            boolean isPendingSuspendingTest = false;
            boolean shouldSuspend = false;
            boolean shouldResume = false;
            final boolean isLastTest;

            synchronized (suspendedThreadLock) {
                if ((pendingSuspendingProgressTest != null)
                        && (getCurrThreadType() == pendingSuspendingProgressTest.threadType)) {
                    isPendingSuspendingTest = true;
                    progressTest = pendingSuspendingProgressTest;
                    System.out.println("Hit the stored suspending test (" + pendingSuspendingProgressTest + " by "
                                       + getCurrThreadType().getDisplayName() + " thread.");
                    pendingSuspendingProgressTest = null;
                } else {
                    progressTest = progressTestQueue.poll();
                    System.out.println("Polled a test (" + progressTest + ") by "
                                       + getCurrThreadType().getDisplayName() + " thread.");
                    if (progressTest == null) {
                        System.out.println(" - it's <null> - quit");
                        return;
                    }
                    if (progressTest.isSuspending()) {
                        assert pendingSuspendingProgressTest == null;
                        if (getCurrThreadType() != progressTest.threadType) {
                            pendingSuspendingProgressTest = progressTest;
                            pendingSuspendingProgressTestResumeExecuted = false;
                            System.out.println(" - it's not a test for the current thread - test stored");
                            progressTest = progressTestQueue.poll();  //poll the next test
                            System.out.println("Polled a test (" + progressTest + ") by "
                                               + getCurrThreadType().getDisplayName() + " thread.");
                        }
                    }
                }

                if (progressTest.isSuspending()) {
                    if (isPendingSuspendingTest && pendingSuspendingProgressTestResumeExecuted) {
                        System.out.println(
                                " - it's a suspending thread but the corresponding"
                                + " resuming test has been already executed"
                                + " so we will not suspend");
                    } else if (progressTestQueue.isEmpty()) {
                        System.out.println(" - it's a suspending thread but"
                                           + " it is the last remaining test"
                                           + " so we will not suspend");
                    } else {
                        shouldSuspend = true;
                    }
                } else if (progressTest.isResuming()) {
                    shouldResume = true;
                    if (pendingSuspendingProgressTest != null) {
                        pendingSuspendingProgressTestResumeExecuted = true;
                    }
                }

                isLastTest = progressTestQueue.isEmpty() && (pendingSuspendingProgressTest == null);

                performTest(progressTest);

                if (isLastTest || (failure != null)) {
                    System.out.println("   - IS LAST TEST - WILL RESUME (if there is a test thread suspended)");
                    stopListening();
                    resumeSuspendedThread();
                    synchronized (testLock) {
                        testLock.notify();      //resumes the thread running the unit test
                    }
                } else if (shouldSuspend) {
                    System.out.println("   - WILL SUSPEND");
                    suspendCurrentThread(progressTest);
                } else if (shouldResume) {
                    System.out.println("   - WILL RESUME");
                    resumeSuspendedThread();
                }
            }
        }

        private void performTest(ProgressTest progressTest) {
            try {
                assertSame(progressTest.progressState, comboSupport.getProgress());
                assertSame(progressTest.threadType,    getCurrThreadType());
                if (progressTest.additionalTests != null) {
                    for (Runnable additionalTest : progressTest.additionalTests) {
                        additionalTest.run();
                    }
                }
            } catch (Throwable t) {
                handleException(t, progressTest.callStack);
            }
        }

        private ThreadType getCurrThreadType() {
            return ThreadType.forBoolean(EventQueue.isDispatchThread());
        }

        private void suspendCurrentThread(ProgressTest progressTest) {
            System.out.println("Suspending the current thread.");
            try {
                suspendedThreadLock.wait();
            } catch (InterruptedException ex) {
                handleException(ex, progressTest.callStack);
            }
        }

        private void resumeSuspendedThread() {
            System.out.println("Resuming the suspended thread (if any).");
            suspendedThreadLock.notify();
        }

        private void handleException(Throwable t,
                                     StackTraceElement[] callersStackTrace) {
            synchronized (this) {
                if (failure == null) {
                    failure = addCallersCallStack(t, callersStackTrace); //store information about the exception
                }
            }
        }

        private Throwable addCallersCallStack(Throwable t,
                                              StackTraceElement[] callersCallStack) {
            t.setStackTrace(concatArrays(t.getStackTrace(), callersCallStack));
            return t;
        }

        private void startListening() {
            if (unpairedSuspendingTestType != null) {
                throw new IllegalStateException(
                        "There is a suspending test scheduled for which" +
                        " there is no corresponding resuming test scheduled.");
            }

            if (listening) {
                throw new IllegalStateException("already listening");
            }
            listening = true;

            comboSupport.setProgressListener(this);
        }

        private void stopListening() {
            comboSupport.setProgressListener(null);
        }

    }

    static <T> T[] concatArrays(T[] a, T[] b) {
        if (b.length == 0) {
            return a;
        }
        if (a.length == 0) {
            return b;
        }
        T[] result = (T[]) java.lang.reflect.Array.newInstance(
                                            a.getClass().getComponentType(),
                                            a.length + b.length);
        System.arraycopy(a, 0, result,        0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

}