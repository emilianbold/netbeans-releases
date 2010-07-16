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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.mapper.cast;

import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.ext.editor.api.LocationStepModifier;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperLsm;

/**
 * Marker interface for AbstractTypeCast, AbstractPseudoComp and AbstractPredicate
 * It is necessary for support nesting.
 * The interface is an analogue of LocationStepModifier, which is defined in
 * the BPEL model.
 * 
 * @author Nikita Krjukov
 */
public interface BpelMapperLsm extends MapperLsm {

    /**
     * This method compares a BPEL model's LSM with mapper defined LSM.
     * It is very helpful while looking for an LSM by a Mapper LSM and vice versa.
     * It ignores location or schema context because it is intended to
     * compare LSMs located at the same layer. Comparing location can be very 
     * time consuming. Such way it helps optimizing search.
     *
     * TODO: WARNING! This approach isn't absolutely reliable.
     * But it is difficult to get rid of the approach. It requies significant
     * refactoring for now. So this method is a candidate to be deprecated.
     * 
     * @param lsm
     * @return
     */
    boolean equalsIgnoreLocation(LocationStepModifier lsm);

    /**
     * Calculates the variable, on base which the predicate is found.
     * The variable is intended to be used as a place where the BPEL Editor
     * extension should be located.
     * @return
     */
    VariableDeclaration getBaseBpelVariable();

}
