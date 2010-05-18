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
