package com.hachimi.mamboaiplatform.ai.model.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StreamMessage {

    /**
     * 消息类型
     */
    private String type;
}
