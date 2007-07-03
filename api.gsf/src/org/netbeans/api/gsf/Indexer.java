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

package org.netbeans.api.gsf;

import java.io.IOException;
import org.netbeans.api.gsf.ParserResult;

/**
 * Language plugins should implement this interface and register the
 * implementation in the Languages folder. This method will be called
 * when the index needs to be updated. The indexer should iterate through
 * its parsing results and store information in the provided index as
 * necessary. Client code like code completion etc. can later retrieve
 * information from the index which is passed around with CompilationInfos.
 * 
 * @author Tor Norbye
 */
public abstract interface Indexer {
    /** Returns true iff this indexer wants to index the given file */
    boolean isIndexable(ParserFile file);
    
    /** For files that are {@link #isIndexable}, index the given file by
     * operating on the provided {@link Index} using the given {@link ParserResult} to
     * fetch AST information. 
     */
    void updateIndex(Index index,  ParserResult result) throws IOException;
    
}
