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

package org.netbeans.modules.java.guards;

import java.util.List;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;

/**
 *
 * @author Jan Pokorsky
 */
public final class JavaGuardedSectionsProvider extends AbstractGuardedSectionsProvider {
    
    public JavaGuardedSectionsProvider(GuardedEditorSupport editor) {
        super(editor);
    }

    public char[] writeSections(List<GuardedSection> sections, char[] buff) {
        JavaGuardedWriter writer = new JavaGuardedWriter();
        writer.setGuardedSection(sections);
        return writer.translate(buff);
    }

    public AbstractGuardedSectionsProvider.Result readSections(char[] buff) {
        JavaGuardedReader reader = new JavaGuardedReader(this);
        return new AbstractGuardedSectionsProvider.Result(
                reader.translateToCharBuff(buff),
                reader.getGuardedSections()
                );
    }
    
    
}
