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
package org.netbeans.modules.etl.ui;

import java.awt.Image;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.view.cookies.ExecuteTestCookie;
import org.netbeans.modules.etl.ui.view.cookies.SelectTablesCookie;
import org.openide.ErrorManager;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Utilities;

/** A node to represent this object. */
public class ETLNode extends DataNode implements PropertyChangeListener {

    public static final int VALID = 1;
    public static final int ERROR = 0;
    public static final int WARNING = 2;
    static Image eTLImg = Utilities.loadImage("org/netbeans/modules/etl/ui/resources/images/ETLDefinition.png");
    static Image errorImg = Utilities.loadImage("org/netbeans/modules/etl/ui/resources/images/ETLDefinitionError.png");
    static Image warningImg = Utilities.loadImage("org/netbeans/modules/etl/ui/resources/images/ETLDefinitionWarning.png");
    private ETLDataObject dObj;
    private int state = VALID;
    private static transient final Logger mLogger = Logger.getLogger(ETLNode.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public ETLNode(ETLDataObject obj) {
        this(obj, Children.LEAF);
        this.dObj = obj;
    }

    private ETLNode(DataObject obj, Children ch) {
        super(obj, ch);
        this.dObj = (ETLDataObject) obj;
        obj.addPropertyChangeListener(this);
        setIconBaseWithExtension("org/netbeans/modules/etl/ui/resources/images/ETLDefinition.png");
        init();
    }

    private void init() {
        CookieSet cs = getCookieSet();
		cs.add(dObj);
        cs.add(new ExecuteTestCookie());
        cs.add(new SelectTablesCookie());
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
            //Property execProp = new PropertySupport.Reflection(this.dObj.getETLDefinition().getSQLDefinition(),
                    //String.class, "getExecutionStrategyStr", null);
            String nbBundle1 = mLoc.t("BUND179: Collaboration Name"); 
            //String nbBundle2 = mLoc.t("PRSR001: Execution Strategy"); 
            nameProp.setName(nbBundle1.substring(15));
            //execProp.setName(Localizer.parse(nbBundle2));
            set.put(nameProp);
            //set.put(execProp);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        sheet.put(set);
        return sheet;
    }

    @Override
    public Image getIcon(int type) {
        Image img = eTLImg;
        try {
            if (state == ERROR) {
                img = Utilities.mergeImages(eTLImg, errorImg, 1, 0);
            } else if (state == WARNING) {
                img = Utilities.mergeImages(eTLImg, warningImg, 1, 0);
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
