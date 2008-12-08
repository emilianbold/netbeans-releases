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

package org.netbeans.modules.groovy.qaf;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;
import org.netbeans.modules.groovy.grailsproject.actions.GotoDomainClassAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author lukas
 */
public class GrailsActionsTest extends GrailsTestCase {

    private static final String APP_PORT = "9998"; //NOI18N
    private static boolean isPortSet = false;
    private OpenAction oa = new OpenAction();

    public GrailsActionsTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (!isPortSet) {
            GrailsSettings.getInstance().setPortForProject(getProject(), APP_PORT);
            isPortSet = true;
        }
    }

    @Override
    protected String getProjectName() {
        return "GrailsActions"; //NOI18N
    }

    public void testGenerateAll() {
        //Generate all
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.actions.Bundle", "CTL_GenerateAllAction");
        getDomainClassNode("Book").performPopupAction(label); //NOI18N
        waitFor("generate-all", "Finished generation for domain class"); //NOI18N
    }

    public void testCreateView() {
        //XXX - grails create-view should be called instead of a wizard
        //Create view
//        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.actions.Bundle", "CTL_CreateViewAction");
//        getDomainClassNode("Author").performPopupAction(label); //NOI18N
    }

    public void testGotoController() {
        //Go to Grails Controller
        Action a = getGrailsNavigateAction("CTL_GotoControllerAction"); //NOI18N
        //from a domain class
        oa.perform(getDomainClassNode("Book")); //NOI18N
        EditorOperator eo = new EditorOperator("Book.groovy"); //NOI18N
        a.performPopup(eo); //NOI18N
        assertTrue(getActiveTC().endsWith("controllers/BookController.groovy")); //NOI18N
        //from a view
//        oa.perform(getViewNode("book|edit")); //NOI18N
//        eo = new EditorOperator("edit.gsp"); //NOI18N
//        a.performPopup(eo); //NOI18N
//        assertTrue(getActiveTC().endsWith("controllers/BookController.groovy")); //NOI18N
    }

    public void testGotoView() {
        //Go to Grails View
        Action a = getGrailsNavigateAction("CTL_GotoViewAction"); //NOI18N
        //from a domain class
        oa.perform(getDomainClassNode("Book")); //NOI18N
        EditorOperator eo = new EditorOperator("Book.groovy"); //NOI18N
        a.performPopup(eo);
        assertTrue(getActiveTC().endsWith("views/book/show.gsp")); //NOI18N
        //from a controller
        oa.perform(getControllerNode("BookController")); //NOI18N
        eo = new EditorOperator("BookController.groovy"); //NOI18N
        a.performPopup(eo);
        assertTrue(getActiveTC().endsWith("views/book/show.gsp")); //NOI18N
    }

    public void testGotoDomainClass() throws InterruptedException, InvocationTargetException {
        //XXX - no direct UI entry to this action
        final GotoDomainClassAction a = new GotoDomainClassAction();
        //from a view
//        oa.perform(getViewNode("book|list"); //NOI18N
//        EditorOperator eo = new EditorOperator("list.gsp"); //NOI18N
//        assertTrue(a.isEnabled());
//        a.actionPerformed(null, (JTextComponent) eo.txtEditorPane().getSource());
//        assertTrue(getActiveTC().endsWith("domain/Book.groovy")); //NOI18N
//        eo.close(false);
        //from a controller
        oa.perform(getControllerNode("BookController")); //NOI18N
        final EditorOperator eo = new EditorOperator("BookController.groovy"); //NOI18N
        assertTrue(a.isEnabled());
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                a.actionPerformed(new ActionEvent(eo.txtEditorPane().getSource(), -1, null));
            }
        });
        assertTrue(getActiveTC().endsWith("domain/Book.groovy")); //NOI18N
    }

    public void testStopApp() {
        //XXX - better to have ability to not open browser during run
        //      (remove TestURLDisplayer)
        runGrailsApp();
        stopGrailsApp();
    }

    private Node getDomainClassNode(String domainClass) {
        //Domain Classes
        String label = getNodeLabel("LBL_grails-app_domain"); //NOI18N
        Node n = new Node(getProjectRootNode(), label);
        return new Node(n, domainClass + ".groovy"); //NOI18N
    }

    private Node getControllerNode(String controller) {
        //Controllers
        String label = getNodeLabel("LBL_grails-app_controllers"); //NOI18N
        Node n = new Node(getProjectRootNode(), label);
        return new Node(n, controller + ".groovy"); //NOI18N
    }

    private Node getViewNode(String view) {
        //Views and Layouts
        String label = getNodeLabel("LBL_grails-app_views"); //NOI18N
        Node n = new Node(getProjectRootNode(), label);
        return new Node(n, view + ".gsp"); //NOI18N
    }

    private String getNodeLabel(String key) {
        return Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.Bundle", key);
    }

    private Action getGrailsNavigateAction(String key) {
        String groupLabel = "Navigate";
        String actionLabel = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.actions.Bundle", key);
        return new Action(null, groupLabel + "|" + actionLabel); //NOI18N
    }

    private String getActiveTC() {
        return TopComponent.getRegistry().getActivated().getToolTipText();
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(GrailsActionsTest.class)
                .enableModules(".*").clusters(".*")); //NOI18N
    }

}