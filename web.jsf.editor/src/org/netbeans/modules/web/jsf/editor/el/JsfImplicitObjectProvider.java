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
package org.netbeans.modules.web.jsf.editor.el;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.web.core.syntax.completion.api.ELExpression;
import org.netbeans.modules.web.core.syntax.spi.ELImplicitObject;
import org.netbeans.modules.web.core.syntax.spi.ImplicitObjectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;


/**
 * @author ads
 *
 */
@ServiceProvider(service=ImplicitObjectProvider.class)
public class JsfImplicitObjectProvider implements ImplicitObjectProvider {
    
    private static final String TEXT_XHTML = "text/xhtml";          // NOI18N
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.core.syntax.spi.ImplicitObjectProvider#getImplicitObjects()
     */
    public Collection<ELImplicitObject> getImplicitObjects() {
        List<ELImplicitObject> result = new ArrayList<ELImplicitObject>(9);
        result.add( new FacesContextObject());
        result.add(new CompositeComponentObject());
        result.add( new ApplicationObject());
        result.add(new ComponentObject());
        result.add( new FlashObject());
        result.add( new ResourceObject());
        result.add( new SessionObject());
        result.add( new ViewObject() );
        result.add( new ViewScopeObject());
        return result;
    }
    
    static class FacesContextObject extends ELImplicitObject{
        public FacesContextObject(){
            super("facesContext");                        //NOI18N
            setType(OBJECT_TYPE);
            setClazz("javax.faces.context.FacesContext"); //NOI18N
        }
    }
    
    static abstract class FaceletContextObject extends ELImplicitObject{
        FaceletContextObject(String name ){
            super( name );
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.web.core.syntax.spi.ELImplicitObject#isApplicable(org.netbeans.modules.web.core.syntax.completion.api.ELExpression)
         */
        @Override
        public boolean isApplicable( ELExpression expression ) {
            FileObject fileObject = expression.getFileObject();
            return fileObject.getMIMEType().equals(TEXT_XHTML);
        }
    }
    
    static class ApplicationObject extends FaceletContextObject {
        public ApplicationObject(){
            super("application");                         //NOI18N
            setType(OBJECT_TYPE);
            setClazz( Object.class.getCanonicalName());
        }
    }
    
    static class ComponentObject extends FaceletContextObject {
        public ComponentObject(){
            super("component");                                   //NOI18N
            setType(OBJECT_TYPE);
            setClazz( "javax.faces.component.UIComponent" );      //NOI18N
        }
    }
    
    static class FlashObject extends FaceletContextObject {
        public FlashObject(){
            super("flash");                                       //NOI18N
            setType(OBJECT_TYPE);
            setClazz( "javax.faces.context.Flash" );              //NOI18N
        }
    }
    
    static class ResourceObject extends FaceletContextObject {
        public ResourceObject(){
            super("resource");                                    //NOI18N
            setType(OBJECT_TYPE);
            setClazz( "javax.faces.application.ResourceHandler" );//NOI18N
        }
    }
    
    static class SessionObject extends FaceletContextObject {
        public SessionObject(){
            super("session");                                    //NOI18N
            setType(OBJECT_TYPE);
            setClazz( Object.class.getCanonicalName() );
        }
    }
    
    static class ViewObject extends FaceletContextObject {
        public ViewObject(){
            super("view");                                       //NOI18N
            setType(OBJECT_TYPE);
            setClazz( "javax.faces.component.UIViewRoot" );      //NOI18N
        }
    }
    
    static class ViewScopeObject extends FaceletContextObject {
        public ViewScopeObject(){
            super("viewScope");                                       //NOI18N
        }
    }

    static class CompositeComponentObject extends ELImplicitObject{
        public CompositeComponentObject(){
            super("cc");                        //NOI18N
            setType(OBJECT_TYPE);
            setClazz(""); //no class will disable the property completion providers //NOI18N
        }
    }

}
