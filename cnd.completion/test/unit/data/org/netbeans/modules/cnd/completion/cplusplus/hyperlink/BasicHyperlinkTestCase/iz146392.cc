#define NULL 0

Product* release_prt(Product* pProduct) {
    AbstractProduct* pPointer(pProduct);
    pProduct = NULL;
    return pPointer;
}
