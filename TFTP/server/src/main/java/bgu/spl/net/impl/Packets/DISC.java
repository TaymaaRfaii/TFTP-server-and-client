package bgu.spl.net.impl.Packets;

public class DISC extends Packet{
    public DISC(){
        opcode=10;
    }
    @Override
    public byte[] encode() {
        byte[] Bytes = new byte [2];
        Bytes[0] = (byte)((opcode >> 8) & 0xFF);
        Bytes[1] = (byte)(opcode & 0xFF);
        return Bytes;
    }
}
