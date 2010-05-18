package org.netbeans.modules.iep.editor.designer.nodes;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.tbls.editor.ps.DocumentationProperty;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.tbls.model.ArrayHashMap;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;

public class PlanNode extends AbstractNode {

    /**
     * The logger.
     */
    private static final Logger mLogger = Logger.getLogger(PlanNode.class.getName());
    private Sheet mSheet = null;
    private PlanComponent mPlanComponent;
    private Node mOriginal;

    public PlanNode(Node original, PlanComponent component) {
        super(Children.LEAF);
        mOriginal = original;
        this.mPlanComponent = component;
    }
    
    @Override
    public String getDisplayName() {
        return mOriginal.getDisplayName();
    }

    @Override
    protected Sheet createSheet() {
        mSheet = new Sheet();
        addToSheet(mSheet);
        //RA do not show alert and logger properties
        //addLoggerAlertProps();
        return mSheet;
    }

    private void addToSheet(Sheet sheet) {
        ArrayHashMap ssTable = new ArrayHashMap();
        try {
            Node orginal = mOriginal;
            PropertySet[] propertySets = orginal.getPropertySets();
            if (propertySets.length != 0) {
                PropertySet pSet = propertySets[0];

                Sheet.Set ss = null;
                ss = new Sheet.Set();
                ss.setName(pSet.getName());
                ss.setDisplayName(pSet.getDisplayName());
                ssTable.put(pSet.getName(), ss);
                ss.setExpert(true);

                Property[] properties = pSet.getProperties();
                if (properties != null) {
                    ss.put(properties);
                }

                DocumentationProperty docProp = new DocumentationProperty(String.class, this.mPlanComponent);
                ss.put(docProp);
            }

            List ssList = ssTable.getValueList();
            for (int i = 0, I = ssList.size(); i < I; i++) {
                sheet.put((Sheet.Set) ssList.get(i));
            }
        } catch (Exception ex) {
            mLogger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
