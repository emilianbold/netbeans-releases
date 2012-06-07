/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsfapi.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.api.TagFeature;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public interface TagFeatureProvider {

    public <T extends TagFeature> Collection<T> createFeatures(Class<T> clazz, Tag tag);

    public static class Query {

        public static <T extends TagFeature> Collection<T> getFeatures(Class<T> clazz, Tag tag) {
            Collection<? extends TagFeatureProvider> all = Lookup.getDefault().lookupAll(TagFeatureProvider.class);
            Collection<T> query = new ArrayList<T>();
            for (TagFeatureProvider fp : all) {
                query.addAll(fp.createFeatures(clazz, tag));
            }
            return query;
        }
    }

//    //example of usage
//    public class Client {
//        public void giveitatry() {
//            Tag t = null;
//            Collection<TagFeature.IterableTagPattern> features =
//                    TagFeatureProvider.Query.getFeatures(TagFeature.IterableTagPattern.class, t);
//            for(TagFeature.IterableTagPattern f : features) {
//                System.out.println("var=" + f.getVar().getName());
//                System.out.println("variable=" + f.getVariables().getName());
//            }
//            
//        }
//    } 
    
    
//    //just an example of the provider: will be placed into a reasonable module once the 
//    public class MyTagFeatureProvider implements TagFeatureProvider {
//
//        @Override
//        public <T extends TagFeature> Collection<T> createFeatures(Class<T> clazz, final Tag tag) {
//            if (clazz.equals(TagFeature.IterableTagPattern.class)) {
//                if (supportsThePattern(tag)) {
//                    return Collections.singleton(clazz.cast(new TagFeature.IterableTagPattern() {
//                        @Override
//                        public Attribute getVar() {
//                            return tag.getAttribute("var");
//                        }
//
//                        @Override
//                        public Attribute getVariables() {
//                            return tag.getAttribute("variable");
//                        }
//                    }));
//                }
//
//            }
//            return Collections.emptyList();
//        }
//    }
    
    
    
}
