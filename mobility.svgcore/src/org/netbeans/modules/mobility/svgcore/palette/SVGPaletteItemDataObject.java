package org.netbeans.modules.mobility.svgcore.palette;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public final class SVGPaletteItemDataObject extends MultiDataObject {
    private final SVGPaletteItemData m_data;
    
    public SVGPaletteItemDataObject(FileObject pf, SVGPaletteItemDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        //CookieSet cookies = getCookieSet();
        //cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        m_data = SVGPaletteItemData.get(pf);
    }

    public  SVGPaletteItemData getData() {
        return m_data;
    }
    
    public File getReferencedFile() throws FileNotFoundException, IOException {
        return new File( m_data.getFilePath());
    }
    
    protected Node createNodeDelegate() {
        return new SVGPaletteItemDataNode(this, getLookup());
    }
    
    @Override
    public Lookup getLookup() {
        InstanceContent ic = new InstanceContent();
        
        ic.add( new ActiveEditorDrop() {
            public boolean handleTransfer(JTextComponent target) {
                SVGDataObject dObj = SVGDataObject.getActiveDataObject(target);
                if ( dObj != null) {
                    try {
                        File file = getReferencedFile();
                        if ( file.exists() && file.isFile()) {
                            SceneManager.log(Level.INFO, "Dropping file " + file.getPath()); //NOI18N
                            insert( dObj.getModel().getSVGBody(file, null), target); 
                            return true;
                        } else {
                            SceneManager.log(Level.SEVERE, "Nothing to drop, file " + file + " not found"); //NOI18N
                        }
                    } catch (Exception ex) {
                        SceneManager.error("Error during image merge", ex); //NOI18N
                    } 
                } else {
                    SceneManager.log(Level.INFO, "SVGDataObject not found."); //NOI18N
                }
                return false;
            }
        });
        ic.add(this);
        return new AbstractLookup(ic);
        //return getCookieSet().getLookup();
    }       
    
    private static void insert( String text, JTextComponent target) {
        Document doc = target.getDocument();
        
        if ( doc instanceof BaseDocument) {
            BaseDocument bDoc = (BaseDocument) doc;

            try {
                bDoc.atomicLock();
                
                Caret caret = target.getCaret();
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                doc.remove(p0, p1 - p0);
                
                int start = caret.getDot();
                doc.insertString(start, text, null);
                
                int end = start + text.length();
                bDoc.getFormatter().reformat(bDoc, start, end);
                bDoc.atomicUnlock();
            } catch( Exception e) {
                bDoc.atomicUndo();
            }
        }
    }
}