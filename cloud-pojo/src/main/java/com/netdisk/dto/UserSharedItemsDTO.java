package com.netdisk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSharedItemsDTO {
    private List<Integer> itemIds;
    private Short expireType;
    private Integer accessLimit;

}
