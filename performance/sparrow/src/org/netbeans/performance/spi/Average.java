/*
 * Average.java
 *
 * Created on October 16, 2002, 9:35 PM
 */

package org.netbeans.performance.spi;

/**An interface defining properties for standard statistical
 * analysis.
 *
 * @author  Tim Boudreau
 */
public interface Average {
    
    /** Returns the highest value in the set of values
     * used to create this element.
     * the set of values used to create this element.
     */
    public Float getMax();
    
    /** Returns the maximum percentage of the mean this
     * value varies by, calculated as<P>
     * <code>((Math.abs (mean - (Math.max (min, max)))/mean) * 100
     * </code>
     */
    public Float getMaxVariance();
    
    /** Returns the mean, or arithmetic average of
     * the set of values used to create this element.
     */
    public Float getMean();
    
    /** Returns the median value of the entry set used to create
     * this element
     */
    public Float getMedian();
    
    /** Returns the lowest value in the set of values
     * used to create this element.
     * the set of values used to create this element.
     */
    public Float getMin();
    
    /** Returns the values used to create this element.      */
    public float[] getSamples();
    
    /** Returns the standard deviation of the statistics used to
     * create this element.
     */
    public Double getStandardDeviation();
    
    /** Returns the standard deviation as a percentage of
     * the mean.
     */
    public Float getVariance();
    
}
