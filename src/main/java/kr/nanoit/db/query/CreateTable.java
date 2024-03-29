package kr.nanoit.db.query;

public final class CreateTable {

    public final static String createMemberTable = "CREATE TABLE IF NOT EXISTS member " +
            "(id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL, " +
            " username VARCHAR(64) UNIQUE NOT NULL, " +
            " email VARCHAR(64) NOT NULL, " +
            " password VARCHAR(64) NOT NULL, " +
            " created_at TIMESTAMPTZ NOT NULL, " +
            " last_modified_at TIMESTAMPTZ NOT NULL)";

    public final static String constraintsAgentAccess_list_id = "ALTER TABLE agent ADD FOREIGN KEY (access_list_id) " +
            "REFERENCES access_list (id) ";

    public final static String constraintsAgentMember_id = "ALTER TABLE agent ADD FOREIGN KEY (member_id) " +
            "REFERENCES member (id) ";

    public final static String constraintsAgentStatus = "ALTER TABLE agent ADD FOREIGN KEY (status) " +
            "REFERENCES agent_status (status) ";

    public final static String createAgentTable = " CREATE TABLE IF NOT EXISTS agent " +
            "(id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL," +
            "member_id BIGINT NOT NULL, " +
            "access_list_id BIGINT NOT NULL, " +
            "status VARCHAR(32) NOT NULL, " +
            "created_at TIMESTAMPTZ NOT NULL, " +
            "last_modified_at TIMESTAMPTZ NOT NULL)";

    public final static String createAccessListTable = "CREATE TABLE IF NOT EXISTS access_list" +
            "(id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL," +
            "address VARCHAR(32) NOT NULL)";

    public final static String createAgentStatusTable = "CREATE TABLE IF NOT EXISTS agent_status" +
            "(status VARCHAR(32) UNIQUE NOT NULL)";

    public final static String createMessageTypeTable = "CREATE TABLE IF NOT EXISTS message_type" +
            "(type VARCHAR(32) UNIQUE NOT NULL)";

    public final static String createMessageStatusTable = "CREATE TABLE IF NOT EXISTS message_status" +
            "(status VARCHAR(32) UNIQUE NOT NULL)";

    public final static String createClientMessageTable = "CREATE TABLE IF NOT EXISTS client_message" +
            "(id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL," +
            "agent_id BIGINT NOT NULL," +
            "type VARCHAR(32) NOT NULL," +
            "status VARCHAR(32) NOT NULL," +
            "send_time TIMESTAMPTZ NOT NULL," +
            "message_num BIGINT NOT NULL," +
            "sender_num VARCHAR(32) NOT NULL," +
            "sender_callback VARCHAR(32) NOT NULL," +
            "sender_name VARCHAR(64) NOT NULL," +
            "receive_time TIMESTAMPTZ NOT NULL," +
            "content VARCHAR(255) NOT NULL," +
            "created_at TIMESTAMPTZ NOT NULL," +
            "last_modified_at TIMESTAMPTZ NOT NULL)";

    public final static String createCompanyMessageTable = "CREATE TABLE IF NOT EXISTS company_message" +
            "(id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL," +
            "client_message_id BIGINT NOT NULL," +
            "relay_company_id bigint NOT NULL," +
            "type VARCHAR(32) NOT NULL," +
            "status VARCHAR(32) NOT NULL," +
            "send_time TIMESTAMPTZ NOT NULL," +
            "message_num BIGINT NOT NULL," +
            "sender_num VARCHAR(32) NOT NULL," +
            "sender_callback VARCHAR(32) NOT NULL," +
            "sender_name VARCHAR(64) NOT NULL," +
            "receive_time TIMESTAMPTZ NOT NULL," +
            "content VARCHAR(255) NOT NULL," +
            "created_at TIMESTAMPTZ NOT NULL," +
            "last_modified_at TIMESTAMPTZ NOT NULL)";

    public final static String createRelayCompanyTable = "CREATE TABLE IF NOT EXISTS relay_company" +
            "(id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL," +
            "created_at TIMESTAMPTZ NOT NULL," +
            "last_modified_at TIMESTAMPTZ NOT NULL)";
}
