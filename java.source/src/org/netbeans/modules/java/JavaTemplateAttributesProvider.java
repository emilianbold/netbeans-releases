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

package org.netbeans.modules.java;

import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Provides attributes that can be used inside scripting templates.
 * <dl><dt><code>package</code></dt>
 * <dd>attribute containing <code>target</code> folder as package.</dd></dl>
 * 
 * @author Jan Pokorsky
 */
public final class JavaTemplateAttributesProvider implements CreateFromTemplateAttributesProvider {
    
    public Map<String, ? extends Object> attributesFor(DataObject template,
                                                          DataFolder target,
                                                          String name) {
        FileObject templateFO = template.getPrimaryFile();
        if (!JavaDataLoader.JAVA_EXTENSION.equals(templateFO.getExt()) || templateFO.isFolder()) {
            return null;
        }
        
        FileObject targetFO = target.getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(targetFO, ClassPath.SOURCE);
        if (cp == null) {
            throw new IllegalStateException("No classpath was found for folder: " + target.getPrimaryFile()); // NOI18N
        }
        return Collections.<String, Object>singletonMap("package", cp.getResourceName(targetFO, '.', false)); // NOI18N
    }

}
