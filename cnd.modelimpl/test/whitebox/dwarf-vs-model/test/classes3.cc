
#include "classes3.h"

namespace GEOM {

    int Point::getX() {
	return x;
    }

    int test_1(Point p) {
	return p.getX() + p.getY();
    }

    int test_2(Point &p) {
	return p.getX() + p.getY();
    }

}

int GEOM::Point::getX() const {
    return x;
}
