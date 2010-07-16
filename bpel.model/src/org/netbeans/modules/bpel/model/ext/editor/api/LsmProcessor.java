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

package org.netbeans.modules.bpel.model.ext.editor.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.support.XPathCastImpl;
import org.netbeans.modules.bpel.model.api.support.XPathPseudoCompImpl;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.w3c.dom.Element;

/**
 * Utility class for LocationStepModifier extensions
 *
 * @author Nikita Krjukov
 */
public class LsmProcessor {

    private LsmProcessor() {}

    /**
     * Clear empty Editor extensions from the destination.
     * @param destination
     */
    public static void clearEmptyEditorEntity(ExtensibleElements destination) {
        List<Editor> editorExt = destination.getChildren(Editor.class);
        for (Editor editor : editorExt) {
            if (clearEmptyContainer(editor)) {
                destination.remove(editor);
            }
        }
    }

    /**
     * Recursively deletes empty containers.
     * The method has to be called inside of transaction.
     * @param container
     * @return true if all children were deleted.
     */
    private static boolean clearEmptyContainer(BpelContainer container) {
        List<BpelEntity> children = container.getChildren();
        int initialSize = children.size();
        int deletedChildCount = 0;
        for (BpelEntity child : children) {
            // Don't remove it if it is not empty.
            Element el = child.getPeer();
            if (el.hasChildNodes() || el.hasAttributes()) {
                continue;
            }
            //
            if (child instanceof BpelContainer) {
                if (clearEmptyContainer((BpelContainer)child)) {
                    container.remove(child);
                    deletedChildCount++;
                }
            }
        }
        //
        return deletedChildCount == initialSize;
    }

    /**
     * Looks for the Editor extension and create it if necessary.
     * @param destination the owner of the Editor extension.
     * @param builder can be null.
     * @param create indicates if it necessary to create the Editor if it's absent.
     * @return
     */
    public static Editor getEditorEntity(ExtensibleElements destination,
            BPELElementsBuilder builder, boolean create) {
        //
        Editor editor = null;
        List<Editor> editorList = destination.getChildren(Editor.class);
        if (editorList != null && !editorList.isEmpty()) {
            editor = editorList.get(0);
        }
        //
        if (editor == null) {
            if (!create) {
                return null;
            }
            //
            if (builder == null) {
                builder = destination.getBpelModel().getBuilder();
            }
            Editor newEditor = builder.createExtensionEntity(Editor.class);
            editor = newEditor;
            destination.addExtensionEntity(Editor.class, editor);
        }
        //
        return editor;
    }

    public static void deleteAllNestedExtensions(ExtensibleElements destination) {
        Editor editor = LsmProcessor.getEditorEntity(destination, null, false);
        if (editor != null) {
            NestedExtensionsVisitor extCollector =
                    new NestedExtensionsVisitor.ForwardTracer() {

                @Override
                public void visit(Cast cast) {
                    getParentContainer().removeExtension(cast);
                }

                @Override
                public void visit(PseudoComp pseudoComp) {
                    getParentContainer().removeExtension(pseudoComp);
                }

                @Override
                public void visit(Predicate preducate) {
                    getParentContainer().removeExtension(preducate);
                }
            };
            //
            editor.accept(extCollector);
        }
    }
    
    /**
     *
     * @deprecated This method comes to IllegalAttributeException in XDM
     * It is recommended to escape using it until the error in model is fixed
     *
     * It is implied that it is called in a BPEL transaction.
     * @param owner
     */
    public static void deleteAllLsm(ExtensibleElements owner) {
        if (owner == null) {
            return;
        }
        //
        Editor editorExt = LsmProcessor.getEditorEntity(owner, null, false);
        if (editorExt == null) {
            return;
        }
        //
        List<LocationStepModifier> allLsmList = editorExt.getChildrenLsm(null);
        for (LocationStepModifier lsm : allLsmList) {
            editorExt.removeExtension(lsm);
        }
    }

    //------------------------------------------------------------------------

    public static class XPathCastResolverImpl implements XPathCastResolver {

        private XPathLsmContainer mLsmContainer;
        private boolean mUseFrom;

        public XPathCastResolverImpl(XPathLsmContainer lsmCont, boolean useFrom) {
            assert lsmCont != null;
            mLsmContainer = lsmCont;
            mUseFrom = useFrom;
        }

        public List<XPathCast> getXPathCasts() {
            throw new UnsupportedOperationException("deprecated"); // NOI18N
        }

        public XPathCast getCast(XPathSchemaContext baseSContext) {
            //
            List<XPathCast> xPathCastList = mUseFrom ?
                mLsmContainer.getFromCasts() : mLsmContainer.getToCasts();
            for (XPathCast xPathCast : xPathCastList) {
                XPathSchemaContext sContext = xPathCast.getSchemaContext();
                assert sContext instanceof CastSchemaContext;
                if (sContext != null && sContext instanceof CastSchemaContext) {
                    XPathSchemaContext castBaseContext =
                            ((CastSchemaContext)sContext).getBaseContext();
                    if (castBaseContext != null &&
                            castBaseContext.equalsChain(baseSContext)) {
                        return xPathCast;
                    }
                }
            }
            //
            return null;
        }

