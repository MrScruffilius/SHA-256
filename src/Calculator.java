import jdk.jfr.Unsigned;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Calculator {
    private final static int[] HASH_VALUES = {0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19};
    private final static int[] CONSTANT_VALUES = {0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
            0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
            0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
            0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
            0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
            0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
            0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
            0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};

    private static String calc(String input) {
        byte[] temp = input.getBytes(StandardCharsets.UTF_8);

        List<Byte> bytes = new ArrayList<>();
        //copy input as bytes to the dynamic list
        for (int i = 0; i < temp.length; i++) {
            bytes.add(temp[i]);
        }
        long inputLength = bytes.size() * 8;
        //append '10000000' byte
        bytes.add(new Byte((byte) -128));
        while ((bytes.size() + 8) % 64 != 0) {
            bytes.add((byte) 0);
        }

        //adds the size of the input as bits as an 64 integer
        for (int i = 56; i >= 0; i -= 8) {
            Long l = inputLength >> i;
            bytes.add(l.byteValue());
        }


        int[] output = HASH_VALUES.clone();
        int[] workingWith = bytes.stream().mapToInt(x -> Byte.toUnsignedInt(x)).toArray();
        int[] manipulated;
        for (int i = 0; i < (workingWith.length * 8) / 512; i++) {
            manipulated = loops(workingWith);


            for (int k = 0; k < 8; k++) {
                output[k] += manipulated[k];
            }
        }
        StringBuilder ret = new StringBuilder();

        for (int k = 0; k < 8; k++) {
            ret.append(Integer.toHexString(output[k]));
        }

        return ret.toString().toUpperCase();
    }

    private static int[] loops(int[] bytes) {
        if (bytes.length != 64)
            return null;

        //vorbereitung der 64 double words

        int[] dWords = new int[64];
        int[] hashValues = HASH_VALUES.clone();
        int[] constantValues = CONSTANT_VALUES.clone();
        int counter = 0;
        for (int i = 0; i < bytes.length; i += 4) {
            int additional = (bytes[i] << 24) | (bytes[i + 1] << 16) | (bytes[i + 2] << 8) | (bytes[i + 3]);
            dWords[counter] = additional;
            counter++;
        }
        for (int i = counter; i < 64; i++) {
            dWords[counter] = 0;
        }
        //CHUNK LOOP
        for (int i = 16; i < 64; i++) {
            dWords[i] = dWords[i - 16] + (((dWords[i - 15] >>> 7) ^ (dWords[i - 15] << 25)) ^ ((dWords[i - 15] >>> 18) ^ (dWords[i - 15] << 14)) ^ (dWords[i - 15] >>> 3)) + dWords[i - 7] + (((dWords[i - 2] >>> 17) ^ (dWords[i - 2] << 15)) ^ ((dWords[i - 2] >>> 19) ^ (dWords[i - 2] << 13)) ^ (dWords[i - 2] >>> 10));
        }

        //COMPRESSION LOOP
        for (int i = 0; i < 64; i++) {
            //temporäre Variablen
            int temp1 = hashValues[7] + (((hashValues[4] >>> 6) ^ (hashValues[4] << 26)) ^ ((hashValues[4] >>> 11) ^ (hashValues[4] << 21)) ^ ((hashValues[4] >>> 25) ^ (hashValues[4] << 7))) + ((hashValues[4] & hashValues[5]) ^ ((~hashValues[4]) & hashValues[6])) + constantValues[i] + dWords[i];
            int temp2 = (((hashValues[0] >>> 2) ^ (hashValues[0] << 30)) ^ ((hashValues[0] >>> 13) ^ (hashValues[0] << 19)) ^ ((hashValues[0] >>> 22) ^ (hashValues[0] << 10))) + ((hashValues[0] & hashValues[1]) ^ (hashValues[0] & hashValues[2]) ^ (hashValues[1] & hashValues[2]));

            hashValues[7] = hashValues[6];
            hashValues[6] = hashValues[5];
            hashValues[5] = hashValues[4];
            hashValues[4] = hashValues[3] + temp1;
            hashValues[3] = hashValues[2];
            hashValues[2] = hashValues[1];
            hashValues[1] = hashValues[0];
            hashValues[0] = temp1 + temp2;
        }

        return hashValues;
    }
}