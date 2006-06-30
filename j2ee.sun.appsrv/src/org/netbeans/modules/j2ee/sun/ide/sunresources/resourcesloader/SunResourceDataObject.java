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
package org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader;

import java.io.InputStream;
import org.xml.sax.InputSource;

import org.openide.filesystems.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;

import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;

import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.*;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.util.WeakListeners;

/** Represents a SunResource object in the Repository.
 *
 * @author nityad
 */
public class SunResourceDataObject extends XMLDataObject implements FileChangeListener { // extends MultiDataObject{
    private static String JDBC_CP = "jdbc-connection-pool"; //NOI18N
    private static String JDBC_DS = "jdbc-resource"; //NOI18N
    private static String PMF = "persistence-manager-factory-resource"; //NOI18N
    private static String MAIL = "mail-resource"; //NOI18N
    private static String JMS = "jms-resource"; //NOI18N

    private ValidateXMLCookie validateCookie = null;
    private CheckXMLCookie checkCookie = null;
    
    ConnPoolBean cpBean = null;
    DataSourceBean dsBean = null;
    PersistenceManagerBean pmfBean = null;
    JavaMailSessionBean mailBean = null;
    JMSBean jmsBean = null;
    
    String resType;
    
    public SunResourceDataObject(FileObject pf, SunResourceDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        pf.addFileChangeListener((FileChangeListener) WeakListeners.create(FileChangeListener.class, this, pf));
        
        resType = getResource(pf);
//        init(pf);
    }
    
//    private void init(FileObject pf) {
//        CookieSet cookies = getCookieSet();
        // Add whatever capabilities you need, e.g.:
        /*
        cookies.add(new ExecSupport(getPrimaryEntry()));
        // See Editor Support template in Editor API:
        cookies.add(new SunResourceEditorSupport(this));
        cookies.add(new CompilerSupport.Compile(getPrimaryEntry()));
        cookies.add(new CompilerSupport.Build(getPrimaryEntry()));
        cookies.add(new CompilerSupport.Clean(getPrimaryEntry()));
        cookies.add(new OpenCookie() {
            public void open() {
                // do something...but usually you want to use OpenSupport instead
            }
        });
         */
//    }
    
    public org.openide.nodes.Node.Cookie getCookie(Class c) {
        Node.Cookie retValue = null;
        if (ValidateXMLCookie.class.isAssignableFrom(c)) {
            if (validateCookie == null) {
                InputSource in = DataObjectAdapters.inputSource(this);
                validateCookie = new ValidateXMLSupport(in);
            }
            return validateCookie;
        } else if (CheckXMLCookie.class.isAssignableFrom(c)) {
            if (checkCookie == null) {
                InputSource in = DataObjectAdapters.inputSource(this);
                checkCookie = new CheckXMLSupport(in);
            }
            return checkCookie;
        }
        
        if (retValue == null) {
            retValue = super.getCookie(c);
        }
        return retValue;
    }
    
    
    public HelpCtx getHelpCtx() {
        return null; // HelpCtx.DEFAULT_HELP;
        // If you add context help, change to:
        // return new HelpCtx(SunResourceDataObject.class);
    }
    
