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

package org.netbeans.modules.palette;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
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
        
        public synchronized PaletteItemNode getInstance() {

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

        private PaletteItemNode createPaletteItemNode(PaletteItemHandler handler) {

            String name = xmlDataObject.getName();
            
            InstanceContent ic = new InstanceContent();
            if (handler.getClassName() != null)
                ic.add(handler.getClassName(), ActiveEditorDropProvider.getInstance());
            else if (handler.getBody() != null)
                ic.add(handler.getBody(), ActiveEditorDropDefaultProvider.getInstance());
            
            PaletteItemNode node = new PaletteItemNode(
                    new DataNode(xmlDataObject, Children.LEAF), 
                    name, 
                    handler.getBundleName(), 
                    handler.getDisplayNameKey(), 
                    handler.getClassName(), 
                    handler.getTooltipKey(), 
                    handler.getIcon16URL(), 
                    handler.getIcon32URL(), 
                    ic
            );

            return node;
        }
    }        

}
