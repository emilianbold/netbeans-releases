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

import java.util.*;
import java.beans.*;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import javax.swing.ImageIcon;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.netbeans.modules.web.core.syntax.*;
import org.netbeans.modules.web.core.syntax.completion.JavaJSPCompletionProvider.CompletionQueryDelegatedToJava;
import org.netbeans.modules.web.core.syntax.completion.JspCompletionQuery.JspCompletionResult;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;


/** Support for code completion of default JSP tags.
 *
 * @author  pjiricka
 */
public class AttrSupports extends Object {
    private static final Logger logger = Logger.getLogger(AttrSupports.class.getName());
    public static class ScopeSupport extends AttributeValueSupport.Default {
        
        public ScopeSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("application");    // NOI18N
            list.add("page");           // NOI18N
            list.add("request");        // NOI18N
            list.add("session");        // NOI18N
            return list;
        }
        
    }
    
    public static class RootVersionSupport extends AttributeValueSupport.Default {
        
        public RootVersionSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("1.2");    // NOI18N
            list.add("2.0");           // NOI18N
            return list;
        }
        
    }
    
    public static class PluginTypeSupport extends AttributeValueSupport.Default {
        
        public PluginTypeSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("bean");    // NOI18N
            list.add("applet");           // NOI18N
            return list;
        }
        
    }
    
    public static class VariableScopeSupport extends AttributeValueSupport.Default {
        
        public VariableScopeSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("AT_BEGIN");    // NOI18N
            list.add("AT_END");           // NOI18N
            list.add("NESTED");        // NOI18N
            return list;
        }
        
    }
    
    public static class YesNoTrueFalseSupport extends AttributeValueSupport.Default {
        
        public YesNoTrueFalseSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("false");    // NOI18N
            list.add("no");           // NOI18N
            list.add("true");        // NOI18N
            list.add("yes");        // NOI18N
            return list;
        }
        
    }
    
    /**
     * Provides code completion for a class name context
     */
    public static class ClassNameSupport extends AttributeValueSupport.Default {
        
        public ClassNameSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            return new ArrayList();
        }
        
        protected String getFakedClassBody(String prefix){
            return "class Foo extends " + prefix; //NOI18N
        }
        
        /** Returns the complete result that contains elements from getCompletionItems.   */
        public CompletionQuery.Result getResult(JTextComponent component, int offset,
                JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            
            String fakedClassBody = getFakedClassBody(valuePart);
            int shiftedOffset = fakedClassBody.length();
            
            logger.fine("JSP CC: delegating CC query to java file:\n" //NOI18N
                            + fakedClassBody.substring(0, shiftedOffset)
                            + "|" + fakedClassBody.substring(shiftedOffset) + "\n"); //NOI18N
            
            CompletionQueryDelegatedToJava delegate = new CompletionQueryDelegatedToJava(
                    offset, shiftedOffset, CompletionProvider.COMPLETION_QUERY_TYPE);
            
            delegate.create(component.getDocument(), fakedClassBody);
            List<? extends CompletionItem> items =  delegate.getCompletionItems();
            
            JspCompletionResult result = new JspCompletionResult(component, null, items,
                    offset + (valuePart.lastIndexOf('.') + 1), valuePart.length(), -1);
            
            return result;
        }
        
        /** Returns generated List of items for completion.
         *  It sets itemLength and itemOffset variables as a side effect
         */
        private List completionResults(int offset, JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            return null;
        }
        
    }
    
    /**
     * Provides code completion for a comma-separated list of imports context
     */
    public static class PackageListSupport extends ClassNameSupport {
        
        public PackageListSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        @Override protected String getFakedClassBody(String prefix){
            int commaPos = prefix.lastIndexOf(",");
            
            if (commaPos > -1){
                prefix = prefix.substring(commaPos + 1);
            }
            
            return "import " + prefix; //NOI18N
        }
    }
    
    public static class GetSetPropertyName extends AttributeValueSupport.Default {
        
        public GetSetPropertyName(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            PageInfo.BeanData[] beanData = sup.getBeanData();
            if(beanData != null) {
                for (int i = 0; i < beanData.length; i++) {
                    list.add(beanData[i].getId());
                }
            }
            return list;
        }
        
    }
    
    
    public static abstract class GetSetPropertyProperty extends AttributeValueSupport.Default {
        
        public GetSetPropertyProperty(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item, boolean setter) {
            ArrayList list = new ArrayList();
            String namePropertyValue = (String)item.getAttributes().get("name");    // NOI18N
            if (namePropertyValue != null) {
                String className = null;
                PageInfo.BeanData[] beanData = sup.getBeanData();
                for (int i = 0; i < beanData.length; i++) {
                    if (beanData[i] == null || beanData[i].getId() == null)
                        continue;
                    
                    if (beanData[i].getId().equals(namePropertyValue)) {
                        className = beanData[i].getClassName();
                        break;
                    }
                }
                
                if (className != null) {
                    try {
                        FileObject fileObject = NbEditorUtilities.getDataObject( sup.getDocument()).getPrimaryFile();
                        ClassLoader cld = JspUtils.getModuleClassLoader( sup.getDocument(), fileObject);
                        Class beanClass = Class.forName(className, false, cld);
                        Introspector.flushFromCaches(beanClass);
                        BeanInfo benInfo = Introspector.getBeanInfo(beanClass);
                        PropertyDescriptor[] properties = benInfo.getPropertyDescriptors();
                        for (int j = 0; j < properties.length; j++) {
                            if (setter && (properties[j].getWriteMethod() != null))
                                list.add(properties[j].getName());
                            if (!setter && (properties[j].getReadMethod() != null) && !properties[j].getName().equals("class")) //NOI18N
                                list.add(properties[j].getName());
                        }
                    } catch (ClassNotFoundException e) {
                        //do nothing
                    } catch (IntrospectionException e) {
                        //do nothing
                    }
                }
            }
            return list;
        }
    }
    
    public static class GetPropertyProperty extends GetSetPropertyProperty {
        
        public GetPropertyProperty() {
            super(true, "jsp:getProperty", "property");     // NOI18N
        }
        
        public List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            return possibleValues(sup, item, false);
        }
        
    }
    
    public static class SetPropertyProperty extends GetSetPropertyProperty {
        
        public SetPropertyProperty() {
            super(true, "jsp:setProperty", "property"); // NOI18N
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            List list = possibleValues(sup, item, true);
            list.add(0, "*");  // NOI18N
            return list;
        }
        
    }
    
    public static class TaglibURI extends AttributeValueSupport.Default {
        
        public TaglibURI() {
            super(false, "taglib", "uri");      // NOI18N
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            List list = new ArrayList();
            Map map = sup.getTagLibraryMappings();
            if (map != null) {
                Iterator iterator = map.keySet().iterator();
                while(iterator.hasNext()) {
                    String s = (String)iterator.next();
                    list.add(s);
                }
            }
            // sort alphabetically
            Collections.sort(list);
            return list;
        }
        
    }
    
    public static class TaglibTagdir extends AttributeValueSupport.Default {
        
        public TaglibTagdir() {
            super(false, "taglib", "tagdir");      // NOI18N
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            List l = new ArrayList();
            FileObject orig = sup.getFileObject();
            FileObject documentBase = JspUtils.guessWebModuleRoot(sup.getDocument(), orig);
            if (documentBase != null) {
                FileObject webInfTags = JspUtils.findRelativeFileObject(documentBase, "WEB-INF/tags");
                if (webInfTags != null) {
                    // WEB-INF/tags itself
                    if (isValidTagDir(webInfTags)) {
                        l.add(JspUtils.findRelativeContextPath(documentBase, webInfTags));
                    }
                    // subfolders of WEB-INF/tags
                    Enumeration en = webInfTags.getFolders(true);
                    while (en.hasMoreElements()) {
                        FileObject subF = (FileObject)en.nextElement();
                        if (isValidTagDir(subF)) {
                            l.add(JspUtils.findRelativeContextPath(documentBase, subF));
                        }
                    }
                }
            }
            // sort alphabetically
            Collections.sort(l);
            return l;
        }
        
        private boolean isValidTagDir(FileObject subF) {
            // must contain at least one file
            return subF.getChildren(false).hasMoreElements();
        }
        
    }
    
    
    /** Support for code completing of package and class. */
    public static class FilenameSupport extends AttributeValueSupport.Default {
        static final ImageIcon PACKAGE_ICON =
                new ImageIcon(org.openide.util.Utilities.loadImage("org/openide/loaders/defaultFolder.gif")); // NOI18N
        
        /** Index where to start substitution */
        private int itemOffset;
        /** Length of currently substituted text */
        private int itemLength;
        
        public FilenameSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            return new ArrayList();
        }
        
        /** Returns the complete result that contains elements from getCompletionItems.   */
        public CompletionQuery.Result getResult(JTextComponent component, int offset,
                JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            List res = completionResults(offset, sup, item, valuePart);
            return new JspCompletionQuery.JspCompletionResult(component,
                    completionTitle(), res,
                    itemOffset, itemLength, -1);
        }
        
        /** Returns generated List of items for completion.
         *  It sets itemLength and itemOffset variables as a side effect
         */
        private List completionResults(int offset, JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            List res = new ArrayList();
            String path = "";   // NOI18N
            String fileNamePart = valuePart;
            int lastSlash = valuePart.lastIndexOf('/');
            if (lastSlash == 0) {
                path = "/"; // NOI18N
                fileNamePart = valuePart.substring(1);
            } else if (lastSlash > 0) { // not a leading slash?
                path = valuePart.substring(0, lastSlash);
                fileNamePart = (lastSlash == valuePart.length())? "": valuePart.substring(lastSlash+1);    // NOI18N
            }
            
            try {
                FileObject orig = sup.getFileObject();
                FileObject documentBase = JspUtils.guessWebModuleRoot(sup.getDocument(), orig);
                // need to normalize fileNamePart with respect to orig
                String ctxPath = JspUtils.resolveRelativeURL("/"+orig.getPath(), path);  // NOI18N
                //is this absolute path?
                if (path.startsWith("/"))
                    ctxPath = documentBase.getPath() + path;
                else
                    ctxPath = ctxPath.substring(1);
                
                
                FileSystem fs = orig.getFileSystem();
                
                FileObject folder = fs.findResource(ctxPath);
                if (folder != null) {
                    res = files(folder, fileNamePart, sup);
                    if (!folder.equals(documentBase) && !path.startsWith("/") // NOI18N
                            && (path.length() == 0 || (path.lastIndexOf("../")+3 == path.length()))){ // NOI18N
                        res.add(0,  new JspCompletionItem.FileAttributeValue("../", java.awt.Color.BLUE, PACKAGE_ICON)); // NOI18N
                    }
                }
            } catch (FileStateInvalidException ex) {
                // unreachable FS - disable completion
            } catch (IllegalArgumentException ex) {
                // resolving failed
            }
            itemOffset = offset - valuePart.length() + lastSlash + 1;  // works even with -1
            itemLength = fileNamePart.length();
            
            
            //set substitute offset
            Iterator i = res.iterator();
            while(i.hasNext()) {
                JspCompletionItem.JspResultItem resultItem = (JspCompletionItem.JspResultItem)i.next();
                resultItem.setSubstituteOffset(itemOffset);
            }
            
            return res;
        }
        
        private List files(FileObject folder, String prefix, JspSyntaxSupport sup) {
            ArrayList res = new ArrayList();
            TreeMap resFolders = new TreeMap();
            TreeMap resFiles = new TreeMap();
            
            Enumeration files = folder.getChildren(false);
            while (files.hasMoreElements()) {
                FileObject file = (FileObject)files.nextElement();
                String fname = file.getNameExt();
                if (fname.startsWith(prefix) && !"cvs".equalsIgnoreCase(fname)) {
                    
                    if (file.isFolder())
                        resFolders.put(file.getNameExt(), new JspCompletionItem.FileAttributeValue(file.getNameExt() + "/", java.awt.Color.BLUE, PACKAGE_ICON));
                    else{
                        java.awt.Image icon = JspUtils.getIcon(sup.getDocument(), file);
                        if (icon != null)
                            resFiles.put(file.getNameExt(), new JspCompletionItem.FileAttributeValue(file.getNameExt(), java.awt.Color.BLACK, new javax.swing.ImageIcon(icon)));
                        else
                            resFiles.put(file.getNameExt(), new JspCompletionItem.FileAttributeValue(file.getNameExt(), java.awt.Color.BLACK));
                    }
                }
            }
            res.addAll(resFolders.values());
            res.addAll(resFiles.values());
            
            return res;
        }
        
    }
    
    public static class TrueFalseSupport extends AttributeValueSupport.Default {
        
        public TrueFalseSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("false");   // NOI18N
            list.add("true");    // NOI18N
            return list;
        }
        
    }
    
    public static class PageLanguage extends AttributeValueSupport.Default {
        
        public PageLanguage() {
            super(false, "page", "language");    // NOI18N
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            list.add("java");    // NOI18N
            return list;
        }
        
    }
    
    public static class EncodingSupport extends AttributeValueSupport.Default {
        
        public EncodingSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList list = new ArrayList();
            Iterator iter = java.nio.charset.Charset.availableCharsets().keySet().iterator();
            
            while (iter.hasNext())
                list.add(iter.next());
            
            return list;
        }
        
    }
    
}
