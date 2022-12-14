package kr.nanoit.module.mapper;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.events.ExceptionStatisticsEvent;
import kr.nanoit.abst.ModuleProcess;
import kr.nanoit.domain.broker.InternalDataFilter;
import kr.nanoit.domain.broker.InternalDataMapper;
import kr.nanoit.domain.broker.InternalDataType;
import kr.nanoit.domain.payload.Payload;
import kr.nanoit.module.broker.Broker;
import lombok.extern.slf4j.Slf4j;

// Mapper
@Slf4j
public class ThreadMapper extends ModuleProcess {

    private final ObjectMapper objectMapper;

    public ThreadMapper(Broker broker, String uuid) {
        super(broker, uuid);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        this.flag = true;
        while (this.flag) {
            Object object;
            try {
                object = broker.subscribe(InternalDataType.MAPPER);
                if (object != null && object instanceof InternalDataMapper) {
                    InternalDataMapper internalDataMapper = (InternalDataMapper) object;
                    Payload payload = objectMapper.readValue(internalDataMapper.getPayload(), Payload.class);

                    if (broker.publish(new InternalDataFilter(internalDataMapper.getMetaData(), payload))) {
                    }
                }
            } catch (InterruptedException | JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                e.printStackTrace();
                shoutDown();
            }
        }
    }

    @Override
    public void shoutDown() {
        this.flag = false;
        log.warn("[MAPPER   THIS THREAD SHUTDOWN]");
    }


    @Override
    public void sleep() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }
}
