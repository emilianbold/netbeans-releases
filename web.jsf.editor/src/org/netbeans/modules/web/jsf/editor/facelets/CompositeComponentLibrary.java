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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.facelets;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.modules.web.jsf.editor.index.CompositeComponentModel;
import org.netbeans.modules.web.jsf.editor.index.JsfIndex;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.LibraryType;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class CompositeComponentLibrary extends FaceletsLibrary {

    /**
     * Name of the folder/s where the composite library components are located. 
     * It's usually placed under the META-INF/resources folder inside the library archive.
     */
    private final String compositeLibraryResourceFolderName;
    private final String defaultPrefix;

    //for cc libraries with facelets library descriptor, the constructor is called by Mojarra
    public CompositeComponentLibrary(FaceletsLibrarySupport support, String compositeLibraryName, String namespace, URL libraryDescriptorURL) {
        super(support, namespace, libraryDescriptorURL);

        this.compositeLibraryResourceFolderName = compositeLibraryName;

        //the default prefix is always computed from the composite library location
        //since even if there's a descriptor for the library, it doesn't contain
        //such information
        this.defaultPrefix = generateVirtualLibraryPrefix();
    }

    @Override
    protected LibraryDescriptor getFaceletsLibraryDescriptor() throws LibraryDescriptorException {
        //return a composite (merged) descriptor from the xml descriptor and the composite components themselves
        return new CompositeLibraryDescriptor(super.getFaceletsLibraryDescriptor(), new CCVirtualLibraryDescriptor());
    }

    @Override
    public LibraryType getType() {
        return LibraryType.COMPOSITE;
    }

    @Override
    public String getDefaultNamespace() {
        return LibraryUtils.getCompositeLibraryURL(getLibraryName(), support.getJsfSupport().isJsf22Plus());
    }

    @Override
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public String getLibraryName() {
        return compositeLibraryResourceFolderName;
    }

    @Override
    public Map<String, ? extends NamedComponent> getComponentsMap() {
        //add the composite components to the class components
        Map<String, NamedComponent> all = new HashMap<String, NamedComponent>(super.getComponentsMap());
        all.putAll(getCompositeComponentsMap());
        return all;
    }

    private Map<String, CompositeComponent> getCompositeComponentsMap() {
        Map<String, CompositeComponent> ccomponents = new HashMap<String, CompositeComponent>();
        Collection<String> componentNames = index().getCompositeLibraryComponents(getLibraryName());
        for (String compName : componentNames) {
            CompositeComponent comp = new CompositeComponent(compName);
            ccomponents.put(compName, comp);
        }
        return ccomponents;
    }

    private JsfIndex index() {
        return support.getJsfSupport().getIndex();
    }

    private String generateVirtualLibraryPrefix() {
        StringTokenizer st = new StringTokenizer(getLibraryName(), "/"); //NOI18N
        LinkedList<String> tokens = new LinkedList<String>();
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }

        //one or more tokens left
        if (tokens.size() == 1) {
            //just library folder
            return tokens.peek();
        } else {
            //more folders
            StringBuilder sb = new StringBuilder();
            for (String folderName : tokens) {
                sb.append(folderName.charAt(0)); //add first char
            }
            return sb.toString();
        }

    }

    public class CompositeComponent extends NamedComponent {

        public CompositeComponent(String name) {
            super(name);
        }

        public CompositeComponentModel getComponentModel() {
            return index().getCompositeComponentModel(getLibraryName(), name);
        }
    }

    protected class CCVirtualLibraryDescriptor implements LibraryDescriptor {

        @Override
        public Map<String, Tag> getTags() {
            Map<String, Tag> map = new HashMap<String, Tag>();
            Collection<CompositeComponent> components = getCompositeComponentsMap().values();
            for (CompositeComponent cc : components) {
                map.put(cc.getName(), new LazyLoadingTag(cc));
            }
            return map;
        }

        @Override
        public String getNamespace() {
            return CompositeComponentLibrary.this.getNamespace();
        }

        @Override
        public String getPrefix() {
            return null;
        }
        
        private class LazyLoadingTag extends GenericTag {

            private CompositeComponent cc;
            private Map<String, Attribute> attrs;
            private String description;

            public LazyLoadingTag(CompositeComponent cc) {
                this.cc = cc;
            }

            private synchronized void load() {
                CompositeComponentModel model = cc.getComponentModel();
                if (model == null) {
                    return;
                }
                String relativePath = model.getRelativePath();

                attrs = new HashMap<String, Attribute>();
                String msgNoTld = NbBundle.getBundle(CompositeComponentLibrary.class).getString("MSG_NO_DESCRIPTOR"); //NOI18N
                for (Map<String, String> attrsMap : model.getExistingInterfaceAttributes()) {
                    String attrname = attrsMap.get("name"); //NOI18N
                    boolean required = Boolean.parseBoolean(attrsMap.get("required")); //NOI18N
                    String attributeDescription = getAttributesDescription(model, true);
                    attrs.put(attrname, new Attribute.DefaultAttribute(attrname, attributeDescription, required));
                }

                StringBuilder sb = new StringBuilder();
                
                if(model.getDisplayName() != null) {
                    sb.append("<p>").append("<b>")
                    .append(NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_COMPOSITE_COMPONENT_DISPLAYNAME"))
                    .append("</b>").append("&nbsp;")
                    .append(model.getDisplayName())
                    .append("</p>"); //NOI18N
                }
                
                if(model.getShortDescription() != null) {
                    sb.append("<p>").append("<b>")
                    .append(NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_COMPOSITE_COMPONENT_SHORTDESCRIPTION"))
                    .append("</b>").append("&nbsp;")
                    .append(model.getShortDescription())
                    .append("</p>"); //NOI18N
                }
                
                sb.append("<p>"); //NOI18N
                sb.append("<b>");//NOI18N
                sb.append(NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_COMPOSITE_COMPONENT_SOURCE"));//NOI18N
                sb.append("</b>");//NOI18N
                sb.append("&nbsp;");//NOI18N
                sb.append(relativePath);
                sb.append("</p>");//NOI18N
                
                sb.append("<p>");//NOI18N
                sb.append(getAttributesDescription(model, false));
                sb.append("</p>");//NOI18N
                sb.append("<p style=\"color: red\">").append(msgNoTld).append("</p>"); //NOI18N

                description = sb.toString();
            }

            private String getAttributesDescription(CompositeComponentModel model, boolean includeNoDescriptorMsg) {
                if (model.getExistingInterfaceAttributes().isEmpty()) {
                    return NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_NO_TAG_ATTRS");//NOI18N
                }

                StringBuilder sb = new StringBuilder();
                sb.append("<b>");//NOI18N
                sb.append(NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_TAG_ATTRS"));//NOI18N
                sb.append("</b>");//NOI18N
                sb.append("<table border=\"1\">"); //NOI18N

                for (Map<String, String> descr : model.getExistingInterfaceAttributes()) {
                    //first generate entry for the attribute name
                    sb.append("<tr>"); //NOI18N
                    sb.append("<td>"); //NOI18N
                    sb.append("<div style=\"font-weight: bold\">"); //NOI18N
                    String attrname = descr.get("name"); //NOI18N);
                    sb.append(attrname);
                    sb.append("</div>"); //NOI18N
                    sb.append("</td>"); //NOI18N

                    //then for the rest of the attributes, except the "name" atttribute
                    if (descr.size() > 1) {
                        sb.append("<td>"); //NOI18N
                        sb.append("<table border=\"0\" padding=\"0\" margin=\"0\" spacing=\"2\">"); //NOI18N
                        for (String key : descr.keySet()) {
                            if (key.equals("name")) {//NOI18N
                                continue; //skip name
                            }
                            String val = descr.get(key);
                            sb.append("<tr><td><b>");//NOI18N
                            sb.append(key);
                            sb.append("</b></td><td>");//NOI18N
                            sb.append(val);
                            sb.append("</td></tr>");//NOI18N
                        }
                        sb.append("</table>"); //NOI18N


                        sb.append("</td>"); //NOI18N
                    }
                    sb.append("</tr>"); //NOI18N
                }
                sb.append("</table>"); //NOI18N

                if (includeNoDescriptorMsg) {
                    String msgNoDescriptor = NbBundle.getBundle(CompositeComponentLibrary.class).getString("MSG_NO_DESCRIPTOR"); //NOI18N
                    sb.append("<p style=\"color: red\">").append(msgNoDescriptor).append("</p>");
                } //NOI18N

                return sb.toString();
            }

            @Override
            public String getName() {
                return cc.getName();
            }

            @Override
            public String getDescription() {
                load();
                return description;
            }

            @Override
            public boolean hasNonGenenericAttributes() {
                load();
                return !attrs.isEmpty();
            }

            @Override
            public Collection<Attribute> getAttributes() {
                load();
                //merge with default attributes
                Collection<Attribute> all = new ArrayList<Attribute>(super.getAttributes());
                all.addAll(attrs.values());
                return all;
            }

            @Override
            public Attribute getAttribute(String name) {
                load();
                Attribute superA = super.getAttribute(name);
                return superA != null ? superA : attrs.get(name);
            }
        }
    }
}
