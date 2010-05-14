/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

package org.netbeans.modules.edm.editor.ui.view;

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;


/**
 * This Class uses the Lookup to find the data object corresponding
 * to the active top component.
 * @author ks161616
 */
public class MashupDataObjectProvider {
    
    /* singleton instance of the top component provider */
    private static MashupDataObjectProvider instance;
    
    public static MashupDataObject activeDataObject;
    
    /**
     * Creates a new instance of DataObjectProvider
     */
    private MashupDataObjectProvider() {        
    }
    
    /**
     * Gets the singleton instance of the provider.
     *    
     */
    public static MashupDataObjectProvider getProvider() {
        if(instance == null) {
            instance = new MashupDataObjectProvider();
        }
        return instance;
    }
    
    /**
     * Gets the active ETL data object.
     *
     */
    public MashupDataObject getActiveDataObject() {
        Object obj = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
        if(obj instanceof MashupDataObject) {
            activeDataObject = (MashupDataObject)obj;
        }
        // If no active data object is found, returns the previously active data object.
        // check if any other ways exists to do this.
        return activeDataObject;
    }
}
