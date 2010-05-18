/*
 * PlanReport.java
 * 
 * Created on Sep 24, 2007, 7:15:36 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.designer.cookies;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JScrollBar;

import org.netbeans.modules.iep.editor.PlanDataObject;
import org.netbeans.modules.iep.editor.PlanDesignViewMultiViewElement;
import org.netbeans.modules.iep.editor.designer.EntityNode;
import org.netbeans.modules.iep.editor.designer.PlanCanvas;
import org.netbeans.modules.tbls.editor.ps.TcgPsI18n;
import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.reportgenerator.api.Report;
import org.netbeans.modules.reportgenerator.api.ReportAttribute;
import org.netbeans.modules.reportgenerator.api.ReportBody;
import org.netbeans.modules.reportgenerator.spi.ReportCookie;
import org.netbeans.modules.reportgenerator.api.ReportElement;
import org.netbeans.modules.reportgenerator.api.ReportElementFactory;
import org.netbeans.modules.reportgenerator.api.ReportException;
import org.netbeans.modules.reportgenerator.api.ReportSection;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;

import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import org.netbeans.modules.iep.model.share.SharedConstants;

/**
 *
 * @author radval
 */
public class PlanReportCookie implements ReportCookie, SharedConstants {

    private Logger mLogger = Logger.getLogger(PlanReportCookie.class.getName());
    
    private PlanDataObject mDataObject;
    
    private static String[] filterTheseProperties = new String[] {"x", "y", "z", "toposcore"};
    
    private static List<String> filterPropertyNames = new ArrayList<String>();

    static {
        for(int i = 0; i < filterTheseProperties.length; i++) {
            String propName = filterTheseProperties[i];
            filterPropertyNames.add(propName);
        }
    }
    public PlanReportCookie(PlanDataObject dataObject) {
        this.mDataObject = dataObject;
        
    }
    
    public Report generateReport() {
        Report report = null;
        try {
            report = ReportElementFactory.getDefault().createReport();
            fillReport(report);
            
        } catch(ReportException ex) {
            mLogger.log(Level.SEVERE, "Failed to generate report", ex);
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message("Failed to generate report, see ide log for details");
            DialogDisplayer.getDefault().notify(msg);
        }
        
        return report;
    }
    
    
    private void fillReport(Report report) throws ReportException {
        ReportElementFactory elementFactory = ReportElementFactory.getDefault();
        
        //report level stuff
        //report name
        report.setName("Report of "+ mDataObject.getPrimaryFile().getNameExt());
        
        //report description
        IEPModel model = mDataObject.getPlanEditorSupport().getModel();
        PlanComponent planComponent = model.getPlanComponent();
        Documentation doc = planComponent.getDocumentation();
        String reportDescription = "";
        if(doc != null) {
            reportDescription = doc.getTextContent();
        } else {
            reportDescription = "This is a report for "+ this.mDataObject.getPrimaryFile().getNameExt() + ". Following is an overview of the iep process.";
        }
        
        report.setDescription(reportDescription);
        report.setOverViewImage(createOverviewImage());
        
        File iepFile = FileUtil.toFile(mDataObject.getPrimaryFile());
        ReportAttribute ra1 = elementFactory.createReportAttribute();
        ra1.setName("Name:");
        ra1.setValue(mDataObject.getName());
        report.addAttribute(ra1);
        
        ReportAttribute ra2 = elementFactory.createReportAttribute();
        ra2.setName("Location:");
        ra2.setValue(iepFile.getAbsolutePath());
        report.addAttribute(ra2);
        
        ReportAttribute ra3 = elementFactory.createReportAttribute();
        ra3.setName("Size (in bytes):");
        long length = iepFile.length();
        ra3.setValue(length);
        report.addAttribute(ra3);
        
        ReportAttribute ra4 = elementFactory.createReportAttribute();
        ra4.setName("Last Modified:");
        ra4.setValue(new Date(iepFile.lastModified()));
        report.addAttribute(ra4);
        
        //report body
        ReportBody body = elementFactory.createReportBody();
        report.setBody(body);
        
        ReportSection section = elementFactory.createReportSection();
        section.setDescription("Following section describe Operators used in this IEP Process.");
        body.addReportSection(section);
        
        PlanDesignViewMultiViewElement designElement = mDataObject.getPlanEditorSupport().getPlanDesignMultiviewElement();
        PlanCanvas canvas = designElement.getGraphView().getPlanCanvas();
        
        JGoDocument document = canvas.getDocument();
        
        List<EntityNode> nodes = new ArrayList<EntityNode>();
        
        JGoListPosition pos = document.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = document.getObjectAtPos(pos);
            pos = document.getNextObjectPos(pos);
            
            if (!obj.isTopLevel()) {
                continue;
            }
            if (obj instanceof EntityNode) {
                EntityNode node = (EntityNode)obj;
                nodes.add(node);
            }
        }
        
        //sort
        Collections.sort(nodes, new EntityNodeComparator());
        
        PlanCanvas planCanvas = designElement.getGraphView().getPlanCanvas();
        
        Iterator<EntityNode> it = nodes.iterator();
        
