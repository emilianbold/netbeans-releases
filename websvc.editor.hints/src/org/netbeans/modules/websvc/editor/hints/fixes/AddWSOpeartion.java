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

package org.netbeans.modules.websvc.editor.hints.fixes;

import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;

import org.netbeans.modules.websvc.core.AddOperationCookie;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;

/**
 * @author Ajit.Bhate@Sun.COM
 */
public class AddWSOpeartion implements Fix {
    private FileObject fileObject;
    
    /** Creates a new instance of AddWSOpeartion */
    public AddWSOpeartion(FileObject fileObject) {
        this.fileObject = fileObject;
    }
    
    public ChangeInfo implement(){
        AddOperationCookie cookie = WebServiceActionProvider.getAddOperationAction(fileObject);
        if(cookie !=null) cookie.addOperation(fileObject);
        return null;
    }
    
    public int hashCode(){
        return 1;
    }
    
    public boolean equals(Object o){
        // TODO: implement equals properly
        return super.equals(o);
    }
    
    public String getText(){
        return NbBundle.getMessage(RemoveAnnotation.class, "LBL_AddWSOpeartion");
    }
}
