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

package org.netbeans.modules.editor;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtKit;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallbackSystemAction;

/** 
* Editor UI
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorUI extends ExtEditorUI {

  private FocusListener focusL;

  private SystemActionUpdater findActionUpdater;
  private SystemActionUpdater replaceActionUpdater;
  private SystemActionUpdater gotoActionUpdater;
  private SystemActionUpdater removeSelectionActionUpdater;

  protected SystemActionUpdater createSystemActionUpdater(
  String editorActionName, boolean updatePerformer, boolean syncEnabling) {
    return new SystemActionUpdater(editorActionName, updatePerformer, syncEnabling);
  }

  public NbEditorUI() {
    // Start syncing the selected system actions
    findActionUpdater = createSystemActionUpdater(ExtKit.findAction, true, false);
    replaceActionUpdater = createSystemActionUpdater(ExtKit.replaceAction, true, false);
    gotoActionUpdater = createSystemActionUpdater(ExtKit.gotoAction, true, false);
    removeSelectionActionUpdater = createSystemActionUpdater(ExtKit.removeSelectionAction,
        true, true);

    focusL = new FocusAdapter() {
      public void focusGained(FocusEvent evt) {
        // Refresh file object when component made active
        Document doc = getDocument();
        if (doc != null) {
          DataObject dob = NbEditorUtilities.getDataObject(doc);
          if (dob != null) {
            FileObject fo = dob.getPrimaryFile();
            if (fo != null) { 
              fo.refresh();
            }
          }
        }
      }
    };

  }

  protected void installUI(JTextComponent c) {
    super.installUI(c);

    c.addFocusListener(focusL);
  }


  protected void uninstallUI(JTextComponent c) {
    super.uninstallUI(c);

    c.removeFocusListener(focusL);
  }

  public final class SystemActionUpdater
  implements PropertyChangeListener, ActionPerformer, FocusListener {
    
    private String editorActionName;

    private boolean updatePerformer;

    private boolean syncEnabling;

    private Action editorAction;

    private Action systemAction;

    private PropertyChangeListener enabledPropertySyncL;


    SystemActionUpdater(String editorActionName, boolean updatePerformer,
    boolean syncEnabling) {
      this.editorActionName = editorActionName;
      this.updatePerformer = updatePerformer;
      this.syncEnabling = syncEnabling;

      synchronized (NbEditorUI.this.getComponentLock()) {
        // if component already installed in EditorUI simulate installation
        JTextComponent component = getComponent();
        if (component != null) {
          propertyChange(new PropertyChangeEvent(NbEditorUI.this,
              EditorUI.COMPONENT_PROPERTY, null, component));
        }

        NbEditorUI.this.addPropertyChangeListener(this);
      }
    }

    public synchronized void focusGained(FocusEvent evt) {
      Action ea = getEditorAction();
      Action sa = getSystemAction();
      if (ea != null && sa != null) {
        if (updatePerformer) {
          if (ea.isEnabled() && sa instanceof CallbackSystemAction) {
            ((CallbackSystemAction)sa).setActionPerformer(this);
          }
        }

        if (syncEnabling) {
          if (enabledPropertySyncL == null) {
            enabledPropertySyncL = new EnabledPropertySyncListener(sa);
          }
          ea.addPropertyChangeListener(enabledPropertySyncL);
        }
      }
    }

    public void focusLost(FocusEvent evt) {
      Action ea = getEditorAction();
      Action sa = getSystemAction();
      if (ea != null && sa != null) {
/*        if (sa instanceof CallbackSystemAction) {
          CallbackSystemAction csa = (CallbackSystemAction)sa;
          if (csa.getActionPerformer() == this) {
            csa.setActionPerformer(null);
          }
        }
*/

        if (syncEnabling && enabledPropertySyncL != null) {
          ea.removePropertyChangeListener(enabledPropertySyncL);
        }
      }
    }

    private void reset() {
      if (enabledPropertySyncL != null) {
        editorAction.removePropertyChangeListener(enabledPropertySyncL);
      }

/*      if (systemAction != null) {
        if (systemAction instanceof CallbackSystemAction) {
          CallbackSystemAction csa = (CallbackSystemAction)systemAction;
          if (!csa.getSurviveFocusChange() || csa.getActionPerformer() == this) {
            csa.setActionPerformer(null);
          }
        }
      }
*/

      editorAction = null;
      systemAction = null;
      enabledPropertySyncL = null;
    }

    /** Perform the callback action */
    public synchronized void performAction(SystemAction action) {
      JTextComponent component = getComponent();
      Action ea = getEditorAction();
      if (component != null && ea != null) {
        ea.actionPerformed(new ActionEvent(component, 0, "")); // NOI18N
      }
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
      String propName = evt.getPropertyName();

      if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
        JTextComponent component = (JTextComponent)evt.getNewValue();
        if (component != null) { // just installed
          component.addPropertyChangeListener(this);
          component.addFocusListener(this);

        } else { // just deinstalled
          component = (JTextComponent)evt.getOldValue();

          component.removePropertyChangeListener(this);
          component.removeFocusListener(this);
        }

        reset();

      } else if ("editorKit".equals(propName)) { // NOI18N

        reset();
      }
    }

    private Action getEditorAction() {
      if (editorAction == null) {
        BaseKit kit = Utilities.getKit(getComponent());
        if (kit != null) {
          editorAction = kit.getActionByName(editorActionName);
        }
      }
      return editorAction;
    }

    private Action getSystemAction() {
      if (systemAction == null) {
        Action ea = getEditorAction();
        if (ea != null) {
          String saClassName = (String)ea.getValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY);
          if (saClassName != null) {
            Class saClass;
            try {
              saClass = Class.forName(saClassName);
            } catch (Throwable t) {
              saClass = null;
            }

            if (saClass != null) {
              if (TopManager.getDefault() != null) {
                systemAction = SystemAction.get(saClass);
              }
            }
          }
        }
      }
      return systemAction;
    }

    protected void finalize() throws Throwable {
      reset();
    }

  }

  /** Listener that listen on changes of the "enabled" property
  * and if changed it changes the same property of the action
  * given in constructor.
  */
  static class EnabledPropertySyncListener implements PropertyChangeListener {

    Action action;

    EnabledPropertySyncListener(Action actionToBeSynced) {
      this.action = actionToBeSynced;
    }

    public void propertyChange(PropertyChangeEvent evt) {
      if ("enabled".equals(evt.getPropertyName())) { // NOI18N
        action.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
      }
    }

  }

}

/*
 * Log
 *  5    Jaga      1.4         4/3/00   Miloslav Metelka no performer clearing
 *  4    Jaga      1.3         3/27/00  Miloslav Metelka checking focus surviving
 *  3    Jaga      1.2         3/24/00  Miloslav Metelka 
 *  2    Jaga      1.1         3/21/00  Miloslav Metelka 
 *  1    Jaga      1.0         3/15/00  Miloslav Metelka 
 * $
 */

