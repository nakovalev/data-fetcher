package io.github.kovalev.datafetcher.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class AttributeNode {
    @NonNull
    private String attribute;
    private List<AttributeNode> subGraph;
}
