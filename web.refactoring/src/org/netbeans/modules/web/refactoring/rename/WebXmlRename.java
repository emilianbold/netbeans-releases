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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring.rename;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;

/**
 * Handles renaming of classes specified in web.xml.
 *
 * @author Erno Mononen
 */
public class WebXmlRename extends BaseWebXmlRename{
    
    private final String oldFqn;
    private final RenameRefactoring rename;

    public WebXmlRename(String oldFqn, RenameRefactoring rename, WebApp webModel, FileObject webDD) {
        super(webDD, webModel);
        this.oldFqn = oldFqn;
        this.rename = rename;
    }
    
    protected List<RenameItem> getRenameItems() {
        return Collections.<RenameItem>singletonList(new RenameItem(getNewFQN(), oldFqn));
    }

    private String getNewFQN(){
        String newName = rename.getNewName();
        int lastDot = oldFqn.lastIndexOf('.');
        return (lastDot <= 0) ? newName : oldFqn.substring(0, lastDot + 1) + newName;
    }

    protected AbstractRefactoring getRefactoring() {
        return rename;
    }
    
}
