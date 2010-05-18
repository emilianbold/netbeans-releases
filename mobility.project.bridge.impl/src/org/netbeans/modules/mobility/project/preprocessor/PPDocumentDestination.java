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

/*
 * PPDocumentDestination.java
 *
 * Created on February 6, 2004, 4:14 PM
 */
package org.netbeans.modules.mobility.project.preprocessor;

import java.io.IOException;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;

/**
 *
 * @author  Adam Sotona
 */
public final class PPDocumentDestination implements CommentingPreProcessor.Destination {
    
    private final BaseDocument document;
    
    
    /** Creates a new instance of PPDocumentDestination */
    public PPDocumentDestination(BaseDocument document) {
        this.document=document;
    }
    
    public void doInsert(final int line, final String s) throws IOException {
        try {
            document.insertString(Utilities.getRowStartFromLineOffset(document, line - 1), s, null);
        } catch (BadLocationException ble) {
            throw new IOException(ble.getLocalizedMessage());
        }
    }
    
    public void doRemove(final int line, final int column, final int length) throws IOException {
        try {
            document.remove(Utilities.getRowStartFromLineOffset(document, line - 1) + column, length);
        } catch (BadLocationException ble) {
            throw new IOException(ble.getLocalizedMessage());
        }
    }
    
    public Writer createWriter(@SuppressWarnings("unused")
	final boolean validOutput) {
        return null;
    }
    
    
    
}
