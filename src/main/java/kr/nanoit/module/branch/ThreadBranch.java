package kr.nanoit.module.branch;

import kr.nanoit.abst.NanoItThread;
import kr.nanoit.domain.broker.InternalDataBranch;
import kr.nanoit.domain.broker.InternalDataOutBound;
import kr.nanoit.domain.broker.InternalDataSender;
import kr.nanoit.domain.broker.InternalDataType;
import kr.nanoit.domain.payload.PayloadType;
import kr.nanoit.module.auth.Auth;
import kr.nanoit.module.broker.Broker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadBranch extends NanoItThread {

    private final Auth auth;

    public ThreadBranch(Broker broker, String uuid) {
        super(broker, uuid);
        this.auth = new Auth();
    }

    @Override
    public void execute() {
        try {
            Object object;
            flag = true;
            while (flag) {
                object = broker.subscribe(InternalDataType.BRANCH);
                if (object != null && object instanceof InternalDataBranch) {
//                    log.info("[BRANCH]   DATA INPUT => {}", object);
                    InternalDataBranch internalDataBranch = (InternalDataBranch) object;
                    PayloadType payloadType = internalDataBranch.getPayload().getType();

                    if (payloadType.equals(PayloadType.AUTHENTICATION)) {
                        auth.verification(internalDataBranch, broker);

                    } else if (payloadType.equals(PayloadType.SEND)) {
                        if (broker.publish(new InternalDataSender(internalDataBranch.getMetaData(), internalDataBranch.getPayload()))) {
//                            log.info("[BRANCH]   SEND DATA TO SENDER => [TYPE : {} DATA : {}]", internalDataBranch.getPayload().getType(), internalDataBranch.getPayload());
                        }

                    } else if (payloadType.equals(PayloadType.REPORT_ACK)) {
                        if (broker.publish(new InternalDataOutBound(internalDataBranch.getMetaData(), internalDataBranch.getPayload()))) {
                            log.info("[BRANCH]   REPORT_ACK DATA TO OutBound => [TYPE : {} DATA : {}]", internalDataBranch.getPayload().getType(), internalDataBranch.getPayload());
                        }

                    } else if (payloadType.equals(PayloadType.ALIVE)) {
                        if (broker.publish(new InternalDataOutBound(internalDataBranch.getMetaData(), internalDataBranch.getPayload()))) {
                            log.info("[BRANCH]   ALIVE DATA TO OutBound => [TYPE : {} DATA : {}]", internalDataBranch.getPayload().getType(), internalDataBranch.getPayload());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shoutDown() {
//        flag = false;
        Thread.interrupted();
        log.warn("[BRANCH   THIS THREAD SHUTDOWN]");
    }

    @Override
    public Thread.State getState() {
        return this.thread.getState();
    }

    @Override
    public void sleep() throws InterruptedException {
        Thread.sleep(500);
    }
}