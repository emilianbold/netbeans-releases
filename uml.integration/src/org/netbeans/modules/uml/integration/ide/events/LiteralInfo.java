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

/*
 * File         : LiteralInfo.java
 * Version      : 1.0
 * Description  : Information about the changes to an enumeration's literal.
 * Author       : Daniel Prusa
 */
package org.netbeans.modules.uml.integration.ide.events;

import java.lang.reflect.Modifier;
import java.util.StringTokenizer;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.integration.ide.ChangeUtils;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.integration.ide.UMLSupport;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class LiteralInfo extends ElementInfo
{
    /** The containing class information. */
    private ClassInfo  mContainer = null;

    /**
     *  The IEnumerationLiteral from which this LiteralInfo was constructed. If the
     * LiteralInfo was constructed by an IDE integration, this should be null.
     */
    private IEnumerationLiteral literal  = null;

    public IEnumerationLiteral getLiteral() {
    	return literal;
    }
    public LiteralInfo(ClassInfo container, int type) {
        super(type);
        setContainingClass(container);
    }

    public LiteralInfo(ClassInfo container, IEnumerationLiteral literal) {
        super(literal);
        setContainingClass(container);

        this.literal = literal;
        setFromLiteral(literal);
    }

    public LiteralInfo(IEnumerationLiteral lit) {
        this(null, lit);
    }
    
    /* (non-Javadoc)
     */
    public IProject getOwningProject() {
        return literal != null?
                    (IProject) literal.getProject() :
               getContainingClass() != null?
                    getContainingClass().getOwningProject() :
                    null;
    }

    public String getFilename() {
        return (mContainer != null? mContainer.getFilename() : null);
    }

    public IProject getProject() {
        return literal != null? (IProject) literal.getProject()
                           : null;
    }

    /**
     * Set all properties for this LiteralInfo using info from the given
     * IEnumerationLiteral.
     * @param attr An <code>IEnumerationLiteral</code> for the literal.
     */
    public void setFromLiteral(IEnumerationLiteral lit) {
        setName(lit.getName());
        // Assuming the container hasn't been set, attempt to create a
        // ClassInfo for it.
        if (getContainingClass() == null) {
            IEnumeration owner = lit.getEnumeration();
            ClassInfo inf = ClassInfo.getRefClassInfo(owner, true);
            setContainingClass(inf);
        }
    }

    /**
     * Retrieves the containing clas of the data member.
     * @return The containing class.
     */
    public ClassInfo getContainingClass()
    {
        return mContainer;
    }

    /**
     * Sets the containing clas of the data member.
     * @param container The containing class.
     */
    public void setContainingClass(ClassInfo container)
    {
        mContainer = container;
    }

    public void update()
    {
        if(getContainingClass() != null)
        {
            SymbolTransaction trans = new SymbolTransaction(getContainingClass());
            update(trans);
        }
    }

    public LiteralTransaction update(SymbolTransaction trans)
    {
        EventManager manager = EventManager.getEventManager();

        LiteralTransaction retVal = new LiteralTransaction(trans, this);
        IEnumerationLiteral lit = retVal.getLiteral();
        
        if (lit == null)
            return null;
        
        manager.getEventFilter().blockEventType(ChangeUtils.RDT_DEPENDENCY_ADDED);
        try
        {
            if(retVal != null)
            {
                if(getChangeType() == ElementInfo.DELETE)
                {
                    lit.delete();
                }
                else
                {
                    if(getNewName() != null)
                    {
                        lit.setName(getNewName());
                    }
                }
            }
        }
        finally
        {
        	manager.getEventFilter().unblockEventType(ChangeUtils.RDT_DEPENDENCY_ADDED);
        }

        return retVal;
    }

    public String getCode() {
        return "L";
    }

    public String toString() {
        StringBuffer str = new StringBuffer("" + getName());
        return str.toString();
    }

}
