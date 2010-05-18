/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.text.MessageFormat;

import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/*
 * This modifies the string used to lookup the bean by name.
 *
 * @author Sandip Chitale
 */
public class RenameBeanNameStringRefactoringElement extends SimpleRefactoringElementImplementation {

    private String oldBeanName;

    private String newBeanName;

    private PositionBounds bounds;

    public RenameBeanNameStringRefactoringElement(String oldBeanName, String newBeanName) {
        this.oldBeanName = oldBeanName;
        this.newBeanName = newBeanName;
    }

    public String getText() {
        return MessageFormat.format(NbBundle.getBundle(
                RenameBeanNameStringRefactoringElement.class).getString("MSG_RenameBeanNameStringText"),
                new Object[] {oldBeanName});
    }

    public String getDisplayText() {
        return MessageFormat.format(NbBundle.getBundle(
                RenameBeanNameStringRefactoringElement.class).getString("MSG_RenameBeanNameStringDisplayText"),
                new Object[] {oldBeanName});
    }

    /**
     * Looks for a return statement of the form <code>return (OldBeanName)getBean("OldBeanName");</code>
     * and replace the quoted bean name with new beanname.
     */
    public void performChange() {
        
    }

    public FileObject getParentFile() {
        return null;
    }

    public PositionBounds getPosition() {
        if (bounds == null) {
        }
        return bounds;
    }
    
    public void openInEditor() {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                new UnsupportedOperationException("New interface method not implemented, do it!")); // NOI18N
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
}
