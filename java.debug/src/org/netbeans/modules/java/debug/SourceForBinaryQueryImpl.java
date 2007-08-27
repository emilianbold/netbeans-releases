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
package org.netbeans.modules.java.debug;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Jan Lahoda
 */
public class SourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {
    
    public SourceForBinaryQueryImpl() {
    }
    
    public synchronized Result findSourceRoots(URL binaryRoot) {
        try {
            String binaryRootS = binaryRoot.toExternalForm();
            URL result = null;
            if (binaryRootS.startsWith("jar:file:")) { // NOI18N
                if (binaryRootS.endsWith("/java/source/javacapi/external/javac-api.jar!/")) { // NOI18N
                    result = new URL(binaryRootS.substring("jar:".length(), binaryRootS.length() - "/java/source/javacapi/external/javac-api.jar!/".length()) + "/retouche/Jsr199/src"); // NOI18N
                } else if (binaryRootS.endsWith("/java/source/javacimpl/external/javac-impl.jar!/")) { // NOI18N
                    result = new URL(binaryRootS.substring("jar:".length(), binaryRootS.length() - "/java/source/javacimpl/external/javac-impl.jar!/".length()) + "/retouche/Jsr199/src"); // NOI18N
                }
                final FileObject resultFO = result != null ? URLMapper.findFileObject(result) : null;
                if (resultFO != null) {
                    return new Result() {
                        public FileObject[] getRoots() {
                            return new FileObject[] {resultFO};
                        }
                        public void addChangeListener(ChangeListener l) {}
                        public void removeChangeListener(ChangeListener l) {}
                    };
                }
            }
        } catch (MalformedURLException e) {
            Logger.getLogger("global").log(Level.INFO, null, e); //NOI18N
        }
        return null;
    }

}
