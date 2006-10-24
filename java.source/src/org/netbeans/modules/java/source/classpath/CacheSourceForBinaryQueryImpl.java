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
package org.netbeans.modules.java.source.classpath;


import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tomas Zezula
 */
public class CacheSourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {

    private String FILE_PROTOCOL = "file";  //NOI18N
    
    /** Creates a new instance of CacheSourceForBinaryQueryImpl */
    public CacheSourceForBinaryQueryImpl() {
    }

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        if (!FILE_PROTOCOL.equals (binaryRoot.getProtocol())) {
            return null;
        }
        URL sourceURL = Index.getSourceRootForClassFolder(binaryRoot);
        if (sourceURL != null) {
            return new R (sourceURL);
        }        
        return null;
    }
    
    private static class R implements SourceForBinaryQuery.Result {
        
        private final FileObject sourceRoot;
        
        public R (final URL sourceRootURL) {
            assert sourceRootURL != null;
            this.sourceRoot = URLMapper.findFileObject(sourceRootURL);
        }

        public void removeChangeListener(ChangeListener l) {
            //Imutable, not needed
        }

        public void addChangeListener(ChangeListener l) {
            //Imutable, not needed
        }

        public FileObject[] getRoots() {
            if (this.sourceRoot == null) {
                return new FileObject[0];
            }
            else {
                return new FileObject[] {this.sourceRoot};
            }
        }                
    }            
}
