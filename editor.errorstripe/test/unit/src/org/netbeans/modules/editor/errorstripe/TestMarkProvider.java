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

package org.netbeans.modules.editor.errorstripe;

import java.util.List;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;

/**
 *
 * @author Jan Lahoda
 */
public class TestMarkProvider extends MarkProvider {

    private List/*<Mark>*/ marks;
    private UpToDateStatus upToDate;

    /** Creates a new instance of TestMarkProvider */
    public TestMarkProvider(List/*<Mark>*/ marks, UpToDateStatus upToDate) {
        this.marks = marks;
        this.upToDate = upToDate;
    }

    public UpToDateStatus getUpToDate() {
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
    
//    public void setUpToDate(UpToDateStatus upToDate) {
//        UpToDateStatus old = this.upToDate;
//        
//        this.upToDate = upToDate;
//        
//        firePropertyChange(PROP_UP_TO_DATE, old, upToDate);
//    }
}
