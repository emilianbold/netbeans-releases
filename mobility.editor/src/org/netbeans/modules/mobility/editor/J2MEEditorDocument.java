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

/*
 * Created on Mar 8, 2004
 *
 */
package org.netbeans.modules.mobility.editor;

import java.util.regex.Pattern;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.mobility.antext.preprocessor.LineParserTokens;
import org.netbeans.mobility.antext.preprocessor.PPBlockInfo;
import org.netbeans.mobility.antext.preprocessor.PPLine;
import org.netbeans.mobility.antext.preprocessor.PPToken;
import org.netbeans.mobility.antext.preprocessor.PreprocessorException;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.TextSwitcher;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;

import javax.swing.JEditorPane;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.mobility.editor.ConfigurationHighlightsLayerFactory.Highlighting;
import org.netbeans.modules.mobility.editor.hints.DisableHint;
import org.netbeans.modules.mobility.editor.hints.InlineIncludeHint;
import org.netbeans.modules.mobility.editor.hints.ReplaceOldSyntaxHint;
import org.netbeans.modules.mobility.project.bridge.J2MEProjectUtilitiesProvider;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Adam Sotona
 *
 * Document type which is aware of editable colored code blocks
 */
public class J2MEEditorDocument extends NbEditorDocument {
    
    public static final String PREPROCESSOR_LINE_LIST = "preprocessor.line.list"; //NOI18N
    
    static final long serialVersionUID = 4863325941230276217L;
    static final Pattern BLOCK_HEADER_PATTERN = Pattern.compile("^\\s*/((/#)|(\\*[\\$#]))\\S"); //NOI18N
    
    /** preprocessor tag error annotations */
    protected ArrayList<PPLine> lineList = new ArrayList<PPLine>();
    
    /** listens for document changes and updates blocks appropriately */
    DocumentListener dl;
    
    
    /** Timer which countdowns the auto-reparsing of configuration blocks. */
    Timer timer;
    
    
    public J2MEEditorDocument(String mimeType){
        super(mimeType);
                
        updateBlockChain(J2MEEditorDocument.this);
        
        dl = new DL();
        
        this.addDocumentListener(dl);
        this.getDocumentProperties().put(TextSwitcher.TEXT_SWITCH_SUPPORT, new ChangeListener() {
            public void stateChanged(@SuppressWarnings("unused")
			final ChangeEvent e) {
                updateBlockChain(J2MEEditorDocument.this);
            }
        });
    }
    
    
    final void setLineList(final ArrayList<PPLine> lineList) {
        this.lineList = lineList;
        putProperty(PREPROCESSOR_LINE_LIST, lineList);
        Highlighting headerLayer = (Highlighting) getProperty(ConfigurationHighlightsLayerFactory.PROP_HIGLIGHT_HEADER_LAYER);
        if (headerLayer != null){
            headerLayer.updateBags();
        }
        Highlighting blockLayer = (Highlighting) getProperty(ConfigurationHighlightsLayerFactory.PROP_HIGLIGHT_BLOCKS_LAYER);
        if (blockLayer != null){
            blockLayer.updateBags();
        }
        processAnnotations();
    }
    
    /** Restart the timer which starts the parser after the specified delay.*/
    void restartTimer() {
        
        if (timer==null) {  // initialize timer
            timer = new Timer(200, new ActionListener() {
                public void actionPerformed(@SuppressWarnings("unused")
				final ActionEvent e) {
                    J2MEEditorDocument.updateBlockChain(J2MEEditorDocument.this);
                    
                }
            });
            timer.setRepeats(false);
        }
        
        timer.restart();
        
    }
    
    final public static void updateBlockChain(final Document doc) {
        if (doc instanceof J2MEEditorDocument) {
            
            final Project p = J2MEProjectUtils.getProjectForDocument(doc);
            if (p != null) {
                final ProjectConfigurationsHelper configHelper = p.getLookup().lookup(ProjectConfigurationsHelper.class);
                if (configHelper == null || !configHelper.isPreprocessorOn()) return;
                final HashMap<String,String> activeIdentifiers=new HashMap<String,String>(configHelper.getActiveAbilities());
                activeIdentifiers.put(configHelper.getActiveConfiguration().getDisplayName(),null);
                try {
                    J2MEProjectUtilitiesProvider utilProvider = Lookup.getDefault().lookup(J2MEProjectUtilitiesProvider.class);
                    if (utilProvider == null) return; //we do not run in full NetBeans, but this should not happen here (no editor)
                    final CommentingPreProcessor cpp = new CommentingPreProcessor(utilProvider.createPPDocumentSource((StyledDocument) doc), null, activeIdentifiers);
                    cpp.run();
                    ((J2MEEditorDocument)doc).setLineList(cpp.getLines());
                } catch (PreprocessorException e) {
                    ErrorManager.getDefault().notify(e);
                }
                repaintDocument(doc);
            }
        }
    }

