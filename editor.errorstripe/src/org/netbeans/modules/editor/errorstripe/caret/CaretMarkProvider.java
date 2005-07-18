/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
