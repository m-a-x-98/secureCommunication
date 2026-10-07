import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class keysMaterialServerTest {

    @Test
    void testHelpers() throws Exception{
        // Int parsing
        int i = 109348;
        int value = 0;
        for (byte b : MessageCodec.intToByteArray(i)) {
            value = (value << 8) + (b & 0xFF);
        }
        assertEquals(i, value);
    }

    @Test
    void longToByteArray_and_readLongAt_roundTrip() {
        long original = 4_611_686_018_427_387_903L; // large value, exercises high bytes too
        byte[] encoded = MessageCodec.longToByteArray(original);

        assertEquals(8, encoded.length);
        assertEquals(original, MessageCodec.readLongAt(encoded, 0));
    }

    @Test
    void readLongAt_respectsOffset() {
        byte[] buf = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] encoded = MessageCodec.longToByteArray(42L);
        System.arraycopy(encoded, 0, buf, 2, 8); // place the value starting at offset 2

        assertEquals(42L, MessageCodec.readLongAt(buf, 2));
    }

    @Test
    void readMessageOffset_parsesUsernameAndAdvancesEndPos() {
        // [2 bytes len=3]['A','B','C']
        byte[] payload = new byte[]{0x00, 0x03, 'A', 'B', 'C'};

        MsgEntry result = MessageCodec.readMessageOffset(payload, 0);

        assertEquals("ABC", result.entry);
        assertEquals(5, result.end_pos); // 2 (length prefix) + 3 (entry bytes)
    }

    @Test
    void readMessageOffset_respectsNonZeroStartOffset() {
        // leading junk byte, then [2 bytes len=2]['h','i']
        byte[] payload = new byte[]{(byte) 0xFF, 0x00, 0x02, 'h', 'i'};

        MsgEntry result = MessageCodec.readMessageOffset(payload, 1);

        assertEquals("hi", result.entry);
        assertEquals(5, result.end_pos);
    }

    @Test
    void readMessageOffset_chainedFields_secondFieldStartsAtFirstEndPos() {
        // username: [len=1]['A'], then password: [len=2]['B','C']
        byte[] payload = new byte[]{0x00, 0x01, 'A', 0x00, 0x02, 'B', 'C'};

        MsgEntry username = MessageCodec.readMessageOffset(payload, 0);
        MsgEntry password = MessageCodec.readMessageOffset(payload, username.end_pos);

        assertEquals("A", username.entry);
        assertEquals("BC", password.entry);
        assertEquals(7, password.end_pos);
    }

    @Test
    void readMessageBytesOffset_returnsRawBytesUnaltered() {
        // includes a byte (0xFF) that would be lossy if decoded as UTF-8 text
        byte[] rawKeyMaterial = new byte[]{0x01, (byte) 0xFF, 0x00, 0x7E};
        byte[] payload = new byte[2 + rawKeyMaterial.length];
        payload[0] = 0x00;
        payload[1] = (byte) rawKeyMaterial.length;
        System.arraycopy(rawKeyMaterial, 0, payload, 2, rawKeyMaterial.length);

        MsgBytes result = MessageCodec.readMessageBytesOffset(payload, 0);

        assertArrayEquals(rawKeyMaterial, result.entry);
        assertEquals(2 + rawKeyMaterial.length, result.end_pos);
    }

    @Test
    void createPayloads() throws Exception{
        byte[] payload = new byte[]{0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01};

        // first 4 bytes: big-endian length prefix
        assertEquals(0x0101000001000000L, MessageCodec.readLongAt(payload, 0)); // see note below
        assertEquals(0x0100000100000000L, MessageCodec.readLongAt(payload, 1)); // see note below
        assertEquals(0x0000010000000001L, MessageCodec.readLongAt(payload, 2)); // see note below
    }

    @Test
    void createConfirmationPayload_isSingleZeroByte() {
        byte[] payload = MessageCodec.createConfirmationPayload();

        assertEquals(1, payload.length);
        assertEquals(0, payload[0]);
    }
}