/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.api.model;
import java.util.List;

/**
 * Represents a source file
 * @author Vladimir Kvashin
 */
public interface CsmFile extends CsmNamedElement, CsmScope, CsmValidable, CsmIdentifiable<CsmFile> {

    /** Gets this file absolute path */
    String getAbsolutePath();

    /** Gets the project, to which the file belongs*/
    CsmProject getProject();

    /** Gets this file text */
    String getText();

    /** Gets this file text */
    String getText(int start, int end);

    /** Sorted (by start offset) list of #include directives in the file */
    List<CsmInclude> getIncludes();
    
    /** Sorted (by start offset) list of declarations in the file */
    List<CsmOffsetableDeclaration> getDeclarations();
    
    /** Sorted (by start offset) list of #define directives in the file */
    List<CsmMacro> getMacros();
    
    /** 
     * Returns true if the file has been already parsed
     * (i.e. was parsed since last change),
     * otherwise false 
     */
    boolean isParsed();
    
    /*
     * Checks whether the file needs to be parsed,
     * if yes, scedules parsing this file.
     * If wait parameter is true, waits until this file is parsed.
     * If the file is already parsed, immediately returns.
     *
     * @param wait determines whether to wait until the file is parsed:
     * if true, waits, otherwise doesn't wait, just puts the given file
     * into parser queue
     */
    void scheduleParsing(boolean wait) throws InterruptedException;
    
    /** returns true if file is source file. */
    boolean isSourceFile();

    /** returns true if file is header file. */
    public boolean isHeaderFile();
    
}
