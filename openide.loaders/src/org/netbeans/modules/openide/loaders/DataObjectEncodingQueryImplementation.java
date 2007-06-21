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
package org.netbeans.modules.openide.loaders;

import java.nio.charset.Charset;
import java.util.logging.Logger;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Tomas Zezula
 */
public class DataObjectEncodingQueryImplementation extends FileEncodingQueryImplementation {
    private static ThreadLocal<DataFolder> TARGET = new ThreadLocal<DataFolder>();
    private static final Logger LOG = Logger.getLogger(DataObjectEncodingQueryImplementation.class.getName());
    
    /** Creates a new instance of DataObjectEncodingQueryImplementation */
    public DataObjectEncodingQueryImplementation() {
    }
    
    public static DataFolder enterIgnoreTargetFolder(DataFolder df) {
        DataFolder prev = TARGET.get();
        TARGET.set(df);
        return prev;
    }
    public static void exitIgnoreTargetFolder(DataFolder prev) {
        TARGET.set(prev);
    }
    
    public Charset getEncoding(FileObject file) {
        assert file != null;
        DataFolder df = TARGET.get();
        if (df != null && df.getPrimaryFile().equals(file.getParent())) {
            // do not create new data objects
            return null;
        }
        try {
            DataObject dobj = DataObject.find(file);
            FileEncodingQueryImplementation impl = dobj.getLookup().lookup (FileEncodingQueryImplementation.class);
            if (impl == null)  {
                return null;
            }
            return impl.getEncoding(file);
        } catch (DataObjectNotFoundException donf) {
            LOG.warning("Invalid DataObject: " + FileUtil.getFileDisplayName(file));
            return null;
        }
    }

}
