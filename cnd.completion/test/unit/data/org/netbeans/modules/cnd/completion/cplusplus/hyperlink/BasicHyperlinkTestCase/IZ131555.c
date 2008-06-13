struct adr_s {
    char *adr_now;
};
adr_int32(adr_s *adr, int i) {
        *adr->adr_now++;
        &adr->adr_now++;
         adr->adr_now++;
        *adr->adr_now;
        &adr->adr_now;
         adr->adr_now;
    i = *adr->adr_now;
    i = &adr->adr_now;
    i = adr->adr_now;
}
