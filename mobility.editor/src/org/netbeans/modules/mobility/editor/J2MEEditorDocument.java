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
 * Created on Mar 8, 2004
 *
 */
package org.netbeans.modules.mobility.editor;

import java.util.regex.Pattern;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.project.Project;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.DrawContext;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.MarkFactory.DrawMark;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.mobility.antext.preprocessor.LineParserTokens;
import org.netbeans.mobility.antext.preprocessor.PPBlockInfo;
import org.netbeans.mobility.antext.preprocessor.PPLine;
import org.netbeans.mobility.antext.preprocessor.PPToken;
import org.netbeans.mobility.antext.preprocessor.PreprocessorException;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.TextSwitcher;
import org.netbeans.modules.mobility.project.preprocessor.PPDocumentSource;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import javax.swing.JEditorPane;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.java.JavaDocument;
import org.netbeans.modules.mobility.editor.hints.DisableHint;
import org.netbeans.modules.mobility.editor.hints.InlineIncludeHint;
import org.netbeans.modules.mobility.editor.hints.ReplaceOldSyntaxHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;






/**
 * @author Adam Sotona
 *
 * Document type which is aware of editable colored code blocks
 */
public class J2MEEditorDocument extends JavaDocument {
    
    public static final String PREPROCESSOR_LINE_LIST = "preprocessor.line.list"; //NOI18N
    
    static final long serialVersionUID = 4863325941230276217L;
    static final Pattern BLOCK_HEADER_PATTERN = Pattern.compile("^\\s*/((/#)|(\\*[\\$#]))\\S"); //NOI18N
    
    static Coloring BLOCK_HEADER_COLORING = new Coloring(Font.decode("Monospaced bold"), Coloring.FONT_MODE_APPLY_STYLE, new Color(0, 112, 0), null);//NOI18N
    static Coloring ACTIVE_BLOCK_COLORING = new Coloring(null, null, new Color(250, 225, 240));
    static Coloring INACTIVE_BLOCK_COLORING = new Coloring(null, null, new Color(235, 225, 250));
    static LookupListener LL = null;
    static Lookup.Result LR = null;
    
    /** preprocessor tag error annotations */
    protected ArrayList<PPLine> lineList = new ArrayList<PPLine>();
    
    /** listens for document changes and updates blocks appropriately */
    DocumentListener dl;
    
    
    /** Timer which countdowns the auto-reparsing of configuration blocks. */
    Timer timer;
    
    /** Config block coloring layers */
    transient ConfigurationBlocksLayer cbl=null;
    transient ConfigurationHeadersLayer chl=null;
    
    private static synchronized void initColoring() {
        if (LL != null) return;
        LR = MimeLookup.getMimeLookup("text/x-java-preprocessor").lookup(new Lookup.Template<FontColorSettings>(FontColorSettings.class)); //NOI18N
        LL = new LookupListener() {
            public void resultChanged(@SuppressWarnings("unused")
			final LookupEvent ev) {
                final FontColorSettings fcs = (FontColorSettings)LR.allInstances().iterator().next();
                BLOCK_HEADER_COLORING = toColoring(fcs.getTokenFontColors("pp-command"), BLOCK_HEADER_COLORING); //NOI18N
                ACTIVE_BLOCK_COLORING = toColoring(fcs.getTokenFontColors("pp-active-block"), ACTIVE_BLOCK_COLORING); //NOI18N
                INACTIVE_BLOCK_COLORING = toColoring(fcs.getTokenFontColors("pp-inactive-block"), INACTIVE_BLOCK_COLORING); //NOI18N
            }
        };
        LR.addLookupListener(LL);
        LL.resultChanged(null);
    }
    
