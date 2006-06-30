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

package org.netbeans.modules.xml.text.navigator;
import java.awt.Component;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.xml.text.structure.XMLDocumentModelProvider;
import org.openide.awt.HtmlRenderer;
import org.openide.util.Utilities;


/** TreeCellRenderer implementatin for the XML Navigator.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class NavigatorTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private static final String TAG_16 = "org/netbeans/modules/xml/text/navigator/resources/tag.png";
    private static final String PI_16 = "org/netbeans/modules/xml/text/navigator/resources/xml_declaration.png";
    private static final String DOCTYPE_16 = "org/netbeans/modules/xml/text/navigator/resources/doc_type.png";
    private static final String CDATA_16 = "org/netbeans/modules/xml/text/navigator/resources/cdata.png";
    
    private static final String ERROR_16 = "org/netbeans/modules/xml/text/navigator/resources/badge_error.png";
    
    private final Image ERROR_IMAGE = Utilities.loadImage(ERROR_16, true);
   
    private final Icon[] TAG_ICON = new Icon[]{getImageIcon(TAG_16, false), getImageIcon(TAG_16, true)};
    private final Icon[] PI_ICON = new Icon[]{getImageIcon(PI_16, false), getImageIcon(PI_16, true)};
    private final Icon[] DOCTYPE_ICON = new Icon[]{getImageIcon(DOCTYPE_16, false), getImageIcon(DOCTYPE_16, true)};
    private final Icon[] CDATA_ICON = new Icon[]{getImageIcon(CDATA_16, false), getImageIcon(CDATA_16, true)};
     
    private HtmlRenderer.Renderer renderer;
    
    public NavigatorTreeCellRenderer() {
        super();
        renderer = HtmlRenderer.createRenderer();
        renderer.setHtml(true);
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        TreeNodeAdapter tna = (TreeNodeAdapter)value;
        DocumentElement de = (DocumentElement)tna.getDocumentElement();
        
        String htmlText = tna.getText(true);
        Component comp = renderer.getTreeCellRendererComponent(tree, htmlText, sel, expanded, leaf, row, hasFocus);
        comp.setEnabled(tree.isEnabled());
        ((JLabel)comp).setToolTipText(tna.getToolTipText().trim().length() > 0 ? tna.getToolTipText() : null);
        
        boolean containsError = tna.getChildrenErrorCount() > 0;
        //normal icons
        if(de.getType().equals(XMLDocumentModelProvider.XML_TAG)
        || de.getType().equals(XMLDocumentModelProvider.XML_EMPTY_TAG)) {
            setIcon(TAG_ICON, containsError);
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_PI)) {
            setIcon(PI_ICON, containsError);
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_DOCTYPE)) {
            setIcon(DOCTYPE_ICON, containsError);
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_CDATA)) {
            setIcon(CDATA_ICON, containsError);
        }
        
        return comp;
    }
    
    public void setIcon(Icon[] icons, boolean containsError) {
        renderer.setIcon(icons[containsError ? 1 : 0]);
    }
    
    private ImageIcon getImageIcon(String name, boolean error){
        ImageIcon icon = new ImageIcon(Utilities.loadImage(name));
        if(error)
            return new ImageIcon(Utilities.mergeImages( icon.getImage(), ERROR_IMAGE, 15, 7 ));
        else
            return icon;
    }
    
}
