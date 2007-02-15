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

package org.netbeans.modules.refactoring.spi.ui;

import javax.swing.Icon;

/**
 * Tree Elements are shown in Refactoring Preview.
 * If you want to implement your own TreeElements, you
 * must register your TreeElementFactoryImplementation 
 * @see TreeElementFactoryImplementation
 * @see TreeElementFactory
 * @author Jan Becicka
 */
public interface TreeElement {
    /**
     * @param isLogical true if parent in lagical view is requested.
     * @return parent of this TreeElement
     */
    public TreeElement getParent(boolean isLogical);
    
    /**
     * @return icon for this TreeElement
     */
    public Icon getIcon();
    /**
     * @param isLogical true if logical description is requested
     * @return text of this TreeElement
     */
    public String getText(boolean isLogical);
    /**
     * @return corresponding object, usually RefactoringElement, FileObject
     */
    public Object getUserObject();
}

