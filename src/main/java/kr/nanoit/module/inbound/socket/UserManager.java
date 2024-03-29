package kr.nanoit.module.inbound.socket;

import kr.nanoit.db.auth.AuthenticaionStatus;
import kr.nanoit.db.auth.MessageService;
import kr.nanoit.domain.message.AgentStatus;
import kr.nanoit.dto.UserInfo;
import kr.nanoit.exception.UpdateFailedException;
import kr.nanoit.module.broker.Broker;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UserManager implements Runnable {

    private final Map<String, UserInfo> userResourceMap;
    private final SocketManager socketManager;
    private final MessageService messageService;

    public UserManager(SocketManager socketManager, MessageService messageService) {
        this.userResourceMap = new ConcurrentHashMap<>();
        this.socketManager = socketManager;
        this.messageService = messageService;
    }

    @Override
    public void run() {
        try {
            while (true) {
                for (Map.Entry<String, UserInfo> entry : userResourceMap.entrySet()) {
                    String uuid = socketManager.forwardUserMap.poll(1, TimeUnit.SECONDS);
                    if (uuid != null) {
                        unregisUser(uuid);
                        if (messageService.updateAgentStatus(entry.getValue().getAgent_id(), entry.getValue().getMemberId(), AgentStatus.DISCONNECTED, new Timestamp(System.currentTimeMillis()))) {
                            log.info("[@SOCKET-{}:USER-MANAGER@] CLIENT DISCONNECTED COMPLETE", entry.getKey());
                        }
                    }
                    Thread.sleep(700L);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean registUser(String uuid, UserInfo userInfo) {
        if (uuid == null) {
            return false;
        }
        return userResourceMap.put(uuid, userInfo) == null;
    }

    public void unregisUser(String uuid) {
        userResourceMap.remove(uuid);
    }

    public boolean isExist(String uuid) {
        return userResourceMap.containsKey(uuid);
    }

    public AuthenticaionStatus getAuthenticationStatus(String uuid) {
        return userResourceMap.get(uuid).getAuthenticaionStatus();
    }


    public UserInfo getUserInfo(String uuid) {
        return userResourceMap.get(uuid);
    }

    public int getUserResourceMapSize() {
        return userResourceMap.size();
    }

}
