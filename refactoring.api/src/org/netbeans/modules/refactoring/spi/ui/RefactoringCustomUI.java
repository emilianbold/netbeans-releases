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

import java.awt.Component;
import java.util.Collection;
import javax.swing.Icon;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;

/**
 * Backward compatible extension to RefactoringUI providing custom preview panel.
 * If implementation of RefactoringUI also implement RefactoringCustomUI, default 
 * Refactoring Preview appears with new "Custom View" toggle button, which
 * shows custom Component
 * 
 * this interface is just prototype and might be subject of change.
 * 
 * @author Jan Becicka
 */
public interface RefactoringCustomUI {
    /**
     * @return component to show
     */ 
    Component getCustomComponent(Collection<RefactoringElementImplementation> elements);
    /**
     * @return icon for toggle button
     */
    Icon getCustomIcon();
    /**
     * tooltip for toggle button
     */ 
    String getCustomToolTip();
}
