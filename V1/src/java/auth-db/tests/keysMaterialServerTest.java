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
        byte[] msg = new byte[]{10, 20, 30};

        byte[] payload = MessageCodec.createPayload(msg);

        // first 4 bytes: big-endian length prefix
        assertEquals(3, MessageCodec.readLongAt(padTo8(intBytesOf(payload)), 0)); // see note below
    }

    // Simpler, more direct version of the length-prefix check (avoids the awkward reuse above)
    @Test
    void createPayload_lengthPrefixMatchesMessageLength() {
        byte[] msg = new byte[]{10, 20, 30};
        byte[] payload = MessageCodec.createPayload(msg);

        int prefixedLen = ((payload[0] & 0xFF) << 24) | ((payload[1] & 0xFF) << 16)
                         | ((payload[2] & 0xFF) << 8)  | (payload[3] & 0xFF);

        assertEquals(msg.length, prefixedLen);
        assertEquals(4 + msg.length, payload.length);

        byte[] extractedMsg = new byte[msg.length];
        System.arraycopy(payload, 4, extractedMsg, 0, msg.length);
        assertArrayEquals(msg, extractedMsg);
    }

    @Test
    void createConfirmationPayload_isSingleZeroByte() {
        byte[] payload = MessageCodec.createConfirmationPayload();

        assertEquals(1, payload.length);
        assertEquals(0, payload[0]);
    }

    // helper used only by the first (overcomplicated) createPayloads test above — see note
    private static byte[] intBytesOf(byte[] payload) {
        return new byte[]{payload[0], payload[1], payload[2], payload[3]};
    }
    private static byte[] padTo8(byte[] fourBytes) {
        byte[] eight = new byte[8];
        System.arraycopy(fourBytes, 0, eight, 4, 4);
        return eight;
    }
}