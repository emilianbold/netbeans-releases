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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Enumeration;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.xml.text.structure.XMLDocumentModelProvider;
import org.openide.ErrorManager;

/** TreeCellRenderer implementation for the XML Navigator.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class NavigatorTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private static HashMap iconsMap = new HashMap();
    
    public static final String TAG_16 = "/org/netbeans/modules/xml/text/navigator/resources/tag.gif";
    public static final String PI_16 = "/org/netbeans/modules/xml/text/navigator/resources/xml.png";
    public static final String DOCTYPE_16 = "/org/netbeans/modules/xml/text/navigator/resources/doc_type.png";
    public static final String COMMENT_16 = "/org/netbeans/modules/xml/text/navigator/resources/comment.gif";
    
    private XMLTagComponent xmltc;
    private PIComponent pic;
    private DOCTYPEComponent dtc;
    private CommentComponent cc;
    
    public NavigatorTreeCellRenderer() {
        super();
        xmltc = new XMLTagComponent();
        pic = new PIComponent();
        dtc = new DOCTYPEComponent();
        cc = new CommentComponent();
    }
    
    private JComponent getXMLTagComponent(TreeNodeAdapter tna, boolean selected) {
        String name = tna.getDocumentElement().getName();
        
        String attribsVisibleText = "";
        AttributeSet attribs = tna.getDocumentElement().getAttributes();
        StringBuffer attribsText = new StringBuffer();
        if(attribs.getAttributeCount() > 0) {
            //add attributes + text content
            Enumeration attrNames = attribs.getAttributeNames();
            if(attrNames.hasMoreElements()) {
                attribsText.append("(");
                while(attrNames.hasMoreElements()) {
                    String aname = (String)attrNames.nextElement();
                    String value = (String)attribs.getAttribute(aname);
                    attribsText.append(aname + "=" + value);
                    if(attrNames.hasMoreElements()) attribsText.append(", ");
                }
                attribsText.append(")");
            }
            if(NavigatorContent.showAttributes) {
                attribsVisibleText = attribsText.length() > ATTRIBS_MAX_LEN ? attribsText.substring(0,ATTRIBS_MAX_LEN) + "..." : attribsText.toString();
            }
        }
        
        String contentText = "";
        String documentText = tna.getDocumentContent();
        if(NavigatorContent.showContent) {
            contentText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
        }
        
        String ttText = attribsText.toString() + " " + documentText;
        
        //update the rendering component content and layout
        xmltc.updateContent(selected, name, attribsVisibleText, contentText, ttText);
        
        return xmltc;
    }
    
    private JComponent getPIComponent(TreeNodeAdapter tna, boolean selected) {
        String name = tna.getDocumentElement().getName();
        
        DocumentElement de = tna.getDocumentElement();
        Document doc = de.getDocumentModel().getDocument();
        String documentText = "???";
        try {
            documentText = doc.getText(de.getStartOffset(), de.getEndOffset() - de.getStartOffset());
            //cut the leading PI name and the <?
            if(documentText.length() > 0) documentText = documentText.substring("<?".length() + de.getName().length(), documentText.length() - 1).trim();
        }catch(BadLocationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        //limit the text length
        String visibleText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;

        //update the rendering component content and layout
        pic.updateContent(selected, name, "", visibleText, documentText);
        
        return pic;
    }
    
    private JComponent getDOCTYPEComponent(TreeNodeAdapter tna, boolean selected) {
        String name = tna.getDocumentElement().getName();
        
        DocumentElement de = tna.getDocumentElement();
        Document doc = de.getDocumentModel().getDocument();
        String documentText = "???";
        try {
            documentText = doc.getText(de.getStartOffset(), de.getEndOffset() - de.getStartOffset());
            //cut the leading PI name and the <?
            if(documentText.length() > 0) documentText = documentText.substring("<!DOCTYPE ".length() + de.getName().length(), documentText.length() - 1).trim();
        }catch(BadLocationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        //limit the text length
        String visibleText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
        
        //update the rendering component content and layout
        dtc.updateContent(selected, name, "", visibleText, documentText);
        
        return dtc;
    }
    
    private JComponent getCommentComponent(TreeNodeAdapter tna, boolean selected) {
        DocumentElement de = tna.getDocumentElement();
        Document doc = de.getDocumentModel().getDocument();
        String documentText = "???";
        try {
            documentText = doc.getText(de.getStartOffset(), de.getEndOffset() - de.getStartOffset());
            //cut the leading PI name and the <?
            if(documentText.length() > "<!---->".length()) documentText = documentText.substring("<!--".length() , documentText.length() - "-->".length()).trim();
        }catch(BadLocationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        //limit the text length
        String visibleText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
        
        //update the rendering component content and layout
        cc.updateContent(selected, "", "", visibleText, documentText);
        
        return cc;
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        Component orig = super.getTreeCellRendererComponent(tree, value, sel, expanded,
                leaf, row, hasFocus );
        DocumentElement de = (DocumentElement)((TreeNodeAdapter)value).getDocumentElement();
        
        if(de.getType().equals(XMLDocumentModelProvider.XML_TAG)
        || de.getType().equals(XMLDocumentModelProvider.XML_EMPTY_TAG)) {
            return getXMLTagComponent((TreeNodeAdapter)value, sel);
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_PI)) {
            return getPIComponent((TreeNodeAdapter)value, sel);
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_DOCTYPE)) {
            return getDOCTYPEComponent((TreeNodeAdapter)value, sel);
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_COMMENT)) {
            return getCommentComponent((TreeNodeAdapter)value, sel);
        }
        
        return orig;
    }
    
    public static ImageIcon getImageIcon(String name){
        if(iconsMap.containsKey(name))
            return (ImageIcon)iconsMap.get(name);
        else{
            iconsMap.put(name, new ImageIcon(NavigatorTreeCellRenderer.class.getResource(name)));
            return (ImageIcon)iconsMap.get(name);
        }
    }

    public abstract class TreeNodeComponent extends JPanel {
        JLabel componentNameLabel, attrsLabel, textLabel;
        JPanel namePanel;
        
        public TreeNodeComponent() {
            super();
            initialize();
        }
        
        protected abstract String getIconName();
            
        public void updateContent(boolean selected, String name, String attrs, String text, String tooltip) {
            componentNameLabel.setText(name);
            attrsLabel.setText(attrs);
            textLabel.setText(text);
            setToolTipText(tooltip);
            namePanel.setBackground(selected ? UIManager.getDefaults().getColor("Tree.selectionBackground") : Color.WHITE);
        }
        
        private void initialize() {
            setBackground(Color.WHITE);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            
            //add icon
            JLabel iconLabel = new JLabel("", getImageIcon(getIconName()), JLabel.CENTER);
            add(iconLabel);
            
            //add name label
            namePanel = new JPanel();
            namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
            namePanel.setBackground(selected ? UIManager.getDefaults().getColor("Tree.selectionBackground") : Color.WHITE);
            namePanel.setBorder(new EmptyBorder(new Insets(0,3,0,3)));
            
            componentNameLabel = new JLabel();
            namePanel.add(componentNameLabel);
            add(namePanel);
            
            attrsLabel = new JLabel();
            attrsLabel.setForeground(Color.GRAY.darker());
            attrsLabel.setBorder(new EmptyBorder(new Insets(0,3,0,3)));
            add(attrsLabel);
            
            textLabel = new JLabel();
            textLabel.setForeground(Color.GRAY);
            add(textLabel);
        }
    }
    public class XMLTagComponent extends TreeNodeComponent {
        public XMLTagComponent() {
            super();
        }
        protected String getIconName() {
            return TAG_16;
        }
    }
    public class PIComponent extends TreeNodeComponent {
        public PIComponent() {
            super();
        }
        protected String getIconName() {
            return PI_16;
        }
    }
    public class DOCTYPEComponent extends TreeNodeComponent {
        public DOCTYPEComponent() {
            super();
        }
        protected String getIconName() {
            return DOCTYPE_16;
        }
    }
    public class CommentComponent extends TreeNodeComponent {
        public CommentComponent() {
            super();
        }
        protected String getIconName() {
            return COMMENT_16;
        }
    }
    
    private static final int ATTRIBS_MAX_LEN = 30;
    private static final int TEXT_MAX_LEN = ATTRIBS_MAX_LEN;
    
}
