/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor.Destination;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor.Source;
import org.netbeans.mobility.antext.preprocessor.PreprocessorException;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.TextSwitcher;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.bridge.J2MEProjectUtilitiesProvider;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
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
                J2MEProjectUtilitiesProvider utilProvider = Lookup.getDefault().lookup(J2MEProjectUtilitiesProvider.class);
                if (utilProvider == null) return; //we do not run in full NetBeans, but this should not happen here (no editor)

                final Source ppSource = utilProvider.createPPDocumentSource(doc);
                final Destination ppDestination = utilProvider.createPPDocumentDestination(doc);
                final ProjectConfiguration conf = pch.getActiveConfiguration();
                final HashMap<String,String> identifiers=new HashMap<String,String>(pch.getAbilitiesFor(conf));
                identifiers.put(conf.getDisplayName(),null);
                final CommentingPreProcessor cpp =new CommentingPreProcessor(ppSource, ppDestination, identifiers);
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
