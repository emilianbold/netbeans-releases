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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.text.MessageFormat;

import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/*
 * This modifies the string used to lookup the bean by name.
 *
 * @author Sandip Chitale
 */
public class RenameBeanNameStringRefactoringElement extends SimpleRefactoringElementImplementation {

    private String oldBeanName;

    private String newBeanName;

    private boolean enabled;

    private int status = RefactoringElementImplementation.NORMAL;

    private PositionBounds bounds;

    public RenameBeanNameStringRefactoringElement(String oldBeanName, String newBeanName) {
        this.oldBeanName = oldBeanName;
        this.newBeanName = newBeanName;

        // initially enabled
        enabled = true;
    }

    public String getText() {
        return MessageFormat.format(NbBundle.getBundle(
                RenameBeanNameStringRefactoringElement.class).getString("MSG_RenameBeanNameStringText"),
                new Object[] {oldBeanName});
    }

    public String getDisplayText() {
        return MessageFormat.format(NbBundle.getBundle(
                RenameBeanNameStringRefactoringElement.class).getString("MSG_RenameBeanNameStringDisplayText"),
                new Object[] {oldBeanName});
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Looks for a return statement of the form <code>return (OldBeanName)getBean("OldBeanName");</code>
     * and replace the quoted bean name with new beanname.
     */
    public void performChange() {
        
    }

    public FileObject getParentFile() {
        return null;
    }

    public PositionBounds getPosition() {
        if (bounds == null) {
        }
        return bounds;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public void openInEditor() {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                new UnsupportedOperationException("New interface method not implemented, do it!")); // NOI18N
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
}
