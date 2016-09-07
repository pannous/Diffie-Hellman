// javac DH.java && java DH
// "Just use libsodium if you can," also applies for every other language below
import java.math.*;
import java.util.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;


public class DH {

	int bitLength=512;	
	int certainty=20;// probabilistic prime generator 1-2^-certainty => practically 'almost sure'

    private static final SecureRandom rnd = new SecureRandom();
// byte[] randomBytes = new byte[32];
// csprng.nextBytes(randombytes);
// Important: Despite its name, don't use SecureRandom.getInstanceStrong()!
// On Linux, this is the equivalent to reading /dev/random which is a pointless performance killer. The default for new SecureRandom() in Java 8 is to read from /dev/urandom, which is what you want

	public static void main(String [] args) throws Exception
	{
		new DH();
	}

	public DH() throws Exception{
	    Random randomGenerator = new Random();
	    BigInteger generatorValue,primeValue,publicA,publicB,secretA,secretB,sharedKeyA,sharedKeyB;

	    primeValue = findPrime();// BigInteger.valueOf((long)g);
	    System.out.println("the prime is "+primeValue);
	     generatorValue	= findPrimeRoot(primeValue);//BigInteger.valueOf((long)p);
	    System.out.println("the generator of the prime is "+generatorValue);

		// on machine 1
	    secretA = new BigInteger(bitLength-2,randomGenerator);
		// on machine 2
	    secretB = new BigInteger(bitLength-2,randomGenerator);

		// to be published:
	    publicA=generatorValue.modPow(secretA, primeValue);
	    publicB=generatorValue.modPow(secretB, primeValue);
	    sharedKeyA = publicB.modPow(secretA,primeValue);// should always be same as:
	    sharedKeyB = publicA.modPow(secretB,primeValue);

	    System.out.println("the public key of A is "+publicA);
	    System.out.println("the public key of B is "+publicB);
	    System.out.println("the shared key for A is "+sharedKeyA);
	    System.out.println("the shared key for B is "+sharedKeyB);
	    System.out.println("The secret key for A is "+secretA);
	    System.out.println("The secret key for B is "+secretB);

	    String getAValue=sharedKeyA.toString();
	    String getBValue=sharedKeyB.toString();

	    MessageDigest md = MessageDigest.getInstance("SHA-256");
	    md.update(getAValue.getBytes());

	    byte byteData[] = md.digest();
	    StringBuffer sb = new StringBuffer();

	    for(int i=0;i<byteData.length;i++)
	    {
	        sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));// ??
	    }

	    String getHexValue = sb.toString();
	    System.out.println("hex format in SHA-256 is "+getHexValue);

	    byte [] key = getAValue.getBytes("UTF-8");

	    MessageDigest sha = MessageDigest.getInstance("SHA-256");
	    key =  sha.digest(key);
	    key = Arrays.copyOf(key, 16);
	    SecretKeySpec secretKeySpec =  new SecretKeySpec(key,"AES");

	    Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

	    CipherInputStream cipt = new CipherInputStream(new FileInputStream(new File("test.jpg")),cipher); // enter your filename here
	    FileOutputStream fop=new FileOutputStream(new File("testEncrypt.jpg"));

	    int i;
	       while((i=cipt.read())!= -1)
	           fop.write(i);

	       cipher.init(Cipher.DECRYPT_MODE,secretKeySpec);

	    CipherInputStream cipt2 = new CipherInputStream(new FileInputStream(new File("testEncrypt.jpg")),cipher); // encryption of image
	    FileOutputStream fop2 = new FileOutputStream(new File("testDecrypt.jpg"));//decryption of images

	    int j;
	    while((j=cipt2.read())!=-1)
	        fop2.write(j);

	}


   //    {
   //       System.load("/opt/local/lib/libcrypto.1.0.0.dylib");
   //    }
   // private native BigInteger BN_generate_prime(BigInteger r,int bits);
   // private native int BN_is_prime_fasttest_ex(BigInteger r,int nchecks);


	private static boolean miller_rabin_pass(BigInteger a, BigInteger n) {
	    BigInteger n_minus_one = n.subtract(BigInteger.ONE);
	    BigInteger d = n_minus_one;
		int s = d.getLowestSetBit();
		d = d.shiftRight(s);
	    BigInteger a_to_power = a.modPow(d, n);
	    if (a_to_power.equals(BigInteger.ONE)) return true;
	    for (int i = 0; i < s-1; i++) {
	        if (a_to_power.equals(n_minus_one)) return true;
	        a_to_power = a_to_power.multiply(a_to_power).mod(n);
	    }
	    if (a_to_power.equals(n_minus_one)) return true;
	    return false;
	}

	public static boolean miller_rabin(BigInteger n) {
	    for (int repeat = 0; repeat < 20; repeat++) {
	        BigInteger a;
	        do {
	            a = new BigInteger(n.bitLength(), rnd);
	        } while (a.equals(BigInteger.ZERO));
	        if (!miller_rabin_pass(a, n)) {
	            return false;
	        }
	    }
	    return true;
	}

