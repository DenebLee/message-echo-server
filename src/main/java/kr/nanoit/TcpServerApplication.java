package kr.nanoit;


import kr.nanoit.abst.ModuleProcess;
import kr.nanoit.abst.ModuleProcessManagerImpl;
import kr.nanoit.db.DataBaseConfig;
import kr.nanoit.db.PostgreSqlDbcp;
import kr.nanoit.db.auth.MessageService;
import kr.nanoit.module.branch.ThreadBranch;
import kr.nanoit.module.broker.Broker;
import kr.nanoit.module.broker.BrokerImpl;
import kr.nanoit.module.carrier.ThreadCarrier;
import kr.nanoit.module.filter.ThreadFilter;
import kr.nanoit.module.inbound.socket.SocketManager;
import kr.nanoit.module.inbound.socket.ThreadTcpServer;
import kr.nanoit.module.inbound.socket.UserManager;
import kr.nanoit.module.mapper.ThreadMapper;
import kr.nanoit.module.outbound.ThreadOutBound;
import kr.nanoit.module.sender.ThreadSender;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class TcpServerApplication {
    public static int port = 12323;
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");

    public static void main(String[] args) throws ClassNotFoundException {

        DataBaseConfig dataBaseConfig = new DataBaseConfig()
                .setIp("192.168.0.64")
                .setPort(5432)
                .setDatabaseName("lee")
                .setUsername("lee")
                .setPassword("1234qwer");
        PostgreSqlDbcp dbcp = new PostgreSqlDbcp(dataBaseConfig);
        MessageService messageService = MessageService.createPostgreSqL(dbcp);
        SocketManager socketManager = new SocketManager();
        Broker broker = new BrokerImpl(socketManager);
        UserManager userManager = new UserManager(socketManager, messageService);

        new ThreadMapper(broker, getRandomUuid());
        new ThreadFilter(broker, getRandomUuid(), userManager);
        new ThreadBranch(broker, getRandomUuid(), messageService, userManager);
        new ThreadSender(broker, getRandomUuid(), messageService, userManager);
        new ThreadOutBound(broker, getRandomUuid(), socketManager, userManager);
        new ThreadCarrier(broker, getRandomUuid(), messageService);

        new ThreadTcpServer(socketManager, broker, port, getRandomUuid());

        Thread socketManagerThread = new Thread(socketManager);
        Thread userManagerThread = new Thread(userManager);

        ModuleProcessManagerImpl moduleProcessManagerImpl = ModuleProcess.moduleProcessManagerImpl;


        socketManagerThread.setDaemon(true);
        userManagerThread.setDaemon(true);
        socketManagerThread.start();
        userManagerThread.start();

        System.out.println("");
        System.out.println("==========================================================================================================================================================================================");
        System.out.println("                                                                    ECHO TEST SERVER START " + SIMPLE_DATE_FORMAT.format(new Date()));
        System.out.println("==========================================================================================================================================================================================");
        System.out.println("");

    }

    private static String getRandomUuid() {
        return UUID.randomUUID().toString();
    }

}