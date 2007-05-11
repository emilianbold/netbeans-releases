/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
    private Vector<UMLOptionsPanel> miscOptionPanels = new Vector() ;
    
    /** Creates a new instance of OptionsSupport */
    public OptionsSupport() {
        this.gatherUMLOptionPanels() ;
    }
    
    private Iterator getPanels(String path) {
        
        FileSystem fs = Repository.getDefault().getDefaultFileSystem() ;
        if (fs == null) return null;
        
        FileObject fo = fs.findResource(path);
        if (fo == null) return null;
        
        Lookup lookup = new FolderLookup(DataFolder.findFolder(fo)).getLookup();
        Iterator it = lookup.lookup(new Lookup.Template(UMLOptionsPanel.class)).
                allInstances().iterator();
        
        return it ;
        
    }
    
    private void gatherUMLOptionPanels() {
        log("gatherUMLOptionPanel");
        
        
        // Get the main panels that have been defined in the layer files of the
        // required modules.
        Iterator it = getPanels ("UML/UMLOptions/Panels") ;        
        
        //if it == null then no panels were found. This would be bad. 
        while (it.hasNext()) {
            UMLOptionsPanel option = (UMLOptionsPanel) it.next();
            umlOptionPanels.addElement(option);
        }
        
        // Now add the misc panels, also declared in the layer files, to the misc panel.
        it = getPanels ("UML/UMLOptions/Misc") ;        
        
        //it is possible to have no Misc panels so check for null.
        if (it != null)
            while (it.hasNext()) {
                UMLOptionsPanel option = (UMLOptionsPanel) it.next();
                miscOptionPanels.addElement(option);
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
    
    protected Vector<UMLOptionsPanel> getMainPanels() {
        return umlOptionPanels ;
    }
    protected Vector<UMLOptionsPanel> getMiscPanels() {
        return miscOptionPanels ;
    }
    
    private void log(String s) {
        if (debug) System.out.println(this.getClass().toString()+"::"+s);
    }
}
