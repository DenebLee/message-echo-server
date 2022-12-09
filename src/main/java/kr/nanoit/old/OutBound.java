package kr.nanoit.old;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.nanoit.domain.broker.InternalDataOutBound;
import kr.nanoit.domain.broker.InternalDataType;
import kr.nanoit.module.broker.Broker;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Client로 전송하는 모듈
 * - 컨슈머 ( 들어온 메시지를 소비만 하는 모듈 )
 * - MetaData에 세션 정보를 찾아서 SocketManager의 소켓리스트에서 검색
 * - 소켓에 실제로 Write 할수 있는 로직이 필요함
 */

@Slf4j
public class OutBound implements Process {
    private final Broker broker;
    private final ObjectMapper objectMapper;
    public boolean flag;
    private Instant start;
    private Instant finish;

    public OutBound(Broker broker) {
        this.broker = broker;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try {
            flag = true;

            while (flag) {
                Object object = broker.subscribe(InternalDataType.OUTBOUND);
                if (object != null && object instanceof InternalDataOutBound) {
//                    log.info("[OUTBOUND] DATA INPUT => {}", object);
                    String payload = toJSON(object);
                    switch (((InternalDataOutBound) object).getPayload().getType()) {

                        // ReportACK? SEND_ACK, ALIVE_ACK, BAD_SEND,AUTHENTICATION_ACK

                        case SEND_ACK:
                            broker.outBound(((InternalDataOutBound) object).getMetaData().getSocketUuid(), payload);
//                            log.info("[OUTBOUND]   TO READ-THREAD => [{}]", payload);
                            break;
                        case ALIVE:
                            break;
                        case BAD_SEND:
                            break;
                        case AUTHENTICATION:
                            break;

                    }
                }
            }
            finish = Instant.now();
        } catch (InterruptedException e) {
            flag = false;
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String toJSON(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(((InternalDataOutBound) object).getPayload());
    }

    @Override
    public String getUuid() {
        return UUID.randomUUID().toString().substring(0, 7);
    }

    @Override
    public boolean getFlag() {
        return this.flag;
    }

    @Override
    public long getRunningTime() {
        return Duration.between(start, finish).toMillis();
    }
}