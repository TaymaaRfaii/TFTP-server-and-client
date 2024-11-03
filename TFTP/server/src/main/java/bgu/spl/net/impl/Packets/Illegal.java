package bgu.spl.net.impl.Packets;

public class Illegal extends Packet{
    public Illegal(){
        opcode=11;
    }
    @Override
    public byte[] encode() {
        byte[] bytesArr = new byte [2];
        bytesArr[0] = (byte)((opcode >> 8) & 0xFF);
        bytesArr[1] = (byte)(opcode & 0xFF);
        return bytesArr;
    }
}