        while(it.hasNext()) {
            EntityNode node = it.next();
            createAndfillReportElement(node, elementFactory, section, planCanvas);
        }
        
        
        
    }
    
    private void createAndfillReportElement(EntityNode node, 
                                            ReportElementFactory elementFactory,
                                            ReportSection section,
                                            PlanCanvas planCanvas) {
        
        OperatorComponent c = node.getModelComponent();
        
        ReportElement element = elementFactory.createReportElement();
        //element.setImage(createEntityNodeImage(node, planCanvas));
        element.setImage(node.getIcon().getImage());
        element.setName(c.getString(PROP_NAME));
        if(c.getDocumentation() != null) {
            element.setDescription(c.getDocumentation().getTextContent());
        }
        section.addReportElement(element);
        
        fillOperatorAttributes(c, element, elementFactory);
    }
    
    private void fillOperatorAttributes(OperatorComponent comp, 
                                        ReportElement element, 
                                        ReportElementFactory elementFactory) {
        List<Property> properties =  comp.getProperties();
        List<Property> filteredProperties = filterProperties(properties);
        Iterator<Property> it = filteredProperties.iterator();
        
        while(it.hasNext()) {
            Property p = it.next();
            String name = p.getName();
            String value = p.getValue();
            if(value != null && !value.equals("")) {
                String newVal = modifyPropertyValue(comp, p);
                
                ReportAttribute attr = elementFactory.createReportAttribute();
                attr.setName(name);
                attr.setValue(newVal);
                element.addAttribute(attr);
            }
        }
    }
    
    private Image createOverviewImage() {
        PlanDesignViewMultiViewElement  designElement = mDataObject.getPlanEditorSupport().getPlanDesignMultiviewElement();
        PlanCanvas planCanvas = designElement.getGraphView().getPlanCanvas();
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        JScrollBar hBar = planCanvas.getHorizontalScrollBar();
        JScrollBar vBar = planCanvas.getVerticalScrollBar();
        
        Dimension d = planCanvas.getDocumentSize();
        planCanvas.convertDocToView(d);

        int x = 0;
        int y = 0;
        int width = d.width;
        int height = d.height;
        
        width = planCanvas.getWidth() > width ? planCanvas.getWidth() : width;
        height = planCanvas.getHeight() > height ? planCanvas.getHeight() : height;
        
        int widthToClip = vBar.isVisible() ? width - vBar.getWidth() : width ;
        int heightToClip = hBar.isVisible() ? height - hBar.getHeight() : height;
        
        // Create an image that supports transparent pixels
        BufferedImage bImage = gc.createCompatibleImage(width, height, Transparency.BITMASK);
        
        
        /*
         * And now this is how we get an image of the component
         */
        Graphics2D g = bImage.createGraphics();
        Dimension oldSize = new Dimension(planCanvas.getSize());
        Point oldViewPos = new Point(planCanvas.getViewPosition());
//        g.clipRect(x, y, widthToClip -1, heightToClip -1);
        
        planCanvas.setViewPosition(x, y);
        planCanvas.setSize(width, height);
        //Then use the current component we're in and call paint on this graphics object
        planCanvas.paint( g );

        //reset old bounds 
        planCanvas.setSize(oldSize);
        planCanvas.setViewPosition(oldViewPos);
        return bImage;
    }
    
    private Image createEntityNodeImage(EntityNode node,
                                        PlanCanvas planCanvas) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        // Create an image that supports transparent pixels
        BufferedImage bImage = gc.createCompatibleImage(planCanvas.getViewRect().width, planCanvas.getViewRect().height, Transparency.BITMASK);

        /*
         * And now this is how we get an image of the component
         */
        Graphics2D g = bImage.createGraphics();

        g.setClip(node.getBoundingRect());
        
        //Then use the current component we're in and call paint on this graphics object
        planCanvas.paint( g);

        return bImage;
    }
    
    
    private List<Property> filterProperties(List<Property> properties) {
        List<Property> filteredProperties = new ArrayList<Property>();
        Iterator<Property> it = properties.iterator();
        while(it.hasNext()) {
            Property prop = it.next();
            String name = prop.getName();
            if(!filterPropertyNames.contains(name)) {
                filteredProperties.add(prop);
            }
        }
        return filteredProperties;
    }
    
    private String modifyPropertyValue(OperatorComponent comp, Property prop) {
        String name = prop.getName();
        String value = prop.getValue();
        String modifiedPropertyValue = prop.getValue();
        
        if(PROP_INPUT_TYPE.equals(name)) {
            modifiedPropertyValue = TcgPsI18n.getI18nStringStripI18N(value);
        } else if(PROP_OUTPUT_TYPE.equals(name)) {
            modifiedPropertyValue = TcgPsI18n.getI18nStringStripI18N(value);
        } else if(PROP_INPUT_ID_LIST.equals(name)) {
            StringBuffer str = new StringBuffer("");
            List<OperatorComponent> inputs =  comp.getInputOperatorList();
            Iterator<OperatorComponent> it = inputs.iterator();
            while(it.hasNext()) {
                OperatorComponent input = it.next();
                str.append(input.getString(PROP_NAME));
                
                if(it.hasNext()) {
                    str.append(",");
                }
            }
            
            modifiedPropertyValue = str.toString();
        }
        
        return modifiedPropertyValue;
    }
    
    class EntityNodeComparator implements Comparator<EntityNode> {

        public int compare(EntityNode o1, EntityNode o2) {
            Rectangle rect1 = o1.getBoundingRect();
            Rectangle rect2 = o2.getBoundingRect();
            
            if(rect1.x < rect2.x && rect1.y < rect2.y) {
                return -1;
            } else if(rect1.x > rect2.x && rect1.y > rect2.y) {
                return 1;
            } else {
                return 0;
            }
        }


        
    }
}
