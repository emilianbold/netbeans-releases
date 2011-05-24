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
package org.netbeans.modules.j2ee.persistence.spi.jpql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.eclipse.persistence.jpa.jpql.TypeHelper;
import org.eclipse.persistence.jpa.jpql.spi.IType;
import org.eclipse.persistence.jpa.jpql.spi.ITypeRepository;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper.State;
import org.openide.util.Exceptions;

/**
 *
 * @author sp153251
 */
public class TypeRepository implements ITypeRepository {
    private final Project project;
    private final Map<String, IType> types;
    private PUDataObject dObj;
    private MetadataModelReadHelper<EntityMappingsMetadata, List<org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity>> readHelper;

    TypeRepository(Project project){
        this.project = project;
        types = new HashMap<String, IType>();
        initTypes();
    }
    
    @Override
    public IType getEnumType(String string) {
        IType ret = types.get(string);
        if(ret == null){
            ret = new Type(null, null);
        }
        return ret;
    }

    @Override
    public IType getType(Class<?> type) {
        return types.get(type.getCanonicalName());
    }

    @Override
    public IType getType(String val) {
        return types.get(val);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return new TypeHelper(this);
    }

    private void initTypes() {
        //add only managed types (entities, mapped superclasses etc)
        if(types == null && readHelper==null){
            try {
                dObj = ProviderUtil.getPUDataObject(project);
            } catch (InvalidPersistenceXmlException ex) {
                Exceptions.printStackTrace(ex);
            }
            EntityClassScope eScope = EntityClassScope.getEntityClassScope(dObj.getPrimaryFile());
            MetadataModel<EntityMappingsMetadata> model = eScope.getEntityMappingsModel(true);
            readHelper = MetadataModelReadHelper.create(
                    model, 
                    new MetadataModelAction<EntityMappingsMetadata, List<org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity>>() {
                        @Override
                        public List<org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity> run(EntityMappingsMetadata metadata) {
                            List<org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity> result = new ArrayList<org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity>();
                            result.addAll(Arrays.asList(metadata.getRoot().getEntity()));
                            return result;
                    }
            });
            readHelper.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (readHelper.getState() == State.FINISHED) {
                        try {
                            readHelper.getResult();
                        } catch (ExecutionException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
            readHelper.start();
        }
    }
    
}
