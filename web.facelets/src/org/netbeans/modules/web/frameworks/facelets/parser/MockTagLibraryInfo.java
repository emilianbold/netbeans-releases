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

package org.netbeans.modules.web.frameworks.facelets.parser;

import com.sun.facelets.tag.AbstractTagLibrary;
import com.sun.facelets.tag.TagLibrary;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

/**
 *
 * @author Petr Pisl
 */
public class MockTagLibraryInfo extends TagLibraryInfo{
    
    private List <TagLibrary> libs;
    private static Field libNamespaceF = null;
    private static Field libFactoriesF = null;
    private static Field libFunctionsF = null;
    
    
    /** Creates a new instance of MockTagLibraryInfo */
    public MockTagLibraryInfo(AbstractTagLibrary lib, String prefix, String uri, TLDParser.Result tld) {
        super(prefix, uri);
        createComponents(lib, tld);
    }
    
    public TagLibraryInfo[] getTagLibraryInfos() {
        return null;
    }
    
    public static Hashtable <String, String> prefixes = null;
    private static int userLibCount = 0;
    
    public static String getPrefix(AbstractTagLibrary lib, TLDParser.Result tld){
        if (prefixes == null){
            prefixes = new Hashtable();
            prefixes.put(Parser.UI_URI, "ui");
            prefixes.put(Parser.JSFH_URI, "h");
            prefixes.put(Parser.JSFC_URI, "f");
            prefixes.put(Parser.JSTLC_URI, "c");
            prefixes.put(Parser.JSTLF_URI, "fn");
        }
        String prefix = prefixes.get(getURI(lib));
        if (prefix == null){
            String uri = getURI(lib);
            if (tld == null) {
                userLibCount++;
                prefix = "u" + userLibCount;
            }
            else
                prefix = tld.getPrefix();
            prefixes.put(getURI(lib), prefix);
        }
        return prefix;
    }
    
    public static String getURI(AbstractTagLibrary lib){
        String uri = null;
        if (libNamespaceF == null){
            try {
                libNamespaceF = AbstractTagLibrary.class.getDeclaredField("namespace");
                libNamespaceF.setAccessible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            uri = (String)libNamespaceF.get(lib);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return uri;
    }
    
    public String toString(){
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        //print("tlibversion", tlibversion, out);
        //print("jspversion", jspversion, out);
        print("shortname", shortname, out);
        print("urn", urn, out);
        print("info", info, out);
        print("uri", uri, out);
        //print("tagLibraryValidator", tagLibraryValidator.toString(), out);
        
        for(int i = 0; i < tags.length; i++)
            out.println(tags[i].getTagName());
        
        //for(int i = 0; i < tagFiles.length; i++)
        //    out.println(tagFiles[i].toString());
        
        //for(int i = 0; i < functions.length; i++)
        //    out.println(functions[i].toString());
        
        return sw.toString();
    }
    
    private final void print(String name, String value, PrintWriter w) {
        if (value != null) {
            w.print(name+" = {\n\t");
            w.print(value);
            w.print("\n}\n");
        }
    }
    
    private void createComponents(AbstractTagLibrary lib, TLDParser.Result tld){
        if (libFactoriesF == null){
            try {
                libFactoriesF = AbstractTagLibrary.class.getDeclaredField("factories");
                libFactoriesF.setAccessible(true);
                libFunctionsF = AbstractTagLibrary.class.getDeclaredField("functions");
                libFunctionsF.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                ex.printStackTrace();
            }
        }
        Map <String, Object> factories = null;
        Map functions = null;
        try {
            factories = (Map)libFactoriesF.get(lib);
            functions = (Map)libFunctionsF.get(lib);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        if (factories != null){
            ArrayList <TagInfo> tagList = new ArrayList(factories.size());
            
            if (tld != null) {
                TagInfo tag;
                for (String name : factories.keySet()) {
                    tag = tld.getTagInfos().get(name);
                    if (tag != null)
                        //tagList.add(tld.getTagInfos().get(name));
                        tagList.add(new TagInfo(name, tag.getTagClassName(), tag.getBodyContent(), tag.getInfoString(),
                                this, tag.getTagExtraInfo(), tag.getAttributes(), tag.getDisplayName(), tag.getSmallIcon(), tag.getLargeIcon(),
                                tag.getTagVariableInfos()));
                    else
                        tagList.add(new TagInfo(name, "", "",  "", this, null, new TagAttributeInfo[0]));
                }
            }
            else{
                
                for (String name : factories.keySet()) {
                    tagList.add(new TagInfo(name, "", "",  "", this, null, new TagAttributeInfo[0]));
                }
            }
            this.tags = new TagInfo[tagList.size()];
            tagList.toArray(this.tags);
        }
        
    }
    
    
    
}
