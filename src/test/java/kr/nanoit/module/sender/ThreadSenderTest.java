package kr.nanoit.module.sender;

import kr.nanoit.db.DataBaseConfig;
import kr.nanoit.db.PostgreSqlDbcp;
import kr.nanoit.db.auth.MessageService;
import kr.nanoit.db.auth.MessageServiceImpl;
import kr.nanoit.domain.broker.*;
import kr.nanoit.domain.entity.AgentEntity;
import kr.nanoit.domain.entity.MemberEntity;
import kr.nanoit.domain.message.AgentStatus;
import kr.nanoit.domain.message.MessageResult;
import kr.nanoit.domain.message.MessageStatus;
import kr.nanoit.domain.payload.*;
import kr.nanoit.dto.ClientMessageDto;
import kr.nanoit.module.broker.Broker;
import kr.nanoit.module.broker.BrokerImpl;
import kr.nanoit.module.inbound.socket.SocketManager;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;


@Testcontainers
class ThreadSenderTest {
    private static DataBaseConfig dataBaseConfig;
    private ThreadSender threadSender;
    private static SocketManager socketManager;
    private Thread senderThread;
    private static Broker broker;
    private String uuid;
    private static MessageService messageService;
    private static PostgreSqlDbcp dbcp;

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:14.5-alpine")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    static void beforeAll() throws ClassNotFoundException, URISyntaxException, IOException {
        socketManager = mock(SocketManager.class);
        dataBaseConfig = new DataBaseConfig();
        dataBaseConfig.setIp(postgreSQLContainer.getHost())
                .setPort(postgreSQLContainer.getFirstMappedPort())
                .setDatabaseName(postgreSQLContainer.getDatabaseName())
                .setUsername(postgreSQLContainer.getUsername())
                .setPassword(postgreSQLContainer.getPassword());

        dbcp = new PostgreSqlDbcp(dataBaseConfig);
        messageService = new MessageServiceImpl(dbcp);
        broker = spy(new BrokerImpl(socketManager));
        dbcp.initSchema();

        // Table dependency data injection
        messageService.insertAccessList(2, "192.168.0.16");
        messageService.insertAgentStatus("CONNECTED", "DISCONNECTED");
        messageService.insertMessageType("AUTHENTICATION");
        messageService.insertMessageType("SEND");
        messageService.insertMessageType("SEND_ACK");
        messageService.insertMessageStatus("SENT", "RECEIVE");

