
namespace bug226516_common {
namespace bug226516_api {
    enum {
        bug226516_first = 1,
        bug226516_second = 2,
    };
}
}

namespace bug226516_aaa {
namespace bug226516_bbb {
    namespace bug226516_api = ::bug226516_common::bug226516_api;

    void bug226516_f()
    {
        // !!! shall follow link to common::api::first
        std::cout << "val=" << bug226516_api::bug226516_first << std::endl;
    }
}
}

namespace bug226516_ccc {
namespace bug226516_ddd {
    using namespace bug226516_common::bug226516_api;
}
}
/*
 * 
 */

namespace bug226516_cd {
    struct bug226516_AA {
    };
}


int bug226516_main(int argc, char** argv) 
{
    // this shall get precendence over above 
    namespace bug226516_cd = ::bug226516_ccc::bug226516_ddd;

    bug226516_aaa::bug226516_bbb::bug226516_f();

    // !!! shall follow link to common::api::second
    std::cout << "val=" << bug226516_cd::bug226516_second << std::endl;


    // !!! shall not follow the link to cd::AA as it shall be ::ccc::ddd::AA -
    //compile time error
    bug226516_cd::bug226516_AA aa;
    
    ::bug226516_cd::bug226516_AA aa;

    return 0;
}