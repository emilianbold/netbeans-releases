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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.spi.jumpto.type;

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jumpto.type.TypeProviderAccessor;

/**
 * A Type Provider participates in the Goto Type dialog by providing TypeDescriptors,
 * one for each matched type, when asked to do so.
 * 
 * The Type Providers are registered in Lookup.
 * 
 * @todo Should we return a Collection rather than a List?
 * 
 * @author Tor Norbye
 */
public interface TypeProvider {
    /** 
     * Describe this provider with an internal name, in case we want to provide
     * some kind of programmatic filtering (e.g. a Java EE dialog wanting to include
     * or omit specific type providers, without relying on class names or 
     * localized display names)
     * 
     * @return An internal String uniquely identifying this type provider, such as
     *   "java"
     */
    String name();

    /** 
     * Describe this provider for the user, in case we want to offer filtering
     * capabilities in the Go To Type dialog
     * 
     * @return A display name describing the types being provided by this TypeProvider,
     *  such as "Java Types", "Ruby Types", etc.
     */
    String getDisplayName();
    
    /** 
     * Compute a list of TypeDescriptors that match the given search text for the given
     * search type. This might be a slow operation, and the infrastructure may end
     * up calling {@link #cancel} on the same type provider during the operation, in which
     * case the method can return incomplete results. If there is a "current project",
     * the Go To Type infrastructure will perform the search in two passes; first it
     * will call {@link #getTypeNames} with the current project, which should be a reasonably
     * fast search, and display those types first. It will then call the method again
     * with a null project, which should return all types.
     * <p>
     * Note that a useful performance optimization is for the TypeProvider to cache
     * a few of its most recent search results, and if the next search (e.g. more user
     * keystrokes) is a simple narrowing of the search, just filter the previous search
     * result. There is an explicit {@link #cleanup} call that the Go To Type dialog
     * will make at the end of the dialog interaction, which can be used to clean up the cache.
     * 
     * @param context search context containg search text and type, optionally project
     * @param result  filled with type descriptors and optional message
     */
    void computeTypeNames(Context context, Result result);
    
    /**
     * Cancel the current operation, if possible. This might be called if the user
     * has typed something (including the backspace key) which makes the current
     * search obsolete and a new one should be initiated.
     */
    void cancel();


    /**
     * The Go To Type dialog is dismissed for now - free up resources if applicable.
     * (A new "session" will be indicated by a new call to getTypeNames.)
     * 
     * This allows the TypeProvider to cache its most recent search result, and if the next
     * search is simply a narrower search, it can just filter the previous result.
     */
    void cleanup();


    /**
     * Represents search context.
     * Contains search type (such as prefix, regexp), search text and
     * optionally project where to search.
     *
     * @since 1.5
     */
    public static final class Context extends Object {
        private final Project project;
        private final String text;
        private final SearchType type;
        
        static {
            TypeProviderAccessor.DEFAULT = new TypeProviderAccessor() {
                @Override
                public Context createContext(Project p, String text, SearchType t) {
                    return new Context(p, text, t);
                }

                @Override
                public Result createResult(List<? super TypeDescriptor> result, String[] message) {
                    return new Result(result, message);
                }
            };
        }
        
        Context(Project project, String text, SearchType type) {
            this.project = project;
            this.text = text;
            this.type = type;
        }
        
        /**
         * Return project representing scope of search, if null, the search is not
         * limited.
         *
         * @return project If not null, the type search is limited to the given project.
         */
        public Project getProject() { return project; }

        /**
          * Return the text used for search.
          *
          * @return The text used for the search; e.g. when getSearchType() == SearchType.PREFIX,
          *   text is the prefix that all returned types should start with.
          */
        public String getText() { return text; }

        /**
         * Return the type of search.
         *
         * @return Type of search performed, such as prefix, regexp or camel case.
         */
        public SearchType getSearchType() { return type; }
    }
    
    /**
     * Represents a collection of <tt>TypeDescriptor</tt>s that match 
     * the given search criteria. Moreover, it can contain message 
     * for the user, such as an incomplete search result.
     *
     * @since 1.5
     */
    public static final class Result extends Object {
        
        private List<? super TypeDescriptor> result;
        private String[] message;

        Result(List<? super TypeDescriptor> result, String[] message) {
            this.result = result;
            this.message = message;
        }
        
        /**
         * Optional message. It can inform the user about result, e.g.
         * that result can be incomplete etc.
         * 
         * @param  msg  message
         */
        public void setMessage(String msg) {
            message[0] = msg;
        }

        /**
          * Adds result descriptor.
          *
          * @param  typeDescriptor  type descriptor to be added to result
          */
        public void addResult(TypeDescriptor typeDescriptor) {
            result.add(typeDescriptor);
        }

        /**
          * Adds list of result descriptors.
          *
          * @param  typeDescriptor  type descriptor to be added to result
          */
        @SuppressWarnings("unchecked")
        public void addResult(List<? extends TypeDescriptor> typeDescriptor) {
            ((List)result).addAll(typeDescriptor);  //workaround javac issue http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6507334
        }
    }

}
