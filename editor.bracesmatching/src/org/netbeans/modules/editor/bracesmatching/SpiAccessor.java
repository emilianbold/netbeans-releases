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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bracesmatching;

import javax.swing.text.Document;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Vita Stejskal
 */
public abstract class SpiAccessor {

    private static SpiAccessor ACCESSOR = null;

    public static synchronized void register(SpiAccessor accessor) {
        assert ACCESSOR == null : "Can't register two SPI package accessors!"; //NOI18N
        ACCESSOR = accessor;
    }
    
    public static synchronized SpiAccessor get() {
        try {
            Class clazz = Class.forName(MatcherContext.class.getName());
        } catch (ClassNotFoundException e) {
            // ignore
        }
        
        assert ACCESSOR != null : "There is no SPI package accessor available!"; //NOI18N
        return ACCESSOR;
    }
    
    protected SpiAccessor() {
    }

    public abstract MatcherContext createCaretContext(Document document, int offset, boolean backward, int lookahead);
}