    public J2MEEditorDocument(Class kitClass) {
        super(kitClass);
        if (LL == null) initColoring();
        cbl = new ConfigurationBlocksLayer();
        this.addLayer(cbl, 1200);
        chl = new ConfigurationHeadersLayer();
        this.addLayer(chl, 1300);
                
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
                    final CommentingPreProcessor cpp = new CommentingPreProcessor(new PPDocumentSource(doc), null, activeIdentifiers);
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
    
    /** responsible for coloring the entire code block */
    class ConfigurationBlocksLayer extends DrawLayer.AbstractLayer {
        
        static final String CONFIG_BLOCKS_LAYER_NAME="CONFIG_BLOCKS_LAYER"; // NOI18N
        
        J2MEEditorDocument doc;
        
        Coloring currentColoring=INACTIVE_BLOCK_COLORING;
        
        public ConfigurationBlocksLayer() {
            super(CONFIG_BLOCKS_LAYER_NAME);
            
        }
        
        public void init(final DrawContext ctx) {
            super.init(ctx);
            doc = (J2MEEditorDocument)ctx.getEditorUI().getDocument();
        }
        
        public boolean extendsEmptyLine() {
            return true;
        }
        
        public boolean extendsEOL() {
            return true;
        }
        
        protected Coloring getColoring(@SuppressWarnings("unused")
		final DrawContext ctx) {
            return currentColoring;
        }
        
        public boolean isActive(final DrawContext ctx, @SuppressWarnings("unused")
		final DrawMark mark) {
            final int line = NbDocument.findLineNumber(doc, ctx.getStartOffset())+1;
            if (line >= lineList.size()) return false;
            final PPBlockInfo b = lineList.get(line - 1).getBlock();
            if (b == null) return false;
            currentColoring = b.isActive() ? ACTIVE_BLOCK_COLORING : INACTIVE_BLOCK_COLORING;
            return  b.isToBeCommented();
        }
        
        public void updateContext(final DrawContext ctx) {
            currentColoring.apply(ctx);
        }
        
    }
    
    
    /** responsible for coloring the entire code block */
    class ConfigurationHeadersLayer extends DrawLayer.AbstractLayer {
        
        static final String CONFIG_HEADERS_LAYER_NAME="CONFIG_HEADERS_LAYER"; // NOI18N
        
        public ConfigurationHeadersLayer() {
            super(CONFIG_HEADERS_LAYER_NAME);
        }
        
        public boolean extendsEmptyLine() {
            return false;
        }
        
        public boolean extendsEOL() {
            return false;
        }
        
        protected Coloring getColoring(@SuppressWarnings("unused")
		final DrawContext ctx) {
            return BLOCK_HEADER_COLORING;
        }
        
        public boolean isActive(final DrawContext ctx, @SuppressWarnings("unused")
		final DrawMark mark) {
            final int s = ctx.getStartOffset();
            return BLOCK_HEADER_PATTERN.matcher(new String(ctx.getBuffer(), s - ctx.getBufferStartOffset(), ctx.getEndOffset() - s)).find();
        }
        
        public void updateContext(final DrawContext ctx) {
            BLOCK_HEADER_COLORING.apply(ctx);
        }
        
    }
    
    /*****              End Draw Layers                                           ********/
    
    
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
    
    protected static Coloring toColoring(final AttributeSet as, final Coloring defaults) {
        if (as == null) return defaults;
        final Color back = (Color)as.getAttribute(StyleConstants.Background);
        final Color fore = (Color)as.getAttribute(StyleConstants.Foreground);
        final Color underline = (Color)as.getAttribute(StyleConstants.Underline);
        final Color strike = (Color)as.getAttribute(StyleConstants.StrikeThrough);
        final Color wave = (Color)as.getAttribute(EditorStyleConstants.WaveUnderlineColor);
        final Font font = toFont(as);
        return new Coloring(font, Coloring.FONT_MODE_DEFAULT, fore, back, underline, strike, wave);
    }
    
    static Font toFont(final AttributeSet s) {
        if (s.getAttribute(StyleConstants.FontFamily) == null) return null;
        int style = 0;
        if (s.getAttribute(StyleConstants.Bold) != null && s.getAttribute(StyleConstants.Bold).equals(Boolean.TRUE)) style += Font.BOLD;
        if (s.getAttribute(StyleConstants.Italic) != null && s.getAttribute(StyleConstants.Italic).equals(Boolean.TRUE)) style += Font.ITALIC;
        return new Font((String) s.getAttribute(StyleConstants.FontFamily), style, ((Integer) s.getAttribute(StyleConstants.FontSize)).intValue());
    }
    
}
