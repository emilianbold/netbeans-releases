/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package jemmyI18NWizard.wizardSupport;

import org.netbeans.test.oo.gui.jam.Jemmy;
import org.netbeans.test.oo.gui.jello.JelloOKCancelDialog;
import org.netbeans.test.oo.gui.jam.JamComboBox;
import org.netbeans.jemmy.operators.JTreeOperator;
import javax.swing.tree.TreePath;


public class SelectSourcesDialog extends JelloOKCancelDialog {
    
    protected JamComboBox filesystemCombo;
    protected JTreeOperator treeOperator;
    
    /** Creates new SelectSourcesDialog */
    public SelectSourcesDialog(String title) {
        super(title);
        filesystemCombo = this.getJamComboBox(0);
        treeOperator = new JTreeOperator(Jemmy.getOp(this));
    }
    
    public void expandRow(int index) {      //index beginning witgh 1
        treeOperator.expandRow(index);
    }
    
    public void selectRow(int index) {
        treeOperator.setSelectionRow(index);
    }
    
    public void collapseRow(int index) {
        treeOperator.collapseRow(index);
    }
    
    public boolean selectFilesystem(String name) {
        for(int i=0;i<filesystemCombo.getItemCount();i++) {
            String selectedName = filesystemCombo.getItemAt(i).toString();
            StringBuffer buffer = new StringBuffer(selectedName);
            int startingAt = selectedName.indexOf("displayName=");
            buffer.delete(0, startingAt+"displayName=".length());
            buffer.deleteCharAt(buffer.length()-1);
            if(name.equals(buffer.toString())) {
                filesystemCombo.setSelectedItem(i);
                return true;
            }
        }
        return false;
    }
    
    public void setSelectedItem(int index) {
        filesystemCombo.setSelectedItem(index);
    }
    
    public String getSelectedFilesystem() {
        return filesystemCombo.getSelectedItem();
    }
    
    public String findPath(String name) {
        return null;
    }
    
    public void expandPath(String[] pathString) {
        TreePath path = treeOperator.findPath(pathString, false, false);
        treeOperator.expandPath(path);
    }
    
    public void selectPath(String[] pathString) {
        TreePath path = treeOperator.findPath(pathString, false, false);
        treeOperator.selectPath(path);
    }
    
}
