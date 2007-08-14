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
package org.netbeans.modules.vmd.api.codegen;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.codegen.CodeUtils;
import org.openide.ErrorManager;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author David Kaspar
 */
public final class MultiGuardedSection {

    // TODO - no other guarded-section (besides MultiGuardedSection) must start with '|' character
    // TODO - add support for deleting whole MultiGuardedSection

    private StyledDocument document;
    private String multiGuardedID;

    private ArrayList<SimpleSection> guardedSections = new ArrayList<SimpleSection> ();
    private ArrayList<String> editableSectionIDs = new ArrayList<String> ();
    private int index = 0;
    private boolean guarded = true;
    private CodeWriter writer;

    private MultiGuardedSection (StyledDocument document, String multiGuardedID) {
        assert multiGuardedID.indexOf ('|') < 0;
        this.document = document;
        this.multiGuardedID = multiGuardedID;
        initialize ();
        JavaCodeGenerator.getDefault ().registerUsedMultiGuardedSection (document, this);
    }

    private void initialize () {
        GuardedSectionManager instance = GuardedSectionManager.getInstance (document);
        Iterable<GuardedSection> allSections = instance.getGuardedSections ();
        // TODO - check if any other section of multiGuardedID is still there
        for (; ;) {
            String prefix = createSectionID (guardedSections.size (), null);
            SimpleSection section = CodeUtils.findSectionByPrefix (allSections, prefix);
            if (section == null)
                break;
            String editableSectionID = section.getName ().substring (prefix.length ());
            addSection (section, editableSectionID);
        }
        if (guardedSections.size () <= 0) {
            String sectionID = createSectionID (0, null);
            addSection (CodeUtils.createSimpleSectionAfter (document, CodeUtils.findLast (allSections), sectionID), null);
        }
    }

    public void switchToGuarded () {
        if (index < 0)
            throw Debug.illegalState ("Section is closed already", multiGuardedID); // NOI18N
        if (guarded)
            throw Debug.illegalState ("Cannot switch to guarded from guarded", multiGuardedID, index); // NOI18N
        if (! isWriterCommitted ())
            throw Debug.illegalState ("Writer is not committed yet", multiGuardedID, index); // NOI18N
        writer = null;
        index ++;
        guarded = ! guarded;
    }

    public void switchToEditable (String editableSectionID) {
        if (index < 0)
            throw Debug.illegalState ("Section is already closed", multiGuardedID); // NOI18N
        if (! guarded)
            throw Debug.illegalState ("Cannot switch from editable to editable", multiGuardedID, index, editableSectionID); // NOI18N
        if (! isWriterCommitted ())
            throw Debug.illegalState ("Writer is not committed yet", multiGuardedID, index); // NOI18N
        writer = null;
        if (index + 1 >= guardedSections.size ()) {
            GuardedSection last = CodeUtils.findLast (guardedSections);
            String sectionID = createSectionID (guardedSections.size (), null);
            SimpleSection newGuardedSection = CodeUtils.createSimpleSectionAfter (document, last, sectionID);
            addSection (newGuardedSection, null);
        }
        editableSectionIDs.set (index, editableSectionID);
        guarded = ! guarded;
    }

    public CodeWriter getWriter () {
        if (index < 0)
            throw Debug.illegalState ("Section is closed already", multiGuardedID); // NOI18N
        if (writer == null) {
            if (guarded) {
                writer = new CodeWriter (document, guardedSections.get (index));
            } else {
                String editableID = editableSectionIDs.get (index);
                writer = new CodeWriter (document, guardedSections.get (index), guardedSections.get (index + 1), JavaCodeGenerator.getDefault ().getUserCode (document, multiGuardedID, editableID));
            }
        }
        return writer;
    }

    public void close () {
        if (index < 0)
            throw Debug.illegalState ("Section is closed already", multiGuardedID); // NOI18N
        if (! guarded  ||  ! isWriterCommitted ())
            throw Debug.illegalState ("Cannot close - last section is not properly commited", multiGuardedID, guarded, isWriterCommitted ()); // NOI18N

        int size = guardedSections.size ();
        if (size > 0)
            editableSectionIDs.set (size - 1, ""); // NOI18N
        for (int a = 0; a < size; a ++)
            try {
                guardedSections.get (a).setName (createSectionID (a, editableSectionIDs.get (a)));
            } catch (PropertyVetoException e) {
                throw Debug.error (e);
            }

        Position begin = guardedSections.get (index).getEndPosition ();
        Position end = guardedSections.get (size - 1).getEndPosition ();
        for (int a = index + 1; a < size; a ++)
            guardedSections.get (a).removeSection ();
        try {
            // TODO - check if there is no gurded section between begin and end. if so, then do not do anything to prevent data-lost
            document.remove (begin.getOffset () + 1, end.getOffset () - (begin.getOffset () + 1));
        } catch (BadLocationException e) {
            throw Debug.error (e);
        }

        index = -1;
    }

