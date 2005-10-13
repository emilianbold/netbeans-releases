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

package org.netbeans.modules.versioning.system.cvss.ui.actions.annotate;
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
 * ErrorStripe liason, real work is done in AnnotationBar.
 *
 * @author Petr Kuzel
 */
final class AnnotationMarkProvider extends MarkProvider {
    
    private List marks = Collections.EMPTY_LIST;
    
    public void setMarks(List marks) {
        List old = this.marks;
        this.marks = marks;
        firePropertyChange(PROP_MARKS, old, marks);
    }
        
    public synchronized List/*<Mark>*/ getMarks() {
        return marks;
    }    
}
