/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.css.formatting.api.support;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.spi.IndentTask;

public final class IndenterFormattingContext implements IndentTask.FormattingContext {

    private boolean firstIndenter = false;
    private boolean lastIndenter = false;
    private boolean initialized = false;
    private BaseDocument doc;

    // value: DocumentListener
    private static final String LISTENER = "IndenterFormattingContext.listener";
    // value: List<Change>
    private static final String CHANGES = "IndenterFormattingContext.changes";
    // value: List<List<Line>>
    private static final String DATA = "IndenterFormattingContext.indentedLines";

    public IndenterFormattingContext(BaseDocument doc) {
        this.doc = doc;
    }

    void setFirstIndenter() {
        this.firstIndenter = true;
        initialized = true;

        if (getListener() != null) {
            // should never happen; perhaps only in a case of a recovery:
            doc.removeDocumentListener(getListener());
        }

        final List<Change> changes = new ArrayList<Change>();

        DocumentListener l = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                changes.add(new Change(e.getOffset(), e.getLength()));
            }

            public void removeUpdate(DocumentEvent e) {
                changes.add(new Change(e.getOffset(), -e.getLength()));
            }

            public void changedUpdate(DocumentEvent e) {
                // ignore
            }
        };
        doc.addDocumentListener(l);

        doc.putProperty(LISTENER, l);
        doc.putProperty(CHANGES, changes);
        doc.putProperty(DATA, new ArrayList<List<AbstractIndenter.Line>>());
    }

    List<Change> getAndClearChanges() {
        List<Change> changes = (List<Change>)doc.getProperty(CHANGES);
        List<Change> result = new ArrayList<Change>(changes);
        changes.clear();
        return result;
    }

    private DocumentListener getListener() {
        return (DocumentListener)doc.getProperty(LISTENER);
    }

    public boolean isFirstIndenter() {
        return firstIndenter;
    }

    void setLastIndenter() {
        this.lastIndenter = true;
        initialized = true;
    }

    public boolean isLastIndenter() {
        return lastIndenter;
    }

    boolean isInitialized() {
        return initialized;
    }

    void disableListener() {
        assert getListener() != null;
        doc.removeDocumentListener(getListener());
    }

    void enableListener() {
        assert getListener() != null;
        doc.addDocumentListener(getListener());
    }

    void removeListener() {
        assert getListener() != null;
        doc.removeDocumentListener(getListener());
        doc.putProperty(LISTENER, null);
    }

    public List<List<AbstractIndenter.Line>> getIndentationData() {
        return (List<List<AbstractIndenter.Line>>)doc.getProperty(DATA);
    }

    static class Change {
        public int offset;
        // positive value is insert; negative removal
        public int change;

        public Change(int offset, int change) {
            this.offset = offset;
            this.change = change;
        }

        @Override
        public String toString() {
            return "Change["+offset+":"+change+"]";
        }

    }

}