        public List<XPathPseudoComp> getPseudoCompList(XPathSchemaContext parentSContext) {
            ArrayList<XPathPseudoComp> result = new ArrayList<XPathPseudoComp>();
            //
            List<XPathPseudoComp> xPathPseudoCompList = mUseFrom ?
                mLsmContainer.getFromPseudoComps() :
                mLsmContainer.getToPseudoComps();
            //
            for (XPathPseudoComp xPseudoComp : xPathPseudoCompList) {
                //
                // Filter by location
                XPathSchemaContext sContext = xPseudoComp.getSchemaContext();
                if (sContext != null && sContext.equalsChain(parentSContext)) {
                    result.add(xPseudoComp);
                }
            }
            //
            return result;
        }
    }

    /**
     * It is an auxiliary container for building complex XPathCastResolver.
     * It collects all PseudoComp and TypeCast in a form which acceptable
     * by XPath model.
     */
    public static class XPathLsmContainer {

        private ArrayList<XPathCast> mFromCasts;
        private ArrayList<XPathCast> mToCasts;
        private ArrayList<XPathPseudoComp> mFromPseudoComps;
        private ArrayList<XPathPseudoComp> mToPseudoComps;

        public XPathLsmContainer() {
            mFromCasts = new ArrayList<XPathCast>();
            mToCasts = new ArrayList<XPathCast>();
            mFromPseudoComps = new ArrayList<XPathPseudoComp>();
            mToPseudoComps = new ArrayList<XPathPseudoComp>();
        }

        public List<XPathCast> getFromCasts() {
            return mFromCasts;
        }

        public List<XPathCast> getToCasts() {
            return mToCasts;
        }

        public List<XPathPseudoComp> getFromPseudoComps() {
            return mFromPseudoComps;
        }

        public List<XPathPseudoComp> getToPseudoComps() {
            return mToPseudoComps;
        }

        @Override
        public String toString() {
            return "FromCast: " + mFromCasts +
                    " FromPseudoComp: " + mFromPseudoComps +
                    " ToCast: " + mToCasts +
                    " ToPseudoComp: " + mToPseudoComps;
        }

    }

    public static class XPathLsmConvertor implements LsmConverter {

        private XPathLsmContainer mContainer;

        public XPathLsmConvertor(XPathLsmContainer container) {
            mContainer = container;
        }

        public XPathCast processCast(Cast cast, XPathCastResolver parentResolver) {
            XPathCast xPathCast = XPathCastImpl.convert(cast, parentResolver);
            //
            if (xPathCast == null) {
                return null;
            }
            //
            boolean useLeftTree = (cast.getSource() != Source.TO);
            if (useLeftTree) {
                mContainer.getFromCasts().add(xPathCast);
            } else {
                mContainer.getToCasts().add(xPathCast);
            }
            //
            return xPathCast;
        }

        public XPathPseudoComp processPseudo(PseudoComp pseudoComp,
                XPathCastResolver parentResolver) {
            //
            XPathPseudoComp xPathPseudo =
                    XPathPseudoCompImpl.convert(pseudoComp, parentResolver);
            //
            if (xPathPseudo == null) {
                return null;
            }
            //
            boolean useLeftTree = (pseudoComp.getSource() != Source.TO);
            if (useLeftTree) {
                mContainer.getFromPseudoComps().add(xPathPseudo);
            } else {
                mContainer.getToPseudoComps().add(xPathPseudo);
            }
            //
            return xPathPseudo;
        }

        public XPathSchemaContextHolder processPredicate(Predicate predicate,
                XPathCastResolver parentResolver, BpelEntity varContextEntity) {
            // Nothing to do here
            return null;
        }


    }

    /**
     * A visitor for collecting a hierarchy of LocationStepModifier objects into
     * the XPathLsmContainer.
     * It takes a root BPEL extension object. Usually it is the Editor object.
     * It iterates over the extensions' hierarchy and registers predicates,
     * type casts and peudo-components in corresponding managers.
     */
    public static class XPathLsmCollector implements NestedExtensionsVisitor {
        //
        protected LsmConverter mConverter;
        protected BpelEntity mVarContextEntity;
        protected boolean mRegisterFrom;
        protected boolean mRegisterTo;
        //
        protected Stack<XPathCastResolver> mResolverStack =
                new Stack<XPathCastResolver>();

        /**
         * LSM = LocationStepModifier
         * @param varContextEntity see BpelXPathModelFactory#create
         * @param registerFrom indicates if it is necessary to register LSM with source = from
         * @param registerTo indicates if it is necessary to register LSM with source = to
         */
        public XPathLsmCollector(LsmConverter converter,
                BpelEntity varContextEntity, 
                boolean registerFrom, boolean registerTo) {
            //
            assert converter != null;
            //
            mConverter = converter;
            mVarContextEntity = varContextEntity;
            mRegisterFrom = registerFrom;
            mRegisterTo = registerTo;
        }

