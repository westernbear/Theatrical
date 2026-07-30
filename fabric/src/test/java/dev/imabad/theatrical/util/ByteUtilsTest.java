package dev.imabad.theatrical.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteUtilsTest {
    @Test
    void convertsLongAndChecksumsUnsignedBytes() {
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8},
                ByteUtils.longToBytes(0x0102030405060708L));
        assertEquals(256, Short.toUnsignedInt(ByteUtils.calculateChecksum(new byte[]{(byte) 0xFF, 1})));
    }
}
