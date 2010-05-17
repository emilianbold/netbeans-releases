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

package org.netbeans.modules.editor.errorstripe.caret;
import java.util.Collections;
import java.util.List;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Lahoda
 */
public class CaretMarkProvider extends MarkProvider implements CaretListener {
    
    private static final RequestProcessor RP = new RequestProcessor("CaretMarkProvider");
    
    private Mark mark;
    private JTextComponent component;
    
    /** Creates a new instance of AnnotationMarkProvider */
    public CaretMarkProvider(JTextComponent component) {
        this.component = component;
        component.addCaretListener(this);
        mark = createMark();
    }

    private Mark createMark() {
        int offset = component.getCaretPosition(); //TODO: AWT?
        Document doc = component.getDocument();
        int line = 0;
        
        if (doc instanceof StyledDocument) {
            line = NbDocument.findLineNumber((StyledDocument) doc, offset);
        }
        
        return new CaretMark(line);
    }
    
    public synchronized List<Mark> getMarks() {
        return Collections.singletonList(mark);
    }

    public void caretUpdate(CaretEvent e) {
        final List<Mark> old = getMarks();
        
        mark = createMark();
        
        final List<Mark> nue = getMarks();
        
        //Do not fire this event under the document's write lock
        //may deadlock with other providers:
        RP.post(new Runnable() {
            public void run() {
                firePropertyChange(PROP_MARKS, old, nue);
            }
        });
    }
    
}
