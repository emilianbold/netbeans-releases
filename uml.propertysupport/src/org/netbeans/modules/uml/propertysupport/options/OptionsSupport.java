/*
 * OptionsSupport.java
 *
 * Created on November 15, 2005, 3:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.propertysupport.options;

import java.util.Iterator;
import java.util.Vector;
import org.netbeans.modules.uml.propertysupport.options.api.UMLOptionsPanel;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;

/**
 *
 * @author krichard
 */
public class OptionsSupport {
    
    private final boolean debug = true ;
    private Vector<UMLOptionsPanel> umlOptionPanels = new Vector() ;
    
    /** Creates a new instance of OptionsSupport */
    public OptionsSupport() {
        this.gatherUMLOptionPanels() ;
    }
    
    private void gatherUMLOptionPanels() {
        log("gatherUMLOptionPanel");
        
        FileSystem fs = Repository.getDefault().getDefaultFileSystem() ;
        if (fs == null) return ;
        
        FileObject fo = fs.findResource("UML/UMLOptions/Panels");
        
        Lookup lookup = new FolderLookup(DataFolder.findFolder(fo)).getLookup();
        Iterator it = lookup.lookup(new Lookup.Template(UMLOptionsPanel.class)).
                allInstances().iterator();
        
        while (it.hasNext()) {
            UMLOptionsPanel option = (UMLOptionsPanel) it.next();
            umlOptionPanels.addElement(option);
        }
        
        //
        //        if (fo == null) return;
        //
        //        FileObject [] panels = fo.getChildren() ;
        //
        //        //DataFolder df = fo != null ? DataFolder.findFolder(fo) : null;
        //
        //        if (panels != null) {
        //            //DataObject [] optionPanels = df.getChildren();
        //            for (int i = 0; i < panels.length; i++) {
        //                try             {
        //                    DataObject panel = DataObject.find(panels[i]);
        //                    InstanceCookie ic = (InstanceCookie) panel.getCookie(InstanceCookie.class);
        //
        //                    if (ic == null)
        //                        continue;
        //
        //                    Object instance;
        //
        //                    try {
        //                        instance = ic.instanceCreate();
        //                    } catch (java.io.IOException e) {
        //                        e.printStackTrace();
        //                        continue;
        //                    } catch (java.lang.ClassNotFoundException e) {
        //                        e.printStackTrace();
        //                        continue;
        //                    }
        //                    if (instance instanceof org.netbeans.modules.uml.propertysupport.options.api.UMLOptionsPanel)
        //                        this.umlOptionPanels.addElement((org.netbeans.modules.uml.propertysupport.options.api.UMLOptionsPanel) instance);
        //                } catch (DataObjectNotFoundException ex) {
        //                    Exceptions.printStackTrace(ex);
        //                }
        //
        //            }
        //        }
        
        
    }
    
    protected Vector<UMLOptionsPanel> panels() {
        return umlOptionPanels ;
    }
    
    private void log(String s) {
        if (debug) System.out.println(this.getClass().toString()+"::"+s);
    }
}
