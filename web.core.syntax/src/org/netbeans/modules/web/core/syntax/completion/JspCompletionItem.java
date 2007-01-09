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

package org.netbeans.modules.web.core.syntax.completion;


import java.awt.Graphics;
import java.io.IOException;
import javax.swing.text.*;
import java.awt.Color;
import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.jsp.tagext.*;

import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.syntax.spi.AutoTagImporterProvider;
import org.openide.util.NbBundle;
import org.netbeans.modules.web.core.syntax.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;



/**
 *
 * @author  Petr Jiricka, Petr Nejedly, Marek Fukala
 */

/* ------------------------------------------------------------------------ */
/** An interface used as a generic value returned in CompletionQuery.Result
 * as an data item
 */
public class JspCompletionItem {
    
    private static final int DIRECTIVE_SORT_PRIORITY = 5;
    private static final int DEFAULT_SORT_PRIORITY = 10;
    
    public static abstract class JspResultItem extends org.netbeans.modules.web.core.syntax.completion.ResultItem {
        /** Contains a help for the item. It can be a url for a file.
         */
        protected  String help;
        protected String text;
        private static ResultItemPaintComponent component = null;
        
        public JspResultItem( String text ) {
            this.text = text;
            help = null;
        }
        
        public JspResultItem( String text, String help ) {
            this( text );
            this.help = help;
        }
        
        public String getItemText() {
            return text;
        }
        
        protected Object getAssociatedObject() {
            return text;
        }
        
        public int getSortPriority() {
            return DEFAULT_SORT_PRIORITY;
        }
        
        public CharSequence getInsertPrefix() {
            return getItemText();
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (component == null) {
                component = new ResultItemPaintComponent.StringPaintComponent();
            }
            component.setSelected(isSelected);
            component.setString(text);
            return component;
        }
        
        /** Returns a url or null, if the help is not URL or the help is not defined.
         */
        public URL getHelpURL(){
            if (help == null || help.equals(""))
                return null;
            try{
                return new URL(help);
            } catch (java.io.IOException e){
            }
            return null;
        }
        
        /** Returns help for the item. It can be only url. If the item doesn't have a help
         *  than returns null. The class can overwrite this method and compounds the help realtime.
         */
        public String getHelp(){
            return help;
        }
        
        /** Returns whether the item has a help.
         */
        public boolean hasHelp(){
            return (help != null && help.length() > 0);
        }
        
        public void setHelp(String help){
            this.help = help;
        }
        
        protected boolean substituteText( JTextComponent c, int offset, int len, String fill, int moveBack) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            try {
                doc.atomicLock();
                try {
                    //test whether we are trying to insert sg. what is already present in the text
                    String currentText = doc.getText(offset, (doc.getLength() - offset) < fill.length() ? (doc.getLength() - offset) : fill.length()) ;
                    if(!fill.substring(0, fill.length() - 1).equals(currentText)) {
                        //remove common part
                        doc.remove( offset, len );
                        doc.insertString( offset, fill, null);
                    } else {
                        c.setCaretPosition(c.getCaret().getDot() + fill.length() - len);
                    }
                    
                    //format the inserted text
                    ExtFormatter f = (ExtFormatter)doc.getFormatter();
                    int[] fmtBlk = f.getReformatBlock(c, fill);
                    if (fmtBlk != null) {
                        fmtBlk[0] = Utilities.getRowStart(doc, fmtBlk[0]);
                        fmtBlk[1] = Utilities.getRowEnd(doc, fmtBlk[1]);
                        f.reformat(doc, fmtBlk[0], fmtBlk[1], true);
                    }
                } finally {
                    doc.atomicUnlock();
                }
                if (moveBack != 0) {
                    Caret caret = c.getCaret();
                    int dot = caret.getDot();
                    caret.setDot(dot - moveBack);
                }
            } catch( BadLocationException exc ) {
                return false;    //not sucessfull
            } catch( IOException exc ) {
                return false;    //not sucessfull
            }
            return true;
        }
        
