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

/*
 * DataViewMultiViewFactory.java
 *
 * Created on October 13, 2005, 1:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.db.dataview.editor;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.db.dataview.editor.DataViewEditorSupport.CloseHandler;
import org.openide.windows.CloneableTopComponent;

/**
 *
 * @author Jeri Lockhart
 */
public class DataViewMultiViewFactory {
    
    /**
     * Creates a new instance of DataViewMultiViewFactory
     */
    public DataViewMultiViewFactory() {
    }
    
    public static CloneableTopComponent createMultiView(DataViewDataObject dataObject) {
        MultiViewDescription views[] = new MultiViewDescription[1];
        views[0] = new DataViewSourceMultiviewDesc(dataObject);
        CloseHandler cHandler = new DataViewEditorSupport.CloseHandler(dataObject);
        CloneableTopComponent multiview = 
                MultiViewFactory.createCloneableMultiView(views, views[0],cHandler);
        multiview.setDisplayName(dataObject.getPrimaryFile().getName());
        multiview.setName(dataObject.getPrimaryFile().getName());                
        return multiview;
    }
}