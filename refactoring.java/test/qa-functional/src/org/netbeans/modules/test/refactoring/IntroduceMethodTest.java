/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.test.refactoring;


import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.modules.test.refactoring.actions.RefactorIntroduceMethodAction;
import org.netbeans.modules.test.refactoring.operators.IntroduceMethodOperator;


/**
 @author (stanislav.sazonov@oracle.com)
 */
public class IntroduceMethodTest extends ModifyingRefactoring {


	private enum access { PUBLIC, PROTECTED, DEFAULT, PRIVATE };

	public IntroduceMethodTest(String name){
		super(name);
	}

	public static Test suite(){
		return JellyTestCase.emptyConfiguration().
				addTest(RenameTest.class, "testIntroduceMethod_1").
				addTest(RenameTest.class, "testIntroduceMethod_2").
				addTest(RenameTest.class, "testIntroduceMethod_3").
				addTest(RenameTest.class, "testIntroduceMethod_4").
				suite();
	}

	public void testIntroduceMethod_1(){
		performIntroduvceMethod("ClassA", "introduceMethod", "myMethod", new int[]{37, 9, 32}, access.PUBLIC);
	}
	
	public void testIntroduceMethod_2(){
		performIntroduvceMethod("ClassA", "introduceMethod", "myMethod", new int[]{37, 9, 32}, access.PROTECTED);
	}
		
	public void testIntroduceMethod_3(){
		performIntroduvceMethod("ClassA", "introduceMethod", "myMethod", new int[]{37, 9, 32}, access.DEFAULT);
	}
			
	public void testIntroduceMethod_4(){
		performIntroduvceMethod("ClassA", "introduceMethod", "myMethod", new int[]{37, 9, 32}, access.PRIVATE);
	}

	private void performIntroduvceMethod(String className, String pkgName, String newName, int[] coordinates, access a){
		openSourceFile(pkgName, className);
		EditorOperator editor = new EditorOperator(className + ".java");
		editor.setCaretPosition(coordinates[0], 1);
		switch(coordinates.length){
			case 1:
				editor.select(coordinates[0]);
				break;
			case 2:
				editor.select(coordinates[0], coordinates[1]);
				break;
			case 3:
				editor.select(coordinates[0], coordinates[1], coordinates[2]);
				break;
		}

		new RefactorIntroduceMethodAction().performPopup(editor);

		new EventTool().waitNoEvent(3000);

		IntroduceMethodOperator imo = new IntroduceMethodOperator();
		imo.getNewName().typeText(newName);
		imo.getAlsoReplace().setSelected(false);
		switch (a) {
			case PUBLIC:
				imo.getRadPublic().setSelected(true);
				break;
			case PROTECTED:
				imo.getRadProtected().setSelected(true);
				break;
			case DEFAULT:
				imo.getRadDefault().setSelected(true);
				break;
			case PRIVATE:
				imo.getRadPrivate().setSelected(true);
				break;
		}
		imo.ok();

		new EventTool().waitNoEvent(3000);

		ref(editor.getText());
		editor.closeDiscard();
	}
}