        public String getPaintText() {
            return getItemText();
        }
        
    }
    
    /** Item representing a JSP tag including its prefix. */
    public static class PrefixTag extends JspResultItem {
        
        private TagInfo tagInfo;
        private boolean isEmpty = false;
        
        private boolean hasAttributes = false;
        
        private static ResultItemPaintComponent.JspTagPaintComponent component = null;
        private static ResultItemPaintComponent.JspTagPaintComponent componentEmpty = null;
        
        PrefixTag( String text ) {
            super(text);
        }
        
        PrefixTag(String prefix, TagInfo ti, SyntaxElement.Tag set) {
            this(prefix + ":" + ti.getTagName()); // NOI18N
            tagInfo = ti;
            if ((tagInfo != null) &&
                    (tagInfo.getBodyContent().equalsIgnoreCase(TagInfo.BODY_CONTENT_EMPTY)))
                isEmpty = true;
            if (tagInfo != null)
                setHelp(tagInfo.getInfoString());
            
            //test whether this tag has some attributes
            if (set != null) hasAttributes = !(set.getAttributes().size() == 0);
        }
        
        PrefixTag(String prefix, TagInfo ti) {
            this(prefix, ti, (SyntaxElement.Tag)null);
        }
        
        public boolean hasHelp(){
            return true;
        }
        
        public TagInfo getTagInfo() {
            return tagInfo;
        }
        
        public String getHelp(){
            URL url = super.getHelpURL();
            if (url != null){
                String surl = url.toString();
                int first = surl.indexOf('#') + 1;
                String help = constructHelp(url);
                if (first > 0){
                    int last = surl.lastIndexOf('#') + 1;
                    String from = surl.substring( first , last - 1 );
                    String to = surl.substring(last);
                    first = help.indexOf(from);
                    if (first > 0){
                        first = first + from.length() + 2;
                        if (first < help.length())
                            help = help.substring(first);
                    }
                    last = help.indexOf(to);
                    if (last > 0)
                        help = help.substring(0, last);
                    return help;
                }
                
                help = help.substring(help.indexOf("<h2>")); //NOI18N
                help = help.substring(0, help.lastIndexOf("<h4>"));//NOI18N
                return help;
            }
            return constructHelp(tagInfo);
        }
        
        public Component getPaintComponent(boolean isSelected) {
            ResultItemPaintComponent comp = (isEmpty ? componentEmpty : component);
            
            if (comp == null) {
                comp = new ResultItemPaintComponent.JspTagPaintComponent(isEmpty);
            }
            comp.setSelected(isSelected);
            comp.setString(text);
            return comp;
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
            String suffix = isEmpty? "/>": ">"; // NOI18N
            
            if(hasAttributes) suffix = "";
            
            if (!getItemText().startsWith("/")) {  // NOI18N
                if (!shift)
                    return substituteText(c, offset, len, getItemText(), 0);  // NOI18N
                
                boolean hasAttrs = true;
                if (tagInfo != null) {
                    TagAttributeInfo[] tAttrs = tagInfo.getAttributes();
                    hasAttrs = (tAttrs != null)? (tAttrs.length > 0): true;
                }
                if (hasAttrs)
                    return substituteText(c, offset, len, getItemText() + ( hasAttributes ? "" : " ") + suffix , suffix.length());  // NOI18N
                else
                    return substituteText(c, offset, len, getItemText() + suffix, 0);
            } else
                // closing tag
                return substituteText(c, offset, len, getItemText().substring(1) + ">", 0);  // NOI18N
        }
        
        protected boolean substituteText( JTextComponent c, int offset, int len, String fill, int moveBack) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            boolean value = false;
            try {
                doc.atomicLock();
                value = super.substituteText(c, offset, len, fill, moveBack);
                FileObject f = Repository.getDefault().getDefaultFileSystem().
                        findResource("Editors/" + NbEditorUtilities.getFileObject(c.getDocument()).getMIMEType()+"/AutoTagImportProviders");
                if (f != null){
                    DataFolder folder = DataFolder.findFolder(f);
                    FolderLookup l = new FolderLookup(folder);
                    Lookup.Result result = l.getLookup().lookup(new Lookup.Template(AutoTagImporterProvider.class));
                    if (result != null){
                        for(Object instance : result.allInstances()){
                            ((AutoTagImporterProvider)instance).importLibrary(c.getDocument(),
                                    tagInfo.getTagLibrary().getPrefixString(), tagInfo.getTagLibrary().getURI());
                        }
                    }
                }
            } finally {
                doc.atomicUnlock();
            }
            return value;
        }
    }
    
    /** Item representing a JSP tag (without prefix) or JSP directive. */
    public abstract static class TagDirective extends  JspResultItem {
        
        protected static ResultItemPaintComponent component = null;
        
        TagDirective( String text ) {
            super(text);
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
            return substituteText(c, offset, len, getItemText() + " ", 0);  // NOI18N
        }
        
        public boolean instantSubstitutionEnabled(JTextComponent c) {
            return true;
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (component == null) {
                component = new ResultItemPaintComponent.JspTagPaintComponent(false);
            }
            component.setSelected(isSelected);
            component.setString(text);
            return component;
        }
        
        
    }
    
    /** Item representing a JSP tag (without prefix). */
    public static class Tag extends JspResultItem {
        
        protected static ResultItemPaintComponent.JspTagPaintComponent component = null;
        
        private TagInfo ti = null;
        
        public Tag( String text ) {
            super(text);
        }
        
        public Tag( String text, TagInfo ti) {
            this(text);
            this.ti = ti;
            if (ti != null)
                setHelp(ti.getInfoString());
        }
        
        public boolean hasHelp(){
            return true;
        }
        
        public TagInfo getTagInfo() {
            return ti;
        }
        
        public String getHelp(){
            URL url = super.getHelpURL();
            if (url != null){
                String surl = url.toString();
                int first = surl.indexOf('#') + 1;
                String help = constructHelp(url);
                if (first > 0){
                    int last = surl.lastIndexOf('#') + 1;
                    String from = surl.substring( first , last - 1 );
                    String to = surl.substring(last);
                    first = help.indexOf(from);
                    if (first > 0){
                        first = first + from.length() + 2;
                        if (first < help.length())
                            help = help.substring(first);
                    }
                    last = help.indexOf(to);
                    if (last > 0)
                        help = help.substring(0, last);
                    return help;
                }
                
                help = help.substring(help.indexOf("<h2>")); //NOI18N
                help = help.substring(0, help.lastIndexOf("<h4>"));//NOI18N
                return help;
            }
            return constructHelp(ti);
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (component == null) {
                component = new ResultItemPaintComponent.JspTagPaintComponent(false);
            }
            component.setSelected(isSelected);
            component.setString(text);
            return component;
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
            if (!getItemText().startsWith("/"))   // NOI18N
                return substituteText(c, offset, len, getItemText() + " ", 0);  // NOI18N
            else
                return substituteText(c, offset, len, getItemText().substring(1) + ">", 0);    // NOI18N
        }
        
        public boolean substituteCommonText( JTextComponent c, int offset, int len, int subLen ) {
            if (!getItemText().startsWith("/")) {  // NOI18N
                return substituteText(c, offset, len, getItemText().substring(subLen), 0);  // NOI18N
            } else {
                return substituteText(c, offset, len, getItemText().substring(1, subLen), 0);  // NOI18N
            }
        }
    }
    
    /** Item representing a JSP tag (without prefix). */
    static class Directive extends TagDirective {
        TagInfo tagInfo;
        
        Directive(String text){
            super(text);
            tagInfo = null;
        }
        
        Directive(String text, TagInfo tagInfo ) {
            super(text);
            this.tagInfo = tagInfo;
            if (tagInfo != null)
                setHelp(tagInfo.getInfoString());
        }
        
        public int getSortPriority() {
            return DIRECTIVE_SORT_PRIORITY;
        }
        
        public String getHelp(){
            if (getHelpURL() != null){
                String text = constructHelp(getHelpURL());
                if (text != null){
                    text = text.substring(text.indexOf("<h2>")); //NOI18N
                    text = text.substring(0, text.lastIndexOf("<h4>"));//NOI18N
                    return text;
                }
            }
            return constructHelp(tagInfo);
        }
        
        public TagInfo getTagInfo(){
            return tagInfo;
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
            return substituteText(c, offset, len, "%@ " + getItemText() + "  %>", 3);    // NOI18N
        }
        
        public boolean substituteCommonText( JTextComponent c, int offset, int len, int subLen ) {
            len = len - 2;
            offset = offset + 2;
            return super.substituteCommonText(c, offset, len, subLen);
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (component == null) {
                component = new ResultItemPaintComponent.JspDirectivePaintComponent();
            }
            component.setSelected(isSelected);
            component.setString(getItemText());
            return component;
        }
        
    }
    
    /** Item representing an attribute of a  JSP tag or directive. */
    public static class Attribute extends JspResultItem {
        private TagAttributeInfo tagAttributeInfo;
        private boolean required;
        
        private static ResultItemPaintComponent.JspTagPaintComponent component = null;
        private static ResultItemPaintComponent.JspTagPaintComponent componentRequired = null;
        
        Attribute( String text ) {
            super(text);
            tagAttributeInfo = null;
            required =  false;
        }
        
        Attribute(TagAttributeInfo tai) {
            super(tai.getName());
            required = tai.isRequired();
            tagAttributeInfo = tai;
            if (tai.getTypeName() == null && tai.isFragment())
                setHelp("fragment"); // NOI!18N
            else
                setHelp(tai.getTypeName());
        }
        
        public Component getPaintComponent(boolean isSelected) {
            ResultItemPaintComponent comp = (required ? componentRequired : component);
            
            if (comp == null) {
                comp = new ResultItemPaintComponent.AttributePaintComponent(required);
            }
            
            comp.setSelected(isSelected);
            comp.setString(getItemText());
            return comp;
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
            //always do the shift => jump into the attribute value between the quotation marks
            return substituteText(c, offset, len, getItemText() + "=\"\"", 1); // NOI18N
        }
        
        public String getHelp() {
            URL url = super.getHelpURL();
            if (url != null){
                String surl = url.toString();
                int first = surl.indexOf('#') + 1;
                int last = surl.lastIndexOf('#') + 1;
                String from;
                
                if (first < last){
                    from = surl.substring( first , last - 1 );
                } else {
                    from = surl.substring( first );
                }
                String text = constructHelp(getHelpURL());
                first = text.indexOf(from);
                if (first > 0){
                    first = first + from.length() + 2;
                    if (first < text.length())
                        text = text.substring(first);
                }
                
                String to = surl.substring(last);
                last = text.indexOf(to);
                if (last > 0)
                    text = text.substring(0, last);
                return text;
            }
            if (tagAttributeInfo != null){
                StringBuffer text = new StringBuffer();
                text.append("<table border=\"0\"><tr><td><b>Name:</b></td><td>");  //NOI18N
                text.append(tagAttributeInfo.getName());                            //NOI18N
                text.append("</td></tr><tr><td><b>Required:</b></td><td>");         //NOI18N
                text.append(tagAttributeInfo.isRequired());                         //NOI18N
                text.append("</td></tr><tr><td><b>Request-time:</b></td><td>");     //NOI18N
                text.append(tagAttributeInfo.canBeRequestTime());                   //NOI18N
                text.append("</td></tr><tr><td><b>Fragment:</b></td><td>");         //NOI18N
                text.append(tagAttributeInfo.isFragment());                         //NOI18N
                text.append("</td></tr></table>");                                  //NOI18N
                return text.toString();
            }
            return super.getHelp();
        }
        
        public URL getHelpURL(){
            URL url = super.getHelpURL();
            if (url != null){
                String surl = url.toString();
                int index = surl.lastIndexOf('#'); // NOI18N
                if (index > 0)
                    surl = surl.substring(0, index);
                try {
                    url =  new URL(surl);
                } catch (MalformedURLException e){
                }
            }
            return url;
        }
        
    }
    
    /** Item representing a JSP attribute value. */
    static class AttributeValue extends JspResultItem {
        
        AttributeValue( String text ) {
            //super(text, Color.red);
            super(text);
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
            return substituteText(c, offset, len, getItemText(), 0);
        }
    }
    
    /** Item representing a File attribute */
    public static class FileAttributeValue extends JspResultItem {
        private javax.swing.ImageIcon icon;
        private Color color;
        
        FileAttributeValue(String text, Color color){
            this(text, color, null);
        }
        
        FileAttributeValue(String text, Color color, javax.swing.ImageIcon icon){
            super(text);
            this.color = color;
            this.icon = icon;
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
            return substituteText(c, offset, len, getItemText(), 0);
        }
        
        public Component getPaintComponent(final boolean isSelected) {
            return new ResultItemPaintComponent() {
                public void draw(Graphics g) {
                    drawIcon(g, FileAttributeValue.this.icon);
                    drawString(g, text, FileAttributeValue.this.color);
                }
                
                public boolean isSelected() {
                    return isSelected;
                }
            };
        }
    }
    
    private static String constructHelp(URL url){
        if (url == null )
            return null;
        try{
            InputStream is = url.openStream();
            byte buffer[] = new byte[1000];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int count = 0;
            do {
                count = is.read(buffer);
                if (count > 0) baos.write(buffer, 0, count);
            } while (count > 0);
            
            is.close();
            String text = baos.toString();
            baos.close();
            return text;
        } catch (java.io.IOException e){
            return null;
        }
    }
    
    private static String constructHelp(TagInfo tagInfo){
        if (tagInfo == null) return null;
        
        StringBuffer sb = new StringBuffer();
        sb.append("<h2>").append(getString("LBL_TagName")).append(" "); //NOI18N
        sb.append(tagInfo.getTagName()).append("</h2>"); // NOI18N
        String val = tagInfo.getDisplayName();
        if (val != null) {
            sb.append("<p>").append(getString("LBL_DisplayName")); //NOI18N
            sb.append("<i>").append(val).append("</i>"); // NOI18N
        }
        val = tagInfo.getInfoString();
        if (val != null)
            sb.append("<hr>").append(val).append("<hr>");                 // NOI18N
        
        sb.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"1\">");// NOI18N
        sb.append("<tr bgcolor=\"#CCCCFF\"><td colspan=\"2\"><font size=\"+2\"><b>");// NOI18N
        sb.append("Tag Information</b></font></td></tr>");// NOI18N
        sb.append("<tr><td>Tag Class</td><td>");// NOI18N
        if (tagInfo.getTagClassName() != null && !tagInfo.getClass().equals("") )
            sb.append(tagInfo.getTagClassName());
        else
            sb.append("<i>None</i>");// NOI18N
        sb.append("</td></tr><tr><td>Body Content</td><td>");// NOI18N
        sb.append(tagInfo.getBodyContent());
        sb.append("</td></tr><tr><td>Display Name</td><td>");// NOI18N
        if (tagInfo.getDisplayName() != null && !tagInfo.getDisplayName().equals("")){
            sb.append(tagInfo.getDisplayName());
        } else
            sb.append("<i>None</i>");// NOI18N
        sb.append("</td></tr></table><br>");// NOI18N
        
        sb.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"1\">");// NOI18N
        sb.append("<tr bgcolor=\"#CCCCFF\"><td colspan=\"3\"><font size=\"+2\"><b>Attributes</b></font></td></tr>");// NOI18N
        
        TagAttributeInfo [] attrs = tagInfo.getAttributes();
        if (attrs != null && attrs.length > 0){
            sb.append("<tr><td><b>Name</b></td><td><b>Required</b></td><td><b>Request-time</b></td></tr>");// NOI18N
            for (int i = 0; i < attrs.length; i++){
                sb.append("<tr><td>");         // NOI18N
                sb.append(attrs[i].getName());
                sb.append("</td><td>");                     // NOI18N
                sb.append(attrs[i].isRequired());
                sb.append("</td><td>");                     // NOI18N
                sb.append(attrs[i].canBeRequestTime());
                sb.append("</td></tr>");                    // NOI18N
            }
        } else {
            sb.append("<tr><td colspan=\"3\"><i>No Attributes Defined.</i></td></tr>");// NOI18N
        }
        sb.append("</table><br>");// NOI18N
        sb.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"1\">");// NOI18N
        sb.append("<tr bgcolor=\"#CCCCFF\"><td colspan=\"4\"><font size=\"+2\"><b>Variables</b></font></td></tr>");// NOI18N
        TagVariableInfo [] variables = tagInfo.getTagVariableInfos();
        if (variables != null && variables.length > 0){
            sb.append("<tr><td><b>Name</b></td><td><b>Type</b></td><td><b>Declare</b></td><td><b>Scope</b></td></tr>");// NOI18N
            for (int i = 0; i < variables.length; i++){
                sb.append("<tr><td>");         // NOI18N
                if (variables[i].getNameGiven() != null && !variables[i].getNameGiven().equals("")){// NOI18N
                    sb.append(variables[i].getNameGiven());
                } else {
                    if(variables[i].getNameFromAttribute() != null && !variables[i].getNameFromAttribute().equals("")) // NOI18N
                        sb.append("<i>From attribute '").append(variables[i].getNameFromAttribute()).append("'</i>");// NOI18N
                    else
                        sb.append("<i>Unknown</i>");  // NOI18N
                }
                sb.append("</td><td><code>");                     // NOI18N
                if (variables[i].getClassName() == null || variables[i].getClassName().equals("") )
                    sb.append("java.lang.String");// NOI18N
                else
                    sb.append(variables[i].getClassName());
                sb.append("</code></td></tr>");                    // NOI18N
                sb.append("</td><td>");                     // NOI18N
                sb.append(variables[i].getDeclare());
                sb.append("</td></tr>");                    // NOI18N
                sb.append("</td><td>");                     // NOI18N
                switch (variables[i].getScope()){
                    case VariableInfo.AT_BEGIN:
                        sb.append("AT_BEGIN"); break;// NOI18N
                    case VariableInfo.AT_END:
                        sb.append("AT_END"); break;// NOI18N
                    default:
                        sb.append("NESTED");// NOI18N
                }
                sb.append("</td></tr>");                    // NOI18N
            }
        } else {
            sb.append("<tr><td colspan=\"4\"><i>No Variables Defined.</i></td></tr>");// NOI18N
        }
        sb.append("</table><br>");// NOI18N
        return sb.toString();
    }
    
    private static String getString(String key){
        return NbBundle.getMessage(JspCompletionItem.class, key);
    }
    

    // ------------------------ EL Items ------------------------------------------
    
    public interface ELItem{};
    
    public static class ELImplicitObject extends JspResultItem implements ELItem {
        
        private static ResultItemPaintComponent.ELImplicitObjectPaintComponent paintComponent = null;
        
        int type;
        
        ELImplicitObject(String text, int type){
            super(text);
            this.type = type;
        }
        
        public int getSortPriority() {
            return 15;
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null)
                paintComponent = new ResultItemPaintComponent.ELImplicitObjectPaintComponent();
            paintComponent.setString(text);
            paintComponent.setType(type);
            return paintComponent;
        }
        
        public String getItemText() {
            String result = text;
            if (type == org.netbeans.modules.web.core.syntax.completion.ELImplicitObjects.MAP_TYPE)
                result = result + "[]";
            return result;    //NOI18N
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
            if (type == org.netbeans.modules.web.core.syntax.completion.ELImplicitObjects.MAP_TYPE)
                return substituteText(c, offset, len, getItemText(), 1);
            else
                return substituteText(c, offset, len, getItemText(), 0);
        }
    }
    
    
    public static class ELBean extends JspResultItem implements ELItem {
        
        private static ResultItemPaintComponent.ELBeanPaintComponent paintComponent = null;
        
        protected String type;
        
        public ELBean( String text, String type ) {
            super(text);
            if (type.lastIndexOf('.')> -1 )
                this.type = type.substring(type.lastIndexOf('.')+1);
            else
                this.type = type;
        }
        
        public int getSortPriority() {
            return 10;
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null)
                paintComponent = new ResultItemPaintComponent.ELBeanPaintComponent();
            paintComponent.setString(text);
            paintComponent.setTypeName(type);
            return paintComponent;
        }
    }
    
    public static class ELProperty extends ELBean implements ELItem {
        
        private static ResultItemPaintComponent.ELPropertyPaintComponent paintComponent = null;
        
        public ELProperty( String text, String type ) {
            super(text, type);
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null)
                paintComponent = new ResultItemPaintComponent.ELPropertyPaintComponent();
            paintComponent.setString(text);
            paintComponent.setTypeName(type);
            return paintComponent;
        }
    }
    
    public static class ELFunction extends ELBean implements ELItem {
        
        private static ResultItemPaintComponent.ELFunctionPaintComponent paintComponent = null;
        
        private String prefix;
        private String parameters;
        
        
        public ELFunction( String prefix, String name, String type, String parameters) {
            super(name, type);
            this.prefix = prefix;
            this.parameters = parameters;
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null)
                paintComponent = new ResultItemPaintComponent.ELFunctionPaintComponent();
            paintComponent.setString(text);
            paintComponent.setTypeName(type);
            paintComponent.setPrefix(prefix);
            paintComponent.setParameters(parameters);
            return paintComponent;
        }
        
        public int getSortPriority() {
            return 12;
        }
        
        public String getItemText() {
            return prefix+":"+text+"()";    //NOI18N
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
            return substituteText(c, offset, len, getItemText(), 1);
        }
    }
    
}
