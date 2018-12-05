package party.loveit.eosforandroidlibrary.ecc;

import java.math.BigInteger;

import party.loveit.eosforandroidlibrary.rpc.vo.transaction.push.TxSign;
import party.loveit.eosforandroidlibrary.utils.Base58;
import party.loveit.eosforandroidlibrary.utils.ByteBuffer;
import party.loveit.eosforandroidlibrary.utils.ByteUtils;
import party.loveit.eosforandroidlibrary.utils.EException;
import party.loveit.eosforandroidlibrary.utils.Hex;
import party.loveit.eosforandroidlibrary.utils.ObjectUtils;
import party.loveit.eosforandroidlibrary.utils.Ripemd160;
import party.loveit.eosforandroidlibrary.utils.Sha;


/**
 * Ecc
 * 
 * @author espritblock http://eblock.io
 *
 */
public class EccTool {

	public static final String address_prefix = "EOS";

	public static final Secp256k secp = new Secp256k();

	/**
	 * 通过种子生成私钥
	 *
	 * @param priKey 种子
	 * @return
	 */
	public static String privateKeyFromSeed(BigInteger priKey) {
		return seedShaPrivate(bigIntegerToBytes(priKey,32));
	}

	/**
	 *
	 * @param seed
	 * @return
	 */
	public static String privateKeyFromSeed(byte[] seed) {
		return seedShaPrivate(seed);
	}

	/**
	 * seedPrivate
	 *
	 * @return
	 */
	private static String seedShaPrivate(byte[] seedSha) {
		if (seedSha == null || seedSha.length == 0) {
			throw new EException("seed_empty", "seed is empty");
		}
		byte[] a = {(byte) 0x80};
		byte[] b = seedSha;
		byte[] private_key = ByteUtils.concat(a, b);
		byte[] checksum = Sha.SHA256(private_key);
		checksum = Sha.SHA256(checksum);
		byte[] check = ByteUtils.copy(checksum, 0, 4);
		byte[] pk = ByteUtils.concat(private_key, check);
		return Base58.encode(pk);
	}

