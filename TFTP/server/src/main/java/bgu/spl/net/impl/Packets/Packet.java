package bgu.spl.net.impl.Packets;
public abstract class Packet {
    protected short opcode;
    public Packet() {}
    //Each packet class implements the encode() method to convert the packet data into its binary
    // representation according to the TFTP protocol specifications.
    public abstract byte[] encode();
    public short getOpcode(){
        return opcode;
    }

}