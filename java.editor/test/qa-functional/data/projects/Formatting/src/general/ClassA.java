/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package general;

/**
 *
 * @author jp159440
 */
@Anno(paramA="a Value", paramB="bValue")
public class ClassA {

    public int number=1;
    private String text="A";
    static float[] floats=new float[3];
    protected double[] doubles=new double[]{3.67, 40, 2e-30};


    static {
        floats[0]=12.6f;
    }

    public ClassA() {
    }

    public int method(String text, int number) {
        if (number == 13) {
            return (int)System.currentTimeMillis();
        } else {
            {
            }
        }
        for (int i=20; i < 100; i++) {
            synchronized (this) {
                while (i % 13 > 5) method(text + " ", number++);
            }
        }
        for (float f: floats) System.out.println(f);
        switch (number) {
            case 1:
                do {
                    out((2 + 3) * this.number--);
                } while (this.number > 6);
                return 10;
            case 2:
                try {
                    toString();
                } catch (IllegalStateException illegalStateException) {
                    illegalStateException.printStackTrace();
                } finally {
                    return 20;
                }
            default:
                return number > 100 ? -1 : -2;
        }
    }
}

