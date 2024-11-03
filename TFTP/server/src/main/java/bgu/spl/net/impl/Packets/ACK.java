package bgu.spl.net.impl.Packets;

public class ACK extends Packet {
    private short numOfBlock;

    public ACK(short numOfBlock) {
        opcode=4;
        this.numOfBlock=numOfBlock;
    }
    public byte[] encode() {
        byte[] Bytes = new byte [4];
        Bytes[0] = (byte)((opcode >> 8) & 0xFF);
        Bytes[1] = (byte)(opcode & 0xFF);
        Bytes[2] = (byte)((numOfBlock >> 8) & 0xFF);
        Bytes[3] = (byte)(numOfBlock & 0xFF);
        return Bytes;
    }
}