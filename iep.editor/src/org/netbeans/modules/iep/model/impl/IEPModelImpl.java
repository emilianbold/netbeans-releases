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


import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.WsOperatorComponent;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.iep.model.WSType;
import org.netbeans.modules.tbls.model.GenUtil;
import org.netbeans.modules.tbls.model.IOUtil;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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

    @Override
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
        FileObject iepFile = getModelSource().getLookup().lookup(FileObject.class);
        File file = null;
        if(iepFile != null) {
            file =  FileUtil.toFile(iepFile);
        } else {
            file = getModelSource().getLookup().lookup(File.class);
        }
        if(file != null) {
            return file.getAbsolutePath();
        }
        
        return null;
    }

    public String getIEPFileName() {
        FileObject iepFile = getModelSource().getLookup().lookup(FileObject.class);
        File file = null;
        if(iepFile != null) {
            file =  FileUtil.toFile(iepFile);
        } else {
            file = getModelSource().getLookup().lookup(File.class);
        }
        
        if(file != null) {
            return file.getName();
        }
        
        return null;
//        DataObject dObj = getModelSource().getLookup().lookup(DataObject.class);
//        return dObj.getPrimaryFile().getNameExt();
    }

    public File getWsdlFile() {
        String wsdlPath = getIEPFilePath();
        wsdlPath = wsdlPath.substring(0, wsdlPath.length() - 4) + ".wsdl"; //4: for '.iep'
        return new File(wsdlPath);
    }

    @Override
    public void saveWsdl(boolean abstractOnly) throws Exception {
//      auto generate .wsdl
        // see org.netbeans.modules.iep.editor.jbiadapter.IEPSEDeployer
        //String tns = NameUtil.makeJavaId(getIEPFileName());

        //now use package qualified name as targetNamespace
        String tns = getQualifiedName();
        FileOutputStream fos = null;
        try {
            // Generate .wsdl in the same dir as its .iep is
            IEPWSDLGenerator gen = new IEPWSDLGenerator(this, abstractOnly);
            String wsdl = gen.getWSDL(tns);
            String wsdlPath = getIEPFilePath();
            wsdlPath = wsdlPath.substring(0, wsdlPath.length() - 4) + ".wsdl"; //4: for '.iep'
            GenUtil.createFile(new File(wsdlPath), false);
            fos = new FileOutputStream(wsdlPath);
            IOUtil.copy(wsdl.getBytes("UTF-8"), fos);
        } catch (Exception e) {
            String msg = NbBundle.getMessage(IEPModelImpl.class, "ModelImpl.FAIL_SAVING_WSDL", new Object[]{getIEPFilePath(), e.getMessage()});
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
    
    public void saveWsdl() throws Exception {
        saveWsdl(false);
    }

    /**
     * @param wsType: if null return all InputOperatorComponent operator
     * @return a list of InputOperatorComponent whose webserice-type is wsType
     */
    public List<WsOperatorComponent> getWebServiceList(WSType wsType) {
        PlanComponent planComponent = this.getPlanComponent();
        if (planComponent == null) {
            return Collections.EMPTY_LIST;
        }

        OperatorComponentContainer ocContainer = planComponent.getOperatorComponentContainer();

        if (ocContainer == null) {
            return Collections.EMPTY_LIST;
        }

        List<WsOperatorComponent> list = new ArrayList<WsOperatorComponent>();
        List<OperatorComponent> operators = ocContainer.getAllOperatorComponent();
        Iterator<OperatorComponent> it = operators.iterator();
        while (it.hasNext()) {
            OperatorComponent oc = it.next();
            if (oc instanceof WsOperatorComponent && oc.getWsType().equals(wsType)) {
               list.add((WsOperatorComponent)oc);
            }
        }

        return list;
    }

    @Override
    public List<OperatorComponent> getEntryList() {
        PlanComponent planComponent = this.getPlanComponent();
        if (planComponent == null) {
            return Collections.EMPTY_LIST;
        }

        OperatorComponentContainer ocContainer = planComponent.getOperatorComponentContainer();
        if (ocContainer == null) {
            return Collections.EMPTY_LIST;
        }

        List<OperatorComponent> list = new ArrayList<OperatorComponent>();
        List<OperatorComponent> operators = ocContainer.getAllOperatorComponent();
        Iterator<OperatorComponent> it = operators.iterator();
        while (it.hasNext()) {
            OperatorComponent oc = it.next();
            if (oc.getBoolean(PROP_ENTRY)) {
                list.add(oc);
            }
        }

        return list;
    }
     
    @Override
    public List<OperatorComponent> getExitList() {
        PlanComponent planComponent = this.getPlanComponent();
        if (planComponent == null) {
            return Collections.EMPTY_LIST;
        }

        OperatorComponentContainer ocContainer = planComponent.getOperatorComponentContainer();
        if (ocContainer == null) {
            return Collections.EMPTY_LIST;
        }

        List<OperatorComponent> list = new ArrayList<OperatorComponent>();
        List<OperatorComponent> operators = ocContainer.getAllOperatorComponent();
        Iterator<OperatorComponent> it = operators.iterator();
        while (it.hasNext()) {
            OperatorComponent oc = it.next();
            if (oc.getBoolean(PROP_EXIT)) {
                list.add(oc);
            }
        }

        return list;
    }

    @Override
    public String getQualifiedName() {
        String qualifiedName = null;

        StringBuffer qName = new StringBuffer();
        String packageName = getPlanComponent().getPackageName();
        if(packageName != null && !packageName.trim().equals("")) {
            qName.append(packageName);
            qName.append(".");
        }
        
        FileObject iepFile = getModelSource().getLookup().lookup(FileObject.class);
        if(iepFile == null) {
            File file = getModelSource().getLookup().lookup(File.class);
            if(file != null) {
                qName.append(file.getName());
            }
        } else {
            qName.append(iepFile.getNameExt());
        }
        
        qualifiedName = qName.toString();
        
//        FileObject iepFile = getModelSource().getLookup().lookup(FileObject.class);
//        
//        if (iepFile != null) {
//            Project project = FileOwnerQuery.getOwner(iepFile);
//            if (project != null) {
//                Sources sources = ProjectUtils.getSources(project);
//                if (sources != null) {
//                    //SourceGroup[] sg = sources.getSourceGroups(Sources.TYPE_GENERIC);
//                    SourceGroup[] sg = sources.getSourceGroups("BIZPRO");
//
//                    if (sg != null) {
//                        for (int i = 0; i < sg.length; i++) {
//                            FileObject rootFolder = sg[i].getRootFolder();
//                            if (FileUtil.isParentOf(rootFolder, iepFile)) {
//                                qualifiedName = FileUtil.getRelativePath(rootFolder, iepFile);
//                                break;
//                            }
//                        }
//
//                    }
//                }
//            }
//        }
//
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
