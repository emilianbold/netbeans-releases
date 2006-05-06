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

package org.netbeans.modules.form.actions;

import javax.swing.*;
import java.awt.*;

import org.openide.util.HelpCtx;
import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.netbeans.modules.form.*;

/**
 * @author Tomas Pavek
 */

public class TestAction extends CallableSystemAction implements Runnable {

    private static String name;

    public TestAction() {
        setEnabled(false);
    }

    protected boolean asynchronous() {
        return false;
    }

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(TestAction.class)
                     .getString("ACT_TestMode"); // NOI18N
        return name;
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.testing"); // NOI18N
    }

    /** @return resource for the action icon */
    protected String iconResource() {
        return "org/netbeans/modules/form/resources/test_form.png"; // NOI18N
    }

    public void performAction() {
        if (formDesigner != null) {
            if (java.awt.EventQueue.isDispatchThread())
                run();
            else
                java.awt.EventQueue.invokeLater(this);
        }
    }

    public void run() {
        RADVisualComponent topComp = formDesigner.getTopDesignComponent();
        if (topComp == null)
            return;

        RADVisualComponent parent = topComp.getParentContainer();
        while (parent != null) {
            topComp = parent;
            parent = topComp.getParentContainer();
        }

        FormModel formModel = formDesigner.getFormModel();
        RADVisualFormContainer formContainer =
            topComp instanceof RADVisualFormContainer ?
                (RADVisualFormContainer) topComp : null;

        // a JFrame or Frame will be used (depending on form is Swing or AWT)
        Object formInstance = topComp.getBeanInstance();
        Class frameClass = formInstance instanceof JComponent
                           || formInstance instanceof JFrame
                           || formInstance instanceof JDialog
                           || formInstance instanceof JApplet
                           || formInstance instanceof JWindow
                           || (!(formInstance instanceof Window)
                               && !(formInstance instanceof Panel)) ?
            JFrame.class : Frame.class;

        try {
            // create a copy of form
            final Frame frame = (Frame)
                FormDesigner.createFormView(topComp, frameClass);

            // set title
            String title = frame.getTitle();
            if (title == null || "".equals(title)) { // NOI18N
                title = topComp == formModel.getTopRADComponent() ?
                        formModel.getName() : topComp.getName();
                frame.setTitle(java.text.MessageFormat.format(
                    org.openide.util.NbBundle.getBundle(TestAction.class)
                                               .getString("FMT_TestingForm"), // NOI18N
                    new Object[] { title }
                ));
            }

            // prepare close operation
            if (frame instanceof JFrame) {
                ((JFrame)frame).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                HelpCtx.setHelpIDString(((JFrame)frame).getRootPane(),
                                        "gui.modes"); // NOI18N
            }
            else {
                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent evt) {
                        frame.dispose();
                    }
                });
            }
 
            // set size
            boolean shouldPack = false;
            if (formModel.isFreeDesignDefaultLayout()) {
                // [temporary hack for new layout: always set the size according to the form designer]
                if (formContainer != null) {
                    Dimension size = formContainer.getDesignerSize();
                    Dimension diffDim = RADVisualFormContainer.getDecoratedWindowContentDimensionDiff();
                    size = new Dimension(size.width + diffDim.width,
                                         size.height + diffDim.height);
                    frame.setSize(size);
                }
                else shouldPack = true;
            }
            else {
                if (formContainer != null
                    && formContainer.getFormSizePolicy()
                                         == RADVisualFormContainer.GEN_BOUNDS
                    && formContainer.getGenerateSize())
                {
                    frame.setSize(formContainer.getFormSize());
                }
                else shouldPack = true;
            }

            // Issue 66594 and 12084
            final boolean pack = shouldPack;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if (pack) {
                        frame.pack();
                    }
                    frame.setBounds(org.openide.util.Utilities.findCenterBounds(frame.getSize()));
                    frame.setVisible(true);
                }
            });
        }
        catch (Exception ex) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
    }

    // -------

    public void setFormDesigner(FormDesigner designer) {
        formDesigner = designer;
        setEnabled(formDesigner != null && formDesigner.getTopDesignComponent() != null);
    }

    private FormDesigner formDesigner;
}
