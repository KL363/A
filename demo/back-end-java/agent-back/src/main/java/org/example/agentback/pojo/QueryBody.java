package org.example.agentback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryBody {
    private String query;
    private int session_id;
    private Boolean stream;
}
