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

package org.netbeans.modules.bpel.mapper.predicates;

import java.util.List;
import java.util.Stack;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperLsm;
import org.netbeans.modules.bpel.mapper.model.BpelMapperLsmProcessor;
import org.netbeans.modules.bpel.mapper.model.BpelMapperLsmProcessor.MapperLsmConvertor;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.model.BpelPathConverter;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.LocationStepModifier;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmContainer;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmProcessor;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmProcessor.CastWrapperResolver;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmProcessor.PseudoWrapperResolver;
import org.netbeans.modules.bpel.model.ext.editor.api.NestedExtensionsVisitor;
import org.netbeans.modules.bpel.model.ext.editor.api.Predicate;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperXPathCastResolver;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.soa.xpath.mapper.utils.XPathMapperUtils;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * This visitor iterates over LSMs hierarchy and modifies expression
 * of the specified predicate.
 *
 * @author Nikita Krjukov
 */
public class BpelPredicatesModificator implements NestedExtensionsVisitor {

    private BpelEntity mSelectedEntity;
    private MapperLsmConvertor mConverter;

    private ExtensibleElements mOwner;

    private XPathPredicateExpression[] mNewPredArr;
    private PredicatedSchemaContext mPredSContext;
    private DirectedList<Object> mPredicatePath;
    private boolean mInLeftTree;

    private Stack<XPathCastResolver> mResolverStack =
                new Stack<XPathCastResolver>();

    public BpelPredicatesModificator(MapperTcContext tcContext) {
        mSelectedEntity = tcContext.getDesignContextController().
                getContext().getSelectedEntity();
        mConverter = new MapperLsmConvertor();
    }

    /**
     * It's implied to be executed inside of BPEL transaction.
     * @param owner
     * @param predSContext
     * @param inLeftTree
     * @param newPredTextArr
     * @throws org.netbeans.modules.bpel.model.api.events.VetoException
     * @throws StopModificationException
     */
    public void modify(ExtensibleElements owner,
            PredicatedSchemaContext predSContext, boolean inLeftTree,
            XPathPredicateExpression[] newPredArr) throws VetoException {
        //
        mOwner = owner;
        mPredSContext = predSContext;
        mNewPredArr = newPredArr;
        mInLeftTree = inLeftTree;
        //
        Editor editor = LsmProcessor.getEditorEntity(owner, null, false);
        if (editor == null) {
            return; // Nothing to modify
        }
        //
        mPredicatePath = BpelPathConverter.singleton().
                constructObjectLocationList(predSContext, true, false);
        LocationStepModifier bpelPredicate = BpelMapperLsmProcessor.findBpelLsm(
                editor, mPredicatePath, owner, inLeftTree);
        //
        if (bpelPredicate != null && bpelPredicate instanceof Predicate) {
            Predicate rootPredicate = (Predicate)bpelPredicate;
            //
            PredicatedSchemaContext oldPredSContextCopy = predSContext.clone();
            oldPredSContextCopy.setPredicateExpressions(newPredArr);
            String newPathText = oldPredSContextCopy.getExpressionString(
                    owner.getNamespaceContext(),
                    null); // TODO: Null value should be replaced
            rootPredicate.setPath(newPathText);
            //
            // Set initial value to Cast resolver stack.
            DirectedList<BpelMapperLsm> location = XPathMapperUtils.extractLsms(
                    mPredicatePath, BpelMapperLsm.class);
            if (location != null && !location.isEmpty()) {
                MapperXPathCastResolver castResolver =
                        new MapperXPathCastResolver(location);
                //
                visitNestedExt(castResolver, (Predicate)bpelPredicate);
            }
        }
    }

