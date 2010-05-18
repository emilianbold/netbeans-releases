/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.bpel.mapper.testutils;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModelFactory;
import org.netbeans.modules.bpel.mapper.model.BpelGraphInfoCollector;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.model.BpelPathConverter;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContextController;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContextFactory;
import org.netbeans.modules.bpel.mapper.multiview.BpelMapperSpiImpl;
import org.netbeans.modules.bpel.mapper.predicates.BpelMapperPredicate;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.references.SchemaReferenceBuilder;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.spi.MapperSpi;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperTreeNode;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.SchemaContextBasedCastResolver;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * Utils
 * 
 * @author Nikita Krjukov
 */
public class BpelMapperTestUtils {

    private static final Logger LOG = Logger.getLogger(
            BpelMapperTestUtils.class.getName());

    public static MapperModel loadMapperModel(BpelEntity bpelEntity) {
        MapperTcContext mapperTcContext = loadMapper(bpelEntity);
        return mapperTcContext.getMapperModel();
    }

    public static MapperTcContext loadMapper(BpelEntity bpelEntity) {
        Lookup emptyLookup = Lookups.fixed();
        Node node = new AbstractNode(Children.LEAF, emptyLookup);
        BpelDesignContext dc = BpelDesignContextFactory.getInstance().
                createBpelDesignContext(bpelEntity, node, emptyLookup);
        MapperTcContext mapperTcContext = BpelMapperTestUtils.createTcContext(dc);
        BpelMapperModelFactory modelFactory = new BpelMapperModelFactory(
                mapperTcContext, dc);
        //
        MapperModel mModel = modelFactory.constructModel();
        assertNotNull(mModel);
        assertEquals(mModel.getClass(), BpelMapperModel.class);
        mapperTcContext.setMapperModel(mModel);
        //
        return mapperTcContext;
    }

