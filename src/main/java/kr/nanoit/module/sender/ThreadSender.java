package kr.nanoit.module.sender;

import kr.nanoit.abst.ModuleProcess;
import kr.nanoit.db.auth.MessageService;
import kr.nanoit.domain.broker.InternalDataCarrier;
import kr.nanoit.domain.broker.InternalDataOutBound;
import kr.nanoit.domain.broker.InternalDataSender;
import kr.nanoit.domain.broker.InternalDataType;
import kr.nanoit.domain.message.MessageResult;
import kr.nanoit.domain.message.MessageStatus;
import kr.nanoit.domain.payload.*;
import kr.nanoit.dto.ClientMessageDto;
import kr.nanoit.exception.FindFailedException;
import kr.nanoit.exception.InsertFailedException;
import kr.nanoit.exception.UpdateFailedException;
import kr.nanoit.module.broker.Broker;
import kr.nanoit.module.inbound.socket.UserManager;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

@Slf4j
public class ThreadSender extends ModuleProcess {

    private final MessageService messageService;
    private InternalDataSender internalDataSender;
    private final UserManager userManager;


    public ThreadSender(Broker broker, String uuid, MessageService messageService, UserManager userManager) {
        super(broker, uuid);
        this.messageService = messageService;
        this.userManager = userManager;
    }

    @Override
    public void run() {
        try {
            this.flag = true;
            while (this.flag) {
                Object object = broker.subscribe(InternalDataType.SENDER);
                if (object != null && object instanceof InternalDataSender) {
                    internalDataSender = (InternalDataSender) object;

                    if (userManager.isExist(internalDataSender.UUID())) {
                        Send send = (Send) internalDataSender.getPayload().getData();

                        if (send == null || send.getContent().isEmpty() || send.getPhoneNum().isEmpty() || send.getName().isEmpty() || send.getCallback().isEmpty()) {
                            sendResult("SendMessage is null", internalDataSender, new Exception());
                        }
                        long agentId = userManager.getUserInfo(internalDataSender.UUID()).getAgent_id();
                        ClientMessageDto messageDto = makeMessage(send, agentId);
                        long id = messageService.insertClientMessage(messageDto.toEntity());
                        messageDto.setId(id);

                        if (id != 0) {
                            if (broker.publish(new InternalDataOutBound(internalDataSender.getMetaData(), new Payload(PayloadType.SEND_ACK, internalDataSender.getPayload().getMessageUuid(), new SendAck(send.getMessageNum(),MessageResult.SUCCESS))))) {
                                log.debug("[OUTBOUND]   SEND DATA TO OutBound => [TYPE : {} DATA : {}]", internalDataSender.getPayload().getType(), internalDataSender.getPayload());
                            } else {
                                log.error("[SENDER] @USER:{}] Broker Publish Error", internalDataSender.UUID());
                            }
                            if (broker.publish(new InternalDataCarrier(internalDataSender.getMetaData(), new Payload(PayloadType.SEND_ACK, internalDataSender.getPayload().getMessageUuid(), messageDto)))) {
                                log.debug("[OUTBOUND]   SEND DATA TO CARRIER => [TYPE : {} DATA : {}]", internalDataSender.getPayload().getType(), internalDataSender.getPayload());
                            } else {
                                log.error("[SENDER] @USER:{}] Broker Publish Carrier Error", internalDataSender.UUID());
                            }
                        }
                        if (!messageService.updateMessageStatus(id, MessageStatus.SENT)) {
                            log.error("[SENDER] @USER:{}] Broker Publish Error", internalDataSender.UUID());
                        }
                    }
                }
            }
        } catch (InsertFailedException e) {
            sendResult(e.getReason(), internalDataSender, e);
        } catch (FindFailedException e) {
            sendResult(e.getReason(), internalDataSender, e);
        } catch (UpdateFailedException e) {
            sendResult(e.getReason(), internalDataSender, e);
        } catch (Exception e) {
            e.printStackTrace();
            shoutDown();
        }

    }

    private void sendResult(String reason, InternalDataSender internalDataSender, Exception exception) {
        if (broker.publish(new InternalDataOutBound(internalDataSender.getMetaData(), new Payload(PayloadType.SEND_ACK, internalDataSender.getPayload().getMessageUuid(), new ErrorPayload(reason))))) {
            log.warn("[SENDER]   key = {} reason = {}", internalDataSender.getMetaData().getSocketUuid(), reason, exception);
        }
    }

    @Override
    public void shoutDown() {
        this.flag = false;
        log.warn("[SENDER]   THIS THREAD SHUTDOWN");
    }

    @Override
    public void sleep() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    private ClientMessageDto makeMessage(Send send, long agentId) {
        ClientMessageDto clientMessageDto = new ClientMessageDto();
        clientMessageDto.setId(0);
        clientMessageDto.setAgent_id(agentId);
        clientMessageDto.setType(PayloadType.SEND);
        clientMessageDto.setStatus(MessageStatus.RECEIVE);
        clientMessageDto.setSend_time(new Timestamp(System.currentTimeMillis()));
        clientMessageDto.setSender_num(send.getPhoneNum());
        clientMessageDto.setSender_callback(send.getCallback());
        clientMessageDto.setSender_name(send.getName());
        clientMessageDto.setContent(send.getContent());
        clientMessageDto.setCreated_at(new Timestamp(System.currentTimeMillis()));
        clientMessageDto.setLast_modified_at(new Timestamp(System.currentTimeMillis()));

        return clientMessageDto;
    }
}

//
//