/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mobility.project.ui.wizard;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * Preloads Java classes that will be loaded if a project is created
 * after a cold start.  Part of fix for issue 147403.  Basically we
 * amortize the classloading cost by doing classloading on a background
 * low-priority thread while the user is still in the wizard.
 * <p/>
 * The idea is to amortize some classloading over the time the machine
 * is relatively idle, as the user goes through the wizard the first time,
 * and to warm up the OS's
 * filesystem cache with the JARs that will be touched once the project
 * is created.
 * <p>
 * Note that the list of classes includes non-public ones in some cases,
 * so it will probably require maintenance on a release-by-release basis.
 *
 * @author Tim Boudreau
 */
public final class ClassPreloader implements Runnable {
    private static volatile boolean done;
    private ClassPreloader() {}

    static void start() {
        //invoked from a static block in NewProjectIterator
        RequestProcessor.getDefault().post(new ClassPreloader(), 0,
                Thread.MIN_PRIORITY);
    }

    public static void stop() {
        //invoked by J2MEProjectGenerator.createNewProject()
        done = true;
    }

    public void run() {
        ClassLoader systemLoader = Lookup.getDefault().lookup(ClassLoader.class);
        assert systemLoader != null;
        Object o;
        Logger logger = Logger.getLogger(ClassPreloader.class.getName());
        int ix = 0;
        final boolean logDetails = logger.isLoggable (Level.FINEST);
        for (String type : TO_PRELOAD) {
            try {
                o = Class.forName(type, true, systemLoader);
                if (logDetails) {
                    logger.log(Level.FINEST, "Preloaded " + type); //NOI18N
                }
                //Try to keep this thread from ever bogging down the UI
                if ((ix++ % 5) == 0) {
                    Thread.yield();
                }
            } catch (ClassNotFoundException ex) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "Could not preload " + type); //NOI18N
                }
            }
            if (done) {
                if (logDetails) {
                    logger.log(Level.FINEST, "Exit preload early"); //NOI18N
                }
                break;
            }
        }
    }
    private static final String[] TO_PRELOAD = new String[]{
        "org.netbeans.modules.mobility.project.J2MEProjectGenerator", //NOI18N
        "org.netbeans.spi.project.support.ant.ProjectGenerator", //NOI18N
        "javax.xml.transform.TransformerFactory", //NOI18N
        "javax.xml.transform.FactoryFinder", //NOI18N
        "com.sun.org.apache.xalan.internal.xsltc.compiler.RelativePathPattern", //NOI18N
        "com.sun.org.apache.bcel.internal.classfile.Node", //NOI18N
        "com.sun.org.apache.bcel.internal.classfile.Constant", //NOI18N
        "com.sun.org.apache.bcel.internal.classfile.Attribute", //NOI18N
        "com.sun.org.apache.bcel.internal.classfile.SourceFile", //NOI18N
        "org.netbeans.api.queries.FileEncodingQuery", //NOI18N
        "com.sun.org.apache.xalan.internal.xsltc.compiler.Comment", //NOI18N
        "com.sun.org.apache.xalan.internal.xsltc.compiler.XslAttribute", //NOI18N
        "com.sun.org.apache.xalan.internal.xsltc.compiler.ValueOf", //NOI18N
        "com.sun.org.apache.xml.internal.utils.SystemIDResolver", //NOI18N
        "javax.script.ScriptEngine", //NOI18N
        "javax.script.ScriptEngineManager", //NOI18N
        "javax.script.Bindings", //NOI18N
        "javax.script.SimpleBindings", //NOI18N
        "javax.script.ScriptEngineManager$1", //NOI18N
        "javax.script.ScriptEngineFactory", //NOI18N
        "com.sun.script.javascript.RhinoScriptEngineFactory", //NOI18N
        "org.netbeans.libs.freemarker.FreemarkerFactory", //NOI18N
        "javax.script.AbstractScriptEngine", //NOI18N
        "org.netbeans.libs.freemarker.FreemarkerEngine", //NOI18N
        "javax.script.ScriptContext", //NOI18N
        "javax.script.SimpleScriptContext", //NOI18N
        "org.openide.loaders.CreateFromTemplateAttributesProvider", //NOI18N
        "org.netbeans.modules.project.uiapi.ProjectTemplateAttributesProvider", //NOI18N
        "org.netbeans.modules.projectapi.ProjectFileEncodingQueryImplementation", //NOI18N
        "org.netbeans.modules.diff.DiffFileEncodingQueryImplementation", //NOI18N
        "org.netbeans.spi.java.project.support.ui.PackageView", //NOI18N
        "org.netbeans.spi.java.project.support.ui.PackageRootNode", //NOI18N
        "org.netbeans.modules.editor.SimpleIndentEngine", //NOI18N
        "org.netbeans.modules.mobility.project.ui.NodeActions$AntAction", //NOI18N
        "org.netbeans.modules.mobility.project.ui.NodeActions$BuildConfigurationAction", //NOI18N
        "org.netbeans.modules.mobility.project.ui.NodeActions$CleanAndBuildConfigurationAction", //NOI18N
        "org.netbeans.modules.mobility.project.ui.NodeActions$CleanConfigurationAction", //NOI18N
        "org.netbeans.modules.mobility.project.ui.NodeActions$DeployConfigurationAction", //NOI18N
        "org.netbeans.modules.mobility.project.ui.NodeActions$SetConfigurationAction", //NOI18N
        "org.netbeans.modules.mobility.project.ui.NodeActions$AddConfigurationAction", //NOI18N
        "freemarker.template.TemplateScalarModel", //NOI18N
        "freemarker.template.TemplateSequenceModel", //NOI18N
        "freemarker.template.TemplateHashModelEx", //NOI18N
        "freemarker.template.TemplateMethodModel", //NOI18N
        "freemarker.template.TemplateMethodModelEx", //NOI18N
        "freemarker.template.GeneralPurposeNothing", //NOI18N
        "freemarker.template.TemplateCollectionModel", //NOI18N
        "freemarker.template.WrappingTemplateModel", //NOI18N
        "com.sun.tools.javac.api.JavacTool", //NOI18N
        "javax.tools.Diagnostic", //NOI18N
        "java.beans.MethodDescriptor", //NOI18N
        "java.beans.GenericBeanInfo", //NOI18N
        "javax.lang.model.type.TypeMirror", //NOI18N
        "javax.lang.model.type.PrimitiveType", //NOI18N
        "org.netbeans.modules.editor.guards.GuardedWriter", //NOI18N
        "org.netbeans.modules.editor.guards.GuardedReader", //NOI18N
        "org.netbeans.api.editor.guards.GuardedSectionManager", //NOI18N
        "org.netbeans.modules.vmd.api.model.VersionDescriptor", //NOI18N
        "org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor", //NOI18N
        "org.netbeans.modules.palette.ui.AutoscrollSupport", //NOI18N
        "org.apache.lucene.index.IndexCommitPoint", //NOI18N
        "org.netbeans.modules.mobility.project.ui.ConfigurationsProvider", //NOI18N
        "org.netbeans.modules.mobility.deployment.ricoh.RicohDeploymentProperties", //NOI18N
        "org.netbeans.modules.xml.text.indent.DTDFormatter", //NOI18N
        "org.netbeans.editor.TokenItem", //NOI18N
        "org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties$PropertyInfo", //NOI18N
        "org.netbeans.modules.mobility.project.ui.customizer.CustomizerAbilities", //NOI18N
        "org.netbeans.modules.profiler.HistoryListener", //NOI18N
        "org.netbeans.modules.profiler.SaveViewAction$ViewProvider", //NOI18N
        "org.netbeans.modules.profiler.LiveResultsWindow", //NOI18N
        "org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler", //NOI18N
        "org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler", //NOI18N
        "org.netbeans.lib.profiler.ui.LiveResultsPanel", //NOI18N
        "org.netbeans.api.visual.widget.Widget", //NOI18N
        "org.netbeans.api.visual.widget.Scene", //NOI18N
        "org.netbeans.api.visual.model.ObjectScene", //NOI18N
        "org.netbeans.api.visual.graph.GraphPinScene", //NOI18N
        "org.netbeans.modules.vmd.api.flow.visual.FlowScene", //NOI18N
        "org.netbeans.modules.visual.widget.WidgetAccessibleContext", //NOI18N
        "org.netbeans.api.visual.action.TwoStateHoverProvider", //NOI18N
        "org.netbeans.modules.debugger.jpda.projects.BreakpointAnnotationProvider", //NOI18N
        "java.awt.TexturePaint", //NOI18N
        "org.netbeans.modules.visual.widget.SatelliteComponent", //NOI18N
        "org.netbeans.api.visual.widget.SceneComponent", //NOI18N
        "org.netbeans.api.visual.action.SelectProvider", //NOI18N
        "org.netbeans.api.visual.action.HoverProvider", //NOI18N
        "org.netbeans.api.visual.widget.LayerWidget", //NOI18N
        "org.netbeans.api.visual.action.MoveProvider", //NOI18N
        "org.netbeans.api.visual.action.AcceptProvider", //NOI18N
        "org.netbeans.api.visual.action.ConnectDecorator", //NOI18N
        "org.netbeans.api.visual.action.ConnectProvider", //NOI18N
        "org.netbeans.api.visual.action.ReconnectDecorator", //NOI18N
        "org.netbeans.api.visual.action.ReconnectProvider", //NOI18N
        "org.netbeans.api.visual.action.TextFieldInplaceEditor", //NOI18N
        "org.netbeans.api.visual.action.EditProvider", //NOI18N
        "org.netbeans.api.visual.action.PopupMenuProvider", //NOI18N
        "org.netbeans.api.visual.action.WidgetAction", //NOI18N
        "org.netbeans.api.visual.router.ConnectionWidgetCollisionsCollector", //NOI18N
        "org.netbeans.api.visual.graph.layout.GraphLayout", //NOI18N
        "org.netbeans.api.visual.graph.layout.GridGraphLayout", //NOI18N
        "org.netbeans.api.visual.model.ObjectSceneListener", //NOI18N
        "org.netbeans.api.visual.model.ObjectState", //NOI18N
        "org.netbeans.api.visual.action.WidgetAction$Chain", //NOI18N
        "org.netbeans.api.visual.border.BorderFactory", //NOI18N
        "org.netbeans.api.visual.border.Border", //NOI18N
        "org.netbeans.modules.visual.router.DirectRouter", //NOI18N
        "org.netbeans.modules.visual.router.FreeRouter", //NOI18N
        "org.netbeans.modules.visual.router.OrthogonalSearchRouter", //NOI18N
        "org.netbeans.modules.visual.action.MouseCenteredZoomAction", //NOI18N
        "org.netbeans.modules.visual.action.DefaultRectangularSelectDecorator", //NOI18N
        "org.netbeans.modules.visual.action.ObjectSceneRectangularSelectProvider", //NOI18N
        "org.netbeans.modules.visual.action.RectangularSelectAction", //NOI18N
        "org.netbeans.modules.visual.anchor.FreeRectangularAnchor", //NOI18N
        "org.netbeans.modules.visual.anchor.RectangularAnchor", //NOI18N
        "org.netbeans.modules.visual.anchor.CircularAnchor", //NOI18N
        "org.netbeans.modules.visual.anchor.DirectionalAnchor", //NOI18N
        "org.netbeans.modules.visual.anchor.ProxyAnchor", //NOI18N
        "org.netbeans.modules.visual.anchor.FixedAnchor", //NOI18N
        "org.netbeans.modules.visual.anchor.CenterAnchor", //NOI18N
        "org.netbeans.modules.visual.action.MouseHoverAction", //NOI18N
        "org.netbeans.modules.visual.router.OrthogonalSearchRouterCore", //NOI18N
        "org.netbeans.modules.visual.action.ForwardKeyEventsAction", //NOI18N
        "org.netbeans.api.visual.model.StateModel", //NOI18N
        "org.netbeans.modules.vmd.api.model.presenters.InfoPresenter", //NOI18N
        "org.netbeans.modules.vmd.api.model.PresenterListener", //NOI18N
        "org.netbeans.modules.vmd.api.screen.display.DeviceBorder", //NOI18N
        "org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfoPresenter", //NOI18N
        "org.netbeans.modules.vmd.midp.components.sources.EventSourceSupport", //NOI18N
        "org.apache.lucene.store.AlreadyClosedException", //NOI18N
        "org.apache.lucene.index.DirectoryIndexReader", //NOI18N
        "org.apache.lucene.index.SegmentInfos", //NOI18N
        "org.apache.lucene.index.CorruptIndexException", //NOI18N
        "org.apache.lucene.index.IndexFileNameFilter", //NOI18N
        "org.apache.lucene.index.IndexFileNames", //NOI18N
        "org.apache.lucene.search.HitCollector", //NOI18N
        "org.netbeans.modules.vmd.api.model.TransactionManager", //NOI18N
        "org.netbeans.modules.vmd.game.integration.components.GamePrimitiveDescriptor", //NOI18N
        "org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor", //NOI18N
        "org.netbeans.modules.vmd.inspector.InspectorUI", //NOI18N
        "org.netbeans.modules.vmd.inspector.InspectorFolderNode", //NOI18N
        "org.netbeans.modules.vmd.inspector.InspectorBeanTreeView", //NOI18N
        "org.netbeans.api.visual.anchor.Anchor", //NOI18N
        "org.netbeans.api.visual.vmd.VMDNodeAnchor", //NOI18N
        "org.netbeans.api.visual.widget.ImageWidget", //NOI18N
        "org.netbeans.api.visual.vmd.VMDGlyphSetWidget", //NOI18N
        "org.netbeans.api.visual.widget.SeparatorWidget", //NOI18N
        "org.netbeans.api.visual.vmd.VMDFactory", //NOI18N
        "org.netbeans.api.visual.vmd.VMDColorScheme", //NOI18N
        "org.netbeans.modules.visual.vmd.VMDOriginalColorScheme", //NOI18N
        "org.netbeans.modules.visual.vmd.VMDNetBeans60ColorScheme", //NOI18N
        "org.netbeans.api.visual.vmd.VMDNodeBorder", //NOI18N
        "org.netbeans.api.visual.anchor.PointShapeFactory", //NOI18N
        "org.netbeans.api.visual.anchor.PointShape", //NOI18N
        "org.netbeans.modules.visual.anchor.ImagePointShape", //NOI18N
        "org.netbeans.modules.visual.border.CompositeBorder", //NOI18N
        "org.netbeans.modules.java.source.usages.fcs.FileChangeSupport", //NOI18N
        "org.openide.nodes.Sheet", //NOI18N
        "org.openide.explorer.propertysheet.ButtonPanel", //NOI18N
        "org.netbeans.modules.java.source.JavaFileFilterQuery", //NOI18N
        "org.netbeans.modules.java.source.parsing.FileObjects", //NOI18N
        "org.netbeans.modules.java.source.parsing.FolderArchive", //NOI18N
        "org.netbeans.modules.java.source.parsing.CachingArchive", //NOI18N
        "com.sun.javadoc.Doc", //NOI18N
        "com.sun.javadoc.ProgramElementDoc", //NOI18N
        "com.sun.javadoc.Type", //NOI18N
        "com.sun.javadoc.ClassDoc", //NOI18N
        "com.sun.javadoc.AnnotationTypeDoc", //NOI18N
        "com.sun.javadoc.DocErrorReporter", //NOI18N
        "com.sun.tools.javadoc.Messager", //NOI18N
        "com.sun.tools.javadoc.DocImpl", //NOI18N
        "com.sun.tools.javadoc.ProgramElementDocImpl", //NOI18N
        "com.sun.tools.javadoc.ClassDocImpl", //NOI18N
        "com.sun.tools.javadoc.AnnotationTypeDocImpl", //NOI18N
        "com.sun.javadoc.MemberDoc", //NOI18N
        "com.sun.javadoc.ExecutableMemberDoc", //NOI18N
        "com.sun.javadoc.MethodDoc", //NOI18N
        "org.openide.loaders.DataLdrActions", //NOI18N
        "org.openide.actions.OpenAction", //NOI18N
        "org.netbeans.core.ui.options.filetypes.OpenAsAction", //NOI18N
        "org.netbeans.core.ui.sysopen.SystemOpenAction", //NOI18N
        "org.openide.actions.RenameAction", //NOI18N
        "org.openide.actions.FileSystemAction", //NOI18N
        "org.openide.actions.FileSystemAction$Menu", //NOI18N
        "org.openide.actions.SaveAsTemplateAction", //NOI18N
        "org.openide.nodes.NodeAcceptor", //NOI18N
    };
}
