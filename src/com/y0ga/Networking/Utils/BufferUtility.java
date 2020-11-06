package com.y0ga.Networking.Utils;

public class BufferUtility {

    public static boolean IsValidBufferSize(int bufferSize) {

        return IsPowerOfTwo(bufferSize);

    }

    private static boolean IsPowerOfTwo(int x)
    {
        return (x != 0) && ((x & (x - 1)) == 0);
    }

}
