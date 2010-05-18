package org.netbeans.modules.iep.project.anttasks;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.WsOperatorComponent;
import org.netbeans.modules.iep.model.NameUtil;
import org.netbeans.modules.iep.model.WSType;
import org.netbeans.modules.iep.model.share.SharedConstants;

public class Util {

    private static final String IEP_FILE_EXTENSION = "iep"; // NOI18N
    
    private static final String INPUT_PORT_TYPE = "InputPt";
    private static final String INPUT_ROLE_NAME = "InputRn";
    private static final String INPUT_PARTNER_LINK = "InputPl";

    public static List<PortMapEntry> generatePortMapEntryList(IEPModel model, String tns,
	    	String processName, String filePath) throws Exception {
        List<PortMapEntry> pmeList = new ArrayList<PortMapEntry>();

        List<WsOperatorComponent> inList = model.getWebServiceList(WSType.IN_ONLY);
        List<WsOperatorComponent> outList = model.getWebServiceList(WSType.OUT_ONLY);
        List<WsOperatorComponent> reqRepList= model.getWebServiceList(WSType.REQUEST_REPLY);
        if (inList.size() > 0) {
            String partnerLink = tns + ":" + INPUT_PARTNER_LINK;
            String portType = tns + ":" + INPUT_PORT_TYPE;
            PortMapEntry pme = new PortMapEntry(partnerLink, portType, 
        	    PortMapEntry.MY_ROLE, INPUT_ROLE_NAME, processName, filePath);
            pmeList.add(pme);
        }
        for (int i = 0, I = outList.size(); i < I; i++) {
            WsOperatorComponent op = outList.get(i);
            String name = op.getProperty(SharedConstants.PROP_NAME).getValue();
            name = NameUtil.makeJavaId(name);
            String partnerLink = tns + ":" + getOutputPartnerLink(name);
            String portType = tns + ":" + getOutputPortType(name);
            String roleName = getOutputRoleName(name);
            PortMapEntry pme = new PortMapEntry(partnerLink, portType, 
        	    PortMapEntry.PARTNER_ROLE, roleName, processName, filePath);
            pmeList.add(pme);
        }
        for (int i = 0, I = reqRepList.size(); i < I; i++) {
            WsOperatorComponent op = reqRepList.get(i);
            String name = op.getProperty(SharedConstants.PROP_NAME).getValue();
            name = NameUtil.makeJavaId(name);
            String partnerLink = tns + ":" + getRequestReplyPartnerLink(name);
            String portType = tns + ":" + getRequestReplyPortType(name);
            String roleName = getRequestReplyRoleName(name);
            PortMapEntry pme = new PortMapEntry(partnerLink, portType, 
        	    PortMapEntry.PARTNER_ROLE, roleName, processName, filePath);
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
    
    private static String getRequestReplyPartnerLink(String opJName) {
        return "RequestReplyPl_" + opJName;
    }

    private static String getRequestReplyPortType(String opJName) {
        return "RequestReplyPt_" + opJName;
    }

    private static String getRequestReplyRoleName(String opJName) {
        return "RequestReplyRn_" + opJName;
    }
    
    public static String getRelativePath(File home, File f){
        return matchPathLists(getPathList(home), getPathList(f));
    }
    
    private static List getPathList(File f) {
        List l = new ArrayList();
        File r;
        try {
            r = f.getCanonicalFile();
            while(r != null) {
                l.add(r.getName());
                r = r.getParentFile();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            l = null;
        }
        return l;
    }
    
    private static String matchPathLists(List r, List f) {
        int i;
        int j;
        String s;
        // start at the beginning of the lists
        // iterate while both lists are equal
        s = "";
        i = r.size()-1;
        j = f.size()-1;

        // first eliminate common root
        while((i >= 0)&&(j >= 0)&&(r.get(i).equals(f.get(j)))) {
            i--;
            j--;
        }

        // for each remaining level in the home path, add a ..
        for(;i>=0;i--) {
            s += ".." + File.separator;
        }

        // for each level in the file path, add the path
        for(;j>=1;j--) {
            s += f.get(j) + File.separator;
        }

        // file name
        s += f.get(j);
        return s;
    }
    
    public static String getIepProcessName(String name) {
	// iep qualified name from the model.getIEPFileName() is of the 
	// form ex: newIepProcess.iep, this has to be striped out and 
	// returned as newIepProcess.
	int _index = name.lastIndexOf(".");
	if (_index != -1) {
	    name = name.substring(0, _index);
	}
	return name;
    }

    
    public static class IEPFileFilter implements FileFilter {

        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
              return true;
            }
            String fileName = pathname.getName();
            int dotIndex = fileName.lastIndexOf('.');
            String fileExtension = null;

            if (dotIndex != -1) {
                fileExtension = fileName.substring(dotIndex + 1);
            }
            if (fileExtension == null) {
                return false;
            }
            if (fileExtension.equalsIgnoreCase(IEP_FILE_EXTENSION)) {
              return true;
            }
            return false;
        }
    }

}
