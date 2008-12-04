/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.debugger.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.python.debugger.config.NetBeansFrontend;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Action which shows PythonDebugging Console component.
 */
public class PythonDebugConsoleAction extends AbstractAction {
    public static String ICON_PATH = "org/netbeans/modules/python/debugger/actions/bugicon.gif";
    public PythonDebugConsoleAction() {
        super(NbBundle.getMessage(PythonDebugConsoleAction.class, "CTL_PythonDebugConsoleAction"));
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt)
    {
//        PythonConsoleTopComponent win = PythonConsoleTopComponent.findInstance();
//        if (win.nTerm() > 1)
//            win.newTab();
//        else{
//            win.open();
//        }
//        win.requestActive();
      // first check for correct initializations
      // of IDE frontend module
      NetBeansFrontend.initCheck() ;
      openPythonDebuggingWindow() ;
    }

    private void openPythonDebuggingWindow()
    {
      JpyDbgView jv = JpyDbgView.getCurrentView() ;
      jv.openPythonDebuggingWindow(null,true) ;
    }

}
