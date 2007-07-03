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

package org.netbeans.modules.refactoring.ruby.ui.tree;

import javax.swing.Icon;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 * @author Tor Norbye
 */
public final class ElementGrip {
    private String toString;
    private FileObject fileObject;
    private Icon icon;
    
    /**
     * Creates a new instance of ElementGrip
     * 
     */
    public ElementGrip(String name, FileObject fileObject, Icon icon) {
        this.toString = name;
        this.fileObject = fileObject;
        this.icon = icon;
    }
    
    public Icon getIcon() {
        return icon;
    }
    public String toString() {
        return toString;
    }

    public ElementGrip getParent() {
        return ElementGripFactory.getDefault().getParent(this);
    }

    public FileObject getFileObject() {
        return fileObject;
    }
}
