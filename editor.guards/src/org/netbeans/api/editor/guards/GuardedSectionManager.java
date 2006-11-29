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

package org.netbeans.api.editor.guards;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.guards.GuardedSectionImpl;
import org.netbeans.modules.editor.guards.GuardedSectionsImpl;
import org.netbeans.modules.editor.guards.GuardsAccessor;
import org.netbeans.modules.editor.guards.InteriorSectionImpl;
import org.netbeans.modules.editor.guards.SimpleSectionImpl;

/**
 * This is the entry point for clients to manipulate guarded sections
 * of the given document.
 *
 * @author Jan Pokorsky
 */
public final class GuardedSectionManager {

    /**
     * Gets the manager instance.
     * @param doc a document containing guarded sections
     * @return the manager instance or <code>null</code>.
     */
    public static GuardedSectionManager getInstance(StyledDocument doc) {
        return (GuardedSectionManager) doc.getProperty(GuardedSectionManager.class);
    }

    /**
     * Tries to find the simple section of the given name.
     * @param name the name of the requested section
     * @return the found guarded section or <code>null</code> if there is no section
     *         of the given name
     */
    public SimpleSection findSimpleSection(String name) {
        GuardedSection s = impl.findSection(name);
        return (s instanceof SimpleSection) ? (SimpleSection) s : null;
    }

    /**
     * Tries to find the interior section of the given name.
     * @param name the name of the looked-for section
     * @return the found guarded section or <code>null</code> if there is no section
     *         of the given name
     */
    public InteriorSection findInteriorSection(String name) {
        GuardedSection s = impl.findSection(name);
        return (s instanceof InteriorSection) ? (InteriorSection) s : null;
    }
    
    /**
     * Creates an empty simple section at the given position.
     * The position must not be within any existing guarded section
     * and the passed name must not be registered for other
     * already existing section. The created section will initially contain
     * one space and a newline.
     * @return SimpleSection instance that can be used for generating text into
     * the protected region
     * @throws IllegalArgumentException if either the name has been already used, or
     * the position is inside another section or Java Element.
     * @throws BadLocationException if pos is outside of document's scope, or
     * the document does not permit creating the guarded section.
     */
    public SimpleSection createSimpleSection(Position pos, String name)
            throws IllegalArgumentException, BadLocationException {
        return impl.createSimpleSection(pos, name);
    }

    /**
     * Creates an empty interior section at the given position.
     * The position must not be within any existing guarded section
     * and the passed name must not be registered to other
     * already existing section. The created section will initially contain
     * one space and a newline in all its parts (header, body and footer).
     * @return InteriorSection instance that can be used for generating text into
     * the protected region
     * @throws IllegalArgumentException if either the name has been already used, or
     * the position is inside another section or Java Element.
     * @throws BadLocationException if pos is outside of document's scope, or
     * the document does not permit creating the guarded section.
     */
    public InteriorSection createInteriorSection(Position pos, String name)
            throws IllegalArgumentException, BadLocationException {
        return impl.createInteriorSection(pos, name);
    }
    
    /** Gets all sections.
     * @return an iterable over {@link GuardedSection}s
     */
    public Iterable<GuardedSection> getGuardedSections() {
        return impl.getGuardedSections();
    }
    
    // package

    // private
    
    static {
        GuardsAccessor.DEFAULT = new GuardsAccessor() {
            public GuardedSectionManager createGuardedSections(GuardedSectionsImpl impl) {
                return new GuardedSectionManager(impl);
            }

            public SimpleSection createSimpleSection(SimpleSectionImpl impl) {
                return new SimpleSection(impl);
            }

            public InteriorSection createInteriorSection(InteriorSectionImpl impl) {
                return new InteriorSection(impl);
            }
            
            public GuardedSectionImpl getImpl(GuardedSection gs) {
                return gs.getImpl();
            }
            
        };
    }
    
    /** Creates a new instance of GuardedDocument */
    private GuardedSectionManager(GuardedSectionsImpl impl) {
        this.impl = impl;
    }

    private final GuardedSectionsImpl impl;

}
