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

package org.netbeans.editor.ext.html.javadoc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Petr Pisl
 */
public class HelpManager {
    
    private static HelpManager manager = null;
    
    private Hashtable helpMap;
    private long lastChange;
    private String helpZipURL;
    private URL lastURL;
    
    /**  HelpManager a new instance of HelpManager */
    private HelpManager() {
        helpMap = null;
        lastChange = 0;
        helpZipURL = null;
        lastURL = null;
    }
    
    static public HelpManager getDefault(){
        if (manager == null){
            manager = new HelpManager();
        }
        return manager;
    }
    
    private void init(){
        if (helpMap != null)
            return;
        // This part of the code is for the easy way how to define config file.
        String help = "";
        try{
            //File file = InstalledFileLocator.getDefault().locate("docs/HtmlHelp.xml", null, false);
            /*File file = new File ("/space/cvs/trunk/HtmlHelp.xml");
            if (file != null && lastChange != file.lastModified()){
                System.out.println("Config file was changed");
                helpMap = null;
                lastChange = file.lastModified();
            }*/
            if (helpMap == null){    
                //Parse the config file
                InputStream in = this.getClass().getClassLoader()
                    .getResourceAsStream("org/netbeans/editor/ext/html/javadoc/resources/HtmlHelp.xml"); //NOI18N
                if (in == null){
                    helpMap = new Hashtable();
                    return;
                }
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();

                SAXHelpHandler handler = new SAXHelpHandler();    
                java.util.Date start = new java.util.Date();
                parser.parse(in, handler);
                in.close();
                
                //parser.parse(file, handler);

                //System.out.println("Parsing config file takes " + (end.getTime() - start.getTime()));
                help = handler.getHelpFile();
                if (help == null || help.equals("")){
                    help = null; 
                    helpMap = new Hashtable();
                    return;
                }
                
                helpMap = handler.getMap();
                
                String url="";
                
                File f = InstalledFileLocator.getDefault().locate(help, null, false); //NoI18N
                if (f != null){
                    try {
                        URL urll = f.toURL();
                        urll = FileUtil.getArchiveRoot(urll);
                        helpZipURL = urll.toString();
                    }
                    catch (java.net.MalformedURLException e){
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                        helpMap = new Hashtable();
                        return;
                    }
                }
            }
        } 
        catch (Exception e){
            ErrorManager.getDefault().log(e.toString());
        }
        
            
        
    }
    
    public URL getRelativeHelpToLast(String link){
        String url = lastURL.toString();
        
        int index;
        
        if (link.trim().charAt(0) == '#'){
            index = url.indexOf('#');
            if (index > -1)
                url = url.substring(0,url.indexOf('#'));
            url = url + link;
        }
        else {
            index = 0;
            url = url.substring(0, url.lastIndexOf('/'));
            while ((index = link.indexOf("../", index)) > -1){      //NOI18N
                url = url.substring(0, url.lastIndexOf('/'));
                link = link.substring(index+3);
            }
            url = url + "/" + link; // NOI18N
        }
        URL newURL = null;
        try{
            newURL = new URL (url);
        }
        catch (java.net.MalformedURLException e){
            ErrorManager.getDefault().log(e.toString());
            return null;
        }
        return newURL;
    }
    
    public String getHelp(String key){
        if (key == null) 
            return null;
        TagHelpItem helpItem = findHelpItem(key);
        URL url = getHelpURL(helpItem);
        if (url == null)
            return null;
        
        //System.out.println(key + " -> url: " + url);
        lastURL = url;
        String help = getHelpText(url);
        int offset = 0;
        //String head = null;
        if (help != null){
            //head = getHead(help);
            if (helpItem.getStartText() != null){
                offset = help.indexOf(helpItem.getStartText());
                if (offset > 0){
                    offset = offset + helpItem.getStartTextOffset();
                    help = help.substring(offset);
                }
            }
            if (helpItem.getEndText() != null){
                offset = help.indexOf(helpItem.getEndText());
                if (offset > 0 ) {
                    offset = offset + helpItem.getEndTextOffset();
                    help = help.substring(0, offset);
                }
            }
        }
        else {
            help = "";
        }
        if (helpItem.getTextBefore() != null)
            help = helpItem.getTextBefore() + help;
        if (helpItem.getTextAfter() != null)
            help = help + helpItem.getTextAfter();
        //if (help.length() > 0){
        //    help = head + help + "</body></html>";
        //}
        return help;
    }
    
    /*private String getHead(String help){
        String head = null;
        int index = help.indexOf ("</head>");
        if (index > 0){
            head = help.substring(0, index);
            head = head + "</head><body>";
        }
        return head;
    }*/
    private String getHelpText (URL url){
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
        }
        catch (java.io.IOException e){
            return null;
        }            
    }
    
    public URL getHelpURL(String key){
        return getHelpURL(findHelpItem(key));
    }
    
    private URL getHelpURL(TagHelpItem helpItem){
        URL url = null;
        
        if(helpItem != null){
            String surl = helpZipURL + helpItem.getFile();
            try{
                url = new URL (surl);
            }
            catch (java.net.MalformedURLException e){
                ErrorManager.getDefault().log(e.toString());
                return null;
            }
        }
        
        return url;
    }
    
    private TagHelpItem findHelpItem(String key){
        if (key == null) return null;
        init();
        Object o = helpMap.get(key.toUpperCase());
        if (o != null){
            TagHelpItem helpItem = (TagHelpItem)o;
        
            if (helpItem != null)
                while (helpItem != null && helpItem.getIdentical() != null){
                    helpItem = (TagHelpItem)helpMap.get(helpItem.getIdentical().toUpperCase());
                }

            return helpItem;
        }
        return null;
    }
}
