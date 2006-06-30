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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunURIManager;
import org.netbeans.modules.j2ee.sun.ide.editors.AdminAuthenticator;
import org.netbeans.modules.j2ee.sun.ide.j2ee.RunTimeDDCatalog;
import org.netbeans.modules.j2ee.sun.ide.j2ee.db.ExecSupport;
import org.netbeans.modules.j2ee.sun.ide.j2ee.db.RegisterPointbase;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public final class AddDomainWizardIterator implements 
        WizardDescriptor.InstantiatingIterator,ChangeListener {

    private int index;

    private WizardDescriptor.Panel[] panels = null;
    
    final static String USER_NAME = "username";                                 //NOI18N
    final static String PASSWORD = "password";                                  //NOI18N
    final static String BLANK = "";                                             //NOI18N
    final static String HOST = "host";                                          //NOI18N
    final static String PORT = "port";                                          //NOI18N
    final static String CREATE_LOCALLY = "create_locally";                      //NOI18N
    final static String DOMAIN_FILE = "domain_file";                            //NOI18N
    final static String PLATFORM_LOCATION = "platform_location";                //NOI18N
    final static String INSTALL_LOCATION = "install_location";                  //NOI18N
    final static String DOMAIN = "domain";                                      //NOI18N
    final static String INSTANCE_PORT = "instance_port";                        //NOI18N
    final static String JMS_PORT = "jms_port";                                  //NOI18N
    final static String ORB_LISTENER_PORT = "orb_listener_port";                //NOI18N
    final static String ORB_SSL_PORT = "orb_ssl_port";                          //NOI18N
    final static String HTTP_SSL_PORT = "http_ssl_port";                        //NOI18N
    final static String ORB_MUTUAL_AUTH_PORT = "orb_mutual_auth_port";          //NOI18N
    final static String ADMIN_JMX_PORT = "admin_jmx_port";                      //NOI18N
    final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage";        // NOI18N 
    final static String TYPE = "type";                                          //NOI18N
    final static String PROP_DISPLAY_NAME = "ServInstWizard_displayName";       // NOI18N
    
    
    private AddDomainDefaultDomainPanel defaultPanel = 
            new  AddDomainDefaultDomainPanel();
    private AddDomainHostPortPanel hppanel = 
            new AddDomainHostPortPanel();
    private AddDomainDirectoryPanel domainDirPanel = 
            new AddDomainDirectoryPanel(false);
    private AddDomainDirectoryPanel personalDirPanel = 
            new AddDomainDirectoryPanel(true);
    private AddDomainPlatformPanel platformPanel = 
            new AddDomainPlatformPanel();
    private AddDomainNamePasswordPanel unamePanel = 
            new AddDomainNamePasswordPanel();
    private AddDomainPortsDefPanel portsPanel =
            new AddDomainPortsDefPanel();
    
    private WizardDescriptor.Panel[] defaultFlow = {
        platformPanel, /*defaultPanel,*/ unamePanel 
    };
    
    private WizardDescriptor.Panel[] remoteFlow = {
        platformPanel, hppanel, unamePanel
    };
    
    private WizardDescriptor.Panel[] localFlow = {
        platformPanel, domainDirPanel, unamePanel
    };

    private WizardDescriptor.Panel[] personalFlow = {
        platformPanel, personalDirPanel, unamePanel, portsPanel
    };
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            decoratePanels(remoteFlow);
            decoratePanels(localFlow);
            decoratePanels(personalFlow);
            decoratePanels(defaultFlow);
            platformPanel.addChangeListener(this);
            panels = defaultFlow;
        }
        return panels;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    //
    private transient Set/*<ChangeListener>*/ listeners = new HashSet/*<ChangeListener>*/(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator/*<ChangeListener>*/ it;
        synchronized (listeners) {
            it = new HashSet/*<ChangeListener>*/(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
//        System.out.println("WI fireChangeEvent on");
        while (it.hasNext()) {
            ChangeListener l = (ChangeListener) it.next();
//            System.out.println("       "+l);
            l.stateChanged(ev);
        }
    }

    public void stateChanged(ChangeEvent e) {
//        System.out.println("WI stateChanged");
        if (wizard.getProperty(TYPE) == REMOTE) { //  && panels != remoteFlow) {
            panels = remoteFlow;
            decoratePanels(remoteFlow);
            fireChangeEvent();
        } else if (wizard.getProperty(TYPE) == DEFAULT) { // && panels != defaultFlow) {
            panels = defaultFlow;
            decoratePanels(defaultFlow);
            fireChangeEvent();
        } else if (wizard.getProperty(TYPE) == LOCAL) { // && panels != localFlow) {
            panels = localFlow;
            decoratePanels(localFlow);
            fireChangeEvent();
        } else if (wizard.getProperty(TYPE) == PERSONAL) { // && panels != personalFlow) {
            panels = personalFlow;
            decoratePanels(personalFlow);
            fireChangeEvent();
        } else {
            System.out.println("THIS CANNOT BE TRUE!");
            panels = defaultFlow;
//            decoratePanels();
            fireChangeEvent();
        }
    }
    
    private void decoratePanels(WizardDescriptor.Panel[] pnls) {
        String[] steps = new String[pnls.length];
        for (int i = 0; i < pnls.length; i++) {
            Component c = pnls[i].getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                jc.putClientProperty("WizardPanel_contentSelectedIndex",        //NOI18N
                        new Integer(i));
                // Sets steps names for a panel
                jc.putClientProperty("WizardPanel_contentData", steps);         //NOI18N
                // Turn on subtitle creation on each step
                jc.putClientProperty("WizardPanel_autoWizardStyle",             //NOI18N
                        Boolean.TRUE);
                // Show steps on the left side with the image on the background
                jc.putClientProperty("WizardPanel_contentDisplayed",            //NOI18N 
                        Boolean.TRUE);
                // Turn on numbering of all steps
                jc.putClientProperty("WizardPanel_contentNumbered",             //NOI18N
                        Boolean.TRUE);
            }
        }
        
    }
    
    private void readObject(ObjectInputStream in) throws 
            IOException, ClassNotFoundException {
        in.defaultReadObject();
        listeners = new HashSet/*<ChangeListener>*/(1);
    }

    public void uninitialize(WizardDescriptor wizard) {
    }
    
    private WizardDescriptor  wizard;
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;       
    }
    
    public java.util.Set instantiate() {
        InstanceProperties ip = createInstance();
        Set result = new HashSet();
        if (ip != null)
            result.add(ip);
        
        return result;
    }

    private void queryForNameAndWord() {
        PasswordPanel pp = new PasswordPanel();
        pp.setPrompt(NbBundle.getMessage(AddDomainWizardIterator.class, 
                "PROMPT_USERNAME_PASSWORD"));                                   //NOI18N
        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor(pp, 
                NbBundle.getMessage(AddDomainWizardIterator.class, 
                "TITLE_USERNAME_PASSWORD"));                                    //NOI18N
        java.awt.Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.show();
        if (dd.getValue() != NotifyDescriptor.CANCEL_OPTION) {
            wizard.putProperty(USER_NAME,pp.getUsername());
            wizard.putProperty(PASSWORD,pp.getTPassword());
        } else {
            wizard.putProperty(USER_NAME,BLANK);
            wizard.putProperty(PASSWORD,BLANK);
        }
    }
    
    public InstanceProperties createInstance(){
        try {
            if(! isPresent((String) wizard.getProperty(HOST), (String) wizard.getProperty(PORT))){
                String uname = (String) wizard.getProperty(USER_NAME);
                if (null == uname)
                    uname = BLANK;
                String password = (String) wizard.getProperty(PASSWORD);
                if (null == password)
                    password = BLANK;
                if (wizard.getProperty(TYPE) == PERSONAL) {
                    // popup username password dialog here.
                    if (uname.trim().length() < 1 || 
                            password.trim().length() < 1)
                        queryForNameAndWord();
                    if (((String)wizard.getProperty(USER_NAME)).trim().length() < 1 || 
                            ((String) wizard.getProperty(PASSWORD)).trim().length() < 8) {
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(NbBundle.getMessage(AddDomainWizardIterator.class,
                                "ERR_Illegal_Values")));                        //NOI18N
                        return null;
                    }
                    CreateDomain cd = new CreateDomain();
                    cd.start();
                }
//                if (null != wizard.getProperty(DOMAIN_FILE)) {
//                    File dom = (File) wizard.getProperty(DOMAIN_FILE);
//                    File f = dom.getParentFile().getParentFile();
//                    String hp = getHostPort(wizard, (File) wizard.getProperty(DOMAIN_FILE));
//                    if (null == hp || hp.length() < 3) {
//                        NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(AddDomainWizardIterator.class, "Msg_InValidDomainDir",
//                                f.getAbsolutePath()), NotifyDescriptor.ERROR_MESSAGE);
//                        DialogDisplayer.getDefault().notify(d);
//                        return null;
//                    }
//                    String selectedItem = wizard.getProperty(HOST)+":"+wizard.getProperty(PORT);
                    // correct it and warn the user that the values were changed
                        /*if (!selectedItem.startsWith(hp)) {
                            NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(AddServerVisualPanel.class, "Msg_CorrectingLocation",
                                    f.getAbsolutePath()), NotifyDescriptor.INFORMATION_MESSAGE);
                            DialogDisplayer.getDefault().notify(d);
                            //socketField.setSelectedItem(hp);
                            String args[] = new String[3];
                            AddServerVisualPanel.parseHostPortDomain(hp, args);
                            wizard.putProperty(HOST,args[0]);
                            wizard.putProperty(PORT,args[1]);
                        }*/
//                }
                String domainDir = (String) wizard.getProperty(INSTALL_LOCATION);
                String displayName = 
                        (String)wizard.getProperty(PROP_DISPLAY_NAME);
                InstanceProperties instanceProperties = 
                        SunURIManager.createInstanceProperties(
                        (File) wizard.getProperty(PLATFORM_LOCATION), 
                        (String) wizard.getProperty(HOST),
                        (String)wizard.getProperty(PORT),
                        uname, password , displayName );
                instanceProperties.setProperty("httpportnumber",                //NOI18N 
                        (String) wizard.getProperty(PORT));
                instanceProperties.setProperty("DOMAIN",                        //NOI18N
                        (String) wizard.getProperty(DOMAIN));
                instanceProperties.setProperty("LOCATION",                      //NOI18N
                        domainDir);
                if (wizard.getProperty(TYPE) != REMOTE)
                    RegisterPointbase.getDefault().register((File) wizard.getProperty(PLATFORM_LOCATION));
                RunTimeDDCatalog.getRunTimeDDCatalog().refresh();
                wizard.putProperty(USER_NAME,BLANK);
                wizard.putProperty(PASSWORD,BLANK);
                return instanceProperties;
            }
            
        }catch (InstanceCreationException e){
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        e.getLocalizedMessage(),
                        NotifyDescriptor.INFORMATION_MESSAGE);
                d.setTitle(NbBundle.getMessage(AddDomainWizardIterator.class,
                        "LBL_RegServerFailed"));                                //NOI18N
                DialogDisplayer.getDefault().notify(d);
        }
        
        return null;
    }
    
    private boolean isPresent(String hostname, String port){
        boolean isPres = false;
        if(! isValidHost(hostname)){
            //don't call create instance
            return true;
        }
        String[] inst = InstanceProperties.getInstanceList();
        for(int i=0; i<inst.length; i++){
            String existHost = inst[i];
            if(existHost.indexOf(SunURIManager.SUNSERVERSURI) != -1){
                String existHostPort = existHost.substring(24, existHost.length());
                //Create host:port
                String inHostPort = hostname + ":" + port;                      //NOI18N
                //String get Port Value from ExistingList
                String existPort = 
                        existHost.substring(existHost.lastIndexOf(":")+1,       //NOI18N 
                        existHost.length()); //NOI18N
                //String get Host Value from ExistingList
                existHost = existHost.substring(24, existHost.lastIndexOf(":")); //NOI18N
                if(existHostPort.equals(inHostPort)){
                    showExistsMessage(hostname);
                    return true;
                }else{
                    try{
                        if(existHost.equals("localhost")){                      //NOI18N
                            String localCanonName = 
                                    InetAddress.getLocalHost().getCanonicalHostName();
                            String currentCanonName = 
                                    InetAddress.getByName(hostname).getCanonicalHostName();
                            if(localCanonName.equals(currentCanonName)){
                                if(existPort.equals(port)){
                                    showExistsMessage(hostname);
                                    return true;
                                }
                            }
                        }else{                           
                            String existCanonName = 
                                    InetAddress.getByName(existHost).getCanonicalHostName();
                            String currentCanonName = 
                                    InetAddress.getByName(hostname).getCanonicalHostName();
                            if(existCanonName.equals(currentCanonName)){
                                if(existPort.equals(port)){
                                    showExistsMessage(hostname);
                                    return true;
                                }
                            }
                        }
                    }catch(Exception ex){
                        //suppress exception - allowing creation to be attempted.
                        return false;
                    }
                }//else
            }//Sun instances
        }
        return isPres;
    }
    
    private boolean isValidHost(String hostname){
        try{
            InetAddress ia = InetAddress.getByName(hostname);
            return true;
        }catch(java.net.UnknownHostException ex){
            String mess = MessageFormat.format(
                    NbBundle.getMessage(AddDomainWizardIterator.class, 
                    "MSG_UnknownHost"), new Object[]{hostname});                //NOI18N
            Util.showInformation(mess, 
                    NbBundle.getMessage(AddDomainWizardIterator.class, 
                    "LBL_UnknownHost"));                                        //NOI18N
            return false;
        }
    }
    
    private void showExistsMessage(String hostname){
        String mess = MessageFormat.format(NbBundle.getMessage(
                AddDomainWizardIterator.class, "MSG_RegServerDuplicate"),     //NOI18N
                new Object[]{hostname});
        Util.showInformation(mess, NbBundle.getMessage(
                AddDomainWizardIterator.class, "LBL_RegServerFailed"));       //NOI18N
    }
    
    static String getHostPort(WizardDescriptor wiz, File domainXml){
        String adminHostPort = null;
        try{
            Class[] argClass = new Class[1];
            argClass[0] = File.class;
            Object[] argObject = new Object[1];
            argObject[0] = domainXml;
            
	    ClassLoader loader = 
                    ServerLocationManager.getServerOnlyClassLoader((File)wiz.getProperty(PLATFORM_LOCATION));
            if(loader != null){
                Class cc = loader.loadClass("org.netbeans.modules.j2ee.sun.bridge.AppServerBridge"); //NOI18N
                java.lang.reflect.Method getHostPort = cc.getMethod("getHostPort", argClass);//NOI18N
                adminHostPort = (String)getHostPort.invoke(null, argObject);
            }
        }catch(Exception ex){
            //Suppressing exception while trying to obtain admin host port value
        }
        return adminHostPort;
    }
    
    private class CreateDomain extends Thread {

        
        public void run() {
            Process process = null;
            // attempt to do the domian/instance create HERE
            File irf = (File) wizard.getProperty(PLATFORM_LOCATION);
            if (null == irf || !irf.exists()) {
                return;
            }
            String installRoot = irf.getAbsolutePath();
            String asadminCmd = installRoot + File.separator +
                    "bin" +                                                     //NOI18N
                    File.separator +
                    "asadmin";                                                  //NOI18N
            
            if (File.separator.equals("\\")) {                                  //NOI18N
                asadminCmd = asadminCmd + ".bat";                               //NOI18N
            }
            String domain = (String) wizard.getProperty(DOMAIN);
            String domainDir = (String) wizard.getProperty(INSTALL_LOCATION);
            String arrnd[] = new String[] { asadminCmd,
                    "create-domain",                                            //NOI18N
                    "--domaindir",                                              //NOI18N
                    domainDir,
                    "--adminport",                                              //NOI18N
                    (String) wizard.getProperty(PORT),
                    "--adminuser",                                              //NOI18N
                    (String) wizard.getProperty(USER_NAME),
                    "--adminpassword",                                          //NOI18N
                    (String) wizard.getProperty(PASSWORD),
                    "--instanceport",                                           //NOI18N
                    (String) wizard.getProperty(INSTANCE_PORT),
                    "--domainproperties",                                       //NOI18N
                    "jms.port="+                                                //NOI18N
                    ((String)wizard.getProperty(JMS_PORT)).trim()+
                    ":orb.listener.port="+                                      //NOI18N
                    ((String)wizard.getProperty(ORB_LISTENER_PORT)).trim()+
                    ":http.ssl.port="+                                          //NOI18N
                    ((String)wizard.getProperty(HTTP_SSL_PORT)).trim()+
                    ":orb.ssl.port="+                                           //NOI18N
                    ((String)wizard.getProperty(ORB_SSL_PORT)).trim()+
                    ":orb.mutualauth.port="+                                    //NOI18N
                    ((String)wizard.getProperty(ORB_MUTUAL_AUTH_PORT)).trim()+
                    ":domain.jmxPort="+                                         //NOI18N
                    ((String)wizard.getProperty(ADMIN_JMX_PORT)).trim(),
                    domain
            };
            try {
                
                ExecSupport ee= new ExecSupport();
                process= Runtime.getRuntime().exec(arrnd);
                ee.displayProcessOutputs(process,
                        NbBundle.getMessage(this.getClass(), "LBL_outputtab")); //NOI18N
                
            } catch (Exception e) {
                Util.showInformation(e.getLocalizedMessage());
            }
            int retVal = 0;
            if (null != process)
                try {
                    retVal = process.waitFor();
                } catch (InterruptedException ie) {
                    retVal = -1;
                }
            if (0 != retVal) {
                Util.showError(NbBundle.getMessage(this.getClass(),
                        "WARN_DELETE_INSTANCE",                                 //NOI18N                      
                        (String)wizard.getProperty(PROP_DISPLAY_NAME)), 
                        NbBundle.getMessage(this.getClass(),
                        "WARN_DELETE_INSTANCE_TITLE"));                         //NOI18N
            }
            
        }
        
    }
    
    static class PasswordPanel extends javax.swing.JPanel {
        
        private static final int DEFAULT_WIDTH = 200;
        private static final int DEFAULT_HEIGHT = 0;
        
        /** Generated serialVersionUID */
        static final long serialVersionUID = 1555749205340031767L;
        
        java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(AdminAuthenticator.class);
        
        /** Creates new form PasswordPanel */
        public PasswordPanel() {
            initComponents();
            
            usernameField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_UserNameField"));
            passwordField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_PasswordField"));
        }
        
        public java.awt.Dimension getPreferredSize() {
            java.awt.Dimension sup = super.getPreferredSize();
            return new java.awt.Dimension( Math.max(sup.width, DEFAULT_WIDTH), Math.max(sup.height, DEFAULT_HEIGHT ));
        }
        
        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the FormEditor.
         */
        private void initComponents() {
            setLayout(new java.awt.BorderLayout());
            
            mainPanel = new javax.swing.JPanel();
            mainPanel.setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gridBagConstraints1;
            mainPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
            
            promptLabel = new javax.swing.JLabel();
            promptLabel.setHorizontalAlignment(0);
            
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 6, 0);
            mainPanel.add(promptLabel, gridBagConstraints1);
            
            jLabel1 = new javax.swing.JLabel();
            jLabel1.setText(bundle.getString("LAB_AUTH_User_Name"));
            jLabel1.setDisplayedMnemonic(bundle.getString("LAB_AUTH_User_Name_Mnemonic").charAt(0));
            
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 12);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            mainPanel.add(jLabel1, gridBagConstraints1);
            
            usernameField = new javax.swing.JTextField();
            usernameField.setMinimumSize(new java.awt.Dimension(70, 20));
            usernameField.setPreferredSize(new java.awt.Dimension(70, 20));
            jLabel1.setLabelFor(usernameField);
            
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 0);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.weightx = 1.0;
            mainPanel.add(usernameField, gridBagConstraints1);
            
            jLabel2 = new javax.swing.JLabel();
            jLabel2.setText(bundle.getString("LAB_AUTH_Password"));
            jLabel2.setDisplayedMnemonic(bundle.getString("LAB_AUTH_Password_Mnemonic").charAt(0));
            
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 0, 12);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            mainPanel.add(jLabel2, gridBagConstraints1);
            
            passwordField = new javax.swing.JPasswordField();
            passwordField.setMinimumSize(new java.awt.Dimension(70, 20));
            passwordField.setPreferredSize(new java.awt.Dimension(70, 20));
            jLabel2.setLabelFor(passwordField);
            
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.weightx = 1.0;
            mainPanel.add(passwordField, gridBagConstraints1);
            
            add(mainPanel, "Center"); // NOI18N
            
        }
        
        // Variables declaration - do not modify                     
        private javax.swing.JPanel mainPanel;
        private javax.swing.JLabel promptLabel;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JTextField usernameField;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JPasswordField passwordField;
        // End of variables declaration                   

        String getUsername( ) {
            return usernameField.getText();
        }

        char[] getPassword( ) {
            return passwordField.getPassword();
        }

        String getTPassword( ) {
            return new String(passwordField.getPassword());
        }

        void setPrompt( String prompt ) {
            if ( prompt == null ) {
                promptLabel.setVisible( false );
                getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_NbAuthenticatorPasswordPanel"));
            }
            else {
                promptLabel.setVisible( true );
                promptLabel.setText( prompt );
                getAccessibleContext().setAccessibleDescription(prompt);
            }
        }
    }
    
    static InstanceType PERSONAL = new InstanceType();
    static InstanceType REMOTE = new InstanceType();
    static InstanceType LOCAL = new InstanceType();
    static InstanceType DEFAULT = new InstanceType();
    
    
    static class InstanceType {
    }
}