    public static void repaintDocument(final Document doc) {
        final DataObject dob = NbEditorUtilities.getDataObject(doc);
        final EditorCookie ec = (EditorCookie) dob.getCookie(EditorCookie.class);
        if (ec != null) {
            final JEditorPane[] panes = ec.getOpenedPanes();
            if (panes != null) {
                for (int i=0;i<panes.length;i++) try {
                    if (panes[0] != null) panes[i].getUI().damageRange(panes[i],0,doc.getLength()+1);
                } catch (NullPointerException npe) {
                    // see CR #6197050, I don't know how to predict and avoid occasionally NPE beeing thrown from damageRange(...) method.
                }
                
            }
        }
        
    }
    
    static String prefixPropertyName(final String configuration, final String propertyName) {
        return "configs." + configuration + '.' + propertyName; // NOI18N
    }
    
    class DL implements DocumentListener {
        
        
        public void changedUpdate(@SuppressWarnings("unused")
		final DocumentEvent arg0) {
        }
        
        public void insertUpdate(@SuppressWarnings("unused")
		final DocumentEvent evt) {
            J2MEEditorDocument.this.restartTimer();
        }
        
        public void removeUpdate(@SuppressWarnings("unused")
		final DocumentEvent evt) {
            J2MEEditorDocument.this.restartTimer();
        }
    }
    
    
    /*****              Annotation Stuff                                           ********/
    
    void processAnnotations() {  //XXX needs to be split for errors and warnings
        final ArrayList<ErrorDescription> errs = new ArrayList();
        DataObject dob = NbEditorUtilities.getDataObject(J2MEEditorDocument.this);
        FileObject fo = dob == null ? null : dob.getPrimaryFile();
        for (PPLine line : lineList ) {
            for (PPLine.Error err : line.getErrors()) {
                PPToken tok = err.token;
                int shift = (tok.getType() == LineParserTokens.END_OF_FILE || tok.getType() == LineParserTokens.END_OF_LINE || tok.getType() == LineParserTokens.OTHER_TEXT) ? Math.max(1, tok.getPadding().length()) : 0;
                int loff = NbDocument.findLineOffset(this, line.getLineNumber()-1);
                errs.add(ErrorDescriptionFactory.createErrorDescription(err.warning ? Severity.WARNING : Severity.ERROR, err.message, fo, loff + tok.getColumn() - shift, loff + tok.getColumn() + tok.getText().length()));  
            }
            ArrayList<Fix> fixes = new ArrayList();
            int start = Utilities.getRowStartFromLineOffset(this, line.getLineNumber()-1);
            if (line.getTokens().size() > 1 && "//#include".equals(line.getTokens().get(0).getText())) { //NOI18N
                fixes.add(new InlineIncludeHint(this, start, line.getTokens().get(1).getText()));
            } else if (line.getType() == PPLine.OLDIF || line.getType() == PPLine.OLDENDIF) {
                PPBlockInfo b = line.getBlock();
                while (b != null && b.getType() != PPLine.OLDIF) {
                    b = b.getParent();
                }
                if (b != null) fixes.add(new ReplaceOldSyntaxHint(this, lineList, b));
            }
            if (line.getType() == PPLine.UNKNOWN) fixes.add(new DisableHint(this, start));
            if (fixes.size() > 0) errs.add(ErrorDescriptionFactory.createErrorDescription(Severity.HINT, NbBundle.getMessage(J2MEEditorDocument.class, "LBL_PreprocessorHint"), fixes, this, line.getLineNumber())); //NOI18N
        }
        HintsController.setErrors(this, "preprocessor-errors", errs); //NOI18N
    }
   
    /*****              End Annotation Stuff                                           ********/
}
