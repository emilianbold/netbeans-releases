package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.*;

import org.netbeans.jemmy.image.*;

import java.awt.*;

import java.awt.image.*;

import java.io.IOException;

import javax.swing.*;

public class jemmy_044 extends JemmyTest {

    JFrameOperator win;
    Robot robot;

    public int runIt(Object obj) {

	try {
	    (new ClassReference("org.netbeans.jemmy.testing.Application_044")).startApplication();

            new QueueTool().waitEmpty(1000);

            robot = new Robot();
	    win = new JFrameOperator("Application_044");

            BufferedImage images[] = getPictures();

            ImageComparator strict = new StrictImageComparator();
            if(!strict.compare(images[0], images[1])) {
                finalize();
                throw(new TestCompletedException(1, "StrictImageComparator true"));
            }
            if(strict.compare(images[0], images[2])) {
                finalize();
                throw(new TestCompletedException(1, "StrictImageComparator false"));
            }

            ImageComparator rough02 = new RoughImageComparator(.2);
            if(!rough02.compare(images[0], images[1])) {
                finalize();
                throw(new TestCompletedException(1, "RoughImageComparator 0.2 true 1"));
            }
            if(!rough02.compare(images[0], images[4])) {
                finalize();
                throw(new TestCompletedException(1, "RoughImageComparator 0.2 true 2"));
            }

            ImageComparator rough001 = new RoughImageComparator(.01);
            if(!rough001.compare(images[0], images[1])) {
                finalize();
                throw(new TestCompletedException(1, "RoughImageComparator 0.01 true"));
            }
            if(rough001.compare(images[0], images[2])) {
                finalize();
                throw(new TestCompletedException(1, "RoughImageComparator 0.01 false"));
            }

            ImageComparator color = 
                new ColorImageComparator(new ColorImageComparator.
                                         ForegroundColorMap(new JButtonOperator(win, 5).
                                                            getForeground().getRGB()),
                                         new ColorImageComparator.
                                         ForegroundColorMap(new JButtonOperator(win, 4).
                                                            getForeground().getRGB()));

            if(!color.compare(images[5], images[4])) {
                new PNGImageSaver().save(ImageTool.subtractImage(images[5], images[4]), "result.png");
                finalize();
                throw(new TestCompletedException(1, "ColorImageComparator true"));
            }

            color = 
                new ColorImageComparator(new ColorImageComparator.
                                         ForegroundColorMap(new JButtonOperator(win, 5).
                                                            getForeground().getRGB()),
                                         new ColorImageComparator.
                                         ForegroundColorMap(new JButtonOperator(win, 5).
                                                            getForeground().getRGB()));
            if(color.compare(images[5], images[4])) {
                new PNGImageSaver().save(ImageTool.subtractImage(images[5], images[4]), "result.png");
                finalize();
                throw(new TestCompletedException(1, "ColorImageComparator false"));
            }

            ImageFinder strictFinder = new StrictImageFinder(images[6]);
            if(strictFinder.findImage(images[0], 0) == null) {
                finalize();
                throw(new TestCompletedException(1, "StrictImageFinder true 0"));
            }
            if(strictFinder.findImage(images[0], 1) == null) {
                finalize();
                throw(new TestCompletedException(1, "StrictImageFinder true 1"));
            }
            if(strictFinder.findImage(images[0], 3) != null) {
                finalize();
                throw(new TestCompletedException(1, "StrictImageFinder false 0"));
            }

            ImageFinder strictFinderFalse = new StrictImageFinder(images[2]);
            if(strictFinderFalse.findImage(images[0], 0) != null) {
                finalize();
                throw(new TestCompletedException(1, "StrictImageFinderFalse false 1"));
            }

            ImageFinder roughFinder = new RoughImageFinder(images[6], .2);
            if(roughFinder.findImage(images[0], 3) == null) {
                finalize();
                throw(new TestCompletedException(1, "RoughImageFinder true 0"));
            }

            roughFinder = new RoughImageFinder(images[2], .2);
            if(roughFinder.findImage(images[0], 0) == null) {
                finalize();
                throw(new TestCompletedException(1, "RoughImageFinder true 1"));
            }

            roughFinder = new RoughImageFinder(images[0], .2);
            if(roughFinder.findImage(images[2], 0) != null) {
                finalize();
                throw(new TestCompletedException(1, "RoughImageFinder false"));
            }



	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

    BufferedImage[] getPictures() {
        BufferedImage[] images = new BufferedImage[7];
        images[0] = getPicture(0);
        images[1] = getPicture(1);
        images[2] = getPicture(2);
        images[3] = getPicture(3);
        images[4] = getPicture(4);
        images[5] = getPicture(5);
        images[6] = getPicture(win.getSource(), "window.png");
        return(images);
    }

    BufferedImage getPicture(int index) {
        return(getPicture(new JButtonOperator(win, index).getSource(),
                          "button" + index + ".png"));
    }

    BufferedImage getPicture(Component comp, String title) {
	try {
            BufferedImage result = ImageTool.getImage(comp);
            new PNGImageSaver().save(result, title);
            return(result);
	} catch(IOException e) {
            getOutput().printStackTrace(e);
            return(null);
        }
    }
}
