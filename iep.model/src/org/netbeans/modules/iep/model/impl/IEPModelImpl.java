/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.iep.model.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.InputOperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.OutputOperatorComponent;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.iep.model.lib.GenUtil;
import org.netbeans.modules.iep.model.lib.IOUtil;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class IEPModelImpl extends IEPModel {

    private Component rootComponent;
    private IEPComponentFactory wcf;

    public IEPModelImpl(ModelSource source) {
        super(source);
        wcf = new IEPComponentFactoryImpl(this);
    }

    @Override
    public IEPComponent createRootComponent(Element root) {
        // TODO Auto-generated method stub
        String namespace = root.getNamespaceURI();
        //if ( IEPModel.IEP_NAMESPACE.equals(namespace) &&
        //     IEPModel.IEP_COMPONENT.equals( root.getLocalName() )) 
        if (IEPModel.IEP_COMPONENT.equals(root.getLocalName())) {
            rootComponent = new PlanComponentImpl(this, root);
            return rootComponent;
        }
        return null;
    }

    @Override
    protected ComponentUpdater<IEPComponent> getComponentUpdater() {
        // TODO Auto-generated method stub
        return new ChildComponentUpdateVisitor<IEPComponent>();
    }

    public IEPComponentFactory getFactory() {
        // TODO Auto-generated method stub
        return wcf;
    }

    public IEPComponent createComponent(IEPComponent parent, Element element) {
        // TODO Auto-generated method stub
        return parent.createChild(element);
    }

    public IEPComponent getRootComponent() {
        return rootComponent;
    }

    public PlanComponent getPlanComponent() {
        return (PlanComponent) rootComponent;
    }

    public ChangeInfo prepareChangeInfo(List<Node> pathToRoot) {
        ChangeInfo change = super.prepareChangeInfo(pathToRoot);
        DocumentComponent parentComponent = findComponent(change.getRootToParentPath());
        if (parentComponent == null) {
            return change;
        }
        change.setParentComponent(parentComponent);
        return change;
    }

    public String getIEPFilePath() {
        DataObject dObj = getModelSource().getLookup().lookup(DataObject.class);
        return FileUtil.toFile(dObj.getPrimaryFile()).getAbsolutePath();
    }

    public String getIEPFileName() {
        DataObject dObj = getModelSource().getLookup().lookup(DataObject.class);
        return dObj.getPrimaryFile().getNameExt();
    }

    public File getWsdlFile() {
        String wsdlPath = getIEPFilePath();
        wsdlPath = wsdlPath.substring(0, wsdlPath.length() - 4) + ".wsdl"; //4: for '.iep'
        return new File(wsdlPath);
    }

    public void saveWsdl() throws Exception {
        // auto generate .wsdl
        // see org.netbeans.modules.iep.editor.jbiadapter.IEPSEDeployer
        //String tns = NameUtil.makeJavaId(getIEPFileName());

        //now use package qualified name as targetNamespace
        String tns = getQualifiedName();
        FileOutputStream fos = null;
        try {
            // Generate .wsdl in the same dir as its .iep is
            IEPWSDLGenerator gen = new IEPWSDLGenerator(this);
            String wsdl = gen.getWSDL(tns);
            String wsdlPath = getIEPFilePath();
            wsdlPath = wsdlPath.substring(0, wsdlPath.length() - 4) + ".wsdl"; //4: for '.iep'
            GenUtil.createFile(new File(wsdlPath), false);
            fos = new FileOutputStream(wsdlPath);
            IOUtil.copy(wsdl.getBytes("UTF-8"), fos);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = NbBundle.getMessage(IEPModelImpl.class, "ModelImpl.FAIL_SAVE_MODEL", new Object[]{getIEPFilePath()});
            throw new Exception(msg, e);

        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public List<InputOperatorComponent> getInputList(boolean wsOnly) {
        PlanComponent planComponent = this.getPlanComponent();
        if (planComponent == null) {
            return Collections.EMPTY_LIST;
        }

        OperatorComponentContainer ocContainer = planComponent.getOperatorComponentContainer();

        if (ocContainer == null) {
            return Collections.EMPTY_LIST;
        }


        List<InputOperatorComponent> list = new ArrayList<InputOperatorComponent>();
        List<OperatorComponent> operators = ocContainer.getAllOperatorComponent();
        Iterator<OperatorComponent> it = operators.iterator();
        while (it.hasNext()) {
            OperatorComponent oc = it.next();
            if (oc instanceof InputOperatorComponent) {
                InputOperatorComponent inOC = (InputOperatorComponent) oc;
                if (wsOnly) {
                    if (inOC.isWebServiceInput()) {
                        list.add(inOC);
                    }
                } else {
                    list.add(inOC);
                }
            }

        }

        return list;
    }

    public List<OutputOperatorComponent> getOutputList(boolean wsOnly) {
        PlanComponent planComponent = this.getPlanComponent();
        if (planComponent == null) {
            return Collections.EMPTY_LIST;
        }

        OperatorComponentContainer ocContainer = planComponent.getOperatorComponentContainer();

        if (ocContainer == null) {
            return Collections.EMPTY_LIST;
        }

        List<OutputOperatorComponent> list = new ArrayList<OutputOperatorComponent>();
        List<OperatorComponent> operators = ocContainer.getAllOperatorComponent();
        Iterator<OperatorComponent> it = operators.iterator();
        while (it.hasNext()) {
            OperatorComponent oc = it.next();
            if (oc instanceof OutputOperatorComponent) {
                OutputOperatorComponent inOC = (OutputOperatorComponent) oc;
                if (wsOnly) {
                    if (inOC.isWebServiceOutput()) {
                        list.add(inOC);
                    }
                } else {
                    list.add(inOC);
                }
            }

        }

        return list;
    }

    @Override
    public String getQualifiedName() {
        String qualifiedName = null;

        FileObject iepFile = getModelSource().getLookup().lookup(FileObject.class);
        if (iepFile != null) {
            Project project = FileOwnerQuery.getOwner(iepFile);
            if (project != null) {
                Sources sources = ProjectUtils.getSources(project);
                if (sources != null) {
                    //SourceGroup[] sg = sources.getSourceGroups(Sources.TYPE_GENERIC);
                    SourceGroup[] sg = sources.getSourceGroups("BIZPRO");

                    if (sg != null) {
                        for (int i = 0; i < sg.length; i++) {
                            FileObject rootFolder = sg[i].getRootFolder();
                            if (FileUtil.isParentOf(rootFolder, iepFile)) {
                                qualifiedName = FileUtil.getRelativePath(rootFolder, iepFile);
                                break;
                            }
                        }

                    }
                }
            }
        }

        if (qualifiedName != null) {
            int dotIndex = qualifiedName.lastIndexOf(".");
            if (dotIndex != -1) {
                qualifiedName = qualifiedName.substring(0, dotIndex) + "_" + qualifiedName.substring(dotIndex + 1, qualifiedName.length());
            }

            qualifiedName = qualifiedName.replaceAll("/", ".");
            qualifiedName = qualifiedName.replaceAll("\\\\", ".");
        }

        return qualifiedName;
    }
}
