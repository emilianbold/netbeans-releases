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
package org.openide.loaders;

import java.io.IOException;
import org.openide.filesystems.FileObject;


/** 
 * Save document under a different file name and/or extension.
 * 
 * The default implementation is available in {@link org.openide.text.DataEditorSupport}. So if your
 * editor support inherits from <code>DataEditorSupport</code> you can implement "Save As" feature
 * for your documents by adding the following lines into your {@link DataObject}'s constructor:
 * 
 * 
 <code><pre>
        getCookieSet().assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                getDataEditorSupport().saveAs( folder, fileName );
            }
        });
</pre></code>
 *
 * @since 6.3
 * @author S. Aubrecht
 */
public interface SaveAsCapable {
    /** 
     * Invoke the save operation.
     * @param folder Folder to save to.
     * @param name New file name to save to.
     * @throws IOException if the object could not be saved
     */
    void saveAs( FileObject folder, String name ) throws IOException;
}
