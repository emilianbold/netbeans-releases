/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.options;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;


/**
 * Implementation of this class represents one category (like "Fonts & Colors" 
 * or "Editor") in Options Dialog. It should be registerred in layers:
 *
 * <pre style="background-color: rgb(255, 255, 153);">
 * &lt;folder name="OptionsDialog"&gt;
 *     &lt;file name="FooOptionsPanel.instance"&gt;
 *         &lt;attr name="instanceClass" stringvalue="org.foo.FooOptionsPanel"/&gt;
 *     &lt;/file&gt;
 * &lt;/folder&gt;</pre>
 *
 * Use standard way how to sort items registered in layers:
 * 
 * <pre style="background-color: rgb(255, 255, 153);">
 * &lt;attr name="GeneralPanel/Advanced" boolvalue="true"/&gt;
 * </pre>
 *
 * @see AdvancedOption
 * @see OptionsCategory.PanelController 
 *
 * @author Jan Jancura
 */
public abstract class OptionsCategory {
    
    /**
     * Returns base name of 32x32 icon used in list on the left side of 
     * Options Dialog. See AbstractNode.setIconBase method for more info.
     *
     * @return base name of 32x32 icon
     */
    public abstract String getIconBase ();
    
    /**
     * Returns name of category used in list on the left side of 
     * Options Dialog.
     *
     * @return name of category
     */
    public abstract String getCategoryName ();
    
    /**
     * This text will be used in title component on the top of Options Dialog
     * when your panel will be selected.
     *
     * @return title of this panel
     */
    public abstract String getTitle ();
    
    /**
     * Returns new {@link PanelController} for this category. PanelController 
     * creates visual component to be used inside of the Options Dialog.
     * You should not do any time-consuming operations inside 
     * the constructor, because it blocks initialization of OptionsDialog. 
     * Initialization should be implemented in update method.
     *
     * @return new instance of PanelController for this options category
     */
    public abstract OptionsPanelController create ();
    
    //compatibility hack, see core/options/build.xml for more details:
    private OptionsCategory.PanelController createOldImpl () {
        return null;
    }
    
    private OptionsPanelController createNewImpl () {
        Class clazz = getClass();
        Method[] methods = clazz.getDeclaredMethods();
        
        for (int cntr = 0; cntr < methods.length; cntr++) {
            Method m = methods[cntr];
            
            if ("create".equals(m.getName()) && m.getReturnType() == PanelController.class) {
                try {
                    return (PanelController) m.invoke(this, new Object[0]);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        return null;
    }
    
    
    /**
     * Temporary patch.
     * @deprecated  This class will not be a part of NB50! Use top 
     *              OptionsPanelController instead.
     */
    static abstract class PanelController extends OptionsPanelController {

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public static final String PROP_VALID = "valid";

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public static final String PROP_CHANGED = "changed";

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public static final String PROP_HELP_CTX = "helpCtx";

        public PanelController() {}
        
        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public abstract void update ();

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public abstract void applyChanges ();

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public abstract void cancel ();

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public abstract boolean isValid ();

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public abstract boolean isChanged ();

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public Lookup getLookup () {
            return Lookup.EMPTY;
        }

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public abstract JComponent getComponent (Lookup masterLookup);

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public abstract HelpCtx getHelpCtx ();

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public abstract void addPropertyChangeListener (PropertyChangeListener l);

        /**
         * @deprecated  This class will not be a part of NB50! Use top 
         *              OptionsPanelController instead.
         */
        public abstract void removePropertyChangeListener (PropertyChangeListener l);
    }
}
