/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package validation;

import java.awt.Component;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.debugger.actions.FinishDebuggerAction;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JSpinnerOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Utility methods useful for testing of debugging.
 * @author Jiri..Skrivanek@sun.com
 */
public class Utils {
    
    /** Default value for Sun App Server. If we test Tomcat, it is 
     * overridden in setTomcatProperties() method. */
    private static int socketPort = 9009;
    public static final String SUN_APP_SERVER = "Sun";
    public static final String TOMCAT = "Tomcat";
    public static final String DEFAULT_SERVER = SUN_APP_SERVER;
    
    /** Sets Swing HTML Browser as default browser. */
    public static void setSwingBrowser() {
        OptionsOperator optionsOper = OptionsOperator.invoke();
        optionsOper.selectGeneral();
        // "Web Browser:"
        String webBrowserLabel = Bundle.getStringTrimmed("org.netbeans.modules.options.general.Bundle", "CTL_Web_Browser");
        JLabelOperator jloWebBrowser = new JLabelOperator(optionsOper, webBrowserLabel);
        // "Swing HTML Browser"
        String swingBrowserLabel = Bundle.getString("org.netbeans.core.ui.Bundle", "Services/Browsers/SwingBrowser.settings");
        new JComboBoxOperator((JComboBox)jloWebBrowser.getLabelFor()).selectItem(swingBrowserLabel);
        optionsOper.ok();
    }
    
    /** Sets a random port for Tomcat server and socket debugger transport. */
    public static void setTomcatProperties() throws Exception {
        // "Tools"
        String toolsItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Tools"); // NOI18N
        // "Server Manager"
        String serverManagerItem = Bundle.getStringTrimmed(
                "org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle",
                "CTL_ServerManager");
        new ActionNoBlock(toolsItem+"|"+serverManagerItem, null).perform();
        // "Server Manager"
        String serverManagerTitle = Bundle.getString(
                "org.netbeans.modules.j2ee.deployment.devmodules.api.Bundle",
                "TXT_ServerManager");
        NbDialogOperator serverManagerOper = new NbDialogOperator(serverManagerTitle);
        String j2eeLabel = Bundle.getString(
                "org.netbeans.modules.j2ee.deployment.impl.ui.Bundle",
                "LBL_J2eeServersNode");
        new Node(new JTreeOperator(serverManagerOper), j2eeLabel+"|"+"Bundled Tomcat").select(); // NOI18N
        // set server port
        JSpinnerOperator serverPortOper = new JSpinnerOperator(serverManagerOper, 0);
        // satisfy focus on spinner which causes changes are reflected
        serverPortOper.getNumberSpinner().scrollToValue((Number)serverPortOper.getNextValue());
        serverPortOper.setValue(new Integer(getPort()));
        // set shutdown port
        JSpinnerOperator shutdownPortOper = new JSpinnerOperator(serverManagerOper, 1);
        // satisfy focus on spinner which causes changes are reflected
        shutdownPortOper.getNumberSpinner().scrollToValue((Number)shutdownPortOper.getNextValue());
        shutdownPortOper.setValue(new Integer(getPort()));
        
        // set socket debugger transport
        // "Startup"
        String startupLabel = Bundle.getString("org.netbeans.modules.tomcat5.customizer.Bundle", "TXT_Startup");
        new JTabbedPaneOperator(serverManagerOper).selectPage(startupLabel);
        // "Socket Port:
        String socketPortLabel = Bundle.getString("org.netbeans.modules.tomcat5.customizer.Bundle", "TXT_SocketPort");
        new JRadioButtonOperator(serverManagerOper, socketPortLabel).setSelected(true);
        // set socket port number
        JSpinnerOperator socketPortOper = new JSpinnerOperator(serverManagerOper, 0);
        // satisfy focus on spinner which causes changes are reflected
        socketPortOper.getNumberSpinner().scrollToValue((Number)socketPortOper.getNextValue());
        socketPort = getPort();
        socketPortOper.setValue(new Integer(socketPort));
        
        serverManagerOper.close();
    }
    
    /** Returns socket port set in setTomcatProperties method.
     * @return socket port used for debugger transport
     */
    public static String getSocketPort() {
        return Integer.toString(socketPort);
    }
    
    /** Returns unique free port number within range of dynamic or private ports
     * (see http://www.iana.org/assignments/port-numbers)
     */
    private static int getPort() throws Exception {
        int port = 0;
        boolean notfree = true;
        while(notfree) {
            port = 49152+new Random().nextInt(16383);
            // test whether port is already used
            ServerSocket socket = null;
            try {
                socket = new ServerSocket(port);
                socket.close();
                // found a free port
                notfree = false;
            } catch (IOException ioe) {
                // BindException: Address already in use thrown
            }
        }
        return port;
    }
    
    /** Finishes debugger and wait until it finishes. */
    public static void finishDebugger() {
        ContainerOperator debugToolbarOper = getDebugToolbar();
        new FinishDebuggerAction().perform();
        // wait until Debug toolbar dismiss
        debugToolbarOper.waitComponentVisible(false);
        // wait until server is not in transient state
        J2eeServerNode serverNode = new J2eeServerNode(DEFAULT_SERVER);
        serverNode.waitFinished();
        new EventTool().waitNoEvent(2000);

        /* cannot be used because of this issue 71263 ('User program finished' not printed to output)
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        // start to track Main Window status bar
        stt.start();
        new FinishDebuggerAction().perform();
        String programFinishedLabel = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_finished");
        stt.waitText(programFinishedLabel);
        stt.stop();
        */
    }
    
    /** Returns ContainerOperator representing Debug toolbar.
     * @return ContainerOperator representing Debug toolbar
     */
    public static ContainerOperator getDebugToolbar() {
        String debugToolbarLabel = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "Toolbars/Debug");
        return MainWindowOperator.getDefault().getToolbar(debugToolbarLabel);
    }
    
    public static class ToolTipChooser implements ComponentChooser {
        private String tooltip;
        public ToolTipChooser(String tooltip) {
            this.tooltip = tooltip;
        }
        public boolean checkComponent(Component comp) {
            return tooltip.equals(((JComponent)comp).getToolTipText());
        }
        public String getDescription() {
            return("ToolTip equals to "+tooltip);
        }
    }
    
    /** Sets breakpoint in editor on line with specified text.
     * @param eo EditorOperator instance where to set breakpoint
     * @param text text to find for setting breakpoint
     * @return line number where breakpoint was set (starts from 1)
     */
    public static int setBreakpoint(EditorOperator eo, String text) throws Exception {
        eo.select(text); // NOI18N
        final int line = eo.getLineNumber();
        // toggle breakpoint via pop-up menu
        new ToggleBreakpointAction().perform(eo.txtEditorPane());
        // wait breakpoint established
        new Waiter(new Waitable() {
            public Object actionProduced(Object editorOper) {
                Object[] annotations = ((EditorOperator)editorOper).getAnnotations(line);
                for (int i = 0; i < annotations.length; i++) {
                    if("Breakpoint".equals(((EditorOperator)editorOper).getAnnotationType(annotations[i]))) { // NOI18N
                        return Boolean.TRUE;
                    }
                }
                return null;
            }
            public String getDescription() {
                return("Wait breakpoint established on line "+line); // NOI18N
            }
        }).waitAction(eo);
        return line;
    }
}
