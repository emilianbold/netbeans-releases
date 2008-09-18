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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.drawingarea.keymap;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.modules.uml.drawingarea.actions.DiagramInputkeyAction;
import org.openide.awt.Toolbar;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author thuy
 */
public class DiagramInputkeyMapper implements DiagramKeyMapConstants{
    private static DiagramInputkeyMapper mapper;
    private TopComponent component;
    
    public static String MAC_ACCELERATOR = "MAC_ACCELERATOR";
    public static String ADDITIONAL_ACCELERATORS = "ADDITIONAL_ACCELERATORS";
    public static String ADDITIONAL_MAC_ACCELERATORS = "ADDITIONAL_MAC_ACCELERATORS";

    public static DiagramInputkeyMapper getInstance() 
    {
        if (mapper == null)
        {
            mapper = new DiagramInputkeyMapper();
        }
        
        return mapper;
    }

    public TopComponent getComponent()
    {
        return component;
    }

    public void setComponent(TopComponent component)
    {
        this.component = component;
    }
    public void registerToolbarActions(Toolbar editorToolbar)
    {
        if(component == null)
        {
            return;
        }
        
        InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = component.getActionMap();
        
        boolean useMac = Utilities.isMac();
        
        int unnamedActionCnt = 0;
        for(Component curComponent : editorToolbar.getComponents())
        {
            if (curComponent instanceof AbstractButton)
            {
                AbstractButton button = (AbstractButton) curComponent;
                Action action = button.getAction();
                
                if(useMac == true)
                {
                    if(action.getValue(MAC_ACCELERATOR) != null)
                    {
                        action.putValue(Action.ACCELERATOR_KEY, 
                                        action.getValue(MAC_ACCELERATOR));
                    }
                    
                    if(action.getValue(ADDITIONAL_MAC_ACCELERATORS) != null)
                    {
                        action.putValue(ADDITIONAL_ACCELERATORS, 
                                        action.getValue(ADDITIONAL_MAC_ACCELERATORS)); 
                    }
                }
                
                String actionName = (String)action.getValue(Action.NAME);
                if(actionName == null)
                {
                    actionName = "UnnamedAction" + (unnamedActionCnt++);
                    
                    // The action must have a name so we can unregister the action.
                    action.putValue(Action.NAME, actionName);
                    button.setText(null);
                }
                
                if(action.getValue(Action.ACCELERATOR_KEY) != null)
                {
                    KeyStroke keystroke = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
                    inputMap.put(keystroke, actionName);
                    actionMap.put(actionName, action);
                    button.getAccessibleContext().setAccessibleName((String) action.getValue(action.SHORT_DESCRIPTION));
                    button.setToolTipText(buildTooltip(action));
                }
                
                if(action.getValue(ADDITIONAL_ACCELERATORS) != null)
                {
                    KeyStroke[] additional = (KeyStroke[]) action.getValue(ADDITIONAL_ACCELERATORS);
                    for(int index = 0; index < additional.length; index++)
                    {
                        KeyStroke stroke = additional[index];
                        
                        String name = actionName + (index + 1);
                        inputMap.put(stroke, name);
                        actionMap.put(name, action);
                    }
                }
            }
        }
    }

    public void unRegisterToolbarActions(Toolbar editorToolbar)
    {
        if(component == null)
        {
            return;
        }
        
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();
        
        for(Component curComponent : editorToolbar.getComponents())
        {
            if (curComponent instanceof JButton)
            {
                JButton button = (JButton) curComponent;
                Action action = button.getAction();
                
                if(action.getValue(Action.ACCELERATOR_KEY) != null)
                {
                    String actionName = (String)action.getValue(Action.NAME);
                    inputMap.remove((KeyStroke)action.getValue(Action.ACCELERATOR_KEY));
                    actionMap.remove(actionName);
                }
            }
        }
    }

    public static String buildTooltip(Action action)
    {
       String tooltip = (String)action.getValue(Action.SHORT_DESCRIPTION);
        if((tooltip == null) || (tooltip.length() == 0))
        {
            tooltip = (String)action.getValue(Action.NAME);
        }
        
        KeyStroke stroke = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
        if(stroke != null)
        {
            String keystroke = keyStrokeToString(stroke);
            tooltip = NbBundle.getMessage(DiagramInputkeyMapper.class, 
                                          "FMT_ButtonHint", tooltip, keystroke);
        }
        
        return tooltip;
    }
   
