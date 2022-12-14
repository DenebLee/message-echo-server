package kr.nanoit.module.inbound.socket;

import kr.nanoit.module.broker.Broker;
import kr.nanoit.module.inbound.thread.gateway.ReadStreamThread;
import kr.nanoit.module.inbound.thread.gateway.WriteStreamThread;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Getter
public class SocketResource {
    public final String uuid;
    private final Socket socket;
    private final LinkedBlockingQueue<String> writeBuffer;

    private final Thread readStreamThread;
    private final Thread writeStreamThread;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;
    private final InputStreamReader inputStreamReader;
    private final OutputStreamWriter outputStreamWriter;
    private boolean readThreadStatus = true;
    private boolean writeThreadStatus = true;
    @Setter
    public AtomicBoolean isAuthComplete = new AtomicBoolean(false);

    public SocketResource(Socket socket, Broker broker, UserManager userManager) throws IOException {
        this.uuid = UUID.randomUUID().toString();
        this.socket = socket;
        this.writeBuffer = new LinkedBlockingQueue<>();

        this.inputStreamReader = new InputStreamReader(socket.getInputStream());
        this.outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        this.bufferedReader = new BufferedReader(this.inputStreamReader);
        this.bufferedWriter = new BufferedWriter(this.outputStreamWriter);

        this.readStreamThread = new Thread(new ReadStreamThread(this::readThreadCleaner, broker, bufferedReader, uuid, isAuthComplete, userManager));
        readStreamThread.setName(uuid + "-read");
        this.writeStreamThread = new Thread(new WriteStreamThread(this::writeThreadCleaner, bufferedWriter, uuid, writeBuffer, isAuthComplete));
        writeStreamThread.setName(uuid + "-write");
        socket.setSoTimeout(100000);

    }

    public void serve() {
        readStreamThread.start();
        writeStreamThread.start();
    }

    public void write(String payload) {
        if (payload != null) {
            writeBuffer.offer(payload);
        }
    }

    public void readThreadCleaner(String calledClassName) {
        try {
            writeThreadStatus = false;
            writeStreamThread.interrupt();
            this.socket.shutdownInput();
            readThreadStatus = false;
        } catch (IOException e) {
            log.error("[@SOCKET:RESOURCE@] key={} SOCKET INPUT STREAM CLOSE FAILED", uuid, e);
        }
    }

    public void writeThreadCleaner(String calledClassName) {
        try {
            readThreadStatus = false;
            readStreamThread.interrupt();
            this.socket.shutdownOutput();
            writeThreadStatus = false;
        } catch (IOException e) {
            log.error("[@SOCKET:RESOURCE@] key={} SOCKET OUT STREAM CLOSE FAILED", uuid, e);
        }

    }

    public boolean isTerminated() {
        return readStreamThread.getState().equals(Thread.State.TERMINATED) && writeStreamThread.getState().equals(Thread.State.TERMINATED);
    }

    public void connectClose() throws IOException {
        this.socket.close();
    }

    public boolean isSocketInputStreamClose() {
        return socket.isInputShutdown();
    }


    public boolean isSocketOutputStreamClose() {
        return socket.isOutputShutdown();
    }

    public int getWriteBufferQueueSize() {
        return writeBuffer.size();
    }

    public boolean isSocketClose() {
        return this.socket.isClosed();
    }

}