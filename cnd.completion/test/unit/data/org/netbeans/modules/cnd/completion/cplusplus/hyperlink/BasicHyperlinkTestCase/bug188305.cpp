int bug188305_main(int argc, char** argv) {
    int a;
    
    struct SampleStruct{
        SampleStruct(int a){}
    } sampleStructInstance(a); //!!!cpp parser complains

    return 0;
}
