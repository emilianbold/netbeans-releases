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

package org.netbeans.modules.palette;
import java.awt.Image;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author Libor Kotouc
 */
public class PaletteEnvironmentProvider implements Environment.Provider {
    
    private static PaletteEnvironmentProvider createProvider() {
        return new PaletteEnvironmentProvider();
    }

    private PaletteEnvironmentProvider() {
    }

// ----------------   Environment.Provider ----------------------------    
    
    public Lookup getEnvironment(DataObject obj) {

        PaletteItemNodeFactory nodeFactory = new PaletteItemNodeFactory((XMLDataObject)obj);
        return nodeFactory.getLookup();
    }

    
    private static class PaletteItemNodeFactory implements InstanceContent.Convertor {

//        private static final String URL_PREFIX_INSTANCES = "PaletteItems/";
        
        private XMLDataObject xmlDataObject = null;

        private Lookup lookup = null;
        
        Reference refNode = new WeakReference(null);

        PaletteItemNodeFactory(XMLDataObject obj) {

            xmlDataObject = obj;

            InstanceContent content = new InstanceContent();
            content.add(Node.class, this);

            lookup = new AbstractLookup(content);
        }
        
        Lookup getLookup() {
            return lookup;
        }
        
        // ----------------   InstanceContent.Convertor ----------------------------    

        public Class type(Object obj) {
            return (Class)obj;
        }

        public String id(Object obj) {
            return obj.toString();
        }

        public String displayName(Object obj) {
            return ((Class)obj).getName();
        }

        public Object convert(Object obj) {
            Object o = null;
            if (obj == Node.class) {
                try {
                    o = getInstance();
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
           
            return o;
        }
        
        // ----------------   helper methods  ----------------------------    
        
        public PaletteItemNode getInstance() {

            synchronized (this) {
                PaletteItemNode node = (PaletteItemNode)refNode.get();
                if (node != null)
                    return node;
                
                FileObject file = xmlDataObject.getPrimaryFile();
                if (file.getSize() == 0L) // item file is empty
                    return null;
                
                PaletteItemHandler handler = new PaletteItemHandler();
                try {
                    XMLReader reader = XMLUtil.createXMLReader(true);
                    reader.setContentHandler(handler);
                    reader.setEntityResolver(EntityCatalog.getDefault());
                    String urlString = xmlDataObject.getPrimaryFile().getURL().toExternalForm();
                    InputSource is = new InputSource(xmlDataObject.getPrimaryFile().getInputStream());
                    is.setSystemId(urlString);
                    reader.parse(is);
                }
                catch (SAXException saxe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, saxe);
                } 
                catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }

                node = createPaletteItemNode(handler);
                refNode = new WeakReference(node);
                
                return node;
            }
        }

        private PaletteItemNode createPaletteItemNode(PaletteItemHandler handler) {

            String displayName = getDisplayName(handler.getBundleName(), handler.getDisplayNameKey(), handler.getClassName());
            String tooltip = getTooltip(handler.getBundleName(), handler.getTooltipKey(), handler.getClassName(), handler.getDisplayNameKey());
            Image icon16 = getIcon(handler.getIcon16URL());
            if (icon16 == null)
                icon16 = Utilities.loadImage("org/netbeans/modules/palette/resources/unknown16.gif"); // NOI18N
            Image icon32 = getIcon(handler.getIcon32URL());
            if (icon32 == null)
                icon32 = Utilities.loadImage("org/netbeans/modules/palette/resources/unknown32.gif"); // NOI18N
            
            
            ArrayList objectsToLookup = new ArrayList();

            ActiveEditorDrop drop = getActiveEditorDrop(handler.getClassName());
            if (drop != null)
                objectsToLookup.add(drop);
            if (handler.getBody() != null)
                objectsToLookup.add(handler.getBody());
            Lookup lookup = Lookups.fixed(objectsToLookup.toArray());

            PaletteItemNode node = new PaletteItemNode(displayName, tooltip, icon16, icon32, lookup);

            return node;
        }
        
        public String getDisplayName(
                String bundleName, 
                String displayNameKey, 
                String instanceName) 
        {

            String displayName = null;
            try {
                displayName = NbBundle.getBundle(bundleName).getString(displayNameKey);

                if (displayName == null && displayNameKey != null)
                    displayName = displayNameKey;

                if (displayName == null) {//derive name from the instance name
                    if (instanceName != null && instanceName.trim().length() > 0) {
                        int dotIndex = instanceName.lastIndexOf('.'); // NOI18N
                        displayName = instanceName.substring(dotIndex);
                    }
                }

                if (displayName == null) // no name derived from the item
                    displayName = xmlDataObject.getName();

            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            
            return (displayName == null ? "" : displayName);
        }

        public String getTooltip(
                String bundleName, 
                String tooltipKey, 
                String instanceName, 
                String displayNameKey) 
        {

            String tooltip = null;
            try {
                tooltip = NbBundle.getBundle(bundleName).getString(tooltipKey);

                if (tooltip == null && tooltipKey != null)
                    tooltip = tooltipKey;

                if (tooltip == null) {//derive name from instance name
                    if (instanceName != null && instanceName.trim().length() > 0) {
                        int dotIndex = instanceName.indexOf('.'); // NOI18N
                        tooltip = instanceName.substring(0, dotIndex).replace('-', '.'); // NOI18N
                    }
                }

                if (tooltip == null) // no tooltip derived from the item
                    tooltip = getDisplayName(bundleName, displayNameKey, instanceName);
                
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }

            return (tooltip == null ? "" :  tooltip);
        }

        public Image getIcon(String iconURL) {

            Image icon = null;
            try {
                icon = Utilities.loadImage(iconURL);
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }

            return icon;
        }

        private ActiveEditorDrop getActiveEditorDrop(String instanceName) {

            ActiveEditorDrop drop = null;

            if (instanceName != null && instanceName.trim().length() > 0) {//we should try to instantiate item drop
                try {
//                    Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
//                    FileObject fo = rep.getDefaultFileSystem().findResource(URL_PREFIX_INSTANCES + instanceName);
//                    DataObject _do = DataObject.find(fo);
//                    InstanceDataObject ido = (InstanceDataObject) _do;
//                    drop = (ActiveEditorDrop)ido.instanceCreate();

                    ClassLoader loader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
                    if (loader == null)
                        loader = getClass ().getClassLoader ();
                    Class instanceClass = loader.loadClass (instanceName);
                    drop = (ActiveEditorDrop)instanceClass.newInstance();
                }
                catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }

            return drop;
        }

    }
    
    
}
