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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.spi.editor.hints;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;

/**
 * Represents one error with a description text, span in the document, list
 * of fixes and a severity. Please see the static methods in the class
 * {@link ErrorDescriptionFactory} if you want to create instances of this class.
 *
 * @author Jan Lahoda
 */
public final class ErrorDescription {

    private final String description;
    private final Severity severity;
    private final LazyFixList fixes;
    private final PositionBounds span;
    private final FileObject file;

    /**
     * The constructor is intentionally not public. Use 
     * {@link ErrorDescriptionFactory} when you need an instance of this class.
     */
    ErrorDescription(FileObject file, String description, Severity severity, LazyFixList fixes, PositionBounds span) {
        this.description = description;
        this.severity    = severity;
        this.fixes       = fixes;
        this.span        = span;
        this.file        = file;
    }

    /**
     * @return description of the error that is displayed to the user.
     */
    public String getDescription() {
        return description;
    }

    /**
     * The severity determines how the hint will be rendered.
     * @return {@link Severity} of the error
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * The list of fixes that will be associated with the error.
     * @return {@link LazyFixList} containing the fixes
     */
    public LazyFixList getFixes() {
        return fixes;
    }

    /**
     * @return where the error will be marked in the document.
     */
    public PositionBounds getRange() {
        return span;
    }

    /**
     * @return associated file or <code>null</code> if there is none
     */
    public FileObject getFile() {
        return file;
    }
    
    @Override
    public String toString() {
        try {
            return span.getBegin().getLine() + ":" + span.getBegin().getColumn() + "-" + span.getEnd().getLine() + ":" + span.getEnd().getColumn() + ":" + severity.getDisplayName() + ":" + description;
        } catch (IOException ex) {
            throw (IllegalStateException) new IllegalStateException().initCause(ex);
        }
    }
}
