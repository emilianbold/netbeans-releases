/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    public void updateUserCodesFromEditor (final StyledDocument document, 
            final DesignDocument designDocument) {
        NbDocument.runAtomic (document, new Runnable() {
            public void run () {
                updateUserCodesFromEditorCore (document, designDocument);
            }
        });
    }

    public void preUpdateCode (final StyledDocument document, 
            final DesignDocument designDocument)
    {
        //org.openide.LifecycleManager.getDefault().saveAll();
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

    private void updateUserCodesFromEditorCore (StyledDocument document, 
            DesignDocument designDocument)
    {
        final Collection<ModelUpdatePresenter> presenters = DocumentSupport.gatherAllPresentersOfClass (designDocument,
                ModelUpdatePresenter.class);
        for (ModelUpdatePresenter presenter : presenters) {
            presenter.modelUpdated();
        }
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
