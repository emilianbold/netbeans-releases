/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.loaders.XMLDataObject;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;


/** MIME Option XML file.
 *
 * @author  Martin Roskanin
 * @since 08/2001
 */
public abstract class MIMEOptionFile{
    
    protected BaseOptions base;
    protected MIMEProcessor processor;
    protected Document dom;
    protected Map properties;
    private boolean loaded = false;
    
    /** File change listener.
     * If file was externally modified, we have to reload settings.*/
    private final FileChangeListener fileListener = new FileChangeAdapter() {
        public void fileChanged(FileEvent fe){
            reloadSettings();
        }
    };
    
    /* Singleton of error catcher */
    private final ErrorCatcher ERROR_CATCHER = new ErrorCatcher();
    
    /** Creates new MIMEOptionFile */
    public MIMEOptionFile(BaseOptions base, Object proc) {
        this.base = base;
        processor = (MIMEProcessor)proc;
        try {
            properties = new HashMap();
            dom = processor.getXMLDataObject().getDocument();
            processor.getXMLDataObject().getPrimaryFile().addFileChangeListener(fileListener);
        } catch (org.xml.sax.SAXException e) {
            e.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    /** Getter for loaded flag.
     *  @return true if XML file has been already loaded */
    public boolean isLoaded(){
        return loaded;
    }
    
    /** Sets loaded flag.
     *  @param load if true file has been already loaded
     */
    protected void setLoaded(boolean load){
        loaded = load;
    }
    
    /** Updates all properties from (external modified) XML file */
    public void reloadSettings(){
        Document oldDoc = dom;
        setLoaded(false);
        try {
            String loc = processor.getXMLDataObject().getPrimaryFile().getURL().toExternalForm();
            // we need to reparse modified xml file, XMLDataObject caches old data ...
            dom = XMLUtil.parse(new InputSource(loc), true, false, ERROR_CATCHER, org.openide.xml.EntityCatalog.getDefault());
            loadSettings();
        }catch(SAXException saxe){
            dom = oldDoc;
            saxe.printStackTrace();
        }catch(IOException ioe){
            dom = oldDoc;
            ioe.printStackTrace();
        }
    }
    
    /** Loads settings stored in XML file
     *  @param propagate if true loaded settings take effect in editor */
    protected abstract void loadSettings(boolean propagate);
    
    /** Loads settings stored in XML file and propagate them */
    protected void loadSettings(){
        loadSettings(true);
    }
    
    /** Updates the settings in property map and save them in XML file */
    protected abstract void updateSettings(Map map);
    
    /** Gets all properties stored in user XML setting file
     *  @return Map of properties */
    public Map getAllProperties(){
        if (!isLoaded()) loadSettings(false);
        return properties;
    }
    
    
    /** Inner class error catcher for handling SAXParseExceptions */
    class ErrorCatcher implements org.xml.sax.ErrorHandler {
        private void message(String level, org.xml.sax.SAXParseException e) {
            System.err.println("Error level:"+level); //NOI18N
            System.err.println("Line number:"+e.getLineNumber()); //NOI18N
            System.err.println("Column number:"+e.getColumnNumber()); //NOI18N
            System.err.println("Public ID:"+e.getPublicId()); //NOI18N
            System.err.println("System ID:"+e.getSystemId()); //NOI18N
            System.err.println("Error message:"+e.getMessage()); //NOI18N
        }
        
        public void error(org.xml.sax.SAXParseException e) {
            message("ERROR",e); //NOI18N
        }
        
        public void warning(org.xml.sax.SAXParseException e) {
            message("WARNING",e); //NOI18N
        }
        
        public void fatalError(org.xml.sax.SAXParseException e) {
            message("FATAL",e); //NOI18N
        }
    } //end of inner class ErrorCatcher
    
}
