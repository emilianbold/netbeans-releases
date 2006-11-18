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

package org.netbeans.spi.editor.guards.support;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.modules.editor.guards.GuardedSectionsImpl;
import org.netbeans.modules.editor.guards.PositionBounds;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;

/**
 * Helper class that simplifies writing particular {@link GuardedSectionsProvider}
 * implementations. Subclasses have to implement just {@link #readSections}
 * and {@link #writeSections}.
 * 
 * @author Jan Pokorsky
 */
public abstract class AbstractGuardedSectionsProvider implements GuardedSectionsProvider {

    private final GuardedSectionsImpl impl;
    
    protected AbstractGuardedSectionsProvider(GuardedEditorSupport editor) {
        this.impl = new GuardedSectionsImpl(editor);
    }

    public final Reader createGuardedReader(InputStream stream, String encoding) throws UnsupportedEncodingException {
        return impl.createGuardedReader(this, stream, encoding);
    }

    public final Writer createGuardedWriter(OutputStream stream, String encoding) throws UnsupportedEncodingException {
        return impl.createGuardedWriter(this, stream, encoding);
    }
    
    /**
     * This should be implemented to persist a list of guarded sections inside
     * the passed content.
     * @param sections guarded sections to persist
     * @param content content
     * @return content including guarded sections
     */
    public abstract char[] writeSections(List<GuardedSection> sections, char[] content);
    
    /**
     * This should be implemented to extract guarded sections out of the passed
     * content.
     * @param content content including guarded sections
     * @return the content that will be presented to users and the list of guarded sections
     */
    public abstract Result readSections(char[] content);
    
    /**
     * Creates a simple section object to represent section read by the {@link IGuardedReader}.
     * @param name the section name 
     * @param begin the start offset
     * @param end the end offset
     * @return the simple section instance
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     */
    public final SimpleSection createSimpleSection(String name, int begin, int end) throws BadLocationException {
        return impl.createSimpleSectionObject(name, PositionBounds.createUnresolved(begin, end, impl));
    }
    
    /**
     * Creates an interior section object to represent section read by
     * the {@link IGuardedReader}.
     * @param name the section name
     * @param header begin the start offset of the first guarded part
     * @param header end the end offset of the first guarded part
     * @param footer begin the start offset of the second guarded part
     * @param footer end the end offset of the second guarded part
     * @return the interior section object
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     */
    public final InteriorSection createInteriorSection(String name, int headerBegin, int headerEnd, int footerBegin, int footerEnd) throws BadLocationException {
        return impl.createInteriorSectionObject(
                name,
                PositionBounds.createUnresolved(headerBegin, headerEnd, impl),
                PositionBounds.createBodyUnresolved(headerEnd + 1, footerBegin - 1, impl),
                PositionBounds.createUnresolved(footerBegin, footerEnd, impl)
                );
    }
    
    public final class Result {

        private final char[] content;

        private final List<GuardedSection> sections;
        
        public Result (char[] content, List<GuardedSection> sections) {
            this.content = content;
            this.sections = sections;
        }
        
        public char[] getContent() {
            return this.content;
        }
        
        public List<GuardedSection> getGuardedSections() {
            return this.sections;
        }
    }
}
