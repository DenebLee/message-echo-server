CREATE TABLE "member"
(
    "id"               BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    "username"         varchar(64) UNIQUE                                  NOT NULL,
    "email"            varchar(64)                                         NOT NULL,
    "password"         varchar(64)                                         NOT NULL,
    "created_at"       timestamptz                                         NOT NULL,
    "last_modified_at" timestamptz                                         NOT NULL
);

CREATE TABLE "agent"
(
    "id"               BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    "member_id"        bigint                                              NOT NULL,
    "access_list_id"   bigint                                              NOT NULL,
    "status"           varchar(32)                                         NOT NULL,
    "created_at"       timestamptz                                         NOT NULL,
    "last_modified_at" timestamptz                                         NOT NULL
);

CREATE TABLE "access_list"
(
    "id"      BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    "address" varchar(32)                                         NOT NULL
);

CREATE TABLE "agent_status"
(
    "status" varchar(32) UNIQUE NOT NULL
);

CREATE TABLE "message_type"
(
    "type" varchar(32) UNIQUE NOT NULL
);

CREATE TABLE "message_status"
(
    "status" varchar(32) UNIQUE NOT NULL
);

CREATE TABLE "client_message"
(
    "id"               BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    "agent_id"         bigint                                              NOT NULL,
    "type"             varchar(32)                                         NOT NULL,
    "status"           varchar(32)                                         NOT NULL,
    "send_time"        timestamptz                                         NOT NULL,
    "sender_num"       varchar(32)                                         NOT NULL,
    "sender_callback"  varchar(32)                                         NOT NULL,
    "sender_name"      varchar(64)                                         NOT NULL,
    "content"          varchar(255)                                        NOT NULL,
    "created_at"       timestamptz                                         NOT NULL,
    "last_modified_at" timestamptz                                         NOT NULL
);

CREATE TABLE "company_message"
(
    "id"                BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    "client_message_id" bigint                                              NOT NULL,
    "relay_company_id"  bigint                                              NOT NULL,
    "type"              varchar(32)                                          NOT NULL,
    "status"            varchar(32)                                          NOT NULL,
    "send_time"         timestamptz                                         NOT NULL,
    "sender_num"        varchar(32)                                         NOT NULL,
    "sender_callback"   varchar(32)                                         NOT NULL,
    "sender_name"       varchar(64)                                         NOT NULL,
    "content"           varchar(255)                                        NOT NULL,
    "created_at"        timestamptz                                         NOT NULL,
    "last_modified_at"  timestamptz                                         NOT NULL
);

CREATE TABLE "relay_company"
(
    "id"               BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    "created_at"       timestamptz                                         NOT NULL,
    "last_modified_at" timestamptz                                         NOT NULL
);

ALTER TABLE "agent"
    ADD FOREIGN KEY ("member_id") REFERENCES "member" ("id") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "agent"
    ADD FOREIGN KEY ("access_list_id") REFERENCES "access_list" ("id") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "agent"
    ADD FOREIGN KEY ("status") REFERENCES "agent_status" ("status") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "client_message"
    ADD FOREIGN KEY ("agent_id") REFERENCES "agent" ("id") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "client_message"
    ADD FOREIGN KEY ("type") REFERENCES "message_type" ("type") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "client_message"
    ADD FOREIGN KEY ("status") REFERENCES "message_status" ("status") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "company_message"
    ADD FOREIGN KEY ("client_message_id") REFERENCES "client_message" ("id") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "company_message"
    ADD FOREIGN KEY ("type") REFERENCES "message_type" ("type") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "company_message"
    ADD FOREIGN KEY ("status") REFERENCES "message_status" ("status") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "company_message"
    ADD FOREIGN KEY ("relay_company_id") REFERENCES "relay_company" ("id") ON DELETE RESTRICT ON UPDATE RESTRICT;
