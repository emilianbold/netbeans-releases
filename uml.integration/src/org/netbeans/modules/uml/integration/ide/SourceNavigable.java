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
 * File         : SourceNavigable.java
 * Version      : 1.0
 * Description  : Base class for classes that can generate source navigation
 *                events.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.integration.ide.events.MemberInfo;
import org.netbeans.modules.uml.integration.ide.events.MethodInfo;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
//import org.netbeans.modules.uml.ui.support.presentationnavigation.SourceNavigator;

/**
 *  A convenience base class for classes that generate source navigation
 * events.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-05-22  Darshan     Created.
 *   2  2002-05-23  Darshan     Moved source navigation calls into invokeLater
 *                              threads, which should hopefully kill the hang
 *                              when navigating.
 *   3  2002-05-27  Darshan     Fixed NPE when orphan element is passed to
 *                              source navigator.
 *   4  2002-06-05  Darshan     Incorporated Sumitabh's code to navigate to
 *                              the source for generalizations, implementations
 *                              and associations.
 *   5  2002-06-17  Darshan     Removed annoying logging statements.
 *   6  2002-06-18  Darshan     Added code to navigate to the classifier for a
 *                              lifeline.
 *   7  2002-06-20  Darshan     Added navigation to the operation for sequence
 *                              diagram messages (fix for bug 145).
 *
 * @author Darshan
 */
abstract public class SourceNavigable implements ISourceNavigable {
    protected SourceNavigator navigator;
    private int lineNoOffset = 0;

    public void setSourceNavigator(SourceNavigator nav) {
        navigator = nav;
    }

    public SourceNavigator getSourceNavigator() {
        return navigator;
    }

    protected void fireNavigateEvent(IElement target) {
        try {
            if(UMLSupport.navigateToSource)
                doFireNavigateEvent(target);
        } catch (Throwable t) {
            Log.stackTrace(t);
        }
    }

    protected void fireNavigateMethod(final MethodInfo minf) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	if (minf != null)
            	{
					String fileName = minf.getFilename();
					String projName = null;
					IProject proj = minf.getProject();
					if (proj != null)
					{
						projName = proj.getName();
					}
					UMLSupport.setProjectForPath(fileName, projName);
					navigator.navigateTo(minf);
            	}
            }
        });
    }

    protected void fireNavigateAttribute(final MemberInfo minf) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	if (minf != null)
            	{
					String fileName = minf.getFilename();
					String projName = null;
					IProject proj = minf.getProject();
					if (proj != null)
					{
						projName = proj.getName();
					}
                    UMLSupport.setProjectForPath(fileName, projName);
					navigator.navigateTo(minf);
            	}
            }
        });
    }

    protected void fireNavigateClass(final ClassInfo cl) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	if (cl != null)
            	{
            		String fileName = cl.getFilename();
            		String projName = null;
            		IProject proj = cl.getProject();
            		if (proj != null)
            		{
            			projName = proj.getName();
            		}
					UMLSupport.setProjectForPath(fileName, projName);
					navigator.navigateTo(cl);
            	}
            }
        });
    }

    /**
     *  Fires a source navigation event with the given target element.
     * @param target The element to be navigated to in the source code.
     */
    protected void doFireNavigateEvent(IElement target) {
        if (navigator == null || target == null) {
            return ;
        }

        // It's a good idea to look for the more exotic elements first, since
        // they typically derive from the basic IClassifier.
        if (target instanceof IOperation) {
            IOperation op = (IOperation)  target;
            final MethodInfo minf = new MethodInfo(null, op);
            minf.setLineNo(lineNoOffset);
            if (minf.getContainingClass() != null)
                fireNavigateMethod(minf);
            lineNoOffset = 0;
            
        } else if (target instanceof IAttribute) {
            IAttribute attr = (IAttribute)  target;
            final MemberInfo minf = new MemberInfo(attr);
            if (minf.getContainingClass() != null)
                fireNavigateAttribute(minf);
        } else if (target instanceof IGeneralization) {
            IGeneralization gen = (IGeneralization)  target;
            IClassifier clazz = gen.getSpecific();
            final ClassInfo cl = new ClassInfo(clazz);
            fireNavigateClass(cl);
        } else if (target instanceof IImplementation) {
            IImplementation imp = (IImplementation)  target;
            IClassifier clazz = imp.getImplementingClassifier();
            final ClassInfo cl = new ClassInfo(clazz);
            fireNavigateClass(cl);
        } else if (target instanceof IAssociation) {
            // General association handling code also takes care of compositions
            // and aggregations.
            IAssociation assoc = (IAssociation)  target;
            ETList<IAssociationEnd> ends = assoc.getEnds();
            if (ends.getCount() > 0) {
                for (int j = 0; j < ends.getCount(); j++) {
                    IAssociationEnd end = ends.item(j);
                    if (end.getIsNavigable()) {
                        INavigableEnd nEnd = (INavigableEnd)  end;
                        final MemberInfo minf = new MemberInfo(nEnd);
                        fireNavigateAttribute(minf);
                        // Don't worry about navigating to other ends - only
                        // one line can be hightlighted anyway
                        break;
                    }
                }
            }
        } else if (target instanceof ILifeline) {
            ILifeline life = (ILifeline)  target;
            IClassifier clazz = life.getRepresentingClassifier();
            if (clazz != null) {
                final ClassInfo cl = new ClassInfo(clazz);
                fireNavigateClass(cl);
            }
        } else if (target instanceof IMessage) {
        	     	
            IMessage mess = (IMessage)  target;
            IOperation op = null;
            IElement owner = mess.getInteraction().getOwner();
            if(owner instanceof IOperation)
            {
            	op = (IOperation)mess.getInteraction().getOwner();
            lineNoOffset = target.getLineNumber()-1;
            }
            if (op != null)
                doFireNavigateEvent(op);
        } else if (target instanceof IClassifier) {
            // Don't move the IClassifier check up, since it tends to swallow
            // other elements.
            IClassifier clazz = (IClassifier)  target;
            if (clazz.getName().equals(
                     Preferences.getDefaultElementName()))
                return ;
            final ClassInfo cl = ClassInfo.getRefClassInfo(clazz, true);
            fireNavigateClass(cl);
        }
    }
}
