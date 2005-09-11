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
        super(BookDataObject.class.getName());
    }

    protected FileObject findPrimaryFile(FileObject fo) {
        if ("book.xml".equals(fo.getNameExt())) {
            return fo;
        } else {
            return null;
        }
    }

    protected String displayName() {
        return "Book";
    }

    protected MultiDataObject createMultiObject(FileObject pf) throws java.io.IOException {
        return new BookDataObject(pf, this);
    }
    
}