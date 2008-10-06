/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.uml.diagrams.actions;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.drawingarea.actions.SceneNodeAction;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class SetMultiplicityAction extends SceneNodeAction
{
    private IAssociationEnd end = null;
    
    protected void performAction(Node[] activatedNodes)
    {
    }

    public String getName()
    {
        return NbBundle.getMessage(SetMultiplicityAction.class, "CTL_SetMultiplicityAction");
    }

    protected Class[] cookieClasses()
    {
        return new Class[]{IAssociationEnd.class};
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean enable(Node[] activatedNodes)
    {
        boolean retVal = false;
        
        if(super.enable(activatedNodes) == true)
        {
            retVal = true;
            if (end == null)
            {
                retVal = false;
            }
        }
        return retVal;
    }
    
    @Override
    public Action createContextAwareInstance(Lookup actionContext)
    {
        end = actionContext.lookup(IAssociationEnd.class);
//        if (end == null)
//        {
//            return null;
//        }
        
        return this;
    }
    
    @Override
    public JMenuItem getPopupPresenter()
    {   
        JMenuItem item =  new Actions.SubMenu(this, new SetMultiplicityModel());
        Actions.connect(item, (Action)this, true);
        
        return item;
    }
    
    protected class SetMultiplicityModel implements Actions.SubMenuModel
    {

        public int getCount()
        {
            return 5;
        }

        public String getLabel(int index)
        {
            String retVal = "";
            
            switch(index)
            {
                case 0:
                    retVal = "0..1"; // NOI18N
                    break;
                case 1:
                    retVal = "0..*"; // NOI18N
                    break;
                case 2:
                    retVal = "*"; // NOI18N
                    break;
                case 3:
                    retVal = "1"; // NOI18N
                    break;
                default:
                    retVal = "1..*"; // NOI18N
            }
            
            return retVal;
        }

        public HelpCtx getHelpCtx(int index)
        {
            return null;
        }

        public void performActionAt(int index)
        {
            IMultiplicity multiplicity = end.getMultiplicity();
            multiplicity.removeAllRanges();
            
            IMultiplicityRange range = multiplicity.createRange();
            switch(index)
            {
                case 0:
                    range.setLower("0"); // NOI18N
                    range.setUpper("1"); // NOI18N
                    multiplicity.addRange(range);
                    break;
                case 1:
                    range.setLower("0"); // NOI18N
                    range.setUpper("*"); // NOI18N
                    multiplicity.addRange(range);
                    break;
                case 2:
                    multiplicity.setRangeThroughString("*"); // NOI18N
                    break;
                case 3:
                    multiplicity.setRangeThroughString("1"); // NOI18N
                    break;
                default:
                    range.setLower("1"); // NOI18N
                    range.setUpper("*"); // NOI18N
                    multiplicity.addRange(range);
            }
        }

        public void addChangeListener(ChangeListener l)
        {
            
        }

        public void removeChangeListener(ChangeListener l)
        {
            
        }
        
    }
}