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


package org.netbeans.modules.uml.core.metamodel.core.foundation;


/**
 * @author sumitabhk
 *
 */
public class OwnerRetriever <Type> {

    private IElement m_cpElement = null;

	public OwnerRetriever()
    {
	}

    public OwnerRetriever(IElement curElem)
    {
        m_cpElement = curElem;
	}

	/**
	 * @param element
	 * @return
	 */
	public static <Type> Type getOwnerByType(IElement element, Class type) {
	    IElement currElem = element;
        Type ret = null;
        if (currElem != null)
        {
            IElement currOwner = currElem.getOwner();
            while (currOwner != null)
            {
                // AZTEC: Note that we can't check type with an instanceof (that 
                // won't even compile), and we can't cast to (Type) and catch a 
                // ClassCastException, because the ClassCastException will be
                // thrown *in the caller*, not in this function (the JSR 
                // compiler supports a very hobbled form of generics). We also
                // can't obtain the 'Class' object used here by doing Type.class
                // - that again, is not supported by the JSR compiler. A full
                // house. :-/
                if (type.isAssignableFrom(currOwner.getClass()))
                    return (Type) currOwner;

                currOwner = currOwner.getOwner();                
            }            
        }
        return null;
	}
    
    /**
     * @param element
     * @deprecated This function is almost guaranteed not to work properly,
     *             thanks to eccentricities in the JSR 14 compiler. Use 
     *             getOwnerByType(IElement, Class) instead.
     * @return
     */
    public Type getOwnerByType(Class type) {
//        IElement currElem = m_cpElement;
//        Type ret = null;
//        if (currElem != null)
//        {
//            IElement currOwner = currElem.getOwner();
//            while (currOwner != null)
//            {
//                try
//                {
////                    ret = (Type) currOwner;
//                   if(type)
//                   ret = castToType.cast(currOwner);
//                }
//                catch (ClassCastException e)
//                {
//                }            
//                    
//                if(ret != null)
//                    return ret;
//                
//                currOwner = currOwner.getOwner();
//            }
//        }
//        return null;
       
       IElement currElem = m_cpElement;
       Type ret = null;
       if (currElem != null)
       {
          IElement currOwner = currElem.getOwner();
          while (currOwner != null)
          {
             // AZTEC: Note that we can't check type with an instanceof (that
             // won't even compile), and we can't cast to (Type) and catch a
             // ClassCastException, because the ClassCastException will be
             // thrown *in the caller*, not in this function (the JSR
             // compiler supports a very hobbled form of generics). We also
             // can't obtain the 'Class' object used here by doing Type.class
             // - that again, is not supported by the JSR compiler. A full
             // house. :-/
             if (type.isAssignableFrom(currOwner.getClass()))
                return (Type) currOwner;
             
             currOwner = currOwner.getOwner();
          }
       }
       return null;
    }
    
	/**
	 * @return
	 */
	public Type getOwner()
	{
	    if(m_cpElement != null)
        {
            try
            {
                return (Type)m_cpElement.getOwner();
            }
            catch (ClassCastException e)
            {
            }
        }
        return null;
	}

}
