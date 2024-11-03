package bgu.spl.net.impl.Packets;

public class DIRQ extends Packet{
    public DIRQ() {
        opcode =6;
    }

    @Override
    public byte[] encode() {
        byte[] Bytes = new byte [2];
        Bytes[0] = (byte)((opcode >> 8) & 0xFF);
        Bytes[1] = (byte)(opcode & 0xFF);
        return Bytes;
    }
}
