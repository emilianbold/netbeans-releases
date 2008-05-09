/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Micro//Systems, Inc. Portions Copyright 1997-2006 Sun
 * Micro//Systems, Inc. All Rights Reserved.
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
 */
package org.netbeans.test.j2ee.hints;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.j2ee.lib.Utils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jindrich Sedek
 */
public class EntityRelations extends HintsUtils {

    private File secondFile = null;
    private static final Logger LOG = Logger.getLogger(EntityRelations.class.getName());
    /** Creates a new instance of EntityRelations */
    public EntityRelations(String S) {
        super(S);
    }

    public void prepareProject() {
        File project1 = new File(getDataDir(), "projects/EntityHintsApp");
        ProjectSupport.openProject(project1);
        if (Utils.checkMissingServer(project1.getName())){
            closeBuildScriptRegeneration();
        }
        File project2 = new File(getDataDir(), "projects/EntityHintsEJB");
        ProjectSupport.openProject(project2);
        if (Utils.checkMissingServer(project2.getName())){
            closeBuildScriptRegeneration();
        }
    }

    private void closeBuildScriptRegeneration() {
        try {
            Thread.sleep(5000);
            String editPropertiesTitle = "Edit Project Properties";
            while (JDialogOperator.findJDialog(editPropertiesTitle, true, true) != null) {
                NbDialogOperator dialogOperator = new NbDialogOperator(editPropertiesTitle);
                new JButtonOperator(dialogOperator, "Regenerate").push();
                LOG.info("Closing buildscript regeneration");
                Thread.sleep(10000);
            }
        } catch (InterruptedException exc) {
            LOG.log(Level.INFO, "interrupt exception", exc);
        }
    }
    
    private EditorOperator openFile(String fileName) throws Exception {
        secondFile = new File(getDataDir(), fileName);
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(secondFile));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        ed.open();
        return new EditorOperator(secondFile.getName()); // wait for opening
    }

    private void testEntityHintsBidirectional(int fixOrder) throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/B.java");
        openFile("projects/EntityHintsEJB/src/java/hints/A.java");
        hintTest(f, fixOrder, "Create", 12);
    }

    private void testEntityHintsUnidirectional(int fixOrder) throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/B.java");
        openFile("projects/EntityHintsEJB/src/java/hints/A.java");
        hintTest(f, fixOrder, null, 12);
    }

    public void testManyToManyBidirectional() throws Exception {
        testEntityHintsBidirectional(3);
    }

    public void testManyToManyBidirectional2() throws Exception {
        testEntityHintsBidirectional(4);
    }

    public void testManyToOneBidirectional() throws Exception {
        testEntityHintsBidirectional(5);
    }

    public void testManyToOneBidirectional2() throws Exception {
        testEntityHintsBidirectional(6);
    }

    public void testOneToManyBidirectional() throws Exception {
        testEntityHintsBidirectional(7);
    }

    public void testOneToOneBidirectional() throws Exception {
        testEntityHintsBidirectional(8);
    }

    public void testManyToOneUnidirectional() throws Exception {
        testEntityHintsUnidirectional(9);
    }

    public void testManyToOneUnidirectional2() throws Exception {
        testEntityHintsUnidirectional(10);
    }

    public void testOneToOneUnidirectional() throws Exception {
        testEntityHintsUnidirectional(11);
    }

    public void testAARelation() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsApp/src/java/hints/CC.java");
        hintTest(f, 3, "Create", 6);
    }

    public void testAARelation2() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsApp/src/java/hints/CC.java");
        hintTest(f, 4, "Create", 6);
    }

    public void testAARelation3() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsApp/src/java/hints/CC.java");
        hintTest(f, 5, null, 6);
    }

    @Override
    protected void closing() {
        if (secondFile != null) {
            write("----SECOND FILE-----");
            write(new EditorOperator(secondFile.getName()).getText());
        }
        EditorOperator.closeDiscardAll();
    }

    public void testCreateID() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/CreateID.java");
        hintTest(f, 0, "Create", 2);
    }

    public void testMakePublic() throws Exception {
        hintTest(new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/MakePublic.java"), 1, null, 2);
    }

    public void testDefaultConstructor() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/DefaultConstructor.java");
        hintTest(f, 0, null, 1);
    }

    public static void main(String[] args) throws Exception {
        //new EntityRelations("test").testAARelation();
        String path = "/export/home/jindra/TRUNK/j2ee/test/qa-functional/data/projects/EntityHintsEJB/src/java/hints/B.java";
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(new File(path)));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        ed.open();
        Thread.sleep(3000);
        FileObject fileObj = FileUtil.toFileObject(new File(path));
//        for (Fix fix : HintsUtils.getFixes(fileObj)) {
//            System.out.println(fix.getText());
//        }
    }
}
