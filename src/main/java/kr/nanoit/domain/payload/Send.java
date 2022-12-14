package kr.nanoit.domain.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.awt.*;
import java.sql.Time;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Send {
    private long agent_id;
    private String sender_num;
    private String sender_callback;
    private String sender_name;
    private String content;
}
