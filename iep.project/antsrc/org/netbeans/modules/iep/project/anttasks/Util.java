package org.netbeans.modules.iep.project.anttasks;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.InputOperatorComponent;
import org.netbeans.modules.iep.model.OutputOperatorComponent;

public class Util {

    private static final String INPUT_PORT_TYPE = "InputPt";
    private static final String INPUT_ROLE_NAME = "InputRn";
    private static final String INPUT_PARTNER_LINK = "InputPl";
    private static String NAME_KEY = "name";

    public static List<PortMapEntry> generatePortMapEntryList(IEPModel model, String tns) throws Exception {
        List<PortMapEntry> pmeList = new ArrayList<PortMapEntry>();

        boolean wsOnly = true;
        List<InputOperatorComponent> inList = model.getInputList(wsOnly);
        List<OutputOperatorComponent> outList = model.getOutputList(wsOnly);
        if (inList.size() > 0) {
            String partnerLink = tns + ":" + INPUT_PARTNER_LINK;
            String portType = tns + ":" + INPUT_PORT_TYPE;
            PortMapEntry pme = new PortMapEntry(partnerLink, portType, PortMapEntry.MY_ROLE, INPUT_ROLE_NAME);
            pmeList.add(pme);
        }
        for (int i = 0, I = outList.size(); i < I; i++) {
            OutputOperatorComponent op = outList.get(i);
            String name = op.getProperty(NAME_KEY).getValue();
            name = NameUtil.makeJavaId(name);
            String partnerLink = tns + ":" + getOutputPartnerLink(name);
            String portType = tns + ":" + getOutputPortType(name);
            String roleName = getOutputRoleName(name);
            PortMapEntry pme = new PortMapEntry(partnerLink, portType, PortMapEntry.PARTNER_ROLE, roleName);
            pmeList.add(pme);
        }
        return pmeList;
    }

    private static String getOutputPartnerLink(String opJName) {
        return "OutputPl_" + opJName;
    }

    private static String getOutputPortType(String opJName) {
        return "OutputPt_" + opJName;
    }

    private static String getOutputRoleName(String opJName) {
        return "OutputRn_" + opJName;
    }
}
