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
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * @author David Kaspar
 */
public final class JavaCodeGenerator {

    private static final JavaCodeGenerator DEFAULT = new JavaCodeGenerator ();

    public static final String VMD_FIELDS_SECTION_ID = "fields"; // NOI18N
    public static final String VMD_METHODS_SECTION_ID = "methods"; // NOI18N

    // document -> a map from editableid to usercode
    // put into the properites of styleddocument
    private final WeakHashMap<StyledDocument, HashMap<String, String>> userCodes = new WeakHashMap<StyledDocument, HashMap<String, String>> ();

    private final HashMap<StyledDocument, ArrayList<String>> usedMultiSectionIDs = new HashMap<StyledDocument, ArrayList<String>> ();

    public static JavaCodeGenerator getDefault () {
        return DEFAULT;
    }

    private JavaCodeGenerator () {
    }

    public void updateUserCodesFromEditor (final StyledDocument document) {
        NbDocument.runAtomic (document, new Runnable() {
            public void run () {
                updateUserCodesFromEditorCore (document);
            }
        });
    }

    public void generateCode (final StyledDocument document, final DesignDocument designDocument) {
        final Collection<CodePresenter> presenters = DocumentSupport.gatherAllPresentersOfClass (designDocument, CodePresenter.class);
        try {
            NbDocument.runAtomic (document, new Runnable() {
                public void run () {
                    generateCodeCore (presenters, document);
                }
            });
        } finally {
            usedMultiSectionIDs.remove (document);
        }

        Collection<CodeGlobalLevelPresenter> globalLevel = DocumentSupport.filterPresentersForClass (presenters, CodeGlobalLevelPresenter.class);
        for (CodeGlobalLevelPresenter presenter : globalLevel)
            presenter.performGlobalGeneration (document);
    }

    private void generateCodeCore (Collection<CodePresenter> presenters, StyledDocument document) {
        Collection<CodeClassLevelPresenter> classLevel = DocumentSupport.filterPresentersForClass (presenters, CodeClassLevelPresenter.class);

        MultiGuardedSection section;

        section = MultiGuardedSection.create (document, VMD_FIELDS_SECTION_ID);
        assert section != null;
        section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Fields \">\n"); // NOI18N
        for (CodeClassLevelPresenter presenter : classLevel)
            presenter.generateFieldSectionCode (section);
        section.getWriter ().write ("//</editor-fold>\n"); // NOI18N
        section.getWriter ().commit ();
        section.close ();

        section = MultiGuardedSection.create (document, VMD_METHODS_SECTION_ID);
        section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Methods \">\n"); // NOI18N
        for (CodeClassLevelPresenter presenter : classLevel)
            presenter.generateMethodSectionCode (section);
        section.getWriter ().write ("//</editor-fold>\n"); // NOI18N
        section.getWriter ().commit ();
        section.close ();

        for (CodeClassLevelPresenter presenter : classLevel)
            presenter.generateClassBodyCode (document);

        removeUnusedSections (document);
    }

    private HashMap<String, String> getEditableUserCodes (StyledDocument document) {
        assert document != null;
        HashMap<String, String> codes = userCodes.get (document);
        if (codes == null) {
            codes = new HashMap<String, String> ();
            userCodes.put (document, codes);
        }
        return codes;
    }

    String getUserCode (StyledDocument document, String multiGuardedID, String editableID) {
        HashMap<String, String> editableUserCodes = getEditableUserCodes (document);
        return editableUserCodes.get (multiGuardedID + "|" + editableID); // NOI18N
    }

    public void putUserCode (StyledDocument document, String multiGuardedID, String editableID, String userCode) {
        HashMap<String, String> editableUserCodes = getEditableUserCodes (document);
        editableUserCodes.put (multiGuardedID + "|" + editableID, userCode); // NOI18N
    }

    private void updateUserCodesFromEditorCore (StyledDocument document) {
        for (GuardedSection section : GuardedSectionManager.getInstance (document).getGuardedSections ()) {
            if (! MultiGuardedSection.isPartOfMultiGuardedSection (section))
                continue;

            Object[] info = MultiGuardedSection.parsePartOfMultiGuardedSection (section);
            GuardedSection nextSection = MultiGuardedSection.findNextPartOfMultiGuardedSectionAfter (document, info);
            if (nextSection == null)
                continue;

            try {
                int begin = section.getEndPosition ().getOffset () + 1;
                int end = nextSection.getStartPosition ().getOffset ();
                String userCode = document.getText (begin, end - begin);
                putUserCode (document, (String) info[0], (String) info[2], userCode);
            } catch (BadLocationException e) {
                ErrorManager.getDefault ().notify (e);
            }
        }
    }

    private void removeUnusedSections (StyledDocument document) {
        Iterable<GuardedSection> allSections = GuardedSectionManager.getInstance (document).getGuardedSections ();
        ArrayList<String> toDoIDs = new ArrayList<String> ();

        for (GuardedSection section : allSections) {
            if (MultiGuardedSection.isPartOfMultiGuardedSection (section)) {
                if (! isMultiSectionUsed (document, section)) {
                    Object[] objects = MultiGuardedSection.parsePartOfMultiGuardedSection (section);
                    String id = (String) objects[0];
                    if (! toDoIDs.contains (id))
                        toDoIDs.add (id);
                }
            }
        }

        for (String id : toDoIDs)
            MultiGuardedSection.remove (document, id);
    }

    private boolean isMultiSectionUsed (StyledDocument document, GuardedSection section) {
        ArrayList<String> multiGuardedIDs = usedMultiSectionIDs.get (document);
        return multiGuardedIDs != null  &&  multiGuardedIDs.contains (MultiGuardedSection.parsePartOfMultiGuardedSection (section)[0]);
    }

    void registerUsedMultiGuardedSection (StyledDocument document, MultiGuardedSection section) {
        ArrayList<String> multiSections = usedMultiSectionIDs.get (document);
        if (multiSections == null) {
            multiSections = new ArrayList<String> ();
            usedMultiSectionIDs.put (document, multiSections);
        }
        multiSections.add (section.getMultiGuardedID ());
    }

}
