/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.sap.model;

import com.sap.conn.jco.JCoTable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tli
 */
public class IDocSegmentDef {
    public int NR;            // Sequential Number of Segment in IDoc Type
    public String SEGMENTTYP; // Segment type in 30-character format
    public String SEGMENTDEF; // IDoc Development: Segment definition
    public String QUALIFIER;  // Flag: Qualified segment in IDoc
    public int SEGLEN;        // Length of Field (Number of Characters)
    public String PARSEG;     // Segment type in 30-character format
    public int PARPNO;        // Sequential number of parent segment
    public String PARFLG;     // Flag for parent segment: Segment is start of segment group
    public String MUSTFL;     // Flag: Mandatory entry
    public int OCCMIN;        // Minimum number of segments in sequence
    public String OCCMAX;        // Maximum number of segments in sequence
    public int HLEVEL;        // Hierarchy level of IDoc type segment
    public String DESCRP;     // Short description of object
    public String GRP_MUSTFL; // Flag for groups: Mandatory
    public int GRP_OCCMIN;    // Minimum number of groups in sequence
    public String GRP_OCCMAX;    // Maximum number of groups in sequence
    public String REFSEGTYP;  // Segment type in 30-character format

    List<IDocSegmentDef> cList = new ArrayList<IDocSegmentDef>();

    public IDocSegmentDef(JCoTable segTable, int i) {
        segTable.setRow(i);
//        System.out.println(i + ": " + segTable.getString("SEGMENTTYP")
//                        + ", [" + segTable.getString("OCCMAX")
//                        + "], " + segTable.getString("NR"));

        NR = segTable.getInt("NR");
        SEGMENTTYP = segTable.getString("SEGMENTTYP");
        SEGMENTDEF = segTable.getString("SEGMENTDEF");
        QUALIFIER = segTable.getString("QUALIFIER");
        SEGLEN = segTable.getInt("SEGLEN");
        PARSEG = segTable.getString("PARSEG");
        PARPNO = segTable.getInt("PARPNO");
        PARFLG = segTable.getString("PARFLG");
        MUSTFL = segTable.getString("MUSTFL");
        OCCMIN = segTable.getInt("OCCMIN");
        OCCMAX = segTable.getString("OCCMAX"); // can be 9999999999
        HLEVEL = segTable.getInt("HLEVEL");
        DESCRP = segTable.getString("DESCRP");
        GRP_MUSTFL = segTable.getString("GRP_MUSTFL");
        GRP_OCCMIN = segTable.getInt("GRP_OCCMIN");
        GRP_OCCMAX = segTable.getString("GRP_OCCMAX"); // can be 9999999999
        REFSEGTYP = segTable.getString("REFSEGTYP");
    }

    public boolean isLeaf() {
        // only valid after parsing the segment table
        return (cList.size() > 0);
    }

    public void addChild(IDocSegmentDef child) {
        cList.add(child);
    }

    public List<IDocSegmentDef> getChildren() {
        return cList;
    }

    public String getMaxValue(String s) {
        try {
            int val = Integer.valueOf(s);
            if (val < 5000) { // xsd limit
                return "" + val; // trim 0's
            }
            return "unbounded";
        } catch (Exception ex) {
            // not in range...
        }
        if (s.endsWith("999")) {
            return "unbounded";
        }
        return s;
    }
}