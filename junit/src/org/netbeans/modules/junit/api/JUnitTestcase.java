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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.junit.api;

import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author answer
 */
public class JUnitTestcase extends Testcase{
    private FileObject classFO = null;

    public JUnitTestcase(String name, String type, TestSession session) {
        super(name, type, session);
    }

    @Override
    public String getName() {
        TestSuite currentSuite = getSession().getCurrentSuite();
        String className = getClassName();
        if (className == null || currentSuite == null) {
            return super.getName();
        }
        String suiteName = currentSuite.getName();
        // if the running suite is actually a test file return just the method name
        if(suiteName == null || suiteName.equals(className)) {
            return super.getName();
        }
        // the running suite is actually a suite, so return method's full path
        return className + "." + super.getName();
    }
    
    public FileObject getClassFileObject(){
        return getClassFileObject(false);
    }
    
    public FileObject getClassFileObject(boolean searchForInnerClass){
        FileLocator fileLocator = getSession().getFileLocator();
        if ((classFO == null) && (fileLocator != null) && (getClassName() != null)){
            String className = getClassName();
            classFO = fileLocator.find(className.replace('.', '/') + ".java"); //NOI18N
            if (classFO == null && searchForInnerClass) {
                int indexOf = className.indexOf('$');
                if (indexOf != -1) { // innerclass
                    className = className.substring(0, indexOf);
                    return fileLocator.find(className.replace('.', '/') + ".java"); //NOI18N
                }
            }
        }
        return classFO;
    }
}
