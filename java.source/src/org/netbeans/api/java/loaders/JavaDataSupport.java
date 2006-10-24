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

package org.netbeans.api.java.loaders;

import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.JavaNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;

/**
 * This class contains helper methods necessary to write extensions
 * of the java data support.
 *
 * @author Jan Pokorsky
 */
public final class JavaDataSupport {

    /** singleton */
    private JavaDataSupport() {
    }
    
    /**
     * In case you write own data loader you should use this entry for the
     * <code>.java</code> file object. The entry provides functionality like
     * create from template.
     * @param mdo the data object this entry will belong to
     * @param javafile the file object for the entry
     * @return the java entry
     */
    public static MultiDataObject.Entry createJavaFileEntry(MultiDataObject mdo, FileObject javafile) {
        return new JavaDataLoader.JavaFileEntry(mdo, javafile);
    }

    /**
     * Creates a default node for a particular java file object.
     * @param javafile the java file object to represent
     * @return the node
     */
    public static Node createJavaNode(FileObject javafile) {
        try {
            DataObject jdo = DataObject.find(javafile);
            return new JavaNode(jdo, true);
        } catch (DataObjectNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
