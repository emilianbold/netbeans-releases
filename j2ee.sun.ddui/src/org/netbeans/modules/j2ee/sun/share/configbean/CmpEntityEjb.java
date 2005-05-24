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

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.model.DDBean;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.FlushAtEndOfMethod;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Cmp;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.OneOneFinders;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PrefetchDisabled;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod;

import com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMappings;
import com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMapping;
import com.sun.jdo.api.persistence.mapping.ejb.beans.EntityMapping;
import com.sun.jdo.api.persistence.mapping.ejb.beans.CmpFieldMapping;
import com.sun.jdo.api.persistence.mapping.ejb.beans.Consistency;

import org.netbeans.modules.j2ee.sun.share.configbean.ConfigQuery;


/**
 *
 * @author  vkraemer
 */
public class CmpEntityEjb extends EntityEjb {
    
	/** Holds value of property cmp. */
	private Cmp cmp;

    /** Holds value of property flush-at-end-of-method. */
    private FlushAtEndOfMethod flushAtEndOfMethod;

    /** Holds value of property schema. */
    private String schema;
    
    /** Holds value of property tableName. */
    private String tableName;
    
    /** Holds value of property consistency. */
    private String consistency;
    
    /** Holds value of property secondaryTables. */

    /** Holds value of property beanName. */
    //private String beanName;
    
    /** Creates a new instance of SunONECmpEntityDConfigBean */
        public CmpEntityEjb() {
		addPropertyChangeListener(new PropertyChangeListener() {
			public void  propertyChange(PropertyChangeEvent ev) {
// FIXME what is this listener for?
//				cmpDDSnippet = null;
			}
		});
	}

    
    /** Getter for property schema.
     * @return Value of property schema.
     *
     */
    public String getSchema() {
        return this.schema;
    }
    
    /** Setter for property schema.
     * @param schema New value of property schema.
     *
     * @throws PropertyVetoException
     *
     */
    public void setSchema(String schema) throws java.beans.PropertyVetoException {
        String oldSchema = this.schema;
        getVCS().fireVetoableChange("schema", oldSchema, schema);
        this.schema = schema;
        getPCS().firePropertyChange("schema", oldSchema, schema);
    }
    
    /** Getter for property tableName.
     * @return Value of property tableName.
     *
     */
    public String getTableName() {
        return this.tableName;
    }
    
    /** Setter for property tableName.
     * @param tableName New value of property tableName.
     *
     * @throws PropertyVetoException
     *
     */
    public void setTableName(String tableName) throws java.beans.PropertyVetoException {
        String oldTableName = this.tableName;
        getVCS().fireVetoableChange("tableName", oldTableName, tableName);
        this.tableName = tableName;
        getPCS().firePropertyChange("tableName", oldTableName, tableName);
    }
    
    /** Getter for property consistency.
     * @return Value of property consistency.
     *
     */
    public String getConsistency() {
        return this.consistency;
    }
    
    /** Setter for property consistency.
     * @param consistency New value of property consistency.
     *
     * @throws PropertyVetoException
     *
     */
    public void setConsistency(String consistency) throws java.beans.PropertyVetoException {
        String oldConsistency = this.consistency;
        getVCS().fireVetoableChange("consistency", oldConsistency, consistency);
        this.consistency = consistency;
        getPCS().firePropertyChange("consistency", oldConsistency, consistency);
    }
    

    

    

    



    /** Getter for property beanName.
     * @return Value of property beanName.
     *
     */
    public String getBeanName() {
        return cleanDDBeanText(getDDBean());
    }
    
    /* ------------------------------------------------------------------------
     * Persistence support.  Loads DConfigBeans from previously saved Deployment
     * plan file.
     */
    protected class CmpEntityEjbSnippet extends EntityEjb.EntityEjbSnippet {
        public CommonDDBean getDDSnippet() {
            Ejb ejb = (Ejb) super.getDDSnippet();

            if(null != cmp){
                ejb.setCmp((Cmp) cmp.clone());
            }

            if(null != flushAtEndOfMethod){
                try{
                    ejb.setFlushAtEndOfMethod((FlushAtEndOfMethod)flushAtEndOfMethod.clone());
                }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException e){
                    //System.out.println("Not Supported Version");      //NOI18N
                }
            }

