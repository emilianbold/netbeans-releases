/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.navigator;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.xml.text.structure.XMLDocumentModelProvider;


/** TreeCellRenderer implementation for the XML Navigator.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class NavigatorTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private static final String TAG_16 = "/org/netbeans/modules/xml/text/navigator/resources/tag.gif";
    private static final String PI_16 = "/org/netbeans/modules/xml/text/navigator/resources/xml.png";
    private static final String DOCTYPE_16 = "/org/netbeans/modules/xml/text/navigator/resources/doc_type.png";
    private static final String CDATA_16 = "/org/netbeans/modules/xml/text/navigator/resources/comment.gif";
    
    private static final Icon TAG_ICON = getImageIcon(TAG_16);
    private static final Icon PI_ICON = getImageIcon(PI_16);
    private static final Icon DOCTYPE_ICON = getImageIcon(DOCTYPE_16);
    private static final Icon CDATA_ICON = getImageIcon(CDATA_16);
    
    public NavigatorTreeCellRenderer() {
        super();
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        Component orig = super.getTreeCellRendererComponent(tree, value, sel, expanded,
                leaf, row, hasFocus );
        TreeNodeAdapter tna = (TreeNodeAdapter)value;
        DocumentElement de = (DocumentElement)tna.getDocumentElement();
        
        setToolTipText(tna.getToolTipText().trim().length() > 0 ? tna.getToolTipText() : null);
        
        if(de.getType().equals(XMLDocumentModelProvider.XML_TAG)
        || de.getType().equals(XMLDocumentModelProvider.XML_EMPTY_TAG)) {
            setIcon(TAG_ICON);
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_PI)) {
            setIcon(PI_ICON);
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_DOCTYPE)) {
            setIcon(DOCTYPE_ICON);
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_CDATA)) {
            setIcon(CDATA_ICON);
        } 
        
        return this;
    }
    
    public static ImageIcon getImageIcon(String name){
        return new ImageIcon(NavigatorTreeCellRenderer.class.getResource(name));
    }
    
}
