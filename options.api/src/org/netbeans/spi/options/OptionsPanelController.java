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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.spi.options;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.modules.options.OptionsPanelControllerAccessor;
import org.netbeans.modules.options.advanced.AdvancedPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * PanelController creates visual representation of one Options Dialog
 * category, and manages communication between Options Dialog and this
 * panel.
 */
public abstract class OptionsPanelController {

    /**
     * Property name constant.
     */
    public static final String PROP_VALID = "valid";

    /**
     * Property name constant.
     */
    public static final String PROP_CHANGED = "changed";

    /**
     * Property name constant.
     */
    public static final String PROP_HELP_CTX = "helpCtx";

    static {
        OptionsPanelControllerAccessor.DEFAULT = new OptionsPanelControllerAccessor() {

            @Override
            public void setCurrentSubcategory(OptionsPanelController controller, String subpath) {
                controller.setCurrentSubcategory(subpath);
            }
            
        };
    }

    /**
     * Creates an advanced tabbed controller, just like Miscellaneous section.
     * @param subpath path to folder under OptionsDialog folder containing 
     * instances of AdvancedOption class. Path is composed from registration 
     * names divided by slash. E.g. "MyCategory" for the following registration:
     * <pre style="background-color: rgb(255, 255, 153);">
     * &lt;folder name="OptionsDialog"&gt;
     *     &lt;file name="MyCategory.instance"&gt;
     *         &lt;attr name="instanceClass" stringvalue="org.foo.MyCategory"/&gt;
     *         &lt;attr name="position" intvalue="900"/&gt;
     *     &lt;/file&gt;
     *     &lt;folder name="MyCategory"&gt;
     *         &lt;file name="SubCategory1.instance"&gt;
     *             &lt;attr name="instanceClass" stringvalue="org.foo.Subcategory1"/&gt;
     *         &lt;/file&gt;
     *         &lt;file name="SubCategory2.instance"&gt;
     *             &lt;attr name="instanceClass" stringvalue="org.foo.Subcategory2"/&gt;
     *         &lt;/file&gt;
     *     &lt;/file&gt;
     * &lt;/folder&gt;</pre>
     * @return OptionsPanelController a controller wrapping all AdvancedOption instances found in the folder
     * @since 1.8
     */
    public static final OptionsPanelController createAdvanced(String subpath) {
        return new AdvancedPanelController(subpath);
    }

    /**
     * Component should load its data here. You should not do any 
     * time-consuming operations inside the constructor, because it 
     * blocks initialization of OptionsDialog. Initialization 
     * should be implemented in update method.
     * This method is called after {@link #getComponent} method.
     * Update method can be called more than one time for the same instance 
     * of JComponent obtained from {@link #getComponent} call.
     */
    public abstract void update ();

    /**
     * This method is called when Options Dialog "OK" button is pressed.
     */
    public abstract void applyChanges ();

    /**
     * This method is called when Options Dialog "Cancel" button is pressed.
     */
    public abstract void cancel ();

    /**
     * Should return <code>true</code> if some option value in this 
     * category is valid.
     * 
     * 
     * @return <code>true</code> if some option value in this 
     * category is valid
     */
    public abstract boolean isValid ();

    /**
     * Should return <code>true</code> if some option value in this 
     * category has been changed.
     * 
     * 
     * @return <code>true</code> if some option value in this 
     * category has been changed
     */
    public abstract boolean isChanged ();

    /**
     * Each option category can provide some lookup. Options Dialog master
     * lookup is composed from these individual lookups. Master lookup
     * can be obtained from {@link #getComponent} call. This lookup is designed
     * to support communication anong individual panels in one Options
     * Dialog.
     * 
     * There is no guarantee that this method will be called from AWT thread.
     * 
     * @return lookup provided by this Options Dialog panel
     */
    public Lookup getLookup () {
        return Lookup.EMPTY;
    }

    /**
     * Returns visual component representing this options category.
     * This method is called before {@link #update} method.
     * 
     * @param masterLookup master lookup composed from lookups provided by 
     *        individual OptionsPanelControllers 
     *        - {@link OptionsPanelController#getLookup}
     * @return visual component representing this options category
     */
    public abstract JComponent getComponent (Lookup masterLookup);

    /**
     * Enables to handle selection of current subcategory. It is called from
     * {@link org.netbeans.api.options.OptionsDisplayer#open(java.lang.String)},
     * if some subpath is defined.
     * @param subpath path of subcategories to be selected. Path is 
     * composed from registration names divided by slash.
     * @see org.netbeans.api.options.OptionsDisplayer
     * @since 1.8
     */
    protected void setCurrentSubcategory(String subpath) {
    }

    /**
     * 
     * Get current help context asociated with this panel.
     * 
     * 
     * @return current help context
     */
    public abstract HelpCtx getHelpCtx ();

    /**
     * Registers new listener.
     * 
     * 
     * @param l a new listener
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
     * Unregisters given listener.
     * 
     * 
     * @param l a listener to be removed
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
}
