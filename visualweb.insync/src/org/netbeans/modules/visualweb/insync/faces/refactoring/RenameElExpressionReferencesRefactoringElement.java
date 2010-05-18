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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.visualweb.insync.faces.ElAttrUpdater;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupVisitor;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RenameElExpressionReferencesRefactoringElement extends MarkupRefactoringElement {

    public RenameElExpressionReferencesRefactoringElement(FileObject fileObject, MarkupUnit markupUnit, String oldName, String newName) {
        super(fileObject, markupUnit, oldName, newName);
    }

    protected Element getPositionElement() {
        return null;
    }

    public String getDisplayText() {
        return oldName + " to " + newName;
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    public FileObject getParentFile() {
        return fileObject;
    }

    public PositionBounds getPosition() {
        return null;
    }

    public String getText() {
        return oldName + " to " + newName;
    }

    public void performChange() {
        Document document = markupUnit.getSourceDom();
        if (document == null) {
            // XXX #107723 NPE.
            log(new NullPointerException("There is null source document for markup unit, markupUnit=" + markupUnit)); // NOI18N
            return;
        }
        MarkupVisitor v = new ElAttrUpdater(oldName, newName);
        v.apply(document);
        markupUnit.flush();        
    }

    @Override
    public void undoChange() {
        Document document = markupUnit.getSourceDom();
        if (document == null) {
            // XXX #107723 NPE.
            log(new NullPointerException("There is null source document for markup unit, markupUnit=" + markupUnit)); // NOI18N
            return;
        }
        MarkupVisitor v = new ElAttrUpdater(newName, oldName);
        v.apply(document);
        markupUnit.flush();        
    }


    private static void log(Exception ex) {
        Logger.getLogger(RenameElExpressionReferencesRefactoringElement.class.getName()).log(Level.INFO, null, ex);
    }
    
}
