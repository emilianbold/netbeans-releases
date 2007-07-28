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

/*
 * CustomePropertyEditor.java
 *
 * Created on March 17, 2005, 8:33 PM
 */

package org.netbeans.modules.uml.propertysupport.nodes;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import org.netbeans.modules.uml.propertysupport.customizers.PropertyElementCustomizer;
import org.netbeans.modules.uml.propertysupport.customizers.ParameterCustomizer;
import java.awt.Component;
import org.netbeans.modules.uml.propertysupport.customizers.Customizer;


/**
 *
 * @author Administrator
 */
public class CustomPropertyEditor extends DefinitionPropertyEditor
{
    Component customizer = null;
    /** Creates a new instance of CustomePropertyEditor */
    public CustomPropertyEditor(IPropertyDefinition def, IPropertyElement element)
    {
        super(def, element);
        
    }
    
    public boolean supportsCustomEditor()
    {
        return true;
    }
    
    public Component getCustomEditor()
    {
        try
        {
            // TODO: Have to add a getCustomEditor on IPropertyElement
            String classID = getDefinition().getProgID();
            if((classID != null) && (classID.length() > 0))
            {
                Class c = Class.forName(classID);
                if(c != null)
                {
                    // HACK HACK Major HACK.  NEED getCustomEditor on IPropertyElement
                    if (customizer == null)
                    {
                        customizer = (Component)c.newInstance();
                    }
                    
                    IPropertyElement element = getElement();
                    if(element != null)
                    {
                        IPropertyDefinition def = getDefinition();
                        if (customizer instanceof PropertyElementCustomizer ||
                                customizer instanceof ParameterCustomizer)
                        {
                            IPropertyDefinition def2 = def.getSubDefinition(0);
                            def = def2;
                        }
                        
                        
                        if(def.isOnDemand() == true)
                        {
                            DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
                            def = builder.loadOnDemandDefintion(def);
                        }
                        
                        if (customizer instanceof Customizer)
                        {
                            ((Customizer)customizer).setElement(element, def);
                            ((Customizer)customizer).setPropertySupport(this);
                        }
                    }
                }
            }
        }
        catch(ClassNotFoundException e)
        {
        }
        catch(ClassCastException ce)
        {
        }
        catch(InstantiationException ie)
        {
        }
        catch(IllegalAccessException ae)
        {
        }
        
        return customizer;
    }
    
}
