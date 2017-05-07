package org.notmysock.hive.joins;

import java.util.Arrays;
import java.util.Random;
import java.util.function.IntPredicate;

public class HashDistributionTest {

  public interface HashFunction {
    public long hash(long key);
  }

  public static class simpleshift implements HashFunction {
    public long hash(long key) {
      key ^= (key >>> 7) ^ (key >>> 4);
      key ^= (key >>> 20) ^ (key >>> 12);
      return (int) key;
    }
  }

  public static class hash64shift implements HashFunction {
    public long hash(long key) {
      key = (~key) + (key << 21); // key = (key << 21) - key - 1;
      key = key ^ (key >>> 24);
      key = (key + (key << 3)) + (key << 8); // key * 265
      key = key ^ (key >>> 14);
      key = (key + (key << 2)) + (key << 4); // key * 21
      key = key ^ (key >>> 28);
      key = key + (key << 31);
      key = key ^ (key >>> 16);
      return (int) key;
    }
  }

  public static class hash6432shift implements HashFunction {
    public long hash(long key) {
      key = (~key) + (key << 18); // key = (key << 18) - key - 1;
      key = key ^ (key >>> 31);
      key = key * 21; // key = (key + (key << 2)) + (key << 4);
      key = key ^ (key >>> 11);
      key = key + (key << 6);
      key = key ^ (key >>> 22);
      return (int) key;
    }
  }
  
  public static int[] testForCollisions(HashFunction hf, int values) {
    Random r = new Random(1234);
    int width = 1 << (int)Math.ceil(Math.log(values)/Math.log(2));
    int[] counters = new int[width];
    Arrays.fill(counters, 0);
    for (int i = 1; i < values; i++) {
      long h = Math.abs(hf.hash((long)2*i));
      counters[(int)(h % width)]++;
    }
    return counters;
  }
  
  public static int maxCollisions(int[] counters) {
    return Arrays.stream(counters).max().orElse(0);
  }
  
  public static int emptySlots(int[] counters) {
    IntPredicate zero = new IntPredicate() {
      @Override
      public boolean test(int value) {
        return value == 0;
      }
    };
    return (int) Arrays.stream(counters).filter(zero).count();
  }
  
  public static int countCollisions(int[] counters) {
    IntPredicate collision = new IntPredicate() {
      @Override
      public boolean test(int value) {
        return value > 1;
      }
    };
    return (int) Arrays.stream(counters).filter(collision).sum();
  }


  public static void main(String[] args) throws Exception {
    int values = 65*1000*1000; // customer db at 10Tb scale
    if (args.length == 1) {
      values = Integer.parseInt(args[0]);
    }
    int[] h64 = testForCollisions(new hash64shift(), values);
    //int[] h32 = testForCollisions(new hash6432shift(), values);
    int[] h2 = testForCollisions(new simpleshift(), values);
    System.out.println("Max collisons:" + maxCollisions(h64) + " vs " + maxCollisions(h2));
    System.out.println("Total empties:" + emptySlots(h64) + " vs " + emptySlots(h2));
    System.out.println("Collision count: " + countCollisions(h64) + " vs " + countCollisions(h2));
  }
}
