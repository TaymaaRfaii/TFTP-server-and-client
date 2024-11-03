package bgu.spl.net.impl.Packets;

public class DATA extends Packet{
    private short Packetsize;
    private short numOfBlock;
    private byte[] data;
    public DATA(short Packetsize, short numOfBlock, byte[] data) {
        opcode =3;
        this.Packetsize=Packetsize;
        this.numOfBlock=numOfBlock;
        this.data=data;
    }
    public short getSize(){
        return Packetsize;
    }
    public short getNumOfBlock(){
        return numOfBlock;
    }
    public byte[] getData(){
        return data;
    }
    @Override
    public byte[] encode() {
        byte[] Bytes = new byte [data.length+6];
        Bytes[0] = (byte)((opcode >> 8) & 0xFF);
        Bytes[1] = (byte)(opcode & 0xFF);
        Bytes[2] = (byte)((Packetsize >> 8) & 0xFF);
        Bytes[3] = (byte)(Packetsize & 0xFF);
        Bytes[4] = (byte)((numOfBlock >> 8) & 0xFF);
        Bytes[5] = (byte)(numOfBlock & 0xFF);
        System.arraycopy(data, 0, Bytes, 6, Bytes.length - 6);
        return Bytes;
    }
}
