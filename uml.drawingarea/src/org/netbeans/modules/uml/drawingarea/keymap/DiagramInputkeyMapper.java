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

import java.util.ResourceBundle;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import org.netbeans.modules.uml.drawingarea.actions.DiagramInputkeyAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author thuy
 */
public class DiagramInputkeyMapper implements DiagramKeyMapConstants{
    private static DiagramInputkeyMapper mapper;
    private TopComponent component;
    
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
            inputMap.put(KeyStroke.getKeyStroke(keyCode, modifiers), command);
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
            //String command = bundle.getStringResource("key." + i + ".command");
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
