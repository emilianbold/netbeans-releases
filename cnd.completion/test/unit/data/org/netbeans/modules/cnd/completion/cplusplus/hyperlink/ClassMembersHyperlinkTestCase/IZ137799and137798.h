// Example from Freeway:
const int NZONES = 3;
const int NSPEEDS = 5;

typedef void* Widget;

class FwyZoneObjects {
    public:
        void objects_initialize(Widget);
        
        struct Zone {
            Widget spop;
            Widget splab;
            Widget spcas;
            Widget spmen;
            Widget sp[NSPEEDS];
            Widget spzonel;
            Widget spzoneu;
        } zz[NZONES];
};
