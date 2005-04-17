/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.errorstripe;

import java.util.List;
import org.netbeans.modules.editor.errorstripe.spi.MarkProvider;

/**
 *
 * @author Jan Lahoda
 */
public class TestMarkProvider extends MarkProvider {
    
    private List/*<Mark>*/ marks;
    private int upToDate;
    
    /** Creates a new instance of TestMarkProvider */
    public TestMarkProvider(List/*<Mark>*/ marks, int upToDate) {
        this.marks = marks;
        this.upToDate = upToDate;
    }

    public int getUpToDate() {
        return upToDate;
    }

    public List getMarks() {
        return marks;
    }
    
    public void setMarks(List/*<Mark>*/ marks) {
        setMarks(marks, true, true);
    }
    
    public void setMarks(List/*<Mark>*/ marks, boolean fireOld, boolean fireNue) {
        List old = this.marks;
        
        this.marks = marks;
        
        firePropertyChange(PROP_MARKS, fireOld ? old : null, fireNue ? this.marks : null);
    }
    
    public void setUpToDate(int upToDate) {
        int old = this.upToDate;
        
        this.upToDate = upToDate;
        
        firePropertyChange(PROP_UP_TO_DATE, new Integer(old), new Integer(upToDate));
    }
}
