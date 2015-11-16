/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor;

import java.util.LinkedList;
import java.util.List;
import javax.swing.text.DefaultCaret;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Extension to standard Swing caret used by all NetBeans editors.
 * <br>
 * It supports multi-caret editing mode where an arbitrary number of carets
 * is placed at arbitrary positions throughout a document.
 * In this mode each caret is described by its <code>CaretInfo</code> object.
 *
 * @author Miloslav Metelka
 * @author Ralph Ruijs
 */
public final class EditorCaret extends DefaultCaret {
    
    private LinkedList<CaretInfo> carets;

    @Override
    public int getDot() {
        return getLastCaret().getDotPosition().getOffset();
    }
    
    @Override
    public int getMark() {
        return getLastCaret().getMarkPosition().getOffset();
    }
    
    private CaretInfo getLastCaret() {
        CaretInfo caret;
        if(carets.isEmpty()) {
            caret = new CaretInfo();
            carets.add(caret);
        } else {
            caret = carets.getLast();
        }
        return caret;
    }
    
    /**
     * Get information about all currently active carets including the "main" caret.
     * <br>
     * Note: {@link #getMainCaretInfo()} can be obtained
     * to check the position of the main caret info in the returned list.
     * <br/>
     * 
     * @return unmodifiable list with size &gt;= 1 infos about all carets.
     */
    public @NonNull List<CaretInfo> getAllCarets() {
        return java.util.Collections.unmodifiableList(carets); // TBD
    }
    
    /**
     * Get information about the caret at the specified offset.
     * 
     * @param offset the offset of the caret
     * @return CaretInfo for the caret at offset, null if there is no caret or the offset is invalid
     */
    public @CheckForNull CaretInfo getCaretAt(int offset) {
        return null;
    }
}
