package dev.jlkesh.java_telegram_bots.domains;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class FIleDomain {
    private String chatId;
    private String fileId;
    private String fileName;
    private LocalDateTime createdAt;
    private Integer rowsCount;

}
