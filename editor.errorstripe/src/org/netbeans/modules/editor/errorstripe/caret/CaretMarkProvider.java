/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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


/**
 *
 * @author Jan Lahoda
 */
public class CaretMarkProvider extends MarkProvider implements CaretListener {
    
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
    
    public synchronized List/*<Mark>*/ getMarks() {
        return Collections.singletonList(mark);
    }

    public void caretUpdate(CaretEvent e) {
        List old = getMarks();
        
        mark = createMark();
        
        List nue = getMarks();
        
        firePropertyChange(PROP_MARKS, old, nue);
    }
    
}
