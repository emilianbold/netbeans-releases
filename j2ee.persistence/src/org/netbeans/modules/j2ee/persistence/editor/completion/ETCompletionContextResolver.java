/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.persistence.editor.completion;

import java.io.IOException;
import org.netbeans.modules.j2ee.persistence.editor.completion.db.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import org.eclipse.persistence.jpa.jpql.ContentAssistProposals;
import org.eclipse.persistence.jpa.jpql.JPQLQueryHelper;
import org.eclipse.persistence.jpa.jpql.spi.IEntity;
import org.eclipse.persistence.jpa.jpql.spi.IMapping;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.dbschema.ColumnElement;
import org.netbeans.modules.dbschema.TableElement;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.editor.completion.AnnotationUtils;
import org.netbeans.modules.j2ee.persistence.editor.completion.CompletionContextResolver;
import org.netbeans.modules.j2ee.persistence.editor.completion.CCParser;
import org.netbeans.modules.j2ee.persistence.editor.completion.JPACompletionItem;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.editor.completion.JPACodeCompletionProvider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedTypeProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.Query;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Sergey Petrov
 */
public class ETCompletionContextResolver implements CompletionContextResolver {
    
    private DatabaseConnection dbconn;
    private DBMetaDataProvider provider;
    
    //annotations names handled somehow by this completion context resolver
    private static final String[] ANNOTATION_QUERY_TYPES = {
        "Table", //0
        "SecondaryTable", //1
        "Column", //2
        "PrimaryKeyJoinColumn", //3
        "JoinColumn", //4
        "JoinTable", //5
        "PersistenceUnit", //6
        "PersistenceContext", //7
        "ManyToMany",//8
        "NamedQuery"//9
    };
    
    private static final String PERSISTENCE_PKG = "javax.persistence";
    
    @Override
    public List resolve(JPACodeCompletionProvider.Context ctx) {
        
        List<JPACompletionItem> result = new ResultItemsFilterList(ctx);
        
        //parse the annotation
        String methodName = ctx.getMethod() == null ? null : ctx.getMethod().getMethodName();
        if (methodName == null || !methodName.equals("createNamedQuery")) return result;
        result = completecreateNamedQueryparameters(ctx, result);
        
        
        return result;
    }

    private List<JPACompletionItem> completecreateNamedQueryparameters(JPACodeCompletionProvider.Context ctx, List<JPACompletionItem> results) {
        Project prj = FileOwnerQuery.getOwner(ctx.getFileObject());
        EntityClassScopeProvider provider = (EntityClassScopeProvider) prj.getLookup().lookup(EntityClassScopeProvider.class);
        EntityClassScope ecs = null;
        Entity[] entities = null;
        if (provider != null) {
            ecs = provider.findEntityClassScope(ctx.getFileObject());
        }
        if (ecs != null) {
            try {
                entities = ecs.getEntityMappingsModel(false).runReadAction(new MetadataModelAction<EntityMappingsMetadata, Entity[]>() {
                   @Override
                    public Entity[] run(EntityMappingsMetadata metadata) throws Exception {
                        return metadata.getRoot().getEntity();
                    }
                });
            } catch (MetadataModelException ex) {
            } catch (IOException ex) {
            }
        }
        if(entities != null)
        for (Entity entity : entities) {
            for(NamedQuery nq:entity.getNamedQuery()){
                results.add(new JPACompletionItem.NamedQueryNameItem(nq.getName(), entity.getName(), ctx.getMethod().isWithQ(), ctx.getMethod().getValueOffset()));
            }
        }
        return results;
    }
    
    private static final class ResultItemsFilterList extends ArrayList {
        private JPACodeCompletionProvider.Context ctx;
        public ResultItemsFilterList(JPACodeCompletionProvider.Context ctx) {
            super();
            this.ctx = ctx;
        }
        
        @Override
        public boolean add(Object o) {
            if(!(o instanceof JPACompletionItem)) return false;
            
            JPACompletionItem ri = (JPACompletionItem)o;
            //check if the pretext corresponds to the result item text
            try {
                String preText = ctx.getBaseDocument().getText(ri.getSubstituteOffset(), ctx.getCompletionOffset() - ri.getSubstituteOffset());
                if(!ri.canFilter() || ri.getItemText().startsWith(preText)) {
                    return super.add(ri);
                }
            }catch(BadLocationException ble) {
                //ignore
            }
            return false;
        }
    }
    
    private static final boolean DEBUG = Boolean.getBoolean("debug." + ETCompletionContextResolver.class.getName());
}
