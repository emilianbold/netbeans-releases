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
package org.netbeans.modules.soa.pojo.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * POJO SE configurator adds POJO SE related build scripts and libraries to Java SE project 
 * @author Sreenivasan Genipudi
 */
public final class PojoProjectConfigurator extends AbstractAction implements ContextAwareAction {
    Lookup ctx = null;
    
    public PojoProjectConfigurator(){
        super(NbBundle.getMessage(PojoProjectConfigurator.class, "CTL_PojoProjectConfigurator")); //NOI18N
    }
    
    public PojoProjectConfigurator(Lookup ctx){
        super(NbBundle.getMessage(PojoProjectConfigurator.class, "CTL_PojoProjectConfigurator")); //NOI18N
        this.ctx = ctx;
    }
    
    public void actionPerformed(ActionEvent e) {
    }

    public Action createContextAwareInstance(Lookup arg0) {
        return new PojoProjectConfigurator(arg0);
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(PojoProjectConfigurator.class, "CTL_PojoProjectConfigurator"); //NOI18N
    }

    protected Class[] cookieClasses() {
        return new Class[]{Project.class};
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isEnabled() {
        boolean ret = false;
        if (ctx != null){
            Project project = ctx.lookup(Project.class);
            if ((project != null) && (project.getClass().getName().equals(
                    "org.netbeans.modules.java.j2seproject.J2SEProject" ))){//NOI18N                
            }        
        }
        return ret;
    }
}

