package dev.jlkesh.java_telegram_bots.dataserver;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FakerApplicationGenerateRequest {
    private String fileName;
    private FileType fileType;
    private Integer count;
    @Builder.Default
    private Set<Field> fields = new HashSet<>();
    private boolean flag;
}
