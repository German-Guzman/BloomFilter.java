package com.github.lovasoa.bloomfilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BloomFilter implements Cloneable{
	public ArrayList<Integer> hashes;
	// private RandomInRange prng;
	public int k; // Number of hash functions
	public ArrayList<Integer> bloom;
	public static final double LN2 = 0.6931471805599453; // ln(2)

	/**
	 * Create a new bloom filter.
	 * 
	 * @param n Expected number of elements
	 * @param m Desired size of the container in bits
	 **/
	public BloomFilter(int m, int n) {
		k=1;
		//k = (int) Math.round((m / n) * LN2);
		if (k <= 0)
			k = 1;
		this.bloom = new ArrayList<Integer>(Collections.nCopies(m, 0));

	}


	public class Hash {
		public void hashpass(String Pass, int k) throws NoSuchAlgorithmException {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(Pass.getBytes());
			byte[] digest = md.digest();
			BigInteger bi = new BigInteger(digest);
			String hashtext = bi.toString(16);
			System.out.println(hashtext);

		}

	}

	public int[] hashk(Integer element, int k) {
		int[] hashes = new int[k];
		int hashValue = element.hashCode();

		for (int i = 0; i < k; i++) {
			hashes[i] = ((hashValue * 13)) + i * 7;
			// System.out.println(hashes[i]);
		}
		return hashes;
	}

	/**
	 * Add an element to the container
	 * 
	 * @param element
	 **/
	public void add(Integer element) {
		int[] hashes = hashk(element, k);
		// int[] hashes = hashCassandra(element.byteValue(),this.bloom.size(),k);
		for (int i = 0; i < k; i++) {

			bloom.set(Math.abs(hashes[i] % this.bloom.size()), 1);
			// System.out.println(hashes[i] % this.bloom.size());
			// System.out.println(hashes[i]);
		}
	}

	/**
	 * Removes all of the elements from this filter.
	 **/
	public void clear() {
		hashes.clear();
	}

	/**
	 * Create a copy of the current filter
	 **/
	public BloomFilter clone() throws CloneNotSupportedException {
		return (BloomFilter) super.clone();
	}

	/**
	 * Generate a unique hash representing the filter
	 **/
	// public int hashCode() {
	// return hashes.hashCode() ;
	// }

	/**
	 * Test if the filters have equal bitsets. WARNING: two filters may contain the
	 * same elements, but not be equal (if the filters have different size for
	 * example).
	 */
	public boolean equals(BloomFilter other) {
		return this.hashes.equals(other.hashes) && this.k == other.k;
	}

	/**
	 * Merge another bloom filter into the current one. After this operation, the
	 * current bloom filter contains all elements in other.
	 **/
	/*
	 * public void merge(BloomFilter other) { if (other.k != this.k ||
	 * other.hashes.size() != this.hashes.size()) { throw new
	 * IllegalArgumentException("Incompatible bloom filters"); } ((BitSet)
	 * this.hashes).or(other.hashes); }
	 */

	public void addAll(HashSet<Integer> set1) {
		for (Integer element : set1)
			add(element);
	}

	public static int[] hashCassandra(byte[] value, int m, int k) {
		int[] result = new int[k];
		long hash1 = murmur3(0, value);
		long hash2 = murmur3((int) hash1, value);
		for (int i = 0; i < k; i++) {
			result[i] = (int) ((hash1 + i * hash2) % m);
		}
		return result;
	}

	public static long murmur3(int seed, byte[] bytes) {
		return Integer.toUnsignedLong(murmur3_signed(seed, bytes));
	}

	public static int murmur3_signed(int seed, byte[] bytes) {
		int h1 = seed;
		// Standard in Guava
		int c1 = 0xcc9e2d51;
		int c2 = 0x1b873593;
		int len = bytes.length;
		int i = 0;

		while (len >= 4) {
			// process()
			int k1 = (bytes[i++] & 0xFF);
			k1 |= (bytes[i++] & 0xFF) << 8;
			k1 |= (bytes[i++] & 0xFF) << 16;
			k1 |= (bytes[i++] & 0xFF) << 24;

			k1 *= c1;
			k1 = Integer.rotateLeft(k1, 15);
			k1 *= c2;

			h1 ^= k1;
			h1 = Integer.rotateLeft(h1, 13);
			h1 = h1 * 5 + 0xe6546b64;

			len -= 4;
		}

		// processingRemaining()
		int k1 = 0;
		switch (len) {
		case 3:
			k1 ^= (bytes[i + 2] & 0xFF) << 16;
			// fall through
		case 2:
			k1 ^= (bytes[i + 1] & 0xFF) << 8;
			// fall through
		case 1:
			k1 ^= (bytes[i] & 0xFF);

			k1 *= c1;
			k1 = Integer.rotateLeft(k1, 15);
			k1 *= c2;
			h1 ^= k1;
		}
		i += len;

		// makeHash()
		h1 ^= i;

		h1 ^= h1 >>> 16;
		h1 *= 0x85ebca6b;
		h1 ^= h1 >>> 13;
		h1 *= 0xc2b2ae35;
		h1 ^= h1 >>> 16;

		return h1;
	}
}