	/**
	 *
	 * @param b
	 * @param numBytes
	 * @return
	 */
	private static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
		if (b == null) {
			return null;
		}
		byte[] bytes = new byte[numBytes];
		byte[] biBytes = b.toByteArray();
		int start = (biBytes.length == numBytes + 1) ? 1 : 0;
		int length = Math.min(biBytes.length, numBytes);
		System.arraycopy(biBytes, start, bytes, numBytes - length, length);
		return bytes;
	}
	/**
	 * privateKey
	 * 
	 * @param pk
	 * @return
	 */
	private static BigInteger privateKey(String pk) {
		byte[] private_wif = Base58.decode(pk);
		byte version = (byte) 0x80;
		if (private_wif[0] != version) {
			throw new EException("version_error", "Expected version " + 0x80 + ", instead got " + version);
		}
		byte[] private_key = ByteUtils.copy(private_wif, 0, private_wif.length - 4);
		byte[] new_checksum = Sha.SHA256(private_key);
		new_checksum = Sha.SHA256(new_checksum);
		new_checksum = ByteUtils.copy(new_checksum, 0, 4);
		byte[] last_private_key = ByteUtils.copy(private_key, 1, private_key.length - 1);
		BigInteger d = new BigInteger(Hex.bytesToHexString(last_private_key), 16);
		return d;
	}

	/**
	 * privateToPublic
	 * 
	 * @param pk
	 * @return
	 */
	public static String privateToPublic(String pk) {
		if (pk == null || pk.length() == 0) {
			throw new EException("args_empty", "args is empty");
		}
		// private key
		BigInteger d = privateKey(pk);
		// publick key
		Point ep = secp.G().multiply(d);
		byte[] pub_buf = ep.getEncoded();
		byte[] csum = Ripemd160.from(pub_buf).bytes();
		csum = ByteUtils.copy(csum, 0, 4);
		byte[] addy = ByteUtils.concat(pub_buf, csum);
		StringBuffer bf = new StringBuffer(address_prefix);
		bf.append(Base58.encode(addy));
		return bf.toString();
	}

	/**
	 * signHash
	 * @param pk
	 * @param b
	 * @return
	 */
	public static String signHash(String pk, byte[] b) {
		String dataSha256 = Hex.bytesToHexString(Sha.SHA256(b));
		BigInteger e = new BigInteger(dataSha256, 16);
		int nonce = 0;
		int i = 0;
		BigInteger d = privateKey(pk);
		Point Q = secp.G().multiply(d);
		nonce = 0;
		Ecdsa ecd = new Ecdsa(secp);
		Ecdsa.SignBigInt sign;
		while (true) {
			sign = ecd.sign(dataSha256, d, nonce++);
			byte der[] = sign.getDer();
			byte lenR = der[3];
			byte lenS = der[5 + lenR];
			if (lenR == 32 && lenS == 32) {
				i = ecd.calcPubKeyRecoveryParam(e, sign, Q);
				i += 4; // compressed
				i += 27; // compact // 24 or 27 :( forcing odd-y 2nd key candidate)
				break;
			}
		}
		byte[] pub_buf = new byte[65];
		pub_buf[0] = (byte) i;
		ByteUtils.copy(sign.getR().toByteArray(), 0, pub_buf, 1, sign.getR().toByteArray().length);
		ByteUtils.copy(sign.getS().toByteArray(), 0, pub_buf, sign.getR().toByteArray().length + 1,
				sign.getS().toByteArray().length);

		byte[] checksum = Ripemd160.from(ByteUtils.concat(pub_buf, "K1".getBytes())).bytes();

		byte[] signatureString = ByteUtils.concat(pub_buf, ByteUtils.copy(checksum, 0, 4));

		return "SIG_K1_" + Base58.encode(signatureString);
	}

	/**
	 * sign string
	 * @param pk
	 * @param data
	 * @return
	 */
	public static String sign(String pk, String data) {
		String dataSha256 = Hex.bytesToHexString(Sha.SHA256(data));
		BigInteger e = new BigInteger(dataSha256, 16);
		int nonce = 0;
		int i = 0;
		BigInteger d = privateKey(pk);
		Point Q = secp.G().multiply(d);
		nonce = 0;
		Ecdsa ecd = new Ecdsa(secp);
		Ecdsa.SignBigInt sign;
		while (true) {
			sign = ecd.sign(dataSha256, d, nonce++);
			byte der[] = sign.getDer();
			byte lenR = der[3];
			byte lenS = der[5 + lenR];
			if (lenR == 32 && lenS == 32) {
				i = ecd.calcPubKeyRecoveryParam(e, sign, Q);
				i += 4; // compressed
				i += 27; // compact // 24 or 27 :( forcing odd-y 2nd key candidate)
				break;
			}
		}
		byte[] pub_buf = new byte[65];
		pub_buf[0] = (byte) i;
		ByteUtils.copy(sign.getR().toByteArray(), 0, pub_buf, 1, sign.getR().toByteArray().length);
		ByteUtils.copy(sign.getS().toByteArray(), 0, pub_buf, sign.getR().toByteArray().length + 1,
				sign.getS().toByteArray().length);

		byte[] checksum = Ripemd160.from(ByteUtils.concat(pub_buf, "K1".getBytes())).bytes();

		byte[] signatureString = ByteUtils.concat(pub_buf, ByteUtils.copy(checksum, 0, 4));

		return "SIG_K1_" + Base58.encode(signatureString);
	}

	/**
	 * signTransaction 
	 * @param privateKey
	 * @param push
	 * @return
	 */
	public static String signTransaction(String privateKey, TxSign push) {
		// tx
		ByteBuffer bf = new ByteBuffer();
		ObjectUtils.writeBytes(push, bf);
		byte[] real = bf.getBuffer();
		// append
		real = ByteUtils.concat(real, java.nio.ByteBuffer.allocate(33).array());

		// final byte [] b = real.clone();
		// int[] a = IntStream.range(0, b.length).map(i -> b[i] & 0xff).toArray();
		// for(int i=1;i<=a.length;i++) {
		// System.out.print(a[i-1]+","+((i%8==0)?"\n":""));
		// }
		return signHash(privateKey, real);
	}

}