    public void visit(Editor editor) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void visit(Cast cast) {
        //
        Source source = cast.getSource();
        if (!((mInLeftTree && source == Source.FROM) ||
                (!mInLeftTree && source == Source.TO))) {
            // Skip cast if it doesn't corespond to source
            return;
        }
        //
        XPathCastResolver parentResolver = mResolverStack.isEmpty() ?
            null : mResolverStack.peek();
        XPathCast xPathCast = mConverter.processCast(cast, parentResolver);
        if (xPathCast == null) {
            return;
        }
        //
        // Process children first because the schema context is going to be changed.
        CastWrapperResolver newResolver =
                    new CastWrapperResolver(xPathCast, parentResolver);
        visitNestedExt(newResolver, cast);
        //
        XPathSchemaContext sContext = xPathCast.getSchemaContext();
        if (sContext == null) {
            return;
        }
        //
        BpelPredicateManager.modifyPredicateInSContext(sContext, mPredSContext, mNewPredArr);
        String exprText = sContext.getExpressionString(
                mOwner.getNamespaceContext(), null);
        //
        try {
            cast.setPath(exprText);
        } catch (VetoException ex) {
            throw new StopModificationException(ex);
        }
    }

    public void visit(PseudoComp pseudoComp) {
        //
        Source source = pseudoComp.getSource();
        if (!((mInLeftTree && source == Source.FROM) ||
                (!mInLeftTree && source == Source.TO))) {
            // Skip cast if it doesn't corespond to source
            return;
        }
        //
        XPathCastResolver parentResolver = mResolverStack.isEmpty() ?
            null : mResolverStack.peek();
        XPathPseudoComp xPathPseudo =
                mConverter.processPseudo(pseudoComp, parentResolver);
        if (xPathPseudo == null) {
            return;
        }
        //
        // Process children first because the schema context is going to be changed.
        PseudoWrapperResolver newResolver =
                    new PseudoWrapperResolver(xPathPseudo, parentResolver);
        visitNestedExt(newResolver, pseudoComp);
        //
        XPathSchemaContext sContext = xPathPseudo.getSchemaContext();
        if (sContext == null) {
            return;
        }
        //
        BpelPredicateManager.modifyPredicateInSContext(sContext, mPredSContext, mNewPredArr);
        String exprText = sContext.getExpressionString(
                mOwner.getNamespaceContext(), null);
        //
        try {
            pseudoComp.setParentPath(exprText);
        } catch (VetoException ex) {
            throw new StopModificationException(ex);
        }
    }

    public void visit(Predicate predicate) {
        //
        Source source = predicate.getSource();
        if (!((mInLeftTree && source == Source.FROM) ||
                (!mInLeftTree && source == Source.TO))) {
            // Skip cast if it doesn't corespond to source
            return;
        }
        //
        XPathCastResolver parentResolver = mResolverStack.isEmpty() ?
            null : mResolverStack.peek();
        XPathSchemaContextHolder predCtxtHolder = mConverter.processPredicate(
                predicate, parentResolver, mSelectedEntity);
        //
        // Process children first because the schema context is going to be changed.
        visitNestedExt(predicate);
        //
        if (predCtxtHolder == null) {
            return;
        }
        //
        XPathSchemaContext sContext = predCtxtHolder.getSchemaContext();
        if (sContext == null) {
            return;
        }
        //
        BpelPredicateManager.modifyPredicateInSContext(sContext, mPredSContext, mNewPredArr);
        String exprText = sContext.getExpressionString(
                mOwner.getNamespaceContext(), null);
        //
        try {
            predicate.setPath(exprText);
        } catch (VetoException ex) {
            throw new StopModificationException(ex);
        }
    }

    protected void visitNestedExt(XPathCastResolver castResolver, LsmContainer cont) {
        assert castResolver != null;
        //
        mResolverStack.push(castResolver);
        try {
            visitNestedExt(cont);
        } finally {
            mResolverStack.pop();
        }
    }

    protected void visitNestedExt(LsmContainer cont) {
        List<LocationStepModifier> nestedExtList = cont.getChildrenLsm(null);
        if (nestedExtList != null && !nestedExtList.isEmpty()) {
            for (LocationStepModifier nestedExt : nestedExtList) {
                if (nestedExt instanceof LsmContainer) {
                    ((LsmContainer)nestedExt).accept(this);
                }
            }
        }
    }

    public static class StopModificationException extends RuntimeException {
        public StopModificationException() {
            super();
        }

        public StopModificationException(String msg) {
            super(msg);
        }

        public StopModificationException(Throwable cause) {
            super(cause);
        }

    }
}
