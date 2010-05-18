package org.netbeans.modules.iep.editor.designer;

import java.awt.Dimension;
import java.awt.Point;
import java.net.URL;

import javax.swing.ImageIcon;

import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoNode;

public class DocumentationNode extends JGoNode {

    private JGoImage mDocumentationIcon = null;
    
    private boolean showingDocumentaion = false;
    
    private DocumentationControl docControl = null;
    
    public DocumentationNode() {
        init();
    }
    
    
    private void init() {
        mDocumentationIcon = new JGoImage(new Point(), new Dimension(10, 10));
        
        try {
            
            URL imgURL = SimpleNode.class.getResource("/images/documentationx16.png");;
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                mDocumentationIcon.loadImage(icon.getImage(), true);
                mDocumentationIcon.setSelectable(false);
                mDocumentationIcon.setResizable(false);
            }
            this.addObjectAtTail(mDocumentationIcon);
            
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    
}
