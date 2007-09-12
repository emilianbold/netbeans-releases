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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xslt.tmap.navigator;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import org.netbeans.modules.xslt.tmap.TMapDataObject;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorPanelWithUndo;
import org.openide.awt.UndoRedo;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;


/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapLogicalNavigatorPanel implements NavigatorPanel/*WithUndo*/ {

    private AtomicReference<UndoRedo.Manager> myUndoRedoRef = 
            new AtomicReference<UndoRedo.Manager>();
    
    /** holds UI of this panel. */
    private JComponent myComponent;
    /** current context to work on. */
    private Lookup.Result<DataObject> myContext;
    
    private TMapModel myModel;
    
    private static String NAV_PANEL_NAME = NbBundle.
            getMessage(TMapLogicalNavigatorPanel.class, "LBL_TMAP_LOGICAL_VIEW"); // NOI18N
    
    public static String getUName() {
        return NAV_PANEL_NAME;
    }
    
    private final LookupListener mySelectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            setNewContent();
        }
    };
    
    public TMapLogicalNavigatorPanel() {
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return NAV_PANEL_NAME;
    }

    /** {@inheritDoc} */
    public String getDisplayHint() {
        return NbBundle.getMessage(TMapLogicalNavigatorPanel.class, 
                "LBL_TMAP_LOGICAL_VIEW_TOOLTIP"); // NOI18N
    }

    /** {@inheritDoc} */
    public JComponent getComponent() {
        if (myComponent == null) {
            myComponent = new TMapLogicalPanel();
        }
        return myComponent;
    }

    /** {@inheritDoc} */
    public void panelActivated(Lookup context) {
        myContext = context.lookup(
            new Lookup.Template(DataObject.class));
        assert myContext !=null;
        DataObject curDataObject = getTMapDataObject();
        myContext.addLookupListener(mySelectionListener);
        mySelectionListener.resultChanged(null);
    }

    /** {@inheritDoc} */
    public void panelDeactivated() {
        if (myContext  != null) {
            myContext.removeLookupListener(mySelectionListener);
            myContext = null;
        }
    }

    /** {@inheritDoc} */
    public Lookup getLookup() {
        return null;
    }
    
    /** {@inheritDoc} */
//    public UndoRedo getUndoRedo() {
//        if (myUndoRedoRef.get() == null) {
//            myUndoRedoRef.compareAndSet(null, createUndoRedo());
//        }
//        return myUndoRedoRef.get();
//    }
//
//    private UndoRedo.Manager createUndoRedo() {
//        UndoRedo.Manager undoRedo = null;
//        TMapDataObject dObj = getTMapDataObject();
//        if (dObj != null) {
//             undoRedo = dObj.getEditorSupport().getUndoManager();
//        }
//
//        return undoRedo;
//    }

    private TMapDataObject getTMapDataObject() {
        TMapDataObject dObj = null;
        Collection<? extends DataObject> dObjs = myContext.allInstances();
        if (dObjs != null && dObjs.size() == 1) {
            DataObject tmpDObj = dObjs.iterator().next();
            if (tmpDObj.getClass() == TMapDataObject.class) {
                dObj = (TMapDataObject)tmpDObj;
            }
        }
        
        return dObj;
    }
    
    private TMapModel getModel(DataObject dOBj) {
        assert dOBj != null;
        Lookup lookup = dOBj.getLookup();
        return lookup != null ? lookup.lookup(TMapModel.class) : null;
    }
    
    private void setNewContent() {
        final DataObject curDataObject = getTMapDataObject();

        TMapModel tMapModel = null;
        if (curDataObject != null) {
            tMapModel = getModel(curDataObject);
        }
        
        if (tMapModel != null && myModel != tMapModel) {
            myModel = tMapModel;
            ((TMapLogicalPanel)getComponent()).
                    navigate(curDataObject.getLookup(), myModel);
            
        }
        
    }

}