boolean isPrime(BigInteger r){
	return miller_rabin(r);
	// return BN_is_prime_fasttest_ex(r,bitLength)==1;
}

public List<BigInteger> primeFactors(BigInteger number) {
    BigInteger n = number;
	BigInteger i=BigInteger.valueOf(2);
	BigInteger limit=BigInteger.valueOf(10000);// speed hack! -> consequences ???
   	List<BigInteger> factors = new ArrayList<BigInteger>();
   	while (!n.equals(BigInteger.ONE)){
		while (n.mod(i).equals(BigInteger.ZERO)){
        factors.add(i);
		n=n.divide(i);
		// System.out.println(i);
		// System.out.println(n);
		if(isPrime(n)){
			factors.add(n);// yes?
			return factors;
		}
     	}
		i=i.add(BigInteger.ONE);
		if(i.equals(limit))return factors;// hack! -> consequences ???
		// System.out.print(i+"    \r");
	}
		System.out.println(factors);
   return factors;
 }

boolean isPrimeRoot(BigInteger g, BigInteger p)
{
    BigInteger totient = p.subtract(BigInteger.ONE); //p-1 for primes;// factor.phi(p);
    List<BigInteger> factors = primeFactors(totient);
    int i = 0;
    int j = factors.size();
    for(;i < j; i++)
    {
        BigInteger factor = factors.get(i);//elementAt
        BigInteger t = totient.divide( factor);
		if(g.modPow(t, p).equals(BigInteger.ONE))return false;
    }
    return true;
}

String download(String address){
	String txt="";
   	URLConnection conn = null;
    InputStream in = null;
    try {
        URL url = new URL(address);
        conn = url.openConnection();
        conn.setReadTimeout(10000);//10 secs
        in = conn.getInputStream();
        byte[] buffer = new byte[1024];
        int numRead;
		String encoding = "UTF-8";
        while ((numRead = in.read(buffer)) != -1) {
				txt+=new String(buffer, 0, numRead, encoding);
        }
    } catch (Exception exception) {
        exception.printStackTrace();
    }
	return txt;
}

void compareWolfram(BigInteger p){
	// String g= download("http://www.wolframalpha.com/input/?i=primitive+root+"+p);
	String url="http://api.wolframalpha.com/v2/query?appid=&input=primitive+root+"+p;
	System.out.println(url);
	String g= download(url);;
	String[] vals=g.split(".plaintext>");
	if(vals.length<3)	System.out.println(g);
	else System.out.println("wolframalpha generatorValue "+vals[3]);	
}

BigInteger findPrimeRoot(BigInteger p){
	int start= 2001;// first best probably precalculated by NSA?
	// preferably  3, 17 and 65537
	if(start==2)compareWolfram(p);

	for(int i=start;i<100000000;i++)
		if(isPrimeRoot(BigInteger.valueOf(i),p))
			return BigInteger.valueOf(i);
			// if(isPrimeRoot(i,p))return BigInteger.valueOf(i);
	return BigInteger.valueOf(0);
}


BigInteger findPrime(){
	Random rnd=new Random();
	BigInteger p=BigInteger.ZERO;
	// while(!isPrime(p))
	p= new BigInteger(bitLength, certainty, rnd);// sufficiently NSA SAFE?!!
	return p;
		
	// BigInteger r;
	// BigInteger r2= BN_generate_prime(r,512);
	//  System.out.println("isPrime(i)? "+r+" "+r2);
	// return r;
}

// Better for NSA:?
// Some of the largest primes not known to have any particular form (that is, no simple formula such as that of Mersenne primes) have been found by taking a piece of semi-random binary data, converting it to a number n, multiplying it by 256k for some positive integer k, and searching for possible primes within the interval [256kn + 1, 256k(n + 1) − 1].
// long findPrime(long from,long to){
// long findPrime2(){
// 	Random randomGenerator = new Random();
// 	
// 	// long n=randomGenerator.nextLong();
// 	int k=2;//randomGenerator.nextInt(2);
// 	long n=randomGenerator.nextInt();
// 	long from=(long)Math.pow(256,k)*n;
// 	long to=(long)Math.pow(256,k)*(n+1);
// 	// BigInteger
// 
//     System.out.println("from "+from);
//     System.out.println("to   "+to);
// 	for(long i=from;i<to;i++){
// 	    System.out.println("isPrime(i)? "+i);
// 		if(isPrime(i))return i;
// 	}
// 	return 0;
// }


}