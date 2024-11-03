package bgu.spl.net.srv;

public class OperationStateTracker {
    private boolean sendRRQ;
    private boolean sendDIRQ;
    private boolean writeRequestExpected;
    private int dataRRQCounter;
    private int dataDIRQCounter;
    public  OperationStateTracker(){
        sendRRQ=false;
        sendDIRQ=false;
        writeRequestExpected=true;
        dataRRQCounter=0;
        dataDIRQCounter=0;
    }
    
    public boolean isSendRRQ() {
        return sendRRQ;
    }
    public boolean isSendDirq() {
        return sendDIRQ;
    }
    public boolean isExpWRQ() {
        return writeRequestExpected;
    }

    //Getter: allow access to the state of the operations.
    public int getDataRRQCounter() {
        return dataRRQCounter;
    }
    public int getDataDIRQCounter() {
        return dataDIRQCounter;
    }
   //setter: used to modify the state of the operations.
    public void setSendRRQ(boolean sendRRQ) {
        this.sendRRQ = sendRRQ;
    }
    public void setSendDirq(boolean sendDirq) {
        this.sendDIRQ = sendDirq;
    }
    public void setExpWRQ(boolean expWRQ) {
        this.writeRequestExpected = expWRQ;
    }
//COUNTERS:used to keep track of the number of data packets expected for RRQ and DIRQ operations.
//increment counters
    public void incDataRRQCounter() {
        this.dataRRQCounter++;
    }
    public void incDataDIRQCounter() {
        this.dataDIRQCounter++;
    }
//decrement counters
    public void decDataRRQCounter() {
        this.dataRRQCounter--;
    }
    public void decDataDIRQCounter() {
        this.dataDIRQCounter--;

    }
}
