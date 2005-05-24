/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
import org.openide.util.HelpCtx;

import java.io.InputStream;

import org.netbeans.modules.j2ee.sun.share.dd.resources.*;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.*;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;

/** Represents a SunResource object in the Repository.
 *
 * @author nityad
 */
public class SunResourceDataObject extends MultiDataObject{
    private static String JDBC_CP = "ConnectionPool"; //NOI18N
    private static String JDBC_DS = "DataSource"; //NOI18N
    private static String PMF = "PersistenceManager"; //NOI18N
    private static String MAIL = "MailSession"; //NOI18N
    private static String JMS = "JMS"; //NOI18N
    
    ConnPoolBean cpBean = null;
    DataSourceBean dsBean = null;
    PersistenceManagerBean pmfBean = null;
    JavaMailSessionBean mailBean = null;
    JMSBean jmsBean = null;
    
    String resType;
    //private org.openide.filesystems.FileChangeListener fl;
    
    public SunResourceDataObject(FileObject pf, SunResourceDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        /*fl = new org.openide.filesystems.FileChangeAdapter(){
                public void fileChanged(org.openide.filesystems.FileEvent evt){
                    
                }
        };
        pf.addFileChangeListener(org.openide.util.WeakListener.fileChange(fl,pf));*/
        
        resType = getResource(pf);
        init(pf);
    }
    
    private void init(FileObject pf) {
        CookieSet cookies = getCookieSet();
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
                node.setValue(WizardConstants.__ResourceType, WizardConstants.__JdbcConnectionPool);
                return node;
            }if(this.resType.equals(this.JDBC_DS)){
                Node node = new DataSourceBeanDataNode(this, getDataSource());
                node.setValue(WizardConstants.__ResourceType, WizardConstants.__JdbcResource);
                return node;
            }if(this.resType.equals(this.PMF)){
                Node node = new PersistenceManagerBeanDataNode(this, getPersistenceManager());
                node.setValue(WizardConstants.__ResourceType, WizardConstants.__PersistenceManagerFactoryResource);
                return node;
            }if(this.resType.equals(this.MAIL)){
                Node node = new JavaMailSessionBeanDataNode(this, getMailSession());
                node.setValue(WizardConstants.__ResourceType, WizardConstants.__MailResource);
                return node;
            }if(this.resType.equals(this.JMS)){    
                Node node = new JMSBeanDataNode(this, getJMS());
                node.setValue(WizardConstants.__ResourceType, WizardConstants.__JmsResource);
                return node;
            }else{
                System.out.println("File with sun-resource not matching defined types ");
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
                Resources resources = Resources.createGraph(in);
                
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
            System.out.println("Null Pointer Exception ex");
            return type;
        }catch(Exception ex){
            System.out.println("Other exceptions  ");
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
    
    // If you made an Editor Support you will want to add these methods:
     
    /*public final void addSaveCookie(SaveCookie save) {
        getCookieSet().add(save);
    }
     
    public final void removeSaveCookie(SaveCookie save) {
        getCookieSet().remove(save);
    }*/
  
}
