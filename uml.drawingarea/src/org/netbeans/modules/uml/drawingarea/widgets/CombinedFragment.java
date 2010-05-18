/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.drawingarea.widgets;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;

/**
 * provide functionality of combned fragment necessary for cdfs/re operation/?loading?
 * @author sp153251
 */
public interface CombinedFragment {
    /**
     * combined fragment need to cover child lifelines, messages, properly adjust operands after cdfs
     */
    public void resizeToModelContent();
    
    /**
     * set message right before this combined fragment
     * @param msg
     * @param msgW
     */
    public void setMessageBefore(IMessage msg,Widget msgW);
    /**
     * set message right after this combined fragment
     * @param msg
     * @param msgW
     */
    public void setMessageAfter(IMessage msg,Widget msgW);
    
    /**
     * 
     * @param cf
     */
    public void setCombinedFragmentBefore(ICombinedFragment cf,Widget cfW);
    /**
     * 
     * @param cf
     */
    public void setCombinedFragmentAfter(ICombinedFragment cf,Widget cfW);
    
    //public void setOwnedCombinedFragments();
    /**
     * after cdfs for example by default all expression labels should be visible
     */
    public void showLabels();
    /**
     * need ability to say combined fragment to calculate children after creation
     * @return
     */
    public ContainerWidget getContainer();
}
