package org.netbeans.modules.iep.editor.designer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.nwoods.jgo.JGoControl;
import com.nwoods.jgo.JGoView;
import javax.rmi.CORBA.Util;

public class DocumentationControl extends JGoControl {

    private DocumentationComponent mComp = null;
    
    public DocumentationControl(JGoView view, EntityNode node) {
        mComp = new DocumentationComponent(view, this, node);
    }
    
        
    @Override
    public JComponent createComponent(JGoView arg0) {
        mComp.setPreferredSize(new Dimension(300, 150));
        return mComp;
    }
    
    public void setDocumentation(String doc) {
        mComp.setDocumentation(doc);
    }
    
    public String getDocumentation() {
        return this.mComp.getDocumentation();
    }
    
    public void storeDocumentation() {
        mComp.storeDocumentation();
    }
}
