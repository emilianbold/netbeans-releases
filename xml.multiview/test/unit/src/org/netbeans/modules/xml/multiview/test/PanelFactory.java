package org.netbeans.modules.xml.multiview.test;

import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.test.bookmodel.*;

/**
 *
 * @author mkuchtiak
 */
public class PanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    private BookDataObject dObj;
    ToolBarDesignEditor editor;
    
    /** Creates a new instance of ServletPanelFactory */
    PanelFactory(ToolBarDesignEditor editor, BookDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        if (key instanceof Book) return new BookPanel((SectionView)editor.getContentView(), dObj, (Book)key);
        else return new ChapterPanel((SectionView)editor.getContentView(), dObj, (Chapter)key);
    }
}