    /**
     * Creates nice textual representation of KeyStroke.
     * Modifiers and an actual key label are concated by plus signs
     * @param the KeyStroke to get description of
     * @return String describing the KeyStroke
     */
    public static String keyStrokeToString(KeyStroke stroke)
    {
        String retVal = "";
        
        String modifText = KeyEvent.getKeyModifiersText(stroke.getModifiers());
        
        String keyText = (stroke.getKeyCode() == KeyEvent.VK_UNDEFINED) ? 
                          String.valueOf(stroke.getKeyChar()) : 
            getKeyText(stroke.getKeyCode());
        
        if (modifText.length() > 0)
        {
            // The mac does not put the '+' character between the keys.
            if(Utilities.isMac() == true)
            {
                retVal = modifText.replaceAll("\\+", "");
            }
            else
            {
                retVal = modifText + '+';
            }
            retVal += keyText;
        }
        else
        {
            retVal = keyText;
        }
        
        return retVal;
    }
    
    /** @return slight modification of what KeyEvent.getKeyText() returns.
     *  The numpad Left, Right, Down, Up get extra result.
     */
    private static String getKeyText(int keyCode)
    {
        String ret = KeyEvent.getKeyText(keyCode);
        if (ret != null)
        {
            switch (keyCode)
            {
                case KeyEvent.VK_KP_DOWN:
                    ret = prefixNumpad(ret, KeyEvent.VK_DOWN);
                    break;
                case KeyEvent.VK_KP_LEFT:
                    ret = prefixNumpad(ret, KeyEvent.VK_LEFT);
                    break;
                case KeyEvent.VK_KP_RIGHT:
                    ret = prefixNumpad(ret, KeyEvent.VK_RIGHT);
                    break;
                case KeyEvent.VK_KP_UP:
                    ret = prefixNumpad(ret, KeyEvent.VK_UP);
                    break;
            }
        }
        return ret;
    }

    private static String prefixNumpad(String key, int testKeyCode)
    {
        if (key.equals(KeyEvent.getKeyText(testKeyCode)))
        {
            key = NbBundle.getBundle(DiagramInputkeyMapper.class).getString("key-prefix-numpad") + key;
        }
        return key;
     }
    
    public void registerKeyMap ()
    {
         if (component == null)
        {
            return;
        }
        int i = 1;
        String keyCodeString = "";
        InputMap inputMap = null;
        ActionMap actionMap = component.getActionMap();
       
        InputKeyResources bundle = (InputKeyResources) ResourceBundle.getBundle(InputKeyResources.class.getName());
        DiagramInputkeyAction inputkeyAction = null; 
        
        while ((keyCodeString = bundle.getStringResource("key." + i + ".keyCode")) != null)
        {
            int keyCode = Integer.valueOf(keyCodeString).intValue();
            int modifiers = Integer.valueOf(bundle.getStringResource("key." + i + ".modifiers")).intValue();
            String command = bundle.getStringResource("key." + i + ".command");
            int focus = Integer.valueOf(bundle.getStringResource("key." + i + ".focus")).intValue();
            
            inputkeyAction = new DiagramInputkeyAction(component, command);
            inputMap = component.getInputMap(focus);
            
            
            if((Utilities.isMac() == true) && 
               (bundle.getStringResource("key." + i + ".mac_modifiers") != null))
            {
                int macModifiers = Integer.valueOf(bundle.getStringResource("key." + i + ".mac_modifiers")).intValue();
                inputMap.put(KeyStroke.getKeyStroke(keyCode, macModifiers), command);
            }
            else
            {
                inputMap.put(KeyStroke.getKeyStroke(keyCode, modifiers), command);
            }
            actionMap.put(command, inputkeyAction);
            
            i++;
        }
    }
    
    public void unRegisterKeyMap ()
    {
         if (component == null)
        {
            return;
        }
        int i = 1;
        String keyCodeString = "";
        InputMap inputMap = null;
        ActionMap actionMap = component.getActionMap();
       
        InputKeyResources bundle = (InputKeyResources) ResourceBundle.getBundle(InputKeyResources.class.getName());
        
        while ((keyCodeString = bundle.getStringResource("key." + i + ".keyCode")) != null)
        {
            int keyCode = Integer.valueOf(keyCodeString).intValue();
            int modifiers = Integer.valueOf(bundle.getStringResource("key." + i + ".modifiers")).intValue();
            int focus = Integer.valueOf(bundle.getStringResource("key." + i + ".focus")).intValue();
            
            inputMap = component.getInputMap(focus);
            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
            String command = (String) inputMap.get(keyStroke);
            inputMap.remove(keyStroke);
            actionMap.remove(command);
            i++;
        }
    }
}
