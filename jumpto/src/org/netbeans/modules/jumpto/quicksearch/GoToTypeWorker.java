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

package org.netbeans.modules.jumpto.quicksearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.netbeans.modules.jumpto.EntityComparator;
import org.netbeans.modules.jumpto.type.TypeComparator;
import org.netbeans.modules.jumpto.type.TypeProviderAccessor;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.util.Lookup;

/**
 * Copy/paste from GoToTypeAction
 * @author  Jan Becicka
 */
public class GoToTypeWorker implements Runnable {

    private volatile boolean isCanceled = false;
    private final String text;
        private Logger LOGGER = Logger.getLogger(GoToTypeWorker.class.getName());

    private final long createTime;

    public GoToTypeWorker( String text ) {
        this.text = text;
        this.createTime = System.currentTimeMillis();
            //LOGGER.fine( "Worker for " + text + " - created after " + ( System.currentTimeMillis() - panel.time ) + " ms."  );
    }

    private List<? extends TypeDescriptor> types;

    public List<? extends TypeDescriptor> getTypes() {
        return types==null?Collections.<TypeDescriptor>emptyList():types;
    }

    public void run() {

            LOGGER.fine( "Worker for " + text + " - started " + ( System.currentTimeMillis() - createTime ) + " ms."  );

        types = getTypeNames( text );
        if ( isCanceled ) {
                LOGGER.fine( "Worker for " + text + " exited after cancel " + ( System.currentTimeMillis() - createTime ) + " ms."  );
            return;
        }
//            ListModel model = Models.fromList(types);
//            if (typeFilter != null) {
//                model = LazyListModel.create(model, GoToTypeAction.this, 0.1, "Not computed yet");;
//            }
        if ( isCanceled ) {
                LOGGER.fine( "Worker for " + text + " exited after cancel " + ( System.currentTimeMillis() - createTime ) + " ms."  );
            return;
        }

//            if ( !isCanceled && model != null ) {
//                LOGGER.fine( "Worker for text " + text + " finished after " + ( System.currentTimeMillis() - createTime ) + " ms."  );
//
//                panel.setModel(model);
//                if (okButton != null && !types.isEmpty()) {
//                    okButton.setEnabled (true);
//                }
//            }


    }

    public void cancel() {
//            if ( panel.time != -1 ) {
//                LOGGER.fine( "Worker for text " + text + " canceled after " + ( System.currentTimeMillis() - createTime ) + " ms."  );
//            }

        isCanceled = true;
    }

    @SuppressWarnings("unchecked")
    private List<? extends TypeDescriptor> getTypeNames(String text) {
        // Multiple providers: merge results
        List<TypeDescriptor> items = new ArrayList<TypeDescriptor>(128);
        List<TypeDescriptor> ccItems = new ArrayList<TypeDescriptor>(128);
        String[] message = new String[1];

        TypeProviderAccessor tpa = TypeProviderAccessor.DEFAULT;

        TypeProvider.Context context =
              tpa.createContext(null, text, SearchType.CASE_INSENSITIVE_PREFIX);
        TypeProvider.Result result = tpa.createResult(items, message);

        TypeProvider.Context ccContext =
              tpa.createContext(null, text, SearchType.CAMEL_CASE);
        TypeProvider.Result ccResult = tpa.createResult(ccItems, message);

        Collection<TypeProvider> providers = (Collection<TypeProvider>)
                Lookup.getDefault().lookupAll(TypeProvider.class);

        try {
            computeTypeNames(providers, context, result);
            computeTypeNames(providers, ccContext, ccResult);
            if (isCanceled) throw new IllegalStateException();
        } catch(IllegalStateException ise) {
            return null; // canceled
        }

        //time = System.currentTimeMillis();

        // TypeComparatorFO is used to avoid duplication of entries
        TreeSet<TypeDescriptor> ts =
                new TreeSet<TypeDescriptor>(new TypeComparatorFO());
        ts.addAll(ccItems);
        ts.addAll(items);
        items.clear();
        items.addAll(ts); //eliminate duplicates
        Collections.sort(items, new TypeComparator());
        //panel.setWarning(message[0]);
        //sort += System.currentTimeMillis() - time;
        //LOGGER.fine("PERF - " + " GSS:  " + gss + " GSB " + gsb + " CP: " + 
        //            cp + " SFB: " + sfb + " GTN: " + gtn + "  ADD: " + add +
        //            "  SORT: " + sort );
        return items;
    }

    /**
     * Computes type names via specified collection of the {@code providers}.
     * @param providers the providers.
     * @param context the search context.
     * @param result the search result.
     * @throws IllegalStateException if operation is canceled.
     */
    private void computeTypeNames(Collection<TypeProvider> providers,
                                  TypeProvider.Context context,
                                  TypeProvider.Result result)
                                  throws IllegalStateException {

        for (TypeProvider provider : providers) {
            if (isCanceled) throw new IllegalStateException();
            provider.computeTypeNames(context, result);
        }
    }

    private class TypeComparatorFO extends EntityComparator<TypeDescriptor> {

        @Override
        public int compare(TypeDescriptor t1, TypeDescriptor t2) {
            int cmpr = compare(t1.getTypeName(), t2.getTypeName());
            if (cmpr != 0) {
                return cmpr;
            }
            cmpr = compare(t1.getOuterName(), t2.getOuterName());
            if (cmpr != 0) {
                return cmpr;
            }
            //FileObject does not have to be available
            //if t1 fo is not null and t2 not null => -1
            //t1 fo null => no check
            if((t1.getFileObject() != null) &&
                    !t1.getFileObject().equals(t2.getFileObject()))
                return -1;

            return compare(t1.getContextName(), t2.getContextName());
        }

    } // TypeComparatorFO

}
