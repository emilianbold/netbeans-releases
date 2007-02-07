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
 * Software is Sun Microsystems, Inc. 
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc. 
 */

package org.openide.loaders;

import java.io.IOException;
import java.util.Map;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Tulach
 */
public abstract class CreateFromTemplateHandler {
    
    protected abstract boolean accept(FileObject orig);
    
    protected abstract FileObject createFromTemplate(
        FileObject orig,
        FileObject f, 
        String name,
        Map<String,Object> parameters
    ) throws IOException;
    
}