            return ejb;
        }

        public boolean hasDDSnippet() {
            if(super.hasDDSnippet()){
                return true;
            }

            if(null != cmp){
                return true;
            }

            if(null != flushAtEndOfMethod){
                return true;
            }
            return false;
        }
    }

    java.util.Collection getSnippets() {
        Collection snippets = new ArrayList();
        snippets.add(new CmpEntityEjbSnippet());

        // FIXME create snippet for sun-cmp-mappings.xml here as well.

        return snippets;
    }


    protected void loadEjbProperties(Ejb savedEjb) {
            super.loadEjbProperties(savedEjb);
        Cmp cmp = savedEjb.getCmp();
        if(null != cmp){
            this.cmp = cmp;
        }

        FlushAtEndOfMethod flushAtEndOfMethod = null;
        try{
            flushAtEndOfMethod = savedEjb.getFlushAtEndOfMethod();
        }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException e){
            //System.out.println("Not Supported Version");      //NOI18N
        }

        if(null != flushAtEndOfMethod){
            this.flushAtEndOfMethod = flushAtEndOfMethod;
        }
    }

	
	/* ------------------------------------------------------------------------
	 * XPath to Factory mapping support
	 */
/*
	private HashMap cmpEjbFactoryMap;
	
	protected Map getXPathToFactoryMap() {
		if(cmpEjbFactoryMap == null) {
			cmpEjbFactoryMap = (HashMap) super.getXPathToFactoryMap();

			// FIXME are there any CMP specific child DConfigBeans?  Their xpath
			//   factory mappings would go here if there are.
		}
		
		return cmpEjbFactoryMap;
	}
 */
        
	/** Getter for property cmp.
	 * @return Value of property cmp.
	 *
	 */
	public Cmp getCmp() {
		return this.cmp;
	}


        /** Getter for property flushAtEndOfMethod.
	 * @return Value of property flushAtEndOfMethod.
	 */
	public FlushAtEndOfMethod getFlushAtEndOfMethod() {
		return this.flushAtEndOfMethod;
	}


	/** Setter for property cmp.
	 * @param cmp New value of property cmp.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setCmp(Cmp cmp) throws java.beans.PropertyVetoException {
		Cmp oldCmp = this.cmp;
		getVCS().fireVetoableChange("cmp", oldCmp, cmp);
		this.cmp = cmp;
		getPCS().firePropertyChange("cmp", oldCmp, cmp);
	}

        
	/** Setter for property flushAtEndOfMethod.
	 * @param flushAtEndOfMethod New value of property flushAtEndOfMethod.
	 *
	 * @throws PropertyVetoException
	 */
	public void setFlushAtEndOfMethod(FlushAtEndOfMethod flushAtEndOfMethod) throws java.beans.PropertyVetoException {
		FlushAtEndOfMethod oldFlushAtEndOfMethod = this.flushAtEndOfMethod;
		getVCS().fireVetoableChange("flushAtEndOfMethod", oldFlushAtEndOfMethod, flushAtEndOfMethod);        //NOI18N
		this.flushAtEndOfMethod = flushAtEndOfMethod;
		getPCS().firePropertyChange("flush at end  of method", oldFlushAtEndOfMethod, flushAtEndOfMethod);   //NOI18N
	}


	//methods called by the customizer model
	public void addFinder(Finder finder){
		if(null == cmp){
			cmp = StorageBeanFactory.getDefault().createCmp();
		}
		OneOneFinders oneOneFinders = cmp.getOneOneFinders();
		if(null == oneOneFinders){
			oneOneFinders = cmp.newOneOneFinders();
			cmp.setOneOneFinders(oneOneFinders);
		}
		oneOneFinders.addFinder(finder);
	}


        public void addMethod(Method method){
/*            
            System.out.println("CmpEntityEjb addMethod ddMethod:" + method);                             //NOI18N
            System.out.println("CmpEntityEjb addMethod name :" + method.getMethodName() );               //NOI18N
            System.out.println("CmpEntityEjb addMethod interface :" + method.getMethodIntf() );          //NOI18N   
            System.out.println("CmpEntityEjb addMethod ejb name :" + method.getEjbName() );              //NOI18N
            System.out.println("CmpEntityEjb addMethod params :" + method.getMethodParams() );           //NOI18N   
*/
            if(null == flushAtEndOfMethod){
                flushAtEndOfMethod = StorageBeanFactory.getDefault().createFlushAtEndOfMethod();
            }
            flushAtEndOfMethod.addMethod(method);
	}


        public void addQueryMethod(QueryMethod queryMethod){
            try{
                if(null == cmp){
                    cmp = StorageBeanFactory.getDefault().createCmp();
                }

                PrefetchDisabled prefetchDisabled = cmp.getPrefetchDisabled();
                if(null == prefetchDisabled){
                    prefetchDisabled = cmp.newPrefetchDisabled();
                    cmp.setPrefetchDisabled(prefetchDisabled);
                }
                prefetchDisabled.addQueryMethod(queryMethod);
            }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException ex){
                //System.out.println("Not Supported Version");      //NOI18N
            }
	}


	public void removeFinder(Finder finder){
		if(null != cmp){
			OneOneFinders oneOneFinders = cmp.getOneOneFinders();
			if(null != oneOneFinders){
				oneOneFinders.removeFinder(finder);
			}
                    try{
                        if(oneOneFinders.sizeFinder() < 1){
                            setCmp(null);
                        }
                    }catch(java.beans.PropertyVetoException ex){
                    }
		}
	}


	public void removeMethod(Method method){
		if(null != flushAtEndOfMethod){
                        flushAtEndOfMethod.removeMethod(method);
		}
	}


	public void removeQueryMethod(QueryMethod queryMethod){
            try{
                if(null != cmp){
                    PrefetchDisabled prefetchDisabled = cmp.getPrefetchDisabled();
                    if(null != prefetchDisabled){
                        prefetchDisabled.removeQueryMethod(queryMethod);
                    }
                }
            }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException ex){
                //System.out.println("Not Supported Version");      //NOI18N
            }
	}


        //List of all the finder methods of cmp bean
        public List getFinderMethods(){
            ArrayList methods = new ArrayList();
            DDBean ddBean = getDDBean();

            //xpath - ejb-jar/enterprise-beans/entity
            DDBean[] childBeans = ddBean.getChildBean("./query");            //NOI18N
            ConfigQuery.MethodData methodData = null;
            DDBean queryMethods[];
            DDBean queryMethod;
            DDBean methodNameBean;
            String methodName;
            DDBean methodParams;
            DDBean methodParam[];
            for(int i=0; i<childBeans.length; i++){
                queryMethods = childBeans[i].getChildBean("./query-method"); //NOI18N
                if(queryMethods.length > 0){
                    queryMethod = queryMethods[0]; 
                    methodNameBean = queryMethod.getChildBean("./method-name")[0]; //NOI18N
                    methodName = methodNameBean.getText();
                    if((methodName != null) && (methodName.length() > 0)){
                        methodParams = queryMethod.getChildBean("./method-params")[0]; //NOI18N
                        methodParam = methodParams.getChildBean("./method-param"); //NOI18N
                        ArrayList params = new ArrayList();
                        if(methodParam != null){
                           for(int j=0; j<methodParam.length; j++){
                               params.add(methodParam[j].getText());
                           } 
                        }
                        methodData = new ConfigQuery.MethodData(methodName, params);
                    }
                }
                methods.add((Object)methodData);
            }
            return methods;
        }


        //List of all the QueryMethod elements(elements from DD)
        public List getPrefetchedMethods(){
            List prefetchedMethodList = new ArrayList();

            try{
                if(cmp != null){
                    PrefetchDisabled prefetchDisabled = cmp.getPrefetchDisabled();
                    if(prefetchDisabled != null){
                        QueryMethod[] queryMethods = prefetchDisabled.getQueryMethod();
                        if(queryMethods != null){
                            for(int i=0; i<queryMethods.length; i++){
                                prefetchedMethodList.add(queryMethods[i]);
                            }
                        }
                    }
                }
            }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException ex){
                //System.out.println("Not Supported Version");      //NOI18N
            }
            return prefetchedMethodList;
        }


        public String getHelpId() {
		return "AS_CFG_CmpEntityEjb";                           //NOI18N
	}
}