    protected Node createNodeDelegate() {
        if(resType != null){
            if(this.resType.equals(this.JDBC_CP)){
                Node node = new ConnPoolBeanDataNode(this, getPool());
                return node;
            }if(this.resType.equals(this.JDBC_DS)){
                Node node = new DataSourceBeanDataNode(this, getDataSource());
                return node;
            }if(this.resType.equals(this.PMF)){
                Node node = new PersistenceManagerBeanDataNode(this, getPersistenceManager());
                return node;
            }if(this.resType.equals(this.MAIL)){
                Node node = new JavaMailSessionBeanDataNode(this, getMailSession());
                return node;
            }if(this.resType.equals(this.JMS)){    
                Node node = new JMSBeanDataNode(this, getJMS());
                return node;
            }else{
                String mess = NbBundle.getMessage(SunResourceDataObject.class, "Info_notSunResource"); //NOI18N
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, mess); 
                return new SunResourceDataNode(this);
            }    
        }else{
            return new SunResourceDataNode(this);
        }   
    }
    
    private String getResource(FileObject primaryFile) {
       String type = null;
       try {
            if((! primaryFile.isFolder()) && primaryFile.isValid()){
                InputStream in = primaryFile.getInputStream();
                Resources resources = DDProvider.getDefault().getResourcesGraph(in);
                
                // identify JDBC Connection Pool xml
                JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
                if(pools.length != 0){
                    ConnPoolBean currCPBean = ConnPoolBean.createBean(pools[0]);
                    type = this.JDBC_CP;
                    setPool(currCPBean);
                    return type;
                }  
                
                // identify JDBC Resources xml
                JdbcResource[] dataSources = resources.getJdbcResource();
                if(dataSources.length != 0){
                    DataSourceBean currDSBean = DataSourceBean.createBean(dataSources[0]);
                    type = this.JDBC_DS;
                    setDataSource(currDSBean);
                    return type;
                }
                
                // import Persistence Manager Factory Resources
                PersistenceManagerFactoryResource[] pmfResources = resources.getPersistenceManagerFactoryResource();
                if(pmfResources.length != 0){
                    PersistenceManagerBean currPMFBean = PersistenceManagerBean.createBean(pmfResources[0]);
                    type = this.PMF;
                    setPersistenceManager(currPMFBean);
                    return type;
                }
                
                // import Mail Resources
                MailResource[] mailResources = resources.getMailResource();
                if(mailResources.length != 0){
                    JavaMailSessionBean currMailBean = JavaMailSessionBean.createBean(mailResources[0]);
                    type = this.MAIL;
                    setMailSession(currMailBean);
                    return type;
                }
                
                // import Java Message Service Resources
                JmsResource[] jmsResources = resources.getJmsResource();
                if(jmsResources.length != 0){
                    JMSBean jmsBean = JMSBean.createBean(jmsResources[0]);
                    type = this.JMS;
                    setJMS(jmsBean);
                    return type;
                }
                
                return type;
            }else
                return type;
        }catch(NullPointerException npe){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, npe);
            return type;
        }catch(Exception ex){
            //ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ex.getLocalizedMessage());
            return type;
        }
       
    }
    
    private void setPool(ConnPoolBean in_cpBean){
        this.cpBean = in_cpBean;
    }
    
    private ConnPoolBean getPool(){
        return this.cpBean;
    }
    
    private void setDataSource(DataSourceBean in_dsBean){
        this.dsBean = in_dsBean;
    }
    
    private DataSourceBean getDataSource(){
        return this.dsBean;
    }
    
    private void setPersistenceManager(PersistenceManagerBean in_pmfBean){
        this.pmfBean = in_pmfBean;
    }
    
    private PersistenceManagerBean getPersistenceManager(){
        return this.pmfBean;
    }
    
    private void setMailSession(JavaMailSessionBean in_mailBean){
        this.mailBean = in_mailBean;
    }
    
    private JavaMailSessionBean getMailSession(){
        return this.mailBean;
    }
    
    private void setJMS(JMSBean in_jmsBean){
        this.jmsBean = in_jmsBean;
    }
    
    private JMSBean getJMS(){
        return this.jmsBean;
    }
    
    public void fileAttributeChanged (FileAttributeEvent fe) {
        updateDataObject();
    }
    
    public void fileChanged (FileEvent fe) {
        updateDataObject();
    }
    
    public void fileDataCreated (FileEvent fe) {
        updateDataObject ();
    }
    
    public void fileDeleted (FileEvent fe) {
        updateDataObject ();
    }
    
    public void fileFolderCreated (FileEvent fe) {
        updateDataObject ();
    }
    
    public void fileRenamed (FileRenameEvent fe) {
        updateDataObject ();
    }
    
    private void updateDataObject(){
        resType = getResource(this.getPrimaryFile());       
    }
    
    public String getResourceType(){
        return resType;
    }
    // If you made an Editor Support you will want to add these methods:
     
    /*public final void addSaveCookie(SaveCookie save) {
        getCookieSet().add(save);
    }
     
    public final void removeSaveCookie(SaveCookie save) {
        getCookieSet().remove(save);
    }*/
  
}
