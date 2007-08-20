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

/*
 * AddProjectConfigurationAction.java
 *
 * Created on July 26, 2005, 6:59 PM
 *
 */
package org.netbeans.modules.mobility.editor.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import javax.swing.*;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.mobility.antext.preprocessor.PreprocessorException;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.TextSwitcher;
import org.netbeans.modules.mobility.project.preprocessor.PPDocumentDestination;
import org.netbeans.modules.mobility.project.preprocessor.PPDocumentSource;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class RecommentAction extends PreprocessorEditorContextAction {
    
    public static final String NAME = NbBundle.getMessage(RecommentAction.class, "LBL_Recomment_Full") ;//NOI18N
    
    /** Creates a new instance of AddProjectConfigurationAction */
    public RecommentAction() {
        super(NAME); //NOI18N
        putValue(NO_KEYBINDING, Boolean.TRUE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift R"));//NOI18N
    }
    
    public boolean isEnabled(final ProjectConfigurationsHelper cfgProvider, @SuppressWarnings("unused")
	final ArrayList preprocessorLineList, @SuppressWarnings("unused")
	final JTextComponent target) {
        return cfgProvider != null && cfgProvider.isPreprocessorOn();
    }
    
    public String getPopupMenuText(@SuppressWarnings("unused")
	final ProjectConfigurationsHelper cfgProvider, @SuppressWarnings("unused")
	final ArrayList preprocessorLineList, @SuppressWarnings("unused")
	final JTextComponent target) {
        return NbBundle.getMessage(RecommentAction.class, "LBL_Recomment") ;//NOI18N
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final ActionEvent evt, final JTextComponent txt) {
        actionPerformed(txt);
    }
    
    static void actionPerformed(final JTextComponent txt) {
        if (txt != null && txt.getDocument() instanceof StyledDocument) actionPerformed(txt.getDocument());
    }
    
    public static void actionPerformed(final Document document) {
        if (!((document instanceof StyledDocument) && (document instanceof BaseDocument))) return;
        final StyledDocument doc = (StyledDocument)document;
        final Project p = J2MEProjectUtils.getProjectForDocument(doc);
        if (p != null) {
            final ProjectConfigurationsHelper pch = p.getLookup().lookup(ProjectConfigurationsHelper.class);
            if (pch != null && pch.isPreprocessorOn()) try {
                final PPDocumentSource ppSource = new PPDocumentSource(doc);
                final PPDocumentDestination ppDestination = new PPDocumentDestination((BaseDocument)doc);
                final ProjectConfiguration conf = pch.getActiveConfiguration();
                final HashMap<String,String> identifiers=new HashMap<String,String>(pch.getAbilitiesFor(conf));
                identifiers.put(conf.getDisplayName(),null);
                final CommentingPreProcessor cpp =new CommentingPreProcessor(ppSource, ppDestination, identifiers) ;
                //note: nbr transaction is already locked here
                try {
                    doc.putProperty(TextSwitcher.SKIP_DUCUMENT_CHANGES, TextSwitcher.SKIP_DUCUMENT_CHANGES);
                    NbDocument.runAtomic(doc,cpp); // NOI18N
                } finally {
                    doc.putProperty(TextSwitcher.SKIP_DUCUMENT_CHANGES, null);
                }
            } catch (PreprocessorException pe) {
                ErrorManager.getDefault().notify(pe);
            }
        }
    }
}