    public static MultiGuardedSection create (StyledDocument document, String multiGuardedID) {
        return multiGuardedID != null ? new MultiGuardedSection (document, multiGuardedID) : null;
    }

    String getMultiGuardedID () {
        return multiGuardedID;
    }

    private void addSection (SimpleSection section, String editableSectionID) {
        guardedSections.add (section);
        editableSectionIDs.add (editableSectionID);
    }

    private String createSectionID (int index, String editableID) {
        return '|' + multiGuardedID + '|' + index + '|' + (editableID != null ? editableID : ""); // NOI18N
    }

    static String createSectionIDPrefix (String multiGuardedID) {
        return '|' + multiGuardedID + '|';
    }

    private static String createSectionID (String multiGuardedID, int index, String editableID) {
        return '|' + multiGuardedID + '|' + index + '|' + (editableID != null ? editableID : ""); // NOI18N
    }

    private boolean isWriterCommitted () {
        return writer != null  &&  writer.isCommitted ();
    }

    public boolean isGuarded () {
        return guarded;
    }

    public static boolean isPartOfMultiGuardedSection (GuardedSection section) {
        return section.getName ().startsWith ("|"); // NOI18N
    }

    /**
     * Parses the section name.
     * @param section the guarded section
     * @return the array of 3 objects: multiGuardedID:String, index:Integer, editableID:String
     */
    static Object[] parsePartOfMultiGuardedSection (GuardedSection section) {
        assert isPartOfMultiGuardedSection (section);
        String name = section.getName ();
        Object[] result = new Object[3];

        int secondSeparator = name.indexOf ('|', 1);
        assert secondSeparator >= 1;
        result[0] = name.substring (1, secondSeparator);

        secondSeparator ++;
        int thirdSeparator = name.indexOf ('|', secondSeparator);
        assert thirdSeparator >= 1;
        result[1] = Integer.parseInt (name.substring (secondSeparator, thirdSeparator));

        thirdSeparator ++;
        result[2] = name.substring (thirdSeparator);

        return result;
    }

    public static boolean matches (GuardedSection section, String multiGuardedID, int index) {
        Object[] objects = parsePartOfMultiGuardedSection (section);
        return multiGuardedID.equals (objects[0])  &&  index == (Integer) objects[1];
    }

    public static boolean matches (GuardedSection section, String multiGuardedID, String editableID) {
        Object[] objects = parsePartOfMultiGuardedSection (section);
        return multiGuardedID.equals (objects[0])  &&  editableID.equals (objects[2]);
    }

    static GuardedSection findNextPartOfMultiGuardedSectionAfter (StyledDocument document, Object[] info) {
        GuardedSectionManager instance = GuardedSectionManager.getInstance (document);
        return CodeUtils.findSectionByPrefix (instance.getGuardedSections (), createSectionID ((String) info[0], (Integer) info[1] + 1, null));
    }

    public static void remove (StyledDocument document, String multiGuardedID) {
        Iterable<GuardedSection> allSections = GuardedSectionManager.getInstance (document).getGuardedSections ();
        // TODO - check if there is any other section in between the guarded-section of multiGuardedID
        ArrayList<SimpleSection> sections = CodeUtils.findSectionsByPrefix (allSections, MultiGuardedSection.createSectionIDPrefix (multiGuardedID));
        CodeUtils.sortSections (sections);

        Iterator<SimpleSection> iterator = sections.iterator ();
        if (! iterator.hasNext ())
            return;

        SimpleSection firstSection = iterator.next ();
        ArrayList<SimpleSection> sectionsBlock = new ArrayList<SimpleSection> ();
        sectionsBlock.add (firstSection);
        int lastIndex = (Integer) MultiGuardedSection.parsePartOfMultiGuardedSection (firstSection)[1];

        while (iterator.hasNext ()) {
            SimpleSection nextSection = iterator.next ();
            int nextIndex = (Integer) MultiGuardedSection.parsePartOfMultiGuardedSection (nextSection)[1];
            if (lastIndex + 1 != nextIndex) {
                removeSectionsBlock (document, sectionsBlock);
                Debug.warning ("Broken multi-guarded-section", "Missing sections between", lastIndex, nextIndex); // NOI18N
            }
            sectionsBlock.add (nextSection);
            lastIndex = nextIndex;
        }
        removeSectionsBlock (document, sectionsBlock);
    }

    private static void removeSectionsBlock (StyledDocument document, ArrayList<SimpleSection> sectionsBlock) {
        Position begin = sectionsBlock.get (0).getStartPosition ();
        Position end = sectionsBlock.get (sectionsBlock.size () - 1).getEndPosition ();
        for (SimpleSection section : sectionsBlock)
            section.removeSection ();
        try {
            document.remove (begin.getOffset (), end.getOffset () - begin.getOffset ());
        } catch (BadLocationException e) {
            ErrorManager.getDefault ().notify (e);
        }
        sectionsBlock.clear ();
    }

}
