/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.jsfsupport.designtime;

import javax.faces.context.FacesContext;

//import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProject;
import org.netbeans.modules.visualweb.jsfsupport.container.RaveFacesContext;
import javax.faces.el.VariableResolver;


/**
 * DesignTimeVariableResolver provides a JSF variable resolver for design-time variable (bean) lookup
 *
 * @author Carl Quinn
 * @author Winston Prakash - Modifications to support JSF 1.2
 * @version 1.0
 */
public class DesignTimeVariableResolver extends VariableResolver {

    private VariableResolver nested;
    public DesignTimeVariableResolver(VariableResolver nested){
        this.nested =  nested;
    }

    // Specified by javax.faces.el.VariableResolver.resolveVariable()
    public Object resolveVariable(FacesContext context, String name) {
        Object value = null;
        //Trace.trace("jsfsupport.container", "MVR.resolveVariable " + name);

        RaveFacesContext raveContext  = (RaveFacesContext)context;
        DesignContext liveContext = raveContext.getDesignContext();

        if (name != null && liveContext != null) {
            DesignProject designProject = liveContext.getProject();
            if (designProject instanceof FacesDesignProject) {
                DesignContext designContext =
                        ((FacesDesignProject) designProject).findDesignContext(name);
                if(designContext != null) {
                    return designContext.getRootContainer();
                }
            } else {
//               DesignContext[] lcs = designProject.getDesignContexts();
//               for (int i = 0; i < lcs.length; i++) {
//                             if (lcs[i] instanceof FacesDesignContext) {
//                                if (name.equals(((FacesDesignContext)lcs[i]).getReferenceName()))
//                                     return lcs[i].getRootContainer();
//                            }
//                          else {
//                                if (name.equals(lcs[i].getDisplayName()))
//                                   return lcs[i].getRootContainer();
//                           }
//                       }
            }
        }

        //try {
            //ELResolver elResolver = context.getApplication().getELResolver();
            //value = elResolver.getValue(context.getELContext(), null, name);
        //} catch (ELException elex) {
            //throw new EvaluationException(elex);
        //}
        return nested.resolveVariable(context, name);
    }
}
