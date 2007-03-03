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

package org.netbeans.modules.uml.integration.netbeans;

/**
 * Company:
 * @author swadebeshp
 * @version 1.0
 */

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
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.integration.ide.Preferences;
import org.netbeans.modules.uml.integration.ide.SourceNavigable;
import org.netbeans.modules.uml.integration.ide.SourceNavigator;
import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.integration.ide.events.MemberInfo;
import org.netbeans.modules.uml.integration.ide.events.MethodInfo;
import org.netbeans.modules.uml.integration.netbeans.actions.SourceNavigateAction;

public class SourceAction extends SourceNavigable{
    private int lineNoOffset = 0;
	NBSourceNavigator gator= new NBSourceNavigator();
	
	public void doFireNavigateEvent(IElement target) {
		navigator=gator;
        if (navigator == null || target == null) {
            return ;
        }
	
        // It's a good idea to look for the more exotic elements first, since
        // they typically derive from the basic IClassifier.
        if (target instanceof IOperation) {
			SourceNavigateAction.Round_Trip=true;
            IOperation op = (IOperation)  target;
            final MethodInfo minf = new MethodInfo(null, op);
            minf.setLineNo(lineNoOffset);
            if (minf.getContainingClass() != null)
                fireNavigateMethod(minf);
            lineNoOffset = 0;
            
        } else if (target instanceof IAttribute) {
			SourceNavigateAction.Round_Trip=true;
            IAttribute attr = (IAttribute)  target;
            final MemberInfo minf = new MemberInfo(attr);
            if (minf.getContainingClass() != null)
                fireNavigateAttribute(minf);
        } else if (target instanceof IGeneralization) {
			SourceNavigateAction.Round_Trip=true;
            IGeneralization gen = (IGeneralization)  target;
            IClassifier clazz = gen.getSpecific();
            final ClassInfo cl = new ClassInfo(clazz);
            fireNavigateClass(cl);
        } else if (target instanceof IImplementation) {
			SourceNavigateAction.Round_Trip=true;
            IImplementation imp = (IImplementation)  target;
            IClassifier clazz = imp.getImplementingClassifier();
            final ClassInfo cl = new ClassInfo(clazz);
            fireNavigateClass(cl);
        } else if (target instanceof IAssociation) {
			SourceNavigateAction.Round_Trip=true;
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
			SourceNavigateAction.Round_Trip=true;
            ILifeline life = (ILifeline)  target;
            IClassifier clazz = life.getRepresentingClassifier();
            if (clazz != null) {
                final ClassInfo cl = new ClassInfo(clazz);
                fireNavigateClass(cl);
            }
        } else if (target instanceof IMessage) {
			SourceNavigateAction.Round_Trip=true;
        	     	
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
			SourceNavigateAction.Round_Trip=true;
            // Don't move the IClassifier check up, since it tends to swallow
            // other elements.
            IClassifier clazz = (IClassifier)  target;
            if (clazz.getName().equals(
                     Preferences.getDefaultElementName()))
                return ;
            final ClassInfo cl = ClassInfo.getRefClassInfo(clazz, true);
            fireNavigateClass(cl);
        }
//		SourceNavigateAction.Round_Trip=false;
    }
}
