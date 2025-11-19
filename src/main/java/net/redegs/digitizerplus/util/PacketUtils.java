package net.redegs.digitizerplus.util;

import java.io.*;

public class PacketUtils {
    public static byte[] Serialize(final Object obj) {
        /* Turns a `Serializable` object into a byte array */
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            out.flush();
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Object Deserialize(byte[] bytes) {
        /* Turns a byte array back into an object */
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        try (ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
