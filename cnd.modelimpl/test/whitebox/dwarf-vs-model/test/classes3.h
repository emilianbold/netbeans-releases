#ifndef _CLASSES3_H
#define _CLASSES3_H

namespace GEOM {

    class Point {

	private:
	    int x;
	    int y;

	public:
	    virtual int getX() const;

	    virtual int getY() const {
		return y;
	    }

	    virtual int getX();

	    virtual int getY() {
		return y;
	    }
    };

}

#endif // _CLASSES3_H
