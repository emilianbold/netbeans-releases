
/*
 * The contents of this file are subject to the terms of the Common
 * Development
The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved.

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
 *
 */
package org.netbeans.modules.edm.editor.graph.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ActionMapUIResource;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.graph.actions.AddTableAction;
import org.netbeans.modules.edm.editor.graph.actions.RuntimeInputAction;
import org.netbeans.modules.edm.editor.graph.actions.AutoLayoutAction;
import org.netbeans.modules.edm.editor.graph.actions.CollapseAllAction;
import org.netbeans.modules.edm.editor.graph.actions.EditJoinAction;
import org.netbeans.modules.edm.editor.graph.actions.EditConnectionAction;
import org.netbeans.modules.edm.editor.graph.actions.ExpandAllAction;
import org.netbeans.modules.edm.editor.graph.actions.FitToHeightAction;
import org.netbeans.modules.edm.editor.graph.actions.FitToPageAction;
import org.netbeans.modules.edm.editor.graph.actions.FitToWidthAction;
import org.netbeans.modules.edm.editor.graph.actions.TestRunAction;
import org.netbeans.modules.edm.editor.graph.actions.ShowOutputAction;
import org.netbeans.modules.edm.editor.graph.actions.ValidationAction;
import org.netbeans.modules.edm.editor.graph.actions.ZoomInAction;
import org.netbeans.modules.edm.editor.graph.actions.ZoomOutAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.edm.editor.graph.components.BasicToolBar;

/**
 *
 * @author karthikeyan s
 */
public class MashupToolbar extends JToolBar {

    private MashupDataObject mObj; 

    /** Creates a new instance of MashupToolbar */
    public MashupToolbar(MashupDataObject dObj) {
        mObj = dObj;
        setRollover(true);
    }

    public JToolBar getToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.getAccessibleContext().setAccessibleName("Mashup ToolBar");
        toolBar.getAccessibleContext().setAccessibleName("Mashup ToolBar");
        toolBar.addSeparator();

        // Fit to page button.

        final JButton expandButton = new JButton(new ExpandAllAction(mObj));
        final JButton collapseButton = new JButton(new CollapseAllAction(mObj));

        expandButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Expand_All_Widgets"));        
        expandButton.setEnabled(false);
        expandButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                collapseButton.setEnabled(true);
                expandButton.setEnabled(false);
            }
        });        
        expandButton.getAccessibleContext().setAccessibleName("Expand All Widgets");
        expandButton.getAccessibleContext().setAccessibleDescription("Expand All Widgets");
        InputMap keyMap11 = new ComponentInputMap(expandButton);
        keyMap11.put(KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK), "action");
        ActionMap actionMap11 = new ActionMapUIResource();
        actionMap11.put("action",new ExpandAllAction(mObj));
        SwingUtilities.replaceUIActionMap(expandButton, actionMap11);
        SwingUtilities.replaceUIInputMap(expandButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap11);
        expandButton.setMnemonic('E');        
        BasicToolBar.processButton(expandButton);
        toolBar.add(expandButton);

        // Auto layout button.
        
        collapseButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Collapse_All_Widgets"));        
        collapseButton.setEnabled(true);
        collapseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                collapseButton.setEnabled(false);
                expandButton.setEnabled(true);
            }
        });

        collapseButton.getAccessibleContext().setAccessibleName("Collapse All Widgets");
        collapseButton.getAccessibleContext().setAccessibleDescription("Collapse All Widgets");
        InputMap keyMap12 = new ComponentInputMap(collapseButton);
        keyMap12.put(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK), "action");
        ActionMap actionMap12 = new ActionMapUIResource();
        actionMap12.put("action",new CollapseAllAction(mObj));
        SwingUtilities.replaceUIActionMap(collapseButton, actionMap12);
        SwingUtilities.replaceUIInputMap(collapseButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap12);
        collapseButton.setMnemonic('C');
        BasicToolBar.processButton(collapseButton);
        toolBar.add(collapseButton);

        // Show output button.
        JButton outputButton = new JButton(new ShowOutputAction(mObj));
        outputButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Toggle_Output"));
        outputButton.getAccessibleContext().setAccessibleName("Toggle Output");
        outputButton.getAccessibleContext().setAccessibleDescription("Toggle Output");
        InputMap keyMap10 = new ComponentInputMap(outputButton);
        keyMap10.put(KeyStroke.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK+InputEvent.ALT_DOWN_MASK), "action");
        ActionMap actionMap10 = new ActionMapUIResource();
        actionMap10.put("action",new ShowOutputAction(mObj));
        SwingUtilities.replaceUIActionMap(outputButton, actionMap10);
        SwingUtilities.replaceUIInputMap(outputButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap10);
        outputButton.setMnemonic('T');
        BasicToolBar.processButton(outputButton);
        toolBar.add(outputButton);      
        
        // Add table button.
        JButton addTableButton = new JButton(new AddTableAction(mObj));
        InputMap keyMap4 = new ComponentInputMap(addTableButton);
        keyMap4.put(KeyStroke.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK), "action");
        ActionMap actionMap4 = new ActionMapUIResource();
        actionMap4.put("action", new AddTableAction(mObj));
        SwingUtilities.replaceUIActionMap(addTableButton, actionMap4);
        SwingUtilities.replaceUIInputMap(addTableButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap4);
        addTableButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Add_Table"));
        addTableButton.getAccessibleContext().setAccessibleName("Add Table");
        addTableButton.getAccessibleContext().setAccessibleDescription("Add Table");
        addTableButton.setMnemonic('T');
        BasicToolBar.processButton(addTableButton);
        toolBar.add(addTableButton);

        toolBar.addSeparator();

        // Edit join view button.
        JButton editButton = new JButton(new EditJoinAction(mObj));
        editButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Edit_Join"));
        InputMap keyMap5 = new ComponentInputMap(editButton);
        keyMap5.put(KeyStroke.getKeyStroke('J', InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK), "action");
        ActionMap actionMap5 = new ActionMapUIResource();
        actionMap5.put("action",new EditJoinAction(mObj));
        SwingUtilities.replaceUIActionMap(editButton, actionMap5);
        SwingUtilities.replaceUIInputMap(editButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap5);
        editButton.getAccessibleContext().setAccessibleName("Edit Join");
        editButton.getAccessibleContext().setAccessibleDescription("Edit Join");
        editButton.setMnemonic('J');
        BasicToolBar.processButton(editButton);
        toolBar.add(editButton);

        // Edit join view button.
        JButton editDBButton = new JButton(new EditConnectionAction(mObj));
        editDBButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Edit_Database_Properties"));
        InputMap keyMap6 = new ComponentInputMap(editDBButton);
        keyMap6.put(KeyStroke.getKeyStroke('P', InputEvent.CTRL_DOWN_MASK+InputEvent.ALT_DOWN_MASK), "action");
        ActionMap actionMap6 = new ActionMapUIResource();
        actionMap6.put("action",new EditConnectionAction(mObj));
        SwingUtilities.replaceUIActionMap(editDBButton, actionMap6);
        SwingUtilities.replaceUIInputMap(editDBButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap6);
        editDBButton.getAccessibleContext().setAccessibleName("Edit Database Properties");
        editDBButton.getAccessibleContext().setAccessibleDescription("Edit Database Properties");
        editDBButton.setMnemonic('P');
        BasicToolBar.processButton(editDBButton);
        toolBar.add(editDBButton);

        // Runtime input button.
        JButton runtimeInputButton = new JButton(new RuntimeInputAction(mObj));

        
        runtimeInputButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Edit_Runtime_Input_Arguments"));
        InputMap keyMap7 = new ComponentInputMap(runtimeInputButton);
        keyMap7.put(KeyStroke.getKeyStroke('U', InputEvent.CTRL_DOWN_MASK+InputEvent.ALT_DOWN_MASK), "action");
        ActionMap actionMap7 = new ActionMapUIResource();
        actionMap7.put("action",new RuntimeInputAction(mObj));
        SwingUtilities.replaceUIActionMap(runtimeInputButton, actionMap7);
        SwingUtilities.replaceUIInputMap(runtimeInputButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap7);
        runtimeInputButton.getAccessibleContext().setAccessibleName("Edit Runtime Input Arguments");
        runtimeInputButton.getAccessibleContext().setAccessibleDescription("Edit Runtime Input Arguments");
        runtimeInputButton.setMnemonic('U');
        BasicToolBar.processButton(runtimeInputButton);
        toolBar.add(runtimeInputButton);

        toolBar.addSeparator();

        // Fit to page button.
        JButton fitButton = new JButton(new FitToPageAction(mObj));
        fitButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Fit_to_Page"));
        fitButton.getAccessibleContext().setAccessibleName("Fit to Page");
        fitButton.getAccessibleContext().setAccessibleDescription("Fit to Page");
        InputMap keyMap17 = new ComponentInputMap(fitButton);
        keyMap17.put(KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK+InputEvent.ALT_DOWN_MASK), "action");
        ActionMap actionMap17 = new ActionMapUIResource();
        actionMap17.put("action",new FitToPageAction(mObj));
        SwingUtilities.replaceUIActionMap(fitButton, actionMap17);
        SwingUtilities.replaceUIInputMap(fitButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap17);
        fitButton.setMnemonic('F');
        BasicToolBar.processButton(fitButton);
        toolBar.add(fitButton);

        // Fit to width button.
        JButton fitToWidthButton = new JButton(new FitToWidthAction(mObj));
        fitToWidthButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Fit_to_Width"));
        fitToWidthButton.getAccessibleContext().setAccessibleName("Fit to Width");
        fitToWidthButton.getAccessibleContext().setAccessibleDescription("Fit to Width");
        InputMap keyMap18 = new ComponentInputMap(fitToWidthButton);
        keyMap18.put(KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK+InputEvent.ALT_DOWN_MASK), "action");
        ActionMap actionMap18 = new ActionMapUIResource();
        actionMap18.put("action",new FitToWidthAction(mObj));
        SwingUtilities.replaceUIActionMap(fitToWidthButton, actionMap18);
        SwingUtilities.replaceUIInputMap(fitToWidthButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap18);
        fitToWidthButton.setMnemonic('W');
        BasicToolBar.processButton(fitToWidthButton);
        toolBar.add(fitToWidthButton);

        // Fit to page button.
        JButton fitToHeightButton = new JButton(new FitToHeightAction(mObj));
        fitToHeightButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Fit_to_Height"));
        fitToHeightButton.getAccessibleContext().setAccessibleName("Fit to Height");
        fitToHeightButton.getAccessibleContext().setAccessibleDescription("Fit to Height");
        InputMap keyMap19 = new ComponentInputMap(fitToHeightButton);
        keyMap19.put(KeyStroke.getKeyStroke('H', InputEvent.CTRL_DOWN_MASK+InputEvent.ALT_DOWN_MASK), "action");
        ActionMap actionMap19 = new ActionMapUIResource();
        actionMap19.put("action",new FitToHeightAction(mObj));
        SwingUtilities.replaceUIActionMap(fitToHeightButton, actionMap19);
        SwingUtilities.replaceUIInputMap(fitToHeightButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap19);
        fitToHeightButton.setMnemonic('H');
        BasicToolBar.processButton(fitToHeightButton);
        toolBar.add(fitToHeightButton);

        toolBar.addSeparator();

        // Zoom in button.
        JButton zoominButton = new JButton(new ZoomInAction(mObj));
        zoominButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Zoom_In"));
        zoominButton.getAccessibleContext().setAccessibleName("Zoom In");
        zoominButton.getAccessibleContext().setAccessibleDescription("Zoom In");
        zoominButton.setMnemonic('I');
        BasicToolBar.processButton(zoominButton);
        toolBar.add(zoominButton);

        // Zoom in button.
        JButton zoomoutButton = new JButton(new ZoomOutAction(mObj));
        zoomoutButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Zoom_Out"));
        zoomoutButton.getAccessibleContext().setAccessibleName("Zoom Out");
        zoomoutButton.getAccessibleContext().setAccessibleDescription("Zoom Out");
        zoomoutButton.setMnemonic('O');
        BasicToolBar.processButton(zoomoutButton);
        toolBar.add(zoomoutButton);

        // Fit to page button.
        JComboBox zoomBox = new ZoomCombo(mObj.getGraphManager());
        zoomBox.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Zoom_graph"));
        zoomBox.getAccessibleContext().setAccessibleName("Zoom graph");
        zoomBox.getAccessibleContext().setAccessibleDescription("Zoom graph");
        toolBar.add(zoomBox);

        toolBar.addSeparator();

        // Auto layout button.                      
        JButton layoutButton = new JButton(new AutoLayoutAction(mObj));
        layoutButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Auto_Layout"));
        InputMap keyMap13 = new ComponentInputMap(layoutButton);
        keyMap13.put(KeyStroke.getKeyStroke('L', InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK), "action");
        ActionMap actionMap13 = new ActionMapUIResource();
        actionMap13.put("action",new AutoLayoutAction(mObj));
        SwingUtilities.replaceUIActionMap(layoutButton, actionMap13);
        SwingUtilities.replaceUIInputMap(layoutButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap13);
        layoutButton.getAccessibleContext().setAccessibleName("Auto Layout");
        layoutButton.getAccessibleContext().setAccessibleDescription("Auto Layout");
        layoutButton.setMnemonic('L');
        BasicToolBar.processButton(layoutButton);
        toolBar.add(layoutButton);

        // Validate button.
        JButton validateButton = new JButton(new ValidationAction(mObj));
        validateButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Validate"));
        InputMap keyMap14 = new ComponentInputMap(validateButton);
        keyMap14.put(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK), "action");
        ActionMap actionMap14 = new ActionMapUIResource();
        actionMap14.put("action",new ValidationAction(mObj));
        SwingUtilities.replaceUIActionMap(validateButton, actionMap14);
        SwingUtilities.replaceUIInputMap(validateButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap14);
        validateButton.getAccessibleContext().setAccessibleName("Validate");
        validateButton.getAccessibleContext().setAccessibleDescription("Validate");
        validateButton.setMnemonic('V');
        BasicToolBar.processButton(validateButton);
        toolBar.add(validateButton);


        // Run collaboration button.
        JButton runButton = new JButton(new TestRunAction(mObj));
        runButton.setToolTipText(NbBundle.getMessage(MashupToolbar.class, "TOOLTIP_Run"));
        InputMap keyMap15 = new ComponentInputMap(runButton);
        keyMap15.put(KeyStroke.getKeyStroke('N', InputEvent.SHIFT_DOWN_MASK+InputEvent.ALT_MASK), "action");
        ActionMap actionMap15 = new ActionMapUIResource();
        actionMap15.put("action",new TestRunAction(mObj));
        SwingUtilities.replaceUIActionMap(runButton, actionMap15);
        SwingUtilities.replaceUIInputMap(runButton, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap15);
        runButton.getAccessibleContext().setAccessibleName("Run");
        runButton.getAccessibleContext().setAccessibleDescription("Run");
        runButton.setMnemonic('N');
        BasicToolBar.processButton(runButton);
        toolBar.add(runButton);

        return toolBar;
    }
    
}