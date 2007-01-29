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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProject;
import com.sun.rave.web.ui.component.Breadcrumbs;
import com.sun.rave.web.ui.component.Hyperlink;
import com.sun.rave.web.ui.component.ImageHyperlink;
import com.sun.rave.web.ui.component.Page;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;
import java.net.URI;
import java.net.URISyntaxException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;

/**
 * DesignInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.Breadcrumbs} component.
 * The following behaviors are implemented:
 * <ul>
 * <li>Upon creation, populate breadcrumbs with two hyperlink components, one
 * for the web application, and one for the current page
 * ({@link org.netbeans.modules.visualweb.web.ui.dt.component.Hyperlink} is used, since that is
 * the only hyperlink used by Creator).</li>
 * </ul>
 *
 * @author gjmurphy
 */
public class BreadcrumbsDesignInfo extends AbstractDesignInfo {

    public BreadcrumbsDesignInfo() {
        super(Breadcrumbs.class);
    }

    // For performance improvement. No need to get all the contexts in the project
    private DesignContext[] getDesignContexts(DesignBean designBean){
        DesignProject designProject = designBean.getDesignContext().getProject();
        DesignContext[] contexts;
        if (designProject instanceof FacesDesignProject) {
            contexts = ((FacesDesignProject)designProject).findDesignContexts(new String[] {
                "request",
                "session",
                "application"
            });
        } else {
            contexts = new DesignContext[0];
        }
        DesignContext[] designContexts = new DesignContext[contexts.length + 1];
        designContexts[0] = designBean.getDesignContext();
        System.arraycopy(contexts, 0, designContexts, 1, contexts.length);
        return designContexts;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        DesignContext context = bean.getDesignContext();
        UIComponent component = (UIComponent)bean.getInstance();
        if (context.canCreateBean(Hyperlink.class.getName(), bean, null)) {
            // Add an initial hyperlink for every page in the project
            try {
                DesignContext[] contexts = bean.getDesignContext().getProject().getDesignContexts();
                //DesignContext[] contexts = getDesignContexts(bean);

                URI rootURI = context.getProject().getResourceFile(new URI("./web")).toURI(); //NOI18N
                for (int i = 0; i < contexts.length; i++) {
                    DesignBean rootBean = contexts[i].getRootContainer();
                    if (rootBean.getInstance() != null &&
                            UIViewRoot.class.isAssignableFrom(rootBean.getInstance().getClass()) &&
                            rootBean.getChildBean(0).getInstance() instanceof Page) {
                        DesignBean hyperlinkBean =
                                context.createBean(Hyperlink.class.getName(), bean, null);
                        URI pageURI = new URI(contexts[i].resolveResource(rootBean.getInstanceName() + ".jsp").toString()); //NOI18N
                        URI relativeURI = rootURI.relativize(pageURI);
                        String contextRelativePath = "/faces/" + relativeURI.toString();
                        hyperlinkBean.getProperty("url").setValue(contextRelativePath); //NOI18N
                        hyperlinkBean.getProperty("text").setValue(((FacesDesignContext) contexts[i]).getDisplayName()); //NOI18N
                    }
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return Result.SUCCESS;
    }
    
    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        Class parentClass = parentBean.getInstance().getClass();
        if(Hyperlink.class.equals(childClass) || ImageHyperlink.class.equals(childClass))
            return true;
        return super.acceptChild(parentBean, childBean, childClass);
    }
    
    protected DesignProperty getDefaultBindingProperty(DesignBean targetBean) {
        return targetBean.getProperty("pages"); //NOI18N
    }
    
}
