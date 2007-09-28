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

import org.netbeans.api.gsf.annotations.NonNull;

/**
 * A PositionManager is responsible for mapping ComObjects provided by a Parser
 * to an offset in the source buffer used by that parser.
 * This service can often be provided by the parser itself (which would in that case
 * implement this interface). Offsets are 0-based.
 *
 * @author Tor Norbye
 */
public interface PositionManager {
    /**
     * Return the offset range of the given ComObject in the buffer, or OffsetRange.NONE
     * if the ComObject is not found
     */
    @NonNull
    OffsetRange getOffsetRange(Element file, Element object);

    /**
     * Return true if this position manager translates source positions
     */
    boolean isTranslatingSource();
    
    /**
     * Return the lexical offset corresponding to the given ast offset.
     * For most source files, this is the same (so it just returns the parameter)
     * but in languages like RHTML where the lexical input file is translated
     * into Ruby before being parsed, the offsets may differ.
     * 
     * @param result The result associated with this parser job
     * @param astOffset The offset in the source processed by the parser
     * @return The lexical offset corresponding to the ast offset, or -1
     *   if the corresponding position couldn't be determined
     */
    int getLexicalOffset(ParserResult result, int astOffset);

    /**
     * Reverse mapping from lexical offset to ast offset. See {@link getLexicalOffset}
     * for an explanation.
     * 
     * @param result The result associated with this parser job
     * @param lexicalOffset The lexical offset in the source buffer
     * @return The corresponding offset in the AST, or -1 if the position couldn't be
     *  determined
     */
    int getAstOffset(ParserResult result, int lexicalOffset);
}
