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

package org.netbeans.modules.vmd.api.codegen;

import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.modules.vmd.api.model.Debug;
import org.openide.text.IndentEngine;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author David Kaspar
 */
public final class CodeWriter {

    private StyledDocument document;
    private String forceValue;
    private SimpleSection section;
    private int offset;
    private int endOffset;
    private Writer writer;
    private StringWriter memory;
    private boolean committed;

    public CodeWriter (StyledDocument document, SimpleSection section) {
        this (document, section.getStartPosition ().getOffset (), Integer.MIN_VALUE, null);
        this.section = section;
    }

    public CodeWriter (StyledDocument document, SimpleSection beforeSection, SimpleSection afterSection, String forceValue) {
        this (document, beforeSection.getEndPosition ().getOffset () + 1, afterSection.getStartPosition ().getOffset (), forceValue);
    }

    private CodeWriter (StyledDocument document, int beginOffset, int endOffset, String forceValue) {
        this.document = document;
        this.forceValue = forceValue;
        this.offset = beginOffset;
        this.endOffset = endOffset;
    }

    public CodeWriter write (String text) {
        assert ! committed;
        if (forceValue != null)
            return this;
        try {
            if (writer == null) {
                memory = new StringWriter (512);
                IndentEngine indentEngine = IndentEngine.find (document);
                if (indentEngine != null)
                    writer = indentEngine.createWriter (document, offset, memory);
                else
                    writer = memory;
            }
            writer.write (text);
            return this;
        } catch (IOException e) {
            throw Debug.error (e);
        }
    }

    public void commit () {
        assert ! committed;
        try {
            String text;
            if (forceValue != null) {
                text = forceValue;
            } else {
                if (writer != null) {
                    writer.flush ();
                    writer.close ();
                    text = memory.getBuffer ().toString ();
                } else
                    text = ""; // NOI18N
            }

            if (section != null) {
                section.setText (text);
                //System.out.println(" Section: " + section.getName());
                //System.out.println(" Code: \n" + text);
            } else {
                if (endOffset != Integer.MIN_VALUE)
                    document.remove (offset, endOffset - offset);
                document.insertString (offset, text, null);
            }

            committed = true;
            writer = null;
            memory = null;
        } catch (IOException e) {
            throw Debug.error (e);
        } catch (BadLocationException e) {
            throw Debug.error (e);
        }
    }

    public boolean isCommitted () {
        return committed;
    }

}
