/*
 * NbChart.java
 *
 * Created on October 16, 2002, 10:32 PM
 */

package org.netbeans.performance.impl.chart;
import java.util.*;
import com.jrefinery.chart.*;
import com.jrefinery.data.*;
import java.awt.Font;
/** A convenience extension of JFreeChart that hides some of the
 *  complexity of creating charts where we do things in a
 *  fairly standard way.
 *
 * @author  Tim Boudreau
 */
public class NbChart extends JFreeChart {
    
    /** Creates a new instance of NbChart */
    public NbChart(String title, String xAxisTitle, String yAxisTitle, NbStatisticalDataset data) {
        super (title,  new Font("Helvetica", Font.BOLD, 14), 
          new VerticalCategoryPlot (data, 
            new HorizontalCategoryAxis(xAxisTitle),
            new VerticalNumberAxis(yAxisTitle),
            new VerticalStatisticalBarRenderer()),
            true);
    }
    
}
