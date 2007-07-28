/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.uml.propertysupport.nodes;

import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;
import org.netbeans.modules.uml.common.Util;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.ui.controls.newdialog.AddPackageVisualPanel1;
import org.netbeans.modules.uml.ui.support.applicationmanager.INameCollisionListener;
import org.netbeans.modules.uml.ui.support.applicationmanager.NameCollisionListener;
import org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditorCollisionHandler;
import org.netbeans.modules.uml.ui.swing.propertyeditor.PropertyEditorCollisionHandler;
import org.netbeans.modules.uml.ui.swing.propertyeditor.PropertyEditorEventsSink;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Trey Spiva
 */
public abstract class DefinitionPropertySupport extends PropertySupport
{
    private IPropertyDefinition mDefinition = null;
    private IPropertyElement mElement = null;
    private ConfigStringTranslator mTranslator = new ConfigStringTranslator();
    private boolean mAutoCommit = true;
    private DefinitionPropertyBuilder.ValidValues mValidValues = null;
    
    /// The event handler for the various controls and the core UML metamodel
    private PropertyEditorEventsSink m_EventsSink = null;
    private NameCollisionListener m_NameCollisionListener = null;
    private PropertyEditorCollisionHandler m_CollisionHandler = null;
    
    /**
     *
     */
    public DefinitionPropertySupport(IPropertyDefinition def,
            IPropertyElement element,
            Class type,
            boolean writable,
            boolean autoCommit)
    {
        super(def.getName(),
                type,
                def.getDisplayName(),
                def.getHelpDescription(),
                true,
                writable);
        
        mDefinition = def;
        mElement = element;
        setAutoCommit(autoCommit);
        
        // We will only have valid values when the control type is "read-only"
        mValidValues = DefinitionPropertyBuilder.instance().retrieveValidValues(mDefinition, mElement);
    }
    
    public void setAutoCommit(boolean value)
    {
        mAutoCommit = value;
    }
    
   /* (non-Javadoc)
    * @see org.openide.nodes.Node.Property#getValue()
    */
    public Object getValue() throws IllegalAccessException, InvocationTargetException
    {
        Object retVal = "";
        
        retVal = retrieveFromElement();
        
        return retVal;
    }
    
    public void setValue(Object value)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        // Create the name collision listener
        m_NameCollisionListener = new NameCollisionListener();
        // add name collision handler to fix 6286583
        m_CollisionHandler = new PropertyEditorCollisionHandler();
        m_CollisionHandler.setPropertyElement(getElement());
        m_NameCollisionListener.setHandler(m_CollisionHandler);
        m_NameCollisionListener.setEnabled(true);
        
        IPropertyElement element = getElement();
        if((element != null))
        {
            if(value != null)
            {
                Object modelElement = element.getElement();
                if ((modelElement instanceof IDiagram || modelElement instanceof IProxyDiagram) &&
                     ("Name".equals(element.getName()) || "Alias".equals(element.getName())))
                {
                    if (!Util.isDiagramNameValid((String)value))
                    {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(
                                AddPackageVisualPanel1.class,
                                "MSG_Invalid_Diagram_Name", value)); // NOI18N
                        DialogDisplayer.getDefault().notify(msg);
                        return;
                    } 
                }
                String translatedValue = translateValueBack(value);
                if(translatedValue != null)
                {
                    //               String origValue = getElement().getValue();
                    //               if(origValue.equals(translatedValue) == false)
                    //               {
                    element.setValue(translatedValue);
                    
                    if(mAutoCommit = true)
                    {
                        element.setModified(true);
                        PreferenceHelper.saveModifiedPreferences(element);
                    }
                    //               }
                }
            }
            
            IPropertyDefinition def = getDefinition();
            if(def.isForceRefresh() == true)
            {
                // Causes the NB property editor to refresh.
                Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
                if (activatedNodes != null)
                {
                    int arrayLength = activatedNodes.length;
                    for (int i = 0; i < arrayLength; i++)
                    {
                        // The setDisplayName causes the property sets to be updated.
                        Node myNode = activatedNodes[i];
                        myNode.setDisplayName(myNode.getDisplayName());
                    }
                }
            }
        }
        m_NameCollisionListener.deInitialize();
    }
    
    public String getShortDescription()
    {
        String retVal = getDefinition().getHelpDescription();
        if((retVal != null) && (retVal.length() > 0))
        {
            retVal = getTranslatorValue(retVal);
        }
        return retVal;
    }
    
    /**
     * @return
     */
    public IPropertyDefinition getDefinition()
    {
        return mDefinition;
    }
    
    /**
     * @param definition
     */
    public void setDefinition(IPropertyDefinition definition)
    {
        mDefinition = definition;
    }
    
    /**
     * @return
     */
    public IPropertyElement getElement()
    {
        return mElement;
    }
    
    /**
     * @param element
     */
    public void setElement(IPropertyElement element)
    {
        mElement = element;
    }
    
    /**
     * @return
     */
    public String getTranslatorValue(String value)
    {
        return mTranslator.translate(getDefinition(), value);
    }
    
    //**************************************************
    // Helper Methods
    //**************************************************
    
    protected Object retrieveFromElement()
    {
        Object retVal = null;
        
        IPropertyElement element = getElement();
        if((element != null))
        {
            //          // We will only have valid values when the control type is an enumeration
            //         retVal = translateEnumerationValue(element.getValue());
            
            String value = element.getValue();
            if(value != null)
            {
                retVal = getTranslatorValue(value);
                if(retVal == null)
                {
                    retVal = value;
                }
            }
            else
            {
                retVal = "";
            }
        }
        
        return retVal;
    }
    
    protected Object translateEnumerationValue(String value)
    {
        String retVal = value;
        
        //       IPropertyDefinition def = getDefinition();
        //       String enumValues = def.getEnumValues();
        //       if(enumValues != null)
        //       {
        //           StringTokenizer tokenizer = new StringTokenizer(enumValues, "|");
        //           int tokens = tokenizer.countTokens();
        //           for(int index = 0; index < tokens; index++)
        //           {
        //               String curToken = tokenizer.nextToken();
        //               if(curToken.equals(value) == true)
        //               {
        //                   retVal = Integer.toString(index);
        //                   break;
        //               }
        //           }
        //
        //           if(mValidValues != null)
        //           {
        //               retVal = mValidValues.translateValue(retVal);
        //           }
        //           else if(retVal != null)
        //           {
        //               retVal = getTranslatorValue(retVal);
        //               if(retVal == null)
        //               {
        //                   retVal = value;
        //               }
        //           }
        //       }
        
        return retVal;
    }
    
    protected String translateEnumBack(String value)
    {
        String retVal = value;
        
        if(mValidValues != null)
        {
            retVal = mValidValues.translateValueBack(value);
            
            IPropertyDefinition def = getDefinition();
            String enumValues = def.getEnumValues();
            if(enumValues != null)
            {
                StringTokenizer tokenizer = new StringTokenizer(enumValues, "|");
                int tokens = tokenizer.countTokens();
                for(int index = 0; index < tokens; index++)
                {
                    String curToken = tokenizer.nextToken();
                    if(curToken.equals(retVal) == true)
                    {
                        retVal = Integer.toString(index);
                        break;
                    }
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Translates the value displayed to the user back into the value that
     * needs to be stored to the database.
     *
     * @param value The object being store to the propetery.
     * @return The string represenation of the value.
     */
    protected String translateValueBack(Object value)
    {
        String retVal = "";
        
        if (value instanceof String)
        {
            retVal = (String)value;
            //         retVal = translateEnumBack((String)value);
        }
        
        return retVal;
    }
}
