/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.blame;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;

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
