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

package org.netbeans.modules.debugger.jpda.ui;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


/**
 * Panel for entering parameters of attaching to a remote VM.
 * If the debugger offers more
 * <A HREF="http://java.sun.com/j2se/1.3/docs/guide/jpda/jdi/com/sun/jdi/connect/connector.html">connectors</A>
 * then the panel contains also a combo-box for selecting a connector.
 *
 * @author Jan Jancura
 */
public class ConnectPanel extends JPanel implements 
Controller, ActionListener {

    /** List of all AttachingConnectors.*/
    private List                    connectors;
    /** Combo with list of all AttachingConnector names.*/
    private JComboBox               cbConnectors;
    /** List of JTextFields containing all parameters of curentConnector. */
    private JTextField[]            tfParams;


    public ConnectPanel () {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager ();
        connectors = new ArrayList ();
        connectors.addAll (vmm.attachingConnectors ());
        connectors.addAll (vmm.listeningConnectors ());
           
        // We temporary do not support these three connectors
        for (Iterator ci = connectors.iterator(); ci.hasNext(); ) {
            String name = ((Connector)ci.next()).name();
            int index = name.lastIndexOf('.');
            if (index >= 0)
                    name = name.substring(index + 1);
           
            if (name.equalsIgnoreCase("SACoreAttachingConnector") || 
                name.equalsIgnoreCase("SAPIDAttachingConnector") ||
                name.equalsIgnoreCase("SADebugServerAttachingConnector"))
                ci.remove();
        }
                
        if (connectors.size () == 0) {
            // no attaching connectors available => print message only
            add (new JLabel (
                NbBundle.getMessage (ConnectPanel.class, "CTL_No_Connector")
            ));
            return;
        }
        int defaultIndex = 0;
        if (connectors.size () > 1) {
            // more than one attaching connector available => 
            // init cbConnectors & selext default connector
            
            cbConnectors = new JComboBox ();
            cbConnectors.getAccessibleContext ().setAccessibleDescription (
                NbBundle.getMessage (ConnectPanel.class, "ACSD_CTL_Connector")
            );
            String lacn = Properties.getDefault ().getProperties ("debugger").
                getString ("last_attaching_connector", "");
            int i, k = connectors.size ();
            for (i = 0; i < k; i++) {
                Connector connector = (Connector) connectors.get (i);
                if ((lacn != null) && connector.name ().equals (lacn))
                    defaultIndex = i;
                int jj = connector.name ().lastIndexOf ('.');
                              
                String s = (jj < 0) ? 
                    connector.name () : 
                    connector.name ().substring (jj + 1);
                cbConnectors.addItem (
                    s + " (" + connector.description () + ")"
                );
            }
            cbConnectors.setActionCommand ("SwitchMe!");
            cbConnectors.addActionListener (this);
        }
        
        cbConnectors.setSelectedIndex (defaultIndex);
    }

    /**
     * Adds options for a selected connector type to this panel.
     */
    private void refresh (int index) {
        removeAll();
        
        Connector connector = (Connector) connectors.get (index);

        GridBagConstraints c;
        GridBagLayout layout = new GridBagLayout ();
        setLayout (layout);
        
        if (cbConnectors != null) { 
            // more than oneconnection => first line contains connector 
            // selector
                c = new GridBagConstraints ();
                c.insets = new Insets (0, 0, 3, 3);
                c.anchor = c.WEST;
                JLabel lblConnectors = new JLabel (
                    NbBundle.getMessage (ConnectPanel.class, "CTL_Connector") // NOI18N
                );
                lblConnectors.setDisplayedMnemonic (
                    NbBundle.getMessage (ConnectPanel.class, "MN_CTL_Connector").charAt (0) // NOI18N
                );
                lblConnectors.getAccessibleContext ().setAccessibleDescription (
                    NbBundle.getMessage (ConnectPanel.class, "ACSD_CTL_Connector")
                );
                lblConnectors.setLabelFor (cbConnectors);
                layout.setConstraints (lblConnectors, c);
            add (lblConnectors);
                c.insets = new Insets (0, 3, 3, 0);
                c.weightx = 1.0;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridwidth = GridBagConstraints.REMAINDER;
                layout.setConstraints (cbConnectors, c);
            add (cbConnectors);
        }
        
        // second line => transport
            c = new GridBagConstraints ();
            c.insets = new Insets (3, 0, 0, 6);
            c.anchor = c.WEST;
            JLabel lblTransport = new JLabel (
                NbBundle.getMessage (ConnectPanel.class, "CTL_Transport")
            );
            lblTransport.setDisplayedMnemonic (
                NbBundle.getMessage (ConnectPanel.class, 
                "CTL_Transport_Mnemonic").charAt(0)
            );
            lblTransport.getAccessibleContext ().setAccessibleDescription (
                NbBundle.getMessage (ConnectPanel.class, "ACSD_CTL_Transport")
            );
            layout.setConstraints (lblTransport, c);
        add (lblTransport);
            final JTextField tfTransport = new JTextField ();
            tfTransport.setEditable (false);
            lblTransport.setLabelFor (tfTransport);
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (3, 3, 0, 0);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            layout.setConstraints (tfTransport, c);
            Transport t = connector.transport();
            tfTransport.setText (t != null ? t.name() : "");
            tfTransport.getAccessibleContext ().setAccessibleDescription (
                NbBundle.getMessage (ConnectPanel.class, "ACSD_CTL_Transport")
            );
            tfTransport.addFocusListener (new FocusAdapter () {
                public void focusGained (FocusEvent evt) {
                    tfTransport.selectAll ();
                }
            });
        add (tfTransport);
        
        // other lines
        Map args = getSavedArgs (connector);
        tfParams = new JTextField [args.size ()];
        Iterator it = new TreeSet (args.keySet ()).iterator ();
        int i = 0;
        while (it.hasNext ()) {
            String name = (String) it.next ();
            Argument a = (Argument) args.get (name);
            String label = translate (a.label ());
            char mnemonic = getMnemonic (a.label ());
                c = new GridBagConstraints ();
                c.insets = new Insets (6, 0, 0, 3);
                c.anchor = GridBagConstraints.WEST;
                JLabel iLabel = new JLabel (label);
                iLabel.setDisplayedMnemonic (mnemonic);
                iLabel.setToolTipText (a.description ());
            add (iLabel, c);
                JTextField tfParam = new JTextField (a.value ());
                iLabel.setLabelFor (tfParam);
                tfParam.setName (name);
                tfParam.getAccessibleContext ().setAccessibleDescription (
                    new MessageFormat (NbBundle.getMessage (
                        ConnectPanel.class, "ACSD_CTL_Argument"
                    )).format (new Object[] { translate(a.label()) })
                ); 
                tfParam.setToolTipText (a.description ());
                c = new GridBagConstraints ();
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.insets = new Insets (6, 3, 0, 0);
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1.0;
            add (tfParam, c);
            tfParams [i ++] = tfParam;
        }
        
        //
        // Create an empty panel that resizes vertically so that
        // other elements have fix height:
            c = new GridBagConstraints ();
            c.weighty = 1.0;
            JPanel p = new JPanel ();
            p.setPreferredSize (new Dimension (1, 1));
        add (p, c);
    }

    /**
     * Refreshes panel with options corresponding to the selected connector type.
     * This method is called when a user selects new connector type.
     */
    public void actionPerformed (ActionEvent e) {
        refresh (((JComboBox) e.getSource ()).getSelectedIndex ());
        Component w = getParent ();
        while ( (w != null) && 
                !(w instanceof Window))
            w = w.getParent ();
        if (w != null) ((Window) w).pack (); // ugly hack...
        return;
    }
    
    public boolean cancel () {
        return true;
    }
    
    public boolean ok () {
        int index = cbConnectors.getSelectedIndex ();
        Connector connector = (Connector) connectors.get (index);
        Map args = getEditedArgs (tfParams, connector);
        if (args == null) return true; // CANCEL
        saveArgs (args, connector);
        
        if (connector instanceof AttachingConnector)
            DebuggerManager.getDebuggerManager ().startDebugging (
                DebuggerInfo.create (
                    AttachingDICookie.ID,
                    new Object [] {
                        AttachingDICookie.create (
                            (AttachingConnector) connector,
                            args
                        )
                    }
                )
            );
        else
        if (connector instanceof ListeningConnector)
            DebuggerManager.getDebuggerManager ().startDebugging (
                DebuggerInfo.create (
                    ListeningDICookie.ID,
                    new Object [] {
                        ListeningDICookie.create (
                            (ListeningConnector) connector,
                            args
                        )
                    }
                )
            );
        return true;
    }
    
    
    // private helper methods ..................................................
    
    /**
     * Is hostname unknown? This method resolves if the specified
     * string means &quot;hostname&nbsp;unknown&quot;.
     * Hostname is considered unknown if:
     * <UL>
     *     <LI>the hostname is <TT>null</TT>
     *     <LI>the hostname is an empty string
     *     <LI>the hostname starts with &quot;<I>x</I><TT>none</TT><I>x</I>&quot;
     *         or &quot;<I>x</I><TT>unknown</TT><I>x</I>&quot;, where <I>x</I>
     *         is any character except <TT>'a'-'z'</TT>, <TT>'A'-'Z'</TT>,
     *         <TT>'-'</TT>.
     * </UL>
     *
     * @param  hostname  hostname to be resolved
     * @return  <TT>true</TT> if the hostname is considered as unknown,
     *          <TT>false</TT> otherwise
     */
    private static boolean isUnknownHost (String hostname) {
        if (hostname == null) {
            return true;
        }
        int length = hostname.length();
        if (length == 0) {
            return true;
        }
        if (length < 6) {
            return false;
        }
        char firstChar = hostname.charAt(0);
        if (('a' <= firstChar && firstChar <= 'z')
            || ('A' <= firstChar && firstChar <= 'Z')
            || (firstChar == '-')) {
                return false;
        }
        char c;
        c = hostname.charAt(5);
        if (c == firstChar) {
            return hostname.substring(1, 5).equalsIgnoreCase("none");   //NOI18N
        }
        if (length < 9) {
            return false;
        }
        c = hostname.charAt(8);
        if (c == firstChar) {
            return hostname.substring(1, 8).equalsIgnoreCase("unknown");    //NOI18N
        }
        return false;
    }

//    private static String getLastAttachingConnectorName () {
//        JPDADebuggerProjectSettings settings = (JPDADebuggerProjectSettings) 
//            JPDADebuggerProjectSettings.findObject (
//                JPDADebuggerProjectSettings.class,
//                true
//            );
//        return settings.getLastConnector ();
//    }
    
    private static Map getSavedArgs (Connector connector) {
        // 1) get default set of args
        Map args = connector.defaultArguments ();

        // 2) load saved version of args
        Map savedArgs = Properties.getDefault ().getProperties ("debugger").
                getMap ("connection_settings", new HashMap ());
        savedArgs = (Map) savedArgs.get (connector.name ());
        if (savedArgs == null) return args;
        
        // 3) refres default args about saved values
        Iterator i = args.keySet ().iterator ();
        while (i.hasNext()) {
            String argName = (String) i.next ();
            String savedValue = (String) savedArgs.get (argName);
            if (savedValue != null)
                ((Argument) args.get (argName)).setValue (savedValue);
        }
        return args;
    }

    private static Map getEditedArgs (
        JTextField[]        tfParams, 
        Connector           connector
    ) {
        // 1) get default set of args
        Map args = connector.defaultArguments ();

        // 2) update values from text fields
        int i, k = tfParams.length;
        for (i = 0; i < k; i++) {
            JTextField tf = tfParams [i];
            String paramName = tf.getName ();
            String paramValue = tf.getText ();
            Argument a = (Argument) args.get (paramName);
            while ( ((!a.isValid (paramValue)) && (!paramValue.equals (""))) ||
                    ( paramValue.equals ("") && a.mustSpecify () )
            ) {
                NotifyDescriptor.InputLine in = null;
                if ( paramValue.equals ("") && a.mustSpecify ())
                    in = new NotifyDescriptor.InputLine (
                        translate(a.label ()),
                        NbBundle.getMessage (
                            ConnectPanel.class, 
                            "CTL_Required_value_title"
                        )
                    );
                else
                    in = new NotifyDescriptor.InputLine (
                        translate(a.label ()),
                        NbBundle.getMessage (
                            ConnectPanel.class, 
                            "CTL_Invalid_value_title"
                        )
                    );
                if (DialogDisplayer.getDefault ().notify (in) == 
                    NotifyDescriptor.CANCEL_OPTION
                ) return null;
                paramValue = in.getInputText ();
            }
            a.setValue (paramValue);
        }
        
        return args;
    }
    
    private static void saveArgs (
        Map                 args,
        Connector           connector
    ) {
        Map defaultValues = connector.defaultArguments ();
        Map argsToSave = new HashMap ();
        Iterator i = args.keySet ().iterator ();
        while (i.hasNext()) {
            String argName = (String) i.next ();
            Argument value = (Argument) args.get (argName);
            Argument defaultValue = (Argument) defaultValues.get (argName);
            if ( value != null &&
                 value != defaultValue &&
                 !value.equals (defaultValue)
            )
                argsToSave.put (argName, value.value ());
        }

        Map m = Properties.getDefault ().getProperties ("debugger").
            getMap ("connection_settings", new HashMap ());
        String name = connector.name ();
        m.put (name, argsToSave);
        Properties.getDefault ().getProperties ("debugger").
                setString ("last_attaching_connector", name);
        Properties.getDefault ().getProperties ("debugger").
                setMap ("connection_settings", m);
    }
    
    private static String translate (String str) {
        if (str.equalsIgnoreCase ("Host"))
            return NbBundle.getMessage (ConnectPanel.class, "CTL_Host");
        if (str.equalsIgnoreCase ("Port"))
            return NbBundle.getMessage (ConnectPanel.class, "CTL_Port");
        if (str.equalsIgnoreCase ("Java executable"))
            return NbBundle.getMessage (ConnectPanel.class, "CTL_ConnectorArgument_JavaExecutable");
        if (str.equalsIgnoreCase ("Corefile"))
            return NbBundle.getMessage (ConnectPanel.class, "CTL_ConnectorArgument_Corefile");
        if (str.equalsIgnoreCase ("Debug server"))
            return NbBundle.getMessage (ConnectPanel.class, "CTL_ConnectorArgument_DebugServer");
        if (str.equalsIgnoreCase ("PID"))
            return NbBundle.getMessage (ConnectPanel.class, "CTL_ConnectorArgument_PID");
        if (str.equalsIgnoreCase ("Name"))
            return NbBundle.getMessage (ConnectPanel.class, "CTL_ConnectorArgument_Name");
        if (str.equalsIgnoreCase ("Timeout"))
            return NbBundle.getMessage (ConnectPanel.class, "CTL_ConnectorArgument_Timeout");
        if (str.equalsIgnoreCase ("Local Address"))
            return NbBundle.getMessage (ConnectPanel.class, "CTL_ConnectorArgument_LocalAddress");
        return str;
    }
    
    private static char getMnemonic (String str) {
        if (str.equalsIgnoreCase ("Host"))
            return NbBundle.getMessage (ConnectPanel.class, 
                "CTL_Host_mnemonic").charAt (0);
        if (str.equalsIgnoreCase ("Port"))
            return NbBundle.getMessage (ConnectPanel.class, 
                "CTL_Port_mnemonic").charAt (0);
        if (str.equalsIgnoreCase("Java executable"))
            return NbBundle.getMessage(ConnectPanel.class, "CTL_ConnectorArgument_JavaExecutable_mnemonic").charAt(0);
        if (str.equalsIgnoreCase("Corefile"))
            return NbBundle.getMessage(ConnectPanel.class, "CTL_ConnectorArgument_Corefile_mnemonic").charAt(0);
        if (str.equalsIgnoreCase("Debug server"))
            return NbBundle.getMessage(ConnectPanel.class, "CTL_ConnectorArgument_DebugServer_mnemonic").charAt(0);
        if (str.equalsIgnoreCase("PID"))
            return NbBundle.getMessage(ConnectPanel.class, "CTL_ConnectorArgument_PID_mnemonic").charAt(0);
        if (str.equalsIgnoreCase("Name"))
            return NbBundle.getMessage(ConnectPanel.class, "CTL_ConnectorArgument_Name_mnemonic").charAt(0);
        if (str.equalsIgnoreCase("Timeout"))
            return NbBundle.getMessage(ConnectPanel.class, "CTL_ConnectorArgument_Timeout_mnemonic").charAt(0);
        if (str.equalsIgnoreCase("Local Address"))
            return NbBundle.getMessage(ConnectPanel.class, "CTL_ConnectorArgument_LocalAddress_mnemonic").charAt(0);
        return str.charAt (0);
    }
}

