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
package org.netbeans.modules.java.preprocessorbridge.spi;

import org.openide.filesystems.FileObject;

/**Allows creation of JavaSource instances for non-Java files.
 * Is expected to produce "virtual" Java source, which is then parsed
 * by the Java parser and used by selected Java features.
 *
 * @author Jan Lahoda, Dusan Balek
 */
public interface JavaSourceProvider {
    
    /**Create {@link PositionTranslatingJavaFileFilterImplementation} for given file.
     * 
     * @param fo file for which the implementation should be created
     * @return PositionTranslatingJavaFileFilterImplementation or null if which provider
     *         cannot create one for this file
     */
    public PositionTranslatingJavaFileFilterImplementation forFileObject(FileObject fo);
    
    /**"Virtual" Java source provider
     * 
     * Currently, only {@link JavaFileFilterImplementation#filterCharSequence},
     * {@link JavaFileFilterImplementation#getOriginalPosition}, 
     * {@link JavaFileFilterImplementation#getJavaSourcePosition} are called.
     */
    public static interface PositionTranslatingJavaFileFilterImplementation extends JavaFileFilterImplementation {
        /**Compute position in the document for given position in the virtual
         * Java source.
         *
         * @param javaSourcePosition position in the virtual Java Source
         * @return position in the document
         */
        public int getOriginalPosition(int javaSourcePosition);
        
        /**Compute position in the virtual Java source for given position
         * in the document.
         * 
         * @param originalPosition position in the document
         * @return position in the virtual Java source
         */
        public int getJavaSourcePosition(int originalPosition);
    }
}
