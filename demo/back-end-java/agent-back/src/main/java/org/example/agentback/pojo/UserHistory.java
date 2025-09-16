package org.example.agentback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserHistory {
    private int id;
    private int userId;
    private int historyId;
    private String role;
    private String content;
    private String askTime;
    private String avatar;
}
