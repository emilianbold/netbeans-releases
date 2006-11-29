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

package org.netbeans.spi.editor.guards;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;

/**
 * Subclasses should be able to read content containing guarded section marks 
 * and vice versa. You should subclass {@link AbstractGuardedSectionsProvider}.
 * Use this interface in case you load or save the {@link javax.swing.text.Document}.
 * @author Jan Pokorsky
 */
public interface GuardedSectionsProvider {
    
    /**
     * Creates a reader able to read persisted sections.
     * @param stream stream containing persisted sections
     * @param encoding encoding to decode read bytes 
     * @return the reader
     * @throws if the encoding is not supported
     */
    Reader createGuardedReader(InputStream stream, String encoding) throws UnsupportedEncodingException;

    /**
     * Creates a writer able to write persisted sections.
     * @param stream stream where the output should be written
     * @param encoding encoding used by the writer
     * @return the writer
     * @throws if the encoding is not supported
     */
    Writer createGuardedWriter(OutputStream stream, String encoding) throws UnsupportedEncodingException;
    
}
