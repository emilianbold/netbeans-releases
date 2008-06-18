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
package org.netbeans.modules.db.dataview.editor;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Utilities;

/** A node to represent this object. */
public class DataViewNode extends DataNode implements PropertyChangeListener {

    public static final int VALID = 1;
    public static final int ERROR = 0;
    public static final int WARNING = 2;
    static Image dvImg = Utilities.loadImage("org/netbeans/modules/db/dataview/editor/images/DataViewDefinition.png");
    static Image errorImg = Utilities.loadImage("org/netbeans/modules/db/dataview/editor/images/DataViewDefinitionError.png");
    static Image warningImg = Utilities.loadImage("org/netbeans/modules/db/dataview/editor/images/DataViewDefinitionWarning.png");
    private DataViewDataObject dObj;
    private int state = VALID;

    public DataViewNode(DataViewDataObject obj) {
        this(obj, Children.LEAF);
        this.dObj = obj;
    }

    private DataViewNode(DataObject obj, Children ch) {
        super(obj, ch);
        this.dObj = (DataViewDataObject) obj;
        obj.addPropertyChangeListener(this);
        setIconBaseWithExtension("org/netbeans/modules/db/dataview/editor/images/DataViewDefinition.png");
        init();
    }

    private void init() {
        CookieSet cs = getCookieSet();
        cs.add(dObj);
    }

    public void setCollabState(int state) {
        this.state = state;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        try {
            Property nameProp = new PropertySupport.Reflection(this.dObj, String.class,
                    "getName", null);
            String nbBundle1 = "Collaboration Name";
            nameProp.setName(nbBundle1);
            set.put(nameProp);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        sheet.put(set);
        return sheet;
    }

    @Override
    public Image getIcon(int type) {
        Image img = dvImg;
        try {
            if (state == ERROR) {
                img = Utilities.mergeImages(dvImg, errorImg, 1, 0);
            } else if (state == WARNING) {
                img = Utilities.mergeImages(dvImg, warningImg, 1, 0);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return img;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        fireIconChange();
    }
}
