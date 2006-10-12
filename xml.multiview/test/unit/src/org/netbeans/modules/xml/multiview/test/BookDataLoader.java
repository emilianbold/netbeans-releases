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
/*
 * BookDataLoader.java
 *
 * Created on March 9, 2005, 4:11 PM
 */

package org.netbeans.modules.xml.multiview.test;

import org.openide.filesystems.*;
import org.openide.loaders.*;
/**
 *
 * @author mkuchtiak
 */
public class BookDataLoader extends UniFileLoader {
    
    public BookDataLoader() {
        super(BookDataLoader.class.getName());
    }
    protected void initialize() {
        super.initialize();
        getExtensions().addExtension("book");
    }
    protected String displayName() {
        return "Book";
    }
    protected MultiDataObject createMultiObject(FileObject pf) throws java.io.IOException {
        return new BookDataObject(pf, this);
    }
    
}
