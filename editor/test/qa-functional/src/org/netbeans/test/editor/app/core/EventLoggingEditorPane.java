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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.test.editor.app.core;

import org.netbeans.test.editor.app.gui.*;
import javax.swing.JEditorPane;
import java.awt.event.KeyEvent;
import javax.swing.text.Keymap;
import javax.swing.KeyStroke;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.*;
import java.awt.event.*;
import org.netbeans.modules.editor.NbEditorDocument;
import javax.swing.text.EditorKit;
import javax.swing.plaf.TextUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.test.editor.app.Main;
import org.openide.text.CloneableEditorSupport;

/**
 *
 * @author  pnejedly
 * @version
 */
public class EventLoggingEditorPane extends JEditorPane {
    
    private Logger logger=null;
    public Hashtable namesToActions;
    private Action[] actions;
    
    /** Creates new EventLoggingEditorPane */
    public EventLoggingEditorPane() {
        super();
    }
    
    public String[] getActionsNames() {
        Action[] as = getEditorKit().getActions();
        String[] ret;
        ret=new String[as.length];
        for( int i=0; i < as.length; i++ )
            ret[i]=(String)(actions[i].getValue( Action.NAME ));
        return ret;
        
    }
    
    public void setLogger(Logger log) {
        logger = log;
    }
    
    public Completion getCompletion() {
        return ((ExtEditorUI)(Utilities.getEditorUI(Main.frame.getEditor()))).getCompletion();
    }
    
    private final boolean myMapEventToAction(KeyEvent e) {
        Keymap binding = getKeymap();
        Completion comp=((ExtEditorUI)(Utilities.getEditorUI(this))).getCompletion();
        
        if (comp.isPaneVisible()) {
            KeyStroke kst=KeyStroke.getKeyStroke(e.getKeyCode(),e.getModifiers(),false);
            
            if (logger != null) {
                String com=(String)(comp.getJDCPopupPanel().getInputMap().get(kst));
                if (com != null) {
                    logger.logCompletionAction(com);
                    return true;
                }
            }
        }
        
        if (binding != null) {
            KeyStroke k = KeyStroke.getKeyStrokeForEvent(e);
            Action a = binding.getAction(k);
            if (a != null) {
                String command = null;
                if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                    command = String.valueOf(e.getKeyChar());
                }
                ActionEvent ae =  new ActionEvent(this, ActionEvent.ACTION_PERFORMED,command, e.getModifiers());
                if (logger != null)
                    logger.logAction( a, ae );
                a.actionPerformed(ae);
                e.consume();
                return true;
            }
        }
        return false;
    }
    
    protected void processComponentKeyEvent(KeyEvent e) {
        int id = e.getID();
        switch(id) {
            case KeyEvent.KEY_TYPED:
                if (myMapEventToAction(e) == false) {
                    // default behavior is to input translated
                    // characters as content if the character
                    // hasn't been mapped in the keymap.
                    Keymap binding = getKeymap();
                    if (binding != null) {
                        Action a = binding.getDefaultAction();
                        if (a != null) {
                            ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                            String.valueOf(e.getKeyChar()), e.getModifiers());
                            if (logger != null)
                                logger.logAction( a, ae );
                            a.actionPerformed(ae);
                            e.consume();
                        }
                    }
                }
                break;
            case KeyEvent.KEY_PRESSED:
                myMapEventToAction(e);
                break;
            case KeyEvent.KEY_RELEASED:
                myMapEventToAction(e);
                break;
        }
    }
    
    public Action[] getActions() {
        if (actions == null)
            return new Action[0];
        return actions;
    }
    
    private void poorSetEditorKit(int index) {
        EditorKit kit = CloneableEditorSupport.getEditorKit(TestSetKitAction.kitsTypes[index]);
        setDocument(new NbEditorDocument(kit.getClass()));
        super.setEditorKit(kit);
        actions = null;
    }
    
    public void setEditorKit(int index) {
        poorSetEditorKit(index);
        System.err.println("Starting kit setting.");
        actions = getEditorKit().getActions();
        namesToActions = new Hashtable( actions.length );  // prepare hashtable for them and fill it with all actions
        for( int i=0; i < actions.length; i++ )
            namesToActions.put( actions[i].getValue( Action.NAME ), actions[i] );
        System.err.println("Ending kit setting.");
    }
    
    public void perform(final java.awt.event.ActionEvent p1) {
        Action a = (Action)namesToActions.get((String)(p1.getActionCommand()));
        if (logger != null)
            logger.logAction( a, p1 );
        a.actionPerformed(p1);
    }
}
