/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.text;

import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import com.netbeans.editor.ActionFactory;
import com.netbeans.editor.EditorUI;
import com.netbeans.editor.ext.ExtKit;
import com.netbeans.editor.ext.FindDialogSupport;
import com.netbeans.editor.ext.GotoDialogSupport;
import org.openide.TopManager;
import org.openide.windows.TopComponent;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.actions.UndoAction;
import org.openide.actions.RedoAction;

/** 
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public abstract class NbEditorKit extends ExtKit {

  /** Shared suport for find and replace dialogs */
  private static NbFindDialogSupport nbFindDialogSupport;

  /** Shared support for goto dialogs */
  private static NbGotoDialogSupport nbGotoDialogSupport;

  /** Action property that stores the name of the corresponding nb-system-action */
  public static final String SYSTEM_ACTION_CLASS_NAME_PROPERTY = "systemActionClassName";

  static final long serialVersionUID =4482122073483644089L;

  public Document createDefaultDocument() {
    return new NbEditorDocument(this.getClass());
  }

  protected EditorUI createEditorUI() {
    return new NbEditorUI();
  }

  protected Action[] createActions() {
    Action[] nbEditorActions = new Action[] {
      new NbBuildPopupMenuAction(),
      new NbFindAction(),
      new NbReplaceAction(),
      new NbGotoAction(),
      new NbUndoAction(),
      new NbRedoAction(),
    };
    return TextAction.augmentList(super.createActions(), nbEditorActions);
  }


  protected void addSystemActionMapping(String editorActionName, Class systemActionClass) {
    Action a = getActionByName(editorActionName);
    if (a != null) {
      a.putValue(SYSTEM_ACTION_CLASS_NAME_PROPERTY, systemActionClass.getName());
    }
  }

  protected void installSystemActionMappings() {
    addSystemActionMapping(cutAction, org.openide.actions.CutAction.class);
    addSystemActionMapping(copyAction, org.openide.actions.CopyAction.class);
    addSystemActionMapping(pasteAction, org.openide.actions.PasteAction.class);
    addSystemActionMapping(removeSelectionAction, org.openide.actions.DeleteAction.class);

    addSystemActionMapping(findAction, org.openide.actions.FindAction.class);
    addSystemActionMapping(replaceAction, org.openide.actions.ReplaceAction.class);
    addSystemActionMapping(gotoAction, org.openide.actions.GotoAction.class);
  }

  public void install(JEditorPane c) {
    installSystemActionMappings();

    super.install(c);
  }

  public Class getFocusableComponentClass(JTextComponent c) {
    return TopComponent.class;
  }

  public class NbBuildPopupMenuAction extends BuildPopupMenuAction {

    static final long serialVersionUID =-8623762627678464181L;
    
    protected JMenuItem getItem(JTextComponent target, String actionName) {
      JMenuItem item = super.getItem(target, actionName);

      if (item == null && actionName != null) { // try if it's an action class name
        Class saClass;
        try {
          saClass = Class.forName(actionName);
        } catch (Throwable t) {
          saClass = null;
        }

        if (saClass != null && SystemAction.class.isAssignableFrom(saClass)) {
          TopManager tm = TopManager.getDefault();
          if (tm != null) { // IDE initialized
            SystemAction sa = SystemAction.get(saClass);
            if (sa instanceof Presenter.Popup) {
              item = ((Presenter.Popup)sa).getPopupPresenter();
              if (item != null && !(item instanceof JMenu)) {
                KeyStroke[] keys = tm.getGlobalKeymap().getKeyStrokesForAction(sa);
                if (keys != null && keys.length > 0) {
                  item.setAccelerator(keys[0]);
                }
              }
            }
          }
        }
      }

      return item;
    }

  }

  public static class NbFindAction extends FindAction {

    public FindDialogSupport getSupport() {
      if (nbFindDialogSupport == null) {
        nbFindDialogSupport = new NbFindDialogSupport();
      }
      return nbFindDialogSupport;
    }

  }

  public static class NbReplaceAction extends ReplaceAction {

    public FindDialogSupport getSupport() {
      if (nbFindDialogSupport == null) {
        nbFindDialogSupport = new NbFindDialogSupport();
      }
      return nbFindDialogSupport;
    }

  }

  public static class NbGotoAction extends GotoAction {

    public GotoDialogSupport getSupport() {
      if (nbGotoDialogSupport == null) {
        nbGotoDialogSupport = new NbGotoDialogSupport();
      }
      return nbGotoDialogSupport;
    }

  }

  public static class NbUndoAction extends ActionFactory.UndoAction {

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
      // Delegate to system undo action
      UndoAction ua = (UndoAction)SystemAction.get(UndoAction.class);
      if (ua != null && ua.isEnabled()) {
        ua.actionPerformed(evt);
      }
    }

  }

  public static class NbRedoAction extends ActionFactory.RedoAction {

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
      // Delegate to system redo action
      RedoAction ra = (RedoAction)SystemAction.get(RedoAction.class);
      if (ra != null && ra.isEnabled()) {
        ra.actionPerformed(evt);
      }
    }

  }




}

/*
 * Log
 *  4    Jaga      1.3         4/7/00   Miloslav Metelka 
 *  3    Jaga      1.2         3/24/00  Miloslav Metelka 
 *  2    Jaga      1.1         3/21/00  Miloslav Metelka 
 *  1    Jaga      1.0         3/15/00  Miloslav Metelka 
 * $
 */

