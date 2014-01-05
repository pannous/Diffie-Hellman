The [Diffie-Hellman key exchange](https://en.wikipedia.org/wiki/Diffie%E2%80%93Hellman_key_exchange) is a simple yet so far practically impeccably unhackable method to encrypt data transported between computers. Its foundation is so easy that it can be understood by any high school student. Still it is likely to be NSA proof for a couple more decades.

This is THE example for a magical mechanism called [perfect forward secrecy](https://en.wikipedia.org/wiki/Perfect_forward_secrecy), where two clients can safely communicate without any prior knowledge and without depending on a corruptible third authorization party. If you are familiar with Diffie-Hellman go-ahead and point out that some minor steps are necessary to fulfill the promise. If you are new to Diffie-Hellman don't hesitate to play through the wiki example yourself to get a feeling for this magic.

Once the Diffie-Hellman key exchange provided both parties with a shared encryption key, it should be used with safe algorithms such as RSA 4096 bit or AES 512 bit, as recommendated by the CCC and others. Fancy 'modern' elliptic encryption algorithms don't offer much except enormous complexity and potential backdoors.

