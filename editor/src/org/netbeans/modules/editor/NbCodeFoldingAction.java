/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.awt.Component;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Utilities;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;

/**
 *  Code Folding action displayed under Menu/View/
 *
 *  @author  Martin Roskanin
 */
public  class NbCodeFoldingAction extends GlobalContextAction implements Presenter.Menu{

    
    /** Creates a new instance of NbCodeFoldingAction */
    public NbCodeFoldingAction() {
    }
    
    public final HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getBundle(NbCodeFoldingAction.class).getString(
            "Menu/View/CodeFolds"); //NOI18N
    }        

    public void resultChanged(org.openide.util.LookupEvent ev) {
    }    
    
    public boolean isEnabled() {
        return false;
    }

    /** Get a menu item that can present this action in a {@link javax.swing.JMenu}.
    * @return the representation for this action
    */
    public JMenuItem getMenuPresenter(){
        return new CodeFoldsMenu(getName());
    }
    
    private static JTextComponent getComponent(){
        return Utilities.getFocusedComponent();
    }
    
    public void actionPerformed (java.awt.event.ActionEvent ev){
    }
    
    private BaseKit getKit(){
        JTextComponent component = getComponent();
        return (component == null) ? BaseKit.getKit(NbEditorKit.class) : Utilities.getKit(component);
    }
    
    private static Object getSettingValue(BaseKit kit, String settingName) {
        return Settings.getValue(kit.getClass(), settingName);
    }

    /** Get the value of the boolean setting from the <code>Settings</code>
     * @param settingName name of the setting to get.
     */
    private static boolean getSettingBoolean(BaseKit kit, String settingName) {
        Boolean val = (Boolean)getSettingValue(kit, settingName);
        return (val != null) ? val.booleanValue() : false;
    }
    

    private boolean isFoldingEnabledInSettings(BaseKit kit){
        return getSettingBoolean(kit, SettingsNames.CODE_FOLDING_ENABLE);
    }
    
    
    public class CodeFoldsMenu extends JMenu implements DynamicMenuContent {
        public CodeFoldsMenu(){
            super();
        }
        
        public CodeFoldsMenu(String s){
            super(s);
            //#40585 fix start - setting the empty, transparent icon for the menu item to align it correctly with other items
            //setIcon(new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/editor/resources/empty.gif"))); //NOI18N
            //#40585 fix end
            org.openide.awt.Mnemonics.setLocalizedText(this, s);
        }

        public JComponent[] getMenuPresenters() {
            return new JComponent[] { this };
        }
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            getPopupMenu();
            return items;
        }
        
        public JPopupMenu getPopupMenu(){
            JPopupMenu pm = super.getPopupMenu();
            pm.removeAll();
            BaseKit bKit = getKit();
            if (bKit==null) bKit = BaseKit.getKit(NbEditorKit.class);
            if (bKit!=null){
                Action action = bKit.getActionByName(NbEditorKit.generateFoldPopupAction);
                if (action instanceof BaseAction){
                    boolean foldingAvailable = isFoldingEnabledInSettings(bKit);
                    JTextComponent component = Utilities.getFocusedComponent();
                    if (foldingAvailable){
                        ActionMap contextActionmap = getContextActionMap();
                        if (contextActionmap!=null){
                            foldingAvailable = contextActionmap.get(BaseKit.collapseFoldAction) != null &&
                                component != null;

                            if (!foldingAvailable){
                                bKit = BaseKit.getKit(NbEditorKit.class);
                                if (bKit!=null){
                                    Action defaultAction = bKit.getActionByName(NbEditorKit.generateFoldPopupAction);
                                    if (defaultAction instanceof BaseAction) action = defaultAction;
                                }
                            }
                        }
                    }

                    JMenu menu = (JMenu)((BaseAction)action).getPopupMenuItem(foldingAvailable ? component : null);
                    if (menu!=null){
                        Component comps[] = menu.getMenuComponents();
                        for (int i=0; i<comps.length; i++){
                            pm.add(comps[i]);
                        }
                    }
                }
            }
            pm.pack();
            return pm;
        }
    }
    
}
