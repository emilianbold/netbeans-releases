/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import java.util.Hashtable;

import org.dom4j.Document;
import org.dom4j.dom.DOMDocumentFactory;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * @author sumitabhk
 *
 */
public class CreationFactory implements ICreationFactory {    
	private ICreationFactory m_CreationFactory = null;
	private long m_RevokeNumber = 0;
	private boolean m_CreateState = false;
	private Document m_FragDocument = null;
	private IConfigManager m_ConfigMan = null;
	
	//stores key and creation data.
	private Hashtable < String, CreationData > m_Creators = new Hashtable < String, CreationData >();
	
	public CreationFactory()
	{
		m_FragDocument = XMLManip.getDOMDocument();
	}
	
	public ICreationFactory getCreationFactory() {
		return m_CreationFactory;
	}

	public void setCreationFactory(ICreationFactory value) {
		m_CreationFactory = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory#cleanUp()
	 */
	public long cleanUp() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getRevokeNumber() {
		return m_RevokeNumber;
	}

	public void setRevokeNumber(long value) {
		m_RevokeNumber = value;
	}

	/**
	 * Retrieves the IUnknown interface for the meta type specified in typeName..
	 * 
	 * @param typeName[in] The type to retrieve.  For example, "Class" will retrieve
	 *					the IUnknown of an implementation supporting the IClass
	 *					interface
	 * @param outer[in] The IUnknown interface if all goes well
	 *
	 * @return HRESULT
	 */
	public Object retrieveMetaType(String typeName, Object outer) {
		m_CreateState = true;
		Object retObj = retrieveEmptyMetaType(typeName, outer);   
		m_CreateState = false;
		
		// Now make sure all the XML elements for the element are prepared
		// correctly.
		if (retObj != null)
		{
			// Fire the pre and post create events
			try {
				Object obj = EventDispatchRetriever.instance().getDispatcher(EventDispatchNameKeeper.lifeTime());
				
				IElementLifeTimeEventDispatcher disp = obj instanceof IElementLifeTimeEventDispatcher ? (IElementLifeTimeEventDispatcher)obj : null;
				
				boolean proceed =disp != null ? disp.fireElementPreCreate(typeName, disp.createPayload("ElementPreCreate")) : true;
				if (proceed)
				{
					IVersionableElement vEle = prepareNewElement(retObj);
					if (disp != null)
					{
						disp.fireElementCreated(vEle, disp.createPayload("ElementCreated"));
					}
				}
			}catch (Exception e){
			}
			
		}
		return retObj;
	}

	/**
	 * Once an element has been initially created, this method is called
	 * in order to establish the appropriate XML datum behind the element.
	 * This call will QuertyInterface for the IVersionableElement interface.
	 * 
	 * @param newElement[in] The element to prepare
	 *
	 * @return 
	 */
	private IVersionableElement prepareNewElement(Object newEle) {
		IVersionableElement vEle = null;
		if (newEle != null && m_FragDocument != null)
		{
			try {
				if (newEle instanceof IVersionableElement)
				{
					vEle = (IVersionableElement)newEle;
					if(m_FragDocument.getRootElement() == null)
					{
						org.dom4j.Element frag = DOMDocumentFactory.getInstance().createElement("");
						m_FragDocument.setRootElement(frag);
					}
					if (m_FragDocument.getRootElement() != null)
					{
						vEle.prepareNode(m_FragDocument.getRootElement());
					}
				}
			} catch (Exception e)
			{
                e.printStackTrace();
			}
		}
		return vEle;
	}

	/**
	 * Creates the COM wrapper to house the actual XML element.  The XML element
	 * has no been initialized as a result of the cell.
	 * 
	 * @param typeName[in] The type to retrieve.  For example, "Class" will retrieve
	 * 						the IUnknown of an implementation supporting the IClass
	 *						interface
	 * @param outer[in]	The controlling outer unknown. Used when aggregating.  Can be 0.
	 * @param result[out] The IUnknown interface if all goes well
	 *
	 * @return HRESULT
	 */
	public Object retrieveEmptyMetaType(String typeName, Object outer) {
		return retrieveEmptyMetaType( "Elements", typeName, outer);
	}

	/**
	 * Creates the COM wrapper to house the actual XML element.  The XML element
	 * has not been initialized as a result of this call.
	 * 
	 * @param subKey[in] 	The registry sub-key where the typeName is found
	 * @param typeName[in] The type to retrieve.  For example "Class" will retrieve
	 *					the IUnknown of an implementation supporting the IClass interface
	 * @param outer[in]	The controlling outer unknown.  Used when aggregating.  Can be 0.
	 * @param result[out] The IUnknown interface if all goes well
	 *
	 * @return HRESULT
	 */
	public Object retrieveEmptyMetaType(String subKey, String typeName, Object outer) {
		Object retObj = null;
		establishConfigManager();

		// Create a unique name from the subKey and the typeName (separated by a |).  We use this
		// key to retrieve the creator the next time around.
		String typeStr = subKey;
		typeStr += "|";
		typeStr += typeName;
		CreationData data = m_Creators.get(typeStr);
		if (data != null)
		{
			retObj = data.createType(m_CreateState, subKey, typeName, m_ConfigMan, outer);
		}
		else
		{
			// Haven't been asked to create this, so let's go retrieve the progID and
			// do the creation
			data = new CreationData();
			data.establishData(m_CreateState, subKey, m_ConfigMan, typeName);
			retObj = data.createType(m_CreateState, subKey, typeName, m_ConfigMan, outer);
			m_Creators.put(typeStr, data);
		}
		
		return retObj;
	}

	/**
	 *
	 * Makes sure this CreationFactory has a ConfigManager
	 * properly installed on it.
	 *
	 * @return HRESULT
	 *
	 */
	private void establishConfigManager() {
		if (m_ConfigMan == null)
		{
			ICoreProduct prod = ProductRetriever.retrieveProduct();
			if (prod != null)
			{
				m_ConfigMan = prod.getConfigManager();
			}
		}
	}

	public void setConfigManager(IConfigManager value) {
		m_ConfigMan = value;
	}

    public class CreationData
    {

        private final static String ATTR_INSTANCE = "instance"; // NOI18N

        private Class m_Class = null;
        private Class m_TransitionClass = null;
        private FileObject definingFileObject = null;

        /**
         * Creates the specified object given the CLSID.  Caches the class factory
         * for the object for later use.
         *
         * @param typeName[in] The simple type we are looking for, e.g., "Class"
         * @param clsid[in] The CLSID of the object implementing the interface needed
         * @param outer[in] The controlling IUnknown
         * @param result[out] The interface requested
         *
         * @return HRESULT
         */
        public Object createType(boolean createState, String subKey, String typeName, IConfigManager conMan, Object outer)
        {
            Object retObj = null;
            establishFactory(createState, subKey, typeName, conMan);
            Class fact = retrieveFactory(createState);
            if (fact != null)
            {
                //Sumitabh find a way to create an inner class using outer.
                try
                {
                    retObj = fact.newInstance();
                }
                catch (InstantiationException e)
                {
                }
                catch (IllegalAccessException e)
                {
                }
            }
            else if (definingFileObject != null)
            {
                retObj = definingFileObject.getAttribute(ATTR_INSTANCE);
            }
            return retObj;
        }

        /**
         *
         * Establishes the appropriate factory on this object.
         *
         * @param createState[in] true if the factory is in a create state, else false
         * @param clsid[out] The CLSID of the type to create
         *
         * @return HRESULT
         *
         */
        private void establishFactory(boolean createState, String subKey, String typeName, IConfigManager conMan)
        {
            if (m_Class == null)
            {
                establishData(createState, subKey, conMan, typeName);
            }
        }

        /**
         *
         * Retrieves the correct ProgID for the element to create, based
         * on the creational status of this factory.
         *
         * @param valueResult[out] The progid, else empty string on error
         *
         * @return ERROR_SUCCESS, else -2000 if the type was not found.
         *
         */
        public void establishData(boolean create, String subKey, IConfigManager configMan, String typeName)
        {

            m_Class = null;

            if (configMan != null)
            {
                StringBuffer createID = new StringBuffer();
                String id = configMan.getIDs(subKey, typeName, createID);
                if (id != null && id.length() > 0)
                {
                    try
                    {
                        m_Class = Class.forName(id);
                    }
                    catch (ClassNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                }

                if (m_Class == null)
                {
                    // Trey - If we are not able to find the type in the
                    //        EssentialConfig.etc file (old way), look
                    //        In the layer file system (new way).
//                            try
//                            {
                        definingFileObject = FileUtil.getConfigFile("MetaData/" + subKey + "/" + typeName);
//                                DataObject dObj = fo != null ? DataObject.find(fo) : null;
//                                if (dObj != null)
//                                {
//                                    Object obj = fo.getAttribute("instance");
//                                    InstanceCookie ic = dObj.getCookie(org.openide.cookies.InstanceCookie.class);
//
//                                    Class cl = ic.instanceClass();
//                                }
//                            }
//                            catch (Exception e)
//                            {
//                                Exceptions.printStackTrace(e);
//                            }
                }

                if (createID.length() > 0)
                {
                    try
                    {
                        m_TransitionClass = Class.forName(createID.toString());
                    }
                    catch (ClassNotFoundException e)
                    {
                    }
                }
            }
        }

        public Class retrieveFactory(boolean createState)
        {
            return createState && m_TransitionClass != null ? m_TransitionClass : m_Class;
        }
    }
}