        MemberEntity memberEntity = new MemberEntity(0, "?????????", "$2a$12$9aqZtS4tclIN.sq3/J8qGuavmarzH5q5.z0Qz.7coXzD1MLjf0zRG", "test@test.com", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
        messageService.insertUser(memberEntity);

        AgentEntity agentEntity = new AgentEntity(4, 1, 2, AgentStatus.DISCONNECTED, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
        messageService.insertAgent(agentEntity);
    }

    @BeforeEach
    void setUp() {

        this.uuid = UUID.randomUUID().toString();
        this.threadSender = spy(new ThreadSender(broker, uuid, messageService));
        this.senderThread = spy(new Thread(threadSender));
        senderThread.start();
    }

    @AfterEach
    void tearDown() {
        this.senderThread.interrupt();
    }

    @DisplayName("Broker?????? ?????? ?????? Send Data??? null ??? ?????? ?????? ????????? ????????? ??????")
    @Test
    void t1() throws InterruptedException {
        // given
        String uuid = UUID.randomUUID().toString();
        InternalDataSender expected = new InternalDataSender(new MetaData(uuid), new Payload(PayloadType.SEND, uuid, null));

        // when
        broker.publish(expected);

        // then
        Object object = broker.subscribe(InternalDataType.OUTBOUND);
        assertThat(object).isInstanceOf(InternalDataOutBound.class);
        InternalDataOutBound actual = (InternalDataOutBound) object;
        assertThat(actual.getMetaData().getSocketUuid()).isEqualTo(expected.getMetaData().getSocketUuid());
        assertThat(actual.getPayload().getType()).isEqualTo(PayloadType.SEND_ACK);
        assertThat(actual.getPayload().getMessageUuid()).isEqualTo(uuid);
        assertThat(actual.getPayload().getData()).isInstanceOf(ErrorPayload.class);
        ErrorPayload errorPayload = (ErrorPayload) actual.getPayload().getData();
        assertThat(errorPayload.getReason()).isEqualTo("SendMessage is null");
    }

    @DisplayName("client_message ???????????? ???????????? insert ??? ??? ????????? ?????? ?????? ?????? ????????? ????????? ??????")
    @Test
    void t2() throws InterruptedException {
        // given
        String uuid = UUID.randomUUID().toString();
        InternalDataSender expected = new InternalDataSender(new MetaData(uuid), new Payload(PayloadType.SEND, uuid, new Send(3, "010-4444-5555", "053-555-4444", "?????????", "?????????")));

        // when
        broker.publish(expected);

        // then
        Object object = broker.subscribe(InternalDataType.OUTBOUND);
        assertThat(object).isInstanceOf(InternalDataOutBound.class);
        InternalDataOutBound actual = (InternalDataOutBound) object;
        assertThat(actual.getMetaData().getSocketUuid()).isEqualTo(expected.getMetaData().getSocketUuid());
        assertThat(actual.getPayload().getType()).isEqualTo(PayloadType.SEND_ACK);
        assertThat(actual.getPayload().getMessageUuid()).isEqualTo(uuid);
        assertThat(actual.getPayload().getData()).isInstanceOf(ErrorPayload.class);
        ErrorPayload errorPayload = (ErrorPayload) actual.getPayload().getData();
        assertThat(errorPayload.getReason()).isEqualTo("ERROR: insert or update on table \"client_message\" violates foreign key constraint \"client_message_agent_id_fkey\"\n" +
                "  Detail: Key (agent_id)=(3) is not present in table \"agent\".");
    }

    @DisplayName("client_message ???????????? ???????????? ??????????????? insert ???????????? clientMessage id?????? return ????????? ??????")
    @Test
    void t3() throws InterruptedException {
        // given
        String uuid = UUID.randomUUID().toString();
        Send send = new Send(4, "010-4444-5555", "053-555-4444", "?????????", "?????????");
        InternalDataSender expected = new InternalDataSender(new MetaData(uuid), new Payload(PayloadType.SEND, uuid, send));

        // when
        broker.publish(expected);

        // then
        Object object = broker.subscribe(InternalDataType.CARRIER);
        assertThat(object).isInstanceOf(InternalDataCarrier.class);
        InternalDataCarrier actual = (InternalDataCarrier) object;
        assertThat(actual.getMetaData().getSocketUuid()).isEqualTo(expected.getMetaData().getSocketUuid());
        assertThat(actual.getPayload().getType()).isEqualTo(PayloadType.SEND_ACK);
        assertThat(actual.getPayload().getMessageUuid()).isEqualTo(uuid);
        assertThat(actual.getPayload().getData()).isInstanceOf(ClientMessageDto.class);
        ClientMessageDto clientMessageDto = (ClientMessageDto) actual.getPayload().getData();


        assertThat(clientMessageDto.getId()).isEqualTo(2);
        assertThat(clientMessageDto.getAgent_id()).isEqualTo(send.getAgent_id());
        assertThat(clientMessageDto.getSender_name()).isEqualTo(send.getSender_name());
        assertThat(clientMessageDto.getSender_callback()).isEqualTo(send.getSender_callback());
        assertThat(clientMessageDto.getSender_num()).isEqualTo(send.getSender_num());
        assertThat(clientMessageDto.getStatus()).isEqualTo(MessageStatus.RECEIVE);
        assertThat(clientMessageDto.getType()).isEqualTo(PayloadType.SEND);
    }

    @DisplayName("client_message ???????????? ???????????? ??????????????? insert ???????????? clientMessage Status ?????? SENT??? ????????? ???")
    @Test
    void t4() throws InterruptedException {
        // given
        String uuid = UUID.randomUUID().toString();
        Send send = new Send(4, "010-4444-5555", "053-555-4444", "?????????", "?????????");
        InternalDataSender expected = new InternalDataSender(new MetaData(uuid), new Payload(PayloadType.SEND, uuid, send));

        // when
        broker.publish(expected);

        // then
        Object object = broker.subscribe(InternalDataType.OUTBOUND);
        assertThat(object).isInstanceOf(InternalDataOutBound.class);
        InternalDataOutBound actual = (InternalDataOutBound) object;
        assertThat(actual.getPayload().getType()).isEqualTo(PayloadType.SEND_ACK);
        assertThat(actual.getPayload().getData()).isInstanceOf(SendAck.class);
        SendAck result = (SendAck) actual.getPayload().getData();


        assertThat(result.getResult()).isEqualTo(MessageResult.SUCCESS);
    }
}