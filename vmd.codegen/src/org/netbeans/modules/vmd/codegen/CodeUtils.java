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
package org.netbeans.modules.vmd.codegen;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.modules.vmd.api.model.Debug;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author David Kaspar
 */
public class CodeUtils {

    public static SimpleSection findSectionByPrefix (Iterable<? extends GuardedSection> sections, String prefix) {
        for (GuardedSection section : sections)
            if (section instanceof SimpleSection)
                if (section.getName ().startsWith (prefix))
                    return (SimpleSection) section;
        return null;
    }

    public static ArrayList<SimpleSection> findSectionsByPrefix (Iterable<? extends GuardedSection> sections, String prefix) {
        ArrayList<SimpleSection> list = new ArrayList<SimpleSection> ();
        for (GuardedSection section : sections)
            if (section instanceof SimpleSection)
                if (section.getName ().startsWith (prefix))
                    list.add ((SimpleSection) section);
        return list;
    }

    public static void sortSections (ArrayList<SimpleSection> sections) {
        Collections.sort (sections, new Comparator<SimpleSection>() {
            public int compare (SimpleSection o1, SimpleSection o2) {
                return o1.getName ().compareTo (o2.getName ());
            }
        });
    }

    public static GuardedSection findLast (Iterable<? extends GuardedSection> sections) {
        GuardedSection last = null;
        for (GuardedSection section : sections) {
            if (last == null) {
                last = section;
                continue;
            }
            if (last.getEndPosition ().getOffset () < section.getEndPosition ().getOffset ())
                last = section;
        }
        return last;
    }

    public static SimpleSection createSimpleSectionAfter (StyledDocument document, GuardedSection sectionForAddingAfter, String sectionID) {
        GuardedSectionManager instance = GuardedSectionManager.getInstance (document);
        try {
            return instance.createSimpleSection (document.createPosition (sectionForAddingAfter.getEndPosition ().getOffset () + 1), sectionID);
        } catch (BadLocationException e) {
            throw Debug.error (e);
        }
    }

}
