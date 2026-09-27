package com.company.notification.mapper;

import com.company.notification.dto.NotificationRequest;
import com.company.notification.dto.NotificationResponse;
import com.company.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "expired", ignore = true)
    Notification toEntity(NotificationRequest request);

    @Mapping(target = "read", expression = "java(entity.getReadAt() != null)")
    NotificationResponse toResponse(Notification entity);

    void updateEntity(NotificationRequest request, @MappingTarget Notification entity);
}