    /**
     * Looks recursively for an activity with the specified name and class.
     * It starts looking from the specified parent. 
     * @param <T>
     * @param parent
     * @param name
     * @param aClass
     * @return the first found activity or null. 
     */
    public static <T extends Activity> T findFirstActivity(
            BpelEntity parent, String name, Class<T> aClass) {
        List<BpelEntity> children = parent.getChildren();
        for (BpelEntity bpelEntity : children) {
            if (bpelEntity instanceof Activity) {
                Activity activity = Activity.class.cast(bpelEntity);
                if (aClass.isInstance(activity) && activity.getName().equals(name)) {
                    return (T)activity;
                }
            }
            T result = findFirstActivity(bpelEntity, name, aClass);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Looks recursively for a BPEL entity with the specified class.
     * It is intended to look for unnamed entities like OnAlarmPick.
     * It starts looking from the specified parent.
     * @param <T>
     * @param parent
     * @param name
     * @param aClass
     * @return the first found activity or null.
     */
    public static <T extends BpelEntity> T findFirstUnnamed(
            BpelEntity parent, Class<T> aClass) {
        List<BpelEntity> children = parent.getChildren();
        for (BpelEntity entity : children) {
            if (aClass.isInstance(entity)) {
                return (T)entity;
            }
            T result = findFirstUnnamed(entity, aClass);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Adds a new Assign activity to the sequence.
     * 
     * @param owner is the Sequence to which the new assign is added
     * @param name is the name for the new Assign. It can be null.
     * In such case it will be calculated automatically.
     * @param position is the position index inside of the sequence
     * @return the new assign activity
     */
    public static Assign createNewAssign(final Sequence owner, 
            final String name, final int position)  {
        final BpelModel bpelModel = owner.getBpelModel();
        Assign newAssign = null;
        try {
            newAssign = bpelModel.invoke(new Callable<Assign>() {
                public Assign call() throws Exception {
                    Assign newAssign = bpelModel.getBuilder().createAssign();
                    if (name != null && name.length() != 0) {
                        newAssign.setName(name);
                    }
                    owner.insertActivity(newAssign, position);
                    return newAssign;
                }
            }, owner);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return newAssign;
    }

    /**
     * Creates dummy MapperTcContext. It is intended for test only.
     * @param dc
     * @return
     */
    public static MapperTcContext createTcContext(BpelDesignContext dc) {

        class TestDCC implements BpelDesignContextController {

            private BpelDesignContext mDC;

            public TestDCC(BpelDesignContext dc) {
                mDC = dc;
            }

            public BpelDesignContext getContext() {
                return mDC;
            }

            public void invalidateMapper(EventObject event) {
            }

            public void showMapper() {
            }

            public void hideMapper() {
            }

            public void cleanup() {
            }

            public void processDataObject(Object dataObject) {
            }

            public void setBpelModelUpdateSource(Object source) {
            }

            public void reloadMapper() {
            }

        }

        class TestTcContext implements MapperTcContext {

            private TestDCC mDCC;
            private BpelMapperModel mMapperModel;
            private Mapper mMapper;

            public TestTcContext(BpelDesignContext dc) {
                if (!GraphicsEnvironment.isHeadless()) {
                    mMapper = new Mapper(null);
                }
                mDCC = new TestDCC(dc);
            }

            public TopComponent getTopComponent() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public BpelDesignContextController getDesignContextController() {
                return mDCC;
            }

            public Mapper getMapper() {
                return mMapper;
            }

            public void setMapper(Mapper mapper) {
                mMapper = mapper;
                if (mMapperModel != null) {
                    mMapper.setModel(mMapperModel);
                }
            }

            public BpelMapperModel getMapperModel() {
                return mMapperModel;
            }

            public void setMapperModel(MapperModel model) {
                assert model instanceof BpelMapperModel;
                mMapperModel = BpelMapperModel.class.cast(model);
                if (mMapper != null) {
                    mMapper.setModel(mMapperModel);
                }
            }

            public MapperSpi getMapperSpi() {
                return BpelMapperSpiImpl.singleton();
            }

        }

        return new TestTcContext(dc);
    }

    /**
     * Test build of the MapperModel by the specified assign.
     * @param assignName
     * @param snapshotBundleKey
     */
    public static void testModelBuildByAssign(BpelModel bpelModel,
            String assignName, String snapshotKey,
            Class bundleBase) {
        //
        Assign assign = BpelMapperTestUtils.findFirstActivity(
                bpelModel.getProcess(), assignName, Assign.class);
        assertNotNull(assign);
        //
        MapperModel mModel = BpelMapperTestUtils.loadMapperModel(assign);
        //
        // Check the mapper model by comparing serialized form
        String bmmText = (new BmmSerializer(
                BpelMapperModel.class.cast(mModel))).serialize();
        String snapshot = TestProperties.getMessage(bundleBase, snapshotKey);
        assertEquals(bmmText, snapshot);
    }

    public static TreePath findInTree(MapperSwingTreeModel treeModel, String path) {
        return findInTree(null, treeModel, path);
    }

    public static TreePath findInTree(MapperTreeNode startWith, 
            MapperSwingTreeModel treeModel, String path) {
        //
        MapperTreeNode parent = startWith;
        String[] pathStepArr = path.split("/");
        for (String step : pathStepArr) {
            if (step == null || step.length() == 0) {
                // Skip empty step
                continue;
            }
            List<MapperTreeNode> children = treeModel.getChildren(parent);
            boolean stepFound = false;
            for (MapperTreeNode child : children) {
                String childDisplayName = treeModel.getDisplayName(child);
                if (childDisplayName != null && childDisplayName.equals(step)) {
                    parent = child;
                    stepFound = true;
                    break;
                }
            }
            //
            if (!stepFound) {
                return null;
            }
        }
        //
        return parent.getTreePath();
    }

    /**
     * Adds a new transit link (the direct link from left to right tree).
     * @param bmm mapper model
     * @param leftTreePath source of the link
     * @param rightTreePath target of the link
     */
    public static void addTransitLink(BpelMapperModel bmm,
            TreePath leftTreePath, TreePath rightTreePath) {
        Graph targetGraph = bmm.getGraph(rightTreePath);
        SourcePin source = new TreeSourcePin(leftTreePath);
        if (bmm.canConnect(rightTreePath, source, targetGraph, null, null)) {
            bmm.connect(rightTreePath, source, targetGraph, null, null);
        }
    }

    /**
     * Looks for a global type by type, namespace and name.
     *
     * @param <T> specifies that the result is an object derived from GlobalType.
     * @param baseModel the base BPEL model to start search from. 
     * @param namespace is the thought type namespace
     * @param name is the thought type name
     * @param type is usually either GlobalSimpleType or GlobalComplexType
     * @return
     */
    public static <T extends GlobalType> T getGlobalTypeByName(
            BpelModel baseModel, String namespace, String name, Class<T> type) {
        //
        Collection<SchemaModel> schemaModels = SchemaReferenceBuilder.
                getSchemaModels(baseModel, namespace, false);
        for (SchemaModel sModel : schemaModels) {
            T result = sModel.resolve(namespace, name, type);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

    /**
     * Construct the new predicate fro the subjectTPath object with
     * the specified expression text.
     * 
     * @param subjectTPath
     * @param predicateText can contain only one predicate.
     * For example, in case of the predicate: [$Var/a/@b],
     * the parameter has to be "$Var/a/@b"
     *
     * @return
     */
    public static BpelMapperPredicate constructPredicate(
            MapperTcContext tcContext,
            TreePath subjectTPath,
            String predicateText) {
        //
        TreeItem subjectTItem = MapperSwingTreeModel.getTreeItem(subjectTPath);
        assert(subjectTItem != null);
        //
        XPathSchemaContext baseSContext = BpelPathConverter.singleton().
                constructContext(subjectTItem, false);
        assert(baseSContext != null);
        //
        // Create a new model
        BpelEntity bpelEntity = tcContext.getDesignContextController().
                getContext().getSelectedEntity();
        //
        if (bpelEntity == null) {
            return null;
        }
        //
        SchemaContextBasedCastResolver castResolver =
                new SchemaContextBasedCastResolver(baseSContext);
        XPathModel xPathModel = BpelXPathModelFactory.create(bpelEntity, castResolver);
        xPathModel.setSchemaContext(baseSContext);
        //
        // Parth expression
        XPathExpression expr = null;
        try {
            expr = xPathModel.parseExpression(predicateText);
        } catch (XPathException ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
        //
        XPathPredicateExpression pExpr = 
                xPathModel.getFactory().newXPathPredicateExpression(expr);
        XPathPredicateExpression[] newPredArr = new XPathPredicateExpression[] {pExpr};
        //
        PredicatedSchemaContext pSContext =
                new PredicatedSchemaContext(baseSContext, newPredArr);
        BpelMapperPredicate newPred = new BpelMapperPredicate(pSContext);
        //
        return newPred;
    }

    public static class BmmHashCodeCalculator {
        private BpelMapperModel mBmm;

        public BmmHashCodeCalculator(BpelMapperModel mm) {
            mBmm = mm;
        }

        public int calculate() {
            int result = 0;
            //
            Map<TreePath, Graph> treePathToGraphMap = mBmm.getGraphsInside(null);
            Set<Entry<TreePath, Graph>> entrySet = treePathToGraphMap.entrySet();
            for (Entry<TreePath, Graph> entry : entrySet) {
                Graph graph = entry.getValue();
                TreePath treePath = entry.getKey();
                int graphHashCode = (new GraphHashCodeCalculator(
                        mBmm, graph, treePath)).calculate();
                result = result + graphHashCode;
            }
            //
            return result;
        }

    }

    private static class GraphHashCodeCalculator {
        private int mHashCode = 7;
        private BpelMapperModel mBmm;
        private Graph mGraph;
        private TreePath mTreePath;

        public GraphHashCodeCalculator(BpelMapperModel mm, Graph graph, TreePath treePath) {
            mBmm = mm;
            mGraph = graph;
            mTreePath = treePath;
        }

        public int calculate() {
            calculate(mTreePath, false);
            calculate(mGraph);
            return mHashCode;
        }

        private void calculate(TreePath treePath, boolean leftModel) {
            //
            MapperSwingTreeModel treeModel = leftModel ?
                mBmm.getLeftTreeModel() :
                mBmm.getRightTreeModel();
            //
            do {
                Object obj = treePath.getLastPathComponent();
                mHashCode = 89 * mHashCode + treeModel.getDisplayName(obj).hashCode();
                treePath = treePath.getParentPath();
            } while (treePath != null);
        }

        private void calculate(Graph graph) {
            //
            BpelGraphInfoCollector gInfo = new BpelGraphInfoCollector(graph);
            //
            ArrayList<Link> trLinks = gInfo.getTransitLinks();
            for (Link link : trLinks) {
                calculate(link);
            }
            //
            ArrayList<Vertex> prRoots = gInfo.getPrimaryRoots();
            for (Vertex vertex : prRoots) {
                calculate(vertex);
            }
            //
            ArrayList<Vertex> secRoots = gInfo.getSecondryRoots();
            for (Vertex vertex : secRoots) {
                calculate(vertex);
            }
        }

        private void calculate(Link link) {
            SourcePin source = link.getSource();
            if (source instanceof TreeSourcePin) {
                TreePath sourceTreePath = TreeSourcePin.class.cast(source).getTreePath();
                calculate(sourceTreePath, true);
            } else if (source instanceof Vertex) {
                calculate(Vertex.class.cast(source));
            } else {
                assert false : "Unknown link type"; // NOI18N
            }
        }

        private void calculate(Vertex vertex) {
            //
            mHashCode = 87 * mHashCode + vertex.getName().hashCode();
            //
            for (int index = 0; index < vertex.getItemCount(); index++) {
                VertexItem vItem = vertex.getItem(index);
                Link inLink = vItem.getIngoingLink();
                calculate(inLink);
            }
        }

    }

    /**
     * Serializes a Bpel Mapper's model
     */
    public static class BmmSerializer {
        
        private StringBuilder mSBuilder = new StringBuilder();
        private BpelMapperModel mBmm;
        
        public BmmSerializer(BpelMapperModel mm) {
            mBmm = mm;
        }
        
        public String serialize() {
            //
            Map<TreePath, Graph> treePathToGraphMap = mBmm.getGraphsInside(null);
            Set<Entry<TreePath, Graph>> entrySet = treePathToGraphMap.entrySet();
            TreeMap<String, Graph> sortedGraphs = new TreeMap<String, Graph>();
            //
            // Create sorted map insted of unsorted.
            // The serialization must generate repeateble result!
            for (Entry<TreePath, Graph> entry : entrySet) {
                TreePath treePath = entry.getKey();
                String treePathText = serializeTreePath(treePath, true);
                sortedGraphs.put(treePathText, entry.getValue());
            }
            //
            boolean isFirst = true;
            Set<Entry<String, Graph>> entrySetSorted = sortedGraphs.entrySet();
            for (Entry<String, Graph> entry : entrySetSorted) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    mSBuilder.append(" || ");
                }
                //
                mSBuilder.append("GRAPH{");
                Graph graph = entry.getValue();
                serialize(graph);
                mSBuilder.append("}");
                //
                mSBuilder.append("-->");
                String treePathText = entry.getKey();
                addTreePath(treePathText);
            }
            //
            return mSBuilder.toString();
        }

        private String serializeTreePath(TreePath treePath, boolean leftModel) {
            //
            MapperSwingTreeModel treeModel = leftModel ?
                mBmm.getLeftTreeModel() :
                mBmm.getRightTreeModel();
            //
            StringBuilder pathBuilder = new StringBuilder();
            Object[] pathArr = treePath.getPath();
            boolean isFirst = true;
            //
            // start value is 1 because the root element is skept
            for (int index = 1; index < pathArr.length; index++) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    pathBuilder.append("/");
                }
                Object obj = pathArr[index];
                pathBuilder.append(treeModel.getDisplayName(obj));
                treePath = treePath.getParentPath();
            }
            //
            return pathBuilder.toString();
        }

        private void addTreePath(String treePathText) {
            if (treePathText != null && treePathText.length() != 0) {
                mSBuilder.append("[").append(treePathText).append("]");
            } else {
                mSBuilder.append("[EMPTY_PATH]"); // NOI18N
            }
        }

        private void serialize(TreePath treePath, boolean leftModel) {
            String treePathText = serializeTreePath(treePath, leftModel);
            addTreePath(treePathText);
        }

        private void serialize(Graph graph) {
            //
            BpelGraphInfoCollector gInfo = new BpelGraphInfoCollector(graph);
            //
            boolean isFirst = true;
            //
            ArrayList<Link> trLinks = gInfo.getTransitLinks();
            for (Link link : trLinks) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    mSBuilder.append(",");
                }
                serialize(link);
            }
            //
            ArrayList<Vertex> prRoots = gInfo.getPrimaryRoots();
            for (Vertex vertex : prRoots) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    mSBuilder.append(",");
                }
                serialize(vertex);
            }
            //
            ArrayList<Vertex> secRoots = gInfo.getSecondryRoots();
            for (Vertex vertex : secRoots) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    mSBuilder.append(",");
                }
                serialize(vertex);
            }
        }

        private void serialize(Link link) {
            SourcePin source = link.getSource();
            if (source instanceof TreeSourcePin) {
                TreePath sourceTreePath = TreeSourcePin.class.cast(source).getTreePath();
                serialize(sourceTreePath, true);
            } else if (source instanceof Vertex) {
                serialize(Vertex.class.cast(source));
            } else {
                assert false : "Unknown link type"; // NOI18N
            }
        }

        private void serialize(Vertex vertex) {
            if (vertex instanceof Constant) {
                serialize(Constant.class.cast(vertex));
                return;
            }
            //
            mSBuilder.append(vertex.getName().trim());
            //
            boolean isFirst = true;
            mSBuilder.append("(");
            for (int index = 0; index < vertex.getItemCount(); index++) {
                VertexItem vItem = vertex.getItem(index);
                Link inLink = vItem.getIngoingLink();
                if (inLink != null) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        mSBuilder.append(",");
                    }
                    serialize(inLink);
                }
            }
            mSBuilder.append(")");
        }

        private void serialize(Constant constant) {
            VertexItem constantVi = constant.getItem(0);
            if (constantVi != null) {
                String text = constantVi.getText();
                if (text != null && text.length() != 0) {
                    //
                    // Remove redundant spaces!
                    text = text.replaceAll("\\s{2,}", "");
                    //
                    mSBuilder.append(text);
                    return;
                }
            }
            mSBuilder.append("undefined_constant"); // NOI18N
        }

    }

    public static class TestProperties {

        /**
         * Cache of resource.
         */
        static final Map<String,Properties> cache =
                new WeakHashMap<String,Properties>();

        public static String getMessage(Class baseClass, String key) {
            return getMessage(baseClass, key, "Test.properties"); // NOI18N
        }

        public static String getMessage(Class baseClass, String key,
                String relativePath) {
            Properties props = getProperties(relativePath, baseClass);
            if (props != null) {
                return props.getProperty(key);
            }
            //
            return null;
        }

        private static Properties getProperties(String relativePath,
                Class baseClass) {
            //
            Properties result = null;
            //
            String name = calculateResourceName(relativePath, baseClass);
            if (name == null) {
                return null;
            }
            //
            result = cache.get(name);
            if (result != null) {
                return result;
            }
            //
            result = loadProperties(name, baseClass.getClassLoader());
            if (result != null) {
                cache.put(name, result);
            }
            //
            return result;
        }

        private static String calculateResourceName(String relativePath,
                Class baseClass) {
            //
            StringBuilder result = new StringBuilder();
            //
            String pName = baseClass.getPackage().getName();
            if (pName.length() > 0) {
                result.append(pName.replace('.', '/')).append('/');
            }
            //
            result.append(relativePath);
            //
            return result.toString();
        }

        /**
         * Load a test properties from a file (without caching).
         * @param name the base name of the properties file,
         * e.g. <samp>org.netbeans.modules.foo.Test</samp>
         * @param loader a class loader to search in
         * @return a Properties object with the content of the found file.
         * Returns null if the file not found.
         */
        private static Properties loadProperties(String rName, ClassLoader loader) {
            Properties prop = new Properties();
            //
            URL url = loader != null ? loader.getResource(rName) :
                ClassLoader.getSystemResource(rName);

            if (url != null) {
                try {
                    InputStream is = url.openStream();

                    try {
                        prop.load(is);
                    } finally {
                        is.close();
                    }
                } catch (IOException e) {
                    Exceptions.attachMessage(e, "While loading: " + rName); // NOI18N
                    LOG.log(Level.WARNING, null, e);

                    return null;
                }
            }
            //
            return prop;
        }

    }

}
