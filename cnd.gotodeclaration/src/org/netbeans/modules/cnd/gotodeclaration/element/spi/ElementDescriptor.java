/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.element.spi;

import javax.swing.Icon;

/**
 * Describes one element in the search list of "Go to Funcion or Variable" dialog
 * @author Vladimir Kvashi
 */
public interface ElementDescriptor {

    /**
     * Gets the name of the element as such
     * @return element name 
     */
    String getDisplayName();
        
    /**
     * Gets an additional context for the element name. 
     * This would typically be the fully qualified name, minus the name part. 
     * Return null if there is no applicable context. 
     * 
     * @return the name of the context of the type, 
     * such as the fully qualified name minus the name part
     */
    String getContextName();

    /** 
     * Gets an icon that should be shown for this element. 
     * The icon should give a visual indication of the type of element
     * (e.g. function or variable or macro)
     * @return An Icon to be shown on the left hand side 
     */
    Icon getIcon();
    
    /**
     * Gets the name of the project that contains the given element.
     * @return the name of the project that contains the given element.
     */
    String getProjectName();
    
    /**
     * Gets the icon that represents the project that contains the given element.
     * @return project icon or null if there are no project associated with element
     */
    Icon getProjectIcon();
    
    /**
     * Gets the absolute path to the file that contains the element.
     * @return the absolute path to the file that contains the element.
     */
    String getAbsoluteFileName();
    
    /**
     * Opens the element in editor
     */
    void open();
}