        public void visit(Cast cast) {
            //
            Source source = cast.getSource();
            if (!((mRegisterFrom && source == Source.FROM) ||
                    (mRegisterTo && source == Source.TO))) {
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
            List<LocationStepModifier> childrenLsm = cast.getChildrenLsm(null);
            if (childrenLsm != null && !childrenLsm.isEmpty()) {
                CastWrapperResolver newResolver =
                        new CastWrapperResolver(xPathCast, parentResolver);
                mResolverStack.push(newResolver);
                try {
                    for (LocationStepModifier childLsm : childrenLsm) {
                        childLsm.accept(this);
                    }
                } finally {
                    mResolverStack.pop();
                }
            }
        }

        public void visit(PseudoComp pseudoComp) {
            //
            Source source = pseudoComp.getSource();
            if (!((mRegisterFrom && source == Source.FROM) ||
                    (mRegisterTo && source == Source.TO))) {
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
            List<LocationStepModifier> childrenLsm = pseudoComp.getChildrenLsm(null);
            if (childrenLsm != null && !childrenLsm.isEmpty()) {
                PseudoWrapperResolver newResolver =
                        new PseudoWrapperResolver(xPathPseudo, parentResolver);
                mResolverStack.push(newResolver);
                try {
                    for (LocationStepModifier childLsm : childrenLsm) {
                        childLsm.accept(this);
                    }
                } finally {
                    mResolverStack.pop();
                }
            }
        }

        public void visit(Predicate predicate) {
            Source source = predicate.getSource();
            if (!((mRegisterFrom && source == Source.FROM) ||
                    (mRegisterTo && source == Source.TO))) {
                // Skip cast if it doesn't corespond to source
                return;
            }
            //
            XPathCastResolver parentResolver = mResolverStack.isEmpty() ?
                null : mResolverStack.peek();
            mConverter.processPredicate(predicate, parentResolver, mVarContextEntity);
            //
            List<LocationStepModifier> childrenLsm = predicate.getChildrenLsm(null);
            if (childrenLsm != null && !childrenLsm.isEmpty()) {
                for (LocationStepModifier childLsm : childrenLsm) {
                    childLsm.accept(this);
                }
            }
        }

        public void visit(Editor editor) {
            List<LocationStepModifier> childrenLsm = editor.getChildrenLsm(null);
            if (childrenLsm != null && !childrenLsm.isEmpty()) {
                for (LocationStepModifier childLsm : childrenLsm) {
                    childLsm.accept(this);
                }
            }
        }
    };

    public static class CastWrapperResolver implements XPathCastResolver {

        private XPathCastResolver mWrapped;
        private XPathCast mCast;

        public CastWrapperResolver(XPathCast cast, XPathCastResolver wrapped) {
            mCast = cast;
            mWrapped = wrapped;
        }

        public List<XPathCast> getXPathCasts() {
            throw new UnsupportedOperationException("Deprecated."); // NOI18N
        }

        public XPathCast getCast(XPathSchemaContext baseSContext) {
            if (baseSContext == null) {
                return null;
            }
            //
            XPathSchemaContext sContext = mCast.getSchemaContext();
            assert sContext instanceof CastSchemaContext;
            if (sContext != null) {
                XPathSchemaContext myBaseSContext =
                        ((CastSchemaContext)sContext).getBaseContext();
                if (baseSContext.equalsChain(myBaseSContext)) {
                    return mCast;
                } else if (mWrapped != null) {
                    return mWrapped.getCast(baseSContext);
                }
            }
            //
            return null;
        }

        public List<XPathPseudoComp> getPseudoCompList(XPathSchemaContext parentSContext) {
            //
            // Delegates to wrapped resolver
            if (parentSContext != null && mWrapped != null) {
                return mWrapped.getPseudoCompList(parentSContext);
            }
            //
            return Collections.EMPTY_LIST;
        }

    }

    public static class PseudoWrapperResolver implements XPathCastResolver {

        private XPathCastResolver mWrapped;
        private XPathPseudoComp mPseudo;

        public PseudoWrapperResolver(XPathPseudoComp pseudo, XPathCastResolver wrapped) {
            mPseudo = pseudo;
            mWrapped = wrapped;
        }

        public List<XPathCast> getXPathCasts() {
            throw new UnsupportedOperationException("Deprecated."); // NOI18N
        }

        public XPathCast getCast(XPathSchemaContext baseSContext) {
            //
            // Delegates to wrapped resolver
            if (baseSContext != null && mWrapped != null) {
                return mWrapped.getCast(baseSContext);
            }
            //
            return null;
        }

        public List<XPathPseudoComp> getPseudoCompList(XPathSchemaContext parentSContext) {
            if (parentSContext == null) {
                return Collections.EMPTY_LIST;
            }
            //
            XPathSchemaContext myParentSContext = mPseudo.getSchemaContext();
            if (myParentSContext != null) {
                if (parentSContext.equalsChain(myParentSContext)) {
                    return Collections.singletonList(mPseudo);
                } else if (mWrapped != null) {
                    return mWrapped.getPseudoCompList(parentSContext);
                }
            }
            //
            return Collections.EMPTY_LIST;
        }

    }

}
