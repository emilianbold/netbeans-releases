/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.netbeans.modules.web.jsf.editor.facelets;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;

public class FaceletsLibrary {

    private boolean compositeLibrary;
    private String namespace;
    private FaceletsLibrarySupport support;
    private final Collection<NamedComponent> components = new ArrayList<NamedComponent>();

    public FaceletsLibrary(FaceletsLibrarySupport support, String namespace) {
        this(support, namespace, false);
    }

    public FaceletsLibrary(FaceletsLibrarySupport support, String namespace, boolean compositeLibrary) {
        this.support = support;
        this.namespace = namespace;
        this.compositeLibrary = compositeLibrary;
    }

    //for default libraries
    void setComponents(Collection<NamedComponent> components) {
        this.components.addAll(components);
    }

    public Collection<NamedComponent> getComponents() {
        return Collections.unmodifiableCollection(components);
    }

    public boolean isCompositeLibrary() {
        return compositeLibrary;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getDefaultPrefix() {
        return getAssociatedTLDLibrary() != null ? getAssociatedTLDLibrary().getDefaultPrefix() : null;
    }

    public String getDisplayName() {
        return getAssociatedTLDLibrary() != null ? getAssociatedTLDLibrary().getDisplayName() : getNamespace();
    }

    public TldLibrary getAssociatedTLDLibrary() {
        return support.getJsfSupport().getTldLibraries().get(getNamespace());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FaceletsLibrary other = (FaceletsLibrary) obj;
        if ((this.namespace == null) ? (other.namespace != null) : !this.namespace.equals(other.namespace)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }


    void putConverter(String name, String id) {
        components.add(new Converter(name, id, null));
    }

    void putConverter(String name, String id, Class handlerClass) {
        components.add(new Converter(name, id, handlerClass));
    }

    void putValidator(String name, String id) {
        components.add(new Validator(name, id, null));
    }

    void putValidator(String name, String id, Class handlerClass) {
        components.add(new Validator(name, id, handlerClass));
    }

    void putBehavior(String name, String id) {
        components.add(new Behavior(name, id, null));
    }

    void putBehavior(String name, String id, Class handlerClass) {
        components.add(new Behavior(name, id, handlerClass));
    }

    void putTagHandler(String name, Class type) {
        components.add(new TagHandler(name, type));
    }

    void putComponent(String name, String componentType,
            String rendererType) {
        components.add(new Component(name, componentType, rendererType, null));
    }

    void putComponent(String name, String componentType,
            String rendererType, Class handlerClass) {
        components.add(new Component(name, componentType, rendererType, handlerClass));
    }

    void putUserTag(String name, URL source) {
        components.add(new UserTag(name, source));
    }

    void putFunction(String name, Method method) {
        components.add(new Function(name, method));
    }

    public NamedComponent createNamedComponent(String name) {
        return new NamedComponent(name);
    }

    public Function createFunction(String name, Method method) {
        return new Function(name, method);
    }

    public class NamedComponent {

        protected String name;

        private NamedComponent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public TldLibrary.Tag getTag() {
            TldLibrary tldLib = getAssociatedTLDLibrary();
            if(tldLib != null) {
                return tldLib.getTags().get(getName());
            } else {
                return null;
            }

        }

        public FaceletsLibrary getLibrary() {
            return FaceletsLibrary.this;
        }

        public String[][] getDescription() {
            return new String[][]{{"name", getName()}}; //NOI18N
        }

        protected String[][] merge(String[][] first, String[][] second) {
            String[][] merged = new String[first.length + second.length][];
            System.arraycopy(first, 0, merged, 0, first.length);
            System.arraycopy(second, 0, merged, first.length, second.length);
            return merged;
        }


    }

    public class BaseComponent extends NamedComponent {

        protected String id;
        protected Class handlerClass;

        private BaseComponent(String name, String id, Class handlerClass) {
            super(name);
            this.id = id;
            this.handlerClass = handlerClass;
        }

        public Class getHandlerClass() {
            return handlerClass;
        }

        public String getId() {
            return id;
        }

        @Override
        public String[][] getDescription() {
            String[][] myDescr = new String[][]{{"id", getId()}, //NOI18N
                                  {"handler class", getHandlerClass() == null ? "N/A" : getHandlerClass().getName()}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }


    }

    public class Converter extends BaseComponent {

        private Converter(String name, String id, Class handlerClass) {
            super(name, id, handlerClass);
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "converter"}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }


    }

    public class Validator extends BaseComponent {

        private Validator(String name, String id, Class handlerClass) {
            super(name, id, handlerClass);
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "validator"}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }
    }

    public class Behavior extends BaseComponent {

        private Behavior(String name, String id, Class handlerClass) {
            super(name, id, handlerClass);
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "behavior"}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }
    }

    public class TagHandler extends NamedComponent {

        protected Class type;

        private TagHandler(String name, Class type) {
            super(name);
            this.type = type;
        }

        public Class getType() {
            return type;
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "tag handler"}, //NOI18N
               {"class type", getType() == null ? "N/A" : getType().getName()}}; //NOI18N
            return merge(super.getDescription(), myDescr); //NOI18N
        }


    }

    public class Component extends NamedComponent {

        protected String componentType;
        protected String rendererType;
        protected Class handlerClass;

        private Component(String name, String componentType, String rendererType, Class handlerClass) {
            super(name);
            this.componentType = componentType;
            this.rendererType = rendererType;
            this.handlerClass = handlerClass;
        }

        public String getComponentType() {
            return componentType;
        }

        public String getRendererType() {
            return rendererType;
        }

        public Class getHandlerClass() {
            return handlerClass;
        }

           @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "component"}, //NOI18N
               {"component type", getComponentType() == null ? "N/A" : getComponentType()}, //NOI18N
               {"renderer type", getRendererType() == null ? "N/A" : getRendererType()}, //NOI18N
               {"handler class", getHandlerClass() == null ? "N/A" : getHandlerClass().getName()}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }


    }

    public class UserTag extends NamedComponent {

        protected URL source;

        private UserTag(String name, URL source) {
            super(name);
            this.source = source;
        }

        public URL getUrl() {
            return source;
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "user tag"}, //NOI18N
               {"URL", getUrl() == null ? "N/A" : getUrl().toExternalForm()}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }


    }

    public class Function extends NamedComponent {

        protected Method function;

        private Function(String name, Method function) {
            super(name);
            this.function = function;
        }

        public Method getFunction() {
            return function;
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "function"}, //NOI18N
               {"function name", getFunction() == null ? "N/A" : getFunction().toGenericString()}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }


    }
}
