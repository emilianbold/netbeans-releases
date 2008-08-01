/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.options;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.editor.Settings;

import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.openide.util.Lookup;
import java.util.Collection;
import java.util.Iterator;
import org.openide.modules.ModuleInfo;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileSystem;

/** MIME Option XML file.
 *
 * @author  Martin Roskanin
 * @since 08/2001
 */
public abstract class MIMEOptionFile{

    private static final Logger LOG = Logger.getLogger(MIMEOptionFile.class.getName());
    
    protected BaseOptions base;
    protected MIMEProcessor processor;
    protected Document dom;
    protected Map properties;
    private boolean loaded = false;
    private boolean wasSaved = false;
    private ModuleInfo editorMI;
    
    /** File change listener.
     * If file was externally modified, we have to reload settings.*/
    private final FileChangeListener fileListener = new FileChangeAdapter() {
        public @Override void fileChanged(FileEvent fe) {
            // ignore changes in settings files that are handled by the new infra
            if (!(processor instanceof AbbrevsMIMEProcessor) && 
                !(processor instanceof FontsColorsMIMEProcessor) && 
                !(processor instanceof KeyBindingsMIMEProcessor))
            {
                reloadSettings();
            }
        }
    };

    /** Editor module uninstallation listener */
    private final PropertyChangeListener moduleListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt){
            if (!ModuleInfo.PROP_ENABLED.endsWith(evt.getPropertyName())) return;
            if (!(evt.getSource() instanceof ModuleInfo)) return;
            
            if (((ModuleInfo)evt.getSource() != null) && (!((ModuleInfo)evt.getSource()).isEnabled())){
                editorUninstalled();
            }
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
        addEditorModuleListener();
    }


    private void addEditorModuleListener(){
        Lookup.Template templ = new Lookup.Template(ModuleInfo.class);
        Lookup.Result result = Lookup.getDefault().lookup(templ);
        // get all modules
        Collection modules = result.allInstances();
        ModuleInfo curInfo;
        for (Iterator iter = modules.iterator(); iter.hasNext(); ) {
            curInfo = (ModuleInfo)iter.next();
            if ("org.netbeans.modules.editor".equals(curInfo.getCodeNameBase())) { //NOI18N
                curInfo.addPropertyChangeListener(moduleListener);
                editorMI = curInfo;
                break;
            }
        }
    }

    /** Editor is uninstalled. We have to detach processor from XMLDO. */    
    private void editorUninstalled(){
        // remove file change listener from xml file object, that listened to external modification
        processor.getXMLDataObject().getPrimaryFile().removeFileChangeListener(fileListener);
        try{
            processor.getXMLDataObject().setValid(false);
        }catch(PropertyVetoException pve){
            LOG.log(Level.INFO, null, pve);
        }
        // remove editor uninstallation listener
        editorMI.removePropertyChangeListener(moduleListener);
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
        synchronized (Settings.class) {
            if (wasSaved){
                // the settings has been saved programatically. We don't need to reload them
                wasSaved = false;
                return;
            }

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
    }
    
    protected void saveSettings(Document doc){
        synchronized (Settings.class) {
            try{
                FileLock lock = processor.getXMLDataObject().getPrimaryFile().lock();
                try{
                    OutputStream os = processor.getXMLDataObject().getPrimaryFile().getOutputStream(lock);
                    try {
                        wasSaved = true;
                        XMLUtil.write(doc, os, "UTF-8"); // NOI18N
                        os.flush();
                    } catch (IOException ioe1){
                        wasSaved = false;
                        LOG.log(Level.WARNING, null, ioe1);
                    } finally {
                        os.close();
                    }
                }catch (IOException ioe2){
                    LOG.log(Level.WARNING, null, ioe2);
                }finally{
                    lock.releaseLock();
                }
            }catch (IOException ioe3){
                LOG.log(Level.WARNING, null, ioe3);
            }
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
    
    /* package */ final void setAllProperties(final Map properties) {
        try {
            FileSystem fs = processor.getXMLDataObject().getPrimaryFile().getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    updateSettings(properties);
                }
            });
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, "Can't save settings by " + processor, ioe); //NOI18N
        }
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
