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

package org.netbeans.modules.testtools;
/*
 * ConfigDataLoader.java
 *
 * Created on November 26, 2002, 11:22 AM
 */


import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.SimpleBeanInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JLabel;
import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ConfigDataLoader extends UniFileLoader {

    static final long serialVersionUID =39178345634634560L;

    static final Image icon = Utilities.loadImage("org/netbeans/modules/testtools/ConfigIcon.gif"); //NO I18N
    
    /** Creates a new instance of ConfigDataLoader */
    public ConfigDataLoader() {
        super("ConfigDataObject");
    }

    /** Get default actions.
    * @return array of default system actions or <CODE>null</CODE> if this loader does not have any
    *   actions
    */
    protected SystemAction[] defaultActions () {
        return new SystemAction[] {
                       SystemAction.get(OpenAction.class),
                       SystemAction.get(FileSystemAction.class),
                       null,
                       SystemAction.get(CutAction.class),
                       SystemAction.get(CopyAction.class),
                       SystemAction.get(PasteAction.class),
                       null,
                       SystemAction.get(DeleteAction.class),
                       SystemAction.get(RenameAction.class),
                       null,
                       SystemAction.get(SaveAsTemplateAction.class),
                       null,
                       SystemAction.get(ToolsAction.class),
                       SystemAction.get(PropertiesAction.class)
                   };
    }

    /** Get the default display name of this loader.
    * @return default display name
    */
    protected String defaultDisplayName () {
        return NbBundle.getMessage(ConfigDataLoader.class, "XTestConfigName"); // NOI18N
    }

    /** performs initialization of Data Loader */    
    protected void initialize () {
        super.initialize ();
        getExtensions().addMimeType("text/xml"); // NOI18N
        getExtensions().addMimeType("application/xml"); // NOI18N
    }


    /** For a given file finds a primary file.
    * @param fo the file to find primary file for
    *
    * @return the primary file for the file or null if the file is not
    *   recognized by this loader
    */
    protected FileObject findPrimaryFile (FileObject fo) {
        fo = super.findPrimaryFile (fo);
        if (fo==null) return null;
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(fo.getInputStream()));
            String line;
            while ((line=br.readLine())!=null)
                if (line.indexOf("<mconfig ")>=0) { // NOI18N
                    br.close();
                    return fo;
                }
        } catch (Exception e) {}
        return null;
    }

    /** creates instance of ConfigDataObject for given FileObject
     * @param primaryFile FileObject
     * @throws DataObjectExistsException when Data Object already exists
     * @throws IOException when some IO problems
     * @return new XTestDataObject for given FileObject */    
    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new ConfigDataObject(primaryFile, this);
    }

    public static class ConfigDataObject extends XMLDataObject implements PropertyChangeListener {
        
        public ConfigDataObject (FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
            super(fo, loader);
            getCookieSet().add(new XTestProjectSupport(getPrimaryFile()));
            addPropertyChangeListener(ConfigDataLoader.ConfigDataObject.this);
        }
        
        protected Node createNodeDelegate() {
            return new ConfigDataNode(super.createNodeDelegate(), this);
        }

        /** handles change of some property
         * @param ev PropertyChangeEvent */    
        public void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();
            if(prop == null || prop.equals(DataObject.PROP_PRIMARY_FILE)) {
               ((XTestProjectSupport) getCookie(XTestProjectSupport.class)).setFileObject(getPrimaryFile());
            }
        }
    }
    
    public static class ConfigDataNode  extends FilterNode {

        ConfigDataObject dob;
        
        public ConfigDataNode(Node n, ConfigDataObject dob) {
            super(n);
            this.dob=dob;
        }
        
        public boolean hasCustomizer() {
            return true;
        }
        
        public Component getCustomizer() {
            return new ConfigCustomizerPanel(dob);
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx(ConfigDataNode.class);
        }

        public Image getIcon(int i) {
            return icon;
        }
        
        public Image getOpenedIcon(int i) {
            return icon;
        }
    }
    
}
