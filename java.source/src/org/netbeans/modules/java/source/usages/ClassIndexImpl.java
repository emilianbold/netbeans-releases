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

package org.netbeans.modules.java.source.usages;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;

/** Should probably final class with private constructor.
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public abstract class ClassIndexImpl {


    public static enum UsageType {

        SUPER_CLASS( 0 ),
        SUPER_INTERFACE( 1 ),
        FIELD_REFERENCE( 2 ), 
        METHOD_REFERENCE( 3 ),
        TYPE_REFERENCE (4);

        private int offset;

        UsageType( final int offset) {
            this.offset = offset;
        }

        int getOffset () {
            return this.offset;
        }
    }
    
    
    public static ClassIndexFactory FACTORY;    
    
    public abstract <T> void search (final String binaryName, final Set<UsageType> usageType, final ResultConvertor<T> convertor, final Set<? super T> result);
    
    public abstract <T> void getDeclaredTypes (String name, ClassIndex.NameKind kind, final ResultConvertor<T> convertor, final Set<? super T> result);
    
    public abstract void getPackageNames (String prefix, boolean directOnly, Set<String> result);
    
    public abstract FileObject[] getSourceRoots ();
   
    public abstract BinaryAnalyser getBinaryAnalyser ();
    
    public abstract SourceAnalyser getSourceAnalyser ();
    
    public abstract void setDirty (JavaSource js);
    
    protected abstract void close () throws IOException;
    
}
