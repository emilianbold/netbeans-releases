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
package org.netbeans.modules.test.refactoring;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.test.refactoring.actions.RenamePopupAction;
import org.netbeans.modules.test.refactoring.operators.RenameOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@SUN.Com
 */
public class RenameTest extends ModifyingRefactoring {

    public RenameTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(RenameTest.class).addTest(
                "testRenameClass",
                "testRenamePackage",
                "testRenameMethod",
                "testRenameGenerics",
                "testRenameVariable",
                "testRenameParameter",
                "testRenameCtor",
                "testRenameProperty").enableModules(".*").clusters(".*").suite();
    }

    public void testRenameClass() {
        performRename("Rename", "renameClass", "Renamed", 3, 17);
    }

    public void testRenamePackage() {
        performRename("RenamePkg", "renamePkg", "renamedPkg", 6, 12);
    }

    public void testRenameMethod() {
        performRename("RenameMethod", "renameClass", "renamedMethod", 5, 18);
    }

    public void testRenameGenerics() {
        performRename("RenameGenerics", "renameClass", "A", 3, 30);
    }

    public void testRenameVariable() {
        performRename("RenameLocalVariable", "renameClass", "renamed", 6, 16);
    }

    public void testRenameParameter() {
        performRename("RenameParameter", "renameClass", "renamned", 5, 34);
    }

    public void testRenameCtor() {
        performRename("RenameCtor", "renameClass", "RenamedCtor", 5, 34);
    }

    public void testRenameProperty() {
        performRename("RenameProperty", "renameClass", "renamed", 16, 17, true);
    }

//    public void testRenameUndo() {
//        String className = "RenameUndo";
//        openSourceFile("renameUndo", className);
//        EditorOperator editor = new EditorOperator(className);
//        editor.setCaretPosition(1, 17);
//        new RenamePopupAction().perform(editor);
//        RenameOperator ro = new RenameOperator();
//        ro.getNewName().typeText("renamedPackage");
//        ro.getRefactor().push();
//        new EventTool().waitNoEvent(1000);
//
//        editor.setCaretPosition(3, 16);
//        new RenamePopupAction().perform(editor);
//        ro = new RenameOperator();
//        ro.getNewName().typeText("renamedClass");
//        ro.getRefactor().push();
//        new EventTool().waitNoEvent(1000);
//
//        new UndoAction().perform(null); //undo rename class
//        new EventTool().waitNoEvent(1000);
//
//        editor.setCaretPosition(3, 26);
//        new RenamePopupAction().perform(editor);
//        ro = new RenameOperator();
//        ro.getNewName().typeText("Z");
//        ro.getRefactor().push();
//        new EventTool().waitNoEvent(1000);
//        
//        new UndoAction().perform(null); //undo rename generics
//        new EventTool().waitNoEvent(1000);
//
//        editor.setCaretPosition(5, 15);
//        new RenamePopupAction().perform(editor);
//        ro = new RenameOperator();
//        ro.getNewName().typeText("RenamedInner");
//        ro.getRefactor().push();
//        new EventTool().waitNoEvent(1000);
//
//        new UndoAction().perform(null); //undo rename inner class
//        new EventTool().waitNoEvent(1000);
//
//        editor.setCaretPosition(8, 20);
//        new RenamePopupAction().perform(editor);
//        ro = new RenameOperator();
//        ro.getNewName().typeText("renamedMethod");
//        ro.getRefactor().push();
//        new EventTool().waitNoEvent(1000);
//
//        new UndoAction().perform(null); //undo rename method
//        new EventTool().waitNoEvent(1000);
//
//        editor.setCaretPosition(11, 27);
//        new RenamePopupAction().perform(editor);
//        ro = new RenameOperator();
//        ro.getNewName().typeText("renamedParam");
//        ro.getRefactor().push();
//        new EventTool().waitNoEvent(1000);
//
//        new UndoAction().perform(null); //undo rename param
//        new EventTool().waitNoEvent(1000);
//
//        editor.setCaretPosition(17, 19);
//        new RenamePopupAction().perform(editor);
//        ro = new RenameOperator();
//        ro.getNewName().typeText("renamedLocal");
//        ro.getRefactor().push();
//        new EventTool().waitNoEvent(1000);
//
//        new UndoAction().perform(null); //undo rename variable
//        new EventTool().waitNoEvent(1000);
//
//        new UndoAction().perform(null); //undo rename pacakge
//        new EventTool().waitNoEvent(1000);
//
//        ref(new File(getDataDir(),"projects/RefactoringTest/src/renameUndo/RenameUndo.java".replace('/', File.separatorChar)));
//
//    }
    
    private void performRename(String className, String pkgName, String newName, int row, int col) {
        performRename(className, pkgName, newName, row, col, false);
    }
    private void performRename(String className, String pkgName, String newName, int row, int col, boolean property) {
        openSourceFile(pkgName, className);
        new EventTool().waitNoEvent(1000);
        EditorOperator editor = new EditorOperator(className + ".java");
        new EventTool().waitNoEvent(1000);
        editor.setCaretPosition(row, col);
        editor.select(row, col, col + 1);
        new EventTool().waitNoEvent(1000);
        new RenamePopupAction().perform(editor);
        new org.netbeans.jemmy.EventTool().waitNoEvent(3000);
        RenameOperator ro = new RenameOperator();
        ro.getNewName().typeText(newName);
        if (property) {
            ro.getProperty().setSelected(true);
        }
        ro.getPreview().push();
        dumpRefactoringResults();
        new EventTool().waitNoEvent(1000);
        editor.closeDiscard();
        new EventTool().waitNoEvent(1000);
    }
}
