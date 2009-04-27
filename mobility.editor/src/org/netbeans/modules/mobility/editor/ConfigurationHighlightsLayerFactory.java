package org.netbeans.modules.mobility.editor;

import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.project.Project;
import org.netbeans.mobility.antext.preprocessor.PPBlockInfo;
import org.netbeans.mobility.antext.preprocessor.PPLine;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.text.NbDocument;
import org.openide.util.WeakListeners;

public class ConfigurationHighlightsLayerFactory implements HighlightsLayerFactory {
    
    static String PROP_HIGLIGHT_HEADER_LAYER = "mobility-embedded-headers-highlighting-layer"; //NOI18N
    static String PROP_HIGLIGHT_BLOCKS_LAYER = "mobility-embedded-blocks-highlighting-layer"; //NOI18N
    
    public ConfigurationHighlightsLayerFactory() {
        super();
    }

    public HighlightsLayer[] createLayers(Context context) {
        return new HighlightsLayer[]{
            HighlightsLayer.create(PROP_HIGLIGHT_HEADER_LAYER,
            ZOrder.SYNTAX_RACK.forPosition(120),
            true,
            new HeadersHighlighting(context.getDocument())),
            HighlightsLayer.create(PROP_HIGLIGHT_BLOCKS_LAYER,
            ZOrder.BOTTOM_RACK.forPosition(110),
            true,
            new BlocksHighlighting(context.getDocument()))
        };
    }
    
    static interface Highlighting {
        void updateBags();
    }

    static class HeadersHighlighting extends AbstractHighlightsContainer  implements Highlighting, DocumentListener{
        private static final Pattern BLOCK_HEADER_PATTERN = Pattern.compile("^\\s*/((/#)|(\\*[\\$#]))\\S"); //NOI18N
        private Document document;
        private OffsetsBag headersBag;
        
        public HeadersHighlighting(Document document) {
            this.document = document;
            this.document.addDocumentListener(WeakListeners.document(this, this.document));
            this.document.putProperty(PROP_HIGLIGHT_HEADER_LAYER, this);
            headersBag = new OffsetsBag(document);
            headersBag.addHighlightsChangeListener(new HighlightsChangeListener() {
                public void highlightChanged(HighlightsChangeEvent event) {
                    fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
                }
            });
            updateBags();
        }

        @Override
        public HighlightsSequence getHighlights(final int startOffset, final int endOffset) {
            return headersBag.getHighlights(startOffset, endOffset);
        }

        public void insertUpdate(DocumentEvent e) {
            this.headersBag.removeHighlights(e.getOffset(), e.getOffset() + e.getLength(), false);
        }

        public void removeUpdate(DocumentEvent e) {
            this.headersBag.removeHighlights(e.getOffset() - 1, e.getOffset() + e.getLength() - 1, false);
        }

        public void changedUpdate(DocumentEvent e) {
        }

        public void updateBags() {
            final Project p = J2MEProjectUtils.getProjectForDocument(document);
            //TODO J2MEProject?
            if (p == null || !(p instanceof J2MEProject)) 
                return;

            document.render(new Runnable() {
                public void run() {
                    StyledDocument doc = (StyledDocument)document;
                    OffsetsBag bag = new OffsetsBag(document, true);
                    Element root = NbDocument.findLineRootElement(doc);
                    int count = root.getElementCount();
                    for(int i = 0; i < count; i++){
                        try {
                            Element elm = root.getElement(i);
                            if (BLOCK_HEADER_PATTERN.matcher(doc.getText(elm.getStartOffset(), elm.getEndOffset() - elm.getStartOffset()).trim()).find()){
                                bag.addHighlight( elm.getStartOffset(), elm.getEndOffset(), getAttributes("pp-command", false, false)); //NOI18N
                            }
                        } catch (BadLocationException ex) {                            
                        }
                    }
                    headersBag.setHighlights(bag);
                }
            });
        }
    }

    static class BlocksHighlighting extends AbstractHighlightsContainer implements Highlighting, DocumentListener{
        private Document document;
        private OffsetsBag blocksBag;

        public BlocksHighlighting(Document document) {
            this.document = document;
            this.document.addDocumentListener(WeakListeners.document(this, this.document));
            this.document.putProperty(PROP_HIGLIGHT_BLOCKS_LAYER, this);
            blocksBag = new OffsetsBag(document, true);
            blocksBag.addHighlightsChangeListener(new HighlightsChangeListener() {
                public void highlightChanged(HighlightsChangeEvent event) {
                    fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
                }
            });
            updateBags();
        }

        @Override
        public HighlightsSequence getHighlights(int startOffset, int endOffset) {
            return blocksBag.getHighlights(startOffset, endOffset);
        }    

        public void updateBags() {
            final Project p = J2MEProjectUtils.getProjectForDocument(document);
            //TODO J2MEProject?
            if (p == null || !(p instanceof J2MEProject))
                return;

            document.render(new Runnable() {
                public void run() {
                    OffsetsBag bag = new OffsetsBag(document, true);
                    ArrayList<PPLine> lineList = (ArrayList<PPLine>)document.getProperty(DocumentPreprocessor.PREPROCESSOR_LINE_LIST);
                    if (lineList == null) return;
                    for (PPLine line : lineList ) {
                        PPBlockInfo b = line.getBlock();
                        if (b != null){                                        
                            StyledDocument doc = (StyledDocument)document;
                            bag.addHighlight(
                                    NbDocument.findLineRootElement(doc).getElement(b.getStartLine() - 1).getStartOffset(),
                                    NbDocument.findLineRootElement(doc).getElement(b.getEndLine() - 1).getEndOffset(),
                                    b.isActive() ? getAttributes("pp-active-block", true, true) : getAttributes("pp-inactive-block", true, true)); //NOI18N
                        }
                    }
                    blocksBag.setHighlights(bag);
                }
            });
        }

        public void insertUpdate(DocumentEvent e) {
            this.blocksBag.removeHighlights(e.getOffset(), e.getOffset() + e.getLength(), false);
        }

        public void removeUpdate(DocumentEvent e) {
            this.blocksBag.removeHighlights(e.getOffset() - 1, e.getOffset() + e.getLength() - 1, false);
        }

        public void changedUpdate(DocumentEvent e) {
        }
   }

    private static AttributeSet getAttributes(String token, boolean extendsEol, boolean extendsEmptyLine) {
        FontColorSettings settings = MimeLookup.getLookup("text/x-java-preprocessor").lookup(FontColorSettings.class); //NOI18N
        return AttributesUtilities.createImmutable(
                settings.getTokenFontColors(token), 
                AttributesUtilities.createImmutable(
                    HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.valueOf(extendsEol),
                    HighlightsContainer.ATTR_EXTENDS_EMPTY_LINE, Boolean.valueOf(extendsEmptyLine)));
    }
}